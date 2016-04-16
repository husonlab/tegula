package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;

/**
 * Euclidean geometry
 * Created by huson on 4/8/16.
 * todo: implement this!
 */
public class SphericalGeometry {
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
        if (true) {
            System.err.println("NOT IMPLEMENTED, using EUCLIDEAN");
            return EuclideanGeometry.createTransform(a1, b1, a2, b2, keepOrientation);
        }

        if (keepOrientation) {
            return null;
        } else {
            return null;
        }
    }
}
