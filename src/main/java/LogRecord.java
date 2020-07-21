public class LogRecord {
    private final int value;
    private final String bssid;

    public LogRecord(int value, String bssid) {
        this.value = value;
        this.bssid = bssid;
    }

    public int getValue() {
        return value;
    }

    public String getBssid() {
        return bssid;
    }
}