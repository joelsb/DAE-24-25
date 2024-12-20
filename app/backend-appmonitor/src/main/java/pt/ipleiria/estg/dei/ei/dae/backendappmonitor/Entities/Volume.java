package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "volumes")
@NamedQueries(
        {
                @NamedQuery(name = "getAllVolumes", query = "SELECT v FROM Volume v ORDER BY v.sentDate"),
                @NamedQuery(name = "getVolumesByOrder", query = "SELECT v FROM Volume v WHERE v.order = :order ORDER BY v.sentDate"),
        }
)
@Entity
public class Volume extends Versionable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotNull
    private Date sentDate;
    private Date deliveredDate;

    @ManyToOne
    private PackageType pack;

    @OneToMany(mappedBy = "volume", fetch = FetchType.EAGER)
    private List<ProductRecord> products;

    @OneToMany(mappedBy = "volume", fetch = FetchType.EAGER)
    private List<Sensor> sensors;

    @ManyToOne
    private Order order;

    public Volume() {
    }

    public Volume(Date sentDate, PackageType pack, List<ProductRecord> products, List<Sensor> sensors, Order order) {
        this.sentDate = sentDate;
        this.deliveredDate = null;
        this.pack = pack;
        this.products = products != null ? new ArrayList<>(products) : new ArrayList<>();
        this.sensors = sensors != null ? new ArrayList<>(sensors) : new ArrayList<>();
        this.order = order;
    }

    public long getId() {
        return id;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Date getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(Date deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public PackageType getPack() {
        return pack;
    }

    public void setPack(PackageType pack) {
        this.pack = pack;
    }

    public List<ProductRecord> getProducts() {
        return new ArrayList<>(products);
    }

    public void addProduct(ProductRecord product) {
        products.add(product);
    }

    public void removeProduct(ProductRecord product) {
        products.remove(product);
    }

    public List<Sensor> getSensors() {
        return new ArrayList<>(sensors);
    }

    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
