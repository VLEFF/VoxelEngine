package org.lwjglb.engine.loaders.vox;

import java.io.File;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.items.Board;

public class VOXLoader {

    public static Mesh[] loadMesh(String fileName) throws Exception {
    	SimpleVoxelFileReader vfr = new SimpleVoxelFileReader();
    	return vfr.read(new File(fileName));
    }
    
    public static Board loadBoard(String fileName, int tileSize) throws Exception {
    	BoardVoxelFileReader vfr = new BoardVoxelFileReader();
    	return vfr.read(new File(fileName), tileSize);
    }
}
