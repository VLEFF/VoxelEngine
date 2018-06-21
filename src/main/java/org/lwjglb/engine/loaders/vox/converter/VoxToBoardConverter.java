package org.lwjglb.engine.loaders.vox.converter;

import java.io.File;

import org.joml.AABBf;
import org.joml.Vector3i;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.items.Board;
import org.lwjglb.engine.items.Tile;
import org.lwjglb.engine.loaders.vox.bean.Vox;
import org.lwjglb.engine.loaders.vox.bean.VoxModel;
import org.lwjglb.engine.loaders.vox.bean.VoxelFaceParam;
import org.lwjglb.engine.loaders.vox.reader.VoxelFileReader;
import org.lwjglb.engine.loaders.vox.reader.VoxelFileReaderParam;

public class VoxToBoardConverter extends VoxConverter{

	public Board read(File file, int tileSize) throws Exception {

        VoxelFileReader voxelFileReader = new VoxelFileReader();
        Vox vox = voxelFileReader.readVox(file);
		VoxelFileReaderParam param = new VoxelFileReaderParam();
		param.setTexture(new Material(createTexture(vox)));
		VoxModel voxModel = vox.getVoxModels().get(0);
		Board board = new Board(voxModel.getWidth(), voxModel.getHeight(), voxModel.getDepth(), tileSize);
		int maxHeight = 0;
		
		for (int xx = 0 ; xx < voxModel.getWidth() ; xx = xx + tileSize) {
			for (int z = 0 ; z < voxModel.getDepth() ; z++) {
				for (int x = xx ; x < xx + tileSize ; x++) {
					for (int y = 0 ; y < voxModel.getHeight() ; y++) {
						if(voxModel.getMatrice()[x][y][z] != null) {
							if(y > maxHeight) {
								maxHeight = y;
							}
                            addVoxel(param, voxModel, tileSize, z, x, y);
						}
					}
				}
				if(z % tileSize == tileSize - 1 || z == voxModel.getDepth() - 1) {
					AABBf boundaryBox = new AABBf(0,0,0,tileSize,maxHeight + 1,tileSize);
					Mesh mesh = createMesh(param, boundaryBox);
					mesh.setMaterial(param.getTexture());
					Tile tile = new Tile(mesh, xx / tileSize, maxHeight + 1, (z - (tileSize - 1)) / tileSize);
					tile.setPosition(xx, 0, z - (tileSize - 1));
					board.getTiles().add(tile);
					param.resetLists();
					maxHeight = 0;
				}
			}
		}
		return board;
	}

  private void addVoxel(VoxelFileReaderParam param, VoxModel voxModel, int tileSize, int z, int x, int y) {
    Vector3i voxPosition = new Vector3i(x, y, z);
    Vector3i voxPositionModuloTileSize = new Vector3i(voxPosition.x % tileSize, voxPosition.y, voxPosition.z % tileSize);
    float colorCoord = getColorCoord(voxModel, voxPosition);

    for (VoxelFaceParam voxelFaceParam : VoxelFaceParam.values()) {
      if (voxelFaceParam.getFilter().test(voxPosition, voxModel)) {
        addPositions(param.getPositions(), voxPositionModuloTileSize, voxelFaceParam.getPositions());
        addSurroundings(param.getSurroundings(), voxModel, voxPosition, voxelFaceParam.getNormal());
        addSurroundingsDiag(param.getSurroundingsDiag(), voxModel, voxPosition, voxelFaceParam.getNormal());
        addNormals(param.getNormals(), voxelFaceParam.getNormal());
        addIndices(param.getIndices());
        addTextCoord(param.getTextCoords(), colorCoord);
      }
    }
  }
}
