package de.hgu.gsehen.evapotranspiration;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Class to represent the environmental data to calculate the GS water balance.
 *
 * @author MO
 *
 */
@Entity
public class DayData implements Comparable<DayData> {

  @Id
  @GeneratedValue
  private long id;
  private Date date;
  private double tempMean;
  private Double tempMin;
  private Double tempMax;
  private double airHumidityRelMean;
  private Double airHumidityRelMin;
  private Double airHumidityRelMax;
  private double globalRad;
  private Double precipitation; // ausgliedern in ein anders objekt?
  private double windspeed2m;
  private Double et0;
  private Double currentKc;
  private Double etc;
  private Double irrigation;
  private Double dailyBalance;
  private Integer currentRootingZone;
  private Double currentAvailableSoilWater;
  private Double currentTotalWaterBalance;

  public Double getCurrentKc() {
    return currentKc;
  }

  public void setCurrentKc(Double currentKc) {
    this.currentKc = currentKc;
  }


  public Double getEtc() {
    return etc;
  }

  public void setEtc(Double etc) {
    this.etc = etc;
  }


  public Double getIrrigation() {
    return irrigation;
  }

  public void setIrrigation(Double irrigation) {
    this.irrigation = irrigation;
  }


  public Double getDailyBalance() {
    return dailyBalance;
  }

  public void setDailyBalance(Double dailyBalance) {
    this.dailyBalance = dailyBalance;
  }

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

  public Double getTempMin() {
    return tempMin;
  }

  public void setTempMin(Double tempMin) {
    this.tempMin = tempMin;
  }

  public Double getTempMax() {
    return tempMax;
  }

  public void setTempMax(Double tempMax) {
    this.tempMax = tempMax;
  }

  public double getAirHumidityRelMean() {
    return airHumidityRelMean;
  }

  public void setAirHumidityRelMean(Double airHumidityRelMean) {
    this.airHumidityRelMean = airHumidityRelMean;
  }

  public Double getAirHumidityRelMin() {
    return airHumidityRelMin;
  }

  public void setAirHumidityRelMin(Double airHumidityRelMin) {
    this.airHumidityRelMin = airHumidityRelMin;
  }

  public Double getAirHumidityRelMax() {
    return airHumidityRelMax;
  }

  public void setAirHumidityRelMax(Double airHumidityRelMax) {
    this.airHumidityRelMax = airHumidityRelMax;
  }

  public double getGlobalRad() {
    return globalRad;
  }

  public void setGlobalRad(double globalRad) {
    this.globalRad = globalRad;
  }

  public Double getPrecipitation() {
    return precipitation;
  }

  public void setPrecipitation(Double precipitation) {
    this.precipitation = precipitation;
  }

  public double getWindspeed2m() {
    return windspeed2m;
  }

  public void setWindspeed2m(double windspeed2m) {
    this.windspeed2m = windspeed2m;
  }

  public Double getEt0() {
    return et0;
  }

  public void setEt0(Double et0) {
    this.et0 = et0;
  }

  public Integer getCurrentRootingZone() {
    return currentRootingZone;
  }

  public void setCurrentRootingZone(Integer currentRootingZone) {
    this.currentRootingZone = currentRootingZone;
  }

  public Double getCurrentAvailableSoilWater() {
    return currentAvailableSoilWater;
  }

  public void setCurrentAvailableSoilWater(Double currentAvailableSoilWater) {
    this.currentAvailableSoilWater = currentAvailableSoilWater;
  }

  public Double getCurrentTotalWaterBalance() {
    return currentTotalWaterBalance;
  }

  public void setCurrentTotalWaterBalance(Double currentTotalWaterBalance) {
    this.currentTotalWaterBalance = currentTotalWaterBalance;
  }

  @Override
  public int compareTo(DayData o) {
    return getDate().compareTo(o.getDate());
  }

  /**
   * Constructor DayData class.
   *
   * @author MO
   *
   * @param tempMean Temperature mean in °C
   * @param tempMin Temperature minimum in °C
   * @param tempMax Temperature maximum in °C
   * @param airHumidityRelMean Relative air humidity in percent mean
   * @param airHumidityRelMin Relative air humidity in percent min
   * @param airHumidityRelMax Relative air humidity in percent max
   * @param globalRad Global radiation in MJ
   * @param precipitation Rainfall
   * @param windspeed2m Mean windspeed at 2 meters above ground level in m/s
   * @param et0 daily evapotranspiration in mm
   * @param currentKc is the days kc value
   * @param dailyBalance is the daily Water blance in mm
   */
  public DayData(Date date, double tempMean, Double tempMin, Double tempMax,
      double airHumidityRelMean, Double airHumidityRelMin, Double airHumidityRelMax,
      double globalRad, Double precipitation, double windspeed2m, Double et0, Double currentKc,
      Double etc, Double irrigation, Double dailyBalance) {
    super();
    this.date = date;
    this.tempMean = tempMean;
    this.tempMin = tempMin;
    this.tempMax = tempMax;
    this.airHumidityRelMean = airHumidityRelMean;
    this.airHumidityRelMin = airHumidityRelMin;
    this.airHumidityRelMax = airHumidityRelMax;
    this.globalRad = globalRad;
    this.precipitation = precipitation;
    this.windspeed2m = windspeed2m;
    this.et0 = et0;
    this.currentKc = currentKc;
    this.etc = etc;
    this.irrigation = irrigation;
    this.dailyBalance = dailyBalance;
    this.currentRootingZone = currentRootingZone;
    this.currentAvailableSoilWater = currentAvailableSoilWater;
    this.currentTotalWaterBalance = currentTotalWaterBalance;
  }

  public DayData() {}


}

