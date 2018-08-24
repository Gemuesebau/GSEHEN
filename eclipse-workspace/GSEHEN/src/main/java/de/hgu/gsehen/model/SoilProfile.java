package de.hgu.gsehen.model;

import java.util.List;

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
  private String name;

  @OneToMany(cascade = {CascadeType.ALL})
  private List<Soil> soilType;
  @OneToMany(cascade = {CascadeType.ALL})
  private List<SoilProfileDepth> profileDepth;

  public SoilProfile(String name, List<Soil> soilType, List<SoilProfileDepth> profileDepth) {
    super();
    this.name = name;
    this.soilType = soilType;
    this.profileDepth = profileDepth;
  }

  public SoilProfile() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Soil> getSoilType() {
    return soilType;
  }

  public void setSoilType(List<Soil> soilType) {
    this.soilType = soilType;
  }

  public List<SoilProfileDepth> getProfileDepth() {
    return profileDepth;
  }

  public void setProfileDepth(List<SoilProfileDepth> profileDepth) {
    this.profileDepth = profileDepth;
  }

  public void configure() {}

  public void visualize() {}

  public void modify() {}
}
