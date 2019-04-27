/*
 * Tiling.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.core.reshape.ReshapeManager;
import tiler.geometry.Tools;
import tiler.main.Document;
import tiler.main.TilingStyle;
import tiler.tiling.parts.OctTree;
import tiler.tiling.parts.QuadTree;
import tiler.util.JavaFXUtils;
import tiler.util.Updateable;

import java.util.LinkedList;
import java.util.Queue;

/**
 * a tiling
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class Tiling extends TilingBase {

    private OctTree keptHyperbolicCopy;
    private QuadTree keptEuclideanCopy = new QuadTree();

    private Point3D refPointHyperbolic = new Point3D(0, 0, 1);
    private Point3D refPointEuclidean = new Point3D(1, 1, 0);

    private final Group recycler = new Group();
    private Transform transformRecycled = new Translate();
    private Group euclideanFund = new Group();
    private Group hyperbolicFund = new Group();

    private Group handles = new Group();

    private int numberOfCopies = 0;

    private double tolerance = 0.0;
    private int referenceChamberIndex = 0;

    /**
     * constructor
     *
     * @param ds
     */
    public Tiling(DSymbol ds, TilingStyle tilingStyle) {
        super(ds, tilingStyle);
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


    public String getGroupName() {
        return groupName;
    }

    /**
     * gets the status line
     *
     * @return status line
     */
    public String getStatusLine() {
        return String.format("Tiling: %d.%d  Tiles: %d  Edges: %d  Vertices: %d  Symmetry group: %s",
                ds.getNr1(), ds.getNr2(), ds.countOrbits(0, 1), ds.countOrbits(0, 2), ds.countOrbits(1, 2), getGroupName());
    }

//----------------------------------------------------------------------------------------------------------------------

    /**
     * Computes tolerance for rounding errors. Tolerance depends on shape of fundamental domain.
     *
     * @return tolerance
     */
    public double computeTolerance(int referenceChamberIndex) {
        final Point3D refPoint;
        if (super.getGeometry() == Geometry.Euclidean) {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        } else {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex).multiply(0.01);
        }
        double tolerance = 100;
        for (Transform g : generators.getTransforms()) {
            double dist = Tools.distance(super.getGeometry(), g.transform(refPoint), refPoint);
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
    public Group createTilingSpherical() {
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
            fund.getChildren().add(fundamentalDomain.getDecorations());


        all.getChildren().add(fund);

        // Make copies of fundamental domain.
        final OctTree seen = new OctTree();
        final Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex).multiply(0.01); // refPoint lies on unit sphere
        seen.insert(super.getGeometry(), refPoint, tolerance); //root node of OctTree is point of reference.

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());
        for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
            Point3D genRef = g.transform(refPoint);
            if (seen.insert(super.getGeometry(), genRef, tolerance)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
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
                if (seen.insert(super.getGeometry(), bpt, tolerance)) {
                    final Group group2 = JavaFXUtils.copyGroup(fund);
                    group2.getTransforms().add(tg);
                    all.getChildren().add(group2);
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                bpt = gt.transform(refPoint);
                if (seen.insert(super.getGeometry(), bpt, tolerance)) {
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
    public Group createTilingHyperbolic(boolean drawFundamentalDomainOnly, double maxDist) {
        //System.err.println("Create Hyperbolic Tiling");

        handles.getChildren().clear();

        //Add all generators
        fDomain.updateGeneratorsAndContraints();

        //System.out.println(refPointHyperbolic);

        final Group all = new Group();
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
                fund.getChildren().add(fundamentalDomain.getDecorations());

            fund.setRotationAxis(refPointHyperbolic);
            fund.getTransforms().add(new Translate());
            setHyperbolicFund(fund);
            all.getChildren().add(fund);
        }

        if (!drawFundamentalDomainOnly) {
            // Make copies of fundamental domain.
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

            final OctTree seen = new OctTree();
            seen.insert(getGeometry(), refPointHyperbolic, tolerance);

            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D genRef = g.transform(refPointHyperbolic);
                if (seen.insert(getGeometry(), genRef, tolerance)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
                    if (makeCopyHyperbolic(genRef)) {
                        if (translateOrIncreaseTiling()) {
                            useRecycler(all, g, genRef, getHyperbolicFund());
                        } else {
                            generateNewCopy(all, g, genRef, fund);
                        }
                    }
                }
            }

            int countChildren = 0;
            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (translateOrIncreaseTiling() && getNumberOfCopies() > 0 && countChildren >= 1.5 * getNumberOfCopies()) {
                    System.out.println(countChildren + " children and " + getNumberOfCopies() + " copies");
                    return FAILED;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) {
                    Transform tg = t.createConcatenation(g);
                    Point3D bpt = tg.transform(refPointHyperbolic);
                    if (seen.insert(super.getGeometry(), bpt, tolerance) && bpt.getZ() < maxDist) {
                        countChildren++;
                        queue.add(tg);
                        if (makeCopyHyperbolic(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(all, tg, bpt, getHyperbolicFund());
                            } else {
                                generateNewCopy(all, tg, bpt, fund);
                            }
                        }
                    }

                    Transform gt = g.createConcatenation(t);
                    bpt = gt.transform(refPointHyperbolic);
                    if (seen.insert(super.getGeometry(), bpt, tolerance) && bpt.getZ() < maxDist) {
                        countChildren++;
                        queue.add(gt);
                        if (makeCopyHyperbolic(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(all, gt, bpt, getHyperbolicFund());
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

    /**
     * create tiling in Euclidean case
     *
     * @param windowCorner
     * @param width
     * @param height
     * @return group
     */
    public Group createTilingEuclidean(Updateable doc, boolean drawFundamentalDomainOnly, Point3D windowCorner, double width, double height) {

        //Add all generators
        fDomain.updateGeneratorsAndContraints();
        generators = fDomain.getGenerators();

        //Add handles
        handles.getChildren().setAll(((new ReshapeManager(this, doc).createHandles())));

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
                fund.getChildren().add(fundamentalDomain.getDecorations());

            all.getChildren().add(fund);
            fund.getTransforms().add(new Translate()); // Add transform (= identity)
            fund.setRotationAxis(refPointEuclidean); // Reference point of fundamental domain
            setEuclideanFund(fund); // Saves the original fundamental domain
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
                            useRecycler(all, g, genRef, getEuclideanFund());
                        } else {
                            generateNewCopy(all, g, genRef, fund);
                        }
                    }
                }
            }

            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (translateOrIncreaseTiling() && queue.size() >= 1.5 * getNumberOfCopies()) {
                    return FAILED;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) { // Creates new transforms for copies
                    Transform tg = t.createConcatenation(g);
                    Point3D bpt = tg.transform(getRefPointEuclidean()); // Reference point corresponding to transform tg

                    if (isInRangeEuclidean(bpt, windowCorner, width, height) && seen.insert(bpt.getX(), bpt.getY(), tolerance)) {
                        queue.add(tg);
                        if (makeCopyEuclidean(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(all, tg, bpt, getEuclideanFund());
                            } else {
                                generateNewCopy(all, tg, bpt, fund);
                            }
                        }
                    }

                    Transform gt = g.createConcatenation(t);
                    bpt = gt.transform(getRefPointEuclidean());

                    if (isInRangeEuclidean(bpt, windowCorner, width, height) && seen.insert(bpt.getX(), bpt.getY(), tolerance)) {
                        queue.add(gt);
                        if (makeCopyEuclidean(bpt)) {
                            if (translateOrIncreaseTiling()) {
                                useRecycler(all, gt, bpt, getEuclideanFund());
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
        fDomain.updateGeneratorsAndContraints();

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
        fDomain.updateGeneratorsAndContraints();

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        final OctTree seen = new OctTree();
        seen.insert(super.getGeometry(), refPoint, tolerance);

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

                if (seen.insert(super.getGeometry(), point, tolerance)) { // Creates a tree of points lying in the copies of fDomain
                    if (point.getZ() < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                        d = point.getZ();
                        backShift = tg;
                        apt = point;
                    }
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                point = gt.transform(refPoint);

                if (seen.insert(super.getGeometry(), point, tolerance)) {
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
            if (fund != null) {
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

    private void generateNewCopy(Group targetGroup, Transform transform, Point3D refPoint, Group fund) {
        Group group2 = JavaFXUtils.copyGroup(fund);
        group2.setRotationAxis(refPoint);
        group2.getTransforms().setAll(transform);
        targetGroup.getChildren().add(group2);
    }

    private boolean makeCopyHyperbolic(Point3D p) {
        return keptHyperbolicCopy.insert(super.getGeometry(), p, tolerance);
    }

    private boolean makeCopyEuclidean(Point3D p) {
        return keptEuclideanCopy.insert(p.getX(), p.getY(), tolerance);
    }

    private boolean translateOrIncreaseTiling() {
        return recycler.getChildren().size() > 0;
    }

    public DSymbol getDSymbol() {
        return ds;
    }

    public Geometry getGeometry() {
        return super.getGeometry();
    }

    public FDomain getfDomain() {
        return fDomain;
    }

    public Group getHandles() {
        return handles;
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
        keptHyperbolicCopy.insert(super.getGeometry(), point, getTolerance());
    }

    public void insertKeptEuclideanCopy(Point3D point) {
        keptEuclideanCopy.insert(point.getX(), point.getY(), getTolerance());
    }

    public Generators getGenerators() {
        return generators;
    }
}
