package tegula.tiling;

import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import tegula.core.dsymbols.Geometry;

import java.util.Arrays;

/**
 * circle used for band cap
 * Cornelius Wiehl, 11.2018
 */
public class BandCap3D {

    /**
     * calculates coordinates for circle by Cornelius 21.11.18
     *
     * @param center
     * @param orientation
     * @param radius
     * @param N
     * @param geom
     * @return
     */
    public static Point3D[] circle(Point3D center, Point3D orientation, double radius, int N, Geometry geom) {

        final Point3D[] coordinates;

        if (geom == Geometry.Hyperbolic) {
            coordinates = Tools.hyperbolicCircleCoordinates(center, orientation, radius, N);
        } else {
            coordinates = new Point3D[N];
            // finds coordinates of a regular n-sided polygon in the x y plane with center
            // at 0
            for (int n = 0; n < N; n++) {
                coordinates[n] = new Point3D(radius * Math.cos(2 * Math.PI * n / N),
                        radius * Math.sin(2 * Math.PI * n / N), 0);
            }

            // Finds normal vector
            final Point3D normal = Tools.getNormalVector(center, geom); // new z Axis
            orientation = orientation.normalize(); // new x Axis
            final Point3D newYAxis = normal.crossProduct(orientation).normalize(); // new y Axis
            // Transform points with Matrix multiplication. Affine Transformation to the
            // plane of the center point on the surface
            for (int n = 0; n < N; n++) {
                double newX = orientation.getX() * coordinates[n].getX() + newYAxis.getX() * coordinates[n].getY()
                        + normal.getX() * coordinates[n].getZ();
                double newY = orientation.getY() * coordinates[n].getX() + newYAxis.getY() * coordinates[n].getY()
                        + normal.getY() * coordinates[n].getZ();
                double newZ = orientation.getZ() * coordinates[n].getX() + newYAxis.getZ() * coordinates[n].getY()
                        + normal.getZ() * coordinates[n].getZ();
                coordinates[n] = new Point3D(newX, newY, newZ).add(center);
            }
        }
        return coordinates;

    }

    /**
     * creates TriangleMesh for given coordinates of a circle by Cornelius 21.11.18
     *
     * @param center
     * @param coordinates
     * @param geom
     * @param above
     * @return
     */
    public static TriangleMesh CircleMesh(Point3D center, Point3D[] coordinates, Geometry geom, double above, boolean flipOrientation) {

        // adds center to the coordinates
        final Point3D[] points3d = new Point3D[coordinates.length + 1];
        points3d[0] = center;
        System.arraycopy(coordinates, 0, points3d, 1, coordinates.length);

        // creates fac array with traingle information
        int N = coordinates.length;
        final int[] faces = new int[6 * N];
        int counter = 2;
        for (int i = 0; i < 6 * N; i = i + 6) {
            if (counter != N + 1) {
                faces[i] = 0;
                faces[i + 1] = 0;
                faces[i + 2] = counter - 1;
                faces[i + 3] = 1;
                faces[i + 4] = counter;
                faces[i + 5] = 2;
                counter++;
            } else {
                faces[i] = 0;
                faces[i + 1] = 0;
                faces[i + 2] = counter - 1;
                faces[i + 3] = 1;
                faces[i + 4] = 1;
                faces[i + 5] = 2;

            }
        }

        // raises all points to the surface
        for (int i = 0; i < points3d.length; i++) {
            Point3D normal = Tools.getNormalVector(points3d[i], geom);
            points3d[i] = points3d[i].add(normal.multiply(above));
        }

        // inverts orientation for Euclidean and Hyperbolic Case
        if (geom != Geometry.Spherical) {
            FundamentalDomainMeshes.invertOrientationOfFaces(faces);
        }
        if (flipOrientation)
            FundamentalDomainMeshes.invertOrientationOfFaces(faces);

        // creates Triangle Mesh
        float[] points = new float[3 * points3d.length];

        for (int i = 0; i < points3d.length; i++) {
            points[3 * i] = (float) points3d[i].getX();
            points[3 * i + 1] = (float) points3d[i].getY();
            points[3 * i + 2] = (float) points3d[i].getZ();
        }

        final float[] texCoords = {0.5f, 0, 0, 0, 1, 1};
        int[] smoothing = new int[faces.length / 6];
        Arrays.fill(smoothing, 1);

        final TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        mesh.getFaceSmoothingGroups().addAll(smoothing);

        return mesh;

    }

    // overloading CircleMesh method with default value
    public static TriangleMesh CircleMesh(Point3D center, Point3D[] coordinates, Geometry geom) {
        return CircleMesh(center, coordinates, geom, 0, false);
    }

}
