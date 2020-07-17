/*
 * ReshapeManager.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.core.reshape;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.geometry.Tools;
import tegula.tiling.Generators;
import tegula.tiling.TilingBase;
import tegula.tilingeditor.StraightenEdges;
import tegula.util.Updateable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

/**
 * setup and manage edge reshape handles
 * Daniel HUson and Ruediger Zeller, 2016
 */
public class ReshapeManager {
    private final Updateable updateable;
    private final FDomain fDomain;
    private final DSymbol ds;
    private final Generators generators;

    /**
     * constructor
     *
     * @param updateable
     */
    public ReshapeManager(TilingBase tiling, Updateable updateable) {
        this.updateable = updateable;
        fDomain = tiling.getfDomain();
        ds = tiling.getDSymbol();
        generators = tiling.getGenerators();
    }

    /**
     * create handles to control shape
     *
     * @return handles
     */
    public Collection<Node> createHandles() {
        final ArrayList<Node> handles = new ArrayList<>();
        // Straighten 0- and 1-edges
        //StraightenEdges.straighten01Edges(fDomain);

        final int size = fDomain.size();

        // Compute handles for vertices
        for (int k = 0; k <= 2; k++) {
            final int i = (k == 0 ? 1 : 0);
            final int j = (k == 2 ? 1 : 2);

            final BitSet visited = new BitSet();
            for (int a = 1; a <= size; a = ds.nextOrbit(i, j, a, visited)) {
                if ((ds.getVij(i, j, a) == 1) && !(i == 0 && j == 2 && a == ds.getS2(a))) {
                    final Shape shape = makeReshapeHandle(fDomain.getVertex3D(k, a), ds.isCycle(i, j, a) ? Color.GREEN : Color.YELLOW);
                    handles.add(shape);
                    setMouseHandler(shape, 1 - i, a);
                } else {
                    handles.add(makeReshapeHandle(fDomain.getVertex3D(k, a), Color.RED));
                }
            }
        }

        // Compute handles for 2-edge-centers
        for (int a = 1; a <= size; a++) {
            if (a < ds.getS2(a)) {
                final Shape shape = makeReshapeHandle(fDomain.getEdgeCenter3D(2, a), Color.GREEN);
                handles.add(shape);
                setMouseHandler(shape, 2, a);
            } else if (a == ds.getS2(a)) {
                handles.add(makeReshapeHandle(fDomain.getEdgeCenter3D(2, a), Color.DARKRED));
            }
        }
        return handles;
    }

    /**
     * make a reshape handle
     *
     * @param v
     * @param color
     * @return reshape handle
     */
    private static Shape makeReshapeHandle(Point3D v, Color color) {
        final Circle shape = new Circle(4);
        shape.setTranslateX(v.getX());
        shape.setTranslateY(v.getY());
        shape.setStroke(Color.DARKGRAY);
        shape.setFill(color);
        return shape;
    }

    /**
     * Reset shape of tiles
     *
     * @param deltaX
     * @param deltaY
     */
    public Point2D resetShape(double deltaX, double deltaY, int type, int a) {
        // Reset Point in fundamental domain
        Point2D transVector = new Point2D(deltaX, deltaY);

        if (type <= 1) {
            final int length = ds.computeOrbitLength(1 - type, 2, a);
            // Add restrictions:
            if (type == 1) {
                transVector = add1Restriction(transVector.getX(), transVector.getY(), a); // Restrictions for 1-handles
            } else {
                transVector = add0Restriction(transVector.getX(), transVector.getY(), a, length); // Restrictions for 0-handles
            }

            transVector = addMirrorRestriction(transVector.getX(), transVector.getY(), length, type, a); // Mirror axis restriction

            final Translate translate = new Translate(transVector.getX(), transVector.getY());

            // Translate Point of type i in chamber a
            Point3D pt = fDomain.getVertex3D(type, a);
            pt = translate.transform(pt);
            Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt);
            fDomain.setVertex(pt2d, type, a);

            // Consider all points in orbit of a (especially if chamber contains boundary edges)
            for (int k = 1; k <= length; k++) {
                // If (1-i)-edge is on boundary
                if (fDomain.isBoundaryEdge(1 - type, a)) {
                    final Transform g = generators.get(1 - type, a);
                    pt = g.transform(pt);
                    pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt);
                    fDomain.setVertex(pt2d, type, ds.getSi(1 - type, a));
                }
                a = ds.getSi(1 - type, a);

                // If 2-edge is on boundary
                if (fDomain.isBoundaryEdge(2, a)) {
                    final Transform g = generators.get(2, a);
                    pt = g.transform(pt);
                    pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt);
                    fDomain.setVertex(pt2d, type, ds.getSi(2, a));
                }
                a = ds.getSi(2, a);
            }
        } else {
            transVector = add2Restriction(generators, deltaX, deltaY, a);
            final Translate t = new Translate(transVector.getX(), transVector.getY());

            Point3D apt = fDomain.getEdgeCenter3D(2, a);
            apt = t.transform(apt);
            Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), apt);
            fDomain.setEdgeCenter(pt2d, 2, a);
            if (fDomain.isBoundaryEdge(2, a)) {
                final Transform g = generators.get(2, a);
                apt = g.transform(apt);
                pt2d = Tools.map3Dto2D(fDomain.getGeometry(), apt);
                fDomain.setEdgeCenter(pt2d, 2, ds.getS2(a));
            }
        }
        // Straighten 0- and 1-edges
        StraightenEdges.straighten01Edges(fDomain);
        return transVector;
    }

    /**
     * Add restrictions to translation in case that handle lies on exactly one mirror axis
     *
     * @param deltaX
     * @param deltaY
     * @param orbitLength
     * @param type
     * @param flag
     * @return
     */
    private Point2D addMirrorRestriction(double deltaX, double deltaY, int orbitLength, int type, int flag) {
        Point3D r = new Point3D(0, 0, 0);
        Point3D n = new Point3D(0, 0, 0);
        Point3D q = new Point3D(0, 0, 0);
        final int m = fDomain.size();
        // Count number of chambers lying in (1-type)-2-orbit containing flag
        final BitSet visited = new BitSet(m);
        ds.markOrbit(1 - type, 2, flag, visited);
        int numberOfChambers = 0;
        for (int d = 1; d <= m; d++) {
            if (visited.get(d)) {
                numberOfChambers++;
            }
        }

        // Restrict movement if handle lies on exactly one mirror axis:
        if (2 * orbitLength / numberOfChambers == 2) { // Condition for exactly one mirror axis
            while (flag <= m) {
                if (fDomain.isBoundaryEdge(1 - type, flag) && ds.getSi(1 - type, flag) == flag) { // Mirror axis is always a boundary for fundamental domain
                    Transform g = generators.get(1 - type, flag);
                    n = fDomain.getVertex3D(1 - type, flag).subtract(g.transform(fDomain.getVertex3D(1 - type, flag))); // Orthogonal direction of mirror axis
                    r = new Point3D(n.getY(), -n.getX(), 0); // Direction of translation for handle = direction of mirror axis
                    Point3D transformed = g.transform(fDomain.getVertex3D(1 - type, flag));
                    q = fDomain.getVertex3D(1 - type, flag).midpoint(transformed);

                    break;
                } else {
                    flag = ds.getSi(1 - type, flag);
                }
                if (fDomain.isBoundaryEdge(2, flag) && ds.getS2(flag) == flag) { // Consider 2-edge-boundaries
                    Transform g = generators.get(2, flag);
                    n = fDomain.getVertex3D(2, flag).subtract(g.transform(fDomain.getVertex3D(2, flag)));
                    r = new Point3D(n.getY(), -n.getX(), 0);
                    Point3D transformed = g.transform(fDomain.getVertex3D(2, flag));
                    q = fDomain.getVertex3D(2, flag).midpoint(transformed);
                    break;
                } else {
                    flag = ds.getSi(2, flag);
                }
            }

            // Change direction (deltaX, deltaY) to translation along mirror axis
            Point3D oldPos = fDomain.getVertex3D(type, flag);
            Point3D newPos = oldPos.add(new Point3D(deltaX, deltaY, 0));

            // While loop prevents from rounding errors
            int counter = 0;
            while (counter <= 50) {
                Point3D qp = q.subtract(newPos);
                double b = (qp.getY() * r.getX() - qp.getX() * r.getY()) / (r.getX() * r.getX() + r.getY() * r.getY());
                newPos = newPos.add(n.multiply(b));
                counter++;
            }

            deltaX = newPos.getX() - oldPos.getX();
            deltaY = newPos.getY() - oldPos.getY();
        }
        return new Point2D(deltaX, deltaY);
    }

    /**
     * Add restrictions for mouse movement to prevent overlaps
     *
     * @param deltaX
     * @param deltaY
     * @param flag
     * @return new direction
     */
    private Point2D add2Restriction(Generators generators, double deltaX, double deltaY, int flag) {
        // Restrict movement for 2-edge-handles
        Point2D transVec = new Point2D(deltaX, deltaY);
        Transform invGen = new Translate();
        if (fDomain.isBoundaryEdge(2, flag)) {
            invGen = generators.get(2, ds.getS2(flag));
        }

        // There exist 4 restrictions. Each restricting line / plane is of the form <x,n> = c.
        // R - directions of line, N - normal vector, c - coordinate
        Point3D[] R = new Point3D[4], N = new Point3D[4], Q = new Point3D[4]; // Save normal vectors N, directions R and position vectors P of restricting lines
        double[] c = new double[4]; // Save coordinates of the 4 restricting lines / planes

        // Define restriction 0 and 1 for flag a
        R[0] = fDomain.getVertex3D(2, flag).subtract(fDomain.getVertex3D(0, flag));
        N[0] = new Point3D(R[0].getY(), -R[0].getX(), 0);
        Q[0] = fDomain.getVertex3D(0, flag);
        c[0] = fDomain.getVertex3D(0, flag).dotProduct(N[0]);
        R[1] = fDomain.getVertex3D(2, flag).subtract(fDomain.getVertex3D(1, flag));
        N[1] = new Point3D(R[1].getY(), -R[1].getX(), 0);
        Q[1] = fDomain.getVertex3D(1, flag);
        c[1] = fDomain.getVertex3D(1, flag).dotProduct(N[1]);

        // Define restrictions 2 and 3 for flag sigma2(a):
        Affine invGenMat = new Affine(invGen.getMxx(), invGen.getMxy(), invGen.getMxz(), 0, invGen.getMyx(), invGen.getMyy(), invGen.getMyz(), 0, invGen.getMzx(), invGen.getMzy(), invGen.getMzz(), 0);
        R[2] = fDomain.getVertex3D(2, ds.getS2(flag)).subtract(fDomain.getVertex3D(0, ds.getS2(flag)));
        R[2] = invGenMat.transform(R[2]);
        N[2] = new Point3D(R[2].getY(), -R[2].getX(), 0);
        Q[2] = fDomain.getVertex3D(0, flag);
        c[2] = fDomain.getVertex3D(0, flag).dotProduct(N[2]);
        R[3] = fDomain.getVertex3D(2, ds.getS2(flag)).subtract(fDomain.getVertex3D(1, ds.getS2(flag)));
        R[3] = invGenMat.transform(R[3]);
        N[3] = new Point3D(R[3].getY(), -R[3].getX(), 0);
        Q[3] = fDomain.getVertex3D(1, flag);
        c[3] = fDomain.getVertex3D(1, flag).dotProduct(N[3]);

        Point3D firstPos = (fDomain.getVertex3D(0, flag).add(fDomain.getVertex3D(1, flag))).multiply(0.5); // Midpoint of 0- and 1- vertex
        Point3D oldPos = fDomain.getEdgeCenter3D(2, flag); // Actual position of handle

        return checkRestriction(transVec, R, N, Q, c, firstPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }

    private Point2D add1Restriction(double deltaX, double deltaY, int flag) {
        // There exist 4 restrictions. Each restricting line / plane is of the form <x,n> = c.
        // R - directions of line, N - normal vector, c - coordinate
        Point3D[] R = new Point3D[4], N = new Point3D[4], Q = new Point3D[4]; // Save normal vectors, directions and position vectors of restricting lines
        double[] c = new double[4]; // Save coordinates of the 4 restricting lines / planes

        Affine genMat = new Affine(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
        Point2D transVec = new Point2D(deltaX, deltaY);
        Transform gen = new Translate();
        for (int i = 0; i <= 1; i++) {
            R[2 * i] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2 * i] = genMat.transform(R[2 * i]);
            N[2 * i] = new Point3D(R[2 * i].getY(), -R[2 * i].getX(), 0);
            Q[2 * i] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2 * i] = N[2 * i].dotProduct(Q[2 * i]);

            flag = ds.getS2(flag);
            if (fDomain.isBoundaryEdge(2, flag)) {
                gen = gen.createConcatenation(generators.get(2, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }

            R[2 * i + 1] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2 * i + 1] = genMat.transform(R[2 * i + 1]);
            N[2 * i + 1] = new Point3D(R[2 * i + 1].getY(), -R[2 * i + 1].getX(), 0);
            Q[2 * i + 1] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2 * i + 1] = N[2 * i + 1].dotProduct(Q[2 * i + 1]);

            flag = ds.getS0(flag);
            if (fDomain.isBoundaryEdge(0, flag)) {
                gen = gen.createConcatenation(generators.get(0, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }
        }

        Point3D apt = fDomain.getEdgeCenter3D(2, ds.getS0(flag));
        if (fDomain.isBoundaryEdge(0, flag)) {
            gen = generators.get(0, ds.getS0(flag));
            apt = gen.transform(apt);
        }

        Point3D firstPos = apt.midpoint(fDomain.getEdgeCenter3D(2, flag));
        Point3D oldPos = fDomain.getVertex3D(1, flag);

        return checkRestriction(transVec, R, N, Q, c, firstPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }

    private Point2D add0Restriction(double deltaX, double deltaY, int flag, int length) {
        // There exist length restrictions. Each restricting line / plane is of the form <x,n> = c.
        // R - directions of line, N - normal vector, c - coordinate

        Point3D[] R = new Point3D[2 * length], N = new Point3D[2 * length], Q = new Point3D[2 * length]; // Save normal vectors, directions and position vectors of restricting lines
        double[] c = new double[2 * length]; // Save coordinates of the 4 restricting lines / planes

        Affine genMat = new Affine(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
        Point2D transVec = new Point2D(deltaX, deltaY);
        Transform gen = new Translate();
        for (int i = 0; i <= length - 1; i++) {
            R[2 * i] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2 * i] = genMat.transform(R[2 * i]);
            N[2 * i] = new Point3D(R[2 * i].getY(), -R[2 * i].getX(), 0);
            Q[2 * i] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2 * i] = N[2 * i].dotProduct(Q[2 * i]);

            flag = ds.getS2(flag);
            if (fDomain.isBoundaryEdge(2, flag)) {
                gen = gen.createConcatenation(generators.get(2, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }

            R[2 * i + 1] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2 * i + 1] = genMat.transform(R[2 * i + 1]);
            N[2 * i + 1] = new Point3D(R[2 * i + 1].getY(), -R[2 * i + 1].getX(), 0);
            Q[2 * i + 1] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2 * i + 1] = N[2 * i + 1].dotProduct(Q[2 * i + 1]);

            flag = ds.getS1(flag);
            if (fDomain.isBoundaryEdge(1, flag)) {
                gen = gen.createConcatenation(generators.get(1, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }
        }

        Point3D apt = fDomain.getEdgeCenter3D(2, ds.getS0(flag));
        if (fDomain.isBoundaryEdge(1, flag)) {
            gen = generators.get(1, ds.getS1(flag));
            apt = gen.transform(apt);
        }

        Point3D oldPos = fDomain.getVertex3D(0, flag);

        return checkRestriction(transVec, R, N, Q, c, oldPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }

    /**
     * Change direction of mouse movement for handle when restrictions are broken
     *
     * @param transVec
     * @param R
     * @param N
     * @param c
     * @param firstPos
     * @param oldPos
     * @return new direction
     */
    private Point2D checkRestriction(Point2D transVec, Point3D[] R, Point3D[] N, Point3D[] Q, double[] c, Point3D firstPos, Point3D oldPos) {

        int length = N.length;

        // Change direction of translation if restrictions are broken
        Transform t = new Translate(transVec.getX(), transVec.getY()); // Original translation vector coming from mouse movement (in shapeHandler)
        Point3D newPos = t.transform(oldPos); // New position of handle
        boolean[] restrictions = new boolean[length];
        boolean[] checkRest = new boolean[length];
        for (int i = 0; i <= length - 1; i++) {
            restrictions[i] = largerOrEqual(c[i], N[i].dotProduct(firstPos));  // Compute restrictions (firstPos fulfills all restrictions and is used to define all restrictions)
        }

        // Make restricted area smaller by factor 0.00001 to prevent from rounding errors
        for (int i = 0; i <= length - 1; i++) {
            Point3D qpt = Q[i].add(N[i].multiply(0.00001));
            if (largerOrEqual(firstPos.dotProduct(N[i]), Q[i].dotProduct(N[i])) == largerOrEqual(qpt.dotProduct(N[i]), Q[i].dotProduct(N[i]))) {
                Q[i] = qpt;
            } else {
                Q[i] = Q[i].subtract(N[i].multiply(0.00001));
            }
            c[i] = Q[i].dotProduct(N[i]);
            checkRest[i] = largerOrEqual(c[i], N[i].dotProduct(newPos)); // Compute restrictions for newPos
        }

        // Loop works as long as newPos does not fulfill restrictions but at most 50 times
        // If a restriction is not fulfilled then newPos is moved onto the restricting line / plane in direction of its normal vector
        int counter = 0;
        while (counter <= 1000 && !Arrays.equals(checkRest, restrictions)) {
            for (int i = 0; i <= length - 1; i++) {
                if (checkRest[i] != restrictions[i]) {
                    Point3D qp = Q[i].subtract(newPos);
                    // b is the coefficient of the normal vector of restriction i
                    double b = (qp.getX() * R[i].getY() - qp.getY() * R[i].getX()) / (R[i].getX() * R[i].getX() + R[i].getY() * R[i].getY());
                    newPos = newPos.add(N[i].multiply(b)); // Move newPos onto the restricting line i
                    checkRest[i] = restrictions[i]; // Now restriction i is fulfilled
                    for (int j = 0; j <= length - 1; j++) { // Check other restrictions for newPos
                        if (j != i) {
                            checkRest[j] = largerOrEqual(c[j], N[j].dotProduct(newPos));
                        }
                    }
                }
            }
            counter++;
        }
        Point3D transVec3d = newPos.subtract(oldPos);
        return new Point2D(transVec3d.getX(), transVec3d.getY());
    }

    private boolean largerOrEqual(double a, double b) {
        return a >= b;
    }

    /**
     * set the mouse handler
     */
    private void setMouseHandler(Shape shape, int m, int a) {
        final ObjectProperty<Point2D> start = new SimpleObjectProperty<>(null);
        final ObjectProperty<Point2D> location = new SimpleObjectProperty<>(null);

        shape.setOnMousePressed((e) -> {
            start.set(new Point2D(e.getSceneX(), e.getSceneY()));
            location.set(start.get());
            e.consume();
        });

        shape.setOnMouseDragged((e) -> {
            final double deltaX = e.getSceneX() - location.get().getX();
            final double deltaY = e.getSceneY() - location.get().getY();

            // Reset shape of fundamental domain
            final Point2D transVector = resetShape(deltaX, deltaY, m, a);

            // Move handles along transVector
            shape.setTranslateX(shape.getTranslateX() + transVector.getX());
            shape.setTranslateY(shape.getTranslateY() + transVector.getY());

            location.set(new Point2D(e.getSceneX() - deltaX + transVector.getX(), e.getSceneY() - deltaY + transVector.getY()));

            e.consume();
        });

        shape.setOnMouseReleased((e) -> {
            final ReshapeEdit edit = new ReshapeEdit(m, a, location.get().subtract(start.get()));

            updateable.update();
        });
    }
}
