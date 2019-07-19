/*
 * MapImageToGeometry.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tiling;

import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import jloda.util.Pair;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

import java.util.HashMap;
import java.util.Map;

/**
 * methods for mapping images onto a sphere
 */
public class MapImageToGeometry {
    /**
     * maps all items onto the desired geometry
     *
     * @param items
     * @return mapped items
     */
    public static Group apply(Geometry geometry, Group items, int maxNumberOfTriangles) {
        final Group result = new Group();

        for (Node item : items.getChildren()) {
            if (item instanceof ImageView) {
                final ImageView imageView = ((ImageView) item);

                final Bounds bounds = item.getBoundsInLocal();
                final Point2D[] points2D = {
                        item.localToParent(new Point2D(bounds.getMinX(), bounds.getMinY())), item.localToParent(new Point2D(bounds.getMinX(), bounds.getMaxY())),
                        item.localToParent(new Point2D(bounds.getMaxX(), bounds.getMaxY())), item.localToParent(new Point2D(bounds.getMaxX(), bounds.getMinY()))};

                for (int i = 0; i < points2D.length; i++) {
                    points2D[i] = new Point2D(points2D[i].getX() / 100, points2D[i].getY() / 100);
                }

                final Point3D[] points3D = {Tools.map2Dto3D(geometry, points2D[0]), Tools.map2Dto3D(geometry, points2D[1]),
                        Tools.map2Dto3D(geometry, points2D[2]), Tools.map2Dto3D(geometry, points2D[3])};

                final TriangleMesh mesh = computeTriangleMesh(geometry, maxNumberOfTriangles, points3D);

                moveSlightlyAbove(geometry, mesh);


                final MeshView meshView = new MeshView(mesh);
                if (true) {
                    final PhongMaterial material = new PhongMaterial();
                    material.setDiffuseMap(imageView.getImage());
                    meshView.setMaterial(material);
                } else {
                    meshView.setMaterial(new PhongMaterial(Color.ORANGE));
                    meshView.setDrawMode(DrawMode.LINE);
                }


                result.getChildren().add(meshView);
            }
        }
        return result;
    }

    /**
     * compute a triangle mesh for the given four points
     *
     * @param geometry
     * @param maxNumberOfTriangles
     * @param points3D
     * @return mesh
     */
    private static TriangleMesh computeTriangleMesh(Geometry geometry, int maxNumberOfTriangles, Point3D[] points3D) {
        TriangleMesh mesh = computeMeshOnFourPoints(points3D);

        int count = 0;

        while (mesh.getFaces().size() / 6 < maxNumberOfTriangles) {
            refine(geometry, mesh);
            if (++count == 4)
                break;
        }
        return mesh;
    }

    /**
     * refine a mesh
     *
     * @param geometry
     * @param mesh
     */
    public static void refine(Geometry geometry, TriangleMesh mesh) {

        final Map<Pair<Integer, Integer>, Integer> pair2point = new HashMap<>(); // each pair of ordered faces is mapped to a new  point in mesh
        final Map<Pair<Integer, Integer>, Integer> pair2texCoord = new HashMap<>(); // each pair of ordered faces is mapped to a new  texture coordinate

        final ObservableFloatArray meshPoints = mesh.getPoints();
        final Point3D[] points3D = new Point3D[meshPoints.size() / 3];
        final Point2D[] texPoints = new Point2D[mesh.getTexCoords().size() / 2];
        for (int i = 0; i < points3D.length; i++) {
            points3D[i] = new Point3D(meshPoints.get(3 * i), meshPoints.get(3 * i + 1), meshPoints.get(3 * i + 2));
        }
        for (int i = 0; i < texPoints.length; i++) {
            texPoints[i] = new Point2D(mesh.getTexCoords().get(2 * i), mesh.getTexCoords().get(2 * i + 1));
        }

        final ObservableIntegerArray nextFaces = FXCollections.observableIntegerArray();

        final ObservableIntegerArray faces = mesh.getFaces();
        for (int f = 0; f < mesh.getFaces().size(); f += 6) {
            final int[] threePoints = new int[3];
            final int[] threeTexCoord = new int[3];
            for (int i = 0; i < 3; i++) {
                threePoints[i] = faces.get(f + 2 * i);
                threeTexCoord[i] = faces.get(f + 2 * i + 1);
            }

            // add edge midpoints:

            for (int i = 0; i < 3; i++) {
                final int ai = threePoints[i];
                final Point3D a = points3D[ai];
                final int at = threeTexCoord[i];

                for (int j = i + 1; j < 3; j++) {
                    final int bi = threePoints[j];
                    final Point3D b = points3D[bi];
                    final int bt = threeTexCoord[j];


                    final Pair<Integer, Integer> pairAB = new Pair<>(Math.min(ai, bi), Math.max(ai, bi));
                    if (!pair2point.containsKey(pairAB)) {
                        final Point3D mid = Tools.midpoint3D(geometry, a, b);
                        mesh.getPoints().addAll((float) mid.getX(), (float) mid.getY(), (float) mid.getZ());
                        final Point2D texMid = texPoints[at].add(texPoints[bt]).multiply(0.5);
                        mesh.getTexCoords().addAll((float) texMid.getX(), (float) texMid.getY());

                        pair2point.put(pairAB, (mesh.getPoints().size() - 1) / 3);
                        pair2texCoord.put(pairAB, (mesh.getTexCoords().size() - 1) / 2);
                    }
                }
            }

            // now setup four new triangles:
            final int[] sixPoints = {threePoints[0], pair2point.get(new Pair<>(Math.min(threePoints[0], threePoints[1]), Math.max(threePoints[0], threePoints[1]))),
                    threePoints[1], pair2point.get(new Pair<>(Math.min(threePoints[1], threePoints[2]), Math.max(threePoints[1], threePoints[2]))),
                    threePoints[2], pair2point.get(new Pair<>(Math.min(threePoints[2], threePoints[0]), Math.max(threePoints[2], threePoints[0])))
            };
            final int[] sixTexCoords = {threeTexCoord[0], pair2texCoord.get(new Pair<>(Math.min(threePoints[0], threePoints[1]), Math.max(threePoints[0], threePoints[1]))),
                    threeTexCoord[1], pair2texCoord.get(new Pair<>(Math.min(threePoints[1], threePoints[2]), Math.max(threePoints[1], threePoints[2]))),
                    threeTexCoord[2], pair2texCoord.get(new Pair<>(Math.min(threePoints[2], threePoints[0]), Math.max(threePoints[2], threePoints[0])))
            };
            nextFaces.addAll(sixPoints[0], sixTexCoords[0], sixPoints[1], sixTexCoords[1], sixPoints[5], sixTexCoords[5]);
            nextFaces.addAll(sixPoints[1], sixTexCoords[1], sixPoints[2], sixTexCoords[2], sixPoints[3], sixTexCoords[3]);
            nextFaces.addAll(sixPoints[3], sixTexCoords[3], sixPoints[4], sixTexCoords[4], sixPoints[5], sixTexCoords[5]);
            nextFaces.addAll(sixPoints[1], sixTexCoords[1], sixPoints[3], sixTexCoords[3], sixPoints[5], sixTexCoords[5]);
        }

        mesh.getFaces().setAll(nextFaces);
    }

    /**
     * move mesh slightly above the tiling
     *
     * @param geometry
     * @param mesh
     */
    private static void moveSlightlyAbove(Geometry geometry, TriangleMesh mesh) {
        final ObservableFloatArray points = mesh.getPoints();
        for (int i = 0; i < mesh.getPoints().size(); i += 3) {
            final Point3D point = Tools.moveSlightlyAbove(geometry, new Point3D(points.get(i), points.get(i + 1), points.get(i + 2)));
            points.set(i, (float) point.getX());
            points.set(i + 1, (float) point.getY());
            points.set(i + 2, (float) point.getZ());
        }
    }

    /**
     * Setup triangle mesh on four points
     *
     * @param points3D
     * @return mesh
     */
    private static TriangleMesh computeMeshOnFourPoints(Point3D[] points3D) {
        if (points3D.length != 4)
            throw new IllegalArgumentException("points3D: must have length 4");

        final TriangleMesh mesh = new TriangleMesh();
        for (Point3D point : points3D) {
            mesh.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ());
        }
        final float[] textureCoordinates = {0, 0, 0, 1, 1, 1, 1, 0};
        mesh.getTexCoords().addAll(textureCoordinates);

        mesh.getFaces().addAll(0, 0, 1, 1, 2, 2);
        mesh.getFaces().addAll(0, 0, 2, 2, 3, 3);

        return mesh;
    }

    public static boolean sideLengthsBelowThreshold(Geometry geometry, TriangleMesh mesh, double maxSideLength) {
        final ObservableFloatArray points = mesh.getPoints();
        final Point3D[] points3D = new Point3D[points.size()];
        for (int i = 0; i < points3D.length; i += 3) {
            points3D[i / 3] = new Point3D(points.get(i), points.get(i + 1), points.get(i + 2));
        }
        final ObservableIntegerArray faces = mesh.getFaces();
        for (int i = 0; i < mesh.getFaces().size(); i += 6) {
            final Point3D a = points3D[faces.get(i)];
            final Point3D b = points3D[faces.get(i + 2)];
            final Point3D c = points3D[faces.get(i + 4)];
            if (longestSide(geometry, a, b, c) > maxSideLength)
                return false;
        }
        return true;
    }


    public static double longestSide(Geometry geometry, Point3D a, Point3D b, Point3D c) {
        return Math.max(Tools.geometricDistance(geometry, a, b), Math.max(Tools.geometricDistance(geometry, a, c), Tools.geometricDistance(geometry, b, b)));
    }
}
