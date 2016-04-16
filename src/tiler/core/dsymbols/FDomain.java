package tiler.core.dsymbols;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import tiler.core.fundamental.Approximate;
import tiler.core.fundamental.Glue;
import tiler.core.fundamental.data.Base;
import tiler.core.fundamental.data.DELANEY;
import tiler.core.fundamental.data.ORB;
import tiler.core.fundamental.data.Util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Compute a fundamental domain for a given Delaney-symbol
 * by huson on 3/28/16.
 */
public class FDomain {
    public static enum Geometry {Euclidean, Spherical, Hyperbolic}

    private final Geometry geometry;

    private final DSymbol dSymbol;
    private final DELANEY d;

    /**
     * constructor
     *
     * @param dSymbol
     */
    public FDomain(DSymbol dSymbol) {
        this.dSymbol = dSymbol;
        this.d = new DELANEY();

        Base.init_delaney(d);

        symbol2delaney(dSymbol, d);
        final Glue glue = new Glue(d);
        glue.glue_delaney();
        Approximate.compute_radius(d);
        Approximate.compute_coords(d);
        boolean changed;
        do {
            changed = Approximate.relax_coords(d);
        }
        while (changed);

        if (d.getCrv() < 0)
            geometry = Geometry.Hyperbolic;
        else if (d.getCrv() > 0)
            geometry = Geometry.Spherical;
        else
            geometry = Geometry.Euclidean;
    }

    /**
     * get number of flags
     *
     * @return size
     */
    public int size() {
        return dSymbol.size();
    }

    /**
     * copy DSymbol to DELANEY
     *
     * @param ds
     * @param d
     */
    private static void symbol2delaney(DSymbol ds, DELANEY d) {
        for (int i = 0; i <= 2; i++)
            for (int a = 1; a <= ds.size(); a++)
                if (ds.getSi(i, a) >= a)
                    Util.define_edg(d, i, a - 1, ds.getSi(i, a) - 1);

        Util.finish_graph(d);

        Util.prepare_m(d);

        for (int i = 0; i <= 1; i++)
            for (int j = i + 1; j <= 2; j++)
                for (int a = 1; a <= ds.size(); a++)
                    Util.define_m(d, a - 1, i, j, ds.getMij(i, j, a));

        /*
        for(int i= 0;i<=1;i++) {
            for (int j = i + 1; j <= 2; j++) {
                for (int a = 0; a < ds.size(); a++) {
                    System.err.println(String.format("%d -> %d",a,d.getNod(a).getOp(3-i-j)));
                    int	op = d.getNod(a).getOp(3-i-j);
                    System.err.println(String.format("m%d%d(%d)=%d", i, j, a, d.getOrb(op).getM()));
                }
            }
        }
        */
    }

    public Point2D getVertex(int i, int a) {
        int vertex = d.getNcr(a - 1).getOc(i);
        return new Point2D(d.getOcr()[vertex].getPosx(), d.getOcr()[vertex].getPosy());
    }

    public Point3D getVertex3D(int i, int a) {
        return map2Dto3D(getVertex(i, a));
    }

    public void setVertex(Point2D apt, int i, int a) {
        int vertex = d.getNcr(a - 1).getOc(i);
        d.getOcr()[vertex].setPosx(apt.getX());
        d.getOcr()[vertex].setPosy(apt.getY());
    }

    public Point2D getEdgeCenter(int i, int a) {
        int edge = d.getNcr(a - 1).getEc()[i];
        return new Point2D(d.getEcr()[edge].getPosx(), d.getEcr()[edge].getPosy());
    }

    public Point3D getEdgeCenter3D(int i, int a) {
        return map2Dto3D(getEdgeCenter(i, a));
    }

    public void setEdgeCenter(Point2D apt, int i, int a) {
        int edge = d.getNcr(a - 1).getEc()[i];

        d.getEcr()[edge].setPosx(apt.getX());
        d.getEcr()[edge].setPosy(apt.getY());
    }

    public Point2D getChamberCenter(int a) {
        return new Point2D(d.getNcr()[a - 1].getPosx(), d.getNcr()[a - 1].getPosy());
    }

    public Point3D getChamberCenter3D(int a) {
        return map2Dto3D(getChamberCenter(a));
    }

    public void setChamberCenter(Point2D apt, int a) {
        d.getNcr()[a - 1].setPosx(apt.getX());
        d.getNcr()[a - 1].setPosy(apt.getY());
    }

    public boolean isBoundaryEdge(int i, int a) {
        int edge = d.getNcr(a - 1).getEc()[i];
        edge = d.getEcr()[edge].getEp();
        return d.getEdg()[edge].getId() == 0;
    }

    public int getOrientation(int a) {
        int node = d.getNcr(a - 1).getNp();
        return d.getNod()[node].getSg();
    }

    public boolean isUnsplitRotation(int i, int a) {
        final int vertex = d.getNcr(a - 1).getOc(i);
        final ORB orb = d.getOrb(d.getOcr(vertex).getOp());
        return orb.getS() == 1;
    }

    public String toString() {
        final StringWriter w = new StringWriter();
        try {
            write(w);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return w.toString();
    }

    public void write(Writer w) throws IOException {
        w.write("Fundamental Domain:\n");
        for (int a = 1; a <= dSymbol.size(); a++) {
            w.write("Chamber: " + a + "\n");
            w.write("Vertices:\n");
            for (int i = 0; i <= 2; i++) {
                final Point2D apt = getVertex(i, a);
                w.write(String.format("v%d: %.6f %.6f\n", i, apt.getX(), apt.getY()));
            }
            w.write("Edge-Centers:\n");
            for (int i = 0; i <= 2; i++) {
                final Point2D apt = getEdgeCenter(i, a);
                w.write(String.format("e%d: %.6f %.6f\n", i, apt.getX(), apt.getY()));
            }
            w.write("Chamber-Center:\n");
            {
                final Point2D apt = getChamberCenter(a);
                w.write(String.format("c: %.6f %.6f\n", apt.getX(), apt.getY()));
            }
        }
    }

    public DSymbol getDSymbol() {
        return dSymbol;
    }

    public double getCurvature() {
        return d.getCrv();
    }

    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * map 2D point to 3D point, depending on set geometry
     *
     * @param apt
     * @return 3D point
     */
    private Point3D map2Dto3D(Point2D apt) {
        switch (geometry) {
            default:
            case Euclidean: {
                return new Point3D(100 * apt.getX(), 100 * apt.getY(), 0);
            }
            case Spherical: {
                final double d = apt.getX() * apt.getX() + apt.getY() * apt.getY();
                return new Point3D(100 * (2 * apt.getX() / (1 + d)), 100 * (2 * apt.getY() / (1 + d)), 100 * ((d - 1) / (d + 1)));
            }
            case Hyperbolic: {
                final double d = apt.getX() * apt.getX() + apt.getY() * apt.getY();
                if (d < 1)
                    return new Point3D(100 * (2 * apt.getX() / (1 - d)), 100 * (2 * apt.getY() / (1 - d)), 100 * ((1 + d) / (1 - d)) - 100);
                else
                    return new Point3D(0, 0, 0);
            }
        }
    }

}
