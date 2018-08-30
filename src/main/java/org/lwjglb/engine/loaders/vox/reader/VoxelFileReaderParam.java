package org.lwjglb.engine.loaders.vox.reader;

import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.loaders.vox.bean.Vox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class VoxelFileReaderParam {
  private Collection<Float> positions = new ArrayDeque<>();
  private Collection<Float> surroundings = new ArrayDeque<>();
  private Collection<Float> surroundingsDiag = new ArrayDeque<>();
  private Collection<Float> textCoords = new ArrayDeque<>();
  private Collection<Float> normals = new ArrayDeque<>();
  private Collection<Integer> indices = new ArrayDeque<>();
  private Material texture;

  public void resetLists(){
    positions = new ArrayDeque<>();
    surroundings = new ArrayDeque<>();
    surroundingsDiag = new ArrayDeque<>();
    textCoords = new ArrayDeque<>();
    normals = new ArrayDeque<>();
    indices = new ArrayDeque<>();
  }

  public Collection<Float> getPositions() {
    return positions;
  }

  public void setPositions(Collection<Float> positions) {
    this.positions = positions;
  }

  public Collection<Float> getSurroundings() {
    return surroundings;
  }

  public void setSurroundings(Collection<Float> surroundings) {
    this.surroundings = surroundings;
  }

  public Collection<Float> getSurroundingsDiag() {
    return surroundingsDiag;
  }

  public void setSurroundingsDiag(Collection<Float> surroundingsDiag) {
    this.surroundingsDiag = surroundingsDiag;
  }

  public Collection<Float> getTextCoords() {
    return textCoords;
  }

  public void setTextCoords(Collection<Float> textCoords) {
    this.textCoords = textCoords;
  }

  public Collection<Float> getNormals() {
    return normals;
  }

  public void setNormals(Collection<Float> normals) {
    this.normals = normals;
  }

  public Collection<Integer> getIndices() {
    return indices;
  }

  public void setIndices(Collection<Integer> indices) {
    this.indices = indices;
  }

  public Material getTexture() {
    return texture;
  }

  public void setTexture(Material texture) {
    this.texture = texture;
  }
}
