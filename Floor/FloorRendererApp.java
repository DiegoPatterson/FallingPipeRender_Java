package com.pipefalling;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class FloorRendererApp extends ApplicationAdapter {
    private static final String FLOOR_MODEL_PATH = "Assets/Models/floor.obj";
    private static final String FLOOR_TEXTURE_PATH = "Assets/Textures/Tile-texture.jpg";

    private PerspectiveCamera camera;
    private CameraInputController cameraController;
    private Environment environment;
    private ModelBatch modelBatch;

    private Model floorModel;
    private ModelInstance floorInstance;
    private Texture floorTexture;

    @Override
    public void create() {
        modelBatch = new ModelBatch();

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(8f, 7f, 8f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -0.6f, -1f, -0.4f));

        floorModel = loadFloorModelOrFallback();
        floorInstance = new ModelInstance(floorModel);
    }

    private Model loadFloorModelOrFallback() {
        floorTexture = loadFloorTexture();

        FileHandle modelFile = Gdx.files.internal(FLOOR_MODEL_PATH);
        if (modelFile.exists()) {
            ObjLoader objLoader = new ObjLoader();
            Model model = objLoader.loadModel(modelFile);
            applyFloorTexture(model, floorTexture);
            return model;
        }

        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));

        if (floorTexture != null) {
            material.set(TextureAttribute.createDiffuse(floorTexture));
        }

        return modelBuilder.createBox(
            20f,
            0.1f,
            20f,
            material,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
        );
    }

    private Texture loadFloorTexture() {
        FileHandle textureFile = Gdx.files.internal(FLOOR_TEXTURE_PATH);
        if (!textureFile.exists()) {
            return null;
        }

        Texture texture = new Texture(textureFile);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }

    private void applyFloorTexture(Model model, Texture texture) {
        if (texture == null) {
            return;
        }
        for (Material material : model.materials) {
            material.set(TextureAttribute.createDiffuse(texture));
            material.remove(ColorAttribute.Diffuse);
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void render() {
        cameraController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.08f, 0.1f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(floorInstance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        if (floorTexture != null) {
            floorTexture.dispose();
        }
        if (floorModel != null) {
            floorModel.dispose();
        }
        if (modelBatch != null) {
            modelBatch.dispose();
        }
    }
}
