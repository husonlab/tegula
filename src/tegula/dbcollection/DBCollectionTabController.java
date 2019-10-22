/*
 * DBCollectionTabController.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.dbcollection;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DBCollectionTabController {

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
    private ComboBox<String> complexityCBox;

    @FXML
    private ComboBox<String> numberOfTilesCBox;

    @FXML
    private ComboBox<String> numberOfEdgesCBox;

    @FXML
    private ComboBox<String> numberOfVerticesCBox;

    @FXML
    private CheckBox planeCheckButton;

    @FXML
    private CheckBox sphereCheckButton;

    @FXML
    private CheckBox hyperbolicCheckButton;

    @FXML
    private ComboBox<String> symmetryGroupCBox;

    @FXML
    private CheckBox maximalSymmetricCheckButton;

    @FXML
    private CheckBox orientableCheckButton;

    @FXML
    private CheckBox hasReflectionsCheckButton;

    @FXML
    private CheckBox noReflectionsCheckButton;

    @FXML
    private TitledPane selectTitledPane;

    @FXML
    private ComboBox<String> searchCBox;

    @FXML
    private Label countLabel;

    @FXML
    void initialize() {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert topVBox != null : "fx:id=\"topVBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert sizeSlider != null : "fx:id=\"sizeSlider\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert pagination != null : "fx:id=\"pagination\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert complexityCBox != null : "fx:id=\"complexityCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert numberOfTilesCBox != null : "fx:id=\"numberOfTilesCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert numberOfEdgesCBox != null : "fx:id=\"numberOfEdgesCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert numberOfVerticesCBox != null : "fx:id=\"numberOfVerticesCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert planeCheckButton != null : "fx:id=\"planeCheckButton\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert sphereCheckButton != null : "fx:id=\"sphereCheckButton\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert hyperbolicCheckButton != null : "fx:id=\"hyperbolicCheckButton\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert symmetryGroupCBox != null : "fx:id=\"symmetryGroupCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert maximalSymmetricCheckButton != null : "fx:id=\"maximalSymmetricCheckButton\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert orientableCheckButton != null : "fx:id=\"orientableCheckButton\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert hasReflectionsCheckButton != null : "fx:id=\"hasReflectionsCheckButton\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert noReflectionsCheckButton != null : "fx:id=\"noReflectionsCheckButton\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert selectTitledPane != null : "fx:id=\"selectTitledPane\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert searchCBox != null : "fx:id=\"searchCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert countLabel != null : "fx:id=\"countLabel\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";

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

    public ComboBox<String> getComplexityCBox() {
        return complexityCBox;
    }

    public ComboBox<String> getNumberOfTilesCBox() {
        return numberOfTilesCBox;
    }

    public ComboBox<String> getNumberOfEdgesCBox() {
        return numberOfEdgesCBox;
    }

    public ComboBox<String> getNumberOfVerticesCBox() {
        return numberOfVerticesCBox;
    }

    public CheckBox getPlaneCheckButton() {
        return planeCheckButton;
    }

    public CheckBox getSphereCheckButton() {
        return sphereCheckButton;
    }

    public CheckBox getHyperbolicCheckButton() {
        return hyperbolicCheckButton;
    }

    public ComboBox<String> getSymmetryGroupCBox() {
        return symmetryGroupCBox;
    }

    public CheckBox getMaximalSymmetricCheckButton() {
        return maximalSymmetricCheckButton;
    }

    public CheckBox getOrientableCheckButton() {
        return orientableCheckButton;
    }

    public CheckBox getHasReflectionsCheckButton() {
        return hasReflectionsCheckButton;
    }

    public CheckBox getNoReflectionsCheckButton() {
        return noReflectionsCheckButton;
    }

    public TitledPane getSelectTitledPane() {
        return selectTitledPane;
    }

    public ComboBox<String> getSearchCBox() {
        return searchCBox;
    }

    public Label getCountLabel() {
        return countLabel;
    }
}
