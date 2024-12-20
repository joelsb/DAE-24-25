package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "sensors")
@Entity
public class Sensor extends Versionable implements Serializable {
    /*
    Id é inserido pelo empregado no momento da criação
    sensorType-SensorType
    volume-Volume
    history-List<SensorRecord>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotNull
    @ManyToOne
    private SensorType sensorType;

    @ManyToOne
    private Volume volume;
    @NotNull
    @OneToMany(mappedBy = "sensor")
    private List<SensorRecord> history;

    public Sensor() {
    }

    public Sensor( SensorType sensorType, Volume volume) {
        this.sensorType = sensorType;
        this.volume = volume;
        this.history = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public Volume getVolume() {
        return volume;
    }

    public void setVolume(Volume volume) {
        this.volume = volume;
    }

    public List<SensorRecord> getHistory() {
        return history;
    }

    public void addRecord(SensorRecord record) {
        history.add(record);
    }

    public void removeRecord(SensorRecord record) {
        history.remove(record);
    }

    public void clearHistory() {
        history.clear();
    }

    public SensorRecord getLastRecord() {
        return history.get(history.size() - 1);
    }

    public boolean isWorking() {
        return getLastRecord() != null;
    }


}
