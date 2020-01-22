/*
 * GlueTilesAroundEdge.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.core.dsymbols;

import jloda.util.BitSetUtils;

import java.util.BitSet;

public class GlueTilesAroundEdge {
    public static DSymbol apply(int edge, DSymbol ds0) {
        final DSymbol ds = new DSymbol(ds0);

        final int a = ds.getFlagForOrbit(0, 2, edge);
        if (a > 0) {
            if (c2_edge_to_glue(a, ds))
                glue_c2_edge(a, ds);
            else if (d1_edge_to_glue(a, ds))
                glue_d1_edge(a, ds);
        } else
            System.err.println("Edge not found: " + edge);
        return ds;
    }

    public static boolean isApplicable(int edge, DSymbol ds) {
        final int a = ds.getFlagForOrbit(0, 2, edge);

        return a > 0 && (c2_edge_to_glue(a, ds) || d1_edge_to_glue(a, ds));
    }

    private static void glue_c2_edge(int p, DSymbol ds) {
        final int[] a = new int[4];
        final int[] b = new int[4];

        a[3] = p;
        a[2] = ds.getS1(p);
        b[3] = ds.getS0(p);
        b[2] = ds.getS1(b[3]);

        final int m12 = ds.getM12(p) - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, p, p, ds);
        final int m01 = 2 * ds.getM01(p) - 2;
        ds.setS1(a[2], b[2]);
        ds.setM01(a[2], m01);
        ds.setM12(a[2], m12);

        final BitSet unused = new BitSet();
        if (m12 == 2) {
            unused.or(DSymbolUtils.detachDiVertex(a[2], ds));
        }

        BitSetUtils.addAll(unused, a[3], b[3]);
        DSymbolUtils.removeUnused(unused, ds);
    }

    /*
     ** determines whether p points to a 0,2-cycle corresponding to a c2-edge
     ** along which fundamental tiles can be glued
     */
    private static boolean c2_edge_to_glue(int p, DSymbol ds) {
        final int q = ds.getS0(p);

        if (p == q || q != ds.getS2(p))    /*	cycle?		*/
            return false;
        return ds.hasTrivialStabilizer(0, 1, p);
    }

    private static void glue_d1_edge(int p, DSymbol ds) {
        final int[] a = new int[4];
        final int[] b = new int[4];

        a[3] = p;
        a[2] = ds.getS1(p);
        b[3] = ds.getS0(p);
        b[2] = ds.getS1(b[3]);

        final int m12a = ds.getM12(p) - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, p, p, ds);
        final int m12b;
        if (ds.inSameOrbit(1, 2, a[3], b[3]))
            m12b = 0;
        else
            m12b = ds.getM12(b[3]) - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, b[3], b[3], ds);
        final int m01 = 2 * (ds.getM01(p) - 1);
        ds.setS1(a[2], a[2]);
        ds.setS1(b[2], b[2]);
        ds.setM01(a[2], m01);
        ds.setM12(a[2], m12a);
        if (m12b > 0)
            ds.setM12(b[2], m12b);

        final BitSet unused = new BitSet();
        if (m12a == 2)
            unused.or(DSymbolUtils.detachDiVertex(a[2], ds));
        if (m12b == 2)
            unused.or(DSymbolUtils.detachDiVertex(b[2], ds));

        BitSetUtils.addAll(unused, a[3], b[3]);
        DSymbolUtils.removeUnused(unused, ds);
    }

    private static boolean d1_edge_to_glue(int p, DSymbol ds) {
        final int q = ds.getS0(p);


        if (p == q || p != ds.getS2(p))    /*	chain?		*/
            return false;
        return ds.hasTrivialStabilizer(0, 1, p);
    }
}
