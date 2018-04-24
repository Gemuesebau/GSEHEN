package de.hgu.gsehen.event;

public interface GsehenEventListener<T extends GsehenEvent> {

  public void handle(T event);
}
