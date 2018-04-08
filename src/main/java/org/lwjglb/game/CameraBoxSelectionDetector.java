package org.lwjglb.game;

import org.joml.AABBf;
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
            AABBf boundaryBox = gameItem.getMesh().getBoundaryBox();
            if(boundaryBox != null) {
            	min.set(gameItem.getPosition());
                max.set(gameItem.getPosition());
                min.add(boundaryBox.minX * gameItem.getScale(), boundaryBox.minY * gameItem.getScale(), boundaryBox.minZ * gameItem.getScale());
                max.add(boundaryBox.maxX * gameItem.getScale(), boundaryBox.maxY * gameItem.getScale(), boundaryBox.maxZ * gameItem.getScale());
                if (Intersectionf.intersectRayAab(center, dir, min, max, nearFar) && nearFar.x < closestDistance) {
                    closestDistance = nearFar.x;
                    hoveredGameItem = gameItem;
                }
            }
        }

        if (hoveredGameItem != null) {
        	hoveredGameItem.setHovered(true);
        	hovered = true;
        }
        return hovered;
    }
    
    protected Tile hoverGameItem(Board board, Vector3f center, Vector3f dir) {
        Tile hoveredTile = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (Tile tile : board.getTiles()) {
        	tile.setHovered(false);
        	tile.setHighlighted(false);
        	Vector3f tilePosition = tile.getPosition();
            min.set(tile.getX() * board.getTileSize(), tile.getY() - 4, tile.getZ() * board.getTileSize());
            max.set((tile.getX() * board.getTileSize()) + board.getTileSize(), tile.getY(), (tile.getZ() * board.getTileSize()) + board.getTileSize());
            if (Intersectionf.intersectRayAab(center, dir, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                hoveredTile = tile;
            }
        }

        if (hoveredTile != null) {
        	hoveredTile.setHovered(true);
        }
        return hoveredTile;
	}
}
