package de.hgu.gsehen.model;

import java.util.function.Consumer;

public interface DrawableParent {

  public void forAllChildDrawables(Consumer<Drawable> handler);
}
