import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class RouteFile {
    private String x, y;
    private Scanner scanner;
    private String timestamp;
    private ArrayList<LogRecord> latestStdScanList;

    public RouteFile(File inputFile) {
        try {
            if (!inputFile.exists() || !inputFile.canRead()) {
                throw new Exception("Invalid Log File");
            }

            scanner = new Scanner(inputFile);
            scanner.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        scanner.close();
    }

    public boolean eof() {
        return scanner.hasNextLine();
    }

    public String getCurrentLocation() {
        return x + " " + y;
    }

    public ArrayList<LogRecord> getExtendedLatestScanList() {
        String newScan = scanner.nextLine();

        latestStdScanList = new ArrayList<>();
        ArrayList<LogRecord> latestScanList = new ArrayList<>();

        x = newScan.split(" ")[1].trim();
        y = newScan.split(" ")[2].trim();

        while (!newScan.startsWith("#")) {
            latestScanList.add(new LogRecord(Integer.parseInt(newScan.split(" ")[4].trim()), newScan.split(" ")[3].trim()));
            latestScanList.add(new LogRecord(Float.parseFloat(newScan.split(" ")[5].trim()), newScan.split(" ")[3].trim()));
            latestStdScanList.add(new LogRecord(Float.parseFloat(newScan.split(" ")[6].trim()), newScan.split(" ")[3].trim()));

            if (scanner.hasNextLine())
                newScan = scanner.nextLine();
            else
                break;
        }
        return latestScanList;
    }

    public ArrayList<LogRecord> getLatestScanList(int value) {
        String newScan = scanner.nextLine();

        latestStdScanList = new ArrayList<>();
        ArrayList<LogRecord> latestScanList = new ArrayList<>();

        timestamp = newScan.split(" ")[0].trim();
        x = newScan.split(" ")[1].trim();
        y = newScan.split(" ")[2].trim();

        while (!newScan.startsWith("#")) {
            latestScanList.add(new LogRecord(Float.parseFloat(newScan.split(" ")[value].trim()), newScan.split(" ")[3].trim()));

            //distanceAP
            if (value == 4)
                latestStdScanList.add(new LogRecord(Float.parseFloat(newScan.split(" ")[value + 2].trim()), newScan.split(" ")[3].trim()));

            if (value == 5)
                latestStdScanList.add(new LogRecord(Float.parseFloat(newScan.split(" ")[value + 1].trim()), newScan.split(" ")[3].trim()));

            if (scanner.hasNextLine())
                newScan = scanner.nextLine();
            else
                break;
        }
        return latestScanList;
    }

    public ArrayList<LogRecord> getLatestStdScanList() {
        return latestStdScanList;
    }

    public String getTimestamp() {
        return timestamp;
    }
}