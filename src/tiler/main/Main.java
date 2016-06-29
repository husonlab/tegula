package tiler.main;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import tiler.tiling.Tiling;

import java.io.StringReader;
import java.util.Properties;

/**
 * main program
 */
public class Main extends Application {
    private static final Properties properties = new Properties();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        System.err.println("Drag mouse to drag");
        System.err.println("Shift-drag mouse to rotate");
        System.err.println("Mouse-wheel to zoom");

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
        Pane root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        final Controller controller = fxmlLoader.getController();

        // setup world and subscene
        final Group world = new Group();
        final SubScene subScene = new SubScene(new Group(world), 800, 800, false, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        world.getTransforms().add(worldTranslate);
        world.getTransforms().add(worldScale);
        world.getTransforms().add(worldRotateProperty.get());

        final Pane topPane = controller.getTopPane();
        topPane.setPickOnBounds(false);

        final StackPane stackPane = controller.getStackPane();
        stackPane.getChildren().add(0, subScene);

        StackPane.setAlignment(topPane, Pos.CENTER);
        StackPane.setAlignment(subScene, Pos.CENTER);

        subScene.heightProperty().bind(stackPane.heightProperty());
        subScene.widthProperty().bind(stackPane.widthProperty());

        // setup top pane and stacked pane

        final Scene scene = new Scene(root, 800, 800);
        MouseHandler.addMouseHandler(scene, worldTranslate, worldScale, worldRotateProperty);


        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        Document document = new Document(stage, world, controller, camera);
        // read in a symbol for debugging:
        document.read(new StringReader("<23.1:20:2 4 6 8 10 12 14 16 18 20,2 10 5 9 8 20 13 15 17 19,11 12 13 14 15 16 17 18 19 20:3 3 5 5,4 4 4>"));
        document.update();

        /*controller.getFieldOfViewSlider().valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                document.getCamera().setFieldOfView(new_val.intValue());
            }
        });*/




        /*
        Box box=new Box(100,100,100);
        box.setMaterial(new PhongMaterial(Color.AQUA));
        world.getChildren().add(box);
        */

        worldRotateProperty.addListener(new ChangeListener<Transform>() {
            @Override
            public void changed(ObservableValue<? extends Transform> observable, Transform oldValue, Transform newValue) {
                int indexOf = world.getTransforms().indexOf(oldValue);
                world.getTransforms().set(indexOf, newValue);
            }
        });
    }

    public static Properties getProperties() {
        return properties;
    }
}
