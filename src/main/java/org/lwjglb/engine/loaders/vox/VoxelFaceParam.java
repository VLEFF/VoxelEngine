package org.lwjglb.engine.loaders.vox;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.function.BiPredicate;

enum VoxelFaceParam {
  RIGHT_FACE(
      new float[][]{{1f, 0f, 1f}, {1f, 0f, 0f}, {1f, 1f, 0f}, {1f, 1f, 1f}},
      new Vector3f(1,0,0),
      (voxPositions, voxModel) -> voxPositions.x == voxModel.getWidth() - 1 || voxModel.getMatrice()[voxPositions.x + 1][voxPositions.y][voxPositions.z] == null),

  LEFT_FACE(
      new float[][]{{0f, 1f, 1f}, {0f, 1f, 0f}, {0f, 0f, 0f}, {0f, 0f, 1f}},
      new Vector3f(-1,0,0),
      (voxPositions, voxModel) -> voxPositions.x == 0 || voxModel.getMatrice()[voxPositions.x - 1][voxPositions.y][voxPositions.z] == null),

  TOP_FACE(
      new float[][]{{0f, 1f, 1f}, {1f, 1f, 1f}, {1f, 1f, 0f}, {0f, 1f, 0f}},
      new Vector3f(0,1,0),
      (voxPositions, voxModel) -> voxPositions.y == voxModel.getHeight() - 1 || voxModel.getMatrice()[voxPositions.x][voxPositions.y + 1][voxPositions.z] == null),

  BOTTOM_FACE(
      new float[][]{{0f, 0f, 0f}, {1f, 0f, 0f}, {1f, 0f, 1f}, {0f, 0f, 1f}},
      new Vector3f(0,-1,0),
      (voxPositions, voxModel) -> voxPositions.y == 0 || voxModel.getMatrice()[voxPositions.x][voxPositions.y - 1][voxPositions.z] == null),

  FRONT_FACE(
      new float[][]{{1f, 0f, 1f}, {1f, 1f, 1f}, {0f, 1f, 1f}, {0f, 0f, 1f}},
      new Vector3f(0,0,1),
      (voxPositions, voxModel) -> voxPositions.z == voxModel.getDepth() - 1 || voxModel.getMatrice()[voxPositions.x][voxPositions.y][voxPositions.z + 1] == null),

  BACK_FACE(
      new float[][]{{0f, 0f, 0f}, {0f, 1f, 0f}, {1f, 1f, 0f}, {1f, 0f, 0f}},
      new Vector3f(0,0,-1),
      (voxPositions, voxModel) -> voxPositions.z == 0 || voxModel.getMatrice()[voxPositions.x][voxPositions.y][voxPositions.z - 1] == null);

  private float[][] positions;
  private Vector3f normal;
  private BiPredicate<Vector3i,VoxModel> filter;

  VoxelFaceParam(float[][] positions, Vector3f normal, BiPredicate<Vector3i,VoxModel> filter) {
    this.positions = positions;
    this.normal = normal;
    this.filter = filter;
  }

  public float[][] getPositions() {
    return positions;
  }

  public Vector3f getNormal() {
    return normal;
  }

  public BiPredicate<Vector3i, VoxModel> getFilter() {
    return filter;
  }
}
