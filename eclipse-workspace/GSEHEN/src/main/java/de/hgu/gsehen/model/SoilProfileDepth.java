package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SoilProfileDepth {

  @Id
  @GeneratedValue
  private long id;
  private double depth;

  public SoilProfileDepth() {
  }

  public SoilProfileDepth(double depth) {
    super();
    this.depth = depth;
  }

  public double getDepth() {
    return depth;
  }

  public void setDepth(double depth) {
    this.depth = depth;
  }
}
