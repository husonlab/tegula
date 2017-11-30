package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Pair;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.core.dsymbols.OrbifoldGroupName;
import tiler.main.Document;
import tiler.util.JavaFXUtils;
import tiler.util.ShapeHandler;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * a tiling
 * Created by huson on 4/11/16.
 */
public class Tiling {
    boolean verbose = false;

    private final DSymbol ds;
    private final String groupName;
    private final FDomain fDomain;

    private final Transforms generators;
    private final Constraints constraints;

    public static OctTree keptHyperbolicCopy;
    public static QuadTree keptEuclideanCopy = new QuadTree();

    public static Point3D refPointHyperbolic = new Point3D(0, 0, 1);
    public static Point3D refPointEuclidean = new Point3D(1, 1, 0);

    public static Group recycler = new Group();
    public static Transform transformRecycled = new Translate();
    public static Group EuclideanFund = new Group();
    public static Group HyperbolicFund = new Group();

    public static Group handles = new Group();
    public static Handle handle = new Handle();


    private final int[] flag2vert;
    private final int[] flag2edge;
    private final int[] flag2tile;

    private final int[] vert2flag;
    private final int[] edge2flag;
    private final int[] tile2flag;

    private final int numbVert;
    private final int numbEdge;
    private final int numbTile;

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
     * @return tolerance
     */
    public double computeTolerance(){
        final Point3D refPoint;
        if (fDomain.getGeometry() == Geometry.Euclidean){
            refPoint = fDomain.getChamberCenter3D(Document.getChamberIndex());
        }
        else {
            refPoint = fDomain.getChamberCenter3D(Document.getChamberIndex()).multiply(0.01);
        }
        double tol = 100;
        for (Transform g : generators.getTransforms()){
            double dist = Tools.distance(fDomain, g.transform(refPoint), refPoint);
            if (dist < tol){
                tol = dist;
            }
        }
        return 0.8*tol;
    }

//----------------------------------------------------------------------------------------------------------------------

    /**
     * create the set of tiles to be shown in spherical case
     *
     * @return tiles
     */
    public Group createTilingSpherical(double tol) {
        //Add handles
        handles.getChildren().clear();
        //addHandles(doc);

        final Group group = new Group();
        final Group fund = FundamentalDomain.buildFundamentalDomain(ds, fDomain);
        group.getChildren().addAll(fund);
        //computeConstraintsAndGenerators();

        // Make copies of fundamental domain.
        final OctTree seen = new OctTree();
        final Point3D refPoint = fDomain.getChamberCenter3D(Document.getChamberIndex()).multiply(0.01); // refPoint lies on unit sphere
        seen.insert(fDomain, refPoint, tol); //root node of OctTree is point of reference.

        final Queue<Transform> queue = new LinkedList<>();
        queue.addAll(generators.getTransforms());
        for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
            Point3D genRef = g.transform(refPoint);
            if (seen.insert(fDomain, genRef, tol)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
                Group group2 = JavaFXUtils.copyFundamentalDomain(fund);
                group2.getTransforms().add(g);
                group.getChildren().add(group2);
            }
        }


        while (queue.size() > 0) {
            final Transform t = queue.poll(); // remove t from queue

            for (Transform g : generators.getTransforms()) {
                Transform tg = t.createConcatenation(g);
                Point3D bpt = tg.transform(refPoint);
                if (seen.insert(fDomain, bpt, tol)) {
                    Group group2 = JavaFXUtils.copyFundamentalDomain(fund);
                    group2.getTransforms().add(tg);
                    group.getChildren().add(group2);
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                bpt = gt.transform(refPoint);
                if (seen.insert(fDomain, bpt, tol)) {
                    Group group2 = JavaFXUtils.copyFundamentalDomain(fund);
                    group2.getTransforms().add(gt);
                    group.getChildren().add(group2);
                    queue.add(gt);
                }
            }
        }
        return group;
    }

//----------------------------------------------------------------------------------------------------------------------

    /**
     * create tiling in hyperbolic case
     *
     * @param maxDist
     * @return group
     */
    public Group createTilingHyperbolic(boolean drawFundamentalDomainOnly, double maxDist, double tol) {

        //Add all generators
        computeConstraintsAndGenerators();

        //Add handles
        handles.getChildren().clear();
        //addHandles(doc);

        refPointHyperbolic = fDomain.getChamberCenter3D(Document.getChamberIndex()).multiply(0.01);
        final OctTree seen = new OctTree();
        seen.insert(fDomain, refPointHyperbolic, tol); // root of OctTree is point of reference

        final Group group = new Group();
        final Group fund = FundamentalDomain.buildFundamentalDomain(ds, fDomain);

        if (makeCopyHyperbolic(refPointHyperbolic)) {
            fund.setRotationAxis(refPointHyperbolic);
            fund.getTransforms().add(new Translate());
            if (translateOrIncreaseTiling()) {// Translate mode of tiling
                useRecycler(group, new Translate(), refPointHyperbolic, HyperbolicFund);
            } else { // Builds up tiling from fundamental domain
                group.getChildren().addAll(fund);
                HyperbolicFund = fund; // Saves the original fundamental domain
            }
        }

        if (!drawFundamentalDomainOnly) {
            // Make copies of fundamental domain.
            final Queue<Transform> queue = new LinkedList<>();
            queue.addAll(generators.getTransforms());

            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D genRef = g.transform(refPointHyperbolic);
                if (seen.insert(fDomain, genRef, tol)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
                    if (makeCopyHyperbolic(genRef)) {
                        if (translateOrIncreaseTiling()) {
                            useRecycler(group, g, genRef, HyperbolicFund);
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
                    if (seen.insert(fDomain, bpt, tol) && bpt.getZ() < maxDist) {
                        countChildren++;
                        queue.add(tg);
                        if (makeCopyHyperbolic(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(group, tg, bpt, HyperbolicFund);
                            } else {
                                generateNewCopy(group, tg, bpt, fund);
                            }
                        }
                    }

                    Transform gt = g.createConcatenation(t);
                    bpt = gt.transform(refPointHyperbolic);
                    if (seen.insert(fDomain, bpt, tol) && bpt.getZ() < maxDist) {
                        countChildren++;
                        queue.add(gt);
                        if (makeCopyHyperbolic(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(group, gt, bpt, HyperbolicFund);
                            } else {
                                generateNewCopy(group, gt, bpt, fund);
                            }
                        }
                    }
                }
            }
        }
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
    public Group createTilingEuclidean(Document doc, boolean drawFundamentalDomainOnly, Point3D windowCorner, double width, double height, double tol) {

        //Add all generators
        computeConstraintsAndGenerators();

        //Add handles
        handles.getChildren().clear();
        addHandles(doc);

        //Calculation of point of reference:
        refPointEuclidean = fDomain.getChamberCenter3D(Document.getChamberIndex()); // Reference point of actual fundamental domain

        final Group group = new Group();
        final Group fund = FundamentalDomain.buildFundamentalDomain(ds, fDomain); // Build fundamental domain

        if (makeCopyEuclidean(refPointEuclidean)) { // Fill empty space with tiles
            fund.getTransforms().add(new Translate()); // Add transform (= identity)
            fund.setRotationAxis(refPointEuclidean); // Reference point of fundamental domain
            if (translateOrIncreaseTiling()) { // Translate mode of tiling
                useRecycler(group, new Translate(), refPointEuclidean, EuclideanFund);
            } else { // Builds up tile from fundamental domain
                group.getChildren().addAll(fund);
                EuclideanFund = fund; // Saves the original fundamental domain
            }

        }

        if (!drawFundamentalDomainOnly) {

            final QuadTree seen = new QuadTree(); // Saves reference points of tiles
            seen.insert(refPointEuclidean.getX(), refPointEuclidean.getY(), tol); // Insert reference point of fDomain

            final Queue<Transform> queue = new LinkedList<>(); // Saves transforms for copies
            queue.addAll(generators.getTransforms()); // Add generators

            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D genRef = g.transform(refPointEuclidean); // Reference point for new copy
                if (isInRangeEuclidean(genRef, windowCorner, width, height) && seen.insert(genRef.getX(), genRef.getY(), tol)) { // Checks whether reference point is in valid range and if it is in QuadTree "seen". Adds it if not.
                    if (makeCopyEuclidean(genRef)) { // Checks whether copy fills empty space after translation of tiles
                        if (translateOrIncreaseTiling()) { // Translate mode of tiling
                            useRecycler(group, g, genRef, EuclideanFund);
                        } else {
                            generateNewCopy(group, g, genRef, fund);
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

                    if (isInRangeEuclidean(bpt, windowCorner, width, height) && seen.insert(bpt.getX(), bpt.getY(), tol)) {
                        queue.add(tg);
                        if (makeCopyEuclidean(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(group, tg, bpt, EuclideanFund);
                            } else {
                                generateNewCopy(group, tg, bpt, fund);
                            }
                        }
                    }

                    Transform gt = g.createConcatenation(t);
                    bpt = gt.transform(refPointEuclidean);

                    if (isInRangeEuclidean(bpt, windowCorner, width, height) && seen.insert(bpt.getX(), bpt.getY(), tol)) {
                        queue.add(gt);
                        if (makeCopyEuclidean(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(group, gt, bpt, EuclideanFund);
                            } else {
                                generateNewCopy(group, gt, bpt, fund);
                            }
                        }
                    }
                }
            }
        }
        return group;
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
    public Transform calculateBackShiftEuclidean(Point3D windowCorner, double width, double height, double tol) {

        if (width < 450) {
            width = 450;
        }
        if (height < 450) {
            height = 450;
        }

        //Add all generators
        computeConstraintsAndGenerators();

        final Queue<Transform> queue = new LinkedList<>();
        queue.addAll(generators.getTransforms());

        Point3D refPoint = fDomain.getChamberCenter3D(Document.getChamberIndex());
        final QuadTree seen = new QuadTree();
        seen.insert(refPoint.getX(), refPoint.getY(), tol);

        Transform backShift = new Translate(), t;
        Point3D point = refPoint, apt = refPoint;
        Point3D midpoint = new Point3D(windowCorner.getX() + width / 2, windowCorner.getY() + height / 2, 0);
        double d = point.distance(midpoint);

        for (Transform g : generators.getTransforms()) {
            point = g.transform(refPoint);
            if (seen.insert(point.getX(), point.getY(), tol)) { // Creates a tree of points lying in the copies of fDomain
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

                if (seen.insert(point.getX(), point.getY(), tol)) { // Creates a tree of points lying in the copies of fDomain
                    if (point.distance(midpoint) < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                        d = point.distance(midpoint);
                        backShift = tg;
                        apt = point;
                    }
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                point = gt.transform(refPoint);

                if (seen.insert(point.getX(), point.getY(), tol)) {
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
    public Transform calculateBackShiftHyperbolic(double tol) {

        //Add all generators
        computeConstraintsAndGenerators();

        final Queue<Transform> queue = new LinkedList<>();
        queue.addAll(generators.getTransforms());


        Point3D refPoint = fDomain.getChamberCenter3D(Document.getChamberIndex());
        final OctTree seen = new OctTree();
        seen.insert(fDomain, refPoint, tol);

        Transform backShift = new Translate(), t;
        Point3D apt = refPoint, point = refPoint;
        double d = apt.getZ();

        double limiter = Document.getValidHyperbolicRange()*100-30;
        int counter = 0;
        while (apt.getZ() >= limiter) { // The loop works as long as the copy of fDomain lies outside the valid range
            counter++;
            if (counter >= 250){
                System.out.println("Loop back shift. BREAK");
                break;
            }
            t = queue.poll(); // remove t from queue
            for (Transform g : generators.getTransforms()) {

                Transform tg = t.createConcatenation(g);
                point = tg.transform(refPoint);

                if (seen.insert(fDomain, point, tol)) { // Creates a tree of points lying in the copies of fDomain
                    if (point.getZ() < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                        d = point.getZ();
                        backShift = tg;
                        apt = point;
                    }
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                point = gt.transform(refPoint);

                if (seen.insert(fDomain, point, tol)) {
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

        if (windowCorner.getX() - 250 - eps <= point.getX() && point.getX() <= windowCorner.getX() + width + eps &&
                windowCorner.getY() - 250 - eps <= point.getY() && point.getY() <= windowCorner.getY() + height + eps) {
            return true;
        } else {
            return false;
        }
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

        if (windowCorner.getX() <= point.getX() && point.getX() <= windowCorner.getX() + width &&
                windowCorner.getY() <= point.getY() && point.getY() <= windowCorner.getY() + height) {
            return true;
        } else {
            return false;
        }
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

        if (left <= point.getX() && point.getX() <= right &&
                up <= point.getY() && point.getY() <= down) {
            return true;
        } else {
            return false;
        }
    }

    private void useRecycler(Group g, Transform t, Point3D p, Group domain) {
        if (recycler.getChildren().size() == 1) { // Refills recycler if almost empty
            Group recycler2 = JavaFXUtils.copyFundamentalDomain(domain); // Copies original fundamental domain used to build up "tiles"
            recycler.getChildren().addAll(recycler2);
        }
        Node node = recycler.getChildren().get(0); // Reuses a copy of recycler
        node.getTransforms().clear(); // Clear all transforms
        node.getTransforms().add(t.createConcatenation(transformRecycled)); // Add transform (maps original fundamental domain to actual fundamental domain)
        node.setRotationAxis(p); // Set new point of reference
        g.getChildren().add(node);
    }

    private void generateNewCopy(Group g, Transform t, Point3D p, Group f) {
        Group group2 = JavaFXUtils.copyFundamentalDomain(f);
        group2.setRotationAxis(p);
        group2.getTransforms().add(t);
        g.getChildren().add(group2);
    }

    private static int getNumberOfCopies() {
        return Document.numberOfCopies;
    }

    private void setBreak(boolean b) {
        Document.isBreak = b;
    }

    private boolean makeCopyHyperbolic(Point3D p) {
        return keptHyperbolicCopy.insert(fDomain, p, Document.getTol());
    }

    private boolean makeCopyEuclidean(Point3D p) {
        return keptEuclideanCopy.insert(p.getX(), p.getY(), Document.getTol());
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
     * straigthen all edges
     */
    public void straightenAllEdges() {
        for (int i = 1; i <= numbEdge; i++) {
            straightenEdge(i);
        }
    }

    /**
     * straighten a specific edge
     *
     * @param edge
     */
    public void straightenEdge(int edge) {

        int i;
        int[] a = new int[5];

        if (edge < 1 || edge > numbEdge)
            throw new RuntimeException(String.format("straighten_edge(edge=%d): edge not in 1..%d", edge, numbEdge));

        a[0] = edge2flag[edge];

        if (ds.getSi(0, a[0]) != a[0])
            a[1] = ds.getSi(0, a[0]);
        else
            a[1] = 0;
        if (ds.getSi(2, a[0]) != a[0] && ds.getSi(2, a[0]) != a[1])
            a[2] = ds.getSi(2, a[0]);
        else
            a[2] = 0;
        if (a[2] != 0 && ds.getSi(0, a[2]) != a[0] && ds.getSi(0, a[2]) != a[1] && ds.getSi(0, a[2]) != a[2])
            a[3] = ds.getSi(0, a[2]);
        else
            a[3] = 0;

        for (i = 0; i < 4; i++) {
            if (a[i] != 0)
            {
                Point2D aPt = fDomain.getVertex(0, a[i]);
                Point2D bPt = fDomain.getVertex(0, ds.getSi(0, a[i]));
                Point3D aPt3d = Tools.map2Dto3D(fDomain.getGeometry(), aPt);
                Point3D bPt3d = Tools.map2Dto3D(fDomain.getGeometry(), bPt);
                if (fDomain.isBoundaryEdge(0, a[i])) {
                    Transform gen = generators.get(0, ds.getSi(0, a[i]));
                    bPt3d = gen.transform(bPt3d);
                }

                Point3D cPt3d = Tools.midpoint3D(fDomain.getGeometry(), aPt3d, bPt3d);
                Point2D cPt = Tools.map3Dto2D(fDomain.getGeometry(), cPt3d);
                fDomain.setVertex(cPt, 1, a[i]);

                cPt3d = Tools.midpoint3D(fDomain.getGeometry(), aPt3d, cPt3d);
                cPt = Tools.map3Dto2D(fDomain.getGeometry(), cPt3d);
                fDomain.setEdgeCenter(cPt, 2, a[i]);
            }
        }
        // Straighten all edges of chambers and recompute chamber centers (= mass point of chamber):
        for (int j = 1; j <= fDomain.size(); j++){
            Point3D A = fDomain.getVertex3D(0, j);
            Point3D B = fDomain.getVertex3D(1, j);
            Point3D C = fDomain.getVertex3D(2, j);
            Point2D AB = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), A, B));
            Point2D AC = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), A, C));
            Point2D BC = Tools.map3Dto2D(fDomain.getGeometry(), Tools.midpoint3D(fDomain.getGeometry(), B, C));

            fDomain.setEdgeCenter(BC, 0, j);
            fDomain.setEdgeCenter(AC, 1, j);
            fDomain.setEdgeCenter(AB, 2, j);

            Point2D vec = (fDomain.getVertex(2, j).subtract(AB)).multiply(0.33333);
            fDomain.setChamberCenter(AB.add(vec), j);
        }
    }

    /**
     * Straighten 0- and 1- edges
     */
    private void straighten01Edges(){
        // Straighten 0- and 1-edges
        for (int a = 1; a <= fDomain.size(); a++){
            // Midpoint between 1- and 2-vertex = new 0-edge center
            Point3D pt3d = (fDomain.getVertex3D(1, a).add(fDomain.getVertex3D(2, a))).multiply(0.5);
            Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
            fDomain.setEdgeCenter(pt2d, 0, a);
            // Midpoint between 0- and 2-vertex = new 1-edge center
            pt3d = (fDomain.getVertex3D(0, a).add(fDomain.getVertex3D(2, a))).multiply(0.5);
            pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
            fDomain.setEdgeCenter(pt2d, 1, a);
            // Recompute chamber center
            Point3D vec = (fDomain.getVertex3D(2, a).subtract(fDomain.getEdgeCenter3D(2, a))).multiply(0.33333);
            pt3d = fDomain.getEdgeCenter3D(2, a).add(vec);
            pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt3d);
            fDomain.setChamberCenter(pt2d, a);
        }
    }


    /**
     * Add handles to change shape
     */
    private void addHandles(Document doc) {
        // Straighten 0- and 1-edges
        straighten01Edges();

        // Compute handles for 0- and 1-vertices
        for (int i = 0; i <= 1; i++) {
            Point3D v;
            int a = 1;
            int m = ds.countOrbits(i, 2);
            BitSet visited = new BitSet(m);
            for (int k = 1; k <= m; k++) {
                v = fDomain.getVertex3D(1-i, a);
                // Add handles if v is no symmetry centre
                if ((ds.getVij(1, 2, a) == 1 && i == 1) || (ds.getVij(0, 2, a) == 1 && i == 0 && a != ds.getS2(a))) {
                    Handle handle = new Handle();
                    Circle circle = new Circle(4);
                    circle.setTranslateX(v.getX());
                    circle.setTranslateY(v.getY());
                    circle.setFill(Color.WHITE);
                    circle.setStroke(Color.DARKGRAY);
                    handle.setShape(circle);
                    handle.setType(1 - i);
                    handle.setFlag(a);
                    handles.getChildren().add(handle.getShape());
                    ShapeHandler.setHandler(doc, handle);
                }
                a = ds.nextOrbit(i, 2, a, visited);
            }
        }

        // Compute handles for 2-edge-centers
        Point3D e;
        int m = fDomain.size();
        BitSet visited = new BitSet(m);
        int a = 1;
        while (a <= m){
            if (!visited.get(a)){
                e = fDomain.getEdgeCenter3D(2,a);
                // Add handles if e does not lie on a mirror axis
                if (a != ds.getS2(a)) {
                    Handle handle = new Handle();
                    Circle circle = new Circle(4);
                    circle.setTranslateX(e.getX());
                    circle.setTranslateY(e.getY());
                    circle.setFill(Color.WHITE);
                    circle.setStroke(Color.DARKGRAY);
                    handle.setShape(circle);
                    handle.setFlag(a);
                    handle.setType(2);
                    handles.getChildren().add(handle.getShape());
                    ShapeHandler.setHandler(doc, handle);
                }
                visited.set(ds.getS2(a));
            }
            a++;
        }
    }

    /**
     * Reset shape of tiles
     * @param deltaX
     * @param deltaY
     * @param handle
     */
    public Point2D resetShape(double deltaX, double deltaY, Handle handle){
        // Reset Point in fundamental domain
        Point2D transVector = new Point2D(deltaX, deltaY);
        Transform g;
        Translate t;
        int i = handle.getType(), a = handle.getFlag();
        if (i <= 1) {
            int l = ds.computeOrbitLength(1 - i, 2, a);
            // Add restrictions:
            if (i == 1){
                transVector = add1Restriction(transVector.getX(), transVector.getY(), a); // Restrictions for 1-handles
            }
            else{
                transVector = add0Restriction(transVector.getX(), transVector.getY(), a, l); // Restrictions for 0-handles
            }

            transVector = addMirrorRestriction(transVector.getX(), transVector.getY(), l, i, a); // Mirror axis restriction


            t = new Translate(transVector.getX(), transVector.getY());

            // Translate Point of type i in chamber a
            Point3D pt = fDomain.getVertex3D(i, a);
            pt = t.transform(pt);
            javafx.geometry.Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt);
            fDomain.setVertex(pt2d, i, a);

            // Consider all points in orbit of a (especially if chamber contains boundary edges)
            for (int k = 1; k <= l; k++) {
                // If (1-i)-edge is on boundary
                if (fDomain.isBoundaryEdge(1 - i, a)) {
                    g = generators.get(1 - i, a);
                    pt = g.transform(pt);
                    pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt);
                    fDomain.setVertex(pt2d, i, ds.getSi(1 - i, a));
                }
                a = ds.getSi(1 - i, a);

                // If 2-edge is on boundary
                if (fDomain.isBoundaryEdge(2, a)) {
                    g = generators.get(2, a);
                    pt = g.transform(pt);
                    pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt);
                    fDomain.setVertex(pt2d, i, ds.getSi(2, a));
                }
                a = ds.getSi(2, a);
            }
        }
        else {
            transVector = add2Restriction(deltaX, deltaY, a);
            t = new Translate(transVector.getX(), transVector.getY());

            Point3D apt = fDomain.getEdgeCenter3D(2,a);
            apt = t.transform(apt);
            Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), apt);
            fDomain.setEdgeCenter(pt2d, 2, a);
            if (fDomain.isBoundaryEdge(2, a)) {
                g = generators.get(2, a);
                apt = g.transform(apt);
                pt2d = Tools.map3Dto2D(fDomain.getGeometry(), apt);
                fDomain.setEdgeCenter(pt2d, 2, ds.getS2(a));
            }
        }
        // Straighten 0- and 1-edges
        straighten01Edges();
        return transVector;
    }

    /**
     * Add restrictions to translation in case that handle lies on exactly one mirror axis
     * @param deltaX
     * @param deltaY
     * @param orbitLength
     * @param type
     * @param flag
     * @return
     */
    private Point2D addMirrorRestriction(double deltaX, double deltaY, int orbitLength, int type, int flag){
        Point3D r = new Point3D(0,0,0), n = new Point3D(0,0,0), q= new Point3D(0,0,0);
        final int m = fDomain.size();
        // Count number of chambers lying in (1-type)-2-orbit containing flag
        final BitSet visited = new BitSet(m);
        ds.markOrbit(1-type, 2, flag, visited);
        int numberOfChambers = 0;
        for (int d = 1; d <= m; d++){
            if (visited.get(d)){
                numberOfChambers++;
            }
        }

        // Restrict movement if handle lies on exactly one mirror axis:
        if (2*orbitLength/numberOfChambers == 2){ // Condition for exactly one mirror axis
            while (flag <= m){
                if (fDomain.isBoundaryEdge(1-type, flag) && ds.getSi(1-type, flag) == flag){ // Mirror axis is always a boundary for fundamental domain
                    Transform g = generators.get(1-type, flag);
                    n = fDomain.getVertex3D(1-type, flag).subtract(g.transform(fDomain.getVertex3D(1-type, flag))); // Orthogonal direction of mirror axis
                    r = new Point3D(n.getY(), -n.getX(), 0); // Direction of translation for handle = direction of mirror axis
                    Point3D transformed = g.transform(fDomain.getVertex3D(1-type, flag));
                    q = fDomain.getVertex3D(1-type, flag).midpoint(transformed);

                    break;
                }
                else{
                    flag = ds.getSi(1-type, flag);
                }
                if (fDomain.isBoundaryEdge(2, flag) && ds.getS2(flag) == flag){ // Consider 2-edge-boundaries
                    Transform g = generators.get(2, flag);
                    n = fDomain.getVertex3D(2, flag).subtract(g.transform(fDomain.getVertex3D(2, flag)));
                    r = new Point3D(n.getY(), -n.getX(), 0);
                    Point3D transformed = g.transform(fDomain.getVertex3D(2, flag));
                    q = fDomain.getVertex3D(2, flag).midpoint(transformed);
                    break;
                }
                else{
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
     * @param deltaX
     * @param deltaY
     * @param flag
     * @return new direction
     */
    private Point2D add2Restriction(double deltaX, double deltaY, int flag){
        // Restrict movement for 2-edge-handles
        Point2D transVec = new Point2D(deltaX, deltaY);
        Transform invGen = new Translate();
        if (fDomain.isBoundaryEdge(2, flag)){
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



    private Point2D add1Restriction(double deltaX, double deltaY, int flag){
        // There exist 4 restrictions. Each restricting line / plane is of the form <x,n> = c.
        // R - directions of line, N - normal vector, c - coordinate
        Point3D[] R = new Point3D[4], N = new Point3D[4], Q = new Point3D[4]; // Save normal vectors, directions and position vectors of restricting lines
        double[] c = new double[4]; // Save coordinates of the 4 restricting lines / planes

        Affine genMat = new Affine(1,0,0,0,0,1,0,0,0,0,1,0);
        Point2D transVec = new Point2D(deltaX, deltaY);
        Transform gen = new Translate();
        for (int i = 0; i <= 1; i++){
            R[2*i] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2*i] = genMat.transform(R[2*i]);
            N[2*i] = new Point3D(R[2*i].getY(), -R[2*i].getX(),0);
            Q[2*i] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2*i] = N[2*i].dotProduct(Q[2*i]);

            flag = ds.getS2(flag);
            if (fDomain.isBoundaryEdge(2, flag)) {
                gen = gen.createConcatenation(generators.get(2, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }

            R[2*i+1] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2*i+1] = genMat.transform(R[2*i+1]);
            N[2*i+1] = new Point3D(R[2*i+1].getY(), -R[2*i+1].getX(),0);
            Q[2*i+1] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2*i+1] = N[2*i+1].dotProduct(Q[2*i+1]);

            flag = ds.getS0(flag);
            if (fDomain.isBoundaryEdge(0, flag)) {
                gen = gen.createConcatenation(generators.get(0, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }
        }

        Point3D apt = fDomain.getEdgeCenter3D(2, ds.getS0(flag));
        if (fDomain.isBoundaryEdge(0, flag)){
            gen = generators.get(0, ds.getS0(flag));
            apt = gen.transform(apt);
        }

        Point3D firstPos = apt.midpoint(fDomain.getEdgeCenter3D(2, flag));
        Point3D oldPos = fDomain.getVertex3D(1, flag);

        return checkRestriction(transVec, R, N, Q, c, firstPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }

    private Point2D add0Restriction(double deltaX, double deltaY, int flag, int length){
        // There exist length restrictions. Each restricting line / plane is of the form <x,n> = c.
        // R - directions of line, N - normal vector, c - coordinate

        Point3D[] R = new Point3D[2*length], N = new Point3D[2*length], Q = new Point3D[2*length]; // Save normal vectors, directions and position vectors of restricting lines
        double[] c = new double[2*length]; // Save coordinates of the 4 restricting lines / planes

        Affine genMat = new Affine(1,0,0,0,0,1,0,0,0,0,1,0);
        Point2D transVec = new Point2D(deltaX, deltaY);
        Transform gen = new Translate();
        for (int i = 0; i <= length-1; i++){
            R[2*i] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2*i] = genMat.transform(R[2*i]);
            N[2*i] = new Point3D(R[2*i].getY(), -R[2*i].getX(),0);
            Q[2*i] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2*i] = N[2*i].dotProduct(Q[2*i]);

            flag = ds.getS2(flag);
            if (fDomain.isBoundaryEdge(2, flag)) {
                gen = gen.createConcatenation(generators.get(2, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }

            R[2*i+1] = fDomain.getVertex3D(2, flag).subtract(fDomain.getEdgeCenter3D(2, flag));
            R[2*i+1] = genMat.transform(R[2*i+1]);
            N[2*i+1] = new Point3D(R[2*i+1].getY(), -R[2*i+1].getX(),0);
            Q[2*i+1] = gen.transform(fDomain.getEdgeCenter3D(2, flag));
            c[2*i+1] = N[2*i+1].dotProduct(Q[2*i+1]);

            flag = ds.getS1(flag);
            if (fDomain.isBoundaryEdge(1, flag)) {
                gen = gen.createConcatenation(generators.get(1, flag));
                genMat = new Affine(gen.getMxx(), gen.getMxy(), gen.getMxz(), 0, gen.getMyx(), gen.getMyy(), gen.getMyz(), 0, gen.getMzx(), gen.getMzy(), gen.getMzz(), 0);
            }
        }

        Point3D apt = fDomain.getEdgeCenter3D(2, ds.getS0(flag));
        if (fDomain.isBoundaryEdge(1, flag)){
            gen = generators.get(1, ds.getS1(flag));
            apt = gen.transform(apt);
        }

        Point3D oldPos = fDomain.getVertex3D(0, flag);

        return checkRestriction(transVec, R, N, Q, c, oldPos, oldPos); // Check if restrictions are fulfilled when translating by mouse coordinates
    }


        /**
         * Change direction of mouse movement for handle when restrictions are broken
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
        for (int i = 0; i <= length-1; i++){
            restrictions[i] = compare(c[i], N[i].dotProduct(firstPos));  // Compute restrictions (firstPos fulfills all restrictions and is used to define all restrictions)
        }

        // Make restricted area smaller by factor 0.00001 to prevent from rounding errors
        for (int i=0; i<=length-1; i++){
            Point3D qpt = Q[i].add(N[i].multiply(0.00001));
            if (compare(firstPos.dotProduct(N[i]), Q[i].dotProduct(N[i])) == compare(qpt.dotProduct(N[i]), Q[i].dotProduct(N[i]))){
                Q[i] = qpt;
            }
            else{
                Q[i] = Q[i].subtract(N[i].multiply(0.00001));
            }
            c[i] = Q[i].dotProduct(N[i]);
            checkRest[i] = compare(c[i], N[i].dotProduct(newPos)); // Compute restrictions for newPos
        }

        // Loop works as long as newPos does not fulfill restrictions but at most 50 times
        // If a restriction is not fulfilled then newPos is moved onto the restricting line / plane in direction of its normal vector
        int counter = 0;
        while (counter <= 1000 && !checkRest.equals(restrictions)){
            for (int i = 0; i <= length-1; i++){
                if (checkRest[i] != restrictions[i]) {
                    Point3D qp = Q[i].subtract(newPos);
                    // b is the coefficient of the normal vector of restriction i
                    double b = (qp.getX() * R[i].getY() - qp.getY() * R[i].getX()) / (R[i].getX() * R[i].getX() + R[i].getY() * R[i].getY());
                    newPos = newPos.add(N[i].multiply(b)); // Move newPos onto the restricting line i
                    checkRest[i] = restrictions[i]; // Now restriction i is fulfilled
                    for (int j = 0; j <= length-1; j++) { // Check other restrictions for newPos
                        if (j != i) {
                            checkRest[j] = compare(c[j], N[j].dotProduct(newPos));
                        }
                    }
                }
            }
            counter++;
        }
        Point3D transVec3d = newPos.subtract(oldPos);
        return new Point2D(transVec3d.getX(), transVec3d.getY());
    }

    private boolean compare(double a, double b){
        if (a >= b) return true;
        else return false;
    }



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
}