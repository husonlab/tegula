/*
 *  Copyright (C) 2019 University of Tuebingen
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
package tegula.main.editor;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import tegula.color.ColorSchemeManager;
import tegula.main.TilingStyle;

/**
 * setup tile color controls
 * Daniel Huson, 4.2019
 */
public class TileColorControls {
    /**
     * setup controllers for color
     *
     * @param tilingEditorTab
     */
    public static void setup(TilingEditorTab tilingEditorTab) {
        final TilingEditorTabController controller = tilingEditorTab.getController();
        final ExtendedTiling extendedTiling = tilingEditorTab.getTilingPane().getExtendedTiling();
        final TilingStyle tilingStyle = extendedTiling.getTilingStyle();


        final int numberOfTiles = extendedTiling.getTilingMeshes().getDSymbol().countOrbits(0, 1);

        final ObservableList<Node> list = controller.getAppearanceVBox().getChildren();

        final int pos = list.indexOf(controller.getTile1ColorPicker());
        int end = pos;
        while (end < list.size() && list.get(end) instanceof ColorPicker) {
            end++;
        }

        if (end - pos > numberOfTiles) {
            list.remove(pos + numberOfTiles, end);
        } else while (end - pos < numberOfTiles) {
            final ColorPicker colorPicker = new ColorPicker();
            list.add(end++, colorPicker);
        }

        for (int t = 0; t < numberOfTiles; t++) {
            final int tileNumber = t;
            final ColorPicker colorPicker = (ColorPicker) list.get(pos + t);
            colorPicker.setOnAction((e) -> {
                tilingStyle.setTileColor(tileNumber, colorPicker.getValue());
                extendedTiling.update();
            });
            colorPicker.setOnShowing((e) -> {
                colorPicker.getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(tilingStyle.getTileColorsScheme()));
                colorPicker.setValue(tilingStyle.getTileColor(tileNumber));
            });
            colorPicker.setValue(tilingStyle.getTileColor(tileNumber));
        }

        // todo: recolor without updating

        controller.getTilesOpacitySlider().valueProperty().addListener((c, o, n) -> {
            for (int t = 0; t < numberOfTiles; t++) {
                final Color color = tilingStyle.getTileColor(t);
                tilingStyle.setTileColor(t, new Color(color.getRed(), color.getGreen(), color.getBlue(), n.doubleValue()));
            }
        });

        controller.getTilesOpacitySlider().valueChangingProperty().addListener((c, o, n) -> {
            if (!n)
                extendedTiling.update();
        });
        controller.getTilesOpacitySlider().setValue(tilingStyle.getTileColor(0).getOpacity());

    }
}
