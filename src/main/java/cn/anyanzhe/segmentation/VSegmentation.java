package cn.anyanzhe.segmentation;

import cn.anyanzhe.constant.Constant;
import cn.anyanzhe.fileops.CsvFileReader;
import cn.anyanzhe.fileops.CsvFileWriter;
import cn.anyanzhe.point.Point;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

// V: Vegetation
public class VSegmentation {
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
   ** vr_segmentation.csv *
   ** vc_segmentation.csv *
   ** vs_segmentation.csv *
   ************************
   **/
  private static final String INPUTFILENAME = "dataset\\cycle_time.csv";
  private static final String OUTPUTFILENAME = "dataset\\vc_segmentation.csv";
  private static final double THETA1 = 2.0;
  private static final double THETA2 = 2.0;
  private static final int PHI = 10;
  private static final double EPSILON = 2.0;

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
      sequence++;
    }

    return points;
  }

  private static double mean(List<Point> points, int startIndex, int endIndex) {
    double sum = 0;
    for (int i = startIndex; i <= endIndex; i++) {
      sum += points.get(i).value;
    }
    return sum / (endIndex - startIndex + 1);
  }

  private static List<Point> filterLSPs(List<Point> levelShiftPoints) {
    int size = levelShiftPoints.size();
    Point tempPoint1;
    Point tempPoint2;
    List<Point> newLevelShiftPoints = new ArrayList<>();
    boolean flag;

    for (int i = 0; i < size - 1; i++) {
      for (int j = i + 1; j < size; j++) {
        tempPoint1 = levelShiftPoints.get(i);
        tempPoint2 = levelShiftPoints.get(j);
        if (tempPoint2.value > tempPoint1.value) {
          levelShiftPoints.set(i, tempPoint2);
          levelShiftPoints.set(j, tempPoint1);
        }
      }
    }

    for (int i = 0; i < size; i++) {
      flag = true;
      tempPoint1 = levelShiftPoints.get(i);
      for (int j = i - 1; j > 0; j--) {
        tempPoint2 = levelShiftPoints.get(j);
        if (Math.abs(tempPoint1.sequence - tempPoint2.sequence) < PHI) {
          flag = false;
          break;
        }
      }
      if (flag) {
        tempPoint1.status = Constant.LEVELSHIFT;
        newLevelShiftPoints.add(tempPoint1);
      }
    }

    return newLevelShiftPoints;
  }

  private static List<Point> findLSPs(List<Point> points) {
    int size = points.size();
    if (size < 2 * PHI + 1) {
      return null;
    }

    List<Point> levelShiftPoints = new ArrayList<>();
    Point currPoint;
    Point nextPoint;

    for (int i = PHI; i < size - PHI - 1; i++) {
      currPoint = points.get(i);
      nextPoint = points.get(i + 1);
      if (Math.abs(currPoint.value - nextPoint.value) <= THETA1) {
        continue;
      }
      if (Math.abs(mean(points, i - PHI, i - 1) -
          mean(points, i + 1, i + PHI)) <= THETA2) {
        continue;
      }
      levelShiftPoints.add(currPoint);
    }

    return filterLSPs(levelShiftPoints);
  }

  private static int isPoV(Point prevPoint, Point currPoint, Point nextPoint) {
    if (prevPoint.value < currPoint.value && currPoint.value > nextPoint.value) {
      return Constant.PEAK;
    }
    if (prevPoint.value > currPoint.value && currPoint.value < nextPoint.value) {
      return Constant.VALLEY;
    }
    return Constant.ORDINARY;
  }

  private static List<Point> findPaVs(List<Point> points) {
    int size = points.size();
    if (size < 4) {
      return null;
    }

    List<Point> peaksAndValleys = new ArrayList<>();
    peaksAndValleys.add(points.get(0));
    Point prevPoint = points.get(1);
    Point currPoint = points.get(2);
    Point nextPoint;
    int sign;

    for (int i = 3; i < size; i++) {
      nextPoint = points.get(i);
      sign = isPoV(prevPoint, currPoint, nextPoint);
      if (sign != Constant.ORDINARY) {
        currPoint.status = sign;
        peaksAndValleys.add(currPoint);
      }

      prevPoint = currPoint;
      currPoint = nextPoint;
    }

    return peaksAndValleys;
  }

  private static double calculateDistance(Point currPoint,
      double slope, double intercept, double constant) {
    return Math.abs(currPoint.value - slope * currPoint.sequence - intercept) / constant;
  }

  public static List<Point> findSTPs(List<Point> points, List<Point> peaksAndValleys) {
    int size = peaksAndValleys.size();
    if (size < 2) {
      return null;
    }

    List<Point> sencondTurningPoints = new ArrayList<>();
    Point head;
    Point tail;
    Point currPoint;
    double slope;
    double intercept;
    double constant;

    for (int i = 0; i < size - 1; i++) {
      head = peaksAndValleys.get(i);
      tail = peaksAndValleys.get(i + 1);
      if ((head.status == Constant.PEAK && tail.status == Constant.PEAK) ||
          (head.status == Constant.VALLEY && tail.status == Constant.VALLEY)) {
        continue;
      }
      slope = (tail.value - head.value) / (tail.sequence - head.sequence);
      intercept = (head.value * tail.sequence - tail.value * head.sequence)
          / (tail.sequence - head.sequence);
      constant = Math.sqrt(slope * slope + 1);
      for (int j = head.sequence + 1; j < tail.sequence; j++) {
        currPoint = points.get(j);
        if (calculateDistance(currPoint, slope, intercept, constant) > EPSILON) {
          currPoint.status = Constant.THIRDTP;
          sencondTurningPoints.add(currPoint);
        }
      }
    }

    return sencondTurningPoints;
  }

  public static void main(String[] args) {
    List<Point> points = getPoints();
    List<Point> levelShiftPoints = findLSPs(points);
    List<Point> peaksAndValleys = findPaVs(points);
    List<Point> secondTurningPoints = findSTPs(points, peaksAndValleys);
    List<Point> turningPoints = new ArrayList<>();
    int size;

    size = levelShiftPoints.size();
    for (int i = 0; i < size; i++) {
      turningPoints.add(levelShiftPoints.get(i));
    }
    size = peaksAndValleys.size();
    for (int i = 0; i < size; i++) {
      turningPoints.add(peaksAndValleys.get(i));
    }
    size = secondTurningPoints.size();
    for (int i = 0; i < size; i++) {
      turningPoints.add(secondTurningPoints.get(i));
    }

    CsvFileWriter csvFileWriter = new CsvFileWriter();
    csvFileWriter.writeRecords(OUTPUTFILENAME, turningPoints);
  }
}
