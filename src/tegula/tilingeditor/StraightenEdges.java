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

package tegula.tilingeditor;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;
import tegula.tiling.Generators;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * straighten edges
 * Ruediger Zeller and Daniel Huson, 2017
 */
public class StraightenEdges {
    /**
     * straighten all edges
     */
    public static void straightenAllEdges(FDomain fDomain) {
        final DSymbol ds = fDomain.getDSymbol();
        final BitSet visited = new BitSet();

        for (int a = 1; a <= ds.size(); a = ds.nextOrbit(0, 2, a, visited))
            straightenEdge(fDomain, a);
    }

    /**
     * straighten all edges
     */
    public static void straightenEdges(FDomain fDomain, ArrayList<Integer> edges) {
        final DSymbol ds = fDomain.getDSymbol();

        for (Integer edge : edges) {
            final int a = ds.getFlagForOrbit(0, 2, edge);
            if (a >= 1)
                straightenEdge(fDomain, a);
        }
    }

    /**
     * straighten a specific edge
     *
     * @param edgeFlag
     */
    public static void straightenEdge(FDomain fDomain, int edgeFlag) {
        final Generators generators = fDomain.getGenerators();
        final DSymbol ds = fDomain.getDSymbol();

        if (edgeFlag < 1 || edgeFlag > ds.size())
            throw new RuntimeException(String.format("straighten_edge(flag=%d): edge not in 1..%d", edgeFlag, ds.size()));

        final int[] orbit02 = new int[4]; // four flags around edge

        orbit02[0] = edgeFlag;

        if (ds.getS0(orbit02[0]) != orbit02[0])
            orbit02[1] = ds.getS0(orbit02[0]);
        else
            orbit02[1] = 0;

        if (ds.getS2(orbit02[0]) != orbit02[0] && ds.getS2(orbit02[0]) != orbit02[1])
            orbit02[2] = ds.getS2(orbit02[0]);
        else
            orbit02[2] = 0;

        if (orbit02[2] != 0 && ds.getS0(orbit02[2]) != orbit02[0] && ds.getS0(orbit02[2]) != orbit02[1] && ds.getS0(orbit02[2]) != orbit02[2])
            orbit02[3] = ds.getS0(orbit02[2]);
        else
            orbit02[3] = 0;

        for (int a : orbit02) {
            if (a != 0) {
                final Point2D aPt = fDomain.getVertex(0, a);
                final Point2D bPt = fDomain.getVertex(0, ds.getS0(a));
                final Point3D aPt3d = Tools.map2Dto3D(fDomain.getGeometry(), aPt);
                Point3D bPt3d = Tools.map2Dto3D(fDomain.getGeometry(), bPt);
                if (fDomain.isBoundaryEdge(0, a)) {
                    Transform gen = generators.get(0, ds.getS0(a));
                    bPt3d = gen.transform(bPt3d);
                }

                final Point3D cPt3d1 = Tools.midpoint3D(fDomain.getGeometry(), aPt3d, bPt3d);
                final Point2D cPt1 = Tools.map3Dto2D(fDomain.getGeometry(), cPt3d1);
                fDomain.setVertex(cPt1, 1, a);

                final Point3D cPt3d2 = Tools.midpoint3D(fDomain.getGeometry(), aPt3d, cPt3d1);
                final Point2D cPt2 = Tools.map3Dto2D(fDomain.getGeometry(), cPt3d2);
                fDomain.setEdgeCenter(cPt2, 2, a);
            }
        }

        // Straighten all edges of chambers and recompute chamber centers (= mass point of chamber):
        for (int a = 1; a <= ds.size(); a++) {
            final Point3D A = fDomain.getVertex3D(0, a);
            final Point3D B = fDomain.getVertex3D(1, a);
            final Point3D C = fDomain.getVertex3D(2, a);
            final Point2D AB = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), A, B));
            final Point2D AC = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), A, C));
            final Point2D BC = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), B, C));

            fDomain.setEdgeCenter(BC, 0, a);
            fDomain.setEdgeCenter(AC, 1, a);
            fDomain.setEdgeCenter(AB, 2, a);

            final Point2D vec = (fDomain.getVertex(2, a).subtract(AB)).multiply(0.33333);
            fDomain.setChamberCenter(AB.add(vec), a);
        }
    }

    /**
     * Straighten 0- and 1- edges
     */
    public static void straighten01Edges(final FDomain fDomain) {

        if (fDomain.getGeometry() != Geometry.Euclidean)
            return; // todo: implement for other two geometries

        // Straighten 0- and 1-edges
        for (int a = 1; a <= fDomain.size(); a++) {
            // Midpoint between 1- and 2-vertex = new 0-edge center
            {
                final Point3D pt3d = (fDomain.getVertex3D(1, a).add(fDomain.getVertex3D(2, a))).multiply(0.5);
                final Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
                fDomain.setEdgeCenter(pt2d, 0, a);
            }
            // Midpoint between 0- and 2-vertex = new 1-edge center
            {
                final Point3D pt3d = (fDomain.getVertex3D(0, a).add(fDomain.getVertex3D(2, a))).multiply(0.5);
                final Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
                fDomain.setEdgeCenter(pt2d, 1, a);
            }
            // Recompute chamber center
            {
                final Point3D vec = (fDomain.getVertex3D(2, a).subtract(fDomain.getEdgeCenter3D(2, a))).multiply(0.33333);
                final Point3D pt3d = fDomain.getEdgeCenter3D(2, a).add(vec);
                final Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
                fDomain.setChamberCenter(pt2d, a);
            }
        }
    }
}
