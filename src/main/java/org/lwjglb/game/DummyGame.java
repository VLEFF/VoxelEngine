package org.lwjglb.game;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjglb.engine.IGameLogic;
import org.lwjglb.engine.MouseInput;
import org.lwjglb.engine.Scene;
import org.lwjglb.engine.SceneLight;
import org.lwjglb.engine.Window;
import org.lwjglb.engine.graph.Camera;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Renderer;
import org.lwjglb.engine.graph.lights.DirectionalLight;
import org.lwjglb.engine.graph.lights.PointLight;
import org.lwjglb.engine.items.Board;
import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.items.SkyBox;
import org.lwjglb.engine.items.Tile;
import org.lwjglb.engine.loaders.assimp.StaticMeshesLoader;
import org.lwjglb.engine.loaders.vox.VOXLoader;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private static final float CAMERA_POS_STEP = 2;

    private float angleInc;

    private float lightAngle;

    private Hud hud;

    private boolean firstTime;

    private boolean sceneChanged;
    
    private boolean activeBorder;
    
    private MouseBoxSelectionDetector mbsd = new MouseBoxSelectionDetector();

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        hud = new Hud();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 90;
        firstTime = true;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        hud.init(window);
        scene = new Scene();

        //Mesh[] straightRoadMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/monu3.obj", "src/main/resources/models/untitled");
        /*Mesh[] teaPotMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/deer.obj", "src/main/resources/models/untitled");
        Mesh[] curvedRoadMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/untitled-1.obj", "src/main/resources/models/untitled");
        Mesh[] planeMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/untitled-2.obj", "src/main/resources/models/untitled");
        Mesh[] oldManMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/untitled-3.obj", "src/main/resources/models/untitled");*/
        

        Board board = VOXLoader.loadBoard("src/main/resources/models/untitled/monu3-bis.vox");
        scene.setBoard(board);

        List<GameItem> cubes = new ArrayList<>();
        
        /*GameItem straightRoad = new GameItem(straightRoadMesh);
        straightRoad.setPosition(0, 0, 0);
        cubes.add(straightRoad);*/
        
        scene.setGameItems(cubes.toArray(new GameItem[cubes.size()]));

        // Shadows
        scene.setRenderShadows(true);

        float skyBoxScale = 100.0f;
        SkyBox skyBox = new SkyBox("src/main/resources/models/skybox.obj", new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Setup Lights
        setupLights();

        camera.getPosition().x = 17.0f;
        camera.getPosition().y =  90.0f;
        camera.getPosition().z = 30.0f;
        camera.getRotation().x = 0f;
        camera.getRotation().y = 0f;
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 0.5f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        sceneLight.setDirectionalLight(directionalLight);
        
        sceneLight.setPointLightList( new PointLight[] {});
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        sceneChanged = false;
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            sceneChanged = true;
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            sceneChanged = true;
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            sceneChanged = true;
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            sceneChanged = true;
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            sceneChanged = true;
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            sceneChanged = true;
            cameraInc.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            sceneChanged = true;
            angleInc -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            sceneChanged = true;
            angleInc += 0.05f;
        } else {
            angleInc = 0;
        }
        if (window.isKeyPressed(GLFW_KEY_C)) {
        	activeBorder = true;
        } else if (window.isKeyPressed(GLFW_KEY_V)) {
        	activeBorder = false;
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput, Window window) {
        if (mouseInput.isRightButtonPressed()) {
            // Update camera based on mouse            
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            sceneChanged = true;
        }

        scene.setRenderBorder(activeBorder);
        
        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        lightAngle += angleInc;
        
        float zValue = (float) Math.cos(Math.toRadians(lightAngle));
        float yValue = (float) Math.sin(Math.toRadians(lightAngle));
        Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();

        // Update view matrix
        camera.updateViewMatrix();
        if(scene.getBoard() != null) {
	    	boolean selected = mbsd.selectGameItem(scene.getBoard(), window, mouseInput.getCurrentPos(), camera);
	    	hud.setText(selected + "");
        }
    }

    @Override
    public void render(Window window, MouseInput mouseInput) {
        if (firstTime) {
            sceneChanged = true;
            firstTime = false;
        }
        renderer.render(window, mouseInput, camera, scene, sceneChanged);
        hud.render(window);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        scene.cleanup();
        if (hud != null) {
            hud.cleanup();
        }
    }
}
