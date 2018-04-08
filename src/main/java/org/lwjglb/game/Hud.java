package org.lwjglb.game;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.lwjglb.engine.items.Tile;

public class Hud {

    private static final String FONT_NAME = "BOLD";

    private long vg;

    private NVGColor colour;

    private ByteBuffer fontBuffer;
    
    private String text = "";
    
    private long frustrumShown;
    private long frustrumMax;

    private final List<Tile> hoveredTiles = new ArrayList<>();

    private final List<Tile> selectedTiles = new ArrayList<>();

    private final List<Tile> highlightedTiles = new ArrayList<>();


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
        nvgText(vg, 50, 25, "Fustrum culling: " + frustrumShown + "/" + frustrumMax + " item shown");
        nvgText(vg, 50, 50, "Hovered:");
        for(int i = 0 ; i < hoveredTiles.size() ; i++) {
            nvgText(vg, 50, 75 + i * 25, hoveredTiles.get(i).toString());
        }

        nvgText(vg, 150, 50, "Selected:");
        for(int i = 0 ; i < selectedTiles.size() ; i++) {
            nvgText(vg, 150, 75 + i * 25, selectedTiles.get(i).toString());
        }

        nvgText(vg, 250, 50, "Highlighted");
        for(int i = 0 ; i < highlightedTiles.size() ; i++) {
            nvgText(vg, 250, 75 + i * 25, highlightedTiles.get(i).toString());
        }


        nvgEndFrame(vg);

        // Restore state
        window.restoreState();
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
    
    public String getText() {
		return text;
	}
    
    public void setText(String text) {
		this.text = text;
	}    

	public long getFrustrumShown() {
		return frustrumShown;
	}

	public void setFrustrumShown(long frustrumShown) {
		this.frustrumShown = frustrumShown;
	}

	public long getFrustrumMax() {
		return frustrumMax;
	}

	public void setFrustrumMax(long l) {
		this.frustrumMax = l;
	}

	public List<Tile> getHoveredTiles() {
		return hoveredTiles;
	}
	
	public List<Tile> getSelectedTiles() {
		return selectedTiles;
	}
	
	public List<Tile> getHighlightedTiles() {
		return highlightedTiles;
	}
}
