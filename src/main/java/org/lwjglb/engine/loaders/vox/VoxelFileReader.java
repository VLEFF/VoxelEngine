package org.lwjglb.engine.loaders.vox;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class VoxelFileReader {

  protected final static float[][] POSITIONS_RIGHT_FACE = {{1f, 0f, 1f}, {1f, 0f, 0f}, {1f, 1f, 0f}, {1f, 1f, 1f}};
  protected final static float[][] POSITIONS_LEFT_FACE = {{0f, 1f, 1f}, {0f, 1f, 0f}, {0f, 0f, 0f}, {0f, 0f, 1f}};
  protected final static float[][] POSITIONS_TOP_FACE = {{0f, 1f, 1f}, {1f, 1f, 1f}, {1f, 1f, 0f}, {0f, 1f, 0f}};
  protected final static float[][] POSITIONS_BOTTOM_FACE = {{0f, 0f, 0f}, {1f, 0f, 0f}, {1f, 0f, 1f}, {0f, 0f, 1f}};
  protected final static float[][] POSITIONS_FRONT_FACE = {{1f, 0f, 1f}, {1f, 1f, 1f}, {0f, 1f, 1f}, {0f, 0f, 1f}};
  protected final static float[][] POSITIONS_BACK_FACE = {{0f, 0f, 0f}, {0f, 1f, 0f}, {1f, 1f, 0f}, {1f, 0f, 0f}};

  protected static class Chunk {
    long id;
    int contentSize;
    int childrenSize;
  }

  protected byte[] buf = new byte[4];

  protected Vox readVox(File file) throws Exception {
    BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
    Chunk chunk = new Chunk();

    if (read32(input) != magicValue('V', 'O', 'X', ' ')) {
      throw new Exception("Not a valid .vox file.");
    }

    if (read32(input) < 150) {
      throw new Exception("Unsupported version.");
    }

    readChunk(input, chunk);
    if (chunk.id != magicValue('M', 'A', 'I', 'N')) {
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
      if (chunk.id == magicValue('S', 'I', 'Z', 'E')) {
        readVoxSize(input, vox);
      } else if (chunk.id == magicValue('X', 'Y', 'Z', 'I')) {
        readVoxContent(input, vox);
      } else if (chunk.id == magicValue('R', 'G', 'B', 'A')) {
        readVoxPalette(input, vox);
      } else if (chunk.id == magicValue('n', 'T', 'R', 'N')) {
        readTransformNode(input, vox);
      } else if (chunk.id == magicValue('n', 'G', 'R', 'P')) {
        readGroupNode(input, vox);
      } else if (chunk.id == magicValue('n', 'S', 'H', 'P')) {
        readShapeNode(input, vox);
      } else {
        skip(input, chunk);
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
    insertShapeNodeInTransformNode(shapeNode, vox.getTransformNode());
  }

  private boolean insertShapeNodeInTransformNode(ShapeNode shapeNode, TransformNode transformNode){
    if(transformNode.getShapeNode() != null){
      return false;
    }
    if(transformNode.getGroupNode() == null){
      transformNode.setShapeNode(shapeNode);
      return true;
    }
    for(int i = 0 ; i < transformNode.getGroupNode().getTransformNodes().size() ; i++){
      if(insertShapeNodeInTransformNode(shapeNode, transformNode.getGroupNode().getTransformNodes().get(i))){
        return true;
      }
    }
    return false;
  }

  private void skip(BufferedInputStream input, Chunk chunk) throws Exception {
    // unexpected chunk, ignore & skip
    int bytesToSkip = chunk.contentSize + chunk.childrenSize;
    if (input.skip(bytesToSkip) != bytesToSkip) {
      throw new Exception("Invalid number of bytes skipped.");
    }
  }

  protected void readChunk(BufferedInputStream input, Chunk chunk) throws IOException {
    chunk.id = read32(input);
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
    byte[] stringBuffer = new byte[length.intValue()];
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

  protected long magicValue(char c0, char c1, char c2, char c3) {
    return ((c3 << 24) & 0xff000000) | ((c2 << 16) & 0x00ff0000) | ((c1 << 8) & 0x0000ff00) | (c0 & 0x000000ff);
  }

  protected int patchColor(int rgba) {
    int b = (rgba & 0x00ff0000) >> 16;
    int g = (rgba & 0x0000ff00) >> 8;
    int r = rgba & 0x000000ff;
    return (0xff << 24) | (r << 16) | (g << 8) | b; //pixel
  }
}
