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

package tiler.util;

import tiler.main.Document;
import tiler.tiling.Handle;
import tiler.tiling.Tiling;

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
     * @param handle
     */
    public static void setHandler(Document doc, Handle handle) {
        new ShapeHandler(doc, handle);
    }

    /**
     * constructor
     *
     * @param handle
     */
    private ShapeHandler(Document doc, Handle handle) {

        handle.getShape().setOnMousePressed((e) -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
            e.consume();
        });

        handle.getShape().setOnMouseDragged((e) -> {
            double deltaX = e.getSceneX() - mouseX;
            double deltaY = e.getSceneY() - mouseY;

            // Reset shape of fundamental domain
            Tiling tiling = doc.getCurrentTiling();
            javafx.geometry.Point2D transVector = tiling.resetShape(deltaX, deltaY, handle);

            // Move handles along transVector
            handle.setTransX(handle.getTransX() + transVector.getX());
            handle.setTransY(handle.getTransY() + transVector.getY());

            mouseX = e.getSceneX() - deltaX + transVector.getX();
            mouseY = e.getSceneY() - deltaY + transVector.getY();

            e.consume();
        });

        handle.getShape().setOnMouseReleased((e) -> {
            doc.update();
        });
    }
}
