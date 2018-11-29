/*
 *  Copyright (C) 2018 Daniel H. Huson
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
package tiler.main_new;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * main view controller
 * Daniel Huson, 11.2018
 */
public class MainViewController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem newMenuItem;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem saveMenuItem;

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
    private ToggleButton showRotationsToggleButton;

    @FXML
    private TextField statusTextField;

    @FXML
    private Pane mainPane;

    @FXML
    private BorderPane topBorderPane;

    @FXML
    private ToolBar rotationsToolBar;

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
    private Button resetButton;

    @FXML
    private ChoiceBox<String> modelChoiceBox;

    @FXML
    private Button increaseHyperbolicTilesButton;

    @FXML
    private Button decreaseHyperbolicTilesButton;

    @FXML
    void initialize() {
    }

    public BorderPane getBorderPane() {
        return borderPane;
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

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
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

    public ToggleButton getShowRotationsToggleButton() {
        return showRotationsToggleButton;
    }

    public TextField getStatusTextField() {
        return statusTextField;
    }

    public Pane getMainPane() {
        return mainPane;
    }

    public BorderPane getTopBorderPane() {
        return topBorderPane;
    }

    public ToolBar getRotationsToolBar() {
        return rotationsToolBar;
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

    public Button getResetButton() {
        return resetButton;
    }

    public ChoiceBox<String> getModelChoiceBox() {
        return modelChoiceBox;
    }

    public Button getIncreaseHyperbolicTilesButton() {
        return increaseHyperbolicTilesButton;
    }

    public Button getDecreaseHyperbolicTilesButton() {
        return decreaseHyperbolicTilesButton;
    }
}
