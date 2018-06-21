package org.lwjglb.engine.loaders.vox;

import org.lwjglb.engine.loaders.vox.bean.Vox;
import org.lwjglb.engine.loaders.vox.reader.VoxelFileReader;

import java.io.File;

public class MainDebugVoxParser {

  public static void main(String[] args) throws Exception {
    try {
      File f = new File("src/main/resources/models/untitled/toto.vox");
      VoxelFileReader vfr = new VoxelFileReader();
      Vox vox = vfr.readVox(f);
      System.out.println(vox.getPalette());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
