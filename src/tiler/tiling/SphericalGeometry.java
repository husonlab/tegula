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
    private static final Point3D X_AXIS = new Point3D(1, 0, 0);  // x-axis of standard basis
    private static final Affine reflection = new Affine(-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);  // Reflection at y-z plane


    /**
     * creates a transformation from a1-b1 to a2-b2, assuming that the distance between both pairs of points is the same
     *
     * @param a1              source point
     * @param b1              source point
     * @param a2              target point
     * @param b2              target point
     * @param keepOrientation keep orientation on sphere
     * @return transformation
     */
    public static Transform createTransform(Point3D a1, Point3D b1, Point3D a2, Point3D b2, boolean keepOrientation) {
        final Point3D n1 = a1.crossProduct(b1);  // Normal Vector of plane spanned by a1 and b1
        final Point3D n2 = a2.crossProduct(b2);  // Normal Vector of plane spanned by a2 and b2
        final Transform rotate;
        final Transform geodesicRotate;
        final Point3D a1Rotate;

        if (n1.angle(n2) == 180) {              // Case a1-b1 and a2-b2 lie on same geodesic with different orientation
            rotate = new Rotate(180, n1.crossProduct(a1.add(b1)));
            a1Rotate = rotate.transform(a1);        // Rotates a1 to geodesic through a2, b2
            if(a1Rotate.angle(a2) == 180){          // Case that a1Rotate = -a2
                geodesicRotate = new Rotate(180,n2);    // Matches a1Rotate with a2
            }
            else {
                geodesicRotate = new Rotate(a1Rotate.angle(a2),a1Rotate.crossProduct(a2)); // Matches a1Rotate with a2
            }
        }
        else {              // Case a1-b1 and a2-b2 lie on different geodesics or on the same geodesic with same orientation
            rotate = new Rotate(n1.angle(n2), n1.crossProduct(n2));    // Maps the geodesic through a1, b1 to geodesic through a2, b2
            a1Rotate = rotate.transform(a1);             // Rotates a1 to geodesic through a2, b2
            if(a1Rotate.angle(a2) == 180){      //Case a1Rotate = -a2
                geodesicRotate = new Rotate(180,n2); // Matches a1Rotate with a2
            }
            else {
                geodesicRotate = new Rotate(a1Rotate.angle(a2),a1Rotate.crossProduct(a2)); // Matches a1Rotate with a2
            }
        }

        // Computes Transformations
        if (keepOrientation) {
            return geodesicRotate.createConcatenation(rotate);
        } else {
            // Change of basis and reflection at y-z plane
            final Point3D k = X_AXIS.crossProduct(n1); // Rotation axis for change of basis
            final double diff2 = X_AXIS.angle(n1);  // Rotation angle for change of basis
            final Transform changeBasis = new Rotate(diff2, k);  // Change of basis
            final Transform changeBasis1 = new Rotate(-diff2, k);  // Inverse of change of basis

            return geodesicRotate.createConcatenation(rotate).createConcatenation(changeBasis).createConcatenation(reflection).createConcatenation(changeBasis1);
        }
    }
}