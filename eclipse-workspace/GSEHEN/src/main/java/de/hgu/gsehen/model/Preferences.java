package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Preferences {
  @Id
  @GeneratedValue
  private long id;

  private String key;

  private String value;

  public Preferences() {
    super();
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public Preferences(String key, String value) {
    this();
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
