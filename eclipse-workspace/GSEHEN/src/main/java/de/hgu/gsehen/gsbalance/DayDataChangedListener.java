package de.hgu.gsehen.gsbalance;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.evapotranspiration.EnvCalculator;
import de.hgu.gsehen.event.DayDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.CollectionUtil;
import de.hgu.gsehen.util.DateUtil;
import java.util.Date;
import java.util.List;

public class DayDataChangedListener implements GsehenEventListener<DayDataChanged> {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(DayDataChanged.class, this);
  }

  @Override
  public void handle(DayDataChanged event) {
    for (Farm farm : gsehenInstance.getFarmsList()) {
      for (Field field : farm.getFields()) {
        for (Plot plot : field.getPlots()) {
          final WeatherDataSource fieldWeatherDataSource = field.getWeatherDataSource();
          final DayData eventDayData = event.getDayData();
          final Date eventDayDataDate = eventDayData.getDate();
          if (
              event.isFromWeatherDataSource(fieldWeatherDataSource)
              && DateUtil.between(
                  eventDayDataDate,
                  CollectionUtil.nvl(plot.getSoilStartDate(), plot.getCropStart()),
                  plot.getCropEnd()
              )
          ) {
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
            copyWeatherData(eventDayData, plotCurrentDayData);
            for (DayData dayData : plotDayDataList) {
              EnvCalculator.calculateEt0(dayData, fieldWeatherDataSource.getLocation());
              DailyBalance.determineCurrentKc(dayData, plot);
              DailyBalance.calculateEtc(dayData, plot);
              DailyBalance.calculateDailyBalance(dayData);
              TotalBalance.determineCurrentRootingZone(dayData, plot);
              TotalBalance.calculateCurrentAvailableSoilWater(dayData, field.getSoilProfile());
            }
            TotalBalance.calculateTotalWaterBalance(plot);
            TotalBalance.recommendIrrigation(plot);
            gsehenInstance.sendRecommendedActionChanged(plot, null);
            // receivers may use plot.getRecommendedAction().getRecommendation()
          }
        }
      }
    }
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
