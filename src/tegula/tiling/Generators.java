/*
 * Generators.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tiling;

import javafx.scene.transform.Transform;

import java.util.ArrayList;

/**
 * keeps track of the generators
 * Created by huson on 4/21/16.
 */
public class Generators {
    private int[][] map;
    private ArrayList<Transform> transforms;

    public Generators() {
        setSize(0);
    }

    /**
     * constructor
     *
	 */
    public Generators(int size) {
        setSize(size);
    }

    public void setSize(int size) {
        map = new int[3][size + 1];
        transforms = new ArrayList<>();
    }

    /**
     * set a transform
     *
     * @param a         1-based
	 */
    public void set(int k, int a, Transform transform) {
        transforms.add(transform);
        map[k][a] = transforms.size(); // save transform index as 1, 2
    }

    /**
     * set the same transform again
     *
     * @param a 1-based
     */
    public void setAgain(int k, int a) {
        if (transforms.size() > 0)
            map[k][a] = transforms.size(); // save transform index as 1, 2
    }

    /**
     * access a transform
     *
     * @param a 1-based
     * @return transform
     */
    public Transform get(int k, int a) {
        return transforms.get(map[k][a] - 1); // transform 1 is 0-th element of array list
    }

    /**
     * get all transforms
     *
     * @return transforms
     */
    public ArrayList<Transform> getTransforms() {
        return transforms;
    }
}
