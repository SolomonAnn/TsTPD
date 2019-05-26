package cn.anyanzhe.experiment;

import cn.anyanzhe.fileops.CsvFileReader;
import cn.anyanzhe.point.Point;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

public class Experiment
{
//  private static final String ACCURATE = "random.csv";
//  private static final String APPROXIMATE = "vrseg.csv";
//  private static final String ACCURATE = "sample.csv";
  private static final String ACCURATE = "stock.csv";
  private static final String APPROXIMATE = "vsegmentation.csv";

  private static List<Point> getPoints(String filename) {
    List<Point> points = new ArrayList<>();
    CsvFileReader csvFileReader = new CsvFileReader();
    Iterable<CSVRecord> csvRecords = csvFileReader.readRecords(filename);
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

  private static double calculateResidualErrorSum(List<Point> points, List<Point> turningPoints) {
    int pointsSize = points.size();
    int turningPointsSize = turningPoints.size();
    Point head = turningPoints.get(0);
    Point tail;
    Point tempPoint;
    double k;
    double b;
    double result = 0.0;

    for (int i = 1; i < turningPointsSize; i++) {
      tail = turningPoints.get(i);

      k = (tail.value - head.value) / (tail.sequence - head.sequence);
      b = (head.value * tail.sequence - tail.value * head.sequence) / (tail.sequence - head.sequence);

      for (int j = head.sequence + 1; j < tail.sequence; j++) {
        tempPoint = points.get(j);
        result += (tempPoint.value - k * tempPoint.sequence - b) * (tempPoint.value - k * tempPoint.sequence - b);
      }

      head = tail;
    }

    return result;
  }

  public static void main(String[] args) {
    List<Point> points = getPoints(ACCURATE);
    List<Point> turningPoints = getPoints(APPROXIMATE);

    System.out.println(calculateResidualErrorSum(points, turningPoints));
  }
}
