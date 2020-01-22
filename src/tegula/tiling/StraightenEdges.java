/*
 * StraightenEdges.java Copyright (C) 2020. Daniel H. Huson
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

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.geometry.Tools;

import java.util.BitSet;

/**
 * straighten edges
 * Ruediger Zeller and Daniel Huson, 2017
 */
public class StraightenEdges {

    /**
     * straighten all edges
     */
    public static void straightenAllEdges(TilingBase tiling) {
        final DSymbol ds = tiling.getDSymbol();
        final BitSet visited = new BitSet();

        for (int a = 1; a <= ds.size(); a = ds.nextOrbit(0, 2, a, visited))
            straightenEdge(tiling, a);
    }

    /**
     * straighten a specific edge
     *
     * @param edgeFlag
     */
    public static void straightenEdge(TilingBase tiling, int edgeFlag) {
        final DSymbol ds = tiling.getDSymbol();
        final FDomain fDomain = tiling.getfDomain();
        final Generators generators = tiling.getGenerators();

        int i;
        int[] a = new int[5];

        if (edgeFlag < 1 || edgeFlag > ds.size())
            throw new RuntimeException(String.format("straighten_edge(flag=%d): edge not in 1..%d", edgeFlag, ds.size()));

        a[0] = edgeFlag;

        if (ds.getSi(0, a[0]) != a[0])
            a[1] = ds.getSi(0, a[0]);
        else
            a[1] = 0;
        if (ds.getSi(2, a[0]) != a[0] && ds.getSi(2, a[0]) != a[1])
            a[2] = ds.getSi(2, a[0]);
        else
            a[2] = 0;
        if (a[2] != 0 && ds.getSi(0, a[2]) != a[0] && ds.getSi(0, a[2]) != a[1] && ds.getSi(0, a[2]) != a[2])
            a[3] = ds.getSi(0, a[2]);
        else
            a[3] = 0;

        for (i = 0; i < 4; i++) {
            if (a[i] != 0) {
                Point2D aPt = fDomain.getVertex(0, a[i]);
                Point2D bPt = fDomain.getVertex(0, ds.getSi(0, a[i]));
                Point3D aPt3d = Tools.map2Dto3D(fDomain.getGeometry(), aPt);
                Point3D bPt3d = Tools.map2Dto3D(fDomain.getGeometry(), bPt);
                if (fDomain.isBoundaryEdge(0, a[i])) {
                    Transform gen = generators.get(0, ds.getSi(0, a[i]));
                    bPt3d = gen.transform(bPt3d);
                }

                Point3D cPt3d = Tools.midpoint3D(fDomain.getGeometry(), aPt3d, bPt3d);
                Point2D cPt = Tools.map3Dto2D(fDomain.getGeometry(), cPt3d);
                fDomain.setVertex(cPt, 1, a[i]);

                cPt3d = Tools.midpoint3D(fDomain.getGeometry(), aPt3d, cPt3d);
                cPt = Tools.map3Dto2D(fDomain.getGeometry(), cPt3d);
                fDomain.setEdgeCenter(cPt, 2, a[i]);
            }
        }
        // Straighten all edges of chambers and recompute chamber centers (= mass point of chamber):
        for (int j = 1; j <= fDomain.size(); j++) {
            Point3D A = fDomain.getVertex3D(0, j);
            Point3D B = fDomain.getVertex3D(1, j);
            Point3D C = fDomain.getVertex3D(2, j);
            Point2D AB = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), A, B));
            Point2D AC = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), A, C));
            Point2D BC = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), B, C));

            fDomain.setEdgeCenter(BC, 0, j);
            fDomain.setEdgeCenter(AC, 1, j);
            fDomain.setEdgeCenter(AB, 2, j);

            Point2D vec = (fDomain.getVertex(2, j).subtract(AB)).multiply(0.33333);
            fDomain.setChamberCenter(AB.add(vec), j);
        }
    }

    /**
     * Straighten 0- and 1- edges
     */
    public static void straighten01Edges(final FDomain fDomain) {
        // Straighten 0- and 1-edges
        for (int a = 1; a <= fDomain.size(); a++) {
            // Midpoint between 1- and 2-vertex = new 0-edge center
            Point3D pt3d = (fDomain.getVertex3D(1, a).add(fDomain.getVertex3D(2, a))).multiply(0.5);
            Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
            fDomain.setEdgeCenter(pt2d, 0, a);
            // Midpoint between 0- and 2-vertex = new 1-edge center
            pt3d = (fDomain.getVertex3D(0, a).add(fDomain.getVertex3D(2, a))).multiply(0.5);
            pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
            fDomain.setEdgeCenter(pt2d, 1, a);
            // Recompute chamber center
            Point3D vec = (fDomain.getVertex3D(2, a).subtract(fDomain.getEdgeCenter3D(2, a))).multiply(0.33333);
            pt3d = fDomain.getEdgeCenter3D(2, a).add(vec);
            pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
            fDomain.setChamberCenter(pt2d, a);
        }
    }

}
