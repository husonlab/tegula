/*
 * DBCollectionTab.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.dbcollection;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.Printable;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.OrbifoldGroupName;
import tegula.main.MainWindow;
import tegula.main.TilingStyle;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * db collection tab
 * Daniel Huson, 10.2019
 */
public class DBCollectionTab extends Tab implements Closeable, Printable, IFileBased {
    private final DBCollectionTabController controller;
    private final Parent root;

    private final Map<Integer, Pane> pageCache = new HashMap<>();

    private final DBCollection dbCollection;

    private final TilingStyle tilingStyle;

    private final BooleanProperty showLabels = new SimpleBooleanProperty(true);

    private final Function<DSymbol, String> labelGetter;

    public static Font font = new Font("Arial", 12);

    private final MainWindow mainWindow;

    private final ObjectProperty<Node> printable = new SimpleObjectProperty<>(null);

    private final DBCollectionPresenter dbCollectionPresenter;

    /**
     * setup collection tab
     *
     * @param mainWindow
     * @param dbCollection
     */
    public DBCollectionTab(MainWindow mainWindow, DBCollection dbCollection) {
        this.mainWindow = mainWindow;
        this.dbCollection = dbCollection;

        setText(Basic.getFileNameWithoutPath(dbCollection.getFileName()));
        dbCollection.fileNameProperty().addListener((c, o, n) -> setText(Basic.getFileNameWithoutPath(n)));

        final ExtendedFXMLLoader<DBCollectionTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        setContent(root);

        this.tilingStyle = new TilingStyle();
        tilingStyle.setShowEdges(true);
        tilingStyle.setBandWidth(4);
        tilingStyle.setShowFaces(false);
        tilingStyle.setShowVertices(false);
        tilingStyle.setBandColor(Color.BLACK);
        tilingStyle.setBackgroundColor(Color.GHOSTWHITE);

        labelGetter = (ds) -> String.format("%d. n:%d t:%d e:%d v:%d g:%s%s", ds.getNr1(), ds.size(),
                ds.countOrbits(0, 1), ds.countOrbits(0, 2), ds.countOrbits(1, 2), OrbifoldGroupName.getGroupName(ds),
                (DSymbolAlgorithms.isMaximalSymmetry(ds) ? " max" : ""));

        dbCollectionPresenter = new DBCollectionPresenter(this);
    }

    /**
     * process a DB select statement
     *
     * @param select
     */
    public void processDBSelect(String select, int currentPageIndex) {
        pageCache.clear();
        dbCollection.setDbSelect(select);

        final Pagination pagination = controller.getPagination();
        pagination.setPageFactory((page) -> {
            try {
                Pane pane = pageCache.get(page);
                if (pane == null || pane.getUserData() instanceof Integer && (Integer) pane.getUserData() != dbCollection.getNumberOfDSymbolsOnPage(page)) {
                    pane = new TilingsPane(dbCollection.getPageOfDSymbols(page), this);
                    pageCache.put(page, pane);
                }
                printable.set(pane);
                return pane;
            } catch (IOException | SQLException e) {
                NotificationManager.showError("Failed: " + e.getMessage());
            }
            return null;
        });
        pagination.setPageCount(Math.max(1, dbCollection.getNumberOfPages()));
        pagination.setCurrentPageIndex(currentPageIndex);
    }

    public void close() {
        try {
            dbCollection.close();
        } catch (IOException e) {
            Basic.caught(e);
        }
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

    public Function<DSymbol, String> getLabelGetter() {
        return labelGetter;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public DBCollection getDbCollection() {
        return dbCollection;
    }

    public int calculatePageSize(double previewSize) {
        final Pane pane = (Pane) getPrintable();
        if (pane != null) {
            double width = controller.getPagination().getWidth();
            double height = controller.getPagination().getHeight();

            int cols = Math.max(1, (int) ((width - 20) / (previewSize + 20)));
            int rows = Math.max(1, (int) ((height - 80) / (previewSize + 30)));

            return rows * cols;
        } else
            return 20;
    }
}

