package tegula.geometry;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.Geometry;

/**
 * Tools for calculation
 * Ruediger Zeller, 2017
 */
public class Tools {
    /**
     * Distance of points a,b. In hyperbolic case: Hyperbolic distance between
     * normalized points on z^2=x^2+y^2+1). In spherical and Euclidean case:
     * Euclidean distance between points.
     *
     * @param geom
     * @param a
     * @param b
     * @return distance
     */
    public static double distance(Geometry geom, Point3D a, Point3D b) {
        if (geom == Geometry.Hyperbolic) {
            a = a.multiply(0.01);
            b = b.multiply(0.01);
            double scalar = a.getZ() * b.getZ() - a.getX() * b.getX() - a.getY() * b.getY();
            return Math.log(Math.abs(scalar + Math.sqrt(Math.abs(scalar * scalar - 1))));
        } else {
            return a.distance(b);
        }
    }

    /**
     * returns geometric distance for two points and given geometry by Cornelius
     * 21.11.18
     *
     * @param geom
     * @param a
     * @param b
     * @return distance
     */
    public static double geometricDistance(Geometry geom, Point3D a, Point3D b) {
        if (geom == Geometry.Hyperbolic) {
            return hyperbolicDistance(a, b);
        } else if (geom == Geometry.Spherical) {
            return sphericalDistance(a, b);
        } else {
            return a.distance(b);
        }
    }

    public static double sphericalDistance(Point3D a, Point3D b) {
        return Math.acos(a.normalize().dotProduct(b.normalize()));
    }

    public static double hyperbolicDistance(Point3D a, Point3D b) {
        double value = Math.abs(minkowskiScalar(a, b));
        return arccosh(value);
    }

    /**
     * Calculates midpoint between points a and b on 3d-models
     *
     * @param geometry
     * @param a
     * @param b
     * @return midpoint between a and b
     */
    public static Point3D midpoint3D(Geometry geometry, Point3D a, Point3D b) {
        if (geometry == Geometry.Euclidean) {
            return a.midpoint(b);
        } else if (geometry == Geometry.Spherical) {
            // return (a.midpoint(b)).normalize().multiply(factor);
            return interpolateSpherePoints(a, b, 0.5);
        } else {
            return interpolateHyperbolicPoints(a, b, 0.5);
        }
    }

    /**
     * Calculates interpolated point with respect to 0 <= pos <= 1 between points a
     * and b on 3d-hyperboloid.
     *
     * @param a
     * @param b
     * @param pos
     * @return midpoint between a and b
     */
    public static Point3D interpolateHyperbolicPoints(Point3D a, Point3D b, double pos) {
        a = a.multiply(0.01);
        b = b.multiply(0.01);

        Point3D xAxis = new Point3D(1, 0, 0);
        Point3D origin = new Point3D(0, 0, 1);

        double rotAngle = xAxis.angle(a.getX(), a.getY(), 0);

        Point3D rotAxis = null;
        if (a.getY() >= 0)
            rotAxis = new Point3D(0, 0, -1);
        else
            rotAxis = new Point3D(0, 0, 1);

        Rotate rotateToX = new Rotate(rotAngle, rotAxis);
        Rotate rotateToXInv = new Rotate(-rotAngle, rotAxis);

        ///////

        double dist = Math.log(Math.abs(a.getZ() + Math.sqrt(Math.abs(a.getZ() * a.getZ() - 1))));

        Affine translate1 = new Affine(Math.cosh(-dist), 0, Math.sinh(-dist), 0, 0, 1, 0, 0, Math.sinh(-dist), 0,
                Math.cosh(-dist), 0);
        Affine translate1Inv = new Affine(Math.cosh(dist), 0, Math.sinh(dist), 0, 0, 1, 0, 0, Math.sinh(dist), 0,
                Math.cosh(dist), 0);

        ///////

        Point3D p2moved = translate1.transform(rotateToX.transform(b));

        rotAngle = xAxis.angle(p2moved.getX(), p2moved.getY(), 0);
        if (p2moved.getY() >= 0)
            rotAxis = new Point3D(0, 0, -1);
        else
            rotAxis = new Point3D(0, 0, 1);

        Rotate rotat2 = new Rotate(-rotAngle, rotAxis); // only inverse is needed

        ///////

        dist = pos * Math.log(Math.abs(p2moved.getZ() + Math.sqrt(Math.abs(p2moved.getZ() * p2moved.getZ() - 1))));

        Affine translate2 = new Affine(Math.cosh(dist), 0, Math.sinh(dist), 0, 0, 1, 0, 0, Math.sinh(dist), 0,
                Math.cosh(dist), 0);

        return rotateToXInv.transform(translate1Inv.transform(rotat2.transform(translate2.transform(origin)))).multiply(100);
    }

    /**
     * by Cornelius calculates two points of equal hyperbolic distance to point
     *
     * @param point
     * @param point2
     * @param distance
     * @return two points of equal hyperbolic distance perpendicular to direction of
     * second point
     */
    public static Point3D[] equidistantHyperbolicPoints(Point3D point, Point3D point2, double distance) {
        point = point.multiply(0.01);
        point2 = point2.multiply(0.01);

        final Point3D[] result = new Point3D[2];
        Point3D xAxis = new Point3D(1, 0, 0);

        // rotates start point on to the x-Axis
        double rotAngle = xAxis.angle(point.getX(), point.getY(), 0);

        Point3D rotAxis = null;
        if (point.getY() >= 0)
            rotAxis = new Point3D(0, 0, -1);
        else
            rotAxis = new Point3D(0, 0, 1);

        Rotate rotateToX = new Rotate(rotAngle, rotAxis);
        Rotate rotateToXInv = new Rotate(-rotAngle, rotAxis);

        // translates start point to ursprung
        double dist = Math.log(Math.abs(point.getZ() + Math.sqrt(Math.abs(point.getZ() * point.getZ() - 1))));

        Affine translate1 = new Affine(Math.cosh(-dist), 0, Math.sinh(-dist), 0, 0, 1, 0, 0, Math.sinh(-dist), 0,
                Math.cosh(-dist), 0);
        Affine translate1Inv = new Affine(Math.cosh(dist), 0, Math.sinh(dist), 0, 0, 1, 0, 0, Math.sinh(dist), 0,
                Math.cosh(dist), 0);

        // uses same rotation and translation on end point
        Point3D endmoved = translate1.transform(rotateToX.transform(point2));

        // rotate moved end point to x axis
        Point3D rotAxis2 = null;
        double rotAngle2 = xAxis.angle(endmoved.getX(), endmoved.getY(), 0);

        if (endmoved.getY() >= 0)
            rotAxis2 = new Point3D(0, 0, -1);
        else
            rotAxis2 = new Point3D(0, 0, 1);

        // Rotate rotat2 = new Rotate(rotAngle2, rotAxis2);
        Rotate rotat2Inv = new Rotate(-rotAngle2, rotAxis2); // only inverse is needed

        // creates points on of equal distance to urpsrung that are perpendicular to the
        // moved end point
        Point3D returnpoint1 = new Point3D(0, arcsinh(-distance),
                Math.sqrt(Math.pow(Math.sinh(distance), 2) + 1));
        Point3D returnpoint2 = new Point3D(0, arcsinh(distance),
                Math.sqrt(Math.pow(Math.sinh(distance), 2) + 1));

        // uses same rotations and translations that were used on end point
        result[0] = rotateToXInv.transform(translate1Inv.transform(rotat2Inv.transform(returnpoint1))).multiply(100);
        result[1] = rotateToXInv.transform(translate1Inv.transform(rotat2Inv.transform(returnpoint2))).multiply(100);

        return result;

    }

    /**
     * * by Cornelius calculates circle coordinates as an n sided polygon to a given
     * center point on hyperboloid with a given orientation
     *
     * @param center
     * @param orientation
     * @param radius
     * @param fine
     * @return circle coordinates
     */
    public static Point3D[] hyperbolicCircleCoordinates(Point3D center, Point3D orientation, double radius, int fine) {
        center = center.multiply(0.01);

        Point3D end = center.add(orientation.normalize());
        Point3D xAxis = new Point3D(1, 0, 0);

        // rotates center point to x axis
        double rotAngle = xAxis.angle(center.getX(), center.getY(), 0);

        Point3D rotAxis = null;
        if (center.getY() >= 0)
            rotAxis = new Point3D(0, 0, -1);
        else
            rotAxis = new Point3D(0, 0, 1);

        Rotate rotateToX = new Rotate(rotAngle, rotAxis);
        Rotate rotateToXInv = new Rotate(-rotAngle, rotAxis);

        // translates center point to ursprung
        double dist = Math.log(Math.abs(center.getZ() + Math.sqrt(Math.abs(center.getZ() * center.getZ() - 1))));

        Affine translate1 = new Affine(Math.cosh(-dist), 0, Math.sinh(-dist), 0, 0, 1, 0, 0, Math.sinh(-dist), 0,
                Math.cosh(-dist), 0);
        Affine translate1Inv = new Affine(Math.cosh(dist), 0, Math.sinh(dist), 0, 0, 1, 0, 0, Math.sinh(dist), 0,
                Math.cosh(dist), 0);

        Point3D endmoved = translate1.transform(rotateToX.transform(end));

        // rotates moved end point to x axis
        Point3D rotAxis2 = null;
        double rotAngle2 = xAxis.angle(endmoved.getX(), endmoved.getY(), 0);

        if (endmoved.getY() >= 0)
            rotAxis2 = new Point3D(0, 0, -1);
        else
            rotAxis2 = new Point3D(0, 0, 1);

        Rotate rotat2Inv = new Rotate(-rotAngle2, rotAxis2); // only inverse is needed

        // creates circle points around (0,0,1);
        Point3D[] coordinates = new Point3D[fine];
        Point3D[] result = new Point3D[fine];
        double zvalue = Math.sqrt(Math.pow(radius, 2) + 1);
        for (int n = 0; n < fine; n++) {
            coordinates[n] = new Point3D(Math.sinh(radius) * Math.cos(2 * Math.PI * n / fine),
                    Math.sinh(radius) * Math.sin(2 * Math.PI * n / fine), zvalue);
        }

        // rotates and translates circle points back to original center
        for (int i = 0; i < fine; i++) {
            result[i] = rotateToXInv.transform(translate1Inv.transform(rotat2Inv.transform(coordinates[i]))).multiply(100);
        }

        return result;
    }

    /**
     * Calculates interpolated point with respect to 0 <= pos <= 1 between points a
     * and b on 3d-sphere
     *
     * @param pointA
     * @param pointB
     * @param pos
     * @return
     */
    public static Point3D interpolateSpherePoints(Point3D pointA, Point3D pointB, double pos) {
        Point3D xAxis = new Point3D(100, 0, 0);
        Point3D yAxis = new Point3D(0, 100, 0);
        Point3D zAxis = new Point3D(0, 0, 100);

        double rotAngle1 = xAxis.angle(new Point3D(pointA.getX(), pointA.getY(), 0));

        Point3D rotAxis = null;
        if (pointA.getY() >= 0)
            rotAxis = zAxis.multiply(-1);
        else
            rotAxis = zAxis;

        Rotate rotateToX = new Rotate(rotAngle1, rotAxis);
        Rotate rotateToXInv = new Rotate(-rotAngle1, rotAxis);

        ////////

        double rotAngle2 = zAxis.angle(rotateToX.transform(pointA).getX(), 0, pointA.getZ());

        if (rotateToX.transform(pointA).getX() >= 0)
            rotAxis = yAxis.multiply(-1);
        else
            rotAxis = yAxis;

        Rotate rotateToZ = new Rotate(rotAngle2, rotAxis);
        Rotate rotateToZInv = new Rotate(-rotAngle2, rotAxis);

        ////////

        Point3D bNew = rotateToZ.transform(rotateToX.transform(pointB));

        double rotAngle3 = xAxis.angle(new Point3D(bNew.getX(), bNew.getY(), 0));
        if (bNew.getY() >= 0)
            rotAxis = zAxis.multiply(-1);
        else
            rotAxis = zAxis;

        Rotate rotBNew = new Rotate(rotAngle3, rotAxis);
        Rotate rotBNewInv = new Rotate(-rotAngle3, rotAxis);

        ////////

        double rotAngle4 = zAxis.angle(rotBNew.transform(bNew).getX(), 0, bNew.getZ());

        if (rotBNew.transform(bNew).getX() >= 0)
            rotAxis = yAxis.multiply(-1);
        else
            rotAxis = yAxis;

        Rotate rotInterpolate = new Rotate(-pos * rotAngle4, rotAxis);

        return rotateToXInv.transform(rotateToZInv.transform(rotBNewInv.transform(rotInterpolate.transform(zAxis))));
    }

    /**
     * Calculate hyperbolic translation along vector (dx,dy)
     *
     * @param dx
     * @param dy
     * @return transform
     */
    public static Transform hyperbolicTranslation(double dx, double dy) {
        dx /= 100;
        dy /= 100;

        Rotate rotateForward, rotateBackward; // Rotations to x-axis and back
        Affine translateX;
        final Point3D X_Axis = new Point3D(1, 0, 0);
        double d = Math.sqrt(dx * dx + dy * dy); // Length of translation
        final Point3D vec = new Point3D(dx, dy, 0);

        double rotAngle = vec.angle(X_Axis); // Rotation angle between direction
        // of translation and x-axis
        Point3D rotAxis = new Point3D(0, 0, 1); // Rotation axis

        if (dy <= 0) {
            rotAxis = new Point3D(0, 0, -1);
        }

        rotateForward = new Rotate(rotAngle, rotAxis);
        rotateBackward = new Rotate(-rotAngle, rotAxis);

        translateX = new Affine(Math.cosh(d), 0, Math.sinh(d), 0, 0, 1, 0, 0, Math.sinh(d), 0, Math.cosh(d), 0); // Translation
        // along
        // x-axis

        return rotateForward.createConcatenation(translateX).createConcatenation(rotateBackward); // Hyperbolic
        // translation
    }

    /**
     * calculates spherical midpoint by Cornelius 21.11.18
     *
     * @param point0
     * @param point1
     * @return 3D point
     */
    public static Point3D sphericalMidpoint(Point3D point0, Point3D point1) {
        Point3D point0n = point0.normalize();
        Point3D point1n = point1.normalize();
        Point3D difference = point1n.subtract(point0n);
        return (point0n.add(difference.multiply(0.5))).normalize().multiply(100);
    }


    /**
     * map 2D point (in 100*unit model) to 3D point (scaled by factor 100)
     * geometry
     *
     * @param apt0
     * @return 3D point
     */
    public static Point3D map2Dto3D(Geometry geometry, Point2D apt0) {
        switch (geometry) {
            default:
            case Euclidean: {
                return new Point3D(apt0.getX(), apt0.getY(), 0);
            }
            case Spherical: {
                final Point2D apt = apt0.multiply(0.01);

                final double d = apt.getX() * apt.getX() + apt.getY() * apt.getY();
                Point3D point = new Point3D((2 * apt.getX() / (1 + d)), (2 * apt.getY() / (1 + d)), ((d - 1) / (d + 1))).multiply(100);

                final Point2D back = map3Dto2D(geometry, point);
                if (apt0.distance(back) > 0.00001)
                    System.err.println(String.format("Mapping propblem (%.4f,%.4f) -> (%.1f,%.1f,%.1f) -back-> (%.4f,%.4f)",
                            apt0.getX(), apt0.getY(), point.getX(), point.getY(), point.getZ(), back.getX(), back.getY()));

                return point;
            }
            case Hyperbolic: {
                final Point2D apt = apt0.multiply(0.01);

                final double d = apt.getX() * apt.getX() + apt.getY() * apt.getY();
                if (d < 1) {
                    final Point3D result = new Point3D((2 * apt.getX() / (1 - d)), (2 * apt.getY() / (1 - d)), ((1 + d) / (1 - d))).multiply(100);

                    if (true) {
                        final Point2D back = map3Dto2D(geometry, result);
                        if (apt0.distance(back) > 0.00001)
                            System.err.println(String.format("Mapping problem (%.4f,%.4f) -> (%.1f,%.1f,%.1f) -back-> (%.4f,%.4f)",
                                    apt0.getX(), apt0.getY(), result.getX(), result.getY(), result.getZ(), back.getX(), back.getY()));
                    }
                    return result;
                } else
                    return new Point3D(0, 0, 0);
            }
        }
    }

    /**
     * Euclidean case:  drop coordinate z = 0. Spherical case:
     * Calculates inverse of stereographic projection. Maps from sphere with radius
     * 100 to Euclidean plane in 100*unit scale. Hyperbolic case: Maps a point on
     * hyperboloid model (scaled by 100) to Poincare disk model (open 100*unit
     * disk).
     *
     * @param apt0
     * @return
     */

    public static Point2D map3Dto2D(Geometry geometry, Point3D apt0) {
        switch (geometry) {
            default:
            case Euclidean: {
                return new Point2D(apt0.getX(), apt0.getY());
            }
            case Spherical: { // Inverse of stereographic projection
                final Point3D apt = apt0.multiply(0.01); // scale by 0.01
                double d = (1 + apt.getZ()) / (1 - apt.getZ());
                return new Point2D((apt.getX() * (d + 1) / 2), (apt.getY() * (d + 1) / 2)).multiply(100);
            }
            case Hyperbolic: { // Transforms hyperboloid model to Poincare disk
                // model
                final Point3D bpt = apt0.multiply(0.01); // scale by 0.01
                final Point2D result = new Point2D(bpt.getX() / (1 + bpt.getZ()), bpt.getY() / (1 + bpt.getZ())).multiply(100);
                if (false) {
                    final Point3D back = map2Dto3D(geometry, result);
                    if (apt0.distance(back) > 0.00001)
                        System.err.println(String.format("Mapping problem (%.4f,%.4f,%.4f) -> (%.1f,%.1f) -back-> (%.4f,%.4f,%.4f)",
                                apt0.getX(), apt0.getY(), apt0.getZ(), result.getX(), result.getY(), back.getX(), back.getY(), back.getZ()));
                }

                return result;
            }
        }
    }

    // more geometric tools ///////////////////////
    public static double arcsinh(double value) {
        return Math.log(value + Math.sqrt(1 + (value * value)));
    }

    public static double arccosh(double value) {
        return Math.log(value + Math.sqrt(value * value - 1));
    }

    public static double minkowskiScalar(Point3D a, Point3D b) {
        return a.getX() * b.getX() + a.getY() * b.getY() - a.getZ() * b.getZ();
    }

    public static Point3D getNormalVector(Point3D point, Geometry geom) {
        if (geom == Geometry.Spherical) {
            return getSphericalNormal(point);
        } else if (geom == Geometry.Hyperbolic) {
            return getHyperbolicNormal(point);
        } else {
            return new Point3D(0, 0, 1);
        }
    }

    public static Point3D getSphericalNormal(Point3D point) {
        return new Point3D(2 * point.getX(), 2 * point.getY(), 2 * point.getZ()).normalize();
    }

    public static Point3D getHyperbolicNormal(Point3D point) {
        return new Point3D(2 * point.getX(), 2 * point.getY(), -2 * point.getZ()).normalize();
    }
    /////////////////////////////////////////////////
}