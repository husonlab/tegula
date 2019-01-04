package tiler.core.dsymbols;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import tiler.core.fundamental.Approximate;
import tiler.core.fundamental.Glue;
import tiler.core.fundamental.data.*;
import tiler.tiling.Tools;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Compute a fundamental domain for a given Delaney-symbol
 * by huson on 3/28/16.
 */
public class FDomain {
    private Geometry geometry;

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
        return Tools.map2Dto3D(geometry, getVertex(i, a));
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
        return Tools.map2Dto3D(geometry, getEdgeCenter(i, a));
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
        return Tools.map2Dto3D(geometry, getChamberCenter(a));
    }

    public void setChamberCenter(Point2D apt, int a) {
        d.getNcr()[a - 1].setPosx(apt.getX());
        d.getNcr()[a - 1].setPosy(apt.getY());
    }

    /**
     * Transformation t maps fDomain to a copy of itself (for Euclidean and hyperbolic case).
     *
     * @param t
     */
    public void recenterFDomain(Transform t) {
        if (geometry == Geometry.Euclidean) {
            for (int z = 0; z < d.getNcrs(); z++) {
                final NCR ncr = d.getNcr(z);
                Point3D position = new Point3D(100 * ncr.getPosx(), 100 * ncr.getPosy(), 0);
                position = t.transform(position);
                position = new Point3D(position.getX(), position.getY(), 0);
                position = position.multiply(0.01);
                ncr.setPosx(position.getX());
                ncr.setPosy(position.getY());
            }

            for (int z = 0; z < d.getEcrs(); z++) {
                final ECR ecr = d.getEcr(z);
                Point3D position = new Point3D(100 * ecr.getPosx(), 100 * ecr.getPosy(), 0);
                position = t.transform(position);
                position = new Point3D(position.getX(), position.getY(), 0);
                position = position.multiply(0.01);
                ecr.setPosx(position.getX());
                ecr.setPosy(position.getY());
            }

            for (int z = 0; z < d.getOcrs(); z++) {
                final OCR ocr = d.getOcr(z);
                Point3D position = new Point3D(100 * ocr.getPosx(), 100 * ocr.getPosy(), 0);
                position = t.transform(position);
                position = new Point3D(position.getX(), position.getY(), 0);
                position = position.multiply(0.01);
                ocr.setPosx(position.getX());
                ocr.setPosy(position.getY());
            }
        } else if (geometry == Geometry.Hyperbolic) {
            for (int z = 0; z < d.getNcrs(); z++) {
                final NCR ncr = d.getNcr(z);
                Point2D position = new Point2D(ncr.getPosx(), ncr.getPosy());
                position = Tools.map3Dto2D(geometry, t.transform(Tools.map2Dto3D(geometry, position)));
                ncr.setPosx(position.getX());
                ncr.setPosy(position.getY());
            }

            for (int z = 0; z < d.getEcrs(); z++) {
                final ECR ecr = d.getEcr(z);
                Point2D position = new Point2D(ecr.getPosx(), ecr.getPosy());
                position = Tools.map3Dto2D(geometry, t.transform(Tools.map2Dto3D(geometry, position)));
                ecr.setPosx(position.getX());
                ecr.setPosy(position.getY());
            }

            for (int z = 0; z < d.getOcrs(); z++) {
                final OCR ocr = d.getOcr(z);
                Point2D position = new Point2D(ocr.getPosx(), ocr.getPosy());
                position = Tools.map3Dto2D(geometry, t.transform(Tools.map2Dto3D(geometry, position)));
                ocr.setPosx(position.getX());
                ocr.setPosy(position.getY());
            }
        }
    }

    /**
     * Translates fDomain (in Euclidean and hyperbolic case).
     *
     * @param dx
     * @param dy
     */
    public void translate(double dx, double dy) {
        if (geometry == Geometry.Euclidean) {
            dx /= 100;
            dy /= 100;

            for (int z = 0; z < d.getNcrs(); z++) {
                final NCR ncr = d.getNcr(z);
                ncr.setPosx(ncr.getPosx() + dx);
                ncr.setPosy(ncr.getPosy() + dy);
            }
            for (int z = 0; z < d.getEcrs(); z++) {
                final ECR ecr = d.getEcr(z);
                ecr.setPosx(ecr.getPosx() + dx);
                ecr.setPosy(ecr.getPosy() + dy);
            }
            for (int z = 0; z < d.getOcrs(); z++) {
                final OCR ocr = d.getOcr(z);
                ocr.setPosx(ocr.getPosx() + dx);
                ocr.setPosy(ocr.getPosy() + dy);
            }
        } else if (geometry == Geometry.Hyperbolic) {
            //dx /= 300;
            //dy /= 300;
            for (int z = 0; z < d.getNcrs(); z++) {
                final NCR ncr = d.getNcr(z);
                Point2D translated = HyperbolicTranslation(dx, dy, ncr.getPosx(), ncr.getPosy());
                ncr.setPosx(translated.getX());
                ncr.setPosy(translated.getY());
            }
            for (int z = 0; z < d.getEcrs(); z++) {
                final ECR ecr = d.getEcr(z);
                Point2D translated = HyperbolicTranslation(dx, dy, ecr.getPosx(), ecr.getPosy());
                ecr.setPosx(translated.getX());
                ecr.setPosy(translated.getY());
            }

            for (int z = 0; z < d.getOcrs(); z++) {
                final OCR ocr = d.getOcr(z);
                Point2D translated = HyperbolicTranslation(dx, dy, ocr.getPosx(), ocr.getPosy());
                ocr.setPosx(translated.getX());
                ocr.setPosy(translated.getY());
            }
        }
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
     * Hyperbolic translation of fundamental domain in Poincare model
     *
     * @param dx
     * @param dy
     * @param posX
     * @param posY
     * @return Point2D
     */
    private Point2D HyperbolicTranslation(double dx, double dy, double posX, double posY) {
        final double distance = Math.sqrt(dx * dx + dy * dy); //Distance for translation along vector (dx,dy)
        final Point2D Y_AXIS = new Point2D(0, 1);  // For definition of hyperbolic translation along y-axis
        final double angle = Y_AXIS.angle(dx, dy); // Rotation angle
        final Point3D rotAxis; //Rotation axis

        if (dx <= 0) {
            rotAxis = Rotate.Z_AXIS;    // Counter-clockwise rotation
        } else {
            rotAxis = Rotate.Z_AXIS.multiply(-1);   // Clockwise rotation
        }
        final Rotate rotateForward = new Rotate(angle, rotAxis); // Rotates fundamental domain forward to perform translation along y-axis
        final Rotate rotateBack = new Rotate(-angle, rotAxis); // Rotates backward to original position

        final Point2D posRotated = rotateBack.transform(posX, posY); // Rotates a given point of fundamental domain forward

        // Translation of rotated point "posRotated" along y-Axis (calculated from concatenation of translations on hyperboloid and mapping from Poincare to hyperboloid model):
        final double d = posRotated.getX() * posRotated.getX() + posRotated.getY() * posRotated.getY();
        final double newPosX = 2 * posRotated.getX() / (1 - d + Math.sinh(distance) * 2 * posRotated.getY() + Math.cosh(distance) * (1 + d));
        final double newPosY = (Math.cosh(distance) * 2 * posRotated.getY() + Math.sinh(distance) * (1 + d)) / (1 - d + Math.sinh(distance) * 2 * posRotated.getY() + Math.cosh(distance) * (1 + d));

        return rotateForward.transform(newPosX, newPosY); //Returns the result of the transform by rotating backward.
    }

    /**
     * get all coordinates
     *
     * @return coordinates
     */
    public Point2D[][] getCoordinates() {
        final Point2D[][] array = new Point2D[dSymbol.size() + 1][7];
        // 0-vertex, 1-vertex, 2-vertex, 0-edge-center, 1-edge-center, 2-edge-center, chamber center

        for (int a = 1; a <= dSymbol.size(); a++) {
            for (int i = 0; i <= 2; i++) {
                array[a][i] = getVertex(i, a);
            }
            for (int i = 0; i <= 2; i++) {
                array[a][i + 3] = getEdgeCenter(i, a);
            }
            array[a][6] = getChamberCenter(a);
        }
        return array;
    }

    /**
     * compute the barycenter of all coordinates
     *
     * @return center
     */
    public Point2D computerCenter() {
        double x = 0;
        double y = 0;
        int count = 0;

        final Point2D[][] arrays = getCoordinates();
        for (Point2D[] array : arrays) {
            for (Point2D point : array) {
                if (point != null) {
                    x += point.getX();
                    y += point.getY();
                    count++;
                }
            }
        }
        if (count == 0)
            return new Point2D(0, 0);
        else
            return new Point2D(x / count, y / count);

    }

    /**
     * set all coordinates (previously saved using getCoordinates)
     *
     * @param array
     */
    public void setCoordinates(Point2D[][] array) {
        for (int a = 1; a <= dSymbol.size(); a++) {
            for (int i = 0; i <= 2; i++) {
                System.err.println(String.format("vertex(%d,%d): (%.2f,%.2f) -> (%.2f,%.2f)",
                        a, i, getVertex(i, a).getX(), getVertex(i, a).getY(), array[a][i].getX(), array[a][i].getY()));
                setVertex(array[a][i], i, a);
            }
            for (int i = 0; i <= 2; i++) {
                System.err.println(String.format("edge(%d,%d): (%.2f,%.2f) -> (%.2f,%.2f)",
                        a, i, getEdgeCenter(i, a).getX(), getEdgeCenter(i, a).getY(), array[a][i + 3].getX(), array[a][i + 3].getY()));
                setEdgeCenter(array[a][i + 3], i, a);
            }
            System.err.println(String.format("chamber(%d): (%.2f,%.2f) -> (%.2f,%.2f)",
                    a, getChamberCenter(a).getX(), getChamberCenter(a).getY(), array[a][6].getX(), array[a][6].getY()));
            setChamberCenter(array[a][6], a);
        }
    }
}
