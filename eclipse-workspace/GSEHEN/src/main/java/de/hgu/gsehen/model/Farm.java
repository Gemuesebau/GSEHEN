package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Farm implements Drawable, DrawableParent {
  private String name;
  private List<GeoPolygon> polygons;
  private List<Field> fields;
  
  /**
   * Konstruktor für eine Farm, der direkt den Namen und die Umrisse setzt.
   * Felder werden mit de.hgu.gsehen.model.Farm.setFields(Field...) gesetzt.
   *
   * @see de.hgu.gsehen.model.Farm.setFields
   * @param name der Name der Farm
   * @param polygons die Umrisse der Farm
   */
  public Farm(String name, GeoPolygon... polygons) {
    this.name = name;
    this.polygons = new ArrayList<>();
    for (GeoPolygon polygon : polygons) {
      this.polygons.add(polygon);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public List<Field> getFields() {
    return fields;
  }

  /**
   * Setzt die Liste der Felder der Farm.
   *
   * @param fields die Felder der Farm
   */
  public void setFields(List<Field> fields) {
    this.fields = fields;
  }

  /**
   * Setzt die Felder der Farm.
   *
   * @param fields die Felder der Farm
   */
  public void setFields(Field... fields) {
    this.fields = new ArrayList<>();
    for (Field field : fields) {
      this.fields.add(field);
    }
  }

  @Override
  public List<GeoPolygon> getPolygons() {
    return polygons;
  }

  public void setPolygon(List<GeoPolygon> polygons) {
    this.polygons = polygons;
  }

  @Override
  public void forAllChildDrawables(Consumer<Drawable> handler) {
    fields.forEach(handler);
  }
}
