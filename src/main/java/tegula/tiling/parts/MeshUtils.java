/*
 * MeshUtils.java Copyright (C) 2023 Daniel H. Huson
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

import javafx.collections.ObservableFloatArray;
import javafx.geometry.Point3D;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import jloda.util.Triplet;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * some mesh utilities
 * Daniel Huson, 5.2020
 */
public class MeshUtils {

    /**
     * produce a copy with a reverse orientation
     *
     * @return copy with reverse orientation
     */
    public static TriangleMesh reverseOrientation(TriangleMesh mesh) {
        if (mesh.getVertexFormat() != VertexFormat.POINT_TEXCOORD)
            throw new RuntimeException("Unsupported vertex format");

        final TriangleMesh result = new TriangleMesh();
        result.getPoints().addAll(mesh.getPoints());
        result.getTexCoords().addAll(mesh.getTexCoords());

        final int[] faces = new int[mesh.getFaces().size()];
        for (int i = 0; i < faces.length; i += 6) {
            faces[i] = mesh.getFaces().get(i);
            faces[i + 1] = mesh.getFaces().get(i + 1);

            faces[i + 2] = mesh.getFaces().get(i + 4);
            faces[i + 3] = mesh.getFaces().get(i + 5);

            faces[i + 4] = mesh.getFaces().get(i + 2);
            faces[i + 5] = mesh.getFaces().get(i + 3);
        }

        result.getFaces().addAll(faces);
        result.getPoints().addAll(mesh.getPoints());
        return result;
    }

    /**
     * returns a copy of a mesh with explicit per-vertex normals for smooth shading, taken from the surface
     * the points are known to lie on rather than from the triangles: the sphere of radius 100, or the
     * hyperboloid of the same radius, both centred on the origin, see {@link Tools#getNormalVector}. Without
     * this a curved surface is shaded per triangle and reads as a set of facets. The euclidean plane is flat,
     * so its mesh is returned unchanged.
     *
     * @param inward negate the normals, for a reversed-orientation back-face mesh
     * @return copy in POINT_NORMAL_TEXCOORD format, or src itself in the euclidean case
     */
    public static TriangleMesh withSurfaceNormals(TriangleMesh src, Geometry geom, boolean inward) {
        if (geom != Geometry.Spherical && geom != Geometry.Hyperbolic)
            return src;
        if (src.getVertexFormat() != VertexFormat.POINT_TEXCOORD)
            throw new RuntimeException("Unsupported vertex format");

        final TriangleMesh result = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        final ObservableFloatArray points = src.getPoints();
        result.getPoints().addAll(points);
        result.getTexCoords().addAll(src.getTexCoords());

        // one normal per point: the normal of the surface there, so that neighbouring triangles agree
        final float sign = inward ? -1f : 1f;
        final float[] normals = new float[points.size()];
        for (int i = 0; i < points.size(); i += 3) {
            final Point3D normal = Tools.getNormalVector(new Point3D(points.get(i), points.get(i + 1), points.get(i + 2)), geom);
            normals[i] = (float) (sign * normal.getX());
            normals[i + 1] = (float) (sign * normal.getY());
            normals[i + 2] = (float) (sign * normal.getZ());
        }
        result.getNormals().addAll(normals);

        // rebuild faces: each (point,texCoord) pair becomes (point,normal,texCoord), reusing the point index for the normal
        final ObservableFaceArray srcFaces = src.getFaces();
        final int[] faces = new int[srcFaces.size() / 2 * 3];
        int j = 0;
        for (int i = 0; i < srcFaces.size(); i += 2) {
            final int p = srcFaces.get(i);
            faces[j++] = p;
            faces[j++] = p;
            faces[j++] = srcFaces.get(i + 1);
        }
        result.getFaces().addAll(faces);
        return result;
    }

    /**
     * combine a collection of triangle meshes
     *
     * @return combined mesh
     */
    public static TriangleMesh combineTriangleMeshes(Collection<TriangleMesh> meshes) {
        return combineTriangleMeshes(meshes.toArray(new TriangleMesh[0]));
    }

    /**
     * combine a collection of triangle meshes
     *
     * @return combined mesh
     */
    public static TriangleMesh combineTriangleMeshes(TriangleMesh... meshes) {
        final TriangleMesh newMesh = new TriangleMesh();
        MeshUtils.setDefaultTexCoordinates(newMesh);

        if (meshes.length == 0)
            return newMesh;
        else if (meshes.length == 1)
            return meshes[0];
        else if (meshes[0].getVertexFormat() != VertexFormat.POINT_TEXCOORD)
            throw new RuntimeException("Unsupported vertex format");

        final int numberOfMeshes = meshes.length;

        final int[][] pointsIndex = new int[numberOfMeshes][];
        for (int m = 0; m < numberOfMeshes; m++) {
            pointsIndex[m] = new int[meshes[m].getPoints().size() / 3];
        }

        // setup points
        {
            final Map<Triplet<Float, Float, Float>, Integer> point2index = new HashMap<>();
            int count = 0;
            for (int m = 0; m < numberOfMeshes; m++) {
                final int[] index = pointsIndex[m];
                final ObservableFloatArray points = meshes[m].getPoints();
                {
                    int p = 0;
                    for (int i = 0; i < points.size(); i += 3) {
                        final Triplet<Float, Float, Float> point = new Triplet<>(points.get(i), points.get(i + 1), points.get(i + 2));
                        Integer pos = point2index.get(point);
                        if (pos == null) {
                            pos = count++;
                            point2index.put(point, pos);
                            newMesh.getPoints().addAll(point.getFirst(), point.getSecond(), point.getThird());
                        }
                        index[p++] = pos;
                    }
                }
            }
            //System.err.println("points in: "+( mesh1.getPoints().size()+ mesh2.getPoints().size())+" out: "+newMesh.getPoints().size());
        }

        // setup faces
        {
            for (int m = 0; m < numberOfMeshes; m++) {
                final int[] index = pointsIndex[m];
                final ObservableFaceArray faces = meshes[m].getFaces();
                for (int i = 0; i < faces.size(); i += 2) {
                    newMesh.getFaces().addAll(index[faces.get(i)], faces.get(i + 1));
                    //newMesh.getFaces().addAll(faces.get(i),faces.get(i+1));
                }
            }
            // System.err.println("faces in: "+( mesh1.getFaces().size()+ mesh2.getFaces().size())+" out: "+newMesh.getFaces().size());
        }

        // add smoothing groups
        for (TriangleMesh mesh : meshes)
            newMesh.getFaceSmoothingGroups().addAll(mesh.getFaceSmoothingGroups());

        return newMesh;

    }

    /**
     * create a fan mesh
     */
    public static TriangleMesh createFan(Point3D center, Point3D... points) {
        final TriangleMesh mesh = new TriangleMesh();

        mesh.getPoints().addAll((float) center.getX(), (float) center.getY(), (float) center.getZ());
        for (Point3D point : points)
            mesh.getPoints().addAll((float) point.getX(), (float) point.getY(), (float) point.getZ());

        final int numberOfPoints = points.length + 1;

        for (int i = 1; i + 1 < numberOfPoints; i++) {
            mesh.getFaces().addAll(0, 0, i, 1, i + 1, 2);
        }
        setDefaultTexCoordinates(mesh);
        setSmoothGroup(mesh, 1);

        return mesh;
    }

    public static void setDefaultTexCoordinates(TriangleMesh mesh) {
        mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);
    }

    public static void setSmoothGroup(TriangleMesh mesh, int group) {
        for (int i = 0; i < mesh.getFaces().size() / 6; i++)
            mesh.getFaceSmoothingGroups().addAll(group);
    }
}
