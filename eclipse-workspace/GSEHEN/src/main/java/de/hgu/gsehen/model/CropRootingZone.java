package de.hgu.gsehen.model;

import javax.persistence.Embeddable;

@Embeddable
public class CropRootingZone {

  Integer rootingZone1;
  Integer rootingZone2;
  Integer rootingZone3;
  Integer rootingZone4;

  public CropRootingZone() {
  }

  /**
   * User input for rooting crop rooting Zones.
   *
   * @param rootingZone1 manually provided rooting zone for crop phase1
   * @param rootingZone2 manually provided rooting zone for crop phase2
   * @param rootingZone3 manually provided rooting zone for crop phase3
   * @param rootingZone4 manually provided rooting zone for crop phase4
   */
  public CropRootingZone(Integer rootingZone1, Integer rootingZone2, Integer rootingZone3,
      Integer rootingZone4) {
    super();
    this.rootingZone1 = rootingZone1;
    this.rootingZone2 = rootingZone2;
    this.rootingZone3 = rootingZone3;
    this.rootingZone4 = rootingZone4;
  }

  public Integer getRootingZone1() {
    return rootingZone1;
  }

  public void setRootingZone1(Integer rootingZone1) {
    this.rootingZone1 = rootingZone1;
  }

  public Integer getRootingZone2() {
    return rootingZone2;
  }

  public void setRootingZone2(Integer rootingZone2) {
    this.rootingZone2 = rootingZone2;
  }

  public Integer getRootingZone3() {
    return rootingZone3;
  }

  public void setRootingZone3(Integer rootingZone3) {
    this.rootingZone3 = rootingZone3;
  }

  public Integer getRootingZone4() {
    return rootingZone4;
  }

  public void setRootingZone4(Integer rootingZone4) {
    this.rootingZone4 = rootingZone4;
  }

}
