package org.lwjglb.engine.loaders.vox.converter;


import org.apache.commons.lang3.ArrayUtils;
import org.joml.AABBf;
import org.joml.AxisAngle4f;
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
import org.lwjglb.engine.loaders.vox.reader.VoxelFileReaderParam;

import javax.imageio.ImageIO;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class VoxConverter {

	private static final List<Integer> INDICES = Arrays.asList(0,1,2,0,2,3);

	protected float getColorCoord(VoxModel voxModel, Vector3i voxPosition) {
		byte color = voxModel.getMatrice()[voxPosition.x][voxPosition.y][voxPosition.z];
		return (1.0f + ((1.0f/256.0f) * color) - (1.0f/512.0f)) % 1;
	}
	
	protected void addTextCoord(Collection<Float> textCoords, float colorCoord) {
		for(int i = 0 ; i < 4 ; i++) {
			textCoords.add(colorCoord);
			textCoords.add(0.5f);
		}
	}
	
	protected void addIndices(Collection<Integer> indices) {
		for(Integer i : INDICES) {
			indices.add(i + (indices.size() / 6) * 4);
		}
	}
	
	protected void addNormals(Collection<Float> normals, Vector3f normal) {
		for(int i = 0 ; i < 4 ; i++) {
			normals.add(normal.x);
			normals.add(normal.y);
			normals.add(normal.z);
		}
	}
	
	protected void addSurroundings(Collection<Float> surroundings, VoxModel voxModel, Vector3i voxPosition, Vector3f normal) {
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
	
	protected void addSurroundingsDiag(Collection<Float> surroundingsDiag, VoxModel voxModel, Vector3i voxPosition, Vector3f normal) {
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
	
	protected void addPositions(Collection<Float> positions, Vector3i voxPosition, float[][] positionsFace){
		for(int i = 0 ; i < 4 ; i++) {
			positions.add(positionsFace[i][0] + voxPosition.x);
			positions.add(positionsFace[i][1] + voxPosition.y);
			positions.add(positionsFace[i][2] + voxPosition.z);
		}
	}
	
	protected Mesh createMesh(VoxelFileReaderParam param, AABBf boundaryBox){
		float[] positionsArray = ArrayUtils.toPrimitive(param.getPositions().toArray(new Float[param.getPositions().size()]));
		float[] surroundingsArray = ArrayUtils.toPrimitive(param.getSurroundings().toArray(new Float[param.getSurroundings().size()]));
		float[] surroundingsDiagArray = ArrayUtils.toPrimitive(param.getSurroundingsDiag().toArray(new Float[param.getSurroundingsDiag().size()]));
		float[] textCoordsArray = ArrayUtils.toPrimitive(param.getTextCoords().toArray(new Float[param.getTextCoords().size()]));
		float[] normalsArray = ArrayUtils.toPrimitive(param.getNormals().toArray(new Float[param.getNormals().size()]));
		int[] indicesArray = ArrayUtils.toPrimitive(param.getIndices().toArray(new Integer[param.getIndices().size()]));
		return new Mesh(positionsArray, surroundingsArray, surroundingsDiagArray, textCoordsArray, normalsArray, indicesArray, boundaryBox);
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
	
	protected Texture createTexture(Vox vox) {
		BufferedImage img = new BufferedImage(256, 1, BufferedImage.TYPE_INT_ARGB);

		for (int c = 0; c < vox.getPalette().length - 1 ; c++) {
			img.setRGB(c, 0, vox.getPalette()[c]);
		}
      return new Texture(convertImageData(img));
	}


	protected Vector3f getTranslationVector(TransformNode transformNode) {
		String[] translationTab = transformNode.getTransformations().get(0).getOrDefault("_t", "0 0 0").split(" ");
		try {
			return new Vector3f(Float.parseFloat(translationTab[1]), Float.parseFloat(translationTab[2]), Float.parseFloat(translationTab[0]));
		} catch (NumberFormatException e) {
			return new Vector3f();
		}
	}

	protected Vector3f getRotation(TransformNode transformNode) {
		String rotation = transformNode.getTransformations().get(0).get("_r");
		Matrix3f matrix = new Matrix3f();
		if (rotation != null) {
			byte b = (byte) Integer.parseInt(rotation);
			int firstLineIndex = b & 0b00000011;
			int secondLineIndex = (b >> 2) & 0b00000011;
			int firstLineSign = (b >> 4) & 0b00000001;
			int secondLineSign = (b >> 5) & 0b00000001;
			int thirdLineSign = (b >> 6) & 0b00000001;

			matrix.setRow(0, firstLineIndex == 0 ? 1 : 0, firstLineIndex == 1 ? 1 : 0, firstLineIndex == 2 ? 1 : 0);
			matrix.setRow(1, secondLineIndex == 0 ? 1 : 0, secondLineIndex == 1 ? 1 : 0, secondLineIndex == 2 ? 1 : 0);
			matrix.setRow(
					2,
					3 - firstLineIndex - secondLineIndex == 0 ? 1 : 0,
					3 - firstLineIndex - secondLineIndex == 1 ? 1 : 0,
					3 - firstLineIndex - secondLineIndex == 2 ? 1 : 0
			);
			matrix.set(firstLineIndex, 0, matrix.get(firstLineIndex, 0) * (firstLineSign == 1 ? -1 : 1));
			matrix.set(secondLineIndex, 1, matrix.get(secondLineIndex, 1) * (secondLineSign == 1 ? -1 : 1));
			matrix.set(3 - firstLineIndex - secondLineIndex, 2, matrix.get(3 - firstLineIndex - secondLineIndex, 2) * (thirdLineSign == 1 ? -1 : 1));
		}
		return matrix.getEulerAnglesZYX(new Vector3f());
	}
}
