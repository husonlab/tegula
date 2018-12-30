/*
 *  Copyright (C) 2018 University of Tuebingen
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

package tiler.core.reshape;

import javafx.geometry.Point2D;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.tiling.Tiling;

import java.util.ArrayList;
import java.util.BitSet;

public class Reshape {

    /**
     * computes all edits that have been applied to this fundamental domain
     *
     * @param fDomain
     * @return edits
     */
    public static ArrayList<ReshapeEdit> computeEdits(FDomain fDomain) {
        final ArrayList<ReshapeEdit> edits = new ArrayList<>();


        if (true)
            return edits; // todo: this doesn't work!!!!

        final DSymbol ds = fDomain.getDSymbol();
        final FDomain original = (new Tiling(ds)).getfDomain(); // compute the original coordinates

        for (int m = 0; m <= 2; m++) {
            final int i, j;
            switch (m) {
                case 0:
                    i = 1;
                    j = 2;
                    break;
                case 1:
                    i = 0;
                    j = 2;
                    break;
                case 2:
                    i = 0;
                    j = 1;
                    break;
                default:
                    throw new RuntimeException("Can't happen");
            }
            final BitSet visited = new BitSet();
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, j, a, visited)) {
                final Point2D offset = fDomain.getVertex(m, a).subtract(original.getVertex(m, a));

                System.err.println("Orig: " + original.getVertex(m, a));
                System.err.println("Curr: " + fDomain.getVertex(m, a));
                System.err.println("Off:  " + offset);

                if (offset.magnitude() > 0) {
                    final int constraint = 0; // todo: set this properly
                    final ReshapeEdit edit = new ReshapeEdit(0, m, a, constraint, offset);
                    edits.add(edit);
                }
            }
        }

        for (int m = 0; m <= 2; m++) {
            for (int a = 1; a <= ds.size(); a++) {
                if (a <= ds.getSi(m, a)) {
                    final Point2D offset = fDomain.getEdgeCenter(m, a).subtract(original.getEdgeCenter(m, a));
                    if (offset.magnitude() > 0) {
                        final int constraint = 0; // todo: set this properly
                        final ReshapeEdit edit = new ReshapeEdit(1, m, a, constraint, offset);
                        edits.add(edit);
                    }
                }
            }
        }

        for (int a = 1; a <= ds.size(); a++) {
            final Point2D offset = fDomain.getChamberCenter(a).subtract(original.getChamberCenter(a));
            if (offset.magnitude() > 0.000001) {
                final ReshapeEdit edit = new ReshapeEdit(2, -1, a, 0, offset);
                edits.add(edit);
            }
        }

        return edits;
    }

    /**
     * applies a list of edits to an fundamental domain for which the symmetry group has changed
     *
     * @param fDomain
     * @param edits
     * @param previousGeometry
     */
    public static void applyEdits(FDomain fDomain, ArrayList<ReshapeEdit> edits, Geometry previousGeometry) {
        final double factor;

        /*
            if (previousGeometry == Geometry.Euclidean && fDomain.getGeometry() != Geometry.Euclidean)
                factor = 0.25;
            else if (previousGeometry != Geometry.Euclidean && fDomain.getGeometry() == Geometry.Euclidean)
                factor = 4;
            else
            */
        factor = 1;

        for (ReshapeEdit edit : edits) {
            // not sure whether we need to substract the ref point here
            final Point2D offset = edit.getOffset().multiply(factor);

            switch (edit.getKind()) {
                case 0: // vertex
                {
                    fDomain.setVertex(fDomain.getVertex(edit.getM(), edit.getA()).add(offset), edit.getM(), edit.getA());
                    break;
                }
                case 1: // edge-center
                {
                    fDomain.setEdgeCenter(fDomain.getEdgeCenter(edit.getM(), edit.getA()).add(offset), edit.getM(), edit.getA());
                    break;
                }
                case 2: // chamber-center
                {
                    fDomain.setChamberCenter(fDomain.getChamberCenter(edit.getA()).add(offset), edit.getA());
                    break;
                }
            }
        }
    }
}
