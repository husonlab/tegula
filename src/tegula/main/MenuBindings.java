/*
 * MenuBindings.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.Tab;
import jloda.fx.util.MemoryUsage;
import jloda.fx.util.Print;
import jloda.fx.util.Printable;
import jloda.fx.window.MainWindowManager;
import tegula.core.dsymbols.DSymbol;
import tegula.tilingcollection.TilingCollectionTab;
import tegula.tilingeditor.TilingEditorTab;

import java.util.Collection;

/**
 * sets up menu item bindings
 * Daniel Huson, 4.2019
 */
public class MenuBindings {
    /**
     * setup all menu item bindings
     *
     * @param mainWindow
     */
    public static void setup(Window mainWindow) {
        final WindowController controller = mainWindow.getController();
        final ReadOnlyObjectProperty<Tab> selectedTab = mainWindow.getMainTabPane().getSelectionModel().selectedItemProperty();
        final BooleanProperty canSave = new SimpleBooleanProperty(false);
        final IntegerProperty selectionInCollection = new SimpleIntegerProperty(0);

        selectedTab.addListener((c, o, n) -> {
            if (n instanceof TilingEditorTab) {
                canSave.unbind();
                canSave.set(((TilingEditorTab) n).isDirty());
                canSave.bind(((TilingEditorTab) n).dirtyProperty());
                selectionInCollection.unbind();
                selectionInCollection.set(0);
            } else if (n instanceof TilingCollectionTab) {
                selectionInCollection.unbind();
                selectionInCollection.set(((TilingCollectionTab) n).getSelectionModel().getSelectedItems().size());
                selectionInCollection.bind(Bindings.size(((TilingCollectionTab) n).getSelectionModel().getSelectedItems()));
                canSave.unbind();
                canSave.set(false);
            } else {
                selectionInCollection.unbind();
                selectionInCollection.set(0);
                canSave.unbind();
                canSave.set(false);
            }
        });

        controller.getNewMenuItem().setOnAction((e) -> MainWindowManager.getInstance().createAndShowWindow(false));

        controller.getSaveMenuItem().setOnAction((e) -> {
            System.err.println("Save: not implemented");
        });
        controller.getSaveMenuItem().disableProperty().bind(canSave.not());

        controller.getPrintMenuItem().setOnAction((e) -> {
            final Tab tab = selectedTab.get();
            if (tab instanceof Printable)
                Print.print(mainWindow.getStage(), ((Printable) tab).getPrintable());
        });
        controller.getPrintMenuItem().disableProperty().bind(selectedTab.isNull());

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(mainWindow.getStage()));

        controller.getCloseMenuItem().setOnAction((e) -> {
            mainWindow.close();
        });

        mainWindow.getStage().setOnCloseRequest((e) -> {
            mainWindow.close();
            e.consume();
        });

        controller.getQuitMenuItem().setOnAction((e) -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final Window window = (Window) MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                // if (!window.clear(true, true)) break;
                window.close();
            }
        });

        controller.getSelectAllMenuItem().setOnAction((c) -> {
            if (selectedTab.get() instanceof TilingCollectionTab) {
                ((TilingCollectionTab) selectedTab.get()).getSelectionModel().selectAll();
            }
        });
        controller.getSelectAllMenuItem().disableProperty().bind(selectedTab.isNull());


        controller.getSelectNoneMenuItem().setOnAction((c) -> {
            if (selectedTab.get() instanceof TilingCollectionTab) {
                ((TilingCollectionTab) selectedTab.get()).getSelectionModel().clearSelection();
            }
        });
        controller.getSelectNoneMenuItem().disableProperty().bind(selectedTab.isNull());

        controller.getFindMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingCollectionTab) {
                final TilingCollectionTab tab = (TilingCollectionTab) selectedTab.get();
                tab.getFindToolBar().setShowFindToolBar(true);
            }
        });

        controller.getFindAgainMenuItem().setOnAction((e) -> {
            final TilingCollectionTab tab = (TilingCollectionTab) selectedTab.get();
            if (tab.getController().getTopVBox().getChildren().contains(tab.getFindToolBar()))
                tab.getFindToolBar().findAgain();
        });

        selectedTab.addListener((c, o, n) -> {
            if (n instanceof TilingCollectionTab) {
                controller.getFindAgainMenuItem().disableProperty().bind(((TilingCollectionTab) n).getFindToolBar().canFindAgainProperty());
            } else {
                controller.getFindAgainMenuItem().disableProperty().unbind();
                controller.getFindAgainMenuItem().setDisable(true);
            }
        });

        controller.getOpenInEditorMenuItem().setOnAction((e) -> {
            final TilingCollectionTab tab = (TilingCollectionTab) selectedTab.get();
            if (tab != null) {
                final Collection<DSymbol> symbols = tab.getSelectionModel().getSelectedItems();
                final String prefix = tab.getTilingCollection().getTitle();
                for (DSymbol dSymbol : symbols) {
                    final TilingEditorTab editorTab = new TilingEditorTab(dSymbol, prefix + "-" + (((TilingCollectionTab) selectedTab.get()).incrementSpawned()));
                    editorTab.getTilingPane().update();
                    mainWindow.getMainTabPane().getTabs().add(editorTab);
                }
            }
        });
        controller.getOpenInEditorMenuItem().disableProperty().bind(selectionInCollection.isEqualTo(0));

        controller.getFullScreenMenuItem().setOnAction((e) -> {
            if (mainWindow.getStage().isFullScreen()) {
                mainWindow.getStage().setFullScreen(false);
                controller.getFullScreenMenuItem().setText("Enter Fullscreen");
            } else {
                mainWindow.getStage().setFullScreen(true);
                controller.getFullScreenMenuItem().setText("Exit Fullscreen");
            }
        });

        final MemoryUsage memoryUsage = MemoryUsage.getInstance();
        controller.getMemoryUsageLabel().textProperty().bind(memoryUsage.memoryUsageStringProperty());
    }
}
