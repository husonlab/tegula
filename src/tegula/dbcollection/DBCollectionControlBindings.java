/*
 * DBCollectionControlBindings.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import jloda.util.StringUtils;
import tegula.core.dsymbols.SymmetryClass;

/**
 * presenter
 * Daniel Huson, 10.2019
 */
public class DBCollectionControlBindings {
    /**
     * constructor
     */
    public static void setup(DBCollectionTab dbCollectionTab) {
        final DBCollection dbCollection = dbCollectionTab.getDbCollection();
        final DBCollectionTabController controller = dbCollectionTab.getController();
        controller.getSelectTitledPane().setExpanded(false);

        dbCollectionTab.getDbCollection().countProperty().addListener((c, o, n) -> {
            if (n != null && n.intValue() == -1)
                dbCollectionTab.getRoot().setCursor(Cursor.WAIT);
            else
                dbCollectionTab.getRoot().setCursor(Cursor.DEFAULT);
        });

        dbCollectionTab.tabPaneProperty().addListener((c, o, n) -> {
            if (n != null) {
                controller.getPagination().prefWidthProperty().unbind();
                controller.getPagination().prefHeightProperty().unbind();

                controller.getPagination().setPrefWidth(dbCollectionTab.getTabPane().getWidth());
                controller.getPagination().setPrefHeight(dbCollectionTab.getTabPane().getHeight());

                controller.getPagination().prefWidthProperty().bind(n.widthProperty());
                controller.getPagination().prefHeightProperty().bind(n.heightProperty());
            }
        });

        controller.getComplexityCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(controller), 0);
            if (n != null)
                controller.getComplexityCBox().getItems().add(n);
        });

        controller.getNumberOfTilesCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(controller), 0);
            if (n != null)
                controller.getNumberOfTilesCBox().getItems().add(n);
        });
        controller.getNumberOfEdgesCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(controller), 0);
            if (n != null)
                controller.getNumberOfEdgesCBox().getItems().add(n);
        });
        controller.getNumberOfVerticesCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(controller), 0);
            if (n != null)
                controller.getNumberOfVerticesCBox().getItems().add(n);
        });

        final EventHandler<ActionEvent> onActionHandler = (e) -> dbCollectionTab.processDBSelect(setupSearch(controller), 0);

        controller.getEuclideanCheckButton().setOnAction(onActionHandler);
        controller.getSphericalCheckButton().setOnAction(onActionHandler);
        controller.getHyperbolicCheckButton().setOnAction(onActionHandler);

        controller.getNormalCheckBox().setOnAction(onActionHandler);
        controller.getSelfDualCheckBox().setOnAction(onActionHandler);

        controller.getMaximalSymmetricCheckBox().setOnAction(onActionHandler);

        controller.getTileFiguresCBox().setOnAction(onActionHandler);
        controller.getTileFiguresCBox().disableProperty().bind(dbCollection.dbVersionProperty().lessThan(0.2f)); // older versions contain errors

        controller.getTileDegreesCBox().setOnAction(onActionHandler);
        controller.getVertexDegreesCBox().setOnAction(onActionHandler);

        controller.getTileDegreesCBox().disableProperty().bind(dbCollection.dbVersionProperty().lessThan(0.1f));
        controller.getVertexDegreesCBox().disableProperty().bind(dbCollection.dbVersionProperty().lessThan(0.1f));

        controller.getVertexFiguresCBox().setOnAction(onActionHandler);
        controller.getVertexFiguresCBox().disableProperty().bind(dbCollection.dbVersionProperty().lessThan(0.2f));

        controller.getColorableCheckBox().setOnAction(onActionHandler);

        controller.getOrbifoldCBox().valueProperty().addListener((c, o, n) -> {
            dbCollectionTab.processDBSelect(setupSearch(controller), 0);
            if (n != null && !controller.getOrbifoldCBox().getItems().contains(n))
                controller.getOrbifoldCBox().getItems().add(n);
        });

        controller.getSymmetryClassCBox().getItems().add("All");

		controller.getSymmetryClassCBox().getItems().addAll(StringUtils.toStrings(SymmetryClass.values()));
		controller.getSymmetryClassCBox().setOnAction(onActionHandler);

        controller.getSearchCBox().setOnAction((c) -> dbCollectionTab.processDBSelect(controller.getSearchCBox().getValue(), 0));

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

    private static String setupSearch(DBCollectionTabController controller) {
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

        {
            final String input = controller.getOrbifoldCBox().getSelectionModel().getSelectedItem();
            if (input != null && input.length() > 0) {
                if (buf.length() > 0)
                    buf.append(" and ");
                final String operator = getOperator(input, true);
                if (operator.equalsIgnoreCase("c"))
                    buf.append(String.format(" instr(orbifold, '%s') > 0", getArgument(input, true)));
                else if (operator.equalsIgnoreCase("!c"))
                    buf.append(String.format(" instr(orbifold, '%s') = 0", getArgument(input, true)));
                else
                    buf.append(String.format(" orbifold %s '%s'", operator, getArgument(input, true)));
            }
        }

        {
            final String input = controller.getSymmetryClassCBox().getSelectionModel().getSelectedItem();
            if (input != null && !input.equals("All")) {
                if (buf.length() > 0)
                    buf.append(" and ");
                buf.append(String.format(" symmetry_class = '%s'", input));
            }
        }

        {
            String input = controller.getTileFiguresCBox().getSelectionModel().getSelectedItem();
            if (input != null) {
                input = input.trim();
                if (input.length() > 0) {
                    if (!input.contains("(") && !input.contains(")"))
                        input = "(" + input + ")";
                    input = input.replaceAll("[.,]", " ");
                    input = input.replaceAll("\\s+", " ");

                    if (!controller.getTileFiguresCBox().getItems().contains(input))
                        controller.getTileFiguresCBox().getItems().add(input);
                    if (buf.length() > 0)
                        buf.append(" and ");
                    final String operator = getOperator(input, true);
                    if (operator.equalsIgnoreCase("c"))
                        buf.append(String.format(" instr(tile_fig, '%s') > 0", getArgument(input, true)));
                    else if (operator.equalsIgnoreCase("!c"))
                        buf.append(String.format(" instr(tile_fig, '%s') = 0", getArgument(input, true)));
                    else
                        buf.append(String.format(" tile_fig %s '%s'", operator, getArgument(input, true)));
                }
            }
        }

        {
            String input = controller.getTileDegreesCBox().getSelectionModel().getSelectedItem();
            if (input != null) {
                input = input.trim();
                if (input.length() > 0) {
                    if (!controller.getTileDegreesCBox().getItems().contains(input))
                        controller.getTileDegreesCBox().getItems().add(input);
                    if (buf.length() > 0)
                        buf.append(" and ");
                    final String operator = getOperator(input, true);
                    if (operator.equalsIgnoreCase("c"))
                        buf.append(String.format(" instr(tile_deg, '%s') > 0", getArgument(input, true)));
                    else if (operator.equalsIgnoreCase("!c"))
                        buf.append(String.format(" instr(tile_deg, '%s') = 0", getArgument(input, true)));
                    else
                        buf.append(String.format(" tile_deg %s '%s'", operator, getArgument(input, true)));
                }
            }
        }

        {
            String input = controller.getVertexFiguresCBox().getSelectionModel().getSelectedItem();
            if (input != null) {
                input = input.trim();
                if (input.length() > 0) {
                    if (!input.contains("(") && !input.contains(")"))
                        input = "(" + input + ")";
                    input = input.replaceAll("[.,]", " ");
                    input = input.replaceAll("\\s+", " ");

                    if (!controller.getVertexFiguresCBox().getItems().contains(input))
                        controller.getVertexFiguresCBox().getItems().add(input);
                    if (buf.length() > 0)
                        buf.append(" and ");
                    final String operator = getOperator(input, true);
                    if (operator.equalsIgnoreCase("c"))
                        buf.append(String.format(" instr(vertex_fig, '%s') > 0", getArgument(input, true)));
                    else if (operator.equalsIgnoreCase("!c"))
                        buf.append(String.format(" instr(vertex_fig, '%s') = 0", getArgument(input, true)));
                    else
                        buf.append(String.format(" vertex_fig %s '%s'", operator, getArgument(input, true)));
                }
            }
        }

        {
            String input = controller.getVertexDegreesCBox().getSelectionModel().getSelectedItem();
            if (input != null) {
                input = input.trim();
                if (input.length() > 0) {
                    if (!controller.getVertexDegreesCBox().getItems().contains(input))
                        controller.getVertexDegreesCBox().getItems().add(input);
                    if (buf.length() > 0)
                        buf.append(" and ");
                    final String operator = getOperator(input, true);
                    if (operator.equalsIgnoreCase("c"))
                        buf.append(String.format(" instr(vertex_deg, '%s') > 0", getArgument(input, true)));
                    else if (operator.equalsIgnoreCase("!c"))
                        buf.append(String.format(" instr(vertex_deg, '%s') = 0", getArgument(input, true)));
                    else
                        buf.append(String.format(" vertex_deg %s '%s'", operator, getArgument(input, true)));
                }
            }
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

        if (!controller.getColorableCheckBox().isIndeterminate()) {
            if (buf.length() > 0)
                buf.append(" and ");
            buf.append(" colorable = '").append(controller.getColorableCheckBox().isSelected()).append("'");
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
