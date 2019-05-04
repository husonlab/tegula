/*
 * TegulaDesign.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.collections.ListChangeListener;
import javafx.stage.Stage;
import jloda.fx.window.IMainWindow;
import jloda.fx.window.MainWindowManager;
import jloda.util.CanceledException;
import jloda.util.UsageException;
import tegula.main.Tegula;
import tegula.main.Version;
import tegula.main.Window;

/**
 * tegula design extended program
 * daniel huson, 5.2019
 */
public class TegulaDesign extends Tegula {

    public static void main (String[] args) throws CanceledException, UsageException {
        Version.NAME="TegulaDesign";

        parseArguments(args);
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.err.println("TegulaDesign");
        MainWindowManager.getInstance().getMainWindows().addListener((ListChangeListener<IMainWindow>)(c)->{
            while(c.next()) {
                for(IMainWindow mainWindow:c.getAddedSubList()) {
                    if(mainWindow instanceof Window) {
                        final Window window=(Window)mainWindow;
                        ExtendMenu.apply(window);
                    }
                }
            }
        });
        super.start(primaryStage);


    }
}
