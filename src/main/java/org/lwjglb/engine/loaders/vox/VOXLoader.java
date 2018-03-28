package org.lwjglb.engine.loaders.vox;

import java.io.File;
import org.lwjglb.engine.graph.Mesh;

public class VOXLoader {

    public static Mesh loadMesh(String fileName) throws Exception {
        return loadMesh(fileName, 1);
    }

    public static Mesh loadMesh(String fileName, int instances) throws Exception {
    	VoxelFileReader vfr = new VoxelFileReader();
    	return vfr.read(new File(fileName));
    }
}
