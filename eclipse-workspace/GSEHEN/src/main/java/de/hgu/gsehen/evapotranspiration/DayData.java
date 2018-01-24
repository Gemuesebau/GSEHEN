package de.hgu.gsehen.evapotranspiration;

import java.sql.Date;

/**
 * Class to represent the environmental data to calculate the GS water balance.
 * 
 * @author MO
 *
 */
public class DayData {


  private Date date;
  private double tempMean;
  private double tempMin;
  private double tempMax;
  private double airHumidityRel;
  private double airHumidityRelMin;
  private double globalRad;
  private double precipitation;
  private double windspeed2m;
  private double et0;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public double getTempMean() {
    return tempMean;
  }

  public void setTempMean(double tempMean) {
    this.tempMean = tempMean;
  }

  public double getTempMin() {
    return tempMin;
  }

  public void setTempMin(double tempMin) {
    this.tempMin = tempMin;
  }

  public double getTempMax() {
    return tempMax;
  }

  public void setTempMax(double tempMax) {
    this.tempMax = tempMax;
  }

  public double getAirHumidityRel() {
    return airHumidityRel;
  }

  public void setAirHumidityRel(double airHumidityRel) {
    this.airHumidityRel = airHumidityRel;
  }

  public double getAirHumidityRelMin() {
    return airHumidityRelMin;
  }

  public void setAirHumidityRelMin(double airHumidityRelMin) {
    this.airHumidityRelMin = airHumidityRelMin;
  }

  public double getGlobalRad() {
    return globalRad;
  }

  public void setGlobalRad(double globalRad) {
    this.globalRad = globalRad;
  }

  public double getPrecipitation() {
    return precipitation;
  }

  public void setPrecipitation(double precipitation) {
    this.precipitation = precipitation;
  }

  public double getWindspeed2m() {
    return windspeed2m;
  }

  public void setWindspeed2m(double windspeed2m) {
    this.windspeed2m = windspeed2m;
  }

  public double getEt0() {
    return et0;
  }

  public void setEt0(double et0) {
    this.et0 = et0;
  }

  /**
   * Constructor DayData class.
   * 
   * @author MO
   * 
   * @param tempMean Temperature mean in °C
   * @param tempMin Temperature minimum in °C
   * @param tempMax Temperature maximum in °C
   * @param airHumidityRel Relative air humidity in percent
   * @param airHumidityRelMin Minimum relative air humidity in percent
   * @param globalRad Global radiation in MJ
   * @param precipitation Rainfall
   * @param windspeed2m Mean windspeed at 2 meters above ground level in m/s
   */
  public DayData(Date date, double tempMean, double tempMin, double tempMax, double airHumidityRel,
      double airHumidityRelMin, double globalRad, double precipitation, double windspeed2m,
      double et0) {
    super();
    this.date = date;
    this.tempMean = tempMean;
    this.tempMin = tempMin;
    this.tempMax = tempMax;
    this.airHumidityRel = airHumidityRel;
    this.airHumidityRelMin = airHumidityRelMin;
    this.globalRad = globalRad;
    this.precipitation = precipitation;
    this.windspeed2m = windspeed2m;
    this.et0 = et0;
  }

}

