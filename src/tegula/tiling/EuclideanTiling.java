/*
 * EuclideanTiling.java Copyright (C) 2020. Daniel H. Huson
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
import jloda.fx.window.NotificationManager;
import jloda.util.ProgramProperties;
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
    private static long previousWarning = 0L;

    private final QuadTree coveredPoints = new QuadTree();

    private Point3D referencePoint = getfDomain().computeReferencePoint(); //new Point3D(1, 1, 0);

    private Transform transformRecycled = new Translate();

    private final Updateable doc;

    private final DoubleProperty widthProperty = new SimpleDoubleProperty(800);
    private final DoubleProperty heightProperty = new SimpleDoubleProperty(800);

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
        if (reset)
            setNumberOfCopies(0);
        generators = getfDomain().getGenerators();

        //Add handles
        handles.getChildren().setAll(((new ReshapeManager(this, doc).createHandles())));

        final Group all = new Group();

        final double margin = 100 * Math.max(getfDomain().getBoundingBox().getWidth(), getfDomain().getBoundingBox().getHeight());

        if (reset) { // need to recompute fundamental domain
            recycler.clear();
            coveredPoints.clear();

            //Tolerance for rounding errors in QuadTree
            tolerance = computeTolerance(getGeometry(), referencePoint, generators);

            //Prototype of fDomain (for copies)
            fundPrototype.getChildren().clear();
            fundPrototype.getChildren().setAll(FundamentalDomain.compute(ds, getfDomain(), tilingStyle));
            all.getChildren().add(provideCopy(new Translate(), fundPrototype)); // Add identity to Prototype
        }

        if (!isDrawFundamentalDomainOnly()) {
            final int maxCopies = ProgramProperties.get("MaxCopiesEuclidean", 5000);
            final QuadTree seen = new QuadTree(); // Saves reference points of tiles

            Point3D pt = transformRecycled.transform(referencePoint);
            seen.insert(pt.getX(), pt.getY(), tolerance); // Insert reference point of fDomain

            // Saves transforms for copies
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms()); // Add generators

            for (Transform generator : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D ref = transformRecycled.createConcatenation(generator).transform(referencePoint); // Reference point for new copy
                if (isInWindowEuclidean(ref, getWidth(), getHeight(), margin) && seen.insert(ref.getX(), ref.getY(), tolerance)) { // Checks whether reference point is in valid range and if it is in QuadTree "seen". Adds it if not.
                    if (insertCoveredPoint(ref)) { // Checks whether copy fills empty space after translation of tiles
                        all.getChildren().add(provideCopy(generator, fundPrototype));
                    }
                }
            }

            final long start = System.currentTimeMillis();

            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (!reset && queue.size() >= 1.5 * getNumberOfCopies()) {
                    return FAILED;
                }
                if (getNumberOfCopies() + all.getChildren().size() > maxCopies) {
                    if (System.currentTimeMillis() - previousWarning > 10000) {
                        NotificationManager.showWarning("Exceeded max copies: " + maxCopies);
                        previousWarning = System.currentTimeMillis();
                    }
                    break;
                }
                if (System.currentTimeMillis() - start > 10000) {
                    if (System.currentTimeMillis() - previousWarning > 10000) {
                        NotificationManager.showWarning("Exceeded max computation time (10sec)");
                        previousWarning = System.currentTimeMillis();
                    }
                    break;
                }

                final Transform t = queue.poll(); // remove t from queue

                if (t != null) {
                    for (Transform g : generators.getTransforms()) { // Creates new transforms for copies
                        {
                            final Transform tg = t.createConcatenation(g);
                            final Point3D ref = transformRecycled.createConcatenation(tg).transform(referencePoint); // Reference point corresponding to transform tg

                            if (isInWindowEuclidean(ref, getWidth(), getHeight(), margin) && seen.insert(ref.getX(), ref.getY(), tolerance)) {
                                queue.add(tg);
                                if (insertCoveredPoint(ref)) {
                                    all.getChildren().add(provideCopy(tg, fundPrototype));
                                }
                            }
                        }

                        {
                            final Transform gt = g.createConcatenation(t);
                            final Point3D ref = transformRecycled.createConcatenation(gt).transform(referencePoint);

                            if (isInWindowEuclidean(ref, getWidth(), getHeight(), margin) && seen.insert(ref.getX(), ref.getY(), tolerance)) {
                                queue.add(gt);
                                if (insertCoveredPoint(ref)) {
                                    all.getChildren().add(provideCopy(gt, fundPrototype));
                                }
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
        if (!isInWindowEuclidean(refPoint, getWidth(), getHeight(), 50)) { // If transformRecycled shifts fDomain out of visible window
            Transform t = calculateBackShiftEuclidean(getWidth(), getHeight());
            transformRecycled = transformRecycled.createConcatenation(t); // new transformRecycled does not shift out of window
        }

        final double margin = 100 * Math.max(getfDomain().getBoundingBox().getWidth(), getfDomain().getBoundingBox().getHeight());

        //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
        int i = 0;
        while (i < tiles.getChildren().size()) {
            final Group group = (Group) tiles.getChildren().get(i); // Copy with index i in tile. Each copy is a node of the group "tile".
            if (group.getTransforms().size() > 0) {
                final Transform nodeTransform = group.getTransforms().get(0); // get transform of node
                final Point3D point = translate.createConcatenation(nodeTransform).transform(referencePoint);

                if (isInWindowEuclidean(point, getWidth(), getHeight(), margin)) {  // keep copy if point still is in valid range
                    group.getTransforms().setAll(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)
                    insertCoveredPoint(point);
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
     * @return transform
     */
    public Transform calculateBackShiftEuclidean(double width, double height) {
        //Add all generators
        generators = getfDomain().getGenerators();

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = transformRecycled.transform(referencePoint);

        final QuadTree seen = new QuadTree();
        seen.insert(refPoint.getX(), refPoint.getY(), tolerance);

        Transform backShift = new Translate(), t;
        Point3D point = refPoint, apt = refPoint;
        Point3D midpoint = new Point3D(0.5 * width, 0.5 * height, 0);
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

        while (!isInWindowEuclidean(apt, width, height, -5)) { // The loop works as long as the copy of fDomain lies outside the valid range for FDomain
            t = queue.poll(); // remove t from queue

            if (t != null) {
                boolean changed = false;
                for (Transform g : generators.getTransforms()) {
                    Transform tg = t.createConcatenation(g);
                    point = transformRecycled.createConcatenation(tg).transform(referencePoint);

                    if (seen.insert(point.getX(), point.getY(), tolerance)) { // Creates a tree of points lying in the copies of fDomain
                        if (point.distance(midpoint) < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                            d = point.distance(midpoint);
                            backShift = tg;
                            apt = point;
                            changed = true;
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
                            changed = true;
                        }
                        queue.add(gt);
                    }
                }
                if (!changed)
                    break;
            }
        }
        //System.err.println("backshift (copies: "+getNumberOfCopies()+")");
        return backShift;
    }

    /**
     * Euclidean case: Checks whether "point" is in visible window
     *
     * @param point
     * @param width
     * @param height
     * @return
     */
    public boolean isInWindowEuclidean(Point3D point, double width, double height, double margin) {
        return -margin < point.getX() && point.getX() < width + margin && -margin < point.getY() && point.getY() < height + margin;
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
        referencePoint = new Point3D(1, 1, 0);
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
        return widthProperty.get();
    }

    public DoubleProperty widthProperty() {
        return widthProperty;
    }

    public void setWidth(double width) {
        this.widthProperty.set(width);
    }

    public double getHeight() {
        return heightProperty.get();
    }

    public DoubleProperty heightProperty() {
        return heightProperty;
    }

    public void setHeight(double height) {
        this.heightProperty.set(height);
    }

}
