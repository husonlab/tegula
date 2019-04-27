/*
 * ExtendedTiling.java Copyright (C) 2019. Daniel H. Huson
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
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.geometry.Tools;
import tegula.main.CameraSettings;
import tegula.main.TilingStyle;
import tegula.tilingcollection.SimpleTiling;
import tegula.util.JavaFXUtils;

import java.util.Collection;

/**
 * editable tiling
 * Daniel Huson, 4.2019
 */
public class ExtendedTiling extends SimpleTiling {
    private final Pane pane;
    private final BooleanProperty maximalTiling = new SimpleBooleanProperty();
    private final BooleanProperty orientableTiling = new SimpleBooleanProperty();
    private final StringProperty groupName = new SimpleStringProperty("");

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
        super(dSymbol, pane, universe, world, perspectiveCamera, tilingStyle);
        this.pane = pane;

        hyperbolicModel.addListener((c, o, n) ->
                CameraSettings.setupHyperbolicCamera(perspectiveCamera, n, true));
    }

    /**
     * replace current tiling by new one
     * @param dSymbol
     */
    public void replaceTiling(DSymbol dSymbol) {
        tiling = TilingBase.createTiling(pane,tilingStyle, dSymbol);
        update();
    }

    private void resetTiling() {
        replaceTiling(tiling.getDSymbol());
    }

    @Override
    public void update() {
        System.err.println("update");
        super.update();
        final DSymbol dSymbol = tiling.getDSymbol();
        maximalTiling.set(DSymbolAlgorithms.isMaximalSymmetry(dSymbol));
        orientableTiling.set(dSymbol.computeOrientation() == 2);
        groupName.setValue(tiling.getGroupName());

        infoLine.setValue(String.format("n:%d t:%d e:%d v:%d g:%s", dSymbol.size(), dSymbol.countOrbits(0, 1), dSymbol.countOrbits(0, 2), dSymbol.countOrbits(1, 2),
                getGroupName() + (isMaximalTiling() ? " max" : "") + (isOrientableTiling() ? " orient." : "")));

        lastUpdate.set(System.currentTimeMillis());
    }

    /**
     * Deletes copies of fundamental domain in hyperbolic case when less tiles are shown.
     */
    public void decreaseTiling() {
        if (tiling instanceof HyperbolicTiling) {
            final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tiling;

            setLimitHyperbolicGroup(getLimitHyperbolicGroup() - 1);

            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
            int bound = world.getChildren().size();
            for (int i = 1; i <= bound; i++) {
                Node node = world.getChildren().get(bound - i);
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
        if (tiling instanceof HyperbolicTiling) {
            final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tiling;

            hyperbolicTilingMeshes.clearKeptHyperbolicCopy();

            for (int i = 0; i < world.getChildren().size(); i++) {
                hyperbolicTilingMeshes.insertKeptHyperbolicCopy(world.getChildren().get(i).getRotationAxis()); // Add existing tiles to tree structure
            }

            hyperbolicTilingMeshes.updateReferenceChamberIndex();

            hyperbolicTilingMeshes.setNumberOfCopies(0);

            // Add new tiles
            hyperbolicTilingMeshes.setInTranslateOrIncreaseTiling(true);

            setLimitHyperbolicGroup(getLimitHyperbolicGroup() + 1);
            hyperbolicTilingMeshes.setMaxDistance(Math.cosh(0.5 * getLimitHyperbolicGroup()));

            world.getChildren().addAll(hyperbolicTilingMeshes.createTiling(false));
            hyperbolicTilingMeshes.setNumberOfCopies(world.getChildren().size());
        }
    }

    public void translateTiling(double dx, double dy) {
        if(tiling instanceof  EuclideanTiling) {
                ((EuclideanTiling) tiling).translateTiling(dx, dy, world);
        } else if(tiling instanceof EuclideanTiling2) {
                world.getChildren().setAll(((EuclideanTiling2) tiling).translateTiling(dx, dy));
        }
        else if(tiling instanceof HyperbolicTiling) {
            translateTilingHyperbolic(dx / 300, dy / 300);
        }
    }

    /**
     * translate a hyperbolic tiling
     *
     * @param dx
     * @param dy
     */
    private void translateTilingHyperbolic(double dx, double dy) {
        if (tiling instanceof HyperbolicTiling) {
            final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tiling;

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
            while (i < world.getChildren().size()) {
                final Node node = world.getChildren().get(i);
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
                final Collection<Node> newTiles = hyperbolicTilingMeshes.createTiling(false);
                if (newTiles == TilingBase.FAILED) { // Generates new tiling if too many rounding errors
                    resetTiling(); // Reset fundamental domain
                    update(); // Update tiling
                } else { // No rounding errors: add new tiles
                    world.getChildren().addAll(newTiles);
                    hyperbolicTilingMeshes.setNumberOfCopies(world.getChildren().size());
                }
            }
        }
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

    public String getGroupName() {
        return groupName.get();
    }

    public ReadOnlyStringProperty groupNameProperty() {
        return groupName;
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
