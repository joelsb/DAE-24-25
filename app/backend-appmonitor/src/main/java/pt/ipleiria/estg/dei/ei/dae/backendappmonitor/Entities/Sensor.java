package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Sensor extends Versionable{
    /*
    Id é inserido pelo empregado no momento da criação
    sensorType-SensorType
    volume-Volume
    history-List<SensorRecord>
     */
    @Id
    private long id;
    @NotNull
    private SensorType sensorType;
    @NotNull
    private Volume volume;
    @NotNull
    private List<SensorRecord> history;

    public Sensor() {
    }

    public Sensor(long id, SensorType sensorType, Volume volume) {
        this.id = id;
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
        return getLastRecord().isWorking();
    }


}
