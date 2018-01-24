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



}

