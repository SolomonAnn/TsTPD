package cn.anyanzhe.fileops;

import cn.anyanzhe.constant.Constant;
import cn.anyanzhe.point.Point;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvFileWriter {
  public void writeRecords(String filename, List<Point> points) {
    try {
      Appendable printWriter = new PrintWriter(filename);
      CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(Constant.HEADER);
      CSVPrinter csvPrinter = csvFormat.print(printWriter);
      for (Point point : points) {
        csvPrinter.printRecord(point.timestamp, Double.toString(point.value), Integer.toString(point.status));
      }
      csvPrinter.flush();
      csvPrinter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
