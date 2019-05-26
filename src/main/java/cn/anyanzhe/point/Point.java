package cn.anyanzhe.point;

import cn.anyanzhe.constant.Constant;

public class Point {
  public String timestamp;
  public double value;
  public int sequence;
  public int status;

  public Point(String timestamp, double value, int sequence) {
    this.timestamp = timestamp;
    this.value = value;
    this.sequence = sequence;
    this.status = Constant.ORDINARY;
  }
}
