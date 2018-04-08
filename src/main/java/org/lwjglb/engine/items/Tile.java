package org.lwjglb.engine.items;

import org.lwjglb.engine.graph.Mesh;

public class Tile extends GameItem{
	
	private int x;
	private int y;
	private int z;
	
	public Tile(Mesh mesh, int x, int y, int z) {
		super(mesh);
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
	
	@Override
	public String toString() {
		return "(" 
				+ x + "," 
				+ y + "," 
				+ z
				+ ")";
	}
}
