package org.lwjglb.game;

import java.util.ArrayList;
import java.util.List;

import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjglb.engine.graph.Camera;
import org.lwjglb.engine.items.GameItem;

public class CameraBoxSelectionDetector {

    private final Vector3f max;

    private final Vector3f min;

    private final Vector2f nearFar;

    private Vector3f dir;

    public CameraBoxSelectionDetector() {
        dir = new Vector3f();
        min = new Vector3f();
        max = new Vector3f();
        nearFar = new Vector2f();
    }

    public void selectGameItem(GameItem[] gameItems, Camera camera) {        
        dir = camera.getViewMatrix().positiveZ(dir).negate();
        selectGameItem(gameItems, camera.getPosition(), dir);
    }
    
    protected List<Vector3f> selectGameItem(GameItem[] gameItems, Vector3f center, Vector3f dir) {
        float closestDistance = Float.POSITIVE_INFINITY;
        List<Vector3f> selectedBlocks = new ArrayList<>();
        for (GameItem gameItem : gameItems) {
            selectedBlocks = new ArrayList<>();
            for(int i = 0 ; i < 15 ; i++) {
            	for(int j = -7 ; j < 7 ; j++) {
            		for(int k = -7 ; k < 7 ; k++) {
                        min.set(j * 8,  ((i + 1) * 8) - 0.0001f, k * 8);
                        max.set((j + 1) * 8, (i + 1) * 8, (k + 1)  * 8);
            			if (Intersectionf.intersectRayAab(center, dir, min, max, nearFar) && nearFar.x < closestDistance) {
            				selectedBlocks.add(new Vector3f(j,i,k));
                        }
                    }
                }
            }
            gameItem.setSelectedBlocks(selectedBlocks);
        }
        return selectedBlocks;
    }
}
