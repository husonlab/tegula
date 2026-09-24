/*
 * TilingPane.java Copyright (C) 2023 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tilingpane;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
import tegula.tiling.EuclideanTiling;
import tegula.tiling.HyperbolicTiling;
import tegula.tiling.TilingBase;
import tegula.tiling.TilingCreator;
import tegula.tilingeditor.SelectionSupport;
import tegula.util.Updateable;
import tegula.window.CameraSettings;
import tegula.window.TilingStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final ObjectProperty<Transform> worldRotate = new SimpleObjectProperty<>(this, "rotation", new Rotate(0, 0, 0, 1));

    private final Translate worldTranslate = new Translate(0, 0, 0);
    private final Scale worldScale = new Scale(1, 1);

    private final Group tiles = new Group();

	// Depth sorting for semi-transparent spherical tilings: JavaFX draws 3D nodes in scene-graph order and
	// lets transparent geometry write depth, so a near tile drawn first simply rejects the tiles behind it,
	// which then vanish instead of showing through. Drawing the copies back to front fixes that.
	private final List<Node> sphericalCopies = new ArrayList<>();
	private final List<Point3D> sphericalCopyCenters = new ArrayList<>();
    private final Group world = new Group();
    private final Group universe = new Group(world);

    private final SubScene subScene;

    private final PointLight pointLight;
    private final AmbientLight ambientLight;

    private final BooleanProperty showSphereAsDisk = new SimpleBooleanProperty(false);

    private Circle disk;

    private final LongProperty lastWorldUpdate = new SimpleLongProperty();

    private final LongProperty lastDSymbolUpdate = new SimpleLongProperty();

    private final MouseHandler mouseHandler;

    private final InvalidationListener updateListener=e->update();

    /**
     * constructor
     */
    public TilingPane(DSymbol dSymbol, TilingStyle tilingStyle) {
        this(dSymbol, tilingStyle, false, true);
    }

    /**
     * constructor
     *
	 */
    public TilingPane(DSymbol dSymbol, TilingStyle tilingStyle, boolean showSphereAsDisk, boolean allowMouseInteraction) {
        this.tilingStyle = tilingStyle;

        TilingStyle.hideFirstTile.addListener(new WeakInvalidationListener(updateListener));

        setShowSphereAsDisk(showSphereAsDisk);

        pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-1000);
        pointLight.setTranslateY(-1000);
        pointLight.setTranslateZ(-10000);
        pointLight.setRotate(90);

        ambientLight = new AmbientLight(Color.WHITE);

        subScene = new SubScene(new Group(universe), 800, 800, true, SceneAntialiasing.BALANCED);

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
            if (o == Geometry.Spherical && n != Geometry.Spherical) {
                worldRotate.setValue(new Rotate()); // remove any rotations
            }
            if (n == Geometry.Euclidean) {
                subScene.setCamera(new ParallelCamera());
            } else {
                var camera=new PerspectiveCamera(true);
                subScene.setCamera(camera);
            }
        });
        geometry.set(dSymbol.computeGeometry());

        setBackground(new Background(new BackgroundFill(tilingStyle.getBackgroundColor(), null, null)));

        setBackground(new Background(new BackgroundFill(tilingStyle.getBackgroundColor(), null, null)));
        tilingStyle.backgroundColorProperty().addListener((c, o, n) -> setBackground(new Background(new BackgroundFill(n, null, null))));

        // need to call this otherwise euclidean dimensions won't be correct

        worldRotate.addListener((observable, oldValue, newValue) -> {
            int indexOf = world.getTransforms().indexOf(oldValue);
            world.getTransforms().set(indexOf, newValue);
			sortCopiesBackToFront();
        });


        widthProperty().addListener(c -> {
            if (getTiling() instanceof EuclideanTiling) {
                update();
            }
        });
        heightProperty().addListener(c -> {
            if (getTiling() instanceof EuclideanTiling) {
                update();
            }
        });

        if (allowMouseInteraction)
            mouseHandler = new MouseHandler(this);
        else
            mouseHandler = null;

        hyperbolicModel.addListener((c, o, n) -> {
            if (getGeometry() == Geometry.Hyperbolic)
                CameraSettings.setupHyperbolicCamera(getCamera(), n, true);
        });

        computTiling(dSymbol);
    }

    /**
     * set the Delaney-Dress symbol
     *
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
        //System.err.println("Update");
        if (getTiling() == null)
            return;

        if (getTiling() instanceof EuclideanTiling) {
            ((EuclideanTiling) getTiling()).setWidth(getWidth() / worldScale.getX());
            ((EuclideanTiling) getTiling()).setHeight(getHeight() / worldScale.getY());
        }

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
			case Spherical -> {
				setupLighting(tilingStyle.isUsePointLight());
				Platform.runLater(() -> CameraSettings.setupSphericalCamera(getCamera()));
			}
			case Hyperbolic -> {
				// the hyperboloid is a curved surface in its own right, and is already viewed through a
				// perspective camera, so a point light gives it real shading just as it does the sphere
				setupLighting(tilingStyle.isUsePointLight());
				Platform.runLater(() -> CameraSettings.setupHyperbolicCamera(getCamera(), hyperbolicModel.get(), false)
				);
			}
			case Euclidean -> {
				// the euclidean plane is flat, so every normal points the same way and a point light only
				// washes it evenly: nothing is gained, and the ambient light is what it wants
				setupLighting(false);
				Platform.runLater(() -> CameraSettings.setupEuclideanCamera(getCamera())
				);
				if (false) {
					System.err.println("Width: " + getWidth());
					System.err.println("tiling width: " + ((EuclideanTiling) getTiling()).getWidth());
					Rectangle test2 = new Rectangle(((EuclideanTiling) getTiling()).getWidth() - 5, ((EuclideanTiling) getTiling()).getHeight() - 5);
					test2.setFill(Color.TRANSPARENT);
					test2.setStroke(Color.GREEN);
					Rectangle small = new Rectangle(100 * getFDomain().getBoundingBox().getWidth(), 100 * getFDomain().getBoundingBox().getHeight());
					small.setTranslateX(50);
					small.setTranslateY(50);
					small.setFill(Color.TRANSPARENT);
					small.setStroke(Color.ORANGE);
					additionalStuff.getChildren().addAll(test2, small);
					//System.err.println(String.format("Rect: %.1f x %.1f", rect.getWidth(), rect.getHeight()));
				}
			}
		}
        tiles.getChildren().setAll(getTiling().update().getChildren());

        getWorld().getChildren().addAll(tiles, additionalStuff);
		setupDepthSorting();
        incrementLastWorldUpdate();
    }

    /**
     * translate a euclidean or hyperbolic tiling
     *
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
	 * lights the scene either by the point light, which shades a surface by how it is turned and so brings out
	 * its relief, or by the ambient light, which lights everything alike. The two are exclusive: were the ambient
	 * light left on, it would flood the shading that the point light is there to give.
	 */
	private void setupLighting(boolean usePointLight) {
		if (usePointLight) {
			if (!universe.getChildren().contains(pointLight))
				universe.getChildren().add(pointLight);
			universe.getChildren().remove(ambientLight);
		} else {
			universe.getChildren().remove(pointLight);
			if (!universe.getChildren().contains(ambientLight))
				universe.getChildren().add(ambientLight);
		}
	}

	/**
	 * records the copies of the fundamental domain and their centers, so that they can be depth sorted
	 */
	private void setupDepthSorting() {
		sphericalCopies.clear();
		sphericalCopyCenters.clear();
		if (getGeometry() != Geometry.Spherical)
			return;
		for (Node copy : tiles.getChildren()) {
			final var bounds = copy.getBoundsInParent();
			if (bounds.isEmpty())
				continue;
			sphericalCopies.add(copy);
			sphericalCopyCenters.add(new Point3D(0.5 * (bounds.getMinX() + bounds.getMaxX()),
					0.5 * (bounds.getMinY() + bounds.getMaxY()), 0.5 * (bounds.getMinZ() + bounds.getMaxZ())));
		}
		sortCopiesBackToFront();
	}

	/**
	 * draws the copies of the fundamental domain from back to front. Only semi-transparent tilings need this:
	 * when everything is opaque the depth buffer sorts the copies correctly by itself, whatever the order.
	 */
	private void sortCopiesBackToFront() {
		if (sphericalCopies.size() < 2 || getGeometry() != Geometry.Spherical)
			return;
		if (getTilingStyle().getTileOpacity() >= 1 && getTilingStyle().getBandOpacity() >= 1)
			return;
		final Transform rotate = getWorldRotate();
		final Map<Node, Double> depth = new HashMap<>();
		for (int i = 0; i < sphericalCopies.size(); i++)
			depth.put(sphericalCopies.get(i), rotate.transform(sphericalCopyCenters.get(i)).getZ());
		// the camera looks along +z, so the largest z is the furthest away and must be drawn first.
		// Sort a copy and then setAll: sorting the children list in place would momentarily hold a node
		// twice, which JavaFX rejects as a duplicate child
		final List<Node> sorted = new ArrayList<>(tiles.getChildren());
		sorted.sort((a, b) -> Double.compare(depth.getOrDefault(b, 0.0), depth.getOrDefault(a, 0.0)));
		if (!sorted.equals(tiles.getChildren()))
			tiles.getChildren().setAll(sorted);
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

    public MouseHandler getMouseHandler() {
        return mouseHandler;
    }

    public void updateTileColors() {
        final Consumer<Node> update = (node) -> {
            final Pair<Character, Integer> pair = SelectionSupport.getTypeAndId(node);
            if (pair != null && pair.getFirst() == 't') {
				final PhongMaterial material = new PhongMaterial(tilingStyle.getTileColor(pair.getSecond()));
				TilingStyle.applyTileSpecular(material, getGeometry());
				((MeshView) node).setMaterial(material);
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
                // the outline of a woven band shares the edge's id but is not painted in the band color
                final boolean outline = tegula.tiling.parts.Weave.OUTLINE.equals(node.getUserData());
                final PhongMaterial material = new PhongMaterial(outline ? getTilingStyle().getWeaveBorderColor()
                        : getTilingStyle().getBandColor());
                TilingStyle.applyBandSpecular(material, getGeometry());
                ((MeshView) node).setMaterial(material);
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

    public Camera getCamera() {
        return subScene.getCamera();
    }
}
