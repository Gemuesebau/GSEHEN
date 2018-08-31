package de.hgu.gsehen.model;

import java.util.ArrayList;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ManualData {

  @Id
  @GeneratedValue
  private long id;
  @OneToMany(cascade = {CascadeType.ALL})
  private List<ManualWaterSupply> manualWaterSupply = new ArrayList<ManualWaterSupply>();

  public ManualData() {}

  public ManualData(List<ManualWaterSupply> manualWaterSupply) {
    super();
    this.manualWaterSupply = manualWaterSupply;
  }



  public List<ManualWaterSupply> getManualWaterSupply() {
    return manualWaterSupply;
  }


  public void setManualWaterSupply(List<ManualWaterSupply> manualWaterSupply) {
    this.manualWaterSupply = manualWaterSupply;
  }

}


