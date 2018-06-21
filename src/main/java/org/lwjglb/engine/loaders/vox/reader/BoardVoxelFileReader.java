package org.lwjglb.engine.loaders.vox.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.AABBf;
import org.joml.Vector3i;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.items.Board;
import org.lwjglb.engine.items.Tile;
import org.lwjglb.engine.loaders.vox.bean.Vox;
import org.lwjglb.engine.loaders.vox.bean.VoxModel;
import org.lwjglb.engine.loaders.vox.bean.VoxelFaceParam;

public class BoardVoxelFileReader extends VoxelFileReader{

	public Board read(File file, int tileSize) throws Exception {

		Vox vox = readVox(file);
		VoxModel voxModel = vox.getVoxModels().get(0);
		Board board = new Board(voxModel.getWidth(), voxModel.getHeight(), voxModel.getDepth(), tileSize);
		Material material = new Material(createTexture(vox));
		List<Float> positions = new ArrayList<>();
		List<Float> surroundings = new ArrayList<>();
		List<Float> surroundingsDiag = new ArrayList<>();
		List<Float> textCoords = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		
		int maxHeight = 0;
		
		for (int xx = 0 ; xx < voxModel.getWidth() ; xx = xx + tileSize) {
			for (int z = 0 ; z < voxModel.getDepth() ; z++) {
				for (int x = xx ; x < xx + tileSize ; x++) {
					for (int y = 0 ; y < voxModel.getHeight() ; y++) {
						if(voxModel.getMatrice()[x][y][z] != null) {	
							
							if(y > maxHeight) {
								maxHeight = y;
							}

                            Vector3i voxPosition = new Vector3i(x,y,z).add(voxModel.getTranslation());
                            Vector3i voxPositionModuloTileSize = new Vector3i(voxPosition.x % tileSize,voxPosition.y ,voxPosition.z % tileSize);
							float colorCoord = getColorCoord(voxModel, voxPosition);

                            for(VoxelFaceParam voxelFaceParam : VoxelFaceParam.values()) {
                                if (voxelFaceParam.getFilter().test(voxPosition, voxModel)) {
                                    addPositions(positions, voxPositionModuloTileSize, voxelFaceParam.getPositions(), voxModel.getTranslation());
                                    addSurroundings(surroundings, voxModel, voxPosition, voxelFaceParam.getNormal());
                                    addSurroundingsDiag(surroundingsDiag, voxModel, voxPosition, voxelFaceParam.getNormal());
                                    addNormals(normals, voxelFaceParam.getNormal());
                                    addIndices(indices);
                                    addTextCoord(textCoords, colorCoord);
                                }
                            }
						}
					}
				}
				if(z % tileSize == tileSize - 1 || z == voxModel.getDepth() - 1) {
					AABBf boundaryBox = new AABBf(0,0,0,tileSize,maxHeight + 1,tileSize);
					Mesh mesh = createMesh(positions, surroundings, surroundingsDiag, textCoords, normals, indices, boundaryBox);
					mesh.setMaterial(material);
					Tile tile = new Tile(mesh, xx / tileSize, maxHeight + 1, (z - (tileSize - 1)) / tileSize);
					tile.setPosition(xx, 0, z - (tileSize - 1));
					board.getTiles().add(tile);
					
					positions = new ArrayList<>();
					surroundings = new ArrayList<>();
					surroundingsDiag = new ArrayList<>();
					textCoords = new ArrayList<>();
					normals = new ArrayList<>();
					indices = new ArrayList<>();
					maxHeight = 0;
				}
			}
		}
		return board;
	}
}
