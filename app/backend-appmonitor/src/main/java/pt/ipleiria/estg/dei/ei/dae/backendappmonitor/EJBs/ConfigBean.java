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
    @EJB
    private SensorTypeBean sensorTypeBean;
    @EJB
    private PackageTypeBean packageTypeBean;


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
            productTypeBean.create("Maquina Aparafusadora", false);

            //Sensor-Type creation
            sensorTypeBean.create("Temperature", "ºC", 30, 10);
            sensorTypeBean.create("Humidity", "%", 80, 20);
            sensorTypeBean.create("Luminosity", "Lumens", 1000, 500);

            //add a mandatory sensor to a product-type
            //add the mandatory sensor Temperature to the product-type Televisao LCD Samsung
            productTypeBean.addMandatorySensor(1L, 1L);
            //add the mandatory sensors Humidity and Temperature to the product-type Gelado OLA - Corneto morango
            productTypeBean.addMandatorySensor(2L, 1L);
            productTypeBean.addMandatorySensor(2L, 2L);

            //package-type creation
            packageTypeBean.create("Caixa Isotermica S");
            packageTypeBean.create("Caixa Isotermica M");
            packageTypeBean.create("Caixa Isotermica L");
            packageTypeBean.create("Caixa Isotermica XL");
            packageTypeBean.create("Caixa Cartao S");
            packageTypeBean.create("Caixa Cartao M");
            packageTypeBean.create("Caixa Cartao L");
            packageTypeBean.create("Caixa Cartao XL");

            //add a mandatory sensor to a package-type
            packageTypeBean.addMandatorySensor(1L, 1L);
            packageTypeBean.addMandatorySensor(1L, 2L);


        }
        catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }
}
