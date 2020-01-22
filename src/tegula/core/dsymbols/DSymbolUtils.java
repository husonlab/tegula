/*
 * DSymbolUtils.java Copyright (C) 2020. Daniel H. Huson
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
 * some utilities
 * Daniel Huson, 5.2019
 */
public class DSymbolUtils {
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
    static void removeUnused(BitSet unused, DSymbol ds) {
        if (true) {
            final int n_new = ds.size() - unused.cardinality();

            for (int a = unused.nextSetBit(0); a != -1 && a <= n_new; a = unused.nextSetBit(a + 1)) {
                final int b = unused.nextClearBit(a + 1);
                if (b == -1)
                    throw new RuntimeException("too few elements");
                move(b, a, ds);
                unused.set(b);
            }
            ds.resize(n_new);
        } else {
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

    /**
     * detach a 1,2-orbit corresponding to a vertex of degree 2 and return the set of flags that need to be removed
     *
     * @param p
     * @param ds
     * @return flags to be removed
     */
    public static BitSet detachDiVertex(int p, DSymbol ds) {
        final BitSet toRemove = new BitSet();

        if (ds.getM12(p) != 2)
            return toRemove;

        final int q = ds.getS1(p);
        final int r = ds.getS2(q);

        final int[] a = new int[5];
        final int[] b = new int[5];


        a[4] = p;
        a[3] = ds.getS0(p);

        final int m01a = ds.getM01(p) - numberOfIncidences(0, 1, 1, 2, p, p, ds);
        if (p == q && q == r) {
            ds.setS0(a[3], a[3]);
            ds.setM01(a[3], m01a);
            toRemove.set(a[4]);
        } else if (p != q && q == r) {
            b[4] = ds.getS1(a[4]);
            b[3] = ds.getS0(b[4]);

            ds.setS0(a[3], b[3]);
            ds.setM01(a[3], m01a);
            toRemove.set(a[4], b[4]);
        } else if (p == q && q != r) {
            a[2] = ds.getS2(a[4]);
            a[1] = ds.getS2(a[3]);
            ds.setS0(a[3], a[3]);
            ds.setS0(a[1], a[1]);
            ds.setM01(a[3], m01a);


            if (!ds.inSameOrbit(0, 1, p, r)) {
                final int m01b = ds.getM01(r) - numberOfIncidences(0, 1, 1, 2, r, p, ds);
                ds.setM01(a[1], m01b);
            }
            BitSetUtils.addAll(toRemove, a[2], a[4]);
        } else if (p != q && p == r) {
            b[4] = ds.getS1(a[4]);
            b[3] = ds.getS0(b[4]);

            ds.setS0(a[3], b[3]);
            ds.setM01(a[3], m01a);
            BitSetUtils.addAll(toRemove, a[4], b[4]);
        } else if (p != q && q != r) {
            a[2] = ds.getS2(a[4]);
            a[1] = ds.getS2(a[3]);
            b[4] = ds.getS1(a[4]);
            b[3] = ds.getS0(b[4]);
            b[2] = ds.getS2(b[4]);
            b[1] = ds.getS2(b[3]);

            final int m01b;
            if (ds.inSameOrbit(0, 1, p, r))
                m01b = 0;
            else
                m01b = ds.getM01(r) - numberOfIncidences(0, 1, 1, 2, r, p, ds);

            if (a[3] != a[4] && a[3] != a[2]) {
                if (b[3] != b[4] && b[3] != b[2]) {
                    ds.setS0(a[3], b[3]);
                    ds.setS0(a[1], b[1]);
                } else if (b[4] == b[1]) {
                    ds.setS0(a[3], a[1]);
                } else if (b[3] == b[4]) {
                    ds.setS0(a[3], b[3]);
                    ds.setS0(a[1], b[1]);
                }
                ds.setM01(a[3], m01a);
                if (m01b > 0)
                    ds.setM01(a[1], m01b);
            } else /*		b[3]!=b[4] && b[3]!=b[2]	*/ {
                if (a[4] == a[1]) {
                    ds.setS0(b[3], b[1]);
                } else if (a[3] == a[4]) {
                    ds.setS0(b[3], b[3]);
                    ds.setS0(b[1], b[1]);
                }
                ds.setM01(b[3], m01a);
                if (m01b > 0)
                    ds.setM01(b[1], m01b);
            }
            BitSetUtils.addAll(toRemove, a[2], a[4], b[2], b[4]);
        }
        return toRemove;
    }
}
