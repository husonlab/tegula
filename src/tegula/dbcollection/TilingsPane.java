/*
 * TilingsPane.java Copyright (C) 2019. Daniel H. Huson
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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jloda.fx.util.ProgramExecutorService;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.tilingeditor.TilingEditorTab;
import tegula.tilingpane.TilingPane;

import java.util.ArrayList;

/**
 * pane containing all given tilings
 * Daniel Huson, 10.2019
 */
public class TilingsPane extends FlowPane {

    /**
     * construct pane and populate with tilings
     *
     * @param dSymbols
     * @param dbCollectionTab
     */
    public TilingsPane(ArrayList<DSymbol> dSymbols, DBCollectionTab dbCollectionTab) {
        for (DSymbol dSymbol : dSymbols) {
            getChildren().add(setupTiling(dSymbol, dbCollectionTab));
        }

        setHgap(20);
        setVgap(20);
        setPadding(new Insets(20, 20, 20, 20));

        setUserData(dSymbols.size());

        setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
    }

    /**
     * sets up the tiling
     *
     * @return node producer
     */
    private Node setupTiling(DSymbol dSymbol, DBCollectionTab dbCollectionTab) {
        final Label label = new Label(dbCollectionTab.getLabelGetter().apply(dSymbol));
        label.setFont(DBCollectionTab.getFont());

        final Label shortLabel = new Label("");
        shortLabel.setFont(DBCollectionTab.getFont());

        final DBCollectionTabController controller = dbCollectionTab.getController();

        dbCollectionTab.showLabelsProperty().addListener((c, o, n) -> {
            if (n) {
                label.setTextFill(Color.BLACK);
                shortLabel.setTextFill(Color.BLACK);
            } else {
                label.setTextFill(Color.WHITE);
                shortLabel.setTextFill(Color.WHITE);
            }
        });

        final double size = controller.getSizeSlider().getValue();

        final Rectangle rectangle = new Rectangle();
        rectangle.setStroke(Color.LIGHTGRAY);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setWidth(size);
        rectangle.setHeight(size);
        rectangle.widthProperty().bind(controller.getSizeSlider().valueProperty());
        rectangle.heightProperty().bind(controller.getSizeSlider().valueProperty());
        final VBox vBox = new VBox(rectangle, size < 150 ? shortLabel : label);

        ProgramExecutorService.getInstance().submit(() -> {
            final TilingPane tilingPane = new TilingPane(dSymbol, dbCollectionTab.getTilingStyle(), true, false);
            tilingPane.getTilingStyle().setBendAnEdge(!DSymbolAlgorithms.isMaximalSymmetry(dSymbol));
            tilingPane.setPrefWidth(0.5 * controller.getSizeSlider().getMax());
            tilingPane.setPrefHeight(0.5 * controller.getSizeSlider().getMax());
            new Scene(tilingPane);

            Platform.runLater(() -> {
                //simpleTilingPane.getSimpleTiling().getTilingStyle().setShowBackEdges(simpleTilingPane.getSimpleTiling().getTiling().getGeometry()== Geometry.Spherical);
                tilingPane.update();

                final ImageView imageView = new ImageView(tilingPane.snapshot(null, null));
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(controller.getSizeSlider().getValue());
                imageView.fitWidthProperty().bind(controller.getSizeSlider().valueProperty());
                imageView.fitWidthProperty().addListener((c, o, n) -> {
                    if (n.doubleValue() < 150)
                        vBox.getChildren().set(1, shortLabel);
                    else
                        vBox.getChildren().set(1, label);
                });
                vBox.getChildren().set(0, imageView);
            });
        });

        vBox.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                final TilingEditorTab editorTab = new TilingEditorTab(new DSymbol(dSymbol), "extracted -" + dSymbol.getNr1());
                dbCollectionTab.getMainWindow().getMainTabPane().getTabs().add(editorTab);
            }
        });
        return vBox;
    }
}
