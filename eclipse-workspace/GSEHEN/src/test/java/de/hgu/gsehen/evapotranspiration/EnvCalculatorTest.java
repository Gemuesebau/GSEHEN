package de.hgu.gsehen.evapotranspiration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.Test;

public class EnvCalculatorTest {

  @Test
  public void testCalculateEt0() {
    GeoData location = new GeoData(false, 0, 0, 0);
    DayData today = new DayData(null, 0, 0, 0, 0, 0, 0, 0, 0);
    double currentEt0 = 5;
    EnvCalculator.calculateEt0(today, location);
    double calculatedEt0 = today.getEt0();
    assertTrue(currentEt0 == calculatedEt0);
  }
}
