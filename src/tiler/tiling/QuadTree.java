package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

/**
 * Created by Ruediger on 2016.07.07.
 */
public class QuadTree {
    private Node root; //Root node of the tree.


    private class Node {
        Point2D a;
        Node NE, SE, SW, NW; // Four nodes for each direction in plane

        Node (Point2D a){
            this.a = a;
        }
    }

    public boolean insert (double b1, double b2, double tol){   //Returns true if point b is added to the tree structure.
        Point2D b = new Point2D(b1,b2);
        if (root == null) {
            root = new Node(b);
            return true;
        }
        Node h = root;
        double posSlope, negSlope;
        while (h != null){
            if (b.distance(h.a) > tol) {
                posSlope = Math.E*(b1-h.a.getX())+h.a.getY();
                negSlope = -1/Math.E*(b1-h.a.getX())+h.a.getY();
                if (b2 >= posSlope && b2 >= negSlope) {
                    if (h.NE == null) {
                        h.NE = new Node(b);
                        h = null;
                    } else h = h.NE;
                } else if (b2 < posSlope && b2 >= negSlope) {
                    if (h.SE == null) {
                        h.SE = new Node(b);
                        h = null;
                    } else h = h.SE;
                } else if (b2 >= posSlope && b2 < negSlope) {
                    if (h.NW == null) {
                        h.NW = new Node(b);
                        h = null;
                    } else h = h.NW;
                } else if (b2 < posSlope && b2 < negSlope) {
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


