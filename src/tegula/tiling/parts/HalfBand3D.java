/*
 * HalfBand3D.java Copyright (C) 2022 Daniel H. Huson
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
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.Collections;

/**
 * represents a band
 * Daniel Huson, 5.2020
 */
public class HalfBand3D {

    /**
     * construct a band from a list of points
     */
    public static TriangleMesh createEuclidean(Point3D[] points0, double bandWidth, StrokeLineCap strokeLineCap, StrokeLineJoin strokeLineJoin) {
        final ArrayList<Point3D> points = new ArrayList<>();

        Collections.addAll(points, points0);
        //System.err.println(points0.length+" -> "+points.size());

        final ArrayList<Point3D> parallelPoints = new ArrayList<>();

        {
            Point3D prev = null;
            for (int i = 0; i < points.size(); i++) {
                final Point3D point = points.get(i);
                if (prev != null) {
                    final Point3D normal = prev.subtract(point).crossProduct(Rotate.Z_AXIS).normalize();
                    if (i == 1) { // take care of first point
                        parallelPoints.add(prev.add(normal.multiply(bandWidth)));
                    }
                    if (i == points.size() - 1) // take care of last point
                        parallelPoints.add(point.add(normal.multiply(bandWidth)));
                    else // intermediate point
                    {
                        parallelPoints.add(shiftAndIntersect(points.get(i - 1), point, points.get(i + 1), bandWidth));
                    }
                }
                prev = point;
            }
        }


        final TriangleMesh band = new TriangleMesh();
        MeshUtils.setDefaultTexCoordinates(band);

        for (Point3D point : points) {
            band.getPoints().addAll((float) point.getX(), (float) point.getY(), 1f);
        }
        for (Point3D point : parallelPoints) {
            band.getPoints().addAll((float) point.getX(), (float) point.getY(), 1f);
        }
        {
            // layout:
            //  p0-- p1-- p2--...-- pk
            //  | \  | \  |  \      |
            // pp0--pp1--pp2--...--ppk

            final int k = points.size();
            for (int i = 0; i < k - 1; i++) {
                band.getFaces().addAll(i, 0, i + k, 1, i + k + 1, 2);
                //System.err.println(i+" "+(i+k)+" "+(i+k+1));
                band.getFaces().addAll(i, 0, i + k + 1, 1, i + 1, 2);
                //System.err.println(i+" "+(i+k+1)+" "+(i+1));
            }
        }
        MeshUtils.setSmoothGroup(band, 1);
        return band;
    }

    /**
     * shift points p1 and p2 orthogonally to p1-p2 by distance d and copies of p2 and p3 similarly, returns the intersection
     * of the two shifted lines
     *
     * @return intersection point
     */
    public static Point3D shiftAndIntersect(Point3D p1, Point3D p2, Point3D q2, double d) {
        // line p1-p2
        final Point3D pTrans = new Point3D(-p2.subtract(p1).getY(), p2.subtract(p1).getX(), p1.getZ()).normalize().multiply(d);
        p1 = p1.add(pTrans);
        p2 = p2.add(pTrans);

        // line q1-q2
        Point3D q1 = p2;
        final Point3D qTrans = new Point3D(-q2.subtract(q1).getY(), q2.subtract(q1).getX(), q1.getZ()).normalize().multiply(d);
        q1 = q1.add(qTrans);
        q2 = q2.add(qTrans);

        final double x1 = p1.getX();
        final double x2 = p2.getX();
        final double x3 = q1.getX();
        final double x4 = q2.getX();

        final double y1 = p1.getY();
        final double y2 = p2.getY();
        final double y3 = q1.getY();
        final double y4 = q2.getY();

        if (Math.abs((x1 * (y2 - y4) + x2 * (y4 - y1) + x4 * (y1 - y2))) < 0.1) // points p1,p2,q2 are co-linear
            return p2;

        final double x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));
        final double y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

        return new Point3D(x, y, 0);
    }
}