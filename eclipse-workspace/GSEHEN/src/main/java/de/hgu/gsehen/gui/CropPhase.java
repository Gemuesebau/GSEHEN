package de.hgu.gsehen.gui;

/**
 * Class representing a crop phase.
 *
 * @author CW
 */
public class CropPhase {

  private Integer phase;
  private String description;
  private String todayMarker;
  private String cropStart;
  private String duration;
  private String rootingZone;

  public CropPhase() {
    super();
  }

  /**
   * Holder object for crop-/soil-values.
   * 
   * @param phase
   *          - Phase 1 to 4.
   * @param description
   *          - Crop description.
   * @param todayMarker
   *          - Todays date.
   * @param cropStart
   *          - The date, the crop was set.
   * @param duration
   *          - Phase duration in days.
   * @param rootingZone
   *          - RootingZone of each phase.
   */
  public CropPhase(Integer phase, String description, String todayMarker, String cropStart,
      String duration, String rootingZone) {
    this.setPhase(phase);
    this.description = description;
    this.setTodayMarker(todayMarker);
    this.setCropStart(cropStart);
    this.setDuration(duration);
    this.rootingZone = rootingZone;
  }

  public Integer getPhase() {
    return phase;
  }

  public void setPhase(Integer phase) {
    this.phase = phase;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTodayMarker() {
    return todayMarker;
  }

  public void setTodayMarker(String todayMarker) {
    this.todayMarker = todayMarker;
  }

  public String getCropStart() {
    return cropStart;
  }

  public void setCropStart(String cropStart2) {
    this.cropStart = cropStart2;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getRootingZone() {
    return rootingZone;
  }

  public void setRootingZone(String rootingZone) {
    this.rootingZone = rootingZone;
  }
}