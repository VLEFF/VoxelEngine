package org.lwjglb.engine.loaders.vox.reader;

import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.loaders.vox.bean.Vox;

import java.util.ArrayList;
import java.util.List;

public class VoxelFileReaderParam {
  private List<Float> positions = new ArrayList<>();
  private List<Float> surroundings = new ArrayList<>();
  private List<Float> surroundingsDiag = new ArrayList<>();
  private List<Float> textCoords = new ArrayList<>();
  private List<Float> normals = new ArrayList<>();
  private List<Integer> indices = new ArrayList<>();
  private Material texture;

  public void resetLists(){
    positions = new ArrayList<>();
    surroundings = new ArrayList<>();
    surroundingsDiag = new ArrayList<>();
    textCoords = new ArrayList<>();
    normals = new ArrayList<>();
    indices = new ArrayList<>();
  }

  public List<Float> getPositions() {
    return positions;
  }

  public void setPositions(List<Float> positions) {
    this.positions = positions;
  }

  public List<Float> getSurroundings() {
    return surroundings;
  }

  public void setSurroundings(List<Float> surroundings) {
    this.surroundings = surroundings;
  }

  public List<Float> getSurroundingsDiag() {
    return surroundingsDiag;
  }

  public void setSurroundingsDiag(List<Float> surroundingsDiag) {
    this.surroundingsDiag = surroundingsDiag;
  }

  public List<Float> getTextCoords() {
    return textCoords;
  }

  public void setTextCoords(List<Float> textCoords) {
    this.textCoords = textCoords;
  }

  public List<Float> getNormals() {
    return normals;
  }

  public void setNormals(List<Float> normals) {
    this.normals = normals;
  }

  public List<Integer> getIndices() {
    return indices;
  }

  public void setIndices(List<Integer> indices) {
    this.indices = indices;
  }

  public Material getTexture() {
    return texture;
  }

  public void setTexture(Material texture) {
    this.texture = texture;
  }
}
