package de.hgu.gsehen.model;

import java.util.Date;

public class SimpleWeatherData {
  private Date dateTime;
  private double temperature;
  private double airHumidityRel;
  private double globalRadiation;
  private double precipitation;
  private double windspeed;

  public Date getDateTime() {
    return dateTime;
  }

  public void setDateTime(Date dateTime) {
    this.dateTime = dateTime;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public double getAirHumidityRel() {
    return airHumidityRel;
  }

  public void setAirHumidityRel(double airHumidityRel) {
    this.airHumidityRel = airHumidityRel;
  }

  public double getGlobalRadiation() {
    return globalRadiation;
  }

  public void setGlobalRadiation(double globalRadiation) {
    this.globalRadiation = globalRadiation;
  }

  public double getPrecipitation() {
    return precipitation;
  }

  public void setPrecipitation(double precipitation) {
    this.precipitation = precipitation;
  }

  public double getWindspeed() {
    return windspeed;
  }

  public void setWindspeed(double windspeed) {
    this.windspeed = windspeed;
  }
}
