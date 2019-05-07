/*
 * DSymbolAlgorithms.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.BitSet;

/**
 * algorithms for modifying Delaney symbols
 * Daniel Huson, 11.2018
 */
public class DSymbolAlgorithms {
    /**
     * computes the dual Delaney symbol
     *
     * @param dSymbol
     * @return dual
     */
    public static DSymbol dualize(DSymbol dSymbol) {
        final int size = dSymbol.size();
        final DSymbol dual = new DSymbol(size);

        for (int a = 1; a <= size; a++) {
            dual.setS0(a, dSymbol.getS2(a));
            dual.setS1(a, dSymbol.getS1(a));
            dual.setS2(a, dSymbol.getS0(a));
            dual.setMatrixIJ(0, 1, a, dSymbol.getM12(a));
            dual.setMatrixIJ(1, 2, a, dSymbol.getM01(a));
            dual.setMatrixIJ(0, 2, a, 2);
        }
        return dual;
    }

    /**
     * computes the Delaney symbol with maximal symmetry
     *
     * @param ds
     * @return maximal symmetry
     */
    public static DSymbol maxSymmetry(DSymbol ds) {
        final int size = ds.size();
        final int[] map = new int[size + 1];

        final DSymbol ds1 = new DSymbol();

        int min_card = size;
        int min_a = 1;
        for (int a = 2; a <= size; a++) {
            if (ds.getM01(1) == ds.getM01(a) && ds.getM12(1) == ds.getM12(a)) {
                for (int b = 1; b <= size; b++)
                    map[b] = b;

                map[a] = 1;
                if (mcheck(a, map, ds)) {
                    int min_c = cardinality(map, size);
                    if (min_c < min_card) {
                        min_card = min_c;
                        min_a = a;
                    }
                }
            }
        }
        for (int b = 1; b <= size; b++)
            map[b] = b;

        map[min_a] = 1;
        mcheck(min_a, map, ds);
        highersym(map, ds, ds1);
        if (ds1.size() == size || DSymbolAlgorithms.isMaximalSymmetry(ds1))
            return ds1;
        else
            return maxSymmetry(ds1);
    }

    /**
     * computes symbol with higher symmetry
     *
     * @param map
     * @param ds1
     * @param ds2
     */
    private static void highersym(int[] map, DSymbol ds1, DSymbol ds2) {
        final int size1 = ds1.size();

        final int[] new_map = new int[ds1.size() + 1];
        final int[] new_inverse = new int[ds1.size() + 1];

        if (ds2.size() > 0)
            ds2.resize(0);

        for (int a = 1; a <= size1; a++) {
            if (map[a] == a) {
                ds2.resize(ds2.size() + 1);
                new_map[a] = ds2.size();
                new_inverse[ds2.size()] = a;
            } else
                new_map[a] = new_map[map[a]];
        }
        for (int a = 1; a <= ds2.size(); a++) {
            for (int i = 0; i <= 2; i++)
                ds2.setSi(i, a, new_map[map[ds1.getSi(i, new_inverse[a])]]);

            ds2.setMatrixIJ(0, 1, a, ds1.getM01(new_inverse[a]));
            ds2.setMatrixIJ(1, 2, a, ds1.getM12(new_inverse[a]));
            ds2.setMatrixIJ(0, 2, a, 2);
        }
    }


    /**
     * computes the cardinality of the map
     *
     * @param map
     * @param n
     * @return
     */
    private static int cardinality(int[] map, int n) {
        int count = 0;
        while (count < map.length && n > 0) {
            if (map[n] == n)
                count++;
            n--;
        }
        return count;
    }


    /**
     * does the given Delaney symbol have maximal symmetry?
     *
     * @param ds
     * @return true, if has maximal symmetry
     */
    public static boolean isMaximalSymmetry(DSymbol ds) {
        final int size = ds.size();
        final int[] map = new int[size + 1];

        for (int a = 2; a <= size; a++) {
            if (ds.getM01(1) == ds.getM01(a) && ds.getM12(1) == ds.getM12(a)) {
                for (int b = 1; b <= size; b++)
                    map[b] = b;
                map[a] = 1;
                if (mcheck(a, map, ds))
                    return false;
            }
        }
        return true;
    }

    /**
     * checks whether given map leads to a homomorphism onto a smaller symbol
     *
     * @param a
     * @param map
     * @param ds
     * @return
     */
    private static boolean mcheck(int a, int[] map, DSymbol ds) {
        final int size = ds.size();

        while (a <= size) {
            boolean ko = false;
            for (int k = a; !ko && k <= size; k++) {
                for (int i = 0; !ko && i <= 2; i++) {
                    if (map[ds.getSi(i, map[k])] != map[ds.getSi(i, k)]) {
                        if (ds.getM01(ds.getSi(i, map[k])) != ds.getM01(ds.getSi(i, k)) || ds.getM12(ds.getSi(i, map[k])) != ds.getM12(ds.getSi(i, k)))
                            return false;
                        a = Math.max(map[ds.getSi(i, map[k])], map[ds.getSi(i, k)]);
                        final int b = Math.min(map[ds.getSi(i, map[k])], map[ds.getSi(i, k)]);
                        for (int k2 = a; k2 <= size; k2++)
                            if (map[k2] == a)
                                map[k2] = b;
                        a = Math.min(a, k);
                        ko = true;
                    }
                }
            }
            if (!ko)
                return true;
        }
        return false;
    }

    /**
     * computes the corresponding orientated Delaney symbol
     *
     * @param ds1
     * @return orientated
     */
    public static DSymbol orientate(DSymbol ds1) {
        if (ds1.computeOrientation() == 2)
            return ds1;
        else {
            final int size1 = ds1.size();

            final DSymbol ds2 = new DSymbol(2 * size1);

            for (int a = 1; a <= size1; a++) {
                for (int i = 0; i <= 2; i++) {
                    ds2.setSi(i, a, size1 + ds1.getSi(i, a));
                }

                ds2.setMatrixIJ(0, 1, a, ds1.getM01(a));
                ds2.setMatrixIJ(0, 1, size1 + a, ds1.getM01(a));
                ds2.setMatrixIJ(1, 2, a, ds1.getM12(a));
                ds2.setMatrixIJ(1, 2, size1 + a, ds1.getM12(a));
                ds2.setMatrixIJ(0, 2, a, 2);
                ds2.setMatrixIJ(0, 2, size1 + a, 2);
            }
            return ds2;
        }
    }

    /*
     ** determines whether all tiles involved in a tiling are disks
     */
    public static boolean allTilesAreDisks(DSymbol ds) {
        if (ds.computeOrientation() != 2)
            ds = orientate(ds);

        final int[][][] v = computeV(ds);

        final int[] fl_01 = new int[ds.size() + 1];
        final int[] fl_12 = new int[ds.size() + 1];

        for (int a = 1; a <= ds.size(); a++) {
            ds.markOrbitX(0, 1, a, fl_01, 1);
            ds.markOrbitX(1, 2, a, fl_12, 1);
            int b = otherSideOfBottleneck(a, fl_01, ds);
            if (b != 0 && bottleneck(a, b, fl_01, fl_12, v, ds))
                return false;
        }
        return true;
    }

    /*
     ** determines the flag b on the other side of the bottleneck at flag a,
     ** returns 0 if no such flag exists
     */
    private static int otherSideOfBottleneck(int a, int[] fl_01, DSymbol ds) {
        int b = ds.getS2(a);

        while (ds.getS1(b) != a) {
            b = ds.getS2(ds.getS1(b));
            if (fl_01[b] == 1) {
                if (b <= a) /*	consider every 2-edge once only	*/
                    return 0;
                else
                    return b;
            }
        }
        return 0;
    }

    /*
     ** determines whether the flags a and b correspond to a bottleneck
     ** vertex in a non disk tile
     */
    private static boolean bottleneck(int a, int b, int[] fl_01, int[] fl_12, int[][][] v, DSymbol ds) {
        final int[] fl = new int[ds.size() + 1];

        boolean ok = bottleneckRec(a, b, a, fl, fl_01, fl_12, v, ds);
        if (ok && fl[ds.getS1(a)] == 1)
            ok = false;	/*	1-op neighbour of 'a' has been marked,
				hence we do not have two components	*/
        return ok;
    }

    /*
     ** determines whether a and b define a bottleneck to a component without
     ** branching numbers
     */
    private static boolean bottleneckRec(int a, int b, int c, int[] fl, int[] fl_01, int[] fl_12, int[][][] v, DSymbol ds) {
        if (fl[c] != 0)
            return true;
        fl[c] = 1;
        if ((fl_01[c] == 0 && v[c][0][1] > 1) || (fl_12[c] == 0 && v[c][1][2] > 1)
                || (v[c][0][2] > 1))
            return false;    /* component contains branching number	*/

        for (int i = 0; i <= 2; i++)
            if (i != 1 || (c != a && c != b)) { /*	do not use 1-op leaving from a or b	*/
                if (!bottleneckRec(a, b, ds.getSi(i, c), fl, fl_01, fl_12, v, ds))
                    return false;
            }
        return true;
    }

    /**
     * compute the branching numbers v_ij
     *
     * @param ds
     * @return branching numbers
     */
    public static int[][][] computeV(DSymbol ds) {
        final int[][][] v = new int[ds.size() + 1][2][3];

        for (int k = 0; k <= 2; k++) {
            final int i = DSymbol.i(k);
            final int j = DSymbol.j(k);
            final BitSet fl = new BitSet();
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, j, a, fl)) {
                int b = a;
                int va = ds.getMij(i, j, a) / ds.computeOrbitLength(i, j, a);
                do {
                    b = ds.getSi(i, b);
                    v[b][i][j] = va;
                    b = ds.getSi(j, b);
                    v[b][i][j] = va;
                }
                while (b != a);
            }
        }
        return v;
    }
}
