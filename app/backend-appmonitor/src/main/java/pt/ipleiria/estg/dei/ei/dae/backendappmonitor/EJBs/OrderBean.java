package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.EJBs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.VolumeCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Customer;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Order;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Volume;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs.OrderCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyIllegalArgumentException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Utils.OrderValidationResult;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Utils.VolumeValidationResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Stateless
public class OrderBean {
    @PersistenceContext
    private EntityManager entityManager;
    @EJB
    private VolumeBean volumeBean;

    private static final Logger logger = Logger.getLogger("ejbs.OrderBean");

    public Order find(long id) throws MyEntityNotFoundException {
        var order = entityManager.find(Order.class, id);
        if (order == null) {
            throw new MyEntityNotFoundException("Order with id: '" + id + "' not found");
        }
        return order;
    }

    public List<Order> findAvailableOrdersWithVolumes() {
        var orders = entityManager.createNamedQuery("getAvailableOrders", Order.class).getResultList();
        for (Order order : orders) {
            Hibernate.initialize(order.getVolumes());
        }
        return orders;
    }

    public List<Order> findAll() {
        var orders = entityManager.createNamedQuery("getAllOrders", Order.class).getResultList();
        for (Order order : orders) {
            Hibernate.initialize(order.getVolumes());
        }
        return orders;
    }

    public List<Order> findAllCustomerOrders(String username) {
        //@NamedQuery(name = "getOrdersByCustomer", query = "SELECT o FROM Order o WHERE o.customer.username = :username ORDER BY o.createdDate"),
        var orders = entityManager.createNamedQuery("getOrdersByCustomer", Order.class)
                .setParameter("username", username)
                .getResultList();
        for (Order order : orders) {
            Hibernate.initialize(order.getVolumes());
        }
        return orders;
    }

    public Order findWithVolumes(long id) throws MyEntityNotFoundException {
        var order = this.find(id);
        Hibernate.initialize(order.getVolumes());
        return order;
    }

    public Order findWithVolumesProductsSensors(long id) throws MyEntityNotFoundException {
        var order = this.findWithVolumes(id);
        for (Volume volume : order.getVolumes()) {
            Hibernate.initialize(volume.getProducts());
            Hibernate.initialize(volume.getSensors());
        }
        return order;
    }

    public Order create(OrderCreateDTO orderCreateDTO) throws MyEntityExistsException, MyEntityNotFoundException, MyIllegalArgumentException {
        // Validar entrada e dependências
        OrderValidationResult result = validateOrderCreation(orderCreateDTO);

        // Criar a entidade Order
        var customer = result.getCustomer();
        var order = new Order(orderCreateDTO.getId(), orderCreateDTO.getCreatedDate(), customer);
        customer.addOrder(order);
        entityManager.persist(order);

        // Create the volumeValitationResult
        VolumeValidationResult volumeResult = new VolumeValidationResult(result.getPackageType(), result.getSensorTypes(), result.getProductTypes());

        volumeBean.create(orderCreateDTO.getVolume(), order, volumeResult);

        return order;
    }



    private OrderValidationResult validateOrderCreation(OrderCreateDTO orderCreateDTO) throws MyEntityExistsException, MyEntityNotFoundException, MyIllegalArgumentException {

        // Check if the order already exists
        if (entityManager.find(Order.class, orderCreateDTO.getId()) != null) {
            throw new MyEntityExistsException("Order with id: '" + orderCreateDTO.getId() + "' already exists");
        }
        if(orderCreateDTO.getId()<=0) {
            throw new MyIllegalArgumentException("Order id must be greater than 0");
        }
        // Validate customer
        var customer = entityManager.find(Customer.class, orderCreateDTO.getCustomerUsername());
        if (customer == null) {
            throw new MyEntityNotFoundException("Customer with username: '" + orderCreateDTO.getCustomerUsername() + "' not found");
        }

        // Validate volume using VolumeBean
        var volumeDTO = orderCreateDTO.getVolume();
        VolumeValidationResult volumeValidationResult = volumeBean.validateVolumeCreation(volumeDTO);

        // Return validated entities wrapped in OrderValidationResult
        return new OrderValidationResult(customer, volumeValidationResult.getPackageType(), volumeValidationResult.getSensorTypes(), volumeValidationResult.getProductTypes());
    }


    public Order updateDeliveredDate(long id, Date deliveredDate) throws MyIllegalArgumentException, MyEntityNotFoundException {
        var order = this.find(id);
        if(order.getDeliveredDate() == null) {
            throw new MyIllegalArgumentException("Order with id: '" + id + "' not delivered yet");
        }
        entityManager.lock(order, LockModeType.OPTIMISTIC);
        order.setDeliveredDate(deliveredDate);
        return order;
    }

    public Order deliver(long id) throws MyEntityNotFoundException, MyEntityExistsException {
        var order = this.find(id);
        Date date = Date.from(new Date().toInstant());
        var volumesIds = new ArrayList<>();
        for (Volume volume : order.getVolumes()) {
            if (volume.getDeliveredDate() == null) {
                volumesIds.add(volume.getId());
            }
        }
        if(!volumesIds.isEmpty()) {
            throw new MyEntityExistsException("Order with id: '" + id + "' has volumes not delivered with the following IDs: (" + volumesIds.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")");
        }
        order.setDeliveredDate(date);
        entityManager.persist(order);
        return order;

    }

}
