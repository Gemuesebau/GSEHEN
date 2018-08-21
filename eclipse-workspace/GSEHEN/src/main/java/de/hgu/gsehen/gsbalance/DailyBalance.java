package de.hgu.gsehen.gsbalance;

import de.hgu.gsehen.evapotranspiration.DayData;


public class DailyBalance {
  public static void calculateEtc(DayData dayData) {
    Double et0 = dayData.getEt0();
    Double kc = 0.3;
    Double faktor = 1.0;
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
