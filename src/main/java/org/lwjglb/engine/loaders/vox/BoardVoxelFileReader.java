package org.lwjglb.engine.loaders.vox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.AABBf;
import org.joml.Vector3f;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.items.Board;
import org.lwjglb.engine.items.Tile;

class BoardVoxelFileReader extends VoxelFileReader{

	public Board read(File file) throws Exception {

		Vox vox = readVox(file);
		Board board = new Board(vox.getWidth(), vox.getHeight(), vox.getDepth(), 8);
		Material material = new Material(createTexture(vox));
		List<Float> positions = new ArrayList<Float>();
		List<Float> surroundings = new ArrayList<Float>();
		List<Float> surroundingsDiag = new ArrayList<Float>();
		List<Float> textCoords = new ArrayList<Float>();
		List<Float> normals = new ArrayList<Float>();
		List<Integer> indices = new ArrayList<Integer>();
		
		int maxHeight = 0;
		
		for (int xx = 0 ; xx < vox.getWidth() ; xx = xx + 8) {
			for (int z = 0 ; z < vox.getDepth() ; z++) {
				for (int x = xx ; x < xx + 8 ; x++) {
					for (int y = 0 ; y < vox.getHeight() ; y++) {
						if(vox.getMatrice()[x][y][z] != null) {	
							
							if(y > maxHeight) {
								maxHeight = y;
							}
							
							float colorCoord = getColorCoord(vox, x, y, z);
							
							if(x == vox.getWidth() - 1 || vox.getMatrice()[x + 1][y][z] == null) {
								Vector3f normal = new Vector3f(1,0,0);
								addPositions(positions, x % 8, y, z % 8, POSITIONS_RIGHT_FACE);
								addSurroundings(surroundings, vox, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, vox, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(x == 0 || vox.getMatrice()[x - 1][y][z] == null) {
								Vector3f normal = new Vector3f(-1,0,0);
								addPositions(positions, x % 8, y, z % 8, POSITIONS_LEFT_FACE);
								addSurroundings(surroundings, vox, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, vox, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(y == vox.getHeight() - 1 || vox.getMatrice()[x][y + 1][z] == null) {
								Vector3f normal = new Vector3f(0,1,0);
								addPositions(positions, x % 8, y, z % 8, POSITIONS_TOP_FACE);
								addSurroundings(surroundings, vox, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, vox, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(y == 0 || vox.getMatrice()[x][y - 1][z] == null) {
								Vector3f normal = new Vector3f(0,-1,0);
								addPositions(positions, x % 8, y, z % 8, POSITIONS_BOTTOM_FACE);
								addSurroundings(surroundings, vox, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, vox, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(z == vox.getDepth() - 1 || vox.getMatrice()[x][y][z + 1] == null) {
								Vector3f normal = new Vector3f(0,0,1);
								addPositions(positions, x % 8, y, z % 8, POSITIONS_FRONT_FACE);
								addSurroundings(surroundings, vox, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, vox, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
							if(z == 0 || vox.getMatrice()[x][y][z - 1] == null) {
								Vector3f normal = new Vector3f(0,0,-1);
								addPositions(positions, x % 8, y, z % 8, POSITIONS_BACK_FACE);
								addSurroundings(surroundings, vox, x, y, z, normal);
								addSurroundingsDiag(surroundingsDiag, vox, x, y, z, normal);
								addNormals(normals, normal);
								addIndices(indices);
								addTextCoord(textCoords, colorCoord);
							}
						}
					}
				}
				if(z % 8 == 7 || z == vox.getDepth() - 1) {
					AABBf boundaryBox = new AABBf(0,0,0,8,z,8);
					Mesh mesh = createMesh(positions, surroundings, surroundingsDiag, textCoords, normals, indices, boundaryBox);
					mesh.setMaterial(material);
					Tile tile = new Tile(mesh, board, xx, maxHeight + 1, z - 7);
					tile.setPosition(xx, 0, z - 7);
					board.getTiles().add(tile);
					
					positions = new ArrayList<Float>();
					surroundings = new ArrayList<Float>();
					surroundingsDiag = new ArrayList<Float>();
					textCoords = new ArrayList<Float>();
					normals = new ArrayList<Float>();
					indices = new ArrayList<Integer>();
					maxHeight = 0;
				}
			}
		}
		return board;
	}
}
