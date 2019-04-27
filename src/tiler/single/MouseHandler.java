/*
 * MouseHandler.java Copyright (C) 2019. Daniel H. Huson
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

package tiler.single;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import jloda.util.Basic;
import tiler.core.dsymbols.Geometry;
import tiler.tiling.EuclideanTiling;
import tiler.tiling.HyperbolicTiling;
import tiler.tiling.SphericalTiling;

/**
 * mouse handler
 * Created by huson on 3/29/16.
 */
public class MouseHandler {
    private double originalMouseDownX;
    private double originalMouseDownY;
    private double mouseDownX;
    private double mouseDownY;
    private long mouseDownTime;

    private final TranslationAnimation animation;

    private long lastScroll = 0;
    private Thread thread = null;
    private final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollEnded = new SimpleObjectProperty<>();


    /**
     * add a mouse handler to the scene
     *
     * @param scene
     * @param worldTranslate
     * @param worldRotateProperty
     */
    public static void addMouseHandler(final Scene scene, final Translate worldTranslate, final Scale worldScale, final ObjectProperty<Transform> worldRotateProperty, final SingleTilingPane singleTilingPane) {
        new MouseHandler(scene, worldTranslate, worldScale, worldRotateProperty, singleTilingPane);
    }

    /**
     * constructor
     *
     * @param worldTranslate
     * @param worldRotateProperty
     */
    private MouseHandler(final Scene scene, final Translate worldTranslate, final Scale worldScale, final ObjectProperty<Transform> worldRotateProperty, final SingleTilingPane singleTilingPane) {
        animation = new TranslationAnimation(singleTilingPane);

        scene.setOnMousePressed((me) -> {
            originalMouseDownX = mouseDownX = me.getSceneX();
            originalMouseDownY = mouseDownY = me.getSceneY();
            mouseDownTime = System.currentTimeMillis();
        });
        scene.setOnMouseDragged((me) -> {
            double mouseDeltaX = me.getSceneX() - mouseDownX;
            double mouseDeltaY = me.getSceneY() - mouseDownY;

            if (me.isPrimaryButtonDown()) {
                if (singleTilingPane.getTiling() instanceof SphericalTiling) {
                    final Point2D delta = new Point2D(me.getSceneX() - mouseDownX, me.getSceneY() - mouseDownY);
                    //noinspection SuspiciousNameCombination
                    final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                    final Rotate rotate = new Rotate(0.25 * delta.magnitude(), dragOrthogonalAxis);
                    worldRotateProperty.setValue(rotate.createConcatenation(worldRotateProperty.get()));
                    mouseDownX = me.getSceneX();
                    mouseDownY = me.getSceneY();
                } else if (singleTilingPane.getTiling() instanceof HyperbolicTiling) {
                    final HyperbolicTiling tiling = (HyperbolicTiling) singleTilingPane.getTiling();

                    double modifierFactor = 1;
                    double dx = mouseDeltaX * modifierFactor;
                    double dy = mouseDeltaY * modifierFactor;

                    if (dx != 0 || dy != 0) {
                        singleTilingPane.translateTiling(dx, dy);

                        // Checks whether (dx,dy) has been modified.
                        if (tiling.directionChanged()) {
                            // Modify mouse position in hyperbolic case.
                            mouseDownX = me.getSceneX() - tiling.getTransVector().getX();
                            mouseDownY = me.getSceneY() - tiling.getTransVector().getY();
                        } else {
                            mouseDownX = me.getSceneX();
                            mouseDownY = me.getSceneY();
                        }
                    }
                } else if (singleTilingPane.getTiling() instanceof EuclideanTiling) {
                    double modifierFactor = 1;
                    double dx = mouseDeltaX * modifierFactor;
                    double dy = mouseDeltaY * modifierFactor;

                    if (dx != 0 || dy != 0) {
                        singleTilingPane.translateTiling(dx, dy);

                        mouseDownX = me.getSceneX();
                        mouseDownY = me.getSceneY();
                    }
                }
            }
        });
        scene.setOnMouseReleased((me) -> {
            if (me.isShiftDown()) {
                if (singleTilingPane.geometryProperty().getValue() != Geometry.Spherical) { // slide
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
                        singleTilingPane.setEuclideanWidth((singleTilingPane.getEuclideanWidth()) / factor);
                        singleTilingPane.setEuclideanHeight((singleTilingPane.getEuclideanHeight()) / factor);
                    }
                    if (onScrollEnded.get() != null) {
                        lastScroll = System.currentTimeMillis();
                        if (thread == null) {
                            thread = new Thread(() -> {
                                while (System.currentTimeMillis() - lastScroll < 100) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        Basic.caught(e);
                                    }
                                }
                                Platform.runLater(() ->
                                {
                                    if (onScrollEnded.get() != null)
                                        onScrollEnded.get().handle(me);
                                    thread = null;
                                });
                            });
                            thread.setDaemon(true);
                            thread.start();
                        }
                    }
                }
        );

        onScrollEnded.set(me -> {
            if (singleTilingPane.getGeometry() == Geometry.Euclidean)
                singleTilingPane.update();
        });

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case P:
                    if (animation.isPlaying())
                        animation.pause();
                    else
                        animation.play();
                    break;
                case LEFT:
                    if (singleTilingPane.getTilingStyle().getBandWidth() - 1 > 1)
                        singleTilingPane.getTilingStyle().setBandWidth(singleTilingPane.getTilingStyle().getBandWidth() - 1);
                    break;
                case RIGHT:
                    singleTilingPane.getTilingStyle().setBandWidth(singleTilingPane.getTilingStyle().getBandWidth() + 1);
                    break;
                case DOWN: {
                    singleTilingPane.decreaseTiling();
                    break;
                }
                case UP: {
                    singleTilingPane.increaseTiling();
                    break;
                }
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
