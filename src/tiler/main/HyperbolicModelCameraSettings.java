/*
 *  Copyright (C) 2018 Daniel H. Huson
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
import javafx.util.Duration;

/**
 * sets up the camera for hyperbolic viewing
 */
public class HyperbolicModelCameraSettings {
    public enum Model {Poincare, Klein, Hyperboloid}

    /**
     * set the camera based on the model
     *
     * @param document
     * @param model
     */
    public static void setModel(Document document, Model model) {
        document.getCamera().setFieldOfView(90);

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

        final RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setNode(document.getCamera());
        rotateTransition.setAxis(new Point3D(0, 1, 0));
        rotateTransition.setToAngle(cameraAngle);
        rotateTransition.setDuration(Duration.millis(1000));

        final TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setNode(document.getCamera());
        translateTransition.setToX(cameraTranslateX);
        translateTransition.setToZ(cameraTranslateZ);
        translateTransition.setDuration(Duration.millis(1000));

        final ParallelTransition parallelTransition = new ParallelTransition(rotateTransition, translateTransition);
        parallelTransition.setOnFinished((e) -> {
            document.getCamera().setFarClip(10000);
        });
        parallelTransition.play();

        document.setCamPoincare(false);

    }
}
