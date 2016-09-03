package alexandr_flyagin.gpslock;

public class LocationStore {
    private int id;
    private double longitude;
    private double latitude;
    private double altitude;
    private float accuracy;
    private float bearing;
    private float speed;
    private long time;

    // Не используется для локального хранения
    private String IMEI;

    public LocationStore() {
    }

    public LocationStore(int id, double longitude, double latitude, double altitude, float accuracy, float bearing, float speed, Long time) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.bearing = bearing;
        this.speed = speed;
        this.time = time;
    }

    @Override
    public String toString() {
        return " id = " + id +
                " longitude = " + longitude +
                " latitude = " + latitude +
                " altitude = " + altitude +
                " accuracy = " + accuracy +
                " bearing = " + bearing +
                " speed = " + speed +
                " time = " + time;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
