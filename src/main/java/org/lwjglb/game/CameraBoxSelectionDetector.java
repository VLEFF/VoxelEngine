package org.lwjglb.game;

import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjglb.engine.graph.Camera;
import org.lwjglb.engine.items.Board;
import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.items.Tile;

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
        hoverGameItem(gameItems, camera.getPosition(), dir);
    }
    
    protected boolean hoverGameItem(GameItem[] gameItems, Vector3f center, Vector3f dir) {
        boolean hovered = false;
        GameItem hoveredGameItem = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (GameItem gameItem : gameItems) {
            gameItem.setHovered(false);
            min.set(gameItem.getPosition());
            max.set(gameItem.getPosition());
            min.add(-gameItem.getScale(), -gameItem.getScale(), -gameItem.getScale());
            max.add(gameItem.getScale(), gameItem.getScale(), gameItem.getScale());
            if (Intersectionf.intersectRayAab(center, dir, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                hoveredGameItem = gameItem;
            }
        }

        if (hoveredGameItem != null) {
        	hoveredGameItem.setHovered(true);
        	hovered = true;
        }
        return hovered;
    }
    
    protected boolean hoverGameItem(Board board, Vector3f center, Vector3f dir) {
    	boolean hovered = false;
        Tile hoveredTile = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (Tile tile : board.getTiles()) {
        	tile.setHovered(false);
            min.set(tile.getX(), tile.getY() - 4, tile.getZ());
            max.set(tile.getX() + board.getTileSize(), tile.getY(), tile.getZ() + board.getTileSize());
            if (Intersectionf.intersectRayAab(center, dir, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                hoveredTile = tile;
            }
        }

        if (hoveredTile != null) {
        	hoveredTile.setHovered(true);
        	hovered = true;
        }
        return hovered;
	}
}
