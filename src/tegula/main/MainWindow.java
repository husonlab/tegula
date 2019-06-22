/*
 * MainWindow.java Copyright (C) 2019. Daniel H. Huson
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
import jloda.fx.util.MemoryUsage;
import jloda.fx.window.IMainWindow;
import jloda.util.FileOpenManager;
import tegula.tilingcollection.TilingCollectionTab;
import tegula.util.TilingFileFilter;

import java.io.File;
import java.util.Collections;

/**
 * the main window
 * Daniel Huson, 4.2019
 */
public class MainWindow implements IMainWindow {
    private Stage stage;
    private final MainWindowController controller;
    private final Parent root;
    private final Pane statusPane;

    private final SplittableTabPane mainTabPane;

    private final Document document;

    private final TreeView<FileBrowser.FileNode> fileTreeView;

    private final ObservableMap<File, TilingCollectionTab> file2CollectionTab = FXCollections.observableHashMap();

    public MainWindow() {
        document = new Document();

        final ExtendedFXMLLoader<MainWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        statusPane = controller.getStatusBar();
        fileTreeView = controller.getTreeView();

        mainTabPane = new SplittableTabPane();
        mainTabPane.prefWidthProperty().bind(controller.getAnchorPane().widthProperty());
        mainTabPane.prefHeightProperty().bind(controller.getAnchorPane().heightProperty());

        controller.getCenterPane().getChildren().add(mainTabPane);


        FileOpenManager.setExtensions(Collections.singletonList(TilingFileFilter.getInstance()));
        FileOpenManager.setFileOpener(new FileOpener());

        FileBrowser.setup(new File("input"), TilingFileFilter.getInstance(), fileTreeView);
        fileTreeView.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                final TreeItem<FileBrowser.FileNode> item = fileTreeView.getSelectionModel().getSelectedItem();
                final File file = item.getValue().getFile();
                if (file.isFile()) {
                    FileOpenManager.getFileOpener().accept(file.getPath());
                }
            }
        });
        fileTreeView.getRoot().setExpanded(true);

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (o instanceof TilingCollectionTab)
                ((TilingCollectionTab) o).precomputeSnapshotsProperty().set(false);
            if (n instanceof TilingCollectionTab)
                ((TilingCollectionTab) n).precomputeSnapshotsProperty().set(true);
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

        final Scene scene = new Scene(root, width, height);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.setX(screenX);
        stage.setY(screenY);

        ControlBindings.setup(this);

        final MemoryUsage memoryUsage = MemoryUsage.getInstance();
        controller.getMemoryUsageLabel().textProperty().bind(memoryUsage.memoryUsageStringProperty());

        stage.show();
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public IMainWindow createNew() {
        return new MainWindow();
    }

    public MainWindowController getController() {
        return controller;
    }

    public ObservableMap<File, TilingCollectionTab> getFile2CollectionTab() {
        return file2CollectionTab;
    }

    public Pane getStatusPane() {
        return statusPane;
    }

    public SplittableTabPane getMainTabPane() {
        return mainTabPane;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public TreeView getFileTreeView() {
        return fileTreeView;
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public void close() {
        stage.hide();
    }
}
