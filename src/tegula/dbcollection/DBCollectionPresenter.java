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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import jloda.util.Basic;
import tegula.core.dsymbols.SymmetryClass;

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

        dbCollectionTab.getDbCollection().countProperty().addListener((c, o, n) -> {
            if (n != null && n.intValue() == -1)
                dbCollectionTab.getRoot().setCursor(Cursor.WAIT);
            else
                dbCollectionTab.getRoot().setCursor(Cursor.DEFAULT);
        });

        dbCollectionTab.tabPaneProperty().addListener((c, o, n) -> {
            if (n != null) {
                controller.getPagination().setPrefWidth(dbCollectionTab.getTabPane().getWidth());
                controller.getPagination().setPrefHeight(dbCollectionTab.getTabPane().getHeight());

                controller.getPagination().prefWidthProperty().bind(n.widthProperty());
                controller.getPagination().prefHeightProperty().bind(n.heightProperty());
            }
        });

        controller.getComplexityCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(), 0);
            if (n != null)
                controller.getComplexityCBox().getItems().add(n);
        });

        controller.getNumberOfTilesCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(), 0);
            if (n != null)
                controller.getNumberOfTilesCBox().getItems().add(n);
        });
        controller.getNumberOfEdgesCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(), 0);
            if (n != null)
                controller.getNumberOfEdgesCBox().getItems().add(n);
        });
        controller.getNumberOfVerticesCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(), 0);
            if (n != null)
                controller.getNumberOfVerticesCBox().getItems().add(n);
        });

        final EventHandler<ActionEvent> onActionHandler = (e) -> dbCollectionTab.processDBSelect(setupSearch(), 0);

        controller.getEuclideanCheckButton().setOnAction(onActionHandler);
        controller.getSphericalCheckButton().setOnAction(onActionHandler);
        controller.getHyperbolicCheckButton().setOnAction(onActionHandler);

        controller.getNormalCheckBox().setOnAction(onActionHandler);
        controller.getSelfDualCheckBox().setOnAction(onActionHandler);

        controller.getMaximalSymmetricCheckBox().setOnAction(onActionHandler);
        controller.getOrientableCheckBox().setOnAction(onActionHandler);
        controller.getHasReflectionsCheckBox().setOnAction(onActionHandler);

        controller.getOrbifoldCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(), 0);
            if (n != null && !controller.getOrbifoldCBox().getItems().contains(n))
                controller.getOrbifoldCBox().getItems().add(n);
        });

        controller.getSymmetryClassCBox().getItems().add("All");

        controller.getSymmetryClassCBox().getItems().addAll(Basic.toStrings(SymmetryClass.values()));
        controller.getSymmetryClassCBox().setOnAction(onActionHandler);

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

        controller.getSizeSlider().setOnMouseReleased((e) -> dbCollectionTab.updatePageSize());
        dbCollectionTab.getMainWindow().getStage().widthProperty().addListener((c, o, n) -> dbCollectionTab.updatePageSize());
        dbCollectionTab.getMainWindow().getStage().heightProperty().addListener((c, o, n) -> dbCollectionTab.updatePageSize());
    }

    public String setupSearch() {
        final StringBuilder buf = new StringBuilder();

        if (addIntSelect("complexity", controller.getComplexityCBox().getValue()) != null) {
            buf.append(addIntSelect("complexity", controller.getComplexityCBox().getValue()));
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

        if (!controller.getEuclideanCheckButton().isSelected()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append("geometry != 'Euclidean'");
        }

        if (!controller.getSphericalCheckButton().isSelected()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append("geometry != 'Spherical'");
        }

        if (!controller.getHyperbolicCheckButton().isSelected()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append("geometry != 'Hyperbolic'");
        }

        final String groupString = controller.getOrbifoldCBox().getSelectionModel().getSelectedItem();
        if (groupString != null && groupString.length() > 0) {
            if (buf.length() > 0)
                buf.append(" and ");
            final String operator = getOperator(groupString, true);
            if (operator.equalsIgnoreCase("c"))
                buf.append(String.format(" instr(orbifold, '%s') > 0", getArgument(groupString, true)));
            else if (operator.equalsIgnoreCase("!c"))
                buf.append(String.format(" instr(orbifold, '%s') = 0", getArgument(groupString, true)));
            else
                buf.append(String.format(" orbifold %s '%s'", operator, getArgument(groupString, true)));
        }

        final String symmetryClass = controller.getSymmetryClassCBox().getSelectionModel().getSelectedItem();
        if (symmetryClass != null && !symmetryClass.equals("All")) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(String.format(" symmetry_class = '%s'", symmetryClass));
        }

        if (!controller.getNormalCheckBox().isIndeterminate()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(" normal = '").append(controller.getNormalCheckBox().isSelected()).append("'");
        }

        if (!controller.getSelfDualCheckBox().isIndeterminate()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(" self_dual = '").append(controller.getSelfDualCheckBox().isSelected()).append("'");
        }

        if (!controller.getMaximalSymmetricCheckBox().isIndeterminate()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(" maximal = '").append(controller.getMaximalSymmetricCheckBox().isSelected()).append("'");

        }

        if (!controller.getOrientableCheckBox().isIndeterminate()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(" orientable = '").append(controller.getOrientableCheckBox().isSelected()).append("'");

        }

        if (!controller.getHasReflectionsCheckBox().isIndeterminate()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(" fixed_point_free = '").append(!controller.getHasReflectionsCheckBox().isSelected()).append("'");
        }


        return buf.toString().replaceAll("\\s\\s", " ");
    }

    private static String addIntSelect(String label, String value) {
        if (value == null)
            return null;
        value = value.trim();
        if (value.length() == 0)
            return null;

        return String.format("%s %s %s", label, getOperator(value, false), getArgument(value, false));
    }

    private static String getOperator(String value, boolean allowC) {
        final String operatorChars = (allowC ? "c!=<>" : "!=<>");
        int pos = 0;
        while (pos < value.length()) {
            if (operatorChars.indexOf(value.charAt(pos)) == -1)
                break;
            pos++;
        }
        if (pos == 0)
            return "=";
        else
            return value.substring(0, pos).trim();
    }

    private static String getArgument(String value, boolean allowC) {
        final String operatorChars = (allowC ? "c!=<>" : "!=<>");
        int pos = 0;
        while (pos < value.length()) {
            if (operatorChars.indexOf(value.charAt(pos)) == -1)
                break;
            pos++;
        }
        if (pos == 0)
            return value.trim();
        else
            return value.substring(pos).trim();
    }


}
