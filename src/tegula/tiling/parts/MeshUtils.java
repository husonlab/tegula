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
}
