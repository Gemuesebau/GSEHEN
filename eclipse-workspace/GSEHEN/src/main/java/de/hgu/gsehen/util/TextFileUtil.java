package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.MessageUtil.logException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class TextFileUtil {
  private static final Logger LOGGER = Logger.getLogger(TextFileUtil.class.getName());

  public static InputStreamReader getReaderForUtf8(String absoluteFileName) throws IOException {
    return new InputStreamReader(new FileInputStream(absoluteFileName), "utf-8");
  }

  /**
   * Reads the contents of a given utf-8-encoded resource as one String.
   *
   * @param absoluteFileName the name of the resource to read
   * @return a String containing the given resource's contents
   * @throws IOException if the resource can't be read (as utf-8)
   */
  public static String getUtf8FileAsOneString(String absoluteFileName) throws IOException {
    try (BufferedReader buffer = new BufferedReader(getReaderForUtf8(absoluteFileName))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public static ResourceBundle getFileAsResourceBundle(String absoluteFolderName, String bundleName,
      Locale locale) throws MalformedURLException {
    return ResourceBundle.getBundle(bundleName, locale,
        new URLClassLoader(new URL[] { new File(absoluteFolderName).toURI().toURL() }));
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public static ScriptEngine evaluateJavaScriptFile(String absoluteFileName) {
    final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    engine.put("LOGGER", LOGGER);
    try {
      engine.eval(
          "function loadLocalJavaScript(relativePathAndName) {"
          + " function() {"
          + "     eval.apply(this, arguments);"
          + " }(Packages." + TextFileUtil.class.getName() + ".getUtf8FileAsOneString("
          + "     \""
          + new File(absoluteFileName).getParent() + File.separatorChar
          + "\" + relativePathAndName));"
          + "}"
          + "function loadLocalResourceBundle(bundleName, locale) {"
          + " function() {"
          + "     eval.apply(this, arguments);"
          + " }(Packages." + TextFileUtil.class.getName() + ".getFileAsResourceBundle("
          + "     \""
          + new File(absoluteFileName).getParent()
          + "\", bundleName, locale));"
          + "}");
      engine.eval(getReaderForUtf8(absoluteFileName));
    } catch (Exception e) {
      logException(LOGGER, Level.SEVERE, e, "util.textresource.evaluatejs.error",
          absoluteFileName);
    }
    return engine;
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public static ScriptEngine evaluateJavaScriptFile(String fileName,
      String... absoluteFolderNames) {
    for (String absoluteFolderName : absoluteFolderNames) {
      String complete = absoluteFolderName + File.separatorChar + fileName;
      if (new File(complete).exists()) {
        return evaluateJavaScriptFile(complete);
      }
    }
    return null;
  }
}
