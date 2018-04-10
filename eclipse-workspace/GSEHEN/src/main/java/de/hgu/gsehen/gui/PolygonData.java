package de.hgu.gsehen.gui;

public class PolygonData {
  private double[] pointsX;
  private double[] pointsY;
  private int pointsCount;

  /**
   * Constructor for polygon data.
   *
   * @param pointsX the polygon's points' x values
   * @param pointsY the polygon's points' y values
   */
  public PolygonData(double[] pointsX, double[] pointsY) {
    int length = pointsX.length;
    if (pointsY.length != length) {
      throw new IllegalArgumentException("x and y array must have the same length");
    }
    this.pointsX = pointsX;
    this.pointsY = pointsY;
    this.pointsCount = length;
  }

  public double[] getPointsX() {
    return pointsX;
  }

  public double[] getPointsY() {
    return pointsY;
  }

  public int getPointsCount() {
    return pointsCount;
  }
}
