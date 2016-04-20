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



        if (keepOrientation) {
            final Point3D n1 = a1.crossProduct(b1);  // Normal Vector of plane spanned by a1 and b1
            final Point3D n2 = a2.crossProduct(b2);  // Normal Vector of plane spanned by a2 and b2

            final Transform rotate = new Rotate(n1.angle(n2), n1.crossProduct(n2)); // Maps the geodesic through a1, b1 to geodesic through a2, b2
            final Point3D a1Rotate = rotate.transform(a1); // Rotates a1 to geodesic through a2, b2
            final Transform geodesicRotate = new Rotate(a1Rotate.angle(a2),a1Rotate.crossProduct(a2)); // Matches a1Rotate with a2 (Rotation leaving geodesic through a2, b2 invariant)

            return geodesicRotate.createConcatenation(rotate);

        } else {
            final Point3D n1 = a1.crossProduct(b1);  // Normal Vector of plane spanned by a1 and b1
            final Point3D n2 = b2.crossProduct(a2);  // Normal Vector of plane spanned by a2 and b2 (different direction!!)
            //final Point3D r = n1.crossProduct(n2);   // Rotation axis (r/|r| is a crossing point of geodesic formed by a1, b1 and of geodesic formed by a2, b2)
            //final double diff = n1.angle(n2);        // Angle between planes = rotation angle


            final Transform rotate = new Rotate(n1.angle(n2), n1.crossProduct(n2)); // Maps the geodesic formed by the pair a1, b1 to the geodesic formed by a2, b2
            final Point3D a1Rotate = rotate.transform(a1); // Rotates a1 to geodesic through a2, b2
            final Transform geodesicRotate = new Rotate(a1Rotate.angle(b2),a1Rotate.crossProduct(b2)); // Matches b1 with b2

            // Change of basis and reflection at y-z plane
            final Point3D X_AXIS = new Point3D(1, 0, 0);  // x-axis of standard basis
            final Point3D n = a1.subtract(b1);  // Normal vector of reflection plane; will be identified with x-axis by change of basis
            final Point3D k = X_AXIS.crossProduct(n); // Rotation axis for change of basis
            final double diff2 = X_AXIS.angle(n);  // Rotation angle for change of basis
            final Transform changeBasis = new Rotate(diff2, k);  // Change of basis
            final Transform changeBasis1 = new Rotate(-diff2, k);  // Inverse of change of basis
            final Affine reflection = new Affine(-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);  // Reflection at y-z plane

            return geodesicRotate.createConcatenation(rotate).createConcatenation(changeBasis).createConcatenation(reflection).createConcatenation(changeBasis1);
        }
    }
}