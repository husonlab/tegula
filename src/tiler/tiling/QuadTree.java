package tiler.tiling;

import javafx.geometry.Point2D;
/**
 * Created by Ruediger on 2016.07.07.
 */
public class QuadTree {
    private double eps = 1;
    //public static Point3D rootPoint = new Point3D(0,0,1); //Todo: better choice
    private Node root; //Root node of the tree.


    private class Node {
        Point2D a;
        Node NE, SE, SW, NW; // Four nodes for each direction in plane

        Node (Point2D a){
            this.a = a;
        }
    }

    public boolean insert (double b1, double b2){   //Returns true if point b is added to the tree structure.
        Point2D b = new Point2D(b1,b2);
        if (root == null) {
            root = new Node(b);
            return true;
        }
        Node h = root;
        while (h != null){
            if (b.distance(h.a) > eps) {
                if (b.getX() >= h.a.getX() && b.getY() >= h.a.getY()) {
                    if (h.NE == null) {
                        h.NE = new Node(b);
                        h = null;
                    } else h = h.NE;
                } else if (b.getX() < h.a.getX() && b.getY() >= h.a.getY()) {
                    if (h.SE == null) {
                        h.SE = new Node(b);
                        h = null;
                    } else h = h.SE;
                } else if (b.getX() >= h.a.getX() && b.getY() < h.a.getY()) {
                    if (h.NW == null) {
                        h.NW = new Node(b);
                        h = null;
                    } else h = h.NW;
                } else if (b.getX() < h.a.getX() && b.getY() < h.a.getY()) {
                    if (h.SW == null) {
                        h.SW = new Node(b);
                        h = null;
                    } else h = h.SW;
                }
            }
            else {return false;}
        }
        return true;
    }
}


