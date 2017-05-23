package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;

/** Tools for calculation
 * Created by Ruediger on 2017.05.22.
 */
public class Tools {

    /**
     * Distance of points a,b.
     * In hyperbolic case: Hyperbolic distance between normalized points on z^2=x^2+y^2+1).
     * In spherical and Euclidean case: Euclidean distance between points.
     * @param f
     * @param a
     * @param b
     * @return distance
     */
    public static double distance(FDomain f, Point3D a, Point3D b){
        if (f.getGeometry() == Geometry.Hyperbolic){
            double scalar = a.getZ()*b.getZ() - a.getX()*b.getX() - a.getY()*b.getY();
            return Math.log(Math.abs(scalar + Math.sqrt(Math.abs(scalar * scalar - 1))));
        }
        else{
            return a.distance(b);
        }
    }




    /**
     * Calculate hyperbolic translation along vector (dx,dy)
     * @param dx
     * @param dy
     * @return transform
     */
    public static Transform hyperbolicTranslation(double dx, double dy){
        Rotate rotateForward, rotateBackward; //Rotations to x-axis and back
        Affine translateX;
        final Point3D X_Axis = new Point3D(1,0,0);
        double d = Math.sqrt(dx*dx+dy*dy);  // Length of translation
        final Point3D vec = new Point3D(dx,dy,0);

        double rotAngle = vec.angle(X_Axis); //Rotation angle between direction of translation and x-axis
        Point3D rotAxis = new Point3D(0,0,1);  // Rotation axis

        if (dy <= 0){ rotAxis = new Point3D(0,0,-1); }

        rotateForward = new Rotate(rotAngle, rotAxis);
        rotateBackward = new Rotate(-rotAngle, rotAxis);

        translateX = new Affine(Math.cosh(d), 0 , Math.sinh(d), 0, 0, 1, 0, 0, Math.sinh(d), 0, Math.cosh(d), 0); // Translation along x-axis

        return rotateForward.createConcatenation(translateX).createConcatenation(rotateBackward); // Hyperbolic translation
    }
}
