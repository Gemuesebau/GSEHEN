package de.hgu.gsehen.model;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class SoilProfile implements Named {

  @Id
  @GeneratedValue
  private long id;
  private String uuid;
  private String name;
  @OneToOne(cascade = {CascadeType.ALL})
  private SoilManualData soilManualData;
  @OneToMany(cascade = {CascadeType.ALL})
  private List<Soil> soilType;
  @OneToMany(cascade = {CascadeType.ALL})
  private List<SoilProfileDepth> profileDepth;

  public SoilProfile(String uuid) {
    this();
    this.uuid = uuid;
  }

  /**
   * Creates a new SoilProfile with the given "payload" data. This constructor doesn't result in the
   * UUID being (newly) created, and is thus only suited for tests! The given lists must have the
   * same size, since soils and depths belong to one another each.
   *
   * @param name a name for the new SoilProfile
   * @param soilManualData the soil's manual data
   * @param soilType the list of soils in this profile
   * @param profileDepth the list of soil depths in this profile
   */
  public SoilProfile(String name, SoilManualData soilManualData, List<Soil> soilType,
      List<SoilProfileDepth> profileDepth) {
    this();
    this.name = name;
    this.soilManualData = soilManualData;
    this.soilType = soilType;
    this.profileDepth = profileDepth;
  }

  public SoilProfile() {
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
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

  public void configure() {
  }

  public void visualize() {
  }

  public void modify() {
  }

  public SoilManualData getSoilManualData() {
    return soilManualData;
  }

  public void setSoilManualData(SoilManualData soilManualData) {
    this.soilManualData = soilManualData;
  }
}
