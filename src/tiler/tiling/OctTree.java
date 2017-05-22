package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.main.Document;

/**
 * Computes an OctTree for 3d-points. Returns true if a given point is added to the tree.
 * Created by Ruediger on 2016.06.23.
 */
public class OctTree {
    private Node root; //Root node of the tree.
    private class Node {
        Point3D a;
        Node ppp, mpp, pmp, ppm, mmp, mpm, pmm, mmm; // Eight nodes for each direction in space

        Node (Point3D a){
            this.a = a;
        }
    }


    public boolean insert (FDomain geom, Point3D b, double tol){   //Returns true if point b is added to the tree structure.

        if (root == null) {
            root = new Node(b);
            return true;
        }
        Node h = root;
        while (h != null){
            if (Tools.distance(geom, b, h.a) > tol) {
                if (b.getX() >= h.a.getX() && b.getY() >= h.a.getY() && b.getZ() >= h.a.getZ()) {
                    if (h.ppp == null) {
                        h.ppp = new Node(b);
                        h = null;
                    } else h = h.ppp;
                } else if (b.getX() < h.a.getX() && b.getY() >= h.a.getY() && b.getZ() >= h.a.getZ()) {
                    if (h.mpp == null) {
                        h.mpp = new Node(b);
                        h = null;
                    } else h = h.mpp;
                } else if (b.getX() >= h.a.getX() && b.getY() < h.a.getY() && b.getZ() >= h.a.getZ()) {
                    if (h.pmp == null) {
                        h.pmp = new Node(b);
                        h = null;
                    } else h = h.pmp;
                } else if (b.getX() >= h.a.getX() && b.getY() >= h.a.getY() && b.getZ() < h.a.getZ()) {
                    if (h.ppm == null) {
                        h.ppm = new Node(b);
                        h = null;
                    } else h = h.ppm;
                } else if (b.getX() < h.a.getX() && b.getY() < h.a.getY() && b.getZ() >= h.a.getZ()) {
                    if (h.mmp == null) {
                        h.mmp = new Node(b);
                        h = null;
                    } else h = h.mmp;
                } else if (b.getX() < h.a.getX() && b.getY() >= h.a.getY() && b.getZ() < h.a.getZ()) {
                    if (h.mpm == null) {
                        h.mpm = new Node(b);
                        h = null;
                    } else h = h.mpm;
                } else if (b.getX() >= h.a.getX() && b.getY() < h.a.getY() && b.getZ() < h.a.getZ()) {
                    if (h.pmm == null) {
                        h.pmm = new Node(b);
                        h = null;
                    } else h = h.pmm;
                } else if (b.getX() < h.a.getX() && b.getY() < h.a.getY() && b.getZ() < h.a.getZ()) {
                    if (h.mmm == null) {
                        h.mmm = new Node(b);
                        h = null;
                    } else h = h.mmm;
                }
            }
            else {return false;}
        }
        return true;
    }
}