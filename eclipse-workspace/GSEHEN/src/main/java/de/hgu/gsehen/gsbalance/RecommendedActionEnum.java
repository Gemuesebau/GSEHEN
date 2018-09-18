package de.hgu.gsehen.gsbalance;

public enum RecommendedActionEnum {
  EXCESS("gsbalance.recommended.action.EXCESS"),
  PAUSE("gsbalance.recommended.action.PAUSE"),
  SOON("gsbalance.recommended.action.SOON"),
  NOW("gsbalance.recommended.action.NOW"),
  IRRIGATION("gsbalance.recommended.action.IRRIGATION"),
  NO_DATA("gsbalance.recommended.action.NO_DATA");

  private String messagePropertyKey;

  RecommendedActionEnum(String messagePropertyKey) {
    this.messagePropertyKey = messagePropertyKey;
  }

  public String getMessagePropertyKey() {
    return messagePropertyKey;
  }
}
