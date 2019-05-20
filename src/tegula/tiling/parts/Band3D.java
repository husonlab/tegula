/*
 * Band3D.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tiling.parts;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.TriangleMesh;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

import java.util.Arrays;

/**
 * represents a band
 * Cornelius Wiehl, 11.2018
 */
public class Band3D extends Group {
    /**
     * connects two points by a band of the given width
     *
     * @param geom
     * @param point0
     * @param point1
     * @param bandWidth
     * @param above
     * todo: implement use of line cap
     * @return mesh
     */
    public static TriangleMesh connect(Geometry geom, Point3D point0, Point3D point1, double bandWidth, double above, StrokeLineCap lineCap1, StrokeLineCap lineCap2) {
        final Point3D[] points3d = new Point3D[6];
        final int[] faces;

        // Points visualized
        // |2|----------|4|
        // |0|----------|1|
        // |3|----------|5|

        switch (geom) {
            default:
            case Euclidean: {
                final Point3D zAxis = new Point3D(0, 0, 1);
                final Point3D diff = point1.subtract(point0).normalize();
                final Point3D normal = diff.crossProduct(zAxis).normalize();

                points3d[0] = point0;
                points3d[1] = point1;
                points3d[2] = point0.add(normal.multiply(bandWidth));
                points3d[3] = point0.add(normal.multiply(-bandWidth));
                points3d[4] = point1.add(normal.multiply(bandWidth));
                points3d[5] = point1.add(normal.multiply(-bandWidth));

                if (lineCap1 == StrokeLineCap.SQUARE) {
                    points3d[2] = points3d[2].add(diff.multiply(0.5 * bandWidth));
                    points3d[0] = points3d[0].add(diff.multiply(0.5 * bandWidth));
                    points3d[3] = points3d[3].add(diff.multiply(0.5 * bandWidth));
                }

                if (lineCap2 == StrokeLineCap.SQUARE) {
                    points3d[4] = points3d[4].add(diff.multiply(-0.5 * bandWidth));
                    points3d[1] = points3d[1].add(diff.multiply(-0.5 * bandWidth));
                    points3d[5] = points3d[5].add(diff.multiply(-0.5 * bandWidth));
                }

                // raises points above surface
                for (int i = 0; i < points3d.length; i++) {
                    points3d[i] = points3d[i].add(0, 0, above);
                }

                faces = new int[]{2, 0, 1, 1, 4, 2, 2, 0, 0, 1, 1, 2, 0, 0, 5, 1, 1, 2, 0, 0, 3, 1, 5, 2};
                break;
            }
            case Spherical: {
                final Point3D diff = point1.subtract(point0).normalize();
                final Point3D normalToSphere0 = Tools.getNormalVector(point0, geom);
                final Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
                final Point3D normal = diff.crossProduct(normalToSphere0.normalize());

                // uses two different normal vectors for two different points
                points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));
                points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));
                points3d[2] = point0.add(normal.multiply(bandWidth)).add(normalToSphere0.normalize().multiply(above));
                points3d[3] = point0.subtract(normal.multiply(bandWidth)).add(normalToSphere0.normalize().multiply(above));
                points3d[4] = point1.add(normal.multiply(bandWidth)).add(normalToSphere1.normalize().multiply(above));
                points3d[5] = point1.subtract(normal.multiply(bandWidth)).add(normalToSphere1.normalize().multiply(above));

                faces = new int[]{2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2};
                break;
            }
            case Hyperbolic: {
                final Point3D diff = point0.subtract(point1);
                final Point3D normalToSphere0 = Tools.getNormalVector(point0, geom); // normal vector for point 0 of points3d
                // array
                final Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
                final Point3D refPoint1 = point0.add(diff); // direction of line used in function below to orientate points
                // correct
                final Point3D refPoint2 = point1.add(diff);
                final Point3D[] pointsForStart = Tools.equidistantHyperbolicPoints(point0, refPoint1, bandWidth);
                final Point3D[] pointsForEnd = Tools.equidistantHyperbolicPoints(point1, refPoint2, bandWidth);

                points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));
                points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));

                // needs a new normal vector for each point
                final Point3D normalToSphere2 = Tools.getHyperbolicNormal(pointsForStart[0]);
                points3d[2] = pointsForStart[0].add(normalToSphere2.normalize().multiply(above));

                final Point3D normalToSphere3 = Tools.getHyperbolicNormal(pointsForStart[1]);
                points3d[3] = pointsForStart[1].add(normalToSphere3.normalize().multiply(above));

                final Point3D normalToSphere4 = Tools.getHyperbolicNormal(pointsForEnd[0]);
                points3d[4] = pointsForEnd[0].add(normalToSphere4.normalize().multiply(above));

                final Point3D normalToSphere5 = Tools.getHyperbolicNormal(pointsForEnd[1]);
                points3d[5] = pointsForEnd[1].add(normalToSphere5.normalize().multiply(above));

                // original structure does not use different normal vectors
                // Point3D diff = point1.subtract(point0).normalize();
                // Point3D normalToSphere0 = Tools.getNormalVector(point0, geom);
                // Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
                // Point3D normal = diff.crossProduct(normalToSphere0.normalize());
                //
                // points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));
                //
                // points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));
                //
                // points3d[2] =
                // point0.add(normal.multiply(size)).add(normalToSphere0.normalize().multiply(above));
                //
                // points3d[3] =
                // point0.subtract(normal.multiply(size)).add(normalToSphere0.normalize().multiply(above));
                //
                // points3d[4] =
                // point1.add(normal.multiply(size)).add(normalToSphere1.normalize().multiply(above));
                //
                // points3d[5] =
                // point1.subtract(normal.multiply(size)).add(normalToSphere1.normalize().multiply(above));

                faces = new int[]{2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2};
                break;
            }
        }

        // creates mesh
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
}
