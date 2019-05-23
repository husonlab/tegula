/*
 * GlueTilesAroundVertex.java Copyright (C) 2019. Daniel H. Huson
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

/**
 * Glues fundamental tiles into non-fundamental tiles.
 * All tilings produced are tilings by disks, each having two or more edges.
 * <p>
 * Based on math by Delone, Zamorzeweva
 * Daniel Huson, 6.90 8.90, 12.98, 5.2019
 */
public class GlueTilesAroundVertex {

    public static DSymbol apply(int vertex, DSymbol ds0) {
        final int a = ds0.getFlagForOrbit(1, 2, vertex);
        if (a < 1)
            return ds0;

        final DSymbol ds = new DSymbol(ds0);
        if (c_vertex_to_glue(a, ds0)) {
            glue_c_vertex(a, ds);
        } else if (d_vertex_to_glue(a, ds0)) {
            glue_d_vertex(a, ds);

        }
        return ds;
    }


    public static boolean isApplicable(int vertex, DSymbol ds) {
        final int a = ds.getFlagForOrbit(1, 2, vertex);
        return a > 0 && (c_vertex_to_glue(a, ds) || d_vertex_to_glue(a, ds));
    }

    /*
     ** glues tiles together at a vertex with rotational symmetry
     */
    private static void glue_c_vertex(int p, DSymbol ds) {
        final int q = ds.getS1(p);

        final int[] a = new int[5];
        final int[] b = new int[5];

        a[4] = p;
        a[3] = ds.getS0(p);
        a[2] = ds.getS1(a[3]);
        a[1] = ds.getS0(a[2]);
        b[4] = q;
        b[3] = ds.getS0(q);
        b[2] = ds.getS1(b[3]);
        b[1] = ds.getS0(b[2]);

        final int m12 = ds.getM12(a[2]) - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, a[2], a[3], ds);
        final int m01 = (ds.getM01(a[1]) - 2) * ds.getM12(p);
        ds.setS1(a[2], b[2]);
        ds.setM01(a[1], m01);
        ds.setM12(a[2], m12);

        final BitSet unused = new BitSet();
        if (m12 == 2)
            unused.or(DSymbolUtils.detachDiVertex(a[2], ds));

        BitSetUtils.addAll(unused, a[3], a[4], b[3], b[4]);
        DSymbolUtils.removeUnused(unused, ds);
    }

    /*
     ** determines whether p points to a 1,2-cycle corresponding to a vertex
     ** around which fundamental tiles can be glued
     */
    private static boolean c_vertex_to_glue(int p, DSymbol ds) {
        int q = ds.getS1(p);
        if (p == q || q != ds.getS2(p))
            return false;
        return ds.hasTrivialStabilizer(0, 1, p);
    }

    /*
     ** glues tiles together at a vertex with dihedral symmetry
     */
    private static void glue_d_vertex(int p, DSymbol ds) {
        int q = ds.getS1(p);

        final int[] a = new int[5];
        final int[] b = new int[5];

        a[4] = p;
        a[3] = ds.getS0(p);
        a[2] = ds.getS1(a[3]);
        a[1] = ds.getS0(a[2]);
        b[4] = q;
        b[3] = ds.getS0(q);
        b[2] = ds.getS1(b[3]);
        b[1] = ds.getS0(b[2]);


        int m12a, m12b;
        if (ds.inSameOrbit(1, 2, a[2], b[2])) {
            m12a = ds.getM12(a[2])
                    - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, a[2], a[3], ds)
                    - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, b[2], b[3], ds);
            m12b = 0;
        } else {
            m12a = ds.getM12(a[2]) - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, a[2], a[3], ds);
            m12b = ds.getM12(b[2]) - DSymbolUtils.numberOfIncidences(1, 2, 0, 2, b[2], b[3], ds);
        }
        final int m01 = (ds.getM01(p) - 2) * ds.getM12(p);

        ds.setS1(a[2], a[2]);
        ds.setM12(a[2], m12a);
        ds.setS1(b[2], b[2]);

        if (m12b > 0)
            ds.setM01(b[2], m12b);
        ds.setM01(a[1], m01);

        final BitSet unused = new BitSet();
        if (m12a == 2)
            unused.or(DSymbolUtils.detachDiVertex(a[2], ds));
        if (m12b == 2)
            unused.or(DSymbolUtils.detachDiVertex(b[2], ds));

        BitSetUtils.addAll(unused, a[3], a[4], b[3], b[4]);
        DSymbolUtils.removeUnused(unused, ds);
    }

    /*
     ** determines whether p points to a 1,2-chain corresponding to a vertex
     ** around which fundamental tiles can be glued
     */
    private static boolean d_vertex_to_glue(int p, DSymbol ds) {
        int q = ds.getS1(p);

        if (p == q || p != ds.getS2(p) || q != ds.getS2(q))    /*	chain?	*/
            return false;
        return ds.hasTrivialStabilizer(0, 1, p);
    }

}
