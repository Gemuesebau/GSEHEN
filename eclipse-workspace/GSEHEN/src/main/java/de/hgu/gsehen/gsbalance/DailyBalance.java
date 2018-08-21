package de.hgu.gsehen.gsbalance;

import de.hgu.gsehen.evapotranspiration.DayData;import

import de.hgu.gsehen.model.Plot;



public class DailyBalance {
  public static void determineCurrentKc(DayData dayData, Plot plot) {
    Date date = dayData.getDate();

    Double currentKc = 1.3;
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
    } ;
    Double et0 = dayData.getEt0();
    if (et0 == null) {
      throw new IllegalStateException("Et0 has not been calculated");
    } ;
    Double irrigation = dayData.getIrrigation();
    dayData.setDailyBalance(et0 - precipitation - irrigation);

  }
}
