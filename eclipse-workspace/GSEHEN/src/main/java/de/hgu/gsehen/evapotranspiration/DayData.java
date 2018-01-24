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


  /**
   * Method to calculate the daily reference evapotranspiration as published by Allen et al. All
   * parameters are daily values.
   * 
   * @author MO
   * @param mean Calculation only with Temperature mean? TRUE = yes, FALSE = no
   * @param geoLen Geographical length of the location your are calculating et0 from in rad
   * @param geoWid Geographical width of the location your are calculating et0 from in rad
   * @param heighAbvNn Height over normal null in m
   * @param tempMean Temperature mean in °C
   * @param tempMin Temperature minimum in °C
   * @param tempMax Temperature maximum in °C
   * @param airHumidityRel Relative air humidity in percent
   * @param airHumidityRelMin Minimum relative air humidity in percent
   * @param globalRad Global radiation in MJ
   * @param precipitation Rainfall
   * @param windspeed2m Mean windspeed at 2 meters above ground level in m/s
   * @return et0 The reference evapotranspiration
   */
  public double calculateEt0(boolean mean, double geoLen, double geoWid, double heighAbvNn,
      double tempMean, double tempMin, double tempMax, double airHumidityRel,
      double airHumidityRelMin, double globalRad, double precipitation, double windspeed2m) {

    return et0;
  }

}

