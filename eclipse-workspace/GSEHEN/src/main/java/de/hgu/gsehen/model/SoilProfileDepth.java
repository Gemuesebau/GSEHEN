package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Table;

@Entity
public class SoilProfileDepth {
  @Id
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
