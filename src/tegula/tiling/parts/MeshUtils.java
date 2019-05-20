/*
 * MeshUtils.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

public class MeshUtils {

    /**
     * produce a copy with a reverse orientation
     *
     * @param mesh
     * @return copy with reverse orientation
     */
    public static TriangleMesh reverseOrientation(TriangleMesh mesh) {
        final TriangleMesh result = new TriangleMesh();
        result.getPoints().addAll(mesh.getPoints());
        result.getTexCoords().addAll(mesh.getTexCoords());

        final int[] faces = new int[mesh.getFaces().size()];
        for (int i = 0; i < faces.length; i += 6) {
            if (mesh.getVertexFormat() == VertexFormat.POINT_TEXCOORD) {
                faces[i] = mesh.getFaces().get(i);
                faces[i + 1] = mesh.getFaces().get(i + 1);

                faces[i + 2] = mesh.getFaces().get(i + 4);
                faces[i + 3] = mesh.getFaces().get(i + 5);

                faces[i + 4] = mesh.getFaces().get(i + 2);
                faces[i + 5] = mesh.getFaces().get(i + 3);
            } else {
                throw new RuntimeException("Not implemented");
            }
        }

        result.getFaces().addAll(faces);
        result.getPoints().addAll(mesh.getPoints());
        return result;
    }

    /**
     * combines two Triangle Meshes by Cornelius 21.11.18
     *
     * @param mesh1
     * @param mesh2
     * @return combined mesh of mesh1 and mesh2
     */
    public static TriangleMesh combineTriangleMesh(TriangleMesh mesh1, TriangleMesh mesh2) {

        TriangleMesh newMesh = new TriangleMesh(); // mesh that is returned later
        int mesh1pointsize = mesh1.getPoints().size(); // number of points of mesh1
        int mesh1facesize = mesh1.getFaces().size(); // number of faces of mesh1
        int facesize = mesh1facesize + mesh2.getFaces().size();// number of faces for new mesh

        // recalculates which points belong to which face
        int[] faces = new int[facesize];
        // no changes for faces of mesh1
        for (int i = 0; i < mesh1facesize; i++) {
            faces[i] = mesh1.getFaces().get(i);
        }
        // changes for mesh2
        for (int i = mesh1facesize; i < facesize; i = i + 6) {

            faces[i] = mesh2.getFaces().get(i - mesh1facesize) + (mesh1pointsize / 3);
            faces[i + 1] = 0;
            faces[i + 2] = mesh2.getFaces().get(i + 2 - mesh1facesize) + (mesh1pointsize / 3);
            faces[i + 3] = 1;
            faces[i + 4] = mesh2.getFaces().get(i + 4 - mesh1facesize) + (mesh1pointsize / 3);
            faces[i + 5] = 2;

        }

        // points can be added easily
        newMesh.getPoints().addAll(mesh1.getPoints());
        newMesh.getPoints().addAll(mesh2.getPoints());

        // handles smoothing groups of mesh
        newMesh.getFaceSmoothingGroups().addAll(mesh1.getFaceSmoothingGroups());
        newMesh.getFaceSmoothingGroups().addAll(mesh2.getFaceSmoothingGroups());

        newMesh.getFaces().addAll(faces);

        return newMesh;

    }
}
