package tiler.main;

import javafx.application.Application;
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

import java.io.StringReader;
import java.util.Properties;

/**
 * main program
 */
public class Main extends Application {

    private static final Properties programPreferences = new Properties();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        camera.setTranslateZ(-500);
        camera.setFieldOfView(35);

        final ObjectProperty<Transform> worldRotateProperty = new SimpleObjectProperty<>(this, "rotation", new Rotate(0, 0, 0, 1));
        final Translate worldTranslate = new Translate(0, 0, 0);
        final Scale worldScale = new Scale(1, 1);

        stage.setTitle("Tiler");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        Pane root = fxmlLoader.load(getClass().getResource("MainView.fxml").openStream());
        final MainViewController mainViewController = fxmlLoader.getController();

        // setup world and subscene
        final Group world = new Group();
        final SubScene subScene = new SubScene(new Group(world), 800, 800, false, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        world.getTransforms().add(worldTranslate);
        world.getTransforms().add(worldScale);
        world.getTransforms().add(worldRotateProperty.get());

        final Pane topPane = mainViewController.getTopPane();
        topPane.setPickOnBounds(false);

        final Pane mainPane = mainViewController.getMainPane();
        mainPane.getChildren().add(0, subScene);

        StackPane.setAlignment(topPane, Pos.CENTER);
        StackPane.setAlignment(subScene, Pos.CENTER);

        subScene.heightProperty().bind(mainPane.heightProperty());
        subScene.widthProperty().bind(mainPane.widthProperty());

        // setup top pane and stacked pane

        final Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        final Document document = new Document(stage, world, mainViewController, camera);
        // read in a symbol for debugging:
        document.read(new StringReader("<23.1:20:2 4 6 8 10 12 14 16 18 20,2 10 5 9 8 20 13 15 17 19,11 12 13 14 15 16 17 18 19 20:3 3 5 5,4 4 4>"));

        MouseHandler.addMouseHandler(scene, worldTranslate, worldScale, worldRotateProperty, document);

        {
            mainViewController.getPoincareButton().setOnAction((e) -> HyperbolicModelCameraSettings.setModel(document, HyperbolicModelCameraSettings.Model.Poincare));
            mainViewController.getKleinButton().setOnAction((e) -> HyperbolicModelCameraSettings.setModel(document, HyperbolicModelCameraSettings.Model.Klein));
            mainViewController.getHyperboloidButton().setOnAction((e) -> HyperbolicModelCameraSettings.setModel(document, HyperbolicModelCameraSettings.Model.Hyperboloid));
        }

        document.update();


        /*mainViewController.getFieldOfViewSlider().valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                document.getCamera().setFieldOfView(new_val.intValue());
            }
        });*/


        /*
        Box box=new Box(100,100,100);
        box.setMaterial(new PhongMaterial(Color.AQUA));
        world.getChildren().add(box);
        */

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

    public static Properties getProgramPreferences() {
        return programPreferences;
    }
}
