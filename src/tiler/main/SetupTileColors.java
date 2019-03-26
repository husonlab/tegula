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

package tiler.main;

import javafx.scene.control.ColorPicker;
import tiler.color.ColorSchemeManager;

/**
 * setup tile color controls
 * Daniel Huson, 12.2018
 */
public class SetupTileColors {
    /**
     * setup controllers for color
     *
     * @param controller
     * @param document
     */
    public static void apply(MainWindowController controller, Document document) {
        final int numberOfTiles = document.getCurrentTiling().getDSymbol().countOrbits(0, 1);

        // remove all but the first
        controller.tileColorPickers.clear();

        final int pos = controller.getToolsToolBar().getItems().indexOf(controller.getTile1ColorPicker());
        int end = pos;
        while (end < controller.getToolsToolBar().getItems().size() && controller.getToolsToolBar().getItems().get(end) instanceof ColorPicker) {
            controller.tileColorPickers.add((ColorPicker) controller.getToolsToolBar().getItems().get(end));
            end++;
        }

        if (end - pos > numberOfTiles) {
            controller.getToolsToolBar().getItems().remove(pos + numberOfTiles, end);
        } else while (end - pos < numberOfTiles) {
            final ColorPicker colorPicker = new ColorPicker();
            controller.getToolsToolBar().getItems().add(end++, colorPicker);
            controller.tileColorPickers.add(colorPicker);
        }

        for (int i = 0; i < numberOfTiles; i++) {
            final int tileNumber = i;
            final ColorPicker colorPicker = (ColorPicker) controller.getToolsToolBar().getItems().get(pos + i);
            colorPicker.setOnAction((e) -> {
                document.getTilingStyle().setTileColor(tileNumber, colorPicker.getValue());
                document.update();
            });
            colorPicker.setOnShowing((e) -> {
                colorPicker.getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(document.getTilingStyle().getTileColorsScheme()));
                colorPicker.setValue(document.getTilingStyle().getTileColor(tileNumber));
            });
        }
    }
}
