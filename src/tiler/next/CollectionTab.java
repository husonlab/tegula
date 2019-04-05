/*
 *  Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.SelectionEffect;
import tiler.core.dsymbols.DSymbol;

/**
 * tab that show a collection of tilings
 * Daniel Huson, 4.2019
 */
public class CollectionTab extends Tab {
    private final CollectionTabController controller;
    private final Parent root;
    private final AMultipleSelectionModel<Node> selectionModel = new AMultipleSelectionModel<>();
    private final IntegerProperty currentNode = new SimpleIntegerProperty(-1);

    private final TilingCollection tilingCollection;

    /**
     * constructor
     */
    public CollectionTab(TilingCollection tilingCollection) {
        this.tilingCollection = tilingCollection;

        final ExtendedFXMLLoader<CollectionTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        final FlowPane flowPane = controller.getFlowPane();

        controller.getScrollPane().setFitToWidth(true);
        flowPane.prefHeightProperty().bind(controller.getScrollPane().heightProperty());

        controller.getFirstTilingButton().setOnAction((e) -> setCurrentNode(0));
        controller.getFirstTilingButton().disableProperty().bind(sizeProperty().isEqualTo(0));

        controller.getNextTilingButton().setOnAction((e) -> setCurrentNode(getCurrentNode() + 1));
        controller.getNextTilingButton().disableProperty().bind(currentNodeProperty().greaterThanOrEqualTo(sizeProperty()));

        controller.getPreviousTilingButton().setOnAction((e) -> setCurrentNode(getCurrentNode() - 1));
        controller.getPreviousTilingButton().disableProperty().bind(currentNodeProperty().lessThanOrEqualTo(0));

        controller.getLastTilingButton().setOnAction((e) -> setCurrentNode(sizeProperty().get() - 1));
        controller.getLastTilingButton().disableProperty().bind(currentNodeProperty().greaterThanOrEqualTo(sizeProperty()));

        setText(tilingCollection.getTitle());
        setTooltip(new Tooltip(tilingCollection.getFileName()));


        tilingCollection.getDSymbols().addListener((ListChangeListener<DSymbol>) (e) -> {
            while (e.next()) {
                for (DSymbol dSymbol : e.getAddedSubList()) {
                    final Rectangle rectangle = (new Rectangle());
                    rectangle.widthProperty().bind(controller.getSizeSlider().valueProperty());
                    rectangle.heightProperty().bind(controller.getSizeSlider().valueProperty());
                    rectangle.setFill(Color.WHITE);
                    rectangle.setStroke(Color.BLACK);
                    /*
                    WritableImage img = new WritableImage((int) rectangle.getWidth(), (int) rectangle.getHeight());
                    rectangle.snapshot(null,img);
                    final ImageView image=new ImageView(img);
                    */
                    final String text = String.format("%d.%d", dSymbol.getNr1(), dSymbol.getNr2());

                    final VBox vBox = new VBox(rectangle, new Label(text));
                    vBox.setAlignment(Pos.CENTER);
                    vBox.setOnMouseClicked((f) -> {
                        if (f.getClickCount() == 2) {
                            System.err.println("Open in editor");
                        }
                    });
                    vBox.setUserData(text);
                    flowPane.getChildren().add(vBox);
                    vBox.setOnMouseClicked((f) -> {
                        if (!f.isShiftDown())
                            selectionModel.clearSelection();
                        if (!selectionModel.getSelectedItems().contains(vBox))
                            selectionModel.select(vBox);
                        else
                            selectionModel.clearSelection(vBox);
                    });
                }
                selectionModel.setItems(flowPane.getChildren());
            }
        });
        setContent(root);

        currentNode.addListener((c, o, n) -> {
            if (n != null && n.intValue() >= 0 && n.intValue() < flowPane.getChildren().size()) {
                selectionModel.clearAndSelect(n.intValue());
            }
        });

        selectionModel.getSelectedItems().addListener((ListChangeListener<Node>) (e) -> {
            boolean first = false;
            while (e.next()) {
                for (Node node : e.getAddedSubList()) {
                    if (!first) {
                        if (node.getUserData() instanceof String) {
                            controller.getTilingNumberTextField().setText(node.getUserData().toString());
                            ensureVisible(controller.getScrollPane(), node);
                            first = true;
                        }
                    }
                    node.setEffect(SelectionEffect.getInstance());
                }
                for (Node node : e.getRemoved()) {
                    if (controller.getTilingNumberTextField().getText().equals(node.getUserData())) {
                        controller.getTilingNumberTextField().setText("");
                    }
                    node.setEffect(null);
                }
            }
        });

        controller.getTilingNumberTextField().setOnAction((e) -> {
            final String text = controller.getTilingNumberTextField().getText();
            int i = 0;
            for (Node node : flowPane.getChildren()) {
                if (text.equals((node.getUserData()))) {
                    setCurrentNode(i);
                    break;
                }
                i++;
            }
        });
    }

    public TilingCollection getTilingCollection() {
        return tilingCollection;
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return tilingCollection.sizeProperty();
    }

    public int getCurrentNode() {
        return currentNode.get();
    }

    public IntegerProperty currentNodeProperty() {
        return currentNode;
    }

    public void setCurrentNode(int currentNode) {
        this.currentNode.set(currentNode);
    }

    private static void ensureVisible(ScrollPane scrollPane, Node node) {
        double heightViewPort = scrollPane.getViewportBounds().getHeight();
        double heightScrollPane = scrollPane.getContent().getBoundsInLocal().getHeight();
        double y = node.getBoundsInParent().getMaxY();
        if (y < (heightViewPort / 2)) {
            scrollPane.setVvalue(0);
            // below 0 of scrollpane

        } else if ((y >= (heightViewPort / 2)) & (y <= (heightScrollPane - heightViewPort / 2))) {
            // between 0 and 1 of scrollpane
            scrollPane.setVvalue((y - (heightViewPort / 2)) / (heightScrollPane - heightViewPort));
        } else if (y >= (heightScrollPane - (heightViewPort / 2))) {
            // above 1 of scrollpane
            scrollPane.setVvalue(1);
        }
    }
}
