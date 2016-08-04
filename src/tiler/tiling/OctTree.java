package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import tiler.core.dsymbols.FDomain;

/**
 * Computes an OctTree for 3d-points. Returns true if a given point is added to the tree.
 * Created by Ruediger on 2016.06.23.
 */
public class OctTree {
    private double eps = 0.01;
    //public static Point3D rootPoint = new Point3D(0,0,1); //Todo: better choice
    private Node root; //Root node of the tree.


    private class Node {
        Point3D a;
        Node ppp, mpp, pmp, ppm, mmp, mpm, pmm, mmm; // Eight nodes for each direction in space

        Node (Point3D a){
            this.a = a;
        }
    }


    private static double distance (FDomain geom, Point3D a, Point3D b){
        if (geom.getGeometry() == FDomain.Geometry.Spherical) {
            double dist = a.distance(b); // Euclidean distance
            return dist;
        }
        else if (geom.getGeometry() == FDomain.Geometry.Euclidean){
            Point2D a2d = new Point2D(a.getX(),a.getY());
            Point2D b2d = new Point2D(b.getX(),b.getY());
            double dist = a2d.distance(b2d);
            return  dist;
        }
        else {
            double scalar = a.getZ()*b.getZ() - a.getX()*b.getX() - a.getY()*b.getY();
            double dist = Math.log(Math.abs(scalar + Math.sqrt(Math.abs(scalar * scalar - 1)))); // Hyperbolic distance on hyperbploid (not scaled with factor 100): dist(x,y) = arcosh(-<x,y>), where <x,y> = x1y1 + x2y2 - x3y3.
            return dist;
        }
    }


    public boolean insert (FDomain geom, Point3D b){   //Returns true if point b is added to the tree structure.

        if (root == null) {
            root = new Node(b);
            return true;
        }
        Node h = root;
        while (h != null){
            if (distance(geom, b, h.a) > eps) {
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