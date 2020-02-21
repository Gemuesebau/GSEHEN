package de.hgu.gsehen.logging;

import de.hgu.gsehen.gui.view.LogDataController;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogDataHandler extends Handler {
  private LogDataController logDataController;

  public LogDataHandler(LogDataController logDataController) {
    this.logDataController = logDataController;
  }

  @Override
  public void close() throws SecurityException {
  }

  @Override
  public void flush() {
  }

  @Override
  public void publish(LogRecord logRecord) {
    logDataController.onLogRecordPublish(logRecord);
  }
}
