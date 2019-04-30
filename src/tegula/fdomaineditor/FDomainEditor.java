/*
 * FDomainEditor.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.fdomaineditor;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import jloda.fx.undo.UndoManager;
import jloda.util.Single;
import tegula.tilingeditor.TilingEditorTab;
import tegula.tilingeditor.TilingEditorTabController;

/**
 * the fundamental domain editor
 * Daniel Huson, 4.2019
 */
public class FDomainEditor {
    private final TilingEditorTabController controller;

    private final double preferredWidthClosed;
    private double preferredWidthOpen;

    private final double preferredHeightClosed;
    private double preferredHeightOpen;

    private double mouseDownX;
    private double mouseDownY;

    private final FDomainPane fDomainPane;

    private final UndoManager undoManager;

    /**
     * constructor
     *
     * @param tilingEditorTab
     */
    public FDomainEditor(TilingEditorTab tilingEditorTab) {
        this.controller = tilingEditorTab.getController();
        this.undoManager = tilingEditorTab.getUndoManager();
        final AnchorPane anchorPane = controller.getFdomainAnchorPane();
        preferredWidthClosed = anchorPane.getPrefWidth();
        preferredHeightClosed = anchorPane.getPrefHeight();
        preferredWidthOpen = 2 * preferredWidthClosed;
        preferredHeightOpen = 2 * preferredHeightClosed;

        fDomainPane = new FDomainPane(tilingEditorTab.getTilingPane(), undoManager);

        tilingEditorTab.getTilingPane().lastUpdateProperty().addListener((e) -> fDomainPane.update());

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

        controller.getFundamentalDomainAccordion().expandedPaneProperty().addListener((c, o, n) -> {
            if (n == controller.getFundamentalDomainTitledPane()) {
                anchorPane.setPrefWidth(Math.min(preferredWidthOpen, controller.getMainPane().getWidth() - 50));
                anchorPane.setPrefHeight(Math.min(preferredHeightOpen, controller.getMainPane().getHeight() - 50));
                controller.getResizeButton().setVisible(true);
            } else {
                anchorPane.setPrefWidth(preferredWidthClosed);
                anchorPane.setPrefHeight(preferredHeightClosed);
                controller.getResizeButton().setVisible(false);
            }
        });

        final Single<Double> origWidth = new Single<>(Double.MIN_VALUE);
        controller.getFundamentalDomainAccordion().widthProperty().addListener((c, o, n) -> {
            if (origWidth.get() == Double.MIN_VALUE)
                origWidth.set(n.doubleValue());
            else
                resizeButton.setTranslateX(n.doubleValue() - origWidth.get());
        });
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }
}
