/*
 * WindowController.java Copyright (C) 2019. Daniel H. Huson
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

public class WindowController {

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
    private MenuItem findMenuItem;

    @FXML
    private MenuItem findAgainMenuItem;

    @FXML
    private MenuItem lineWidthMenuItem;

    @FXML
    private MenuItem openInEditorMenuItem;

    @FXML
    private Menu viewMenu;

    @FXML
    private CheckMenuItem showLabelsMenuItem;

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
    private MenuItem fullScreenMenuItem;

    @FXML
    private Menu tilingsMenu;

    @FXML
    private MenuItem dualizeMenuItem;

    @FXML
    private MenuItem maxSymmetryMenuItem;

    @FXML
    private MenuItem orientateMenuItem;

    @FXML
    private CheckMenuItem straightenMenuItem;

    @FXML
    private Menu windowMenu;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private TreeView<?> treeView;

    @FXML
    private StackPane centerPane;

    @FXML
    private FlowPane statusBar;

    @FXML
    private Label memoryUsageLabel;

    @FXML
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'Window.fxml'.";
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'Window.fxml'.";
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'Window.fxml'.";
        assert fileMenu != null : "fx:id=\"fileMenu\" was not injected: check your FXML file 'Window.fxml'.";
        assert newMenuItem != null : "fx:id=\"newMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert openRecentMenu != null : "fx:id=\"openRecentMenu\" was not injected: check your FXML file 'Window.fxml'.";
        assert saveMenuItem != null : "fx:id=\"saveMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert pageSetupMenuItem != null : "fx:id=\"pageSetupMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert printMenuItem != null : "fx:id=\"printMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert quitMenuItem != null : "fx:id=\"quitMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert editMenu != null : "fx:id=\"editMenu\" was not injected: check your FXML file 'Window.fxml'.";
        assert undoMenuItem != null : "fx:id=\"undoMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert redoMenuItem != null : "fx:id=\"redoMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert copyMenuItem != null : "fx:id=\"copyMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert pasteMenuItem != null : "fx:id=\"pasteMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert selectAllMenuItem != null : "fx:id=\"selectAllMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert selectNoneMenuItem != null : "fx:id=\"selectNoneMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert findMenuItem != null : "fx:id=\"findMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert findAgainMenuItem != null : "fx:id=\"findAgainMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert lineWidthMenuItem != null : "fx:id=\"lineWidthMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert openInEditorMenuItem != null : "fx:id=\"openInEditorMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert viewMenu != null : "fx:id=\"viewMenu\" was not injected: check your FXML file 'Window.fxml'.";
        assert showLabelsMenuItem != null : "fx:id=\"showLabelsMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert showFDomainMenuItem != null : "fx:id=\"showFDomainMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert showChambersMenuItem != null : "fx:id=\"showChambersMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert showMoreTilesMenuItem != null : "fx:id=\"showMoreTilesMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert showLessTilesMenuItem != null : "fx:id=\"showLessTilesMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert resetMenuItem != null : "fx:id=\"resetMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert fullScreenMenuItem != null : "fx:id=\"fullScreenMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert tilingsMenu != null : "fx:id=\"tilingsMenu\" was not injected: check your FXML file 'Window.fxml'.";
        assert dualizeMenuItem != null : "fx:id=\"dualizeMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert maxSymmetryMenuItem != null : "fx:id=\"maxSymmetryMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert orientateMenuItem != null : "fx:id=\"orientateMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert straightenMenuItem != null : "fx:id=\"straightenMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert windowMenu != null : "fx:id=\"windowMenu\" was not injected: check your FXML file 'Window.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'Window.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'Window.fxml'.";
        assert mainSplitPane != null : "fx:id=\"mainSplitPane\" was not injected: check your FXML file 'Window.fxml'.";
        assert treeView != null : "fx:id=\"treeView\" was not injected: check your FXML file 'Window.fxml'.";
        assert centerPane != null : "fx:id=\"centerPane\" was not injected: check your FXML file 'Window.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'Window.fxml'.";
        assert memoryUsageLabel != null : "fx:id=\"memoryUsageLabel\" was not injected: check your FXML file 'Window.fxml'.";

        // if we are running on MacOS, put the specific menu items in the right places
        if (ProgramProperties.isMacOS()) {
            getMenuBar().setUseSystemMenuBar(true);
            fileMenu.getItems().remove(getQuitMenuItem());
            windowMenu.getItems().remove(getAboutMenuItem());
            //editMenu.getItems().remove(getPreferencesMenuItem());
        } else {
            getAboutMenuItem().setOnAction((e) -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(1)));
        }

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

    public MenuItem getFindMenuItem() {
        return findMenuItem;
    }

    public MenuItem getFindAgainMenuItem() {
        return findAgainMenuItem;
    }

    public MenuItem getLineWidthMenuItem() {
        return lineWidthMenuItem;
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

    public MenuItem getFullScreenMenuItem() {
        return fullScreenMenuItem;
    }

    public Menu getTilingsMenu() {
        return tilingsMenu;
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

    public CheckMenuItem getStraightenMenuItem() {
        return straightenMenuItem;
    }

    public Menu getWindowMenu() {
        return windowMenu;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    public TreeView<FileBrowser.FileNode> getTreeView() {
        return (TreeView<FileBrowser.FileNode>) treeView;
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
}
