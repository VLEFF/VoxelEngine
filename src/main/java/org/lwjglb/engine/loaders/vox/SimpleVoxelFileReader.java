package org.lwjglb.engine.loaders.vox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;

class SimpleVoxelFileReader extends VoxelFileReader{

	public Mesh read(File file) throws Exception {

		Vox vox = readVox(file);
		List<Float> positions = new ArrayList<Float>();
		List<Float> textCoords = new ArrayList<Float>();
		List<Float> normals = new ArrayList<Float>();
		List<Integer> indices = new ArrayList<Integer>();
		
		for (int x = 0 ; x < vox.getWidth() ; x++) {
			for (int y = 0 ; y < vox.getHeight() ; y++) {
				for (int z = 0 ; z < vox.getDepth() ; z++) {
					if(vox.getMatrice()[x][y][z] != null) {		
						byte color = vox.getMatrice()[x][y][z];
						float colorCoord = (1.0f + ((1.0f/256.0f) * color) - (1.0f/512.0f)) % 1;
						
						if(x == vox.getWidth() - 1 || vox.getMatrice()[x + 1][y][z] == null) {
							addPositions(positions, x, y, z, POSITIONS_RIGHT_FACE);
							addNormals(normals, new Vector3f(1,0,0));
							addIndices(indices);
							addTextCoord(textCoords, colorCoord);
						}
						if(x == 0 || vox.getMatrice()[x - 1][y][z] == null) {
							addPositions(positions, x, y, z, POSITIONS_LEFT_FACE);
							addNormals(normals, new Vector3f(-1,0,0));
							addIndices(indices);
							addTextCoord(textCoords, colorCoord);
						}
						if(y == vox.getHeight() - 1 || vox.getMatrice()[x][y + 1][z] == null) {
							addPositions(positions, x, y, z, POSITIONS_TOP_FACE);
							addNormals(normals, new Vector3f(0,1,0));
							addIndices(indices);
							addTextCoord(textCoords, colorCoord);
						}
						if(y == 0 || vox.getMatrice()[x][y - 1][z] == null) {
							addPositions(positions, x, y, z, POSITIONS_BOTTOM_FACE);
							addNormals(normals, new Vector3f(0,-1,0));
							addIndices(indices);
							addTextCoord(textCoords, colorCoord);
						}
						if(z == vox.getDepth() - 1 || vox.getMatrice()[x][y][z + 1] == null) {
							addPositions(positions, x, y, z, POSITIONS_FRONT_FACE);
							addNormals(normals, new Vector3f(0,0,1));
							addIndices(indices);
							addTextCoord(textCoords, colorCoord);
						}
						if(z == 0 || vox.getMatrice()[x][y][z - 1] == null) {
							addPositions(positions, x, y, z, POSITIONS_BACK_FACE);
							addNormals(normals, new Vector3f(0,0,-1));
							addIndices(indices);
							addTextCoord(textCoords, colorCoord);
						}
					}
				}
			}
		}
				
		Mesh mesh = createMesh(positions, textCoords, normals, indices);
		mesh.setMaterial(new Material(createTexture(vox)));
		return mesh;
	}
}
