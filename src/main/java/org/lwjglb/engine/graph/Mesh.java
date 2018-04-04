package org.lwjglb.engine.graph;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.AABBf;
import org.lwjgl.system.MemoryUtil;
import org.lwjglb.engine.items.GameItem;

public class Mesh {

    public static final int MAX_WEIGHTS = 4;
    
    public static final int TEXTURE_SIZE = 8;

    protected final int vaoId;

    protected final List<Integer> vboIdList;

    private final int vertexCount;

    private Material material;

    private float boundingRadius;
    
    private AABBf boundaryBox;
    
    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices) {
        this(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0), null);
    }

    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices, AABBf boundaryBox) {
        this(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0), boundaryBox);
    }
    
    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights) {
    	this(positions, textCoords, normals, indices, jointIndices, weights, null);
    }

    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights, AABBf boundaryBox) {
        FloatBuffer posBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        FloatBuffer borderCoordsBuffer = null;
        FloatBuffer vecNormalsBuffer = null;
        FloatBuffer weightsBuffer = null;
        IntBuffer jointIndicesBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            calculateBoundingRadius(positions);
            
            this.boundaryBox = boundaryBox;
            
            vertexCount = indices.length;
            vboIdList = new ArrayList<>();

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            
            // Texture coordinates VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);            
            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Vertex normals VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            vecNormalsBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // Weights
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            weightsBuffer = MemoryUtil.memAllocFloat(weights.length);
            weightsBuffer.put(weights).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

            // Joint indices
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            jointIndicesBuffer = MemoryUtil.memAllocInt(jointIndices.length);
            jointIndicesBuffer.put(jointIndices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, jointIndicesBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);
            
            float[] borderCoords = new float[textCoords.length];
            for(int i = 0 ; i <= textCoords.length - 8 ; i = i + 8) {
            	borderCoords[i] = 0.1f;
            	borderCoords[i+1] = 0.1f;
            	borderCoords[i+2] = 0.1f;
            	borderCoords[i+3] = 0.9f;
            	borderCoords[i+4] = 0.9f;
            	borderCoords[i+5] = 0.9f;
            	borderCoords[i+6] = 0.9f;
            	borderCoords[i+7] = 0.1f;
            }
            vboId = glGenBuffers();
            vboIdList.add(vboId);            
            borderCoordsBuffer = MemoryUtil.memAllocFloat(borderCoords.length);
            borderCoordsBuffer.put(borderCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, borderCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(6, 2, GL_FLOAT, false, 0, 0);
            
            // Index VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (textCoordsBuffer != null) {
                MemoryUtil.memFree(textCoordsBuffer);
            }
            if (borderCoordsBuffer != null) {
                MemoryUtil.memFree(borderCoordsBuffer);
            }
            if (vecNormalsBuffer != null) {
                MemoryUtil.memFree(vecNormalsBuffer);
            }
            if (weightsBuffer != null) {
                MemoryUtil.memFree(weightsBuffer);
            }
            if (jointIndicesBuffer != null) {
                MemoryUtil.memFree(jointIndicesBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }    
    
    private void calculateBoundingRadius(float positions[]) {
        int length = positions.length;
        boundingRadius = 0;
        for(int i=0; i< length; i++) {
            float pos = positions[i];
            boundingRadius = Math.max(Math.abs(pos), boundingRadius);
        }
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public final int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public float getBoundingRadius() {
        return boundingRadius;
    }

    public void setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }
    
    public AABBf getBoundaryBox() {
		return boundaryBox;
	}
    
    public void setBoundaryBox(AABBf boundaryBox) {
		this.boundaryBox = boundaryBox;
	}

    protected void initRender() {
        Texture texture = material != null ? material.getTexture() : null;
        if (texture != null) {
            // Activate first texture bank
            glActiveTexture(GL_TEXTURE0);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        Texture normalMap = material != null ? material.getNormalMap() : null;
        if (normalMap != null) {
            // Activate second texture bank
            glActiveTexture(GL_TEXTURE1);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, normalMap.getId());
        }
        Texture borderTexture = material != null ? material.getTextureBorder() : null;
        if (borderTexture != null) {
            // Activate second texture bank
            glActiveTexture(GL_TEXTURE10);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, borderTexture.getId());
        }
        Texture borderTextureLeft = material != null ? material.getTextureBorderLeft() : null;
        if (borderTextureLeft != null) {
            // Activate second texture bank
            glActiveTexture(GL_TEXTURE11);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, borderTextureLeft.getId());
        }
        Texture borderTextureTop = material != null ? material.getTextureBorderTop() : null;
        if (borderTextureTop != null) {
            // Activate second texture bank
            glActiveTexture(GL_TEXTURE12);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, borderTextureTop.getId());
        }
        Texture borderTextureRight = material != null ? material.getTextureBorderRight() : null;
        if (borderTextureRight != null) {
            // Activate second texture bank
            glActiveTexture(GL_TEXTURE13);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, borderTextureRight.getId());
        }
        Texture borderTextureBottom = material != null ? material.getTextureBorderBottom() : null;
        if (borderTextureBottom != null) {
            // Activate second texture bank
            glActiveTexture(GL_TEXTURE14);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, borderTextureBottom.getId());
        }

        // Draw the mesh
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(6);
    }

    protected void endRender() {
        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glDisableVertexAttribArray(6);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void render() {
        initRender();

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        endRender();
    }

    public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer) {
        initRender();

        for (GameItem gameItem : gameItems) {
            if (gameItem.isInsideFrustum()) {
                // Set up data requiered by gameItem
                consumer.accept(gameItem);
                // Render this game item
                glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
            }
        }

        endRender();
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the texture
        Texture texture = material.getTexture();
        if (texture != null) {
            texture.cleanup();
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void deleteBuffers() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    protected static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    protected static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

}
