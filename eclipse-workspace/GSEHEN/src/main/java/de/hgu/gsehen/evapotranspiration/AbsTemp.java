package de.hgu.gsehen.evapotranspiration;

public class AbsTemp {
  /**
   * This class combines all absolute Temperatures.
   *
   * @param absTempMean absolute Temperature mean
   * @param absTempMax absolute temperature maximum
   * @param absTempMin absolute temperature minimum
   */
  public AbsTemp(double absTempMean, double absTempMax, double absTempMin) {
    super();
    this.absTempMean = absTempMean;
    this.absTempMax = absTempMax;
    this.absTempMin = absTempMin;
  }

  private double absTempMean;
  private double absTempMax;
  private double absTempMin;

  public double getAbsTempMean() {
    return absTempMean;
  }

  public void setAbsTempMean(double absTempMean) {
    this.absTempMean = absTempMean;
  }

  public double getAbsTempMax() {
    return absTempMax;
  }

  public void setAbsTempMax(double absTempMax) {
    this.absTempMax = absTempMax;
  }

  public double getAbsTempMin() {
    return absTempMin;
  }

  public void setAbsTempMin(double absTempMin) {
    this.absTempMin = absTempMin;
  }


}
