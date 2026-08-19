/*
 * DSymbolAlgorithms.java Copyright (C) 2023 Daniel H. Huson
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

import jloda.util.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * algorithms for modifying Delaney symbols
 * Daniel Huson, 11.2018
 */
public class DSymbolAlgorithms {
    /**
     * computes the dual Delaney symbol
     *
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
     * @return maximal symmetry
     */
    public static DSymbol maxSymmetry(DSymbol ds) {
        var size = ds.size();
        var map = new int[size + 1];

        var min_card = size;
        var min_a = 1;
        for (var a = 2; a <= size; a++) {
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
        for (var b = 1; b <= size; b++)
            map[b] = b;
        map[min_a] = 1;
        mcheck(min_a, map, ds);
        var ds1=higherSymmetry(map, ds);
        if (ds1.size() == size || DSymbolAlgorithms.isMaximalSymmetry(ds1))
            return ds1;
        else
            return maxSymmetry(ds1);
    }

    /**
     * determines whether mapping flag 1 onto flag a produces a D-symbol with higher symmetry
     * @param a other flag
     * @param ds input D-symbol
     * @return homomorphic symbol or null
     */
    public static DSymbol higherSymmetry(int a,DSymbol ds,BitSet... sets) {
        var size = ds.size();
        var equivalenceMap = new int[size + 1];

        if (ds.getM01(1) == ds.getM01(a) && ds.getM12(1) == ds.getM12(a)) {
                for (int b = 1; b <= size; b++)
                    equivalenceMap[b] = b;
                equivalenceMap[a] = 1;
                if (!mcheck(a, equivalenceMap, ds))
                    return null;
            return higherSymmetry(equivalenceMap, ds,sets);
        }
        else return null;
    }

    /**
     * computes symbol with higher symmetry
     * @param equivalenceMap bap between equivalent taxa
     * @param ds1 the input D-symbol
     * @param sets sets of flags in the input D-symbol that need to be mapped to the new D-symbol
     * @return the new D-symbol
     */
    private static DSymbol higherSymmetry(int[] equivalenceMap, DSymbol ds1,BitSet... sets) {
        var size1 = ds1.size();

        var new_map = new int[ds1.size() + 1];
        var new_inverse = new int[ds1.size() + 1];

        var ds2=new DSymbol();

        for (var a = 1; a <= size1; a++) {
            if (equivalenceMap[a] == a) {
                ds2.resize(ds2.size() + 1);
                new_map[a] = ds2.size();
                new_inverse[ds2.size()] = a;
            } else
                new_map[a] = new_map[equivalenceMap[a]];
        }
        for (var a = 1; a <= ds2.size(); a++) {
            for (var i = 0; i <= 2; i++)
                ds2.setSi(i, a, new_map[equivalenceMap[ds1.getSi(i, new_inverse[a])]]);

            ds2.setMatrixIJ(0, 1, a, ds1.getM01(new_inverse[a]));
            ds2.setMatrixIJ(1, 2, a, ds1.getM12(new_inverse[a]));
            ds2.setMatrixIJ(0, 2, a, 2);
        }

        for(var set:sets){
            var list=BitSetUtils.asList(set);
            set.clear();
            for(var b:list) {
                set.set(new_map[b]);
            }
        }

        return ds2;
    }


    /**
     * computes the cardinality of the map
     *
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
     * checks whether given equivalence map leads to a homomorphism onto a smaller symbol
     *
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
     * determines whether tiling is "normal", meaning that the intersection of any two tiles is either empty, a
     * single point or a single line segment.
     * todo: this is mathematically incorrect and Olaf is working on a correct algorithm
     *
     * @return true, if simple
     */
    public static boolean isNormal(DSymbol ds) {
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
        while (!queue.isEmpty()) {
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
        while (!stack.isEmpty()) {
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
        while (!stack.isEmpty()) {
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

    public static String computeVertexFigure(DSymbol dSymbol) {
        return computeSignature(dualize(dSymbol));
    }

    /**
     * computes the signature of the corresponding tiling.
     * This is of the form (a_1 a_2 a_3 ...)(b_1 b_2 b_3 ...)..., where a_1, a_2 etc are the vertex degrees of the first tile etc
     *
	 */
    public static String computeSignature(DSymbol dSymbol) {
        final ArrayList<Pair<Integer, String>> cycles = new ArrayList<>();
        final BitSet visited = new BitSet();
        for (int t = 1; t <= dSymbol.size(); t = dSymbol.nextOrbit(0, 1, t, visited)) {
            final ArrayList<Integer> degrees = new ArrayList<>();
            int a = t;
            do {
                degrees.add(dSymbol.getM12(a));
                a = dSymbol.getS1(dSymbol.getS0(a));
            }
            while (a != t);
            final int v01 = dSymbol.getVij(0, 1, t);
            if (v01 > 1) {
                final ArrayList<Integer> orig = new ArrayList<>(degrees);
                for (int copy = 1; copy < v01; copy++) {
                    degrees.addAll(orig);
                }
            }
            cycles.add(new Pair<>(degrees.size(), createRotatedString(degrees)));
        }
        cycles.sort((a, b) -> {
            if (a.getFirst() < b.getFirst())
                return -1;
            else if (a.getFirst() > b.getFirst())
                return 1;
            else return a.getSecond().compareTo(b.getSecond());
        });

        final StringBuilder buf = new StringBuilder();
        String prev = null;
        int prevCount = 0;
        for (Pair<Integer, String> pair : cycles) {
            final String str = pair.getSecond();
            if (prev == null) {
                prev = str;
                prevCount = 1;
            } else if (str.equals(prev)) {
                prevCount++;
            } else {
                if (prevCount > 1)
                    buf.append(String.format(" %dx", prevCount));
                buf.append(prev);
                prev = str;
                prevCount = 1;
            }
        }
        if (prev != null) {
            if (prevCount > 1)
                buf.append(String.format(" %dx", prevCount));
            buf.append(prev);
        }
        return buf.toString();
    }

    private static String createRotatedString(ArrayList<Integer> degrees) {
        final Integer[] sorted1 = minLexRotation(degrees.toArray(new Integer[0]));
		final Integer[] sorted2 = minLexRotation(CollectionUtils.reverse(degrees).toArray(new Integer[0]));

        final Integer[] array;
        if (Arrays.compare(sorted1, sorted2) <= 0) {
            array = sorted1;
        } else
            array = sorted2;

        final StringBuilder buf = new StringBuilder();
        for (Integer i : array) {
            if (buf.length() == 0)
                buf.append("(");
            else
                buf.append(" ");
            buf.append(i);
        }
        buf.append(")");

        return buf.toString();
    }

    public static String computeTileDegrees(DSymbol ds) {
        final ArrayList<Integer> list = new ArrayList<>();
        for (int a : ds.orbits(0, 1))
            list.add(ds.getM01(a));
        list.sort(Integer::compare);
		return StringUtils.toString(list, " ");
    }

    public static String computeVertexDegrees(DSymbol ds) {
        final ArrayList<Integer> list = new ArrayList<>();
        for (int a : ds.orbits(1, 2))
            list.add(ds.getM12(a));
        list.sort(Integer::compare);
		return StringUtils.toString(list, " ");
    }

    /**
     * is the tiling colorable, ie, no two equivalent tiles share an edge
     *
     * @return is colorable
     */
    public static boolean isColorable(DSymbol ds) {
        final int[] orbits = new int[ds.size() + 1];
        for (int a = 1; a <= ds.size(); a++) {
            ds.markOrbit(0, 1, a, orbits, a);
        }

        for (int a = 1; a <= ds.size(); a++) {
            if (orbits[a] == orbits[ds.getS2(a)])
                return false;
        }
        return true;
    }

    public static Integer[] minLexRotation(Integer[] numbers) {
        final int n = numbers.length;
        if (n == 0)
            return numbers;

        final Integer[][] array = new Integer[n][];
        final Integer[] concat = new Integer[2 * numbers.length];
        System.arraycopy(numbers, 0, concat, 0, n);
        System.arraycopy(numbers, 0, concat, n, n);

        for (int i = 0; i < n; i++) {
            array[i] = new Integer[numbers.length];
            System.arraycopy(concat, i, array[i], 0, n);
        }
        Arrays.sort(array, (a, b) -> {
            for (int i = 0; i < a.length; i++) {
                if (a[i] < b[i])
                    return -1;
                else if (a[i] > b[i])
                    return 1;
            }
            return 0;
        });
        return array[0];
    }

    /**
     * the Crockford base-32 alphabet, which omits I, L, O and U so that keys cannot be misread
     */
    private static final String CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    /**
     * default number of hash bits used in a canonical key
     */
    public static final int DEFAULT_KEY_BITS = 60;

    /**
     * computes the canonical form of a Delaney symbol.
     * <p>
     * For a connected symbol, consider the relabeling obtained from each flag in turn as described in
     * {@link #canonicalOrder}, and take the one whose protocol, see {@link #canonicalProtocol}, is
     * smallest as a sequence of integers.
     * <p>
     * If the symbol is not connected, then each connected component is canonicalized on its own and the
     * components are concatenated in increasing order of their protocol.
     * <p>
     * Two Delaney symbols are isomorphic if and only if they have the same canonical form, so the canonical
     * form is a complete invariant and can be used as a permanent identifier for the corresponding tiling.
     * The symbol number is set to 0.0, as it is not part of the tiling.
     *
     * @return canonical form
     */
    public static DSymbol canonicalForm(DSymbol ds) {
        if (ds.size() == 0)
            return new DSymbol(0);

        final ArrayList<BitSet> components = connectedComponents(ds);
        if (components.size() == 1)
            return canonicalFormConnected(ds);

        final ArrayList<DSymbol> parts = new ArrayList<>();
        for (BitSet component : components)
            parts.add(canonicalFormConnected(extractComponent(ds, component)));
        parts.sort((a, b) -> Arrays.compare(protocol(a), protocol(b)));

        final DSymbol result = new DSymbol(0);
        for (DSymbol part : parts)
            result.append(part);
        return result;
    }

    /**
     * computes the canonical form of a Delaney symbol, as a string.
     * This is a valid Delaney symbol string with symbol number 0.0, so it can be read back in
     *
     * @return canonical form as string
     */
    public static String canonicalString(DSymbol ds) {
        return canonicalForm(ds).toString();
    }

    /**
     * computes the protocol of the canonical form of a Delaney symbol.
     * The protocol of a symbol of size n is the sequence of 5n numbers
     * s0(1)..s0(n), s1(1)..s1(n), s2(1)..s2(n), m01(1)..m01(n), m12(1)..m12(n).
     * It holds everything except m02, which is always 2, so a symbol can be rebuilt from it, see
     * {@link #fromProtocol}. It is what canonical forms are compared by and what keys are computed from,
     * so that neither depends on how a symbol happens to be written as text
     *
     * @return protocol of the canonical form
     */
    public static int[] canonicalProtocol(DSymbol ds) {
        return protocol(canonicalForm(ds));
    }

    /**
     * the protocol of a Delaney symbol, as written
     *
     * @return protocol
     */
    public static int[] protocol(DSymbol ds) {
        final int size = ds.size();
        final int[] result = new int[5 * size];
        int k = 0;
        for (int i = 0; i <= 2; i++) {
            for (int a = 1; a <= size; a++)
                result[k++] = ds.getSi(i, a);
        }
        for (int a = 1; a <= size; a++)
            result[k++] = ds.getM01(a);
        for (int a = 1; a <= size; a++)
            result[k++] = ds.getM12(a);
        return result;
    }

    /**
     * the protocol, written as space-separated numbers. This is what a canonical key hashes
     *
     * @return protocol as string
     */
    public static String protocolString(int[] protocol) {
        final StringBuilder buf = new StringBuilder();
        for (int value : protocol) {
            if (buf.length() > 0)
                buf.append(" ");
            buf.append(value);
        }
        return buf.toString();
    }

    /**
     * rebuilds a Delaney symbol from its protocol. The symbol number is set to 0.0
     *
     * @return symbol
     */
    public static DSymbol fromProtocol(int[] protocol) {
        final int size = protocol.length / 5;
        final DSymbol result = new DSymbol(size);
        int k = 0;
        for (int i = 0; i <= 2; i++) {
            for (int a = 1; a <= size; a++)
                result.setSi(i, a, protocol[k++]);
        }
        for (int a = 1; a <= size; a++)
            result.setMatrixIJ(0, 1, a, protocol[k++]);
        for (int a = 1; a <= size; a++)
            result.setMatrixIJ(1, 2, a, protocol[k++]);
        for (int a = 1; a <= size; a++)
            result.setMatrixIJ(0, 2, a, 2);
        return result;
    }

    /**
     * determines whether a Delaney symbol is already canonically labeled
     *
     * @return true, if canonically labeled
     */
    public static boolean isCanonical(DSymbol ds) {
        final DSymbol copy = new DSymbol(ds);
        copy.setNr1(0);
        copy.setNr2(0);
        copy.setComment(null);
        return copy.toString().equals(canonicalString(ds));
    }

    /**
     * computes the canonical key of a Delaney symbol, using the default number of hash bits
     *
     * @return canonical key
     */
    public static String canonicalKey(DSymbol ds) {
        return canonicalKey(ds, DEFAULT_KEY_BITS);
    }

    /**
     * computes the canonical key of a Delaney symbol.
     * This is the size of the symbol, followed by the leading bits of the SHA-256 hash of the protocol of
     * its canonical form, written in Crockford base 32 and grouped in fours, for example DS07-K3QF-2M7V-XB4T.
     * Isomorphic Delaney symbols have the same key. Different symbols of the same size have different keys,
     * unless their hashes collide; because the size is part of the key, only symbols of the same size can collide
     *
     * @param bits number of hash bits to use, a multiple of 5 between 20 and 255
     * @return canonical key
     */
    public static String canonicalKey(DSymbol ds, int bits) {
        return keyForProtocol(canonicalProtocol(ds), bits);
    }

    /**
     * computes the key for a protocol that is already in hand, without canonicalizing again
     *
     * @return key
     */
    public static String keyForProtocol(int[] protocol, int bits) {
        final byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                    .digest(protocolString(protocol).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // every Java platform is required to provide SHA-256
        }
        return formatKey(protocol.length / 5, digest, bits);
    }

    /**
     * renders the leading bits of a hash as a key: the size of the symbol, then the bits in Crockford
     * base 32, grouped in fours
     *
     * @return key
     */
    static String formatKey(int size, byte[] digest, int bits) {
        if (bits < 20 || bits > 255 || bits % 5 != 0)
            throw new IllegalArgumentException("formatKey(): bits must be a multiple of 5 between 20 and 255, got: " + bits);

        final StringBuilder buf = new StringBuilder(String.format("DS%02d", size));
        for (int i = 0; i < bits / 5; i++) {
            if (i % 4 == 0)
                buf.append("-");
            int value = 0;
            for (int bit = 5 * i; bit < 5 * i + 5; bit++)
                value = (value << 1) | ((digest[bit >> 3] >> (7 - (bit & 7))) & 1);
            buf.append(CROCKFORD_BASE32.charAt(value));
        }
        return buf.toString();
    }

    /**
     * canonicalizes a connected Delaney symbol
     *
     * @return canonical form
     */
    private static DSymbol canonicalFormConnected(DSymbol ds) {
        int[] best = null;
        int[] bestOrder = null;
        for (int seed = 1; seed <= ds.size(); seed++) {
            final int[] order = canonicalOrder(ds, seed);
            if (order == null)
                continue; // not reachable from this seed, so the symbol is not connected
            final int[] candidate = protocolFor(ds, order);
            if (best == null || Arrays.compare(candidate, best) < 0) {
                best = candidate;
                bestOrder = order;
            }
        }
        return relabel(ds, bestOrder);
    }

    /**
     * the order in which a traversal from the given flag reaches the flags of a connected Delaney symbol.
     * <p>
     * The traversal keeps one queue per index and always draws from the lowest-index non-empty queue,
     * taking from the back for indices 0 and 1 and from the front for index 2. Taking from the back for
     * 0 and 1 exhausts a tile before moving to the next one, so the flags of a tile come out consecutively
     * numbered, which makes a canonical form much easier to read. This is the traversal used by Gavrog
     * (O. Delgado-Friedrichs, Data structures and algorithms for tilings I, Theoretical Computer
     * Science 303 (2003) 431-445, and https://github.com/odf/gavrog)
     *
     * @return new2old, where new2old[k] is the flag that receives the new label k, or null if the symbol
     * is not connected
     */
    private static int[] canonicalOrder(DSymbol ds, int seed) {
        final int size = ds.size();
        final ArrayList<ArrayDeque<Integer>> queues = new ArrayList<>();
        for (int i = 0; i <= 2; i++)
            queues.add(new ArrayDeque<>());

        final int[] new2old = new int[size + 1];
        final boolean[] seen = new boolean[size + 1];
        int count = 1;
        seen[seed] = true;
        new2old[1] = seed;
        for (int i = 0; i <= 2; i++)
            queues.get(i).addLast(ds.getSi(i, seed));

        while (count < size) {
            int found = 0;
            int via = -1;
            scan:
            for (int i = 0; i <= 2; i++) {
                final ArrayDeque<Integer> queue = queues.get(i);
                while (!queue.isEmpty()) {
                    final int b = (i < 2 ? queue.removeLast() : queue.removeFirst());
                    if (b >= 1 && b <= size && !seen[b]) {
                        found = b;
                        via = i;
                        break scan;
                    }
                }
            }
            if (found == 0)
                return null;
            seen[found] = true;
            new2old[++count] = found;
            for (int j = 0; j <= 2; j++) {
                if (j != via)
                    queues.get(j).addLast(ds.getSi(j, found));
            }
        }
        return new2old;
    }

    /**
     * the protocol that the given relabeling of a Delaney symbol would have, computed without building
     * the relabeled symbol
     *
     * @return protocol
     */
    private static int[] protocolFor(DSymbol ds, int[] new2old) {
        final int size = ds.size();
        final int[] old2new = new int[size + 1];
        for (int a = 1; a <= size; a++)
            old2new[new2old[a]] = a;

        final int[] result = new int[5 * size];
        int k = 0;
        for (int i = 0; i <= 2; i++) {
            for (int a = 1; a <= size; a++)
                result[k++] = old2new[ds.getSi(i, new2old[a])];
        }
        for (int a = 1; a <= size; a++)
            result[k++] = ds.getM01(new2old[a]);
        for (int a = 1; a <= size; a++)
            result[k++] = ds.getM12(new2old[a]);
        return result;
    }

    /**
     * relabels a Delaney symbol, giving the flag new2old[a] the new label a
     *
     * @return relabeled symbol, with symbol number 0.0
     */
    private static DSymbol relabel(DSymbol ds, int[] new2old) {
        final int size = ds.size();
        final int[] old2new = new int[size + 1];
        for (int a = 1; a <= size; a++)
            old2new[new2old[a]] = a;

        final DSymbol result = new DSymbol(size);
        for (int a = 1; a <= size; a++) {
            for (int i = 0; i <= 2; i++)
                result.setSi(i, a, old2new[ds.getSi(i, new2old[a])]);
        }
        for (int a = 1; a <= size; a++) {
            result.setMatrixIJ(0, 1, a, ds.getM01(new2old[a]));
            result.setMatrixIJ(1, 2, a, ds.getM12(new2old[a]));
            result.setMatrixIJ(0, 2, a, ds.getM02(new2old[a]));
        }
        return result;
    }

    /**
     * computes the connected components of a Delaney symbol
     *
     * @return components, each as the set of flags that it contains
     */
    static ArrayList<BitSet> connectedComponents(DSymbol ds) {
        final ArrayList<BitSet> result = new ArrayList<>();
        final BitSet seen = new BitSet();
        for (int a = 1; a <= ds.size(); a++) {
            if (!seen.get(a)) {
                final BitSet component = new BitSet();
                final ArrayDeque<Integer> stack = new ArrayDeque<>();
                component.set(a);
                seen.set(a);
                stack.push(a);
                while (!stack.isEmpty()) {
                    final int b = stack.pop();
                    for (int i = 0; i <= 2; i++) {
                        final int c = ds.getSi(i, b);
                        if (c >= 1 && c <= ds.size() && !component.get(c)) {
                            component.set(c);
                            seen.set(c);
                            stack.push(c);
                        }
                    }
                }
                result.add(component);
            }
        }
        return result;
    }

    /**
     * extracts the given set of flags as a Delaney symbol in its own right.
     * The set must be closed under s0, s1 and s2
     *
     * @return component
     */
    static DSymbol extractComponent(DSymbol ds, BitSet component) {
        final int[] old2new = new int[ds.size() + 1];
        final int[] new2old = new int[component.cardinality() + 1];

        int count = 0;
        for (int a = component.nextSetBit(1); a != -1; a = component.nextSetBit(a + 1)) {
            old2new[a] = ++count;
            new2old[count] = a;
        }

        final DSymbol result = new DSymbol(count);
        for (int a = 1; a <= count; a++) {
            for (int i = 0; i <= 2; i++)
                result.setSi(i, a, old2new[ds.getSi(i, new2old[a])]);
        }
        for (int a = 1; a <= count; a++) {
            result.setMatrixIJ(0, 1, a, ds.getM01(new2old[a]));
            result.setMatrixIJ(1, 2, a, ds.getM12(new2old[a]));
            result.setMatrixIJ(0, 2, a, ds.getM02(new2old[a]));
        }
        return result;
    }

    public static void main(String[] args) {
        DSymbol dSymbol = new DSymbol("<17.0:2:1 2,1 2,2:4 3,6>");
        System.err.println(computeSignature(dSymbol));
    }
}
