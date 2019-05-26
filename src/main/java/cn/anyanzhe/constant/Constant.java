package cn.anyanzhe.constant;

public class Constant {
  //types of a turning point

  public static final int ORDINARY    = 0;
  public static final int PEAK        = 1;
  public static final int VALLEY      = 2;
  public static final int SECONDTP    = 3;
  public static final int THIRDTP     = 4;
  public static final int LEVELSHIFT  = 5;
  public static final int ILL         = 6;

  /**
   ***********************************
   ** comparison between two values **
   ***********************************
   ** m < n                         **
   ** UP        :   T(m) < T(n)     **
   ** STABLE    :   T(m) = T(n)     **
   ** DOWN      :   T(m) > T(n)     **
   ***********************************
   **/
  public static final int UP = 1;
  public static final int STABLE = 0;
  public static final int DOWN = -1;

  public static final String[] HEADER = new String[]{"Timestamp", "Value", "Status"};
}
