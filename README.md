# sensors-metrics [![Build Status](https://travis-ci.com/eltherion/sensors-metrics.svg?branch=master)](https://travis-ci.org/eltherion/sensors-metrics) [![Coverage Status](https://coveralls.io/repos/github/eltherion/sensors-metrics/badge.svg?branch=master)](https://coveralls.io/github/eltherion/sensors-metrics?branch=master) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
Application that extracts measurements from simple sensors log files and displays basic statistics to standard output.

## Running aplication

### Requirements

* Java JDK 14.0.0 or higher installed and available on search path
* SBT 1.3.10 or higher installed and available on search path
* Git client installed and available on search path
* Cloned repository for this project:

```bash
git clone https://github.com/eltherion/sensors-metrics.git
```

### Starting tests

To run tests navigate to cloned repository folder and execute execute:

```bash
cd /path/to/cloned/repository
sbt ";test;it:test"
```

### Starting application

Navigate to cloned repository folder:

```bash
cd /path/to/cloned/repository
```

Then execute following command providing */path/to/your/input/directory* indicating directory with csv files and one of the available implementations:

#### Monix Task

```bash
sbt "runMain com.datart.sensors.metrics.MainMonixTaskImpl /path/to/your/input/directory"
```
#### Cats Effect IO

```bash
sbt "runMain com.datart.sensors.metrics.MainCatsEffectIOImpl /path/to/your/input/directory"
```
#### Future

```bash
sbt "runMain com.datart.sensors.metrics.MainFutureImpl /path/to/your/input/directory"
```

#### ZIO Task

```bash
sbt "runMain com.datart.sensors.metrics.MainZIOTaskImpl /path/to/your/input/directory"
```

## Background story

The sensors are in a network, and they are divided into groups. Each sensor submits its data to its group leader.
Each leader produces a daily report file for a group. The network periodically re-balances itself, so the sensors could 
change the group assignment over time, and their measurements can be reported by different leaders. The program should 
help spot sensors with highest average humidity.

## Expected Input

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

## Expected Output

- Program prints statistics to StdOut
- It reports how many files it processed
- It reports how many measurements it processed
- It reports how many measurements failed
- For each sensor it calculates min/avg/max humidity
- `NaN` values are ignored from min/avg/max
- Sensors with only `NaN` measurements have min/avg/max as `NaN/NaN/NaN`
- Program sorts sensors by highest avg humidity (`NaN` values go last)

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
