package org.lwjglb.engine.loaders.vox.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joml.AABBf;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjglb.engine.graph.Material;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.loaders.vox.bean.Layer;
import org.lwjglb.engine.loaders.vox.bean.TransformNode;
import org.lwjglb.engine.loaders.vox.bean.Vox;
import org.lwjglb.engine.loaders.vox.bean.VoxModel;
import org.lwjglb.engine.loaders.vox.bean.VoxelFaceParam;
import org.lwjglb.engine.loaders.vox.reader.VoxelFileReader;
import org.lwjglb.engine.loaders.vox.reader.VoxelFileReaderParam;

public class VoxToGameObjectsConverter extends VoxConverter{

	public Map<Layer, List<GameItem>> read(File file) throws Exception {
		VoxelFileReader voxelFileReader = new VoxelFileReader();
		Vox vox = voxelFileReader.readVox(file);

		List<Mesh> meshes = getMeshes(vox);

		Map<Layer,List<GameItem>> mapLayers = vox.getLayers().stream().collect(Collectors.toMap(l -> l,l -> new ArrayList<>()));
		createGameItems(vox.getTransformNode(), meshes, mapLayers, new Vector3f(), new Vector3f());

		return mapLayers;
	}

	private List<Mesh> getMeshes(Vox vox) {
		List<Mesh> meshes = new ArrayList<>();
		VoxelFileReaderParam param = new VoxelFileReaderParam();
		param.setTexture(new Material(createTexture(vox)));
		for(VoxModel voxModel : vox.getVoxModels()) {
			for (int x = 0 ; x < voxModel.getWidth() ; x++) {
				for (int y = 0 ; y < voxModel.getHeight() ; y++) {
					for (int z = 0 ; z < voxModel.getDepth() ; z++) {
						if(voxModel.getMatrice()[x][y][z] != null) {
							addVoxel(param, voxModel, x, y, z);
						}
					}
				}
			}
			Vector3f vec = new Vector3f(voxModel.getWidth(), voxModel.getHeight(), voxModel.getDepth());
			AABBf boundaryBox = new AABBf(new Vector3f().sub(vec), new Vector3f().add(vec));
			Mesh mesh = createMesh(param, boundaryBox);
			mesh.setMaterial(param.getTexture());
			meshes.add(mesh);
			param.resetLists();
		}
		return meshes;
	}

	private void addVoxel(VoxelFileReaderParam param, VoxModel voxModel, int x, int y, int z) {
		Vector3i voxPosition = new Vector3i(x - (voxModel.getWidth() / 2), y - (voxModel.getHeight() / 2), z - (voxModel.getDepth() / 2));
		Vector3i voxPositionUnsigned = new Vector3i(x, y, z);
		float colorCoord = getColorCoord(voxModel, voxPositionUnsigned);

		for (VoxelFaceParam voxelFaceParam : VoxelFaceParam.values()) {
			if (voxelFaceParam.getFilter().test(voxPositionUnsigned, voxModel)) {
				addPositions(param.getPositions(), voxPosition, voxelFaceParam.getPositions());
				addSurroundings(param.getSurroundings(), voxModel, voxPositionUnsigned, voxelFaceParam.getNormal());
				addSurroundingsDiag(param.getSurroundingsDiag(), voxModel, voxPositionUnsigned, voxelFaceParam.getNormal());
				addNormals(param.getNormals(), voxelFaceParam.getNormal());
				addIndices(param.getIndices());
				addTextCoord(param.getTextCoords(), colorCoord);
			}
		}
	}

	private void createGameItems(TransformNode transformNode, List<Mesh> meshes, Map<Layer,List<GameItem>> mapLayers, Vector3f translation, Vector3f rotation){
		Vector3f t = getTranslationVector(transformNode).add(translation);
		Vector3f r = getRotation(transformNode).add(rotation);
		if(transformNode.getShapeNode() != null){
			Layer layer = mapLayers.keySet().stream().filter(l -> l.getNodeId() == transformNode.getLayerId()).findFirst().get();
			List<GameItem> items = mapLayers.getOrDefault(layer, new ArrayList<>());
			Mesh mesh = meshes.get(transformNode.getShapeNode().getShapeNodeModels().get(0).getModelId().intValue());
			GameItem item = new GameItem(mesh);
			item.setPosition(t);
			item.setRotation(new Quaternionf().rotateXYZ(r.y, r.z, r.x));
			items.add(item);
			mapLayers.put(layer, items);
		} else if(transformNode.getGroupNode() != null){
			for(TransformNode node : transformNode.getGroupNode().getTransformNodes()) {
				createGameItems(node, meshes, mapLayers, t, r);
			}
		}
	}
}
