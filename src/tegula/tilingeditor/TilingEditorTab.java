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

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import jloda.fx.control.AnotherMultipleSelectionModel;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.Printable;
import jloda.util.Basic;
import tegula.core.dsymbols.*;
import tegula.fdomaineditor.FDomainEditor;
import tegula.main.TilingStyle;
import tegula.tiling.TilingBase;
import tegula.tilingpane.TilingPane;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.io.File;
import java.util.Stack;

/**
 * a tab that contains a single editable tiling
 * Daniel Huson, 4.2019
 */
public class TilingEditorTab extends Tab implements IFileBased, Closeable, Printable {
    private final StringProperty fileName = new SimpleStringProperty("Untitled");

    private final TilingEditorTabController controller;
    private final Node root;

    private final TilingPane tilingPane;
    private final TilingStyle tilingStyle = new TilingStyle();

    private final ObjectProperty<Geometry> geometry = new SimpleObjectProperty<>(Geometry.Euclidean);
    private final BooleanProperty maximalTiling = new SimpleBooleanProperty();
    private final BooleanProperty orientableTiling = new SimpleBooleanProperty();
    private final BooleanProperty diskTiling = new SimpleBooleanProperty();
    private final StringProperty groupName = new SimpleStringProperty();
    private final StringProperty infoLine = new SimpleStringProperty("");

    private final BooleanProperty canDualizeTiling = new SimpleBooleanProperty(false);
    private final BooleanProperty canContractEdge = new SimpleBooleanProperty(false);
    private final BooleanProperty canTruncateVertex = new SimpleBooleanProperty(false);
    private final BooleanProperty canGlueTilesAroundEdge = new SimpleBooleanProperty(false);
    private final BooleanProperty canGlueTilesAroundVertex = new SimpleBooleanProperty(false);
    private final BooleanProperty canSplitTile = new SimpleBooleanProperty(false);

    private final AnotherMultipleSelectionModel<Integer> vertexSelection = new AnotherMultipleSelectionModel<>();
    private final AnotherMultipleSelectionModel<Integer> edgeSelection = new AnotherMultipleSelectionModel<>();
    private final AnotherMultipleSelectionModel<Integer> tileSelection = new AnotherMultipleSelectionModel<>();

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

        vertexSelection.setSelectionMode(SelectionMode.MULTIPLE);
        edgeSelection.setSelectionMode(SelectionMode.MULTIPLE);
        tileSelection.setSelectionMode(SelectionMode.MULTIPLE);

        final ExtendedFXMLLoader<TilingEditorTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        setContent(root);

        tilingPane = new TilingPane(dSymbol, tilingStyle);
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

        // reset the top and bottom node so they are drawn on top of the tiling:

        final Node top = controller.getBorderPane().getTop();
        if (top != null) {
            controller.getBorderPane().getChildren().remove(top);
            controller.getBorderPane().setTop(top);
        }
        final Node bottom = controller.getBorderPane().getBottom();
        if (bottom != null) {
            controller.getBorderPane().getChildren().remove(bottom);
            controller.getBorderPane().setBottom(bottom);
        }

        GroupEditingControls.setup(this);
        TileColorControls.setup(this);

        controller.getMainPane().widthProperty().addListener((c, o, n) -> {
            if (o.doubleValue() == 0)
                o = 800;

            tilingPane.setEuclideanWidth(tilingPane.getEuclideanWidth() * (n.doubleValue() / o.doubleValue()));
            if (getTiling().getGeometry() == Geometry.Euclidean)
                tilingPane.update();
        });

        controller.getMainPane().heightProperty().addListener((c, o, n) -> {
            if (o.doubleValue() == 0)
                o = 800;

            tilingPane.setEuclideanHeight(tilingPane.getEuclideanHeight() * (n.doubleValue() / o.doubleValue()));
            if (getTiling().getGeometry() == Geometry.Euclidean)
                tilingPane.update();
        });

        tilingPane.lastDSymbolUpdateProperty().addListener((e) -> {
            geometry.set(dSymbol.computeGeometry());
            maximalTiling.set(DSymbolAlgorithms.isMaximalSymmetry(getTiling().getDSymbol()));
            orientableTiling.set(getTiling().getDSymbol().computeOrientation() == 2);
            diskTiling.set(DSymbolAlgorithms.allTilesAreDisks(getTiling().getDSymbol()));
            groupName.setValue(OrbifoldGroupName.getGroupName(getTiling().getDSymbol()));
            infoLine.setValue(String.format("n:%d t:%d e:%d v:%d g:%s", getTiling().getDSymbol().size(), getTiling().getDSymbol().countOrbits(0, 1),
                    getTiling().getDSymbol().countOrbits(0, 2), getTiling().getDSymbol().countOrbits(1, 2),
                    getGroupName() + (isMaximalTiling() ? " max" : "") + (isOrientableTiling() ? " orient." : "") + (isDiskTiling() ? "" : " non-disks"))
                    + (DSymbolAlgorithms.isSimpleTiling(getTiling().getDSymbol()) ? " simple" : "")
                    + String.format(" (objects: %,d)", computeSize(tilingPane.getWorld())));

            canDualizeTiling.set(!Isomorphic.isomorphic(dSymbol, DSymbolAlgorithms.dualize(dSymbol)));
            SelectionSupport.setupSelection(getTiling().getDSymbol(), vertexSelection, edgeSelection, tileSelection);
            updateCanContractEdge();
            updateCanGlueTiles();
        });

        tilingPane.lastWorldUpdateProperty().addListener((e) -> {
            TileColorControls.setup(this);
            GroupEditingControls.setup(this);
            controller.getBorderPane().setTop(controller.getBorderPane().getTop());
            controller.getBorderPane().setBottom(controller.getBorderPane().getBottom());

            SelectionSupport.setupListeners(tilingPane.getWorld(), vertexSelection, edgeSelection, tileSelection);
            highlightSelections(null);
        });

        vertexSelection.getSelectedItems().addListener((InvalidationListener) (e) -> {
            highlightSelections('v');
            canTruncateVertex.set(vertexSelection.getSelectedItems().size() == 1);
            updateCanGlueTiles();
        });
        edgeSelection.getSelectedItems().addListener((InvalidationListener) (e) -> {
            highlightSelections('e');
            updateCanContractEdge();
            updateCanGlueTiles();
        });
        tileSelection.getSelectedItems().addListener((InvalidationListener) (e) -> highlightSelections('t'));

        tilingPane.setOnMouseClicked((e) -> {
            if (!e.isMetaDown() && e.getClickCount() == 2) {
                vertexSelection.clearSelection();
                edgeSelection.clearSelection();
                tileSelection.clearSelection();
            }
        });

        tilingPane.incrementLastDSymbolUpdate();
        tilingPane.incrementLastWorldUpdate();
    }

    public static int computeSize(Node node) {
        int count = 0;
        final Stack<Node> stack = new Stack<>();
        stack.push(node);
        while (stack.size() > 0) {
            node = stack.pop();
            count++;
            if (node instanceof Parent) {
                stack.addAll(((Parent) node).getChildrenUnmodifiable());
            }
        }
        return count;
    }

    /**
     * constructor from a file
     *
     * @param file
     */
    public TilingEditorTab(File file) {
        this(null, file.getPath());
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
        setText(getTitle());
    }

    public String getTitle() {
        return Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName.get()), "");
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }

    public TilingPane getTilingPane() {
        return tilingPane;
    }

    public TilingBase getTiling() {
        return getTilingPane().getTiling();
    }

    public TilingEditorTabController getController() {
        return controller;
    }

    private boolean closing = false;

    public void close() {
        if (!closing) {
            closing = true;
            if (getTilingPane().getMouseHandler() != null)
                getTilingPane().getMouseHandler().getAnimator().stop();
            if (getOnClosed() != null) {
                getOnClosed().handle(null);
            }
        }
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public Node getPrintable() {
        return controller.getMainPane();
    }

    public Geometry getGeometry() {
        return geometry.get();
    }

    public ReadOnlyObjectProperty<Geometry> geometryProperty() {
        return geometry;
    }

    public boolean isMaximalTiling() {
        return maximalTiling.get();
    }

    public ReadOnlyBooleanProperty maximalTilingProperty() {
        return maximalTiling;
    }

    public boolean isOrientableTiling() {
        return orientableTiling.get();
    }

    public ReadOnlyBooleanProperty orientableTilingProperty() {
        return orientableTiling;
    }

    public boolean isDiskTiling() {
        return diskTiling.get();
    }

    public BooleanProperty diskTilingProperty() {
        return diskTiling;
    }

    public String getGroupName() {
        return groupName.get();
    }

    public ReadOnlyStringProperty groupNameProperty() {
        return groupName;
    }

    public ReadOnlyStringProperty infoLineProperty() {
        return infoLine;
    }

    public ReadOnlyBooleanProperty canContractEdgeProperty() {
        return canContractEdge;
    }

    public void updateCanContractEdge() {
        if (edgeSelection.getSelectedItems().size() == 1) {
            int edge = edgeSelection.getSelectedItems().get(0);
            canContractEdge.set(ContractEdge.getContractEdge(edge, getTiling().getDSymbol()) != 0);
        } else
            canContractEdge.set(false);
    }

    public ReadOnlyBooleanProperty canTruncateVertexProperty() {
        return canTruncateVertex;
    }

    public BooleanProperty canDualizeTilingProperty() {
        return canDualizeTiling;
    }

    public BooleanProperty canGlueTilesAroundEdgeProperty() {
        return canGlueTilesAroundEdge;
    }

    public BooleanProperty canGlueTilesAroundVertexProperty() {
        return canGlueTilesAroundVertex;
    }

    public void updateCanGlueTiles() {
        if (edgeSelection.getSelectedItems().size() == 1 && vertexSelection.getSelectedItems().size() == 0) {
            canGlueTilesAroundEdge.set(GlueTilesAroundEdge.isApplicable(edgeSelection.getSelectedItems().get(0), getTiling().getDSymbol()));
            canGlueTilesAroundVertex.set(false);
        } else if (edgeSelection.getSelectedItems().size() == 0 && vertexSelection.getSelectedItems().size() == 1) {
            canGlueTilesAroundVertex.set(GlueTilesAroundVertex.isApplicable(vertexSelection.getSelectedItems().get(0), getTiling().getDSymbol()));
            canGlueTilesAroundEdge.set(false);
        } else {
            canGlueTilesAroundVertex.set(false);
            canGlueTilesAroundEdge.set(false);
        }
    }

    public BooleanProperty getCanSplitTileProperty() {
        return canSplitTile;
    }

    public void highlightSelections(Character type) {
        SelectionSupport.highlightSelection(tilingPane.getWorld(), vertexSelection, edgeSelection, tileSelection, tilingStyle, type);
    }

    public void selectAll(boolean select) {
        if (select) {
            vertexSelection.selectAll();
            edgeSelection.selectAll();
            tileSelection.selectAll();
        } else {
            vertexSelection.clearSelection();
            edgeSelection.clearSelection();
            tileSelection.clearSelection();
        }
    }

    public AnotherMultipleSelectionModel<Integer> getVertexSelection() {
        return vertexSelection;
    }

    public AnotherMultipleSelectionModel<Integer> getEdgeSelection() {
        return edgeSelection;
    }

    public AnotherMultipleSelectionModel<Integer> getTileSelection() {
        return tileSelection;
    }
}
