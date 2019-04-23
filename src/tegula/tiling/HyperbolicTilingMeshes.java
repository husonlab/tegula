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
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.main.TilingStyle;
import tegula.main.editor.ExtendedTiling;
import tegula.util.JavaFXUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * computes a tiling represented by meshes from a fundamental domain represented by meshes
 * <p>
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class HyperbolicTilingMeshes extends TilingMeshesBase {
    final private OctTree keptHyperbolicCopy = new OctTree();

    private Point3D refPointHyperbolic = new Point3D(0, 0, 1);

    private final Group recycler = new Group();
    private Transform transformRecycled = new Translate();
    private Group hyperbolicFund = new Group();

    private int numberOfCopies = 0;

    private final BooleanProperty inTranslateOrIncreaseTiling = new SimpleBooleanProperty(false);
    private final DoubleProperty maxDistance = new SimpleDoubleProperty(1);

    /**
     * constructor
     *
     * @param tilingStyle
     * @param ds
     */
    public HyperbolicTilingMeshes(TilingStyle tilingStyle, DSymbol ds) {
        super(tilingStyle, ds);
    }

    /**
     * create tiling
     *
     * @return group
     */
    public ArrayList<Node> createTiling() {
        final ArrayList<Node> all = new ArrayList<>();
        //System.err.println("Create Hyperbolic Tiling");
        //System.out.println(refPointHyperbolic);

        final Group fund = fundamentalDomainMeshes.getAll();

        refPointHyperbolic = fDomain.getChamberCenter3D(referenceChamberIndex);

        final OctTree allReferencePoints = new OctTree();

        if (!isInTranslateOrIncreaseTiling()) { // need to recompute fundamental domain
            fundamentalDomainMeshes.update(ds, fDomain, tilingStyle);

            fund.setRotationAxis(refPointHyperbolic);
            fund.getTransforms().add(new Translate());
            all.add(fund);
            setHyperbolicFund(fund);
            allReferencePoints.insert(getGeometry(), refPointHyperbolic, tolerance);
        }

        if (!isDrawFundamentalDomainOnly()) {
            // Make copies of fundamental domain.
            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());

            // apply all generators:
            for (Transform transform : generators.getTransforms()) {
                Point3D genRef = transform.transform(refPointHyperbolic);
                if (allReferencePoints.insert(fDomain.getGeometry(), genRef, tolerance)) {
                    if (makeCopyHyperbolic(genRef)) {
                        if (isInTranslateOrIncreaseTiling()) {
                            all.add(useRecycler(transform, genRef, getHyperbolicFund()));
                        } else {
                            all.add(createCopy(transform, genRef, getHyperbolicFund()));
                        }
                    }
                }
            }

            int countChildrenAdded = 0;
            while (queue.size() > 0) {

                // Breaks while loop if too many copies (rounding errors)
                if (isInTranslateOrIncreaseTiling() && getNumberOfCopies() > 0 && countChildrenAdded >= 1.5 * getNumberOfCopies()) {
                    System.out.println(countChildrenAdded + " children and " + getNumberOfCopies() + " copies");
                    return FAILED;
                }

                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) {
                    {
                        final Transform tg = t.createConcatenation(g);
                        final Point3D bpt = tg.transform(refPointHyperbolic);

                        if (bpt.getZ() < getMaxDistance() && allReferencePoints.insert(fDomain.getGeometry(), bpt, tolerance)) {
                            queue.add(tg);
                            if (makeCopyHyperbolic(bpt)) {
                                if (isInTranslateOrIncreaseTiling()) {
                                    all.add(useRecycler(tg, bpt, getHyperbolicFund()));
                                } else {
                                    all.add(createCopy(tg, bpt, getHyperbolicFund()));

                                }
                            }
                        }
                    }

                    {
                        final Transform gt = g.createConcatenation(t);
                        final Point3D bpt = gt.transform(refPointHyperbolic);
                        if (bpt.getZ() < getMaxDistance() && allReferencePoints.insert(fDomain.getGeometry(), bpt, tolerance)) {
                            countChildrenAdded++;
                            queue.add(gt);
                            if (makeCopyHyperbolic(bpt)) {
                                if (isInTranslateOrIncreaseTiling()) {
                                    all.add(useRecycler(gt, bpt, getHyperbolicFund()));
                                } else {
                                    all.add(createCopy(gt, bpt, getHyperbolicFund()));
                                }
                            }
                        }
                    }
                }
                if (all.size() > 2000)
                    break;
            }

            System.err.println("Count: " + countChildrenAdded);
        }

        // only what one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.add(fundamentalDomainMeshes.getChambers());

        if (tilingStyle.isShowHandles())
            all.add(fundamentalDomainMeshes.getHandles());

        if (!isInTranslateOrIncreaseTiling())
            setNumberOfCopies(all.size());

        return all;
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

    public boolean isInTranslateOrIncreaseTiling() {
        return inTranslateOrIncreaseTiling.get();
    }

    public BooleanProperty inTranslateOrIncreaseTilingProperty() {
        return inTranslateOrIncreaseTiling;
    }

    public void setInTranslateOrIncreaseTiling(boolean inTranslateOrIncreaseTiling) {
        this.inTranslateOrIncreaseTiling.set(inTranslateOrIncreaseTiling);
    }

    public double getMaxDistance() {
        return maxDistance.get();
    }

    public DoubleProperty maxDistanceProperty() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance.set(maxDistance);
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

    public void clearKeptHyperbolicCopy() {
        keptHyperbolicCopy.clear();
    }

    public Point3D getRefPointHyperbolic() {
        return refPointHyperbolic;
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

}


