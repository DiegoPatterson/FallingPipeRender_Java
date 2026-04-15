package app;

/*
 * Build (cmd.exe):
 * javac -encoding UTF-8 -cp "C:\Users\burns\Downloads\lwjgl-release-3.3.4-custom\*;C:\Users\burns\Documents\GitHub\FallingPipeRender_Java\src\main\java" -d "C:\Users\burns\Documents\GitHub\FallingPipeRender_Java\out" "C:\Users\burns\Documents\GitHub\FallingPipeRender_Java\src\main\java\app\Main.java"
 *
 * Run (cmd.exe):
 * java -Dorg.lwjgl.librarypath="C:\Users\burns\Documents\GitHub\FallingPipeRender_Java\.natives" -cp "C:\Users\burns\Downloads\lwjgl-release-3.3.4-custom\*" app.Main
 */

// java -cp ".\out;C:\Users\burns\Downloads\lwjgl-release-3.3.4-custom\*" app.Main

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_QUAD_STRIP;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import org.lwjgl.stb.STBImage;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Main {
    public static void main(String[] args) {
        new FallingPipe3D().run();
    }

    private static final class FallingPipe3D {
        private static final int START_WIDTH = 1280;
        private static final int START_HEIGHT = 720;

        private static final float FLOOR_Y = 0.0f;
        private static final float TABLE_TOP_MIN_X = -1.5f;
        private static final float TABLE_TOP_MAX_X = 1.5f;
        private static final float TABLE_TOP_MIN_Z = -1.0f;
        private static final float TABLE_TOP_MAX_Z = 1.0f;
        private static final float TABLE_TOP_Y = 1.5f;
        private static final float TABLE_TOP_THICKNESS = 0.1f;
        private static final float TABLE_SURFACE_Y = TABLE_TOP_Y + TABLE_TOP_THICKNESS;
        private static final float LEG_WIDTH = 0.15f;
        private static final float LEG_HEIGHT = 1.5f;

        private static final float PIPE_RADIUS = 0.18f;
        private static final float PIPE_LENGTH = 2.0f;
        private static final float PIPE_START_X = 0.15f;
        private static final float PIPE_Z = 0.0f;
        private static final float PIPE_ROLL_DURATION = 1.15f;
        private static final float PIPE_FALL_DURATION = 0.90f;
        private static final float PIPE_FALL_FORWARD = 1.35f;
        private static final float PIPE_BOUNCE_DURATION = 1.25f;
        private static final float PIPE_BOUNCE_HEIGHT = 0.24f;
        private static final float PIPE_BOUNCE_DAMPING = 2.2f;
        private static final float PIPE_GROUND_ROLL_DURATION = 2.40f;
        private static final float PIPE_GROUND_ROLL_DISTANCE = 1.35f;
        private static final float PIPE_TABLE_TILT_DEG = -52.0f;
        private static final float PIPE_FALL_TILT_DEG = -96.0f;

        // Directional light used for toon shading bands.
        private static final float LIGHT_X = 0.50f;
        private static final float LIGHT_Y = 0.85f;
        private static final float LIGHT_Z = 0.20f;
        private static final float SHADOW_EPSILON_Y = 0.006f;

        private long window;
        private int width = START_WIDTH;
        private int height = START_HEIGHT;

        private Texture floorTexture;
        private Texture woodTexture;
        private Texture metalTexture;

        private ImpactSound impactSound;

        private float cameraYaw = 35.0f;
        private float cameraPitch = 22.0f;
        private float cameraDistance = 7.8f;
        private boolean dragging;
        private double lastMouseX;
        private double lastMouseY;

        private float animationTime;
        private boolean impactPlayed;
        private boolean spaceHeld;
        private boolean resetHeld;

        void run() {
            GLFWErrorCallback.createPrint(System.err).set();
            if (!glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            try {
                initWindow();
                initGL();
                loadResources();
                restart();
                loop();
            } catch (Exception exception) {
                throw new RuntimeException("Failed to run LWJGL scene", exception);
            } finally {
                cleanup();
            }
        }

        private void initWindow() {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

            window = glfwCreateWindow(width, height, "Falling Pipe 3D", NULL, NULL);
            if (window == NULL) {
                throw new IllegalStateException("Failed to create GLFW window");
            }

            glfwSetFramebufferSizeCallback(window, (ignoredWindow, newWidth, newHeight) -> {
                width = Math.max(1, newWidth);
                height = Math.max(1, newHeight);
                glViewport(0, 0, width, height);
            });

            glfwSetScrollCallback(window, (ignoredWindow, xOffset, yOffset) -> {
                cameraDistance -= (float) yOffset * 0.35f;
                cameraDistance = clamp(cameraDistance, 4.5f, 14.0f);
            });

            long monitor = glfwGetPrimaryMonitor();
            if (monitor != NULL) {
                var mode = glfwGetVideoMode(monitor);
                if (mode != null) {
                    glfwSetWindowPos(window, (mode.width() - width) / 2, (mode.height() - height) / 2);
                }
            }

            glfwMakeContextCurrent(window);
            glfwSwapInterval(1);
            glfwShowWindow(window);
        }

        private void initGL() {
            GL.createCapabilities();
            glViewport(0, 0, width, height);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glClearColor(0.58f, 0.74f, 0.92f, 1.0f);
        }

        private void loadResources() throws IOException {
            floorTexture = Texture.load("Assets/Textures/Tile-texture.jpg", true);
            woodTexture = Texture.load("Assets/Textures/Wood-texture.jpg", true);
            metalTexture = Texture.load("Assets/Textures/photo-metal-texture-pattern.jpg", true);
            impactSound = new ImpactSound("Assets/Sounds/jixaw-metal-pipe-falling-sound.wav");
        }

        private void restart() {
            animationTime = 0.0f;
            impactPlayed = false;
        }

        private void loop() {
            double previousTime = glfwGetTime();
            while (!glfwWindowShouldClose(window)) {
                double now = glfwGetTime();
                float delta = (float) (now - previousTime);
                previousTime = now;

                handleInput();
                update(delta);
                render();

                glfwSwapBuffers(window);
                glfwPollEvents();
            }
        }

        private void handleInput() {
            if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
            }

            boolean spaceDown = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
            if (spaceDown && !spaceHeld) {
                restart();
            }
            spaceHeld = spaceDown;

            boolean resetDown = glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS;
            if (resetDown && !resetHeld) {
                cameraYaw = 35.0f;
                cameraPitch = 22.0f;
                cameraDistance = 7.8f;
                restart();
            }
            resetHeld = resetDown;

            boolean leftDown = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
            if (leftDown) {
                double[] x = new double[1];
                double[] y = new double[1];
                glfwGetCursorPos(window, x, y);

                if (!dragging) {
                    dragging = true;
                    lastMouseX = x[0];
                    lastMouseY = y[0];
                } else {
                    double dx = x[0] - lastMouseX;
                    double dy = y[0] - lastMouseY;
                    lastMouseX = x[0];
                    lastMouseY = y[0];

                    cameraYaw += (float) dx * 0.30f;
                    cameraPitch += (float) dy * 0.20f;
                    cameraPitch = clamp(cameraPitch, -10.0f, 80.0f);
                }
            } else {
                dragging = false;
            }
        }

        private void update(float deltaSeconds) {
            animationTime += deltaSeconds;
            PipePose pose = getPipePose(animationTime);
            if (!impactPlayed && pose.hitGround) {
                impactSound.play();
                impactPlayed = true;
            }
        }

        private void render() {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            setPerspective(58.0f, (float) width / (float) height, 0.1f, 100.0f);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glTranslatef(0.0f, 0.0f, -cameraDistance);
            glRotatef(cameraPitch, 1.0f, 0.0f, 0.0f);
            glRotatef(cameraYaw, 0.0f, 1.0f, 0.0f);
            glTranslatef(0.0f, -1.1f, 0.0f);

            drawFloor();

            PipePose pose = getPipePose(animationTime);
            drawTableShadow();
            drawPipeShadow(pose);
            drawTable();
            drawPipe(pose);

            glfwSetWindowTitle(window,
                    "Falling Pipe 3D | Drag Left Mouse: Orbit | Wheel: Zoom | SPACE: Replay | R: Reset");
        }

        private void setPerspective(float fovDeg, float aspect, float zNear, float zFar) {
            float top = (float) (zNear * Math.tan(Math.toRadians(fovDeg * 0.5f)));
            float bottom = -top;
            float right = top * aspect;
            float left = -right;

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glFrustum(left, right, bottom, top, zNear, zFar);
        }

        private void drawFloor() {
            floorTexture.bind();
            applyToonColor(0.0f, 1.0f, 0.0f);
            glBegin(GL_QUADS);
            glNormal3f(0.0f, 1.0f, 0.0f);
            glTexCoord2f(0.0f, 0.0f);
            glVertex3f(-5.0f, FLOOR_Y, -4.0f);
            glTexCoord2f(8.0f, 0.0f);
            glVertex3f(5.0f, FLOOR_Y, -4.0f);
            glTexCoord2f(8.0f, 6.0f);
            glVertex3f(5.0f, FLOOR_Y, 4.0f);
            glTexCoord2f(0.0f, 6.0f);
            glVertex3f(-5.0f, FLOOR_Y, 4.0f);
            glEnd();
        }

        private void drawTable() {
            woodTexture.bind();
            glColor3f(1.0f, 1.0f, 1.0f);

            drawTexturedBox(
                    TABLE_TOP_MIN_X,
                    TABLE_TOP_Y,
                    TABLE_TOP_MIN_Z,
                    TABLE_TOP_MAX_X,
                    TABLE_TOP_Y + TABLE_TOP_THICKNESS,
                    TABLE_TOP_MAX_Z,
                    1.5f,
                    1.0f
            );

            float inset = 0.05f;
            drawTexturedBox(
                    TABLE_TOP_MIN_X + inset,
                    FLOOR_Y,
                    TABLE_TOP_MIN_Z + inset,
                    TABLE_TOP_MIN_X + inset + LEG_WIDTH,
                    FLOOR_Y + LEG_HEIGHT,
                    TABLE_TOP_MIN_Z + inset + LEG_WIDTH,
                    0.35f,
                    2.8f
            );
            drawTexturedBox(
                    TABLE_TOP_MAX_X - inset - LEG_WIDTH,
                    FLOOR_Y,
                    TABLE_TOP_MIN_Z + inset,
                    TABLE_TOP_MAX_X - inset,
                    FLOOR_Y + LEG_HEIGHT,
                    TABLE_TOP_MIN_Z + inset + LEG_WIDTH,
                    0.35f,
                    2.8f
            );
            drawTexturedBox(
                    TABLE_TOP_MIN_X + inset,
                    FLOOR_Y,
                    TABLE_TOP_MAX_Z - inset - LEG_WIDTH,
                    TABLE_TOP_MIN_X + inset + LEG_WIDTH,
                    FLOOR_Y + LEG_HEIGHT,
                    TABLE_TOP_MAX_Z - inset,
                    0.35f,
                    2.8f
            );
            drawTexturedBox(
                    TABLE_TOP_MAX_X - inset - LEG_WIDTH,
                    FLOOR_Y,
                    TABLE_TOP_MAX_Z - inset - LEG_WIDTH,
                    TABLE_TOP_MAX_X - inset,
                    FLOOR_Y + LEG_HEIGHT,
                    TABLE_TOP_MAX_Z - inset,
                    0.35f,
                    2.8f
            );
        }

        private void drawTexturedBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                     float uScale, float vScale) {
            glBegin(GL_QUADS);

            glNormal3f(0.0f, 1.0f, 0.0f);
            applyToonColor(0.0f, 1.0f, 0.0f);
            quad(minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, uScale, vScale);

            glNormal3f(0.0f, -1.0f, 0.0f);
            applyToonColor(0.0f, -1.0f, 0.0f);
            quad(minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, uScale, vScale);

            glNormal3f(0.0f, 0.0f, 1.0f);
            applyToonColor(0.0f, 0.0f, 1.0f);
            quad(minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, uScale, vScale);

            glNormal3f(0.0f, 0.0f, -1.0f);
            applyToonColor(0.0f, 0.0f, -1.0f);
            quad(maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, uScale, vScale);

            glNormal3f(1.0f, 0.0f, 0.0f);
            applyToonColor(1.0f, 0.0f, 0.0f);
            quad(maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, uScale, vScale);

            glNormal3f(-1.0f, 0.0f, 0.0f);
            applyToonColor(-1.0f, 0.0f, 0.0f);
            quad(minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, uScale, vScale);

            glEnd();
        }

        private void quad(float ax, float ay, float az,
                          float bx, float by, float bz,
                          float cx, float cy, float cz,
                          float dx, float dy, float dz,
                          float uScale, float vScale) {
            glTexCoord2f(0.0f, 0.0f);
            glVertex3f(ax, ay, az);
            glTexCoord2f(uScale, 0.0f);
            glVertex3f(bx, by, bz);
            glTexCoord2f(uScale, vScale);
            glVertex3f(cx, cy, cz);
            glTexCoord2f(0.0f, vScale);
            glVertex3f(dx, dy, dz);
        }

        private PipePose getPipePose(float t) {
            float tableY = TABLE_SURFACE_Y + PIPE_RADIUS;
            float rollEndX = TABLE_TOP_MAX_X + 0.20f;

            if (t <= PIPE_ROLL_DURATION) {
                float p = clamp(t / PIPE_ROLL_DURATION, 0.0f, 1.0f);
                float x = lerp(PIPE_START_X, rollEndX, p);
                float rollDeg = lerp(0.0f, PIPE_TABLE_TILT_DEG, easeOutCubic(p));
                return new PipePose(x, tableY, PIPE_Z, rollDeg, 0.30f, false);
            }

            float rawFallTime = t - PIPE_ROLL_DURATION;
            float fallTime = Math.min(rawFallTime, PIPE_FALL_DURATION);
            float xVelocity = PIPE_FALL_FORWARD / PIPE_FALL_DURATION;
            float x = rollEndX + xVelocity * fallTime;
            float postImpactTime = Math.max(0.0f, rawFallTime - PIPE_FALL_DURATION);

            if (postImpactTime > 0.0f) {
                float groundRollProgress = clamp(postImpactTime / PIPE_GROUND_ROLL_DURATION, 0.0f, 1.0f);
                x += PIPE_GROUND_ROLL_DISTANCE * easeOutBack(groundRollProgress);
            }

            float floorContactY = FLOOR_Y + PIPE_RADIUS;
            float initialDropSpeed = 0.55f;
            float gravity = (2.0f * ((tableY - floorContactY) - initialDropSpeed * PIPE_FALL_DURATION))
                    / (PIPE_FALL_DURATION * PIPE_FALL_DURATION);
            float y = tableY - initialDropSpeed * fallTime - 0.5f * gravity * fallTime * fallTime;
            boolean hitGround = rawFallTime >= PIPE_FALL_DURATION;

            if (y < floorContactY) {
                y = floorContactY;
            }

            if (hitGround) {
                float bounceTime = postImpactTime;
                if (bounceTime <= PIPE_BOUNCE_DURATION) {
                    float envelope = (float) Math.exp(-PIPE_BOUNCE_DAMPING * bounceTime);
                    float wave = (float) Math.sin(13.0f * bounceTime);
                    float bounce = PIPE_BOUNCE_HEIGHT * envelope * wave;
                    if (bounce < 0.0f) {
                        bounce = 0.0f;
                    }
                    y = floorContactY + bounce;
                } else {
                    y = floorContactY;
                }
            }

            float fallProgress = clamp(rawFallTime / PIPE_FALL_DURATION, 0.0f, 1.0f);
            float rollDeg = lerp(PIPE_TABLE_TILT_DEG, PIPE_FALL_TILT_DEG, easeInOutCubic(fallProgress));
            float shadow = hitGround ? 0.36f : 0.24f;
            return new PipePose(x, y, PIPE_Z, rollDeg, shadow, hitGround);
        }

        private void drawPipe(PipePose pose) {
            metalTexture.bind();
            glColor3f(1.0f, 1.0f, 1.0f);

            glPushMatrix();
            glTranslatef(pose.x, pose.y, pose.z);
            // Align cylinder length to world X so it lies naturally across the table.
            glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
            glRotatef(pose.rollDeg, 1.0f, 0.0f, 0.0f);
            drawCylinderY(PIPE_RADIUS, PIPE_LENGTH, 30);
            glPopMatrix();
        }

        private void drawCylinderY(float radius, float length, int segments) {
            float yMin = -length * 0.5f;
            float yMax = length * 0.5f;

            // Outer hollow tube (sides only - no end caps for see-through effect)
            glBegin(GL_QUAD_STRIP);
            for (int i = 0; i <= segments; i++) {
                float a = (float) (Math.PI * 2.0 * i / segments);
                float x = (float) Math.cos(a) * radius;
                float z = (float) Math.sin(a) * radius;
                float u = (float) i / segments;

                glNormal3f((float) Math.cos(a), 0.0f, (float) Math.sin(a));
                applyToonColor((float) Math.cos(a), 0.0f, (float) Math.sin(a));
                glTexCoord2f(u, 0.0f);
                glVertex3f(x, yMin, z);
                glTexCoord2f(u, 2.2f);
                glVertex3f(x, yMax, z);
            }
            glEnd();

            // Inner hollow cylinder (to show interior thickness)
            float innerRadius = radius * 0.85f;
            glBegin(GL_QUAD_STRIP);
            for (int i = 0; i <= segments; i++) {
                float a = (float) (Math.PI * 2.0 * i / segments);
                float x = (float) Math.cos(a) * innerRadius;
                float z = (float) Math.sin(a) * innerRadius;
                float u = (float) i / segments;

                glNormal3f(-(float) Math.cos(a), 0.0f, -(float) Math.sin(a));
                applyToonColor(-(float) Math.cos(a), 0.0f, -(float) Math.sin(a));
                glTexCoord2f(u, 0.0f);
                glVertex3f(x, yMin, z);
                glTexCoord2f(u, 2.2f);
                glVertex3f(x, yMax, z);
            }
            glEnd();

            // End caps (rim rings to show metal thickness at ends)
            glBegin(GL_QUAD_STRIP);
            for (int i = 0; i <= segments; i++) {
                float a = (float) (Math.PI * 2.0 * i / segments);
                float xOuter = (float) Math.cos(a) * radius;
                float zOuter = (float) Math.sin(a) * radius;
                float xInner = (float) Math.cos(a) * innerRadius;
                float zInner = (float) Math.sin(a) * innerRadius;

                glNormal3f(0.0f, 1.0f, 0.0f);
                applyToonColor(0.0f, 1.0f, 0.0f);
                glTexCoord2f((xOuter + radius) / (2.0f * radius), (zOuter + radius) / (2.0f * radius));
                glVertex3f(xInner, yMax, zInner);
                glTexCoord2f((xOuter + radius) / (2.0f * radius), (zOuter + radius) / (2.0f * radius));
                glVertex3f(xOuter, yMax, zOuter);
            }
            glEnd();

            glBegin(GL_QUAD_STRIP);
            for (int i = 0; i <= segments; i++) {
                float a = (float) (Math.PI * 2.0 * i / segments);
                float xOuter = (float) Math.cos(a) * radius;
                float zOuter = (float) Math.sin(a) * radius;
                float xInner = (float) Math.cos(a) * innerRadius;
                float zInner = (float) Math.sin(a) * innerRadius;

                glNormal3f(0.0f, -1.0f, 0.0f);
                applyToonColor(0.0f, -1.0f, 0.0f);
                glTexCoord2f((xOuter + radius) / (2.0f * radius), (zOuter + radius) / (2.0f * radius));
                glVertex3f(xOuter, yMin, zOuter);
                glTexCoord2f((xOuter + radius) / (2.0f * radius), (zOuter + radius) / (2.0f * radius));
                glVertex3f(xInner, yMin, zInner);
            }
            glEnd();
        }

        private void applyToonColor(float nx, float ny, float nz) {
            float ndotl = nx * LIGHT_X + ny * LIGHT_Y + nz * LIGHT_Z;
            float lit = quantizeToon(Math.max(0.0f, ndotl));
            glColor3f(lit, lit, lit);
        }

        private float quantizeToon(float intensity) {
            if (intensity > 0.85f) {
                return 1.0f;
            }
            if (intensity > 0.55f) {
                return 0.78f;
            }
            if (intensity > 0.25f) {
                return 0.54f;
            }
            return 0.32f;
        }

        private void drawPipeShadow(PipePose pose) {
            glDisable(GL_TEXTURE_2D);
            glColor4f(0.0f, 0.0f, 0.0f, pose.shadowAlpha);

            float height = Math.max(0.0f, pose.y - FLOOR_Y);
            float shadowX = pose.x - projectToFloorX(height);
            float shadowZ = pose.z - projectToFloorZ(height);

            glPushMatrix();
            glTranslatef(shadowX, FLOOR_Y + SHADOW_EPSILON_Y, shadowZ);
            glRotatef(90.0f, 1.0f, 50.0f, 5.0f);
            glScalef(PIPE_LENGTH * 0.58f, PIPE_RADIUS * 1.55f, 1.0f);
            drawDisk(36);
            glPopMatrix();

            glEnable(GL_TEXTURE_2D);
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }

        private void drawTableShadow() {
            glDisable(GL_TEXTURE_2D);
            glColor4f(0.0f, 0.0f, 0.0f, 0.22f);

            float topHeight = TABLE_SURFACE_Y - FLOOR_Y;
            float dx = projectToFloorX(topHeight);
            float dz = projectToFloorZ(topHeight);

            glBegin(GL_QUADS);
            glVertex3f(TABLE_TOP_MIN_X + dx, FLOOR_Y + SHADOW_EPSILON_Y, TABLE_TOP_MIN_Z + dz);
            glVertex3f(TABLE_TOP_MAX_X + dx, FLOOR_Y + SHADOW_EPSILON_Y, TABLE_TOP_MIN_Z + dz);
            glVertex3f(TABLE_TOP_MAX_X + dx, FLOOR_Y + SHADOW_EPSILON_Y, TABLE_TOP_MAX_Z + dz);
            glVertex3f(TABLE_TOP_MIN_X + dx, FLOOR_Y + SHADOW_EPSILON_Y, TABLE_TOP_MAX_Z + dz);
            glEnd();

            glEnable(GL_TEXTURE_2D);
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }

        private float projectToFloorX(float heightAboveFloor) {
            return (LIGHT_X / LIGHT_Y) * heightAboveFloor;
        }

        private float projectToFloorZ(float heightAboveFloor) {
            return (LIGHT_Z / LIGHT_Y) * heightAboveFloor;
        }

        private void drawDisk(int segments) {
            glBegin(GL_TRIANGLE_FAN);
            glVertex3f(0.0f, 0.0f, 0.0f);
            for (int i = 0; i <= segments; i++) {
                float a = (float) (Math.PI * 2.0 * i / segments);
                glVertex3f((float) Math.cos(a), (float) Math.sin(a), 0.0f);
            }
            glEnd();
        }

        private void cleanup() {
            if (impactSound != null) {
                impactSound.close();
            }
            if (floorTexture != null) {
                floorTexture.close();
            }
            if (woodTexture != null) {
                woodTexture.close();
            }
            if (metalTexture != null) {
                metalTexture.close();
            }
            if (window != NULL) {
                glfwDestroyWindow(window);
            }
            glfwTerminate();
        }

        private static float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }

        private static float easeOutCubic(float t) {
            float v = 1.0f - t;
            return 1.0f - v * v * v;
        }

        private static float easeInOutCubic(float t) {
            if (t < 0.5f) {
                return 4.0f * t * t * t;
            }
            float v = -2.0f * t + 2.0f;
            return 1.0f - (v * v * v) / 2.0f;
        }

        private static float easeOutBack(float t) {
            float c1 = 1.70158f;
            float c3 = c1 + 1.0f;
            float v = t - 1.0f;
            return 1.0f + c3 * v * v * v + c1 * v * v;
        }

        private static final class PipePose {
            final float x;
            final float y;
            final float z;
            final float rollDeg;
            final float shadowAlpha;
            final boolean hitGround;

            PipePose(float x, float y, float z, float rollDeg, float shadowAlpha, boolean hitGround) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.rollDeg = rollDeg;
                this.shadowAlpha = shadowAlpha;
                this.hitGround = hitGround;
            }
        }

        private static final class Texture {
            private final int id;

            private Texture(int id) {
                this.id = id;
            }

            static Texture load(String path, boolean repeat) throws IOException {
                STBImage.stbi_set_flip_vertically_on_load(true);

                IntBuffer width = BufferUtils.createIntBuffer(1);
                IntBuffer height = BufferUtils.createIntBuffer(1);
                IntBuffer components = BufferUtils.createIntBuffer(1);
                ByteBuffer image = STBImage.stbi_load(path, width, height, components, 4);
                if (image == null) {
                    throw new IOException("Failed to load texture " + path + ": " + STBImage.stbi_failure_reason());
                }

                int textureId = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, textureId);
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, repeat ? GL_REPEAT : GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, repeat ? GL_REPEAT : GL_CLAMP_TO_EDGE);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                STBImage.stbi_image_free(image);

                return new Texture(textureId);
            }

            void bind() {
                glBindTexture(GL_TEXTURE_2D, id);
            }

            void close() {
                glDeleteTextures(id);
            }
        }

        private static final class ImpactSound {
            private final Clip clip;

            ImpactSound(String path) {
                Clip loadedClip = null;

                try {
                    loadedClip = loadClipFromFile(new File(path));
                } catch (Exception firstError) {
                    File wavFallback = wavFallback(path);
                    if (wavFallback != null && wavFallback.exists()) {
                        try {
                            loadedClip = loadClipFromFile(wavFallback);
                        } catch (Exception secondError) {
                            System.err.println("Impact sound could not be loaded from MP3 or WAV fallback: " + secondError.getMessage());
                        }
                    } else {
                        System.err.println("Impact sound could not be loaded in-app: " + firstError.getMessage());
                        System.err.println("Tip: place a WAV file next to the MP3 with the same name for reliable playback.");
                    }
                }

                clip = loadedClip;
            }

            void play() {
                if (clip == null) {
                    return;
                }
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                clip.start();
            }

            private Clip loadClipFromFile(File file) throws Exception {
                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                Clip loaded = AudioSystem.getClip();
                loaded.open(stream);
                return loaded;
            }

            private File wavFallback(String originalPath) {
                String lower = originalPath.toLowerCase();
                if (!lower.endsWith(".mp3")) {
                    return null;
                }
                String wavPath = originalPath.substring(0, originalPath.length() - 4) + ".wav";
                return new File(wavPath);
            }

            void close() {
                if (clip != null) {
                    clip.close();
                }
            }
        }
    }
}
