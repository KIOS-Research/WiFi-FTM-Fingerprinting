public class LocDistance {
    private final double distance;
    private final String location;

    public LocDistance(double distance, String location) {
        this.distance = distance;
        this.location = location;
    }

    public double getDistance() {
        return distance;
    }

    public String getLocation() {
        return location;
    }
}