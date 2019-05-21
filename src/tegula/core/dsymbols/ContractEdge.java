/*
 * ContractEdge.java Copyright (C) 2019. Daniel H. Huson
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
                ds.setM01(b, ds0.getM01(b) - numberOfIncidences(0, 1, 0, 2, b, a, ds0));
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
            final int m = ds0.getM12(a0) + numberOfIncidences(1, 2, 0, 2, a0, a, ds0) * (ds.getM12(a) - 2);
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

        removeUnused(toDelete, ds);

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
        int[] a2edge = ds.computeOrbits(0, 2);
        int a = 1;
        while (a <= ds.size()) {
            if (a2edge[a] == edge)
                break;
            a++;
        }
        if (a > ds.size())
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

        if (ds.getVij(1, 2, a) == 1 && numberOfIncidences(1, 2, 0, 2, a, a, ds) == 1
                && (ds.getS2(a) == a || !ds.hasFixPoints(1, 2, a))
                && ds.getM01(a) - numberOfIncidences(0, 1, 0, 2, a, a, ds) > 1
                && (a == a2 || ds.getM01(a2) - numberOfIncidences(0, 1, 0, 2, a2, a2, ds) > 1))
            return a;
        else if (a != a0 && ds.getVij(1, 2, a0) == 1
                && numberOfIncidences(1, 2, 0, 2, a0, a0, ds) == 1
                && (ds.getS2(a0) == a0 || !ds.hasFixPoints(1, 2, a0))
                && ds.getM01(a0) - numberOfIncidences(0, 1, 0, 2, a0, a0, ds) > 1
                && (a0 == a02 || ds.getM01(a02) - numberOfIncidences(0, 1, 0, 2, a02, a02, ds) > 1))
            return a0;
        else
            return 0;
    }


    /*
     ** determines whether the given flag lies between two equivalent tiles
     */
    static boolean isBadEdge(int a, DSymbol ds) {
        return numberOfIncidences(0, 2, 0, 1, a, a, ds) == 2;
    }

    /*
     ** do all tiles have degree 3 or more?
     */
    static boolean hasNoDigons(DSymbol ds) {
        for (int a = 1; a <= ds.size(); a++)
            if (ds.getM01(a) == 2)
                return false;
        return true;
    }

    /*
     ** If X is a vertex, an edge or a face corresponding to the i,j-orbit
     ** containing flag a and if Y corresponds to the k,l orbit containing flag b
     ** then this function returns the number of elements in the gamma-orbit of
     ** Y that are incident to X
     */
    public static int numberOfIncidences(int i, int j, int k, int l, int a, int b, DSymbol ds) {
        return (intersection(i, j, k, l, a, b, ds) * (ds.hasFixPoints(i, j, a) ? 2 : 1) * ds.getVij(i, j, a)) / 2;
    }

    /*
     ** returns the cardinality of the intersection of the i,j-orbit containing a
     ** and the i,k-orbit containing b
     */
    public static int intersection(int i, int j, int k, int l, int a, int b, DSymbol ds) {
        final BitSet fla = new BitSet();
        final BitSet flb = new BitSet();
        ds.markOrbit(i, j, a, fla);
        ds.markOrbit(k, l, b, flb);
        return BitSetUtils.intersection(fla, flb).cardinality();
    }

    /**
     * remove unused flags from D-symbol
     *
     * @param unused
     * @param ds
     */
    private static void removeUnused(BitSet unused, DSymbol ds) {
        if (false)
            for (int a = 1; a <= ds.size(); a++) {
                for (int i = 0; i <= 2; i++) {
                    if (ds.getSi(i, a) < 1)
                        System.err.println("S(" + i + "," + a + ")=" + ds.getSi(i, a));
                    if (ds.getSi(i, a) != ds.getSi(i, ds.getSi(i, ds.getSi(i, a))))
                        System.err.println(String.format("ds.getSi(%d,%d)=%d !=%d=ds.getSi(%d,ds.getSi(%d,%d))",
                                i, a, ds.getSi(i, a), ds.getSi(i, ds.getSi(i, ds.getSi(i, a))), i, i, ds.getSi(i, a)));
                }
            }


        final int n_new = ds.size() - unused.cardinality();

        int b = unused.nextClearBit(n_new + 1);

        for (int a : BitSetUtils.members(unused)) {
            if (a > n_new) {
                if (b <= ds.size())
                    throw new RuntimeException("incomplete remove unused");
                break;
            }
            move(b, a, ds);
            b = unused.nextClearBit(b + 1);
        }

        if (false)
            for (int a = 1; a <= n_new; a++) {
                for (int i = 0; i <= 2; i++) {
                    if (ds.getSi(i, a) < 1 || ds.getSi(i, a) > n_new)
                        System.err.println("S(" + i + "," + a + ")=" + ds.getSi(i, a));
                    if (ds.getSi(i, a) != ds.getSi(i, ds.getSi(i, ds.getSi(i, a))))
                        System.err.println(String.format("ds.getSi(%d,%d)=%d !=%d=ds.getSi(%d,ds.getSi(%d,%d))",
                                i, a, ds.getSi(i, a), ds.getSi(i, ds.getSi(i, ds.getSi(i, a))), i, i, ds.getSi(i, a)));
                }
            }
        ds.resize(n_new);

        if (false)
            for (int a = 1; a <= ds.size(); a++) {
                for (int i = 0; i <= 2; i++) {
                    if (ds.getSi(i, a) < 1 || ds.getSi(i, a) > ds.size())
                        System.err.println("S(" + i + "," + a + ")=" + ds.getSi(i, a));
                    if (ds.getSi(i, a) != ds.getSi(i, ds.getSi(i, ds.getSi(i, a))))
                        System.err.println(String.format("ds.getSi(%d,%d)=%d !=%d=ds.getSi(%d,ds.getSi(%d,%d))",
                                i, a, ds.getSi(i, a), ds.getSi(i, ds.getSi(i, ds.getSi(i, a))), i, i, ds.getSi(i, a)));
                }
            }
    }

    private static void move(int src, int target, DSymbol ds) {
        if (target != src) {
            for (int k = 0; k <= 2; k++) {
                if (ds.getSi(k, src) == src)
                    ds.setSi(k, target, target);
                else
                    ds.setSi(k, target, ds.getSi(k, src));

                final int i = DSymbol.i(k);
                final int j = DSymbol.j(k);
                ds.setMatrixIJ(i, j, target, ds.getMij(i, j, src));
            }
        }
    }
}
