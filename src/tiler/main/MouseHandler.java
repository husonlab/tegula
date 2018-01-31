package tiler.main;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tiler.core.dsymbols.Geometry;

/**
 * mouse handler
 * Created by huson on 3/29/16.
 */
public class MouseHandler {
    public static boolean cbPullFDomain = false;
    private double originalMouseDownX;
    private double originalMouseDownY;
    private double mouseDownX;
    private double mouseDownY;
    private long mouseDownTime;

    private final TranslationAnimation animation;

    /**
     * add a mouse handler to the scene
     *
     * @param scene
     * @param worldTranslate
     * @param worldRotateProperty
     */
    public static void addMouseHandler(final Scene scene, final Translate worldTranslate, final Scale worldScale, final ObjectProperty<Transform> worldRotateProperty, final Document document) {
        new MouseHandler(scene, worldTranslate, worldScale, worldRotateProperty, document);
    }

    /**
     * constructor
     *
     * @param worldTranslate
     * @param worldRotateProperty
     */
    private MouseHandler(final Scene scene, final Translate worldTranslate, final Scale worldScale, final ObjectProperty<Transform> worldRotateProperty, final Document document) {
        animation = new TranslationAnimation(document);

        scene.setOnMousePressed((me) -> {
            originalMouseDownX = mouseDownX = me.getSceneX();
            originalMouseDownY = mouseDownY = me.getSceneY();
            mouseDownTime = System.currentTimeMillis();
        });
        scene.setOnMouseDragged((me) -> {
            double mouseDeltaX = me.getSceneX() - mouseDownX;
            double mouseDeltaY = me.getSceneY() - mouseDownY;


            if (me.isPrimaryButtonDown()) {
                if (document.geometryProperty().getValue() != Geometry.Spherical) { // slide
                    double modifierFactor = 1;
                    double dx = mouseDeltaX * modifierFactor;
                    double dy = mouseDeltaY * modifierFactor;

                    if (dx != 0 || dy != 0) {
                        if (cbPullFDomain && !animation.isPlaying()){
                            document.translateFDomain(dx, dy);
                        }
                        else {
                            document.translateTiling(dx, dy);
                        }

                        // Checks whether (dx,dy) has been modified.
                        if (document.directionChanged()){
                            // Modify mouse position in hyperbolic case.
                            mouseDownX = me.getSceneX() - document.getTranslation().getX();
                            mouseDownY = me.getSceneY() - document.getTranslation().getY();
                        }
                        else {
                            mouseDownX = me.getSceneX();
                            mouseDownY = me.getSceneY();
                        }

                    }

                } else { //// rotate
                    final Point2D delta = new Point2D(me.getSceneX() - mouseDownX, me.getSceneY() - mouseDownY);
                    //noinspection SuspiciousNameCombination
                    final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                    final Rotate rotate = new Rotate(0.25 * delta.magnitude(), dragOrthogonalAxis);
                    worldRotateProperty.setValue(rotate.createConcatenation(worldRotateProperty.get()));
                    mouseDownX = me.getSceneX();
                    mouseDownY = me.getSceneY();
                }
            }
        });
        scene.setOnMouseReleased((me) -> {
            if (cbPullFDomain){
                document.update();
            }
            if (me.isShiftDown()) {
                if (document.geometryProperty().getValue() != Geometry.Spherical) { // slide
                    double mouseDeltaX = me.getSceneX() - originalMouseDownX;
                    double mouseDeltaY = me.getSceneY() - originalMouseDownY;

                    double modifierFactor = 1;
                    double dx = mouseDeltaX * modifierFactor;
                    double dy = mouseDeltaY * modifierFactor;

                    animation.set(dx, dy, System.currentTimeMillis() - mouseDownTime);

                    if (dx != 0 || dy != 0) {
                        animation.play();
                    } else
                        animation.pause();
                }
            }
        });

        scene.setOnScroll(me -> {
                    if (me.getDeltaY() != 0) {
                        double factor = (me.getDeltaY() > 0 ? 1.1 : 0.9);
                        worldScale.setX(factor * worldScale.getX());
                        worldScale.setY(factor * worldScale.getY());
                        document.width *= 1 / factor;
                        document.height *= 1 / factor;
                        if (document.geometryProperty().getValue() == Geometry.Euclidean) {
                            document.update();
                        }
                    }
                }
        );

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case P:
                    if (animation.isPlaying())
                        animation.pause();
                    else
                        animation.play();
                    break;
                case LEFT:
                    break;
                case RIGHT:
                    break;
                case DOWN:
                    break;
                case UP:
                    break;
                case EQUALS:
                case PLUS:
                    break;
                case UNDERSCORE:
                case MINUS:
                    break;
            }
        });


    }

}
