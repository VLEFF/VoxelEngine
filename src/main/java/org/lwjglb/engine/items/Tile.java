package org.lwjglb.engine.items;

import org.lwjglb.engine.graph.Mesh;

public class Tile extends GameItem{
	
	private int x;
	private int y;
	private int z;
	
	private final Board board;
	
	public Tile(Mesh mesh, Board board, int x, int y, int z) {
		super(mesh);
		this.board = board;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getZ() {
		return z;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public Board getBoard() {
		return board;
	}
	
	@Override
	public String toString() {
		return "(" 
				+ (x / board.getTileSize()) + "," 
				+ y + "," 
				+ (z / board.getTileSize()) 
				+ ")";
	}
}