package tiler.old;

/**
 * geometry
 * Created by huson on 3/29/16.
 */
public class Geometry {
    public Geom geom;
    public static double maxDiffForEquals = 1e-14;


    /**
     * constructor
     */
    public Geometry(Geom geom) {
        this.geom = geom;

    }

    public static double getMaxDiffForEquals() {
        return maxDiffForEquals;
    }

    public static void setMaxDiffForEquals(double maxDiffForEquals) {
        Geometry.maxDiffForEquals = maxDiffForEquals;
    }

    public Geom getGeom() {
        return geom;
    }

    public void setGeom(Geom geom) {
        this.geom = geom;
    }
}
