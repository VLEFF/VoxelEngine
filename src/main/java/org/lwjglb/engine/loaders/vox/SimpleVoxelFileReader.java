package org.lwjglb.engine.loaders.vox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.AABBf;
import org.joml.Vector3f;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;

class SimpleVoxelFileReader extends VoxelFileReader{

	public Mesh[] read(File file) throws Exception {
		Vox vox = readVox(file);
		List<Mesh> meshes = new ArrayList<>();
		Material texture = new Material(createTexture(vox));
		for(VoxModel voxModel : vox.getVoxModels()) {
			List<Float> positions = new ArrayList<Float>();
			List<Float> surroundings = new ArrayList<Float>();
			List<Float> surroundingsDiag = new ArrayList<Float>();
			List<Float> textCoords = new ArrayList<Float>();
			List<Float> normals = new ArrayList<Float>();
			List<Integer> indices = new ArrayList<Integer>();
			for (int x = 0 ; x < voxModel.getWidth() ; x++) {
				for (int y = 0 ; y < voxModel.getHeight() ; y++) {
					for (int z = 0 ; z < voxModel.getDepth() ; z++) {
						if(voxModel.getMatrice()[x][y][z] != null) {		
							
							float colorCoord = getColorCoord(voxModel, x, y, z);
							
							if(x == voxModel.getWidth() - 1 || voxModel.getMatrice()[x + 1][y][z] == null) {
								Vector3f normal = new Vector3f(1,0,0);
								addPositions(positions, x, y, z, POSITIONS_RIGHT_FACE);
								addSurroundings(surroundings, voxModel, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, voxModel, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(x == 0 || voxModel.getMatrice()[x - 1][y][z] == null) {
								Vector3f normal = new Vector3f(-1,0,0);
								addPositions(positions, x, y, z, POSITIONS_LEFT_FACE);
								addSurroundings(surroundings, voxModel, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, voxModel, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(y == voxModel.getHeight() - 1 || voxModel.getMatrice()[x][y + 1][z] == null) {
								Vector3f normal = new Vector3f(0,1,0);
								addPositions(positions, x, y, z, POSITIONS_TOP_FACE);
								addSurroundings(surroundings, voxModel, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, voxModel, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(y == 0 || voxModel.getMatrice()[x][y - 1][z] == null) {
								Vector3f normal = new Vector3f(0,-1,0);
								addPositions(positions, x, y, z, POSITIONS_BOTTOM_FACE);
								addSurroundings(surroundings, voxModel, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, voxModel, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(z == voxModel.getDepth() - 1 || voxModel.getMatrice()[x][y][z + 1] == null) {
								Vector3f normal = new Vector3f(0,0,1);
								addPositions(positions, x, y, z, POSITIONS_FRONT_FACE);
								addSurroundings(surroundings, voxModel, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, voxModel, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(z == 0 || voxModel.getMatrice()[x][y][z - 1] == null) {
								Vector3f normal = new Vector3f(0,0,-1);
								addPositions(positions, x, y, z, POSITIONS_BACK_FACE);
								addSurroundings(surroundings, voxModel, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, voxModel, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
						}
					}
				}
			}
			AABBf boundaryBox = new AABBf(0, 0, 0, voxModel.getWidth(), voxModel.getHeight(), voxModel.getDepth());
			Mesh mesh = createMesh(positions, surroundings, surroundingsDiag, textCoords, normals, indices, boundaryBox);
			mesh.setMaterial(texture);
			meshes.add(mesh);
		}
		return meshes.toArray(new Mesh[meshes.size()]);
	}
}
