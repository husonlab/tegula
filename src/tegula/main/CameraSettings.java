/*
 * CameraSettings.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.main;

import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.util.Duration;

/**
 * set the camera based on geometry and model
 * Daniel Huson, 4.2019
 */
public class CameraSettings {
    public enum HyperbolicModel {Poincare, Klein, Hyperboloid}

    public static void setupEuclideanCamera(Camera camera) {
    }

    public static void setupSphericalCamera(Camera camera) {
        camera.setRotate(0);
        camera.setTranslateX(0);
        camera.setTranslateZ(-850);
        if (camera instanceof PerspectiveCamera)
            ((PerspectiveCamera) camera).setFieldOfView(15);
        camera.setFarClip(10000);
    }

    /**
     * setup camera for hyperbolic tilings
     *
     * @param camera  perspective camera
     * @param model   hyperbolic model
     * @param animate if true, animate transition to model
     */
    public static void setupHyperbolicCamera(Camera camera, HyperbolicModel model, boolean animate) {

        if (camera instanceof PerspectiveCamera)
            ((PerspectiveCamera) camera).setFieldOfView(90);

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
        parallelTransition.setOnFinished((e) -> camera.setFarClip(10000));
        parallelTransition.play();

    }
}
