package de.hgu.gsehen.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Table;

@Entity
public class SoilProfile {

  @Id
  private Set<Soil> soilType;
  private Set<SoilProfileDepth> profileDepth;

  public Set<Soil> getSoilType() {
    return soilType;
  }

  public void setSoilType(Set<Soil> soilType) {
    this.soilType = soilType;
  }

  public Set<SoilProfileDepth> getProfileDepth() {
    return profileDepth;
  }

  public void setProfileDepth(Set<SoilProfileDepth> profileDepth) {
    this.profileDepth = profileDepth;
  }

  public void configure(){}

  public void visualize(){}

  public void modify(){}
}
