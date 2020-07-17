/*
 * Lines.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.tiling.parts;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.Geometry;

import java.util.Arrays;
import java.util.List;

/**
 * 2D and 3D lines
 * <p>
 * Ruediger Zeller, 2017
 */
public class Lines {
    /**
     * a 3D line connecting two points
     * Slightly modified code from Rahel Lüthy (http://netzwerg.ch/blog/2015/03/22/javafx-3d-line/).
     *
     * @param origin
     * @param target
     * @param color
     * @param width
     * @return line
     */
    public static Node createLine3D(Point3D origin, Point3D target, Color color, double width) {
        final Point3D yAxis = new Point3D(0, 1, 0);
        final Point3D diff = target.subtract(origin);
        final double height = diff.magnitude();

        final Point3D mid = target.midpoint(origin);
        final Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        final Point3D axisOfRotation = diff.crossProduct(yAxis);
        final double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        final Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        final Cylinder line = new Cylinder(width, height, 3);
        line.setCullFace(CullFace.BACK);
        line.setDrawMode(DrawMode.FILL);
        line.setMaterial(new PhongMaterial(color));

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

    /**
     * a 3D poly line connecting points
     *
     * @param points
     * @param color
     * @param width
     * @return line
     */
    public static Node createPolyLine3D(List<Point3D> points, Color color, double width) {
        final Group group = new Group();
        for (int i = 0; i < points.size() - 1; i++) {
            group.getChildren().add(createLine3D(points.get(i), points.get(i + 1), color, width));
        }
        return group;
    }

    /**
     * make a 2D or3D line from a to b to c
     *
     * @param geometry
     * @param a
     * @param b
     * @param c
     * @param color
     * @param width
     * @return line
     */
    public static Node createLine(Geometry geometry, Point3D a, Point3D b, Point3D c, Color color, float width) {
        if (geometry == Geometry.Euclidean && false) {
            final Polyline polyLine = new Polyline(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY());
            polyLine.setStroke(color);
            polyLine.setStrokeWidth(width);
            polyLine.setStrokeLineCap(StrokeLineCap.ROUND);
            return polyLine;

        } else
            return createPolyLine3D(Arrays.asList(a, b, c), color, width);
    }
}
