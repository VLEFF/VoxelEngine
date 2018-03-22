package org.lwjglb.game;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;

import org.joml.Vector3f;
import org.lwjgl.nanovg.NVGColor;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.NULL;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.Window;

public class Hud {

    private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;

    private List<Vector3f> selectedBlocks;

    public void init(Window window) throws Exception {
        this.vg = window.getOptions().antialiasing ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
        if (this.vg == NULL) {
            throw new Exception("Could not init nanovg");
        }

        fontBuffer = Utils.ioResourceToByteBuffer("/fonts/OpenSans-Bold.ttf", 150 * 1024);
        int font = nvgCreateFontMem(vg, FONT_NAME, fontBuffer, 0);
        if (font == -1) {
            throw new Exception("Could not add font");
        }
        colour = NVGColor.create();
    }

    public void render(Window window) {
        nvgBeginFrame(vg, window.getWidth(), window.getHeight(), 1);

        // Clicks Text
        nvgFontSize(vg, 25.0f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgFillColor(vg, rgba(0x23, 0xa1, 0xf1, 255, colour));
        for(int i = 0 ; i < selectedBlocks.size() ; i++) {
        	Vector3f selectedBlock = selectedBlocks.get(i);
            nvgText(vg, 50, 50 + (i * 25), (selectedBlock.x + 1) + "," +  selectedBlock.y + "," +  (selectedBlock.z + 1));
        }


        nvgEndFrame(vg);

        // Restore state
        window.restoreState();
    }
    
    public List<Vector3f> getSelectedBlocks() {
		return selectedBlocks;
	}
    
    public void setSelectedBlocks(List<Vector3f> selectedBlocks) {
		this.selectedBlocks = selectedBlocks;
	}

    private NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);

        return colour;
    }

    public void cleanup() {
        nvgDelete(vg);
    }
}
