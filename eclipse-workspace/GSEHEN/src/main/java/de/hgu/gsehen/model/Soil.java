package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Class representing a type of soil.
 *
 * @author AT
 */
@Entity
public class Soil {
  
  public Soil() {
  }
  
  @Id
  @GeneratedValue
  private long id;
  private String name;
  private double availableWaterCapacity;
  private String description;

  public Soil(String name) {
    this.name = name;
  }

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
