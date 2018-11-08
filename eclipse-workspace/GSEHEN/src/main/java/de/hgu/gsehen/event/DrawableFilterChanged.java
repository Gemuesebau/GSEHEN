package de.hgu.gsehen.event;

import de.hgu.gsehen.model.Drawable;
import java.util.function.Predicate;

public class DrawableFilterChanged extends GsehenEvent {
  private Predicate<Drawable> filter;

  public void setFilter(Predicate<Drawable> filter) {
    this.filter = filter;
  }

  public Predicate<Drawable> getFilter() {
    return filter;
  }
}
