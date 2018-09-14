package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SoilManualData {

  @Id
  @GeneratedValue
  private long id;
  Double soilKc;
  Integer soilZone;
  Double rainMax;
  Integer daysPause;

  public SoilManualData() {}

  /**
   * User input data for soil parameters.
   * 
   * @param soilZone manually provided soil depth for calculations without crop
   * @param rainMax manually provided "strong rain event" in mm after wich a calaculation pause is
   *        sheduled
   * @param daysPause manually provided period of days the calculation pause is lasting
   */
  public SoilManualData(Double soilKc, Integer soilZone, Double rainMax, Integer daysPause) {
    super();
    this.soilKc = soilKc;
    this.soilZone = soilZone;
    this.rainMax = rainMax;
    this.daysPause = daysPause;
  }

  public Integer getSoilZone() {
    return soilZone;
  }

  public void setSoilZone(Integer soilZone) {
    this.soilZone = soilZone;
  }

  public Double getRainMax() {
    return rainMax;
  }

  public void setRainMax(Double rainMax) {
    this.rainMax = rainMax;
  }

  public Integer getDaysPause() {
    return daysPause;
  }

  public void setDaysPause(Integer daysPause) {
    this.daysPause = daysPause;
  }

  public Double getSoilKc() {
    return soilKc;
  }

  public void setSoilKc(Double soilKc) {
    this.soilKc = soilKc;
  }
}
