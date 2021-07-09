package radioMapBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Builder {

    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir") + "/src/main/resources/";

        String radioMapFile = workingDirectory + "radioMap";
        String radioMapFolder = workingDirectory + "radioMaps/";

        int rssDefaultNaNValue = -110;
        int ftmDefaultNaNValue = 100;
        int stdDefaultNaNValue = 1;

        RadioMap rss = new RadioMap(radioMapFile, "rssRadioMap", radioMapFolder, 4, rssDefaultNaNValue);
        RadioMap ftm = new RadioMap(radioMapFile, "ftmRadioMap", radioMapFolder, 5, ftmDefaultNaNValue);
        RadioMap std = new RadioMap(radioMapFile, "stdRadioMap", radioMapFolder, 6, stdDefaultNaNValue);

        rss.createRadioMap();
        ftm.createRadioMap();
        std.createRadioMap();

        int first = 0;

        ArrayList<String> macKeys = new ArrayList<>();
        HashMap<String, ArrayList<Float>> macAddressMap;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(radioMapFolder + "/exdRadioMap", false);

            fileOutputStream.write(("# RSS " + rss.minValue + " " + rss.maxValue + " " + minMaxNormalization(rssDefaultNaNValue, rss.minValue, rss.maxValue) + "\n").getBytes());
            fileOutputStream.write(("# FTM " + ftm.minValue + " " + ftm.maxValue + " " + minMaxNormalization(ftmDefaultNaNValue, ftm.minValue, ftm.maxValue) + "\n").getBytes());
            fileOutputStream.write("# X, Y, ".getBytes());

            for (Map.Entry<String, HashMap<Integer, ArrayList<Object>>> entry : rss.newRadioMap.entrySet()) {
                for (Map.Entry<Integer, ArrayList<Object>> entry1 : entry.getValue().entrySet()) {
                    macAddressMap = RadioMap.uncheckedCast(entry1.getValue().get(0));
                    for (Map.Entry<String, ArrayList<Float>> entry2 : macAddressMap.entrySet()) {
                        String macAddress = entry2.getKey();
                        if (!macKeys.contains(macAddress.toLowerCase())) {
                            macKeys.add(macAddress.toLowerCase());
                            if (first == 0) {
                                fileOutputStream.write(macAddress.toLowerCase().getBytes());
                            } else {
                                fileOutputStream.write((", " + macAddress.toLowerCase()).getBytes());
                            }
                            first += 1;
                        }
                    }
                }
            }

            String x_y;
            int count = 0;
            int maxValues;

            for (Map.Entry<String, HashMap<Integer, ArrayList<Object>>> entry : rss.newRadioMap.entrySet()) {
                x_y = entry.getKey();
                for (Map.Entry<Integer, ArrayList<Object>> entry1 : entry.getValue().entrySet()) {
                    maxValues = 0;
                    macAddressMap = RadioMap.uncheckedCast(entry1.getValue().get(0));
                    for (Map.Entry<String, ArrayList<Float>> entry2 : macAddressMap.entrySet()) {
                        ArrayList<Float> values = entry2.getValue();
                        if (values.size() > maxValues) {
                            maxValues = values.size();
                        }
                    }

                    if (count == 0)
                        fileOutputStream.write("\n".getBytes());

                    for (int i = 0; i < maxValues; i++) {
                        fileOutputStream.write(x_y.getBytes());
                        for (String macKey : macKeys) {
                            float rssValue = rssDefaultNaNValue;
                            float ftmValue = ftmDefaultNaNValue;
                            if (macAddressMap.containsKey(macKey.toLowerCase())) {
                                rssValue = macAddressMap.get(macKey.toLowerCase()).get(i);
                                HashMap<String, ArrayList<Float>> ftmMap = RadioMap.uncheckedCast(ftm.newRadioMap.get(entry.getKey()).get(0).get(0));
                                ftmValue = ftmMap.get(macKey.toLowerCase()).get(i);
                            }
                            fileOutputStream.write((", " + minMaxNormalization(rssValue, rss.minValue, rss.maxValue) + ", " + minMaxNormalization(ftmValue, ftm.minValue, ftm.maxValue)).getBytes());
                        }
                        fileOutputStream.write("\n".getBytes());
                    }
                    count += 1;
                }
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double minMaxNormalization(double x, double min, double max) {
        return (x - min) / (max - min);
    }
}