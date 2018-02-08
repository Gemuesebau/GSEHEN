package de.hgu.gsehen.evapotranspiration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

public class EnvCalculatorTest {

  @Test
  public void testCalculateEt0() throws ParseException {
    GeoData location = new GeoData(false, 7.95, 49.99, 110);
    SimpleDateFormat tag = new SimpleDateFormat("yyyy-MM-dd");
    DayData today = new DayData(tag.parse("2016-06-06"), 20.91875, 13.7, 28.4, 87.2708333333,
        28.32588, 0.0, 1.0381944444, 0.0);
    double currentEt0 = 4.9;
    EnvCalculator.calculateEt0(today, location);
    double calculatedEt0 = today.getEt0();

    System.out.println(today.getEt0());
    assertEquals(currentEt0, calculatedEt0, 0.1);
  }
}
