/*
 * ControlBindings.java Copyright (C) 2020. Daniel H. Huson
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
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.Print;
import jloda.fx.util.Printable;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.Geometry;
import tegula.dbcollection.ICollectionTab;
import tegula.tiling.HyperbolicTiling;
import tegula.tilingeditor.TilingEditorTab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

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
    public static void setup(final MainWindow window) {
        final MainWindowController controller = window.getController();
        final ReadOnlyObjectProperty<Tab> selectedTab = window.getMainTabPane().getSelectionModel().selectedItemProperty();
        final BooleanProperty isCollectionTabSelected = new SimpleBooleanProperty(false);
        selectedTab.addListener((c, o, n) -> {
            isCollectionTabSelected.set(n instanceof ICollectionTab);
            if (n instanceof ICollectionTab)
                controller.getShowLabelsMenuItem().setSelected(((ICollectionTab) n).isShowLabels());
        });
        final IntegerProperty selectionInCollection = new SimpleIntegerProperty(0);

        selectedTab.addListener((c, o, n) -> {
            controller.getSaveSelectedMenuItem().disableProperty().unbind();
            controller.getSaveSelectedMenuItem().setDisable(true);

            if (n instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) n;
                controller.getUndoMenuItem().disableProperty().unbind();
                controller.getUndoMenuItem().setDisable(!tab.getUndoManager().undoableProperty().get());
                controller.getUndoMenuItem().disableProperty().bind(tab.getUndoManager().undoableProperty().not());
                controller.getUndoMenuItem().textProperty().bind(tab.getUndoManager().undoNameProperty());
                controller.getRedoMenuItem().disableProperty().unbind();
                controller.getRedoMenuItem().setDisable(!tab.getUndoManager().redoableProperty().get());
                controller.getRedoMenuItem().disableProperty().bind(tab.getUndoManager().redoableProperty().not());
                controller.getRedoMenuItem().textProperty().bind(tab.getUndoManager().redoNameProperty());

                controller.getShowChambersMenuItem().setSelected(tab.getTilingStyle().isShowAllChambers());

                // todo: this needs to be dynamic
                controller.getShowMoreTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);
                controller.getShowLessTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);

                controller.getSaveSelectedMenuItem().setDisable(false);
            } else {
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

            if (n instanceof ICollectionTab) {
                final ICollectionTab tab = (ICollectionTab) n;

                selectionInCollection.unbind();
                selectionInCollection.set(tab.getSelectionModel().getSelectedItems().size());
                selectionInCollection.bind(Bindings.size(tab.getSelectionModel().getSelectedItems()));
                controller.getSaveSelectedMenuItem().disableProperty().bind(selectionInCollection.isEqualTo(0));

            } else {
                selectionInCollection.unbind();
                selectionInCollection.set(0);
            }
        });

        controller.getNewMenuItem().setOnAction((e) -> {
            final MainWindow newWindow = (MainWindow) MainWindowManager.getInstance().createAndShowWindow(false);
            newWindow.getStage().setTitle(ProgramProperties.getProgramName() + " " + (++windowsCreated));
            MainWindowManager.getInstance().setLastFocusedMainWindow(newWindow);
        });

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(window.getStage()));

        controller.getOpenButton().setOnAction(controller.getOpenMenuItem().getOnAction());
        controller.getOpenButton().disableProperty().bind(controller.getOpenMenuItem().disableProperty());

        RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
        RecentFilesManager.getInstance().setupMenu(controller.getOpenRecentMenu());

        controller.getSaveSelectedMenuItem().setOnAction((e) -> {
            final Tab tab = selectedTab.get();
            if (tab != null) {
                final File previousFile = new File(ProgramProperties.get("SaveSelectionFile", "selected.tgs") + "+");

                final FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(previousFile.getParentFile());
                fileChooser.setInitialFileName(previousFile.getName());
                fileChooser.setTitle("Append to File - " + ProgramProperties.getProgramVersion());
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Tilings file", "*.tgs+", "*.tgs"));
                File selectedFile = fileChooser.showSaveDialog(window.getStage());

                if (selectedFile != null) {
                    if (selectedFile.getPath().endsWith("+"))
                        selectedFile = new File(selectedFile.getPath().replaceAll("\\+$", ""));
                    ProgramProperties.put("SaveSelectionFile", selectedFile.getPath());
                    RecentFilesManager.getInstance().insertRecentFile(selectedFile.getPath());
                    final ArrayList<DSymbol> list = new ArrayList<>();
                    if (tab instanceof TilingEditorTab) {
                        list.add(((TilingEditorTab) tab).getTiling().getDSymbol());
                    } else if (tab instanceof ICollectionTab) {
                        list.addAll(((ICollectionTab) tab).getSelectionModel().getSelectedItems());
                    }
                    try (final BufferedWriter w = new BufferedWriter(new FileWriter(selectedFile, true))) {
                        w.write(Basic.toString(list, "\n") + "\n");
                    } catch (IOException ex) {
                        Basic.caught(ex);
                        NotificationManager.showError("Save selection failed: " + ex.getMessage());
                    }
                }
            }
        });

        controller.getPrintMenuItem().setOnAction((e) -> {
            final Tab tab = selectedTab.get();
            if (tab instanceof Printable)
                Print.printSnapshot(window.getStage(), ((Printable) tab).getPrintable());
        });
        controller.getPrintMenuItem().disableProperty().bind(selectedTab.isNull());

        controller.getPrintButton().setOnAction(controller.getPrintMenuItem().getOnAction());
        controller.getPrintButton().disableProperty().bind(controller.getPrintMenuItem().disableProperty());

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(window.getStage()));

        controller.getCloseMenuItem().setOnAction((e) -> {
            MainWindowManager.getInstance().closeMainWindow(window);
        });

        window.getStage().setOnCloseRequest((e) -> {
            controller.getCloseMenuItem().getOnAction().handle(null);
            e.consume();
        });

        controller.getQuitMenuItem().setOnAction((e) -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final MainWindow aWindow = (MainWindow) MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                // if (!aWindow.clear(true, true)) break;
                MainWindowManager.getInstance().closeMainWindow(window);

            }
        });

        controller.getUndoMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getUndoManager().undo();
            }
        });

        controller.getUndoButton().setOnAction(controller.getUndoMenuItem().getOnAction());
        controller.getUndoButton().disableProperty().bind(controller.getUndoMenuItem().disableProperty());

        controller.getRedoMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.getUndoManager().redo();
            }
        });

        controller.getRedoButton().setOnAction(controller.getRedoMenuItem().getOnAction());
        controller.getRedoButton().disableProperty().bind(controller.getRedoMenuItem().disableProperty());


        controller.getOpenInEditorMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof ICollectionTab) {
                final ICollectionTab tab = (ICollectionTab) selectedTab.get();
                final Collection<DSymbol> symbols = Basic.reverse(tab.getSelectionModel().getSelectedItems());
                for (DSymbol dSymbol : symbols) {
                    final TilingEditorTab editorTab = new TilingEditorTab(new DSymbol(dSymbol), Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(tab.getFileName()), ":" + dSymbol.getNr1()));
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
            if (selectedTab.get() instanceof ICollectionTab) {
                ((ICollectionTab) selectedTab.get()).setShowLabels(controller.getShowLabelsMenuItem().isSelected());
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


        controller.getFirstPageMenuItem().setOnAction((e) -> {
            final ICollectionTab collectionTab = ((ICollectionTab) selectedTab.get());
            collectionTab.gotoPage(ICollectionTab.FirstPage);
        });
        controller.getFirstPageMenuItem().disableProperty().bind(isCollectionTabSelected.not());

        controller.getLastPageMenuItem().setOnAction((e) -> {
            final ICollectionTab collectionTab = ((ICollectionTab) selectedTab.get());
            collectionTab.gotoPage(ICollectionTab.LastPage);
        });
        controller.getLastPageMenuItem().disableProperty().bind(isCollectionTabSelected.not());

        controller.getChoosePageMenuItem().setOnAction((e) -> {
            final ICollectionTab collectionTab = ((ICollectionTab) selectedTab.get());

            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Page selection");
            dialog.setHeaderText("Choose page");
            dialog.setContentText(String.format("Enter page number (range: 1-%d):", collectionTab.getNumberOfPages()));

// Traditional way to get the response value.
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                if (Basic.isInteger(result.get())) {
                    collectionTab.gotoPage(Basic.parseInt(result.get()));
                }
            });
        });
        controller.getChoosePageMenuItem().disableProperty().bind(isCollectionTabSelected.not());


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
            } else if (selectedTab.get() instanceof ICollectionTab) {
                final ICollectionTab tab = (ICollectionTab) selectedTab.get();
                tab.getSelectionModel().selectAll();
            }
        });

        controller.getSelectAllMenuItem().disableProperty().bind(selectedTab.isNull());

        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                tab.selectAll(false);
            } else if (selectedTab.get() instanceof ICollectionTab) {
                final ICollectionTab tab = (ICollectionTab) selectedTab.get();
                tab.getSelectionModel().clearSelection();
            }
        });
        controller.getSelectNoneMenuItem().disableProperty().bind(selectedTab.isNull());

        controller.getCheckForUpdatesMenuItem().setOnAction((e) -> CheckForUpdate.apply(ProgramProperties.getProgramURL()));
        MainWindowManager.getInstance().changedProperty().addListener((c, o, n) -> controller.getCheckForUpdatesMenuItem().disableProperty().set(MainWindowManager.getInstance().size() > 1
                || (MainWindowManager.getInstance().size() == 1 && !MainWindowManager.getInstance().getMainWindow(0).isEmpty())));

    }
}
