/*
 * ControlBindings.java Copyright (C) 2019. Daniel H. Huson
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
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.Print;
import jloda.fx.util.Printable;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.Geometry;
import tegula.tiling.HyperbolicTiling;
import tegula.tilingcollection.TilingCollectionTab;
import tegula.tilingeditor.TilingEditorTab;

import java.util.Collection;

/**
 * sets up menu item bindings
 * Daniel Huson, 4.2019
 */
public class ControlBindings {
    private static int windowsCreated = 1;

    /**
     * setup all menu item bindings
     *
     * @param window
     */
    public static void setup(final Window window) {
        final WindowController controller = window.getController();
        final ReadOnlyObjectProperty<Tab> selectedTab = window.getMainTabPane().getSelectionModel().selectedItemProperty();
        final BooleanProperty isCollectionTabSelected = new SimpleBooleanProperty(false);
        selectedTab.addListener((c, o, n) -> {
            isCollectionTabSelected.set(n instanceof TilingCollectionTab);
            if (n instanceof TilingCollectionTab)
                controller.getShowLabelsMenuItem().setSelected(((TilingCollectionTab) n).isShowLabels());
        });
        final BooleanProperty canSave = new SimpleBooleanProperty(false);
        final IntegerProperty selectionInCollection = new SimpleIntegerProperty(0);

        selectedTab.addListener((c, o, n) -> {
            if (n instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) n;
                canSave.unbind();
                canSave.set(tab.isDirty());
                canSave.bind(tab.dirtyProperty());
                controller.getUndoMenuItem().disableProperty().unbind();
                controller.getUndoMenuItem().setDisable(!tab.getUndoManager().canUndoProperty().get());
                controller.getUndoMenuItem().disableProperty().bind(tab.getUndoManager().canUndoProperty().not());
                controller.getUndoMenuItem().textProperty().bind(tab.getUndoManager().undoNameProperty());
                controller.getRedoMenuItem().disableProperty().unbind();
                controller.getRedoMenuItem().setDisable(!tab.getUndoManager().canRedoProperty().get());
                controller.getRedoMenuItem().disableProperty().bind(tab.getUndoManager().canRedoProperty().not());
                controller.getRedoMenuItem().textProperty().bind(tab.getUndoManager().redoNameProperty());

                controller.getShowChambersMenuItem().setSelected(tab.getTilingStyle().isShowAllChambers());

                controller.getShowMoreTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);
                controller.getShowLessTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);

            } else {
                canSave.unbind();
                canSave.set(false);
                controller.getUndoMenuItem().disableProperty().unbind();
                controller.getUndoMenuItem().setDisable(true);
                controller.getUndoMenuItem().textProperty().unbind();
                controller.getUndoMenuItem().setText("Undo");
                controller.getRedoMenuItem().disableProperty().unbind();
                controller.getRedoMenuItem().setDisable(true);
                controller.getRedoMenuItem().textProperty().unbind();
                controller.getRedoMenuItem().setText("Redo");

                controller.getShowChambersMenuItem().setSelected(false);

                controller.getShowMoreTilesMenuItem().setDisable(true);
                controller.getShowLessTilesMenuItem().setDisable(true);
            }
            if (n instanceof TilingCollectionTab) {
                final TilingCollectionTab tab = (TilingCollectionTab) n;
                selectionInCollection.unbind();
                selectionInCollection.set(tab.getSelectionModel().getSelectedItems().size());
                selectionInCollection.bind(Bindings.size(tab.getSelectionModel().getSelectedItems()));
            } else {
                selectionInCollection.unbind();
                selectionInCollection.set(0);
            }
        });

        controller.getNewMenuItem().setOnAction((e) -> {
            final Window newWindow = (Window) MainWindowManager.getInstance().createAndShowWindow(false);
            newWindow.getStage().setTitle(ProgramProperties.getProgramName() + " [" + (++windowsCreated) + "]");
            MainWindowManager.getInstance().setLastFocusedMainWindow(newWindow);
        });

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(window.getStage()));

        RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
        RecentFilesManager.getInstance().setupMenu(controller.getOpenRecentMenu());

        controller.getSaveMenuItem().setOnAction((e) -> {
            System.err.println("Save: not implemented");
        });
        controller.getSaveMenuItem().disableProperty().bind(canSave.not());

        controller.getPrintMenuItem().setOnAction((e) -> {
            final Tab tab = selectedTab.get();
            if (tab instanceof Printable)
                Print.print(window.getStage(), ((Printable) tab).getPrintable());
        });
        controller.getPrintMenuItem().disableProperty().bind(selectedTab.isNull());

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(window.getStage()));

        controller.getCloseMenuItem().setOnAction((e) -> {
            window.close();
        });

        window.getStage().setOnCloseRequest((e) -> {
            window.close();
            e.consume();
        });

        controller.getQuitMenuItem().setOnAction((e) -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final Window aWindow = (Window) MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                // if (!aWindow.clear(true, true)) break;
                aWindow.close();
            }
        });

        controller.getUndoMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getUndoManager().undo();
            }
        });

        controller.getRedoMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getUndoManager().redo();
            }
        });

        controller.getSelectAllMenuItem().setOnAction((c) -> {
            if (selectedTab.get() instanceof TilingCollectionTab) {
                final TilingCollectionTab tab = (TilingCollectionTab) selectedTab.get();
                tab.getSelectionModel().selectAll();
            }
        });
        controller.getSelectAllMenuItem().disableProperty().bind(selectedTab.isNull());


        controller.getSelectNoneMenuItem().setOnAction((c) -> {
            if (selectedTab.get() instanceof TilingCollectionTab) {
                final TilingCollectionTab tab = (TilingCollectionTab) selectedTab.get();
                tab.getSelectionModel().clearSelection();
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
                final TilingCollectionTab tab = (TilingCollectionTab) selectedTab.get();
                controller.getFindAgainMenuItem().disableProperty().bind(tab.getFindToolBar().canFindAgainProperty());
            } else {
                controller.getFindAgainMenuItem().disableProperty().unbind();
                controller.getFindAgainMenuItem().setDisable(true);
            }
        });

        controller.getOpenInEditorMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingCollectionTab) {
                final TilingCollectionTab tab = (TilingCollectionTab) selectedTab.get();
                final Collection<DSymbol> symbols = tab.getSelectionModel().getSelectedItems();
                final String prefix = tab.getTilingCollection().getTitle();
                for (DSymbol dSymbol : symbols) {
                    final TilingEditorTab editorTab = new TilingEditorTab(new DSymbol(dSymbol), prefix + "-" + dSymbol.getNr1());
                    window.getMainTabPane().getTabs().add(editorTab);
                }
            }
        });
        controller.getOpenInEditorMenuItem().disableProperty().bind(selectionInCollection.isEqualTo(0));

        controller.getFullScreenMenuItem().setOnAction((e) -> {
            if (window.getStage().isFullScreen()) {
                window.getStage().setFullScreen(false);
            } else {
                window.getStage().setFullScreen(true);
            }
        });
        window.getStage().fullScreenProperty().addListener((c, o, n) -> {
            controller.getFullScreenMenuItem().setText(n ? "Exit Fullscreen" : "Enter Fullscreen");
        });

        controller.getShowLabelsMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingCollectionTab) {
                ((TilingCollectionTab) selectedTab.get()).setShowLabels(controller.getShowLabelsMenuItem().isSelected());
            }
        });
        controller.getShowLabelsMenuItem().disableProperty().bind(isCollectionTabSelected.not());

        controller.getShowChambersMenuItem().setOnAction((e) -> {
            final boolean selected = controller.getShowChambersMenuItem().isSelected();
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getUndoManager().doAndAdd(new UndoableRedoableCommand("show chambers") {
                    @Override
                    public void undo() {
                        tab.getTilingStyle().setShowAllChambers(!selected);
                        tab.getTilingPane().update();
                    }

                    @Override
                    public void redo() {
                        tab.getTilingStyle().setShowAllChambers(selected);
                        tab.getTilingPane().update();
                    }
                });
            }
        });
        controller.getShowChambersMenuItem().disableProperty().bind(isCollectionTabSelected);

        controller.getShowMoreTilesMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                if (tab.getTiling() instanceof HyperbolicTiling)
                    tab.getUndoManager().doAndAdd(new UndoableRedoableCommand("show more tiles") {
                        @Override
                        public void undo() {
                            tab.getTilingPane().decreaseTiling();
                        }

                        @Override
                        public void redo() {
                            tab.getTilingPane().increaseTiling();
                        }
                    });
            }
        });

        controller.getShowLessTilesMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                if (tab.getTiling() instanceof HyperbolicTiling)
                    tab.getUndoManager().doAndAdd(new UndoableRedoableCommand("show less tiles") {
                        @Override
                        public void undo() {
                            tab.getTilingPane().increaseTiling();
                        }

                        @Override
                        public void redo() {
                            tab.getTilingPane().decreaseTiling();
                        }
                    });
            }
        });

        controller.getStraightenMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getController().getStraightenEdgesButton().getOnAction().handle(e);
            }
        });
        controller.getStraightenMenuItem().disableProperty().bind(isCollectionTabSelected);

        controller.getDualizeMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getController().getDualizeButton().getOnAction().handle(e);
            }
        });
        controller.getDualizeMenuItem().disableProperty().bind(isCollectionTabSelected);

        controller.getMaxSymmetryMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getController().getMaximizeButton().getOnAction().handle(e);
            }
        });
        controller.getMaxSymmetryMenuItem().disableProperty().bind(isCollectionTabSelected);

        controller.getOrientateMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getController().getOrientateButton().getOnAction().handle(e);
            }
        });
        controller.getOrientateMenuItem().disableProperty().bind(isCollectionTabSelected);

        controller.getSelectAllMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.selectAll(true);
            }
        });
        controller.getSelectAllMenuItem().disableProperty().bind(isCollectionTabSelected);

        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.selectAll(false);
            }
        });
        controller.getSelectNoneMenuItem().disableProperty().bind(isCollectionTabSelected);

    }
}
