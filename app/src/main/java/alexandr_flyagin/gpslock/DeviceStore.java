package alexandr_flyagin.gpslock;

public class DeviceStore {
    private String IMEI;
    private String name;
    private String description;

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviceStore() {
    }

    public DeviceStore(String IMEI, String name, String description) {
        this.IMEI = IMEI;
        this.name = name;
        this.description = description;
    }
}
