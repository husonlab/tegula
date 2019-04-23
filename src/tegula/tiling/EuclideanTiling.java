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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import tegula.main.TilingStyle;
import tegula.tiling.util.QuadTree;
import tegula.util.JavaFXUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static tegula.Options.showReferencePoints;
import static tegula.Options.useNewEuclideanCode;

/**
 * computes a tiling represented by meshes from a fundamental domain represented by meshes
 * <p>
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class EuclideanTiling extends TilingBase {
    final private QuadTree keptEuclideanCopy = new QuadTree();

    private Point3D refPointEuclidean = new Point3D(1, 1, 0);

    private final Group recycler = new Group();
    private Transform transformRecycled = new Translate();
    private Group euclideanFund = new Group();

    private int numberOfCopies;

    private final BooleanProperty inTranslateOrIncreaseTiling = new SimpleBooleanProperty(false);
    private final DoubleProperty width = new SimpleDoubleProperty(600);
    private final DoubleProperty height = new SimpleDoubleProperty(600);

    /**
     * constructor
     *
     * @param tilingStyle
     * @param ds
     */
    public EuclideanTiling(TilingStyle tilingStyle, DSymbol ds) {
        super(tilingStyle, ds);
    }


    /**
     * create tiling
     */
    public ArrayList<Node> createTiling() {
        final ArrayList<Node> all = new ArrayList<>();

        //Add handles
        //handles.getChildren().setAll(((new ReshapeManager(doc).createHandles())));

        if (useNewEuclideanCode) {
            if (!isInWindowEuclidean(getRefPointEuclidean(), getWidth(), getHeight())) { // Fund. domain is not in visible window
                System.err.println("Recenter");
            }
        }

        //Calculation of point of reference:
        if (!useNewEuclideanCode) {
            refPointEuclidean = fDomain.getChamberCenter3D(referenceChamberIndex); // Reference point of actual fundamental domain
            //System.err.println("RefPoint: " + refPointEuclidean);
        }

        final Group fund = fundamentalDomainMeshes.getAll();

        if (!isInTranslateOrIncreaseTiling()) { // need to recompute fundamental domain
            setNumberOfCopies(0);
            fundamentalDomainMeshes.update(ds, fDomain, tilingStyle);

            if (useNewEuclideanCode) {
                refPointEuclidean = new Point3D(fundamentalDomainMeshes.getReference().getTranslateX(), fundamentalDomainMeshes.getReference().getTranslateY(), fundamentalDomainMeshes.getReference().getTranslateZ());

                System.err.println("RefPoint: " + refPointEuclidean);

                tolerance = computeTolerance(fDomain.getGeometry(), refPointEuclidean, generators);
                System.err.println("tolerance: " + tolerance);
            }

            if (showReferencePoints)
                fund.getChildren().addAll(fundamentalDomainMeshes.getReferences().getChildren());

            all.add(fund);
            fund.getTransforms().add(new Translate()); // Add transform (= identity)
            fund.setRotationAxis(refPointEuclidean); // Reference point of fundamental domain

            fund.setUserData("fund");
            setEuclideanFund(fund); // Saves the original fundamental domain
        }


        if (!isDrawFundamentalDomainOnly()) {
            final QuadTree allReferencePoints = new QuadTree(); // Saves reference points of tiles

            allReferencePoints.insert(refPointEuclidean.getX(), refPointEuclidean.getY(), tolerance); // Insert reference point of fDomain

            // Saves transforms for copies
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms()); // Add generators

            for (Transform generator : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                final Point3D genRef = generator.transform(refPointEuclidean); // Reference point for new copy
                if (isInRangeEuclidean(genRef, getWidth(), getHeight()) && allReferencePoints.insert(genRef.getX(), genRef.getY(), tolerance)) { // Checks whether reference point is in valid range and if it is in QuadTree "seen". Adds it if not.
                    if (makeCopyEuclidean(genRef)) { // Checks whether copy fills empty space after translation of tiles
                        if (isInTranslateOrIncreaseTiling()) { // Translate mode of tiling
                            all.add(useRecycler(generator, genRef, getEuclideanFund()));
                        } else {
                            all.add(createCopy(generator, genRef, fund));

                        }
                    }
                }
            }

            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (isInTranslateOrIncreaseTiling() && queue.size() >= 1.5 * getNumberOfCopies()) {
                    return FAILED; // too many rounding errors
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) { // Creates new transforms for copies
                    {
                        final Transform tg = t.createConcatenation(g);
                        final Point3D bpt = tg.transform(getRefPointEuclidean()); // Reference point corresponding to transform tg

                        if (isInRangeEuclidean(bpt, getWidth(), getHeight()) && allReferencePoints.insert(bpt.getX(), bpt.getY(), tolerance)) {
                            queue.add(tg);
                            if (makeCopyEuclidean(bpt)) {
                                if (isInTranslateOrIncreaseTiling()) {
                                    all.add(useRecycler(tg, bpt, getEuclideanFund()));
                                } else {
                                    all.add(createCopy(tg, bpt, fund));
                                }
                            }
                        }
                    }

                    {
                        final Transform gt = g.createConcatenation(t);
                        final Point3D bpt = gt.transform(getRefPointEuclidean());

                        if (isInRangeEuclidean(bpt, getWidth(), getHeight()) && allReferencePoints.insert(bpt.getX(), bpt.getY(), tolerance)) {
                            queue.add(gt);
                            if (makeCopyEuclidean(bpt)) {
                                if (isInTranslateOrIncreaseTiling()) {
                                    all.add(useRecycler(gt, bpt, getEuclideanFund()));
                                } else {
                                    all.add(createCopy(gt, bpt, fund));
                                }
                            }
                        }
                    }
                    if (all.size() > 100000) {
                        System.err.println("Too many copies: " + all.size());
                        break;
                    }
                }
            }
        }

        // only want one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.add(fundamentalDomainMeshes.getChambers());

        if (tilingStyle.isShowHandles())
            all.add(getHandles());

        if (true) {
            final Point2D windowCorner = new Point2D(-0.5 * getWidth() - 10, -0.5 * getHeight() - 10);
            Rectangle rectangle = new Rectangle(windowCorner.getX(), windowCorner.getY(), getWidth(), getHeight());
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(Color.DARKRED);
            all.add(rectangle);
        }

        if (showReferencePoints) {
            Sphere sphere = new Sphere(5);
            sphere.setMaterial(new PhongMaterial(Color.BLUE.deriveColor(1, 1, 1, 0.5)));
            sphere.setTranslateX(refPointEuclidean.getX());
            sphere.setTranslateY(refPointEuclidean.getY());
            sphere.setTranslateZ(refPointEuclidean.getZ());
            all.add(sphere);
        }

        if (!isInTranslateOrIncreaseTiling())
            setNumberOfCopies(all.size());

        return all;
    }

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


    private boolean makeCopyEuclidean(Point3D p) {
        return keptEuclideanCopy.insert(p.getX(), p.getY(), tolerance);
    }


    public void setEuclideanFund(Group g) {
        euclideanFund = g;
    }

    public Group getEuclideanFund() {
        return euclideanFund;
    }


    public void clearKeptEuclideanCopy() {
        keptEuclideanCopy.clear();
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

    public void insertKeptEuclideanCopy(Point3D point) {
        keptEuclideanCopy.insert(point.getX(), point.getY(), tolerance);
    }

    public int getReferenceChamberIndex() {
        return referenceChamberIndex;
    }

    public void setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public boolean isInTranslateOrIncreaseTiling() {
        return inTranslateOrIncreaseTiling.get();
    }

    public BooleanProperty inTranslateOrIncreaseTilingProperty() {
        return inTranslateOrIncreaseTiling;
    }

    public void setInTranslateOrIncreaseTiling(boolean inTranslateOrIncreaseTiling) {
        this.inTranslateOrIncreaseTiling.set(inTranslateOrIncreaseTiling);
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double getHeight() {
        return height.get();
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }
}


