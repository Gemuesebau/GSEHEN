package de.hgu.gsehen.model;

import de.hgu.gsehen.gsbalance.RecommendedAction;
import de.hgu.gsehen.gui.GeoPolygon;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Plot extends Drawable {

  @Id
  @GeneratedValue
  private long id;

  private String name;
  private double area;
  @OneToOne(cascade = {CascadeType.ALL})
  private GeoPolygon polygon;
  @OneToOne(cascade = {CascadeType.ALL})
  private Location location;
  private Double scalingFactor;
  @OneToOne(cascade = {CascadeType.ALL})
  private WeatherData weatherData;
  private Double rootingZone;
  @OneToOne(cascade = {CascadeType.ALL})
  private WaterBalance waterBalance;
  @OneToOne(cascade = {CascadeType.ALL})
  private ManualData manualData;
  @OneToOne(cascade = {CascadeType.ALL})
  private RecommendedAction recommendedAction;
  private Date soilStartDate;
  private Double soilStartValue;
  private Boolean calculationPaused;
  @OneToOne(cascade = {CascadeType.ALL})
  private Crop crop;
  @Embedded
  private CropDevelopmentStatus cropDevelopmentStatus;
  @Embedded
  private CropRootingZone cropRootingZone;
  private Date cropStart;
  private Date cropEnd;
  private Boolean isActive;

  public Plot() {
    super();
  }

  public Plot(String name, GeoPolygon polygon) {
    this();
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

  public RecommendedAction getRecommendedAction() {
    return recommendedAction;
  }

  public void setRecommendedAction(RecommendedAction recommendedAction) {
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

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public CropDevelopmentStatus getCropDevelopmentStatus() {
    return cropDevelopmentStatus;
  }

  public void setCropDevelopmentStatus(CropDevelopmentStatus cropDevelopmentStatus) {
    this.cropDevelopmentStatus = cropDevelopmentStatus;
  }

  public ManualData getManualData() {
    return manualData;
  }

  public void setManualData(ManualData manualData) {
    this.manualData = manualData;
  }

  public CropRootingZone getCropRootingZone() {
    return cropRootingZone;
  }

  public void setCropRootingZone(CropRootingZone cropRootingZone) {
    this.cropRootingZone = cropRootingZone;
  }

  public void visualize() {}

  public void configure() {}

  public void modify() {}

  public void archive() {}

  /**
   * Constructor for Plot.
   * 
   * @param name Plot name
   * @param area Plot area in m²
   * @param polygon Subordinated polygon
   * @param location Subordinated location
   * @param scalingFactor Individual kc scaling factor
   * @param weatherData Subordianted weather Data source
   * @param rootingZone Maximum rooting zone for the plot
   * @param waterBalance WaterBalance containing, DayData
   * @param recommendedAction RecommendedAction Irrigation recommendation
   * @param soilStartDate Start of waterbalancing for soil, Date
   * @param soilStartValue Water content in mm for first soillayer 10cm
   * @param calculationPaused Is the calculation paused due to heavy rainfall
   * @param crop Subordinated crop
   * @param cropDevelopmentStatus user input data concerning crop development phases
   * @param cropStart Start date of the crop
   * @param cropEnd End date of the crop
   * @param isActive Is the plot active and not archived
   */
  public Plot(String name, double area, GeoPolygon polygon, Location location, Double scalingFactor,
      WeatherData weatherData, Double rootingZone, WaterBalance waterBalance, ManualData manualData,
      RecommendedAction recommendedAction, Date soilStartDate, Double soilStartValue,
      Boolean calculationPaused, Crop crop, CropDevelopmentStatus cropDevelopmentStatus,
      CropRootingZone cropRootingZone, Date cropStart, Date cropEnd, Boolean isActive) {
    super();
    this.name = name;
    this.area = area;
    this.polygon = polygon;
    this.location = location;
    this.scalingFactor = scalingFactor;
    this.weatherData = weatherData;
    this.rootingZone = rootingZone;
    this.waterBalance = waterBalance;
    this.manualData = manualData;
    this.recommendedAction = recommendedAction;
    this.soilStartDate = soilStartDate;
    this.soilStartValue = soilStartValue;
    this.calculationPaused = calculationPaused;
    this.crop = crop;
    this.cropDevelopmentStatus = cropDevelopmentStatus;
    this.cropRootingZone = cropRootingZone;
    this.cropStart = cropStart;
    this.cropEnd = cropEnd;
    this.isActive = isActive;
  }

  @Override
  public String toString() {
    return " " + getClass().getSimpleName() + " '" + getName() + "'";
  }


}
