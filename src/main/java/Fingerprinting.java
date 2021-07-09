import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Fingerprinting {
    private static int current_input = 0;
    private static boolean use_scanner = false;
    private static final ArrayList<String> inputs = new ArrayList<>();

    private static final ArrayList<String> rssResults = new ArrayList<>();
    private static final ArrayList<String> ftmResults = new ArrayList<>();

    private static final String[] algorithmNames = {"KNN", "WKNN", "MAP", "MMSE"};
    private static final String[] distanceAlgorithms = {"Euclidean", "Bhattacharyya", "KullbackLeibler"};

    private static String errorPath;
    private static String routeFilePath;
    private static String coordinatesPath;
    private static int distanceAlgorithm = -1;

    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir") + "/src/main/resources/";

        //Path Variables
        errorPath = workingDirectory + "errors/";
        routeFilePath = workingDirectory + "route";
        coordinatesPath = workingDirectory + "coordinates/";

        //Task
        if (args.length == 0) {
            use_scanner = true;
        } else if (args[0].equals("results")) {
            printResults(workingDirectory);
            return;
        } else {
            inputs.addAll(Arrays.asList(args[0].split(" ")));
        }

        String rssRadioMapPath = workingDirectory + "radioMaps/rssRadioMap";
        String ftmRadioMapPath = workingDirectory + "radioMaps/ftmRadioMap";
        String stdRadioMapPath = workingDirectory + "radioMaps/stdRadioMap";
        String exdRadioMapPath = workingDirectory + "radioMaps/exdRadioMap";

        //RadioMaps
        try {
            RadioMap rssRadioMap = new RadioMap(new File(rssRadioMapPath));
            RadioMap ftmRadioMap = new RadioMap(new File(ftmRadioMapPath));
            Algorithms.stdRadioMap = new RadioMap(new File(stdRadioMapPath));
            ExtendedRadioMap exdRadioMap = new ExtendedRadioMap(new File(exdRadioMapPath));

            //Menu
            boolean doRSS = menu("RSS");
            boolean doFTM = menu("FTM");
            boolean doEXD = menu("EXD");

            progressBar(-1);
            for (int i = 0; i < 4; i++) {
                if (doRSS)
                    execute("rss", i, 4, rssRadioMap, null);

                if (doFTM)
                    execute("ftm", i, 5, ftmRadioMap, null);

                if (i != 0 && doRSS && doFTM) {
                    execute("fused", i, 6, null, null);
                    rssResults.clear();
                    ftmResults.clear();
                }

                if (doEXD)
                    execute("exd", i, -1, null, exdRadioMap);

                progressBar(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void rssMenu() {
        for (int i = 0; i < 4; i++) {
            System.out.print("\t" + algorithmNames[i] + " - " + ((i > 1) ? "σ" : "K") + ": ");

            String value = inputMethod();
            if (Double.parseDouble(value) > 0)
                Algorithms.parameters[i] = value;
            else {
                throw new IllegalStateException("Unexpected value.");
            }
        }
    }

    private static void ftmMenu() {
        for (int i = 0; i < 2; i++) {
            System.out.print("\t" + algorithmNames[i] + " - K: ");

            String value = inputMethod();
            if (Double.parseDouble(value) > 0)
                Algorithms.parameters[i + 4] = value;
            else {
                throw new IllegalStateException("Unexpected value.");
            }
        }

        System.out.println("\tAlgorithm to calculate the distance");
        System.out.println("\t\t1. Euclidean");
        System.out.println("\t\t2. Bhattacharyya");
        System.out.println("\t\t3. Kullback–Leibler");

        System.out.print("\n\tValue: ");

        String value = inputMethod();
        if (value.equals("1") || value.equals("2") || value.equals("3")) {
            distanceAlgorithm = Integer.parseInt(value);
        } else {
            throw new IllegalStateException("Unexpected value: " + value);
        }

        System.out.print("\tUse σ from file (Y/n)? ");

        value = inputMethod();
        if (value.equals("Y") || value.equals("y")) {
            Algorithms.sGreekFromFile = true;

            Algorithms.parameters[6] = "-1";
            Algorithms.parameters[7] = "-1";
        } else if (value.equals("N") || value.equals("n")) {
            Algorithms.sGreekFromFile = false;

            System.out.print("\tMAP - σ: ");

            value = inputMethod();
            if (Double.parseDouble(value) > 0)
                Algorithms.parameters[6] = value;
            else {
                throw new IllegalStateException("Unexpected value.");
            }

            System.out.print("\tMMSE - σ: ");

            value = inputMethod();
            if (Double.parseDouble(value) > 0)
                Algorithms.parameters[7] = value;
            else {
                throw new IllegalStateException("Unexpected value.");
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    private static void exdMenu() {
        for (int i = 0; i < 4; i++) {
            System.out.print("\t" + algorithmNames[i] + " - " + ((i > 1) ? "σ" : "K") + ": ");

            String value = inputMethod();
            if (Double.parseDouble(value) > 0)
                ExtendedAlgorithms.parameters[i] = value;
            else {
                throw new IllegalStateException("Unexpected value.");
            }
        }
    }

    private static void printResults(String workingDirectory) {
        System.out.println("\nRSS\n---");
        System.out.println("KNN: " + readContent("rss", "KNN"));
        System.out.println("WKNN - Euclidean: " + readContent("rss", "WKNN - Euclidean"));
        System.out.println("MAP: " + readContent("rss", "MAP"));
        System.out.println("MMSE: " + readContent("rss", "MMSE"));

        System.out.println("\nFTM\n---");
        System.out.println("KNN: " + readContent("ftm", "KNN"));
        System.out.println("WKNN - Euclidean: " + readContent("ftm", "WKNN - Euclidean"));
        System.out.println("WKNN - Bhattacharyya: " + readContent("ftm", "WKNN - Bhattacharyya"));
        System.out.println("WKNN - KullbackLeibler: " + readContent("ftm", "WKNN - KullbackLeibler"));
        System.out.println("MAP: " + readContent("ftm", "MAP"));
        System.out.println("MMSE: " + readContent("ftm", "MMSE"));

        System.out.println("\nFUSED\n---");
        System.out.println("KNN: NaN");
        System.out.println("WKNN: " + readContent("fused", "WKNN"));
        System.out.println("MAP: " + readContent("fused", "MAP"));
        System.out.println("MMSE: " + readContent("fused", "MMSE"));

        System.out.println("\nEXD\n---");
        System.out.println("KNN: " + readContent("exd", "KNN"));
        System.out.println("WKNN - Euclidean: " + readContent("exd", "WKNN - Euclidean"));
        System.out.println("MAP: " + readContent("exd", "MAP"));
        System.out.println("MMSE: " + readContent("exd", "MMSE"));

        totalPoints();

        try {
            String fileName = workingDirectory + "/results";
            String[] results = new String[algorithmNames.length];

            results[0] = readContent("rss", "KNN") + "," +
                    readContent("ftm", "KNN") + "," +
                    readContent("exd", "KNN") + "\n";

            results[1] = readContent("rss", "WKNN - Euclidean") + "," +
                    readContent("ftm", "WKNN - Euclidean") + "," +
                    readContent("ftm", "WKNN - Bhattacharyya") + "," +
                    readContent("ftm", "WKNN - KullbackLeibler") + "," +
                    readContent("exd", "WKNN - Euclidean") + "\n";

            results[2] = readContent("rss", "MAP") + "," +
                    readContent("ftm", "MAP") + "," +
                    readContent("exd", "MAP") + "\n";

            results[3] = readContent("rss", "MMSE") + "," +
                    readContent("ftm", "MMSE") + "," +
                    readContent("exd", "MMSE") + "\n";

            File folder = new File(fileName);
            if (!folder.exists() && !folder.mkdirs()) {
                System.out.println("Error creating results folder");
                return;
            }

            fileName += "/results";
            for (int i = 0; i < algorithmNames.length; i++) {
                File resultsFile = new File(fileName + algorithmNames[i]);

                PrintWriter printWriter;
                if (resultsFile.exists() && !resultsFile.isDirectory()) {
                    printWriter = new PrintWriter(new FileOutputStream(fileName + algorithmNames[i], true));
                } else {
                    printWriter = new PrintWriter(fileName + algorithmNames[i]);
                }

                printWriter.printf("%s", results[i]);
                printWriter.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String inputMethod() {
        String value;
        if (use_scanner)
            value = new Scanner(System.in).next();
        else {
            value = inputs.get(current_input++);
            System.out.println(value);
        }
        return value;
    }

    private static void progressBar(int i) {
        if (i == -1)
            System.out.print("|        |\r");
        else if (i == 0)
            System.out.print("|==      |\r");
        else if (i == 1)
            System.out.print("|====    |\r");
        else if (i == 2)
            System.out.print("|======  |\r");
        else if (i == 3)
            System.out.print("|========|\r");
    }

    private static boolean menu(String type) {
        System.out.print(type + " (Y/n)? ");

        String value = inputMethod();
        if (value.equals("Y") || value.equals("y")) {
            switch (type) {
                case "RSS":
                    rssMenu();
                    break;
                case "FTM":
                    ftmMenu();
                    break;
                case "EXD":
                    exdMenu();
                    break;
            }
            return true;
        } else if (value.equals("N") || value.equals("n")) {
            return false;
        } else {
            throw new IllegalStateException("Unexpected value.");
        }
    }

    private static String euclideanDistance(String d1, String d2) {
        double x1 = Double.parseDouble(d1.split(" ")[0]);
        double y1 = Double.parseDouble(d1.split(" ")[1]);

        double x2 = Double.parseDouble(d2.split(" ")[0]);
        double y2 = Double.parseDouble(d2.split(" ")[1]);

        return String.valueOf(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    private static double readContent(String type, String algorithm) {
        double mean;
        try {
            File file = new File(errorPath + type + "/" + algorithm);
            Scanner scanner = new Scanner(file);

            double n = 0;
            double sum = 0;
            while (scanner.hasNextLine()) {
                String string = scanner.nextLine();
                sum += Double.parseDouble(string);
                n++;
            }
            scanner.close();
            mean = sum / n;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mean = Double.NaN;
        }
        return mean;
    }

    private static void totalPoints() {
        int rssPoints = 0;
        int ftmPoints = 0;

        File rssFile = new File(errorPath + "rss/WKNN - Euclidean");
        File ftmFile = new File(errorPath + "ftm/WKNN - Bhattacharyya");

        try {
            Scanner rssScanner = new Scanner(rssFile);
            Scanner ftmScanner = new Scanner(ftmFile);

            while (rssScanner.hasNextLine()) {
                String rssString = rssScanner.nextLine();
                String ftmString = ftmScanner.nextLine();

                if (Double.parseDouble(rssString) < Double.parseDouble(ftmString)) {
                    rssPoints++;
                } else {
                    ftmPoints++;
                }
            }

            System.out.println("\nTotal Points");
            System.out.println("RSS: " + rssPoints + "/" + (rssPoints + ftmPoints));
            System.out.println("FTM: " + ftmPoints + "/" + (rssPoints + ftmPoints));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void execute(String type, int i, int value, RadioMap radioMap, ExtendedRadioMap exdRadioMap) {
        if (!type.equals("fused") && !type.equals("exd"))
            Algorithms.TYPE = type;

        File error = new File(errorPath + type);
        if (!error.exists() && !error.mkdirs()) {
            System.out.println("Error creating folder " + type);
            return;
        }

        File coordinates = new File(coordinatesPath + type);
        if (!coordinates.exists() && !coordinates.mkdirs()) {
            System.out.println("Error creating folder " + type);
            return;
        }

        try {
            RouteFile routeFile = new RouteFile(new File(routeFilePath));

            File errorFile;
            File coordinatesFile;
            File realCoordinatesFile;

            if ((type.equals("rss") || type.equals("exd")) && (i == 1)) {
                errorFile = new File(errorPath + type + "/" + algorithmNames[i] + " - Euclidean");
                coordinatesFile = new File(coordinatesPath + type + "/" + algorithmNames[i] + " - Euclidean");
            } else if (type.equals("ftm") && (i == 1)) {
                errorFile = new File(errorPath + type + "/" + algorithmNames[i] + " - " + distanceAlgorithms[distanceAlgorithm - 1]);
                coordinatesFile = new File(coordinatesPath + type + "/" + algorithmNames[i] + " - " + distanceAlgorithms[distanceAlgorithm - 1]);
            } else {
                errorFile = new File(errorPath + type + "/" + algorithmNames[i]);
                coordinatesFile = new File(coordinatesPath + type + "/" + algorithmNames[i]);
            }

            realCoordinatesFile = new File(coordinatesPath + "/realCoordinates");

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(errorFile, false));
            BufferedWriter bufferedWriterCoordinates = new BufferedWriter(new FileWriter(coordinatesFile, false));
            BufferedWriter bufferedWriterRealCoordinates = new BufferedWriter(new FileWriter(realCoordinatesFile, false));

            int counter = 0;
            while (routeFile.eof()) {
                String result;

                switch (type) {
                    case "rss":
                        result = Algorithms.ProcessingAlgorithms(routeFile.getLatestScanList(value), radioMap, i + 1);
                        if (i != 0) rssResults.add(result);
                        distanceFromAP(routeFile, result, type, i);
                        break;
                    case "ftm":
                        result = Algorithms.ProcessingAlgorithms(routeFile.getLatestScanList(value), routeFile.getLatestStdScanList(), radioMap, i + 1, distanceAlgorithm);
                        if (i != 0) ftmResults.add(result);
                        distanceFromAP(routeFile, result, type, i);
                        break;
                    case "fused":
                        routeFile.getLatestScanList(value);
                        result = Algorithms.FusedEngine(rssResults.get(counter), ftmResults.get(counter));
                        counter++;
                        break;
                    case "exd":
                        result = ExtendedAlgorithms.ProcessingAlgorithms(routeFile.getExtendedLatestScanList(), exdRadioMap, i + 1);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value.");
                }

                if (result != null) {
                    bufferedWriterCoordinates.write(result + "\n");
                    bufferedWriterRealCoordinates.write(routeFile.getCurrentLocation() + "\n");
                    bufferedWriter.write(euclideanDistance(result, routeFile.getCurrentLocation()) + "\n");
                } else {
                    throw new IllegalStateException("Unexpected value.");
                }
            }

            routeFile.closeFile();
            bufferedWriter.close();
            bufferedWriterCoordinates.close();
            bufferedWriterRealCoordinates.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void distanceFromAP(RouteFile routeFile, String result, String type, int i) throws IOException {
        Map<String, String> map = new HashMap<String, String>() {
            {
                put("b8:08:cf:a0:73:5a", "44.05 27.94");
                put("b8:08:cf:a0:73:5f", "29.46 26.05");
                put("b8:08:cf:a0:73:a0", "19.61 27.94");
                put("d0:c6:37:d2:17:5c", "01.92 23.45");
                put("d0:c6:37:d2:19:e1", "18.50 16.76");
                put("d0:c6:37:d2:3e:12", "25.39 11.05");
                put("b8:08:cf:a0:73:4b", "37.24 11.47");
                put("b8:08:cf:a0:73:96", "51.69 16.71");
            }
        };

        File file = new File(errorPath + type + "/" + algorithmNames[i] + " - dAP");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));

        ArrayList<LogRecord> rangeList = routeFile.getLatestStdScanList();
        for (LogRecord record : rangeList) {
            bufferedWriter.write(record.getBssid() + "\t" + euclideanDistance(result, map.get(record.getBssid())) + "\n");
        }

        if (rangeList.size() < 8) {
            ArrayList<String> ap = new ArrayList<>();

            for (LogRecord logRecord : rangeList) {
                ap.add(logRecord.getBssid());
            }

            if (!ap.contains("b8:08:cf:a0:73:5a")) {
                bufferedWriter.write("b8:08:cf:a0:73:5a\tNaN\n");
            } else if (!ap.contains("b8:08:cf:a0:73:5f")) {
                bufferedWriter.write("b8:08:cf:a0:73:5f\tNaN\n");
            } else if (!ap.contains("b8:08:cf:a0:73:a0")) {
                bufferedWriter.write("b8:08:cf:a0:73:a0\tNaN\n");
            } else if (!ap.contains("d0:c6:37:d2:17:5c")) {
                bufferedWriter.write("d0:c6:37:d2:17:5c\tNaN\n");
            } else if (!ap.contains("d0:c6:37:d2:19:e1")) {
                bufferedWriter.write("d0:c6:37:d2:19:e1\tNaN\n");
            } else if (!ap.contains("d0:c6:37:d2:3e:12")) {
                bufferedWriter.write("d0:c6:37:d2:3e:12\tNaN\n");
            } else if (!ap.contains("b8:08:cf:a0:73:4b")) {
                bufferedWriter.write("b8:08:cf:a0:73:4b\tNaN\n");
            } else if (!ap.contains("b8:08:cf:a0:73:96")) {
                bufferedWriter.write("b8:08:cf:a0:73:96\tNaN\n");
            }
        }
        bufferedWriter.close();
    }
}