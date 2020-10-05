package radioMapBuilder;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RadioMap {
    private final String inputFile;
    private final String outputFile;
    private final String radioMapFolder;

    private final int readValue;
    private final int defaultNaNValue;

    public double minValue;
    public double maxValue;
    public HashMap<String, HashMap<Integer, ArrayList<Object>>> newRadioMap = new HashMap<>();

    public RadioMap(String inputFile, String outputFile, String radioMapFolder, int readValue, int defaultNaNValue) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.radioMapFolder = radioMapFolder;

        this.readValue = readValue;
        this.defaultNaNValue = defaultNaNValue;

        minValue = Double.MAX_VALUE;
        maxValue = Double.MIN_VALUE;
    }

    public void createRadioMap() {
        parseInputFileToRadioMap(new File(inputFile));
        writeRadioMap();
    }

    private void writeRadioMap() {
        DecimalFormat decimalFormat = new DecimalFormat("###.#");
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');

        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        HashMap<String, ArrayList<Integer>> macAddressMap;

        File radioMap = new File(radioMapFolder);
        if (!radioMap.exists() && !radioMap.mkdirs()) {
            System.out.println("Error creating radioMap folder");
            return;
        }

        File radioMapFile = new File(radioMapFolder + File.separatorChar + outputFile);

        if (newRadioMap.isEmpty()) return;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(radioMapFile, false);

            int count = 0;
            int first = 0;
            int maxValues;

            String x_y;
            String header = "# X, Y, ";
            String nanValue = "# NaN " + defaultNaNValue + "\n";

            fileOutputStream.write(nanValue.getBytes());
            fileOutputStream.write(header.getBytes());

            ArrayList<String> macKeys = new ArrayList<>();

            for (Map.Entry<String, HashMap<Integer, ArrayList<Object>>> entry : newRadioMap.entrySet()) {
                for (Map.Entry<Integer, ArrayList<Object>> entry1 : entry.getValue().entrySet()) {
                    macAddressMap = uncheckedCast(entry1.getValue().get(0));
                    for (Map.Entry<String, ArrayList<Integer>> entry2 : macAddressMap.entrySet()) {
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

            for (Map.Entry<String, HashMap<Integer, ArrayList<Object>>> entry : newRadioMap.entrySet()) {
                x_y = entry.getKey();
                for (Map.Entry<Integer, ArrayList<Object>> entry1 : entry.getValue().entrySet()) {
                    maxValues = 0;
                    macAddressMap = uncheckedCast(entry1.getValue().get(0));
                    for (Map.Entry<String, ArrayList<Integer>> entry2 : macAddressMap.entrySet()) {
                        ArrayList<Integer> values = entry2.getValue();
                        if (values.size() > maxValues) {
                            maxValues = values.size();
                        }
                    }
                    if (count == 0) {
                        fileOutputStream.write("\n".getBytes());
                    }
                    for (int i = 0; i < maxValues; i++) {
                        fileOutputStream.write(x_y.getBytes());
                        for (String macKey : macKeys) {
                            int value;
                            if (macAddressMap.containsKey(macKey.toLowerCase())) {
                                if (i >= macAddressMap.get(macKey.toLowerCase()).size() && macAddressMap.get(macKey.toLowerCase()).size() < maxValues) {
                                    value = defaultNaNValue;
                                } else {
                                    value = macAddressMap.get(macKey.toLowerCase()).get(i);
                                }
                            } else {
                                value = defaultNaNValue;
                            }
                            if (readValue == 6 && value == 0) {
                                value = defaultNaNValue;
                            }
                            fileOutputStream.write((", " + decimalFormat.format(value)).getBytes());

                            if (value != defaultNaNValue) {
                                if (value > maxValue)
                                    maxValue = value;

                                if (value < minValue)
                                    minValue = value;
                            }
                        }
                        fileOutputStream.write("\n".getBytes());
                    }
                    count += 1;
                }
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println((radioMapFile.delete()) ? "File deleted" : "File not deleted");
        }
    }

    private void parseInputFileToRadioMap(File inputFile) {
        ArrayList<Integer> values;
        ArrayList<Object> orientationList;
        HashMap<String, ArrayList<Integer>> macAddressMap;

        if (!authenticateInputFile(inputFile)) return;

        try {
            int value;
            String key;
            String line;
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            line = bufferedReader.readLine();
            while (line != null) {
                if (!(line.startsWith("#") || line.trim().isEmpty())) {
                    line = line.replace(", ", " ");
                    String[] temp = line.split(" ");

                    value = Integer.parseInt(temp[readValue]);
                    key = temp[1] + ", " + temp[2];

                    HashMap<Integer, ArrayList<Object>> orientationLists = newRadioMap.get(key);
                    if (orientationLists == null) {
                        orientationLists = new HashMap<>(Math.round(4));

                        orientationList = new ArrayList<>(2);
                        orientationLists.put(0, orientationList);

                        macAddressMap = new HashMap<>();
                        values = new ArrayList<>();

                        values.add(value);

                        macAddressMap.put(temp[3].toLowerCase(), values);

                        orientationList.add(macAddressMap);
                        orientationList.add(0);
                        newRadioMap.put(key, orientationLists);
                    } else if (orientationLists.get(0) == null) {
                        orientationList = new ArrayList<>(2);
                        orientationLists.put(0, orientationList);

                        macAddressMap = new HashMap<>();
                        values = new ArrayList<>();

                        values.add(value);

                        macAddressMap.put(temp[3].toLowerCase(), values);
                        orientationList.add(macAddressMap);
                        orientationList.add(0);
                        newRadioMap.put(key, orientationLists);
                    } else {
                        macAddressMap = uncheckedCast(orientationLists.get(0).get(0));
                        values = macAddressMap.get(temp[3].toLowerCase());

                        if (values == null) {
                            values = new ArrayList<>();
                        }

                        int position = (int) orientationLists.get(0).get(1);
                        if (position == values.size()) {
                            position = position + 1;
                            orientationLists.get(0).set(1, position);
                        } else {
                            for (int i = values.size(); i < position - 1; i++) {
                                values.add(defaultNaNValue);
                            }
                        }
                        values.add(value);
                        macAddressMap.put(temp[3].toLowerCase(), values);
                    }
                }
                line = bufferedReader.readLine();
            }
            fileReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticateInputFile(File inputFile) {
        try {
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int currentLine = 1;
            String line = bufferedReader.readLine();
            while (line != null) {
                if (!(line.startsWith("#") || line.trim().isEmpty())) {
                    line = line.replace(", ", " ");
                    String[] temp = line.split(" ");

                    Double.parseDouble(temp[1]);
                    Double.parseDouble(temp[2]);

                    if (!temp[3].matches("[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}")) {
                        throw new Exception("MAC Address is not valid. Line: " + currentLine);
                    }

                    Integer.parseInt(temp[4]);
                    Integer.parseInt(temp[5]);
                    Integer.parseInt(temp[6]);
                }
                currentLine++;
                line = bufferedReader.readLine();
            }
            fileReader.close();
            bufferedReader.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T uncheckedCast(Object obj) {
        return (T) obj;
    }
}