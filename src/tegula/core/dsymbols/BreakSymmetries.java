/*
 * BreakSymmetries.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class BreakSymmetries {
    public enum Task {RemoveOne, RemoveSome, RemoveAll}

    /**
     * greedily remove rotations
     *
     * @param task one, some or all - only some returns more than one symbol
     * @return all tilings with some rotations removed
     */
    public static ArrayList<DSymbol> removeRotations(final DSymbol ds0, final Task task) {
        final ArrayList<DSymbol> result = new ArrayList<>();

        final Stack<DSymbol> stack = new Stack<>();
        stack.push(DSymbolAlgorithms.orientate(ds0));

        while (stack.size() > 0) {
            final DSymbol ds = stack.pop();

			final Set<Integer> vijs = OrbifoldGroupName.getGroupNameAsList(ds).stream().filter(NumberUtils::isInteger).map(NumberUtils::parseInt).collect(Collectors.toSet());

            for (int[] ija : ds.orbitLabelsStream().filter(o -> ds.getVij(o[0], o[1], o[2]) > 1).sorted(Comparator.comparingInt(o -> ds.getVij(o[0], o[1], o[2]))).collect(Collectors.toList())) {
                final int v = ds.getVij(ija[0], ija[1], ija[2]);

                final ArrayList<Integer> orbit = ds.orbitMembers(ija[0], ija[1], ija[2]);
                for (int a : orbit) {
                    for (int z = 0; z <= 1; z++) {
                        final int i = ija[z];
                        final int ai = ds.getSi(i, a);

                        final DSymbol ds1 = new DSymbol();
                        for (int k = 0; k < v; k++) { // v copies
                            ds1.append(ds);
                        }

                        final int offset = ds.size();

                        for (int k = 0; k < v; k++) { // v copies
                            final int p;
                            if (k == 0)
                                p = a + (v - 1) * offset;  // wrap around
                            else
                                p = a + (k - 1) * offset;

                            final int q = ai + k * offset;

                            ds1.setSi(i, p, q);
                        }

                        boolean ok = true;
                        for (int b : orbit) {
                            for (int k = 0; k <= 2; k++) {
                                final int mij = ds1.getMij(DSymbol.i(k), DSymbol.j(k), b);
                                final int rij = ds1.computeOrbitLength(DSymbol.i(k), DSymbol.j(k), b);
                                if (k == 1 && rij > 2 || mij % rij != 0) {
                                    ok = false;
                                    break;
                                }
                            }
                        }

                        if (false && ok) {
							final Set<Integer> newVijs = OrbifoldGroupName.getGroupNameAsList(ds1).stream().filter(NumberUtils::isInteger).map(NumberUtils::parseInt).collect(Collectors.toSet());
                            if (vijs.equals(newVijs))
                                ok = false;
                        }

                        if (ok) {
                            final boolean hasMore = ds1.orbitLabelsStream().anyMatch(o -> ds1.getVij(o[0], o[1], o[2]) > 1);

                            switch (task) {
                                case RemoveOne:
                                    ds1.setNr1(ds.getNr1());
                                    ds1.setNr2(ds.getNr2() + 1);
                                    result.add(ds1);
                                    return result;
                                case RemoveSome:
                                    result.add(ds1);
                                    if (hasMore)
                                        stack.push(ds1);
                                    break;
                                case RemoveAll:
                                    if (hasMore)
                                        stack.push(ds1);
                                    else {
                                        ds1.setNr1(ds.getNr1());
                                        ds1.setNr2(ds.getNr2() + 1);
                                        result.add(ds1);
                                        return result;
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        }
        int nr2 = ds0.getNr2();
        for (DSymbol ds : result) {
            ds.setNr1(ds.getNr1());
            ds.setNr2(++nr2);
        }
        return result;
    }

    /**
     * greedily remove rotations
     *
     * @param ds0 orientable dsymbol
     * @return all tilings with some rotations removed
     */
    public static DSymbol removeRotations(final DSymbol ds0, int k, int which) {
        final String groupName = OrbifoldGroupName.getGroupName(ds0);
        if (groupName.contains("*") || groupName.contains("x"))
            return null; // is not orientable

        final int i0 = DSymbol.i(k);
        final int j0 = DSymbol.j(k);
        final int a0 = ds0.getFlagForOrbit(i0, j0, which);
        final int v = ds0.getVij(i0, j0, a0);

        final ArrayList<Integer> orbit = ds0.orbitMembers(i0, j0, a0);
        for (int a : orbit) {
            for (int z = 0; z <= 1; z++) {
                final int i = (z == 0 ? i0 : j0);
                final int ai = ds0.getSi(i, a);

                final DSymbol ds1 = new DSymbol();
                for (int t = 0; t < v; t++) { // v copies
                    ds1.append(ds0);
                }

                final int offset = ds0.size();

                for (int t = 0; t < v; t++) { // v copies
                    final int p;
                    if (t == 0)
                        p = a + (v - 1) * offset;  // wrap around
                    else
                        p = a + (t - 1) * offset;

                    final int q = ai + t * offset;

                    ds1.setSi(i, p, q);
                }

                boolean ok = (ds1.size() > ds0.size());
                if (ok) {
                    for (int a1 : orbit) {
                        for (int i1 = 0; i1 <= 1; i1++) {
                            for (int j1 = i1 + 1; j1 <= 2; j1++) {
                                final int mij = ds1.getMij(i1, j1, a1);
                                final int rij = ds1.computeOrbitLength(i1, j1, a1);
                                if (i1 == 0 && j1 == 2 && rij > 2 || mij % rij != 0) {
                                    ok = false;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (ok)
                    return ds1;
            }
        }
        return null;
    }

}
