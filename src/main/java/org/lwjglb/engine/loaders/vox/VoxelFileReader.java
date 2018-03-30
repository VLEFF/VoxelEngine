package org.lwjglb.engine.loaders.vox;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.joml.Vector4f;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Texture;
import org.lwjglb.engine.loaders.assimp.TextureCache;

class VoxelFileReader {
	
	private static class Chunk {
		long id;
		int contentSize;
		int childrenSize;
	}

	private byte[] buf = new byte[4];

	public Mesh read(File file) throws Exception {

		Mesh mesh = null;
		
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

		boolean foundPalette = false;
		int width = 0;
		int depth = 0;
		int height = 0;
		for (; ; ) {

			try {
				readChunk(input, chunk);
			} catch (IOException ignored) {
				break;
			}

			if (chunk.id == magicValue('S', 'I', 'Z', 'E')) {
				depth = (int) read32(input);
				width = (int) read32(input);
				height = (int) read32(input);
			} else if (chunk.id == magicValue('X', 'Y', 'Z', 'I')) {

				int numVoxels = (int) read32(input);
				
				Byte[][][] matrice= new Byte[width][height][depth];

				for (int v = 0; v < numVoxels ; v++) {
					int z = input.read();
					int x = input.read();
					int y = input.read();
					matrice[x][y][z] = (byte) (input.read() & 0xff);
				}
				
				float[] positions = new float[numVoxels * 3 * 6 * 4];
				float[] textCoords = new float[numVoxels * 2 * 6 * 4];
				float[] normals = new float[numVoxels * 3 * 6 * 4];
				int[] indices = new int[numVoxels * 3 * 12];
				int numPixel = 0;
				for (int x = 0 ; x < width ; x++) {
					for (int y = 0 ; y < height ; y++) {
						for (int z = 0 ; z < depth ; z++) {
							if(matrice[x][y][z] != null) {
								
								byte color = matrice[x][y][z];
								float colorCoord = (1.0f + ((1.0f/256.0f) * color) - (1.0f/512.0f)) % 1;
								
								if(x == width - 1 || matrice[x + 1][y][z] == null) {
									//right
									positions[(numPixel * 3 * 6 * 4) + 12] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 13] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 14] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 15] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 16] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 17] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 18] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 19] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 20] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 21] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 22] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 23] = 1 + z;
									
									//right
									indices[(numPixel * 3 * 12) + 6] = 4 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 7] = 5 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 8] = 6 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 9] = 4 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 10] = 6 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 11] = 7 + (numPixel * 6 * 4);

									//right
									normals[(numPixel * 3 * 6 * 4) + 12] = 1;
									normals[(numPixel * 3 * 6 * 4) + 13] = 0;
									normals[(numPixel * 3 * 6 * 4) + 14] = 0;
									normals[(numPixel * 3 * 6 * 4) + 15] = 1;
									normals[(numPixel * 3 * 6 * 4) + 16] = 0;
									normals[(numPixel * 3 * 6 * 4) + 17] = 0;
									normals[(numPixel * 3 * 6 * 4) + 18] = 1;
									normals[(numPixel * 3 * 6 * 4) + 19] = 0;
									normals[(numPixel * 3 * 6 * 4) + 20] = 0;
									normals[(numPixel * 3 * 6 * 4) + 21] = 1;
									normals[(numPixel * 3 * 6 * 4) + 22] = 0;
									normals[(numPixel * 3 * 6 * 4) + 23] = 0;

									//right
									textCoords[(numPixel * 2 * 6 * 4) + 8] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 9] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 10] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 11] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 12] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 13] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 14] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 15] = 0.5f;
								}
								if(x == 0 || matrice[x - 1][y][z] == null) {
									//left
									positions[(numPixel * 3 * 6 * 4) + 0] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 1] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 2] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 3] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 4] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 5] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 6] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 7] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 8] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 9] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 10] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 11] = 1 + z;
									
									//left
									indices[(numPixel * 3 * 12) + 0] = 0 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 1] = 1 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 2] = 2 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 3] = 0 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 4] = 2 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 5] = 3 + (numPixel * 6 * 4);

									//left
									normals[(numPixel * 3 * 6 * 4) + 0] = -1;
									normals[(numPixel * 3 * 6 * 4) + 1] = 0;
									normals[(numPixel * 3 * 6 * 4) + 2] = 0;
									normals[(numPixel * 3 * 6 * 4) + 3] = -1;
									normals[(numPixel * 3 * 6 * 4) + 4] = 0;
									normals[(numPixel * 3 * 6 * 4) + 5] = 0;
									normals[(numPixel * 3 * 6 * 4) + 6] = -1;
									normals[(numPixel * 3 * 6 * 4) + 7] = 0;
									normals[(numPixel * 3 * 6 * 4) + 8] = 0;
									normals[(numPixel * 3 * 6 * 4) + 9] = -1;
									normals[(numPixel * 3 * 6 * 4) + 10] = 0;
									normals[(numPixel * 3 * 6 * 4) + 11] = 0;
									
									//left
									textCoords[(numPixel * 2 * 6 * 4) + 0] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 1] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 2] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 3] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 4] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 5] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 6] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 7] = 0.5f;
									
								}
								if(y == height - 1 || matrice[x][y + 1][z] == null) {
									//top
									positions[(numPixel * 3 * 6 * 4) + 60] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 61] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 62] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 63] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 64] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 65] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 66] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 67] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 68] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 69] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 70] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 71] = 0 + z;
									
									//top
									indices[(numPixel * 3 * 12) + 30] = 20 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 31] = 21 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 32] = 22 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 33] = 20 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 34] = 22 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 35] = 23 + (numPixel * 6 * 4);
									
									//top
									normals[(numPixel * 3 * 6 * 4) + 60] = 0;
									normals[(numPixel * 3 * 6 * 4) + 61] = 1;
									normals[(numPixel * 3 * 6 * 4) + 62] = 0;
									normals[(numPixel * 3 * 6 * 4) + 63] = 0;
									normals[(numPixel * 3 * 6 * 4) + 64] = 1;
									normals[(numPixel * 3 * 6 * 4) + 65] = 0;
									normals[(numPixel * 3 * 6 * 4) + 66] = 0;
									normals[(numPixel * 3 * 6 * 4) + 67] = 1;
									normals[(numPixel * 3 * 6 * 4) + 68] = 0;
									normals[(numPixel * 3 * 6 * 4) + 69] = 0;
									normals[(numPixel * 3 * 6 * 4) + 70] = 1;
									normals[(numPixel * 3 * 6 * 4) + 71] = 0;
									
									//top
									textCoords[(numPixel * 2 * 6 * 4) + 40] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 41] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 42] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 43] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 44] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 45] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 46] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 47] = 0.5f;
								}
								if(y == 0 || matrice[x][y - 1][z] == null) {
									//bottom
									positions[(numPixel * 3 * 6 * 4) + 48] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 49] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 50] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 51] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 52] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 53] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 54] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 55] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 56] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 57] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 58] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 59] = 1 + z;
									
									//bottom
									indices[(numPixel * 3 * 12) + 24] = 16 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 25] = 17 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 26] = 18 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 27] = 16 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 28] = 18 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 29] = 19 + (numPixel * 6 * 4);
									
									//bottom
									normals[(numPixel * 3 * 6 * 4) + 48] = 0;
									normals[(numPixel * 3 * 6 * 4) + 49] = -1;
									normals[(numPixel * 3 * 6 * 4) + 50] = 0;
									normals[(numPixel * 3 * 6 * 4) + 51] = 0;
									normals[(numPixel * 3 * 6 * 4) + 52] = -1;
									normals[(numPixel * 3 * 6 * 4) + 53] = 0;
									normals[(numPixel * 3 * 6 * 4) + 54] = 0;
									normals[(numPixel * 3 * 6 * 4) + 55] = -1;
									normals[(numPixel * 3 * 6 * 4) + 56] = 0;
									normals[(numPixel * 3 * 6 * 4) + 57] = 0;
									normals[(numPixel * 3 * 6 * 4) + 58] = -1;
									normals[(numPixel * 3 * 6 * 4) + 59] = 0;
									
									//bottom
									textCoords[(numPixel * 2 * 6 * 4) + 32] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 33] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 34] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 35] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 36] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 37] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 38] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 39] = 0.5f;
								}
								if(z == depth - 1 || matrice[x][y][z + 1] == null) {
									//front
									positions[(numPixel * 3 * 6 * 4) + 24] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 25] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 26] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 27] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 28] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 29] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 30] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 31] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 32] = 1 + z;
									positions[(numPixel * 3 * 6 * 4) + 33] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 34] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 35] = 1 + z;
									
									//front
									indices[(numPixel * 3 * 12) + 12] = 8 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 13] = 9 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 14] = 10 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 15] = 8 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 16] = 10 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 17] = 11 + (numPixel * 6 * 4);
									
									//front
									normals[(numPixel * 3 * 6 * 4) + 24] = 0;
									normals[(numPixel * 3 * 6 * 4) + 25] = 0;
									normals[(numPixel * 3 * 6 * 4) + 26] = 1;
									normals[(numPixel * 3 * 6 * 4) + 27] = 0;
									normals[(numPixel * 3 * 6 * 4) + 28] = 0;
									normals[(numPixel * 3 * 6 * 4) + 29] = 1;
									normals[(numPixel * 3 * 6 * 4) + 30] = 0;
									normals[(numPixel * 3 * 6 * 4) + 31] = 0;
									normals[(numPixel * 3 * 6 * 4) + 32] = 1;
									normals[(numPixel * 3 * 6 * 4) + 33] = 0;
									normals[(numPixel * 3 * 6 * 4) + 34] = 0;
									normals[(numPixel * 3 * 6 * 4) + 35] = 1;
									
									//front
									textCoords[(numPixel * 2 * 6 * 4) + 16] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 17] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 18] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 19] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 20] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 21] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 22] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 23] = 0.5f;
								}
								if(z == 0 || matrice[x][y][z - 1] == null) {
									//back
									positions[(numPixel * 3 * 6 * 4) + 36] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 37] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 38] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 39] = 0 + x;
									positions[(numPixel * 3 * 6 * 4) + 40] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 41] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 42] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 43] = 1 + y;
									positions[(numPixel * 3 * 6 * 4) + 44] = 0 + z;
									positions[(numPixel * 3 * 6 * 4) + 45] = 1 + x;
									positions[(numPixel * 3 * 6 * 4) + 46] = 0 + y;
									positions[(numPixel * 3 * 6 * 4) + 47] = 0 + z;
									
									//back
									indices[(numPixel * 3 * 12) + 18] = 12 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 19] = 13 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 20] = 14 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 21] = 12 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 22] = 14 + (numPixel * 6 * 4);
									indices[(numPixel * 3 * 12) + 23] = 15 + (numPixel * 6 * 4);
									
									//back
									normals[(numPixel * 3 * 6 * 4) + 36] = 0;
									normals[(numPixel * 3 * 6 * 4) + 37] = 0;
									normals[(numPixel * 3 * 6 * 4) + 38] = -1;
									normals[(numPixel * 3 * 6 * 4) + 39] = 0;
									normals[(numPixel * 3 * 6 * 4) + 40] = 0;
									normals[(numPixel * 3 * 6 * 4) + 41] = -1;
									normals[(numPixel * 3 * 6 * 4) + 42] = 0;
									normals[(numPixel * 3 * 6 * 4) + 43] = 0;
									normals[(numPixel * 3 * 6 * 4) + 44] = -1;
									normals[(numPixel * 3 * 6 * 4) + 45] = 0;
									normals[(numPixel * 3 * 6 * 4) + 46] = 0;
									normals[(numPixel * 3 * 6 * 4) + 47] = -1;
									
									//back
									textCoords[(numPixel * 2 * 6 * 4) + 24] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 25] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 26] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 27] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 28] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 29] = 0.5f;
									textCoords[(numPixel * 2 * 6 * 4) + 30] = colorCoord;
									textCoords[(numPixel * 2 * 6 * 4) + 31] = 0.5f;
								}
								numPixel++;
							}
							
						}
					}
				}
				mesh = new Mesh(positions, textCoords, normals, indices);
			} else if (chunk.id == magicValue('R', 'G', 'B', 'A')) {

				// MagicaVoxel documentation:
				// - last color is not used
				// - the first color is corresponding to color index 1

				BufferedImage img = new BufferedImage(256, 1, BufferedImage.TYPE_INT_ARGB);

				for (int p = 1; p < 256; p++) {
					int rgba = (int) read32(input);
					int color = patchColor(rgba);
					img.setRGB(p - 1, 0, color);
				}
				
				try{
			       File f = new File("D:\\Output.png");
			       ImageIO.write(img, "png", f);
			     }catch(IOException e){
			       System.out.println("Error: " + e);
			     }
				
	            Texture texture = new Texture(convertImageData(img));
				mesh.setMaterial(new Material(texture));

				if (input.skip(4) != 4) {
					throw new Exception("Unexpected EOF.");
				}

				foundPalette = true;

			} else {
				// unexpected chunk, ignore & skip
				int bytesToSkip = chunk.contentSize + chunk.childrenSize;
				if (input.skip(bytesToSkip) != bytesToSkip) {
					throw new Exception("Invalid number of bytes skipped.");
				}
			}

		}

		if (!foundPalette) {
			// default palette
			for (int p = 0; p < 256; p++) {
			}
		}

		input.close();
		return mesh;
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

	private void readChunk(BufferedInputStream input, Chunk chunk) throws IOException {
		chunk.id = read32(input);
		chunk.contentSize = (int) read32(input);
		chunk.childrenSize = (int) read32(input);
	}

	private long read32(BufferedInputStream input) throws IOException {
		if (input.read(buf) < 4) {
			throw new IOException();
		}
		return (buf[0] & 0xff) | ((buf[1] & 0xff) << 8) | ((buf[2] & 0xff) << 16) | ((buf[3] & 0xff) << 24);
	}

	private long magicValue(char c0, char c1, char c2, char c3) {
		return ((c3 << 24) & 0xff000000) | ((c2 << 16) & 0x00ff0000) | ((c1 << 8) & 0x0000ff00) | (c0 & 0x000000ff);
	}

	private int patchColor(int rgba) {
		int b = (rgba & 0x00ff0000) >> 16;
		int g = (rgba & 0x0000ff00) >> 8;
		int r = rgba & 0x000000ff;
		return (0xff<<24) | (r<<16) | (g<<8) | b; //pixel
	}

	private static final int[] defaultPalette = {
			0x00000000, 0xffffffff, 0xffccffff, 0xff99ffff, 0xff66ffff, 0xff33ffff, 0xff00ffff, 0xffffccff, 0xffccccff, 0xff99ccff, 0xff66ccff, 0xff33ccff, 0xff00ccff, 0xffff99ff, 0xffcc99ff, 0xff9999ff,
			0xff6699ff, 0xff3399ff, 0xff0099ff, 0xffff66ff, 0xffcc66ff, 0xff9966ff, 0xff6666ff, 0xff3366ff, 0xff0066ff, 0xffff33ff, 0xffcc33ff, 0xff9933ff, 0xff6633ff, 0xff3333ff, 0xff0033ff, 0xffff00ff,
			0xffcc00ff, 0xff9900ff, 0xff6600ff, 0xff3300ff, 0xff0000ff, 0xffffffcc, 0xffccffcc, 0xff99ffcc, 0xff66ffcc, 0xff33ffcc, 0xff00ffcc, 0xffffcccc, 0xffcccccc, 0xff99cccc, 0xff66cccc, 0xff33cccc,
			0xff00cccc, 0xffff99cc, 0xffcc99cc, 0xff9999cc, 0xff6699cc, 0xff3399cc, 0xff0099cc, 0xffff66cc, 0xffcc66cc, 0xff9966cc, 0xff6666cc, 0xff3366cc, 0xff0066cc, 0xffff33cc, 0xffcc33cc, 0xff9933cc,
			0xff6633cc, 0xff3333cc, 0xff0033cc, 0xffff00cc, 0xffcc00cc, 0xff9900cc, 0xff6600cc, 0xff3300cc, 0xff0000cc, 0xffffff99, 0xffccff99, 0xff99ff99, 0xff66ff99, 0xff33ff99, 0xff00ff99, 0xffffcc99,
			0xffcccc99, 0xff99cc99, 0xff66cc99, 0xff33cc99, 0xff00cc99, 0xffff9999, 0xffcc9999, 0xff999999, 0xff669999, 0xff339999, 0xff009999, 0xffff6699, 0xffcc6699, 0xff996699, 0xff666699, 0xff336699,
			0xff006699, 0xffff3399, 0xffcc3399, 0xff993399, 0xff663399, 0xff333399, 0xff003399, 0xffff0099, 0xffcc0099, 0xff990099, 0xff660099, 0xff330099, 0xff000099, 0xffffff66, 0xffccff66, 0xff99ff66,
			0xff66ff66, 0xff33ff66, 0xff00ff66, 0xffffcc66, 0xffcccc66, 0xff99cc66, 0xff66cc66, 0xff33cc66, 0xff00cc66, 0xffff9966, 0xffcc9966, 0xff999966, 0xff669966, 0xff339966, 0xff009966, 0xffff6666,
			0xffcc6666, 0xff996666, 0xff666666, 0xff336666, 0xff006666, 0xffff3366, 0xffcc3366, 0xff993366, 0xff663366, 0xff333366, 0xff003366, 0xffff0066, 0xffcc0066, 0xff990066, 0xff660066, 0xff330066,
			0xff000066, 0xffffff33, 0xffccff33, 0xff99ff33, 0xff66ff33, 0xff33ff33, 0xff00ff33, 0xffffcc33, 0xffcccc33, 0xff99cc33, 0xff66cc33, 0xff33cc33, 0xff00cc33, 0xffff9933, 0xffcc9933, 0xff999933,
			0xff669933, 0xff339933, 0xff009933, 0xffff6633, 0xffcc6633, 0xff996633, 0xff666633, 0xff336633, 0xff006633, 0xffff3333, 0xffcc3333, 0xff993333, 0xff663333, 0xff333333, 0xff003333, 0xffff0033,
			0xffcc0033, 0xff990033, 0xff660033, 0xff330033, 0xff000033, 0xffffff00, 0xffccff00, 0xff99ff00, 0xff66ff00, 0xff33ff00, 0xff00ff00, 0xffffcc00, 0xffcccc00, 0xff99cc00, 0xff66cc00, 0xff33cc00,
			0xff00cc00, 0xffff9900, 0xffcc9900, 0xff999900, 0xff669900, 0xff339900, 0xff009900, 0xffff6600, 0xffcc6600, 0xff996600, 0xff666600, 0xff336600, 0xff006600, 0xffff3300, 0xffcc3300, 0xff993300,
			0xff663300, 0xff333300, 0xff003300, 0xffff0000, 0xffcc0000, 0xff990000, 0xff660000, 0xff330000, 0xff0000ee, 0xff0000dd, 0xff0000bb, 0xff0000aa, 0xff000088, 0xff000077, 0xff000055, 0xff000044,
			0xff000022, 0xff000011, 0xff00ee00, 0xff00dd00, 0xff00bb00, 0xff00aa00, 0xff008800, 0xff007700, 0xff005500, 0xff004400, 0xff002200, 0xff001100, 0xffee0000, 0xffdd0000, 0xffbb0000, 0xffaa0000,
			0xff880000, 0xff770000, 0xff550000, 0xff440000, 0xff220000, 0xff110000, 0xffeeeeee, 0xffdddddd, 0xffbbbbbb, 0xffaaaaaa, 0xff888888, 0xff777777, 0xff555555, 0xff444444, 0xff222222, 0xff111111
	};

}
