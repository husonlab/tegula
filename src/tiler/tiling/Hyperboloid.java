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

import javafx.scene.shape.TriangleMesh;

/**
 * sets up a hyberboloid in Z direction
 * Created by huson on 4/25/16.
 */
public class Hyperboloid extends TriangleMesh {
    /**
     * constructor
     *
     * @param height the height
     * @param levels levels of subdivision
     */
    public Hyperboloid(int height, int levels) {
        if (levels <= 1)
            throw new RuntimeException("layers must be 2 or more");

        int parts = 50;

        getPoints().addAll(0, 0, 100);

        float zDelta = height / levels;

        for (int l = 1; l <= levels; l++) {
            double radius = 100 * Math.sqrt((zDelta*(l) / 100 + 1) * (zDelta*(l) / 100 + 1)  - 1);
            for (int i = 0; i < parts; i++) {
                getPoints().addAll((float) (radius * Math.cos((i * 2.0 * Math.PI) / parts)), (float) (radius * Math.sin((i * 2.0 * Math.PI) / parts)), zDelta * (l)+100);
            }
        }
        getTexCoords().setAll(0, 0, 1, 1, 2, 2);

        // bottom triangles:
        for (int i = 0; i < parts; i++) {
            getFaces().addAll(0, 0, i, 1, (i + 1 < parts ? i + 1 : 1), 2);
            getFaces().addAll(0, 0, (i + 1 < parts ? i + 1 : 1), 2, i, 1);

        }


        if (true)
            for (int l = 1; l < levels; l++) {
                int firstPrev = (l-1) * parts + 1;
                int firstNext = (l) * parts + 1;
                for (int i = 0; i < parts; i++) {
                    int a = firstPrev + i;
                    int b = firstNext + i;
                    int c = (b < (l+1) * parts ? b + 1 : firstNext);
                    int d = (a < l * parts ? a + 1 : firstPrev);

                    getFaces().addAll(a, 0, b, 1, c, 2);
                    getFaces().addAll(a, 0, c, 1, d, 2);

                    getFaces().addAll(a, 0, c, 2, c, 1);
                    getFaces().addAll(a, 0, d, 2, c, 1);

                }
            }
    }
}
