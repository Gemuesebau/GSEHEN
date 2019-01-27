package de.hgu.gsehen.model;

import de.hgu.gsehen.evapotranspiration.GeoData;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class WeatherDataSource implements Named {
  @Id
  @GeneratedValue
  private long id;
  private String uuid;

  private String name;
  private String pluginJsFileName;
  private boolean manualImportActive;
  private boolean automaticImportActive;
  private Double automaticImportFrequencySeconds;
  @SuppressWarnings("checkstyle:all")
  private String pluginConfigurationJSON;
  private double locationLng;
  private double locationLat;
  private double locationMetersAboveSeaLevel;

  public WeatherDataSource(String uuid) {
    this();
    this.uuid = uuid;
  }

  public WeatherDataSource() {
  }

  public double getLocationLng() {
    return locationLng;
  }

  public void setLocationLng(double locationLng) {
    this.locationLng = locationLng;
  }

  public double getLocationLat() {
    return locationLat;
  }

  public void setLocationLat(double locationLat) {
    this.locationLat = locationLat;
  }

  public double getLocationMetersAboveSeaLevel() {
    return locationMetersAboveSeaLevel;
  }

  public void setLocationMetersAboveSeaLevel(double locationMetersAboveSeaLevel) {
    this.locationMetersAboveSeaLevel = locationMetersAboveSeaLevel;
  }

  public GeoData getLocation() {
    return new GeoData(false, locationLng, locationLat, locationMetersAboveSeaLevel);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPluginJsFileName() {
    return pluginJsFileName;
  }

  public void setPluginJsFileName(String pluginJsFileName) {
    this.pluginJsFileName = pluginJsFileName;
  }

  public boolean isManualImportActive() {
    return manualImportActive;
  }

  public void setManualImportActive(boolean manualImportActive) {
    this.manualImportActive = manualImportActive;
  }

  public boolean isAutomaticImportActive() {
    return automaticImportActive;
  }

  public void setAutomaticImportActive(boolean automaticImportActive) {
    this.automaticImportActive = automaticImportActive;
  }

  public Double getAutomaticImportFrequencySeconds() {
    return automaticImportFrequencySeconds;
  }

  public void setAutomaticImportFrequencySeconds(Double automaticImportFrequencySeconds) {
    this.automaticImportFrequencySeconds = automaticImportFrequencySeconds;
  }

  @SuppressWarnings("checkstyle:all")
  public String getPluginConfigurationJSON() {
    //return pluginConfigurationJSON; FIXME Plugin muss das JSON liefern, wenn man "Save" klickt.
    pluginConfigurationJSON = "" + pluginConfigurationJSON; // dummy
    return "{"
        + "  \"measIntervalSeconds\": 100,"
        + "  \"windspeedMeasHeightMeters\": 2,"
        + "  \"dataFilePath\": \"C:\\\\Users\\\\cwitzke\\\\Desktop\\\\10MinDaten.csv\","
        + "  \"dateFormatString\": \"y-M-d\","
        + "  \"numberFormat\": \"GERMAN\""
        + "}";
  }

  @SuppressWarnings("checkstyle:all")
  public void setPluginConfigurationJSON(String pluginConfigurationJSON) {
    this.pluginConfigurationJSON = pluginConfigurationJSON;
  }
}
