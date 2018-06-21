package org.lwjglb.engine.loaders.vox.bean;

import java.util.Map;

public class ShapeNodeModel {
  private Long modelId;
  private Map<String,String> modelAttrib;

  public Long getModelId() {
    return modelId;
  }

  public void setModelId(Long modelId) {
    this.modelId = modelId;
  }

  public Map<String, String> getModelAttrib() {
    return modelAttrib;
  }

  public void setModelAttrib(Map<String, String> modelAttrib) {
    this.modelAttrib = modelAttrib;
  }
}
