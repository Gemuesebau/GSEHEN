package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class WeatherData {
  @Id
  @GeneratedValue
  private long id;
  

}
