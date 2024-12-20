package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.EJBs;

import jakarta.ejb.Stateless;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Customer;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Order;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.User;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Volume;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityNotFoundException;

import java.util.Date;
import java.util.List;

@Stateless
public class OrderBean {
    @PersistenceContext
    private EntityManager entityManager;

    public Order create(Date createdDate, String customerUsername) throws MyEntityExistsException, MyEntityNotFoundException {


            var customer = entityManager.find(Customer.class, customerUsername);
            var order = new Order(createdDate,customer);
            customer.addOrder(order);
            //TODO: CREATE VOLUMES AND ASSOCIATE THEM TO THE ORDER
            entityManager.persist(order);
            return order;
    }

    public Order updateDeliveredDate(long id, Date deliveredDate) throws MyEntityNotFoundException {
        var order = entityManager.find(Order.class, id);
        if(order == null) {
            throw new MyEntityNotFoundException("Order (" + id + ") not found");
        }
        order.setDeliveredDate(deliveredDate);
        return order;
    }

    public Order addVolume(long id, Volume volume) throws MyEntityNotFoundException {
        var order = entityManager.find(Order.class, id);
        if(order == null) {
            throw new MyEntityNotFoundException("Order (" + id + ") not found");
        }
        else{
            order.addVolume(volume);
            volume.setOrder(order);
        }
        return order;
    }

    public Order find(long id) throws MyEntityNotFoundException {
        var order = entityManager.find(Order.class, id);
        if(order == null) {
            throw new MyEntityNotFoundException("Order (" + id + ") not found");
        }
        return order;
    }

    public List<Order> findAvailableOrders() {
        return entityManager.createNamedQuery("getAvailableOrders", Order.class).getResultList();
    }

    public List<Volume> findVolumes(long id) throws MyEntityNotFoundException {
        var order = entityManager.find(Order.class, id);
        if (order == null) {
            throw new MyEntityNotFoundException("Order (" + id + ") not found");
        }
        return entityManager.createNamedQuery("getVolumesByOrder", Volume.class)
                .setParameter("order", order)
                .getResultList();
    }

    public Order findWithVolumes(long id)throws MyEntityNotFoundException{
        var order = this.find(id);
        Hibernate.initialize(order.getVolumes());
        return order;
    }

    public List<Order> findAll() {
        return entityManager.createNamedQuery("getAllOrders", Order.class).getResultList();
    }
}
