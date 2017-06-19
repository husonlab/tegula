/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package tiler.util;

import javafx.scene.shape.Shape;

/**
 * handles mouse events on handles
 * Created by huson on 6/19/17.
 */
public class ShapeHandler {
    private double mouseX;
    private double mouseY;

    /**
     * set the handler
     *
     * @param shape
     */
    public static void setHandler(Shape shape) {
        new ShapeHandler(shape);
    }

    /**
     * constructor
     *
     * @param shape
     */
    private ShapeHandler(Shape shape) {

        shape.setOnMousePressed((e) -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
            e.consume();
        });

        shape.setOnMouseDragged((e) -> {
            double deltaX = e.getSceneX() - mouseX;
            double deltaY = e.getSceneY() - mouseY;

            mouseX = e.getSceneX();
            mouseY = e.getSceneY();

            shape.setTranslateX(shape.getTranslateX() + deltaX);
            shape.setTranslateY(shape.getTranslateY() + deltaY);
            e.consume();

        });

    }
}
