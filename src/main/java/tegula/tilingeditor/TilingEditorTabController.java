/*
 * TilingEditorTabController.java Copyright (C) 2023 Daniel H. Huson
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

package tegula.tilingeditor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class TilingEditorTabController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane borderPane;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private StackPane mainPane;

    @FXML
    private TextField infoTextField;

    @FXML
    private VBox symmetiesVBox;

    @FXML
    private TextField groupTextField;

    @FXML
    private TitledPane hyperbolicModelTitledPane;

    @FXML
    private ChoiceBox<String> modelChoiceBox;

    @FXML
    private Button showLessTilesButton;

    @FXML
    private Button showMoreTilesButton;

    @FXML
    private TitledPane lightingTitledPane;

    @FXML
    private RadioButton pointLightRadioButton;

    @FXML
    private RadioButton ambientLightRadioButton;


    @FXML
    private VBox appearanceVBox;

    @FXML
    private ChoiceBox<String> colorSchemeChoiceBox;

    @FXML
    private ToggleButton showFacesToggleButton;

    @FXML
    private ToggleButton showBackFacesToggleButton;

    @FXML
    private ColorPicker tile1ColorPicker;

    @FXML
    private Slider tilesOpacitySlider;

    @FXML
    private ToggleButton showEdgesToggleButton;

    @FXML
    private ToggleButton showBackEdgesToggleButton;

    @FXML
    private ToggleButton showVerticesToggleButton;

    @FXML
    private ToggleButton showBackVerticesToggleButton;

    @FXML
    private Spinner<Integer> bandWidthSpinner;

    @FXML
    private ColorPicker bandsColorPicker;

    @FXML
    private Slider bandsOpacitySlider;

    @FXML
    private CheckBox smoothEdgesCheckBox;

    @FXML
    private ColorPicker backgroundColorPicker;

    @FXML
    private TitledPane fundamentalDomainTitledPane;

    @FXML
    private AnchorPane fdomainAnchorPane;

    @FXML
    private ToolBar fdomainToolBar;

    @FXML
    private Button resizeButton;

    @FXML
    private Button straightenEdgesButton;

    @FXML
    private Button dualizeButton;

    @FXML
    private Button orientateButton;

    @FXML
    private Button removeRotationsButton;

    @FXML
    private Button maximizeButton;

    @FXML
    private Button stopAnimationButton;

    @FXML
    private Button contractEdgeButton;

    @FXML
    private Button truncateVertexButton;

    @FXML
    private Button glueTilesButton;

    @FXML
    private Button splitTileButton;

    @FXML
    private Button resetButton;

	@FXML
	private VBox editButtonsVBox;

    @FXML
    void initialize() {
		final ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(pointLightRadioButton, ambientLightRadioButton);
        pointLightRadioButton.setSelected(true);
    }


    public BorderPane getBorderPane() {
        return borderPane;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public StackPane getMainPane() {
        return mainPane;
    }

    public TextField getInfoTextField() {
        return infoTextField;
    }

    public VBox getSymmetiesVBox() {
        return symmetiesVBox;
    }

    public TextField getGroupTextField() {
        return groupTextField;
    }

    public ChoiceBox<String> getModelChoiceBox() {
        return modelChoiceBox;
    }

    public Button getShowLessTilesButton() {
        return showLessTilesButton;
    }

    public Button getShowMoreTilesButton() {
        return showMoreTilesButton;
    }

    public VBox getAppearanceVBox() {
        return appearanceVBox;
    }

    public ChoiceBox<String> getColorSchemeChoiceBox() {
        return colorSchemeChoiceBox;
    }

    public ToggleButton getShowFacesToggleButton() {
        return showFacesToggleButton;
    }

    public ToggleButton getBackFacesToggleButton() {
        return showBackFacesToggleButton;
    }

    public CheckBox getSmoothEdgesCheckBox() {
        return smoothEdgesCheckBox;
    }

    public ColorPicker getTile1ColorPicker() {
        return tile1ColorPicker;
    }

    public ToggleButton getShowEdgesToggleButton() {
        return showEdgesToggleButton;
    }

    public ToggleButton getShowBackEdgesToggleButton() {
        return showBackEdgesToggleButton;
    }

    public ToggleButton getShowNodesToggleButton() {
        return showVerticesToggleButton;
    }

    public ToggleButton getShowBackNodesToggleButton() {
        return showBackVerticesToggleButton;
    }

    public Spinner<Integer> getBandWidthSpinner() {
        return bandWidthSpinner;
    }

    public ColorPicker getBandsColorPicker() {
        return bandsColorPicker;
    }

    public ColorPicker getBackgroundColorPicker() {
        return backgroundColorPicker;
    }

    public Button getDualizeButton() {
        return dualizeButton;
    }

    public Button getStraightenEdgesButton() {
        return straightenEdgesButton;
    }

    public Button getOrientateButton() {
        return orientateButton;
    }

    public Button getRemoveRotationsButton() {
        return removeRotationsButton;
    }

    public Button getMaximizeButton() {
        return maximizeButton;
    }

    public TitledPane getFundamentalDomainTitledPane() {
        return fundamentalDomainTitledPane;
    }

    public TitledPane getHyperbolicModelTitledPane() {
        return hyperbolicModelTitledPane;
    }

    public TitledPane getLightingTitledPane() {
        return lightingTitledPane;
    }

    public RadioButton getPointLightRadioButton() {
        return pointLightRadioButton;
    }

    public RadioButton getAmbientLightRadioButton() {
        return ambientLightRadioButton;
    }

    public Button getResizeButton() {
        return resizeButton;
    }

    public AnchorPane getFdomainAnchorPane() {
        return fdomainAnchorPane;
    }

    public ToolBar getFdomainToolBar() {
        return fdomainToolBar;
    }

    public Slider getTilesOpacitySlider() {
        return tilesOpacitySlider;
    }

    public Slider getBandsOpacitySlider() {
        return bandsOpacitySlider;
    }

    public Button getStopAnimationButton() {
        return stopAnimationButton;
    }

    public Button getContractEdgeButton() {
        return contractEdgeButton;
    }

    public Button getTruncateVertexButton() {
        return truncateVertexButton;
    }

    public Button getGlueTilesButton() {
        return glueTilesButton;
    }

    public Button getSplitTileButton() {
        return splitTileButton;
    }

    public Button getResetButton() {
        return resetButton;
    }

	public VBox getEditButtonsVBox() {
		return editButtonsVBox;
	}
}
