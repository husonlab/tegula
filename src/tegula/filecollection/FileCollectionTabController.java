/*
 * FileCollectionTabController.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.filecollection;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class FileCollectionTabController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane borderPane;

    @FXML
    private VBox topVBox;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private Slider sizeSlider;

    @FXML
    private Pagination pagination;


    @FXML
    private Label countLabel;

    @FXML
    void initialize() {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'FileCollectionTab.fxml'.";
        assert topVBox != null : "fx:id=\"topVBox\" was not injected: check your FXML file 'FileCollectionTab.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'FileCollectionTab.fxml'.";
        assert sizeSlider != null : "fx:id=\"sizeSlider\" was not injected: check your FXML file 'FileCollectionTab.fxml'.";
        assert pagination != null : "fx:id=\"pagination\" was not injected: check your FXML file 'FileCollectionTab.fxml'.";
        assert countLabel != null : "fx:id=\"countLabel\" was not injected: check your FXML file 'FileCollectionTab.fxml'.";
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public VBox getTopVBox() {
        return topVBox;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public Slider getSizeSlider() {
        return sizeSlider;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public Label getCountLabel() {
        return countLabel;
    }
}
