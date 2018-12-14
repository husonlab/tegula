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
package tiler.tiling;

import javafx.scene.Group;
import javafx.scene.shape.Shape;

/**
 * handle used for reshaping edges
 * Ruediger Zeller, 2017
 */
public class Handle extends Group {
    private Shape shape;
    private int type;
    private int flag;

    public void setTransX(double dx) {
        shape.setTranslateX(dx);
    }

    public void setTransY(double dy) {
        shape.setTranslateY(dy);
    }

    public double getTransX() {
        return shape.getTranslateX();
    }

    public double getTransY() {
        return shape.getTranslateY();
    }

    public void setShape(Shape s) {
        this.shape = s;
    }

    public Shape getShape() {
        return shape;
    }

    public void setType(int t) {
        this.type = t;
    }

    public int getType() {
        return this.type;
    }

    public void setFlag(int a) {
        this.flag = a;
    }

    public int getFlag() {
        return this.flag;
    }
}
