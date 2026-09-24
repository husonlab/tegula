/*
 * ReshapeUtilities.java Copyright (C) 2023 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.fdomaineditor;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;
import tegula.tiling.Generators;
import tegula.tilingeditor.StraightenEdges;

import java.util.Arrays;
import java.util.BitSet;

/**
 * updates fundamental domain after interactive reshape event
 * Daniel Huson and Ruediger Zeller, 4.2019
 */
public class ReshapeUtilities {
    public enum Type {Vertex, EdgeCenter, ChamberCenter}

    /**
     * reshape fundamental domain after translation of point of given type
     *
	 */
    public static Point2D resetShape(FDomain fDomain, double deltaX, double deltaY, Type type, int k, int a, double factor, boolean constrain) {
        final DSymbol ds = fDomain.getDSymbol();
        final Generators generators = fDomain.getGenerators();
        Point2D transVector = new Point2D(deltaX, deltaY);

        if (!constrain) {
            final Translate translate = new Translate(transVector.getX(), transVector.getY());
            switch (type) {
                case Vertex:
                    fDomain.setVertex(translate.transform(fDomain.getVertex(k, a)), k, a);
                    break;
                case EdgeCenter:
                    fDomain.setEdgeCenter(translate.transform(fDomain.getEdgeCenter(k, a)), k, a);
                    break;
                case ChamberCenter:
                    fDomain.setChamberCenter(translate.transform(fDomain.getChamberCenter(a)), a);
                    break;
            }
        } else {
            switch (type) {
                case Vertex: {
                    if (k == 2)
                        break; // interaction implemented below for k=2

                    final int i = DSymbol.i(k);
                    final int j = DSymbol.j(k);
                    final int length = ds.computeOrbitLength(i, j, a);

                    // Add restrictions in Euclidean case:
                    if (fDomain.getGeometry() == Geometry.Euclidean) {
                        if (k == 1) {
                            transVector = add1Restriction(fDomain, transVector.getX(), transVector.getY(), a); // Restrictions for 1-handles
                        } else {
                            transVector = add0Restriction(fDomain, transVector.getX(), transVector.getY(), a, length); // Restrictions for 0-handles
                        }
                    }

                    transVector = addMirrorRestriction(fDomain, transVector.getX(), transVector.getY(), length, k, a); // Mirror axis restriction

                    final Translate translate = new Translate(transVector.getX(), transVector.getY());

                    // Translate Point of type k in chamber a
                    fDomain.setVertex(translate.transform(fDomain.getVertex(k, a)), k, a);

                    final Point3D pt0 = Tools.map2Dto3D(fDomain.getGeometry(), fDomain.getVertex(k, a));

                    // Consider all points in orbit of a (especially if chamber contains boundary edges)
                    for (int z = 1; z <= length - 1; z++) {
                        // If i=(1-k) -edge is on boundary
                        if (fDomain.isBoundaryEdge(i, a) && ds.getSi(i, a) != a) {
                            final Transform g = generators.get(i, a);
                            final Point3D pt1 = g.transform(pt0);
                            fDomain.setVertex(Tools.map3Dto2D(fDomain.getGeometry(), pt1), k, ds.getSi(i, a));
                        }
                        a = ds.getSi(i, a);

                        // If 2-edge is on boundary
                        if (fDomain.isBoundaryEdge(j, a) && ds.getSi(j, a) != a) {
                            final Transform g = generators.get(j, a);
                            final Point3D pt1 = g.transform(pt0);
                            fDomain.setVertex(Tools.map3Dto2D(fDomain.getGeometry(), pt1), k, ds.getSi(j, a));
                        }
                        a = ds.getSi(j, a);
                    }
                }
                case EdgeCenter: {
                    if (k != 2)
                        break; // only allow reshaping of center of proper tiling edge
                    if (fDomain.getGeometry() == Geometry.Euclidean) { // Add restrictions in Euclidean case
                        transVector = add2Restriction(fDomain, deltaX, deltaY, a, factor);
                    }
                    if (ds.getSi(k, a) == a) break;
                    final Translate translate = new Translate(transVector.getX(), transVector.getY());


                    fDomain.setEdgeCenter(translate.transform(fDomain.getEdgeCenter(k, a)), k, a);
                    final Point3D pt0 = Tools.map2Dto3D(fDomain.getGeometry(), fDomain.getEdgeCenter(k, a));

                    if (fDomain.isBoundaryEdge(k, a)) {
                        final Transform g = generators.get(k, a);
                        final Point3D pt1 = g.transform(pt0);
                        final Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt1);
                        fDomain.setEdgeCenter(pt2d, k, ds.getS2(a));
                    }
                    break;
                }
                case ChamberCenter:
            }

            // Straighten 0- and 1-edges
            StraightenEdges.straighten01Edges(fDomain);
        }
        return transVector.multiply(factor);
    }

    /**
     * Add restrictions to translation in case that handle lies on exactly one mirror axis
     *
	 */
    private static Point2D addMirrorRestriction(final FDomain fDomain, double deltaX, double deltaY, int orbitLength, int k, int a) {
        final DSymbol ds = fDomain.getDSymbol();

        final int m = fDomain.size();
        // Count number of chambers lying in (1-type)-2-orbit containing flag
        final BitSet visited = new BitSet(m);
        ds.markOrbit(1 - k, 2, a, visited);
        int numberOfChambers = 0;
        for (int d = 1; d <= m; d++) {
            if (visited.get(d)) {
                numberOfChambers++;
            }
        }

        Transform g = new Translate();
        if (2 * orbitLength / numberOfChambers == 2) { // Condition for exactly one mirror axis
            // Calculate reflection g:
            while (a <= m) {
                if (fDomain.isBoundaryEdge(1 - k, a) && ds.getSi(1 - k, a) == a) { // Mirror axis = (1-k)-edge
                    g = fDomain.getGenerators().get(1 - k, a);
                    // System.out.println(1-k + " und " + a);
                    break;
                } else {
                    a = ds.getSi(1 - k, a);
                }
                if (fDomain.isBoundaryEdge(2, a) && ds.getS2(a) == a) { // Mirror axis = 2-edge
                    g = fDomain.getGenerators().get(2, a);
                    // System.out.println(2 + " und " + a);
                    break;
                } else {
                    a = ds.getSi(2, a);
                }
            }

            // Restrict mouse movement to mirror axis
            Point2D pt2d = fDomain.getVertex(k, a);
            pt2d = pt2d.add(deltaX, deltaY);
            Point3D pt3d = Tools.map2Dto3D(fDomain.getGeometry(), pt2d);
            pt3d = Tools.midpoint3D(fDomain.getGeometry(), pt3d, g.transform(pt3d)); // Position on mirror axis in 3D-model

            Point2D newPos = Tools.map3Dto2D(fDomain.getGeometry(), pt3d); // Position on mirror axis in 2d-model
            Point2D oldPos = fDomain.getVertex(k, a);

            // Direction of mouse movement
            deltaX = newPos.getX() - oldPos.getX();
            deltaY = newPos.getY() - oldPos.getY();
        }

        return new Point2D(deltaX, deltaY);
    }

    /**
     * Add restrictions for mouse movement to prevent overlaps
     *
     * @return new direction
     */
    private static Point2D add2Restriction(final FDomain fDomain, double deltaX, double deltaY, int a, double factor) {
        final DSymbol ds = fDomain.getDSymbol();

        // Restrict movement for 2-edge-handles
        final Point2D transVec = new Point2D(deltaX, deltaY);
        final Transform invGen;
        if (fDomain.isBoundaryEdge(2, a)) {
            invGen = fDomain.getGenerators().get(2, ds.getS2(a));
        } else
            invGen = new Translate();

        // There exist 4 restrictions. Each restricting line / plane is of the form <x,n> = c.
        // R - directions of line, N - normal vector, c - coordinate
        Point3D[] R = new Point3D[4], N = new Point3D[4], Q = new Point3D[4]; // Save normal vectors N, directions R and position vectors P of restricting lines
        double[] c = new double[4]; // Save coordinates of the 4 restricting lines / planes

        // Define restriction 0 and 1 for flag a
        R[0] = fDomain.getVertex3D(2, a).subtract(fDomain.getVertex3D(0, a));
        N[0] = new Point3D(R[0].getY(), -R[0].getX(), 0);
        Q[0] = fDomain.getVertex3D(0, a);
        c[0] = fDomain.getVertex3D(0, a).dotProduct(N[0]);
        R[1] = fDomain.getVertex3D(2, a).subtract(fDomain.getVertex3D(1, a));
        N[1] = new Point3D(R[1].getY(), -R[1].getX(), 0);
        Q[1] = fDomain.getVertex3D(1, a);
        c[1] = fDomain.getVertex3D(1, a).dotProduct(N[1]);

        // Define restrictions 2 and 3 for flag sigma2(a):
        Affine invGenMat = new Affine(invGen.getMxx(), invGen.getMxy(), invGen.getMxz(), 0, invGen.getMyx(), invGen.getMyy(), invGen.getMyz(), 0, invGen.getMzx(), invGen.getMzy(), invGen.getMzz(), 0);
        R[2] = fDomain.getVertex3D(2, ds.getS2(a)).subtract(fDomain.getVertex3D(0, ds.getS2(a)));
        R[2] = invGenMat.transform(R[2]);
        N[2] = new Point3D(R[2].getY(), -R[2].getX(), 0);
        Q[2] = fDomain.getVertex3D(0, a);
        c[2] = fDomain.getVertex3D(0, a).dotProduct(N[2]);
        R[3] = fDomain.getVertex3D(2, ds.getS2(a)).subtract(fDomain.getVertex3D(1, ds.getS2(a)));
        R[3] = invGenMat.transform(R[3]);
        N[3] = new Point3D(R[3].getY(), -R[3].getX(), 0);
        Q[3] = fDomain.getVertex3D(1, a);
        c[3] = fDomain.getVertex3D(1, a).dotProduct(N[3]);

        Point3D firstPos = (fDomain.getVertex3D(0, a).add(fDomain.getVertex3D(1, a))).multiply(0.5); // Midpoint of 0- and 1- vertex
        Point3D oldPos = fDomain.getEdgeCenter3D(2, a); // Actual position of handle

        return checkRestriction(transVec, R, N, Q, c, firstPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }

    private static Point2D add1Restriction(final FDomain fDomain, double deltaX, double deltaY, int flag) {
        final DSymbol ds = fDomain.getDSymbol();

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
                gen = gen.createConcatenation(fDomain.getGenerators().get(2, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }

            R[2 * i + 1] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2 * i + 1] = genMat.transform(R[2 * i + 1]);
            N[2 * i + 1] = new Point3D(R[2 * i + 1].getY(), -R[2 * i + 1].getX(), 0);
            Q[2 * i + 1] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2 * i + 1] = N[2 * i + 1].dotProduct(Q[2 * i + 1]);

            flag = ds.getS0(flag);
            if (fDomain.isBoundaryEdge(0, flag)) {
                gen = gen.createConcatenation(fDomain.getGenerators().get(0, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }
        }

        Point3D apt = fDomain.getEdgeCenter3D(2, ds.getS0(flag));
        if (fDomain.isBoundaryEdge(0, flag)) {
            gen = fDomain.getGenerators().get(0, ds.getS0(flag));
            apt = gen.transform(apt);
        }

        Point3D firstPos = apt.midpoint(fDomain.getEdgeCenter3D(2, flag));
        Point3D oldPos = fDomain.getVertex3D(1, flag);

        return checkRestriction(transVec, R, N, Q, c, firstPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }

    private static Point2D add0Restriction(final FDomain fDomain, double deltaX, double deltaY, int flag, int length) {
        final DSymbol ds = fDomain.getDSymbol();

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
                gen = gen.createConcatenation(fDomain.getGenerators().get(2, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }

            R[2 * i + 1] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2 * i + 1] = genMat.transform(R[2 * i + 1]);
            N[2 * i + 1] = new Point3D(R[2 * i + 1].getY(), -R[2 * i + 1].getX(), 0);
            Q[2 * i + 1] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2 * i + 1] = N[2 * i + 1].dotProduct(Q[2 * i + 1]);

            flag = ds.getS1(flag);
            if (fDomain.isBoundaryEdge(1, flag)) {
                gen = gen.createConcatenation(fDomain.getGenerators().get(1, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }
        }

        Point3D apt = fDomain.getEdgeCenter3D(2, ds.getS0(flag));
        if (fDomain.isBoundaryEdge(1, flag)) {
            gen = fDomain.getGenerators().get(1, ds.getS1(flag));
            apt = gen.transform(apt);
        }

        Point3D oldPos = fDomain.getVertex3D(0, flag);

        return checkRestriction(transVec, R, N, Q, c, oldPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }

    /**
     * Change direction of mouse movement for handle when restrictions are broken
     *
     * @return new direction
     */
    private static Point2D checkRestriction(Point2D transVec, Point3D[] R, Point3D[] N, Point3D[] Q, double[] c, Point3D firstPos, Point3D oldPos) {
        int length = N.length;

        // Change direction of translation if restrictions are broken
        Transform t = new Translate(transVec.getX() * 100, transVec.getY() * 100); // Original translation vector coming from mouse movement (in shapeHandler)
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

        // Loop works as long as newPos does not fulfill restrictions but at most 1000 times
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
        return new Point2D(transVec3d.getX(), transVec3d.getY()).multiply(0.01);
    }

    private static boolean largerOrEqual(double a, double b) {
        return a >= b;
    }

    /**
     * carries the shape of a reshaped fundamental domain over to a freshly computed one, after the rotational
     * degrees of the Delaney symbol have been changed.
     * <p>
     * Changing a rotational degree changes only the m-values of the symbol, not its size or its involutions,
     * so the chambers of the two domains correspond one-to-one. The vertices cannot be carried over: where
     * they lie is dictated by the new angles, and possibly in a different geometry altogether, so they are
     * left as computed. What is carried over is how far the user had pulled each edge center and each chamber
     * center away from where the program itself puts them, which is what one sees as the shape of the domain.
     * <p>
     * That displacement is measured against an unreshaped domain for the old symbol, rather than against a
     * formula for where an edge center "should" sit: in a curved geometry the domain is solved for, and its
     * edge centers land neither at the midpoint of their two vertices' coordinates nor at the geodesic
     * midpoint. Measuring canonical-to-canonical keeps an unreshaped domain exactly as computed, so that the
     * triangulation of the chambers stays where it belongs. The displacement is expressed as a multiple of
     * the length of the edge it belongs to, so that it also survives the change of scale that comes with a
     * change of geometry.
     *
     * @param reshaped coordinates of the reshaped domain, as returned by FDomain.getCoordinates()
     * @param straight coordinates of an unreshaped domain for the same, old, Delaney symbol
     * @param target   the freshly computed domain for the new symbol, reshaped in place
     * @return true if the shape was carried over
     */
    public static boolean transferShape(Point2D[][] reshaped, Point2D[][] straight, FDomain target) {
        if (reshaped == null || straight == null || target == null)
            return false;
        final int size = target.getDSymbol().size();
        if (reshaped.length != size + 1 || straight.length != size + 1)
            return false; // not the same chambers, so there is nothing to map onto

        // the positions to measure the new shape against, taken before anything is moved: the two chambers
        // either side of an edge share the one stored edge center, so moving it for the first of them would
        // otherwise leave the second measuring against a point that has already been moved
        final Point2D[][] canonical = target.getCoordinates();

        for (int a = 1; a <= size; a++) {
            final Point2D[] was = reshaped[a], is = straight[a];
            if (was == null || is == null)
                continue;

            for (int i = 0; i <= 2; i++) {
                // the i-edge runs between the two vertices other than i. It is shared with the neighbouring
                // chamber, which spans it by those very same two vertices, so both chambers place its center
                // at the same point and the domain does not tear open along the edge
                final int j = (i == 0 ? 1 : 0);
                final int k = (i == 2 ? 1 : 2);
                final double[] bend = relativeToEdge(is[j], is[k], is[3 + i], was[3 + i]);
                if (bend != null) {
                    final Point2D point = alongEdge(canonical[a][j], canonical[a][k], canonical[a][3 + i], bend);
                    if (point != null && isFinite(point))
                        target.setEdgeCenter(point, i, a);
                }
            }

            final double[] offset = relativeToEdge(is[0], is[1], is[6], was[6]);
            if (offset != null) {
                final Point2D point = alongEdge(canonical[a][0], canonical[a][1], canonical[a][6], offset);
                if (point != null && isFinite(point))
                    target.setChamberCenter(point, a);
            }
        }
        return true;
    }

    /**
     * where a point sits relative to a base point on the edge from a to b: how far it is displaced along the
     * edge and how far off it, both as multiples of the edge's length so that they are free of any unit and
     * of any scale
     */
    private static double[] relativeToEdge(Point2D a, Point2D b, Point2D base, Point2D point) {
        if (a == null || b == null || base == null || point == null)
            return null;
        final Point2D along = b.subtract(a);
        final double lengthSquared = along.dotProduct(along);
        if (!(lengthSquared > 0))
            return null;
        final Point2D across = new Point2D(-along.getY(), along.getX());
        final Point2D offset = point.subtract(base);
        final double[] result = {offset.dotProduct(along) / lengthSquared, offset.dotProduct(across) / lengthSquared};
        return (Double.isFinite(result[0]) && Double.isFinite(result[1]) ? result : null);
    }

    /**
     * the inverse of relativeToEdge: puts the point back, relative to a new edge and base point
     */
    private static Point2D alongEdge(Point2D a, Point2D b, Point2D base, double[] offset) {
        if (a == null || b == null || base == null)
            return null;
        final Point2D along = b.subtract(a);
        final Point2D across = new Point2D(-along.getY(), along.getX());
        return base.add(along.multiply(offset[0])).add(across.multiply(offset[1]));
    }

    private static boolean isFinite(Point2D point) {
        return Double.isFinite(point.getX()) && Double.isFinite(point.getY());
    }
}
