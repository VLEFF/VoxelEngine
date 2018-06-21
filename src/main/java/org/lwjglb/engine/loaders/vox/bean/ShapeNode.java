package org.lwjglb.engine.loaders.vox.bean;

import java.util.List;
import java.util.Map;

public class ShapeNode {
  private Long nodeId;
  private Map<String,String> nodeAttrib;
  private List<ShapeNodeModel> shapeNodeModels;

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

  public List<ShapeNodeModel> getShapeNodeModels() {
    return shapeNodeModels;
  }

  public void setShapeNodeModels(List<ShapeNodeModel> shapeNodeModels) {
    this.shapeNodeModels = shapeNodeModels;
  }
}
