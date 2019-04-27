/*
 * ColorSchemeDialogController.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.color;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

/**
 * controller for color scheme dialog
 * Daniel Huson, 12.2018
 */
public class ColorSchemeDialogController {

    @FXML
    private TextField nameTextArea;

    @FXML
    private FlowPane colorsFlowPane;

    @FXML
    private ColorPicker firstColorPicker;

    @FXML
    private ColorPicker secondColorPicker;

    @FXML
    private Button addColorButton;

    @FXML
    private Label openFromImageButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    public TextField getNameTextArea() {
        return nameTextArea;
    }

    public FlowPane getColorsFlowPane() {
        return colorsFlowPane;
    }

    public ColorPicker getFirstColorPicker() {
        return firstColorPicker;
    }

    public ColorPicker getSecondColorPicker() {
        return secondColorPicker;
    }

    public Button getAddColorButton() {
        return addColorButton;
    }

    public Label getOpenFromImageButton() {
        return openFromImageButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }
}
