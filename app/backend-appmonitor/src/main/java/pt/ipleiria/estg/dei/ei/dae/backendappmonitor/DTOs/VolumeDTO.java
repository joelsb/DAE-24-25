package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs;

import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Volume;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class VolumeDTO {
    private long id;
    private Date sentDate;
    private Date deliveredDate;
    private String packageTypeName;
    private Long packageTypeId;
    private List<ProductRecordDTO> products = new ArrayList<>();
    private List<SensorDTO> sensors = new ArrayList<>();
    private Long orderId;

    public VolumeDTO() {
    }

    public VolumeDTO(long id, Date sentDate, Date deliveredDate, Long packageTypeId, Long orderId, String packageTypeName) {
        this.id = id;
        this.sentDate = sentDate;
        this.deliveredDate = deliveredDate;
        this.packageTypeId = packageTypeId;
        this.orderId = orderId;
        this.packageTypeName = packageTypeName;
    }

    public static VolumeDTO from(Volume volume) {
        return new VolumeDTO(
                volume.getId(),
                volume.getSentDate(),
                volume.getDeliveredDate(),
                null,
                null,
                volume.getPackageType().getName()
        );
    }

    public static VolumeDTO fromManager(Volume volume){
        return new VolumeDTO(
                volume.getId(),
                volume.getSentDate(),
                volume.getDeliveredDate(),
                null,
                volume.getOrder().getId(),
                null
        );
    }
    public static VolumeDTO fromEmployee(Volume volume){
        return new VolumeDTO(
                volume.getId(),
                volume.getSentDate(),
                volume.getDeliveredDate(),
                null,
                volume.getOrder().getId(),
                null
        );
    }

    public static List<VolumeDTO> fromEmployee(List<Volume> volumes){
        return volumes.stream().map(VolumeDTO::fromEmployee).collect(Collectors.toList());
    }

    public static List<VolumeDTO> fromManager(List<Volume> volumes){
        return volumes.stream().map(VolumeDTO::fromManager).collect(Collectors.toList());
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

    public String getPackageTypeName() {
        return packageTypeName;
    }

    public void setPackageTypeName(String packageTypeName) {
        this.packageTypeName = packageTypeName;
    }

    public Long getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(Long packageTypeId) {
        this.packageTypeId = packageTypeId;
    }

    public List<ProductRecordDTO> getProducts() {
        return  new ArrayList<>(products) ;
    }

    public void setProducts(List<ProductRecordDTO> products) {

        this.products = products;
    }

    public List<SensorDTO> getSensors() {
        return  new ArrayList<>(sensors);
    }

    public void setSensors(List<SensorDTO> sensors) {
        this.sensors = sensors;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }


}
