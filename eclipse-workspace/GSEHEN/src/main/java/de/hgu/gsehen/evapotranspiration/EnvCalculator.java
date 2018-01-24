package de.hgu.gsehen.evapotranspiration;

public class EnvCalculator {
  /**
   * Method to calculate the daily reference evapotranspiration as published by Allen et al. All
   * parameters are daily values. aufruf verändert das objet eto wert
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
  public static void calculateEt0() {

  }
}
