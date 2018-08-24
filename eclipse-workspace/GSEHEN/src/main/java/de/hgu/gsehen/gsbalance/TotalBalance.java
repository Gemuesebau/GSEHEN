package de.hgu.gsehen.gsbalance;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;

public class TotalBalance {

  public static void determineCurrentRootingZone(DayData dayData, Plot plot) {
    Date today = dayData.getDate();
    Date cropStart = plot.getCropStart();
    Date cropEnd = plot.getCropEnd();
    Date soilStart = plot.getSoilStartDate();
    Crop crop = plot.getCrop();
    Integer rootingZone1 = crop.getRootingZone1();
    Integer rootingZone2 = crop.getRootingZone2();
    Integer rootingZone3 = crop.getRootingZone3();
    Integer rootingZone4 = crop.getRootingZone4();
    Integer currentRootingZone = null;
    int phase1 = crop.getPhase1();
    Integer phase2 = crop.getPhase2();
    Integer phase3 = crop.getPhase3();
    Integer phase4 = crop.getPhase4();
    if (today.compareTo(soilStart) >= 0 && today.compareTo(cropStart) < 0) {
      currentRootingZone = 10;
    }
    if (today.compareTo(cropStart) >= 0 && today.compareTo(cropEnd) <= 0) {
      LocalDate localToday = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate localCropStart = cropStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate localCropEnd = cropEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      if (localToday.compareTo(localCropStart.plusDays(phase1)) < 0
          | (localToday.compareTo(localCropEnd) <= 0 && phase2 == null)) {
        currentRootingZone = rootingZone1;
      } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2)) < 0
          | (localToday.compareTo(localCropEnd) <= 0 && phase3 == null)) {
        if (rootingZone2 == null) {
          throw new NullPointerException();
        }
        currentRootingZone = rootingZone2;
      } else if (localToday.compareTo(localCropStart.plusDays(phase1 + phase2 + phase3)) < 0
          | (localToday.compareTo(localCropEnd) <= 0 && phase4 == null)) {
        if (rootingZone3 == null) {
          throw new NullPointerException();
        }
        currentRootingZone = rootingZone3;
      } else if (localToday
          .compareTo(localCropStart.plusDays(phase1 + phase2 + phase3 + phase4)) < 0
          | localToday.compareTo(localCropEnd) <= 0) {
        if (rootingZone4 == null) {
          throw new NullPointerException();
        }
        currentRootingZone = rootingZone4;
      }


    }
    dayData.setCurrentRootingZone(currentRootingZone);

  }

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

}
