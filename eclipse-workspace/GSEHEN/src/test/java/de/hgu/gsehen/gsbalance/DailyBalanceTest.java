package de.hgu.gsehen.gsbalance;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.evapotranspiration.GeoData;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.CropDevelopmentStatus;
import de.hgu.gsehen.model.CropRootingZone;
import de.hgu.gsehen.model.Plot;

class DailyBalanceTest {
  GeoData location;
  SimpleDateFormat tag;

  DayData today;
  Plot plot;
  Crop crop;
  SimpleDateFormat soilStartDate;
  SimpleDateFormat cropStart;
  SimpleDateFormat cropEnd;

  @Test
  @BeforeEach
  void onCreate() {
    location = new GeoData(false, 7.95, 49.99, 110);
    tag = new SimpleDateFormat("yyyy-MM-dd");
    soilStartDate = new SimpleDateFormat("yyyy-MM-dd");
    cropStart = new SimpleDateFormat("yyyy-MM-dd");
    cropEnd = new SimpleDateFormat("yyyy-MM-dd");

    try {
      today = new DayData(tag.parse("2016-06-06"), 20.91875, 13.7, 28.4, 87.2708333333, null, null,
          28.32588, 1.0, 1.0381944444, 2.3, null, null, 2.2, null, null, null, null);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    crop = new Crop("Salat", true, 0.6, 0.8, 1.3, null, 10, 20, 30, null, "Pflanzung",
        "30% Bedeckung", "80%Bedeckunng", null, 10, 20, 30, null, "Toller Salat");

    CropDevelopmentStatus cropDevelopmentStatus = new CropDevelopmentStatus(null, null, null, null);
    CropRootingZone cropRootingZone = new CropRootingZone(null, null, null, null);
    try {
      plot = new Plot("Feld2", 200, null, null, 1.0, null, 120.0, null, null, null,
          soilStartDate.parse("2016-06-04"), 100.0, false, crop, cropDevelopmentStatus,
          cropRootingZone, cropStart.parse("2016-06-06"), cropEnd.parse("2016-09-06"), true);
    } catch (ParseException r) {
      r.printStackTrace();
    }
  }

  @Test
  void testdetermineCurrentKc() throws ParseException {
    DailyBalance.determineCurrentKc(today, plot);
    System.out.println(today.getCurrentKc());
    assertEquals(0.6, today.getCurrentKc(), 0.1);

    today.setDate(tag.parse("2016-06-16"));
    DailyBalance.determineCurrentKc(today, plot);
    System.out.println(today.getCurrentKc());
    assertEquals(0.8, today.getCurrentKc(), 0.1);

    today.setDate(tag.parse("2016-07-16"));
    DailyBalance.determineCurrentKc(today, plot);
    System.out.println(today.getCurrentKc());
    assertEquals(1.3, today.getCurrentKc(), 0.1);

    today.setDate(tag.parse("2016-09-06"));
    DailyBalance.determineCurrentKc(today, plot);
    System.out.println(today.getCurrentKc());
    assertEquals(1.3, today.getCurrentKc(), 0.1);


    today.setDate(tag.parse("2016-09-06"));
    crop.setKc3(null);
    crop.setPhase3(null);
    DailyBalance.determineCurrentKc(today, plot);
    System.out.println(today.getCurrentKc());
    assertEquals(0.8, today.getCurrentKc(), 0.1);

    today.setDate(tag.parse("2016-09-06"));
    crop.setKc2(null);
    crop.setPhase2(null);
    crop.setKc3(null);
    crop.setPhase3(null);
    DailyBalance.determineCurrentKc(today, plot);
    System.out.println(today.getCurrentKc());
    assertEquals(0.6, today.getCurrentKc(), 0.1);
  }

  @Test
  void testCalculateEtc() {
    DailyBalance.determineCurrentKc(today, plot);
    DailyBalance.calculateEtc(today, plot);

    System.out.println(today.getEtc());
  }

  @Test
  void testCalculateDailyBalance() {
    DailyBalance.determineCurrentKc(today, plot);
    DailyBalance.calculateEtc(today, plot);
    DailyBalance.calculateDailyBalance(today);
    System.out.println(today.getDailyBalance());
  }
}
