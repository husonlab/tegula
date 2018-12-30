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

package tiler.core.reshape;

import javafx.geometry.Point2D;

/**
 * reshape edits that need to be applied when changing the symmetry group
 * Daniel Husonb, 12.2018
 */
public class ReshapeEdit {
    private final int kind;
    private final int m;
    private final int a;
    private final int constraint;
    private final Point2D offset;

    public ReshapeEdit(int kind, int m, int a, int constraint, Point2D offset) {
        this.kind = kind;
        this.m = m;
        this.a = a;
        this.constraint = constraint;
        this.offset = offset;
        System.err.println("offset: " + offset);
    }

    public int getKind() {
        return kind;
    }

    public int getM() {
        return m;
    }

    public int getA() {
        return a;
    }

    public int getConstraint() {
        return constraint;
    }

    public Point2D getOffset() {
        return offset;
    }
}
