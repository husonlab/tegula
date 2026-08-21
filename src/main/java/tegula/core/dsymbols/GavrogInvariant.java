/*
 * GavrogInvariant.java Copyright (C) 2026 Daniel H. Huson
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

/**
 * the canonical form of a Delaney symbol as computed by Gavrog
 * <p>
 * This is a port of DelaneySymbol.invariant() and Traversal.findNext() from Gavrog by Olaf
 * Delgado-Friedrichs (https://github.com/odf/gavrog, Apache License 2.0), which are the reference
 * implementation of Algorithm 8 in O. Delgado-Friedrichs, Data structures and algorithms for
 * tilings I, Theoretical Computer Science 303 (2003) 431-445.
 * <p>
 * The paper fixes the shape of the algorithm -- try every element as a seed, relabel in traversal
 * order, keep the smallest -- but explicitly leaves the comparison function open: "The particular
 * choice of comparison function is irrelevant here as long as it is used consistently." Gavrog
 * closes that gap with the protocol built below, and with a traversal that is not the plain
 * depth-first search of the paper: it keeps one queue per index, always drains the lowest-index
 * non-empty queue, and takes from the back for indices 0 and 1 but from the front for index 2.
 * <p>
 * The invariant is the protocol itself, a sequence of integers. Two Delaney symbols are isomorphic
 * exactly when their protocols are equal. This is a different canonical form from the one in
 * {@link DSymbolAlgorithms}, not a better or worse one: both are complete invariants, they simply
 * pick different representatives of an isomorphism class.
 * <p>
 * Daniel Huson, 8.2026
 */
public class GavrogInvariant {
    /**
     * Tegula Delaney symbols are 2-dimensional, so the indices are 0, 1 and 2
     */
    private static final int DIM = 2;

    /**
     * the protocol for one seed, together with the labeling that produced it
     */
    private record Candidate(int[] protocol, int[] new2old) {
    }

    /**
     * computes the Gavrog invariant of a Delaney symbol.
     * For a symbol that is not connected, the invariants of the connected components are sorted and
     * concatenated, as Gavrog does
     *
     * @return invariant
     */
    public static int[] invariant(DSymbol ds) {
        if (ds.size() == 0)
            return new int[0];

        final ArrayList<BitSet> components = DSymbolAlgorithms.connectedComponents(ds);
        if (components.size() == 1)
            return computeConnected(ds).protocol();

        final ArrayList<int[]> parts = new ArrayList<>();
        for (BitSet component : components)
            parts.add(computeConnected(DSymbolAlgorithms.extractComponent(ds, component)).protocol());
        parts.sort(GavrogInvariant::compareProtocols);

        int length = 0;
        for (int[] part : parts)
            length += part.length;
        final int[] result = new int[length];
        int pos = 0;
        for (int[] part : parts) {
            System.arraycopy(part, 0, result, pos, part.length);
            pos += part.length;
        }
        return result;
    }

    /**
     * the invariant, written as space-separated numbers
     *
     * @return invariant as string
     */
    public static String invariantString(DSymbol ds) {
        final int[] invariant = invariant(ds);
        final StringBuilder buf = new StringBuilder();
        for (int value : invariant) {
            if (buf.length() > 0)
                buf.append(" ");
            buf.append(value);
        }
        return buf.toString();
    }

    /**
     * computes the canonical form, that is, the symbol relabeled by the numbering that produced the
     * invariant. The symbol number is set to 0.0
     *
     * @return canonical form
     */
    public static DSymbol canonicalForm(DSymbol ds) {
        if (ds.size() == 0)
            return new DSymbol(0);

        final ArrayList<BitSet> components = DSymbolAlgorithms.connectedComponents(ds);
        if (components.size() == 1) {
            final Candidate best = computeConnected(ds);
            return relabel(ds, best.new2old());
        }

        final ArrayList<Candidate> parts = new ArrayList<>();
        final ArrayList<DSymbol> subs = new ArrayList<>();
        for (BitSet component : components) {
            final DSymbol sub = DSymbolAlgorithms.extractComponent(ds, component);
            subs.add(sub);
            parts.add(computeConnected(sub));
        }
        final Integer[] order = new Integer[parts.size()];
        for (int i = 0; i < order.length; i++)
            order[i] = i;
        Arrays.sort(order, (a, b) -> compareProtocols(parts.get(a).protocol(), parts.get(b).protocol()));

        final DSymbol result = new DSymbol(0);
        for (int i : order)
            result.append(relabel(subs.get(i), parts.get(i).new2old()));
        return result;
    }

    /**
     * the canonical form, as a string
     *
     * @return canonical form as string
     */
    public static String canonicalString(DSymbol ds) {
        return canonicalForm(ds).toString();
    }

    /**
     * computes the invariant of a connected Delaney symbol, together with the labeling that produced it.
     * Follows DelaneySymbol.invariant() in Gavrog, including its early cutoff and its treatment of an
     * undefined v-value as larger than any defined one
     *
     * @return best candidate
     */
    private static Candidate computeConnected(DSymbol ds) {
        final int size = ds.size();
        final int[] current = new int[(size + 1) * (4 * DIM + 3)];

        int[] best = null;
        int bestLength = 0;
        int[] bestNew2Old = null;
        int protocolLength = -1;

        for (int seed = 1; seed <= size; seed++) {
            final int[][] edges = traversal(ds, seed);
            final int[] old2new = new int[size + 1];
            final int[] new2old = new int[size + 1];
            int nextE = 1;
            int k = 0;
            boolean abandoned = false;

            for (int[] edge : edges) {
                final int i = edge[0];
                final int d = edge[1];

                final boolean elementIsNew;
                final int e;
                if (old2new[d] == 0) {
                    elementIsNew = true;
                    e = nextE++;
                    old2new[d] = e;
                    new2old[e] = d;
                } else {
                    elementIsNew = false;
                    e = old2new[d];
                }

                if (best != null) {
                    if (i > best[k]) {
                        abandoned = true;
                        break;
                    } else if (i < best[k])
                        best = null;
                }
                current[k++] = i;

                if (i >= 0) {
                    final int ei = old2new[ds.getSi(i, d)];
                    if (best != null) {
                        if (ei > best[k]) {
                            abandoned = true;
                            break;
                        } else if (ei < best[k])
                            best = null;
                    }
                    current[k++] = ei;
                }

                if (best != null) {
                    if (e > best[k]) {
                        abandoned = true;
                        break;
                    } else if (e < best[k])
                        best = null;
                }
                current[k++] = e;

                if (elementIsNew) {
                    boolean bad = false;
                    for (int m = 0; m < DIM; m++) {
                        final int v = ds.getVij(m, m + 1, d);
                        if (best != null) {
                            if (v > best[k] || (v == 0 && best[k] != 0)) {
                                bad = true;
                                break;
                            } else if (v < best[k] || (v != 0 && best[k] == 0))
                                best = null;
                        }
                        current[k++] = v;
                    }
                    if (bad) {
                        abandoned = true;
                        break;
                    }
                }
            }

            if (!abandoned) {
                // Gavrog compares protocols position by position without a length check, which is only
                // sound because every seed yields a protocol of the same length. Verify that here
                if (protocolLength == -1)
                    protocolLength = k;
                else if (k != protocolLength)
                    throw new RuntimeException("Gavrog invariant: protocol length %d != %d".formatted(k, protocolLength));
            }

            if (best == null) {
                best = current.clone();
                bestLength = k;
                bestNew2Old = new2old;
            }
        }
        return new Candidate(Arrays.copyOf(best, bestLength), bestNew2Old);
    }

    /**
     * the index priority traversal of Gavrog, visiting every edge.
     * One queue per index, always draining the lowest-index non-empty queue, taking from the back for
     * indices 0 and 1 and from the front for index 2
     *
     * @return edges, each as a pair of an index and an element, with index -1 for the seed
     */
    private static int[][] traversal(DSymbol ds, int seed) {
        final int size = ds.size();
        final List<Deque<Integer>> buffer = new ArrayList<>();
        for (int i = 0; i <= DIM; i++)
            buffer.add(new ArrayDeque<>());
        final int[] number = new int[size + 1];
        int next = 1;

        final ArrayList<int[]> result = new ArrayList<>();
        boolean seedUsed = false;

        while (true) {
            int[] edge = null;
            scan:
            for (int i = 0; i <= DIM; i++) {
                final Deque<Integer> queue = buffer.get(i);
                while (!queue.isEmpty()) {
                    final int d = (i < 2 ? queue.removeLast() : queue.removeFirst());
                    if (number[d] == 0) {
                        number[d] = next++;
                        for (int j = 0; j <= DIM; j++) {
                            if (j != i)
                                buffer.get(j).addLast(ds.getSi(j, d));
                        }
                        edge = new int[]{i, d};
                        break scan;
                    } else if (number[ds.getSi(i, d)] <= number[d]) {
                        edge = new int[]{i, d};
                        break scan;
                    }
                }
            }
            if (edge == null) {
                if (seedUsed)
                    break;
                seedUsed = true;
                number[seed] = next++;
                for (int i = 0; i <= DIM; i++)
                    buffer.get(i).addLast(ds.getSi(i, seed));
                edge = new int[]{-1, seed};
            }
            result.add(edge);
        }
        return result.toArray(new int[0][]);
    }

    /**
     * relabels a symbol, giving the element new2old[a] the new label a. The symbol number is set to 0.0
     *
     * @return relabeled symbol
     */
    private static DSymbol relabel(DSymbol ds, int[] new2old) {
        final int size = ds.size();
        final int[] old2new = new int[size + 1];
        for (int a = 1; a <= size; a++)
            old2new[new2old[a]] = a;

        final DSymbol result = new DSymbol(size);
        for (int a = 1; a <= size; a++) {
            for (int i = 0; i <= DIM; i++)
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
     * compares two protocols lexicographically
     *
     * @return comparison
     */
    private static int compareProtocols(int[] a, int[] b) {
        return Arrays.compare(a, b);
    }
}
