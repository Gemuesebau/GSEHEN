package de.hgu.gsehen.gsbalance;

import static org.junit.jupiter.api.Assertions.fail;

import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.evapotranspiration.GeoData;
import de.hgu.gsehen.model.Plot;

class DailyBalanceTest {

  GeoData location = new GeoData(false, 7.95, 49.99, 110);
  SimpleDateFormat tag = new SimpleDateFormat("yyyy-MM-dd");

  DayData today = new DayData(tag.parse("2016-06-06"), 20.91875, 13.7, 28.4, 87.2708333333, null,
      null, 28.32588, 1.0, 1.0381944444, 0.0, null, null, 2.2, null);
  Plot plot = new Plot();



  @Test
  void testDetermineCurrentKc() {
    fail("Not yet implemented");
  }

  @Test
  void testCalculateEtc() {
    fail("Not yet implemented");
  }

  @Test
  void testCalculateDailyBalance() {
    fail("Not yet implemented");
  }

}
