package de.hgu.gsehen.model;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class SoilProfile {

  @Id
  @GeneratedValue
  private long id;
  @OneToMany (cascade = {CascadeType.ALL})
  private Set<Soil> soilType;
  @OneToMany(cascade = {CascadeType.ALL})
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
