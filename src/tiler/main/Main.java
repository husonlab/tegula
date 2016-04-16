package tiler.main;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.OrbifoldGroupName;
import tiler.tiling.EuclideanGeometry;
import tiler.tiling.FundamentalDomain;

import java.io.Reader;
import java.io.StringReader;

/**
 * main program
 */
public class Main extends Application {
    private DSymbol dsymbol = new DSymbol();
    private FDomain fDomain;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        // Reader r = new StringReader("<23.1:20:2 4 6 8 10 12 14 16 18 20,2 10 5 9 8 20 13 15 17 19,11 12 13 14 15 16 17 18 19 20:3 3 6 5,4 4 4>");
        // edit the last 6 in above symbol to 5 for spherical and 7 or above for hyperbolic
        Reader r = new StringReader("<23.1:20:2 4 6 8 10 12 14 16 18 20,2 10 5 9 8 20 13 15 17 19,11 12 13 14 15 16 17 18 19 20:3 3 5 5,4 4 4>");
        //Reader r = new StringReader("<62.1:2:2,2,2:4,4>");
        // Reader r = new StringReader("<93.1:1:1,1,1:3,6>");

        {
            if (dsymbol.read(r)) {
                System.err.println(dsymbol);
                System.err.println("Group: " + OrbifoldGroupName.getGroupName(dsymbol));
                fDomain = new FDomain(dsymbol);
                System.err.println(fDomain);
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.err.println("Drag mouse to drag");
        System.err.println("Shift-drag mouse to rotate");
        System.err.println("Mouse-wheel to zoom");

        final ObjectProperty<Transform> worldRotateProperty = new SimpleObjectProperty<>(this, "rotation", new Rotate(0, 0, 0, 1));
        final Translate worldTranslate = new Translate(500, 500);
        final Scale worldScale = new Scale(5, 5);

        stage.setTitle("Tiler");

        final Group world = new Group();

        world.getTransforms().add(worldTranslate);
        world.getTransforms().add(worldScale);
        world.getTransforms().add(worldRotateProperty.get());

        // setup top pane and stacked pane

        final FXMLLoader fxmlLoader = new FXMLLoader();
        Pane root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        final Controller controller = fxmlLoader.getController();

        final Pane topPane = controller.getTopPane();
        topPane.setPickOnBounds(false);

        final Pane worldPane = controller.getWorldPane();
        worldPane.getChildren().add(world);


        // setup scene and stage:
        boolean useZBuffer = false; // for debugging, it is visually clearer not to use a zbuffer

        final Scene scene = new Scene(root, 1000, 1000, useZBuffer);
        MouseHandler.addMouseHandler(scene, worldTranslate, worldScale, worldRotateProperty);

        //scene.setCamera(new PerspectiveCamera(true));

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();


        // add a huge sphere for debugging:
        if (fDomain.getGeometry() == FDomain.Geometry.Spherical) {
            Sphere sphere = new Sphere(100);
            sphere.setDrawMode(DrawMode.LINE);
            world.getChildren().add(sphere);
        } else if (fDomain.getGeometry() == FDomain.Geometry.Hyperbolic) {
            Circle circle = new Circle(100);
            circle.getTransforms().add(new Translate(0, 0, 100));
            circle.setStroke(Color.DARKGREY);
            circle.setFill(Color.TRANSPARENT);
            world.getChildren().add(circle);
        }

        final Group fund = FundamentalDomain.buildFundamentalDomain(dsymbol, fDomain);
        world.getChildren().addAll(fund);

        if (false) {
            Transform transform = EuclideanGeometry.createTransform(fDomain.getVertex3D(0, 15), fDomain.getVertex3D(1, 15), fDomain.getVertex3D(0, 5),
                    fDomain.getVertex3D(1, 5), true);
            Group group2 = FundamentalDomain.buildFundamentalDomain(dsymbol, fDomain);
            group2.getTransforms().add(transform);
            world.getChildren().add(group2);
        }

        if (false) {
            Transform transform = EuclideanGeometry.createTransform(fDomain.getVertex3D(0, 7), fDomain.getVertex3D(2, 7), fDomain.getVertex3D(0, 8), fDomain.getVertex3D(2, 8), true);
            Group group2 = FundamentalDomain.buildFundamentalDomain(dsymbol, fDomain);
            group2.getTransforms().add(transform);
            world.getChildren().add(group2);
        }


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
}
