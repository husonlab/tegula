/*
 * MainWindowController.java Copyright (C) 2020. Daniel H. Huson
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
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert fileMenu != null : "fx:id=\"fileMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert newMenuItem != null : "fx:id=\"newMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert saveSelectedMenuItem != null : "fx:id=\"saveSelectedMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openRecentMenu != null : "fx:id=\"openRecentMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pageSetupMenuItem != null : "fx:id=\"pageSetupMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert printMenuItem != null : "fx:id=\"printMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert quitMenuItem != null : "fx:id=\"quitMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert editMenu != null : "fx:id=\"editMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert undoMenuItem != null : "fx:id=\"undoMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert redoMenuItem != null : "fx:id=\"redoMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert copyMenuItem != null : "fx:id=\"copyMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pasteMenuItem != null : "fx:id=\"pasteMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectAllMenuItem != null : "fx:id=\"selectAllMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectNoneMenuItem != null : "fx:id=\"selectNoneMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openInEditorMenuItem != null : "fx:id=\"openInEditorMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert viewMenu != null : "fx:id=\"viewMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert showLabelsMenuItem != null : "fx:id=\"showLabelsMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert colorPreviewMenuItem != null : "fx:id=\"colorPreviewMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert showChambersMenuItem != null : "fx:id=\"showChambersMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert showMoreTilesMenuItem != null : "fx:id=\"showMoreTilesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert showLessTilesMenuItem != null : "fx:id=\"showLessTilesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert zoomInMenuItem != null : "fx:id=\"zoomInMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert zoomOutMenuItem != null : "fx:id=\"zoomOutMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert resetMenuItem != null : "fx:id=\"resetMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert fullScreenMenuItem != null : "fx:id=\"fullScreenMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert tilingsMenu != null : "fx:id=\"tilingsMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert firstPageMenuItem != null : "fx:id=\"firstPageMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert lastPageMenuItem != null : "fx:id=\"lastPageMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert choosePageMenuItem != null : "fx:id=\"choosePageMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert dualizeMenuItem != null : "fx:id=\"dualizeMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert maxSymmetryMenuItem != null : "fx:id=\"maxSymmetryMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert orientateMenuItem != null : "fx:id=\"orientateMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert straightenMenuItem != null : "fx:id=\"straightenMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert windowMenu != null : "fx:id=\"windowMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert checkForUpdatesMenuItem != null : "fx:id=\"checkForUpdatesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert mainSplitPane != null : "fx:id=\"mainSplitPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert centerPane != null : "fx:id=\"centerPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert memoryUsageLabel != null : "fx:id=\"memoryUsageLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";

        assert openButton != null : "fx:id=\"openButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert printButton != null : "fx:id=\"printButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert zoomInButton != null : "fx:id=\"zoomInButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert zoomOutButton != null : "fx:id=\"zoomOutButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert undoButton != null : "fx:id=\"undoButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert redoButton != null : "fx:id=\"redoButton\" was not injected: check your FXML file 'MainWindow.fxml'.";


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
}
