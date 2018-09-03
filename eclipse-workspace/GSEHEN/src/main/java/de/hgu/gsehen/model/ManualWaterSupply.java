package de.hgu.gsehen.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ManualWaterSupply {

  @Id
  @GeneratedValue
  private long id;
  private Date date;
  private Double irrigation;
  private Double precipitation;

  public ManualWaterSupply() {}

  /**
   * Constructor for ManualWaterSupply.
   * 
   * @param date Date of Manual action
   * @param irrigation applied amount of water in mm
   * @param precipitation amount of rainfall in mm
   */
  public ManualWaterSupply(Date date, Double irrigation, Double precipitation) {
    super();
    this.date = date;
    this.irrigation = irrigation;
    this.precipitation = precipitation;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Double getIrrigation() {
    return irrigation;
  }

  public void setIrrigation(Double irrigation) {
    this.irrigation = irrigation;
  }

  public Double getPrecipitation() {
    return precipitation;
  }

  public void setPrecipitation(Double precipitation) {
    this.precipitation = precipitation;
  }
}
