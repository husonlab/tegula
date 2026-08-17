/*
 * Band3D.java Copyright (C) 2023 Daniel H. Huson
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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.TriangleMesh;
import jloda.util.Pair;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * represents a band
 * Cornelius Wiehl, 11.2018
 */
public class Band3D {
    /**
     * connects two points by a band of the given width, held at a constant height above the surface
     */
    public static TriangleMesh connect(Geometry geom, Point3D[] corners, double bandWidth, double nudgeAbove, StrokeLineCap strokeLineCap) {
        final double[] nudges = new double[corners.length];
        Arrays.fill(nudges, nudgeAbove);
        return connect(geom, corners, bandWidth, nudges, strokeLineCap);
    }

    /**
     * connects two points by a band of the given width, whose height above the surface is given separately for
     * each corner. A band that rises and falls along its length is what lets bands interlace: where one passes
     * over another, it simply stands higher and the depth buffer hides the one below, see {@link Weave}.
     *
     * @param nudgeAboveAt height above the surface at each corner, one entry per corner
     */
    public static TriangleMesh connect(Geometry geom, Point3D[] corners, double bandWidth, double[] nudgeAboveAt, StrokeLineCap strokeLineCap) {
        return connect(geom, corners, bandWidth, bandWidth, nudgeAboveAt, strokeLineCap);
    }

    /**
     * connects two points by a band as above, but rounds its ends off with a disc of a width of its own.
     * <p>
     * A band is outlined by drawing a wider band of the outline color just beneath it. The discs that round off
     * the ends are flat, though, so where a band slopes away from one, a wider disc jutting out beneath it
     * rises in front of the band and shows as a bar across it. Giving the outline the discs of the band it
     * outlines rather than its own keeps them tucked underneath whatever the slope.
     *
     * @param capWidth width of the disc that rounds off each end of the band
     */
    public static TriangleMesh connect(Geometry geom, Point3D[] corners, double bandWidth, double capWidth, double[] nudgeAboveAt, StrokeLineCap strokeLineCap) {
        final ArrayList<TriangleMesh> meshes = new ArrayList<>();

        Point3D previous4 = null;
        Point3D previous5 = null;

        for (int pos = 0; pos + 1 < corners.length; pos++) {
            final Point3D point0 = corners[pos];
            final Point3D point1 = corners[pos + 1];
            double nudgeAbove0 = nudgeAboveAt[pos];
            double nudgeAbove1 = nudgeAboveAt[pos + 1];

            final Point3D[] points3d = new Point3D[6];
            final int[] faces;

            // Points visualized
            // |2|----------|4|
            // |0|----------|1|
            // |3|----------|5|


            switch (geom) {
                case Euclidean: {
                    final Point3D zAxis = new Point3D(0, 0, 1);
                    final Point3D direction = point1.subtract(point0);
                    final Point3D normal = direction.crossProduct(zAxis).normalize();
                    points3d[0] = point0;
                    points3d[1] = point1;
                    points3d[2] = point0.add(normal.multiply(bandWidth));
                    points3d[3] = point0.add(normal.multiply(-bandWidth));
                    points3d[4] = point1.add(normal.multiply(bandWidth));
                    points3d[5] = point1.add(normal.multiply(-bandWidth));
                    // points 0, 2 and 3 belong to point0 and so take its height, points 1, 4 and 5 that of point1
                    points3d[0] = points3d[0].add(0, 0, nudgeAbove0);
                    points3d[2] = points3d[2].add(0, 0, nudgeAbove0);
                    points3d[3] = points3d[3].add(0, 0, nudgeAbove0);
                    points3d[1] = points3d[1].add(0, 0, nudgeAbove1);
                    points3d[4] = points3d[4].add(0, 0, nudgeAbove1);
                    points3d[5] = points3d[5].add(0, 0, nudgeAbove1);
                    faces = new int[]{2, 0, 1, 1, 4, 2, 2, 0, 0, 1, 1, 2, 0, 0, 5, 1, 1, 2, 0, 0, 3, 1, 5, 2};

                    if (strokeLineCap == StrokeLineCap.SQUARE) {
                        points3d[2] = points3d[2].add(direction.multiply(-bandWidth));
                        points3d[0] = points3d[0].add(direction.multiply(-bandWidth));
                        points3d[3] = points3d[3].add(direction.multiply(-bandWidth));

                        points3d[4] = points3d[4].add(direction.multiply(bandWidth));
                        points3d[1] = points3d[1].add(direction.multiply(bandWidth));
                        points3d[5] = points3d[5].add(direction.multiply(bandWidth));
                    } else if (strokeLineCap == StrokeLineCap.ROUND) {
                        if (isGapPossible(previous4, points3d[2], previous5, points3d[3])) {
                            final Point3D tangent = direction.multiply(bandWidth / direction.magnitude());
                            final Point3D[] coordinates = BandCap3D.circle(point0, tangent, capWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point0, coordinates, geom, nudgeAbove0, false));
                        }
                        if (pos + 1 == corners.length - 1) {
                            final Point3D tangent = direction.multiply(bandWidth / direction.magnitude());
                            final Point3D[] coordinates = BandCap3D.circle(point1, tangent, capWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point1, coordinates, geom, nudgeAbove1, false));
                        }
                    }
                    break;
                }
// uses two different normal vectors for two different points
                case Spherical: {
                    final Point3D diff = point1.subtract(point0).normalize();
                    final Point3D normalToSphere0 = Tools.getNormalVector(point0, geom);
                    final Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
                    final Point3D normal = diff.crossProduct(normalToSphere0.normalize());
                    points3d[0] = point0.add(normalToSphere0.normalize().multiply(nudgeAbove0));
                    points3d[1] = point1.add(normalToSphere1.normalize().multiply(nudgeAbove1));
                    points3d[2] = point0.add(normal.multiply(bandWidth)).add(normalToSphere0.normalize().multiply(nudgeAbove0));
                    points3d[3] = point0.subtract(normal.multiply(bandWidth)).add(normalToSphere0.normalize().multiply(nudgeAbove0));
                    points3d[4] = point1.add(normal.multiply(bandWidth)).add(normalToSphere1.normalize().multiply(nudgeAbove1));
                    points3d[5] = point1.subtract(normal.multiply(bandWidth)).add(normalToSphere1.normalize().multiply(nudgeAbove1));
                    faces = new int[]{2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2};
                    if (strokeLineCap == StrokeLineCap.ROUND) {
                        if (isGapPossible(previous4, points3d[2], previous5, points3d[3])) {
                            final Point3D tangent = point0.crossProduct(new Point3D(1, 0, 0));
                            final Point3D[] coordinates = BandCap3D.circle(point0, tangent, capWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point0, coordinates, geom, nudgeAbove0, false));
                        }
                        if (pos + 1 == corners.length - 1) {
                            final Point3D tangent = point1.crossProduct(new Point3D(1, 0, 0));
                            final Point3D[] coordinates = BandCap3D.circle(point1, tangent, capWidth, 24, geom);
                            meshes.add(BandCap3D.CircleMesh(point1, coordinates, geom, nudgeAbove1, false));
                        }
                    }
                    break;
                }
                case Hyperbolic:
                    // hyperbolic bands are lifted off the hyperboloid by a fixed amount; any requested height
                    // (as used by the weave) is added on top of that
                    nudgeAbove0 += 0.1;
                    nudgeAbove1 += 0.1;
                    final Point3D diff = point0.subtract(point1);
                    final Point3D refPoint0 = point0.add(diff); // direction of line used in function below to orientate points
                    final Point3D refPoint1 = point1.add(diff);
                    final Pair<Point3D, Point3D> pointsForStart = Tools.equidistantHyperbolicPoints(point0, refPoint0, bandWidth);
                    final Pair<Point3D, Point3D> pointsForEnd = Tools.equidistantHyperbolicPoints(point1, refPoint1, bandWidth);
                {
                    points3d[0] = point0.add(Tools.getNormalVector(point0, geom).multiply(nudgeAbove0));
                    points3d[1] = point1.add(Tools.getNormalVector(point1, geom).multiply(nudgeAbove1));
                    points3d[2] = pointsForStart.getFirst().add(Tools.getHyperbolicNormal(pointsForStart.getFirst()).multiply(nudgeAbove0));
                    points3d[3] = pointsForStart.getSecond().add(Tools.getHyperbolicNormal(pointsForStart.getSecond()).multiply(nudgeAbove0));
                    points3d[4] = pointsForEnd.getFirst().add(Tools.getHyperbolicNormal(pointsForEnd.getFirst()).multiply(nudgeAbove1));
                    points3d[5] = pointsForEnd.getSecond().add(Tools.getHyperbolicNormal(pointsForEnd.getSecond()).multiply(nudgeAbove1));
                }
                faces = new int[]{2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2};
                if (strokeLineCap == StrokeLineCap.ROUND) {
                    if (isGapPossible(previous4, points3d[2], previous5, points3d[3])) {
                        final Point3D tangent = point1.subtract(point0);
                        final Point3D[] coordinates = BandCap3D.circle(point0, tangent, capWidth, 24, geom);
                        meshes.add(BandCap3D.CircleMesh(point0, coordinates, geom, nudgeAbove0, false));
                    }
                    if (pos + 1 == corners.length - 1) {
                        final Point3D tangent = point0.add(point1);
                        final Point3D[] coordinates = BandCap3D.circle(point1, tangent, capWidth, 24, geom);
                        meshes.add(BandCap3D.CircleMesh(point1, coordinates, geom, nudgeAbove1, false));
                    }
                }
                break;
                default:
                    throw new RuntimeException("Invalid case");
            }

            // creates mesh
            float[] points = new float[3 * points3d.length];

            for (int i = 0; i < points3d.length; i++) {
                points[3 * i] = (float) points3d[i].getX();
                points[3 * i + 1] = (float) points3d[i].getY();
                points[3 * i + 2] = (float) points3d[i].getZ();
            }

            int[] smoothing = new int[faces.length / 6];
            Arrays.fill(smoothing, 1);

            final TriangleMesh mesh = new TriangleMesh();
            mesh.getPoints().addAll(points);
            mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);
            mesh.getFaces().addAll(faces);
            mesh.getFaceSmoothingGroups().addAll(smoothing);
            meshes.add(mesh);

            previous4 = points3d[4];
            previous5 = points3d[5];
        }
        return MeshUtils.combineTriangleMeshes(meshes);
    }

    /**
     * builds the outline of the band that {@link #connect} makes from the same corners and width: a thin strip
     * running along either side of it, at the very same heights.
     * <p>
     * The outline cannot be a wider band tucked just beneath the band, because on a sphere that fails: towards
     * the rim the offset between the two runs across the line of sight rather than along it, so it separates
     * them not at all, and the wider strip beneath simply covers the band. Nor can the two be made coplanar and
     * left to the drawing order, which only makes them fight for the same depth. Setting the outline beside the
     * band instead of beneath it means the two never contend for a pixel, at any angle and in any geometry.
     *
     * @param borderWidth width of the strip along one side of the band
     */
    public static TriangleMesh outline(Geometry geom, Point3D[] corners, double bandWidth, double borderWidth,
                                       double[] nudgeAboveAt) {
        final int n = corners.length;
        if (n < 2)
            return new TriangleMesh();

        // The corners are worked out per point, from the tangent there rather than from one segment, so that
        // consecutive quads of the strip share their corners exactly. Taking them per segment instead leaves a
        // sliver at every joint wherever the band turns, and the strip then reads as a dashed line.
        final Point3D[][] inner = new Point3D[n][], outer = new Point3D[n][];
        for (int i = 0; i < n; i++) {
            final double nudge = nudgeAt(nudgeAboveAt, geom, i);
            final Point3D tangent = corners[Math.min(n - 1, i + 1)].subtract(corners[Math.max(0, i - 1)]);
            inner[i] = sideCorners(geom, corners[i], tangent, bandWidth, nudge);
            outer[i] = sideCorners(geom, corners[i], tangent, bandWidth + borderWidth, nudge);
        }

        final ArrayList<TriangleMesh> meshes = new ArrayList<>();

        // A ring at either end, to round the join off. Two strips that meet at an angle leave a wedge open on
        // the outside of the bend, and the strap then reads as a dashed line: the band rounds its own bend off
        // with a disc, and this is that disc's rim. The bend is real at a vertex, where the strap turns, and
        // comes from the curvature at the center of an edge, where the tangent of one strip is taken from its
        // last segment and the next one's from its first. It is why the breaks show on the sphere and on the
        // hyperboloid, whose boundary polylines curve, and not in the plane, whose refined polylines are straight.
        addJoin(geom, corners[0], corners[1].subtract(corners[0]), bandWidth, borderWidth, nudgeAt(nudgeAboveAt, geom, 0), meshes);
        addJoin(geom, corners[n - 1], corners[n - 1].subtract(corners[n - 2]), bandWidth, borderWidth,
                nudgeAt(nudgeAboveAt, geom, n - 1), meshes);

        for (int i = 0; i + 1 < n; i++) {
            if (inner[i] == null || outer[i] == null || inner[i + 1] == null || outer[i + 1] == null)
                continue;
            // 0..3 one side, 4..7 the other: inner and outer here, then inner and outer at the next point
            final Point3D[] all = {inner[i][0], outer[i][0], inner[i + 1][0], outer[i + 1][0],
                    inner[i][1], outer[i][1], inner[i + 1][1], outer[i + 1][1]};
            final float[] points = new float[8 * 3];
            for (int j = 0; j < 8; j++) {
                points[3 * j] = (float) all[j].getX();
                points[3 * j + 1] = (float) all[j].getY();
                points[3 * j + 2] = (float) all[j].getZ();
            }
            // wound to face the same way as the band itself, whose winding differs between the geometries
            final int[] faces = (geom == Geometry.Euclidean
                    ? new int[]{1, 0, 2, 0, 3, 0, 1, 0, 0, 0, 2, 0, 4, 0, 7, 0, 6, 0, 4, 0, 5, 0, 7, 0}
                    : new int[]{1, 0, 3, 0, 2, 0, 1, 0, 2, 0, 0, 0, 4, 0, 6, 0, 7, 0, 4, 0, 7, 0, 5, 0});

            final TriangleMesh mesh = new TriangleMesh();
            mesh.getPoints().addAll(points);
            mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);
            mesh.getFaces().addAll(faces);
            final int[] smoothing = new int[faces.length / 6];
            Arrays.fill(smoothing, 1);
            mesh.getFaceSmoothingGroups().addAll(smoothing);
            meshes.add(mesh);
        }
        return MeshUtils.combineTriangleMeshes(meshes);
    }

    /** height of the band at one of its corners, including the fixed lift that hyperbolic bands ride at */
    private static double nudgeAt(double[] nudgeAboveAt, Geometry geom, int i) {
        return nudgeAboveAt[i] + (geom == Geometry.Hyperbolic ? 0.1 : 0);
    }

    /**
     * how far to either side of the outline a join reaches, as a fraction of a quarter turn. A wedge only ever
     * opens to the side of a strap, never along it, and a join that ran the whole way round would lay a dark
     * arc across the band that carries on through the joint.
     */
    private static final double JOIN_REACH = 0.3;

    /**
     * adds the arcs that round one end of an outline off, running from the edge of the band to the edge of the
     * outline to either side of it, so that no wedge is left open whatever angle the next strip leaves at
     */
    private static void addJoin(Geometry geom, Point3D center, Point3D tangent, double bandWidth, double borderWidth,
                                double nudgeAbove, ArrayList<TriangleMesh> meshes) {
        final int steps = 24;
        if (!(tangent.magnitude() > 0))
            return;
        final Point3D up = (geom == Geometry.Euclidean ? new Point3D(0, 0, 1)
                : Tools.getNormalVector(center, geom).normalize());
        final Point3D[] inner = BandCap3D.circle(center, tangent, bandWidth, steps, geom);
        final Point3D[] outer = BandCap3D.circle(center, tangent, bandWidth + borderWidth, steps, geom);
        if (inner == null || outer == null || inner.length != steps || outer.length != steps)
            return;
        final Point3D lift = (geom == Geometry.Euclidean ? new Point3D(0, 0, nudgeAbove) : up.multiply(nudgeAbove));

        final float[] points = new float[2 * steps * 3];
        for (int i = 0; i < steps; i++) {
            final Point3D in = inner[i].add(lift), out = outer[i].add(lift);
            points[6 * i] = (float) in.getX();
            points[6 * i + 1] = (float) in.getY();
            points[6 * i + 2] = (float) in.getZ();
            points[6 * i + 3] = (float) out.getX();
            points[6 * i + 4] = (float) out.getY();
            points[6 * i + 5] = (float) out.getZ();
        }
        // only the quads that lie to one side or the other of the band, see JOIN_REACH
        final java.util.List<Integer> wanted = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            final double turns = (i + 0.5) / steps; // 0 is along the band, 1/4 and 3/4 are to either side
            if (Math.abs(turns - 0.25) <= 0.25 * JOIN_REACH || Math.abs(turns - 0.75) <= 0.25 * JOIN_REACH)
                wanted.add(i);
        }
        final int[] faces = new int[wanted.size() * 12];
        for (int q = 0; q < wanted.size(); q++) {
            final int i = wanted.get(q);
            final int in0 = 2 * i, out0 = in0 + 1, in1 = 2 * ((i + 1) % steps), out1 = in1 + 1;
            // wind so that the front side faces out of the surface, as the rest of the band does
            final boolean flip = outer[i].subtract(inner[i])
                    .crossProduct(outer[(i + 1) % steps].subtract(inner[i])).dotProduct(up) < 0;
            final int at = 12 * q;
            if (!flip) {
                faces[at] = in0; faces[at + 2] = out0; faces[at + 4] = out1;
                faces[at + 6] = in0; faces[at + 8] = out1; faces[at + 10] = in1;
            } else {
                faces[at] = in0; faces[at + 2] = out1; faces[at + 4] = out0;
                faces[at + 6] = in0; faces[at + 8] = in1; faces[at + 10] = out1;
            }
        }
        final TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);
        mesh.getFaces().addAll(faces);
        final int[] smoothing = new int[faces.length / 6];
        Arrays.fill(smoothing, 1);
        mesh.getFaceSmoothingGroups().addAll(smoothing);
        meshes.add(mesh);
    }

    /**
     * the two points at the given distance either side of a point of the band, in the order used by
     * {@link #connect}
     *
     * @param tangent direction of the band at that point
     */
    private static Point3D[] sideCorners(Geometry geom, Point3D point, Point3D tangent, double halfWidth, double nudgeAbove) {
        if (!(tangent.magnitude() > 0))
            return null;
        switch (geom) {
            case Euclidean -> {
                final Point3D normal = tangent.crossProduct(new Point3D(0, 0, 1)).normalize();
                return new Point3D[]{point.add(normal.multiply(halfWidth)).add(0, 0, nudgeAbove),
                        point.add(normal.multiply(-halfWidth)).add(0, 0, nudgeAbove)};
            }
            case Spherical -> {
                final Point3D normalToSphere = Tools.getNormalVector(point, geom).normalize();
                final Point3D normal = tangent.normalize().crossProduct(normalToSphere);
                return new Point3D[]{point.add(normal.multiply(halfWidth)).add(normalToSphere.multiply(nudgeAbove)),
                        point.subtract(normal.multiply(halfWidth)).add(normalToSphere.multiply(nudgeAbove))};
            }
            default -> {
                // connect() takes its reference point backwards along the band, so this does too
                final Pair<Point3D, Point3D> pair = Tools.equidistantHyperbolicPoints(point, point.subtract(tangent), halfWidth);
                return new Point3D[]{pair.getFirst().add(Tools.getHyperbolicNormal(pair.getFirst()).multiply(nudgeAbove)),
                        pair.getSecond().add(Tools.getHyperbolicNormal(pair.getSecond()).multiply(nudgeAbove))};
            }
        }
    }

    public static boolean isGapPossible(Point3D a1, Point3D a2, Point3D b1, Point3D b2) {
        return a1 == null || a2 == null || b1 == null || b2 == null || a1.distance(a2) > 0.0001 || b1.distance(b2) > 0.0001;
    }
}
