/*
 * DBCollectionTab.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.dbcollection;

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
import jloda.util.FileUtils;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.OrbifoldGroupName;
import tegula.main.MainWindow;
import tegula.main.TilingStyle;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * db collection tab
 * Daniel Huson, 10.2019
 */
public class DBCollectionTab extends Tab implements ICollectionTab, Closeable, Printable, IFileBased {
    private final DBCollectionTabController controller;
    private final Parent root;

    private final Map<Integer, Pane> pageCache = new HashMap<>();

    private final DBCollection dbCollection;

    private final TilingStyle tilingStyle;

    private final BooleanProperty showLabels = new SimpleBooleanProperty(true);
    private final BooleanProperty colorPreview = new SimpleBooleanProperty(false);

    private final StringProperty previousQuery = new SimpleStringProperty("");

    private final Function<DSymbol, String> labelGetter;

    public static final Font font = new Font("Arial", 12);

    private final MainWindow mainWindow;

    private final ObjectProperty<Node> printable = new SimpleObjectProperty<>(null);

    private final AMultipleSelectionModel<DSymbol> selectionModel = new AMultipleSelectionModel<>();

    /**
     * setup collection tab
     *
	 */
    public DBCollectionTab(MainWindow mainWindow, DBCollection dbCollection) {
		this.mainWindow = mainWindow;
		this.dbCollection = dbCollection;

		setText(FileUtils.getFileNameWithoutPath(dbCollection.getFileName()));
		dbCollection.fileNameProperty().addListener((c, o, n) -> setText(FileUtils.getFileNameWithoutPath(n)));

		final ExtendedFXMLLoader<DBCollectionTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
		root = extendedFXMLLoader.getRoot();
		controller = extendedFXMLLoader.getController();

		setContent(root);

		this.tilingStyle = new TilingStyle();
		tilingStyle.setShowEdges(true);
		tilingStyle.setBandCapFineness(0);
        tilingStyle.setBandWidth(4);
        tilingStyle.setShowFaces(false);
        tilingStyle.setTileOpacity(0.8);
        tilingStyle.setShowVertices(false);
        tilingStyle.setBandColor(Color.BLACK);
        tilingStyle.setBackgroundColor(Color.GHOSTWHITE);
        tilingStyle.setTileColorsScheme("Alhambra");
        tilingStyle.setSphericalUsePointLight(false);

        colorPreview.addListener((c, o, n) -> {
            tilingStyle.setShowFaces(n);
            pageCache.clear();
            processDBSelect(previousQuery.get(), controller.getPagination().getCurrentPageIndex());
        });

        labelGetter = (ds) -> String.format("%d. n:%d t:%d e:%d v:%d g:%s", ds.getNr1(), ds.size(),
                ds.countOrbits(0, 1), ds.countOrbits(0, 2), ds.countOrbits(1, 2), OrbifoldGroupName.getGroupName(ds));

        DBCollectionControlBindings.setup(this);

        controller.getPagination().pageCountProperty().bind(dbCollection.countProperty().divide(dbCollection.pageSizeProperty()).add(1));

        Platform.runLater(() -> {
            dbCollection.setDbSelect("complexity >0");
            updatePageSize();
        });

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

    /**
     * process a DB select statement
     *
	 */
    public void processDBSelect(String select, int currentPageIndex) {
        previousQuery.set(select);

        pageCache.clear();
        dbCollection.setDbSelect(select);

        updatePageSize();

        final Pagination pagination = controller.getPagination();
        pagination.setPageFactory(page -> {
            Pane pane = pageCache.get(page);
            if (pane == null || pane.getUserData() instanceof Integer && (Integer) pane.getUserData() != dbCollection.getNumberOfDSymbolsOnPage(page)) {
                final TilingsPane paneNew = new TilingsPane();
                pane = paneNew;
                ProgramExecutorService.getInstance().submit(() -> {
                    final ArrayList<DSymbol> dSymbols;
                    try {
                        dSymbols = dbCollection.getPageOfDSymbols(page);
                        Platform.runLater(() -> selectionModel.setItems(dSymbols));
                        Platform.runLater(() -> paneNew.addTilings(dSymbols, DBCollectionTab.this, controller.getSizeSlider()));
                    } catch (IOException | SQLException e) {
                        NotificationManager.showError("Failed: " + e.getMessage());
                    }
                });
                pageCache.put(page, pane);
            }
            printable.set(pane);
            return pane;
        });
        pagination.setCurrentPageIndex(currentPageIndex);
    }

    public void close() {
        dbCollection.close();
    }

    public Node getPrintable() {
        return printable.get();
    }

    public static Font getFont() {
        return font;
    }

    @Override
    public String getFileName() {
        return dbCollection.getFileName();
    }

    @Override
    public StringProperty fileNameProperty() {
        return dbCollection.fileNameProperty();
    }

    @Override
    public void setFileName(String fileName) {
        dbCollection.setFileName(fileName);
    }

    @Override
    public String getTitle() {
        return dbCollection.getTitle();
    }

    public DBCollectionTabController getController() {
        return controller;
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }

    public boolean isShowLabels() {
        return showLabels.get();
    }

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

    public Function<DSymbol, String> getLabelGetter() {
        return labelGetter;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public DBCollection getDbCollection() {
        return dbCollection;
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

        if (pageSize > 0 && pageSize != dbCollection.getPageSize()) {
            dbCollection.setPageSize(pageSize);
        }
    }


    @Override
    public void gotoPage(int page) {
        page = Math.max(1, Math.min(getNumberOfPages(), page));
        controller.getPagination().setCurrentPageIndex(page - 1);

    }

    @Override
    public int getNumberOfPages() {
        return getDbCollection().getNumberOfPages();
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

