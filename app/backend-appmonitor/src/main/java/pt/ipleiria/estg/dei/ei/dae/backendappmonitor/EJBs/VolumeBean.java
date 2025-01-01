package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.EJBs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.PackageType;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Volume;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.ProductRecord;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Sensor;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Order;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityNotFoundException;
import java.util.Date;
import jakarta.ejb.*;
import jakarta.persistence.*;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.ProductRecordDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.SensorDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.VolumeCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.*;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityNotFoundException;


import java.util.Date;
import java.util.List;

@Stateless
public class VolumeBean {

    @PersistenceContext
    private EntityManager entityManager;

    public Volume create(Date sentDate , PackageType pack, List<ProductRecord> products,List<Sensor> sensors, Order order) {
        var volume = new Volume(sentDate,pack,order);
        pack.addVolume(volume);
        order.addVolume(volume);
        //Precorrer a lista products e set o volume ao product
        for (ProductRecord product : products) {
            product.setVolume(volume);
        }
        //Precorrer a lista sensors e set o volume ao sensor
        for (Sensor sensor : sensors) {
            sensor.setVolume(volume);
        }
        
        entityManager.persist(volume);
        return volume;
    }
    public Volume addSensor(long id, Sensor sensor) throws MyEntityNotFoundException {
        var volume = entityManager.find(Volume.class, id);
        if(volume == null) {
            throw new MyEntityNotFoundException("Volume (" + id + ") not found");
        }
        volume.addSensor(sensor);
        sensor.setVolume(volume);
        return volume;
    }
    public Volume addProduct(long id, ProductRecord productRecord) throws MyEntityNotFoundException {
        var volume = entityManager.find(Volume.class, id);
        if(volume == null) {
            throw new MyEntityNotFoundException("Volume (" + id + ") not found");
        }
        volume.addProduct(productRecord);
        productRecord.setVolume(volume);
        return volume;
    }

    public Volume find(long id) throws MyEntityNotFoundException {
        var volume = entityManager.find(Volume.class, id);
        if(volume == null) {
            throw new MyEntityNotFoundException("Volume with id: '" + id + "' not found");
        }
        return volume;
    }

    public List<Volume> findAll() {
        return entityManager.createNamedQuery("getAllVolumes", Volume.class).getResultList();
    }

    public Volume findWithSensorsProducts(long id) throws MyEntityNotFoundException {
        var volume = this.find(id);
        Hibernate.initialize(volume.getSensors());
        Hibernate.initialize(volume.getProducts());
        return volume;
    }

    public List<Volume> findAllWithSensorsProducts() {
        var volumes = entityManager.createNamedQuery("getAllVolumes", Volume.class).getResultList();
        volumes.forEach(volume -> {
            Hibernate.initialize(volume.getSensors());
            Hibernate.initialize(volume.getProducts());
        });
        return volumes;
    }

    public void addVolumeToOrder(VolumeCreateDTO volumeCreateDTO) throws MyEntityNotFoundException, MyEntityExistsException {
        var orderId = volumeCreateDTO.getOrderId();
        var order = entityManager.find(Order.class, orderId);
        if(order == null) {
            throw new MyEntityNotFoundException("Order with id: '" + orderId + "' not found");
        }
        var volume = this.create(volumeCreateDTO, order);
        order.addVolume(volume);
    }

    public Volume setDelivered(long id) throws MyEntityNotFoundException {
        var volume = entityManager.find(Volume.class, id);
        if(volume == null) {
            throw new MyEntityNotFoundException("Volume with id: '" + id + "' not found");
        }
        //data de hoje
        Date date = Date.from(new Date().toInstant());

        volume.setDeliveredDate(date);
        entityManager.persist(volume);
        return volume;
    }


    public Volume create(VolumeCreateDTO volumeCreateDTO, Order order) throws MyEntityNotFoundException, MyEntityExistsException {
        //TODO: Falta utilizar os beans aqui tbm, reutilizar codigo

        //Validate the volume
        validateVolumeCreation(volumeCreateDTO);

        var packageType = entityManager.find(PackageType.class, volumeCreateDTO.getPackageTypeId());
        var volume = new Volume(volumeCreateDTO.getSentDate(), packageType, order);
        packageType.addVolume(volume);
        order.addVolume(volume);

        entityManager.persist(volume);

        for (SensorDTO sensorDTO : volumeCreateDTO.getSensors()) {
            var sensorType = entityManager.find(SensorType.class, sensorDTO.getSensorTypeId());
            var sensor = new Sensor( sensorType, volume);
            volume.addSensor(sensor);

            // Persist each sensor
            entityManager.persist(sensor);
        }

        for (ProductRecordDTO productDTO : volumeCreateDTO.getProducts()) {
            var product = entityManager.find(ProductType.class, productDTO.getProductId());
            var productRecord = new ProductRecord(product, productDTO.getQuantity(), volume);
            volume.addProduct(productRecord);

            // Persist each product record
            entityManager.persist(productRecord);
        }
        //persist everything else
        entityManager.persist(volume);

        return volume;
    }

    public void validateVolumeCreation(VolumeCreateDTO volumeCreateDTO) throws MyEntityNotFoundException, MyEntityExistsException {
        //validate the Volume creation

        var packageType = entityManager.find(PackageType.class, volumeCreateDTO.getPackageTypeId());
        if (packageType == null) {
            throw new MyEntityNotFoundException("PackageType with id: '" + volumeCreateDTO.getPackageTypeId() + "' not found");
        }

        if (entityManager.find(Volume.class, volumeCreateDTO.getId()) != null) {
            throw new MyEntityExistsException("Volume with id: '" + volumeCreateDTO.getId() + "' already exists");
        }

        for (SensorDTO sensorDTO : volumeCreateDTO.getSensors()) {
            if (entityManager.find(SensorType.class, sensorDTO.getSensorTypeId()) == null) {
                throw new MyEntityNotFoundException("SensorType not found for id: '" + sensorDTO.getSensorTypeId() + "'");
            }
            if (entityManager.find(Sensor.class, sensorDTO.getId()) != null) {
                throw new MyEntityExistsException("Sensor with id: '" + sensorDTO.getId() + "' already exists");
            }
        }

        for (ProductRecordDTO productDTO : volumeCreateDTO.getProducts()) {
            if (entityManager.find(ProductType.class, productDTO.getProductId()) == null) {
                throw new MyEntityNotFoundException("ProductType not found for id: '" + productDTO.getProductId() + "'");
            }
        }
    }


}
