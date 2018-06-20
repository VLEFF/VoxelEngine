package org.lwjglb.engine.loaders.vox;

import org.joml.Matrix3f;
import org.joml.Vector3i;

public class VoxModel {

  private int width;

  private int height;

  private int depth;

  private Byte[][][] matrice;

  private int numVoxel;

  private Vector3i translation;

  private Matrix3f rotation;

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public Byte[][][] getMatrice() {
    return matrice;
  }

  public void setMatrice(Byte[][][] matrice) {
    this.matrice = matrice;
  }

  public int getNumVoxel() {
    return numVoxel;
  }

  public void setNumVoxel(int numVoxel) {
    this.numVoxel = numVoxel;
  }

  public Vector3i getTranslation() {
    return translation;
  }

  public void setTranslation(Vector3i translation) {
    this.translation = translation;
  }

  public Matrix3f getRotation() {
    return rotation;
  }

  public void setRotation(Matrix3f rotation) {
    this.rotation = rotation;
  }
}
