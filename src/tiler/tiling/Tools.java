package tiler.tiling;

import javafx.geometry.Point3D;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;

/** Tools for calculation
 * Created by Ruediger on 2017.05.22.
 */
public class Tools {

    /**
     * Distance of points a,b (in hyperbolic case: distance between normalized points on z^2=x^2+y^2+1)
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
}
