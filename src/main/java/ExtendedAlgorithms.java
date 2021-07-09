import java.util.ArrayList;
import java.util.Comparator;

public class ExtendedAlgorithms {
    public static String[] parameters = new String[4];

    public static String ProcessingAlgorithms(ArrayList<LogRecord> latestScanList, ExtendedRadioMap RM, int algorithm_choice) {
        int i, j;
        LogRecord temp_LR;
        int notFoundCounter = 0;
        ArrayList<String> observedValues = new ArrayList<>();
        ArrayList<String> macAddressList = RM.getMacAddressList();

        // Read parameter of algorithm
        String rssNaNValue = RM.getRssNaN();
        String ftmNaNValue = RM.getFtmNaN();

        // Check which mac addresses of radio map, we are currently listening.
        for (i = 0; i < macAddressList.size(); ++i) {
            for (j = 0; j < latestScanList.size(); ++j) {
                temp_LR = latestScanList.get(j);
                // MAC Address Matched
                if (macAddressList.get(i).compareTo(temp_LR.getBssid()) == 0) {
                    observedValues.add(String.valueOf(minMaxNormalization(temp_LR.getValue(), RM.getRssMin(), RM.getRssMax())));
                    observedValues.add(String.valueOf(minMaxNormalization(latestScanList.get(j + 1).getValue(), RM.getFtmMin(), RM.getFtmMax())));
                    break;
                }
            }
            // A MAC Address is missing so we place a small value, NaN value
            if (j == latestScanList.size()) {
                observedValues.add(String.valueOf(rssNaNValue));
                observedValues.add(String.valueOf(ftmNaNValue));
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

    private static String KNN_WKNN_Algorithm(ExtendedRadioMap RM, ArrayList<String> observedValues, String parameter, boolean isWeighted) {
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

    private static String MAP_MMSE_Algorithm(ExtendedRadioMap RM, ArrayList<String> observedValues, String parameter, boolean isWeighted) {
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

        // Find the location of user with the highest probability
        for (String location : RM.getLocationHashMap().keySet()) {

            values = RM.getLocationHashMap().get(location);
            curResult = calculateProbability(values, observedValues, sGreek);

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

        return myLocation;
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

    private static double calculateProbability(ArrayList<String> l1, ArrayList<String> l2, double sGreek) {
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

            sumWeights += locationWeight;
            weightedSumX += locationWeight * x;
            weightedSumY += locationWeight * y;
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

            weightedSumX += (x * NP);
            weightedSumY += (y * NP);
        }

        return weightedSumX + " " + weightedSumY;
    }

    private static Double minMaxNormalization(double x, double min, double max) {
        return (x - min) / (max - min);
    }

    private static String readParameter(int algorithm_choice) {
        if (algorithm_choice == 1) {
            return parameters[0];
        } else if (algorithm_choice == 2) {
            return parameters[1];
        } else if (algorithm_choice == 3) {
            return parameters[2];
        } else if (algorithm_choice == 4) {
            return parameters[3];
        }
        return null;
    }
}