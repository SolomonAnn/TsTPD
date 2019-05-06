package cn.anyanzhe.constant;

public class Constant {
  /**
   **********************************
   ** types of a turning point     **
   **********************************
   ** ORDINARY    : ordinary       **
   ** UP2STABLE   : up -> stable   **
   ** PEAK        : up -> down     **
   ** STABLE2UP   : stable -> up   **
   ** STABLE2DOWN : stable -> down **
   ** VALLEY      : down -> up     **
   ** DOWN2STABLE : down -> stable **
   ** THIRDTP     : thrid tp       **
   ** ILL         : ill            **
   **********************************
   **/
  public static final int ORDINARY    = 0;
  public static final int UP2STABLE   = 1;
  public static final int PEAK        = 2;
  public static final int STABLE2UP   = 3;
  public static final int STABLE2DOWN = 4;
  public static final int VALLEY      = 5;
  public static final int DOWN2STABLE = 6;
  public static final int THIRDTP     = 7;
  public static final int ILL         = 8;

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
