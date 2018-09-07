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
   */
  public CropPhase(Integer phase, String description, Date today, Date cropStart,
      String duration) {

    this.setPhase(phase);
    this.description = description;
    this.setToday(today);
    this.setCropStart(cropStart);
    this.setDuration(duration);

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
}