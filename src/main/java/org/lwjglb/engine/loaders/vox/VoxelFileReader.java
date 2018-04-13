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
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joml.AABBf;
import org.joml.Vector3f;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Texture;

abstract class VoxelFileReader {
	
	protected final static float[][] POSITIONS_RIGHT_FACE = {{1f,0f,1f},{1f,0f,0f},{1f,1f,0f},{1f,1f,1f}};
	protected final static float[][] POSITIONS_LEFT_FACE = {{0f,1f,1f},{0f,1f,0f},{0f,0f,0f},{0f,0f,1f}};
	protected final static float[][] POSITIONS_TOP_FACE = {{0f,1f,1f},{1f,1f,1f},{1f,1f,0f},{0f,1f,0f}};
	protected final static float[][] POSITIONS_BOTTOM_FACE = {{0f,0f,0f},{1f,0f,0f},{1f,0f,1f},{0f,0f,1f}};
	protected final static float[][] POSITIONS_FRONT_FACE = {{1f,0f,1f},{1f,1f,1f},{0f,1f,1f},{0f,0f,1f}};
	protected final static float[][] POSITIONS_BACK_FACE = {{0f,0f,0f},{0f,1f,0f},{1f,1f,0f},{1f,0f,0f}};
	
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
		while(true) {
			try {
				readChunk(input, chunk);
			} catch (IOException ignored) {
				break;
			}
			if (chunk.id == magicValue('n', 'T', 'R', 'N')) {
				//System.out.println(read32(input));
			}
			if (chunk.id == magicValue('S', 'I', 'Z', 'E')) {
				readVoxSize(input, vox);
			} else if (chunk.id == magicValue('X', 'Y', 'Z', 'I')) {
				readVoxContent(input, vox);
			} else if (chunk.id == magicValue('R', 'G', 'B', 'A')) {
				readVoxPalette(input, vox);
			} else {
				skip(input, chunk);
			}

		}
		input.close();
		return vox;
	}
	
	private void readVoxSize(BufferedInputStream input, Vox vox) throws IOException {
		vox.setDepth((int) read32(input));
		vox.setWidth((int) read32(input));
		vox.setHeight((int) read32(input));
	}
	
	private void readVoxContent(BufferedInputStream input, Vox vox) throws IOException {
		vox.setNumVoxel((int) read32(input));
		Byte[][][] matrice= new Byte[vox.getWidth()][vox.getHeight()][vox.getDepth()];
		for (int v = 0; v < vox.getNumVoxel() ; v++) {
			int z = input.read();
			int x = input.read();
			int y = input.read();
			matrice[x][y][z] = (byte) (input.read() & 0xff);
		}
		vox.setMatrice(matrice);
	}
	
	private void readVoxPalette(BufferedInputStream input, Vox vox) throws Exception {
		int[] palette = new int[256];
		for (int p = 1; p < 256; p++) {
			int rgba = (int) read32(input);
			palette[p-1] = patchColor(rgba);
		}
		vox.setPalette(palette);
		if (input.skip(4) != 4) {
			throw new Exception("Unexpected EOF.");
		}
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

	protected long magicValue(char c0, char c1, char c2, char c3) {
		return ((c3 << 24) & 0xff000000) | ((c2 << 16) & 0x00ff0000) | ((c1 << 8) & 0x0000ff00) | (c0 & 0x000000ff);
	}

	protected int patchColor(int rgba) {
		int b = (rgba & 0x00ff0000) >> 16;
		int g = (rgba & 0x0000ff00) >> 8;
		int r = rgba & 0x000000ff;
		return (0xff<<24) | (r<<16) | (g<<8) | b; //pixel
	}
	
	protected float getColorCoord(Vox vox, int x, int y, int z) {
		byte color = vox.getMatrice()[x][y][z];
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
	
	protected void addSurroundings(List<Float> surroundings, Vox vox, int x, int y, int z, Vector3f normal) {
		boolean inBoundary = isInBoundary(vox, x, y, z, normal);
		for(int i = 0 ; i < 4 ; i++) {
			if(normal.x != 0) {
				surroundings.add(inBoundary && y > 0 && vox.getMatrice()[x + (int) normal.x][y - 1][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z > 0 && vox.getMatrice()[x + (int) normal.x][y][z - 1] != null ? 1f : 0f);
				surroundings.add(inBoundary && y < vox.getHeight() - 1 && vox.getMatrice()[x + (int) normal.x][y + 1][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z < vox.getDepth() - 1 && vox.getMatrice()[x + (int) normal.x][y][z + 1] != null ? 1f : 0f);
			} else if(normal.y != 0) {
				surroundings.add(inBoundary && x > 0 && vox.getMatrice()[x - 1][y + (int) normal.y][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z > 0 && vox.getMatrice()[x][y + (int) normal.y][z - 1] != null ? 1f : 0f);
				surroundings.add(inBoundary && x < vox.getWidth() - 1 && vox.getMatrice()[x + 1][y + (int) normal.y][z] != null ? 1f : 0f);
				surroundings.add(inBoundary && z < vox.getDepth() - 1 && vox.getMatrice()[x][y + (int) normal.y][z + 1] != null ? 1f : 0f);
			} else if(normal.z != 0) {
				surroundings.add(inBoundary && x > 0 && vox.getMatrice()[x - 1][y][z + (int) normal.z] != null ? 1f : 0f);
				surroundings.add(inBoundary && y > 0 && vox.getMatrice()[x][y - 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundings.add(inBoundary && x < vox.getWidth() - 1 && vox.getMatrice()[x + 1][y][z + (int) normal.z] != null ? 1f : 0f);
				surroundings.add(inBoundary && y < vox.getHeight() - 1 && vox.getMatrice()[x][y + 1][z + (int) normal.z] != null ? 1f : 0f);
			}
		}
	}
	
	protected void addSurroundingsDiag(List<Float> surroundingsDiag, Vox vox, int x, int y, int z, Vector3f normal) {
		boolean inBoundary = isInBoundary(vox, x, y, z, normal);
		for(int i = 0 ; i < 4 ; i++) {
			if(normal.x != 0) {
				surroundingsDiag.add(inBoundary && y > 0 && z > 0 && vox.getMatrice()[x + (int) normal.x][y - 1][z - 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && y > 0 && z < vox.getDepth() - 1 && vox.getMatrice()[x + (int) normal.x][y - 1][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && y < vox.getHeight() - 1 && z < vox.getDepth() - 1 && vox.getMatrice()[x + (int) normal.x][y + 1][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && y < vox.getHeight() - 1 && z > 0 && vox.getMatrice()[x + (int) normal.x][y + 1][z - 1] != null ? 1f : 0f);
			} else if(normal.y != 0) {
				surroundingsDiag.add(inBoundary && x > 0 && z > 0 && vox.getMatrice()[x - 1][y + (int) normal.y][z - 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x > 0 && z < vox.getDepth() - 1 && vox.getMatrice()[x - 1][y + (int) normal.y][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < vox.getWidth() - 1 && z < vox.getDepth() - 1 && vox.getMatrice()[x + 1][y + (int) normal.y][z + 1] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < vox.getWidth() - 1 && z > 0 && vox.getMatrice()[x + 1][y + (int) normal.y][z - 1] != null ? 1f : 0f);
			} else if(normal.z != 0) {
				surroundingsDiag.add(inBoundary && x > 0 && y > 0 && vox.getMatrice()[x - 1][y - 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x > 0 && y < vox.getHeight() - 1 && vox.getMatrice()[x - 1][y + 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < vox.getWidth() - 1 && y < vox.getHeight() - 1 && vox.getMatrice()[x + 1][y + 1][z + (int) normal.z] != null ? 1f : 0f);
				surroundingsDiag.add(inBoundary && x < vox.getWidth() - 1 && y > 0 && vox.getMatrice()[x + 1][y - 1][z + (int) normal.z] != null ? 1f : 0f);
			}
		}
	}
	
	private boolean isInBoundary(Vox vox, int x, int y, int z, Vector3f normal) {
		return x + normal.x < vox.getWidth() 
				&& x + normal.x >= 0 
				&& y + normal.y < vox.getHeight()
				&& y + normal.y >= 0 
				&& z + normal.z < vox.getDepth()
				&& z + normal.z >= 0;
	}
	
	protected void addPositions(List<Float> positions, int x, int y, int z, float[][] positionsFace){
		for(int i = 0 ; i < 4 ; i++) {
			positions.add(positionsFace[i][0] + x);
			positions.add(positionsFace[i][1] + y);
			positions.add(positionsFace[i][2] + z);
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
