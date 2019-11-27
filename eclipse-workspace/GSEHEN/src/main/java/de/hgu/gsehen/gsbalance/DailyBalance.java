package de.hgu.gsehen.gsbalance;

import static de.hgu.gsehen.util.MessageUtil.logMessage;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.CropDevelopmentStatus;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.SoilProfile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DailyBalance {

  private static final Logger LOGGER = Logger.getLogger(DailyBalance.class.getName());

  /**
   * Method to determine the current kc-value for the day and actual crop on the plot.
   * 
   * @param dayData the day
   * @param plot the Plot
   * @param soilProfile the soil profile
   */
  @SuppressWarnings("checkstyle:variabledeclarationusagedistance")
  public static void determineCurrentKc(DayData dayData, Plot plot, SoilProfile soilProfile) {
    Date today = dayData.getDate();
    Date cropStart = plot.getCropStart();
    Date cropEnd = plot.getCropEnd();
    Date soilStart = plot.getSoilStartDate();
    Crop crop = plot.getCrop();
    CropDevelopmentStatus cropDevelopmentStatus = plot.getCropDevelopmentStatus();
    Double kc1 = crop.getKc1();
    Double kc2 = crop.getKc2();
    Double kc3 = crop.getKc3();
    Double kc4 = crop.getKc4();
    Double soilKc;
    if (soilProfile != null) {
      if (soilProfile.getSoilManualData().getSoilKc() != null) {
        soilKc = soilProfile.getSoilManualData().getSoilKc();
      } else {
        soilKc = 0.3;
        logMessage(LOGGER, Level.INFO, "no.soil.kc.set.to.standard.0.3");
      }
    } else {
      soilKc = 0.3;
      logMessage(LOGGER, Level.INFO, "no.soil.kc.set.to.standard.0.3");
    }
    Double currentKc = null;
    int phase1;
    Integer phase2;
    Integer phase3;
    Integer phase4;
    if (cropDevelopmentStatus.getPhase1() != null) {
      phase1 = cropDevelopmentStatus.getPhase1();
    } else {
      phase1 = crop.getPhase1();
    }

    if (cropDevelopmentStatus.getPhase2() != null) {
      phase2 = cropDevelopmentStatus.getPhase2();
    } else {
      phase2 = crop.getPhase2();
    }

    if (cropDevelopmentStatus.getPhase3() != null) {
      phase3 = cropDevelopmentStatus.getPhase3();
    } else {
      phase3 = crop.getPhase3();
    }
    if (cropDevelopmentStatus.getPhase4() != null) {
      phase4 = cropDevelopmentStatus.getPhase4();
    } else {
      phase4 = crop.getPhase4();
    }

    if (soilStart == null && cropStart == null) {
      currentKc = 0.0;
    } else if (cropStart == null && today.compareTo(soilStart) >= 0) {
      currentKc = soilKc;
    } else {
      if (cropEnd == null) {
        cropEnd = today;
      }

      if (soilStart != null) {
        if (today.compareTo(soilStart) >= 0 && today.compareTo(cropStart) < 0) {
          currentKc = soilKc;
        }
      }
      if (today.compareTo(cropStart) >= 0 && today.compareTo(cropEnd) <= 0) {
        LocalDate localToday = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localCropStart =
            cropStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localCropEnd = cropEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (localToday.compareTo(localCropStart.plusDays(phase1)) < 0
            || (localToday.compareTo(localCropEnd) <= 0 && phase2 == null)) {
          currentKc = kc1;
        } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2)) < 0
            || (localToday.compareTo(localCropEnd) <= 0 && phase3 == null)) {
          if (kc2 == null) {
            throw new NullPointerException();
          }
          currentKc = kc2;
        } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2 + phase3)) < 0
            || (localToday.compareTo(localCropEnd) <= 0 && phase4 == null)) {
          if (kc3 == null) {
            throw new NullPointerException();
          }
          currentKc = kc3;
        } else if (localToday
            .compareTo(localCropStart.plusDays(phase1 + phase2 + phase3 + phase4)) < 0
            || localToday.compareTo(localCropEnd) <= 0) {
          if (kc4 == null) {
            throw new NullPointerException();
          }
          currentKc = kc4;
        }


      }
    }
    dayData.setCurrentKc(currentKc);

  }



  /**
   * Calculates the actual/current crop evapotranspiration.
   * 
   * @param dayData the day
   * @param plot the plot
   */
  public static void calculateEtc(DayData dayData, Plot plot) {
    // TODO: Add logging event
    Double et0 = dayData.getEt0();
    Double kc = dayData.getCurrentKc();
    Double faktor = plot.getScalingFactor();
    if (faktor == null) {
      faktor = 1.0;
    }
    dayData.setEtc(et0 * kc * faktor);
  }



  /**
   * Method to calculate the daily water balance.
   * 
   * @param dayData the day
   * @throws IllegalStateException if prerequisites are not fullfilled
   */
  public static void calculateDailyBalance(DayData dayData) throws IllegalStateException {
    Double precipitation = dayData.getPrecipitation();
    if (precipitation == null) {
      throw new IllegalStateException("Precipitation has net been provided"); // logging
    }
    Double etc = dayData.getEtc();
    if (etc == null) {
      throw new IllegalStateException("Etc has not been calculated"); // logging
    }
    Double irrigation = dayData.getIrrigation();
    dayData.setDailyBalance(etc - precipitation - irrigation);

  }
}
