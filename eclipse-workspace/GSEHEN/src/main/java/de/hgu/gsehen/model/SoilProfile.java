package de.hgu.gsehen.model;

import java.util.Set;

public class SoilProfile {
  // TODO Sets auf drei Elemente beschränken?!

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
