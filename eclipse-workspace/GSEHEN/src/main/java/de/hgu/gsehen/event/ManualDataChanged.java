package de.hgu.gsehen.event;

import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.util.Date;

public class ManualDataChanged extends GsehenEvent {
  private Field field;
  private Plot plot;
  private Date date;

  public Field getField() {
    return field;
  }

  public void setField(Field field) {
    this.field = field;
  }

  public Plot getPlot() {
    return plot;
  }

  public void setPlot(Plot plot) {
    this.plot = plot;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
