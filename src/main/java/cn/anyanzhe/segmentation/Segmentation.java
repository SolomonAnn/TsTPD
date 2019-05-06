package cn.anyanzhe.segmentation;

import cn.anyanzhe.constant.Constant;
import cn.anyanzhe.fileops.CsvFileReader;
import cn.anyanzhe.fileops.CsvFileWriter;
import cn.anyanzhe.point.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

public class Segmentation {
  private static final String INPUTFILENAME = "sample.csv";
  private static final String OUTPUTFILENAME = "segmentation.csv";

  private static boolean hasUpperBound = true;
  private static boolean hasLowerBound = true;
  private static double upperBound = 17;
  private static double lowerBound = 0.0;
  private static double threshold = 1.0;

  private static int sign(double x, double y) {
    if (x > y) {
      return Constant.DOWN;
    } else if (x == y) {
      return Constant.STABLE;
    } else {
      return Constant.UP;
    }
  }

  private static int isFirstTurningPoint(int prevSign, int nextSign) {
    if (prevSign == Constant.UP && nextSign == Constant.DOWN) {
      return Constant.PEAK;
    }
    if (prevSign == Constant.DOWN && nextSign == Constant.UP) {
      return Constant.VALLEY;
    }
    return Constant.ORDINARY;
  }

  private static int isSecondTurningPoint(int prevSign, int nextSign) {
    if (prevSign == Constant.UP && nextSign == Constant.STABLE) {
      return Constant.UP2STABLE;
    }
    if (prevSign == Constant.STABLE) {
      if (nextSign == Constant.UP) {
        return Constant.STABLE2UP;
      } else if (nextSign == Constant.DOWN) {
        return Constant.STABLE2DOWN;
      }
    }
    if (prevSign == Constant.DOWN && nextSign == Constant.STABLE) {
      return Constant.DOWN2STABLE;
    }
    return Constant.ORDINARY;
  }

  private static int isTurningPoint(Point prevPoint, Point centerPoint, Point nextPoint) {
    int prevSign;
    int nextSign;
    int firstResult;
    int secondResult;

    prevSign = sign(prevPoint.value, centerPoint.value);
    nextSign = sign(centerPoint.value, nextPoint.value);
    firstResult = isFirstTurningPoint(prevSign, nextSign);
    if (firstResult != Constant.ORDINARY) {
      return firstResult;
    } else {
      secondResult = isSecondTurningPoint(prevSign, nextSign);
      if (secondResult != Constant.ORDINARY) {
        return secondResult;
      }
    }

    return Constant.ORDINARY;
  }

  private static double calculateDistance(double k, double b, double currValue, int currIndex) {
    return Math.abs(currValue - k * currIndex - b) / Math.sqrt(k * k + 1);
  }

  private static List<Point> findThirdTurningPoints(List<Point> points, int startIndex, int endIndex) {
    List<Point> turningPoints = new LinkedList<>();
    Point startPoint = points.get(startIndex);
    Point endPoint = points.get(endIndex);
    Point currPoint;
    double k = (endPoint.value - startPoint.value) / (endIndex - startIndex);
    double b = (startPoint.value * endIndex - endPoint.value * startIndex) / (endIndex - startIndex);

    for (int i = startIndex + 1; i < endIndex; i++) {
      currPoint = points.get(i);
      if (calculateDistance(k, b, currPoint.value, i) > threshold) {
        currPoint.status = Constant.THIRDTP;
        turningPoints.add(currPoint);
      }
    }
    return turningPoints;
  }

  public static void main(String[] args) {
    CsvFileReader csvFileReader;
    Iterable<CSVRecord> csvRecords;

    String timestamp;
    double value;

    List<Point> points = new ArrayList<>();
    Point currPoint;
    Point prevPoint = null;
    Point nextPoint;
    Point centerPoint = null;
    List<Point> turningPoints = new LinkedList<>();
    List<Point> thirdTurningPoints;

    int result;

    int index = 0;
    int startIndex = 0;
    int endIndex = 0;

    csvFileReader = new CsvFileReader();
    csvRecords = csvFileReader.readRecords(INPUTFILENAME);
    for (CSVRecord csvRecord : csvRecords) {
      timestamp = csvRecord.get("Timestamp");
      value = Double.parseDouble(csvRecord.get("Value"));
      currPoint = new Point(timestamp, value);
      points.add(currPoint);
      index += 1;

      if (prevPoint == null) {
        prevPoint = currPoint;
        continue;
      }
      if (centerPoint == null) {
        centerPoint = currPoint;
        continue;
      }
      nextPoint = currPoint;

      result = isTurningPoint(prevPoint, centerPoint, nextPoint);
      if (result != Constant.ORDINARY) {
        centerPoint.status = result;
        if (result == Constant.PEAK && hasUpperBound
            && Math.abs(centerPoint.value - upperBound) > threshold) {
          centerPoint.status = Constant.ILL;
        }
        if (result == Constant.VALLEY && hasLowerBound
            && Math.abs(centerPoint.value - lowerBound) > threshold) {
          centerPoint.status = Constant.ILL;
        }
        turningPoints.add(centerPoint);
        if (startIndex == 0) {
          startIndex = index - 2;
        } else if (endIndex == 0) {
          endIndex = index - 2;
        } else {
          startIndex = endIndex;
          endIndex = index - 2;
        }
      }

      if (startIndex != 0 && endIndex != 0 && endIndex - startIndex > 1) {
        thirdTurningPoints = findThirdTurningPoints(points, startIndex, endIndex);
        if (thirdTurningPoints != null) {
          for (Point point : thirdTurningPoints) {
            turningPoints.add(point);
          }
        }
      }

      prevPoint = centerPoint;
      centerPoint = nextPoint;
    }
    CsvFileWriter csvFileWriter = new CsvFileWriter();
    csvFileWriter.writeRecords(OUTPUTFILENAME, turningPoints);
  }
}
