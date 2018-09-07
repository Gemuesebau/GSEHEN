package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Class representing a crop.
 *
 * @author AT
 */
@Entity
public class Crop {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String name;
  private boolean active;
  private double kc1;
  private Double kc2;
  private Double kc3;
  private Double kc4;
  private int phase1;
  private Integer phase2;
  private Integer phase3;
  private Integer phase4;
  private String bbch1;
  private String bbch2;
  private String bbch3;
  private String bbch4;
  private int rootingZone1;
  private Integer rootingZone2;
  private Integer rootingZone3;
  private Integer rootingZone4;
  private String description;

  public Crop() {
    super();
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public Crop(String name, boolean active, double kc1, Double kc2, Double kc3, Double kc4,
      int phase1, Integer phase2, Integer phase3, Integer phase4, String bbch1, String bbch2,
      String bbch3, String bbch4, int rootingZone1, Integer rootingZone2, Integer rootingZone3,
      Integer rootingZone4, String description) {

    this.name = name;
    this.active = active;
    this.kc1 = kc1;
    this.kc2 = kc2;
    this.kc3 = kc3;
    this.kc4 = kc4;
    this.phase1 = phase1;
    this.phase2 = phase2;
    this.phase3 = phase3;
    this.phase4 = phase4;
    this.bbch1 = bbch1;
    this.bbch2 = bbch2;
    this.bbch3 = bbch3;
    this.bbch4 = bbch4;
    this.rootingZone1 = rootingZone1;
    this.rootingZone2 = rootingZone2;
    this.rootingZone3 = rootingZone3;
    this.rootingZone4 = rootingZone4;
    this.description = description;

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public double getKc1() {
    return kc1;
  }

  public void setKc1(double kc1) {
    this.kc1 = kc1;
  }

  public Double getKc2() {
    return kc2;
  }

  public void setKc2(Double kc2) {
    this.kc2 = kc2;
  }

  public Double getKc3() {
    return kc3;
  }

  public void setKc3(Double kc3) {
    this.kc3 = kc3;
  }

  public Double getKc4() {
    return kc4;
  }

  public void setKc4(Double kc4) {
    this.kc4 = kc4;
  }

  public int getPhase1() {
    return phase1;
  }

  public void setPhase1(int phase1) {
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

  public String getBbch1() {
    return bbch1;
  }

  public void setBbch1(String bbch1) {
    this.bbch1 = bbch1;
  }

  public String getBbch2() {
    return bbch2;
  }

  public void setBbch2(String bbch2) {
    this.bbch2 = bbch2;
  }

  public String getBbch3() {
    return bbch3;
  }

  public void setBbch3(String bbch3) {
    this.bbch3 = bbch3;
  }

  public String getBbch4() {
    return bbch4;
  }

  public void setBbch4(String bbch4) {
    this.bbch4 = bbch4;
  }

  public int getRootingZone1() {
    return rootingZone1;
  }

  public void setRootingZone1(int rootingZone1) {
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Crop other = (Crop) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }
}
