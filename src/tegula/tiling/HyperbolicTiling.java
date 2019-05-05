/*
 * HyperbolicTiling.java Copyright (C) 2019. Daniel H. Huson
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
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.geometry.Tools;
import tegula.main.TilingStyle;
import tegula.tiling.parts.OctTree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * a hyperbolic tiling
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class HyperbolicTiling extends TilingBase implements TilingCreator {
    public static final double ValidHyperbolicRange = 4.8;

    private final OctTree coveredPoints = new OctTree();

    private Point3D referencePoint = new Point3D(0, 0, 1);

    private Transform transformRecycled = new Translate();

    private Group handles = new Group();

    private Point2D transVector = new Point2D(0, 0);
    private boolean changeDirection;

    /**
     * constructor
     *
     * @param ds
     */
    public HyperbolicTiling(DSymbol ds, TilingStyle tilingStyle) {
        super(ds, tilingStyle);
    }

    /**
     * update the tiling
     */
    public Group update() {
        recycler.clear();
        transformRecycled = new Translate();
        fundPrototype.getChildren().clear();
        coveredPoints.clear();

        generators = fDomain.getGenerators();

        referencePoint = fDomain.computeReferencePoint();
        tolerance = computeTolerance(getGeometry(), referencePoint, generators);

        fundamentalDomain.buildFundamentalDomain(ds, fDomain, tilingStyle);

        double diameterFDomain = fDomain.calculateDiameter();
        if (2.8 * diameterFDomain > tilingStyle.getHyperbolicLimit()) {
            tilingStyle.setHyperbolicLimit((int) Math.round(2.8 * diameterFDomain));
        }

        if (referencePoint.getZ() >= ValidHyperbolicRange) {// Fundamental domain is shifted back
            fDomain.recenterFDomain(calculateBackShiftHyperbolic()); // Shifts back fDomain into valid range (slower algorithm)
        }

        final Group tiles = produceTiles(true);
        setNumberOfCopies(tiles.getChildren().size());
        return tiles;
    }

    /**
     * produces hyperbolic tiles
     */
    private Group produceTiles(boolean reset) {
        final double maxDist = Math.cosh(0.5 * tilingStyle.getHyperbolicLimit());
        //System.err.println("Create Hyperbolic Tiling");

        handles.getChildren().clear();

        //Add all generators
        generators = fDomain.getGenerators();

        //System.out.println(refPointHyperbolic);

        final Group all = new Group();

        referencePoint = fDomain.computeReferencePoint();
        tolerance = computeTolerance(getGeometry(), referencePoint, generators);

        if (reset) { // need to recompute fundamental domain
            fundamentalDomain.buildFundamentalDomain(ds, fDomain, tilingStyle);
            fundPrototype.getChildren().setAll(fundamentalDomain.getAllRequested());

            fundPrototype.setRotationAxis(referencePoint);
            fundPrototype.getTransforms().setAll(new Translate());

            all.getChildren().add(provideCopy(new Translate(), referencePoint, fundPrototype));
        }

        if (!isDrawFundamentalDomainOnly()) {
            final OctTree seen = new OctTree();
            seen.insert(getGeometry(), referencePoint, tolerance);

            // Make copies of fundamental domain.
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                final Point3D ref = g.transform(referencePoint);
                if (seen.insert(getGeometry(), ref, tolerance)) {    // Checks whether point "ref" is in OctTree "seen". Adds it if not.
                    if (insertCoveredPoint(ref)) {
                        all.getChildren().add(provideCopy(g, ref, fundPrototype));
                    }
                }
            }

            int countChildren = 0;
            while (queue.size() > 0) {
                // Breaks while loop if too many copies (rounding errors)
                if (!reset && getNumberOfCopies() > 0 && countChildren >= 1.5 * getNumberOfCopies()) {
                    System.out.println(countChildren + " children and " + getNumberOfCopies() + " copies");
                    return FAILED;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) {
                    {
                        final Transform tg = t.createConcatenation(g);
                        final Point3D ref = tg.transform(referencePoint);
                        if (seen.insert(getGeometry(), ref, tolerance) && ref.getZ() < maxDist) {
                            countChildren++;
                            queue.add(tg);
                            if (insertCoveredPoint(ref)) {
                                all.getChildren().add(provideCopy(tg, ref, fundPrototype));

                            }
                        }
                    }
                    {
                        final Transform gt = g.createConcatenation(t);
                        final Point3D ref = gt.transform(referencePoint);
                        if (seen.insert(getGeometry(), ref, tolerance) && ref.getZ() < maxDist) {
                            countChildren++;
                            queue.add(gt);
                            if (insertCoveredPoint(ref)) {
                                all.getChildren().add(provideCopy(gt, ref, fundPrototype));
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

    /**
     * translate the tiling
     *
     * @param dx
     * @param dy
     * @param tiles
     */
    public void translateTiling(double dx, double dy, Group tiles) {
        changeDirection = false;

        dx /= 300;
        dy /= 300;
        double maxDist = Math.cosh(0.5 * tilingStyle.getHyperbolicLimit());

        // Calculate hyperbolic translation of group:
        Transform translate = Tools.hyperbolicTranslation(dx, dy);

        // OctTree is used for saving copies which are kept under translation
        coveredPoints.clear();

        // Translates fDomain by vector (dx,dy).
        fDomain.translate(dx, dy);
        transformRecycled = translate.createConcatenation(transformRecycled); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain


        // Recenter fDomain if too far away from center
        final Point3D refPoint = fDomain.computeReferencePoint();
        tolerance = computeTolerance(getGeometry(), refPoint, generators);

        if (refPoint.getZ() >= ValidHyperbolicRange) {
            final Transform t = calculateBackShiftHyperbolic();
            if (t instanceof Translate) {
                System.err.println("calculateBackShiftHyperbolic: failed");
                calculateBackShiftHyperbolic();
            }
            fDomain.recenterFDomain(t); // Shifts back fDomain into valid range
            transformRecycled = t.createConcatenation(transformRecycled); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
        }

        //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
        int i = 0;
        while (i < tiles.getChildren().size()) {
            final Group group = (Group) tiles.getChildren().get(i);
            if (group instanceof Group) {
                if (group.getChildren().size() == 0)
                    throw new RuntimeException("Fund copy empty");
            }

            final Transform nodeTransform = group.getTransforms().get(0);
            final Point3D point = translate.transform(group.getRotationAxis()); // point = translated reference point of node

            if (point.getZ() > maxDist || !insertCoveredPoint(point)) {
                tiles.getChildren().remove(i);
                recycler.push(group); // Remove node and add to recycler
            } else {
                group.getTransforms().setAll(translate.createConcatenation(nodeTransform));
                group.setRotationAxis(point);
                // if (!insertCoveredPoint(point))
                //     System.err.println("Already present");
                i++;
            }
        }

        //Second step: Create new tiles ----------------------------------------------------------------------------
        Group newTiles = produceTiles(false);
        if (newTiles == FAILED) { // Generates new tiling if too much rounding errors
            reset(); // Reset fundamental domain
            tiles.getChildren().setAll(produceTiles(true)); // Update tiling
        } else { // No rounding errors: add new tiles
            tiles.getChildren().addAll(newTiles.getChildren());
        }
        setNumberOfCopies(tiles.getChildren().size());
    }

    /**
     * Deletes copies of fundamental domain in hyperbolic case when less tiles are shown.
     */
    public void decreaseTiling(Group tiles) {
        tilingStyle.setHyperbolicLimit(tilingStyle.getHyperbolicLimit() - 1);

        double maxDist = Math.cosh(0.5 * tilingStyle.getHyperbolicLimit());
        int bound = tiles.getChildren().size();
        for (int i = 1; i <= bound; i++) {
            final Group group = (Group) tiles.getChildren().get(bound - i);
            if (group.getRotationAxis().getZ() > maxDist) {
                tiles.getChildren().remove(bound - i);
                recycler.add(group);
            }
        }
    }

    /**
     * Adds copies of fundamental domain in hyperbolic case when more tiles are shown
     */
    public void increaseTiling(Group tiles) {
        tilingStyle.setHyperbolicLimit(tilingStyle.getHyperbolicLimit() + 1);

        coveredPoints.clear();
        for (int i = 0; i < tiles.getChildren().size(); i++) {
            insertCoveredPoint(tiles.getChildren().get(i).getRotationAxis()); // Add existing tiles to tree structure
        }

        setNumberOfCopies(0);
        // Add new tiles
        Group newTiles = produceTiles(false);
        tiles.getChildren().addAll(newTiles.getChildren());
        setNumberOfCopies(tiles.getChildren().size());
    }

    /**
     * Hyperbolic case: Transform shifting back fundamental domain if out of bounds
     *
     * @return transform
     */
    public Transform calculateBackShiftHyperbolic() {

        //Add all generators
        generators = fDomain.getGenerators();

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

        Point3D refPoint = getfDomain().computeReferencePoint();
        tolerance = computeTolerance(getGeometry(), refPoint, generators);

        final OctTree seen = new OctTree();
        seen.insert(getGeometry(), refPoint, tolerance);

        Transform backShift = new Translate(), t;
        Point3D apt = refPoint;
        double d = apt.getZ();

        final double limit = ValidHyperbolicRange - 0.3;
        int counter = 0;
        while (apt.getZ() >= limit) { // The loop works as long as the copy of fDomain lies outside the valid range
            counter++;
            if (counter >= 250) {
                System.out.println("Loop back shift. BREAK");
                break;
            }
            t = queue.poll(); // remove t from queue
            for (Transform g : generators.getTransforms()) {
                {
                    final Transform tg = t.createConcatenation(g);
                    final Point3D point = tg.transform(refPoint);

                if (seen.insert(getGeometry(), point, tolerance)) { // Creates a tree of points lying in the copies of fDomain
                    if (point.getZ() < d) { // Optimizes the choice of the transformation copying fDomain back to the valid range
                        d = point.getZ();
                        backShift = tg;
                        apt = point;
                    }

                }
                    queue.add(tg);
                }

                {
                    final Transform gt = g.createConcatenation(t);
                    final Point3D point = gt.transform(refPoint);

                    if (seen.insert(getGeometry(), point, tolerance)) {
                        if (point.getZ() < d) {
                            d = point.getZ();
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
     * provides a copy of the fundamental domain, using the recycler, if possible
     *
     * @param transform
     * @param refPoint
     * @param fund
     * @return copy
     */
    private Group provideCopy(Transform transform, Point3D refPoint, Group fund) {
        final Group copy = (recycler.size() > 0 ? recycler.pop() : CopyTiles.apply(fund));
        copy.setRotationAxis(refPoint);
        copy.getTransforms().setAll(transform.createConcatenation(transformRecycled));
        return copy;
    }

    @Override
    public void reset() {
        super.reset();
        fundPrototype.getChildren().clear();
        referencePoint = null;
        recycler.clear();
        coveredPoints.clear();
    }

    public Point2D getTransVector() {
        return transVector;
    }

    public boolean directionChanged() {
        return changeDirection;
    }

    /**
     * attempt to insert point to oct tree of covered points
     *
     * @param p
     * @return false, if point is already covered
     */
    private boolean insertCoveredPoint(Point3D p) {
        return coveredPoints.insert(getGeometry(), p, tolerance);
    }
}
