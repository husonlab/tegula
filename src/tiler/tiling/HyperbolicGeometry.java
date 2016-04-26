package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * Euclidean geometry
 * Created by huson on 4/8/16.
 */
public class HyperbolicGeometry {

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
            return null;
        } else {
            return null;
        }
    }
}