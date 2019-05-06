package cn.anyanzhe.fileops;

import java.io.FileReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CsvFileReader {
  public Iterable<CSVRecord> readRecords(String filename) {
    FileReader fileReader;
    CSVFormat csvFormat;
    CSVParser csvParser;
    Iterable<CSVRecord> csvRecords;

    try {
      fileReader = new FileReader(filename);
      csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
      csvParser = csvFormat.parse(fileReader);
      csvRecords = csvParser.getRecords();
      return csvRecords;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
