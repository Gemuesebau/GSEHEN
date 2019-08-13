package de.hgu.gsehen.gsbalance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.evapotranspiration.EnvCalculator;
import de.hgu.gsehen.evapotranspiration.UtilityFunctions;
import de.hgu.gsehen.event.DayDataChanged;
import de.hgu.gsehen.event.ManualDataChanged;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.ManualData;
import de.hgu.gsehen.model.ManualWaterSupply;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.WaterBalance;

public class Recommender {
  private static final Logger LOGGER = Logger.getLogger(Recommender.class.getName());
  private static final String COPY_WD_LOGLEVEL = System.getProperty("copyWdLoglevel", "FINE");

  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(DayDataChanged.class,
        event -> forAllFieldsAndPlots((field, plot) -> copyWeatherData(event, field, plot)));
    gsehenInstance.registerForEvent(ManualDataChanged.class,
        event -> performCalculations(event.getField(), event.getPlot()));
  }

  private Level getLevelForName(String copyWdLoglevel) {
    try {
      return (Level) Level.class.getField(copyWdLoglevel).get(null);
    } catch (Exception e) {
      return Level.INFO;
    }
  }

  private DayData getCurrentDayData(Plot plot, final Date eventDayDataDate) {
    if (plot == null) {
      throw new IllegalArgumentException("No plot given for day data calculation!");
    }
    guaranteeDailyBalances(plot);
    final List<DayData> plotDayDataList = plot.getWaterBalance().getDailyBalances();
    DayData plotCurrentDayData = null;
    for (DayData plotDayData : plotDayDataList) {
      if (eventDayDataDate.equals(plotDayData.getDate())) {
        plotCurrentDayData = plotDayData;
        break;
      }
    }
    if (plotCurrentDayData == null) {
      plotCurrentDayData = new DayData();
      plotCurrentDayData.setDate(eventDayDataDate);
      plotDayDataList.add(plotCurrentDayData);
    }
    return plotCurrentDayData;
  }

  private void applyManualData(DayData dayData, Plot plot) {
    if (plot == null) {
      throw new IllegalArgumentException("No plot given for manual data processing!");
    }
    guaranteeManualData(plot);
    final Date date = dayData.getDate();
    final List<ManualWaterSupply> manualList = plot.getManualData().getManualWaterSupply();
    double irrigation = 0.0;
    Double precipitation = null;
    for (ManualWaterSupply waterSupply : manualList) {
      if (date.equals(waterSupply.getDate())) {
        if (waterSupply.getIrrigation() != null) {
          irrigation = waterSupply.getIrrigation();
        }
        if (waterSupply.getPrecipitation() != null) {
          precipitation = waterSupply.getPrecipitation();
        }
        break;
      }
    }
    dayData.setIrrigation(irrigation);
    if (precipitation != null) {
      dayData.setPrecipitation(precipitation);
    }
  }

  private void performCalculations(Field field, Plot plot) {
    if (field == null || plot == null) {
      throw new IllegalArgumentException("No field or plot given for day data calculation!");
    }
    guaranteeDailyBalances(plot);
    for (DayData dayData : plot.getWaterBalance().getDailyBalances()) {
      applyManualData(dayData, plot);
      EnvCalculator.calculateEt0(dayData, gsehenInstance
          .getWeatherDataSourceForUuid(field.getWeatherDataSourceUuid()).getLocation());
      DailyBalance.determineCurrentKc(dayData, plot,
          gsehenInstance.getSoilProfileForUuid(field.getSoilProfileUuid()));
      DailyBalance.calculateEtc(dayData, plot);
      DailyBalance.calculateDailyBalance(dayData);
      TotalBalance.determineCurrentRootingZone(dayData, plot,
          gsehenInstance.getSoilProfileForUuid(field.getSoilProfileUuid()));
      TotalBalance.calculateCurrentAvailableSoilWater(dayData,
          gsehenInstance.getSoilProfileForUuid(field.getSoilProfileUuid()));
    }
    TotalBalance.calculateTotalWaterBalance(plot,
        gsehenInstance.getSoilProfileForUuid(field.getSoilProfileUuid()));
    TotalBalance.recommendIrrigation(plot);
    gsehenInstance.sendRecommendedActionChanged(plot, null);
  }

  private void copyWeatherData(DayDataChanged event, Field field, Plot plot) {
    final List<DayData> eventDayDataList = event.getDayData();
    for (DayData eventDayData : eventDayDataList) {
      if (eventDayData == null) {
        continue;
      }
      Date date = eventDayData.getDate();
      if (event.isFromWeatherDataSource(field.getWeatherDataSourceUuid())
          && UtilityFunctions.determineDataStartDate(plot).compareTo(date) <= 0) {
        LOGGER.log(getLevelForName(COPY_WD_LOGLEVEL),
            "Replacing day data for plot " + plot.getName() + " at " + date);
        copyWeatherData(eventDayData, getCurrentDayData(plot, date));
      }
    }
    performCalculations(field, plot);
  }

  private void copyWeatherData(DayData eventDayData, DayData plotCurrentDayData) {
    plotCurrentDayData.setTempMax(eventDayData.getTempMax());
    plotCurrentDayData.setTempMin(eventDayData.getTempMin());
    plotCurrentDayData.setTempMean(eventDayData.getTempMean());
    plotCurrentDayData.setAirHumidityRelMax(eventDayData.getAirHumidityRelMax());
    plotCurrentDayData.setAirHumidityRelMin(eventDayData.getAirHumidityRelMin());
    plotCurrentDayData.setAirHumidityRelMean(eventDayData.getAirHumidityRelMean());
    plotCurrentDayData.setGlobalRad(eventDayData.getGlobalRad());
    plotCurrentDayData.setPrecipitation(eventDayData.getPrecipitation());
    plotCurrentDayData.setWindspeed2m(eventDayData.getWindspeed2m());
  }

  /**
   * Clears all day data related to the weather data source identified by the given UUID.
   *
   * @param weatherDataSourceUuid the UUID of the WDS for which the day data is to be deleted
   */
  public static void clearDayData(String weatherDataSourceUuid) {
    forAllFieldsAndPlots((field, plot) -> {
      if (weatherDataSourceUuid.equals(field.getWeatherDataSourceUuid())) {
        if (plot.getWaterBalance() != null) {
          plot.getWaterBalance().getDailyBalances().clear();
        }
      }
    });
  }

  private static void forAllFieldsAndPlots(BiConsumer<Field, Plot> handler) {
    for (Farm farm : Gsehen.getInstance().getFarmsList()) {
      for (Field field : farm.getFields()) {
        for (Plot plot : field.getPlots()) {
          handler.accept(field, plot);
        }
      }
    }
  }

  private void guaranteeDailyBalances(Plot plot) {
    if (plot.getWaterBalance() == null) {
      plot.setWaterBalance(new WaterBalance());
    }
    if (plot.getWaterBalance().getDailyBalances() == null) {
      plot.getWaterBalance().setDailyBalances(new ArrayList<DayData>());
    }
  }

  private void guaranteeManualData(Plot plot) {
    if (plot.getManualData() == null) {
      plot.setManualData(new ManualData());
    }
    if (plot.getManualData().getManualWaterSupply() == null) {
      plot.getManualData().setManualWaterSupply(new ArrayList<ManualWaterSupply>());
    }
  }
}
