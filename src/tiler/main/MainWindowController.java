/*
 * MainWindowController.java Copyright (C) 2019. Daniel H. Huson
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

package tiler.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem newMenuItem;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private Menu openRecentMenu;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem pageSetupMenuItem;

    @FXML
    private MenuItem printMenuItem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem quitMenuItem;

    @FXML
    private MenuItem copyMenuItem;

    @FXML
    private MenuItem pasteMenuItem;

    @FXML
    private MenuItem selectAllMenuItem;

    @FXML
    private MenuItem lineWidthMenuItem;

    @FXML
    private MenuItem straightenSelectedMenuItem;

    @FXML
    private MenuItem straightenAllMenuItem;

    @FXML
    private CheckMenuItem straightenAlwaysCheckMenuItem;

    @FXML
    private Menu colorsMenu;

    @FXML
    private MenuItem addColorSchemeMenuItem;

    @FXML
    private CheckMenuItem showFDomainMenuItem;

    @FXML
    private CheckMenuItem showChambersMenuItem;

    @FXML
    private MenuItem showMoreTilesMenuItem;

    @FXML
    private MenuItem showLessTilesMenuItem;

    @FXML
    private MenuItem resetMenuItem;

    @FXML
    private MenuItem firstTilingMenuItem;

    @FXML
    private MenuItem nextTilingMenuItem;

    @FXML
    private MenuItem previousTilingMenuItem;

    @FXML
    private MenuItem lastTilingMenuItem;

    @FXML
    private MenuItem dualizeMenuItem;

    @FXML
    private MenuItem maxSymmetryMenuItem;

    @FXML
    private MenuItem orientateMenuItem;

    @FXML
    private CheckMenuItem editSymmetryGroupMenuItem;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private Button openFileButton;

    @FXML
    private Button printButton;

    @FXML
    private Button firstTilingButton;

    @FXML
    private Button previousTilingButton;

    @FXML
    private TextField tilingNumberTextField;

    @FXML
    private Button nextTilingButton;

    @FXML
    private Button lastTilingButton;

    @FXML
    private ChoiceBox<?> modelChoiceBox;

    @FXML
    private Button showMoreTilesButton;

    @FXML
    private Button showLessTilesButton;

    @FXML
    private ToggleButton showRotationsToggleButton;

    @FXML
    private ToggleButton toolsToggleButton;

    @FXML
    private Button testButton;

    @FXML
    private FlowPane statusFlowPane;

    @FXML
    private Pane mainPane;

    @FXML
    private ToolBar rotationsToolBar;

    @FXML
    private TextField groupTextField;

    @FXML
    private Spinner<?> v1Spinner;

    @FXML
    private Spinner<?> v2Spinner;

    @FXML
    private Spinner<?> v3Spinner;

    @FXML
    private Spinner<?> v4Spinner;

    @FXML
    private Spinner<?> v5Spinner;

    @FXML
    private Spinner<?> v6Spinner;

    @FXML
    private Spinner<?> v7Spinner;

    @FXML
    private Spinner<?> v8Spinner;

    @FXML
    private Spinner<?> v9Spinner;

    @FXML
    private Spinner<?> v10Spinner;

    @FXML
    private ToolBar toolsToolBar;

    @FXML
    private Button dualizeButton;

    @FXML
    private Button orientateButton;

    @FXML
    private Button maximizeButton;

    @FXML
    private CheckBox showBandsCheckBox;

    @FXML
    private Spinner<Integer> bandWidthSpinner;

    @FXML
    private ColorPicker bandsColorPicker;

    @FXML
    private CheckBox showFacesCheckBox;

    @FXML
    private CheckBox smoothEdgesCheckBox;

    @FXML
    private CheckBox backEdgesCheckBox;

    @FXML
    private ColorPicker tile1ColorPicker;

    @FXML
    private ColorPicker backgroundColorPicker;


    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public MenuItem getNewMenuItem() {
        return newMenuItem;
    }

    public MenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public Menu getOpenRecentMenu() {
        return openRecentMenu;
    }

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public MenuItem getPageSetupMenuItem() {
        return pageSetupMenuItem;
    }

    public MenuItem getPrintMenuItem() {
        return printMenuItem;
    }

    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public MenuItem getQuitMenuItem() {
        return quitMenuItem;
    }

    public MenuItem getCopyMenuItem() {
        return copyMenuItem;
    }

    public MenuItem getPasteMenuItem() {
        return pasteMenuItem;
    }

    public MenuItem getSelectAllMenuItem() {
        return selectAllMenuItem;
    }

    public MenuItem getLineWidthMenuItem() {
        return lineWidthMenuItem;
    }

    public MenuItem getStraightenSelectedMenuItem() {
        return straightenSelectedMenuItem;
    }

    public MenuItem getStraightenAllMenuItem() {
        return straightenAllMenuItem;
    }

    public CheckMenuItem getStraightenAlwaysCheckMenuItem() {
        return straightenAlwaysCheckMenuItem;
    }

    public Menu getColorsMenu() {
        return colorsMenu;
    }

    public MenuItem getAddColorSchemeMenuItem() {
        return addColorSchemeMenuItem;
    }

    public CheckMenuItem getShowFDomainMenuItem() {
        return showFDomainMenuItem;
    }

    public CheckMenuItem getShowChambersMenuItem() {
        return showChambersMenuItem;
    }

    public MenuItem getShowMoreTilesMenuItem() {
        return showMoreTilesMenuItem;
    }

    public MenuItem getShowLessTilesMenuItem() {
        return showLessTilesMenuItem;
    }

    public MenuItem getResetMenuItem() {
        return resetMenuItem;
    }

    public MenuItem getFirstTilingMenuItem() {
        return firstTilingMenuItem;
    }

    public MenuItem getNextTilingMenuItem() {
        return nextTilingMenuItem;
    }

    public MenuItem getPreviousTilingMenuItem() {
        return previousTilingMenuItem;
    }

    public MenuItem getLastTilingMenuItem() {
        return lastTilingMenuItem;
    }

    public MenuItem getDualizeMenuItem() {
        return dualizeMenuItem;
    }

    public MenuItem getMaxSymmetryMenuItem() {
        return maxSymmetryMenuItem;
    }

    public MenuItem getOrientateMenuItem() {
        return orientateMenuItem;
    }

    public CheckMenuItem getEditSymmetryGroupMenuItem() {
        return editSymmetryGroupMenuItem;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public Button getOpenFileButton() {
        return openFileButton;
    }

    public Button getPrintButton() {
        return printButton;
    }

    public Button getFirstTilingButton() {
        return firstTilingButton;
    }

    public Button getPreviousTilingButton() {
        return previousTilingButton;
    }

    public TextField getTilingNumberTextField() {
        return tilingNumberTextField;
    }

    public Button getNextTilingButton() {
        return nextTilingButton;
    }

    public Button getLastTilingButton() {
        return lastTilingButton;
    }

    public ChoiceBox<?> getModelChoiceBox() {
        return modelChoiceBox;
    }

    public Button getShowMoreTilesButton() {
        return showMoreTilesButton;
    }

    public Button getShowLessTilesButton() {
        return showLessTilesButton;
    }

    public ToggleButton getShowRotationsToggleButton() {
        return showRotationsToggleButton;
    }

    public ToggleButton getToolsToggleButton() {
        return toolsToggleButton;
    }

    public FlowPane getStatusFlowPane() {
        return statusFlowPane;
    }

    public Pane getMainPane() {
        return mainPane;
    }

    public ToolBar getRotationsToolBar() {
        return rotationsToolBar;
    }

    public TextField getGroupTextField() {
        return groupTextField;
    }

    public Spinner<?> getV1Spinner() {
        return v1Spinner;
    }

    public Spinner<?> getV2Spinner() {
        return v2Spinner;
    }

    public Spinner<?> getV3Spinner() {
        return v3Spinner;
    }

    public Spinner<?> getV4Spinner() {
        return v4Spinner;
    }

    public Spinner<?> getV5Spinner() {
        return v5Spinner;
    }

    public Spinner<?> getV6Spinner() {
        return v6Spinner;
    }

    public Spinner<?> getV7Spinner() {
        return v7Spinner;
    }

    public Spinner<?> getV8Spinner() {
        return v8Spinner;
    }

    public Spinner<?> getV9Spinner() {
        return v9Spinner;
    }

    public Spinner<?> getV10Spinner() {
        return v10Spinner;
    }

    public ToolBar getToolsToolBar() {
        return toolsToolBar;
    }

    public Button getDualizeButton() {
        return dualizeButton;
    }

    public Button getOrientateButton() {
        return orientateButton;
    }

    public Button getMaximizeButton() {
        return maximizeButton;
    }

    public CheckBox getShowBandsCheckBox() {
        return showBandsCheckBox;
    }

    public Spinner<Integer> getBandWidthSpinner() {
        return bandWidthSpinner;
    }

    public ColorPicker getBandsColorPicker() {
        return bandsColorPicker;
    }

    public CheckBox getShowFacesCheckBox() {
        return showFacesCheckBox;
    }

    public CheckBox getSmoothEdgesCheckBox() {
        return smoothEdgesCheckBox;
    }

    public CheckBox getBackEdgesCheckBox() {
        return backEdgesCheckBox;
    }

    public ColorPicker getTile1ColorPicker() {
        return tile1ColorPicker;
    }

    public ColorPicker getBackgroundColorPicker() {
        return backgroundColorPicker;
    }

    public ObservableList<ColorPicker> getTileColorPickers() {
        return tileColorPickers;
    }

    /**
     * gets a V label
     *
     * @param i
     * @return
     */
    public Spinner<Integer> getVSpinner(int i) {
        switch (i) {
            case 0:
                return (Spinner<Integer>) v1Spinner;
            case 1:
                return (Spinner<Integer>) v2Spinner;
            case 2:
                return (Spinner<Integer>) v3Spinner;
            case 3:
                return (Spinner<Integer>) v4Spinner;
            case 4:
                return (Spinner<Integer>) v5Spinner;
            case 5:
                return (Spinner<Integer>) v6Spinner;
            case 6:
                return (Spinner<Integer>) v7Spinner;
            case 7:
                return (Spinner<Integer>) v8Spinner;
            case 8:
                return (Spinner<Integer>) v9Spinner;
            default:
            case 9:
                return (Spinner<Integer>) v10Spinner;
        }
    }

    @FXML
    void initialize() {
        for (int i = 0; i < 10; i++)
            getVSpinner(i).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24));
        ((Spinner<Integer>) bandWidthSpinner).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
    }

    public final ObservableList<ColorPicker> tileColorPickers = FXCollections.observableArrayList();

    final ToggleGroup colorsMenuToggleGroup = new ToggleGroup();

    public ToggleGroup getColorsMenuToggleGroup() {
        return colorsMenuToggleGroup;
    }

    public Button getTestButton() {
        return testButton;
    }

}
