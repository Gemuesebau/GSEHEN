package de.hgu.gsehen.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import de.hgu.gsehen.evapotranspiration.DayData;


@Entity
public class WaterBalance {

  @Id
  @GeneratedValue
  private long id;
  @OneToMany(cascade = {CascadeType.ALL})
  private List<DayData> dailyBalances = new ArrayList<DayData>();

  public WaterBalance() {}

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

  /*
   * Collections.sort(dailyBalances); Collections.sort(myList, new Comparator<MyObject>() { public
   * int compare(MyObject o1, MyObject o2) { return o1.getDateTime().compareTo(o2.getDateTime()); }
   * });
   */

}
