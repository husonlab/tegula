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

package tiler.main_new;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * starts the main tiler program
 * todo: this will replace Main.java in the future
 * Daniel Huson, 11.2018
 */
public class PeriodicTiler extends Application {
    public static final ObservableSet<Color> customColors = FXCollections.observableSet();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("PeriodicTiler");

        new MainView(primaryStage);
        primaryStage.sizeToScene();
    }
}
