/*
 * DBCollectionTabController.java Copyright (C) 2020. Daniel H. Huson
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
    private CheckBox euclideanCheckBox;

    @FXML
    private CheckBox sphericalCheckBox;

    @FXML
    private CheckBox hyperbolicCheckBox;

    @FXML
    private ComboBox<String> orbifoldCBox;

    @FXML
    private ComboBox<String> symmetryClassCBox;

    @FXML
    private ComboBox<String> signatureCBox;

    @FXML
    private CheckBox maximalSymmetricCheckBox;

    @FXML
    private CheckBox orientableCheckBox;

    @FXML
    private CheckBox colorableCheckBox;

    @FXML
    private CheckBox normalCheckBox;

    @FXML
    private CheckBox selfDualCheckBox;

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
        assert euclideanCheckBox != null : "fx:id=\"euclideanCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert sphericalCheckBox != null : "fx:id=\"sphericalCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert hyperbolicCheckBox != null : "fx:id=\"hyperbolicCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert orbifoldCBox != null : "fx:id=\"orbifoldCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert symmetryClassCBox != null : "fx:id=\"symmetryClassCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert signatureCBox != null : "fx:id=\"signatureCBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert maximalSymmetricCheckBox != null : "fx:id=\"maximalSymmetricCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert orientableCheckBox != null : "fx:id=\"orientableCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert colorableCheckBox != null : "fx:id=\"colorableCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert normalCheckBox != null : "fx:id=\"normalCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
        assert selfDualCheckBox != null : "fx:id=\"selfDualCheckBox\" was not injected: check your FXML file 'DBCollectionTab.fxml'.";
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

    public CheckBox getEuclideanCheckButton() {
        return euclideanCheckBox;
    }

    public CheckBox getSphericalCheckButton() {
        return sphericalCheckBox;
    }

    public CheckBox getHyperbolicCheckButton() {
        return hyperbolicCheckBox;
    }

    public ComboBox<String> getOrbifoldCBox() {
        return orbifoldCBox;
    }

    public ComboBox<String> getSymmetryClassCBox() {
        return symmetryClassCBox;
    }

    public ComboBox<String> getSignatureCBox() {
        return signatureCBox;
    }

    public CheckBox getMaximalSymmetricCheckBox() {
        return maximalSymmetricCheckBox;
    }

    public CheckBox getOrientableCheckBox() {
        return orientableCheckBox;
    }

    public CheckBox getColorableCheckBox() {
        return colorableCheckBox;
    }

    public CheckBox getNormalCheckBox() {
        return normalCheckBox;
    }

    public CheckBox getSelfDualCheckBox() {
        return selfDualCheckBox;
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
