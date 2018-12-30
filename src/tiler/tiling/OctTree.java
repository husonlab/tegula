package tiler.tiling;

import javafx.geometry.Point3D;
import tiler.core.dsymbols.Geometry;

/**
 * An OctTree for 3D points
 * Created by Ruediger on 2016.06.23.
 */
public class OctTree {
    private Node root; //Root node of the tree.

    private class Node {
        Point3D a;
        Node ppp, mpp, pmp, ppm, mmp, mpm, pmm, mmm; // Eight nodes for each direction in space

        Node(Point3D a) {
            this.a = a;
        }
    }


    /**
     * Returns true if point b is added to the tree structure.
     *
     * @param geom
     * @param point
     * @param tolerance
     * @return
     */
    public boolean insert(Geometry geom, Point3D point, double tolerance) {

        if (root == null) {
            root = new Node(point);
            return true;
        }
        Node h = root;
        while (h != null) {
            if (Tools.distance(geom, point, h.a) > tolerance) {
                if (point.getX() >= h.a.getX() && point.getY() >= h.a.getY() && point.getZ() >= h.a.getZ()) {
                    if (h.ppp == null) {
                        h.ppp = new Node(point);
                        h = null;
                    } else h = h.ppp;
                } else if (point.getX() < h.a.getX() && point.getY() >= h.a.getY() && point.getZ() >= h.a.getZ()) {
                    if (h.mpp == null) {
                        h.mpp = new Node(point);
                        h = null;
                    } else h = h.mpp;
                } else if (point.getX() >= h.a.getX() && point.getY() < h.a.getY() && point.getZ() >= h.a.getZ()) {
                    if (h.pmp == null) {
                        h.pmp = new Node(point);
                        h = null;
                    } else h = h.pmp;
                } else if (point.getX() >= h.a.getX() && point.getY() >= h.a.getY() && point.getZ() < h.a.getZ()) {
                    if (h.ppm == null) {
                        h.ppm = new Node(point);
                        h = null;
                    } else h = h.ppm;
                } else if (point.getX() < h.a.getX() && point.getY() < h.a.getY() && point.getZ() >= h.a.getZ()) {
                    if (h.mmp == null) {
                        h.mmp = new Node(point);
                        h = null;
                    } else h = h.mmp;
                } else if (point.getX() < h.a.getX() && point.getY() >= h.a.getY() && point.getZ() < h.a.getZ()) {
                    if (h.mpm == null) {
                        h.mpm = new Node(point);
                        h = null;
                    } else h = h.mpm;
                } else if (point.getX() >= h.a.getX() && point.getY() < h.a.getY() && point.getZ() < h.a.getZ()) {
                    if (h.pmm == null) {
                        h.pmm = new Node(point);
                        h = null;
                    } else h = h.pmm;
                } else if (point.getX() < h.a.getX() && point.getY() < h.a.getY() && point.getZ() < h.a.getZ()) {
                    if (h.mmm == null) {
                        h.mmm = new Node(point);
                        h = null;
                    } else h = h.mmm;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}