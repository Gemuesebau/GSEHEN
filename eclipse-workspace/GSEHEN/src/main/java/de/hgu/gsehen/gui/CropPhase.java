package de.hgu.gsehen.gui;

import java.util.Date;

/**
 * Class representing a crop phase.
 *
 * @author CW
 */
public class CropPhase {

  private Integer phase;
  private String description;
  private Date today;
  private Date cropStart;
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
   * @param today
   *          - Todays date.
   * @param cropStart
   *          - The date, the crop was set.
   * @param duration
   *          - Phase duration in days.
   * @param rootingZone
   *          - RootingZone of each phase.
   */
  public CropPhase(Integer phase, String description, Date today, Date cropStart, String duration,
      String rootingZone) {

    this.setPhase(phase);
    this.description = description;
    this.setToday(today);
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

  public Date getToday() {
    return today;
  }

  public void setToday(Date today) {
    this.today = today;
  }

  public Date getCropStart() {
    return cropStart;
  }

  public void setCropStart(Date cropStart) {
    this.cropStart = cropStart;
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