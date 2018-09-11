package de.hgu.gsehen.util;

import de.hgu.gsehen.gsbalance.RecommendedAction;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageUtil {
  @SuppressWarnings("checkstyle:javadocmethod")
  public static String renderMessage(Locale locale, ResourceBundle bundle,
      final RecommendedAction recommendedAction) {
    DecimalFormat numberFormat = (DecimalFormat)NumberFormat.getNumberInstance(locale);
    numberFormat.applyPattern("#,##0.00");
    return MessageFormat.format(
        bundle.getString(recommendedAction.getRecommendation().getMessagePropertyKey()),
        CollectionUtil.fillObjectArray(3,
            index -> formatDouble(recommendedAction.getParameterValue(index), numberFormat)));
  }

  private static Object formatDouble(Object value, NumberFormat numberFormat) {
    if (value instanceof Double) {
      return numberFormat.format((Double)value);
    } else {
      return value;
    }
  }
}
