package de.hgu.gsehen.util;

/**
 * An exception where the message is actually a message bundle key.
 *
 * @author AT
 */
@SuppressWarnings("serial")
public class GsehenLocalizedException extends RuntimeException {
  private Object[] parameters;

  public GsehenLocalizedException(String messageKey, Object... parameters) {
    super(messageKey);
    this.parameters = parameters;
  }

  public Object[] getParameters() {
    return parameters;
  }
}
