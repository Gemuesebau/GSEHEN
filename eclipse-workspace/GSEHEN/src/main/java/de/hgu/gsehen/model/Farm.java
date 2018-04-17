package de.hgu.gsehen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import de.hgu.gsehen.gui.GeoPolygon;

public class Farm implements Drawable, DrawableParent {
  private String name;
  private GeoPolygon polygon;
  private List<Field> fields;
  
  public Farm(String name, GeoPolygon polygon, Field... fields) {
    this.name = name;
    this.polygon = polygon;
    this.fields = new ArrayList<>();
    for (Field field : fields) {
      this.fields.add(field);
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
  public void setFields(List<Field> fields) {
    this.fields = fields;
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
    fields.forEach(handler);
  }
}
