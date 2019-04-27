/*
 * ExtendedTilingPane.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tilingeditor;

import tegula.core.dsymbols.DSymbol;
import tegula.main.TilingStyle;
import tegula.single.SingleTilingPane;

/**
 * setup an extended tiling pane
 * Daniel Huson, 4.2019
 */
public class ExtendedTilingPane extends SingleTilingPane {

    /**
     * constructor
     *
     * @param dSymbol
     * @param tilingStyle
     */
    public ExtendedTilingPane(DSymbol dSymbol, TilingStyle tilingStyle) {
        super(dSymbol, tilingStyle, DEFAULT_BACKGROUND_COLOR, false);
    }
}
