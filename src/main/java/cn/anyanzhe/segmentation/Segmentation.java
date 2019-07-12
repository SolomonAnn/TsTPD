package cn.anyanzhe.segmentation;

import cn.anyanzhe.constant.Constant;
import cn.anyanzhe.fileops.CsvFileReader;
import cn.anyanzhe.fileops.CsvFileWriter;
import cn.anyanzhe.point.Point;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

public class Segmentation {
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
   ** r_segmentation.csv *
   ** c_segmentation.csv *
   ** s_segmentation.csv *
   ************************
   **/
  private static final String INPUTFILENAME = "dataset\\sample.csv";
  private static final String OUTPUTFILENAME = "dataset\\s_segmentation.csv";
  private static final double EPSILON = 100.0;
  private static final double EPSILON1 = 100.0;
  private static final double EPSILON2 = 0.5;
  private static final int THETA = 5;

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

  private static int sign(double x, double y) {
    if (x > y) {
      return Constant.DOWN;
    } else if (x == y) {
      return Constant.STABLE;
    }
    return Constant.UP;
  }

  private static int isFTP(int prevSign, int nextSign) {
    if (prevSign == Constant.UP && nextSign == Constant.DOWN) {
      return Constant.PEAK;
    }
    if (prevSign == Constant.DOWN && nextSign == Constant.UP) {
      return Constant.VALLEY;
    }
    return Constant.ORDINARY;
  }

  private static int isSTP(Point prevPoint, Point currPoint, Point nextPoint) {
    int prevSign = sign(prevPoint.value, currPoint.value);
    int nextSign = sign(currPoint.value, nextPoint.value);
    int isFirstTurningPoint = isFTP(prevSign, nextSign);

    if (isFirstTurningPoint != Constant.ORDINARY) {
      return isFirstTurningPoint;
    }
    if (prevSign != nextSign) {
      return Constant.SECONDTP;
    }

    return Constant.ORDINARY;
  }

  private static List<Point> findSTPs(List<Point> points) {
    int size = points.size();
    if (size < 3) {
      return null;
    }

    List<Point> secondTurningPoints = new ArrayList<>();
    Point prevPoint = points.get(0);
    Point currPoint = points.get(1);
    Point nextPoint;
    int isSecondTurningPoint;

    for (int i = 2; i < size; i++) {
      nextPoint = points.get(i);
      isSecondTurningPoint = isSTP(prevPoint, currPoint, nextPoint);
      if (isSecondTurningPoint != Constant.ORDINARY) {
        currPoint.status = isSecondTurningPoint;
        secondTurningPoints.add(currPoint);
      }

      prevPoint = currPoint;
      currPoint = nextPoint;
    }

    return secondTurningPoints;
  }

  private static double calculateDistance(Point currPoint,
      double slope, double intercept, double constant) {
    return Math.abs(currPoint.value - slope * currPoint.sequence - intercept) / constant;
  }

  public static List<Point> findTTPs(List<Point> points, List<Point> secondTurningPoints) {
    int size = secondTurningPoints.size();
    if (size < 2) {
      return null;
    }

    List<Point> thirdTurningPoints = new ArrayList<>();

    Point head;
    Point tail;
    Point currPoint;
    double slope;
    double intercept;
    double constant;

    for (int i = 0; i < size - 1; i++) {
      head = secondTurningPoints.get(i);
      tail = secondTurningPoints.get(i + 1);
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
          thirdTurningPoints.add(currPoint);
        }
      }
    }

    return thirdTurningPoints;
  }

  private static List<Point> mergePoints(List<Point> secondTurningPoints, List<Point> thirdTurningPoints) {
    List<Point> turningPoints = new ArrayList<>();
    int secondSize = secondTurningPoints.size();
    int thirdSize = thirdTurningPoints.size();
    int secondIndex = 0;
    int thirdIndex = 0;
    Point secondPoint;
    Point thirdPoint;

    while (secondIndex < secondSize && thirdIndex < thirdSize) {
      secondPoint = secondTurningPoints.get(secondIndex);
      thirdPoint = thirdTurningPoints.get(thirdIndex);

      if (secondPoint.sequence < thirdPoint.sequence) {
        turningPoints.add(secondPoint);
        secondIndex++;
      } else {
        turningPoints.add(thirdPoint);
        thirdIndex++;
      }
    }

    for (int i = secondIndex; i < secondSize; i++) {
      turningPoints.add(secondTurningPoints.get(i));
    }
    for (int i = thirdIndex; i < thirdSize; i++) {
      turningPoints.add(thirdTurningPoints.get(i));
    }

    return turningPoints;
  }

  private static boolean isITPs(Point firstPoint, Point secondPoint, Point thirdPoint, Point fourthPoint) {
    double slope;
    double intercept;
    double constant;

    if (((firstPoint.value > secondPoint.value && firstPoint.value > thirdPoint.value) &&
        (fourthPoint.value < secondPoint.value && fourthPoint.value < thirdPoint.value)) ||
        ((firstPoint.value < secondPoint.value && firstPoint.value < thirdPoint.value) &&
        (fourthPoint.value > secondPoint.value && fourthPoint.value > thirdPoint.value))) {
      slope = (fourthPoint.value - firstPoint.value) / (fourthPoint.sequence - firstPoint.sequence);
      intercept = (firstPoint.value * fourthPoint.sequence - fourthPoint.value * firstPoint.sequence)
          / (fourthPoint.sequence - firstPoint.sequence);
      constant = Math.sqrt(slope * slope + 1);
      if (calculateDistance(secondPoint, slope, intercept, constant) < EPSILON1 &&
          calculateDistance(thirdPoint, slope, intercept, constant) < EPSILON1) {
        return true;
      }
    }

//    if (Math.abs((firstPoint.value - thirdPoint.value)) < EPSILON2 &&
//        Math.abs((secondPoint.value - fourthPoint.value)) < EPSILON2 &&
//        firstPoint.sequence - fourthPoint.sequence < THETA) {
//      return true;
//    }

    return false;
  }

  private static List<Point> filterITPs(List<Point> turningPoints) {
    int size = turningPoints.size();
    if (size < 4) {
      return turningPoints;
    }

    List<Point> significantTurningPoints = new ArrayList<>();
    Point firstPoint;
    Point secondPoint;
    Point thirdPoint;
    Point fourthPoint;

    for (int i = 0; i < size - 3; i++) {
      firstPoint = turningPoints.get(i);
      secondPoint = turningPoints.get(i + 1);
      thirdPoint = turningPoints.get(i + 2);
      fourthPoint = turningPoints.get(i + 3);

      if (isITPs(firstPoint, secondPoint, thirdPoint, fourthPoint)) {
        significantTurningPoints.add(firstPoint);
        significantTurningPoints.add(fourthPoint);
        i += 3;
      } else {
        significantTurningPoints.add(firstPoint);
      }
    }

    return significantTurningPoints;
  }

  public static void main(String[] args) {
    List<Point> points = getPoints();
    List<Point> secondTurningPoints = findSTPs(points);
    List<Point> thirdTurningPoints = findTTPs(points, secondTurningPoints);
    List<Point> turningPoints = mergePoints(secondTurningPoints, thirdTurningPoints);
    List<Point> significantTurningPoints = filterITPs(turningPoints);

    CsvFileWriter csvFileWriter = new CsvFileWriter();
    csvFileWriter.writeRecords(OUTPUTFILENAME, significantTurningPoints);
  }
}

//public class Segmentation {
//  private static final String INPUTFILENAME = "stock.csv";
////  private static final String INPUTFILENAME = "random.csv";
////  private static final String OUTPUTFILENAME = "rseg.csv";
////  private static final String INPUTFILENAME = "sample.csv";
//  private static final String OUTPUTFILENAME = "segmentation.csv";
//
//  private static boolean hasUpperBound = false;
//  private static boolean hasLowerBound = false;
//  private static double upperBound = 17;
//  private static double lowerBound = 0.0;
//  private static double threshold = 5.0;
//
//  private static int sign(double x, double y) {
//    if (x > y) {
//      return Constant.DOWN;
//    } else if (x == y) {
//      return Constant.STABLE;
//    } else {
//      return Constant.UP;
//    }
//  }
//
//  private static int isFirstTurningPoint(int prevSign, int nextSign) {
//    if (prevSign == Constant.UP && nextSign == Constant.DOWN) {
//      return Constant.PEAK;
//    }
//    if (prevSign == Constant.DOWN && nextSign == Constant.UP) {
//      return Constant.VALLEY;
//    }
//    return Constant.ORDINARY;
//  }
//
//  private static int isSecondTurningPoint(int prevSign, int nextSign) {
//    if (prevSign == Constant.UP && nextSign == Constant.STABLE) {
//      return Constant.SECONDTP;
//    }
//    if (prevSign == Constant.STABLE) {
//      if (nextSign == Constant.UP) {
//        return Constant.SECONDTP;
//      } else if (nextSign == Constant.DOWN) {
//        return Constant.SECONDTP;
//      }
//    }
//    if (prevSign == Constant.DOWN && nextSign == Constant.STABLE) {
//      return Constant.SECONDTP;
//    }
//    return Constant.ORDINARY;
//  }
//
//  private static int isTurningPoint(Point prevPoint, Point centerPoint, Point nextPoint) {
//    int prevSign;
//    int nextSign;
//    int firstResult;
//    int secondResult;
//
//    prevSign = sign(prevPoint.value, centerPoint.value);
//    nextSign = sign(centerPoint.value, nextPoint.value);
//    firstResult = isFirstTurningPoint(prevSign, nextSign);
//    if (firstResult != Constant.ORDINARY) {
//      return firstResult;
//    } else {
//      secondResult = isSecondTurningPoint(prevSign, nextSign);
//      if (secondResult != Constant.ORDINARY) {
//        return secondResult;
//      }
//    }
//
//    return Constant.ORDINARY;
//  }
//
//  private static double calculateDistance(double k, double b, double currValue, int currIndex) {
//    return Math.abs(currValue - k * currIndex - b) / Math.sqrt(k * k + 1);
//  }
//
//  private static List<Integer> findThirdTurningPoints(List<Point> points, int startIndex, int endIndex) {
//    List<Integer> thirdTurningPoints = new LinkedList<>();
//    Point startPoint = points.get(startIndex);
//    Point endPoint = points.get(endIndex);
//    Point currPoint;
//    double k = (endPoint.value - startPoint.value) / (endIndex - startIndex);
//    double b = (startPoint.value * endIndex - endPoint.value * startIndex) / (endIndex - startIndex);
//
//    for (int i = startIndex + 1; i < endIndex; i++) {
//      currPoint = points.get(i);
//      if (calculateDistance(k, b, currPoint.value, i) > threshold) {
//        currPoint.status = Constant.THIRDTP;
//        thirdTurningPoints.add(i);
//      }
//    }
//    return thirdTurningPoints;
//  }
//
//  private static List<Point> updateMarks(List<Point> points, Map<Integer, Integer> marks) {
//    Point point;
//    int key;
//    for (Map.Entry<Integer, Integer> entry : marks.entrySet()) {
//      key = entry.getKey();
//      point = points.get(key);
//      point.status = entry.getValue();
//      points.set(key, point);
//    }
//    return points;
//  }
//
//  private static List<Point> getPoints() {
//    List<Point> points = new ArrayList<>();
//    CsvFileReader csvFileReader = new CsvFileReader();
//    Iterable<CSVRecord> csvRecords = csvFileReader.readRecords(INPUTFILENAME);
//    String timestamp;
//    double value;
//    int sequence = 0;
//    for (CSVRecord csvRecord : csvRecords) {
//      timestamp = csvRecord.get("Timestamp");
//      value = Double.parseDouble(csvRecord.get("Value"));
//      points.add(new Point(timestamp, value, sequence));
//      sequence++;
//    }
//    return points;
//  }
//
//  private static TreeMap<Integer, Integer> markSecondTurningPoints(List<Point> points) {
//    int size = points.size();
//    TreeMap<Integer, Integer> marks = new TreeMap<>();
//    int mark;
//    Point currPoint;
//    Point prevPoint;
//    Point nextPoint;
//
//    for (int i = 1; i < size - 1; i++) {
//      prevPoint = points.get(i - 1);
//      currPoint = points.get(i);
//      nextPoint = points.get(i + 1);
//      mark = isTurningPoint(prevPoint, currPoint, nextPoint);
//      if (mark != Constant.ORDINARY) {
////        if (mark == Constant.PEAK && hasUpperBound
////            && Math.abs(currPoint.value - upperBound) > threshold) {
////          mark= Constant.ILL;
////        }
////        if (mark == Constant.VALLEY && hasLowerBound
////            && Math.abs(currPoint.value - lowerBound) > threshold) {
////          mark = Constant.ILL;
////        }
//        marks.put(i, mark);
//      }
//    }
//    return marks;
//  }
//
//  private static TreeMap<Integer, Integer> markThirdTurningPoints(List<Point> points,
//      TreeMap<Integer, Integer> marks) {
//    TreeMap<Integer, Integer> newMarks = new TreeMap<>(marks);
//    int key;
//    int higherKey;
//    Point startPoint;
//    Point endPoint;
//    Point currPoint;
//
//    for (Entry<Integer, Integer> entry : marks.entrySet()) {
//      key = entry.getKey();
//      if (marks.higherKey(key) == null) {
//        break;
//      }
//      higherKey = marks.higherKey(key);
//      startPoint = points.get(key);
//      endPoint = points.get(higherKey);
//
//      double k = (endPoint.value - startPoint.value) / (higherKey - key);
//      double b = (startPoint.value * higherKey - endPoint.value * key) / (higherKey - key);
//
//      for (int i = key + 1; i < higherKey; i++) {
//        currPoint = points.get(i);
//        if (calculateDistance(k, b, currPoint.value, i) > threshold) {
//          newMarks.put(i, Constant.THIRDTP);
//        }
//      }
//    }
//    return newMarks;
//  }
//
//  public static TreeMap<Integer, Integer> eliminateInsignificantTurningPoints(List<Point> points,
//      TreeMap<Integer, Integer> marks) {
//    TreeMap<Integer, Integer> newMarks = new TreeMap<>(marks);
//    int firstKey;
//    int secondKey;
//    int thirdKey;
//    int fourthKey;
//    Point firstPoint;
//    Point secondPoint;
//    Point thirdPoint;
//    Point fourthPoint;
//
//    if (marks.size() < 4) {
//      return newMarks;
//    }
//    for (Entry<Integer, Integer> entry : marks.entrySet()) {
//      firstKey = entry.getKey();
//      secondKey = marks.higherKey(firstKey);
//      thirdKey = marks.higherKey(secondKey);
//      if (marks.higherKey(thirdKey) == null) {
//        break;
//      }
//      fourthKey = marks.higherKey(thirdKey);
//
//      firstPoint = points.get(firstKey);
//      secondPoint = points.get(secondKey);
//      thirdPoint = points.get(thirdKey);
//      fourthPoint = points.get(fourthKey);
//
//      if ((firstPoint.value < secondPoint.value && firstPoint.value < thirdPoint.value
//          && fourthPoint.value > secondPoint.value && fourthPoint.value > thirdPoint.value) ||
//          (firstPoint.value > secondPoint.value && firstPoint.value > thirdPoint.value
//              && fourthPoint.value < secondPoint.value && fourthPoint.value < thirdPoint.value)) {
//        double k = (fourthPoint.value - firstPoint.value) / (fourthKey - firstKey);
//        double b = (firstPoint.value * fourthKey - fourthPoint.value * firstKey) / (fourthKey - firstKey);
//
//        if (calculateDistance(k, b, secondPoint.value, secondKey) < threshold) {
//          newMarks.remove(secondKey);
//        }
//        if (calculateDistance(k, b, fourthPoint.value, fourthKey) < threshold) {
//          newMarks.remove(fourthKey);
//        }
//      }
//    }
//    return newMarks;
//  }
//
//  private static List<Point> getTurningPoints(List<Point> points, Map<Integer, Integer> marks) {
//    List<Point> turningPoints = new LinkedList<>();
//    for (Integer key : marks.keySet()) {
//      turningPoints.add(points.get(key));
//    }
//    return turningPoints;
//  }
//
//  public static void main(String[] args) {
//    List<Point> points = getPoints();
//    List<Point> newPoints = new ArrayList<>();
//    TreeMap<Integer, Integer> marks;
//    int size = points.size();
//
//    for (int i = 0; i < size; i++) {
//      newPoints.add(points.get(i));
//    }
//
//    marks = markSecondTurningPoints(newPoints);
//    newPoints = updateMarks(newPoints, marks);
//    marks = markThirdTurningPoints(newPoints, marks);
//    newPoints = updateMarks(newPoints, marks);
//    marks = eliminateInsignificantTurningPoints(newPoints, marks);
//    for (int i = 0; i < size; i++) {
//      Point currPoint = points.get(i);
//      points.set(i, new Point(currPoint.timestamp, currPoint.value, currPoint.sequence));
//    }
//    points = updateMarks(points, marks);
//    CsvFileWriter csvFileWriter = new CsvFileWriter();
//    csvFileWriter.writeRecords(OUTPUTFILENAME, points);
////    secondTurningPoints = getTurningPoints(points, marks);
//
////    size = points.size();
////    for (int i = 0; i < size; i++) {
////      currPoint = points.get(i);
////      if (prevPoint == null) {
////        prevPoint = currPoint;
////        continue;
////      }
////      if (centerPoint == null) {
////        centerPoint = currPoint;
////        continue;
////      }
////      nextPoint = currPoint;
////
////      result = isTurningPoint(prevPoint, centerPoint, nextPoint);
////      if (result != Constant.ORDINARY) {
////        centerPoint.status = result;
////        if (result == Constant.PEAK && hasUpperBound
////            && Math.abs(centerPoint.value - upperBound) > threshold) {
////          centerPoint.status = Constant.ILL;
////        }
////        if (result == Constant.VALLEY && hasLowerBound
////            && Math.abs(centerPoint.value - lowerBound) > threshold) {
////          centerPoint.status = Constant.ILL;
////        }
////        points.set(centerPoint.sequence, centerPoint);
////        secondTurningPoints.add(centerPoint);
////      }
////
////      prevPoint = centerPoint;
////      centerPoint = nextPoint;
////    }
////
////    size = secondTurningPoints.size();
////    for (int i = 0; i < size - 1; i++) {
////      thirdTurningPoints = findThirdTurningPoints(points, secondTurningPoints.get(i).sequence, secondTurningPoints.get(i + 1).sequence);
////      if (thirdTurningPoints != null) {
////        for (Integer thirdTurningPoint : thirdTurningPoints) {
////          currPoint = points.get(thirdTurningPoint);
////          currPoint.status = Constant.THIRDTP;
////          points.set(thirdTurningPoint, currPoint);
////        }
////      }
////    }
//
//
//
////    for (CSVRecord csvRecord : csvRecords) {
////      timestamp = csvRecord.get("Timestamp");
////      value = Double.parseDouble(csvRecord.get("Value"));
////      currPoint = new Point(timestamp, value);
////      points.add(currPoint);
////      index += 1;
////
////      if (prevPoint == null) {
////        prevPoint = currPoint;
////        continue;
////      }
////      if (centerPoint == null) {
////        centerPoint = currPoint;
////        continue;
////      }
////      nextPoint = currPoint;
////
////      result = isTurningPoint(prevPoint, centerPoint, nextPoint);
////      if (result != Constant.ORDINARY) {
////        centerPoint.status = result;
////        if (result == Constant.PEAK && hasUpperBound
////            && Math.abs(centerPoint.value - upperBound) > threshold) {
////          centerPoint.status = Constant.ILL;
////        }
////        if (result == Constant.VALLEY && hasLowerBound
////            && Math.abs(centerPoint.value - lowerBound) > threshold) {
////          centerPoint.status = Constant.ILL;
////        }
////        turningPoints.add(centerPoint);
////        if (startIndex == 0) {
////          startIndex = index - 2;
////        } else if (endIndex == 0) {
////          endIndex = index - 2;
////        } else {
////          startIndex = endIndex;
////          endIndex = index - 2;
////        }
////      }
////
////      if (startIndex != 0 && endIndex != 0 && endIndex - startIndex > 1) {
////        thirdTurningPoints = findThirdTurningPoints(points, startIndex, endIndex);
////        if (thirdTurningPoints != null) {
////          for (Point point : thirdTurningPoints) {
////            turningPoints.add(point);
////          }
////        }
////      }
////
////      prevPoint = centerPoint;
////      centerPoint = nextPoint;
////    }
////    CsvFileWriter csvFileWriter = new CsvFileWriter();
////    csvFileWriter.writeRecords(OUTPUTFILENAME, points);
//  }
//}
