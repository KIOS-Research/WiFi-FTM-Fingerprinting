package dataBuilder;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Builder {
    public static void main(String[] args) throws IOException {
        String fileName = "";
        String filePath = fileName + ".csv";

        List<Data> dataList = read_csv(filePath);
        assert dataList != null;

        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        int[] range = new int[2];
        for (int i = 0; i < dataList.size(); i++) {
            range[0] = i;
            while (dataList.get(i).time_rob.equals(dataList.get(i + 1).time_rob)) {
                i++;

                if (i >= dataList.size() - 1)
                    break;
            }
            range[1] = i + 1;

            printWriter.printf("# Timestamp, X, Y, MAC Address of AP, RSS, Range, stdRange\n");

            boolean duplicate = false;
            for (int j = range[0]; j < range[1]; j++) {
                for (int k = j; k < range[1]; k++) {
                    if (j != k && dataList.get(k).ap_id.equals(dataList.get(j).ap_id)) {
                        duplicate = true;
                        break;
                    }
                }
                if (duplicate) break;
            }

            if (!duplicate) {
                for (int j = range[0]; j < range[1]; j++) {
                    Data data = dataList.get(j);
                    printWriter.printf("%f %f %f %s %d %f %f\n", data.time_rob, data.x_loc, data.y_loc, data.ap_id, data.rssi, data.range, data.range_std);
                }
            } else {
                HashMap<String, Data> hashMap = new HashMap<>();
                for (int j = range[0]; j < range[1]; j++) {
                    Data data = dataList.get(j);
                    hashMap.put(data.ap_id, data);
                }

                for (Map.Entry<String, Data> dataEntry : hashMap.entrySet()) {
                    Data data = dataEntry.getValue();
                    printWriter.printf("%f %f %f %s %d %f %f\n", data.time_rob, data.x_loc, data.y_loc, data.ap_id, data.rssi, data.range, data.range_std);
                }
            }
        }
        printWriter.close();
    }

    private static List<Data> read_csv(String filePath) {
        try {
            FileReader fileReader = new FileReader(filePath);
            CsvToBean<Data> dataCsvToBean = new CsvToBeanBuilder<Data>(fileReader)
                    .withType(Data.class)
                    .build();

            return dataCsvToBean.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
