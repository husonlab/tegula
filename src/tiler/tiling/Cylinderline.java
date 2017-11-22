package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.List;

/**
 * Created by Ruediger on 2017.03.08. Slightly modified code from Rahel Lüthy (http://netzwerg.ch/blog/2015/03/22/javafx-3d-line/).
 */
public class Cylinderline {
    /**
     * Created by Ruediger on 2017.03.08.
     */
    public static Node createConnection(Point3D origin, Point3D target, Color color, double width) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(width, height, 1);
        line.setCullFace(CullFace.BACK);
        line.setDrawMode(DrawMode.FILL);
        line.setMaterial(new PhongMaterial(color));

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

    public static Node PolyLine3D(List<Point3D> points, Color color, double width){
        Group g = new Group();
        if (points.size() > 2) {
            for (int i=0; i<points.size()-1; i++) {
                g.getChildren().add(createConnection(points.get(i), points.get(i+1), color, width));
            }
        }
        return  g;
    }
}
