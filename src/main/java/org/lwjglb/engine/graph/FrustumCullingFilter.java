package org.lwjglb.engine.graph;

import java.util.List;
import java.util.Map;

import org.joml.AABBf;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjglb.engine.items.GameItem;

public class FrustumCullingFilter {

    private final Matrix4f prjViewMatrix;

    private final FrustumIntersection frustumInt;

    public FrustumCullingFilter() {
        prjViewMatrix = new Matrix4f();
        frustumInt = new FrustumIntersection();
    }

    public void updateFrustum(Matrix4f projMatrix, Matrix4f viewMatrix) {
        // Calculate projection view matrix
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        // Update frustum intersection class
        frustumInt.set(prjViewMatrix);
    }

    public void filter(Map<? extends Mesh, List<GameItem>> mapMesh) {
        for (List<GameItem> gameItems : mapMesh.values()) {
        	for (GameItem gameItem : gameItems) {
                if (!gameItem.isDisableFrustumCulling()) {
                    gameItem.setInsideFrustum(insideFrustum(gameItem));
                }
            }
        }
    }

    public boolean insideFrustum(GameItem gameItem) {
        AABBf boundaryBox = gameItem.getMesh().getBoundaryBox();
    	Vector3f min = new Vector3f(gameItem.getPosition());
    	Vector3f max = new Vector3f(gameItem.getPosition());
        min.add(boundaryBox.minX * gameItem.getScale(), boundaryBox.minY * gameItem.getScale(), boundaryBox.minZ * gameItem.getScale());
        max.add(boundaryBox.maxX * gameItem.getScale(), boundaryBox.maxY * gameItem.getScale(), boundaryBox.maxZ * gameItem.getScale());
        return frustumInt.testAab(min, max);
    }
}
