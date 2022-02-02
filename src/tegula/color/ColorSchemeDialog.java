/*
 * ColorSchemeDialog.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.color;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jloda.fx.util.ColorSchemeManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * dialog for maintaining color schemes
 * todo: complete implementation
 * Daniel Huson, 12.2018
 */
public class ColorSchemeDialog {
    /**
     * setup and show the color scheme dialog
     *
	 */
    public ColorSchemeDialog(Object mainViewController) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        Pane root = fxmlLoader.load(getClass().getResource("ColorSchemeDialog.fxml").openStream());
        final ColorSchemeDialogController controller = fxmlLoader.getController();
        final Stage stage = new Stage();

        final ArrayList<ColorPicker> colorPickers = new ArrayList<>();

        controller.getFirstColorPicker().setValue(Color.TRANSPARENT);
        colorPickers.add(controller.getFirstColorPicker());
        controller.getSecondColorPicker().setValue(Color.TRANSPARENT);
        colorPickers.add(controller.getSecondColorPicker());

        controller.getAddColorButton().setOnAction((e) -> {
            final ColorPicker colorPicker = new ColorPicker();
            colorPicker.setValue(Color.TRANSPARENT);
            colorPicker.setMinWidth(controller.getFirstColorPicker().getMinWidth());
            colorPicker.setMaxWidth(controller.getFirstColorPicker().getMaxWidth());
            colorPicker.setPrefWidth(controller.getFirstColorPicker().getPrefWidth());
            colorPicker.setMinHeight(controller.getFirstColorPicker().getMinHeight());
            colorPicker.setMaxHeight(controller.getFirstColorPicker().getMaxHeight());
            colorPicker.setPrefHeight(controller.getFirstColorPicker().getPrefHeight());
            FlowPane.setMargin(colorPicker, new Insets(2, 2, 2, 2));
            controller.getColorsFlowPane().getChildren().add(controller.getColorsFlowPane().getChildren().size() - 1, colorPicker);
            colorPickers.add(colorPicker);
        });

        controller.getSaveButton().setOnAction((e) -> {
            final ObservableList<Color> colorScheme = FXCollections.observableArrayList();

            for (ColorPicker colorPicker : colorPickers) {
                if (colorPicker.getValue() != Color.TRANSPARENT)
                    colorScheme.add(colorPicker.getValue());
            }
            final String name = controller.getNameTextArea().getText();
            ColorSchemeManager.getInstance().setColorScheme(name, colorScheme);
            /*
            SetupController.addToColorsMenu(mainViewController, document, name);
            document.getTilingStyle().setTileColorsScheme(name);
            document.update();
            */
            stage.hide();
        });
        controller.getSaveButton().disableProperty().bind(controller.getNameTextArea().textProperty().isEmpty());

        controller.getCancelButton().setOnAction((e) -> stage.hide());

        final Scene scene = new Scene(root);
        stage.setTitle("Color Scheme - Tegula");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.showAndWait(); // modal dialog?

    }
}
