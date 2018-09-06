package de.hgu.gsehen.model;

import javax.persistence.Embeddable;

@Embeddable
public class CropDevelopmentStatus {

  private int phase1;
  private Integer phase2;
  private Integer phase3;
  private Integer phase4;

  public int getPhase1() {
    return phase1;
  }

  public void setPhase1(int phase1) {
    this.phase1 = phase1;
  }

  public Integer getPhase2() {
    return phase2;
  }

  public void setPhase2(Integer phase2) {
    this.phase2 = phase2;
  }

  public Integer getPhase3() {
    return phase3;
  }

  public void setPhase3(Integer phase3) {
    this.phase3 = phase3;
  }

  public Integer getPhase4() {
    return phase4;
  }

  public void setPhase4(Integer phase4) {
    this.phase4 = phase4;
  }
}
