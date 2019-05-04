/*
 * ExtendMenu.java Copyright (C) 2019. Daniel H. Huson
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

package teguladesign.main;

import javafx.scene.control.MenuItem;
import jloda.fx.util.ColorSchemeManager;
import tegula.main.Window;
import teguladesign.color.ColorCapture;

public class ExtendMenu  {
    private static ColorCapture colorCapture;

    public static void apply (Window window) {
        final MenuItem captureColorsMenuItem=new MenuItem("Capture Colors...");
        captureColorsMenuItem.setOnAction((e)->{
            if(colorCapture==null) {
                colorCapture=new ColorCapture(ColorSchemeManager.getInstance());
            }
            colorCapture.getStage().show();
            colorCapture.getStage().toFront();
        });

        window.getController().getColorsMenu().getItems().add(captureColorsMenuItem);
    }
}
