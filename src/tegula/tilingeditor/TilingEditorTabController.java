/*
 * TilingEditorTabController.java Copyright (C) 2019. Daniel H. Huson
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
    private ChoiceBox<?> modelChoiceBox;

    @FXML
    private Button showLessTilesButton;

    @FXML
    private Button showMoreTilesButton;

    @FXML
    private VBox appearanceVBox;

    @FXML
    private ChoiceBox<?> colorSchemeChoiceBox;

    @FXML
    private CheckBox showFacesCheckBox;

    @FXML
    private CheckBox backFacesCheckBox;

    @FXML
    private CheckBox smoothEdgesCheckBox;

    @FXML
    private ColorPicker tile1ColorPicker;

    @FXML
    private CheckBox showBandsCheckBox;

    @FXML
    private CheckBox backBandsCheckBox;

    @FXML
    private Spinner<?> bandWidthSpinner;

    @FXML
    private ColorPicker bandsColorPicker;

    @FXML
    private ColorPicker backgroundColorPicker;

    @FXML
    private Button straightenEdgesButton;

    @FXML
    private Button dualizeButton;

    @FXML
    private Button orientateButton;

    @FXML
    private Button maximizeButton;

    @FXML
    private Accordion fundamentalDomainAccordion;

    @FXML
    private TitledPane fundamentalDomainTitledPane;

    @FXML
    private Button resizeButton;

    @FXML
    private AnchorPane fdomainAnchorPane;

    @FXML
    private Slider tilesOpacitySlider;

    @FXML
    private Slider bandsOpacitySlider;

    @FXML
    private Button stopAnimationButton;

    @FXML
    private Accordion hyperbolicModelAccordion;

    @FXML
    private Button undoReshapeButton;

    @FXML
    private Button redoReshapeButton;


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

    public ChoiceBox<?> getModelChoiceBox() {
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
        return (ChoiceBox<String>) colorSchemeChoiceBox;
    }

    public CheckBox getShowFacesCheckBox() {
        return showFacesCheckBox;
    }

    public CheckBox getBackFacesCheckBox() {
        return backFacesCheckBox;
    }


    public CheckBox getSmoothEdgesCheckBox() {
        return smoothEdgesCheckBox;
    }

    public ColorPicker getTile1ColorPicker() {
        return tile1ColorPicker;
    }

    public CheckBox getShowBandsCheckBox() {
        return showBandsCheckBox;
    }

    public CheckBox getBackBandsCheckBox() {
        return backBandsCheckBox;
    }

    public Spinner<Integer> getBandWidthSpinner() {
        return (Spinner<Integer>) bandWidthSpinner;
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

    public Button getMaximizeButton() {
        return maximizeButton;
    }

    public Accordion getFundamentalDomainAccordion() {
        return fundamentalDomainAccordion;
    }

    public TitledPane getFundamentalDomainTitledPane() {
        return fundamentalDomainTitledPane;
    }

    public Button getResizeButton() {
        return resizeButton;
    }

    public AnchorPane getFdomainAnchorPane() {
        return fdomainAnchorPane;
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

    public Accordion getHyperbolicModelAccordion() {
        return hyperbolicModelAccordion;
    }

    public Button getUndoReshapeButton() {
        return undoReshapeButton;
    }

    public Button getRedoReshapeButton() {
        return redoReshapeButton;
    }
}
