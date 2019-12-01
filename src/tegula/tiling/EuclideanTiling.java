/*
 * EuclideanTiling.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.reshape.ReshapeManager;
import tegula.main.TilingStyle;
import tegula.tiling.parts.QuadTree;
import tegula.util.Updateable;

import java.util.LinkedList;
import java.util.Queue;

/**
 * a euclidean tiling
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class EuclideanTiling extends TilingBase implements TilingCreator {
    public static final Group FAILED = new Group();

    private final QuadTree coveredPoints = new QuadTree();

    private Point3D referencePoint = fDomain.computeReferencePoint(); //new Point3D(1, 1, 0);

    private Transform transformRecycled = new Translate();

    private final Updateable doc;

    private final Point3D windowCorner = new Point3D(0, 0, 0); // Upper left corner of window in Euclidean case

    private final DoubleProperty width = new SimpleDoubleProperty(800);
    private final DoubleProperty height = new SimpleDoubleProperty(800);

    /**
     * constructor
     *
     * @param ds
     */
    public EuclideanTiling(DSymbol ds, TilingStyle tilingStyle, Updateable doc) {
        super(ds, tilingStyle);
        this.doc = doc;
    }

    /**
     * update the tiling
     */
    public Group update() {
        final Group tiles = produceTiles(true);
        setNumberOfCopies(tiles.getChildren().size());
        return tiles;
    }

    /**
     * produces tiles
     *
     * @return group
     */
    private Group produceTiles(boolean reset) {
        generators = fDomain.getGenerators();

        //Add handles
        handles.getChildren().setAll(((new ReshapeManager(this, doc).createHandles())));

        final Group all = new Group();

        if (reset) { // need to recompute fundamental domain
            recycler.clear();
            coveredPoints.clear();

            //Tolerance for rounding errors in QuadTree
            tolerance = computeTolerance(getGeometry(), referencePoint, generators);


            //Prototype of fDomain (for copies)
            fundPrototype.getChildren().clear();
            fundPrototype.getChildren().setAll(FundamentalDomain.compute(ds, fDomain, tilingStyle));
            all.getChildren().add(provideCopy(new Translate(), fundPrototype)); // Add identity to Prototype
        }

        if (!isDrawFundamentalDomainOnly()) {
            final QuadTree seen = new QuadTree(); // Saves reference points of tiles

            Point3D pt = transformRecycled.transform(referencePoint);
            seen.insert(pt.getX(), pt.getY(), tolerance); // Insert reference point of fDomain

            // Saves transforms for copies
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms()); // Add generators

            for (Transform generator : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D ref = transformRecycled.createConcatenation(generator).transform(referencePoint); // Reference point for new copy
                if (isInRangeEuclidean(ref, windowCorner, getWidth(), getHeight()) && seen.insert(ref.getX(), ref.getY(), tolerance)) { // Checks whether reference point is in valid range and if it is in QuadTree "seen". Adds it if not.
                    if (insertCoveredPoint(ref)) { // Checks whether copy fills empty space after translation of tiles
                        all.getChildren().add(provideCopy(generator, fundPrototype));
                    }
                }
            }

            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (!reset && queue.size() >= 1.5 * getNumberOfCopies()) {
                    return FAILED;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) { // Creates new transforms for copies
                    {
                        final Transform tg = t.createConcatenation(g);
                        final Point3D ref = transformRecycled.createConcatenation(tg).transform(referencePoint); // Reference point corresponding to transform tg

                        if (isInRangeEuclidean(ref, windowCorner, getWidth(), getHeight()) && seen.insert(ref.getX(), ref.getY(), tolerance)) {
                            queue.add(tg);
                            if (insertCoveredPoint(ref)) {
                                all.getChildren().add(provideCopy(tg, fundPrototype));
                            }
                        }
                    }

                    {
                        final Transform gt = g.createConcatenation(t);
                        final Point3D ref = transformRecycled.createConcatenation(gt).transform(referencePoint);

                        if (isInRangeEuclidean(ref, windowCorner, getWidth(), getHeight()) && seen.insert(ref.getX(), ref.getY(), tolerance)) {
                            queue.add(gt);
                            if (insertCoveredPoint(ref)) {
                                all.getChildren().add(provideCopy(gt, fundPrototype));
                            }
                        }
                    }
                }
            }
        }
        return all;
    }

    /**
     * translate the tiling
     *
     * @param dx
     * @param dy
     * @param tiles
     */
    public void translateTiling(double dx, double dy, Group tiles) {

        // QuadTree is used for saving copies which are kept under translation
        coveredPoints.clear();

        Translate translate = new Translate(dx, dy, 0); // Mouse translation (MouseHandler)

        transformRecycled = translate.createConcatenation(transformRecycled); // Translates tiles to correct place

        final Point3D refPoint = transformRecycled.transform(referencePoint); // Translates reference point to correct place

        //Recompute transformRecycled if necessary
        if (!isInWindowEuclidean(refPoint, windowCorner, width.get(), height.get())) { // If transformRecycled shifts fDomain out of visible window
            Transform t = calculateBackShiftEuclidean(windowCorner, width.get(), height.get());
            transformRecycled = transformRecycled.createConcatenation(t); // new transformRecycled does not shift out of window
        }

        //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
        int i = 0;
        while (i < tiles.getChildren().size()) {
            Group group = (Group) tiles.getChildren().get(i); // Copy with index i in tile. Each copy is a node of the group "tile".
            if (group.getTransforms().size() > 0) {
                Transform nodeTransform = group.getTransforms().get(0); // get transform of node
                Point3D point = translate.createConcatenation(nodeTransform).transform(referencePoint);

                if (isInRangeEuclidean(point, windowCorner, width.get(), height.get())) {  // keep copy if point still is in valid range
                    group.getTransforms().setAll(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)
                    if (!insertCoveredPoint(point)) // Save copy as a kept one
                        System.err.println("Already present");

                    i++;
                } else { // point is out of valid range
                    tiles.getChildren().remove(i);
                    recycler.push(group); // Remove node and add to recycler
                }
            } else
                i++;
        }

        //Second step: Create new tiles ----------------------------------------------------------------------------
        // Create new tiles to fill empty space of valid range. Add new tiles to the group "tiles"
        Group newTiles = produceTiles(false);

        if (newTiles == FAILED) { // Generates new tiling if too many rounding errors
            reset(); // Reset fundamental domain
            tiles.getChildren().setAll(produceTiles(true));
        } else { // No rounding errors: add new tiles
            tiles.getChildren().addAll(newTiles.getChildren());
            setNumberOfCopies(tiles.getChildren().size());
            //System.err.println("Number of copies: " + getNumberOfCopies());
        }
    }

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
        generators = fDomain.getGenerators();

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = transformRecycled.transform(referencePoint);

        final QuadTree seen = new QuadTree();
        seen.insert(refPoint.getX(), refPoint.getY(), tolerance);

        Transform backShift = new Translate(), t;
        Point3D point = refPoint, apt = refPoint;
        Point3D midpoint = new Point3D(windowCorner.getX() + width / 2, windowCorner.getY() + height / 2, 0);
        double d = point.distance(midpoint);

        for (Transform g : generators.getTransforms()) {
            point = transformRecycled.createConcatenation(g).transform(referencePoint);
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
                point = transformRecycled.createConcatenation(tg).transform(referencePoint);

                if (seen.insert(point.getX(), point.getY(), tolerance)) { // Creates a tree of points lying in the copies of fDomain
                    if (point.distance(midpoint) < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                        d = point.distance(midpoint);
                        backShift = tg;
                        apt = point;
                    }
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                point = transformRecycled.createConcatenation(gt).transform(referencePoint);

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

    /**
     * provides a copy of the fundamental domain, using the recycler, if possible
     *
     * @param transform
     * @param fund
     * @return copy
     */
    private Node provideCopy(Transform transform, Group fund) {
        final Node copy = (recycler.size() > 0 ? recycler().pop() : CopyTiles.apply(fund));
        copy.getTransforms().setAll(transformRecycled.createConcatenation(transform));
        return copy;
    }


    @Override
    public void reset() {
        super.reset();
        fundPrototype.getChildren().clear();
        coveredPoints.clear();
        referencePoint = null;
        recycler.clear();
    }

    /**
     * attempt to insert point to quad tree of covered points
     *
     * @param p
     * @return false, if point is already covered
     */
    private boolean insertCoveredPoint(Point3D p) {
        return coveredPoints.insert(p.getX(), p.getY(), tolerance);
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
