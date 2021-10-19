/*
 * TilingsPane.java Copyright (C) 2020. Daniel H. Huson
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
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.SelectionEffect;
import jloda.util.FileUtils;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.Geometry;
import tegula.main.TilingStyle;
import tegula.tilingeditor.TilingEditorTab;
import tegula.tilingpane.TilingPane;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * pane containing all given tilings
 * Daniel Huson, 10.2019
 */
public class TilingsPane extends FlowPane {
    private static final Object sync = new Object();
    private static ExecutorService executorService = null;

    /**
     * construct pane
     */
    public TilingsPane() {
        synchronized (sync) {
            if (executorService == null)
                executorService = Executors.newFixedThreadPool(ProgramExecutorService.getNumberOfCoresToUse());
        }
        setHgap(20);
        setVgap(20);
        setPadding(new Insets(20, 20, 20, 20));
        setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
    }

    /**
     * populate with tilings
     *
     * @param dSymbols
     * @param dbCollectionTab
     */
    public void addTilings(Collection<DSymbol> dSymbols, ICollectionTab dbCollectionTab, Slider sizeSlider) {
        for (DSymbol dSymbol : dSymbols) {
            getChildren().add(setupTiling(dSymbol, dbCollectionTab, sizeSlider));
        }
        setUserData(dSymbols.size());
    }

    /**
     * sets up the tiling
     *
     * @return node producer
     */
    private Node setupTiling(DSymbol dSymbol, ICollectionTab collectionTab, Slider sizeSlider) {
        final Label label = new Label(collectionTab.getLabelGetter().apply(dSymbol));
        label.setFont(DBCollectionTab.getFont());

        final Label shortLabel = new Label("");
        shortLabel.setFont(DBCollectionTab.getFont());

        collectionTab.showLabelsProperty().addListener((c, o, n) -> {
            if (n) {
                label.setTextFill(Color.BLACK);
                shortLabel.setTextFill(Color.BLACK);
            } else {
                label.setTextFill(Color.WHITE);
                shortLabel.setTextFill(Color.WHITE);
            }
        });

        final double size = sizeSlider.getValue();

        final Rectangle rectangle = new Rectangle();
        rectangle.setStroke(Color.LIGHTGRAY);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setWidth(size);
        rectangle.setHeight(size);
        rectangle.widthProperty().bind(sizeSlider.valueProperty());
        rectangle.heightProperty().bind(sizeSlider.valueProperty());
        final VBox vBox = new VBox(rectangle, size < 150 ? shortLabel : label);
        vBox.setUserData(dSymbol);

        executorService.submit(() -> {
            final TilingStyle tilingStyle = new TilingStyle(collectionTab.getTilingStyle());

            final TilingPane tilingPane = new TilingPane(dSymbol, tilingStyle, true, false);
            tilingPane.getTilingStyle().setBendAnEdge(!DSymbolAlgorithms.isMaximalSymmetry(dSymbol));
            tilingPane.getTilingStyle().setBandWidth(tilingPane.getGeometry() == Geometry.Spherical ? 4 : 8);
            tilingPane.setPrefWidth(0.5 * sizeSlider.getMax());
            tilingPane.setPrefHeight(0.5 * sizeSlider.getMax());
            new Scene(tilingPane);

            // need to wait a short while before making a snapshot (otherwise some hyperbolic tilings will not appear)
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                Platform.runLater(() -> {
                    final ImageView imageView = new ImageView(tilingPane.snapshot(null, null));
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(sizeSlider.getValue());
                    imageView.fitWidthProperty().bind(sizeSlider.valueProperty());
                    imageView.fitWidthProperty().addListener((c, o, n) -> {
                        if (n.doubleValue() < 150)
                            vBox.getChildren().set(1, shortLabel);
                        else
                            vBox.getChildren().set(1, label);
                    });
                    vBox.getChildren().set(0, imageView);
                });
            }, tilingPane.getGeometry() == Geometry.Hyperbolic ? 600 : 100, TimeUnit.MILLISECONDS);
        });

        final AMultipleSelectionModel<DSymbol> selectionModel = collectionTab.getSelectionModel();
        vBox.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 1) {
                if (!e.isShiftDown())
                    selectionModel.clearSelection();
                selectionModel.select((DSymbol) vBox.getUserData());
            } else if (e.getClickCount() == 2) {
				final TilingEditorTab editorTab = new TilingEditorTab(new DSymbol(dSymbol), FileUtils.replaceFileSuffix(FileUtils.getFileNameWithoutPath(collectionTab.getFileName()), "-" + dSymbol.getNr1()));
                collectionTab.getMainWindow().getMainTabPane().getTabs().add(editorTab);
            }
        });
        return vBox;
    }

    public void select(Collection<? extends DSymbol> which, boolean select) {
        for (Node node : getChildren()) {
            if (node.getUserData() instanceof DSymbol) {
                final DSymbol dSymbol = (DSymbol) node.getUserData();
                if (which.contains(dSymbol)) {
                    if (select)
                        node.setEffect(SelectionEffect.getInstance());
                    else
                        node.setEffect(null);
                }

            }
        }
    }
}
