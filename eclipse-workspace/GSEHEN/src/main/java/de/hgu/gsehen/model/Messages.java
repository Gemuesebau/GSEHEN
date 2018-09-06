package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Messages {
  
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  private String key;
  private String localeId;
  private String text;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getLocaleId() {
    return localeId;
  }

  public void setLocaleId(String localeId) {
    this.localeId = localeId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}

