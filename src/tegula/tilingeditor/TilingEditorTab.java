/*
 * TilingEditorTab.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tilingeditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Basic;
import tegula.core.dsymbols.DSymbol;
import tegula.fdomaineditor.FDomainEditor;
import tegula.main.TilingStyle;
import tegula.single.SingleTilingPane;
import tegula.tiling.TilingBase;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.io.File;

/**
 * a tab that contains a single editable tiling
 * Daniel Huson, 4.2019
 */
public class TilingEditorTab extends Tab implements IFileBased, Closeable {
    private final StringProperty fileName = new SimpleStringProperty("Untitled");

    private final TilingEditorTabController controller;
    private final Node root;

    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final SingleTilingPane tilingPane;
    private final TilingStyle tilingStyle = new TilingStyle();

    private final UndoManager undoManager = new UndoManager();

    /**
     * constructor
     *
     * @param dSymbol
     * @param name
     */
    public TilingEditorTab(DSymbol dSymbol, String name) {
        setFileName(name);
        setText(getTitle());

        final ExtendedFXMLLoader<TilingEditorTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        setContent(root);

        tilingPane = new SingleTilingPane(dSymbol, tilingStyle);
        tilingPane.prefWidthProperty().bind(controller.getMainPane().widthProperty());

        final FDomainEditor fDomainEditor = new FDomainEditor(this);

        tilingPane.prefHeightProperty().bind(controller.getMainPane().prefHeightProperty());

        controller.getMainPane().getChildren().add(tilingPane);

        tabPaneProperty().addListener((c, o, n) -> {
            if (n != null) {
                controller.getBorderPane().prefWidthProperty().bind(n.widthProperty());
                controller.getBorderPane().prefHeightProperty().bind(n.heightProperty());
            } else {
                controller.getBorderPane().prefWidthProperty().unbind();
                controller.getBorderPane().prefHeightProperty().unbind();
            }
        });


        ControlBindings.setup(this);

        // reset the top node so it is drawn on top of the tiling:

        controller.getBorderPane().setTop(controller.getBorderPane().getTop());
        controller.getBorderPane().setBottom(controller.getBorderPane().getBottom());

        GroupEditingControls.setup(this);
        TileColorControls.setup(this);

        tilingPane.lastUpdateProperty().addListener((c, o, n) -> {
                    TileColorControls.setup(this);
                    GroupEditingControls.setup(this);
                    controller.getBorderPane().setTop(controller.getBorderPane().getTop());
                    controller.getBorderPane().setBottom(controller.getBorderPane().getBottom());
                }
        );
    }

    /**
     * constructor from a file
     *
     * @param file
     */
    public TilingEditorTab(File file) {
        this(null, file.getPath());
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty.set(dirty);
    }


    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getTitle() {
        return Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName.get()), "");
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }

    public SingleTilingPane getTilingPane() {
        return tilingPane;
    }

    public TilingBase getTiling() {
        return getTilingPane().getTiling();
    }

    public TilingEditorTabController getController() {
        return controller;
    }

    public void close() {
        if (getTilingPane().getMouseHandler() != null)
            getTilingPane().getMouseHandler().getAnimator().stop();
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }
}
