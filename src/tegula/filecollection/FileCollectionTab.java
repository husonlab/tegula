/*
 * FileCollectionTab.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.filecollection;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.Printable;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.FileUtils;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.OrbifoldGroupName;
import tegula.dbcollection.ICollectionTab;
import tegula.dbcollection.TilingsPane;
import tegula.main.MainWindow;
import tegula.main.TilingStyle;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * db collection tab
 * Daniel Huson, 12.2019
 */
public class FileCollectionTab extends Tab implements ICollectionTab, Closeable, Printable, IFileBased {
    private final FileCollectionTabController controller;
    private final Parent root;

    private final Map<Integer, Pane> pageCache = new HashMap<>();

    private final FileCollection fileCollection;

    private final TilingStyle tilingStyle;

    private final BooleanProperty showLabels = new SimpleBooleanProperty(true);

    private final BooleanProperty colorPreview = new SimpleBooleanProperty(false);

    private final Function<DSymbol, String> labelGetter;

    public static final Font font = new Font("Arial", 12);

    private final MainWindow mainWindow;

    private final ObjectProperty<Node> printable = new SimpleObjectProperty<>(null);

    private final AMultipleSelectionModel<DSymbol> selectionModel = new AMultipleSelectionModel<>();

    /**
     * setup collection tab
     *
     * @param mainWindow
     * @param fileCollection
     */
    public FileCollectionTab(MainWindow mainWindow, FileCollection fileCollection) {
		this.mainWindow = mainWindow;
		this.fileCollection = fileCollection;

		setText(FileUtils.getFileNameWithoutPath(fileCollection.getFileName()));
		fileCollection.fileNameProperty().addListener((c, o, n) -> setText(FileUtils.getFileNameWithoutPath(n)));

		final ExtendedFXMLLoader<FileCollectionTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
		root = extendedFXMLLoader.getRoot();
		controller = extendedFXMLLoader.getController();

		setContent(root);

		tilingStyle = new TilingStyle();
		tilingStyle.setShowEdges(true);
		tilingStyle.setBandWidth(4);
        tilingStyle.setBandCapFineness(0);
        tilingStyle.setShowFaces(false);
        tilingStyle.setTileOpacity(0.8);
        tilingStyle.setShowVertices(false);
        tilingStyle.setBandColor(Color.BLACK);
        tilingStyle.setBackgroundColor(Color.GHOSTWHITE);
        tilingStyle.setTileColorsScheme("Alhambra");
        tilingStyle.setSphericalUsePointLight(false);

        colorPreview.addListener((c, o, n) -> {
            tilingStyle.setShowFaces(n);
            processOpenFile(controller.getPagination().getCurrentPageIndex());
        });

        labelGetter = (ds) -> String.format("%d. n:%d t:%d e:%d v:%d g:%s%s", ds.getNr1(), ds.size(),
                ds.countOrbits(0, 1), ds.countOrbits(0, 2), ds.countOrbits(1, 2), OrbifoldGroupName.getGroupName(ds),
                (DSymbolAlgorithms.isMaximalSymmetry(ds) ? " max" : ""));

        FileCollectionControlBindings.setup(this);

        controller.getPagination().pageCountProperty().bind(fileCollection.totalCountProperty().subtract(1).divide(fileCollection.pageSizeProperty()).add(1));

        ProgramExecutorService.getInstance().submit(() -> processOpenFile(0));

        selectionModel.getSelectedItems().addListener((ListChangeListener<DSymbol>) (e) -> {
            final TilingsPane tilingsPane = (TilingsPane) getPrintable();
            if (tilingsPane != null) {
                while (e.next()) {
                    if (e.getAddedSize() > 0) {
                        tilingsPane.select(e.getAddedSubList(), true);
                    }
                    if (e.getRemovedSize() > 0) {
                        tilingsPane.select(e.getRemoved(), false);
                    }
                }
            }
        });
    }

    private void processOpenFile(int pageNumber) {
        try {
			final ArrayList<String> lines = FileUtils.getLinesFromFile(fileCollection.getFileName());

            Platform.runLater(() -> {
                fileCollection.setLines(lines);
                pageCache.clear();

                final Pagination pagination = controller.getPagination();
                updatePageSize();

                pagination.setPageFactory((page) -> {
                    Pane pane = pageCache.get(page);
                    if (pane == null || pane.getUserData() instanceof Integer && (Integer) pane.getUserData() != fileCollection.getNumberOfDSymbolsOnPage(page)) {
                        final TilingsPane paneNew = new TilingsPane();
                        pane = paneNew;
                        ProgramExecutorService.getInstance().submit(() -> {
                            final Collection<DSymbol> dSymbols = fileCollection.getPageOfDSymbols(page);
                            Platform.runLater(() -> selectionModel.setItems(dSymbols));
                            Platform.runLater(() -> paneNew.addTilings(dSymbols, FileCollectionTab.this, controller.getSizeSlider()));
                        });
                        pageCache.put(page, pane);
                    }
                    printable.set(pane);
                    updatePageSize();

                    return pane;
                });
                pagination.setCurrentPageIndex(pageNumber);
            });
        } catch (IOException e) {
            Basic.caught(e);
            NotificationManager.showError("Open file '" + fileCollection.getFileName() + "' failed: " + e.getMessage());
        }
    }

    public void close() {
        fileCollection.close();
    }

    @Override
    public Node getPrintable() {
        return printable.get();
    }

    public static Font getFont() {
        return font;
    }

    @Override
    public String getFileName() {
        return fileCollection.getFileName();
    }

    @Override
    public StringProperty fileNameProperty() {
        return fileCollection.fileNameProperty();
    }

    @Override
    public void setFileName(String fileName) {
        fileCollection.setFileName(fileName);
    }

    @Override
    public String getTitle() {
        return fileCollection.getTitle();
    }

    public FileCollectionTabController getController() {
        return controller;
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }

    @Override
    public boolean isShowLabels() {
        return showLabels.get();
    }

    @Override
    public BooleanProperty showLabelsProperty() {
        return showLabels;
    }

    public boolean isColorPreview() {
        return colorPreview.get();
    }

    @Override
    public BooleanProperty colorPreviewProperty() {
        return colorPreview;
    }

    public void setColorPreview(boolean colorPreview) {
        this.colorPreview.set(colorPreview);
    }

    @Override
    public Function<DSymbol, String> getLabelGetter() {
        return labelGetter;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public FileCollection getFileCollection() {
        return fileCollection;
    }

    @Override
    public AMultipleSelectionModel<DSymbol> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public void setShowLabels(boolean show) {
        showLabels.set(show);
    }


    public Parent getRoot() {
        return root;
    }

    @Override
    public void updatePageSize() {
        final double width = controller.getPagination().getWidth();
        final double height = controller.getPagination().getHeight();

        final int cols = Math.max(1, (int) ((width - 20) / (controller.getSizeSlider().getValue() + 20)));
        final int rows = Math.max(1, (int) ((height - 80) / (controller.getSizeSlider().getValue() + 30)));

        final int pageSize = rows * cols;

        if (pageSize > 0 && pageSize != fileCollection.getPageSize()) {
            fileCollection.setPageSize(pageSize);
        }
    }

    @Override
    public void gotoPage(int page) {
        page = Math.max(1, Math.min(getNumberOfPages(), page));
        controller.getPagination().setCurrentPageIndex(page - 1);

    }

    @Override
    public int getNumberOfPages() {
        return getFileCollection().getNumberOfPages();
    }

    public void changePreviewSize(boolean larger) {
        if (larger) {
            if (1.1 * getController().getSizeSlider().getValue() < getController().getSizeSlider().getMax()) {
                getController().getSizeSlider().setValue(1.1 * getController().getSizeSlider().getValue());
                updatePageSize();
            }
        } else {
            if (1 / 1.1 * getController().getSizeSlider().getValue() >= getController().getSizeSlider().getMin()) {
                getController().getSizeSlider().setValue(1 / 1.1 * getController().getSizeSlider().getValue());
                updatePageSize();
            }
        }
    }
}

