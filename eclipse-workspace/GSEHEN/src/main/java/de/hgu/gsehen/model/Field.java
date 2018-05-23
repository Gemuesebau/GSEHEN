package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Field implements Drawable, DrawableParent {
  private WeatherDataSource weatherDataSource;
  private SoilProfile soilProfile;
  private Double rootingZone;
  private Location location;
  private GeoPolygon polygon;
  private String name;
  private List<Plot> plots;
  private double area;

  public Field() {}

  /**
   * Konstruktor für ein Feld, der direkt Name, Umrisse und enthaltene Plots setzt.
   *
   * @param name der Name des Feldes
   * @param polygon die Umrisse des Feldes
   * @param plots die enthaltenen Plots
   */
  public Field(String name, GeoPolygon polygon, Plot... plots) {
    setNameAndPolygon(name, polygon);
    this.plots = new ArrayList<>();
    for (Plot plot : plots) {
      this.plots.add(plot);
    }
  }

  @Override
  public void setNameAndPolygon(String name, GeoPolygon polygon) {
    this.name = name;
    this.polygon = polygon;
  }

  public WeatherDataSource getWeatherDataSource() {
    return weatherDataSource;
  }

  public void setWeatherDataSource(WeatherDataSource weatherDataSource) {
    this.weatherDataSource = weatherDataSource;
  }

  public SoilProfile getSoilProfile() {
    return soilProfile;
  }

  public void setSoilProfile(SoilProfile soilProfile) {
    this.soilProfile = soilProfile;
  }

  public Double getRootingZone() {
    return rootingZone;
  }

  public void setRootingZone(Double rootingZone) {
    this.rootingZone = rootingZone;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  @Override
  public GeoPolygon getPolygon() {
    return polygon;
  }

  @Override
  public GeoPolygon getPolygonByName(String name) {
    this.name = name;
    return polygon;
  }

  public void setPolygon(GeoPolygon polygon) {
    this.polygon = polygon;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Plot> getPlots() {
    return plots;
  }

  public void setPlots(List<Plot> plots) {
    this.plots = plots;
  }

  /**
   * Setzt die Plots des Fields.
   *
   * @param plots - Die Plots des Fields
   */
  public void setPlots(Plot... plots) {
    this.plots = new ArrayList<>();
    for (Plot plot : plots) {
      this.plots.add(plot);
    }
  }

  public double getArea() {
    return area;
  }

  public void setArea(double area) {
    this.area = area;
  }

  public void configure() {}

  public void modify() {}

  @Override
  public void forAllChildDrawables(Consumer<Drawable> handler) {
    if (plots == null) {
      return;
    }
    plots.forEach(handler);
  }

  @Override
  public String toString() {
    return " " + getClass().getSimpleName() + " '" + getName() + "'";
  }
}
