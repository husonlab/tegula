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

package tegula.single;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import jloda.util.Basic;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.Geometry;
import tegula.core.dsymbols.OrbifoldGroupName;
import tegula.main.CameraSettings;
import tegula.main.TilingStyle;
import tegula.tiling.*;
import tegula.util.HasHyperbolicModel;
import tegula.util.Updateable;

/**
 * a single tiling pane
 * Daniel Huson and Ruediger Zeller, 4.2019
 */
public class SingleTilingPane extends StackPane implements Updateable {
    private final SimpleObjectProperty<HasHyperbolicModel.HyperbolicModel> hyperbolicModel = new SimpleObjectProperty<>(HasHyperbolicModel.HyperbolicModel.Poincare);
    private final ObjectProperty<TilingBase> tiling = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Geometry> geometry = new SimpleObjectProperty<>();
    private final TilingStyle tilingStyle;

    private final DoubleProperty euclideanWidth = new SimpleDoubleProperty(800);
    private final DoubleProperty euclideanHeight = new SimpleDoubleProperty(800);

    private final ObjectProperty<Transform> worldRotate = new SimpleObjectProperty<>(this, "rotation", new Rotate(0, 0, 0, 1));

    private final Translate worldTranslate = new Translate(0, 0, 0);
    private final Scale worldScale = new Scale(1, 1);

    private final Group tiles = new Group();
    private final Group world = new Group();
    private final Group universe = new Group(world);

    private PerspectiveCamera perspectiveCamera;

    private final PointLight pointLight;
    private final AmbientLight ambientLight;

    private boolean drawFundamentalDomainOnly = false;

    private final BooleanProperty showSphereAsDisk = new SimpleBooleanProperty(false);

    private Circle disk;

    private final LongProperty lastUpdate = new SimpleLongProperty();

    private final BooleanProperty maximalTiling = new SimpleBooleanProperty();
    private final BooleanProperty orientableTiling = new SimpleBooleanProperty();
    private final StringProperty groupName = new SimpleStringProperty();
    private final StringProperty infoLine = new SimpleStringProperty("");

    private final MouseHandler mouseHandler;

    private SubScene subScene;

    /**
     * constructor
     *
     * @param dSymbol
     */
    public SingleTilingPane(DSymbol dSymbol, TilingStyle tilingStyle) {
        this(dSymbol, tilingStyle, false, true);
    }

    /**
     * constructor
     *
     * @param dSymbol
     * @param showSphereAsDisk
     * @param allowMouseInteraction
     */
    public SingleTilingPane(DSymbol dSymbol, TilingStyle tilingStyle, boolean showSphereAsDisk, boolean allowMouseInteraction) {
        this.tilingStyle = tilingStyle;

        setShowSphereAsDisk(showSphereAsDisk);

        //root.prefWidthProperty().bind(widthProperty());
        //root.prefHeightProperty().bind(heightProperty());

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

        subScene = new SubScene(new Group(universe), 800, 800, false, SceneAntialiasing.BALANCED);
        subScene.setCamera(perspectiveCamera);

        world.getTransforms().add(worldTranslate);
        world.getTransforms().add(worldScale);
        world.getTransforms().add(getWorldRotate());

        //final Pane mainPane = controller.getMainPane();
        StackPane.setAlignment(subScene, Pos.CENTER);
        getChildren().add(0, subScene);

        subScene.heightProperty().bind(heightProperty());
        subScene.widthProperty().bind(widthProperty());

        // setup top pane and stacked pane

        geometryProperty().addListener((c, o, n) -> {
            setUseDepthBuffer(this, n != Geometry.Euclidean);
            if (o == Geometry.Spherical && n != Geometry.Spherical) {
                worldRotate.setValue(new Rotate()); // remove any rotations
            }
            if (o != Geometry.Hyperbolic && n == Geometry.Hyperbolic) {
                reset(); // looks like this helps to avoid the program getting stuck????
            }
        });

        setBackground(new Background(new BackgroundFill(tilingStyle.getBackgroundColor(), null, null)));

        setBackground(new Background(new BackgroundFill(tilingStyle.getBackgroundColor(), null, null)));
        tilingStyle.backgroundColorProperty().addListener((c, o, n) -> {
            setBackground(new Background(new BackgroundFill(n, null, null)));
        });

        // need to call this otherwise euclidean dimensions won't be correct
        setUseDepthBuffer(this, true);


        worldRotate.addListener((observable, oldValue, newValue) -> {
            int indexOf = world.getTransforms().indexOf(oldValue);
            world.getTransforms().set(indexOf, newValue);
        });

        if (allowMouseInteraction)
            mouseHandler = new MouseHandler(this);
        else
            mouseHandler = null;

        hyperbolicModel.addListener((c, o, n) -> {
            if (getGeometry() == Geometry.Hyperbolic)
                CameraSettings.setupHyperbolicCamera(perspectiveCamera, n, true);
        });

        replaceTiling(dSymbol);
        update();
    }


    /**
     * set the Delaney-Dress symbol
     *
     * @param dSymbol
     */
    public void replaceTiling(DSymbol dSymbol) {
        geometry.set(dSymbol.computeGeometry());
        tiling.set(TilingCreator.create(dSymbol, tilingStyle, this));
        maximalTiling.set(DSymbolAlgorithms.isMaximalSymmetry(getTiling().getDSymbol()));
        orientableTiling.set(getTiling().getDSymbol().computeOrientation() == 2);
        groupName.setValue(OrbifoldGroupName.getGroupName(dSymbol));
        infoLine.setValue(String.format("n:%d t:%d e:%d v:%d g:%s", dSymbol.size(), dSymbol.countOrbits(0, 1), dSymbol.countOrbits(0, 2), dSymbol.countOrbits(1, 2),
                getGroupName() + (isMaximalTiling() ? " max" : "") + (isOrientableTiling() ? " orient." : "")));
        update();
    }

    // Reset fundamental domain (without updating the set of tiles)
    public void reset() {
        if (getTiling() != null)
            replaceTiling(getTiling().getDSymbol());
    }


    /**
     * update the tiling
     */
    public void update() {
        // System.err.println("Update");

        if (getTiling() == null)
            return;

        getWorld().getChildren().clear();
        final Group additionalStuff = new Group();

        if (disk != null)
            getChildren().remove(disk);

        if (isShowSphereAsDisk() && (getGeometry() == Geometry.Spherical || getTiling().getGeometry() == Geometry.Hyperbolic)) {
            if (disk == null) {
                disk = new Circle(100);
                final double factor = (getTiling().getGeometry() == Geometry.Spherical ? 0.45 : 0.5);
                if (true) {
                    subScene.heightProperty().addListener((e) -> {
                        disk.setRadius(factor * Math.min(getWidth(), getHeight()));
                    });
                    subScene.widthProperty().addListener((e) -> {
                        disk.setRadius(factor * Math.min(getWidth(), getHeight()));
                    });
                }
                StackPane.setAlignment(disk, Pos.CENTER);
                getChildren().add(0, disk);
            } else if (!getChildren().contains(disk))
                getChildren().add(0, disk);

            disk.setStroke(getTilingStyle().getBackgroundColor().darker());
            disk.setFill(getTilingStyle().getBackgroundColor());

            disk.setStrokeWidth(2);
            setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        } else {
            setBackground(new Background(new BackgroundFill(getTilingStyle().getBackgroundColor(), null, null)));
        }


        switch (getGeometry()) {
            case Spherical: {
                CameraSettings.setupSphericalCamera(perspectiveCamera);

                if (!universe.getChildren().contains(pointLight))
                    universe.getChildren().add(pointLight);
                universe.getChildren().remove(ambientLight);
                break;
            }
            case Hyperbolic: {
                universe.getChildren().remove(pointLight);
                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);

                // need to do this later because tilings are built in separate thread when reading file
                Platform.runLater(() -> CameraSettings.setupHyperbolicCamera(perspectiveCamera, hyperbolicModel.get(), false));


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
                    //System.err.println(String.format("Rect: %.1f x %.1f", rect.getWidth(), rect.getHeight()));
                }
                break;
            }
        }
        tiles.getChildren().setAll(getTiling().update().getChildren());

        getWorld().getChildren().addAll(tiles, additionalStuff);

        lastUpdate.set(System.currentTimeMillis());
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
            subScene = newSubScene;
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
        return geometry;
    }

    public boolean isShowSphereAsDisk() {
        return showSphereAsDisk.get();
    }

    public BooleanProperty showSphereAsDiskProperty() {
        return showSphereAsDisk;
    }

    public void setShowSphereAsDisk(boolean showSphereAsDisk) {
        this.showSphereAsDisk.set(showSphereAsDisk);
    }

    public Geometry getGeometry() {
        return geometry.get();
    }


    public ReadOnlyObjectProperty<HasHyperbolicModel.HyperbolicModel> hyperbolicModelProperty() {
        return hyperbolicModel;
    }

    public LongProperty lastUpdateProperty() {
        return lastUpdate;
    }

    public Transform getWorldRotate() {
        return worldRotate.get();
    }

    public ObjectProperty<Transform> worldRotateProperty() {
        return worldRotate;
    }

    public void setWorldRotate(Transform worldRotate) {
        this.worldRotate.set(worldRotate);
    }

    public Translate getWorldTranslate() {
        return worldTranslate;
    }

    public Scale getWorldScale() {
        return worldScale;
    }

    public HasHyperbolicModel.HyperbolicModel getHyperbolicModel() {
        return hyperbolicModel.get();
    }

    public void setHyperbolicModel(HasHyperbolicModel.HyperbolicModel hyperbolicModel) {
        this.hyperbolicModel.set(hyperbolicModel);
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

    public String getInfoLine() {
        return infoLine.get();
    }

    public ReadOnlyStringProperty infoLineProperty() {
        return infoLine;
    }

    public MouseHandler getMouseHandler() {
        return mouseHandler;
    }

    public void updateTileColors() {
        CopyTiles.visitAllNodes(tiles, (node) -> {
            if (node instanceof MeshView && node.getUserData() instanceof String) {
                final String str = (String) node.getUserData();
                //System.err.println(str);
                if (str.startsWith("t=")) {
                    final int tileNumber = Basic.parseInt(str.substring(2));
                    ((MeshView) node).setMaterial(new PhongMaterial(tilingStyle.getTileColor(tileNumber)));
                }
            }
        });
    }

    public void updateBandColors() {
        CopyTiles.visitAllNodes(tiles, (node) -> {
            if (node instanceof MeshView && node.getUserData() instanceof String) {
                final String str = (String) node.getUserData();
                if (str.startsWith("e=")) {
                    ((MeshView) node).setMaterial(new PhongMaterial(getTilingStyle().getBandColor()));
                }
            }
        });
    }

}
