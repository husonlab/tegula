/*
 *  Copyright (C) 2018 University of Tuebingen
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
package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Pair;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.core.dsymbols.OrbifoldGroupName;
import tiler.main.Document;
import tiler.main.TilingStyle;
import tiler.util.JavaFXUtils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * a tiling
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class Tiling {
    boolean verbose = false;

    private final DSymbol ds;
    private final String groupName;
    private final FDomain fDomain;

    private final FundamentalDomain fundamentalDomain = new FundamentalDomain();

    private final Transforms generators;
    private final Constraints constraints;

    private OctTree keptHyperbolicCopy;
    private QuadTree keptEuclideanCopy = new QuadTree();

    private Point3D refPointHyperbolic = new Point3D(0, 0, 1);
    private Point3D refPointEuclidean = new Point3D(1, 1, 0);

    private final Group recycler = new Group();
    private Transform transformRecycled = new Translate();
    private Group euclideanFund = new Group();
    private Group hyperbolicFund = new Group();

    private Group handles = new Group();

    private final int[] flag2vert;
    private final int[] flag2edge;
    private final int[] flag2tile;

    private final int[] vert2flag;
    private final int[] edge2flag;
    private final int[] tile2flag;

    private final int numbVert;
    private final int numbEdge;
    private final int numbTile;

    private boolean isBreak = false;
    private int numberOfCopies = 0;

    private double tolerance = 0.0;
    private int referenceChamberIndex = 0;

    /**
     * constructor
     *
     * @param ds
     */
    public Tiling(DSymbol ds) {
        this.ds = ds;
        this.groupName = OrbifoldGroupName.getGroupName(ds);
        this.fDomain = new FDomain(ds);
        this.constraints = new Constraints(ds.size());
        this.generators = new Transforms(ds.size());

        flag2vert = new int[ds.size() + 1];
        numbVert = ds.countOrbits(1, 2);
        vert2flag = new int[numbVert + 1];

        for (int a = 1, count = 1; a <= ds.size(); a = ds.nextOrbit(1, 2, a, flag2vert, count++)) // this also sets flag2vert
            vert2flag[count] = a;
        flag2edge = new int[ds.size() + 1];
        numbEdge = ds.countOrbits(0, 2);
        edge2flag = new int[numbEdge + 1];

        for (int a = 1, count = 1; a <= ds.size(); a = ds.nextOrbit(0, 2, a, flag2edge, count++)) // this also sets flag2vert
            edge2flag[count] = a;
        flag2tile = new int[ds.size() + 1];
        numbTile = ds.countOrbits(0, 1);
        tile2flag = new int[numbTile + 1];

        for (int a = 1, count = 1; a <= ds.size(); a = ds.nextOrbit(0, 1, a, flag2tile, count++)) // this also sets flag2vert
            tile2flag[count] = a;
    }

    /**
     * set the reference chamber index and the corresponding tolerance
     *
     * @param referenceChamberIndex
     */
    public void setReferenceChamberIndex(int referenceChamberIndex) {
        this.referenceChamberIndex = referenceChamberIndex;
        tolerance = computeTolerance(referenceChamberIndex);
    }

    public double getTolerance() {
        return tolerance;
    }

    public int getReferenceChamberIndex() {
        return referenceChamberIndex;
    }

    /**
     * set up the constraints and symmetry group generators
     */
    public void computeConstraintsAndGenerators() {
        generators.getTransforms().clear();

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

        if (verbose)
            System.err.println(String.format("Init. boundary edge: a0 %d i0 %d\n", a0, i0));

        int aa = a0;
        int ii = i0;

        int j0 = computeLowIndex(i0);
        int k0 = computeHighIndex(i0);
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
            int[] array = nextBoundaryEdge(i0, j0, k0, a0);
            i = array[0];
            j = array[1];
            k = array[2];
            a = array[3];

            if (ds.getVij(i0, j0, a0) > 1 || isSplitOrbit(i0, j0, a0))
                found = true;
            if (++count > 1000)
                break;
        }
        while (!found);

        if (!found && verbose)
            System.err.println("Can't find boundary corner");

        boolean circle_boundary = false;
        if (!found) {
            a0 = aa;
            i0 = ii;

            j0 = computeLowIndex(i0);
            k0 = computeHighIndex(i0);
            circle_boundary = true;
        }

        if (verbose)
            System.err.println(String.format("Boundary edge before corner: a0 %d i0 %d j0 %d k0 %d\n", a0, i0, j0, k0));

        int a00 = a0;
        int i00 = i0;
        int j00 = j0;

        count = 0;
        do {
            int[] array = nextBoundaryEdge(i0, j0, k0, a0);
            i = array[0];
            j = array[1];
            k = array[2];
            a = array[3];

            int b = ds.getSi(i, a);
            if (circle_boundary || (ds.getVij(i, j, a) > 1) || isSplitOrbit(i, j, a)) {
                if (verbose)
                    System.err.println(String.format("Starting new section: vij: %d split_orbit: %d\n",
                            ds.getVij(i, j, a), isSplitOrbit(i, j, a) ? 1 : 0));
                count++;
                setConstraint(0, k, a, constraints, Constraints.ConstraintType.FIXED);
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

                if (verbose)
                    System.err.println(String.format("New section: a %d i %d\n", a, i));

            } else {
                generators.setAgain(i, a); // same transform as previously set
                if (a == b) {
                    setConstraint(0, k, a, constraints, Constraints.ConstraintType.LINE); // this will use previously set line
                    constraints.setConstraint(1, i, a, Constraints.ConstraintType.LINE);
                } else {
                    setConstraint(0, k, a, constraints, Constraints.ConstraintType.SYMMETRIC_BOUNDARY);
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

        if (verbose)
            System.err.println(String.format("Found %d boundary sections\n", count));
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
    private void setConstraint(int kind, int k, int a, Constraints constraints, Constraints.ConstraintType type) {
        final int i = computeLowIndex(k);
        final int j = computeHighIndex(k);

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
    private boolean isSplitOrbit(int i, int j, int a) {
        int b = a;
        int count = 0;

        do {
            if (fDomain.isBoundaryEdge(i, b))
                count++;
            b = ds.getSi(i, b);
            if (fDomain.isBoundaryEdge(j, b))
                count++;
            b = ds.getSi(j, b);
        }
        while (b != a);
        return count > 2;
    }

    /**
     * computes the smallest integer between 0 and 2 that does not equal i
     *
     * @param i
     * @return smallest index
     */
    public static int computeLowIndex(final int i) {
        return i > 0 ? 0 : 1;
    }

    /**
     * computes the largest integer between 0 and 2 that does not equal i
     *
     * @param i
     * @return smallest index
     */
    public static int computeHighIndex(final int i) {
        return i < 2 ? 2 : 1;
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
    private int[] nextBoundaryEdge(final int i0, final int j0, final int k0, final int a0) {
        int a00 = a0;

        if (!fDomain.isBoundaryEdge(i0, a00))
            throw new RuntimeException(String.format("nextBoundaryEdge(i0=%d,j0=%d,k0=%d,a0=%d): (a0=%d,i0=%d) not on boundary",
                    i0, j0, k0, a00, a00, i0));

        if (fDomain.isBoundaryEdge(j0, a00)) {
            return new int[]{j0, i0, k0, a00};
        }

        do {
            a00 = ds.getSi(j0, a00);
            if (fDomain.isBoundaryEdge(i0, a00)) {
                return new int[]{i0, j0, k0, a00};
            }
            a00 = ds.getSi(i0, a00);
            if (fDomain.isBoundaryEdge(j0, a00)) {
                return new int[]{j0, i0, k0, a00};
            }
        }
        while (a00 != a0);

        throw new RuntimeException(String.format("nextBoundaryEdge(i0=%d,j0=%d,k0=%d,a0=%d): %s",
                i0, j0, k0, a00, "Can't find other end of boundary orbit"));
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
            case Spherical:
                return SphericalGeometry.createTransform(a1, b1, a2, b2, keepOrientation);
            case Hyperbolic:
                return HyperbolicGeometry.createTransform(a1, b1, a2, b2, keepOrientation);
        }
    }

    public String getGroupName() {
        return groupName;
    }

    /**
     * gets the status line
     *
     * @return status line
     */
    public String getStatusLine() {
        return String.format("Tiling: %d.%d  Vertices: %d  Edges: %d  Tiles: %d  Symmetry group: %s",
                ds.getNr1(), ds.getNr2(), numbVert, numbEdge, numbTile, getGroupName());
    }

//----------------------------------------------------------------------------------------------------------------------

    /**
     * Computes tolerance for rounding errors. Tolerance depends on shape of fundamental domain.
     *
     * @return tolerance
     */
    public double computeTolerance(int referenceChamberIndex) {
        final Point3D refPoint;
        if (fDomain.getGeometry() == Geometry.Euclidean) {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        } else {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex).multiply(0.01);
        }
        double tolerance = 100;
        for (Transform g : generators.getTransforms()) {
            double dist = Tools.distance(fDomain.getGeometry(), g.transform(refPoint), refPoint);
            if (dist < tolerance) {
                tolerance = dist;
            }
        }
        return 0.8 * tolerance;
    }

//----------------------------------------------------------------------------------------------------------------------

    /**
     * create the set of tiles to be shown in spherical case
     *
     * @return tiles
     */
    public Group createTilingSpherical(TilingStyle tilingStyle) {
        handles.getChildren().clear();

        fundamentalDomain.buildFundamentalDomain(ds, fDomain, tilingStyle);

        final Group all = new Group();
        final Group fund = new Group();
        if (tilingStyle.isShowFaces())
            fund.getChildren().add(fundamentalDomain.getTiles());
        if (tilingStyle.isShowAllChambers())
            fund.getChildren().add(fundamentalDomain.getChambers());
        if (tilingStyle.isShowBands())
            fund.getChildren().add(fundamentalDomain.getBands());

        if (tilingStyle.isShowOtherStuff())
            fund.getChildren().add(fundamentalDomain.getOtherStuff());

        if (tilingStyle.isShowSymmetryIcons())
            fund.getChildren().add(fundamentalDomain.getSymmetryIcons());

        all.getChildren().add(fund);

        // Make copies of fundamental domain.
        final OctTree seen = new OctTree();
        final Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex).multiply(0.01); // refPoint lies on unit sphere
        seen.insert(fDomain.getGeometry(), refPoint, tolerance); //root node of OctTree is point of reference.

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());
        for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
            Point3D genRef = g.transform(refPoint);
            if (seen.insert(fDomain.getGeometry(), genRef, tolerance)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
                final Group group2 = JavaFXUtils.copyGroup(fund);
                group2.getTransforms().add(g);
                all.getChildren().add(group2);
            }
        }

        while (queue.size() > 0) {
            final Transform t = queue.poll(); // remove t from queue

            for (Transform g : generators.getTransforms()) {
                Transform tg = t.createConcatenation(g);
                Point3D bpt = tg.transform(refPoint);
                if (seen.insert(fDomain.getGeometry(), bpt, tolerance)) {
                    final Group group2 = JavaFXUtils.copyGroup(fund);
                    group2.getTransforms().add(tg);
                    all.getChildren().add(group2);
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                bpt = gt.transform(refPoint);
                if (seen.insert(fDomain.getGeometry(), bpt, tolerance)) {
                    Group group2 = JavaFXUtils.copyGroup(fund);
                    group2.getTransforms().add(gt);
                    all.getChildren().add(group2);
                    queue.add(gt);
                }
            }
        }

        // only want one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.getChildren().add(fundamentalDomain.getHandles());

        if (tilingStyle.isShowHandles())
            all.getChildren().add(fundamentalDomain.getHandles());

        return all;
    }

    //----------------------------------------------------------------------------------------------------------------------

    /**
     * create tiling in hyperbolic case
     *
     * @param maxDist
     * @return group
     */
    public Group createTilingHyperbolic(boolean drawFundamentalDomainOnly, double maxDist, TilingStyle tilingStyle) {
        //System.err.println("Create Hyperbolic Tiling");

        handles.getChildren().clear();

        //Add all generators
        computeConstraintsAndGenerators();

        //System.out.println(refPointHyperbolic);

        final Group group = new Group();
        final Group fund = new Group();

        refPointHyperbolic = fDomain.getChamberCenter3D(referenceChamberIndex).multiply(0.01);

        if (!translateOrIncreaseTiling()) { // need to recompute fundamental domain
            fundamentalDomain.buildFundamentalDomain(ds, fDomain, tilingStyle);
            if (tilingStyle.isShowFaces())
                fund.getChildren().add(fundamentalDomain.getTiles());
            if (tilingStyle.isShowAllChambers())
                fund.getChildren().add(fundamentalDomain.getChambers());
            if (tilingStyle.isShowBands())
                fund.getChildren().add(fundamentalDomain.getBands());
            if (tilingStyle.isShowOtherStuff())
                fund.getChildren().add(fundamentalDomain.getOtherStuff());

            if (tilingStyle.isShowSymmetryIcons())
                fund.getChildren().add(fundamentalDomain.getSymmetryIcons());

            fund.setRotationAxis(refPointHyperbolic);
            fund.getTransforms().add(new Translate());
            setHyperbolicFund(fund);
        }

        if (!drawFundamentalDomainOnly) {
            // Make copies of fundamental domain.
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

            final OctTree seen = new OctTree();

            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D genRef = g.transform(refPointHyperbolic);
                if (seen.insert(fDomain.getGeometry(), genRef, tolerance)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
                    if (makeCopyHyperbolic(genRef)) {
                        if (translateOrIncreaseTiling()) {
                            useRecycler(group, g, genRef, getHyperbolicFund());
                        } else {
                            generateNewCopy(group, g, genRef, fund);
                        }
                    }
                }
            }

            int countChildren = 0;
            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (translateOrIncreaseTiling() && getNumberOfCopies() > 0 && countChildren >= 1.5 * getNumberOfCopies()) {
                    setBreak(true);
                    System.out.println(countChildren + " children and " + getNumberOfCopies() + " copies");
                    break;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) {
                    Transform tg = t.createConcatenation(g);
                    Point3D bpt = tg.transform(refPointHyperbolic);
                    if (seen.insert(fDomain.getGeometry(), bpt, tolerance) && bpt.getZ() < maxDist) {
                        countChildren++;
                        queue.add(tg);
                        if (makeCopyHyperbolic(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(group, tg, bpt, getHyperbolicFund());
                            } else {
                                generateNewCopy(group, tg, bpt, fund);
                            }
                        }
                    }

                    Transform gt = g.createConcatenation(t);
                    bpt = gt.transform(refPointHyperbolic);
                    if (seen.insert(fDomain.getGeometry(), bpt, tolerance) && bpt.getZ() < maxDist) {
                        countChildren++;
                        queue.add(gt);
                        if (makeCopyHyperbolic(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(group, gt, bpt, getHyperbolicFund());
                            } else {
                                generateNewCopy(group, gt, bpt, fund);
                            }
                        }
                    }
                }
            }
        }

        // only what one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            group.getChildren().add(fundamentalDomain.getChambers());

        if (tilingStyle.isShowHandles())
            group.getChildren().add(fundamentalDomain.getHandles());

        return group;
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * create tiling in Euclidean case
     *
     * @param windowCorner
     * @param width
     * @param height
     * @return group
     */
    public Group createTilingEuclidean(Document doc, boolean drawFundamentalDomainOnly, Point3D windowCorner, double width, double height, TilingStyle tilingStyle) {

        //Add all generators
        computeConstraintsAndGenerators();

        //Add handles
        handles.getChildren().setAll(((new ReshapeManager(doc).createHandles())));

        //Calculation of point of reference:
        refPointEuclidean = fDomain.getChamberCenter3D(referenceChamberIndex); // Reference point of actual fundamental domain

        final Group all = new Group();
        final Group fund = new Group();

        if (!translateOrIncreaseTiling()) { // need to recompute fundamental domain
            fundamentalDomain.buildFundamentalDomain(ds, fDomain, tilingStyle);

            if (tilingStyle.isShowFaces())
                fund.getChildren().add(fundamentalDomain.getTiles());
            if (tilingStyle.isShowAllChambers())
                fund.getChildren().add(fundamentalDomain.getChambers());
            if (tilingStyle.isShowBands())
                fund.getChildren().add(fundamentalDomain.getBands());
            if (tilingStyle.isShowOtherStuff())
                fund.getChildren().add(fundamentalDomain.getOtherStuff());

            if (tilingStyle.isShowSymmetryIcons())
                fund.getChildren().add(fundamentalDomain.getSymmetryIcons());

            all.getChildren().add(fund);
            euclideanFund = fund; // Saves the original fundamental domain
        }

        if (makeCopyEuclidean(refPointEuclidean)) { // Fill empty space with tiles
            fund.getTransforms().add(new Translate()); // Add transform (= identity)
            fund.setRotationAxis(refPointEuclidean); // Reference point of fundamental domain
        }

        if (!drawFundamentalDomainOnly) {
            final QuadTree seen = new QuadTree(); // Saves reference points of tiles
            seen.insert(refPointEuclidean.getX(), refPointEuclidean.getY(), tolerance); // Insert reference point of fDomain

            // Saves transforms for copies
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms()); // Add generators

            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D genRef = g.transform(refPointEuclidean); // Reference point for new copy
                if (isInRangeEuclidean(genRef, windowCorner, width, height) && seen.insert(genRef.getX(), genRef.getY(), tolerance)) { // Checks whether reference point is in valid range and if it is in QuadTree "seen". Adds it if not.
                    if (makeCopyEuclidean(genRef)) { // Checks whether copy fills empty space after translation of tiles
                        if (translateOrIncreaseTiling()) { // Translate mode of tiling
                            useRecycler(all, g, genRef, euclideanFund);
                        } else {
                            generateNewCopy(all, g, genRef, fund);
                        }
                    }
                }
            }

            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (translateOrIncreaseTiling() && queue.size() >= 1.5 * getNumberOfCopies()) {
                    setBreak(true);
                    break;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) { // Creates new transforms for copies
                    Transform tg = t.createConcatenation(g);
                    Point3D bpt = tg.transform(refPointEuclidean); // Reference point corresponding to transform tg

                    if (isInRangeEuclidean(bpt, windowCorner, width, height) && seen.insert(bpt.getX(), bpt.getY(), tolerance)) {
                        queue.add(tg);
                        if (makeCopyEuclidean(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(all, tg, bpt, euclideanFund);
                            } else {
                                generateNewCopy(all, tg, bpt, fund);
                            }
                        }
                    }

                    Transform gt = g.createConcatenation(t);
                    bpt = gt.transform(refPointEuclidean);

                    if (isInRangeEuclidean(bpt, windowCorner, width, height) && seen.insert(bpt.getX(), bpt.getY(), tolerance)) {
                        queue.add(gt);
                        if (makeCopyEuclidean(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(all, gt, bpt, euclideanFund);
                            } else {
                                generateNewCopy(all, gt, bpt, fund);
                            }
                        }
                    }
                }
            }
        }

        // only what one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.getChildren().add(fundamentalDomain.getChambers());

        if (tilingStyle.isShowHandles())
            all.getChildren().add(fundamentalDomain.getHandles());

        return all;
    }

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Euclidean case: Shifts back fundamental domain if out of bounds
     *
     * @param height
     * @param width
     * @param windowCorner
     * @return transform
     */
    public Transform calculateBackShiftEuclidean(Point3D windowCorner, double width, double height) {

        if (width < 450) {
            width = 450;
        }
        if (height < 450) {
            height = 450;
        }

        //Add all generators
        computeConstraintsAndGenerators();

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        final QuadTree seen = new QuadTree();
        seen.insert(refPoint.getX(), refPoint.getY(), tolerance);

        Transform backShift = new Translate(), t;
        Point3D point = refPoint, apt = refPoint;
        Point3D midpoint = new Point3D(windowCorner.getX() + width / 2, windowCorner.getY() + height / 2, 0);
        double d = point.distance(midpoint);

        for (Transform g : generators.getTransforms()) {
            point = g.transform(refPoint);
            if (seen.insert(point.getX(), point.getY(), tolerance)) { // Creates a tree of points lying in the copies of fDomain
                if (point.distance(midpoint) < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                    d = point.distance(midpoint);
                    backShift = g;
                    apt = point;
                }
            }
        }

        while (!isInRangeForFDomainEuclidean(apt, windowCorner, width, height)) { // The loop works as long as the copy of fDomain lies outside the valid range for FDomain
            t = queue.poll(); // remove t from queue

            for (Transform g : generators.getTransforms()) {

                Transform tg = t.createConcatenation(g);
                point = tg.transform(refPoint);

                if (seen.insert(point.getX(), point.getY(), tolerance)) { // Creates a tree of points lying in the copies of fDomain
                    if (point.distance(midpoint) < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                        d = point.distance(midpoint);
                        backShift = tg;
                        apt = point;
                    }
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                point = gt.transform(refPoint);

                if (seen.insert(point.getX(), point.getY(), tolerance)) {
                    if (point.distance(midpoint) < d) {
                        d = point.distance(midpoint);
                        backShift = gt;
                        apt = point;
                    }
                    queue.add(gt);
                }
            }
        }
        return backShift;
    }

//----------------------------------------------------------------------------------------------------------------------

    /**
     * Hyperbolic case: Transform shifting back fundamental domain if out of bounds
     *
     * @return transform
     */
    public Transform calculateBackShiftHyperbolic() {

        //Add all generators
        computeConstraintsAndGenerators();

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        final OctTree seen = new OctTree();
        seen.insert(fDomain.getGeometry(), refPoint, tolerance);

        Transform backShift = new Translate(), t;
        Point3D apt = refPoint, point = refPoint;
        double d = apt.getZ();

        double limiter = Document.getValidHyperbolicRange() * 100 - 30;
        int counter = 0;
        while (apt.getZ() >= limiter) { // The loop works as long as the copy of fDomain lies outside the valid range
            counter++;
            if (counter >= 250) {
                System.out.println("Loop back shift. BREAK");
                break;
            }
            t = queue.poll(); // remove t from queue
            for (Transform g : generators.getTransforms()) {

                Transform tg = t.createConcatenation(g);
                point = tg.transform(refPoint);

                if (seen.insert(fDomain.getGeometry(), point, tolerance)) { // Creates a tree of points lying in the copies of fDomain
                    if (point.getZ() < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                        d = point.getZ();
                        backShift = tg;
                        apt = point;
                    }
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                point = gt.transform(refPoint);

                if (seen.insert(fDomain.getGeometry(), point, tolerance)) {
                    if (point.getZ() < d) {
                        d = point.getZ();
                        backShift = gt;
                        apt = point;
                    }
                    queue.add(gt);
                }
            }
        }
        return backShift;
    }

    /**
     * Euclidean case: Checks whether "point" is in valid range
     *
     * @param point
     * @param windowCorner
     * @param width
     * @param height
     * @return
     */
    public boolean isInRangeEuclidean(Point3D point, Point3D windowCorner, double width, double height) {
        // Adjust width and height for a range around visible window. Range around window has at least dimensions 600 times 600
        if (width >= 350) {
            width += 250;
        } else {
            width = 600;
        }

        if (height >= 350) {
            height += 250;
        } else {
            height = 600;
        }

        double eps = 0;

        return windowCorner.getX() - 250 - eps <= point.getX() && point.getX() <= windowCorner.getX() + width + eps &&
                windowCorner.getY() - 250 - eps <= point.getY() && point.getY() <= windowCorner.getY() + height + eps;
    }

    /**
     * Euclidean case: Checks whether "point" is in visible window
     *
     * @param point
     * @param windowCorner
     * @param width
     * @param height
     * @return
     */
    public boolean isInWindowEuclidean(Point3D point, Point3D windowCorner, double width, double height) { //Checks whether point is in visible window
        if (width < 450) {
            width = 450;
        }
        if (height < 450) {
            height = 450;
        }

        return windowCorner.getX() <= point.getX() && point.getX() <= windowCorner.getX() + width &&
                windowCorner.getY() <= point.getY() && point.getY() <= windowCorner.getY() + height;
    }

    /**
     * Euclidean case: checks whether fundamental domain lies in its valid range which is inside visible window.
     *
     * @param point
     * @param windowCorner
     * @param width
     * @param height
     * @return
     */
    public boolean isInRangeForFDomainEuclidean(Point3D point, Point3D windowCorner, double width, double height) {
        if (width < 450) {
            width = 450;
        }
        if (height < 450) {
            height = 450;
        }

        double left = windowCorner.getX() + 50, right = windowCorner.getX() + width - 50;
        double up = windowCorner.getY() + 50, down = windowCorner.getY() + height - 50;

        return left <= point.getX() && point.getX() <= right && up <= point.getY() && point.getY() <= down;
    }

    private void useRecycler(Group g, Transform t, Point3D p, Group domain) {
        if (recycler.getChildren().size() == 1) { // Refills recycler if almost empty
            final Group fund = JavaFXUtils.copyGroup(domain); // Copies original fundamental domain used to build up "tiles"
            recycler.getChildren().add(fund);
            if (fund instanceof Group) {
                if (fund.getChildren().size() == 0)
                    throw new RuntimeException("Fund copy empty");
            }
        }
        final Node node = recycler.getChildren().get(0);

        // Reuses a copy of recycler
        node.getTransforms().clear(); // Clear all transforms
        node.getTransforms().add(t.createConcatenation(transformRecycled)); // Add transform (maps original fundamental domain to actual fundamental domain)
        node.setRotationAxis(p); // Set new point of reference
        g.getChildren().add(node);
    }

    private void generateNewCopy(Group g, Transform t, Point3D p, Group f) {
        Group group2 = JavaFXUtils.copyGroup(f);
        group2.setRotationAxis(p);
        group2.getTransforms().add(t);
        g.getChildren().add(group2);
    }

    private boolean makeCopyHyperbolic(Point3D p) {
        return keptHyperbolicCopy.insert(fDomain.getGeometry(), p, tolerance);
    }

    private boolean makeCopyEuclidean(Point3D p) {
        return keptEuclideanCopy.insert(p.getX(), p.getY(), tolerance);
    }

    private boolean translateOrIncreaseTiling() {
        if (recycler.getChildren().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

    /**
     * compute middle point
     *
     * @param
     * @param
     * @return middle
     */
    /*
    public static Point2D middle(Geometry geometry, Point2D p, Point2D q) {
        double d;

        int sign = -1; // hyperbolic

        switch (geometry) {
            default:
            case Euclidean:
                d = 0.5;
                break;
            case Spherical:
                sign = 1; // spherical
            case Hyperbolic:
                d = 2 * (1 + sign * p.dotProduct(q));
                if (d <= 0) d = 0;
                else d = 1 / Math.sqrt(d);
                break;
        }
        return new Point2D(d * (p.getX() + q.getX()), d * (p.getY() + q.getY()));
    }
    */
    public DSymbol getDSymbol() {
        return ds;
    }

    public Geometry getGeometry() {
        return fDomain.getGeometry();
    }

    public FDomain getfDomain() {
        return fDomain;
    }

    public Group getHandles() {
        return handles;
    }
    //public Transforms getGenerators(){return generators;}


    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean isBreak) {
        this.isBreak = isBreak;
    }

    public void setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setHyperbolicFund(Group fund) {
        hyperbolicFund = fund;
    }

    public Group getHyperbolicFund() {
        return hyperbolicFund;
    }

    public void setEuclideanFund(Group g) {
        euclideanFund = g;
    }

    public Group getEuclideanFund() {
        return euclideanFund;
    }


    public void clearKeptHyperbolicCopy() {
        keptHyperbolicCopy = new OctTree();
    }

    public void clearKeptEuclideanCopy() {
        this.keptEuclideanCopy = new QuadTree();
    }

    public Point3D getRefPointHyperbolic() {
        return refPointHyperbolic;
    }

    public Point3D getRefPointEuclidean() {
        return refPointEuclidean;
    }

    public Group getRecycler() {
        return recycler;
    }

    public Transform getTransformRecycled() {
        return transformRecycled;
    }

    public void setTransformRecycled(Transform transformRecycled) {
        this.transformRecycled = transformRecycled;
    }

    public void insertKeptHyperbolicCopy(Point3D point) {
        keptHyperbolicCopy.insert(fDomain.getGeometry(), point, getTolerance());
    }

    public void insertKeptEuclideanCopy(Point3D point) {
        keptEuclideanCopy.insert(point.getX(), point.getY(), getTolerance());
    }

    public Transforms getGenerators() {
        return generators;
    }
}
