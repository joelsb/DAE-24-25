package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs;

import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Volume;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class VolumeDTO {
    /*
    Atributes
    id: long
    sentDate: Date
    deliveredDate: Date
    package: PackageType
    products: List<ProductRecord>
    sensors: List<Sensor>
    order: Order (não detalhado no diagrama)
     */
    private long id;
    private Date sentDate;
    private Date deliveredDate;
    private long packageTypeId;
    private List<ProductRecordDTO> products;
    private List<SensorDTO> sensors;
    private long orderId;

    public VolumeDTO() {
    }

    public VolumeDTO(long id, Date sentDate, Date deliveredDate, long packageTypeId, long orderId) {
        this.id = id;
        this.sentDate = sentDate;
        this.deliveredDate = deliveredDate;
        this.packageTypeId = packageTypeId;
        this.products = new ArrayList<>();
        this.sensors = new ArrayList<>();
        this.orderId = orderId;
    }

    public static VolumeDTO from(Volume volume) {
        return new VolumeDTO(
                volume.getId(),
                volume.getSentDate(),
                volume.getDeliveredDate(),
                volume.getPack().getId(),
                volume.getOrder().getId()
        );
    }

    public static List<VolumeDTO> from(List<Volume> volumes) {
        return volumes.stream().map(VolumeDTO::from).collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public long getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(long packageTypeId) {
        this.packageTypeId = packageTypeId;
    }

    public List<ProductRecordDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductRecordDTO> products) {
        this.products = products;
    }

    public List<SensorDTO> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorDTO> sensors) {
        this.sensors = sensors;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
