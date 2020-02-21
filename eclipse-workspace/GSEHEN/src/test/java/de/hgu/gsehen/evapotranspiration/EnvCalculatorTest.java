package de.hgu.gsehen.evapotranspiration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.junit.Test;

public class EnvCalculatorTest {

  @Test
  @SuppressWarnings("checkstyle:variabledeclarationusagedistance")
  public void testCalculateEt0() throws ParseException {
    GeoData location = new GeoData(false, 7.95, 49.99, 110);
    GeoData location2 = new GeoData(false, 7.95, 50.8, 100);
    SimpleDateFormat tag = new SimpleDateFormat("yyyy-MM-dd");

    DayData today = new DayData(tag.parse("2016-06-06"), 20.91875, 13.7, 28.4, 87.2708333333, null,
        null, 28.32588, 1.0, 1.0381944444, 0.0, null, null, 2.2, null, null, null, null);
    DayData todayBrussels =
        new DayData(tag.parse("2015-07-06"), 16.9, 12.3, 21.5, ((84.0 + 63.0) / 2.0), 63.0, 84.0,
            22.07, 0.0, 2.078, null, null, null, null, null, null, null, null);


    double etoBrussels = 3.9;
    EnvCalculator.calculateEt0(todayBrussels, location2);
    double calculatedBrusselsEt0 = todayBrussels.getEt0();
    System.out.println(todayBrussels.getEt0());
    assertEquals(etoBrussels, calculatedBrusselsEt0, 0.1);

    double currentEt0 = 4.9;
    EnvCalculator.calculateEt0(today, location);
    double calculatedEt0 = today.getEt0();
    System.out.println(today.getEt0());
    assertEquals(currentEt0, calculatedEt0, 0.1);

  }
}
