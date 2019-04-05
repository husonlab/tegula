/*
 *  Copyright (C) 2018. Daniel H. Huson
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jloda.fx.control.SplittableTabPane;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.window.IMainWindow;

import java.io.File;

/**
 * the main window
 * Daniel Huson, 4.2019
 */
public class NextWindow implements IMainWindow {
    private Stage stage;
    private final NextWindowController controller;
    private final Parent root;
    private final Pane statusPane;

    private final SplittableTabPane mainTabPane;

    private final NextDocument document;

    private final TreeView<FileBrowser.FileNode> fileTreeView;

    private final ObservableMap<File, CollectionTab> file2CollectionTab = FXCollections.observableHashMap();

    public NextWindow() {
        document = new NextDocument();

        final ExtendedFXMLLoader<NextWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        statusPane = controller.getStatusBar();
        fileTreeView = controller.getTreeView();

        mainTabPane = new SplittableTabPane();
        mainTabPane.prefWidthProperty().bind(controller.getBorderPane().widthProperty());
        mainTabPane.prefHeightProperty().bind(controller.getBorderPane().heightProperty());

        controller.getCenterPane().getChildren().add(mainTabPane);

        FileBrowser.setup(new File("input"), TilingCollection.getExtensionFilter(), fileTreeView);
        fileTreeView.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                final TreeItem<FileBrowser.FileNode> item = fileTreeView.getSelectionModel().getSelectedItem();
                final File file = item.getValue().getFile();
                if (file.isFile()) {
                    TilingCollection tilingCollection = getDocument().getFile2tilingCollection().get(file);
                    if (tilingCollection == null) {
                        tilingCollection = new TilingCollection(file.getPath());
                        getDocument().getFile2tilingCollection().put(file, tilingCollection);
                        tilingCollection.load(getStatusPane(), null);
                    }
                    if (file2CollectionTab.get(file) == null) {
                        final CollectionTab tab = new CollectionTab(tilingCollection);
                        tab.setOnClosed((f) -> {
                            getDocument().getFile2tilingCollection().remove(file);
                            file2CollectionTab.remove(file);
                            mainTabPane.getTabs().remove(tab);
                        });
                        file2CollectionTab.put(file, tab);
                        mainTabPane.getTabs().add(tab);
                    }
                    mainTabPane.getSelectionModel().select(file2CollectionTab.get(file));
                }
            }
        });

        controller.getBorderPane().widthProperty().addListener((c, o, n) -> {
            if (o.doubleValue() > 0 && n.doubleValue() > 0) {
                controller.getMainSplitPane().setDividerPositions
                        (controller.getMainSplitPane().getDividerPositions()[0] * o.doubleValue() / n.doubleValue());
            }
        });

    }

    /**
     * show this main window
     */
    public void show(Stage stage, double screenX, double screenY, double width, double height) {
        if (stage == null)
            stage = new Stage();
        this.stage = stage;

        final Scene scene = new Scene(root, 800, 800);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public IMainWindow createNew() {
        return new NextWindow();
    }

    public NextWindowController getController() {
        return controller;
    }

    public ObservableMap<File, CollectionTab> getFile2CollectionTab() {
        return file2CollectionTab;
    }

    public Pane getStatusPane() {
        return statusPane;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public TreeView getFileTreeView() {
        return fileTreeView;
    }

    public NextDocument getDocument() {
        return document;
    }

    @Override
    public void close() {
    }
}
