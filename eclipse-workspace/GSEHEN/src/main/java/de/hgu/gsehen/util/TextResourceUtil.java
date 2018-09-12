package de.hgu.gsehen.util;

import de.hgu.gsehen.Gsehen;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class TextResourceUtil {
  private static final Logger LOGGER = Logger.getLogger(TextResourceUtil.class.getName());

  public static final String JS_RESOURCE_FOLDER = "/de/hgu/gsehen/js";
  public static final String PLUGINS_FOLDER = JS_RESOURCE_FOLDER + "/plugins";

  public static InputStreamReader getReaderForUtf8(String resourceName) throws IOException {
    return new InputStreamReader(TextResourceUtil.class.getResourceAsStream(resourceName), "utf-8");
  }

  /**
   * Reads the contents of a given utf-8-encoded resource as one String.
   *
   * @param resourceName
   *          the name of the resource to read
   * @return a String containing the given resource's contents
   * @throws IOException
   *           if the resource can't be read (as utf-8)
   */
  public static String getUtf8ResourceAsOneString(String resourceName) throws IOException {
    try (BufferedReader buffer = new BufferedReader(getReaderForUtf8(resourceName))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public static ScriptEngine evaluateJsResource(String jsResourceFileName) {
    final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    engine.put("LOGGER", LOGGER);
    try {
      engine.eval("function loadGsehenJs(gsehenJsFileName) {"
          + "  eval("
          + "    Packages." + TextResourceUtil.class.getName() + ".getUtf8ResourceAsOneString("
          + "      \"/de/hgu/gsehen/js/\" + gsehenJsFileName)"
          + "  )"
          + "}");
      engine.eval(getReaderForUtf8(jsResourceFileName));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't evaluate " + jsResourceFileName, e);
    }
    return engine;
  }

  static {
    for (File f : new File(Gsehen.class.getResource(PLUGINS_FOLDER).getPath()).listFiles()) {
      System.out.println(f);
    }
  }
}
