package org.lwjglb.engine.items;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final List<Tile> tiles = new ArrayList<>();

    private final int width;
    
    private final int height;
    
    private final int depth;
    
    private final int tileSize;
    
    public Board(int width, int height, int depth, int tileSize) {
		super();
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.tileSize = tileSize;
	}

	public List<Tile> getTiles() {
		return tiles;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDepth() {
		return depth;
	}
	
	public int getTileSize() {
		return tileSize;
	}
}
