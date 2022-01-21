/*
 * BandCap3D.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tiling.parts;

import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;
import tegula.tiling.FundamentalDomain;

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
     * @param tangent
     * @param radius
     * @param N
     * @param geom
     * @return
     */
    public static Point3D[] circle(Point3D center, Point3D tangent, double radius, int N, Geometry geom) {

        final Point3D[] coordinates;

        if (geom == Geometry.Hyperbolic) {
            coordinates = Tools.hyperbolicCircleCoordinates(center, tangent, radius, N);
        } else {
            coordinates = new Point3D[N];
            // finds coordinates of a regular n-sided polygon in the x y plane with center
            // at 0
            for (int n = 0; n < N; n++) {
                coordinates[n] = new Point3D(radius * Math.cos(2 * Math.PI * n / N), radius * Math.sin(2 * Math.PI * n / N), 0);
            }

            // Finds normal vector
            final Point3D normal = Tools.getNormalVector(center, geom); // new z Axis
            tangent = tangent.normalize(); // new x Axis
            final Point3D newYAxis = normal.crossProduct(tangent).normalize(); // new y Axis
            // Transform points with Matrix multiplication. Affine Transformation to the
            // plane of the center point on the surface
            for (int n = 0; n < N; n++) {
                double newX = tangent.getX() * coordinates[n].getX() + newYAxis.getX() * coordinates[n].getY() + normal.getX() * coordinates[n].getZ();
                double newY = tangent.getY() * coordinates[n].getX() + newYAxis.getY() * coordinates[n].getY() + normal.getY() * coordinates[n].getZ();
                double newZ = tangent.getZ() * coordinates[n].getX() + newYAxis.getZ() * coordinates[n].getY() + normal.getZ() * coordinates[n].getZ();
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
        final Point3D[] points3d = new Point3D[coordinates.length + 1];
        points3d[0] = center;
        System.arraycopy(coordinates, 0, points3d, 1, coordinates.length);

        int N = coordinates.length;
        final int[] faces = new int[6 * N];
        int counter = 2;
        for (int i = 0; i < 6 * N; ) {
            if (counter != N + 1) {
                faces[i++] = 0;
                faces[i++] = 0;
                faces[i++] = counter - 1;
                faces[i++] = 1;
                faces[i++] = counter;
                faces[i++] = 2;
                counter++;
            } else {
                faces[i++] = 0;
                faces[i++] = 0;
                faces[i++] = counter - 1;
                faces[i++] = 1;
                faces[i++] = 1;
                faces[i++] = 2;

            }
        }

        for (int i = 0; i < points3d.length; i++) {
            Point3D normal = Tools.getNormalVector(points3d[i], geom);
            points3d[i] = points3d[i].add(normal.multiply(above));
        }

        if (geom != Geometry.Spherical) {
            FundamentalDomain.invertOrientationOfFaces(faces);
        }
        if (flipOrientation)
            FundamentalDomain.invertOrientationOfFaces(faces);

        float[] points = new float[3 * points3d.length];

        for (int i = 0; i < points3d.length; i++) {
            points[3 * i] = (float) points3d[i].getX();
            points[3 * i + 1] = (float) points3d[i].getY();
            points[3 * i + 2] = (float) points3d[i].getZ();
        }


        final TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);
        mesh.getFaces().addAll(faces);
        {
            int[] smoothing = new int[faces.length / 6];
            Arrays.fill(smoothing, 1);
            mesh.getFaceSmoothingGroups().addAll(smoothing);
        }

        return mesh;
    }

    // overloading CircleMesh method with default value
    public static TriangleMesh CircleMesh(Point3D center, Point3D[] coordinates, Geometry geom) {
        return CircleMesh(center, coordinates, geom, 0, false);
    }

}
