package de.hgu.gsehen.gsbalance;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.evapotranspiration.EnvCalculator;
import de.hgu.gsehen.event.DayDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.event.ManualDataChanged;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.ManualWaterSupply;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.WaterBalance;
import de.hgu.gsehen.util.CollectionUtil;
import de.hgu.gsehen.util.DateUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Recommender {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(DayDataChanged.class,
        new GsehenEventListener<DayDataChanged>() {
        @Override
        public void handle(DayDataChanged event) {
          for (Farm farm : gsehenInstance.getFarmsList()) {
            for (Field field : farm.getFields()) {
              for (Plot plot : field.getPlots()) {
                final DayData eventDayData = event.getDayData();
                final Date eventDayDataDate = eventDayData.getDate();
                if (
                    event.isFromWeatherDataSource(field.getWeatherDataSource())
                    && DateUtil.between(
                        eventDayDataDate,
                        CollectionUtil.nvl(plot.getSoilStartDate(), plot.getCropStart()),
                        plot.getCropEnd()
                    )
                ) {
                  copyWeatherData(eventDayData, getCurrentDayData(plot, eventDayDataDate));
                  performCalculations(field, plot);
                }
              }
            }
          }
        }
      });
    gsehenInstance.registerForEvent(ManualDataChanged.class,
        event -> performCalculations(event.getField(), event.getPlot()));
  }

  private DayData getCurrentDayData(Plot plot, final Date eventDayDataDate) {
    if (plot == null) {
      throw new IllegalArgumentException("No plot given for day data calculation!"); 
    }
    if (plot.getWaterBalance() == null) {
      plot.setWaterBalance(new WaterBalance());
    }
    if (plot.getWaterBalance().getDailyBalances() == null) {
      plot.getWaterBalance().setDailyBalances(new ArrayList<DayData>());
    }
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
    for (DayData dayData : plot.getWaterBalance().getDailyBalances()) {
      applyManualData(dayData, plot);
      EnvCalculator.calculateEt0(dayData, field.getWeatherDataSource().getLocation());
      DailyBalance.determineCurrentKc(dayData, plot);
      DailyBalance.calculateEtc(dayData, plot);
      DailyBalance.calculateDailyBalance(dayData);
      TotalBalance.determineCurrentRootingZone(dayData, plot);
      TotalBalance.calculateCurrentAvailableSoilWater(dayData, field.getSoilProfile());
    }
    TotalBalance.calculateTotalWaterBalance(plot);
    TotalBalance.recommendIrrigation(plot);
    gsehenInstance.sendRecommendedActionChanged(plot, null);
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
}
