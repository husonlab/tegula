/*
 * TilingCollectionTabController.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tilingcollection;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class TilingCollectionTabController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox topVBox;


    @FXML
    private BorderPane borderPane;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private ToggleButton labelsToggleButton;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private Slider sizeSlider;

    @FXML
    void initialize() {
        assert topVBox != null : "fx:id=\"topVBox\" was not injected: check your FXML file 'TilingCollectionTab.fxml'.";
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'TilingCollectionTab.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'TilingCollectionTab.fxml'.";
        assert labelsToggleButton != null : "fx:id=\"labelsToggleButton\" was not injected: check your FXML file 'TilingCollectionTab.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'TilingCollectionTab.fxml'.";
    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public VBox getTopVBox() {
        return topVBox;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public ToggleButton getLabelsToggleButton() {
        return labelsToggleButton;
    }

    public AnchorPane getMainAnchorPane() {
        return mainAnchorPane;
    }

    public Slider getSizeSlider() {
        return sizeSlider;
    }
}
