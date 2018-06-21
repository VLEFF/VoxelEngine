package org.lwjglb.engine.loaders.vox.bean;

import java.util.List;
import java.util.Map;

public class TransformNode {
  private GroupNode groupNode;
  private ShapeNode shapeNode;

  private Long nodeId;
  private Map<String,String> nodeAttrib;
  private Long childNodeId;
  private Long reservedId;
  private Long layerId;
  private List<Map<String,String>> transformations;

  public GroupNode getGroupNode() {
    return groupNode;
  }

  public void setGroupNode(GroupNode groupNode) {
    this.groupNode = groupNode;
  }

  public ShapeNode getShapeNode() {
    return shapeNode;
  }

  public void setShapeNode(ShapeNode shapeNode) {
    this.shapeNode = shapeNode;
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

  public Long getChildNodeId() {
    return childNodeId;
  }

  public void setChildNodeId(Long childNodeId) {
    this.childNodeId = childNodeId;
  }

  public Long getReservedId() {
    return reservedId;
  }

  public void setReservedId(Long reservedId) {
    this.reservedId = reservedId;
  }

  public Long getLayerId() {
    return layerId;
  }

  public void setLayerId(Long layerId) {
    this.layerId = layerId;
  }

  public List<Map<String, String>> getTransformations() {
    return transformations;
  }

  public void setTransformations(List<Map<String, String>> transformations) {
    this.transformations = transformations;
  }
}
