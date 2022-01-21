/*
 * QuadTree.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Quad tree
 * Ruediger Zeller, 2016
 */
public class QuadTree {
    private QNode root; //Root node of the tree.
    private int size = 0;

    /**
     * constructor
     */
    public QuadTree() {

    }

    /**
     * attempts to add (x,y) to quad tree, using the given tolerance
     *
     * @param x
     * @param y
     * @param tolerance
     * @return true, if inserted
     */
    public boolean insert(double x, double y, double tolerance) {
        final Point2D b = new Point2D(x, y);
        if (root == null) {
            root = new QNode(b);
        } else {
            QNode h = root;
            while (h != null) {
                if (b.distance(h.p) > tolerance) {
                    final double posSlope = Math.E * (x - h.p.getX()) + h.p.getY();
                    final double negSlope = -1 / Math.E * (x - h.p.getX()) + h.p.getY();
                    if (y >= posSlope && y >= negSlope) {
                        if (h.NE == null) {
                            h.NE = new QNode(b);
                            h = null;
                        } else h = h.NE;
                    } else if (y < posSlope && y >= negSlope) {
                        if (h.SE == null) {
                            h.SE = new QNode(b);
                            h = null;
                        } else h = h.SE;
                    } else if (y >= posSlope && y < negSlope) {
                        if (h.NW == null) {
                            h.NW = new QNode(b);
                            h = null;
                        } else h = h.NW;
                    } else if (y < posSlope && y < negSlope) {
                        if (h.SW == null) {
                            h.SW = new QNode(b);
                            h = null;
                        } else h = h.SW;
                    }
                } else {
                    return false;
                }
            }
        }
        size++;
        return true;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    public ArrayList<Point2D> values() {
        ArrayList<Point2D> result = new ArrayList<>(size());
        if (root != null) {
            final Stack<QNode> stack = new Stack<>();
            stack.push(root);
            while (stack.size() > 0) {
                QNode q = stack.pop();
                if (q.p != null)
                    result.add(q.p);
                if (q.NE != null)
                    stack.push(q.NE);
                if (q.SE != null)
                    stack.push(q.SE);
                if (q.NW != null)
                    stack.push(q.NW);
                if (q.SW != null)
                    stack.push(q.SW);
            }
        }
        return result;

    }

    private static class QNode {
        final Point2D p;
        QNode NE, SE, SW, NW; // Four nodes for each direction in plane

        QNode(Point2D p) {
            this.p = p;
        }
    }
}
