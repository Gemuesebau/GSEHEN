package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class DeveloperController {

  @FXML
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  private MenuItem reloadMapViewHTMLMenuItem;

  @FXML
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  private MenuItem reloadFarmViewHTMLMenuItem;

  @FXML
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  public void reloadMapViewHTML(ActionEvent e) {
    Gsehen.getMaps().reload();
  }

  @FXML
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  public void reloadFarmViewHTML(ActionEvent e) {
    Gsehen.getFarms().reload();
  }
}
