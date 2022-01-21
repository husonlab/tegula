/*
 * MainWindowController.java Copyright (C) 2022 Daniel H. Huson
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
package tegula.main;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import jloda.fx.window.SplashScreen;
import jloda.util.ProgramProperties;

import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

public class MainWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem newMenuItem;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem saveSelectedMenuItem;

    @FXML
    private Menu openRecentMenu;

    @FXML
    private MenuItem pageSetupMenuItem;

    @FXML
    private MenuItem printMenuItem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem quitMenuItem;

    @FXML
    private Menu editMenu;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private MenuItem copyMenuItem;

    @FXML
    private MenuItem pasteMenuItem;

    @FXML
    private MenuItem selectAllMenuItem;

    @FXML
    private MenuItem selectNoneMenuItem;

    @FXML
    private MenuItem openInEditorMenuItem;

    @FXML
    private Menu viewMenu;

    @FXML
    private CheckMenuItem showLabelsMenuItem;

    @FXML
    private CheckMenuItem colorPreviewMenuItem;

    @FXML
    private CheckMenuItem showChambersMenuItem;

    @FXML
    private MenuItem showMoreTilesMenuItem;

    @FXML
    private MenuItem showLessTilesMenuItem;

    @FXML
    private MenuItem resetMenuItem;

    @FXML
    private MenuItem zoomInMenuItem;

    @FXML
    private MenuItem zoomOutMenuItem;

    @FXML
    private MenuItem setMaxCopiesHyperbolicMenuItem;

    @FXML
    private MenuItem setMaxCopiesEuclideanMenuItem;

    @FXML
    private MenuItem fullScreenMenuItem;

    @FXML
    private Menu tilingsMenu;

    @FXML
    private MenuItem firstPageMenuItem;

    @FXML
    private MenuItem lastPageMenuItem;

    @FXML
    private MenuItem choosePageMenuItem;

    @FXML
    private MenuItem dualizeMenuItem;

    @FXML
    private MenuItem maxSymmetryMenuItem;

    @FXML
    private MenuItem orientateMenuItem;

    @FXML
    private MenuItem straightenMenuItem;

    @FXML
    private Menu windowMenu;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private MenuItem checkForUpdatesMenuItem;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private StackPane centerPane;

    @FXML
    private FlowPane statusBar;

    @FXML
    private Label memoryUsageLabel;

    @FXML
    private Button openButton;

    @FXML
    private Button printButton;

    @FXML
    private Button zoomInButton;

    @FXML
    private Button zoomOutButton;

    @FXML
    private Button undoButton;

    @FXML
    private Button redoButton;


    @FXML
    private TextArea infoTextArea;


    @FXML
    private CheckMenuItem useDarkThemeCheckMenuItem;

    @FXML
    void initialize() {
        // if we are running on MacOS, put the specific menu items in the right places
        if (ProgramProperties.isMacOS()) {
            getMenuBar().setUseSystemMenuBar(true);
            fileMenu.getItems().remove(getQuitMenuItem());
            // windowMenu.getItems().remove(getAboutMenuItem());
            //editMenu.getItems().remove(getPreferencesMenuItem());
        }
        getAboutMenuItem().setOnAction((e) -> SplashScreen.showSplash(Duration.ofMinutes(1)));
    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getFileMenu() {
        return fileMenu;
    }

    public MenuItem getNewMenuItem() {
        return newMenuItem;
    }

    public MenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public MenuItem getSaveSelectedMenuItem() {
        return saveSelectedMenuItem;
    }

    public Menu getOpenRecentMenu() {
        return openRecentMenu;
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

    public Menu getEditMenu() {
        return editMenu;
    }

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public MenuItem getRedoMenuItem() {
        return redoMenuItem;
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

    public MenuItem getSelectNoneMenuItem() {
        return selectNoneMenuItem;
    }

    public MenuItem getOpenInEditorMenuItem() {
        return openInEditorMenuItem;
    }

    public Menu getViewMenu() {
        return viewMenu;
    }

    public CheckMenuItem getShowLabelsMenuItem() {
        return showLabelsMenuItem;
    }

    public CheckMenuItem getColorPreviewMenuItem() {
        return colorPreviewMenuItem;
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

    public MenuItem getZoomInMenuItem() {
        return zoomInMenuItem;
    }

    public MenuItem getZoomOutMenuItem() {
        return zoomOutMenuItem;
    }

    public MenuItem getSetMaxCopiesHyperbolicMenuItem() {
        return setMaxCopiesHyperbolicMenuItem;
    }

    public MenuItem getSetMaxCopiesEuclideanMenuItem() {
        return setMaxCopiesEuclideanMenuItem;
    }

    public MenuItem getFullScreenMenuItem() {
        return fullScreenMenuItem;
    }

    public Menu getTilingsMenu() {
        return tilingsMenu;
    }

    public MenuItem getFirstPageMenuItem() {
        return firstPageMenuItem;
    }

    public MenuItem getLastPageMenuItem() {
        return lastPageMenuItem;
    }

    public MenuItem getChoosePageMenuItem() {
        return choosePageMenuItem;
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

    public MenuItem getStraightenMenuItem() {
        return straightenMenuItem;
    }

    public Menu getWindowMenu() {
        return windowMenu;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public MenuItem getCheckForUpdatesMenuItem() {
        return checkForUpdatesMenuItem;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    public StackPane getCenterPane() {
        return centerPane;
    }

    public FlowPane getStatusBar() {
        return statusBar;
    }

    public Label getMemoryUsageLabel() {
        return memoryUsageLabel;
    }

    public Button getOpenButton() {
        return openButton;
    }

    public Button getPrintButton() {
        return printButton;
    }

    public Button getZoomInButton() {
        return zoomInButton;
    }

    public Button getZoomOutButton() {
        return zoomOutButton;
    }

    public Button getUndoButton() {
        return undoButton;
    }

    public Button getRedoButton() {
        return redoButton;
    }

    public TextArea getInfoTextArea() {
        return infoTextArea;
    }

    public CheckMenuItem getUseDarkThemeCheckMenuItem() {
        return useDarkThemeCheckMenuItem;
    }
}
