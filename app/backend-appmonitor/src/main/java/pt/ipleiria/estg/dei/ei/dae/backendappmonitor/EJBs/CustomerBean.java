package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.EJBs;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Customer;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Exceptions.MyEntityNotFoundException;

import java.util.List;


@Stateless
public class CustomerBean {
    @PersistenceContext
    private EntityManager entityManager;

    public Customer find(String username) {
        var customer = entityManager.find(Customer.class, username);
        if (customer == null) {
            throw new RuntimeException("User (" + username + ") not found");
        }
        return customer;
    }
    public List<Customer> findAll() {
        // remember, maps to: “SELECT a FROM User a ORDER BY a.name”
        return entityManager.createNamedQuery("getAllCustomers", Customer.class).getResultList();
    }

    public Customer findWithOrders(String username) throws MyEntityNotFoundException {
        var customer = this.find(username);
        //Initialize the lazy collection
        Hibernate.initialize(customer.getOrders());
        return customer;
    }

    public List<Customer> findAllWithOrders() {
        var customers = this.findAll();
        customers.forEach(customer -> Hibernate.initialize(customer.getOrders()));
        return customers;
    }

    public Customer create(String username, String password, String name, String email) throws MyEntityExistsException {
        if(entityManager.find(Customer.class, username) != null) {
            throw new MyEntityExistsException("Customer (" + username + ") already exists");
        }
        var customer = new Customer(
                username, password, name, email);
        entityManager.persist(customer);
        return customer;
    }

    public Customer update(String username, String name, String email) throws MyEntityNotFoundException {
        var customer = entityManager.find(Customer.class, username);
        if (customer == null) {
            throw new MyEntityNotFoundException("Customer (" + username + ") not found");
        }
        customer.setName(name);
        customer.setEmail(email);
        return customer;
    }

    public Customer updatePassword(String username, String password) throws MyEntityNotFoundException {
        var customer = entityManager.find(Customer.class, username);
        if (customer == null) {
            throw new MyEntityNotFoundException("Customer (" + username + ") not found");
        }
        customer.setPassword(password);
        return customer;
    }
}
