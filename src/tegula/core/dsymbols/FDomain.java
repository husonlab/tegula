/*
 * FDomain.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.core.dsymbols;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import tegula.core.fundamental.Approximate;
import tegula.core.fundamental.Glue;
import tegula.core.fundamental.data.*;
import tegula.geometry.Tools;
import tegula.tiling.Constraints;
import tegula.tiling.Generators;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;

/**
 * Compute a fundamental domain for a given Delaney-symbol
 * by huson on 3/28/16.
 */
public class FDomain {
    private Geometry geometry;
    private String groupName;

    private final DSymbol dSymbol;
    private final DELANEY d;

    private final Generators generators = new Generators();
    private final Constraints constraints = new Constraints();

    private Bounds boundingBox;

    /**
     * constructor
     *
     * @param dSymbol
     */
    public FDomain(DSymbol dSymbol, boolean addBendToAnEdge) {
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

        if (addBendToAnEdge) { // add a small "nose" to tile to show symmetry (note that some tilings exist for which this doesn't work)
            final Generators generators = getGenerators();

            for (int a = 1; a <= dSymbol.size(); a++) {
                final int b = dSymbol.getS2(a);
                if (a != b) {
                    final Point2D addA = getChamberCenter(a).subtract(getEdgeCenter(2, a)).multiply(0.6);
                    setEdgeCenter(getEdgeCenter(2, a).add(addA), 2, a);
                    if (isBoundaryEdge(2, a)) {
                        final Point3D a3D = Tools.map2Dto3D(getGeometry(), getEdgeCenter(2, a));
                        final Transform gen = generators.get(2, a);
                        final Point3D b3D = gen.transform(a3D);
                        final Point2D b2D = Tools.map3Dto2D(getGeometry(), b3D);
                        setEdgeCenter(b2D, 2, b);
                    }
                    break;
                }
            }
        }


        boundingBox = computeBoundingBox();
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

    public Point2D getVertex(int k, int a) {
        int vertex = d.getNcr(a - 1).getOc(k);
        return new Point2D(d.getOcr()[vertex].getPosx(), d.getOcr()[vertex].getPosy());
    }

    public Point3D getVertex3D(int k, int a) {
        return Tools.map2Dto3D(geometry, getVertex(k, a));
    }

    public void setVertex(Point2D apt, int k, int a) {
        int vertex = d.getNcr(a - 1).getOc(k);
        d.getOcr()[vertex].setPosx(apt.getX());
        d.getOcr()[vertex].setPosy(apt.getY());
    }

    public Point2D getEdgeCenter(int i, int a) {
        int edge = d.getNcr(a - 1).getEc()[i];
        return new Point2D(d.getEcr()[edge].getPosx(), d.getEcr()[edge].getPosy());
    }

    public Point3D getEdgeCenter3D(int k, int a) {
        return Tools.map2Dto3D(geometry, getEdgeCenter(k, a));
    }

    public void setEdgeCenter(Point2D apt, int k, int a) {
        int edge = d.getNcr(a - 1).getEc()[k];
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

    public boolean isBoundaryEdge(int k, int a) {
        int edge = d.getNcr(a - 1).getEc()[k];
        edge = d.getEcr()[edge].getEp();
        return d.getEdg()[edge].getId() == 0;
    }

    public int getOrientation(int a) {
        int node = d.getNcr(a - 1).getNp();
        return d.getNod()[node].getSg();
    }

    public boolean isUnsplitRotation(int k, int a) {
        final int vertex = d.getNcr(a - 1).getOc(k);
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

    public String getGroupName() {
        return groupName;
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

    private Bounds computeBoundingBox() {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Point2D[] array : getCoordinates()) {
            if (array != null) {
                for (Point2D apt : array) {
                    if (apt != null) {
                        minX = Math.min(minX, apt.getX());
                        maxX = Math.max(maxX, apt.getX());
                        minY = Math.min(minY, apt.getY());
                        maxY = Math.max(maxY, apt.getY());
                    }
                }
            }
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    public Point2D[][] getCoordinates() {
        final Point2D[][] table = new Point2D[dSymbol.size() + 1][7];
        // 0-vertex, 1-vertex, 2-vertex, 0-edge-center, 1-edge-center, 2-edge-center, chamber center

        for (int a = 1; a <= dSymbol.size(); a++) {
            for (int i = 0; i <= 2; i++) {
                table[a][i] = getVertex(i, a);
            }
            for (int i = 0; i <= 2; i++) {
                table[a][i + 3] = getEdgeCenter(i, a);
            }
            table[a][6] = getChamberCenter(a);
        }
        return table;
    }

    public void setCoordinates(Point2D[][] table) {
        for (int a = 1; a <= dSymbol.size(); a++) {
            for (int i = 0; i <= 2; i++) {
                setVertex(table[a][i], i, a);
            }
            for (int i = 0; i <= 2; i++) {
                setEdgeCenter(table[a][i + 3], i, a);
            }
            setChamberCenter(table[a][6], a);
        }
        boundingBox = computeBoundingBox();
    }

    public Generators getGenerators() {
        ComputeGeneratorsAndConstraints.apply(this, generators, constraints);
        return generators;
    }

    public Constraints getConstraints() {
        ComputeGeneratorsAndConstraints.apply(this, generators, constraints);
        return constraints;
    }

    public Bounds getBoundingBox() {
        return boundingBox;
    }

    public Point3D computeReferencePoint() {
        double dMax = 0, dMin = 100000, dist;
        int index = 1;
        for (int i = 1; i <= size(); i++) {
            Point3D a = getChamberCenter3D(i).multiply(0.01);
            for (int j = 1; j <= size(); j++) {
                if (j != i) {
                    dist = Tools.distance(getGeometry(), a, getChamberCenter3D(j).multiply(0.01));
                    if (dist > dMax) {
                        dMax = dist;
                    }
                }
            }
            if (dMax < dMin) {
                dMin = dMax;
                index = i;
            }
            dMax = 0;
        }
        if (geometry.equals(Geometry.Hyperbolic))
            return getChamberCenter3D(index).multiply(0.01);
        else
            return getChamberCenter3D(index);
    }

    /**
     * calculate the diameter
     *
     * @return diameter
     */
    public double calculateDiameter() {
        // Save vertices of fundamental domain in list:
        LinkedList<Point3D> vertices = new LinkedList<>();
        for (int k = 1; k <= size(); k++) {
            vertices.add(getVertex3D(0, k));
            vertices.add(getVertex3D(1, k));
            vertices.add(getVertex3D(2, k));
        }
        double d = 0;

        for (int i = 0; i <= vertices.size() - 1; i++) {
            for (int j = i + 1; j <= vertices.size() - 1; j++) {
                Point3D a = vertices.get(i), b = vertices.get(j);
                // Calculate hyperbolic distance between a and b:
                double scalar = (a.getZ() * b.getZ() - a.getX() * b.getX() - a.getY() * b.getY()) / 10000;
                double dist = Math.log(Math.abs(scalar + Math.sqrt(Math.abs(scalar * scalar - 1)))); // Inverse function of cosh
                if (dist > d) { // Find maximal distance
                    d = dist;
                }
            }
        }
        return d;
    }


    /**
     * set all coordinates (previously saved using getCoordinates)
     *
     * @param table
     */
    public static void reportCoordinates(Point2D[][] table) {
        for (int a = 1; a < table.length; a++) {
            for (int i = 0; i <= 2; i++) {
                System.err.println(String.format("Vertex(%d,%d): %.6f,%.6f", a, i, table[a][i].getX(), table[a][i].getY()));
            }
            for (int i = 0; i <= 2; i++) {
                System.err.println(String.format("EdgeCr(%d,%d): %.6f,%.6f", a, i, table[a][i + 3].getX(), table[a][i + 3].getY()));
            }
            System.err.println(String.format("ChamberCr(%d): %.6f,%.6f", a, table[a][6].getX(), table[a][6].getY()));
        }
    }
}
