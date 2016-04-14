package main;

import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * mouse handler
 * Created by huson on 3/29/16.
 */
public class MouseHandler {

    private double mouseDownX;
    private double mouseDownY;

    private Transform rotateAtMouseDown;

    private double cameraTranslateXAtMouseDown;
    private double cameraTranslateYAtMouseDown;

    /**
     * add a mouse handler to the scene
     *
     * @param scene
     * @param worldTranslate
     * @param worldRotateProperty
     */
    public static void addMouseHandler(final Scene scene, final Translate worldTranslate, final Scale worldScale, final ObjectProperty<Transform> worldRotateProperty) {
        new MouseHandler(scene, worldTranslate, worldScale, worldRotateProperty);
    }

    /**
     * constructor
     *
     * @param worldTranslate
     * @param worldRotateProperty
     */
    private MouseHandler(final Scene scene, final Translate worldTranslate, final Scale worldScale, final ObjectProperty<Transform> worldRotateProperty) {
        scene.setOnMousePressed((me) -> {
            mouseDownX = me.getSceneX();
            mouseDownY = me.getSceneY();
            rotateAtMouseDown = worldRotateProperty.getValue().clone();
            cameraTranslateXAtMouseDown = worldTranslate.getX();
            cameraTranslateYAtMouseDown = worldTranslate.getY();
        });
        scene.setOnMouseDragged((me) -> {
            double mouseDeltaX = me.getSceneX() - mouseDownX;
            double mouseDeltaY = me.getSceneY() - mouseDownY;

            if (me.isPrimaryButtonDown()) {
                if (!me.isShiftDown() && !me.isAltDown()) { // slide
                    double modifierFactor = 1;
                    worldTranslate.setX(cameraTranslateXAtMouseDown + mouseDeltaX * modifierFactor);
                    worldTranslate.setY(cameraTranslateYAtMouseDown + mouseDeltaY * modifierFactor);
                } else if (me.isShiftDown() && !me.isAltDown()) { //// rotate
                    double modifierFactor = 0.25;
                    //noinspection SuspiciousNameCombination
                    Point3D dragOrthogonalAxis = new Point3D(mouseDeltaY, -mouseDeltaX, 0);
                    double rotationAngle = modifierFactor * Math.sqrt(mouseDeltaX * mouseDeltaX + mouseDeltaY * mouseDeltaY);
                    worldRotateProperty.setValue(new Rotate(rotationAngle, dragOrthogonalAxis).createConcatenation(rotateAtMouseDown));
                }
            }
        });

        scene.setOnScroll(new EventHandler<ScrollEvent>() {
                              @Override
                              public void handle(final ScrollEvent me) {
                                  if (me.getDeltaY() != 0) {
                                      double factor = (me.getDeltaY() > 0 ? 1.1 : 0.9);
                                      worldScale.setX(factor * worldScale.getX());
                                      worldScale.setY(factor * worldScale.getY());
                                  }
                              }
                          }
        );

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent event) {

                switch (event.getCode()) {
                    case SPACE:
                        break;
                    case LEFT:
                        worldTranslate.setX(worldTranslate.getX() - 5);
                        break;
                    case RIGHT:
                        worldTranslate.setX(worldTranslate.getX() + 5);
                        break;
                    case DOWN:
                        worldTranslate.setY(worldTranslate.getY() + 5);
                        break;
                    case UP:
                        worldTranslate.setY(worldTranslate.getY() - 5);
                        break;
                    case EQUALS:
                    case PLUS:
                        if (event.isControlDown()) {
                        }
                        break;
                    case UNDERSCORE:
                    case MINUS:
                        if (event.isControlDown()) {
                        }
                        break;
                }
            }
        });


    }

}
