package org.lwjglb.engine.loaders.vox;

import java.io.File;

public class MainDebugVoxParser {

  public static void main(String[] args) throws Exception {
    try {
      File f = new File("src/toto.vox");
      VoxelFileReader vfr = new VoxelFileReader();
      Vox vox = vfr.readVox(f);
      System.out.println(vox.getPalette());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
