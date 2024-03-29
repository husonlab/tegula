/*
 * OrbifoldGroupName.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.NumberUtils;
import jloda.util.StringUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;

/**
 * computes the orbifold name for the symmetry group
 * Created by huson on 3/29/16.
 */
public class OrbifoldGroupName {
    /**
     * computes the orbifold group name
     *
     * @return orbifold group name
     */
    public static String getGroupName(DSymbol dSymbol) {
		return StringUtils.toString(getGroupNameAsList(dSymbol), "");
    }

    /**
     * is the group pq or *pw with p!=q, or *p
     *
     * @return true, if invalid group
     */
    public static boolean isInvalidSphericalGroup(DSymbol dSymbol) {
		final ArrayList<String> group = getGroupNameAsList(dSymbol);
		return group.size() == 2 && group.get(0).equals("*") && NumberUtils.isInteger(group.get(1)) ||
			   group.size() == 2 && NumberUtils.isInteger(group.get(0)) && NumberUtils.isInteger(group.get(1)) && NumberUtils.parseInt(group.get(0)) != NumberUtils.parseInt(group.get(1))
			   || group.size() == 3 && group.get(0).equals("*") && NumberUtils.isInteger(group.get(1)) && NumberUtils.isInteger(group.get(2)) && NumberUtils.parseInt(group.get(1)) != NumberUtils.parseInt(group.get(2));
	}

    /**
     * computes the orbifold group name
     *
     * @return orbifold group name
     */
    public static ArrayList<String> getGroupNameAsList(DSymbol dSymbol) {
        final ArrayList<Integer> rotations = new ArrayList<>();
        final ArrayList<ArrayList<Integer>> boundary = new ArrayList<>();
        final ArrayList<ArrayList<Integer>> rev_boundary = new ArrayList<>();
        final BitSet[] mark = new BitSet[]{new BitSet(), new BitSet(), new BitSet()};
        final BitSet fl = new BitSet();

        final int euler = dSymbol.computeEulerCharacteristic();

        final int[] orientation = new int[dSymbol.size() + 1];
        final int orientable = dSymbol.computeOrientation(orientation);

        for (int k = 0; k <= 2; k++) {
            final int i = DSymbol.i(k);
            final int j = DSymbol.j(k);
            for (int a = 1; a <= dSymbol.size(); a = dSymbol.nextOrbit(i, j, a, fl)) {
                if (dSymbol.isCycle(i, j, a)) {
                    int v = dSymbol.getVij(i, j, a);
                    if (v > 1)
                        rotations.add(v);
                    dSymbol.markOrbit(i, j, a, mark[3 - (i + j)]);
                }
            }
        }

        int n_boundary_rotations = 0;

        if (!dSymbol.isFixedPointFree()) {
            for (int a = 1; a <= dSymbol.size(); a++) {
                for (int i = 0; i <= 2; i++) {
                    if (dSymbol.getSi(i, a) == a) {
                        int j;
                        if (orientation[a] == 1)
                            j = ((i + 1) % 3);
                        else
                            j = ((i + 2) % 3);

                        if (!(mark[3 - (i + j)].get(a))) {
                            ArrayList<Integer> tmp = computeBoundaryComponent(i, j, a, mark, dSymbol);
                            boundary.add(computeLargestCyclicOrdering(tmp));
                            if (tmp.size() > 0)
                                reverse(tmp);
                            rev_boundary.add(computeLargestCyclicOrdering(tmp));
                            n_boundary_rotations += tmp.size();
                        }
                    }
                }
            }
        }

        final ArrayList<String> list = new ArrayList<>();

        final int n_boundary = boundary.size();

        if (orientable > 0) {
            final int genus = (2 - euler - n_boundary) / 2;
            for (int k = 1; k <= genus; k++)
                list.add("o");
        }

        if (rotations.size() > 0) {
            rotations.sort(new IntegerCompareDown());
            addInts(rotations, list);
        }

        if (rotations.size() == 0 && n_boundary_rotations == 0)
            list.add("1");

        if (n_boundary > 0) {
            boundary.sort(new IntegerListCompareDown());
            rev_boundary.sort(new IntegerListCompareDown());

            final ArrayList<ArrayList<Integer>> boundaryToReport = (compare(boundary, rev_boundary) < 0 ? rev_boundary : boundary);

            for (ArrayList<Integer> value : boundaryToReport) {
                list.add("*");
                addInts(value, list);
            }
        }

        if (orientable == 0) {
            final int genus = 2 - euler - n_boundary;
            for (int k = 1; k <= genus; k++)
                list.add("x");
        }
        return list;
    }

    /**
     * compute a boundary component
     *
     * @return boundary component
     */
    static private ArrayList<Integer> computeBoundaryComponent(int i, int j, int a, BitSet[] mark, DSymbol dSymbol) {
        int i1, j1, k, k0;
        int b, b1;
        ArrayList<Integer> bound = new ArrayList<>();

        b = a;
        k = k0 = 3 - (i + j);

        do {
            int v = dSymbol.getVij(i, j, b);
            if (v > 1)
                bound.add(v);

            dSymbol.markOrbit(i, j, b, mark[3 - (i + j)]);

            int[] result = dSymbol.computeOtherChainEnd(i, j, b);
            i1 = result[0];
            b1 = result[2];

            b = b1;
            i = i1;
            j = k;
            k = 3 - (i + j);
        }
        while (b != a || k != k0);

        return bound;
    }

    /**
     * compute largest cyclic ordering of a boundary component
     *
     * @return largest cyclic ordering
     */
    static private ArrayList<Integer> computeLargestCyclicOrdering(ArrayList<Integer> in) {
        final int n = in.size();
        if (n == 0)
            return in;

        final int[] twoCopies = new int[2 * n];
        for (int i = 0; i < n; i++) {
            twoCopies[i] = twoCopies[i + n] = in.get(i);
        }

        int best = 0;
        for (int s = 1; s < n; ++s) {
            for (int i = 0; i < n; ++i) {
                int a = twoCopies[i + best];
                int b = twoCopies[i + s];
                if (a > b)
                    break;
                else if (b > a) {
                    best = s;
                    break;
                }
            }
        }

        final ArrayList<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; ++i)
            out.add(twoCopies[i + best]);
        return out;
    }

    /**
     * write array of ints as string
     *
	 */
    static private void addInts(ArrayList<Integer> in, ArrayList<String> list) {
        for (Integer n : in) {
            if (n >= 0 && n < 10)
                list.add(String.format("%d", n));
            else
                list.add(String.format("(%d)", n));
        }
    }


    private static <T> void reverse(ArrayList<T> list) {
        final ArrayList<T> tmp = new ArrayList<>(list.size());
        for (int i = list.size() - 1; i >= 0; i--)
            tmp.add(list.get(i));
        list.clear();
        list.addAll(tmp);
    }

    private static int compare(final ArrayList<ArrayList<Integer>> a, final ArrayList<ArrayList<Integer>> b) {
        final Iterator<ArrayList<Integer>> ita = a.iterator();
        final Iterator<ArrayList<Integer>> itb = b.iterator();
        final IntegerListCompareDown integerListCompareDown = new IntegerListCompareDown();

        while (ita.hasNext() && itb.hasNext()) {
            int compare = integerListCompareDown.compare(ita.next(), itb.next());
            if (compare > 0)
                return -1;
            else if (compare < 0)
                return 1;
        }
        if (ita.hasNext())
            return -1;
        else if (itb.hasNext())
            return 1;
        else
            return 0;
    }

    public static String clean(String name) {
        return name.replaceAll("\\*", "s");
    }
}

class IntegerCompareDown implements Comparator<Integer> {
    @Override
    public int compare(Integer a, Integer b) {
        return -a.compareTo(b);
    }
}

class IntegerListCompareDown implements Comparator<ArrayList<Integer>> {
    @Override
    public int compare(ArrayList<Integer> a, ArrayList<Integer> b) {
        final Iterator<Integer> ita = a.iterator();
        final Iterator<Integer> itb = b.iterator();

        while (ita.hasNext() && itb.hasNext()) {
            int ia = ita.next();
            int ib = itb.next();
            if (ia > ib)
                return -1;
            else if (ia < ib)
                return 1;
        }
        if (ita.hasNext())
            return -1;
        else if (itb.hasNext())
            return 1;
        else
            return 0;
    }
}

