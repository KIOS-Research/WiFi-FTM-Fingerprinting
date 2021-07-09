import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ExtendedRadioMap {
    private String rssNaN, ftmNaN;
    private Double rssMin, rssMax;
    private Double ftmMin, ftmMax;

    private ArrayList<String> macAddressList;
    private HashMap<String, ArrayList<String>> locationHashMap;

    public ExtendedRadioMap(File inputFile) {
        try {
            macAddressList = new ArrayList<>();
            locationHashMap = new HashMap<>();

            if (!constructRadioMap(inputFile)) {
                throw new Exception("Invalid java.java.RadioMap File");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getRssMin() {
        return rssMin;
    }

    public Double getRssMax() {
        return rssMax;
    }

    public String getRssNaN() {
        return rssNaN;
    }

    public Double getFtmMin() {
        return ftmMin;
    }

    public Double getFtmMax() {
        return ftmMax;
    }

    public String getFtmNaN() {
        return ftmNaN;
    }

    private boolean constructRadioMap(File inputFile) {
        if (!inputFile.exists() || !inputFile.canRead()) {
            return false;
        }

        macAddressList.clear();
        locationHashMap.clear();

        String key;
        String line;
        String[] temp;

        ArrayList<String> values;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(inputFile));
            // Read the RSS (Min/Max/NaN)
            line = reader.readLine();

            temp = line.split(" ");

            if (!temp[1].equals("RSS"))
                return false;

            rssMin = Double.parseDouble(temp[2].trim());
            rssMax = Double.parseDouble(temp[3].trim());
            rssNaN = temp[4];

            // Read the FTM (Min/Max/NaN)
            line = reader.readLine();

            temp = line.split(" ");
            if (!temp[1].equals("FTM"))
                return false;

            ftmMin = Double.parseDouble(temp[2]);
            ftmMax = Double.parseDouble(temp[3]);
            ftmNaN = temp[4];

            line = reader.readLine();

            // Must exists
            if (line == null)
                return false;

            line = line.replace(", ", " ");
            temp = line.split(" ");

            final int index = 3;

            // Must have more than 4 fields
            if (temp.length < index)
                return false;

            // Store all Mac Addresses Heading Added
            macAddressList.addAll(Arrays.asList(temp).subList(index, temp.length));

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;

                line = line.replace(", ", " ");
                temp = line.split(" ");

                if (temp.length < index)
                    return false;

                key = temp[0] + " " + temp[1];
                values = new ArrayList<>(Arrays.asList(temp).subList(index - 1, temp.length));

                if (macAddressList.size() * 2 != values.size())
                    return false;

                locationHashMap.put(key, values);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    public ArrayList<String> getMacAddressList() {
        return macAddressList;
    }

    public HashMap<String, ArrayList<String>> getLocationHashMap() {
        return locationHashMap;
    }
}