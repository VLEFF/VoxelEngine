package org.lwjglb.game;

import org.joml.Quaternionf;
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
import org.lwjglb.engine.graph.weather.Fog;
import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.items.SkyBox;
import org.lwjglb.engine.loaders.assimp.StaticMeshesLoader;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private static final float CAMERA_POS_STEP = 2;

    private float angleInc;

    private float lightAngle;

    private boolean firstTime;

    private boolean sceneChanged;
    
    private boolean activeBorder;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 90;
        firstTime = true;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        scene = new Scene();

        Mesh[] straightRoadMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/untitled-0.obj", "src/main/resources/models/untitled");
        Mesh[] curvedRoadMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/untitled-1.obj", "src/main/resources/models/untitled");
        Mesh[] planeMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/untitled-2.obj", "src/main/resources/models/untitled");
        Mesh[] oldManMesh = StaticMeshesLoader.load("src/main/resources/models/untitled/untitled-3.obj", "src/main/resources/models/untitled");
        

        List<GameItem> cubes = new ArrayList<>();
        float f = (float) Math.sqrt(2);
       
        GameItem straightRoad = new GameItem(straightRoadMesh);
        straightRoad.setPosition(0, 0, 0);
        straightRoad.setRotation(new Quaternionf(0, 1, 0, 0));
        cubes.add(straightRoad);
        straightRoad = new GameItem(straightRoadMesh);
        straightRoad.setPosition(126, 0, 126);
        straightRoad.setRotation(new Quaternionf(0, f/2, 0,  f/2));
        cubes.add(straightRoad);
        straightRoad = new GameItem(straightRoadMesh);
        straightRoad.setPosition(0, 0, 252);
        cubes.add(straightRoad);
        straightRoad = new GameItem(straightRoadMesh);
        straightRoad.setPosition(-126, 0, 126);
        straightRoad.setRotation(new Quaternionf(0, f/2, 0,  -f/2));
        cubes.add(straightRoad);
        
        
        GameItem curvedRoad = new GameItem(curvedRoadMesh);
        curvedRoad.setPosition(126, 0, 0);
        curvedRoad.setRotation(new Quaternionf(0, f/2, 0,  f/2));
        cubes.add(curvedRoad);
        curvedRoad = new GameItem(curvedRoadMesh);
        curvedRoad.setPosition(126, 0, 252);
        cubes.add(curvedRoad);
        curvedRoad = new GameItem(curvedRoadMesh);
        curvedRoad.setPosition(-126, 0, 252);
        curvedRoad.setRotation(new Quaternionf(0, f/2, 0,  -f/2));
        cubes.add(curvedRoad);
        curvedRoad = new GameItem(curvedRoadMesh);
        curvedRoad.setPosition(-126, 0, 0);
        curvedRoad.setRotation(new Quaternionf(0, 1, 0,  0));
        cubes.add(curvedRoad);
        
        GameItem plane = new GameItem(planeMesh);
        plane.setPosition(0, 0, 126);
        cubes.add(plane);
        
        GameItem oldMan = new GameItem(oldManMesh);
        oldMan.setPosition(0, 1, 0);
        cubes.add(oldMan);
        
        scene.setGameItems(cubes.toArray(new GameItem[cubes.size()]));

        // Shadows
        scene.setRenderShadows(true);

        // Setup Lights
        setupLights();

        camera.getPosition().x = -17.0f;
        camera.getPosition().y =  17.0f;
        camera.getPosition().z = -30.0f;
        camera.getRotation().x = 20.0f;
        camera.getRotation().y = 140.f;
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
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
        if (lightAngle < 0) {
            lightAngle = 0;
        } else if (lightAngle > 180) {
            lightAngle = 180;
        }
        float zValue = (float) Math.cos(Math.toRadians(lightAngle));
        float yValue = (float) Math.sin(Math.toRadians(lightAngle));
        Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();

        // Update view matrix
        camera.updateViewMatrix();
    }

    @Override
    public void render(Window window) {
        if (firstTime) {
            sceneChanged = true;
            firstTime = false;
        }
        renderer.render(window, camera, scene, sceneChanged);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();

        scene.cleanup();
    }
}
