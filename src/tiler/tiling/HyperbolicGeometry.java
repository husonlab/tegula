package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
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
        if (true) {
            final Transform a1Zero = matchZero(a1);
            final Transform a2Zero = matchZero(a2);
            final Point3D b1Zero = a1Zero.transform(b1);
            final Point3D b2Zero = a2Zero.transform(b2);
            final Transform b1Rotb2;
            final Point3D b1Rotb2Axis;
            final Point2D b1Zero2D = new Point2D(b1Zero.getX(), b1Zero.getY());
            final double b1Rotb2Angle = b1Zero2D.angle(b2Zero.getX(), b2Zero.getY());

            if (b1Zero.getX() * b2Zero.getY() - b2Zero.getX() * b1Zero.getY() >= 0) {
                b1Rotb2Axis = new Point3D(0, 0, 1);
            }
            else{
                b1Rotb2Axis = new Point3D(0, 0, -1);
            }

            b1Rotb2 = new Rotate(b1Rotb2Angle,b1Rotb2Axis);

            final double a2Rot = Math.sqrt(a2.getX() * a2.getX() + a2.getY() * a2.getY());
            final Affine InvTrans = new Affine(1, 0, 0, 0, 0, a2.getZ()/100, a2Rot/100, 0, 0, a2Rot/100, a2.getZ()/100, 0);

            final Point2D a22D = new Point2D(a2.getX(),a2.getY());
            final double InvRotAngle = a22D.angle(0, 1);
            final Point3D InvRotAxis;
            if (a2.getX() >= 0){
                InvRotAxis = new Point3D(0,0,1);
            }
            else{
                InvRotAxis = new Point3D(0,0,-1);
            }
            final Transform InvRot = new Rotate(-InvRotAngle, InvRotAxis);

            return InvRot.createConcatenation(InvTrans).createConcatenation(b1Rotb2).createConcatenation(a1Zero);

            //return  a1Zero;
        } else {
            return null;
        }
    }


    public static Transform matchZero(Point3D a){
        final Point2D a2D = new Point2D(a.getX(), a.getY());

        final Transform rot1;
        final Point3D rot1Axis;
        final double rot1Angle = a2D.angle(0, 1);

        if (a.getX() >= 0) {
            rot1Axis = new Point3D(0, 0, 1);
            rot1 = new Rotate(rot1Angle, rot1Axis);
        }
        else {
            rot1Axis = new Point3D(0, 0, -1);
            rot1 = new Rotate(rot1Angle, rot1Axis);
        }

        double aRot1Y = Math.sqrt(a.getX() * a.getX() + a.getY() * a.getY());
        final Affine trans = new Affine(1, 0, 0, 0, 0, a.getZ()/100, -aRot1Y/100, 0, 0, -aRot1Y/100, a.getZ()/100, 0);
        return trans.createConcatenation(rot1);
    }

}