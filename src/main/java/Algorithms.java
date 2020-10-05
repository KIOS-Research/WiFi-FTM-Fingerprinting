import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Algorithms {
    private static final ArrayList<Double> rssProbabilities = new ArrayList<>();
    private static final ArrayList<Double> ftmProbabilities = new ArrayList<>();
    private static final ArrayList<Double[][]> rssCoordinates = new ArrayList<>();
    private static final ArrayList<Double[][]> ftmCoordinates = new ArrayList<>();

    public static String TYPE;
    public static RadioMap stdRadioMap;
    public static boolean sGreekFromFile;
    public static String[] parameters = new String[8];

    public static String FusedEngine(String rssResult, String ftmResult) {
        RealMatrix rssLoc = MatrixUtils.createRealMatrix(2, 1);
        rssLoc.setEntry(0, 0, Double.parseDouble(rssResult.split(" ")[0]));
        rssLoc.setEntry(1, 0, Double.parseDouble(rssResult.split(" ")[1]));

        RealMatrix ftmLoc = MatrixUtils.createRealMatrix(2, 1);
        ftmLoc.setEntry(0, 0, Double.parseDouble(ftmResult.split(" ")[0]));
        ftmLoc.setEntry(1, 0, Double.parseDouble(ftmResult.split(" ")[1]));

        RealMatrix rssCov = calculateCovarianceMatrix(rssLoc);
        RealMatrix ftmCov = calculateCovarianceMatrix(ftmLoc);

        try {
            RealMatrix rssInv = MatrixUtils.inverse(rssCov);
            RealMatrix ftmInv = MatrixUtils.inverse(ftmCov);

            RealMatrix sigma = MatrixUtils.inverse(rssInv.add(ftmInv));
            RealMatrix location = sigma.multiply(rssInv.multiply(rssLoc).add(ftmInv.multiply(ftmLoc)));

            return location.getEntry(0, 0) + " " + location.getEntry(1, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String ProcessingAlgorithms(ArrayList<LogRecord> latestScanList, RadioMap RM, int algorithm_choice) {
        int i, j;
        LogRecord temp_LR;
        int notFoundCounter = 0;
        ArrayList<String> observedValues = new ArrayList<>();
        ArrayList<String> macAddressList = RM.getMacAddressList();

        // Read parameter of algorithm
        String NaNValue = RM.getNaN();

        // Check which mac addresses of radio map, we are currently listening.
        for (i = 0; i < macAddressList.size(); ++i) {
            for (j = 0; j < latestScanList.size(); ++j) {
                temp_LR = latestScanList.get(j);
                // MAC Address Matched
                if (macAddressList.get(i).compareTo(temp_LR.getBssid()) == 0) {
                    observedValues.add(String.valueOf(temp_LR.getValue()));
                    break;
                }
            }
            // A MAC Address is missing so we place a small value, NaN value
            if (j == latestScanList.size()) {
                observedValues.add(String.valueOf(NaNValue));
                ++notFoundCounter;
            }
        }

        if (notFoundCounter == macAddressList.size())
            return null;

        // Read parameter of algorithm
        String parameter = readParameter(algorithm_choice);

        if (parameter == null)
            return null;

        switch (algorithm_choice) {
            case 1:
                return KNN_WKNN_Algorithm(RM, observedValues, parameter, false);
            case 2:
                return KNN_WKNN_Algorithm(RM, observedValues, parameter, true);
            case 3:
                return MAP_MMSE_Algorithm(RM, observedValues, parameter, false);
            case 4:
                return MAP_MMSE_Algorithm(RM, observedValues, parameter, true);
        }
        return null;
    }

    public static String ProcessingAlgorithms(ArrayList<LogRecord> latestScanList, ArrayList<LogRecord> latestStdScanList, RadioMap RM, int algorithm_choice, int distanceAlgorithm) {
        int i, j;
        LogRecord temp_LR;
        LogRecord stdTemp_LR;
        int notFoundCounter = 0;
        ArrayList<String> observedValues = new ArrayList<>();
        ArrayList<String> stdObservedValues = new ArrayList<>();
        ArrayList<String> macAddressList = RM.getMacAddressList();

        // Read parameter of algorithm
        String NaNValue = RM.getNaN();
        String stdNaNValue = stdRadioMap.getNaN();

        // Check which mac addresses of radio map, we are currently listening.
        for (i = 0; i < macAddressList.size(); ++i) {
            for (j = 0; j < latestScanList.size(); ++j) {
                temp_LR = latestScanList.get(j);
                stdTemp_LR = latestStdScanList.get(j);
                // MAC Address Matched
                if (macAddressList.get(i).compareTo(temp_LR.getBssid()) == 0) {
                    observedValues.add(String.valueOf(temp_LR.getValue()));
                    stdObservedValues.add(String.valueOf(stdTemp_LR.getValue()));
                    break;
                }
            }
            // A MAC Address is missing so we place a small value, NaN value
            if (j == latestScanList.size()) {
                observedValues.add(String.valueOf(NaNValue));
                stdObservedValues.add(String.valueOf(stdNaNValue));
                ++notFoundCounter;
            }
        }

        if (notFoundCounter == macAddressList.size())
            return null;

        // Read parameter of algorithm
        String parameter = readParameter(algorithm_choice);

        if (parameter == null)
            return null;

        switch (algorithm_choice) {
            case 1:
                return KNN_WKNN_Algorithm(RM, observedValues, stdObservedValues, parameter, false, distanceAlgorithm);
            case 2:
                return KNN_WKNN_Algorithm(RM, observedValues, stdObservedValues, parameter, true, distanceAlgorithm);
            case 3:
                return MAP_MMSE_Algorithm(RM, observedValues, parameter, false);
            case 4:
                return MAP_MMSE_Algorithm(RM, observedValues, parameter, true);
        }
        return null;
    }

    private static String KNN_WKNN_Algorithm(RadioMap RM, ArrayList<String> observedValues, String parameter, boolean isWeighted) {
        int K;
        double curResult;
        String myLocation;

        ArrayList<String> values;
        ArrayList<LocDistance> locDistanceResultsList = new ArrayList<>();

        try {
            K = Integer.parseInt(parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        clearLists();
        // Construct a list with locations-distances pairs
        for (String location : RM.getLocationHashMap().keySet()) {
            values = RM.getLocationHashMap().get(location);
            curResult = calculateEuclideanDistance(values, observedValues);

            if (curResult == Double.NEGATIVE_INFINITY)
                return null;

            locDistanceResultsList.add(0, new LocDistance(curResult, location));
        }

        // Sort locations-distances pairs based on minimum distances
        locDistanceResultsList.sort(Comparator.comparingDouble(LocDistance::getDistance));

        if (!isWeighted) {
            myLocation = calculateAverageKDistanceLocations(locDistanceResultsList, K);
        } else {
            myLocation = calculateWeightedAverageKDistanceLocations(locDistanceResultsList, K);
        }

        return myLocation;
    }

    private static String KNN_WKNN_Algorithm(RadioMap RM, ArrayList<String> observedValues, ArrayList<String> stdObservedValues, String parameter, boolean isWeighted, int distanceAlgorithm) {
        int K;
        double curResult;
        String myLocation;

        ArrayList<String> values;
        ArrayList<String> stdValues;
        ArrayList<LocDistance> locDistanceResultsList = new ArrayList<>();

        try {
            K = Integer.parseInt(parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        clearLists();
        // Construct a list with locations-distances pairs
        for (String location : RM.getLocationHashMap().keySet()) {
            values = RM.getLocationHashMap().get(location);
            stdValues = stdRadioMap.getLocationHashMap().get(location);

            if (!isWeighted)
                curResult = calculateEuclideanDistance(values, observedValues);
            else
                switch (distanceAlgorithm) {
                    case 1:
                        curResult = calculateEuclideanDistance(values, observedValues);
                        break;
                    case 2:
                        curResult = calculateBhattacharyyaDistance(values, stdValues, observedValues, stdObservedValues);
                        break;
                    case 3:
                        curResult = calculateKullbackLeibler(values, stdValues, observedValues, stdObservedValues);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + distanceAlgorithm);
                }

            if (curResult == Double.NEGATIVE_INFINITY)
                return null;

            locDistanceResultsList.add(0, new LocDistance(curResult, location));
        }

        // Sort locations-distances pairs based on minimum distances
        locDistanceResultsList.sort(Comparator.comparingDouble(LocDistance::getDistance));

        if (!isWeighted) {
            myLocation = calculateAverageKDistanceLocations(locDistanceResultsList, K);
        } else {
            myLocation = calculateWeightedAverageKDistanceLocations(locDistanceResultsList, K);
        }
        return myLocation;
    }

    private static String MAP_MMSE_Algorithm(RadioMap RM, ArrayList<String> observedValues, String parameter, boolean isWeighted) {
        double sGreek;
        double curResult;
        double highestProbability = Double.NEGATIVE_INFINITY;

        String myLocation = null;
        ArrayList<String> values;
        ArrayList<LocDistance> locDistanceResultsList = new ArrayList<>();

        try {
            sGreek = Double.parseDouble(parameter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        clearLists();
        // Find the location of user with the highest probability
        for (String location : RM.getLocationHashMap().keySet()) {
            values = RM.getLocationHashMap().get(location);
            curResult = calculateProbability(values, observedValues, sGreek, location);

            if (!isWeighted) {
                rssProbabilities.add(curResult);
                ftmProbabilities.add(curResult);

                rssCoordinates.add(new Double[][]{{Double.parseDouble(location.split(" ")[0])}, {Double.parseDouble(location.split(" ")[1])}});
                ftmCoordinates.add(new Double[][]{{Double.parseDouble(location.split(" ")[0])}, {Double.parseDouble(location.split(" ")[1])}});
            }

            if (curResult == Double.NEGATIVE_INFINITY)
                return null;
            else if (curResult > highestProbability) {
                highestProbability = curResult;
                myLocation = location;
            }

            if (isWeighted)
                locDistanceResultsList.add(0, new LocDistance(curResult, location));
        }

        if (isWeighted)
            myLocation = calculateWeightedAverageProbabilityLocations(locDistanceResultsList);
        else {
            double sumRSSProbabilities = 0;
            double sumFTMProbabilities = 0;

            for (Double probability : rssProbabilities)
                sumRSSProbabilities += probability;

            for (Double probability : ftmProbabilities)
                sumFTMProbabilities += probability;

            for (int i = 0; i < rssProbabilities.size(); i++) {
                rssProbabilities.set(i, rssProbabilities.get(i) / sumRSSProbabilities);
            }

            for (int i = 0; i < ftmProbabilities.size(); i++) {
                ftmProbabilities.set(i, ftmProbabilities.get(i) / sumFTMProbabilities);
            }
        }

        return myLocation;
    }

    private static RealMatrix calculateCovarianceMatrix(RealMatrix predictedLocation) {
        ArrayList<Double> probabilities = null;
        ArrayList<Double[][]> coordinates = null;

        if (TYPE.equals("rss")) {
            coordinates = rssCoordinates;
            probabilities = rssProbabilities;
        } else if (TYPE.equals("ftm")) {
            coordinates = ftmCoordinates;
            probabilities = ftmProbabilities;
        }

        double x = predictedLocation.getEntry(0, 0);
        double y = predictedLocation.getEntry(1, 0);
        RealMatrix covMatrix = MatrixUtils.createRealMatrix(2, 2);

        for (int i = 0; i < Objects.requireNonNull(probabilities).size(); i++) {
            double xi = coordinates.get(i)[0][0], yi = coordinates.get(i)[1][0];

            covMatrix.setEntry(0, 0, covMatrix.getEntry(0, 0) + probabilities.get(i) * Math.pow(xi - x, 2));
            covMatrix.setEntry(0, 1, covMatrix.getEntry(0, 1) + probabilities.get(i) * (xi - x) * (yi - y));
            covMatrix.setEntry(1, 0, covMatrix.getEntry(1, 0) + probabilities.get(i) * (yi - y) * (xi - x));
            covMatrix.setEntry(1, 1, covMatrix.getEntry(1, 1) + probabilities.get(i) * Math.pow(yi - y, 2));
        }

        return covMatrix;
    }

    private static double calculateEuclideanDistance(ArrayList<String> l1, ArrayList<String> l2) {
        double v1;
        double v2;
        double temp;
        double finalResult = 0;

        for (int i = 0; i < l1.size(); ++i) {
            try {
                v1 = Double.parseDouble(l1.get(i).trim());
                v2 = Double.parseDouble(l2.get(i).trim());
            } catch (Exception e) {
                e.printStackTrace();
                return Double.NEGATIVE_INFINITY;
            }

            temp = v1 - v2;
            temp *= temp;
            finalResult += temp;
        }
        return Math.sqrt(finalResult);
    }

    private static double calculateKullbackLeibler(ArrayList<String> values, ArrayList<String> stdValues, ArrayList<String> observedValues, ArrayList<String> stdObservedValues) {
        int n = 5; //Number of APs
        double distance;

        RealMatrix m0 = MatrixUtils.createRealMatrix(n, 1);
        RealMatrix m1 = MatrixUtils.createRealMatrix(n, 1);

        RealMatrix s0 = MatrixUtils.createRealMatrix(n, n);
        RealMatrix s1 = MatrixUtils.createRealMatrix(n, n);

        for (int i = 0; i < n; i++) {
            m0.setEntry(i, 0, Double.parseDouble(values.get(i)));
            m1.setEntry(i, 0, Double.parseDouble(observedValues.get(i)));
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    s0.setEntry(i, j, Math.pow(Double.parseDouble(stdValues.get(i)), 2));

                    if (Double.parseDouble(stdObservedValues.get(j)) != 0)
                        s1.setEntry(i, j, Math.pow(Double.parseDouble(stdObservedValues.get(j)), 2));
                    else
                        s1.setEntry(i, j, 1);
                }
            }
        }

        try {
            double detS0 = new LUDecomposition(s0).getDeterminant();
            double detS1 = new LUDecomposition(s1).getDeterminant();
            distance = (1 / 2.0) * (MatrixUtils.inverse(s1).multiply(s0).getTrace() + m1.subtract(m0).transpose().multiply(MatrixUtils.inverse(s1)).multiply(m1.subtract(m0)).getEntry(0, 0) - n + Math.log(detS1 / detS0));
        } catch (Exception e) {
            e.printStackTrace();
            return Double.NEGATIVE_INFINITY;
        }

        return distance;
    }

    private static double calculateBhattacharyyaDistance(ArrayList<String> values, ArrayList<String> stdValues, ArrayList<String> observedValues, ArrayList<String> stdObservedValues) {
        int n = 5; //Number of APs
        double distance;

        RealMatrix m1 = MatrixUtils.createRealMatrix(n, 1);
        RealMatrix m2 = MatrixUtils.createRealMatrix(n, 1);

        RealMatrix s = MatrixUtils.createRealMatrix(n, n);
        RealMatrix s1 = MatrixUtils.createRealMatrix(n, n);
        RealMatrix s2 = MatrixUtils.createRealMatrix(n, n);

        for (int i = 0; i < n; i++) {
            m1.setEntry(i, 0, Double.parseDouble(values.get(i)));
            m2.setEntry(i, 0, Double.parseDouble(observedValues.get(i)));
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    s1.setEntry(i, j, Math.pow(Double.parseDouble(stdValues.get(i)), 2));

                    if (Double.parseDouble(stdObservedValues.get(j)) != 0)
                        s2.setEntry(i, j, Math.pow(Double.parseDouble(stdObservedValues.get(j)), 2));
                    else
                        s2.setEntry(i, j, 1);

                    s.setEntry(i, j, (s1.getEntry(i, j) + s2.getEntry(i, j)) / 2);
                }
            }
        }

        try {
            double detS = new LUDecomposition(s).getDeterminant();
            double detS1 = new LUDecomposition(s1).getDeterminant();
            double detS2 = new LUDecomposition(s2).getDeterminant();

            distance = (1 / 8.0) * (m1.subtract(m2)).transpose().multiply(MatrixUtils.inverse(s)).multiply((m1.subtract(m2))).getEntry(0, 0);
            distance += (1 / 2.0) * Math.log(detS / (Math.sqrt(detS1 * detS2)));
        } catch (Exception e) {
            e.printStackTrace();
            return Double.NEGATIVE_INFINITY;
        }

        return distance;
    }

    private static double calculateProbability(ArrayList<String> l1, ArrayList<String> l2, double sGreek, String location) {
        double v1;
        double v2;
        double temp;
        double finalResult = 1;

        for (int i = 0; i < l1.size(); ++i) {
            try {
                v1 = Double.parseDouble(l1.get(i).trim());
                v2 = Double.parseDouble(l2.get(i).trim());
            } catch (Exception e) {
                e.printStackTrace();
                return Double.NEGATIVE_INFINITY;
            }

            if (!TYPE.equals("rss") && sGreekFromFile) {
                sGreek = Double.parseDouble(stdRadioMap.getLocationHashMap().get(location).get(i));

                if (sGreek == 0)
                    sGreek = Double.parseDouble(stdRadioMap.getNaN());
            }

            temp = (1 / (Math.sqrt(2 * Math.PI) * sGreek)) * Math.exp(-(Math.pow(v1 - v2, 2)) / (2 * Math.pow(sGreek, 2)));

            //Do not allow zero instead stop on small possibility
            if (finalResult * temp != 0)
                finalResult = finalResult * temp;
        }
        return finalResult;
    }

    private static String calculateAverageKDistanceLocations(ArrayList<LocDistance> locDistanceResultsList, int K) {
        double x, y;
        double sumX = 0;
        double sumY = 0;
        String[] locationArray;

        int K_Min = Math.min(K, locDistanceResultsList.size());

        // Calculate the sum of X and Y
        for (int i = 0; i < K_Min; ++i) {
            locationArray = locDistanceResultsList.get(i).getLocation().split(" ");

            try {
                x = Double.parseDouble(locationArray[0].trim());
                y = Double.parseDouble(locationArray[1].trim());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            sumX += x;
            sumY += y;
        }

        // Calculate the average
        sumX /= K_Min;
        sumY /= K_Min;

        return sumX + " " + sumY;
    }

    private static String calculateWeightedAverageKDistanceLocations(ArrayList<LocDistance> locDistanceResultsList, int K) {
        double x, y;
        double locationWeight;
        double sumWeights = 0;
        double weightedSumX = 0;
        double weightedSumY = 0;

        String[] locationArray;

        int K_Min = Math.min(K, locDistanceResultsList.size());

        // Calculate the weighted sum of X and Y
        for (int i = 0; i < K_Min; ++i) {
            if (locDistanceResultsList.get(i).getDistance() != 0.0) {
                locationWeight = 1 / locDistanceResultsList.get(i).getDistance();
            } else {
                locationWeight = 100;
            }
            locationArray = locDistanceResultsList.get(i).getLocation().split(" ");

            try {
                x = Double.parseDouble(locationArray[0].trim());
                y = Double.parseDouble(locationArray[1].trim());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (TYPE.equals("rss")) {
                rssCoordinates.add(new Double[][]{{x}, {y}});
                rssProbabilities.add(locationWeight);
            } else if (TYPE.equals("ftm")) {
                ftmCoordinates.add(new Double[][]{{x}, {y}});
                ftmProbabilities.add(locationWeight);
            }

            sumWeights += locationWeight;
            weightedSumX += locationWeight * x;
            weightedSumY += locationWeight * y;
        }

        if (TYPE.equals("rss")) {
            for (int i = 0; i < rssProbabilities.size(); i++) {
                rssProbabilities.set(i, rssProbabilities.get(i) / sumWeights);
            }
        } else if (TYPE.equals("ftm")) {
            for (int i = 0; i < ftmProbabilities.size(); i++) {
                ftmProbabilities.set(i, ftmProbabilities.get(i) / sumWeights);
            }
        }

        weightedSumX /= sumWeights;
        weightedSumY /= sumWeights;

        return weightedSumX + " " + weightedSumY;
    }

    private static String calculateWeightedAverageProbabilityLocations(ArrayList<LocDistance> locDistanceResultsList) {
        double NP;
        double x, y;
        double weightedSumX = 0;
        double weightedSumY = 0;
        double sumProbabilities = 0;

        String[] locationArray;

        // Calculate the sum of all probabilities
        for (LocDistance locDistance : locDistanceResultsList) sumProbabilities += locDistance.getDistance();

        // Calculate the weighted (Normalized Probabilities) sum of X and Y
        for (LocDistance locDistance : locDistanceResultsList) {
            locationArray = locDistance.getLocation().split(" ");

            try {
                x = Double.parseDouble(locationArray[0].trim());
                y = Double.parseDouble(locationArray[1].trim());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            NP = locDistance.getDistance() / sumProbabilities;

            if (TYPE.equals("rss")) {
                rssProbabilities.add(NP);
                rssCoordinates.add(new Double[][]{{x}, {y}});
            } else if (TYPE.equals("ftm")) {
                ftmProbabilities.add(NP);
                ftmCoordinates.add(new Double[][]{{x}, {y}});
            }

            weightedSumX += (x * NP);
            weightedSumY += (y * NP);
        }
        return weightedSumX + " " + weightedSumY;
    }

    private static String readParameter(int algorithm_choice) {
        if (algorithm_choice == 1) {
            return (TYPE.equals("rss") ? parameters[0] : parameters[4]);
        } else if (algorithm_choice == 2) {
            return (TYPE.equals("rss") ? parameters[1] : parameters[5]);
        } else if (algorithm_choice == 3) {
            return (TYPE.equals("rss") ? parameters[2] : parameters[6]);
        } else if (algorithm_choice == 4) {
            return (TYPE.equals("rss") ? parameters[3] : parameters[7]);
        }
        return null;
    }

    private static void clearLists() {
        rssCoordinates.clear();
        ftmCoordinates.clear();
        rssProbabilities.clear();
        ftmProbabilities.clear();
    }
}