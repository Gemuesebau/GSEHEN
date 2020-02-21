package de.hgu.gsehen.model;

import de.hgu.gsehen.Gsehen;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Class representing a type of soil.
 *
 * @author AT
 */
@Entity
public class Soil implements Named {

  public Soil() {
  }

  @Id
  @GeneratedValue
  private long id;
  private String name;
  private double availableWaterCapacity;
  private String description;

  /**
   * Soil, that is given in a field.
   * 
   * @param name - Name of the soil.
   * @param availableWaterCapacity - Available water capacity of the soil.
   * @param description - Description of the soil.
   */
  public Soil(String name, double availableWaterCapacity, String description) {
    super();
    this.name = name;
    this.availableWaterCapacity = availableWaterCapacity;
    this.description = description;
  }

  public Soil(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getAvailableWaterCapacity() {
    return availableWaterCapacity;
  }

  public void setAvailableWaterCapacity(double availableWaterCapacity) {
    this.availableWaterCapacity = availableWaterCapacity;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Creates the given soils.
   *
   * @return a list of soils
   */
  public List<Soil> soils() {
    final ResourceBundle mainBundle = ResourceBundle.getBundle("i18n.main",
        Gsehen.getInstance().getSelectedLocale());

    List<Soil> soils = new ArrayList<Soil>();

    Soil sand = new Soil(mainBundle.getString("fieldview.sand"), 8, "");
    soils.add(sand);

    Soil sandyLoam = new Soil(mainBundle.getString("fieldview.sandyloam"), 12, "");
    soils.add(sandyLoam);

    Soil loam = new Soil(mainBundle.getString("fieldview.loam"), 17, "");
    soils.add(loam);

    Soil clayLoam = new Soil(mainBundle.getString("fieldview.clayloam"), 18, "");
    soils.add(clayLoam);

    Soil siltyClay = new Soil(mainBundle.getString("fieldview.siltyclay"), 20, "");
    soils.add(siltyClay);

    Soil clay = new Soil(mainBundle.getString("fieldview.clay"), 23, "");
    soils.add(clay);

    return soils;
  }
}
