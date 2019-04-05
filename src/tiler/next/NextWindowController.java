/*
 *  Copyright (C) 2019. Daniel H. Huson
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

package tiler.next;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class NextWindowController {

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
    private SplitPane mainSplitPane;

    @FXML
    private TreeView<FileBrowser.FileNode> treeView;

    @FXML
    private StackPane centerPane;

    @FXML
    private FlowPane statusBar;

    @FXML
    void initialize() {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert newMenuItem != null : "fx:id=\"newMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert openRecentMenu != null : "fx:id=\"openRecentMenu\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert saveMenuItem != null : "fx:id=\"saveMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert pageSetupMenuItem != null : "fx:id=\"pageSetupMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert printMenuItem != null : "fx:id=\"printMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert quitMenuItem != null : "fx:id=\"quitMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert copyMenuItem != null : "fx:id=\"copyMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert pasteMenuItem != null : "fx:id=\"pasteMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert selectAllMenuItem != null : "fx:id=\"selectAllMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert lineWidthMenuItem != null : "fx:id=\"lineWidthMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert straightenSelectedMenuItem != null : "fx:id=\"straightenSelectedMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert straightenAllMenuItem != null : "fx:id=\"straightenAllMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert straightenAlwaysCheckMenuItem != null : "fx:id=\"straightenAlwaysCheckMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert colorsMenu != null : "fx:id=\"colorsMenu\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert addColorSchemeMenuItem != null : "fx:id=\"addColorSchemeMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert showFDomainMenuItem != null : "fx:id=\"showFDomainMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert showChambersMenuItem != null : "fx:id=\"showChambersMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert showMoreTilesMenuItem != null : "fx:id=\"showMoreTilesMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert showLessTilesMenuItem != null : "fx:id=\"showLessTilesMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert resetMenuItem != null : "fx:id=\"resetMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert firstTilingMenuItem != null : "fx:id=\"firstTilingMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert nextTilingMenuItem != null : "fx:id=\"nextTilingMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert previousTilingMenuItem != null : "fx:id=\"previousTilingMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert lastTilingMenuItem != null : "fx:id=\"lastTilingMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert dualizeMenuItem != null : "fx:id=\"dualizeMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert maxSymmetryMenuItem != null : "fx:id=\"maxSymmetryMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert orientateMenuItem != null : "fx:id=\"orientateMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert editSymmetryGroupMenuItem != null : "fx:id=\"editSymmetryGroupMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert mainSplitPane != null : "fx:id=\"mainSplitPane\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert treeView != null : "fx:id=\"treeView\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert centerPane != null : "fx:id=\"centerPane\" was not injected: check your FXML file 'NextWindow.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'NextWindow.fxml'.";

    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
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

    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    public TreeView<FileBrowser.FileNode> getTreeView() {
        return treeView;
    }

    public StackPane getCenterPane() {
        return centerPane;
    }

    public FlowPane getStatusBar() {
        return statusBar;
    }
}
