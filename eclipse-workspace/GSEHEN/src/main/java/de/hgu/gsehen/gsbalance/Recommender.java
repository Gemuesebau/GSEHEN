package de.hgu.gsehen.gsbalance;

import static de.hgu.gsehen.util.MessageUtil.logMessage;

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
import de.hgu.gsehen.util.LoggingList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Recommender {
  private static final Logger LOGGER = Logger.getLogger(Recommender.class.getName());

  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(DayDataChanged.class,
        event -> forAllFieldsAndPlots((field, plot) -> replaceDayData(event, field, plot)));
    gsehenInstance.registerForEvent(ManualDataChanged.class,
        event -> performCalculations(event.getField(), event.getPlot()));
  }

  @SuppressWarnings({ "checkstyle:needbraces", "checkstyle:indentation" })
  private static void forAllFieldsAndPlots(BiConsumer<Field, Plot> handler) {
    final LoggingList<Farm> farmsList = Gsehen.getInstance().getFarmsList();
    if (farmsList != null) for (Farm farm : farmsList) {
      if (farm != null && farm.getFields() != null) for (Field field : farm.getFields()) {
        if (field != null && field.getPlots() != null) for (Plot plot : field.getPlots()) {
          if (plot != null) handler.accept(field, plot);
        }
      }
    }
  }

  private void replaceDayData(DayDataChanged event, Field field, Plot plot) {
    if (event.isFromWeatherDataSource(field.getWeatherDataSourceUuid())) {
      if (plot.getWaterBalance() != null && plot.getWaterBalance().getDailyBalances() != null) {
        logMessage(LOGGER, Level.FINE, "clear.day.data.for.plot", plot.getName());
        plot.getWaterBalance().getDailyBalances().clear();
      }
    }
    final List<DayData> eventDayDataList = event.getDayData();
    if (eventDayDataList != null) {
      for (DayData eventDayData : eventDayDataList) {
        if (eventDayData == null) {
          continue;
        }
        Date date = eventDayData.getDate();
        if (date != null && event.isFromWeatherDataSource(field.getWeatherDataSourceUuid())
            && UtilityFunctions.determineDataStartDate(plot).compareTo(date) <= 0) {
          logMessage(LOGGER, Level.FINE, "copy.day.data.for.plot.at.date", plot.getName(), date);
          copyWeatherData(eventDayData, getCurrentDayData(plot, date));
        }
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
