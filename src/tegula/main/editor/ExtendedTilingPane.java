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

package tegula.main.editor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.Geometry;
import tegula.main.TilingStyle;

/**
 * setup an extended tiling pane
 * Daniel Huson, 4.2019
 */
public class ExtendedTilingPane extends Pane {
    private final ExtendedTiling extendedTiling;

    private final ObjectProperty<Transform> worldRotate = new SimpleObjectProperty<>();
    private final ObjectProperty<Scale> worldScale = new SimpleObjectProperty<>();
    private final ObjectProperty<Translate> worldTranslate = new SimpleObjectProperty<>();

    private SubScene subScene;

    private final BooleanProperty useDepthBuffering = new SimpleBooleanProperty();

    /**
     * constructor
     *
     * @param dSymbol
     * @param tilingStyle
     * @param backgroundColor for sphere, hyperbolic circle or plane
     */
    public ExtendedTilingPane(DSymbol dSymbol, TilingStyle tilingStyle, Color backgroundColor) {
        resetView();

        final PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        camera.setTranslateZ(-500);
        camera.setFieldOfView(35);

        final Group world = new Group();
        final Group universe = new Group(world);

        if (tilingStyle == null) {
            tilingStyle = new TilingStyle();
            tilingStyle.setBandColor(Color.BLACK);
            tilingStyle.setShowFaces(false);
        }

        extendedTiling = new ExtendedTiling(dSymbol, this, universe, world, camera, tilingStyle);
        widthProperty().addListener((c, o, n) -> {
            extendedTiling.setWidth(n.doubleValue());
        });
        heightProperty().addListener((c, o, n) -> {
            extendedTiling.setHeight(n.doubleValue());
        });

        useDepthBuffering.addListener((c, o, n) -> {
            if (subScene != null) {
                subScene.setCamera(null);
                getChildren().remove(subScene);
            }
            subScene = new SubScene(new Group(universe), getWidth(), getHeight(), n, SceneAntialiasing.BALANCED);
            subScene.setCamera(camera);
            subScene.heightProperty().bind(heightProperty());
            subScene.widthProperty().bind(widthProperty());
            getChildren().add(subScene);
        });

        useDepthBuffering.bind(extendedTiling.geometryProperty().isNotEqualTo(Geometry.Euclidean));

        if (backgroundColor != Color.TRANSPARENT) {
            if (extendedTiling.getTilingMeshes().getGeometry() != Geometry.Euclidean) {
                final double factor = (extendedTiling.getTilingMeshes().getGeometry() == Geometry.Spherical ? 0.45 : 0.5);
                final Circle circle = new Circle(factor * Math.min(getWidth(), getHeight()));
                circle.setStroke(backgroundColor.darker());
                circle.setFill(backgroundColor);

                circle.setStrokeWidth(2);
                subScene.heightProperty().addListener((e) -> {
                    circle.setRadius(factor * Math.min(getWidth(), getHeight()));
                });
                subScene.widthProperty().addListener((e) -> {
                    circle.setRadius(factor * Math.min(getWidth(), getHeight()));
                });
                StackPane.setAlignment(circle, Pos.CENTER);
                getChildren().add(circle);
                setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
            } else // euclidean
            {
                setBackground(new Background(new BackgroundFill(backgroundColor, null, null)));

            }
        } else {
            setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        }

        StackPane.setAlignment(subScene, Pos.CENTER);

        world.getTransforms().add(getWorldTranslate());
        world.getTransforms().add(getWorldScale());
        world.getTransforms().add(getWorldRotate());

        final ChangeListener<? super Transform> listener = (c, o, n) -> {
            int indexOf = world.getTransforms().indexOf(o);
            world.getTransforms().set(indexOf, n);
        };

        worldTranslate.addListener(listener);
        worldScale.addListener(listener);
        worldRotate.addListener(listener);
    }

    public ExtendedTiling getExtendedTiling() {
        return extendedTiling;
    }

    public Transform getWorldRotate() {
        return worldRotate.get();
    }

    public ObjectProperty<Transform> worldRotateProperty() {
        return worldRotate;
    }

    public Scale getWorldScale() {
        return worldScale.get();
    }

    public ObjectProperty<Scale> worldScaleProperty() {
        return worldScale;
    }

    public void setWorldScale(Scale worldScale) {
        this.worldScale.set(worldScale);
    }

    public Translate getWorldTranslate() {
        return worldTranslate.get();
    }

    public ObjectProperty<Translate> worldTranslateProperty() {
        return worldTranslate;
    }

    public void setWorldTranslate(Translate worldTranslate) {
        this.worldTranslate.set(worldTranslate);
    }

    public void resetView() {
        worldRotate.set(new Rotate(0, 0, 0, 1));
        worldScale.set(new Scale(1, 1, 1));
        worldTranslate.set(new Translate(0, 0, 0));
    }

    public void replaceTiling(DSymbol ds) {
        final Geometry oldGeometry = extendedTiling.getGeometry();
        extendedTiling.replaceTiling(ds);
        if (extendedTiling.getGeometry() != oldGeometry)
            resetView();
    }
}
