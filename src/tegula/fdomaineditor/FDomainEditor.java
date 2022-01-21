/*
 * FDomainEditor.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.fdomaineditor;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import jloda.fx.undo.UndoManager;
import jloda.util.Single;
import tegula.main.TilingStyle;
import tegula.tilingeditor.TilingEditorTab;
import tegula.tilingeditor.TilingEditorTabController;
import tegula.undoable.ChangeDSymbolCommand;

/**
 * the fundamental domain editor
 * Daniel Huson, 4.2019
 */
public class FDomainEditor {
    private final double preferredWidthClosed;
    private double preferredWidthOpen;

    private final double preferredHeightClosed;
    private double preferredHeightOpen;

    private double mouseDownX;
    private double mouseDownY;

    private final FDomainPane fDomainPane;

    private final TilingStyle tilingStyle;

    private final UndoManager undoManager;

    /**
     * constructor
     *
     * @param tilingEditorTab
     */
    public FDomainEditor(TilingEditorTab tilingEditorTab) {
        this.tilingStyle = tilingEditorTab.getTilingStyle();
        final TilingEditorTabController controller = tilingEditorTab.getController();
        this.undoManager = tilingEditorTab.getUndoManager();
        final AnchorPane anchorPane = controller.getFdomainAnchorPane();
        preferredWidthClosed = anchorPane.getPrefWidth();
        preferredHeightClosed = anchorPane.getPrefHeight();
        preferredWidthOpen = 2 * preferredWidthClosed;
        preferredHeightOpen = 2 * preferredHeightClosed;

        fDomainPane = new FDomainPane(tilingEditorTab, undoManager);

        fDomainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> tilingEditorTab.getTabPane().requestFocus());

        tilingEditorTab.getTilingPane().lastWorldUpdateProperty().addListener((e) -> fDomainPane.update());

        AnchorPane.setTopAnchor(fDomainPane, 30.0);
        AnchorPane.setLeftAnchor(fDomainPane, 0.0);
        AnchorPane.setBottomAnchor(fDomainPane, 5.0);
        AnchorPane.setRightAnchor(fDomainPane, 0.0);

        anchorPane.getChildren().add(0, fDomainPane);

        final Button resizeButton = controller.getResizeButton();

        resizeButton.setOnMousePressed((e) -> {
            mouseDownX = e.getSceneX();
            mouseDownY = e.getSceneY();
        });
        resizeButton.setVisible(false);

        resizeButton.setOnMouseDragged((e) -> {
            final Point2D delta;
            if (e.isShiftDown()) { // maintain aspect ratio
                final double min = Math.min(e.getSceneX() - mouseDownX, mouseDownY - e.getSceneY());
                delta = new Point2D(min, min);
            } else
                delta = new Point2D(e.getSceneX() - mouseDownX, mouseDownY - e.getSceneY());

            final double newWidth = anchorPane.getPrefWidth() + delta.getX();
            final double newHeight = anchorPane.getPrefHeight() + delta.getY();
            if (delta.getX() != 0 && newWidth >= preferredWidthClosed) {
                anchorPane.setPrefWidth(newWidth);
                preferredWidthOpen = newWidth;
            }
            if (delta.getY() != 0 && newHeight >= preferredHeightClosed) {
                anchorPane.setPrefHeight(newHeight);
                preferredHeightOpen = newHeight;
            }
            mouseDownX = e.getSceneX();
            mouseDownY = e.getSceneY();
        });

        controller.getResetButton().setOnAction((e) -> {
            final Point2D[][] coordinates = getfDomainPane().getFDomain().getCoordinates();
            undoManager.doAndAdd(new ChangeDSymbolCommand("reset", getfDomainPane().getFDomain().getDSymbol(), getfDomainPane().getFDomain().getDSymbol(),
                    tilingEditorTab.getTilingPane()::computTiling, coordinates, tilingEditorTab.getTilingPane()::changeCoordinates));
        });
        controller.getResetButton().disableProperty().bind(undoManager.undoableProperty().not());


        controller.getFundamentalDomainTitledPane().expandedProperty().addListener((c, o, n) -> {
            if (n) {
                if (!anchorPane.getChildren().contains(fDomainPane))
                    anchorPane.getChildren().add(0, fDomainPane);
                anchorPane.setPrefWidth(Math.min(preferredWidthOpen, controller.getMainPane().getWidth() - 50));
                anchorPane.setPrefHeight(Math.min(preferredHeightOpen, controller.getMainPane().getHeight() - 50));
                controller.getResizeButton().setVisible(true);
            } else {
                anchorPane.getChildren().remove(fDomainPane);

                anchorPane.setPrefWidth(preferredWidthClosed);
                anchorPane.setPrefHeight(preferredHeightClosed);
                controller.getResizeButton().setVisible(false);
            }
        });

        if (false) {
            final Button doubleButton = new Button("2x");
            doubleButton.setOnAction((e) -> {
                anchorPane.setPrefWidth(2 * anchorPane.getWidth());
                anchorPane.setPrefHeight(2 * anchorPane.getHeight());
            });
            controller.getFdomainToolBar().getItems().add(doubleButton);

            final Button halfButton = new Button("1/2x");
            halfButton.setOnAction((e) -> {
                anchorPane.setPrefWidth(0.5 * anchorPane.getWidth());
                anchorPane.setPrefHeight(0.5 * anchorPane.getHeight());
            });
            controller.getFdomainToolBar().getItems().add(halfButton);
        }

        final Single<Double> origWidth = new Single<>(Double.MIN_VALUE);
        controller.getFundamentalDomainTitledPane().widthProperty().addListener((c, o, n) -> {
            if (origWidth.get() == Double.MIN_VALUE)
                origWidth.set(n.doubleValue());
            else
                resizeButton.setTranslateX(n.doubleValue() - origWidth.get());
        });
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public double getPreferredWidthOpen() {
        return preferredWidthOpen;
    }

    public double getPreferredHeightOpen() {
        return preferredHeightOpen;
    }

    public double getPreferredWidthClosed() {
        return preferredWidthClosed;
    }

    public double getPreferredHeightClosed() {
        return preferredHeightClosed;
    }

    public FDomainPane getfDomainPane() {
        return fDomainPane;
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }
}
