package com.pipefalling;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class FloorRendererApp extends ApplicationAdapter {
    private static final String FLOOR_MODEL_PATH = "Assets/Models/floor.obj";
    private static final String FLOOR_TEXTURE_PATH = "Assets/Textures/Tile-texture.jpg";
    private static final String TABLE_MODEL_PATH = "Assets/Models/table.obj";
    private static final String TABLE_TEXTURE_PATH = "Assets/Textures/Wood-texture.jpg";
    private static final String PIPE_MODEL_PATH = "Assets/Models/pipe.obj";
    private static final String PIPE_TEXTURE_PATH = "Assets/Textures/photo-metal-texture-pattern.jpg";
    private static final String PIPE_IMPACT_SOUND_WAV_PATH = "Assets/Sounds/jixaw-metal-pipe-falling-sound.wav";
    private static final String PIPE_IMPACT_SOUND_MP3_PATH = "Assets/Sounds/jixaw-metal-pipe-falling-sound.mp3";

    private static final float FALLBACK_PIPE_RADIUS = 0.09f;
    private static final float FALLBACK_PIPE_LENGTH = 1.2f;
    private static final float PIPE_VISUAL_ROTATION_X_DEG = 90f;
    private static final float PIPE_SHADOW_ROTATION_Y_DEG = 90f;
    private static final float TABLE_EDGE_X = 1.5f;
    private static final float TABLE_MARGIN_X = 0.03f;
    private static final float TABLE_TOP_Y = 0.89f;
    private static final float FLOOR_TOP_Y = 0.05f;
    private static final float GRAVITY = -6.2f;
    private static final float SUN_DISTANCE = 16f;
    private static final float SHADOW_EPSILON = 0.0025f;
    private static final int FLOOR_SHADOW_GRID_X = 64;
    private static final int FLOOR_SHADOW_GRID_Z = 48;
    private static final int TABLE_SHADOW_GRID_X = 48;
    private static final int TABLE_SHADOW_GRID_Z = 32;
    private static final float SHADOW_ALPHA_FLOOR = 0.24f;
    private static final float SHADOW_ALPHA_TABLE = 0.20f;
    private static final float TABLE_FLOOR_SHADOW_ALPHA = 0.18f;
    private static final float TABLE_MIN_X = -1.5f;
    private static final float TABLE_MAX_X = 1.5f;
    private static final float TABLE_MIN_Z = -1f;
    private static final float TABLE_MAX_Z = 1f;
    private static final float TABLE_MIN_Y = FLOOR_TOP_Y;
    private static final float TABLE_MAX_Y = TABLE_TOP_Y;
    private static final float CAMERA_MOVE_SPEED = 4.0f;

    private PerspectiveCamera camera;
    private CameraInputController cameraController;
    private Environment environment;
    private ModelBatch modelBatch;
    private ImmediateModeRenderer20 shadowRenderer;

    private Model floorModel;
    private ModelInstance floorInstance;
    private Texture floorTexture;

    private Model tableModel;
    private ModelInstance tableInstance;
    private Texture tableTexture;

    private Model pipeModel;
    private ModelInstance pipeInstance;
    private Texture pipeTexture;

    private Model sunModel;
    private ModelInstance sunInstance;
    private Sound pipeImpactSound;

    private final Vector3 sunDirectionToScene = new Vector3(-0.6f, -1f, -0.4f).nor();
    private final Vector3 pointToSunDirection = new Vector3();
    private final Vector3 pipeCenter = new Vector3();
    private final Vector3 shadowOriginLocal = new Vector3();
    private final Vector3 shadowDirectionLocal = new Vector3();
    private final Vector3 cameraMove = new Vector3();
    private final Vector3 cameraForward = new Vector3();
    private final Vector3 cameraRight = new Vector3();

    private boolean pipeLoadedFromObj;
    private float pipeBaseRotationZ;
    private float pipeLocalMinX;
    private float pipeLocalMaxX;
    private float pipeLocalMinY;
    private float pipeLocalMaxY;
    private float pipeLocalMinZ;
    private float pipeLocalMaxZ;
    private float pipeLocalCenterX;
    private float pipeLocalCenterY;
    private float pipeLocalCenterZ;
    private float pipeHalfLength;
    private float pipeShadowRadius;
    private float pipePoseMinY;
    private float pipePoseMaxX;

    private float animationTimer;
    private float pipeX;
    private float pipeY;
    private float pipeZ;
    private float pipeVelocityX;
    private float pipeVelocityY;
    private float pipeTextureUOffset;
    private boolean pipeFalling;
    private boolean pipeLanded;
    private boolean pipeImpactPlayed;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        shadowRenderer = new ImmediateModeRenderer20(40000, false, true, 0);

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(8f, 7f, 8f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        cameraController = new CameraInputController(camera);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.R) {
                    resetViewAndAnimation();
                    return true;
                }
                if (keycode == Input.Keys.SPACE) {
                    replayAnimation();
                    return true;
                }
                return false;
            }
        });
        inputMultiplexer.addProcessor(cameraController);
        Gdx.input.setInputProcessor(inputMultiplexer);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.62f, 0.62f, 0.62f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 0.97f, sunDirectionToScene.x, sunDirectionToScene.y, sunDirectionToScene.z));

        floorModel = loadFloorModelOrFallback();
        floorInstance = new ModelInstance(floorModel);

        tableModel = loadTableModelOrFallback();
        tableInstance = new ModelInstance(tableModel);
        tableInstance.transform.setToTranslation(0f, 0.05f, 0f);

        pipeModel = loadPipeModelOrFallback();
        configurePipeGeometry();
        pipeInstance = new ModelInstance(pipeModel);

        sunModel = createSunModel();
        sunInstance = new ModelInstance(sunModel);
        positionSun();

        pipeImpactSound = loadPipeImpactSound();

        resetPipeState();
    }

    private Sound loadPipeImpactSound() {
        FileHandle wavFile = Gdx.files.internal(PIPE_IMPACT_SOUND_WAV_PATH);
        if (wavFile.exists()) {
            return Gdx.audio.newSound(wavFile);
        }

        FileHandle mp3File = Gdx.files.internal(PIPE_IMPACT_SOUND_MP3_PATH);
        if (mp3File.exists()) {
            return Gdx.audio.newSound(mp3File);
        }

        return null;
    }

    private void replayAnimation() {
        resetPipeState();
    }

    private void resetViewAndAnimation() {
        camera.position.set(8f, 7f, 8f);
        camera.lookAt(0f, 0f, 0f);
        camera.up.set(Vector3.Y);
        camera.update();
        cameraController.target.set(0f, 0f, 0f);
        replayAnimation();
    }

    private void updateCameraKeyboardMovement(float delta) {
        cameraMove.setZero();

        cameraForward.set(camera.direction.x, 0f, camera.direction.z);
        if (cameraForward.len2() > 1e-6f) {
            cameraForward.nor();
        }

        cameraRight.set(cameraForward).crs(Vector3.Y).nor();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            cameraMove.add(cameraForward);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            cameraMove.sub(cameraForward);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            cameraMove.sub(cameraRight);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            cameraMove.add(cameraRight);
        }

        if (cameraMove.len2() > 1e-6f) {
            cameraMove.nor().scl(CAMERA_MOVE_SPEED * delta);
            camera.position.add(cameraMove);
            cameraController.target.add(cameraMove);
            camera.update();
        }
    }

    private Model createSunModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(new Color(1f, 0.93f, 0.66f, 1f)));
        return modelBuilder.createSphere(
            0.9f,
            0.9f,
            0.9f,
            20,
            20,
            material,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
    }

    private void positionSun() {
        pointToSunDirection.set(sunDirectionToScene).scl(-SUN_DISTANCE);
        sunInstance.transform.setToTranslation(pointToSunDirection.x, pointToSunDirection.y + 6f, pointToSunDirection.z);
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

    private Model loadTableModelOrFallback() {
        tableTexture = loadTableTexture();

        FileHandle modelFile = Gdx.files.internal(TABLE_MODEL_PATH);
        if (modelFile.exists()) {
            ObjLoader objLoader = new ObjLoader();
            Model model = objLoader.loadModel(modelFile);
            applyTableTexture(model, tableTexture);
            return model;
        }

        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(new Color(0.55f, 0.38f, 0.24f, 1f)));
        if (tableTexture != null) {
            material.set(TextureAttribute.createDiffuse(tableTexture));
        }

        modelBuilder.begin();

        modelBuilder.part(
            "tableTop",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material
        ).box(0f, 0.78f, 0f, 3f, 0.12f, 2f);

        float legHeight = 0.78f;
        float legSize = 0.15f;
        float xOffset = 1.35f;
        float zOffset = 0.85f;

        modelBuilder.part(
            "legFL",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material
        ).box(-xOffset, legHeight / 2f, -zOffset, legSize, legHeight, legSize);

        modelBuilder.part(
            "legFR",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material
        ).box(xOffset, legHeight / 2f, -zOffset, legSize, legHeight, legSize);

        modelBuilder.part(
            "legBL",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material
        ).box(-xOffset, legHeight / 2f, zOffset, legSize, legHeight, legSize);

        modelBuilder.part(
            "legBR",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material
        ).box(xOffset, legHeight / 2f, zOffset, legSize, legHeight, legSize);

        return modelBuilder.end();
    }

    private Texture loadTableTexture() {
        FileHandle textureFile = Gdx.files.internal(TABLE_TEXTURE_PATH);
        if (!textureFile.exists()) {
            return null;
        }

        Texture texture = new Texture(textureFile);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }

    private void applyTableTexture(Model model, Texture texture) {
        for (Material material : model.materials) {
            // Force opaque table surfaces in case OBJ/MTL includes alpha blending.
            material.remove(BlendingAttribute.Type);
            if (texture != null) {
                material.set(TextureAttribute.createDiffuse(texture));
                material.remove(ColorAttribute.Diffuse);
            } else if (!material.has(ColorAttribute.Diffuse)) {
                material.set(ColorAttribute.createDiffuse(new Color(0.55f, 0.38f, 0.24f, 1f)));
            }
        }
    }

    private Model loadPipeModelOrFallback() {
        pipeTexture = loadPipeTexture();

        FileHandle modelFile = Gdx.files.internal(PIPE_MODEL_PATH);
        if (modelFile.exists()) {
            pipeLoadedFromObj = true;
            ObjLoader objLoader = new ObjLoader();
            Model model = objLoader.loadModel(modelFile);
            applyPipeTexture(model, pipeTexture);
            return model;
        }

        pipeLoadedFromObj = false;

        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(new Color(0.73f, 0.73f, 0.75f, 1f)));
        if (pipeTexture != null) {
            material.set(TextureAttribute.createDiffuse(pipeTexture));
        }

        return modelBuilder.createCylinder(
            FALLBACK_PIPE_RADIUS * 2f,
            FALLBACK_PIPE_LENGTH,
            FALLBACK_PIPE_RADIUS * 2f,
            24,
            material,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
        );
    }

    private void configurePipeGeometry() {
        pipeBaseRotationZ = pipeLoadedFromObj ? 0f : -90f;

        BoundingBox bounds = new BoundingBox();
        pipeModel.calculateBoundingBox(bounds);
        bounds.mul(new Matrix4().idt().rotate(Vector3.Z, pipeBaseRotationZ));

        BoundingBox poseBounds = new BoundingBox();
        pipeModel.calculateBoundingBox(poseBounds);
        poseBounds.mul(new Matrix4().idt().rotate(Vector3.Z, pipeBaseRotationZ).rotate(Vector3.X, PIPE_VISUAL_ROTATION_X_DEG));

        pipeLocalMinX = bounds.min.x;
        pipeLocalMaxX = bounds.max.x;
        pipeLocalMinY = bounds.min.y;
        pipeLocalMaxY = bounds.max.y;
        pipeLocalMinZ = bounds.min.z;
        pipeLocalMaxZ = bounds.max.z;

        pipeLocalCenterX = (pipeLocalMinX + pipeLocalMaxX) * 0.5f;
        pipeLocalCenterY = (pipeLocalMinY + pipeLocalMaxY) * 0.5f;
        pipeLocalCenterZ = (pipeLocalMinZ + pipeLocalMaxZ) * 0.5f;

        pipeHalfLength = (pipeLocalMaxX - pipeLocalMinX) * 0.5f;
        pipeShadowRadius = Math.max((pipeLocalMaxY - pipeLocalMinY) * 0.5f, (pipeLocalMaxZ - pipeLocalMinZ) * 0.5f);

        if (pipeHalfLength < 0.01f) {
            pipeHalfLength = FALLBACK_PIPE_LENGTH * 0.5f;
        }
        if (pipeShadowRadius < 0.01f) {
            pipeShadowRadius = FALLBACK_PIPE_RADIUS;
        }

        pipePoseMinY = poseBounds.min.y;
        pipePoseMaxX = poseBounds.max.x;
    }

    private Texture loadPipeTexture() {
        FileHandle textureFile = Gdx.files.internal(PIPE_TEXTURE_PATH);
        if (!textureFile.exists()) {
            return null;
        }

        Texture texture = new Texture(textureFile);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }

    private void applyPipeTexture(Model model, Texture texture) {
        for (Material material : model.materials) {
            if (texture != null) {
                material.set(TextureAttribute.createDiffuse(texture));
                material.remove(ColorAttribute.Diffuse);
            } else if (!material.has(ColorAttribute.Diffuse)) {
                material.set(ColorAttribute.createDiffuse(new Color(0.73f, 0.73f, 0.75f, 1f)));
            }
        }
    }

    private void resetPipeState() {
        animationTimer = 0f;
        pipeX = -0.9f;
        pipeY = TABLE_TOP_Y - pipePoseMinY;
        pipeZ = 0f;
        pipeVelocityX = 0f;
        pipeVelocityY = 0f;
        pipeTextureUOffset = 0f;
        pipeFalling = false;
        pipeLanded = false;
        pipeImpactPlayed = false;
        updatePipeTextureAnimation();
        updatePipeTransform();
    }

    private void updatePipeAnimation(float delta) {
        animationTimer += delta;

        if (!pipeFalling && !pipeLanded) {
            // Keep a fixed orientation while it slides so it does not clip through the tabletop.
            float slideDistance = 0.45f * delta;
            pipeX += slideDistance;
            advancePipeTextureRoll(slideDistance);
            pipeY = TABLE_TOP_Y - pipePoseMinY;
            float pipeFrontX = pipeX + pipePoseMaxX;
            if (pipeFrontX >= TABLE_EDGE_X - TABLE_MARGIN_X) {
                pipeFalling = true;
                pipeVelocityX = 1.25f;
                pipeVelocityY = 0f;
            }
        }

        if (pipeFalling) {
            pipeVelocityY += GRAVITY * delta;
            pipeX += pipeVelocityX * delta;
            pipeY += pipeVelocityY * delta;

            float floorRestY = FLOOR_TOP_Y - pipePoseMinY;
            if (pipeY <= floorRestY) {
                pipeY = floorRestY;
                pipeVelocityY = 0f;
                pipeFalling = false;
                pipeLanded = true;
                if (!pipeImpactPlayed && pipeImpactSound != null) {
                    pipeImpactSound.play(0.8f);
                    pipeImpactPlayed = true;
                }
            }
        }

        updatePipeTransform();
    }

    private void advancePipeTextureRoll(float travelDistance) {
        float radius = Math.max(pipeShadowRadius, 0.01f);
        float circumference = (float) (Math.PI * 2.0 * radius);
        float turns = travelDistance / circumference;
        pipeTextureUOffset += turns;
        pipeTextureUOffset -= (float) Math.floor(pipeTextureUOffset);
        updatePipeTextureAnimation();
    }

    private void updatePipeTextureAnimation() {
        if (pipeInstance == null && pipeModel == null) {
            return;
        }

        Iterable<Material> targetMaterials = pipeInstance != null ? pipeInstance.materials : pipeModel.materials;
        for (Material material : targetMaterials) {
            TextureAttribute diffuse = (TextureAttribute) material.get(TextureAttribute.Diffuse);
            if (diffuse != null) {
                diffuse.offsetU = pipeTextureUOffset;
            }
        }
    }

    private void updatePipeTransform() {
        pipeInstance.transform.idt()
            .rotate(Vector3.Z, pipeBaseRotationZ)
            .rotate(Vector3.X, PIPE_VISUAL_ROTATION_X_DEG)
            .setTranslation(pipeX, pipeY, pipeZ);
    }

    private boolean isPointShadowedByPipe(float x, float y, float z) {
        pipeCenter.set(pipeX + pipeLocalCenterX, pipeY + pipeLocalCenterY, pipeZ + pipeLocalCenterZ);
        pointToSunDirection.set(sunDirectionToScene).scl(-1f);

        float ox = x - pipeCenter.x;
        float oy = y - pipeCenter.y;
        float oz = z - pipeCenter.z;

        float dx = pointToSunDirection.x;
        float dy = pointToSunDirection.y;
        float dz = pointToSunDirection.z;

        if (Math.abs(PIPE_SHADOW_ROTATION_Y_DEG) > 0.001f) {
            float radians = (float) Math.toRadians(PIPE_SHADOW_ROTATION_Y_DEG);
            float cos = (float) Math.cos(radians);
            float sin = (float) Math.sin(radians);

            shadowOriginLocal.set(
                cos * ox - sin * oz,
                oy,
                sin * ox + cos * oz
            );
            shadowDirectionLocal.set(
                cos * dx - sin * dz,
                dy,
                sin * dx + cos * dz
            );

            ox = shadowOriginLocal.x;
            oy = shadowOriginLocal.y;
            oz = shadowOriginLocal.z;
            dx = shadowDirectionLocal.x;
            dy = shadowDirectionLocal.y;
            dz = shadowDirectionLocal.z;
        }

        float a = dy * dy + dz * dz;
        if (a < 1e-6f) {
            return false;
        }

        float b = 2f * (oy * dy + oz * dz);
        float c = oy * oy + oz * oz - pipeShadowRadius * pipeShadowRadius;

        float discriminant = b * b - 4f * a * c;
        if (discriminant < 0f) {
            return false;
        }

        float sqrtD = (float) Math.sqrt(discriminant);
        float inv2a = 0.5f / a;
        float t0 = (-b - sqrtD) * inv2a;
        float t1 = (-b + sqrtD) * inv2a;

        return isHitOnFinitePipe(ox, dx, t0) || isHitOnFinitePipe(ox, dx, t1);
    }

    private boolean isPointShadowedByTable(float x, float y, float z) {
        pointToSunDirection.set(sunDirectionToScene).scl(-1f);
        return intersectsRayAabb(
            x,
            y,
            z,
            pointToSunDirection.x,
            pointToSunDirection.y,
            pointToSunDirection.z,
            TABLE_MIN_X,
            TABLE_MIN_Y,
            TABLE_MIN_Z,
            TABLE_MAX_X,
            TABLE_MAX_Y,
            TABLE_MAX_Z
        );
    }

    private boolean intersectsRayAabb(
        float ox,
        float oy,
        float oz,
        float dx,
        float dy,
        float dz,
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ
    ) {
        float tMin = 0f;
        float tMax = Float.POSITIVE_INFINITY;

        if (!intersectAxis(ox, dx, minX, maxX, tMin, tMax)) {
            return false;
        }
        tMin = axisTMin;
        tMax = axisTMax;

        if (!intersectAxis(oy, dy, minY, maxY, tMin, tMax)) {
            return false;
        }
        tMin = axisTMin;
        tMax = axisTMax;

        if (!intersectAxis(oz, dz, minZ, maxZ, tMin, tMax)) {
            return false;
        }
        tMin = axisTMin;
        tMax = axisTMax;

        return tMax > 1e-5f;
    }

    private float axisTMin;
    private float axisTMax;

    private boolean intersectAxis(float origin, float direction, float min, float max, float currentTMin, float currentTMax) {
        if (Math.abs(direction) < 1e-6f) {
            if (origin < min || origin > max) {
                return false;
            }
            axisTMin = currentTMin;
            axisTMax = currentTMax;
            return true;
        }

        float invD = 1f / direction;
        float t0 = (min - origin) * invD;
        float t1 = (max - origin) * invD;
        if (t0 > t1) {
            float tmp = t0;
            t0 = t1;
            t1 = tmp;
        }

        axisTMin = Math.max(currentTMin, t0);
        axisTMax = Math.min(currentTMax, t1);
        return axisTMax >= axisTMin;
    }

    private boolean isHitOnFinitePipe(float ox, float dx, float t) {
        if (t <= 0f) {
            return false;
        }
        float hitX = ox + dx * t;
        return hitX >= -pipeHalfLength && hitX <= pipeHalfLength;
    }

    private void emitShadowQuad(float x0, float z0, float x1, float z1, float y, float alpha) {
        shadowRenderer.color(0f, 0f, 0f, alpha);
        shadowRenderer.vertex(x0, y, z0);
        shadowRenderer.color(0f, 0f, 0f, alpha);
        shadowRenderer.vertex(x1, y, z0);
        shadowRenderer.color(0f, 0f, 0f, alpha);
        shadowRenderer.vertex(x1, y, z1);

        shadowRenderer.color(0f, 0f, 0f, alpha);
        shadowRenderer.vertex(x0, y, z0);
        shadowRenderer.color(0f, 0f, 0f, alpha);
        shadowRenderer.vertex(x1, y, z1);
        shadowRenderer.color(0f, 0f, 0f, alpha);
        shadowRenderer.vertex(x0, y, z1);
    }

    private void renderRayTracedShadows() {
        float floorY = FLOOR_TOP_Y + SHADOW_EPSILON;
        float floorMinX = -5f;
        float floorMaxX = 5f;
        float floorMinZ = -4f;
        float floorMaxZ = 4f;

        float tableY = TABLE_TOP_Y + SHADOW_EPSILON;
        float tableMinX = TABLE_MIN_X;
        float tableMaxX = TABLE_MAX_X;
        float tableMinZ = TABLE_MIN_Z;
        float tableMaxZ = TABLE_MAX_Z;

        float floorStepX = (floorMaxX - floorMinX) / FLOOR_SHADOW_GRID_X;
        float floorStepZ = (floorMaxZ - floorMinZ) / FLOOR_SHADOW_GRID_Z;
        float tableStepX = (tableMaxX - tableMinX) / TABLE_SHADOW_GRID_X;
        float tableStepZ = (tableMaxZ - tableMinZ) / TABLE_SHADOW_GRID_Z;

        // Keep shadows depth-tested so floor shadows cannot draw over the table.
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shadowRenderer.begin(camera.combined, GL20.GL_TRIANGLES);

        for (int ix = 0; ix < FLOOR_SHADOW_GRID_X; ix++) {
            float x0 = floorMinX + ix * floorStepX;
            float x1 = x0 + floorStepX;
            float sx = (x0 + x1) * 0.5f;
            for (int iz = 0; iz < FLOOR_SHADOW_GRID_Z; iz++) {
                float z0 = floorMinZ + iz * floorStepZ;
                float z1 = z0 + floorStepZ;
                float sz = (z0 + z1) * 0.5f;
                float alpha = 0f;
                if (isPointShadowedByTable(sx, floorY, sz)) {
                    alpha += TABLE_FLOOR_SHADOW_ALPHA;
                }
                if (pipeLanded && isPointShadowedByPipe(sx, floorY, sz)) {
                    alpha += SHADOW_ALPHA_FLOOR;
                }
                if (alpha > 0f) {
                    emitShadowQuad(x0, z0, x1, z1, floorY, Math.min(0.42f, alpha));
                }
            }
        }

        if (!pipeLanded) {
            for (int ix = 0; ix < TABLE_SHADOW_GRID_X; ix++) {
                float x0 = tableMinX + ix * tableStepX;
                float x1 = x0 + tableStepX;
                float sx = (x0 + x1) * 0.5f;
                for (int iz = 0; iz < TABLE_SHADOW_GRID_Z; iz++) {
                    float z0 = tableMinZ + iz * tableStepZ;
                    float z1 = z0 + tableStepZ;
                    float sz = (z0 + z1) * 0.5f;
                    if (isPointShadowedByPipe(sx, tableY, sz)) {
                        emitShadowQuad(x0, z0, x1, z1, tableY, SHADOW_ALPHA_TABLE);
                    }
                }
            }
        }

        shadowRenderer.end();
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        cameraController.update();
        updateCameraKeyboardMovement(delta);
        updatePipeAnimation(delta);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.08f, 0.1f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(sunInstance, environment);
        modelBatch.render(floorInstance, environment);
        modelBatch.render(tableInstance, environment);
        modelBatch.render(pipeInstance, environment);
        modelBatch.end();

        renderRayTracedShadows();
    }

    @Override
    public void dispose() {
        if (floorTexture != null) {
            floorTexture.dispose();
        }
        if (tableTexture != null) {
            tableTexture.dispose();
        }
        if (pipeTexture != null) {
            pipeTexture.dispose();
        }
        if (floorModel != null) {
            floorModel.dispose();
        }
        if (tableModel != null) {
            tableModel.dispose();
        }
        if (pipeModel != null) {
            pipeModel.dispose();
        }
        if (sunModel != null) {
            sunModel.dispose();
        }
        if (pipeImpactSound != null) {
            pipeImpactSound.dispose();
        }
        if (modelBatch != null) {
            modelBatch.dispose();
        }
        if (shadowRenderer != null) {
            shadowRenderer.dispose();
        }
    }
}
