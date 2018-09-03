package de.hgu.gsehen.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Function;

public class MessageUtil {

  public static String renderMessage(ResourceBundle bundle, String messageKey, int parameterCount,
      Function<Integer, Object> parameterFunc) {
    return MessageFormat.format(bundle.getString(messageKey),
        CollectionUtil.fillObjectArray(parameterCount, parameterFunc));
  }
}
