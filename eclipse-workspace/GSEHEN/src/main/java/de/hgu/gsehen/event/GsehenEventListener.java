package de.hgu.gsehen.event;

public interface GsehenEventListener<T extends GsehenEvent> {

  /**
   * Handles a GSEHEN event of generic type T.
   *
   * @param event the event to handle
   */
  public void handle(T event);
}
