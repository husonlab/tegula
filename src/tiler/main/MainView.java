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

package tiler.main;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import tiler.core.dsymbols.Geometry;

import java.io.IOException;
import java.io.StringReader;

/**
 * the main view
 * todo: extensive refactoring required
 * Daniel Huson, 1.2018
 */
public class MainView {
    /**
     * create a new main view
     *
     * @param stage
     * @throws IOException
     */
    public MainView(Stage stage) throws IOException {
        if (stage == null)
            stage = new Stage();

        final PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        camera.setTranslateZ(-500);
        camera.setFieldOfView(35);

        final ObjectProperty<Transform> worldRotateProperty = new SimpleObjectProperty<>(this, "rotation", new Rotate(0, 0, 0, 1));
        final Translate worldTranslate = new Translate(0, 0, 0);
        final Scale worldScale = new Scale(1, 1);

        stage.setTitle("PeriodicTiler");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        Pane root = fxmlLoader.load(getClass().getResource("MainView.fxml").openStream());
        final MainViewController mainViewController = fxmlLoader.getController();

        // setup world and subscene
        final Group world = new Group();
        final Group universe = new Group(world);

        final SubScene subScene = new SubScene(new Group(universe), 800, 800, false, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        world.getTransforms().add(worldTranslate);
        world.getTransforms().add(worldScale);
        world.getTransforms().add(worldRotateProperty.get());

        final Pane mainPane = mainViewController.getMainPane();
        mainPane.getChildren().add(0, subScene);

        //StackPane.setAlignment(topPane, Pos.CENTER);
        StackPane.setAlignment(subScene, Pos.CENTER);

        subScene.heightProperty().bind(mainPane.heightProperty());
        subScene.widthProperty().bind(mainPane.widthProperty());

        // setup top pane and stacked pane

        final Scene scene = new Scene(root, 800, 800);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        final Document document = new Document(universe, world, camera);

        SetupController.setup(mainViewController, document, stage);

        document.setWidth(800);
        document.setHeight(600);

        document.geometryProperty().addListener((c, o, n) -> {
            document.setUseDepthBuffer(mainPane, n != Geometry.Euclidean);
        });

        // read in a symbol for debugging:
        document.read(new StringReader("<23.1:20:2 4 6 8 10 12 14 16 18 20,2 10 5 9 8 20 13 15 17 19,11 12 13 14 15 16 17 18 19 20:3 3 5 5,4 4 4>"));

        MouseHandler.addMouseHandler(scene, worldTranslate, worldScale, worldRotateProperty, document);

        document.update();

        document.geometryProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == Geometry.Spherical && newValue != Geometry.Spherical) {
                worldRotateProperty.setValue(new Rotate()); // remove any rotations
            }
            if (oldValue != Geometry.Hyperbolic && newValue == Geometry.Hyperbolic) {
                document.reset(); // looks like this helps to avoid the program getting stuck????
            }
        });

        worldRotateProperty.addListener((observable, oldValue, newValue) -> {
            int indexOf = world.getTransforms().indexOf(oldValue);
            world.getTransforms().set(indexOf, newValue);
        });
    }
}
