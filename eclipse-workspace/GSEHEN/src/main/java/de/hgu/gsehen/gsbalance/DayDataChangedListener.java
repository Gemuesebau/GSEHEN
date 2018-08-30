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
          if (
              event.isFromWeatherDataSource(fieldWeatherDataSource)
              && DateUtil.between(
                  event.getDayData().getDate(),
                  CollectionUtil.nvl(plot.getSoilStartDate(), plot.getCropStart()),
                  plot.getCropEnd()
              )
          ) {
            for (DayData dayData : plot.getWaterBalance().getDailyBalances()) {
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
}
