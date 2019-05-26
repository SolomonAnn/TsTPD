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
      int size = points.size();
      Point currPoint;

      for (int i = 0; i < size; i++) {
        currPoint = points.get(i);
        if (currPoint.status != Constant.ORDINARY) {
          csvPrinter.printRecord(currPoint.timestamp,
              Double.toString(currPoint.value),
              Integer.toString(currPoint.status));
        }
      }
      csvPrinter.flush();
      csvPrinter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
