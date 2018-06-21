package org.lwjglb.engine.loaders.vox.reader;


import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.AABBf;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Texture;
import org.lwjglb.engine.loaders.vox.bean.GroupNode;
import org.lwjglb.engine.loaders.vox.bean.Layer;
import org.lwjglb.engine.loaders.vox.bean.Matl;
import org.lwjglb.engine.loaders.vox.bean.RObj;
import org.lwjglb.engine.loaders.vox.bean.ShapeNode;
import org.lwjglb.engine.loaders.vox.bean.ShapeNodeModel;
import org.lwjglb.engine.loaders.vox.bean.TransformNode;
import org.lwjglb.engine.loaders.vox.bean.Vox;
import org.lwjglb.engine.loaders.vox.bean.VoxModel;

public class VoxelFileReader {

  protected static class Chunk {
    String id;
    int contentSize;
    int childrenSize;
  }

  protected byte[] buf = new byte[4];

  public Vox readVox(File file) throws Exception {
    BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
    Chunk chunk = new Chunk();

    if (!"VOX ".equals(readString(input, 4))) {
      throw new Exception("Not a valid .vox file.");
    }

    if (read32(input) < 150) {
      throw new Exception("Unsupported version.");
    }

    readChunk(input, chunk);
    if (!"MAIN".equals(chunk.id)) {
      throw new Exception("Main chunk expected.");
    }

    if (input.skip(chunk.contentSize) != chunk.contentSize) {
      throw new Exception("Invalid number of bytes skipped.");
    }

    Vox vox = new Vox();
    while (true) {
      try {
        readChunk(input, chunk);
      } catch (IOException ignored) {
        break;
      }
      switch (chunk.id) {
        case "SIZE" : readVoxSize(input, vox); break;
        case "XYZI" : readVoxContent(input, vox); break;
        case "RGBA" : readVoxPalette(input, vox); break;
        case "nTRN" : readTransformNode(input, vox); break;
        case "nGRP" : readGroupNode(input, vox); break;
        case "nSHP" : readShapeNode(input, vox); break;
        case "LAYR" : readLayer(input, vox); break;
        case "MATL" : readMatl(input, vox); break;
        case "rOBJ" : readrOBJ(input, vox); break;
        default : skip(input, chunk); break;
      }
    }
    input.close();

    return vox;
  }

  private void readVoxSize(BufferedInputStream input, Vox vox) throws IOException {
    VoxModel voxModel = new VoxModel();
    voxModel.setDepth((int) read32(input));
    voxModel.setWidth((int) read32(input));
    voxModel.setHeight((int) read32(input));
    vox.getVoxModels().add(voxModel);
  }

  private void readVoxContent(BufferedInputStream input, Vox vox) throws IOException {
    VoxModel voxModel = vox.getVoxModels().get(vox.getVoxModels().size() - 1);
    voxModel.setNumVoxel((int) read32(input));
    Byte[][][] matrice = new Byte[voxModel.getWidth()][voxModel.getHeight()][voxModel.getDepth()];
    for (int v = 0; v < voxModel.getNumVoxel(); v++) {
      int z = input.read();
      int x = input.read();
      int y = input.read();
      matrice[x][y][z] = (byte) (input.read() & 0xff);
    }
    voxModel.setMatrice(matrice);
  }

  private void readVoxPalette(BufferedInputStream input, Vox vox) throws Exception {
    int[] palette = new int[256];
    for (int p = 1; p < 256; p++) {
      int rgba = (int) read32(input);
      palette[p - 1] = patchColor(rgba);
    }
    vox.setPalette(palette);
    if (input.skip(4) != 4) {
      throw new Exception("Unexpected EOF.");
    }
  }

  private void readTransformNode(BufferedInputStream input, Vox vox) throws IOException {
    TransformNode transformNode = new TransformNode();
    transformNode.setNodeId(read32(input));
    transformNode.setNodeAttrib(readAttribMap(input));
    transformNode.setChildNodeId(read32(input));
    transformNode.setReservedId(read32(input));
    transformNode.setLayerId(read32(input));
    Long nbFrame = read32(input);
    transformNode.setTransformations(new ArrayList<>(nbFrame.intValue()));
    for (int i = 0; i < nbFrame; i++) {
      transformNode.getTransformations().add(readAttribMap(input));
    }
    if(vox.getTransformNode() != null) {
      insertTransformNodeInGroupNode(transformNode, vox.getTransformNode().getGroupNode());
    } else {
      vox.setTransformNode(transformNode);
    }
  }

  private boolean insertTransformNodeInGroupNode(TransformNode transformNode, GroupNode groupNode){
    for(int i = 0 ; i < groupNode.getChildNodes().size() ; i++){
      if(groupNode.getTransformNodes().size() <= i){
        groupNode.getTransformNodes().add(transformNode);
        return true;
      }
      if(groupNode.getTransformNodes().get(i).getGroupNode() != null){
        if(insertTransformNodeInGroupNode(transformNode, groupNode.getTransformNodes().get(i).getGroupNode())) {
          return true;
        }
      }
    }
    return false;
  }

  private void readGroupNode(BufferedInputStream input, Vox vox) throws IOException {
    GroupNode groupNode = new GroupNode();
    groupNode.setNodeId(read32(input));
    groupNode.setNodeAttrib(readAttribMap(input));
    groupNode.setChildNodes(read32List(input));
    groupNode.setTransformNodes(new ArrayList<>(groupNode.getChildNodes().size()));
    insertGroupNodeInTransformNode(groupNode, vox.getTransformNode());
  }

  private boolean insertGroupNodeInTransformNode(GroupNode groupNode, TransformNode transformNode){
    if(transformNode.getGroupNode() == null){
      if(transformNode.getShapeNode() == null) {
        transformNode.setGroupNode(groupNode);
        return true;
      }
      return false;
    }
    for(int i = 0 ; i < transformNode.getGroupNode().getTransformNodes().size() ; i++){
      if(insertGroupNodeInTransformNode(groupNode, transformNode.getGroupNode().getTransformNodes().get(i))) {
        return true;
      }
    }
    return false;
  }

  private void readShapeNode(BufferedInputStream input, Vox vox) throws IOException {
    ShapeNode shapeNode = new ShapeNode();
    shapeNode.setNodeId(read32(input));
    shapeNode.setNodeAttrib(readAttribMap(input));
    Long nbModel = read32(input);
    shapeNode.setShapeNodeModels(new ArrayList<>(nbModel.intValue()));
    for (int i = 0; i < nbModel; i++) {
      ShapeNodeModel shapeNodeModel = new ShapeNodeModel();
      shapeNodeModel.setModelId(read32(input));
      shapeNodeModel.setModelAttrib(readAttribMap(input));
      shapeNode.getShapeNodeModels().add(shapeNodeModel);
    }
    insertShapeNodeInTransformNode(shapeNode, vox.getTransformNode(), vox);
  }

  private boolean insertShapeNodeInTransformNode(ShapeNode shapeNode, TransformNode transformNode, Vox vox){
    if(transformNode.getShapeNode() != null){
      return false;
    }
    if(transformNode.getGroupNode() == null){
      transformNode.setShapeNode(shapeNode);
      return true;
    }
    for(int i = 0 ; i < transformNode.getGroupNode().getTransformNodes().size() ; i++){
      if(insertShapeNodeInTransformNode(shapeNode, transformNode.getGroupNode().getTransformNodes().get(i), vox)){
        return true;
      }
    }
    return false;
  }

  private void readLayer(BufferedInputStream input, Vox vox) throws IOException {
    Layer layer = new Layer();
    layer.setNodeId(read32(input));
    layer.setNodeAttrib(readAttribMap(input));
    layer.setUnknown(read32(input));
    layer.setUnknown(read32(input));
    vox.getLayers().add(layer);
  }

  private void readMatl(BufferedInputStream input, Vox vox) throws IOException {
    Matl matl = new Matl();
    matl.setMaterialId(read32(input));
    matl.setNodeAttrib(readAttribMap(input));
    vox.getMatls().add(matl);
  }

  private void readrOBJ(BufferedInputStream input, Vox vox) throws IOException {
    RObj rObj = new RObj();
    rObj.setNodeAttrib(readAttribMap(input));
    vox.getrOBJs().add(rObj);
  }

  private void skip(BufferedInputStream input, Chunk chunk) throws Exception {
    // unexpected chunk, ignore & skip
    int bytesToSkip = chunk.contentSize + chunk.childrenSize;
    if (input.skip(bytesToSkip) != bytesToSkip) {
      throw new Exception("Invalid number of bytes skipped.");
    }
  }

  protected void readChunk(BufferedInputStream input, Chunk chunk) throws IOException {
    chunk.id = readString(input, 4);
    chunk.contentSize = (int) read32(input);
    chunk.childrenSize = (int) read32(input);
  }

  protected long read32(BufferedInputStream input) throws IOException {
    if (input.read(buf) < 4) {
      throw new IOException();
    }
    return (buf[0] & 0xff) | ((buf[1] & 0xff) << 8) | ((buf[2] & 0xff) << 16) | ((buf[3] & 0xff) << 24);
  }

  protected String readString(BufferedInputStream input) throws IOException {
    Long length = read32(input);
    return readString(input, length.intValue());
  }

  protected String readString(BufferedInputStream input, int length) throws IOException {
    byte[] stringBuffer = new byte[length];
    if (input.read(stringBuffer) < length) {
      throw new IOException();
    }
    return new String(stringBuffer, StandardCharsets.UTF_8);
  }

  protected Map<String,String> readAttribMap(BufferedInputStream input) throws IOException{
    Long nbNodeAttrib = read32(input);
    Map<String,String> attribMap = new HashMap<>(nbNodeAttrib.intValue());
    for (int i = 0; i < nbNodeAttrib; i++) {
      attribMap.put(readString(input), readString(input));
    }
    return attribMap;
  }

  protected List<Long> read32List(BufferedInputStream input) throws IOException{
    Long nbChild = read32(input);
    List<Long> list = new ArrayList<>(nbChild.intValue());
    for (int i = 0; i < nbChild; i++) {
      list.add(read32(input));
    }
    return list;
  }

  protected int patchColor(int rgba) {
    int b = (rgba & 0x00ff0000) >> 16;
    int g = (rgba & 0x0000ff00) >> 8;
    int r = rgba & 0x000000ff;
    return (0xff << 24) | (r << 16) | (g << 8) | b; //pixel
  }
	
	protected float getColorCoord(VoxModel voxModel, Vector3i voxPosition) {
		byte color = voxModel.getMatrice()[voxPosition.x][voxPosition.y][voxPosition.z];
		return (1.0f + ((1.0f/256.0f) * color) - (1.0f/512.0f)) % 1;
	}
}
