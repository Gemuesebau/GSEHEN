package de.hgu.gsehen.model;

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
public class Farm extends Drawable implements DrawableParent {
  @Id
  @GeneratedValue
  private long id;
  private String name;
  @OneToOne(cascade = CascadeType.ALL)
  private GeoPolygon polygon;
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<Field> fields;

  public Farm() {
    super();
    fields = new ArrayList<>();
  }

  /**
   * Konstruktor für eine Farm, der direkt den Namen und die Umrisse setzt. Felder werden mit
   * de.hgu.gsehen.model.Farm.setFields(Field...), oder
   * de.hgu.gsehen.model.Farm.getFields().add(...) gesetzt.
   *
   * @see de.hgu.gsehen.model.Farm#setFields(List)
   * @param name der Name der Farm
   * @param polygon die Umrisse der Hauptgebäudeanlage, nicht der Felder!
   */
  public Farm(String name, GeoPolygon polygon) {
    this();
    setNameAndPolygon(name, polygon);
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
    this.name = name;
  }

  @Override
  public GeoPolygon getPolygon() {
    return polygon;
  }

  public void setPolygon(GeoPolygon polygon) {
    this.polygon = polygon != null ? polygon : new GeoPolygon();
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
