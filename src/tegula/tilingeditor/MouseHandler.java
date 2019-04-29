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

package tegula.tilingeditor;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import jloda.util.Basic;
import tegula.core.dsymbols.Geometry;
import tegula.single.SingleTilingPane;
import tegula.single.TranslateAnimation;
import tegula.tiling.EuclideanTiling;
import tegula.tiling.HyperbolicTiling;
import tegula.tiling.SphericalTiling;

/**
 * set up user interaction
 * Daniel Huson, 4.2019
 */
public class MouseHandler {
    private double originalMouseDownX;
    private double originalMouseDownY;
    private double mouseDownX;
    private double mouseDownY;
    private long mouseDownTime;

    private final TranslateAnimation animation;

    private final Sphere sphere = new Sphere(4, 4);
    private final RotateTransition rotateTransition = new RotateTransition();


    private long lastScroll = 0;
    private Thread thread = null;
    private final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollEnded = new SimpleObjectProperty<>();

    /**
     * constructor
     *
     * @param tilingEditorTab
     */
    private MouseHandler(TilingEditorTab tilingEditorTab) {
        final SingleTilingPane tilingPane = tilingEditorTab.getTilingPane();

        animation = new TranslateAnimation(tilingPane);

        sphere.rotateProperty().addListener((c, o, n) -> {
            final Rotate rotate = new Rotate(n.doubleValue() - o.doubleValue(), sphere.getRotationAxis());
            tilingPane.worldRotateProperty().setValue(rotate.createConcatenation(tilingPane.getWorldRotate()));
        });
        rotateTransition.setNode(sphere);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);

        tilingPane.setOnMousePressed((me) -> {
            originalMouseDownX = mouseDownX = me.getSceneX();
            originalMouseDownY = mouseDownY = me.getSceneY();
            mouseDownTime = System.currentTimeMillis();
        });

        tilingPane.setOnMouseDragged((me) -> {
            double mouseDeltaX = me.getSceneX() - mouseDownX;
            double mouseDeltaY = me.getSceneY() - mouseDownY;

            if (me.isPrimaryButtonDown()) {

                if (tilingPane.getTiling() instanceof SphericalTiling) {
                    final Point2D delta = new Point2D(me.getSceneX() - mouseDownX, me.getSceneY() - mouseDownY);
                    //noinspection SuspiciousNameCombination
                    final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                    final Rotate rotate = new Rotate(0.25 * delta.magnitude(), dragOrthogonalAxis);
                    tilingPane.setWorldRotate(rotate.createConcatenation(tilingPane.getWorldRotate()));
                    mouseDownX = me.getSceneX();
                    mouseDownY = me.getSceneY();
                } else if (tilingPane.getTiling() instanceof HyperbolicTiling) {
                    final HyperbolicTiling tiling = (HyperbolicTiling) tilingPane.getTiling();

                    double modifierFactor = 1;
                    double dx = mouseDeltaX * modifierFactor;
                    double dy = mouseDeltaY * modifierFactor;

                    if (dx != 0 || dy != 0) {
                        tilingPane.translateTiling(dx, dy);

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
                } else if (tilingPane.getTiling() instanceof EuclideanTiling) {
                    double modifierFactor = 1;
                    double dx = mouseDeltaX * modifierFactor;
                    double dy = mouseDeltaY * modifierFactor;

                    if (dx != 0 || dy != 0) {
                        tilingPane.translateTiling(dx, dy);

                        mouseDownX = me.getSceneX();
                        mouseDownY = me.getSceneY();
                    }
                }
            }
        });

        tilingPane.setOnMouseReleased((me) -> {
            if (me.isShiftDown()) {
                if (tilingPane.geometryProperty().getValue() != Geometry.Spherical) { // slide
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

        tilingPane.setOnMouseClicked((me) -> {
            if (me.getClickCount() == 2) {
                rotateTransition.stop();
                animation.pause();
            }
        });

        tilingPane.setOnScroll((me) -> {
            final Scale worldScale = tilingPane.getWorldScale();

            if (me.getDeltaY() != 0) {
                        double factor = (me.getDeltaY() > 0 ? 1.1 : 0.9);
                worldScale.setX(factor * worldScale.getX());
                worldScale.setY(factor * worldScale.getY());
                tilingPane.setEuclideanWidth((tilingPane.getEuclideanWidth()) / factor);
                tilingPane.setEuclideanHeight((tilingPane.getEuclideanHeight()) / factor);
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

        onScrollEnded.set((me) -> {
            if (tilingPane.getGeometry() == Geometry.Euclidean || tilingPane.getGeometry() == Geometry.Hyperbolic)
                tilingPane.update();
        });
    }

    /**
     * create mouse handler for interaction for tiling editor
     *
     * @param tilingEditorTab
     */
    public static void setup(TilingEditorTab tilingEditorTab) {
        new MouseHandler(tilingEditorTab);
    }
}
