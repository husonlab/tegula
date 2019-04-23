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

package tegula.tilingeditor;

import javafx.beans.property.*;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;
import tegula.main.CameraSettings;
import tegula.main.TilingStyle;
import tegula.tiling.EuclideanTiling;
import tegula.tiling.HyperbolicTiling;
import tegula.tiling.TilingBase;
import tegula.tilingcollection.SimpleTiling;
import tegula.util.JavaFXUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static tegula.Options.useNewEuclideanCode;

/**
 * editable tiling
 * Daniel Huson, 4.2019
 */
public class ExtendedTiling extends SimpleTiling {
    private final Pane pane;
    private final BooleanProperty maximalTiling = new SimpleBooleanProperty();
    private final BooleanProperty orientableTiling = new SimpleBooleanProperty();
    private final StringProperty group = new SimpleStringProperty("");

    private final StringProperty infoLine = new SimpleStringProperty("");

    private final LongProperty lastUpdate = new SimpleLongProperty(0);

    /**
     * constructor
     *
     * @param dSymbol
     * @param universe
     * @param world
     * @param perspectiveCamera
     * @param tilingStyle
     */
    public ExtendedTiling(DSymbol dSymbol, Pane pane, Group universe, Group world, PerspectiveCamera perspectiveCamera, TilingStyle tilingStyle) {
        super(dSymbol, universe, world, perspectiveCamera, tilingStyle);
        this.pane = pane;

        hyperbolicModel.addListener((c, o, n) ->
                CameraSettings.setupHyperbolicCamera(perspectiveCamera, n, true));
    }

    /**
     * Deletes copies of fundamental domain in hyperbolic case when less tiles are shown.
     */
    public void decreaseTiling() {
        if (tilingMeshes instanceof HyperbolicTiling) {
            final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tilingMeshes;

            setLimitHyperbolicGroup(getLimitHyperbolicGroup() - 1);

            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
            int bound = all.getChildren().size();
            for (int i = 1; i <= bound; i++) {
                Node node = all.getChildren().get(bound - i);
                if (node.getRotationAxis().getZ() > maxDist) {
                    //tiles.getChildren().remove(node);
                    hyperbolicTilingMeshes.getRecycler().getChildren().add(node);
                }
            }
        }
    }

    /**
     * Adds copies of fundamental domain in hyperbolic case when more tiles are shown
     */
    public void increaseTiling() {
        if (tilingMeshes instanceof HyperbolicTiling) {
            final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tilingMeshes;

            hyperbolicTilingMeshes.clearKeptHyperbolicCopy();

            for (int i = 0; i < all.getChildren().size(); i++) {
                hyperbolicTilingMeshes.insertKeptHyperbolicCopy(all.getChildren().get(i).getRotationAxis()); // Add existing tiles to tree structure
            }

            hyperbolicTilingMeshes.updateReferenceChamberIndex();

            hyperbolicTilingMeshes.setNumberOfCopies(0);

            // Add new tiles
            hyperbolicTilingMeshes.setInTranslateOrIncreaseTiling(true);

            setLimitHyperbolicGroup(getLimitHyperbolicGroup() + 1);
            hyperbolicTilingMeshes.setMaxDistance(Math.cosh(0.5 * getLimitHyperbolicGroup()));

            all.getChildren().addAll(hyperbolicTilingMeshes.createTiling());
            hyperbolicTilingMeshes.setNumberOfCopies(all.getChildren().size());
        }
    }

    public void translateTiling(double dx, double dy) {
        if (getGeometry() == Geometry.Euclidean)
            translateTilingEuclidean(dx, dy);
        else
            translateTilingHyperbolic(dx / 300, dy / 300);
    }

    /**
     * translate a hyperbolic tiling
     *
     * @param dx
     * @param dy
     */
    private void translateTilingHyperbolic(double dx, double dy) {
        if (tilingMeshes instanceof HyperbolicTiling) {
            final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tilingMeshes;

            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
            dx *= 100;
            dy *= 100;

            // Calculate hyperbolic translation of group:
            final Transform translate = Tools.hyperbolicTranslation(dx, dy);

            // OctTree is used for saving copies which are kept under translation
            hyperbolicTilingMeshes.clearKeptHyperbolicCopy();

            // Translates fDomain by vector (dx,dy).
            //tiling.getfDomain().translate(dx, dy);
            //tiling.getfDomain().updateGeneratorsAndContraints();

            hyperbolicTilingMeshes.setTransformRecycled(translate.createConcatenation(hyperbolicTilingMeshes.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain

            // Recenter fDomain if too far away from center
            Point3D refPoint = hyperbolicTilingMeshes.getfDomain().getChamberCenter3D(hyperbolicTilingMeshes.getReferenceChamberIndex()).multiply(0.01);
            if (refPoint.getZ() >= validHyperbolicRange) {
                final Transform t = hyperbolicTilingMeshes.calculateBackShiftHyperbolic();
                recenterFDomain(t); // Shifts back fDomain into valid range
                hyperbolicTilingMeshes.setTransformRecycled(t.createConcatenation(hyperbolicTilingMeshes.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
            }

            //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
            int i = 0;
            while (i < all.getChildren().size()) {
                final Node node = all.getChildren().get(i);
                if (node instanceof Group) {
                    if (((Group) node).getChildren().size() == 0)
                        throw new RuntimeException("Fund copy empty");
                }

                final Transform nodeTransform = node.getTransforms().get(0);
                final Point3D point = translate.transform(node.getRotationAxis()); // point = translated reference point of node

                if (point.getZ() > maxDist) {
                    hyperbolicTilingMeshes.getRecycler().getChildren().add(node); // Remove node and add to recycler
                } else {
                    node.getTransforms().remove(0);
                    node.getTransforms().add(translate.createConcatenation(nodeTransform));
                    node.setRotationAxis(point);
                    hyperbolicTilingMeshes.insertKeptHyperbolicCopy(point); // Save kept copy
                    i++;
                }
            }

            if (hyperbolicTilingMeshes.getRecycler().getChildren().size() == 0) { // Fill recycler if necessary
                final Group fund = JavaFXUtils.copyGroup(hyperbolicTilingMeshes.getHyperbolicFund()); // Copy original fundamental domain which was used to build "tiles"
                if (fund.getChildren().size() == 0)
                    throw new RuntimeException("Fund copy empty");
                hyperbolicTilingMeshes.getRecycler().getChildren().add(fund); // Add copy to recycler
            }

            if (false) {
                //Second step: Create new tiles ----------------------------------------------------------------------------
                hyperbolicTilingMeshes.setInTranslateOrIncreaseTiling(true);
                hyperbolicTilingMeshes.setMaxDistance(maxDist);
                final Collection<Node> newTiles = hyperbolicTilingMeshes.createTiling();
                if (newTiles == TilingBase.FAILED) { // Generates new tiling if too many rounding errors
                    resetTiling(); // Reset fundamental domain
                    update(); // Update tiling
                } else { // No rounding errors: add new tiles
                    all.getChildren().addAll(newTiles);
                    hyperbolicTilingMeshes.setNumberOfCopies(all.getChildren().size());
                }
            }
        }
    }

    /**
     * translate a euclidean tiling
     *
     * @param dx
     * @param dy
     */
    private void translateTilingEuclidean(double dx, double dy) {
        if (tilingMeshes instanceof EuclideanTiling) {
            final EuclideanTiling euclideanTilingMeshes = (EuclideanTiling) tilingMeshes;

            euclideanTilingMeshes.clearKeptEuclideanCopy();
            Translate translate = new Translate(dx, dy, 0); // Mouse translation (MouseHandler)


            if (useNewEuclideanCode) {
                Node fund = null;
                for (Node node : all.getChildren()) {
                    if (node.getTransforms().size() > 0) {
                        final Transform nodeTransform = node.getTransforms().get(0); // get transform of node
                        node.getTransforms().setAll(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)

                        Point3D referencePoint = node.getRotationAxis().add(dx, dy, 0); // point = reference point of node (saved as rotation axis) + mouse translation
                        referencePoint = translate.transform(referencePoint);
                        node.setRotationAxis(referencePoint); // "point" serves as new reference of copy

                        if (node.getUserData() instanceof String && node.getUserData().equals("fund"))
                            fund = node;
                    }
                }

                if (fund != null) {
                    final Node ref0 = find("ref0", all.getChildren());
                    if (ref0 instanceof Sphere) {
                        if (!isInsideVisibleRectangle(pane, ref0)) {
                            final Node ref1 = find("ref1", all.getChildren());
                            final Node ref2 = find("ref2", all.getChildren());

                            if (ref1 instanceof Sphere && ref2 instanceof Sphere) {
                                final Point2D point0 = getPaneCoordinates(pane, ref0);
                                final Point2D point1 = getPaneCoordinates(pane, ref1);
                                final Point2D point2 = getPaneCoordinates(pane, ref2);

                                for (Node copy : findAll("copy", all.getChildren())) {
                                    if (copy != fund) {
                                        final Node nRef0 = find("ref0", ((Group) copy).getChildren());
                                        if (nRef0 instanceof Sphere) {
                                            if (isInsideVisibleRectangle(pane, nRef0)) { // this ref is visible
                                                final Node nRef1 = find("ref1", ((Group) copy).getChildren());
                                                final Node nRef2 = find("ref2", ((Group) copy).getChildren());
                                                if (nRef1 instanceof Sphere && nRef2 instanceof Sphere) {
                                                    final Point2D nPoint0 = getPaneCoordinates(pane, nRef0);
                                                    final Point2D nPoint1 = getPaneCoordinates(pane, nRef1);
                                                    final Point2D nPoint2 = getPaneCoordinates(pane, nRef2);

                                                    final Point2D delta0 = point0.subtract(nPoint0);
                                                    final Point2D delta1 = point1.subtract(nPoint1).subtract(delta0);
                                                    final Point2D delta2 = point2.subtract(nPoint2).subtract(delta0);


                                                    if (delta1.magnitude() < 0.01 && delta2.magnitude() < 0.01) {
                                                        System.err.println("Found insider");
                                                        ((Sphere) nRef0).setMaterial(new PhongMaterial(Color.BLACK));
                                                        ((Sphere) ref0).setMaterial(new PhongMaterial(Color.GRAY));
                                                        // make this the new fund
                                                        copy.setUserData("fund");
                                                        fund.setUserData("copy");
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }

                            }
                        }
                    }
                }
                return;
            }

            if (!useNewEuclideanCode) {

                euclideanTilingMeshes.getfDomain().translate(dx, dy);
                euclideanTilingMeshes.getfDomain().updateGeneratorsAndContraints();

                euclideanTilingMeshes.setTransformRecycled(translate.createConcatenation(euclideanTilingMeshes.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain

                final Point3D refPoint = euclideanTilingMeshes.getfDomain().getChamberCenter3D(euclideanTilingMeshes.getReferenceChamberIndex()); // Point of reference in Euclidean fundamental domain

                if (!euclideanTilingMeshes.isInWindowEuclidean(refPoint, width.get(), height.get())) { // If fundamental domain is out of visible window
                    Transform t = euclideanTilingMeshes.calculateBackShiftEuclidean(width.get(), height.get());
                    euclideanTilingMeshes.setTransformRecycled(t.createConcatenation(euclideanTilingMeshes.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
                    recenterFDomain(t); // Shifts back fDomain into visible window
                }
            }

            //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
            int i = 0;
            while (i < all.getChildren().size()) {
                Node node = all.getChildren().get(i); // Copy with index i in tile. Each copy is a node of the group "tile".
                if (node.getTransforms().size() > 0) {
                    Transform nodeTransform = node.getTransforms().get(0); // get transform of node
                    Point3D point = node.getRotationAxis().add(dx, dy, 0); // point = reference point of node (saved as rotation axis) + mouse translation

                    if (euclideanTilingMeshes.isInRangeEuclidean(point, width.get(), height.get())) {  // keep copy if point still is in valid range
                        node.getTransforms().remove(0); // remove old transform
                        node.getTransforms().add(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)
                        node.setRotationAxis(point); // "point" serves as new reference of copy
                        euclideanTilingMeshes.insertKeptEuclideanCopy(point); // Save copy as a kept one
                        i++;
                    } else { // when point is out of valid range
                        euclideanTilingMeshes.getRecycler().getChildren().add(node); // Remove node and add to recycler
                    }
                } else
                    i++;
            }

            if (euclideanTilingMeshes.getRecycler().getChildren().size() == 0) { // Fill recycler if necessary
                final Group fund = JavaFXUtils.copyGroup(euclideanTilingMeshes.getEuclideanFund()); // Copy original fundamental domain which was used to build "tiles"
                euclideanTilingMeshes.getRecycler().getChildren().addAll(fund); // Add copy to recycler
            }

            //Second step: Create new tiles ----------------------------------------------------------------------------
            // Create new tiles to fill empty space of valid range. Add new tiles to the group "tiles"
            euclideanTilingMeshes.setInTranslateOrIncreaseTiling(true);
            euclideanTilingMeshes.setWidth(getWidth());
            euclideanTilingMeshes.setHeight(getHeight());
            final Collection<Node> newTiles = euclideanTilingMeshes.createTiling();

            if (newTiles == TilingBase.FAILED) { // Generates new tiling if too much rounding errors
                resetTiling(); // Reset fundamental domain
                update(); // Update tiling
            } else { // No rounding errors: add new tiles
                all.getChildren().addAll(newTiles);
                euclideanTilingMeshes.setNumberOfCopies(all.getChildren().size());
                //System.err.println("Number of copies: " + tiling.getNumberOfCopies());
            }
        }
    }

    /**
     * is the given shape currently showing in the pane?
     *
     * @param pane
     * @param shape
     * @return true, if bounding box intersects pane
     */
    private static boolean isInsideVisibleRectangle(Pane pane, Node shape) {
        final Bounds refBounds = shape.localToScreen(shape.getBoundsInParent());
        final Bounds paneScreenCoordinates = pane.localToScreen(pane.getBoundsInParent());

        final Point2D middle = new Point2D(0.5 * (refBounds.getMinX() + refBounds.getMaxX()),
                0.5 * (refBounds.getMinY() + refBounds.getMaxY()));

        return paneScreenCoordinates.contains(middle);
    }

    /**
     * get the main coordinates of a node
     *
     * @param pane
     * @param shape
     * @return pane coordinates
     */
    private static Point2D getPaneCoordinates(Pane pane, Node shape) {
        final Bounds refBounds = shape.localToScreen(shape.getBoundsInParent());
        final Bounds paneScreenCoordinates = pane.localToScreen(pane.getBoundsInParent());

        return new Point2D(0.5 * (refBounds.getMinX() + refBounds.getMaxX()) - paneScreenCoordinates.getMinX(),
                0.5 * (refBounds.getMinY() + refBounds.getMaxY()) - paneScreenCoordinates.getMinY());
    }

    private static Node find(String tag, List<Node> nodes) {
        for (Node node : nodes) {
            if (node.getUserData() instanceof String && node.getUserData().equals(tag))
                return node;
            else if (node instanceof Parent) {
                Node result = find(tag, ((Parent) node).getChildrenUnmodifiable());
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    private static List<Node> findAll(String tag, List<Node> nodes) {
        final ArrayList<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getUserData() instanceof String && node.getUserData().equals(tag)) {
                result.add(node);
            } else if (node instanceof Parent) {
                result.addAll(findAll(tag, ((Parent) node).getChildrenUnmodifiable()));
            }
        }
        return result;
    }

    private void resetTiling() {
        replaceTiling(tilingMeshes.getDSymbol());
    }

    public void replaceTiling(DSymbol dSymbol) {
        tilingMeshes = TilingBase.createTiling(tilingStyle, dSymbol);
        update();
    }

    @Override
    public void update() {
        System.err.println("update");
        super.update();
        final DSymbol dSymbol = tilingMeshes.getDSymbol();
        maximalTiling.set(DSymbolAlgorithms.isMaximalSymmetry(dSymbol));
        orientableTiling.set(dSymbol.computeOrientation() == 2);
        group.setValue(tilingMeshes.getGroupName());
        infoLine.setValue(String.format("n:%d t:%d e:%d v:%d g:%s", dSymbol.size(), dSymbol.countOrbits(0, 1), dSymbol.countOrbits(0, 2), dSymbol.countOrbits(1, 2),
                group.get() + (isMaximalTiling() ? " max" : "") + (isOrientableTiling() ? " orient." : "")));
        lastUpdate.set(System.currentTimeMillis());
    }

    public boolean isMaximalTiling() {
        return maximalTiling.get();
    }

    public ReadOnlyBooleanProperty maximalTilingProperty() {
        return maximalTiling;
    }

    public boolean isOrientableTiling() {
        return orientableTiling.get();
    }

    public ReadOnlyBooleanProperty orientableTilingProperty() {
        return orientableTiling;
    }

    public String getGroup() {
        return group.get();
    }

    public ReadOnlyStringProperty groupProperty() {
        return group;
    }

    public long getLastUpdate() {
        return lastUpdate.get();
    }

    public ReadOnlyLongProperty lastUpdateProperty() {
        return lastUpdate;
    }

    public String getInfoLine() {
        return infoLine.get();
    }

    public ReadOnlyStringProperty infoLineProperty() {
        return infoLine;
    }
}
