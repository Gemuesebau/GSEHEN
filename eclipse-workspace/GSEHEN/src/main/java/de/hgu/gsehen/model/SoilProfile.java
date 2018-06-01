package de.hgu.gsehen.model;

/**
 * Class representing a type of soil.
 *
 * @author AT
 */
public class SoilProfile {
  private String name;
  private double availableWaterCapacity;
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getAvailableWaterCapacity() {
    return availableWaterCapacity;
  }

  public void setAvailableWaterCapacity(double availableWaterCapacity) {
    this.availableWaterCapacity = availableWaterCapacity;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
