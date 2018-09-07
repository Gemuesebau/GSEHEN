package de.hgu.gsehen.model;

import javax.persistence.Embeddable;

@Embeddable
public class CropDevelopmentStatus {

  public CropDevelopmentStatus() {};

  /**
   * Class inheritng manually provied bbch phase lengths in days for the actual crop.
   * 
   * @param phase1 BBCH phase 1
   * @param phase2 BBCH phase 2
   * @param phase3 BBCH phase 3
   * @param phase4 BBCH phase 4
   */
  public CropDevelopmentStatus(Integer phase1, Integer phase2, Integer phase3, Integer phase4) {
    super();
    this.phase1 = phase1;
    this.phase2 = phase2;
    this.phase3 = phase3;
    this.phase4 = phase4;
  }

  private Integer phase1;
  private Integer phase2;
  private Integer phase3;
  private Integer phase4;

  public Integer getPhase1() {
    return phase1;
  }

  public void setPhase1(Integer phase1) {
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
