package de.hgu.gsehen.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import de.hgu.gsehen.evapotranspiration.GeoData;

@Entity
public class WeatherDataSource {
  @Id
  @GeneratedValue
  private long id;

  public GeoData getLocation() {
    // FIXME add persistent / configurable property "location"!!
    return new GeoData(false, 7.95, 49.99, 110);
  }
}
