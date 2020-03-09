/*
 * BreakSymmetries.java Copyright (C) 2020. Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class BreakSymmetries {
    public enum Task {RemoveOne, RemoveSome, RemoveAll}

    /**
     * greedily remove rotations
     *
     * @param ds0
     * @param task one, some or all - only some returns more than one symbol
     * @return all tilings with some rotations removed
     */
    public static ArrayList<DSymbol> removeRotations(final DSymbol ds0, final Task task) {
        final ArrayList<DSymbol> result = new ArrayList<>();

        DSymbol ds = DSymbolAlgorithms.orientate(ds0);

        int count = 0;

        while (true) {
            final DSymbol dsf = ds;

            for (int[] ija : ds.orbitStream().filter(o -> dsf.getVij(o[0], o[1], o[2]) > 1).sorted(Comparator.comparingInt(o -> dsf.getVij(o[0], o[1], o[2]))).collect(Collectors.toList())) {
                final int i = ija[0];
                final int j = ija[1];
                final int a = ija[2];
                final int ai = ds.getSi(i, a);
                final int v = ds.getVij(i, j, a);

                final DSymbol ds1 = new DSymbol();
                for (int k = 0; k < v; k++) { // v copies
                    ds1.append(ds);
                }

                final int offset = ds.size();

                System.err.println("offset: " + offset + " v*offset: " + (offset * v) + " new size: " + ds1.size());

                for (int k = 0; k < v; k++) { // v copies
                    if (k == 0) {
                        int p = a + (v - 1) * offset;
                        int q = ai;
                        ds1.setSi(i, p, q);
                    } else {
                        int p = a + (k - 1) * offset;
                        int q = ai + k * offset;
                        ds1.setSi(i, p, q);
                    }
                }
                try {
                    for (int b = 1; b <= ds1.size(); b++) {
                        for (int k = 0; k <= 2; k++)
                            ds1.getVij(DSymbol.i(k), DSymbol.j(k), b); // call this to trigger divisibility check
                    }

                    System.err.println(ds1);

                    final boolean hasMore = ds1.orbitStream().anyMatch(o -> ds1.getVij(o[0], o[1], o[2]) > 1);

                    if (task != Task.RemoveAll || !hasMore) {
                        final DSymbol dsNew = new DSymbol(ds1);
                        dsNew.setNr1(ds.getNr1());
                        dsNew.setNr2(++count);
                        result.add(dsNew);
                        if (task == Task.RemoveOne || task == Task.RemoveAll || task == Task.RemoveSome && !hasMore)
                            return result;
                    }
                    ds = ds1;
                    break;
                } catch (Exception ignored) {
                }
            }
        }
    }
}
