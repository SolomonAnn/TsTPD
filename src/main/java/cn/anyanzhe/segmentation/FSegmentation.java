package cn.anyanzhe.segmentation;

import cn.anyanzhe.constant.Constant;
import cn.anyanzhe.fileops.CsvFileReader;
import cn.anyanzhe.fileops.CsvFileWriter;
import cn.anyanzhe.point.Point;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

// F: Financial
public class FSegmentation {
  /**
   ************************
   ** Input Filename      *
   ************************
   ** random_walk.csv     *
   ** cycle_time.csv      *
   ** stock_price.csv     *
   ************************
   ** Output Filename     *
   ************************
   ** fr_segmentation.csv *
   ** fc_segmentation.csv *
   ** fs_segmentation.csv *
   ************************
   **/
  private static final String INPUTFILENAME = "dataset\\cycle_time.csv";
  private static final String OUTPUTFILENAME = "dataset\\fc_segmentation.csv";
  private static final double EPSILON = 0.05;

  private static List<Point> getPoints() {
    List<Point> points = new ArrayList<>();
    CsvFileReader csvFileReader = new CsvFileReader();
    Iterable<CSVRecord> csvRecords = csvFileReader.readRecords(INPUTFILENAME);
    String timestamp;
    double value;
    int sequence;

    for (CSVRecord csvRecord : csvRecords) {
      timestamp = csvRecord.get("Timestamp");
      value = Double.parseDouble(csvRecord.get("Value"));
      sequence = Integer.parseInt(timestamp);
      points.add(new Point(timestamp, value, sequence));
    }

    return points;
  }

  private static int isTP(Point prevPoint, Point currPoint, Point nextPoint) {
    if (prevPoint.value < currPoint.value && currPoint.value > nextPoint.value) {
      return Constant.PEAK;
    }
    if (prevPoint.value > currPoint.value && currPoint.value < nextPoint.value) {
      return Constant.VALLEY;
    }
    return Constant.ORDINARY;
  }

  private static List<Point> findTPs(List<Point> points) {
    int size = points.size();
    if (size < 3) {
      return null;
    }

    List<Point> turningPoints = new ArrayList<>();
    Point prevPoint = points.get(0);
    Point currPoint = points.get(1);
    Point nextPoint;
    int sign;

    for (int i = 2; i < size; i++) {
      nextPoint = points.get(i);
      sign = isTP(prevPoint, currPoint, nextPoint);
      if (sign != Constant.ORDINARY) {
        currPoint.status = sign;
        turningPoints.add(currPoint);
      }

      prevPoint = currPoint;
      currPoint = nextPoint;
    }

    return turningPoints;
  }

  private static boolean containsITPsInUptrend(Point firstPoint, Point secondPoint,
      Point thirdPoint, Point fourthPoint) {
    if (firstPoint.status == Constant.VALLEY && secondPoint.status == Constant.PEAK &&
        thirdPoint.status == Constant.VALLEY && fourthPoint.status == Constant.PEAK &&
        firstPoint.value < thirdPoint.value && secondPoint.value < fourthPoint.value &&
        Math.abs(secondPoint.value - thirdPoint.value) <
            Math.abs(firstPoint.value - thirdPoint.value)
                + Math.abs(secondPoint.value - fourthPoint.value)) {
      return true;
    }
    return false;
  }

  private static boolean containsITPsInDowntrend(Point firstPoint, Point secondPoint,
      Point thirdPoint, Point fourthPoint) {
    if (firstPoint.status == Constant.PEAK && secondPoint.status == Constant.VALLEY &&
        thirdPoint.status == Constant.PEAK && fourthPoint.status == Constant.VALLEY &&
        firstPoint.value > thirdPoint.value && secondPoint.value > fourthPoint.value &&
        Math.abs(secondPoint.value - thirdPoint.value) <
            Math.abs(firstPoint.value - thirdPoint.value)
                + Math.abs(secondPoint.value - fourthPoint.value)) {
      return true;
    }
    return false;
  }

  private static boolean containsITPsInSameTrend(Point firstPoint, Point secondPoint,
      Point thirdPoint, Point fourthPoint) {
    if (Math.abs(firstPoint.value - thirdPoint.value) < firstPoint.value * EPSILON &&
        Math.abs(secondPoint.value - fourthPoint.value) < fourthPoint.value * EPSILON) {
      return true;
    }
    return false;
  }

  private static List<Point> eliminateITPs(List<Point> turningPoints) {
    int size = turningPoints.size();
    if (size < 4) {
      return turningPoints;
    }

    List<Point> newTurningPoints = new ArrayList<>();
    Point firstPoint;
    Point secondPoint;
    Point thirdPoint;
    Point fourthPoint;

    for (int i = 0; i < size - 3; i++) {
      firstPoint = turningPoints.get(i);
      secondPoint = turningPoints.get(i + 1);
      thirdPoint = turningPoints.get(i + 2);
      fourthPoint = turningPoints.get(i + 3);
      if (containsITPsInUptrend(firstPoint, secondPoint, thirdPoint, fourthPoint) ||
          containsITPsInDowntrend(firstPoint, secondPoint, thirdPoint, fourthPoint) ||
          containsITPsInSameTrend(firstPoint, secondPoint, thirdPoint, fourthPoint)) {
        newTurningPoints.add(firstPoint);
        newTurningPoints.add(fourthPoint);
        i += 3;
      } else {
        newTurningPoints.add(firstPoint);
      }
    }

    return newTurningPoints;
  }

  public static void main(String[] args) {
    List<Point> points = getPoints();
    List<Point> turningPoints = findTPs(points);
    List<Point> newTurningPoints = eliminateITPs(turningPoints);

    CsvFileWriter csvFileWriter = new CsvFileWriter();
    csvFileWriter.writeRecords(OUTPUTFILENAME, newTurningPoints);
  }
}
