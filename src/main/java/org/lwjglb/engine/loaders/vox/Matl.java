package org.lwjglb.engine.loaders.vox;

import java.util.Map;

public class Matl {

  private Long materialId;
  private Map<String,String> nodeAttrib;

  public Long getMaterialId() {
    return materialId;
  }

  public void setMaterialId(Long materialId) {
    this.materialId = materialId;
  }

  public Map<String, String> getNodeAttrib() {
    return nodeAttrib;
  }

  public void setNodeAttrib(Map<String, String> nodeAttrib) {
    this.nodeAttrib = nodeAttrib;
  }
}
