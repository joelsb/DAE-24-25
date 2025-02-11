package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.EJBs;

import jakarta.ejb.EJB;
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

import java.util.ArrayList;
import java.util.Date;

import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.ProductRecordDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.SensorDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.VolumeCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.*;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyIllegalArgumentException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Utils.VolumeValidationResult;


import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Stateless
public class VolumeBean {

    @PersistenceContext
    private EntityManager entityManager;
    @EJB
    private SensorBean sensorBean;
    @EJB
    private ProductRecordBean productRecordBean;

    @EJB
    private SensorTypeBean sensorTypeBean;

    private static final Logger logger = Logger.getLogger("VolumeBean");

    public List<Volume> findAvailableVolumes() {
        return entityManager.createNamedQuery("getAvailableVolumes", Volume.class).getResultList();
    }

    public List<Volume> findAllCustomerVolumes(String username) {
        return entityManager.createNamedQuery("getVolumesByCustomer", Volume.class)
                .setParameter("username", username)
                .getResultList();
    }

    public Volume find(long id) throws MyEntityNotFoundException {
        var volume = entityManager.find(Volume.class, id);
        if (volume == null) {
            throw new MyEntityNotFoundException("Volume with id: '" + id + "' not found");
        }
        return volume;
    }

    public List<Volume> findAll() {
        return entityManager.createNamedQuery("getAllVolumes", Volume.class).getResultList();
    }

    public Volume findWithSensors(long id) throws MyEntityNotFoundException {
        var volume = this.find(id);
        Hibernate.initialize(volume.getSensors());
        for (var sensor : volume.getSensors()) {
            Hibernate.initialize(sensor.getHistory());
        }
        return volume;
    }

    public Volume findWithProducts(long id) throws MyEntityNotFoundException {
        var volume = this.find(id);
        Hibernate.initialize(volume.getProducts());
        return volume;
    }

    public Volume findWithSensorsProducts(long id) throws MyEntityNotFoundException {
        var volume = this.find(id);
        Hibernate.initialize(volume.getSensors());
        Hibernate.initialize(volume.getProducts());
        for (var sensor : volume.getSensors()) {
            Hibernate.initialize(sensor.getHistory());
        }
        return volume;
    }

    public List<Volume> findAllWithSensorsProducts() {
        var volumes = this.findAll();
        volumes.forEach(volume -> {
            Hibernate.initialize(volume.getSensors());
            Hibernate.initialize(volume.getProducts());
        });
        return volumes;
    }

    public void addVolumeToOrder(VolumeCreateDTO volumeCreateDTO) throws MyEntityNotFoundException, MyEntityExistsException, MyIllegalArgumentException {
        var orderId = volumeCreateDTO.getOrderId();
        var order = entityManager.find(Order.class, orderId);
        if (order == null) {
            throw new MyEntityNotFoundException("Order with id: '" + orderId + "' not found");
        }
        if(order.getDeliveredDate() != null) {
            throw new MyEntityNotFoundException("Order with id: '" + orderId + "' already delivered");
        }
        VolumeValidationResult result = validateVolumeCreation(volumeCreateDTO);
        var volume = this.create(volumeCreateDTO, order, result);
        order.addVolume(volume);
    }

    public Volume deliver(long id) throws MyEntityNotFoundException, MyIllegalArgumentException {
        var volume = this.find(id);
        //data de hoje
        Date date = Date.from(new Date().toInstant());
        if (volume.getDeliveredDate() != null) {
            throw new MyIllegalArgumentException("Volume with id: '" + id + "' already delivered");
        }
        volume.setDeliveredDate(date);
        entityManager.persist(volume);
        return volume;
    }


    public Volume create(VolumeCreateDTO volumeCreateDTO, Order order, VolumeValidationResult result) throws MyEntityNotFoundException, MyEntityExistsException, MyIllegalArgumentException {

        // Extract validated entities
        var packageType = result.getPackageType();
        var sensorTypes = result.getSensorTypes();
        var productTypes = result.getProductTypes();

        // Create the Volume
        var volume = new Volume(volumeCreateDTO.getId(), volumeCreateDTO.getSentDate(), packageType, order);
        if(packageType != null) {
            packageType.addVolume(volume);
        }
        order.addVolume(volume);
        entityManager.persist(volume);

        // Use the validated SensorTypes and create sensors
        var sensorDTOs = volumeCreateDTO.getSensors();
        for (int i = 0; i < sensorDTOs.size(); i++) {
            var sensorDTO = sensorDTOs.get(i);
            var sensorType = sensorTypes.get(i); // Use validated SensorType
            var sensor = sensorBean.create(sensorDTO.getId(), sensorType.getId(), volume.getId());
        }

        // Use the validated ProductTypes and create product records
        var productDTOs = volumeCreateDTO.getProducts();
        for (int i = 0; i < productDTOs.size(); i++) {
            var productDTO = productDTOs.get(i);
            var productType = productTypes.get(i); // Use validated ProductType
            var productRecord = productRecordBean.create(productType.getId(), productDTO.getQuantity(), volume.getId());
        }

        return volume;
    }


    public VolumeValidationResult validateVolumeCreation(VolumeCreateDTO volumeCreateDTO) throws MyEntityNotFoundException, MyEntityExistsException, MyIllegalArgumentException {
        VolumeValidationResult result = new VolumeValidationResult();

        // Check if Volume already exists
        var existingVolume = entityManager.find(Volume.class, volumeCreateDTO.getId());
        if (existingVolume != null) {
            throw new MyEntityExistsException("Volume with id: '" + volumeCreateDTO.getId() + "' already exists");
        }
        if(volumeCreateDTO.getId()<=0) {
            throw new MyIllegalArgumentException("Volume id must be greater than 0");
        }
        // Validate Sensors
        for (SensorDTO sensorDTO : volumeCreateDTO.getSensors()) {
            var sensorType = entityManager.find(SensorType.class, sensorDTO.getSensorTypeId());
            if (sensorType == null) {
                throw new MyEntityNotFoundException("SensorType not found for id: '" + sensorDTO.getSensorTypeId() + "'");
            }
            result.addSensorType(sensorType);

            var existingSensor = entityManager.find(Sensor.class, sensorDTO.getId());
            if (existingSensor != null) {
                throw new MyEntityExistsException("Sensor with id: '" + sensorDTO.getId() + "' already exists");
            }
        }

        var mandatoryPackage = false;
        //Hash map (key=sensorTypeId, value=quantity)
        HashMap<Long, Integer> allMandatorySensors = new HashMap<>();

        // Validate Products
        for (ProductRecordDTO productDTO : volumeCreateDTO.getProducts()) {
            var productType = entityManager.find(ProductType.class, productDTO.getProductId());
            if (productType == null) {
                throw new MyEntityNotFoundException("ProductType not found for id: '" + productDTO.getProductId() + "'");
            }
            // Retrieve mandatory sensors from the product type
            for (SensorType mandatorySensor : productType.getMandatorySensors()) {
                Long sensorTypeId = mandatorySensor.getId();

                // If the sensorTypeId already exists in the map, increment its quantity by the quantity in the productRecord
                allMandatorySensors.merge(sensorTypeId, productDTO.getQuantity(), Integer::sum);
            }
            if (productType.isMandatoryPackage()) {
                mandatoryPackage = true;
            }
            result.addProductType(productType);
        }
        // Initialize mandatory sensors list

        if (mandatoryPackage) {
            // Retrieve and validate packageType
            var packageType = entityManager.find(PackageType.class, volumeCreateDTO.getPackageTypeId());
            if (packageType == null) {
                throw new MyEntityNotFoundException("PackageType not found for id: '" + volumeCreateDTO.getPackageTypeId() + "'");
            }
            result.setPackageType(packageType);

            // Add mandatory sensors from packageType to the hash map
            for (SensorType mandatorySensor : packageType.getMandatorySensors()) {
                Long sensorTypeId = mandatorySensor.getId();

                // Increment the count for this sensorTypeId in the map
                allMandatorySensors.merge(sensorTypeId, 1, Integer::sum);
            }
        }

        // Validate mandatory sensors using the helper method
        validateMandatorySensors(result, allMandatorySensors);

        return result;
    }

    // Extracted helper method for clarity and reusability

    private void validateMandatorySensors(VolumeValidationResult result, HashMap<Long, Integer> mandatorySensors) throws MyEntityNotFoundException {
        // Check for missing or insufficient sensors
        var missingSensors = mandatorySensors.entrySet().stream()
                .filter(entry -> {
                    var sensorTypeId = entry.getKey(); // SensorTypeId from the mandatory sensors map
                    var requiredCount = entry.getValue(); // Required quantity of this sensor
                    var actualCount = result.getSensorTypes().stream()
                            .filter(sensor -> sensor.getId()==sensorTypeId) // Match by sensorTypeId
                            .count();
                    return actualCount < requiredCount; // Check if the count is insufficient
                })
                .map(entry -> "<br>{ SensorTypeName: '" + entityManager.find(SensorType.class, entry.getKey()).getName()  + "' " +
                        ", Actual: '" + result.getSensorTypes().stream().filter(sensor -> sensor.getId()==entry.getKey()).count() + "' " +
                        ", Required: '" + entry.getValue() + "' }<br>")

                .collect(Collectors.toList());

        // Throw an exception if there are missing sensors
        if (!missingSensors.isEmpty()) {
            throw new MyEntityNotFoundException("The following sensors are missing or insufficient in quantity: " + missingSensors);
        }
    }
}

