package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Affine;



/**
 * Spherical geometry
 * Created by Huson and Zeller on 4/19/16.
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
        final Point3D n1 = a1.crossProduct(b1);  // Normal Vector of plane spanned by a1 and b1
        final Point3D n2 = a2.crossProduct(b2);  // Normal Vector of plane spanned by a2 and b2
        final Point3D r = n1.crossProduct(n2);   // Rotation axis
        final double diff = n1.angle(n2);        // Angle between planes = rotation angle
        final Transform rotate = new Rotate(diff, r); // Maps a1-b1 to a2-b2 or after reflection b1-a1 to a2-b2

        if (keepOrientation) {
            return rotate;
        } else {
            final Point3D X_AXIS = new Point3D(1, 0, 0);  // x-axis of standard basis
            final Point3D n = b1.subtract(a1);  // Normal vector of reflection plane; will be mapped by a change of basis to x-axis of standard basis
            final Affine reflection = new Affine(-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);  // Reflection at y-z plane
            final Point3D k = X_AXIS.crossProduct(n); // Rotation axis for change of basis
            final double diff2 = X_AXIS.angle(n);  // Rotation angle for change of basis
            final Transform changebasis = new Rotate(diff2, k);  // Change of basis
            final Transform changebasis1 = new Rotate(-diff2, k);  // Inverse of change of basis

            return rotate
                    .createConcatenation(changebasis)
                    .createConcatenation(reflection)
                    .createConcatenation(changebasis1);
        }
    }
}
