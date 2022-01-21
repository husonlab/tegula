/*
 * ContractEdge.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.core.dsymbols;

import java.util.BitSet;

/**
 * contract an edge of a tiling
 */
public class ContractEdge {

    public static DSymbol apply(int edge, DSymbol ds) {
        final int a = getContractEdge(edge, ds);
        if (a > 0)
            return contractEdge(a, ds);
        else
            return ds;
    }

    private static DSymbol contractEdge(int a, DSymbol ds0) {
        final DSymbol ds = new DSymbol(ds0);
        {
            final BitSet fl = new BitSet();
            for (int b = 1; b <= ds.size(); b = ds.nextOrbit(0, 1, b, fl))
                ds.setM01(b, ds0.getM01(b) - DSymbolUtils.numberOfIncidences(0, 1, 0, 2, b, a, ds0));
        }

        final int a0 = ds.getS0(a);
        final int a1 = ds.getS1(a);
        final int a2 = ds.getS2(a);
        final int a01 = ds.getS1(a0);
        final int a20 = ds.getS0(a2);
        final int a21 = ds.getS1(a2);
        final int a201 = ds.getS1(a20);

        if (a == a0) {
            final int m = 2 * (ds0.getM12(a) - 1);
            ds.setM12(a, m);
        } else {
            final int m = ds0.getM12(a0) + DSymbolUtils.numberOfIncidences(1, 2, 0, 2, a0, a, ds0) * (ds.getM12(a) - 2);
            ds.setM12(a, m);
            ds.setM12(a0, m);
        }

        switch (ds.computeOrbitCardinality(0, 2, a)) {
            case 1:
                ds.setS1(a1, a1);
                break;
            case 2:
                if (a0 != a && a2 == a)    /* 2-fix */ {
                    if (ds.getS1(a0) == a0 && ds.getS2(a0) == a0)
                        ds.setS1(a1, a1);        /* whole 1-2-orbit will die */
                    else
                        ds.setS1(a1, a01);
                } else if (a2 != a && a0 == a) /* 0-fix */ {
                    ds.setS1(a1, a1);
                    ds.setS1(a21, a21);

                } else /* no-fix */
                    ds.setS1(a1, a21);
                break;
            case 4:
                if (a01 == a20)
                    ds.setS1(a1, a21);
                else {
                    ds.setS1(a1, a01 == a0 ? a1 : a01);
                    ds.setS1(a21, a201 == a20 ? a21 : a201);
                }
                break;
            default:
                throw new RuntimeException("illegal 0-2-orbit");
        }


        final BitSet toDelete = new BitSet();
        toDelete.set(a);
        toDelete.set(a0);
        toDelete.set(a2);
        toDelete.set(a20);

        DSymbolUtils.removeUnused(toDelete, ds);

        System.err.println(ds.toString());
        return ds;
    }

    /**
     * determines whether the given edge is contractible
     *
     * @param edge
     * @param ds
     * @return 0, if not contractible, otherwise flag to apply contraction to
     */
    public static int getContractEdge(int edge, DSymbol ds) {
        final int a = ds.getFlagForOrbit(0, 2, edge);
        if (a < 1)
            return 0;

/* check whether edge is contractible,
	condition: the stabilizer of one of the two end vertices of the edge must
	be a subgroup of the stabilizer of the edge,
	i.e. the corresponding 1,2 orbit must have v12=1 and must be either a cycle
	or a chain whose 2-fixpoint lies in the 0,2 orbit of the edge
*/
        int a0 = ds.getS0(a);
        int a02 = ds.getS2(a0);
        int a2 = ds.getS2(a);

        if (ds.getVij(1, 2, a) == 1 && DSymbolUtils.numberOfIncidences(1, 2, 0, 2, a, a, ds) == 1
                && (ds.getS2(a) == a || !ds.hasFixPoints(1, 2, a))
                && ds.getM01(a) - DSymbolUtils.numberOfIncidences(0, 1, 0, 2, a, a, ds) > 1
                && (a == a2 || ds.getM01(a2) - DSymbolUtils.numberOfIncidences(0, 1, 0, 2, a2, a2, ds) > 1))
            return a;
        else if (a != a0 && ds.getVij(1, 2, a0) == 1
                && DSymbolUtils.numberOfIncidences(1, 2, 0, 2, a0, a0, ds) == 1
                && (ds.getS2(a0) == a0 || !ds.hasFixPoints(1, 2, a0))
                && ds.getM01(a0) - DSymbolUtils.numberOfIncidences(0, 1, 0, 2, a0, a0, ds) > 1
                && (a0 == a02 || ds.getM01(a02) - DSymbolUtils.numberOfIncidences(0, 1, 0, 2, a02, a02, ds) > 1))
            return a0;
        else
            return 0;
    }


    /*
     ** determines whether the given flag lies between two equivalent tiles
     */
    static boolean isBadEdge(int a, DSymbol ds) {
        return DSymbolUtils.numberOfIncidences(0, 2, 0, 1, a, a, ds) == 2;
    }

}
