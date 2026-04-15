package Floor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class floor {
    public static void main(String[] args) throws IOException {
        // Load the tile texture
        BufferedImage tileTexture = ImageIO.read(new File("../Assets/Textures/Tile-texture.jpg"));
        
        // Set up the camera - positioned at 65 degrees looking down on the plane
        Camera camera = new Camera(
            new Vector3(0, 4, 3),       // Position - elevated above the plane
            new Vector3(0, 0, 0),       // Look at - center of floor
            60                          // Field of view
        );

        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Raytrace the floor plane
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double ndcX = (x + 0.5) / width;
                double ndcY = (y + 0.5) / height;
                double screenX = 2 * ndcX - 1;
                double screenY = 1 - 2 * ndcY;
                double aspectRatio = (double) width / height;
                screenX *= aspectRatio;

                Vector3 rayDirection = camera.generateRayDirection(screenX, screenY);
                Ray ray = new Ray(camera.position, rayDirection);

                Color color = traceRay(ray, tileTexture);
                image.setRGB(x, y, color.getRGB());
            }
        }

        // Write the rendered image to file
        ImageIO.write(image, "png", new File("floor_render.png"));
        System.out.println("Floor rendered to floor_render.png");
    }

    private static Color traceRay(Ray ray, BufferedImage texture) {
        // Intersect ray with the floor plane at y = 0
        Intersection floorIntersection = intersectFloorPlane(ray);
        
        if (floorIntersection.hit) {
            // Basic shading based on surface normal
            double lightIntensity = Math.max(0.3, floorIntersection.normal.dot(new Vector3(1, 1, 1).normalize()));
            
            // Sample from tile texture
            Vector2 uv = calculateFloorUV(floorIntersection.point);
            Color baseColor = sampleTexture(texture, uv);
            
            // Apply lighting
            int r = (int) Math.min(255, baseColor.getRed() * lightIntensity);
            int g = (int) Math.min(255, baseColor.getGreen() * lightIntensity);
            int b = (int) Math.min(255, baseColor.getBlue() * lightIntensity);
            
            return new Color(r, g, b);
        } else {
            // Background color (sky blue)
            return new Color(135, 206, 235);
        }
    }

    private static Intersection intersectFloorPlane(Ray ray) {
        final double EPSILON = 0.0000001;
        
        // Floor plane is at y = 0
        // Floor extends from -3.0 to 3.0 in x and -2.0 to 2.0 in z
        double y = 0.0;
        
        // Check if ray is parallel to the plane
        if (Math.abs(ray.direction.y) < EPSILON) {
            return new Intersection(false, 0, null, null);
        }
        
        // Calculate intersection point
        double t = (y - ray.origin.y) / ray.direction.y;
        
        if (t < EPSILON) {
            return new Intersection(false, 0, null, null);
        }
        
        Vector3 intersectPoint = new Vector3(
            ray.origin.x + ray.direction.x * t,
            ray.origin.y + ray.direction.y * t,
            ray.origin.z + ray.direction.z * t
        );
        
        // Check if point is within floor bounds (double the table size)
        // Table was: x from -1.5 to 1.5, z from -1.0 to 1.0
        // Floor is: x from -3.0 to 3.0, z from -2.0 to 2.0
        if (intersectPoint.x >= -3.0 && intersectPoint.x <= 3.0 &&
            intersectPoint.z >= -2.0 && intersectPoint.z <= 2.0) {
            Vector3 normal = new Vector3(0, 1, 0);
            return new Intersection(true, t, intersectPoint, normal);
        }
        
        return new Intersection(false, 0, null, null);
    }

    private static Vector2 calculateFloorUV(Vector3 point) {
        // Create a repeating tile pattern across the floor
        // Each tile covers a certain world space distance
        double tileSize = 0.5;  // Each texture tile repeats every 0.5 units
        
        double u = (point.x % tileSize) / tileSize;
        double v = (point.z % tileSize) / tileSize;
        
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
}

class Camera {
    Vector3 position;
    Vector3 lookAt;
    double fieldOfView;
    Vector3 forward;
    Vector3 right;
    Vector3 up;

    Camera(Vector3 position, Vector3 lookAt, double fieldOfView) {
        this.position = position;
        this.lookAt = lookAt;
        this.fieldOfView = fieldOfView;
        
        // Calculate camera basis vectors
        forward = lookAt.subtract(position).normalize();
        up = new Vector3(0, 1, 0);
        right = forward.cross(up).normalize();
        up = right.cross(forward).normalize();
    }
    
    Vector3 generateRayDirection(double screenX, double screenY) {
        // Calculate the half width and height of the view plane
        double tanHalfFOV = Math.tan(Math.toRadians(fieldOfView / 2.0));
        double viewHeight = 2.0 * tanHalfFOV;
        double viewWidth = viewHeight * (800.0 / 600.0); // aspect ratio
        
        // Calculate ray direction
        Vector3 direction = forward.multiply(1.0)
            .add(right.multiply(screenX * viewWidth / 2.0))
            .add(up.multiply(screenY * viewHeight / 2.0));
        
        return direction.normalize();
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
