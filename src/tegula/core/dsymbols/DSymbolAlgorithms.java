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

import jloda.util.BitSetUtils;
import jloda.util.Single;

import java.util.*;

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
            ds2.setNr1(ds1.getNr1());
            ds2.setNr2(ds1.getNr2());

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

    /**
     * compute the branching numbers v_ij
     *
     * @param ds
     * @return branching numbers
     */
    public static int[] computeVij(int i, int j, DSymbol ds) {
        final int[] v = new int[ds.size() + 1];

        final BitSet fl = new BitSet();
        for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, j, a, fl)) {
            int b = a;
            int va = ds.getMij(i, j, a) / ds.computeOrbitLength(i, j, a);
            do {
                b = ds.getSi(i, b);
                v[b] = va;
                b = ds.getSi(j, b);
                v[b] = va;
            }
            while (b != a);
        }
        return v;
    }


    /**
     * determines whether tiling is "simple", meaning that the intersection of any two tiles is either empty, a
     * single point or a single line segment.
     *
     * @param ds
     * @return true, if simple
     */
    public static boolean isSimpleTiling(DSymbol ds) {
        if (ds.computeOrientation() != 2) {
            ds = orientate(ds);
            ds = reorder(ds);
        }

        final int[] orientation = new int[ds.size() + 1];
        ds.computeOrientation(orientation);

        final int[] tile = ds.computeOrbits(0, 1);
        final int[] vertex = ds.computeOrbits(1, 2);
        final int[][][] v = computeV(ds);

        for (int a1 = 1; a1 <= ds.size(); a1++) {
            for (int a2 = a1 + 1; a2 <= ds.size(); a2++) {
                if (orientation[a2] != orientation[a1] && tile[a1] == tile[a2]) {
                    for (int b1 = a1; b1 <= ds.size(); b1++) {
                        if (orientation[b1] != orientation[a1] && inSameOrbit(a1, 2, 1, b1, ds)) {
                            for (int b2 = a1; b2 <= ds.size(); b2++) {
                                if (orientation[b2] == orientation[a1] && tile[b1] == tile[b2]
                                        && ((a1 == b2 && a2 == b1) || a1 != b2 && a2 != b1)
                                        && inSameOrbit(a2, 2, 1, b2, ds)) {
                                    if (hasCapturedIsland(a1, a2, b1, b2, tile, vertex, v, ds))
                                        return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * determine whether the four flags completely surround an "island"
     *
     * @param a1
     * @param a2
     * @param b1
     * @param b2
     * @param tile
     * @param vertex
     * @param v
     * @param ds
     * @return true, if island
     */
    private static boolean hasCapturedIsland(int a1, int a2, int b1, int b2, int[] tile, int[] vertex, int[][][] v, DSymbol ds) {
        if (ds.getS1(a1) == b1 && v[a1][1][2] == 1)
            return false;
        if (ds.getS1(a2) == b2 && v[a2][1][2] == 1)
            return false;

        final BitSet shore = new BitSet();

        final boolean allowSingleTwoFoldRotation;
        if (a1 == b2 && a2 == b1) {
            allowSingleTwoFoldRotation = true;

            int c = a1;
            shore.set(c);
            while (true) {
                c = ds.getS0(c);
                shore.set(c);
                if (c == a2)
                    break;
                c = ds.getS1(c);
                shore.set(c);
            }

        } else {
            allowSingleTwoFoldRotation = false;

            final BitSet aShore = new BitSet();
            {
                int c = a1;
                aShore.set(c);
                while (true) {
                    c = ds.getS0(c);
                    aShore.set(c);
                    if (c == a2)
                        break;
                    c = ds.getS1(c);
                    aShore.set(c);
                }
            }
            final BitSet bShore = new BitSet();
            {
                int c = b1;
                bShore.set(c);
                while (true) {
                    c = ds.getS0(c);
                    bShore.set(c);
                    if (c == b2)
                        break;
                    c = ds.getS1(c);
                    bShore.set(c);
                }
            }
            if (aShore.intersects(bShore))
                return false;
            shore.or(aShore);
            shore.or(bShore);
        }

        final BitSet island = ds.getSi(2, shore);
        if (shore.intersects(island)) {
            return false;
        }
        for (int a : BitSetUtils.members(island)) {
            if (tile[a] == tile[a1] || tile[a] == tile[b1])
                return false;
        }

        final BitSet shoreVertices = new BitSet();
        for (int a : BitSetUtils.members(shore)) {
            shoreVertices.set(vertex[a]);
        }

        // visit all members of the island and complete the island
        final Queue<Integer> queue = new LinkedList<>(BitSetUtils.asList(island));
        while (queue.size() > 0) {
            final int p = queue.poll();
            for (int i = 0; i <= 2; i++) {
                final int q = ds.getSi(i, p);
                if (!island.get(q) && (i < 2 || !shore.get(q))) {
                    island.set(q);
                    queue.add(q);
                }
            }
        }

        if (!isConnected(island, ds))
            return false;

        final int[] old2new = new int[ds.size() + 1];

        final BitSet islandAndShore = new BitSet();
        islandAndShore.or(island);
        islandAndShore.or(shore);

        final DSymbol dsNew = new DSymbol(islandAndShore.cardinality());
        {
            int count = 0;
            for (int c : BitSetUtils.members(islandAndShore)) {
                old2new[c] = ++count;
            }
        }
        for (int c : BitSetUtils.members(islandAndShore)) {
            if (islandAndShore.get(ds.getS0(c)))
                dsNew.setS0(old2new[c], old2new[ds.getS0(c)]);
            else {
                dsNew.clear();
                return false;
            }

            if (c == a1 || c == a2 || c == b1 || c == b2) {
                final int cS1 = ds.getS1(c);
                if (islandAndShore.get(cS1) && cS1 != a1 && cS1 != a2 && cS1 != b1 && cS1 != b2) {
                    dsNew.clear(); // need to clear to prevent problems in debugger
                    return false;
                }
                dsNew.setS1(old2new[c], old2new[c]);
            } else {
                dsNew.setS1(old2new[c], old2new[ds.getS1(c)]);
            }
            if (islandAndShore.get(ds.getS2(c)))
                dsNew.setS2(old2new[c], old2new[ds.getS2(c)]);
            else {
                dsNew.clear();
                return false;
            }
        }

        for (int a = 1; a <= dsNew.size(); a++) {
            if (dsNew.getS1(dsNew.getS1(a)) != a) {
                dsNew.clear();
                return false;
            }
        }

        if (!isConnected(dsNew)) {
            dsNew.clear();
            return false;
        }

        for (int c : BitSetUtils.members(islandAndShore)) {
            final int newC = old2new[c];

            if (tile[c] == tile[a1] || tile[c] == tile[b1])
                dsNew.setMatrixIJ(0, 1, newC, dsNew.computeOrbitLength(0, 1, newC));
            else
                dsNew.setMatrixIJ(0, 1, newC, ds.getM01(c));

            if (vertex[c] == vertex[a1] || vertex[c] == vertex[a2])
                dsNew.setMatrixIJ(1, 2, newC, dsNew.computeOrbitLength(1, 2, newC));
            else
                dsNew.setMatrixIJ(1, 2, newC, ds.getM12(c));

            dsNew.setMatrixIJ(0, 2, old2new[c], 2);

            for (int k = 0; k <= 2; k++) {
                final int i = DSymbol.i(k);
                final int j = DSymbol.j(k);
                if (dsNew.getMij(i, j, old2new[c]) % dsNew.computeOrbitLength(i, j, old2new[c]) != 0) {
                    System.err.println("Problem m(" + i + "," + j + "," + old2new[c] + "):" + "m=" + dsNew.getMij(i, j, old2new[c]) + " r=" + dsNew.computeOrbitLength(i, j, old2new[c]));
                    return false;
                }
            }
        }

        final String groupName = OrbifoldGroupName.getGroupName(dsNew);
        return groupName.equals("1*") || allowSingleTwoFoldRotation && groupName.equals("2*");
    }

    private static boolean isConnected(DSymbol ds) {
        final BitSet seen = new BitSet();
        final Stack<Integer> stack = new Stack<>();
        stack.push(1);
        seen.set(1);
        while (stack.size() > 0) {
            final int a = stack.pop();
            for (int i = 0; i <= 2; i++) {
                final int b = ds.getSi(i, a);
                if (!seen.get(b)) {
                    seen.set(b);
                    stack.push(b);
                }
            }
        }
        return seen.cardinality() == ds.size();
    }

    private static boolean isConnected(BitSet set, DSymbol ds) {
        final Stack<Integer> stack = new Stack<>();
        final int aMember = set.nextSetBit(0);
        if (aMember == -1)
            return true; // empty set
        stack.push(aMember);
        final BitSet visited = new BitSet();
        visited.set(aMember);
        while (stack.size() > 0) {
            final int a = stack.pop();
            for (int i = 0; i <= 2; i++) {
                final int b = ds.getSi(i, a);
                if (set.get(b) && !visited.get(b)) {
                    visited.set(b);
                    stack.push(b);
                }
            }
        }
        return visited.cardinality() == set.cardinality();
    }

    public static boolean inSameOrbit(final int a, final int i, final int j, final int b, final DSymbol ds) {
        int c = a;
        do {
            c = ds.getSi(i, c);
            if (c == b)
                return true;
            c = ds.getSi(j, c);
            if (c == b)
                return true;
        }
        while (c != a);
        return false;
    }


    public static DSymbol reorder(DSymbol ds) {
        final int numberOfTiles = ds.countOrbits(0, 1);
        final int[] tile2flag = new int[numberOfTiles + 1];
        final int[] size = new int[numberOfTiles + 1];
        {
            final BitSet visited = new BitSet();
            int tileNo = 0;
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(0, 1, a, visited)) {
                tileNo++;
                final int f = ds.getFixpointInOrbit(0, 1, a);
                if (f > 0)
                    tile2flag[tileNo] = f;
                else
                    tile2flag[tileNo] = a;
                size[tileNo] = ds.computeOrbitLength(0, 1, a);
            }
        }
        final Integer[] tileOrder = new Integer[numberOfTiles + 1];
        for (int t = 1; t <= numberOfTiles; t++) {
            tileOrder[t] = t;
        }
        Arrays.sort(tileOrder, 1, tileOrder.length, Comparator.comparingInt(a -> size[a]));

        final Single<Integer> count = new Single<>(0);
        final int[] old2new = new int[ds.size() + 1];
        final int[] new2old = new int[ds.size() + 1];
        for (int i = 1; i <= numberOfTiles; i++) {
            final int t = tileOrder[i];
            ds.visitOrbit(0, 1, tile2flag[t], (b) -> {
                count.set(count.get() + 1);
                final int bNew = count.get();
                old2new[b] = bNew;
                new2old[bNew] = b;
            });
        }

        final DSymbol result = new DSymbol(ds.size());
        result.setNr1(ds.getNr1());
        result.setNr2(ds.getNr2());
        for (int a = 1; a <= result.size(); a++) {
            for (int i = 0; i <= 2; i++) {
                result.setSi(i, a, old2new[ds.getSi(i, new2old[a])]);
            }
        }
        for (int a = 1; a <= result.size(); a++) {
            result.setM01(a, ds.getM01((new2old[a])));
            result.setM12(a, ds.getM12((new2old[a])));
            result.setM02(a, ds.getM02((new2old[a])));
        }
        return result;
    }
}
