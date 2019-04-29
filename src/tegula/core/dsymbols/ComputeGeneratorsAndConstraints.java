/*
 * ComputeGeneratorsAndConstraints.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;
import javafx.util.Pair;
import tegula.geometry.EuclideanGeometry;
import tegula.geometry.HyperbolicGeometry;
import tegula.geometry.SphericalGeometry;
import tegula.tiling.Constraints;
import tegula.tiling.Generators;

/**
 * Symmetry group constraints and generators
 * <p>
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class ComputeGeneratorsAndConstraints {
    /**
     * compute all generators and constraints for the given fundamental domain
     *
     * @param fDomain
     * @param generators
     * @param constraints
     */
    public static void apply(FDomain fDomain, Generators generators, Constraints constraints) {
        final DSymbol ds = fDomain.getDSymbol();
        generators.setSize(ds.size());
        constraints.setSize(ds.size());

        boolean found = false;
        int a0;
        int i0 = 0;
        for (a0 = 1; a0 <= ds.size(); a0++) {
            for (i0 = 0; i0 <= 2; i0++) {
                if (fDomain.isBoundaryEdge(i0, a0)) {
                    found = true;
                    break;
                }
            }
            if (found)
                break;
        }
        if (!found)
            throw new RuntimeException("computeConstraintsAndMore(): Can't find boundary edge");

        int aa = a0;
        int ii = i0;

        int j0 = DSymbol.i(i0);
        int k0 = DSymbol.j(i0);
        int i = i0;
        int j = k0;
        int k = j0;
        int a = a0;

        found = false;
        int count = 0;

        do {
            a0 = a;
            i0 = i;
            j0 = k;
            k0 = j;
            int[] array = nextBoundaryEdge(fDomain, i0, j0, k0, a0);
            i = array[0];
            j = array[1];
            k = array[2];
            a = array[3];

            if (ds.getVij(i0, j0, a0) > 1 || isSplitOrbit(fDomain, i0, j0, a0))
                found = true;
            if (++count > 1000)
                break;
        }
        while (!found);

        boolean circle_boundary = false;
        if (!found) {
            a0 = aa;
            i0 = ii;

            j0 = DSymbol.i(i0);
            k0 = DSymbol.j(i0);
            circle_boundary = true;
        }

        int a00 = a0;
        int i00 = i0;
        int j00 = j0;

        count = 0;
        do {
            int[] array = nextBoundaryEdge(fDomain, i0, j0, k0, a0);
            i = array[0];
            j = array[1];
            k = array[2];
            a = array[3];

            int b = ds.getSi(i, a);
            if (circle_boundary || (ds.getVij(i, j, a) > 1) || isSplitOrbit(fDomain, i, j, a)) {
                count++;
                setConstraint(ds, 0, k, a, constraints, Constraints.ConstraintType.FIXED);
                // compute generator:
                {
                    Point3D pt0 = fDomain.getVertex3D(k, a);
                    Point3D pt1 = fDomain.getVertex3D(j, a);
                    Point3D pt2 = fDomain.getVertex3D(k, b);
                    Point3D pt3 = fDomain.getVertex3D(j, b);

                    boolean keepOrientation = (fDomain.getOrientation(a) != fDomain.getOrientation(b));

                    Transform transform = getTransform(fDomain.getGeometry(), pt0, pt1, pt2, pt3, keepOrientation);
                    generators.set(i, a, transform);
                }

                if (a == b) {
                    Point2D aPt = fDomain.getVertex(k, a);
                    Point2D bPt = fDomain.getVertex(j, a);
                    constraints.setLineConstraint(1, i, a, new Pair<>(aPt, bPt));
                } else
                    constraints.setConstraint(1, i, a, Constraints.ConstraintType.SYMMETRIC_BOUNDARY);
            } else {
                generators.setAgain(i, a); // same transform as previously set
                if (a == b) {
                    setConstraint(ds, 0, k, a, constraints, Constraints.ConstraintType.LINE); // this will use previously set line
                    constraints.setConstraint(1, i, a, Constraints.ConstraintType.LINE);
                } else {
                    setConstraint(ds, 0, k, a, constraints, Constraints.ConstraintType.SYMMETRIC_BOUNDARY);
                    constraints.setConstraint(1, i, a, Constraints.ConstraintType.SYMMETRIC_BOUNDARY);
                }
            }
            a0 = a;
            i0 = i;
            j0 = k;
            k0 = j;
            if (circle_boundary)
                break;
        }
        while (!(a0 == a00 && i0 == i00 && j0 == j00));

        if (count == 0)
            throw new RuntimeException("constraints_and_more(): Can't find boundary section");
    }

    /**
     * set a constraint on all vertices in the appropriate i,j-orbit
     *
     * @param kind
     * @param k
     * @param a
     * @param constraints
     * @param type
     */
    private static void setConstraint(final DSymbol ds, int kind, int k, int a, Constraints constraints, Constraints.ConstraintType type) {
        final int i = DSymbol.i(k);
        final int j = DSymbol.j(k);

        int b = a;

        do {
            constraints.setConstraint(kind, k, b, type);
            b = ds.getSi(i, b);
            constraints.setConstraint(kind, k, b, type);
            b = ds.getSi(j, b);
        }
        while (b != a);
    }

    /**
     * is this orbit split into multiple pieces?
     *
     * @param i
     * @param j
     * @param a
     * @return true, if split
     */
    private static boolean isSplitOrbit(final FDomain fDomain, int i, int j, int a) {
        int b = a;
        int count = 0;

        do {
            if (fDomain.isBoundaryEdge(i, b))
                count++;
            b = fDomain.getDSymbol().getSi(i, b);
            if (fDomain.isBoundaryEdge(j, b))
                count++;
            b = fDomain.getDSymbol().getSi(j, b);
        }
        while (b != a);
        return count > 2;
    }

    /**
     * finds the next boundary edge
     *
     * @param i0
     * @param j0
     * @param k0
     * @param a0
     * @return i, j, k and a for next boundary edge
     */
    private static int[] nextBoundaryEdge(final FDomain fDomain, final int i0, final int j0, final int k0, final int a0) {
        int a00 = a0;

        if (!fDomain.isBoundaryEdge(i0, a00))
            throw new RuntimeException(String.format("nextBoundaryEdge(i0=%d,j0=%d,k0=%d,a0=%d): (a0=%d,i0=%d) not on boundary", i0, j0, k0, a00, a00, i0));

        if (fDomain.isBoundaryEdge(j0, a00)) {
            return new int[]{j0, i0, k0, a00};
        }

        do {
            a00 = fDomain.getDSymbol().getSi(j0, a00);
            if (fDomain.isBoundaryEdge(i0, a00)) {
                return new int[]{i0, j0, k0, a00};
            }
            a00 = fDomain.getDSymbol().getSi(i0, a00);
            if (fDomain.isBoundaryEdge(j0, a00)) {
                return new int[]{j0, i0, k0, a00};
            }
        }
        while (a00 != a0);

        throw new RuntimeException(String.format("nextBoundaryEdge(i0=%d,j0=%d,k0=%d,a0=%d): %s", i0, j0, k0, a00, "Can't find other end of boundary orbit"));
    }

    /**
     * get the transform that maps a1-b1 to a2-b2, keeping orientation, if desired
     *
     * @param geom
     * @param a1
     * @param b1
     * @param a2
     * @param b2
     * @param keepOrientation
     * @return transform
     */
    public static Transform getTransform(Geometry geom, Point3D a1, Point3D b1, Point3D a2, Point3D b2, boolean keepOrientation) {
        switch (geom) {
            default:
            case Euclidean:
                return EuclideanGeometry.createTransform(a1, b1, a2, b2, keepOrientation);
            case Spherical: {
                return SphericalGeometry.createTransform(a1, b1, a2, b2, keepOrientation);
            }
            case Hyperbolic:
                return HyperbolicGeometry.createTransform(a1, b1, a2, b2, keepOrientation);
        }
    }
}
