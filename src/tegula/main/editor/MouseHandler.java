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
import javafx.util.Duration;
import jloda.util.Basic;
import tegula.core.dsymbols.Geometry;

/**
 * set up user interaction
 * Daniel Huson, 4.2019
 */
public class MouseHandler {
    private double mouseDownX;
    private double mouseDownY;

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
        final ExtendedTilingPane tilingPane = tilingEditorTab.getTilingPane();

        sphere.rotateProperty().addListener((c, o, n) -> {
            final Rotate rotate = new Rotate(n.doubleValue() - o.doubleValue(), sphere.getRotationAxis());
            tilingPane.worldRotateProperty().setValue(rotate.createConcatenation(tilingPane.getWorldRotate()));
        });
        rotateTransition.setNode(sphere);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);

        tilingPane.setOnMousePressed((e) -> {
            mouseDownX = e.getSceneX();
            mouseDownY = e.getSceneY();
        });

        tilingPane.setOnMouseDragged((e) -> {
            final Point2D delta = new Point2D(e.getSceneX() - mouseDownX, e.getSceneY() - mouseDownY);
            if (delta.magnitude() > 0) {
                switch (tilingPane.getExtendedTiling().getTilingMeshes().getGeometry()) {

                    case Hyperbolic: // translate
                    case Euclidean: // translate
                    {
                        double dx = delta.getX();
                        double dy = delta.getY();
                        if (dx != 0 || dy != 0) {
                            tilingPane.getExtendedTiling().translateTiling(dx, dy);
                        }
                        break;
                    }
                    case Spherical: // rotate
                    {
                        //noinspection SuspiciousNameCombination
                        final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                        final double byAngle = 0.25 * delta.magnitude();
                        final Rotate rotate = new Rotate(byAngle, dragOrthogonalAxis);
                        tilingPane.worldRotateProperty().setValue(rotate.createConcatenation(tilingPane.getWorldRotate()));

                        if (e.isShiftDown()) {
                            rotateTransition.stop();
                            rotateTransition.setAxis(dragOrthogonalAxis);
                            rotateTransition.setByAngle(1000 * byAngle);
                            rotateTransition.setDuration(Duration.seconds(10));
                            rotateTransition.play();
                        }
                    }
                    break;
                }
            }

            mouseDownX = e.getSceneX();
            mouseDownY = e.getSceneY();
        });

        tilingPane.setOnMouseClicked((me) -> {
            if (me.getClickCount() == 2)
                rotateTransition.stop();
        });


        tilingPane.setOnScroll((me) -> {
                    if (me.getDeltaY() != 0) {
                        double factor = (me.getDeltaY() > 0 ? 1.1 : 0.9);
                        tilingPane.getWorldScale().setX(factor * tilingPane.getWorldScale().getX());
                        tilingPane.getWorldScale().setY(factor * tilingPane.getWorldScale().getY());
                        tilingPane.getExtendedTiling().setWidth((tilingPane.getExtendedTiling().getWidth()) / factor);
                        tilingPane.getExtendedTiling().setHeight((tilingPane.getExtendedTiling().getHeight()) / factor);
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
            if (tilingPane.getExtendedTiling().getGeometry() == Geometry.Euclidean)
                tilingPane.getExtendedTiling().update();
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
