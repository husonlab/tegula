/*
 * TileRelief.java Copyright (C) 2026 Daniel H. Huson
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
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

import java.util.Collection;

/**
 * builds tiles in relief: each tile is inset from its true boundary, so that neighbouring tiles are
 * delineated by a gap rather than by a drawn band, and optionally carries a lip along its boundary that
 * bends down into the surface (into the sphere, into the hyperboloid, or below the euclidean plane).
 * <p>
 * A chamber (flag) triangle has vertices (0-vertex, 1-vertex, 2-vertex) = (tiling vertex, edge center,
 * tile center). All chambers of a tile share the same 2-vertex, so insetting every chamber towards that
 * common point erodes the tile as a whole, without opening cracks between the chambers of one tile.
 * Daniel Huson, 8.2026
 */
public class TileRelief {
    // orientation of the emitted lip triangles that JavaFX treats as front-facing (determined empirically)
    private static final boolean FLIP_LIP_WINDING = false;

    /**
     * how far the substrate must lie below the tiles, whatever the lip depth. A substrate that hugs the tiles
     * z-fights with them, speckling the whole tiling, so its depth must not simply follow the lip down to 0
     */
    public static final double MIN_SUBSTRATE_DEPTH = 2.5;

    /**
     * lips shallower than this are not worth building: they are invisible, and their near-degenerate quads
     * only risk z-fighting with the edge of the tile
     */
    public static final double MIN_LIP_DEPTH = 0.05;

    /**
     * smallest sine used when converting a perpendicular inset into a radial step, which caps that step at
     * amount/MITRE_LIMIT and so decides how faithfully the gap comes out at the width asked for.
     * <p>
     * The cap is needed because keeping the gap at exactly the requested width around a sharp tip means cutting
     * that tip back by amount/sin(angle), which for a fine enough tip amputates it altogether. Where the cap
     * bites, the gap is narrower than asked instead. The value trades one against the other, and 0.25 leaves the
     * gap short only at tips whose interior angle is below roughly 29 degrees: measured over the spherical
     * database, the mean achieved inset is 0.96 of the amount asked for, against 0.89 at the old value of 0.5,
     * while going further to 0.15 buys only another 0.02 and doubles the worst-case erosion of a tip.
     */
    private static final double MITRE_LIMIT = 0.25;

    /**
     * indices into a chamber's point array that trace the part of the tile boundary that runs through the
     * chamber, namely 0-vertex -> 2-edge-center -> 1-vertex. These are the same points that the bands
     * (drawn edges) follow.
     *
     * @return indices of the boundary polyline
     */
    public static int[] boundaryIndices(Geometry geom, boolean smoothEdges) {
        if (geom == Geometry.Spherical && smoothEdges)
            return new int[]{0, 16, 13, 22, 10, 46, 43, 52, 7, 142, 139, 148, 136, 172, 169, 178, 5,
                    271, 268, 277, 265, 301, 298, 307, 262, 397, 394, 403, 391, 427, 424, 433, 1};
        else if (geom == Geometry.Hyperbolic)
            return new int[]{0, 9, 7, 10, 5, 11, 8, 12, 1};
        else
            return new int[]{0, 5, 1};
    }

    /**
     * extract the boundary polyline from a chamber's points
     */
    public static Point3D[] extractBoundary(Point3D[] chamberPoints, int[] boundaryIndices) {
        final Point3D[] boundary = new Point3D[boundaryIndices.length];
        for (int i = 0; i < boundaryIndices.length; i++)
            boundary[i] = chamberPoints[boundaryIndices[i]];
        return boundary;
    }

    /**
     * sines of the angles at which the tile boundary leaves the two ends of a chamber's boundary polyline,
     * measured against the spoke that runs from that end to the tile center. These are what
     * {@link #insetTowardsCenter} turns into the radial step at the ends of the boundary.
     * <p>
     * Each end is shared with a neighbouring chamber of the same tile, which has to move it to exactly the same
     * place. The two chambers do not in general meet the spoke at the same angle, so neither may use its own
     * sine there: the caller combines the sines of both chambers first, see {@link #insetTowardsCenter}.
     *
     * @return {sine at the first boundary point, sine at the last boundary point}
     */
    public static double[] cornerSines(Point3D[] points, Point3D center, int[] boundaryIndices) {
        final int n = boundaryIndices.length;
        if (n < 2)
            return new double[]{1, 1};
        final Point3D first = points[boundaryIndices[0]], second = points[boundaryIndices[1]];
        final Point3D last = points[boundaryIndices[n - 1]], nextToLast = points[boundaryIndices[n - 2]];
        return new double[]{sineOfAngle(second.subtract(first), center.subtract(first)),
                sineOfAngle(last.subtract(nextToLast), center.subtract(last))};
    }

    /**
     * erodes a chamber so that the tile boundary moves inwards by the given amount, measured perpendicular to
     * the boundary. Every point keeps its azimuth around the tile center and is scaled along that ray, so
     * points shared with the neighbouring chambers of the same tile move identically and the tile does not
     * crack open along its spokes.
     * <p>
     * Note that the radial step is not simply the amount: the ray from the tile center meets the boundary at
     * an angle, so a radial step of d only insets the boundary by d*sin(angle). Eroding radially by a constant
     * amount therefore insets a lot where the ray is perpendicular to the boundary and hardly at all where it
     * runs along it, which pinches long tiles into bow-ties. Dividing by the sine restores a uniform inset.
     *
     * @param cornerSines sines to use at the two ends of the boundary instead of this chamber's own, see
     *                    {@link #cornerSines}; null to use its own
     */
    public static Point3D[] insetTowardsCenter(Geometry geom, Point3D[] points, Point3D center,
                                               int[] boundaryIndices, double amount, boolean keepOriginal,
                                               double[] cornerSines) {
        final Point3D[] original = (keepOriginal ? points.clone() : null);
        final int n = boundaryIndices.length;
        if (amount <= 0 || n < 2)
            return original;

        // a frame in the tangent plane at the tile center, in which to measure azimuths around it
        final Point3D up = outOfSurface(geom, center);
        Point3D e1 = points[boundaryIndices[0]].subtract(center);
        e1 = e1.subtract(up.multiply(e1.dotProduct(up)));
        if (!(e1.magnitude() > 0.000001))
            return original;
        e1 = e1.normalize();
        final Point3D e2 = up.crossProduct(e1);

        final double[] azimuth = new double[n], distance = new double[n], step = new double[n];
        for (int j = 0; j < n; j++) {
            final Point3D b = points[boundaryIndices[j]];
            azimuth[j] = azimuth(b, center, e1, e2);
            distance[j] = Tools.geometricDistance(geom, center, b);
            final Point3D along = points[boundaryIndices[Math.min(n - 1, j + 1)]]
                    .subtract(points[boundaryIndices[Math.max(0, j - 1)]]);
            final double sine;
            if (cornerSines != null && j == 0)
                sine = cornerSines[0];
            else if (cornerSines != null && j == n - 1)
                sine = cornerSines[1];
            else
                sine = sineOfAngle(along, center.subtract(b));
            // A mitre limit, as in polygon offsetting: at a sharp corner the exact offset runs away along the
            // bisector, and the coarse boundary sampling then cannot represent it, which tears small holes
            // where tiles meet. Capping the step at twice the amount trades a little corner accuracy for that.
            step[j] = amount / Math.max(sine, MITRE_LIMIT);
        }
        unwrap(azimuth);

        for (int i = 0; i < points.length; i++) {
            if (points[i] == null)
                continue;
            // note the negated test: in hyperbolic geometry the distance of a point to itself comes back as
            // NaN (arccosh of a value a hair below 1), and any comparison with NaN is false, so "<= 0" would
            // let it through and the point would be interpolated to garbage
            if (!(Tools.geometricDistance(geom, center, points[i]) > 0))
                continue;
            final double a = nearestBranch(azimuth(points[i], center, e1, e2), azimuth[0]);
            final int j = segmentAt(a, azimuth);
            final double f = fractionWithin(a, azimuth, j);
            final double boundaryDistance = distance[j] + f * (distance[j + 1] - distance[j]);
            final double radialStep = step[j] + f * (step[j + 1] - step[j]);
            if (!(boundaryDistance > 0))
                continue;
            // the whole ray is scaled by one factor, so the map stays monotone: were points just inside the
            // eroded range to end up further out than points just outside it, the triangles between them
            // would flip orientation, be back-face culled, and leave holes in the tile
            final Point3D result = interpolate(geom, center, points[i],
                    Math.max(0, (boundaryDistance - radialStep) / boundaryDistance));
            if (isFinite(result))
                points[i] = result;
        }
        return original;
    }

    private static double azimuth(Point3D point, Point3D center, Point3D e1, Point3D e2) {
        final Point3D d = point.subtract(center);
        return Math.atan2(d.dotProduct(e2), d.dotProduct(e1));
    }

    /** makes a run of azimuths continuous, so that it does not jump when it crosses +/- pi */
    private static void unwrap(double[] azimuth) {
        for (int j = 1; j < azimuth.length; j++) {
            while (azimuth[j] - azimuth[j - 1] > Math.PI) azimuth[j] -= 2 * Math.PI;
            while (azimuth[j] - azimuth[j - 1] < -Math.PI) azimuth[j] += 2 * Math.PI;
        }
    }

    private static double nearestBranch(double a, double reference) {
        while (a - reference > Math.PI) a -= 2 * Math.PI;
        while (a - reference < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    /** sine of the angle between two vectors */
    private static double sineOfAngle(Point3D u, Point3D v) {
        final double lengths = u.magnitude() * v.magnitude();
        if (!(lengths > 0))
            return 1;
        final double cos = u.dotProduct(v) / lengths;
        return Math.sqrt(Math.max(0, 1 - cos * cos));
    }

    /** index of the boundary segment containing the given azimuth, clamped to the ends */
    private static int segmentAt(double a, double[] azimuth) {
        final int n = azimuth.length;
        final boolean increasing = (azimuth[n - 1] >= azimuth[0]);
        for (int j = 0; j + 1 < n; j++) {
            final double lo = azimuth[j], hi = azimuth[j + 1];
            if (increasing ? (a >= lo && a <= hi) : (a <= lo && a >= hi))
                return j;
        }
        return (Math.abs(a - azimuth[0]) <= Math.abs(a - azimuth[n - 1]) ? 0 : n - 2);
    }

    private static double fractionWithin(double a, double[] azimuth, int j) {
        final double span = azimuth[j + 1] - azimuth[j];
        return (Math.abs(span) < 1e-12 ? 0 : Math.max(0, Math.min(1, (a - azimuth[j]) / span)));
    }

    private static boolean isFinite(Point3D point) {
        return Double.isFinite(point.getX()) && Double.isFinite(point.getY()) && Double.isFinite(point.getZ());
    }

    /**
     * point at the given fraction along the geodesic from a to b (fraction 0 gives a, fraction 1 gives b)
     */
    private static Point3D interpolate(Geometry geom, Point3D a, Point3D b, double fraction) {
        return switch (geom) {
            // interpolateSpherePoints returns a unit-length direction, so restore the sphere radius
            case Spherical -> Tools.interpolateSpherePoints(a, b, fraction).multiply(b.magnitude());
            case Hyperbolic -> Tools.interpolateHyperbolicPoints(a, b, fraction);
            default -> a.add(b.subtract(a).multiply(fraction));
        };
    }

    /**
     * moves a point into the surface, i.e. away from the viewer: towards the center of the sphere, into the
     * hyperboloid, or below the euclidean plane.
     * <p>
     * Note that the sphere and the hyperboloid are displaced radially rather than along the surface normal at
     * the point: a constant displacement along the (Minkowski) normal does not produce a surface parallel to
     * the hyperboloid, it warps it, so that the displaced surface pokes back through the tiles.
     */
    public static Point3D pushIntoSurface(Geometry geom, Point3D point, double depth) {
        return switch (geom) {
            case Spherical -> {
                final double length = point.magnitude();
                yield (length <= 0 ? point : point.multiply(Math.max(0.05, (length - depth) / length)));
            }
            case Hyperbolic -> point.multiply(1 + depth / 100);
            default -> point.add(0, 0, depth);
        };
    }

    /**
     * unit vector pointing from the given point out of the surface, towards the viewer
     */
    private static Point3D outOfSurface(Geometry geom, Point3D point) {
        final Point3D direction = point.subtract(pushIntoSurface(geom, point, 1));
        return (direction.magnitude() > 0 ? direction.normalize() : new Point3D(0, 0, -1));
    }

    /**
     * creates the grout floor for one tile: the ring between the inset tile boundary and the true tile
     * boundary, sunk into the surface. Neighbouring tiles are eroded towards their own centers, so these
     * rings meet exactly along the true tile boundaries and together they floor the whole gap.
     * <p>
     * Note that the floor deliberately does not extend underneath the tile itself. An opaque surface lying
     * directly behind a semi-transparent tile shows through along every triangle edge of that tile, because
     * the two half-covered samples either side of a shared edge each blend the backdrop separately and do not
     * add back up to full coverage. Keeping the floor out from under the tiles avoids that entirely.
     *
     * @param insetBoundaries the (inset) tile boundary polylines, chamber by chamber
     * @param outerBoundaries the corresponding true tile boundary polylines
     * @return floor mesh, in POINT_NORMAL_TEXCOORD format
     */
    public static TriangleMesh createFloorRing(Geometry geom, java.util.List<Point3D[]> insetBoundaries,
                                               java.util.List<Point3D[]> outerBoundaries, double depth) {
        final TriangleMesh mesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        MeshUtils.setDefaultTexCoordinates(mesh);

        int pointCount = 0, normalCount = 0;
        for (int b = 0; b < insetBoundaries.size() && b < outerBoundaries.size(); b++) {
            final Point3D[] inset = insetBoundaries.get(b), outer = outerBoundaries.get(b);
            if (inset == null || outer == null || inset.length < 2 || inset.length != outer.length)
                continue;
            final int firstPoint = pointCount, firstNormal = normalCount;

            final Point3D[] innerPoints = new Point3D[inset.length], edgePoints = new Point3D[inset.length];
            for (int i = 0; i < inset.length; i++) {
                final Point3D inner = pushIntoSurface(geom, inset[i], depth);
                final Point3D edge = pushIntoSurface(geom, outer[i], depth);
                innerPoints[i] = inner;
                edgePoints[i] = edge;
                mesh.getPoints().addAll((float) inner.getX(), (float) inner.getY(), (float) inner.getZ());
                mesh.getPoints().addAll((float) edge.getX(), (float) edge.getY(), (float) edge.getZ());
                pointCount += 2;
                final Point3D normal = outOfSurface(geom, inner);
                mesh.getNormals().addAll((float) normal.getX(), (float) normal.getY(), (float) normal.getZ());
                normalCount++;
            }
            for (int i = 0; i + 1 < inset.length; i++) {
                final int inner0 = firstPoint + 2 * i, edge0 = inner0 + 1;
                final int inner1 = firstPoint + 2 * (i + 1), edge1 = inner1 + 1;
                final int normal0 = firstNormal + i, normal1 = firstNormal + i + 1;

                // wind the triangles so that their front side faces out of the surface: the floor can then be
                // back-face culled like everything else, so that the floors of tiles on the far side of a
                // sphere are not seen from below through the semi-transparent tiles in front of them
                final Point3D up = outOfSurface(geom, innerPoints[i]);
                final boolean flip = edgePoints[i].subtract(innerPoints[i])
                        .crossProduct(edgePoints[i + 1].subtract(innerPoints[i])).dotProduct(up) < 0;
                if (!flip) {
                    mesh.getFaces().addAll(inner0, normal0, 0, edge0, normal0, 1, edge1, normal1, 2);
                    mesh.getFaces().addAll(inner0, normal0, 0, edge1, normal1, 1, inner1, normal1, 2);
                } else {
                    mesh.getFaces().addAll(inner0, normal0, 0, edge1, normal1, 1, edge0, normal0, 2);
                    mesh.getFaces().addAll(inner0, normal0, 0, inner1, normal1, 1, edge1, normal1, 2);
                }
            }
        }
        return mesh;
    }

    /**
     * creates the lip for one tile: a band of quads that runs along the tile boundary from the tile surface
     * down into the surface. The lip is given explicit outward normals, so that it catches the light
     * differently than the top of the tile and thus delineates the tile even when seen head-on.
     *
     * @param boundaries the boundary polylines of all chambers of the tile
     * @param center     the tile center, used to orient the lip outwards
     * @return lip mesh, in POINT_NORMAL_TEXCOORD format
     */
    public static TriangleMesh createLip(Geometry geom, java.util.List<Point3D[]> boundaries, Point3D center, double depth) {
        final TriangleMesh mesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        MeshUtils.setDefaultTexCoordinates(mesh);

        int pointCount = 0;   // two points (top and bottom) per boundary point
        int normalCount = 0;  // one normal per boundary point, shared by its top and bottom point

        for (int b = 0; b < boundaries.size(); b++) {
            final Point3D[] boundary = boundaries.get(b);
            if (boundary == null || boundary.length < 2)
                continue;
            final int firstPoint = pointCount;
            final int firstNormal = normalCount;

            for (Point3D top : boundary) {
                final Point3D bottom = pushIntoSurface(geom, top, depth);
                mesh.getPoints().addAll((float) top.getX(), (float) top.getY(), (float) top.getZ());
                mesh.getPoints().addAll((float) bottom.getX(), (float) bottom.getY(), (float) bottom.getZ());
                pointCount += 2;

                // outward normal: the direction away from the tile center, made tangential to the surface
                final Point3D outward = top.subtract(center);
                final Point3D up = top.subtract(bottom).normalize();
                Point3D normal = outward.subtract(up.multiply(outward.dotProduct(up)));
                normal = (normal.magnitude() < 0.000001 ? up : normal.normalize());
                mesh.getNormals().addAll((float) normal.getX(), (float) normal.getY(), (float) normal.getZ());
                normalCount++;
            }

            for (int i = 0; i + 1 < boundary.length; i++) {
                final int top0 = firstPoint + 2 * i, bottom0 = top0 + 1;
                final int top1 = firstPoint + 2 * (i + 1), bottom1 = top1 + 1;
                final int normal0 = firstNormal + i, normal1 = firstNormal + i + 1;

                // wind the two triangles so that their geometric front side is the one facing away from the
                // tile center; the lip can then be back-face culled like the rest of the tile
                final Point3D edgeFrom = boundary[i], edgeTo = boundary[i + 1];
                final Point3D outward = edgeFrom.subtract(center);
                final boolean flip = edgeTo.subtract(edgeFrom).crossProduct(pushIntoSurface(geom, edgeFrom, 1).subtract(edgeFrom)).dotProduct(outward) < 0;

                if (flip == FLIP_LIP_WINDING) {
                    mesh.getFaces().addAll(top0, normal0, 0, top1, normal1, 1, bottom1, normal1, 2);
                    mesh.getFaces().addAll(top0, normal0, 0, bottom1, normal1, 1, bottom0, normal0, 2);
                } else {
                    mesh.getFaces().addAll(top0, normal0, 0, bottom1, normal1, 1, top1, normal1, 2);
                    mesh.getFaces().addAll(top0, normal0, 0, bottom0, normal0, 1, bottom1, normal1, 2);
                }
            }
        }
        // no face smoothing groups: the explicit normals determine the shading
        return mesh;
    }
}
