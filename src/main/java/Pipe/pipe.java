package Pipe;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class pipe {
    public static void main(String[] args) throws IOException {
        // Load the metal texture
        BufferedImage metalTexture = ImageIO.read(new File("../Assets/Textures/photo-metal-texture-pattern.jpg"));
        
        // Set up the camera - positioned to view the pipe
        Camera camera = new Camera(
            new Vector3(2, 1.5, 2.5),   // Position - angled view
            new Vector3(0, 0.8, 0),     // Look at - center of pipe
            60                          // Field of view
        );

        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Raytrace the pipe
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

                Color color = traceRay(ray, metalTexture, 0);
                image.setRGB(x, y, color.getRGB());
            }
        }

        // Write the rendered image to file
        ImageIO.write(image, "png", new File("pipe_render.png"));
        System.out.println("Pipe rendered to pipe_render.png");
    }

    private static Color traceRay(Ray ray, BufferedImage texture, int depth) {
        // Limit recursion depth to avoid infinite loops
        if (depth >= 4) {
            return new Color(50, 50, 50);  // Dark background for deep reflections
        }
        
        // Intersect ray with the pipe cylinder
        Intersection pipeIntersection = intersectCylinder(ray);
        
        if (pipeIntersection.hit) {
            // Calculate reflective metallic shading
            Vector3 lightDir = new Vector3(1, 1, 1).normalize();
            double diffuse = Math.max(0.2, pipeIntersection.normal.dot(lightDir));
            
            // Sample from metal texture
            Vector2 uv = calculateCylinderUV(pipeIntersection.point);
            Color baseColor = sampleTexture(texture, uv);
            
            // Calculate reflection ray
            Vector3 viewDir = ray.direction.multiply(-1);
            Vector3 reflectionDir = pipeIntersection.normal.multiply(2 * pipeIntersection.normal.dot(viewDir))
                                                          .subtract(ray.direction);
            Ray reflectionRay = new Ray(pipeIntersection.point.add(reflectionDir.multiply(0.001)), reflectionDir);
            
            // Trace reflection ray
            Color reflectionColor = traceRay(reflectionRay, texture, depth + 1);
            
            // Calculate specular highlight for metallic reflection
            Vector3 reflectDir = pipeIntersection.normal.multiply(2 * pipeIntersection.normal.dot(viewDir))
                                                       .subtract(viewDir);
            double specular = Math.pow(Math.max(0, reflectDir.dot(lightDir)), 16);
            
            // Combine: texture + diffuse + reflection + specular
            // Metal pipe is highly reflective (70%) while keeping some texture (30%)
            double reflectivity = 0.7;
            double textureWeight = 0.3;
            
            int r = (int) Math.min(255, 
                baseColor.getRed() * textureWeight * diffuse + 
                reflectionColor.getRed() * reflectivity +
                255 * specular * 0.4);
            int g = (int) Math.min(255, 
                baseColor.getGreen() * textureWeight * diffuse + 
                reflectionColor.getGreen() * reflectivity +
                255 * specular * 0.4);
            int b = (int) Math.min(255, 
                baseColor.getBlue() * textureWeight * diffuse + 
                reflectionColor.getBlue() * reflectivity +
                255 * specular * 0.4);
            
            return new Color(r, g, b);
        } else {
            // Background color (dark gray with gradient)
            return new Color(30, 30, 35);
        }
    }

    private static Intersection intersectCylinder(Ray ray) {
        final double EPSILON = 0.0000001;
        
        // Cylinder parameters: centered on y-axis, from y=0 to y=2, radius=0.3
        double cylinderRadius = 0.3;
        double cylinderYMin = 0.0;
        double cylinderYMax = 2.0;
        
        // Ray-cylinder intersection (infinite cylinder, then check y bounds)
        Vector3 oc = ray.origin;
        double a = ray.direction.x * ray.direction.x + ray.direction.z * ray.direction.z;
        double b = 2.0 * (oc.x * ray.direction.x + oc.z * ray.direction.z);
        double c = oc.x * oc.x + oc.z * oc.z - cylinderRadius * cylinderRadius;
        
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return new Intersection(false, 0, null, null);
        }
        
        double t0 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        
        // Find the closest valid intersection
        double t = -1;
        if (t0 > EPSILON) {
            Vector3 hitPoint = new Vector3(
                ray.origin.x + ray.direction.x * t0,
                ray.origin.y + ray.direction.y * t0,
                ray.origin.z + ray.direction.z * t0
            );
            if (hitPoint.y >= cylinderYMin && hitPoint.y <= cylinderYMax) {
                t = t0;
            }
        }
        
        if (t < EPSILON && t1 > EPSILON) {
            Vector3 hitPoint = new Vector3(
                ray.origin.x + ray.direction.x * t1,
                ray.origin.y + ray.direction.y * t1,
                ray.origin.z + ray.direction.z * t1
            );
            if (hitPoint.y >= cylinderYMin && hitPoint.y <= cylinderYMax) {
                t = t1;
            }
        }
        
        if (t < EPSILON) {
            return new Intersection(false, 0, null, null);
        }
        
        Vector3 intersectPoint = new Vector3(
            ray.origin.x + ray.direction.x * t,
            ray.origin.y + ray.direction.y * t,
            ray.origin.z + ray.direction.z * t
        );
        
        // Calculate normal (points outward from cylinder axis)
        Vector3 normal = new Vector3(intersectPoint.x, 0, intersectPoint.z).normalize();
        
        return new Intersection(true, t, intersectPoint, normal);
    }

    private static Vector2 calculateCylinderUV(Vector3 point) {
        // Map cylinder surface to UV coordinates
        // U: wraps around the cylinder based on angle
        // V: is based on height along the cylinder
        
        double angle = Math.atan2(point.z, point.x);
        double u = (angle + Math.PI) / (2 * Math.PI);  // 0 to 1 around the cylinder
        
        double v = (point.y - 0.0) / 2.0;  // 0 to 1 from bottom to top
        v = Math.max(0, Math.min(v, 1));
        
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
