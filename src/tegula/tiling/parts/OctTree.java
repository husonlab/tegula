/*
 * OctTree.java Copyright (C) 2022 Daniel H. Huson
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
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;

/**
 * An OctTree for 3D points
 * Ruediger Zeller, 2016
 */
public class OctTree {
    private Node root; //Root node of the tree.
    private int size;

    private static class Node {
        final Point3D a;
        Node ppp, mpp, pmp, ppm, mmp, mpm, pmm, mmm; // Eight nodes for each direction in space

        Node(Point3D a) {
            this.a = a;
        }
    }

    /**
     * number of inserted points
     *
     * @return size
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if point b is added to the tree structure.
     *
     * @param geom
     * @param point
     * @param tolerance
     * @return true, if inserted
     */
    public boolean insert(Geometry geom, Point3D point, double tolerance) {
        double tol = tolerance * 0.01;
        if (root == null) {
            root = new Node(point);
            size++;
            return true;
        }
        Node h = root;
        while (h != null) {
            if (Tools.distance(geom, point, h.a) > tolerance) {
                if (point.getX() >= h.a.getX() - tol && point.getY() >= h.a.getY() - tol && point.getZ() >= h.a.getZ() - tol) {
                    if (h.ppp == null) {
                        h.ppp = new Node(point);
                        h = null;
                    } else h = h.ppp;
                } else if (point.getX() < h.a.getX() + tol && point.getY() >= h.a.getY() - tol && point.getZ() >= h.a.getZ() - tol) {
                    if (h.mpp == null) {
                        h.mpp = new Node(point);
                        h = null;
                    } else h = h.mpp;
                } else if (point.getX() >= h.a.getX() - tol && point.getY() < h.a.getY() + tol && point.getZ() >= h.a.getZ() - tol) {
                    if (h.pmp == null) {
                        h.pmp = new Node(point);
                        h = null;
                    } else h = h.pmp;
                } else if (point.getX() >= h.a.getX() - tol && point.getY() >= h.a.getY() - tol && point.getZ() < h.a.getZ() + tol) {
                    if (h.ppm == null) {
                        h.ppm = new Node(point);
                        h = null;
                    } else h = h.ppm;
                } else if (point.getX() < h.a.getX() + tol && point.getY() < h.a.getY() + tol && point.getZ() >= h.a.getZ() - tol) {
                    if (h.mmp == null) {
                        h.mmp = new Node(point);
                        h = null;
                    } else h = h.mmp;
                } else if (point.getX() < h.a.getX() + tol && point.getY() >= h.a.getY() - tol && point.getZ() < h.a.getZ() + tol) {
                    if (h.mpm == null) {
                        h.mpm = new Node(point);
                        h = null;
                    } else h = h.mpm;
                } else if (point.getX() >= h.a.getX() - tol && point.getY() < h.a.getY() + tol && point.getZ() < h.a.getZ() + tol) {
                    if (h.pmm == null) {
                        h.pmm = new Node(point);
                        h = null;
                    } else h = h.pmm;
                } else if (point.getX() < h.a.getX() + tol && point.getY() < h.a.getY() + tol && point.getZ() < h.a.getZ() + tol) {
                    if (h.mmm == null) {
                        h.mmm = new Node(point);
                        h = null;
                    } else h = h.mmm;
                }
            } else {
                return false;
            }
        }
        size++;
        return true;
    }

    public void clear() {
        root = null;
        size = 0;
    }
}
