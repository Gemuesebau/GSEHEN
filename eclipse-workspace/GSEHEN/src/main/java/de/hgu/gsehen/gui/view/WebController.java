package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

@SuppressWarnings({"checkstyle:commentsindentation"})
public abstract class WebController {
  private static final Pattern INCLUDE_DIRECTIVE = Pattern.compile("#include\\(\"([^\"]+)\"\\)");

  protected Gsehen application;
  protected WebEngine engine;

  private String loadWorkerSucceededScript;
  private boolean loaded = false;

  public boolean isLoaded() {
    return loaded;
  }

  private String getCompanionFileContents(String dotExtension) {
    return getFileContents(getClass().getSimpleName().toLowerCase() + dotExtension);
  }

  @SuppressWarnings("checkstyle:rightcurly")
  private String getFileContents(String fileName) {
    try {
      String result = processIncludes(new String(Files.readAllBytes(
          Paths.get(Maps.class.getResource(fileName).toURI())
      ), "utf-8"));
      getLogger().info("finished processing " + fileName);
      return result;
    }
    catch (Exception e) {
      throw new RuntimeException(fileName + " couldn't be loaded", e);
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
   * Reload map view HTML, and re-initialize JavaScript.
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
