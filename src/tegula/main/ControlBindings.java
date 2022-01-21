/*
 * ControlBindings.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import jloda.fx.dialog.SetIntegerParameterDialog;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.FileOpenManager;
import jloda.fx.util.Print;
import jloda.fx.util.Printable;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.*;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.Geometry;
import tegula.dbcollection.ICollectionTab;
import tegula.tiling.HyperbolicTiling;
import tegula.tilingeditor.TilingEditorTab;
import tegula.tilingpane.TilingPane;

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
            if (n instanceof ICollectionTab) {
                controller.getShowLabelsMenuItem().setSelected(((ICollectionTab) n).isShowLabels());
                controller.getColorPreviewMenuItem().setSelected(((ICollectionTab) n).isColorPreview());
            }
        });
        final IntegerProperty selectionInCollection = new SimpleIntegerProperty(0);

        controller.getShowLessTilesMenuItem().setDisable(true);
        controller.getShowMoreTilesMenuItem().setDisable(true);
        controller.getUndoMenuItem().setDisable(true);
        controller.getRedoMenuItem().setDisable(true);

        final InvalidationListener listener = e -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingEditorTab tab = (TilingEditorTab) selectedTab.get();
                controller.getShowMoreTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);
                controller.getShowLessTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);
            }
        };

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
                controller.getSaveSelectedMenuItem().setDisable(false);

                controller.getShowMoreTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);
                controller.getShowLessTilesMenuItem().setDisable(tab.getTiling().getGeometry() != Geometry.Hyperbolic);
                tab.getTiling().fDomainObjectProperty().addListener(listener);
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
                if (o instanceof TilingEditorTab)
                    ((TilingEditorTab) o).getTiling().fDomainObjectProperty().removeListener(listener);

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
						w.write(StringUtils.toString(list, "\n") + "\n");
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

        controller.getCloseMenuItem().setOnAction((e) -> MainWindowManager.getInstance().closeMainWindow(window));

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
                final Collection<DSymbol> symbols = CollectionUtils.reverse(tab.getSelectionModel().getSelectedItems());
                for (DSymbol dSymbol : symbols) {
					final TilingEditorTab editorTab = new TilingEditorTab(new DSymbol(dSymbol), FileUtils.replaceFileSuffix(FileUtils.getFileNameWithoutPath(tab.getFileName()), "-" + dSymbol.getNr1()));
                    window.getMainTabPane().getTabs().add(editorTab);
                    window.getMainTabPane().getSelectionModel().select(editorTab);
                }
            }
        });
        controller.getOpenInEditorMenuItem().disableProperty().bind(selectionInCollection.isEqualTo(0));

        controller.getFullScreenMenuItem().setOnAction((e) -> {
            window.getStage().setFullScreen(!window.getStage().isFullScreen());
        });
        window.getStage().fullScreenProperty().addListener((c, o, n) -> controller.getFullScreenMenuItem().setText(n ? "Exit Fullscreen" : "Enter Fullscreen"));

        controller.getShowLabelsMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof ICollectionTab) {
                ((ICollectionTab) selectedTab.get()).setShowLabels(controller.getShowLabelsMenuItem().isSelected());
            }
        });
        controller.getShowLabelsMenuItem().disableProperty().bind(isCollectionTabSelected.not());

        controller.getColorPreviewMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof ICollectionTab) {
                ((ICollectionTab) selectedTab.get()).setColorPreview(controller.getColorPreviewMenuItem().isSelected());
            }
        });
        controller.getColorPreviewMenuItem().disableProperty().bind(isCollectionTabSelected.not());


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

        controller.getZoomInMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingPane tilingPane = ((TilingEditorTab) selectedTab.get()).getTilingPane();
                ((TilingEditorTab) selectedTab.get()).getUndoManager().doAndAdd(new UndoableRedoableCommand("zoom in") {
                    @Override
                    public void undo() {
                        tilingPane.getWorldScale().setX(1.0 / 1.05 * tilingPane.getWorldScale().getX());
                        tilingPane.getWorldScale().setY(1.0 / 1.05 * tilingPane.getWorldScale().getY());
                        if (tilingPane.getGeometry() == Geometry.Euclidean) {
                        }
                    }

                    @Override
                    public void redo() {
                        tilingPane.getWorldScale().setX(1.05 * tilingPane.getWorldScale().getX());
                        tilingPane.getWorldScale().setY(1.05 * tilingPane.getWorldScale().getY());
                        if (tilingPane.getGeometry() == Geometry.Euclidean)
                            tilingPane.update();
                    }
                });
            } else if (selectedTab.get() instanceof ICollectionTab) {
                ((ICollectionTab) selectedTab.get()).changePreviewSize(true);
            }
        });
        controller.getZoomInMenuItem().disableProperty().bind(selectedTab.isNull());
        controller.getZoomInButton().setOnAction(controller.getZoomInMenuItem().getOnAction());
        controller.getZoomInButton().disableProperty().bind(selectedTab.isNull());

        controller.getZoomOutMenuItem().setOnAction((e) -> {
            if (selectedTab.get() instanceof TilingEditorTab) {
                final TilingPane tilingPane = ((TilingEditorTab) selectedTab.get()).getTilingPane();
                ((TilingEditorTab) selectedTab.get()).getUndoManager().doAndAdd(new UndoableRedoableCommand("zoom out") {
                    @Override
                    public void undo() {
                        tilingPane.getWorldScale().setX(1.05 * tilingPane.getWorldScale().getX());
                        tilingPane.getWorldScale().setY(1.05 * tilingPane.getWorldScale().getY());
                        if (tilingPane.getGeometry() == Geometry.Euclidean)
                            tilingPane.update();
                    }

                    @Override
                    public void redo() {
                        tilingPane.getWorldScale().setX(1.0 / 1.05 * tilingPane.getWorldScale().getX());
                        tilingPane.getWorldScale().setY(1.0 / 1.05 * tilingPane.getWorldScale().getY());
                        if (tilingPane.getGeometry() == Geometry.Euclidean)
                            tilingPane.update();
                    }
                });
            }
            if (selectedTab.get() instanceof ICollectionTab) {
                ((ICollectionTab) selectedTab.get()).changePreviewSize(false);
            }
        });
        controller.getZoomOutMenuItem().disableProperty().bind(selectedTab.isNull());
        controller.getZoomOutButton().setOnAction(controller.getZoomOutMenuItem().getOnAction());
        controller.getZoomOutButton().disableProperty().bind(selectedTab.isNull());

        controller.getSetMaxCopiesHyperbolicMenuItem().setOnAction(e -> {
            final Integer result = SetIntegerParameterDialog.apply(window.getStage(), "Set max copies of hyperbolic fundamental domain", ProgramProperties.get("MaxCopiesHyperbolic", 5000));
            if (result != null)
                ProgramProperties.put("MaxCopiesHyperbolic", result);
        });
        controller.getSetMaxCopiesEuclideanMenuItem().setOnAction(e -> {
            final Integer result = SetIntegerParameterDialog.apply(window.getStage(), "Set max copies of hyperbolic fundamental domain", ProgramProperties.get("MaxCopiesEuclidean", 5000));
            if (result != null)
                ProgramProperties.put("MaxCopiesEuclidean", result);
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
                if (NumberUtils.isInteger(result.get())) {
                    collectionTab.gotoPage(NumberUtils.parseInt(result.get()));
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

        controller.getCheckForUpdatesMenuItem().setOnAction((e) -> CheckForUpdate.apply());
        MainWindowManager.getInstance().changedProperty().addListener((c, o, n) -> controller.getCheckForUpdatesMenuItem().disableProperty().set(MainWindowManager.getInstance().size() > 1
                || (MainWindowManager.getInstance().size() == 1 && !MainWindowManager.getInstance().getMainWindow(0).isEmpty())));


        controller.getAnchorPane().getChildren().remove(controller.getInfoTextArea());
        controller.getAnchorPane().getChildren().add(controller.getInfoTextArea());
        AnchorPane.setTopAnchor(controller.getInfoTextArea(), 100.0);
        AnchorPane.setLeftAnchor(controller.getInfoTextArea(), 50.0);
        controller.getInfoTextArea().visibleProperty().bind(Bindings.size(window.getMainTabPane().getTabs()).isEqualTo(0));

        controller.getUseDarkThemeCheckMenuItem().selectedProperty().bindBidirectional(MainWindowManager.useDarkThemeProperty());
        controller.getUseDarkThemeCheckMenuItem().setSelected(MainWindowManager.isUseDarkTheme());
    }
}
