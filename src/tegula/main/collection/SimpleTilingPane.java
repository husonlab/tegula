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
package tegula.main.collection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
 * setup a simple tiling pane
 * Daniel Huson, 4.2019
 */
public class SimpleTilingPane extends StackPane {
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.GHOSTWHITE;
    private final SimpleTiling simpleTiling;

    private final ObjectProperty<Transform> worldRotation;

    /**
     * constructor
     *
     * @param dSymbol
     * @param tilingStyle
     */
    public SimpleTilingPane(DSymbol dSymbol, TilingStyle tilingStyle) {
        this(dSymbol, tilingStyle, DEFAULT_BACKGROUND_COLOR);
    }

    /**
     * constructor
     *
     * @param dSymbol
     * @param tilingStyle
     * @param backgroundColor for sphere, hyperbolic circle or plane
     */
    public SimpleTilingPane(DSymbol dSymbol, TilingStyle tilingStyle, Color backgroundColor) {
        final PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        camera.setTranslateZ(-500);
        camera.setFieldOfView(35);

        worldRotation = new SimpleObjectProperty<>(this, "rotation", new Rotate(0, 0, 0, 1));

        final Translate worldTranslate = new Translate(0, 0, 0);
        final Scale worldScale = new Scale(1, 1);

        final Group world = new Group();
        final Group universe = new Group(world);

        if (tilingStyle == null) {
            tilingStyle = new TilingStyle();
            tilingStyle.setBandColor(Color.BLACK);
            tilingStyle.setShowFaces(false);
        }

        this.simpleTiling = new SimpleTiling(dSymbol, universe, world, camera, tilingStyle);
        widthProperty().addListener((c, o, n) -> simpleTiling.setWidth(n.doubleValue()));
        heightProperty().addListener((c, o, n) -> simpleTiling.setHeight(n.doubleValue()));

        final SubScene subScene = new SubScene(new Group(universe), getWidth(), getHeight(), simpleTiling.getGeometry() != Geometry.Euclidean, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        if (backgroundColor != Color.TRANSPARENT) {
            if (simpleTiling.getTilingMeshes().getGeometry() == Geometry.Spherical || simpleTiling.getTilingMeshes().getGeometry() == Geometry.Hyperbolic) {
                final double factor = (simpleTiling.getTilingMeshes().getGeometry() == Geometry.Spherical ? 0.45 : 0.5);
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
        getChildren().add(subScene);

        world.getTransforms().add(worldTranslate);
        world.getTransforms().add(worldScale);
        world.getTransforms().add(getWorldRotation());

        worldRotation.addListener((observable, oldValue, newValue) -> {
            int indexOf = world.getTransforms().indexOf(oldValue);
            world.getTransforms().set(indexOf, newValue);
        });

        subScene.heightProperty().bind(heightProperty());
        subScene.widthProperty().bind(widthProperty());
    }

    public void update() {
        getSimpleTiling().update();
    }

    public SimpleTiling getSimpleTiling() {
        return simpleTiling;
    }

    public Transform getWorldRotation() {
        return worldRotation.get();
    }

    public ObjectProperty<Transform> worldRotationProperty() {
        return worldRotation;
    }
}
