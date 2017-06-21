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

import javafx.geometry.Point3D;
import javafx.scene.transform.Translate;
import tiler.core.dsymbols.FDomain;
import tiler.main.Document;
import tiler.tiling.FundamentalDomain;
import tiler.tiling.Handle;
import tiler.tiling.Tools;

import java.awt.geom.Point2D;

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
    public static void setHandler(Handle handle, FDomain fDomain) {
        new ShapeHandler(handle, fDomain);
    }

    /**
     * constructor
     *
     * @param handle
     */
    private ShapeHandler(Handle handle, FDomain fDomain) {

        handle.getShape().setOnMousePressed((e) -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
            e.consume();
        });

        handle.getShape().setOnMouseDragged((e) -> {
            double deltaX = e.getSceneX() - mouseX;
            double deltaY = e.getSceneY() - mouseY;

            mouseX = e.getSceneX();
            mouseY = e.getSceneY();

            // Move handles
            handle.setTransX(handle.getTransX() + deltaX);
            handle.setTransY(handle.getTransY() + deltaY);

            // Reset Point in fundamental domain
            Translate t = new Translate(deltaX, deltaY);
            int i = handle.getType(), a = handle.getFlag();
            Point3D pt = fDomain.getVertex3D(i, a);
            pt = t.transform(pt);
            javafx.geometry.Point2D pt2d = Tools.map3Dto2D(fDomain.getGeometry(), pt);
            fDomain.setVertex(pt2d, i, a); // Todo: Consider all chambers of orbit

            e.consume();
        });
    }
}
