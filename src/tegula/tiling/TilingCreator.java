/*
 * TilingCreator.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tiling;

import javafx.scene.Group;
import tegula.core.dsymbols.DSymbol;
import tegula.main.TilingStyle;
import tegula.util.Updateable;

/**
 * creates a tiling
 * Daniel Husobn, 4.2019
 */
public interface TilingCreator {
    Group update();

    /**
     * create a tiling
     *
     * @param dSymbol
     * @param tilingStyle
     * @param doc
     * @return tiling
     */
    public static TilingBase create(DSymbol dSymbol, TilingStyle tilingStyle, Updateable doc) {
        switch (dSymbol.computeGeometry()) {
            case Spherical:
                return new SphericalTiling(dSymbol, tilingStyle);
            case Hyperbolic:
                return new HyperbolicTiling(dSymbol, tilingStyle);
            default:
            case Euclidean:
                return new EuclideanTiling(dSymbol, tilingStyle, doc);
        }
    }
}
