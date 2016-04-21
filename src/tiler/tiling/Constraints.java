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

package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * represents contraints on positions of nodes in fundamental domain
 * Created by huson on 4/21/16.
 */
public class Constraints {
    public enum ConstraintType {
        FIXED, SYMMETRIC_BOUNDARY, FREE, LINE;

        public int code() {
            switch (this) {
                case FIXED:
                    return -3;
                case SYMMETRIC_BOUNDARY:
                    return -1;
                case LINE:
                    return Integer.MAX_VALUE;
                default:
                case FREE:
                    return 0;
            }
        }

        public static ConstraintType decode(int i) {
            if (i > 0)
                return LINE;
            switch (i) {
                case -3:
                    return FIXED;
                case -1:
                    return SYMMETRIC_BOUNDARY;
                default:
                    return FREE;
            }
        }
    }

    private final int[][][] constraints;
    private final ArrayList<Pair<Point2D, Point2D>> lines;

    /**
     * constructor
     *
     * @param size Delaney symbol size
     */
    public Constraints(int size) {
        // setup the constraints map
        constraints = new int[3][3][]; // indexing starts at 1
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                constraints[i][j] = new int[size + 1];
            }
        }
        lines = new ArrayList<>();
    }

    /**
     * set constraint
     *
     * @param kind
     * @param i
     * @param a
     * @param type if type is line, then use last set line
     */
    public void setConstraint(int kind, int i, int a, ConstraintType type) {
        if (type == ConstraintType.LINE) {
            if (lines.size() == 0)
                throw new RuntimeException("No line defined");
            constraints[kind][i][a] = lines.size(); // lines are saved as 1, 2, 3...
        } else
            constraints[kind][i][a] = type.code();
    }

    /**
     * set a line constraint (indicating that vertex must lie on given line)
     *
     * @param kind
     * @param i
     * @param a
     */
    public void setLineConstraint(int kind, int i, int a, Pair<Point2D, Point2D> line) {
        lines.add(line);
        constraints[kind][i][a] = lines.size(); // lines are saved as 1, 2, 3...
    }

    /**
     * get the type of constraint
     *
     * @param kind
     * @param i
     * @param a
     * @return constraint type
     */
    public ConstraintType getType(int kind, int i, int a) {
        return ConstraintType.decode(constraints[kind][i][a]);
    }

    public Pair<Point2D, Point2D> getLine(int kind, int i, int a) {
        int lineNumber = constraints[kind][i][a];
        if (lineNumber <= 0)
            throw new RuntimeException("Not a line constraint");
        return lines.get(lineNumber - 1); // line number 1 is 0-th element of array list
    }
}
