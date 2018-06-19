package org.lwjglb.engine.loaders.vox;

import java.util.ArrayList;
import java.util.List;

public class Vox {

  private final List<VoxModel> voxModels = new ArrayList<>();

  private TransformNode transformNode;

  private int[] palette;

  public int[] getPalette() {
    return palette;
  }

  public void setPalette(int[] palette) {
    this.palette = palette;
  }

  public TransformNode getTransformNode() {
    return transformNode;
  }

  public void setTransformNode(TransformNode transformNode) {
    this.transformNode = transformNode;
  }

  public List<VoxModel> getVoxModels() {
    return voxModels;
  }
}
