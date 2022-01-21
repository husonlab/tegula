/*
 * SelectionSupport.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.tilingeditor;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import jloda.fx.control.AnotherMultipleSelectionModel;
import jloda.util.NumberUtils;
import jloda.util.Pair;
import jloda.util.StringUtils;
import tegula.core.dsymbols.DSymbol;
import tegula.main.TilingStyle;

import java.util.function.Consumer;

/**
 * sets up selection support by adding
 */
public class SelectionSupport {
    public static final PhongMaterial selectedMeshMaterial = new PhongMaterial(Color.RED);

    public static void setupSelection(DSymbol dSymbol, AnotherMultipleSelectionModel<Integer> nodeSelection,
                                      AnotherMultipleSelectionModel<Integer> edgeSelection, AnotherMultipleSelectionModel<Integer> tileSelection) {
        nodeSelection.setItems(allNumbers(1, dSymbol.countOrbits(1, 2)));
        edgeSelection.setItems(allNumbers(1, dSymbol.countOrbits(0, 2)));
        tileSelection.setItems(allNumbers(1, dSymbol.countOrbits(0, 1)));
    }

    public static void setupListeners(Group group, AnotherMultipleSelectionModel<Integer> vertexSelection,
                                      AnotherMultipleSelectionModel<Integer> edgeSelection, AnotherMultipleSelectionModel<Integer> tileSelection) {

        final EventHandler<? super MouseEvent> mouseClickHandler = (e) -> {
            final Pair<Character, Integer> pair = getTypeAndId((Node) e.getSource());
            if (pair != null) {
                if (e.getClickCount() == 2) {
                    if (!e.isShiftDown()) {
                        vertexSelection.clearSelection();
                        edgeSelection.clearSelection();
                        tileSelection.clearSelection();
                    }
                    final Integer id = pair.getSecond();
                    switch (pair.getFirst()) {
                        case 't':
                            if (tileSelection.isSelected(id))
                                tileSelection.clearSelection(id);
                            else
                                tileSelection.select(id); // must be Integer, not int!
                            break;
                        case 'e':
                            if (edgeSelection.isSelected(id))
                                edgeSelection.clearSelection(id);
                            else
                                edgeSelection.select(id); // must be Integer, not int!
                            break;
                        case 'v':
                            if (vertexSelection.isSelected(id))
                                vertexSelection.clearSelection(id);
                            else
                                vertexSelection.select(id); // must be Integer, not int!
                            break;
                    }
                    e.consume();
                }
            }
        };

        visitAllNodes(group, (node) -> {
            final Pair<Character, Integer> pair = getTypeAndId(node);
            if (pair != null) {
                node.setOnMouseClicked(mouseClickHandler);
            }
        });
    }

    public static void highlightSelection(Group group, AnotherMultipleSelectionModel<Integer> vertexSelection,
                                          AnotherMultipleSelectionModel<Integer> edgeSelection, AnotherMultipleSelectionModel<Integer> tileSelection,
                                          final TilingStyle tilingStyle, Character type) {

        SelectionSupport.visitAllNodes(group, (node) -> {
            final Pair<Character, Integer> pair = getTypeAndId(node);
            if (pair != null) {
                final Integer id = pair.getSecond();


                final PhongMaterial material;
                final double scaleFactor;
                final DrawMode drawMode;

                if (type == null || pair.getFirst() == type) {
                    switch (pair.getFirst()) {
                        case 'v':
                            if (vertexSelection.getSelectedItems().contains(id)) {
                                scaleFactor = 1.5;
                                material = selectedMeshMaterial;
                                drawMode = DrawMode.LINE;
                            } else {
                                scaleFactor = 1.0;
                                material = new PhongMaterial(tilingStyle.getBandColor());
                                drawMode = DrawMode.FILL;
                            }
                            break;
                        case 'e':
                            if (edgeSelection.getSelectedItems().contains(id)) {
                                scaleFactor = 1.0;
                                material = selectedMeshMaterial;
                                drawMode = DrawMode.LINE;
                            } else {
                                scaleFactor = 1.0;
                                material = new PhongMaterial(tilingStyle.getBandColor());
                                drawMode = DrawMode.FILL;
                            }
                            break;
                        case 't':
                            if (tileSelection.getSelectedItems().contains(id)) {
                                scaleFactor = 1.0;
                                material = selectedMeshMaterial;
                                drawMode = DrawMode.LINE;
                            } else {
                                scaleFactor = 1.0;
                                material = new PhongMaterial(tilingStyle.getTileColor(id));
                                drawMode = DrawMode.FILL;
                            }
                            break;
                        default:
                            material = null;
                            scaleFactor = 1;
                            drawMode = DrawMode.FILL;
                            break;
                    }
                    if (material != null) {
                        node.setScaleX(scaleFactor);
                        node.setScaleY(scaleFactor);
                        node.setScaleZ(scaleFactor);
                        if (node instanceof MeshView) {
                            ((MeshView) node).setMaterial(material);
                            ((MeshView) node).setDrawMode(drawMode);
                        }
                    }
                }
            }
        });
    }

    public static void visitAllNodes(Node node, Consumer<Node> consumer) {
        consumer.accept(node);
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable())
                visitAllNodes(child, consumer);
        }
    }

    public static Pair<Character, Integer> getTypeAndId(Node node) {
        if (node.getId() != null) {
			final String[] tokens = StringUtils.split(node.getId(), '=');
			if (tokens.length == 2 && (tokens[0].equals("v") || tokens[0].equals("e") || tokens[0].equals("t")) && NumberUtils.isInteger(tokens[1]))
				return new Pair<>(tokens[0].charAt(0), NumberUtils.parseInt(tokens[1]));
		}
        return null;
    }

    private static Integer[] allNumbers(int start, int length) {
        final Integer[] array = new Integer[length];

        for (int i = 0; i < length; i++)
            array[i] = start + i;
        return array;
    }

}
