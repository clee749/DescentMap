package util;

public class LineInfo {
  public final double a;
  public final double b;
  public final double c;
  public final double hypot_a_b;

  public LineInfo(double x, double y, double direction) {
    // (y - y0) = m * (x - x0);
    // y - y0 = m * x - m * x0;
    // 0 = m * x - y + y0 - m * x0;
    a = Math.tan(direction);
    b = -1;
    c = y - a * x;
    hypot_a_b = Math.hypot(a, b);
  }
}
