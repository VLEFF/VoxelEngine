package org.lwjglb.engine.loaders.vox.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.AABBf;
import org.joml.Vector3i;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.loaders.vox.bean.Vox;
import org.lwjglb.engine.loaders.vox.bean.VoxModel;
import org.lwjglb.engine.loaders.vox.bean.VoxelFaceParam;

public class SimpleVoxelFileReader extends VoxelFileReader{

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
							Vector3i voxPosition = new Vector3i(x,y,z);
							float colorCoord = getColorCoord(voxModel, voxPosition);

							for(VoxelFaceParam voxelFaceParam : VoxelFaceParam.values()) {
                                if(voxelFaceParam.getFilter().test(voxPosition, voxModel)) {
                                    addPositions(positions, voxPosition, voxelFaceParam.getPositions(), voxModel.getTranslation());
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
			}
			AABBf boundaryBox = new AABBf(0, 0, 0, voxModel.getWidth(), voxModel.getHeight(), voxModel.getDepth());
			Mesh mesh = createMesh(positions, surroundings, surroundingsDiag, textCoords, normals, indices, boundaryBox);
			mesh.setMaterial(texture);
			meshes.add(mesh);
		}
		return meshes.toArray(new Mesh[meshes.size()]);
	}
}
