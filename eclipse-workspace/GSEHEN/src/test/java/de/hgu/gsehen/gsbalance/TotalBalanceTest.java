package de.hgu.gsehen.gsbalance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.evapotranspiration.GeoData;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;

class TotalBalanceTest {
  GeoData location;
  SimpleDateFormat tag;

  DayData today;
  Plot plot;
  Crop crop;
  SoilProfile soilProfile;
  Soil soil1;
  Soil soil2;
  Soil soil3;
  SoilProfileDepth depth1;
  SoilProfileDepth depth2;
  SoilProfileDepth depth3;
  SimpleDateFormat soilStartDate;
  SimpleDateFormat cropStart;
  SimpleDateFormat cropEnd;

  @Test
  @BeforeEach
  void onCreate() throws ParseException {
    location = new GeoData(false, 7.95, 49.99, 110);
    tag = new SimpleDateFormat("yyyy-MM-dd");
    soilStartDate = new SimpleDateFormat("yyyy-MM-dd");
    cropStart = new SimpleDateFormat("yyyy-MM-dd");
    cropEnd = new SimpleDateFormat("yyyy-MM-dd");

    today = new DayData(tag.parse("2016-06-06"), 20.91875, 13.7, 28.4, 87.2708333333, null, null,
        28.32588, 1.0, 1.0381944444, 2.3, null, null, 2.2, null);


    crop = new Crop("Salat", true, 0.6, 0.8, 1.3, null, 10, 20, 30, null, "Pflanzung",
        "30% Bedeckung", "80%Bedeckunng", null, 10, 20, 30, null, "Toller Salat");

    plot = new Plot("Feld2", 200, null, null, 1.0, null, 120.0, null, "bla",
        soilStartDate.parse("2016-06-04"), 100.0, false, crop, cropStart.parse("2016-06-06"),
        cropEnd.parse("2016-09-06"), true);
    soil1 = new Soil("Sand", 8.0, null);
    soil2 = new Soil("SandyLoam", 12.0, null);
    soil3 = new Soil("Loam", 17.0, null);
    depth1 = new SoilProfileDepth(11.0);
    depth2 = new SoilProfileDepth(8.0);
    depth3 = new SoilProfileDepth(11.0);
    List<Soil> soilList = new ArrayList<Soil>();
    soilList.addAll(Arrays.asList(soil1, soil2, soil3));
    List<SoilProfileDepth> profileList = new ArrayList<SoilProfileDepth>();
    profileList.addAll(Arrays.asList(depth1, depth2, depth3));
    soilProfile = new SoilProfile("Feld2", soilList, profileList);


  }

  @Test
  void testDetermineCurrentRootingZone() throws ParseException {
    TotalBalance.determineCurrentRootingZone(today, plot);

    TotalBalance.determineCurrentRootingZone(today, plot);
    System.out.println(today.getCurrentRootingZone());
    assert (10 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-06-16"));
    TotalBalance.determineCurrentRootingZone(today, plot);
    System.out.println(today.getCurrentRootingZone());
    assert (20 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-07-16"));
    TotalBalance.determineCurrentRootingZone(today, plot);
    System.out.println(today.getCurrentRootingZone());
    assert (30 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-09-06"));
    TotalBalance.determineCurrentRootingZone(today, plot);
    System.out.println(today.getCurrentRootingZone());
    assert (30 == today.getCurrentRootingZone());


    today.setDate(tag.parse("2016-09-06"));
    crop.setKc3(null);
    crop.setPhase3(null);
    TotalBalance.determineCurrentRootingZone(today, plot);
    System.out.println(today.getCurrentRootingZone());
    assert (20 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-09-06"));
    crop.setKc2(null);
    crop.setPhase2(null);
    crop.setKc3(null);
    crop.setPhase3(null);
    TotalBalance.determineCurrentRootingZone(today, plot);
    System.out.println(today.getCurrentRootingZone());
    assert (10 == today.getCurrentRootingZone());
  }

  @Test
  void testCalculateCurrentAvailableSoilWater() throws ParseException {

    TotalBalance.determineCurrentRootingZone(today, plot);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    assert (soilProfile.getSoilType().size() == 3);
    System.out.println("text" + today.getCurrentAvailableSoilWater());
    // assertEquals(today.getCurrentAvailableSoilWater(), 8.0, 0.01);


    // If there is only one Soil
    List<Soil> soil = new ArrayList<Soil>(Arrays.asList(soil1));
    soilProfile.setSoilType(soil);
    List<SoilProfileDepth> depth = new ArrayList<SoilProfileDepth>(Arrays.asList(depth1));
    soilProfile.setProfileDepth(depth);
    assert (soilProfile.getSoilType().size() == 1);
    TotalBalance.determineCurrentRootingZone(today, plot);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    System.out.println(today.getCurrentAvailableSoilWater());
    assertEquals(today.getCurrentAvailableSoilWater(), 8.0, 0.01);


    today.setDate(tag.parse("2016-09-06"));
    TotalBalance.determineCurrentRootingZone(today, plot);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    assertEquals(today.getCurrentAvailableSoilWater(), 8.0 * 3, 0.01);
  }
}
