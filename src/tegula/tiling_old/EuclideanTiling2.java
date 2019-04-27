/*
 * EuclideanTiling2.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tiling_old;

import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.main.TilingStyle;
import tegula.tiling_old.util.QuadTree;
import tegula.util.JavaFXUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * euclidean tiling
 * Ruediger Zeller, 2017
 */
public class EuclideanTiling2 extends TilingBase {
    private Point3D windowCorner = new Point3D(-500, -500, 0); // Upper left corner of window in Euclidean case

    private final Group world=new Group();
    private final Group tiles = new Group();

    private final DoubleProperty width = new SimpleDoubleProperty(800);
    private final DoubleProperty height = new SimpleDoubleProperty(600);

    private QuadTree keptEuclideanCopy = new QuadTree();

    private Point3D refPointEuclidean = new Point3D(1, 1, 0);

    private final Group recycler = new Group();
    private Transform transformRecycled = new Translate();
    private Group euclideanFund = new Group();

    private int numberOfCopies=0;

    private final LongProperty updateNumber = new SimpleLongProperty(0); // incremented each time updated

    /**
     * constructor
     *
     * @param tilingStyle
     * @param ds
     */
    public EuclideanTiling2(Pane pane,TilingStyle tilingStyle, DSymbol ds) {
        super(pane,tilingStyle,ds);

        generators = fDomain.getGenerators();

        fundamentalDomain.includeTilesProperty().bind(tilingStyle.showFacesProperty());
        fundamentalDomain.includeBandsProperty().bind(tilingStyle.showBandsProperty());
        fundamentalDomain.includeDecorationsProperty().bind(tilingStyle.showOtherStuffProperty());
        fundamentalDomain.includeChambersProperty().bind(tilingStyle.showAllChambersProperty());
        // do NOT bind handles property

        widthProperty().addListener((c,o,n)->{
            windowCorner=new Point3D(-0.5*n.doubleValue(),windowCorner.getY(),0);
            System.err.println(String.format("Width: %.1f",n.doubleValue()));
            System.err.println(String.format("Corner: %.1f %.1f",windowCorner.getX(),windowCorner.getY()));
        });
        heightProperty().addListener((c,o,n)->{
            windowCorner=new Point3D(windowCorner.getX(),-0.5*n.doubleValue(),0);
            System.err.println(String.format("Height: %.1f",n.doubleValue()));
            System.err.println(String.format("Corner: %.1f %.1f",windowCorner.getX(),windowCorner.getY()));
        });
    }

    public void reset() {
        fDomain = new FDomain(ds);
        fDomain.updateGeneratorsAndContraints();
        generators = fDomain.getGenerators();
    }

    /**
     * clear the tiling
     */
    public void clear() {
        tiles.getChildren().clear();
    }


    /**
     * update the tiling
     */
    public void update() {
        System.err.println("Update");
        tiles.getChildren().clear();

        // Compute tolerance for rounding errors (depends on shape of fundamental domain):
        fDomain.updateGeneratorsAndContraints();
        generators = fDomain.getGenerators();

        // Empty recycler for copies and reset transform for recycled copies.
        recycler.getChildren().clear();
        transformRecycled=(new Translate());

        euclideanFund=new Group();
        keptEuclideanCopy.clear();

            // Calculate optimal chamber, where chamber center is as far away from boundary as possible
            referenceChamberIndex=fDomain.computeOptimalChamberIndex();
            tolerance = computeTolerance(referenceChamberIndex);

        if (!isInWindowEuclidean(refPointEuclidean, windowCorner, width.get(), height.get())) { // Fund. domain is not in visible window
                fDomain.recenterFDomain(calculateBackShiftEuclidean(windowCorner, width.get(), height.get())); // Shifts back fDomain into valid range for fund. domain
            }
            tiles.getChildren().setAll(createTiling( true));
            tiles.getChildren().size();

        //Add rectangles for debugging
         final Group additionalStuff = new Group();

        if (true) {
            final Group rectangles=new Group();
            Rectangle rect = new Rectangle(width.get(), height.get());
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.BLACK);
            Rectangle range = new Rectangle(width.get() + 250, height.get() + 250);
            range.setFill(Color.TRANSPARENT);
            range.setStroke(Color.BLACK);
            Rectangle test = new Rectangle(width.get() + 200, height.get() + 200);
            test.setFill(Color.TRANSPARENT);
            test.setStroke(Color.BLACK);
            Rectangle test2 = new Rectangle(width.get() + 150, height.get() + 150);
            test2.setFill(Color.TRANSPARENT);
            test2.setStroke(Color.BLACK);
            Rectangle small=new Rectangle(40,40);
            small.setFill(Color.TRANSPARENT);
            small.setStroke(Color.ORANGE);

            rectangles.getChildren().addAll(rect, range, test, test2,small);
            additionalStuff.getChildren().add(rectangles);
            rectangles.translateXProperty().bind(widthProperty().multiply(-0.5));
            rectangles.translateYProperty().bind(heightProperty().multiply(-0.5));

            System.err.println(String.format("Rect: %.1f x %.1f",rect.getWidth(),rect.getHeight()));
        }
        world.getChildren().setAll(tiles,additionalStuff);


        updateNumber.set(updateNumber.get() + 1);
    }

    /**
     * create tiling in Euclidean case
     *
     * @return group
     */
    public Collection<Node> createTiling(boolean reset) {
        final double width=getWidth();
        final double height=getHeight();

        //Add all generators
        fDomain.updateGeneratorsAndContraints();
        generators=fDomain.getGenerators();


        //Calculation of point of reference:
        refPointEuclidean = fDomain.getChamberCenter3D(referenceChamberIndex); // Reference point of actual fundamental domain

        final Group all = new Group();
        final Group fund = new Group();

        if (reset) { // need to recompute fundamental domain
            fundamentalDomain.update(ds, fDomain, tilingStyle);
            fund.getChildren().addAll(fundamentalDomain.getAll());

            recycler.getChildren().clear();

            all.getChildren().add(fund);
            fund.getTransforms().add(new Translate()); // Add transform (= identity)
            fund.setRotationAxis(refPointEuclidean); // Reference point of fundamental domain
            euclideanFund=fund; // Saves the original fundamental domain
        }

        if (!isDrawFundamentalDomainOnly()) {
            final QuadTree seen = new QuadTree(); // Saves reference points of tiles
            seen.insert(refPointEuclidean.getX(), refPointEuclidean.getY(), tolerance); // Insert reference point of fDomain

            // Saves transforms for copies
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms()); // Add generators

            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D genRef = g.transform(refPointEuclidean); // Reference point for new copy
                if (isInRangeEuclidean(genRef, windowCorner, width, height) && seen.insert(genRef.getX(), genRef.getY(), tolerance)) { // Checks whether reference point is in valid range and if it is in QuadTree "seen". Adds it if not.
                    if (makeCopyEuclidean(genRef)) { // Checks whether copy fills empty space after translation of tiles
                        if (!reset) { // Translate mode of tiling
                            useRecycler(all, g, genRef, euclideanFund);
                        } else {
                            generateNewCopy(all, g, genRef, fund);
                        }
                    }
                }
            }

            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (false && !reset && queue.size() >= 1.5 * numberOfCopies) {
                    return FAILED;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) { // Creates new transforms for copies
                    Transform tg = t.createConcatenation(g);
                    Point3D bpt = tg.transform(refPointEuclidean); // Reference point corresponding to transform tg

                    if (isInRangeEuclidean(bpt, windowCorner, width, height) && seen.insert(bpt.getX(), bpt.getY(), tolerance)) {
                        queue.add(tg);
                        if (makeCopyEuclidean(bpt)) {
                            if (!reset) {
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
                            if (!reset) {
                                useRecycler(all, gt, bpt,euclideanFund);
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

        System.err.println("End size: "+all.getChildren().size());

        return all.getChildren();
    }

    public void translateFDomain(double dx, double dy) {
        // A filled recycler is a criterion for translation of whole tiling (see translateTiling)
        if (recycler.getChildren().size() > 0) {
            recycler.getChildren().clear();
        }

        keptEuclideanCopy.clear();

        fDomain.translate(dx, dy); // Translates fDomain by vector (dx,dy).


        setDrawFundamentalDomainOnly(true);
        tiles.getChildren().setAll(createTiling(false));
        setDrawFundamentalDomainOnly(false);
        numberOfCopies=tiles.getChildren().size();
    }

    /**
     * translate the tiling
     * @param dx
     * @param dy
     * @return new tiles generated
     */
    public Collection<Node> translateTiling(double dx, double dy) {

        if(!world.getChildren().contains(tiles))
            world.getChildren().add(0,tiles);

        // Translation of whole tiling in Euclidean case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        keptEuclideanCopy.clear();
        Translate translate = new Translate(dx, dy, 0); // Mouse translation (MouseHandler)

        fDomain.translate(dx, dy); // Translates fDomain by vector (dx,dy).
        transformRecycled=translate.createConcatenation(transformRecycled); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain

        final Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex); // Point of reference in Euclidean fundamental domain

        if (!isInWindowEuclidean(refPoint, windowCorner, getWidth(), getHeight())) { // If fundamental domain is out of visible window
            Transform t = calculateBackShiftEuclidean(windowCorner, getWidth(),getHeight());
            transformRecycled=t.createConcatenation(transformRecycled); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
            fDomain.recenterFDomain(t); // Shifts back fDomain into visible window
        }

        //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
        int i = 0;
        while (i < tiles.getChildren().size()) {
            Node node = tiles.getChildren().get(i); // Copy with index i in tile. Each copy is a node of the group "tile".
            if (node.getTransforms().size() > 0) {
                Transform nodeTransform = node.getTransforms().get(0); // get transform of node
                Point3D point = node.getRotationAxis().add(dx, dy, 0); // point = reference point of node (saved as rotation axis) + mouse translation

                if (isInRangeEuclidean(point, windowCorner,getWidth(), getHeight())) {  // keep copy if point still is in valid range
                    node.getTransforms().remove(0); // remove old transforms
                    node.getTransforms().add(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)
                    node.setRotationAxis(point); // "point" serves as new reference of copy
                    keptEuclideanCopy.insert(point.getX(),point.getY(),tolerance); // Save copy as a kept one
                    i++;
                } else { // when point is out of valid range
                    recycler.getChildren().add(node); // Remove node and add to recycler
                }
            } else
                i++;
        }

        if (recycler.getChildren().size() == 0) { // Fill recycler if necessary
            final Group fund = JavaFXUtils.copyGroup(euclideanFund); // Copy original fundamental domain which was used to build "tiles"
            recycler.getChildren().addAll(fund); // Add copy to recycler
        }

        //Second step: Create new tiles ----------------------------------------------------------------------------
        // Create new tiles to fill empty space of valid range. Add new tiles to the group "tiles"
        Collection<Node> newTiles = createTiling(false);

        if (newTiles==FAILED) { // Generates new tiling if too much rounding errors
            reset(); // Reset fundamental domain
            update(); // Update tiling
        } else { // No rounding errors: add new tiles
            tiles.getChildren().addAll(newTiles);
            numberOfCopies=tiles.getChildren().size();
            System.err.println("Number of copies: " + numberOfCopies);
        }
        return world.getChildren();
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

        fDomain.updateGeneratorsAndContraints();
        generators=fDomain.getGenerators();

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


    private void generateNewCopy(Group g, Transform t, Point3D p, Group f) {
        Group group2 = JavaFXUtils.copyGroup(f);
        group2.setRotationAxis(p);
        group2.getTransforms().add(t);
        g.getChildren().add(group2);
    }

    private boolean makeCopyEuclidean(Point3D p) {
        return keptEuclideanCopy.insert(p.getX(), p.getY(), tolerance);
    }

    private void useRecycler(Group g, Transform t, Point3D p, Group domain) {
        if (recycler.getChildren().size() == 1) { // Refills recycler if almost empty
            final Group fund = JavaFXUtils.copyGroup(domain); // Copies original fundamental domain used to build up "tiles"
            if (fund != null && fund.getChildren().size() == 0)
                    throw new RuntimeException("Fund copy empty");
            recycler.getChildren().add(fund);
        }
        final Node node = recycler.getChildren().get(0);

        // Reuses a copy of recycler
        node.getTransforms().clear(); // Clear all transforms
        node.getTransforms().add(t.createConcatenation(transformRecycled)); // Add transform (maps original fundamental domain to actual fundamental domain)
        node.setRotationAxis(p); // Set new point of reference
        g.getChildren().add(node);
    }

    public Group getWorld() {
        return world;
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
