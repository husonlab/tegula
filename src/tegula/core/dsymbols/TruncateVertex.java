/*
 * TruncateVertex.java Copyright (C) 2020. Daniel H. Huson
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

/**
 * truncate a vertex
 * Daniel Huson, 5.2019
 */
public class TruncateVertex {
    /*
     ** transforms Delaney symbol corresponding to the following geometrical
     ** operation: truncates a vertex of the corrresponding tiling
     */
    public static DSymbol apply(int vertex, DSymbol ds0) {
        final DSymbol ds = new DSymbol(ds0);
        final int start;
        {
            int a = ds.getFlagForOrbit(1, 2, vertex);
            if (a < 1)
                return ds;

            /* sets 'a' on 1- or 2-fixpoint, if existent */
            if (ds.hasFixPoints(1, 2, a)) {
                for (int i = 1; ds.getS1(a) != a && ds.getS2(a) != a; i = (i == 1 ? 2 : 1))
                    a = ds.getSi(i, a);
            }
            start = a;
        }

        // a1, a2;	/* insert position in old 1-2-orbit */
        // e1, e2, /* new flags to be inserted into old 1-2-orbit */
        //  b1, b2, /* new flags belonging to 0-1-orbit */
        // b0, b   /* used to define 1-operation on new 0-1-orbit */


        // transform m12 on old 1-2-orbit: (all touched nodes will have m12=3)
        final int m01New = ds.getM12(start);
        ds.setM12(start, 3);

        int a1 = start;
        int b0 = 0, b = -1;
        do {
            int a2 = ds.getS1(a1);
            /* transforms m01 on old 0-1-orbit : */
            ds.setMij(0, 1, a1, ds.getMij(0, 1, a1) + (2 - (ds.hasFixPoints(0, 1, a1) ? 0 : 1) - (a1 == a2 ? 1 : 0)) * ds.getVij(0, 1, a1));

            // M01(a1,ds)+=(2-nofix(0,1,a1,ds)-del(a1,a2)) *M01(a1,ds)/orblen(0,1,a1,ds);

            if (a1 != a2) {    /* not 0-fixpoint */
                final int e1 = newFlag(ds.getM01(a1), ds);
                final int e2 = newFlag(ds.getM01(a2), ds);

                final int b1 = newFlag(m01New, ds);
                final int b2 = newFlag(m01New, ds);

                ds.setS1(a1, e1);
                ds.setS0(e1, e2);
                ds.setS1(a2, e2);
                ds.setS2(e1, b1);
                ds.setS0(b1, b2);
                ds.setS2(b2, e2);
                ds.setS1(b2, b2);
                ds.setS1(b0 == 0 ? (b = b1) : b0, b1);

                a1 = ds.getS2(a2);
                if (a1 == start) {    /* end of 1-2-cycle */
                    ds.setS1(b, b2);
                    return ds;
                }
                b0 = b2;
            } else {    /* 0-fixpoint */
                final int e1 = newFlag(ds.getM01(a1), ds);
                final int b1 = newFlag(m01New, ds);

                ds.setS1(a1, e1);
                ds.setS0(e1, e1);
                ds.setS2(b1, e1);
                ds.setS0(b1, b1);
                ds.setS1(b0 == 0 ? (b = b1) : b0, b1);
                if (a1 == start)
                    /* begin of 1-2-chain */
                    a1 = ds.getS2(a1);
                else
                    /* end of 1-2-chain */
                    return ds;
                b0 = b1;
            }
        }
        while (a1 != ds.getS2(a1));
        /* end of 1-2-chain reached */
        return ds;
    }

    private static int newFlag(int m01, DSymbol ds) {
        final int a = ds.size() + 1;
        ds.resize(a);
        ds.setMatrixIJ(0, 1, a, m01);
        ds.setMatrixIJ(1, 2, a, 3);
        ds.setMatrixIJ(0, 2, a, 2);
        return a;
    }
}
