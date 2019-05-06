package cn.anyanzhe.point;

import cn.anyanzhe.constant.Constant;

public class Point {
  public String timestamp;
  public double value;
  public int status;

  public Point(String timestamp, double value) {
    this.timestamp = timestamp;
    this.value = value;
    this.status = Constant.ORDINARY;
  }
}
