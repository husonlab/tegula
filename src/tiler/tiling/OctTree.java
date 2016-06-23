package tiler.tiling;

import javafx.geometry.Point3D;

/**
 * Created by Ruediger on 2016.06.23.
 */
public class OctTree {
    private double eps = 0;
    public Node root = new Node(10, 11, 5);


    public class Node {
        double x, y, z;
        Node ppp, mpp, pmp, ppm, mmp, mpm, pmm, mmm; // Eight nodes

        Node (double x, double y, double z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public boolean insert (double x, double y, double z){
        Node h = root;
        Point3D point = new Point3D(x,y,z);
        //Node root = new Node(0.01, 0.34, 0.55);

        while (h != null){
            if (point.distance(h.x,h.y,h.z) > eps && x >= h.x && y >= h.y && z >= h.z){
                if (h.ppp == null){
                    h.ppp = new Node(x,y,z);
                    h = null;
                }
                else h = h.ppp;
            }
            else if (point.distance(h.x,h.y,h.z) > eps && x <= h.x && y >= h.y && z >= h.z){
                if (h.mpp == null){
                    h.mpp = new Node(x,y,z);
                    h = null;
                }
                else h = h.mpp;
            }
            else if (point.distance(h.x,h.y,h.z) > eps && x >= h.x && y <= h.y && z >= h.z){
                if (h.pmp == null){
                    h.pmp = new Node(x,y,z);
                    h = null;
                }
                else h = h.pmp;
            }
            else if (point.distance(h.x,h.y,h.z) > eps && x >= h.x && y >= h.y && z <= h.z){
                if (h.ppm == null){
                    h.ppm = new Node(x,y,z);
                    h = null;
                }
                else h = h.ppm;
            }
            else if (point.distance(h.x,h.y,h.z) > eps && x <= h.x && y <= h.y && z >= h.z){
                if (h.mmp == null){
                    h.mmp = new Node(x,y,z);
                    h = null;
                }
                else h = h.mmp;
            }
            else if (point.distance(h.x,h.y,h.z) > eps && x <= h.x && y >= h.y && z <= h.z){
                if (h.mpm == null){

                    h.mpm = new Node(x,y,z);
                    h = null;
                }
                else h = h.mpm;
            }
            else if (point.distance(h.x,h.y,h.z) > eps && x >= h.x && y <= h.y && z <= h.z){
                if (h.pmm == null){
                    h.pmm = new Node(x,y,z);
                    h = null;
                }
                else h = h.pmm;
            }
            else if (point.distance(h.x,h.y,h.z) > eps && x <= h.x && y <= h.y && z <= h.z){
                if (h.mmm == null){
                    h.mmm = new Node(x,y,z);
                    h = null;
                }
                else h = h.mmm;
            }
            else {//System.out.println(h.x);
                return false;}
        }
        return true;
    }
}
