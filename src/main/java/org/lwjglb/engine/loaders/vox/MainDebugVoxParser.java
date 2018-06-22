package org.lwjglb.engine.loaders.vox;

import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.loaders.vox.bean.Layer;
import org.lwjglb.engine.loaders.vox.bean.Vox;
import org.lwjglb.engine.loaders.vox.converter.VoxConverter;
import org.lwjglb.engine.loaders.vox.converter.VoxToBoardConverter;
import org.lwjglb.engine.loaders.vox.converter.VoxToGameObjectsConverter;
import org.lwjglb.engine.loaders.vox.reader.VoxelFileReader;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MainDebugVoxParser {

  public static void main(String[] args) throws Exception {
    try {
      File f = new File("src/main/resources/models/untitled/toto.vox");
      VoxToGameObjectsConverter conv = new VoxToGameObjectsConverter();
      Map<Layer, List<GameItem>> layers = conv.read(f);
      System.out.println(layers);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
