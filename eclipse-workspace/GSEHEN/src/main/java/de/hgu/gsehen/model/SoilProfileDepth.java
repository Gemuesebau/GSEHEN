package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SoilProfileDepth {
  
  @Id
  @GeneratedValue
  private long id;
  private double depthStart;
  private double depthEnd;

  public double getDepthStart() {
    return depthStart;
  }

  public void setDepthStart(double depthStart) {
    this.depthStart = depthStart;
  }

  public double getDepthEnd() {
    return depthEnd;
  }

  public void setDepthEnd(double depthEnd) {
    this.depthEnd = depthEnd;
  }
}
