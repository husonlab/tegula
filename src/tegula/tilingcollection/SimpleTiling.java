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
package tegula.tilingcollection;

import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.main.CameraSettings;
import tegula.main.TilingStyle;
import tegula.tiling.EuclideanTiling;
import tegula.tiling.HyperbolicTiling;
import tegula.tiling.TilingBase;
import tegula.util.IHasHyperbolicModel;

import java.util.LinkedList;

/**
 * simple tiling
 * Daniel Huson, 4.2019
 */
public class SimpleTiling implements IHasHyperbolicModel {
    protected final ObjectProperty<HyperbolicModel> hyperbolicModel = new SimpleObjectProperty<>(IHasHyperbolicModel.HyperbolicModel.Poincare);
    protected final ObjectProperty<Geometry> geometry = new SimpleObjectProperty<>();

    protected final Group universe;
    protected final Group world;

    protected final Group all = new Group();
    protected TilingBase tilingMeshes;

    protected PerspectiveCamera perspectiveCamera;

    protected final PointLight pointLight;
    protected final AmbientLight ambientLight;

    protected final DoubleProperty width = new SimpleDoubleProperty(1000);
    protected final DoubleProperty height = new SimpleDoubleProperty(1000);

    protected final IntegerProperty limitHyperbolicGroup = new SimpleIntegerProperty(17);
    protected static final double validHyperbolicRange = 4.8;

    protected final TilingStyle tilingStyle;


    /**
     * constructs a simple tiling
     *
     * @param dSymbol
     * @param universe
     * @param world
     * @param perspectiveCamera
     * @param tilingStyle
     */
    public SimpleTiling(DSymbol dSymbol, Group universe, Group world, PerspectiveCamera perspectiveCamera, TilingStyle tilingStyle) {
        tilingMeshes = TilingBase.createTiling(tilingStyle, dSymbol);

        this.universe = universe;
        this.world = world;
        this.perspectiveCamera = perspectiveCamera;
        this.tilingStyle = tilingStyle;
        tilingStyle.setBandWidth(2);

        pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-1000);
        pointLight.setTranslateY(-1000);
        pointLight.setTranslateZ(-10000);
        pointLight.setRotate(90);

        ambientLight = new AmbientLight(Color.WHITE);
    }

    /**
     * update the tiling
     */
    public void update() {
        geometry.set(tilingMeshes.getGeometry());

        //System.err.println("Update");
        all.getChildren().clear();

        world.getChildren().clear();

        switch (getGeometry()) {
            case Spherical: {
                CameraSettings.setupSphericalCamera(perspectiveCamera);

                tilingMeshes.updateReferenceChamberIndex();
                all.getChildren().setAll(tilingMeshes.createTiling());

                if (!universe.getChildren().contains(pointLight))
                    universe.getChildren().add(pointLight);
                universe.getChildren().remove(ambientLight);

                break;
            }
            case Euclidean: {
                CameraSettings.setupEuclideanCamera(perspectiveCamera);

                final EuclideanTiling euclideanTilingMeshes = (EuclideanTiling) tilingMeshes;
                euclideanTilingMeshes.getRecycler().getChildren().clear();
                euclideanTilingMeshes.setTransformRecycled(new Translate());

                universe.getChildren().remove(pointLight);

                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);

                // Reset Euclidean fundamental domain and QuadTree
                euclideanTilingMeshes.setEuclideanFund(new Group());
                euclideanTilingMeshes.clearKeptEuclideanCopy();

                // Calculate optimal chamber, where chamber center is as far away from boundary as possible
                euclideanTilingMeshes.updateReferenceChamberIndex();

                if (!euclideanTilingMeshes.isInWindowEuclidean(euclideanTilingMeshes.getRefPointEuclidean(), width.get(), height.get())) { // Fund. domain is not in visible window
                    System.err.println("Recenter");
                    // recenterFDomain(tiling.calculateBackShiftEuclidean(width.get(), height.get())); // Shifts back fDomain into valid range for fund. domain
                }
                euclideanTilingMeshes.setInTranslateOrIncreaseTiling(false);
                euclideanTilingMeshes.setWidth(getWidth());
                euclideanTilingMeshes.setHeight(getHeight());
                all.getChildren().setAll(euclideanTilingMeshes.createTiling());
                break;
            }
            case Hyperbolic: {         // Hyperbolic case -----------------------------
                universe.getChildren().remove(pointLight);
                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);
                CameraSettings.setupHyperbolicCamera(perspectiveCamera, getHyperbolicModel(), false);

                final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tilingMeshes;

                // Calculate optimal chamber, where chamber center is as far away from boundary as possible
                hyperbolicTilingMeshes.updateReferenceChamberIndex();

                double diameterFDomain = calculateDiameter(hyperbolicTilingMeshes.getfDomain());
                if (2.8 * diameterFDomain > getLimitHyperbolicGroup()) {
                    setLimitHyperbolicGroup((int) Math.round(2.8 * diameterFDomain));
                }

                // Reset hyperbolic fundamental domain.
                hyperbolicTilingMeshes.setHyperbolicFund(new Group());
                hyperbolicTilingMeshes.clearKeptHyperbolicCopy();

                //Reset Fundamental Domain if necessary:
                if (hyperbolicTilingMeshes.getRefPointHyperbolic().getZ() >= validHyperbolicRange) {// Fundamental domain is shifted back
                    recenterFDomain(hyperbolicTilingMeshes.calculateBackShiftHyperbolic()); // Shifts back fDomain into valid range (slower algorithm)
                }

                // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.
                hyperbolicTilingMeshes.setMaxDistance(Math.cosh(0.5 * getLimitHyperbolicGroup()));

                hyperbolicTilingMeshes.setInTranslateOrIncreaseTiling(false);

                all.getChildren().setAll(hyperbolicTilingMeshes.createTiling());
                break;
            }
        }

        // world
        world.getChildren().setAll(all);
    }

    private double calculateDiameter(FDomain f) {
        // Save vertices of fundamental domain in list:
        LinkedList<Point3D> vertices = new LinkedList<>();
        for (int k = 1; k <= f.size(); k++) {
            vertices.add(f.getVertex3D(0, k));
            vertices.add(f.getVertex3D(1, k));
            vertices.add(f.getVertex3D(2, k));
        }
        double d = 0;

        for (int i = 0; i <= vertices.size() - 1; i++) {
            for (int j = i + 1; j <= vertices.size() - 1; j++) {
                Point3D a = vertices.get(i), b = vertices.get(j);
                // Calculate hyperbolic distance between a and b:
                double scalar = (a.getZ() * b.getZ() - a.getX() * b.getX() - a.getY() * b.getY()) / 10000;
                double dist = Math.log(Math.abs(scalar + Math.sqrt(Math.abs(scalar * scalar - 1)))); // Inverse function of cosh
                if (dist > d) { // Find maximal distance
                    d = dist;
                }
            }
        }
        return d;
    }

    protected void recenterFDomain(Transform t) {
        tilingMeshes.getfDomain().recenterFDomain(t);
    }

    public HyperbolicModel getHyperbolicModel() {
        return hyperbolicModel.get();
    }

    public ObjectProperty<HyperbolicModel> hyperbolicModelProperty() {
        return hyperbolicModel;
    }

    public void setHyperbolicModel(HyperbolicModel hyperbolicModel) {
        this.hyperbolicModel.set(hyperbolicModel);
    }

    public int getLimitHyperbolicGroup() {
        return limitHyperbolicGroup.get();
    }

    public IntegerProperty limitHyperbolicGroupProperty() {
        return limitHyperbolicGroup;
    }

    public void setLimitHyperbolicGroup(int limitHyperbolicGroup) {
        this.limitHyperbolicGroup.set(limitHyperbolicGroup);
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

    public PerspectiveCamera getPerspectiveCamera() {
        return perspectiveCamera;
    }

    public void setPerspectiveCamera(PerspectiveCamera perspectiveCamera) {
        this.perspectiveCamera = perspectiveCamera;
    }

    public TilingBase getTilingMeshes() {
        return tilingMeshes;
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }

    public Geometry getGeometry() {
        return geometry.get();
    }

    public ObjectProperty<Geometry> geometryProperty() {
        return geometry;
    }

    public static double getValidHyperbolicRange() {
        return validHyperbolicRange;
    }
}
