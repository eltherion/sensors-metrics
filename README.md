# sensors-metrics [![Build Status](https://travis-ci.org/eltherion/sensors-metrics.svg?branch=master)](https://travis-ci.org/eltherion/sensors-metrics) [![Coverage Status](https://coveralls.io/repos/github/eltherion/sensors-metrics/badge.svg?branch=master)](https://coveralls.io/github/eltherion/sensors-metrics?branch=master) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
Application that extracts measurements from simple sensors log files and displays basic statistics to standard output.

## Running aplication

### Requirements

### Starting

## Background story

The sensors are in a network, and they are divided into groups. Each sensor submits its data to its group leader.
Each leader produces a daily report file for a group. The network periodically re-balances itself, so the sensors could 
change the group assignment over time, and their measurements can be reported by different leaders. The program should 
help spot sensors with highest average humidity.

## Input

- Program takes one argument: a path to directory
- Directory contains many CSV files (*.csv), each with a daily report from one group leader
- Format of the file: 1 header line + many lines with measurements
- Measurement line has sensor id and the humidity value
- Humidity value is integer in range `[0, 100]` or `NaN` (failed measurement)
- The measurements for the same sensor id can be in the different files

### Example input

leader-1.csv
```
sensor-id,humidity
s1,10
s2,88
s1,NaN
```

leader-2.csv
```
sensor-id,humidity
s2,80
s3,NaN
s2,78
s1,98
```

### Example output

```
Num of processed files: 2
Num of processed measurements: 7
Num of failed measurements: 2

Sensors with highest avg humidity:

sensor-id,min,avg,max
s2,78,82,88
s1,10,54,98
s3,NaN,NaN,NaN
```