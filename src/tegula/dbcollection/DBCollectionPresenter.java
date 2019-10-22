/*
 * DBCollectionPresenter.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.application.Platform;

/**
 * presenter
 * Daniel Huson, 10.2019
 */
public class DBCollectionPresenter {
    private final DBCollectionTab dbCollectionTab;
    private final DBCollection dbCollection;
    private final DBCollectionTabController controller;

    /**
     * constructor
     *
     * @param dbCollectionTab
     */
    public DBCollectionPresenter(DBCollectionTab dbCollectionTab) {
        this.dbCollectionTab = dbCollectionTab;
        this.dbCollection = dbCollectionTab.getDbCollection();
        this.controller = dbCollectionTab.getController();

        controller.getSelectTitledPane().setExpanded(false);

        dbCollectionTab.tabPaneProperty().addListener((c, o, n) -> {
            if (n != null) {
                controller.getPagination().prefWidthProperty().bind(n.widthProperty());
                controller.getPagination().prefHeightProperty().bind(n.heightProperty());
            }
        });

        controller.getComplexityCBox().valueProperty().addListener((c, o, n) -> dbCollectionTab.processDBSelect(setupSearch(), 0));

        controller.getNumberOfTilesCBox().valueProperty().addListener((c, o, n) -> dbCollectionTab.processDBSelect(setupSearch(), 0));
        controller.getNumberOfEdgesCBox().valueProperty().addListener((c, o, n) -> dbCollectionTab.processDBSelect(setupSearch(), 0));
        controller.getNumberOfVerticesCBox().valueProperty().addListener((c, o, n) -> dbCollectionTab.processDBSelect(setupSearch(), 0));

        controller.getSearchCBox().setOnAction((c) -> {
            dbCollectionTab.processDBSelect(controller.getSearchCBox().getValue(), 0);
        });

        dbCollection.countProperty().addListener((c, o, n) -> controller.getCountLabel().setText(n == null ? "?" : String.format("Found: %,d", n.intValue())));

        dbCollection.dbSelectProperty().addListener((c, o, n) -> {
            if (n != null) {
                controller.getSearchCBox().setValue(n);
                if (!controller.getSearchCBox().getItems().contains(n)) {
                    controller.getSearchCBox().getItems().add(n);
                }
            }

        });

        controller.getSizeSlider().setOnMouseReleased((e) -> updatePageSize());
        dbCollectionTab.getMainWindow().getStage().widthProperty().addListener((c, o, n) -> updatePageSize());
        dbCollectionTab.getMainWindow().getStage().heightProperty().addListener((c, o, n) -> updatePageSize());

        Platform.runLater(this::updatePageSize);
    }

    public void updatePageSize() {
        final int pageSize = dbCollectionTab.calculatePageSize(controller.getSizeSlider().getValue());
        if (pageSize > 0 && pageSize != dbCollection.getPageSize()) {
            final int currentSymbol = controller.getPagination().getCurrentPageIndex() * dbCollection.getPageSize();

            System.err.println("Changing page size: " + dbCollection.getPageSize() + " -> " + pageSize);
            System.err.println("Current page: " + controller.getPagination().getCurrentPageIndex() + " -> " + (currentSymbol / pageSize));

            dbCollection.setPageSize(pageSize);
            dbCollectionTab.processDBSelect(dbCollection.getDbSelect(), currentSymbol / pageSize); // reload
        }
    }

    private String setupSearch() {
        final StringBuilder buf = new StringBuilder();

        if (addIntSelect("size", controller.getComplexityCBox().getValue()) != null) {
            buf.append(addIntSelect("size", controller.getComplexityCBox().getValue()));
        }

        if (addIntSelect("tiles", controller.getNumberOfTilesCBox().getValue()) != null) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(addIntSelect("tiles", controller.getNumberOfTilesCBox().getValue()));
        }

        if (addIntSelect("edges", controller.getNumberOfEdgesCBox().getValue()) != null) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(addIntSelect("edges", controller.getNumberOfEdgesCBox().getValue()));
        }

        if (addIntSelect("vertices", controller.getNumberOfVerticesCBox().getValue()) != null) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(addIntSelect("vertices", controller.getNumberOfVerticesCBox().getValue()));
        }

        return buf.toString().replaceAll("\\s\\s", " ");
    }

    private static String addIntSelect(String label, String value) {
        if (value == null)
            return null;
        value = value.trim();
        if (value.length() == 0)
            return null;
        if (!value.contains("="))
            return label + " == " + value;
        else
            return label + value;
    }
}
