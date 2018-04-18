package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;

import java.util.Date;

public class Plot implements Drawable, NamedPolygonHolder {
  private String name;
  private double area;
  private GeoPolygon polygon;
  private Location location;
  private Double scalingFactor;
  private WeatherData weatherData;
  private Double rootingZone;
  private WaterBalance waterBalance;
  private String recommendedAction;
  private Date soilStartDate;
  private Double soilStartValue;
  private Boolean calculationPaused;
  private Crop crop;
  private Date cropStart; 
  private Date cropEnd; 

  public Plot() {
  }

  public Plot(String name, GeoPolygon polygon) {
    setNameAndPolygon(name, polygon);
  }

  @Override
  public void setNameAndPolygon(String name, GeoPolygon polygon) {
    this.name = name;
    this.polygon = polygon;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getArea() {
    return area;
  }

  public void setArea(double area) {
    this.area = area;
  }

  @Override
  public GeoPolygon getPolygon() {
    return polygon;
  }

  public void setPolygon(GeoPolygon polygon) {
    this.polygon = polygon;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public Double getScalingFactor() {
    return scalingFactor;
  }

  public void setScalingFactor(Double scalingFactor) {
    this.scalingFactor = scalingFactor;
  }

  public WeatherData getWeatherData() {
    return weatherData;
  }

  public void setWeatherData(WeatherData weatherData) {
    this.weatherData = weatherData;
  }

  public Double getRootingZone() {
    return rootingZone;
  }

  public void setRootingZone(Double rootingZone) {
    this.rootingZone = rootingZone;
  }

  public WaterBalance getWaterBalance() {
    return waterBalance;
  }

  public void setWaterBalance(WaterBalance waterBalance) {
    this.waterBalance = waterBalance;
  }

  public String getRecommendedAction() {
    return recommendedAction;
  }

  public void setRecommendedAction(String recommendedAction) {
    this.recommendedAction = recommendedAction;
  }

  public Date getSoilStartDate() {
    return soilStartDate;
  }

  public void setSoilStartDate(Date soilStartDate) {
    this.soilStartDate = soilStartDate;
  }

  public Double getSoilStartValue() {
    return soilStartValue;
  }

  public void setSoilStartValue(Double soilStartValue) {
    this.soilStartValue = soilStartValue;
  }

  public Boolean getCalculationPaused() {
    return calculationPaused;
  }

  public void setCalculationPaused(Boolean calculationPaused) {
    this.calculationPaused = calculationPaused;
  }

  public Crop getCrop() {
    return crop;
  }

  public void setCrop(Crop crop) {
    this.crop = crop;
  }

  public Date getCropStart() {
    return cropStart;
  }

  public void setCropStart(Date cropStart) {
    this.cropStart = cropStart;
  }

  public Date getCropEnd() {
    return cropEnd;
  }

  public void setCropEnd(Date cropEnd) {
    this.cropEnd = cropEnd;
  }

  public void visualize(){}

  public void configure(){}

  public void modify(){}

  public void archive(){}
}
