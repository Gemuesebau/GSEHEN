package de.hgu.gsehen.event;

import de.hgu.gsehen.model.Drawable;

public class DrawableSelected extends GsehenViewEvent {
  private Drawable subject;

  public Drawable getSubject() {
    return subject;
  }

  public void setSubject(Drawable subject) {
    this.subject = subject;
  }
}
