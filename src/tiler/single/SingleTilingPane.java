/*
 * SingleTilingPane.java Copyright (C) 2019. Daniel H. Huson
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

package tiler.single;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Basic;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.Geometry;
import tiler.main.TilingStyle;
import tiler.tiling.EuclideanTiling;
import tiler.tiling.HyperbolicTiling;
import tiler.tiling.TilingBase;
import tiler.tiling.TilingCreator;
import tiler.util.HasHyperbolicModel;
import tiler.util.Updateable;

import java.io.IOException;
import java.io.StringReader;

/**
 * a single tiling pane
 * Daniel Huson and Ruediger Zeller, 4.2019
 */
public class SingleTilingPane implements Updateable {
    private final SingleTilingPaneController controller;
    private final Pane root;

    private final SimpleObjectProperty<HasHyperbolicModel.HyperbolicModel> hyperbolicModel = new SimpleObjectProperty<>(HasHyperbolicModel.HyperbolicModel.Poincare);
    private final ObjectProperty<TilingBase> tiling = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Geometry> geometryProperty = new SimpleObjectProperty<>();
    private final TilingStyle tilingStyle = new TilingStyle();

    private final DoubleProperty euclideanWidth = new SimpleDoubleProperty(800);
    private final DoubleProperty euclideanHeight = new SimpleDoubleProperty(800);

    private final Group tiles = new Group();
    private final Group world = new Group();
    private final Group universe = new Group(world);


    private PerspectiveCamera perspectiveCamera;

    private final PointLight pointLight;
    private final AmbientLight ambientLight;

    private boolean drawFundamentalDomainOnly = false;

    private final LongProperty updateNumber = new SimpleLongProperty(0); // incremented each time updated


    public SingleTilingPane(final Stage stage, DSymbol dSymbol) {
        final ExtendedFXMLLoader<SingleTilingPaneController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = (Pane) extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();


        stage.setScene(new Scene(root, 800, 800));
        stage.sizeToScene();
        stage.show();


        perspectiveCamera = new PerspectiveCamera(true);
        perspectiveCamera.setNearClip(0.1);
        perspectiveCamera.setFarClip(10000.0);
        perspectiveCamera.setTranslateZ(-500);
        perspectiveCamera.setFieldOfView(35);

        pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-1000);
        pointLight.setTranslateY(-1000);
        pointLight.setTranslateZ(-10000);
        pointLight.setRotate(90);

        ambientLight = new AmbientLight(Color.WHITE);

        euclideanWidthProperty().addListener((c, o, n) -> {
            if (getTiling() instanceof EuclideanTiling) {
                ((EuclideanTiling) getTiling()).setWidth(n.doubleValue());
            }
        });

        euclideanHeightProperty().addListener((c, o, n) -> {
            if (getTiling() instanceof EuclideanTiling) {
                ((EuclideanTiling) getTiling()).setHeight(n.doubleValue());
            }
        });

        final ObjectProperty<Transform> worldRotateProperty = new SimpleObjectProperty<>(this, "rotation", new Rotate(0, 0, 0, 1));
        final Translate worldTranslate = new Translate(0, 0, 0);
        final Scale worldScale = new Scale(1, 1);


        final SubScene subScene = new SubScene(new Group(universe), 800, 800, false, SceneAntialiasing.BALANCED);
        subScene.setCamera(perspectiveCamera);

        world.getTransforms().add(worldTranslate);
        world.getTransforms().add(worldScale);
        world.getTransforms().add(worldRotateProperty.get());

        final Pane mainPane = controller.getMainPane();
        mainPane.getChildren().add(0, subScene);

        subScene.heightProperty().bind(mainPane.heightProperty());
        subScene.widthProperty().bind(mainPane.widthProperty());

        // setup top pane and stacked pane


        geometryProperty().addListener((c, o, n) -> {
            setUseDepthBuffer(mainPane, n != Geometry.Euclidean);
            if (o == Geometry.Spherical && n != Geometry.Spherical) {
                worldRotateProperty.setValue(new Rotate()); // remove any rotations
            }
            if (o != Geometry.Hyperbolic && n == Geometry.Hyperbolic) {
                reset(); // looks like this helps to avoid the program getting stuck????
            }
        });


        worldRotateProperty.addListener((observable, oldValue, newValue) -> {
            int indexOf = world.getTransforms().indexOf(oldValue);
            world.getTransforms().set(indexOf, newValue);
        });

        if (dSymbol.computeGeometry() == Geometry.Euclidean) { // todo: there is a bug that requires that we first show a non-euclidean tiling
            final DSymbol tmp = new DSymbol();
            try {
                tmp.read(new StringReader("<1.1:1:1,1,1:5,3>"));
                setDSymbol(tmp);
                update();
            } catch (IOException e) {
                Basic.caught(e);
            }
            Platform.runLater(() -> {
                setDSymbol(dSymbol);
                update();
                stage.show();
            });
        } else {
            setDSymbol(dSymbol);
            update();
            stage.show();
        }

        MouseHandler.addMouseHandler(stage.getScene(), worldTranslate, worldScale, worldRotateProperty, this);
    }


    /**
     * set the Delaney-Dress symbol
     *
     * @param dSymbol
     */
    public void setDSymbol(DSymbol dSymbol) {
        geometryProperty.set(dSymbol.computeGeometry());
        tiling.set(TilingCreator.create(dSymbol, tilingStyle, this));
    }

    // Reset fundamental domain (without updating the set of tiles)
    public void reset() {
        if (getTiling() != null)
            setDSymbol(getTiling().getDSymbol());
    }


    /**
     * update the tiling
     */
    public void update() {
        System.err.println("Update");

        if (getTiling() == null)
            return;

        getWorld().getChildren().clear();
        final Group additionalStuff = new Group();

        switch (getGeometry()) {
            case Spherical: {
                if (!universe.getChildren().contains(pointLight))
                    universe.getChildren().add(pointLight);
                universe.getChildren().remove(ambientLight);
                CameraSettings.setupSphericalCamera(perspectiveCamera);
                break;
            }
            case Hyperbolic: {
                universe.getChildren().remove(pointLight);
                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);

                CameraSettings.setupHyperbolicCamera(perspectiveCamera, hyperbolicModel.get(), false);

                break;
            }
            case Euclidean: {
                universe.getChildren().remove(pointLight);
                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);
                // CameraSettings.setupEuclideanCamera(perspectiveCamera);

                //Add rectangles for debugging
                if (true) {
                    Rectangle rect = new Rectangle(euclideanWidth.get(), euclideanHeight.get());
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.BLACK);
                    Rectangle range = new Rectangle(euclideanWidth.get() + 250, euclideanHeight.get() + 250);
                    range.setFill(Color.TRANSPARENT);
                    range.setStroke(Color.BLACK);
                    Rectangle test = new Rectangle(euclideanWidth.get() + 200, euclideanHeight.get() + 200);
                    test.setFill(Color.TRANSPARENT);
                    test.setStroke(Color.BLACK);
                    Rectangle test2 = new Rectangle(euclideanWidth.get() + 150, euclideanHeight.get() + 150);
                    test2.setFill(Color.TRANSPARENT);
                    test2.setStroke(Color.BLACK);
                    Rectangle small = new Rectangle(40, 40);
                    small.setFill(Color.TRANSPARENT);
                    small.setStroke(Color.ORANGE);

                    additionalStuff.getChildren().addAll(rect, range, test, test2, small);
                    System.err.println(String.format("Rect: %.1f x %.1f", rect.getWidth(), rect.getHeight()));
                }
                break;
            }
        }
        tiles.getChildren().setAll(getTiling().update().getChildren());

        getWorld().getChildren().setAll(tiles);
        getWorld().getChildren().addAll(additionalStuff.getChildren());

        updateNumber.setValue(updateNumber.get() + 1);
    }

    /**
     * translate a euclidean or hyperbolic tiling
     *
     * @param dx
     * @param dy
     */
    public void translateTiling(double dx, double dy) {
        if (getTiling() instanceof EuclideanTiling) {
            final EuclideanTiling tiling = (EuclideanTiling) getTiling();
            tiling.translateTiling(dx, dy, tiles);
        } else if (getTiling() instanceof HyperbolicTiling) {
            final HyperbolicTiling tiling = (HyperbolicTiling) getTiling();
            tiling.translateTiling(dx, dy, tiles);
        }
    }

    public void decreaseTiling() {
        if (getTiling() instanceof HyperbolicTiling) {
            final HyperbolicTiling tiling = (HyperbolicTiling) getTiling();
            tiling.decreaseTiling(tiles);
        }
    }

    public void increaseTiling() {
        if (getTiling() instanceof HyperbolicTiling) {
            final HyperbolicTiling tiling = (HyperbolicTiling) getTiling();
            tiling.increaseTiling(tiles);
        }
    }

    /**
     * determine whether to use depth buffer
     *
     * @param useDepthBuffer
     */
    public void setUseDepthBuffer(final Pane mainPane, boolean useDepthBuffer) {
        final SubScene subScene = (SubScene) mainPane.getChildren().get(0);
        if (useDepthBuffer != subScene.isDepthBuffer()) {
            mainPane.getChildren().remove(subScene);
            ((Group) subScene.getRoot()).getChildren().remove(universe);

            final SubScene newSubScene = new SubScene(new Group(universe), subScene.getWidth(), subScene.getHeight(), useDepthBuffer, subScene.getAntiAliasing());
            newSubScene.heightProperty().bind(mainPane.heightProperty());
            newSubScene.widthProperty().bind(mainPane.widthProperty());
            if (useDepthBuffer) {
                perspectiveCamera = new PerspectiveCamera(true);
                newSubScene.setCamera(perspectiveCamera);
            } else
                perspectiveCamera = null;
            mainPane.getChildren().add(0, newSubScene);
        }
    }

    public Group getWorld() {
        return world;
    }

    public TilingBase getTiling() {
        return tiling.get();
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }

    public ObjectProperty<TilingBase> tilingProperty() {
        return tiling;
    }

    public double getEuclideanWidth() {
        return euclideanWidth.get();
    }

    public DoubleProperty euclideanWidthProperty() {
        return euclideanWidth;
    }

    public void setEuclideanWidth(double euclideanWidth) {
        this.euclideanWidth.set(euclideanWidth);
    }

    public double getEuclideanHeight() {
        return euclideanHeight.get();
    }

    public DoubleProperty euclideanHeightProperty() {
        return euclideanHeight;
    }

    public void setEuclideanHeight(double euclideanHeight) {
        this.euclideanHeight.set(euclideanHeight);
    }

    public ReadOnlyObjectProperty<Geometry> geometryProperty() {
        return geometryProperty;
    }

    public Geometry getGeometry() {
        return geometryProperty.get();
    }


    public ReadOnlyObjectProperty<HasHyperbolicModel.HyperbolicModel> hyperbolicModelProperty() {
        return hyperbolicModel;
    }

    public Parent getRoot() {
        return root;
    }
}
