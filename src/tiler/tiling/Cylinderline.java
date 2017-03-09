package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Created by Ruediger on 2017.03.08.
 */
public class Cylinderline {
    /**
     * Created by Ruediger on 2017.03.08.
     */
    public static Cylinder createConnection(Point3D origin, Point3D target, Color color, double width) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(width, height);
        line.setMaterial(new PhongMaterial(color));

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }
}
