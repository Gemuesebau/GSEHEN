package de.hgu.gsehen.gsbalance;

public enum RecommendedActionEnum {
  EXCESS("gsbalance.recommended.action.EXCESS"),
  PAUSE("gsbalance.recommended.action.PAUSE"),
  SOON("gsbalance.recommended.action.SOON"),
  IRRIGATION("gsbalance.recommended.action.IRRIGATION");

  private String messagePropertyKey;

  RecommendedActionEnum(String messagePropertyKey) {
    this.messagePropertyKey = messagePropertyKey;
  }

  public String getMessagePropertyKey() {
    return messagePropertyKey;
  }
}
