package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.EJBs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.util.logging.Logger;

@Singleton
@Startup

public class ConfigBean {
    @EJB
    private CustomerBean customerBean;
    @EJB
    private ManagerBean managerBean;
    @EJB
    private EmployeeBean employeeBean;
    @EJB
    private ProductTypeBean productTypeBean;


    private static final Logger logger = Logger.getLogger("ejbs.ConfigBean");

    @PostConstruct
    public void populateDB() {
        try {
            //Users creation
            customerBean.create("Joel", "123", "Joel", "joelsb@mail.com");
            customerBean.create("Tiago", "123", "Tiago", "tiago@mail.com");
            employeeBean.create("Jose", "123", "Jose", "jose@mail.com", "warehouse1");
            managerBean.create("Ana", "123", "Ana", "ana@mail.com", "office1");

            //Product-Type creation
            productTypeBean.create("Televisao LCD Samsung", false);
            productTypeBean.create("Gelado OLA - Corneto morango", true);

        }
        catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }
}
