package de.hgu.gsehen.gsbalance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.evapotranspiration.EnvCalculator;
import de.hgu.gsehen.evapotranspiration.GeoData;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.CropDevelopmentStatus;
import de.hgu.gsehen.model.CropRootingZone;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilManualData;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;
import de.hgu.gsehen.model.WaterBalance;

class TotalBalanceTest {
  GeoData location;
  SimpleDateFormat tag;

  DayData today, today2, today3, today4, today5;
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



  void onCreate(int days) throws ParseException {
    location = new GeoData(false, 7.95, 49.99, 110);
    tag = new SimpleDateFormat("yyyy-MM-dd");
    soilStartDate = new SimpleDateFormat("yyyy-MM-dd");
    cropStart = new SimpleDateFormat("yyyy-MM-dd");
    cropEnd = new SimpleDateFormat("yyyy-MM-dd");

    today = new DayData(tag.parse("2016-06-06"), 20.91875, 13.7, 28.4, 87.2708333333, null, null,
        28.32588, 0.0, 1.0381944444, 2.3, null, null, 5.0, null, null, null, null);

    today2 = new DayData(tag.parse("2016-06-07"), 20.91875, 13.7, 28.4, 87.2708333333, null, null,
        28.32588, 0.0, 1.0381944444, 2.3, null, null, 2.0, null, null, null, null);


    today3 = new DayData(tag.parse("2016-06-08"), 20.91875, 13.7, 28.4, 87.2708333333, null, null,
        28.32588, 0.0, 1.0381944444, 2.3, null, null, 0.0, null, null, null, null);

    today4 = new DayData(tag.parse("2016-06-08"), 20.91875, 13.7, 28.4, 87.2708333333, null, null,
        28.32588, 0.0, 1.0381944444, 2.3, null, null, 1.0, null, null, null, null);

    today5 = new DayData(tag.parse("2016-06-08"), 20.91875, 13.7, 28.4, 87.2708333333, null, null,
        28.32588, 0.0, 1.0381944444, 2.3, null, null, 1.0, null, null, null, null);
    crop = new Crop("Salat", true, 0.6, 0.8, 1.3, null, 10, 20, 30, null, "Pflanzung",
        "30% Bedeckung", "80%Bedeckunng", null, 10, 20, 30, null, "Toller Salat");

    CropDevelopmentStatus cropDevelopmentStatus = new CropDevelopmentStatus(null, null, null, null);
    CropRootingZone cropRootingZone = new CropRootingZone(null, null, null, null);

    plot = new Plot("Feld2", 200, null, null, 1.0, null, 120, null, null, null,
        soilStartDate.parse("2016-06-04"), null, false, crop, cropDevelopmentStatus,
        cropRootingZone, cropStart.parse("2016-06-06"), cropEnd.parse("2016-09-06"), true);
    SoilManualData soilManualData = new SoilManualData(null, null, null, null);
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
    soilProfile = new SoilProfile("Feld2", soilManualData, soilList, profileList);
    List<DayData> dailyBalances = new ArrayList<DayData>();
    switch (days) {
      case 1:
        dailyBalances.addAll(Arrays.asList(today));
        break;
      case 2:
        dailyBalances.addAll(Arrays.asList(today, today2));
        break;
      case 3:
        dailyBalances.addAll(Arrays.asList(today, today2, today3));
        break;
      case 4:
        dailyBalances.addAll(Arrays.asList(today, today2, today3, today4));
        break;

      case 5:
        dailyBalances.addAll(Arrays.asList(today, today2, today3, today4, today5));
        break;

      default:
        dailyBalances.addAll(Arrays.asList(today, today2, today3, today4));
        break;
    }
    WaterBalance waterBalance = new WaterBalance(dailyBalances);
    plot.setWaterBalance(waterBalance);
    // plot.setLocation(location); //TODO: After Merge of GeoPoint Location etc.

  }

  @Test
  void testDetermineCurrentRootingZone() throws ParseException {
    onCreate(1);

    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);

    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    // System.out.println(today.getCurrentRootingZone());
    assert (10 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-06-16"));
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    // System.out.println(today.getCurrentRootingZone());
    assert (20 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-07-16"));
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    // System.out.println(today.getCurrentRootingZone());
    assert (30 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-09-06"));
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    // System.out.println(today.getCurrentRootingZone());
    assert (30 == today.getCurrentRootingZone());


    today.setDate(tag.parse("2016-09-06"));
    crop.setKc3(null);
    crop.setPhase3(null);
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    // System.out.println(today.getCurrentRootingZone());
    assert (20 == today.getCurrentRootingZone());

    today.setDate(tag.parse("2016-09-06"));
    crop.setKc2(null);
    crop.setPhase2(null);
    crop.setKc3(null);
    crop.setPhase3(null);
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    // System.out.println(today.getCurrentRootingZone());
    assert (10 == today.getCurrentRootingZone());
  }

  @Test
  void testCalculateCurrentAvailableSoilWater() throws ParseException {
    onCreate(1);

    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    assert (soilProfile.getSoilType().size() == 3);
    assertEquals(today.getCurrentAvailableSoilWater(), 8.0, 0.01);

    today.setDate(tag.parse("2016-06-16"));
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    // System.out.println(today.getCurrentAvailableSoilWater());
    assertEquals(today.getCurrentAvailableSoilWater(), 20.1, 0.01);

    today.setDate(tag.parse("2016-09-06"));
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    assertEquals(today.getCurrentAvailableSoilWater(), 37.1, 0.01);

    // If there is only one Soil
    today.setDate(tag.parse("2016-06-06"));
    List<Soil> soil = new ArrayList<Soil>(Arrays.asList(soil1));
    soilProfile.setSoilType(soil);
    List<SoilProfileDepth> depth = new ArrayList<SoilProfileDepth>(Arrays.asList(depth1));
    soilProfile.setProfileDepth(depth);
    assert (soilProfile.getSoilType().size() == 1);
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    assertEquals(today.getCurrentAvailableSoilWater(), 8.0, 0.01);


    today.setDate(tag.parse("2016-09-06"));
    TotalBalance.determineCurrentRootingZone(today, plot, soilProfile);
    TotalBalance.calculateCurrentAvailableSoilWater(today, soilProfile);
    assertEquals(today.getCurrentAvailableSoilWater(), 8.0 * 3, 0.01);
  }

  @Test
  void testCalculateTotalWaterBalanceAndrecommendIrrigation() throws ParseException {
    onCreate(1);
    calculateGs();
    for (DayData dates : plot.getWaterBalance().getDailyBalances()) {
      System.out.println(dates.getCurrentTotalWaterBalance() + " " + dates.getDailyBalance());
    }

    onCreate(2);
    calculateGs();
    for (DayData dates : plot.getWaterBalance().getDailyBalances()) {
      System.out.println(dates.getCurrentTotalWaterBalance() + " " + dates.getDailyBalance());
    }

    onCreate(3);
    calculateGs();
    for (DayData dates : plot.getWaterBalance().getDailyBalances()) {
      System.out.println(dates.getCurrentTotalWaterBalance() + " " + dates.getDailyBalance());
    }

    onCreate(4);
    calculateGs();

    for (DayData dates : plot.getWaterBalance().getDailyBalances()) {
      System.out.println(dates.getCurrentTotalWaterBalance() + " " + dates.getDailyBalance());
    }
    onCreate(5);
    calculateGs();

    for (DayData dates : plot.getWaterBalance().getDailyBalances()) {
      System.out.println(dates.getCurrentTotalWaterBalance() + " " + dates.getDailyBalance());
    }
    onCreate(5);
    plot.getWaterBalance().getDailyBalances().get(0).setPrecipitation(32.0);
    calculateGs();
    for (DayData dates : plot.getWaterBalance().getDailyBalances()) {
      System.out.println(dates.getCurrentTotalWaterBalance() + " " + dates.getDailyBalance());
    }
  }

  private void calculateGs() {
    for (DayData elem : plot.getWaterBalance().getDailyBalances()) {
      System.out.println(elem);
      EnvCalculator.calculateEt0(elem, location);
      System.out.println("Et0 is " + elem.getEt0());
      DailyBalance.determineCurrentKc(elem, plot, null);
      System.out.println("Current kc " + elem.getCurrentKc());
      DailyBalance.calculateEtc(elem, plot);
      System.out.println("Current Etc " + elem.getEtc());
      DailyBalance.calculateDailyBalance(elem);
      System.out.println("Daily Balance " + elem.getDailyBalance());
      TotalBalance.determineCurrentRootingZone(elem, plot, soilProfile);
      System.out.println("Current rooting zone" + elem.getCurrentRootingZone());
      TotalBalance.calculateCurrentAvailableSoilWater(elem, soilProfile);
      System.out.println("Availabe Soil water " + elem.getCurrentAvailableSoilWater());
    }
    TotalBalance.calculateTotalWaterBalance(plot, soilProfile);

    try {
      TotalBalance.recommendIrrigation(plot);
      System.out.println(plot.getRecommendedAction().getRecommendation());
      System.out.println(
          "There is " + plot.getRecommendedAction().getAvailableWater() + " mm water left");
      System.out
          .println("This is " + plot.getRecommendedAction().getAvailableWaterPercent() + " %");
    } catch (UnsupportedOperationException e) {
      System.out.println(e);
    }
  }
}
