package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * Euclidean geometry
 * Created by huson on 4/8/16.
 */
public class EuclideanGeometry {
    private static final Point3D X_AXIS = new Point3D(1, 0, 0);
    private static final Point3D Z_AXIS = new Point3D(0, 0, 1);
    private static final Affine reflection = new Affine(1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1, 0);  // Reflection at x-z plane


    private static final double RAD2DEG_FACTOR = 180.0 / Math.PI;

    /**
     * creates a transformation from a1-b1 to a2-b2, assuming that the distance between both pairs of points is the same
     *
     * @param a1              source point
     * @param b1              source point
     * @param a2              target point
     * @param b2              target point
     * @param keepOrientation keep orientation in x-y plane
     * @return transformation
     */
    public static Transform createTransform(Point3D a1, Point3D b1, Point3D a2, Point3D b2, boolean keepOrientation) {
        if (keepOrientation) {
            final double diff = computeAngleXY(b2.subtract(a2)) - computeAngleXY(b1.subtract(a1));
            final Transform rotate = new Rotate(diff, a1.getX(), a1.getY(), 0, Z_AXIS); // rotate so that a1-b1 has same angle as a2-b2
            final Transform translate = new Translate(a2.getX() - a1.getX(), a2.getY() - a1.getY()); // translate a1 to a2
            return translate.createConcatenation(rotate);
        } else {
            final Transform rotate1 = new Rotate(-computeAngleXY(b1.subtract(a1)), a1.getX(), a1.getY(), 0, Z_AXIS); // rotate so that a1-b1 parallel to  x-axis
            final Translate transToXAxis = new Translate(0,-a1.getY(),0); //Translate to x-axis
            final Affine rotateX = reflection; // Reflect at a-z-plane
            final Translate transBack = new Translate(0,a1.getY(),0); // Translate back from x-axis
            //final Transform rotateX = new Rotate(180, 0, a1.getY(), 0, X_AXIS); // rotate around axis parallel to x-axis by 180 to create flip
            final Transform rotate2 = new Rotate(computeAngleXY(b2.subtract(a2)), a1.getX(), a1.getY(), 0, Z_AXIS); // rotate to match angle of a2-b2
            final Transform translate = new Translate(a2.getX() - a1.getX(), a2.getY() - a1.getY()); // translate a1 to a2

            return translate
                    .createConcatenation(rotate2)
                    .createConcatenation(transBack)
                    .createConcatenation(rotateX)
                    .createConcatenation(transToXAxis)
                    .createConcatenation(rotate1);
        }
    }

    /**
     * Computes the angle of a two-dimensional vector.
     *
     * @param p Point2D
     * @return angle in degree
     */
    public static double computeAngleXY(Point3D p) {

        if (p.getX() != 0) {
            double x = Math.abs(p.getX());
            double y = Math.abs(p.getY());
            double a = Math.atan(y / x);

            if (p.getX() > 0) {
                if (p.getY() > 0)
                    return RAD2DEG_FACTOR * a;
                else
                    return RAD2DEG_FACTOR * (2.0 * Math.PI - a);
            } else // p.getX()<0
            {
                if (p.getY() > 0)
                    return RAD2DEG_FACTOR * (Math.PI - a);
                else
                    return RAD2DEG_FACTOR * (Math.PI + a);
            }
        } else if (p.getY() > 0)
            return RAD2DEG_FACTOR * (0.5 * Math.PI);
        else // p.y<0
            return RAD2DEG_FACTOR * (-0.5 * Math.PI);
    }
}