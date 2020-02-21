package de.hgu.gsehen.model;

import de.hgu.gsehen.evapotranspiration.DayData;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class WaterBalance {

  @Id
  @GeneratedValue
  private long id;
  @OneToMany(cascade = {CascadeType.ALL})
  private List<DayData> dailyBalances = new ArrayList<DayData>();

  public WaterBalance() {
  }

  public WaterBalance(List<DayData> dailyBalances) {
    super();
    this.dailyBalances = dailyBalances;
  }

  public List<DayData> getDailyBalances() {
    return dailyBalances;
  }

  public void setDailyBalances(List<DayData> dailyBalances) {
    this.dailyBalances = dailyBalances;
  }
}
