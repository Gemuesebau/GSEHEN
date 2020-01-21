package de.hgu.gsehen.util;

public class Pair<T> {
  private T left;
  private T right;
  
  public Pair(T left, T right) {
    this.left = left;
    this.right = right;
  }

  public T getLeft() {
    return left;
  }

  public void setLeft(T left) {
    this.left = left;
  }

  public T getRight() {
    return right;
  }

  public void setRight(T right) {
    this.right = right;
  }

  @Override
  public String toString() {
    return "[" + String.valueOf(left) + ", " + String.valueOf(right) + "]";
  }
}
