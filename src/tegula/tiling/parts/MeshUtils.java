/*
 * MeshUtils.java Copyright (C) 2020. Daniel H. Huson
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

import javafx.collections.ObservableFloatArray;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import jloda.util.Triplet;

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
     * @param mesh
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
     * combine a collection of triangle meshes
     *
     * @return combined mesh
     */
    public static TriangleMesh combineTriangleMeshes(TriangleMesh... meshes) {
        if (meshes.length > 0 && meshes[0].getVertexFormat() != VertexFormat.POINT_TEXCOORD)
            throw new RuntimeException("Unsupported vertex format");

        final TriangleMesh newMesh = new TriangleMesh();

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
                            newMesh.getPoints().addAll(point.get1(), point.get2(), point.get3());
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

}
