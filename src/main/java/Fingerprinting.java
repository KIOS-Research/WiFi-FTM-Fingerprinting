import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Fingerprinting {
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

        //Results
        if (args.length == 1) {
            printResults();
            return;
        }

        String rssRadioMapPath = workingDirectory + "radioMaps/rssRadioMap";
        String ftmRadioMapPath = workingDirectory + "radioMaps/ftmRadioMap";
        String stdRadioMapPath = workingDirectory + "radioMaps/stdRadioMap";

        //RadioMaps
        try {
            RadioMap rssRadioMap = new RadioMap(new File(rssRadioMapPath));
            RadioMap ftmRadioMap = new RadioMap(new File(ftmRadioMapPath));
            Algorithms.stdRadioMap = new RadioMap(new File(stdRadioMapPath));

            //Menu
            boolean doRSS = menu("RSS");
            boolean doFTM = menu("FTM");

            progressBar(-1);
            for (int i = 0; i < 4; i++) {
                if (doRSS)
                    execute("rss", i, 4, rssRadioMap);

                if (doFTM)
                    execute("ftm", i, 5, ftmRadioMap);

                if (i != 0 && doRSS && doFTM){
                    execute("fused", i, 6, null);
                    rssResults.clear();
                    ftmResults.clear();
                }

                progressBar(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void rssMenu() {
        for (int i = 0; i < 4; i++) {
            System.out.print("\t" + algorithmNames[i] + " - " + ((i > 1) ? "σ" : "K") + ": ");
            String value = new Scanner(System.in).next();

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
            String value = new Scanner(System.in).next();

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
        String value = new Scanner(System.in).next();

        if (value.equals("1") || value.equals("2") || value.equals("3")) {
            distanceAlgorithm = Integer.parseInt(value);
        } else {
            throw new IllegalStateException("Unexpected value: " + value);
        }

        System.out.print("\tUse σ from file (Y/n)? ");
        value = new Scanner(System.in).next();

        if (value.equals("Y") || value.equals("y")) {
            Algorithms.sGreekFromFile = true;

            Algorithms.parameters[6] = "-1";
            Algorithms.parameters[7] = "-1";
        } else if (value.equals("N") || value.equals("n")) {
            Algorithms.sGreekFromFile = false;

            System.out.print("\tMAP - σ: ");
            value = new Scanner(System.in).next();

            if (Double.parseDouble(value) > 0)
                Algorithms.parameters[6] = value;
            else {
                throw new IllegalStateException("Unexpected value.");
            }

            System.out.print("\tMMSE - σ: ");
            value = new Scanner(System.in).next();

            if (Double.parseDouble(value) > 0)
                Algorithms.parameters[7] = value;
            else {
                throw new IllegalStateException("Unexpected value.");
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    private static void printResults() {
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
        String value;
        System.out.print(type + " (Y/n)? ");
        value = new Scanner(System.in).next();
        if (value.equals("Y") || value.equals("y")) {
            switch (type) {
                case "RSS":
                    rssMenu();
                    break;
                case "FTM":
                    ftmMenu();
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

    private static void execute(String type, int i, int value, RadioMap radioMap) {
        if (!type.equals("fused"))
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

            if (type.equals("rss") && (i == 1)) {
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
                        break;
                    case "ftm":
                        result = Algorithms.ProcessingAlgorithms(routeFile.getLatestScanList(value), routeFile.getLatestStdScanList(), radioMap, i + 1, distanceAlgorithm);
                        if (i != 0) ftmResults.add(result);
                        break;
                    case "fused":
                        routeFile.getLatestScanList(value);
                        result = Algorithms.FusedEngine(rssResults.get(counter), ftmResults.get(counter));
                        counter++;
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
}
