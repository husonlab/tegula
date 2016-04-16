package tiler.core.dsymbols;

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
     * @param dSymbol
     * @return orbifold group name
     */
    public static String getGroupName(DSymbol dSymbol) {
        final ArrayList<Integer> rotations = new ArrayList<>();
        final ArrayList<ArrayList<Integer>> boundary = new ArrayList<>();
        final ArrayList<ArrayList<Integer>> rev_boundary = new ArrayList<>();
        int n_boundary;
        int i, j;
        int a;
        final BitSet[] mark = new BitSet[]{new BitSet(), new BitSet(), new BitSet()};
        final BitSet fl = new BitSet();
        int[] orientation = new int[dSymbol.size() + 1];


        final int euler = dSymbol.computeEulerCharacteristic();

        final int orientable = dSymbol.computeOrientation(orientation);

        for (i = 0; i <= 1; i++) {
            for (j = i + 1; j <= 2; j++) {
                for (a = 1; a <= dSymbol.size(); a = dSymbol.nextOrbit(i, j, a, fl)) {
                    if (dSymbol.isCycle(i, j, a)) {
                        int v = dSymbol.getVij(i, j, a);
                        if (v > 1)
                            rotations.add(v);
                        dSymbol.markOrbit(i, j, a, mark[3 - (i + j)]);
                    }
                }
            }
        }

        int n_boundary_rotations = 0;

        if (!dSymbol.isFixedPointFree()) {
            for (a = 1; a <= dSymbol.size(); a++) {
                for (i = 0; i <= 2; i++) {
                    if (dSymbol.getSi(i, a) == a) {
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

        n_boundary = boundary.size();

        String conway = "";

        int genus = 0;
        if (orientable > 0) {
            genus = (2 - euler - n_boundary) / 2;
            for (int k = 1; k <= genus; k++)
                conway += 'o';
        }

        if (rotations.size() > 0) {
            rotations.sort(new IntegerCompareDown());
            conway += intsAsString(rotations);
        }

        if (rotations.size() == 0 && n_boundary_rotations == 0)
            conway += '1';

        if (n_boundary > 0) {
            boundary.sort(new IntegerListCompareDown());
            rev_boundary.sort(new IntegerListCompareDown());

            final ArrayList<ArrayList<Integer>> boundaryToReport = (compare(boundary, rev_boundary) < 0 ? rev_boundary : boundary);

            for (ArrayList<Integer> it : boundaryToReport) {
                conway += '*';
                conway += intsAsString(it);
            }
        }

        if (orientable == 0) {
            genus = 2 - euler - n_boundary;
            for (int k = 1; k <= genus; k++)
                conway += 'x';
        }
        return conway;
    }

    /**
     * compute a boundary component
     *
     * @param i
     * @param j
     * @param a
     * @param mark
     * @param dSymbol
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
            j1 = result[1];
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
     * @param in
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
     * @param in
     * @return string
     */
    static private String intsAsString(ArrayList<Integer> in) {
        final StringBuilder buf = new StringBuilder();

        for (Integer n : in) {
            if (n >= 0 && n < 10)
                buf.append(String.format("%d", n));
            else
                buf.append(String.format("(%d)", n));
        }
        return buf.toString();
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

