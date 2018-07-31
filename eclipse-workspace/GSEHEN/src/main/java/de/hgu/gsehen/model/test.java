package de.hgu.gsehen.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "NAME")
public class test{
  @Id
  private String name;

  

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
