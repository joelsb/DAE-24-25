package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.EJBs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.ProductType;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.SensorType;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyIllegalArgumentException;

import java.util.List;

@Stateless
public class ProductTypeBean {
    @PersistenceContext
    private EntityManager entityManager;
    @EJB
    private XLSXFileBean xlsxFileBean;

    public ProductType find(Long id) throws MyEntityNotFoundException {
        var productType = entityManager.find(ProductType.class, id);
        if (productType == null) {
            throw new MyEntityNotFoundException("ProductType with id: '" + id + "' not found");
        }
        return productType;
    }

    public ProductType findByName(String name) {
        return entityManager.createNamedQuery("getProductTypeByName", ProductType.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    public List<ProductType> findAll() {
        return entityManager.createNamedQuery("getAllProductTypes", ProductType.class).getResultList();
    }

    public ProductType findWithMandatorySensors(Long id) throws MyEntityNotFoundException {
        var productType = this.find(id);
        //Initialize the lazy collection
        Hibernate.initialize(productType.getMandatorySensors());
        return productType;
    }

    public List<ProductType> findAllWithMandatorySensors() {
        var productTypes = this.findAll();
        productTypes.forEach(productType -> Hibernate.initialize(productType.getMandatorySensors()));
        return productTypes;
    }

    public ProductType create(Long id, String name, Boolean mandatoryPackage) throws MyEntityExistsException, MyIllegalArgumentException {
        if(!entityManager.createNamedQuery("getProductTypeByName", ProductType.class)
                .setParameter("name", name)
                .getResultList().isEmpty()) {
            throw new MyEntityExistsException("ProductType with name: '" + name + "' already exists");
        }
        if(id ==null || id<=0) throw new MyIllegalArgumentException("ProductType id must be greater than 0");
        if(entityManager.find(ProductType.class, id) != null){
            throw new MyEntityExistsException("ProductType with id: '" + id + "' already exists");
        }
        if(name == null || name.isEmpty()) throw new MyIllegalArgumentException("ProductType name cannot be empty");
        if(mandatoryPackage == null) throw new MyIllegalArgumentException("ProductType mandatoryPackage cannot be null");
        var productType = new ProductType(id, name, mandatoryPackage);
        entityManager.persist(productType);
        xlsxFileBean.saveAllProductTypesToXlsx();
        return productType;
    }

    public ProductType update(Long id, String name, Boolean mandatoryPackage) throws MyEntityNotFoundException, MyEntityExistsException, MyIllegalArgumentException {
        if(id == null || id <= 0) throw new MyIllegalArgumentException("ProductType id must be greater than 0");
        var productType = this.find(id);
        if(!entityManager.createNamedQuery("getProductTypeByName", ProductType.class)
                .setParameter("name", name)
                .getResultList().isEmpty() && !productType.getName().equals(name)) {
            throw new MyEntityExistsException("ProductType with id: '" + name + "' already exists");
        }
        entityManager.lock(productType, LockModeType.OPTIMISTIC);
        if(mandatoryPackage != null) productType.setMandatoryPackage(mandatoryPackage);
        if(name != null) productType.setName(name);
        xlsxFileBean.saveAllProductTypesToXlsx();
        return productType;
    }

    public void addMandatorySensor(Long id, Long sensorTypeId) throws MyEntityNotFoundException, MyEntityExistsException, MyIllegalArgumentException {
        if(id == null || id <= 0) throw new MyIllegalArgumentException("ProductType id must be greater than 0");
        var productType = this.find(id);
        if (productType == null) throw new MyIllegalArgumentException("ProductType with id: '" + id + "' not found");
        var sensorType = entityManager.find(SensorType.class, sensorTypeId);
        if (sensorType == null) {
            throw new MyEntityNotFoundException("SensorType with id: '" + sensorTypeId + "' not found");
        }
        if(productType.getMandatorySensors().contains(sensorType)){
            throw new MyEntityExistsException("SensorType with id: '" + sensorTypeId + "' already exists in ProductType with id: '" + id + "'");
        }
        productType.addMandatorySensor(sensorType);
        if(!sensorType.getProductTypes().contains(productType)){
            sensorType.addProductType(productType);
        }
    }

    public ProductType removeMandatorySensor(Long id, Long sensorTypeId) throws MyEntityNotFoundException {
        var productType = this.find(id);
        var sensorType = entityManager.find(SensorType.class, sensorTypeId);
        if (sensorType == null) {
            throw new MyEntityNotFoundException("SensorType with id: '" + sensorTypeId + "' not found");
        }
        if(!productType.getMandatorySensors().contains(sensorType)){
            throw new MyEntityNotFoundException("SensorType with id: '" + sensorTypeId + "' not found in ProductType with id: '" + id + "'");
        }
        productType.removeMandatorySensor(sensorType);
        if(!sensorType.getProductTypes().contains(productType)){
            throw new MyEntityNotFoundException("SensorType with id: '" + sensorTypeId + "' not found in ProductType  with id: '" + id + "'");
        }
        sensorType.removeProductType(productType);
        return productType;
    }
}
