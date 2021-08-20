# WiFi-FTM-Fingerprinting
The source code for the paper: Sami Huilla and Chrysanthos Pepi and Michalis Antoniou and Christos Laoudias and Seppo Horsmanheimo and Sergio Lembo and Matti Laukkanen and Georgios Ellinas, “Indoor Fingerprinting with Wi-Fi Fine Timing Measurements Through Range Filtering and Fingerprinting Methods”, IEEE 31st Annual International Symposium on Personal, Indoor and Mobile Radio Communications (PIMRC), 2020.

## Abstract
Wi-Fi technology has been thoroughly studied for indoor localization. This
is mainly due to the existing infrastructure inside buildings for wireless
connectivity and the uptake of mobile devices where Wi-Fi
location-dependent measurements, e.g., timing and signal strength readings,
are readily available to determine the user location. To enhance the
accuracy of Wi-Fi solutions, a two-way ranging approach was recently
introduced into the IEEE 802.11 standard for the provision of Fine Timing
Measurements (FTM). Such measurements enable a more reliable estimation of
the distance between FTM-capable Wi-Fi access points and user-carried
devices; thus, promising to deliver meter-level location accuracy. In this
work, we propose two novel solutions that leverage FTM and follow different
approaches, which have not been investigated in the literature. The first
solution is based on an Unscented Kalman Filter (UKF) algorithm to process
FTM ranging measurements, while the second solution relies on an FTM
fingerprinting method. Experimental results using real-life data collected
in a typical office environment demonstrate the effectiveness of both
solutions, while the FTM fingerprinting approach demonstrated 1.12m and
2.13m localization errors for the 67-th and 95-th percentiles,
respectively. This is a two to three times improvement over the traditional
Wi-Fi signal strength fingerprinting approach and the UKF ranging
algorithm.

### Project
The project consist of 3 tasks, RadioMap Builder, Fingerprinting Localization, and Results inside the `build.gradle` file.

#### RadioMap Builder
To generate radioMaps

- Input: radioMap
- Output: rssRadioMap, ftmRadioMap, stdRadioMap, exdRadioMap

#### Fingerprint Localization
To compute positioning errors through fingerprinting

- Input: route
- Output: coordinates of the estimate locations and positioning errors

#### Results
To print positioning errors

#### Structure of input files
In order to run the project input files (radioMap and route) should follow the below structure. Sample files included.
```
...
# Timestamp, X, Y, MAC Address of AP, RSS, Range, stdRange
1572514592169.957000 3.4513764 44.006992 ec:58:ea:78:b9:0c -49 23421 4590
1572514592169.957000 3.4513764 44.006992 ec:58:ea:38:b9:0c -50 2009 579
1572514592169.957000 3.4513764 44.006992 ec:58:ea:78:b9:08 -45 3590 381
1572514592169.957000 3.4513764 44.006992 ec:58:ea:38:b9:08 -46 9573 1136
1572514592169.957000 3.4513764 44.006992 38:ff:36:31:86:e8 -79 23653 785
1572514592169.957000 3.4513764 44.006992 38:ff:36:b1:86:e8 -80 16814 1214
1572514592169.957000 3.4513764 44.006992 38:ff:36:71:86:e8 -80 20202 256
...
```

## Contributors
Chrysanthos Pepi [KIOS Research and Innovation Center of Excellence, University of Cyprus](https://www.kios.ucy.ac.cy/)

Michalis Antoniou [KIOS Research and Innovation Center of Excellence, University of Cyprus](https://www.kios.ucy.ac.cy/)

Christos Laoudias [KIOS Research and Innovation Center of Excellence, University of Cyprus](https://www.kios.ucy.ac.cy/)

## How to cite
Sami Huilla and Chrysanthos Pepi and Michalis Antoniou and Christos Laoudias and Seppo Horsmanheimo and Sergio Lembo and Matti Laukkanen and Georgios Ellinas, “Indoor Fingerprinting with Wi-Fi Fine Timing Measurements Through Range Filtering and Fingerprinting Methods”, IEEE 31st Annual International Symposium on Personal, Indoor and Mobile Radio Communications (PIMRC), 2020.
```
@INPROCEEDINGS{Huilla2020,
AUTHOR="Sami Huilla and Chrysanthos Pepi and Michalis Antoniou and Christos
Laoudias and Seppo Horsmanheimo and Sergio Lembo and Matti Laukkanen and
Georgios Ellinas",
TITLE="Indoor Fingerprinting with {Wi-Fi} Fine Timing Measurements Through Range
Filtering and Fingerprinting Methods",
BOOKTITLE="IEEE 31st Annual International Symposium on Personal, Indoor and
Mobile Radio Communications (PIMRC) to appear",
ADDRESS="London, United Kingdom (Great Britain)",
DAYS=30,
MONTH=aug,
YEAR=2020"}
```
## License
European Union Public License (EUPL) v1.2
