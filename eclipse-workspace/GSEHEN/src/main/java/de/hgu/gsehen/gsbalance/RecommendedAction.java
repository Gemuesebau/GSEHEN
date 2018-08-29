package de.hgu.gsehen.gsbalance;

public class RecommendedAction {
  String recommendation;
  Double availableWater;
  Double availableWaterPercent;
  Integer projectedDaysToIrrigation;

  /**
   * @param recommendation Text of recommended action
   * @param availableWater The remaining available soil water
   * @param availableWaterPercent Percentual remaining available soil water
   * @param projectedDaysToIrrigation A simple projection of days until an irrigation might be
   *        recommended
   */
  public RecommendedAction(String recommendation, Double availableWater,
      Double availableWaterPercent, Integer projectedDaysToIrrigation) {
    super();
    this.recommendation = recommendation;
    this.availableWater = availableWater;
    this.availableWaterPercent = availableWaterPercent;
    this.projectedDaysToIrrigation = projectedDaysToIrrigation;
  }

  public RecommendedAction() {}

  public String getRecommendation() {
    return recommendation;
  }

  public void setRecommendation(String recommendation) {
    this.recommendation = recommendation;
  }

  public Double getAvailableWater() {
    return availableWater;
  }

  public void setAvailableWater(Double availableWater) {
    this.availableWater = availableWater;
  }

  public Double getAvailableWaterPercent() {
    return availableWaterPercent;
  }

  public void setAvailableWaterPercent(Double availableWaterPercent) {
    this.availableWaterPercent = availableWaterPercent;
  }

  public Integer getProjectedDaysToIrrigation() {
    return projectedDaysToIrrigation;
  }

  public void setProjectedDaysToIrrigation(Integer projectedDaysToIrrigation) {
    this.projectedDaysToIrrigation = projectedDaysToIrrigation;
  }



}
