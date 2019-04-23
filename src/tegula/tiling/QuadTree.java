package tegula.tiling;

import javafx.geometry.Point2D;

/**
 * Created by Ruediger on 2016.07.07.
 */
public class QuadTree {
    private Node root; //Root node of the tree.
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
        Point2D b = new Point2D(x, y);
        if (root == null) {
            root = new Node(b);
            size++;
            return true;
        }
        Node h = root;
        double posSlope, negSlope;
        while (h != null) {
            if (b.distance(h.a) > tolerance) {
                posSlope = Math.E * (x - h.a.getX()) + h.a.getY();
                negSlope = -1 / Math.E * (x - h.a.getX()) + h.a.getY();
                if (y >= posSlope && y >= negSlope) {
                    if (h.NE == null) {
                        h.NE = new Node(b);
                        h = null;
                    } else h = h.NE;
                } else if (y < posSlope && y >= negSlope) {
                    if (h.SE == null) {
                        h.SE = new Node(b);
                        h = null;
                    } else h = h.SE;
                } else if (y >= posSlope && y < negSlope) {
                    if (h.NW == null) {
                        h.NW = new Node(b);
                        h = null;
                    } else h = h.NW;
                } else if (y < posSlope && y < negSlope) {
                    if (h.SW == null) {
                        h.SW = new Node(b);
                        h = null;
                    } else h = h.SW;
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
    }

    public int size() {
        return size;
    }

    private class Node {
        Point2D a;
        Node NE, SE, SW, NW; // Four nodes for each direction in plane

        Node(Point2D a) {
            this.a = a;
        }
    }
}


