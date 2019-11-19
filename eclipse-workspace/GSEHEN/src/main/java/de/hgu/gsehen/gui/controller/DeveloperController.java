package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class DeveloperController {
  private static DeveloperController instance = null;

  {
    instance = this;
  }

  public static DeveloperController getInstance() {
    return instance;
  }

  @FXML
  private MenuItem developerMenu;

  public MenuItem getDeveloperMenu() {
    return developerMenu;
  }

  @FXML
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  private MenuItem reloadMapViewHTMLMenuItem;

  @FXML
  private MenuItem jsPromptForMapViewMenuItem;

  @FXML
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  public void reloadMapViewHTML(ActionEvent e) {
    Gsehen.getMaps().reload();
  }

  @FXML
  public void jsPromptForMapView() {
    Gsehen.jsPrompt(Gsehen.getMaps());
  }
}
