/*
 * Weave.java Copyright (C) 2026 Daniel H. Huson
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

package tegula.tiling.parts;

import javafx.geometry.Point3D;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * decides, for each edge end, whether the band drawn along it passes over or under the bands it meets at that
 * vertex, so that the bands interlace in the manner of Moorish tilings.
 * <p>
 * A <em>dart</em> is a vertex together with one of the edges at it. Flags that share a vertex and an edge
 * differ only in the tile, so a dart is an s2-orbit {a, s2(a)}. Applying s1 keeps the vertex and the tile and
 * moves to the next edge, so the darts either side of dart(a) around its vertex are dart(s1(a)) and
 * dart(s1(s2(a))). Bands go alternately over and under around a vertex exactly when neighbouring darts carry
 * opposite labels, that is, when this dart graph is 2-colorable. Every dart belongs to just one vertex, so the
 * graph falls apart into one component per vertex and each vertex can be decided on its own.
 * <p>
 * The labels have to be a function of the D-symbol, because the renderer builds one fundamental domain and
 * copies it: a label is thus really a labelling of the quotient, and the symmetry group can obstruct it.
 * A vertex cannot be woven if
 * <ul>
 * <li>its degree is odd — there is no way to alternate around an odd cycle at all;</li>
 * <li>a mirror bisects one of the tile corners at it (in D-symbol terms s1(a) == a, or s1(a) == s2(a)), since
 * such a mirror carries an over dart to an under dart. Note that a mirror running <em>along</em> an edge is
 * harmless: it carries over to over;</li>
 * <li>a rotation at it advances the darts by an odd number of steps, as for the 4-fold vertex of the square
 * tiling, whose four darts are all the same dart in the quotient.</li>
 * </ul>
 * Such vertices are simply left unwoven, and the bands meet there flat, as they did before.
 * <p>
 * A classical interlace asks for more than this: a strap should alternate over and under along its
 * <em>length</em> too, not merely around each vertex. That is a second demand, that the two darts of one edge
 * differ, and it ties the vertices to one another. It is therefore asked of each component of the enlarged
 * dart graph, and a component that cannot meet it falls back on the per-vertex labelling, so that a tiling can
 * interlace strictly in one part and merely cross in another.
 * Daniel Huson, 8.2026
 */
public class Weave {
    /**
     * marks the mesh that outlines a band, set as its user data.
     * <p>
     * An outline has to carry the same "e=" id as the band it belongs to, so that clicking it still selects the
     * edge, but that id is also what tells the band colors to be reapplied to a node. Without this marker the
     * outline would be repainted in the band color the next moment, and then, every band being the one color,
     * nothing of the weave would show: a white strap passing over a white strap looks like no strap at all.
     */
    public static final String OUTLINE = "weave-outline";

    /**
     * representative of the dart of a flag, namely of the s2-orbit that the flag lies in
     */
    private static int dart(DSymbol ds, int a) {
        return Math.min(a, ds.getS2(a));
    }

    /**
     * decides for every flag whether the band along its edge passes over (+1) or under (-1) the other bands at
     * the flag's vertex, or whether that vertex is not woven at all (0).
     * <p>
     * Note that the label belongs to the flag's <em>0-vertex</em> end of its edge; the other end of the same
     * edge is the flag s0(a), which carries its own label.
     *
     * @return label per flag, indexed 1..ds.size()
     */
    public static int[] computeDartLabels(DSymbol ds) {
        return alternateAlongStraps(ds, computeVertexLabels(ds));
    }

    /**
     * decides at every vertex on its own which of its bands pass over and which under, by 2-coloring the darts
     * around it. A vertex whose darts cannot be 2-colored is left unwoven, with all its labels 0.
     *
     * @return label per flag, indexed 1..ds.size()
     */
    private static int[] computeVertexLabels(DSymbol ds) {
        final int[] label = new int[ds.size() + 1];

        for (int a0 : ds.orbits(1, 2)) {
            if (ds.getM12(a0) % 2 == 1) // odd degree: the bands cannot alternate around this vertex
                continue;

            final Map<Integer, Integer> color = new HashMap<>();
            final Deque<Integer> queue = new ArrayDeque<>();
            final int root = dart(ds, a0);
            color.put(root, 1);
            queue.add(root);
            boolean weavable = true;

            while (!queue.isEmpty() && weavable) {
                final int d = queue.poll();
                final int here = color.get(d);
                // both flags of the dart, as each leads to one of its two neighbours around the vertex
                for (int a : new int[]{d, ds.getS2(d)}) {
                    final int neighbour = dart(ds, ds.getS1(a));
                    if (neighbour == d) { // a mirror through the tile corner identifies the two darts
                        weavable = false;
                        break;
                    }
                    final Integer there = color.get(neighbour);
                    if (there == null) {
                        color.put(neighbour, -here);
                        queue.add(neighbour);
                    } else if (there == here) { // an odd cycle: a rotation moves the darts an odd number of steps
                        weavable = false;
                        break;
                    }
                }
            }

            if (weavable) {
                for (int a : ds.orbitMembers(1, 2, a0))
                    label[a] = color.getOrDefault(dart(ds, a), 0);
            }
        }
        return label;
    }

    /**
     * strengthens the labelling so that a strap alternates over and under along its <em>length</em> as well as
     * around each vertex, which is what a classical interlace does.
     * <p>
     * Alternating around a vertex asks neighbouring darts to differ; alternating along a strap asks the two
     * darts of one edge, dart(a) and dart(s0(a)), to differ as well. The second kind of demand ties the
     * vertices to one another, so the dart graph no longer falls apart into one component per vertex and the
     * question has to be asked of each component of the larger graph. Where a component is 2-colorable under
     * both demands, the strap really does alternate; where it is not, that component keeps the per-vertex
     * labelling it already had, so a tiling can interlace strictly in one part and merely cross in another.
     * <p>
     * Darts of vertices that could not be woven at all carry no label and take no part: an edge with one end at
     * such a vertex places no demand on its other end.
     *
     * @param vertexLabels the per-vertex labelling to strengthen, and to fall back on
     * @return label per flag, indexed 1..ds.size()
     */
    private static int[] alternateAlongStraps(DSymbol ds, int[] vertexLabels) {
        final int[] label = vertexLabels.clone();

        final Set<Integer> woven = new HashSet<>();
        for (int a = 1; a <= ds.size(); a++) {
            if (vertexLabels[a] != 0)
                woven.add(dart(ds, a));
        }

        final Set<Integer> visited = new HashSet<>();
        for (int a = 1; a <= ds.size(); a++) {
            final int root = dart(ds, a);
            if (!woven.contains(root) || !visited.add(root))
                continue;

            final Map<Integer, Integer> color = new HashMap<>();
            final Deque<Integer> queue = new ArrayDeque<>();
            color.put(root, 1);
            queue.add(root);
            boolean alternating = true;

            // the whole component is collected even once it is known not to 2-color, so that it is not
            // visited again from another of its darts
            while (!queue.isEmpty()) {
                final int d = queue.poll();
                final int here = color.get(d);
                for (int neighbour : neighbours(ds, d, woven)) {
                    final Integer there = color.get(neighbour);
                    if (there == null) {
                        color.put(neighbour, -here);
                        visited.add(neighbour);
                        queue.add(neighbour);
                    } else if (there == here) {
                        alternating = false;
                    }
                }
            }

            if (alternating) {
                for (var entry : color.entrySet()) {
                    label[entry.getKey()] = entry.getValue();
                    label[ds.getS2(entry.getKey())] = entry.getValue();
                }
            }
        }
        return label;
    }

    /**
     * the darts that have to carry the opposite label to the given one: the two next to it around its vertex,
     * and the one at the other end of its edge. Darts that are not woven are left out.
     */
    private static List<Integer> neighbours(DSymbol ds, int d, Set<Integer> woven) {
        final List<Integer> list = new ArrayList<>();
        for (int a : new int[]{d, ds.getS2(d)}) {
            final int around = dart(ds, ds.getS1(a)); // next edge at the same vertex
            if (woven.contains(around))
                list.add(around);
            final int along = dart(ds, ds.getS0(a)); // other end of the same edge
            if (woven.contains(along))
                list.add(along);
        }
        return list;
    }

    /**
     * how many of the vertices of the tiling could be woven, and how many there are in total. Used to tell the
     * user why a tiling does not interlace everywhere.
     *
     * @return {number of vertex orbits woven, number of vertex orbits}
     */
    public static int[] countWovenVertices(DSymbol ds, int[] dartLabels) {
        int woven = 0, all = 0;
        for (int a0 : ds.orbits(1, 2)) {
            all++;
            if (dartLabels[a0] != 0)
                woven++;
        }
        return new int[]{woven, all};
    }

    /**
     * how many of the edges of the tiling carry a strap that alternates over and under along it, out of those
     * whose two ends are both woven and so could
     *
     * @return {number of edge orbits alternating, number of edge orbits that could}
     */
    public static int[] countAlternatingEdges(DSymbol ds, int[] dartLabels) {
        int alternating = 0, could = 0;
        for (int a0 : ds.orbits(0, 2)) {
            final int here = dartLabels[a0], there = dartLabels[ds.getS0(a0)];
            if (here != 0 && there != 0) {
                could++;
                if (here != there)
                    alternating++;
            }
        }
        return new int[]{alternating, could};
    }

    /**
     * how finely the polyline of a band has to be sampled for it to undulate smoothly. The euclidean case
     * supplies only three points per half edge, and a band that rises over so few points is a pair of ramps
     * rather than a curve: worse, the round caps that {@link Band3D} sets at the ends of a band are flat discs,
     * so where the band ramps away from a cap the disc juts out in front of it.
     */
    private static final int MIN_SEGMENTS = 16;

    /** a strand that passes over a vertex is lifted there; one that passes under, or is not woven, stays down */
    private static double lift(int dartLabel, double amplitude) {
        return (dartLabel > 0 ? amplitude : 0);
    }

    /**
     * subdivides a polyline until it has enough segments for a band along it to rise and fall smoothly,
     * repeatedly halving every segment at its (geodesic) midpoint
     */
    public static Point3D[] refine(Geometry geom, Point3D[] points) {
        Point3D[] result = points;
        while (result.length >= 2 && result.length - 1 < MIN_SEGMENTS) {
            final Point3D[] finer = new Point3D[2 * result.length - 1];
            for (int i = 0; i + 1 < result.length; i++) {
                finer[2 * i] = result[i];
                finer[2 * i + 1] = Tools.midpoint3D(geom, result[i], result[i + 1]);
            }
            finer[finer.length - 1] = result[result.length - 1];
            result = finer;
        }
        return result;
    }

    /**
     * height above the surface at which the band along the edge of the given flag runs, from its 0-vertex end
     * to the center of the edge. A strand that passes over a vertex rises to the full amplitude there; one that
     * passes under stays down on the surface, and the two halves of an edge meet at their mean.
     * <p>
     * Note that the strands are lifted rather than straddling the surface: the bands run only just above the
     * tiles — half a unit, on a sphere of radius 100 — so a strand pushed <em>down</em> to pass under another
     * would sink into the tiles and disappear behind them. Lifting is also what real interlace does: the strap
     * underneath lies flat and the one on top rides over it.
     * <p>
     * Both halves arrive at the center of the edge with the same value and a horizontal tangent, so the strand
     * undulates smoothly along its length rather than kinking at the middle of an edge.
     *
     * @param steps     number of points along the half edge
     * @param reversed  true if the points run from the center of the edge to the vertex instead
     * @param amplitude how far over and under the strands are displaced
     * @return height per point
     */
    public static double[] heightProfile(DSymbol ds, int[] dartLabels, int a, int steps, boolean reversed,
                                         double amplitude) {
        final double atVertex = lift(dartLabels[a], amplitude);
        final double atCenter = 0.5 * (lift(dartLabels[a], amplitude) + lift(dartLabels[ds.getS0(a)], amplitude));

        final double[] height = new double[steps];
        for (int i = 0; i < steps; i++) {
            final double t = (steps == 1 ? 0 : (double) i / (steps - 1));
            final double smooth = t * t * (3 - 2 * t); // smoothstep: flat at the vertex and at the center
            // smoothstep is symmetric, so 1-smooth(t) is smooth(1-t): the profile simply runs backwards
            height[i] = atVertex + (atCenter - atVertex) * (reversed ? 1 - smooth : smooth);
        }
        return height;
    }
}
