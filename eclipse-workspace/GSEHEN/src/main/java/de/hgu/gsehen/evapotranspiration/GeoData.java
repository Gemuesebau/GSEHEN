package de.hgu.gsehen.evapotranspiration;

/**
 * Class to represent the geographical data needed to calculate the et0.
 *
 * @author MO
 *
 */
public class GeoData {

  private boolean mean;
  private double geoLen;
  private double geoWid;
  private double heighAbvNn;

  public boolean isMean() {
    return mean;
  }

  public void setMean(boolean mean) {
    this.mean = mean;
  }

  public double getGeoLen() {
    return geoLen;
  }

  public void setGeoLen(double geoLen) {
    this.geoLen = geoLen;
  }

  public double getGeoWid() {
    return geoWid;
  }

  public void setGeoWid(double geoWid) {
    this.geoWid = geoWid;
  }

  public double getHeighAbvNn() {
    return heighAbvNn;
  }

  public void setHeighAbvNn(double heighAbvNn) {
    this.heighAbvNn = heighAbvNn;
  }

  /**
   * Constructor GeoData class. Typical values for Geisenheim:
   * geoLen=7.95,geoWid=49.99,heighAbvNn=110
   *
   * @param mean Calculation only with Temperature mean? TRUE = yes, FALSE = no
   * @param geoLen Geographical length of the location your are calculating et0 from in degree
   * @param geoWid Geographical width of the location your are calculating et0 from in degree
   * @param heighAbvNn Height over normal null in m
   */
  public GeoData(boolean mean, double geoLen, double geoWid, double heighAbvNn) {
    super();
    this.mean = mean;
    this.geoLen = geoLen;
    this.geoWid = geoWid;
    this.heighAbvNn = heighAbvNn;
  }
}
