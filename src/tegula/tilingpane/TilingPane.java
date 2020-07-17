/*
 * TilingPane.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.tilingpane;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
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
import jloda.util.Pair;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.main.CameraSettings;
import tegula.main.TilingStyle;
import tegula.tiling.EuclideanTiling;
import tegula.tiling.HyperbolicTiling;
import tegula.tiling.TilingBase;
import tegula.tiling.TilingCreator;
import tegula.tilingeditor.SelectionSupport;
import tegula.util.Updateable;

import java.util.function.Consumer;

/**
 * a single tiling pane
 * Daniel Huson and Ruediger Zeller, 4.2019
 */
public class TilingPane extends StackPane implements Updateable {
    private final SimpleObjectProperty<CameraSettings.HyperbolicModel> hyperbolicModel = new SimpleObjectProperty<>(CameraSettings.HyperbolicModel.Poincare);
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

    private final BooleanProperty showSphereAsDisk = new SimpleBooleanProperty(false);

    private Circle disk;

    private final LongProperty lastWorldUpdate = new SimpleLongProperty();

    private final LongProperty lastDSymbolUpdate = new SimpleLongProperty();

    private final MouseHandler mouseHandler;

    private SubScene subScene;

    /**
     * constructor
     */
    public TilingPane(DSymbol dSymbol, TilingStyle tilingStyle) {
        this(dSymbol, tilingStyle, false, true);
    }

    /**
     * constructor
     *
     * @param showSphereAsDisk
     * @param allowMouseInteraction
     */
    public TilingPane(DSymbol dSymbol, TilingStyle tilingStyle, boolean showSphereAsDisk, boolean allowMouseInteraction) {
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

        subScene = new SubScene(new Group(universe), 800, 800, true, SceneAntialiasing.BALANCED);
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
        });

        setBackground(new Background(new BackgroundFill(tilingStyle.getBackgroundColor(), null, null)));

        setBackground(new Background(new BackgroundFill(tilingStyle.getBackgroundColor(), null, null)));
        tilingStyle.backgroundColorProperty().addListener((c, o, n) -> setBackground(new Background(new BackgroundFill(n, null, null))));

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

        computTiling(dSymbol);
    }

    /**
     * set the Delaney-Dress symbol
     *
     * @param dSymbol
     */
    public void computTiling(DSymbol dSymbol) {
        geometry.set(dSymbol.computeGeometry());
        tiling.set(TilingCreator.create(dSymbol, tilingStyle, this));
        incrementLastDSymbolUpdate();
        update();
    }

    // Reset fundamental domain (without updating the set of tiles)
    public void reset() {
        if (getTiling() != null)
            computTiling(getTiling().getDSymbol());
    }


    /**
     * update the tiling
     */
    public void update() {
        // System.err.println("Update");
        setUseDepthBuffer(this, getGeometry() != Geometry.Euclidean);


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
                    subScene.heightProperty().addListener((e) -> disk.setRadius(factor * Math.min(getWidth(), getHeight())));
                    subScene.widthProperty().addListener((e) -> disk.setRadius(factor * Math.min(getWidth(), getHeight())));
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
            case Spherical:
                if (!universe.getChildren().contains(pointLight))
                    universe.getChildren().add(pointLight);
                universe.getChildren().remove(ambientLight);
                Platform.runLater(() -> {
                    CameraSettings.setupSphericalCamera(perspectiveCamera);
                });
                break;
// need to do this later because tilings are built in separate thread when reading file
            case Hyperbolic:
                universe.getChildren().remove(pointLight);
                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);
                Platform.runLater(() -> {
                            CameraSettings.setupHyperbolicCamera(perspectiveCamera, hyperbolicModel.get(), false);
                        }
                );
                break;
// CameraSettings.setupEuclideanCamera(perspectiveCamera);
//Add rectangles for debugging
            case Euclidean:
                universe.getChildren().remove(pointLight);
                if (!universe.getChildren().contains(ambientLight))
                    universe.getChildren().add(ambientLight);
                if (false) {
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
        tiles.getChildren().setAll(getTiling().update().getChildren());

        getWorld().getChildren().addAll(tiles, additionalStuff);
        incrementLastWorldUpdate();
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
        if (useDepthBuffer != subScene.isDepthBuffer() || useDepthBuffer && perspectiveCamera == null) {
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


    public LongProperty lastWorldUpdateProperty() {
        return lastWorldUpdate;
    }

    public void incrementLastWorldUpdate() {
        lastWorldUpdate.set(lastWorldUpdate.get() + 1);
    }

    public LongProperty lastDSymbolUpdateProperty() {
        return lastDSymbolUpdate;
    }

    public void incrementLastDSymbolUpdate() {
        lastDSymbolUpdate.set(lastDSymbolUpdate.get() + 1);
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

    public CameraSettings.HyperbolicModel getHyperbolicModel() {
        return hyperbolicModel.get();
    }

    public void setHyperbolicModel(CameraSettings.HyperbolicModel hyperbolicModel) {
        this.hyperbolicModel.set(hyperbolicModel);
    }

    public ObjectProperty<CameraSettings.HyperbolicModel> hyperbolicModelProperty() {
        return hyperbolicModel;
    }

    public PerspectiveCamera getPerspectiveCamera() {
        return perspectiveCamera;
    }

    public MouseHandler getMouseHandler() {
        return mouseHandler;
    }

    public void updateTileColors() {
        final Consumer<Node> update = (node) -> {
            final Pair<Character, Integer> pair = SelectionSupport.getTypeAndId(node);
            if (pair != null && pair.getFirst() == 't') {
                ((MeshView) node).setMaterial(new PhongMaterial(tilingStyle.getTileColor(pair.getSecond())));
            }
        };

        visitAllNodes(tiles, update);

        for (Node node : getTiling().recycler()) {
            visitAllNodes(node, update);
        }

        if (getTiling().getFundPrototype() != null) {
            visitAllNodes(getTiling().getFundPrototype(), update);
        }
        incrementLastWorldUpdate();
    }

    public void updateBandColors() {
        final Consumer<Node> update = (node) -> {
            final Pair<Character, Integer> pair = SelectionSupport.getTypeAndId(node);
            if (pair != null && (pair.getFirst() == 'e' || pair.getFirst() == 'v')) {
                ((MeshView) node).setMaterial(new PhongMaterial(getTilingStyle().getBandColor()));
            }
        };

        visitAllNodes(tiles, update);

        for (Node node : getTiling().recycler()) {
            visitAllNodes(node, update);
        }

        if (getTiling().getFundPrototype() != null) {
            visitAllNodes(getTiling().getFundPrototype(), update);
        }
        incrementLastWorldUpdate();
    }

    public FDomain getFDomain() {
        return getTiling().getfDomain();
    }

    public void changeCoordinates(Point2D[][] coordinates) {
        getFDomain().setCoordinates(coordinates);
        update();

    }

    private static void visitAllNodes(Node node, Consumer<Node> consumer) {
        consumer.accept(node);
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable())
                visitAllNodes(child, consumer);
        }
    }
}
