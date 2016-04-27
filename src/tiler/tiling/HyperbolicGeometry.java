package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * Hyperbolic geometry
 * Created by Zeller on 4/27/16.
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
        final Point3D Y_AXIS = new Point3D(0,1,0);
        final Transform a1ToZero = matchZero(a1, false).createConcatenation(matchVec(a1, Y_AXIS, false)); // Maps a1 to minimal point of hyperboloid
        final Transform a2ToZero = matchZero(a2, false).createConcatenation(matchVec(a2, Y_AXIS, false)); // Maps a2 to minimal point of hyperboloid
        final Point3D b2Zero = a2ToZero.transform(b2);

        if (keepOrientation) {

            final Point3D b1Zero = a1ToZero.transform(b1);

            return matchVec(a2, Y_AXIS, true)
                    .createConcatenation(matchZero(a2, true))
                    .createConcatenation(matchVec(b1Zero, b2Zero, false))   // Maps b1Zero to b2Zero by rotation
                    .createConcatenation(a1ToZero);

        } else {
            final Affine reflection = new Affine(-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);  // Reflection at y-z plane
            final Point3D b1Zero = reflection.createConcatenation(a1ToZero).transform(b1);

            return matchVec(a2, Y_AXIS, true)
                    .createConcatenation(matchZero(a2, true))
                    .createConcatenation(matchVec(b1Zero, b2Zero, false))
                    .createConcatenation(reflection)
                    .createConcatenation(a1ToZero);
        }
    }


    public static Transform matchVec(Point3D a, Point3D vec, boolean Inv) {     // Matches two points on the hyperboloid which have same z-component by a rotation around Z_Axis
        final Point2D a2D = new Point2D(a.getX(), a.getY());    // Consider only x and y components to calculate angle of rotation
        final Point2D vec2D = new Point2D(vec.getX(), vec.getY());

        final Transform rot1;   //Rotation
        final Point3D rot1Axis; // Rotation axis
        final double rot1Angle = a2D.angle(vec2D); // Rotation angle

        if (a2D.getX() * vec2D.getY() - vec2D.getX() * a2D.getY() >= 0) {
            rot1Axis = new Point3D(0, 0, 1);
        } else {
            rot1Axis = new Point3D(0, 0, -1);
        }

        if (Inv){   // Calculates the inverse if Inv is set to true
            rot1 = new Rotate(-rot1Angle, rot1Axis);
        }
        else {
            rot1 = new Rotate(rot1Angle, rot1Axis);
        }
        return rot1;
    }


    public static Affine matchZero(Point3D a, boolean Inv){     // Maps a given point "a" which is in the intersection of y-z-plane and hyperboloid to the minimum of the hyperboloid
        double aRot1Y = Math.sqrt(a.getX() * a.getX() + a.getY() * a.getY());   // distance of "a" from z-axis
        final Affine trans;
        if (Inv){   // Calculates inverse if Inv = true
            trans = new Affine(1, 0, 0, 0, 0, a.getZ()/100, aRot1Y/100, 0, 0, aRot1Y/100, a.getZ()/100, 0); // Isometry leaving y-z-plane invariant
        }
        else {
            trans = new Affine(1, 0, 0, 0, 0, a.getZ() / 100, -aRot1Y / 100, 0, 0, -aRot1Y / 100, a.getZ() / 100, 0);
        }
        return trans;
    }
}