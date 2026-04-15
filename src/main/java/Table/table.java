package Table;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.io.IOException;

public class table {
    public static void main(String[] args) throws IOException {
        // Initialize the table scene
        TableScene tableScene = new TableScene();
        
        // Load the wood texture
        BufferedImage woodTexture = ImageIO.read(new File("../Assets/Textures/Wood-texture.jpg"));
        
        // Set up the camera
        Camera camera = new Camera(
            new Vector3(0, 1.5, 2.5),   // Position - centered
            new Vector3(0, 0.45, 0),    // Look at - center of table
            60                          // Field of view
        );

        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Raytrace the table
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double ndcX = (x + 0.5) / width;
                double ndcY = (y + 0.5) / height;
                double screenX = 2 * ndcX - 1;
                double screenY = 1 - 2 * ndcY;
                double aspectRatio = (double) width / height;
                screenX *= aspectRatio;

                Vector3 rayDirection = new Vector3(screenX, screenY, -1).normalize();
                Ray ray = new Ray(camera.position, rayDirection);

                Color color = traceRay(ray, tableScene, woodTexture);
                image.setRGB(x, y, color.getRGB());
            }
        }

        // Write the rendered image to file
        ImageIO.write(image, "png", new File("table_render.png"));
        System.out.println("Table rendered to table_render.png");
    }

    private static Vector2 calculateTableTopUV(Vector3 point) {
        // Map the tabletop surface to UV coordinates
        // Table top extends from -1.5 to 1.5 in x and -1.0 to 1.0 in z
        double u = (point.x + 1.5) / 3.0;      // x from -1.5 to 1.5 maps to 0 to 1
        double v = (point.z + 1.0) / 2.0;      // z from -1.0 to 1.0 maps to 0 to 1
        return new Vector2(u, v);
    }

    private static Vector2 calculateLegUV(Vector3 point, Vector3 normal) {
        // Map the leg surface to UV coordinates based on surface normal direction
        // This creates a wrapping texture effect on the legs
        double u, v;
        
        // Determine which face of the leg we're on based on the normal
        double absNx = Math.abs(normal.x);
        double absNy = Math.abs(normal.y);
        double absNz = Math.abs(normal.z);
        
        if (absNy > absNx && absNy > absNz) {
            // Top or bottom face - use x and z
            u = (point.x % 2.0) / 2.0;  // Repeating texture
            v = (point.z % 2.0) / 2.0;  // Repeating texture
        } else if (absNx >= absNz) {
            // Front or back face - use y and z
            u = (point.y % 2.0) / 2.0;  // Repeating texture vertically
            v = (point.z % 2.0) / 2.0;  // Repeating texture horizontally
        } else {
            // Left or right face - use y and x
            u = (point.y % 2.0) / 2.0;  // Repeating texture vertically
            v = (point.x % 2.0) / 2.0;  // Repeating texture horizontally
        }
        
        // Ensure values are positive and in [0, 1]
        if (u < 0) u += 1.0;
        if (v < 0) v += 1.0;
        
        return new Vector2(u, v);
    }

    private static Color sampleTexture(BufferedImage texture, Vector2 uv) {
        int texWidth = texture.getWidth();
        int texHeight = texture.getHeight();

        // Ensure UV coordinates are within the [0, 1] range
        double u = Math.max(0, Math.min(uv.x, 1));
        double v = Math.max(0, Math.min(uv.y, 1));

        // Convert UV coordinates to texture pixel coordinates
        int texX = (int) (u * (texWidth - 1));
        int texY = (int) (v * (texHeight - 1));

        // Get the color from the texture
        return new Color(texture.getRGB(texX, texY));
    }

    private static Color traceRay(Ray ray, TableScene scene, BufferedImage texture) {
        Intersection closestIntersection = null;
        int hitObjectType = -1;  // 0 for tabletop, 1 for leg

        // Test ray against tabletop cube
        Intersection tableTopIntersection = intersectBox(ray, scene.tableTop);
        if (tableTopIntersection.hit && (closestIntersection == null || tableTopIntersection.distance < closestIntersection.distance)) {
            closestIntersection = tableTopIntersection;
            hitObjectType = 0;
        }

        // Test ray against table legs
        for (int i = 0; i < scene.legs.size(); i++) {
            Intersection legIntersection = intersectBox(ray, scene.legs.get(i));
            if (legIntersection.hit && (closestIntersection == null || legIntersection.distance < closestIntersection.distance)) {
                closestIntersection = legIntersection;
                hitObjectType = 1;
            }
        }

        if (closestIntersection != null && closestIntersection.hit) {
            // Basic shading based on surface normal
            double lightIntensity = Math.max(0.2, closestIntersection.normal.dot(new Vector3(1, 1, 1).normalize()));
            
            Color baseColor;
            if (hitObjectType == 0) {
                // Sample from wood texture for tabletop
                Vector2 uv = calculateTableTopUV(closestIntersection.point);
                baseColor = sampleTexture(texture, uv);
            } else {
                // Sample from wood texture for legs as well
                Vector2 uv = calculateLegUV(closestIntersection.point, closestIntersection.normal);
                baseColor = sampleTexture(texture, uv);
            }

            // Apply lighting
            int r = (int) Math.min(255, baseColor.getRed() * lightIntensity);
            int g = (int) Math.min(255, baseColor.getGreen() * lightIntensity);
            int b = (int) Math.min(255, baseColor.getBlue() * lightIntensity);

            return new Color(r, g, b);
        } else {
            // Background color (light gray)
            return new Color(200, 200, 200);
        }
    }

    private static Intersection intersectBox(Ray ray, Box box) {
        // Ray-AABB intersection using slab method
        final double EPSILON = 0.0000001;
        
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;
        Vector3 normalAtHit = new Vector3(0, 0, 0);
        
        // Test intersection with box slabs (x, y, z)
        // For each axis, calculate the t values where ray enters and exits the slab
        
        // X-axis
        if (Math.abs(ray.direction.x) > EPSILON) {
            double t1 = (box.min.x - ray.origin.x) / ray.direction.x;
            double t2 = (box.max.x - ray.origin.x) / ray.direction.x;
            
            if (t1 > t2) {
                double temp = t1;
                t1 = t2;
                t2 = temp;
            }
            
            if (t1 > tMin) {
                tMin = t1;
                normalAtHit = new Vector3(-Math.signum(ray.direction.x), 0, 0);
            }
            if (t2 < tMax) {
                tMax = t2;
            }
        } else {
            if (ray.origin.x < box.min.x || ray.origin.x > box.max.x) {
                return new Intersection(false, 0, null, null);
            }
        }
        
        // Y-axis
        if (Math.abs(ray.direction.y) > EPSILON) {
            double t1 = (box.min.y - ray.origin.y) / ray.direction.y;
            double t2 = (box.max.y - ray.origin.y) / ray.direction.y;
            
            if (t1 > t2) {
                double temp = t1;
                t1 = t2;
                t2 = temp;
            }
            
            if (t1 > tMin) {
                tMin = t1;
                normalAtHit = new Vector3(0, -Math.signum(ray.direction.y), 0);
            }
            if (t2 < tMax) {
                tMax = t2;
            }
        } else {
            if (ray.origin.y < box.min.y || ray.origin.y > box.max.y) {
                return new Intersection(false, 0, null, null);
            }
        }
        
        // Z-axis
        if (Math.abs(ray.direction.z) > EPSILON) {
            double t1 = (box.min.z - ray.origin.z) / ray.direction.z;
            double t2 = (box.max.z - ray.origin.z) / ray.direction.z;
            
            if (t1 > t2) {
                double temp = t1;
                t1 = t2;
                t2 = temp;
            }
            
            if (t1 > tMin) {
                tMin = t1;
                normalAtHit = new Vector3(0, 0, -Math.signum(ray.direction.z));
            }
            if (t2 < tMax) {
                tMax = t2;
            }
        } else {
            if (ray.origin.z < box.min.z || ray.origin.z > box.max.z) {
                return new Intersection(false, 0, null, null);
            }
        }
        
        if (tMin > tMax || tMax < EPSILON) {
            return new Intersection(false, 0, null, null);
        }
        
        double t = (tMin > EPSILON) ? tMin : tMax;
        
        Vector3 intersectPoint = new Vector3(
            ray.origin.x + ray.direction.x * t,
            ray.origin.y + ray.direction.y * t,
            ray.origin.z + ray.direction.z * t
        );
        
        return new Intersection(true, t, intersectPoint, normalAtHit);
    }
}

class TableScene {
    Box tableTop;
    ArrayList<Box> legs = new ArrayList<>();

    TableScene() {
        // Table top: a cube with low height (0.1 units)
        // Positioned on top of legs which are 1.5 units tall
        // Centered at origin, extends from -1.5 to 1.5 in x, -1.0 to 1.0 in z, 1.5 to 1.6 in y
        tableTop = new Box(
            new Vector3(-1.5, 1.5, -1.0),   // min corner
            new Vector3(1.5, 1.6, 1.0)      // max corner
        );
        
        // Create four tall rectangular legs at corners
        // Each leg is a tall rectangle (0.15 x 0.15 in cross-section, 0.8 units tall)
        double legWidth = 0.15;
        double legDepth = 0.15;
        double legHeight = 1.5;
        
        // Front-left leg
        legs.add(new Box(
            new Vector3(-1.5 + 0.05, 0, -1.0 + 0.05),
            new Vector3(-1.5 + 0.05 + legWidth, legHeight, -1.0 + 0.05 + legDepth)
        ));
        
        // Front-right leg
        legs.add(new Box(
            new Vector3(1.5 - 0.05 - legWidth, 0, -1.0 + 0.05),
            new Vector3(1.5 - 0.05, legHeight, -1.0 + 0.05 + legDepth)
        ));
        
        // Back-left leg
        legs.add(new Box(
            new Vector3(-1.5 + 0.05, 0, 1.0 - 0.05 - legDepth),
            new Vector3(-1.5 + 0.05 + legWidth, legHeight, 1.0 - 0.05)
        ));
        
        // Back-right leg
        legs.add(new Box(
            new Vector3(1.5 - 0.05 - legWidth, 0, 1.0 - 0.05 - legDepth),
            new Vector3(1.5 - 0.05, legHeight, 1.0 - 0.05)
        ));
    }
}

class Camera {
    Vector3 position;
    Vector3 lookAt;
    double fieldOfView;

    Camera(Vector3 position, Vector3 lookAt, double fieldOfView) {
        this.position = position;
        this.lookAt = lookAt;
        this.fieldOfView = fieldOfView;
    }
}

class Vector3 {
    double x, y, z;

    Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vector3 subtract(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    Vector3 cross(Vector3 v) {
        return new Vector3(
            y * v.z - z * v.y,
            z * v.x - x * v.z,
            x * v.y - y * v.x
        );
    }

    double dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    Vector3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0) length = 1;
        return new Vector3(x / length, y / length, z / length);
    }

    double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}

class Ray {
    Vector3 origin;
    Vector3 direction;

    Ray(Vector3 origin, Vector3 direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }
}

class Vector2 {
    double x, y;

    Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

class Box {
    Vector3 min;  // Minimum corner of the axis-aligned bounding box
    Vector3 max;  // Maximum corner of the axis-aligned bounding box

    Box(Vector3 min, Vector3 max) {
        this.min = min;
        this.max = max;
    }
}

class Intersection {
    boolean hit;
    double distance;
    Vector3 point;
    Vector3 normal;

    Intersection(boolean hit, double distance, Vector3 point, Vector3 normal) {
        this.hit = hit;
        this.distance = distance;
        this.point = point;
        this.normal = normal;
    }
}
