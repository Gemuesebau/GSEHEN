package de.hgu.gsehen.gsbalance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class RecommendedAction {

  @Id
  @GeneratedValue
  private long id;
  RecommendedActionEnum recommendation;
  Double availableWater;
  Double availableWaterPercent;
  Integer projectedDaysToIrrigation;
  private Double waterContentToAim;

  /**
   * Constructor for a recommended (irrigation) action.
   *
   * @param recommendation recommended action
   * @param availableWater The remaining available soil water
   * @param availableWaterPercent Percentual remaining available soil water
   * @param projectedDaysToIrrigation A simple projection of days until an irrigation might be
   *        recommended
   */
  public RecommendedAction(RecommendedActionEnum recommendation, Double availableWater,
      Double availableWaterPercent, Integer projectedDaysToIrrigation) {
    super();
    this.recommendation = recommendation;
    this.availableWater = availableWater;
    this.availableWaterPercent = availableWaterPercent;
    this.projectedDaysToIrrigation = projectedDaysToIrrigation;
  }

  public RecommendedAction() {
  }

  public RecommendedActionEnum getRecommendation() {
    return recommendation;
  }

  public void setRecommendation(RecommendedActionEnum recommendation) {
    this.recommendation = recommendation;
  }

  /**
   * Returns a value suited for java message parameter substitution.
   *
   * @param index the parameter index
   * @return the value of the appropriate property
   */
  public Object getParameterValue(int index) {
    switch (index) {
      case 0:
        return getAvailableWater();
      case 1:
        return getProjectedDaysToIrrigation();
      case 2:
        return getWaterContentToAim();
      default:
        return null;
    }
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

  public Double getWaterContentToAim() {
    return waterContentToAim;
  }

  public void setWaterContentToAim(Double waterContentToAim) {
    this.waterContentToAim = waterContentToAim;
  }
}
