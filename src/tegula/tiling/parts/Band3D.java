/*
 * Band3D.java Copyright (C) 2020. Daniel H. Huson
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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * represents a band
 * Cornelius Wiehl, 11.2018
 */
public class Band3D {
    /**
     * connects two points by a band of the given width
     */
    public static TriangleMesh connect(Geometry geom, Point3D[] corners, double bandWidth, double above, StrokeLineCap strokeLineCap) {
        final ArrayList<TriangleMesh> meshes = new ArrayList<>();

        for (int pos = 0; pos + 1 < corners.length; pos++) {
            final Point3D point0 = corners[pos];
            final Point3D point1 = corners[pos + 1];

            final Point3D[] points3d = new Point3D[6];
            final int[] faces;

            // Points visualized
            // |2|----------|4|
            // |0|----------|1|
            // |3|----------|5|


            switch (geom) {// raises points above surface
                case Euclidean: {
                    final Point3D zAxis = new Point3D(0, 0, 1);
                    final Point3D direction = point1.subtract(point0);
                    final Point3D normal = direction.crossProduct(zAxis).normalize();
                    points3d[0] = point0;
                    points3d[1] = point1;
                    points3d[2] = point0.add(normal.multiply(bandWidth));
                    points3d[3] = point0.add(normal.multiply(-bandWidth));
                    points3d[4] = point1.add(normal.multiply(bandWidth));
                    points3d[5] = point1.add(normal.multiply(-bandWidth));
                    for (int i = 0; i < points3d.length; i++) {
                        points3d[i] = points3d[i].add(0, 0, above);
                    }
                    faces = new int[]{2, 0, 1, 1, 4, 2, 2, 0, 0, 1, 1, 2, 0, 0, 5, 1, 1, 2, 0, 0, 3, 1, 5, 2};
                    if (strokeLineCap == StrokeLineCap.SQUARE) {
                        points3d[2] = points3d[2].add(direction.multiply(-bandWidth));
                        points3d[0] = points3d[0].add(direction.multiply(-bandWidth));
                        points3d[3] = points3d[3].add(direction.multiply(-bandWidth));

                        points3d[4] = points3d[4].add(direction.multiply(bandWidth));
                        points3d[1] = points3d[1].add(direction.multiply(bandWidth));
                        points3d[5] = points3d[5].add(direction.multiply(bandWidth));
                    } else if (strokeLineCap == StrokeLineCap.ROUND) {
                        final Point3D arrow0 = direction.multiply(-bandWidth / direction.magnitude());
                        final Point3D arrow1 = direction.multiply(bandWidth / direction.magnitude());
                        final int parts = 12;
                        final double delta = (180.0 / parts);
                        final Point3D[] semiCircle0 = new Point3D[parts + 1];
                        final Point3D[] semiCircle1 = new Point3D[parts + 1];

                        final Rotate rotate = new Rotate(90, Rotate.Z_AXIS);

                        for (int i = 0; i <= parts; i++) {
                            semiCircle0[i] = point0.add(rotate.transform(arrow0)).add(0, 0, above);
                            semiCircle1[i] = point1.add(rotate.transform(arrow1)).add(0, 0, above);

                            rotate.setAngle(rotate.getAngle() - delta);
                        }
                        if (pos == 0)
                            meshes.add(MeshUtils.createFan(point0, semiCircle0));
                        meshes.add(MeshUtils.createFan(point1, semiCircle1));
                    }
                    break;
                }
// uses two different normal vectors for two different points
                case Spherical: {
                    final Point3D diff = point1.subtract(point0).normalize();
                    final Point3D normalToSphere0 = Tools.getNormalVector(point0, geom);
                    final Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
                    final Point3D normal = diff.crossProduct(normalToSphere0.normalize());
                    points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));
                    points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));
                    points3d[2] = point0.add(normal.multiply(bandWidth)).add(normalToSphere0.normalize().multiply(above));
                    points3d[3] = point0.subtract(normal.multiply(bandWidth)).add(normalToSphere0.normalize().multiply(above));
                    points3d[4] = point1.add(normal.multiply(bandWidth)).add(normalToSphere1.normalize().multiply(above));
                    points3d[5] = point1.subtract(normal.multiply(bandWidth)).add(normalToSphere1.normalize().multiply(above));
                    faces = new int[]{2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2};
                    if (strokeLineCap == StrokeLineCap.ROUND) {
                        if (pos == 0) {
                            final Point3D tangent = point0.crossProduct(new Point3D(1, 0, 0));
                            final Point3D[] coordinates = BandCap3D.circle(point0, tangent, bandWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point0, coordinates, geom, above, false));
                        }
                        {
                            final Point3D tangent = point1.crossProduct(new Point3D(1, 0, 0));
                            final Point3D[] coordinates = BandCap3D.circle(point1, tangent, bandWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point1, coordinates, geom, above, false));
                        }
                    }
                    break;
                }
// array
// correct
// needs a new normal vector for each point
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
                case Hyperbolic:
                    final Point3D diff = point0.subtract(point1);
                    final Point3D normalToSphere0 = Tools.getNormalVector(point0, geom); // normal vector for point 0 of points3d
                    final Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
                    final Point3D refPoint1 = point0.add(diff); // direction of line used in function below to orientate points
                    final Point3D refPoint2 = point1.add(diff);
                    final Point3D[] pointsForStart = Tools.equidistantHyperbolicPoints(point0, refPoint1, bandWidth);
                    final Point3D[] pointsForEnd = Tools.equidistantHyperbolicPoints(point1, refPoint2, bandWidth);
                    points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));
                    points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));
                    final Point3D normalToSphere2 = Tools.getHyperbolicNormal(pointsForStart[0]);
                    points3d[2] = pointsForStart[0].add(normalToSphere2.normalize().multiply(above));
                    final Point3D normalToSphere3 = Tools.getHyperbolicNormal(pointsForStart[1]);
                    points3d[3] = pointsForStart[1].add(normalToSphere3.normalize().multiply(above));
                    final Point3D normalToSphere4 = Tools.getHyperbolicNormal(pointsForEnd[0]);
                    points3d[4] = pointsForEnd[0].add(normalToSphere4.normalize().multiply(above));
                    final Point3D normalToSphere5 = Tools.getHyperbolicNormal(pointsForEnd[1]);
                    points3d[5] = pointsForEnd[1].add(normalToSphere5.normalize().multiply(above));
                    faces = new int[]{2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2};
                    if (strokeLineCap == StrokeLineCap.ROUND) {
                        if (pos == 0) {
                            final Point3D tangent = point1.subtract(point0);
                            final Point3D[] coordinates = BandCap3D.circle(point0, tangent, bandWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point0, coordinates, geom, above, false));
                        }
                        {
                            final Point3D tangent = point0.add(point1);
                            final Point3D[] coordinates = BandCap3D.circle(point1, tangent, bandWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point1, coordinates, geom, above, false));
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid case");
            }

            // creates mesh
            float[] points = new float[3 * points3d.length];

            for (int i = 0; i < points3d.length; i++) {
                points[3 * i] = (float) points3d[i].getX();
                points[3 * i + 1] = (float) points3d[i].getY();
                points[3 * i + 2] = (float) points3d[i].getZ();
            }

            int[] smoothing = new int[faces.length / 6];
            Arrays.fill(smoothing, 1);

            final TriangleMesh mesh = new TriangleMesh();
            mesh.getPoints().addAll(points);
            mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);
            mesh.getFaces().addAll(faces);
            mesh.getFaceSmoothingGroups().addAll(smoothing);
            meshes.add(mesh);
        }
        return MeshUtils.combineTriangleMeshes(meshes);
    }
}
