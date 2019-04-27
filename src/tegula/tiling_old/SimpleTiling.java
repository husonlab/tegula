/*
 * SimpleTiling.java Copyright (C) 2019. Daniel H. Huson
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.main.CameraSettings;
import tegula.main.TilingStyle;
import tegula.util.HasHyperbolicModel;

import java.util.LinkedList;

/**
 * simple tiling
 * Daniel Huson, 4.2019
 */
public class SimpleTiling implements HasHyperbolicModel {
    protected final ObjectProperty<HyperbolicModel> hyperbolicModel = new SimpleObjectProperty<>(HasHyperbolicModel.HyperbolicModel.Poincare);
    protected final ObjectProperty<Geometry> geometry = new SimpleObjectProperty<>();

    protected final Group universe;
    protected final Group world;

    protected TilingBase tiling;

    protected PerspectiveCamera perspectiveCamera;

    protected final PointLight pointLight;
    protected final AmbientLight ambientLight;

    protected final IntegerProperty limitHyperbolicGroup = new SimpleIntegerProperty(17);
    protected static final double validHyperbolicRange = 4.8;

    protected final TilingStyle tilingStyle;
    private DoubleProperty zoomFactor =new SimpleDoubleProperty(1);


    /**
     * constructs a simple tiling
     *  @param dSymbol
     * @param universe
     * @param world
     * @param perspectiveCamera
     * @param tilingStyle
     */
    public SimpleTiling(DSymbol dSymbol, final Pane pane, Group universe, Group world, PerspectiveCamera perspectiveCamera, TilingStyle tilingStyle) {
        tiling = TilingBase.createTiling(pane,tilingStyle, dSymbol);

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

        zoomFactor.addListener((c,o,n)->{
            if(tiling instanceof EuclideanTiling) {
                ((EuclideanTiling) tiling).setZoomFactor(n.doubleValue());
            }
        });
    }

    /**
     * update the tiling
     */
    public void update() {
        geometry.set(tiling.getGeometry());

        //System.err.println("Update");
        world.getChildren().clear();

       if(tiling instanceof SphericalTiling) {
                CameraSettings.setupSphericalCamera(perspectiveCamera);

                tiling.updateReferenceChamberIndex();
                world.getChildren().setAll(tiling.createTiling(true));

                if (!universe.getChildren().contains(pointLight))
                    universe.getChildren().add(pointLight);
                universe.getChildren().remove(ambientLight);
        } else if(tiling instanceof EuclideanTiling) {
                CameraSettings.setupEuclideanCamera(perspectiveCamera);

                final EuclideanTiling euclideanTiling = (EuclideanTiling) tiling;
                euclideanTiling.setTransformRecycled(new Translate());

                universe.getChildren().remove(pointLight);

                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);

                euclideanTiling.setZoomFactor(getZoomFactor());

           world.getChildren().setAll(euclideanTiling.createTiling(true));
            } else if(tiling instanceof EuclideanTiling2) {
           CameraSettings.setupEuclideanCamera(perspectiveCamera);

           final EuclideanTiling2 euclideanTiling2 = (EuclideanTiling2) tiling;

           euclideanTiling2.update();

           universe.getChildren().remove(pointLight);

           if (!universe.getChildren().contains(ambientLight))
               universe.getChildren().add(ambientLight);

           world.getChildren().setAll(euclideanTiling2.getWorld());
       }
       else { //  if(tiling instanceof HyperbolicTiling) {
                universe.getChildren().remove(pointLight);
                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);
                CameraSettings.setupHyperbolicCamera(perspectiveCamera, getHyperbolicModel(), false);

                final HyperbolicTiling hyperbolicTilingMeshes = (HyperbolicTiling) tiling;

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

           world.getChildren().setAll(hyperbolicTilingMeshes.createTiling(true));
        }

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
        tiling.getfDomain().recenterFDomain(t);
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
        return getTiling().getPane().getWidth();
    }


    public double getHeight() {
        return getTiling().getPane().getHeight();
    }



    public PerspectiveCamera getPerspectiveCamera() {
        return perspectiveCamera;
    }

    public void setPerspectiveCamera(PerspectiveCamera perspectiveCamera) {
        this.perspectiveCamera = perspectiveCamera;
    }

    public TilingBase getTiling() {
        return tiling;
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

    public double getZoomFactor() {
        return zoomFactor.get();
    }

    public DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }

    public Group getWorld() {
        return world;
    }
}
