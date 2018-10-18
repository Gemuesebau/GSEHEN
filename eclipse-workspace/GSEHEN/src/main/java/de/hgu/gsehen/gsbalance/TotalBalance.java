package de.hgu.gsehen.gsbalance;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.CropDevelopmentStatus;
import de.hgu.gsehen.model.CropRootingZone;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilManualData;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;

public class TotalBalance {

  @SuppressWarnings("checkstyle:javadocmethod")
  public static void determineCurrentRootingZone(DayData dayData, Plot plot,
      SoilProfile soilProfile) {
    final Date today = dayData.getDate();
    final Date cropStart = plot.getCropStart();
    Date cropEnd = plot.getCropEnd();
    final Date soilStart = plot.getSoilStartDate();
    Crop crop = plot.getCrop();
    CropRootingZone cropRootingZone = plot.getCropRootingZone();
    Integer rootingZone1;
    Integer rootingZone2;
    Integer rootingZone3;
    Integer rootingZone4;
    if (cropRootingZone.getRootingZone1() != null) {
      rootingZone1 = cropRootingZone.getRootingZone1();
    } else {
      rootingZone1 = crop.getRootingZone1();
    }

    if (cropRootingZone.getRootingZone2() != null) {
      rootingZone2 = cropRootingZone.getRootingZone2();
    } else {
      rootingZone2 = crop.getRootingZone2();
    }

    if (cropRootingZone.getRootingZone3() != null) {
      rootingZone3 = cropRootingZone.getRootingZone3();
    } else {
      rootingZone3 = crop.getRootingZone3();
    }

    if (cropRootingZone.getRootingZone4() != null) {
      rootingZone4 = cropRootingZone.getRootingZone4();
    } else {
      rootingZone4 = crop.getRootingZone4();
    }
    SoilManualData soilManualData = soilProfile.getSoilManualData();
    Integer soilZone;
    if (soilManualData != null && soilManualData.getSoilZone() != null) {
      soilZone = soilManualData.getSoilZone();
    } else {
      soilZone = 10;
    }
    CropDevelopmentStatus cropDevelopmentStatus = plot.getCropDevelopmentStatus();
    Integer currentRootingZone = null;
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
      currentRootingZone = 0;
    } else if (cropStart == null && today.compareTo(soilStart) >= 0) {
      currentRootingZone = soilZone;
    } else {
      if (cropEnd == null) {
        cropEnd = today;
      }
      if (today.compareTo(soilStart) >= 0 && today.compareTo(cropStart) < 0) {
        currentRootingZone = soilZone;
      }
      if (today.compareTo(cropStart) >= 0 && today.compareTo(cropEnd) <= 0) {
        LocalDate localToday = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localCropStart =
            cropStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localCropEnd = cropEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (localToday.compareTo(localCropStart.plusDays(phase1)) < 0
            || (localToday.compareTo(localCropEnd) <= 0 && phase2 == null)) {
          currentRootingZone = rootingZone1;
        } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2)) < 0
            || (localToday.compareTo(localCropEnd) <= 0 && phase3 == null)) {
          if (rootingZone2 == null) {
            throw new NullPointerException();
          }
          currentRootingZone = rootingZone2;
        } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2 + phase3)) < 0
            || (localToday.compareTo(localCropEnd) <= 0 && phase4 == null)) {
          if (rootingZone3 == null) {
            throw new NullPointerException();
          }
          currentRootingZone = rootingZone3;
        } else if (localToday
            .compareTo(localCropStart.plusDays(phase1 + phase2 + phase3 + phase4)) < 0
            || localToday.compareTo(localCropEnd) <= 0) {
          if (rootingZone4 == null) {
            throw new NullPointerException();
          }
          currentRootingZone = rootingZone4;
        }


      }
    }
    Integer maxRootingZone = plot.getRootingZone();
    if (maxRootingZone != null) {
      if (currentRootingZone > maxRootingZone) {
        currentRootingZone = maxRootingZone;
      }
    }
    dayData.setCurrentRootingZone(currentRootingZone);

  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public static void calculateCurrentAvailableSoilWater(DayData dayData, SoilProfile soilProfile) {
    Integer currentRootingZone = dayData.getCurrentRootingZone();
    Double currentAvailableSoilWater = 0.0;
    Integer remaningRootingZone = currentRootingZone;
    List<Soil> soils = soilProfile.getSoilType();
    List<SoilProfileDepth> profiles = soilProfile.getProfileDepth();
    int i;
    for (i = 0; i < soils.size(); i++) {
      Double rest = (profiles.get(i).getDepth() - remaningRootingZone);
      if (rest >= 0 || rest < 0 && i + 1 == soils.size()) {
        currentAvailableSoilWater += remaningRootingZone * 100.0 * 100.0
            * (soils.get(i).getAvailableWaterCapacity() / 100) / 1000;
        break;
      } else if (rest < 0) {
        currentAvailableSoilWater += (remaningRootingZone + rest) * 100.0 * 100.0
            * (soils.get(i).getAvailableWaterCapacity() / 100) / 1000;
        remaningRootingZone = Math.abs(rest.intValue());
      }
    }
    dayData.setCurrentAvailableSoilWater(currentAvailableSoilWater);
  }

  // TODO: Starkregenereignis und dauer der Pause konfigurierbar machen.
  /**
   * Method to calculate the total water balance of a plot.
   * 
   * @param plot the desired plot
   */
  public static void calculateTotalWaterBalance(Plot plot, SoilProfile soilProfile) {
    SoilManualData soilManualData = soilProfile.getSoilManualData();
    Double rainMax;
    Integer daysPause;
    if (soilManualData != null && soilManualData.getRainMax() != null) {
      rainMax = soilManualData.getRainMax();
    } else {
      rainMax = 30.0;
    }
    if (soilManualData != null && soilManualData.getDaysPause() != null) {
      daysPause = soilManualData.getDaysPause();
    } else {
      daysPause = 2;
    }
    Double startValue;
    List<DayData> dailyBalances = plot.getWaterBalance().getDailyBalances();
    if (dailyBalances.isEmpty()) {
      return;
    }
    if (plot.getSoilStartValue() != null) {
      startValue =
          dailyBalances.get(0).getCurrentAvailableSoilWater() * (plot.getSoilStartValue() / 100);
    } else {
      startValue = dailyBalances.get(0).getCurrentAvailableSoilWater();
    }
    int i;
    int size = dailyBalances.size();
    for (i = 0; i < size; i++) {
      plot.setCalculationPaused(false);
      Double currentTotalWaterBalance = null;
      if (i == 0) {
        currentTotalWaterBalance = startValue - dailyBalances.get(0).getDailyBalance();
        dailyBalances.get(0).setCurrentTotalWaterBalance(currentTotalWaterBalance);
      } else {
        currentTotalWaterBalance = dailyBalances.get(i - 1).getCurrentTotalWaterBalance()
            - dailyBalances.get(i).getDailyBalance();
        dailyBalances.get(i).setCurrentTotalWaterBalance(currentTotalWaterBalance);
      }
      // Calculation Pause
      int k = 0;
      if (currentTotalWaterBalance > dailyBalances.get(i).getCurrentAvailableSoilWater()
          && dailyBalances.get(i).getPrecipitation() > rainMax) {
        plot.setCalculationPaused(true);
        for (k = 1; k <= daysPause; k++) {
          if (i + k > size) {
            break;
          }
          Double maxWater = dailyBalances.get(i + k).getCurrentAvailableSoilWater();
          dailyBalances.get(i + k).setCurrentTotalWaterBalance(maxWater);

        }

      }
      // TotalWaterBalance not bigger than CurrentAvailableSoilWater
      dailyBalances.get(i)
          .setCurrentTotalWaterBalance(Math.min(dailyBalances.get(i).getCurrentTotalWaterBalance(),
              dailyBalances.get(i).getCurrentAvailableSoilWater()));

      if (k != 0 && k > 0) {
        i += k - 1;
      }
    }
  }

  /**
   * Method to recommend an irrigation decision for a plot.
   *
   * @param plot Plot in question
   */
  public static void recommendIrrigation(Plot plot) {
    RecommendedAction recommendedAction = new RecommendedAction(null, null, null, null);
    if (plot.getWaterBalance().getDailyBalances().isEmpty()) {
      recommendedAction.setRecommendation(RecommendedActionEnum.NO_DATA);
    } else {
      DayData currentDay = plot.getWaterBalance().getDailyBalances()
          .get(plot.getWaterBalance().getDailyBalances().size() - 1);
      Double currentAvailableSoilWater = currentDay.getCurrentAvailableSoilWater();
      Double waterContentToAim =
          currentAvailableSoilWater * 0.9 - currentDay.getCurrentTotalWaterBalance();
      Double availableWater = currentAvailableSoilWater * 0.3 - waterContentToAim;
      recommendedAction.setAvailableWater(availableWater);
      recommendedAction
          .setAvailableWaterPercent((availableWater / (currentAvailableSoilWater * 0.3)) * 100);
      recommendedAction.setAvailableWater(availableWater);
      final int projectedDaysToIrrigation = (int) Math.floor(availableWater / currentDay.getEtc());
      recommendedAction.setProjectedDaysToIrrigation(projectedDaysToIrrigation);
      recommendedAction.setWaterContentToAim(waterContentToAim);

      if (waterContentToAim > currentAvailableSoilWater) {
        recommendedAction.setRecommendation(RecommendedActionEnum.EXCESS);
        // throw new UnsupportedOperationException(
        // "The water balance exceeds the total available soil water\n"
        // + "- your plants are dead for sure \\u2620");
      } else {
        if (plot.getCalculationPaused()) {
          recommendedAction.setRecommendation(RecommendedActionEnum.PAUSE);
        } else {
          if (availableWater > 0) {
            if (projectedDaysToIrrigation == 0) {
              recommendedAction.setRecommendation(RecommendedActionEnum.NOW);
            } else {
              recommendedAction.setRecommendation(RecommendedActionEnum.SOON);
            }
          } else {
            recommendedAction.setRecommendation(RecommendedActionEnum.IRRIGATION);
          }
        }
      }
    }
    plot.setRecommendedAction(recommendedAction);
  }
}
