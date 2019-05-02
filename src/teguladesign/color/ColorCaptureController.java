/*
 * ColorCaptureController.java Copyright (C) 2019. Daniel H. Huson
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

package teguladesign.color;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ColorCaptureController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField nameTextField;

    @FXML
    private StackPane dropTarget;

    @FXML
    private Text dropLabel;

    @FXML
    private Slider separationSlider;

    @FXML
    private Button cancelButton;

    @FXML
    private Button applyButton;

    @FXML
    void initialize() {
        assert nameTextField != null : "fx:id=\"nameTextField\" was not injected: check your FXML file 'ColorCapture.fxml'.";
        assert dropTarget != null : "fx:id=\"dropTarget\" was not injected: check your FXML file 'ColorCapture.fxml'.";
        assert dropLabel != null : "fx:id=\"dropLabel\" was not injected: check your FXML file 'ColorCapture.fxml'.";
        assert separationSlider != null : "fx:id=\"separationSlider\" was not injected: check your FXML file 'ColorCapture.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'ColorCapture.fxml'.";
        assert applyButton != null : "fx:id=\"applyButton\" was not injected: check your FXML file 'ColorCapture.fxml'.";

    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public TextField getNameTextField() {
        return nameTextField;
    }

    public StackPane getDropTarget() {
        return dropTarget;
    }

    public Text getDropLabel() {
        return dropLabel;
    }

    public Slider getSeparationSlider() {
        return separationSlider;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }
}
