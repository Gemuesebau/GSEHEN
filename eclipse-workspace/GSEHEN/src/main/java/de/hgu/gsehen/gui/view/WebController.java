package de.hgu.gsehen.gui.view;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hgu.gsehen.Gsehen;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

@SuppressWarnings({"checkstyle:commentsindentation"})
public abstract class WebController {
  private static final Pattern INCLUDE_DIRECTIVE = Pattern.compile("#include\\(\"([^\"]+)\"\\)");

  protected Gsehen application;
  protected WebEngine engine;
  protected String loadWorkerSucceededScript;

  private boolean loaded = false;

  public boolean isLoaded() {
    return loaded;
  }

  protected String getCompanionFileContents(String dotExtension) {
    return getFileContents(getClass().getSimpleName().toLowerCase() + dotExtension);
  }

  @SuppressWarnings("checkstyle:rightcurly")
  protected String getFileContents(String fileName) {
    int totalRead = 0;
    try {
      InputStream inputStream = getClass().getResourceAsStream(fileName);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int read;
      while ((read = inputStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, read);
        totalRead += read;
      }
      return processIncludes(new String(outputStream.toByteArray(), "utf-8"));
    }
    catch (Exception e) {
      throw new RuntimeException(fileName + " couldn't be loaded", e);
    }
    finally {
      getLogger().info("finished processing " + fileName + " (" + totalRead + " bytes read)");
    }
  }

  private String processIncludes(String source) {
    Matcher matcher = INCLUDE_DIRECTIVE.matcher(source);
    StringBuilder builder = new StringBuilder();
    int lastEnd = 0;
    while (matcher.find()) {
      builder.append(source.substring(lastEnd, matcher.start()));
      builder.append(getFileContents(matcher.group(1)));
      lastEnd = matcher.end();
    }
    builder.append(source.substring(lastEnd));
    return builder.toString();
  }

  /**
   * Reload HTML, and re-initialize JavaScript.
   */
  public void reload() {
    loadWorkerSucceededScript = getCompanionFileContents(".js");
    engine.loadContent(getCompanionFileContents(".html"));
  }

  protected abstract Logger getLogger();

  protected void alert(String message) {
    getLogger().info(message);
  }

  @SuppressWarnings({"checkstyle:javadocmethod"})
  public WebController(Gsehen application, WebView webView) {
    this.application = application;
    engine = webView.getEngine();
    engine.setOnAlert(event -> alert(event.getData()));
    engine.executeScript("console.log = function(message){alert(message)};");
    engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
      if (newState == State.SUCCEEDED) {
        JSObject win = (JSObject)engine.executeScript("window");
        win.setMember("webController", this);
        engine.executeScript(loadWorkerSucceededScript);
        loaded = true;
      }
    });
    getLogger().info("WebEngine initialized");
  }
}
