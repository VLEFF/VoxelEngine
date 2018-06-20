package org.lwjglb.engine.loaders.vox;


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

class VoxelFileReader {

  protected static class Chunk {
    String id;
    int contentSize;
    int childrenSize;
  }

  protected byte[] buf = new byte[4];

  protected Vox readVox(File file) throws Exception {
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
    insertShapeNodeInTransformNode(shapeNode, vox.getTransformNode(), vox, new Vector3i(), new Matrix3f());
  }

  private boolean insertShapeNodeInTransformNode(ShapeNode shapeNode, TransformNode transformNode, Vox vox, Vector3i translation, Matrix3f rotation){
    translation = new Vector3i(translation).add(getTranslationVector(transformNode));
    rotation = new Matrix3f(rotation).add(getRotationMatrix(transformNode));
    if(transformNode.getShapeNode() != null){
      return false;
    }
    if(transformNode.getGroupNode() == null){
      applyTransformation(vox, shapeNode, translation, rotation);
      transformNode.setShapeNode(shapeNode);
      return true;
    }
    for(int i = 0 ; i < transformNode.getGroupNode().getTransformNodes().size() ; i++){
      if(insertShapeNodeInTransformNode(shapeNode, transformNode.getGroupNode().getTransformNodes().get(i), vox, translation, rotation)){
        return true;
      }
    }
    return false;
  }

  private Vector3i getTranslationVector(TransformNode transformNode) {
    String[] translationTab = transformNode.getTransformations().get(0).getOrDefault("_t", "0 0 0").split(" ");
    try {
      return new Vector3i(Integer.parseInt(translationTab[0]), Integer.parseInt(translationTab[1]), Integer.parseInt(translationTab[2]));
    } catch (NumberFormatException e) {
      return new Vector3i();
    }
  }

  private Matrix3f getRotationMatrix(TransformNode transformNode) {
    String rotation = transformNode.getTransformations().get(0).get("_r");
    Matrix3f matrix = new Matrix3f();
    if(rotation != null) {
      byte b = rotation.getBytes(StandardCharsets.UTF_8)[0];
      int firstLineIndex = b & 0b00000011;
      int secondLineIndex = (b >> 2) & 0b00000011;
      int firstLineSign = (b >> 4) & 0b00000001;
      int secondLineSign = (b >> 5) & 0b00000001;
      int thirdLineSign = (b >> 6) & 0b00000001;

      matrix.setRow(0, firstLineIndex == 0 ? 1 : 0, firstLineIndex == 1 ? 1 : 0, firstLineIndex == 2 ? 1 : 0);
      matrix.setRow(0, secondLineIndex == 0 ? 1 : 0, secondLineIndex == 1 ? 1 : 0, secondLineIndex == 2 ? 1 : 0);
      matrix.setRow(0, 3 - firstLineIndex - secondLineIndex == 0 ? 1 : 0, 3 - firstLineIndex - secondLineIndex == 1 ? 1 : 0, 3 - firstLineIndex - secondLineIndex == 2 ? 1 : 0);
      matrix.set(firstLineIndex, 0, matrix.get(firstLineIndex, 0) * (firstLineSign == 1 ? -1 : 1));
      matrix.set(secondLineIndex, 1, matrix.get(secondLineIndex, 1) * (secondLineSign == 1 ? -1 : 1));
      matrix.set(3 - firstLineIndex - secondLineIndex, 2, matrix.get(3 - firstLineIndex - secondLineIndex, 2) * (thirdLineSign == 1 ? -1 : 1));
    }
    return matrix;
  }

  private void applyTransformation(Vox vox, ShapeNode shapeNode, Vector3i translation, Matrix3f rotation) {
    VoxModel voxModel = vox.getVoxModels().get(shapeNode.getShapeNodeModels().get(0).getModelId().intValue());
    voxModel.setTranslation(translation);
    voxModel.setRotation(rotation);
  }

  private void skip(BufferedInputStream input, Chunk chunk) throws Exception {
    // unexpected chunk, ignore & skip
    int bytesToSkip = chunk.contentSize + chunk.childrenSize;
    if (input.skip(bytesToSkip) != bytesToSkip) {
      throw new Exception("Invalid number of bytes skipped.");
    }
  }
  
	public static ByteBuffer convertImageData(BufferedImage bi) {
	    try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
	        ImageIO.write(bi, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			return Utils.inputStreamToByteBuffer(is, 1024);
	    } catch (IOException ex) {
	        //TODO
	    }
	    return null;
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

  protected long magicValue(char c0, char c1, char c2, char c3) {
    return ((c3 << 24) & 0xff000000) | ((c2 << 16) & 0x00ff0000) | ((c1 << 8) & 0x0000ff00) | (c0 & 0x000000ff);
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
	
	protected void addTextCoord(List<Float> textCoords, float colorCoord) {
		for(int i = 0 ; i < 4 ; i++) {
			textCoords.add(colorCoord);
			textCoords.add(0.5f);
		}
	}
	
	protected void addIndices(List<Integer> indices) {
		indices.add(0 + (indices.size() / 6) * 4);
		indices.add(1 + (indices.size() / 6) * 4);
		indices.add(2 + (indices.size() / 6) * 4);
		indices.add(0 + (indices.size() / 6) * 4);
		indices.add(2 + (indices.size() / 6) * 4);
		indices.add(3 + (indices.size() / 6) * 4);
	}
	
	protected void addNormals(List<Float> normals, Vector3f normal) {
		for(int i = 0 ; i < 4 ; i++) {
			normals.add(normal.x);
			normals.add(normal.y);
			normals.add(normal.z);
		}
	}
	
	protected void addSurroundings(List<Float> surroundings, VoxModel voxModel, Vector3i voxPosition, Vector3f normal) {
		boolean inBoundary = isInBoundary(voxModel, voxPosition, normal);
        int x = voxPosition.x;
        int y = voxPosition.y;
        int z = voxPosition.z;
		for(int i = 0 ; i < 4 ; i++) {
			if(normal.x != 0) {
				surroundings.add(inBoundary && y > 0 && voxModel.getMatrice()[x + (int) normal.x][y - 1][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z > 0 && voxModel.getMatrice()[x + (int) normal.x][y][z - 1] != null ? 1f : 0f);
				surroundings.add(inBoundary && y < voxModel.getHeight() - 1 && voxModel.getMatrice()[x + (int) normal.x][y + 1][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z < voxModel.getDepth() - 1 && voxModel.getMatrice()[x + (int) normal.x][y][z + 1] != null ? 1f : 0f);
			} else if(normal.y != 0) {
				surroundings.add(inBoundary && x > 0 && voxModel.getMatrice()[x - 1][y + (int) normal.y][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z > 0 && voxModel.getMatrice()[x][y + (int) normal.y][z - 1] != null ? 1f : 0f);
				surroundings.add(inBoundary && x < voxModel.getWidth() - 1 && voxModel.getMatrice()[x + 1][y + (int) normal.y][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z < voxModel.getDepth() - 1 && voxModel.getMatrice()[x][y + (int) normal.y][z + 1] != null ? 1f : 0f);
			} else if(normal.z != 0) {
				surroundings.add(inBoundary && x > 0 && voxModel.getMatrice()[x - 1][y][z + (int) normal.z] != null ? 1f : 0f);
				surroundings.add(inBoundary && y > 0 && voxModel.getMatrice()[x][y - 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundings.add(inBoundary && x < voxModel.getWidth() - 1 && voxModel.getMatrice()[x + 1][y][z + (int) normal.z] != null ? 1f : 0f);
				surroundings.add(inBoundary && y < voxModel.getHeight() - 1 && voxModel.getMatrice()[x][y + 1][z + (int) normal.z] != null ? 1f : 0f);
			}
		}
	}
	
	protected void addSurroundingsDiag(List<Float> surroundingsDiag, VoxModel voxModel, Vector3i voxPosition, Vector3f normal) {
		boolean inBoundary = isInBoundary(voxModel, voxPosition, normal);
		int x = voxPosition.x;
        int y = voxPosition.y;
        int z = voxPosition.z;
		for(int i = 0 ; i < 4 ; i++) {
			if(normal.x != 0) {
				surroundingsDiag.add(inBoundary && y > 0 && z > 0 && voxModel.getMatrice()[x + (int) normal.x][y - 1][z - 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && y > 0 && z < voxModel.getDepth() - 1 && voxModel.getMatrice()[x + (int) normal.x][y - 1][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && y < voxModel.getHeight() - 1 && z < voxModel.getDepth() - 1 && voxModel.getMatrice()[x + (int) normal.x][y + 1][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && y < voxModel.getHeight() - 1 && z > 0 && voxModel.getMatrice()[x + (int) normal.x][y + 1][z - 1] != null ? 1f : 0f);
			} else if(normal.y != 0) {
				surroundingsDiag.add(inBoundary && x > 0 && z > 0 && voxModel.getMatrice()[x - 1][y + (int) normal.y][z - 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x > 0 && z < voxModel.getDepth() - 1 && voxModel.getMatrice()[x - 1][y + (int) normal.y][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < voxModel.getWidth() - 1 && z < voxModel.getDepth() - 1 && voxModel.getMatrice()[x + 1][y + (int) normal.y][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < voxModel.getWidth() - 1 && z > 0 && voxModel.getMatrice()[x + 1][y + (int) normal.y][z - 1] != null ? 1f : 0f);
			} else if(normal.z != 0) {
				surroundingsDiag.add(inBoundary && x > 0 && y > 0 && voxModel.getMatrice()[x - 1][y - 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x > 0 && y < voxModel.getHeight() - 1 && voxModel.getMatrice()[x - 1][y + 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < voxModel.getWidth() - 1 && y < voxModel.getHeight() - 1 && voxModel.getMatrice()[x + 1][y + 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < voxModel.getWidth() - 1 && y > 0 && voxModel.getMatrice()[x + 1][y - 1][z + (int) normal.z] != null ? 1f : 0f);
			}
		}
	}
	
	private boolean isInBoundary(VoxModel voxModel, Vector3i voxPosition, Vector3f normal) {
        int x = voxPosition.x;
        int y = voxPosition.y;
        int z = voxPosition.z;
		return x + normal.x < voxModel.getWidth() 
				&& x + normal.x >= 0 
				&& y + normal.y < voxModel.getHeight()
				&& y + normal.y >= 0 
				&& z + normal.z < voxModel.getDepth()
				&& z + normal.z >= 0;
	}
	
	protected void addPositions(List<Float> positions, Vector3i voxPosition, float[][] positionsFace){
		for(int i = 0 ; i < 4 ; i++) {
			positions.add(positionsFace[i][0] + voxPosition.x);
			positions.add(positionsFace[i][1] + voxPosition.y);
			positions.add(positionsFace[i][2] + voxPosition.z);
		}
	}
	
	protected Mesh createMesh(List<Float> positions, List<Float> surroundings, List<Float> surroundingsDiag, List<Float> textCoords, List<Float> normals, List<Integer> indices, AABBf boundaryBox){
		float[] positionsArray = ArrayUtils.toPrimitive(positions.toArray(new Float[positions.size()]));
		float[] surroundingsArray = ArrayUtils.toPrimitive(surroundings.toArray(new Float[surroundings.size()]));
		float[] surroundingsDiagArray = ArrayUtils.toPrimitive(surroundingsDiag.toArray(new Float[surroundingsDiag.size()]));
		float[] textCoordsArray = ArrayUtils.toPrimitive(textCoords.toArray(new Float[textCoords.size()]));
		float[] normalsArray = ArrayUtils.toPrimitive(normals.toArray(new Float[normals.size()]));
		int[] indicesArray = ArrayUtils.toPrimitive(indices.toArray(new Integer[indices.size()]));
		return new Mesh(positionsArray, surroundingsArray, surroundingsDiagArray, textCoordsArray, normalsArray, indicesArray, boundaryBox);
	}
	
	protected Texture createTexture(Vox vox) {
		BufferedImage img = new BufferedImage(256, 1, BufferedImage.TYPE_INT_ARGB);

		for (int c = 0; c < vox.getPalette().length - 1 ; c++) {
			img.setRGB(c, 0, vox.getPalette()[c]);
		}
      return new Texture(convertImageData(img));
	}
}
