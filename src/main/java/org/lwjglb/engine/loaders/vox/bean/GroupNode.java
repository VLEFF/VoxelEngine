package org.lwjglb.engine.loaders.vox.bean;

import java.util.List;
import java.util.Map;

public class GroupNode {

  private List<TransformNode> transformNodes;

  private Long nodeId;
  private Map<String,String> nodeAttrib;
  private List<Long> childNodes;

  public List<TransformNode> getTransformNodes() {
    return transformNodes;
  }

  public void setTransformNodes(List<TransformNode> transformNodes) {
    this.transformNodes = transformNodes;
  }

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

  public List<Long> getChildNodes() {
    return childNodes;
  }

  public void setChildNodes(List<Long> childNodes) {
    this.childNodes = childNodes;
  }
}
