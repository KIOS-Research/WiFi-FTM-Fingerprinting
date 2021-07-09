public class LogRecord {
    private final float value;
    private final String bssid;

    public LogRecord(float value, String bssid) {
        this.value = value;
        this.bssid = bssid;
    }

    public float getValue() {
        return value;
    }

    public String getBssid() {
        return bssid;
    }
}