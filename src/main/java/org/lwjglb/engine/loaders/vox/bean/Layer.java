package org.lwjglb.engine.loaders.vox.bean;

import java.util.List;
import java.util.Map;

public class Layer {

  private Long nodeId;
  private Map<String,String> nodeAttrib;
  private Long unknown;

  public Long getNodeId() {
    return nodeId;
  }

  public void setNodeId(Long nodeId) {
    this.nodeId = nodeId;
  }

  public Map<String, String> getNodeAttrib() {
    return nodeAttrib;
  }

  public void setNodeAttrib(Map<String, String> nodeAttrib) {
    this.nodeAttrib = nodeAttrib;
  }

  public Long getUnknown() {
    return unknown;
  }

  public void setUnknown(Long unknown) {
    this.unknown = unknown;
  }
}
