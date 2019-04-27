/*
 * HyperbolicModelCameraSettings.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.util.Duration;
import tiler.util.HasHyperbolicModel;

/**
 * sets up the camera for hyperbolic viewing
 */
public class HyperbolicModelCameraSettings {
    /**
     * set the camera based on the model
     *
     * @param document
     * @param model
     */
    public static void setModel(Document document, HasHyperbolicModel.HyperbolicModel model, boolean animate) {
        document.setHyperbolicModel(setModel(document.getPerspectiveCamera(), model, animate));
    }

    /**
     * set the camera based on the model
     *
     * @param camera
     * @param model
     */
    public static HasHyperbolicModel.HyperbolicModel setModel(PerspectiveCamera camera, HasHyperbolicModel.HyperbolicModel model, boolean animate) {

        camera.setFieldOfView(90);

        final double cameraAngle;
        final double cameraTranslateX;
        final double cameraTranslateZ;


        switch (model) {
            default:
            case Poincare:
                cameraAngle = 0;
                cameraTranslateX = 0;
                cameraTranslateZ = -100;
                break;
            case Klein:
                cameraAngle = 0;
                cameraTranslateX = 0;
                cameraTranslateZ = 0;
                break;
            case Hyperboloid:
                cameraAngle = -45;
                cameraTranslateX = 1000;
                cameraTranslateZ = 50;
                break;
        }

        final Duration duration;
        if (animate)
            duration = Duration.millis(1000);
        else
            duration = Duration.ONE;

        final RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setNode(camera);
        rotateTransition.setAxis(new Point3D(0, 1, 0));
        rotateTransition.setToAngle(cameraAngle);
        rotateTransition.setDuration(duration);

        final TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setNode(camera);
        translateTransition.setToX(cameraTranslateX);
        translateTransition.setToZ(cameraTranslateZ);
        translateTransition.setDuration(duration);

        final ParallelTransition parallelTransition = new ParallelTransition(rotateTransition, translateTransition);
        parallelTransition.setOnFinished((e) -> {
            camera.setFarClip(10000);
        });
        parallelTransition.play();

        return model;
    }
}
