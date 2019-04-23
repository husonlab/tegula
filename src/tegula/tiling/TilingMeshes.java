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
package tegula.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.main.TilingStyle;
import tegula.main.editor.ExtendedTiling;
import tegula.util.JavaFXUtils;

import java.util.LinkedList;
import java.util.Queue;

import static tegula.Options.showReferencePoints;
import static tegula.Options.useNewEuclideanCode;

/**
 * computes a tiling represented by meshes from a fundamental domain represented by meshes
 * <p>
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class TilingMeshes {
    private final TilingStyle tilingStyle;
    private final DSymbol ds;
    private final FDomain fDomain;

    private final FundamentalDomainMeshes fundamentalDomainMeshes;

    private final Generators generators;

    final private OctTree keptHyperbolicCopy = new OctTree();
    final private QuadTree keptEuclideanCopy = new QuadTree();

    private Point3D refPointHyperbolic = new Point3D(0, 0, 1);
    private Point3D refPointEuclidean = new Point3D(1, 1, 0);

    private final Group recycler = new Group();
    private Transform transformRecycled = new Translate();
    private Group euclideanFund = new Group();
    private Group hyperbolicFund = new Group();

    public final static Group FAILED = new Group(); // returned by createTiling when too many rounding errors occur, indicating a recompute

    private int numberOfCopies = 0;

    private double tolerance = 0.0;
    private int referenceChamberIndex = 1;

    /**
     * constructor
     *
     * @param tilingStyle
     * @param ds
     */
    public TilingMeshes(TilingStyle tilingStyle, DSymbol ds) {
        this.tilingStyle = tilingStyle;
        this.ds = ds;
        fDomain = new FDomain(ds);
        generators = fDomain.getGenerators();

        fundamentalDomainMeshes = new FundamentalDomainMeshes();
        fundamentalDomainMeshes.includeTilesProperty().bind(tilingStyle.showFacesProperty());
        fundamentalDomainMeshes.includeBandsProperty().bind(tilingStyle.showBandsProperty());
        fundamentalDomainMeshes.includeDecorationsProperty().bind(tilingStyle.showOtherStuffProperty());
        fundamentalDomainMeshes.includeChambersProperty().bind(tilingStyle.showAllChambersProperty());
        // do NOT bind handles property
    }


//----------------------------------------------------------------------------------------------------------------------

    /**
     * create the set of tiles to be shown in spherical case
     *
     * @return tiles
     */
    public Group createTilingSpherical() {
        final Group all = new Group();

        fundamentalDomainMeshes.update(ds, fDomain, tilingStyle);
        final Group fund = fundamentalDomainMeshes.getAll();

        all.getChildren().add(fund);

        // Make copies of fundamental domain.
        final OctTree seen = new OctTree();
        final Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex); // refPoint lies on unit sphere
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
            all.getChildren().add(fundamentalDomainMeshes.getHandles());

        if (tilingStyle.isShowHandles())
            all.getChildren().add(fundamentalDomainMeshes.getHandles());

        return all;
    }

    //----------------------------------------------------------------------------------------------------------------------

    /**
     * create tiling in hyperbolic case
     *
     * @param maxDist
     * @return group
     */
    public Group createTilingHyperbolic(boolean translateOrIncreaseTiling, boolean drawFundamentalDomainOnly, double maxDist) {
        //System.err.println("Create Hyperbolic Tiling");
        //System.out.println(refPointHyperbolic);

        final Group fund = fundamentalDomainMeshes.getAll();
        final Group all = new Group();

        refPointHyperbolic = fDomain.getChamberCenter3D(referenceChamberIndex);

        final OctTree allReferencePoints = new OctTree();

        if (!translateOrIncreaseTiling) { // need to recompute fundamental domain
            fundamentalDomainMeshes.update(ds, fDomain, tilingStyle);

            fund.setRotationAxis(refPointHyperbolic);
            fund.getTransforms().add(new Translate());
            all.getChildren().add(fund);
            setHyperbolicFund(fund);
            allReferencePoints.insert(getGeometry(), refPointHyperbolic, tolerance);
        }

        if (!drawFundamentalDomainOnly) {
            // Make copies of fundamental domain.
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

            // apply all generators:
            for (Transform transform : generators.getTransforms()) {
                Point3D genRef = transform.transform(refPointHyperbolic);
                if (allReferencePoints.insert(fDomain.getGeometry(), genRef, tolerance)) {
                    if (makeCopyHyperbolic(genRef)) {
                        if (translateOrIncreaseTiling) {
                            all.getChildren().add(useRecycler(transform, genRef, getHyperbolicFund()));
                        } else {
                            all.getChildren().add(createCopy(transform, genRef, getHyperbolicFund()));
                        }
                    }
                }
            }

            int countChildrenAdded = 0;
            while (queue.size() > 0) {

                // Breaks while loop if too many copies (rounding errors)
                if (translateOrIncreaseTiling && getNumberOfCopies() > 0 && countChildrenAdded >= 1.5 * getNumberOfCopies()) {
                    System.out.println(countChildrenAdded + " children and " + getNumberOfCopies() + " copies");
                    return FAILED;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) {
                    {
                        final Transform tg = t.createConcatenation(g);
                        final Point3D bpt = tg.transform(refPointHyperbolic);

                        if (bpt.getZ() < maxDist && allReferencePoints.insert(fDomain.getGeometry(), bpt, tolerance)) {
                            queue.add(tg);
                            if (makeCopyHyperbolic(bpt)) {
                                if (translateOrIncreaseTiling) {
                                    all.getChildren().add(useRecycler(tg, bpt, getHyperbolicFund()));
                                } else {
                                    all.getChildren().add(createCopy(tg, bpt, getHyperbolicFund()));

                                }
                            }
                        }
                    }

                    {
                        final Transform gt = g.createConcatenation(t);
                        final Point3D bpt = gt.transform(refPointHyperbolic);
                        if (bpt.getZ() < maxDist && allReferencePoints.insert(fDomain.getGeometry(), bpt, tolerance)) {
                            countChildrenAdded++;
                            queue.add(gt);
                            if (makeCopyHyperbolic(bpt)) {
                                if (translateOrIncreaseTiling) {
                                    all.getChildren().add(useRecycler(gt, bpt, getHyperbolicFund()));
                                } else {
                                    all.getChildren().add(createCopy(gt, bpt, getHyperbolicFund()));
                                }
                            }
                        }
                    }
                }
                if (all.getChildren().size() > 2000)
                    break;
            }

            System.err.println("Count: " + countChildrenAdded);
        }

        // only what one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.getChildren().add(fundamentalDomainMeshes.getChambers());

        if (tilingStyle.isShowHandles())
            all.getChildren().add(fundamentalDomainMeshes.getHandles());

        return all;
    }
//----------------------------------------------------------------------------------------------------------------------

    /**
     * create tiling in Euclidean case
     *
     * @param width
     * @param height
     * @return group
     */
    public Group createTilingEuclidean(final boolean translateOrIncreaseTiling, final boolean drawFundamentalDomainOnly, final double width, final double height) {

        //Add handles
        //handles.getChildren().setAll(((new ReshapeManager(doc).createHandles())));

        if (useNewEuclideanCode) {
            if (!isInWindowEuclidean(getRefPointEuclidean(), width, height)) { // Fund. domain is not in visible window
                System.err.println("Recenter");
            }
        }

        //Calculation of point of reference:
        if (!useNewEuclideanCode) {
            refPointEuclidean = fDomain.getChamberCenter3D(referenceChamberIndex); // Reference point of actual fundamental domain
            //System.err.println("RefPoint: " + refPointEuclidean);
        }

        final Group fund = fundamentalDomainMeshes.getAll();
        final Group all = new Group();

        if (!translateOrIncreaseTiling) { // need to recompute fundamental domain
            fundamentalDomainMeshes.update(ds, fDomain, tilingStyle);

            if (useNewEuclideanCode) {
                refPointEuclidean = new Point3D(fundamentalDomainMeshes.getReference().getTranslateX(), fundamentalDomainMeshes.getReference().getTranslateY(), fundamentalDomainMeshes.getReference().getTranslateZ());

                System.err.println("RefPoint: " + refPointEuclidean);

                tolerance = computeTolerance(fDomain.getGeometry(), refPointEuclidean, generators);
                System.err.println("tolerance: " + tolerance);
            }

            if (showReferencePoints)
                fund.getChildren().addAll(fundamentalDomainMeshes.getReferences().getChildren());

            all.getChildren().add(fund);
            fund.getTransforms().add(new Translate()); // Add transform (= identity)
            fund.setRotationAxis(refPointEuclidean); // Reference point of fundamental domain

            fund.setUserData("fund");
            setEuclideanFund(fund); // Saves the original fundamental domain
        }


        if (!drawFundamentalDomainOnly) {
            final QuadTree allReferencePoints = new QuadTree(); // Saves reference points of tiles

            allReferencePoints.insert(refPointEuclidean.getX(), refPointEuclidean.getY(), tolerance); // Insert reference point of fDomain

            // Saves transforms for copies
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms()); // Add generators

            for (Transform generator : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                final Point3D genRef = generator.transform(refPointEuclidean); // Reference point for new copy
                if (isInRangeEuclidean(genRef, width, height) && allReferencePoints.insert(genRef.getX(), genRef.getY(), tolerance)) { // Checks whether reference point is in valid range and if it is in QuadTree "seen". Adds it if not.
                    if (makeCopyEuclidean(genRef)) { // Checks whether copy fills empty space after translation of tiles
                        if (translateOrIncreaseTiling) { // Translate mode of tiling
                            all.getChildren().add(useRecycler(generator, genRef, getEuclideanFund()));
                        } else {
                            all.getChildren().add(createCopy(generator, genRef, fund));

                        }
                    }
                }
            }

            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (translateOrIncreaseTiling && queue.size() >= 1.5 * getNumberOfCopies()) {
                    return FAILED; // too many rounding errors
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) { // Creates new transforms for copies
                    {
                        final Transform tg = t.createConcatenation(g);
                        final Point3D bpt = tg.transform(getRefPointEuclidean()); // Reference point corresponding to transform tg

                        if (isInRangeEuclidean(bpt, width, height) && allReferencePoints.insert(bpt.getX(), bpt.getY(), tolerance)) {
                            queue.add(tg);
                            if (makeCopyEuclidean(bpt)) {
                                if (translateOrIncreaseTiling) {
                                    all.getChildren().add(useRecycler(tg, bpt, getEuclideanFund()));
                                } else {
                                    all.getChildren().add(createCopy(tg, bpt, fund));
                                }
                            }
                        }
                    }

                    {
                        final Transform gt = g.createConcatenation(t);
                        final Point3D bpt = gt.transform(getRefPointEuclidean());

                        if (isInRangeEuclidean(bpt, width, height) && allReferencePoints.insert(bpt.getX(), bpt.getY(), tolerance)) {
                            queue.add(gt);
                            if (makeCopyEuclidean(bpt)) {
                                if (translateOrIncreaseTiling) {
                                    all.getChildren().add(useRecycler(gt, bpt, getEuclideanFund()));
                                } else {
                                    all.getChildren().add(createCopy(gt, bpt, fund));
                                }
                            }
                        }
                    }
                    if (all.getChildren().size() > 100000) {
                        System.err.println("Too many copies: " + all.getChildren().size());
                        break;
                    }
                }
            }
        }

        // only want one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.getChildren().add(fundamentalDomainMeshes.getChambers());

        if (tilingStyle.isShowHandles())
            all.getChildren().add(fundamentalDomainMeshes.getHandles());

        if (true) {
            final Point2D windowCorner = new Point2D(-0.5 * width - 10, -0.5 * height - 10);
            Rectangle rectangle = new Rectangle(windowCorner.getX(), windowCorner.getY(), width, height);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(Color.DARKRED);
            all.getChildren().add(rectangle);
        }

        if (showReferencePoints) {
            Sphere sphere = new Sphere(5);
            sphere.setMaterial(new PhongMaterial(Color.BLUE.deriveColor(1, 1, 1, 0.5)));
            sphere.setTranslateX(refPointEuclidean.getX());
            sphere.setTranslateY(refPointEuclidean.getY());
            sphere.setTranslateZ(refPointEuclidean.getZ());
            all.getChildren().add(sphere);
        }

        return all;
    }

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Euclidean case: Shifts back fundamental domain if out of bounds
     *
     * @param height
     * @param width
     * @return transform
     */
    public Transform calculateBackShiftEuclidean(double width, double height) {
        if (width < 450) {
            width = 450;
        }
        if (height < 450) {
            height = 450;
        }
        final Point2D windowCorner = new Point2D(-0.5 * width, -0.5 * height);

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        final QuadTree seen = new QuadTree();
        seen.insert(refPoint.getX(), refPoint.getY(), tolerance);

        Transform backShift = new Translate();
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

        while (!isInRangeForFDomainEuclidean(apt, width, height)) { // The loop works as long as the copy of fDomain lies outside the valid range for FDomain
            final Transform t = queue.poll(); // remove t from queue
            if (t != null) {
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
        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        final OctTree seen = new OctTree();
        seen.insert(fDomain.getGeometry(), refPoint, tolerance);

        Transform backShift = new Translate(), t;
        Point3D apt = refPoint, point = refPoint;
        double d = apt.getZ();

        double limiter = ExtendedTiling.getValidHyperbolicRange() * 100 - 30;
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
     * @param width
     * @param height
     * @return
     */
    public boolean isInRangeEuclidean(Point3D point, double width, double height) {
        final Point2D windowCorner = new Point2D(-0.5 * width, -0.5 * height);

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

        return windowCorner.getX() - 250 <= point.getX() && point.getX() <= windowCorner.getX() + width && windowCorner.getY() - 250 <= point.getY() && point.getY() <= windowCorner.getY() + height;
    }

    /**
     * Euclidean case: Checks whether "point" is in visible window
     *
     * @param point
     * @param width
     * @param height
     * @return
     */
    public boolean isInWindowEuclidean(Point3D point, double width, double height) {

        //Checks whether point is in visible window
        if (width < 450) {
            width = 450;
        }
        if (height < 450) {
            height = 450;
        }

        return Math.abs(point.getX()) <= 0.5 * width && Math.abs(point.getY()) <= 0.5 * height;
    }

    /**
     * Euclidean case: checks whether fundamental domain lies in its valid range which is inside visible window.
     *
     * @param point
     * @param width
     * @param height
     * @return
     */
    public boolean isInRangeForFDomainEuclidean(Point3D point, double width, double height) {
        if (width < 450) {
            width = 450;
        }
        if (height < 450) {
            height = 450;
        }

        return Math.abs(point.getX()) <= 0.5 * width - 50 && Math.abs(point.getY()) <= 0.5 * height - 50;
    }

    /**
     * attempt a recycle a copy of the fundamental domain
     *
     * @param transform
     * @param refPoint
     * @param fundamentalDomain
     * @return copy
     */
    private Node useRecycler(Transform transform, Point3D refPoint, Group fundamentalDomain) {
        if (recycler.getChildren().size() == 1) { // Refills recycler if almost empty
            final Group fund = JavaFXUtils.copyGroup(fundamentalDomain); // Copies original fundamental domain used to build up "tiles"
            recycler.getChildren().add(fund);
            if (fund instanceof Group) {
                if (fund.getChildren().size() == 0)
                    throw new RuntimeException("Fund copy empty");
            }
        }
        final Node node = recycler.getChildren().get(0);

        // Reuses a copy of recycler
        node.getTransforms().clear(); // Clear all transforms
        node.getTransforms().add(transform.createConcatenation(transformRecycled)); // Add transform (maps original fundamental domain to actual fundamental domain)
        node.setRotationAxis(refPoint); // Set new point of reference
        return node;
    }

    private Group createCopy(Transform transform, Point3D refPoint, Group fundamentalDomain) {
        final Group copy = JavaFXUtils.copyGroup(fundamentalDomain);
        copy.getTransforms().clear();
        copy.setRotationAxis(refPoint);
        copy.getTransforms().add(transform);
        copy.setUserData("copy");
        return copy;
    }

    private boolean makeCopyHyperbolic(Point3D p) {
        return keptHyperbolicCopy.insert(fDomain.getGeometry(), p, tolerance);
    }

    private boolean makeCopyEuclidean(Point3D p) {
        return keptEuclideanCopy.insert(p.getX(), p.getY(), tolerance);
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
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
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

    /**
     * compute the tolerance
     *
     * @param geometry
     * @param refPoint
     * @param generators
     * @return tolerance
     */
    public static double computeTolerance(final Geometry geometry, Point3D refPoint, Generators generators) {
        double tolerance = 100;
        for (Transform g : generators.getTransforms()) {
            double dist = Tools.distance(geometry, g.transform(refPoint), refPoint);
            if (dist < tolerance) {
                tolerance = dist;
            }
        }
        return 0.8 * tolerance;
    }

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
        return fundamentalDomainMeshes.getHandles();
    }
    //public Transforms getGenerators(){return generators;}


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
        keptHyperbolicCopy.clear();
    }

    public void clearKeptEuclideanCopy() {
        keptEuclideanCopy.clear();
    }

    public Point3D getRefPointHyperbolic() {
        return refPointHyperbolic;
    }

    public Point3D getRefPointEuclidean() {
        return refPointEuclidean;
    }

    public void setRefPointEuclidean(Point3D refPointEuclidean) {
        this.refPointEuclidean = refPointEuclidean;
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
        keptHyperbolicCopy.insert(fDomain.getGeometry(), point, tolerance);
    }

    public void insertKeptEuclideanCopy(Point3D point) {
        keptEuclideanCopy.insert(point.getX(), point.getY(), tolerance);
    }

    public void updateReferenceChamberIndex() {
        referenceChamberIndex = fDomain.computeOptimalChamberIndex();
        tolerance = computeTolerance(fDomain.getGeometry(), fDomain.getChamberCenter3D(referenceChamberIndex), generators);
    }

    public int getReferenceChamberIndex() {
        return referenceChamberIndex;
    }

    public String getGroupName() {
        return fDomain.getGroupName();
    }

    /**
     * gets the status line
     *
     * @return status line
     */
    public String getStatusLine() {
        return String.format("Tiling: %d.%d  Vertices: %d  Edges: %d  Tiles: %d  Symmetry group: %s",
                ds.getNr1(), ds.getNr2(), ds.countOrbits(1, 2), ds.countOrbits(0, 2), ds.countOrbits(0, 1), getGroupName());
    }
}


