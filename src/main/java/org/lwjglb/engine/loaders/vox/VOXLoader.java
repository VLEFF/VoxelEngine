package org.lwjglb.engine.loaders.vox;

import java.io.File;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.items.Board;
import org.lwjglb.engine.loaders.vox.converter.VoxToBoardConverter;
import org.lwjglb.engine.loaders.vox.converter.VoxToGameObjectsConverter;

public class VOXLoader {

    public static Mesh[] loadMesh(String fileName) throws Exception {
      VoxToGameObjectsConverter vfr = new VoxToGameObjectsConverter();
    	return vfr.read(new File(fileName));
    }
    
    public static Board loadBoard(String fileName, int tileSize) throws Exception {
        VoxToBoardConverter vfr = new VoxToBoardConverter();
    	return vfr.read(new File(fileName), tileSize);
    }
}
