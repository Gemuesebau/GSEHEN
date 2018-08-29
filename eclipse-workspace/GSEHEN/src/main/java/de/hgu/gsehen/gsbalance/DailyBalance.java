package de.hgu.gsehen.gsbalance;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Plot;


public class DailyBalance {
  public static void determineCurrentKc(DayData dayData, Plot plot) {
    Date today = dayData.getDate();
    Date cropStart = plot.getCropStart();
    Date cropEnd = plot.getCropEnd();
    Date soilStart = plot.getSoilStartDate();
    Crop crop = plot.getCrop();
    Double kc1 = crop.getKc1();
    Double kc2 = crop.getKc2();
    Double kc3 = crop.getKc3();
    Double kc4 = crop.getKc4();
    Double currentKc = null;
    int phase1 = crop.getPhase1();
    Integer phase2 = crop.getPhase2();
    Integer phase3 = crop.getPhase3();
    Integer phase4 = crop.getPhase4();
    if (today.compareTo(soilStart) >= 0 && today.compareTo(cropStart) < 0) {
      currentKc = 0.3;
    }
    if (today.compareTo(cropStart) >= 0 && today.compareTo(cropEnd) <= 0) {
      LocalDate localToday = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate localCropStart = cropStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate localCropEnd = cropEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      if (localToday.compareTo(localCropStart.plusDays(phase1)) < 0
          | (localToday.compareTo(localCropEnd) <= 0 && phase2 == null)) {
        currentKc = kc1;
      } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2)) < 0
          | (localToday.compareTo(localCropEnd) <= 0 && phase3 == null)) {
        if (kc2 == null) {
          throw new NullPointerException();
        }
        currentKc = kc2;
      } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2 + phase3)) < 0
          | (localToday.compareTo(localCropEnd) <= 0 && phase4 == null)) {
        if (kc3 == null) {
          throw new NullPointerException();
        }
        currentKc = kc3;
      } else if (localToday
          .compareTo(localCropStart.plusDays(phase1 + phase2 + phase3 + phase4)) < 0
          | localToday.compareTo(localCropEnd) <= 0) {
        if (kc4 == null) {
          throw new NullPointerException();
        }
        currentKc = kc4;
      }


    }
    dayData.setCurrentKc(currentKc);

  }



  public static void calculateEtc(DayData dayData, Plot plot) {
    Double et0 = dayData.getEt0();
    Double kc = dayData.getCurrentKc();
    Double faktor = plot.getScalingFactor();
    dayData.setEtc(et0 * kc * faktor);
  }



  public static void calculateDailyBalance(DayData dayData) throws IllegalStateException {
    Double precipitation = dayData.getPrecipitation();
    if (precipitation == null) {
      throw new IllegalStateException("Precipitation has net been provided");
    }
    Double etc = dayData.getEtc();
    if (etc == null) {
      throw new IllegalStateException("Etc has not been calculated");
    }
    Double irrigation = dayData.getIrrigation();
    dayData.setDailyBalance(etc - precipitation - irrigation);

  }
}
