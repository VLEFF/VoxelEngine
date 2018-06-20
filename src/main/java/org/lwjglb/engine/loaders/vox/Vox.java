package org.lwjglb.engine.loaders.vox;

import java.util.ArrayList;
import java.util.List;

public class Vox {

  private final List<VoxModel> voxModels = new ArrayList<>();
  private final List<Layer> layers = new ArrayList<>();
  private final List<Matl> matls = new ArrayList<>();
  private final List<RObj> rOBJs = new ArrayList<>();

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

  public List<Layer> getLayers() {
    return layers;
  }

  public List<Matl> getMatls() {
    return matls;
  }

  public List<RObj> getrOBJs() {
    return rOBJs;
  }
}
