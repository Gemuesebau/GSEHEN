package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Farm implements Drawable, DrawableParent {
  private String name;
  private GeoPolygon polygon;
  private List<Field> fields;

  public Farm() {}

  /**
   * Konstruktor für eine Farm, der direkt den Namen und die Umrisse setzt. Felder werden mit
   * de.hgu.gsehen.model.Farm.setFields(Field...) gesetzt.
   *
   * @see de.hgu.gsehen.model.Farm.setFields
   * @param name der Name der Farm
   * @param polygon die Umrisse der Hauptgebäudeanlage, nicht der Felder!
   */
  public Farm(String name, GeoPolygon polygon) {
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
  public GeoPolygon getPolygon() {
    return polygon;
  }

  public void setPolygon(GeoPolygon polygon) {
    this.polygon = polygon;
  }

  @Override
  public void forAllChildDrawables(Consumer<Drawable> handler) {
    if (fields == null) {
      return;
    }
    fields.forEach(handler);
  }

  @Override
  public String toString() {
    return " " + getClass().getSimpleName() + " '" + getName() + "'";
  }
}
