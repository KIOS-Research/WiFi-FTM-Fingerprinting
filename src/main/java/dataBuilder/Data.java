package dataBuilder;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import java.math.BigDecimal;

public class Data {
//    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "time")
    public BigDecimal time;

//    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "device_id")
    public String device_id;

//    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "ap_id")
    public String ap_id;

//    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "range")
    public BigDecimal range;

//    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "range_std")
    public BigDecimal range_std;

//    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "rssi")
    public Integer rssi;

//    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "attempted")
    public Integer attempted;

//    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "successful")
    public Integer successful;

//    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "time_rob")
    public BigDecimal time_rob;

//    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "x_loc")
    public BigDecimal x_loc;

//    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "y_loc")
    public BigDecimal y_loc;

//    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "z_loc")
    public Float z_loc;

//    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "heading")
    public BigDecimal heading;

//    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "speed")
    public BigDecimal speed;
}
