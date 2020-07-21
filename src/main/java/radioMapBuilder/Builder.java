package radioMapBuilder;

public class Builder {

    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir") + "/src/main/resources/";

        String radioMapFile = workingDirectory + "radioMap";
        String radioMapFolder = workingDirectory + "radioMaps/";

        int rssDefaultNaNValue = -110;
        int ftmDefaultNaNValue = 100000;
        int stdDefaultNaNValue = 1;

        RadioMap rss = new RadioMap(radioMapFile, "rssRadioMap", radioMapFolder, 4, rssDefaultNaNValue);
        RadioMap ftm = new RadioMap(radioMapFile, "ftmRadioMap", radioMapFolder, 5, ftmDefaultNaNValue);
        RadioMap std = new RadioMap(radioMapFile, "stdRadioMap", radioMapFolder, 6, stdDefaultNaNValue);

        rss.createRadioMap();
        ftm.createRadioMap();
        std.createRadioMap();
    }
}