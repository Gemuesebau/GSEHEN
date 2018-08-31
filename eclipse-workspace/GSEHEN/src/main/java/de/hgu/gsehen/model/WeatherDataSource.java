package de.hgu.gsehen.model;

import de.hgu.gsehen.evapotranspiration.GeoData;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class WeatherDataSource {
  @Id
  @GeneratedValue
  private long id;

  private String name;
  private int measIntervalSeconds;
  private double windspeedMeasHeightMeters;
  private String dateFormatString;
  private String numberLocaleId;
  private String dataFilePath;
  private double locationLng;
  private double locationLat;
  private double locationMetersAboveSeaLevel;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getMeasIntervalSeconds() {
    return measIntervalSeconds;
  }

  public void setMeasIntervalSeconds(int measIntervalSeconds) {
    this.measIntervalSeconds = measIntervalSeconds;
  }

  public double getWindspeedMeasHeightMeters() {
    return windspeedMeasHeightMeters;
  }

  public void setWindspeedMeasHeightMeters(double windspeedMeasHeightMeters) {
    this.windspeedMeasHeightMeters = windspeedMeasHeightMeters;
  }

  public String getDateFormatString() {
    return dateFormatString;
  }

  public void setDateFormatString(String dateFormatString) {
    this.dateFormatString = dateFormatString;
  }

  public String getNumberLocaleId() {
    return numberLocaleId;
  }

  public void setNumberLocaleId(String numberLocaleId) {
    this.numberLocaleId = numberLocaleId;
  }

  public String getDataFilePath() {
    return dataFilePath;
  }

  public void setDataFilePath(String dataFilePath) {
    this.dataFilePath = dataFilePath;
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

  @SuppressWarnings({"checkstyle:javadocmethod", "checkstyle:rightcurly"})
  public DecimalFormat getNumberFormat() {
    Locale locale = Locale.ENGLISH;
    try {
      locale = (Locale)Locale.class.getField(numberLocaleId).get(null);
    }
    catch (Exception e) {
      /* do nothing */
    }
    return (DecimalFormat)NumberFormat.getNumberInstance(locale);
  }

  public SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat(dateFormatString);
  }
}
