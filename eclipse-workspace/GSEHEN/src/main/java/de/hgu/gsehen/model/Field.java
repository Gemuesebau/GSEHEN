package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Field extends Drawable implements DrawableParent {

  @Id
  @GeneratedValue
  private long id;
  private String weatherDataSourceUuid;
  private String soilProfileUuid;
  private Double rootingZone;
  @OneToOne(cascade = {CascadeType.ALL})
  private GeoPoint location;
  @OneToOne(cascade = {CascadeType.ALL})
  private GeoPolygon polygon;
  private String name;
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<Plot> plots;
  private double area;

  public Field() {
    super();
    plots = new ArrayList<>();
  }

  /**
   * Konstruktor für ein Feld, der direkt Name, Umrisse und enthaltene Plots setzt.
   *
   * @param name der Name des Feldes
   * @param polygon die Umrisse des Feldes
   * @param plots die enthaltenen Plots
   */
  public Field(String name, GeoPolygon polygon, Plot... plots) {
    this();
    setNameAndPolygon(name, polygon);
    setPlots(plots);
  }

  @Override
  public void setNameAndPolygon(String name, GeoPolygon polygon) {
    setName(name);
    setPolygon(polygon);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name != null ? name : "Unbenannt"; // FIXME localize
  }

  @Override
  public GeoPolygon getPolygon() {
    return polygon;
  }

  public void setPolygon(GeoPolygon polygon) {
    this.polygon = polygon != null ? polygon : new GeoPolygon();
  }

  public String getWeatherDataSourceUuid() {
    return weatherDataSourceUuid;
  }

  public void setWeatherDataSourceUuid(String weatherDataSourceUuid) {
    this.weatherDataSourceUuid = weatherDataSourceUuid;
  }

  public String getSoilProfileUuid() {
    return soilProfileUuid;
  }

  public void setSoilProfileUuid(String soilProfileUuid) {
    this.soilProfileUuid = soilProfileUuid;
  }

  public Double getRootingZone() {
    return rootingZone;
  }

  public void setRootingZone(Double rootingZone) {
    this.rootingZone = rootingZone;
  }

  public GeoPoint getLocation() {
    return location;
  }

  public void setLocation(GeoPoint location) {
    this.location = location;
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
