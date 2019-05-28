/*
 * FDomainPane.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import jloda.fx.shapes.NGonShape;
import jloda.fx.undo.UndoManager;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.geometry.Tools;
import tegula.main.TilingStyle;
import tegula.tilingeditor.TilingEditorTab;
import tegula.undoable.ChangeCoordinatesCommand;

import java.util.Arrays;
import java.util.BitSet;

/**
 * representation of a fundamental domain in 2D
 */
public class FDomainPane extends StackPane {
    private final ObjectProperty<FDomain> fDomain = new SimpleObjectProperty<>();
    private final ObjectProperty<DSymbol> dSymbol = new SimpleObjectProperty<>();
    private final TilingEditorTab tilingEditorTab;
    private final TilingStyle tilingStyle;

    private NGonShape[][] vertexHandles;
    private NGonShape[][] edgeHandles;
    private NGonShape[] chamberCenterHandles;

    private final Group decorations = new Group();

    private final DoubleProperty scaleFactor = new SimpleDoubleProperty(1);

    private final UndoManager undoManager;

    public FDomainPane(TilingEditorTab tilingEditorTab, UndoManager undoManager) {
        this.tilingEditorTab = tilingEditorTab;
        this.undoManager = undoManager;
        tilingStyle = tilingEditorTab.getTilingStyle();

        setFDomain(tilingEditorTab.getTiling().getfDomain());
        setDSymbol(tilingEditorTab.getTiling().getDSymbol());

        tilingEditorTab.getTilingPane().lastWorldUpdateProperty().addListener((e) -> {
            setFDomain(tilingEditorTab.getTiling().getfDomain());
            setDSymbol(tilingEditorTab.getTiling().getDSymbol());
        });

        widthProperty().addListener((c) -> update());
        heightProperty().addListener((c) -> update());
    }

    /**
     * update the view
     */
    public void update() {
        final FDomain fDomain = getFDomain();
        final DSymbol ds = getDSymbol();
        scaleFactor.set(Math.min((getWidth() - 30) / (fDomain.getBoundingBox().getMaxX() - fDomain.getBoundingBox().getMinX()), (getHeight() - 30) / (fDomain.getBoundingBox().getMaxY() - fDomain.getBoundingBox().getMinY())));

        getChildren().clear();

            final Group vertices = new Group();
            final Group edges = new Group();
            final Group polygons = new Group();

            vertexHandles = new NGonShape[ds.size() + 1][3];
            edgeHandles = new NGonShape[ds.size() + 1][3];
            chamberCenterHandles = new NGonShape[ds.size() + 1];

            /*
             * setup nodes, one per orbit:
             */
            for (int h = 0; h <= 2; h++) {
                final int i = DSymbol.i(h), j = DSymbol.j(h), k = h;
                {
                    final BitSet visited = new BitSet();
                    for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, j, a, visited)) {
                        ds.visitOrbit(i, j, a, b -> {
                            final NGonShape vertexHandle = new NGonShape(fDomain.getVertex(k, b).multiply(getScaleFactor()));
                            final NGonShape existingVertexHandle = find(vertexHandle, vertices.getChildren());
                            if (existingVertexHandle != null)
                                vertexHandles[b][k] = existingVertexHandle;
                            else {
                                Color color;
                                if (ds.getVij(i, j, b) > 1 || (k == 1 && b == ds.getS2(b))) { // is the center of a rotation
                                    color = Color.RED;
                                    vertexHandle.setN(ds.getVij(i, j, b));
                                    vertexHandle.setSize(10, 10);
                                } else if ((ds.getVij(i, j, b) == 1) && !(i == 0 && j == 2 && b == ds.getS2(b))
                                        && !(k == 2 && !getFDomain().isBoundaryEdge(0, b))) {
                                    setMouseHandler(undoManager, getScaleFactor(), vertexHandle, ReshapeUtilities.Type.Vertex, k, b);
                                    if (ds.isCycle(i, j, b)) // can be freely moved
                                        color = Color.GREEN;
                                    else
                                        color = Color.YELLOW; // we are having problems with these
                                    vertexHandle.setSize(10, 10);
                                } else {
                                    color = Color.GRAY; // not moveable
                                    vertexHandle.setN(4);
                                    vertexHandle.setSize(3, 3);
                                }
                                vertexHandle.setFill(color);
                                vertexHandle.setStroke(Color.GRAY);
                                vertexHandles[b][k] = vertexHandle;
                                vertices.getChildren().add(vertexHandle);
                            }
                        });
                    }
                }
                {
                    final BitSet visited = new BitSet();
                    for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, j, a, visited)) {
                        ds.visitOrbit(i, j, a, b -> {
                            final NGonShape edgeHandle = new NGonShape(fDomain.getEdgeCenter(k, b).multiply(getScaleFactor()));
                            final NGonShape existingEdgeHandle = find(edgeHandle, vertices.getChildren());
                            if (existingEdgeHandle != null)
                                edgeHandles[b][k] = existingEdgeHandle;
                            else {
                                Color color;
                                if (k == 2) { // center of an edge
                                    setMouseHandler(undoManager, getScaleFactor(), edgeHandle, ReshapeUtilities.Type.EdgeCenter, 2, b);
                                    if (b != ds.getS2(b))
                                        color = Color.GREEN; // freely moveable
                                    else
                                        color = Color.YELLOW; // restricted to line
                                    edgeHandle.setSize(10, 10);
                                } else {
                                    color = Color.GRAY;
                                    edgeHandle.setN(4);
                                    edgeHandle.setSize(3, 3);
                                }
                                edgeHandle.setFill(color);
                                edgeHandle.setStroke(Color.GRAY);
                                edgeHandles[b][k] = edgeHandle;
                                vertices.getChildren().add(edgeHandle);
                            }
                        });
                    }
                }
            }

            // setup chamber centers:
            for (int a = 1; a <= ds.size(); a++) {
                final NGonShape c = new NGonShape(fDomain.getChamberCenter(a).multiply(getScaleFactor()));
                chamberCenterHandles[a] = c;
                c.setN(32);
                c.setFill(Color.LIGHTGRAY);
                c.setStroke(Color.LIGHTGRAY);
                c.setSize(4, 4);
                vertices.getChildren().add(c);
            }

            // setup flag 2 tile number map
            final int[] a2tile = ds.computeOrbits(0, 1);

            // setup lines and polygons, and bind to nodes
            for (int a = 1; a <= ds.size(); a++) {
                final Shape[] vs = vertexHandles[a];
                final Shape[] es = edgeHandles[a];
                final Shape c = chamberCenterHandles[a];

                final Polygon polygon = new Polygon(vs[0].getLayoutX(), vs[0].getLayoutY(), es[2].getLayoutX(), es[2].getLayoutY(), vs[1].getLayoutX(), vs[1].getLayoutY(),
                        es[0].getLayoutX(), es[0].getLayoutY(), vs[2].getLayoutX(), vs[2].getLayoutY(), es[1].getLayoutX(), es[1].getLayoutY());

                if (tilingStyle.isShowFaces())
                    polygon.setFill(tilingStyle.getTileColorFullOpacity(a2tile[a]));
                else
                    polygon.setFill(Color.GAINSBORO);

                polygon.setUserData(a);
                polygons.getChildren().add(polygon);

                final ChangeListener<Number> listener = (d, o, n) -> polygon.getPoints().setAll(vs[0].getLayoutX(), vs[0].getLayoutY(), es[2].getLayoutX(), es[2].getLayoutY(), vs[1].getLayoutX(), vs[1].getLayoutY(),
                        es[0].getLayoutX(), es[0].getLayoutY(), vs[2].getLayoutX(), vs[2].getLayoutY(), es[1].getLayoutX(), es[1].getLayoutY());

                for (DoubleProperty property : Arrays.asList(vs[0].layoutXProperty(), vs[0].layoutYProperty(), es[2].layoutXProperty(), es[2].layoutYProperty(), vs[1].layoutXProperty(), vs[1].layoutYProperty(),
                        es[0].layoutXProperty(), es[0].layoutYProperty(), vs[2].layoutXProperty(), vs[2].layoutYProperty(), es[1].layoutXProperty(), es[1].layoutYProperty())) {
                    property.addListener(listener);
                }

                for (int i = 0; i <= 2; i++) {
                    int prev = (i == 0 ? 2 : i - 1);
                    int next = (i == 2 ? 0 : i + 1);

                    final Shape ePrev = es[prev];
                    final Shape v = vs[i];
                    final Shape eNext = es[next];

                    final Line vePrev = new Line();
                    vePrev.startXProperty().bind(v.layoutXProperty());
                    vePrev.startYProperty().bind(v.layoutYProperty());
                    vePrev.endXProperty().bind(ePrev.layoutXProperty());
                    vePrev.endYProperty().bind(ePrev.layoutYProperty());

                    if (i == 0) {
                        vePrev.setStroke(Color.BLACK);
                        vePrev.setStrokeWidth(3);
                        if (tilingStyle.isShowFaces() && fDomain.isBoundaryEdge(prev, a))
                            vePrev.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, tilingStyle.getTileColorFullOpacity(a2tile[ds.getS2(a)]), 6, 1, 0, 0));
                    } else {
                        vePrev.setStroke(Color.GRAY);
                        if (tilingStyle.isShowFaces() && fDomain.isBoundaryEdge(prev, a))
                            vePrev.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, tilingStyle.getTileColorFullOpacity(a2tile[a]), 6, 1, 0, 0));
                    }

                    final Line veNext = new Line();
                    veNext.startXProperty().bind(v.layoutXProperty());
                    veNext.startYProperty().bind(v.layoutYProperty());
                    veNext.endXProperty().bind(eNext.layoutXProperty());
                    veNext.endYProperty().bind(eNext.layoutYProperty());
                    if (i == 1) {
                        veNext.setStroke(Color.BLACK);
                        veNext.setStrokeWidth(3);
                        if (tilingStyle.isShowFaces() && fDomain.isBoundaryEdge(next, a))
                            veNext.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, tilingStyle.getTileColorFullOpacity(a2tile[ds.getS2(a)]), 6, 1, 0, 0));
                    } else {
                        veNext.setStroke(Color.GRAY);
                        if (tilingStyle.isShowFaces() && fDomain.isBoundaryEdge(next, a))
                            veNext.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, tilingStyle.getTileColorFullOpacity(a2tile[a]), 6, 1, 0, 0));
                    }

                    final Line vc = new Line();
                    vc.startXProperty().bind(v.layoutXProperty());
                    vc.startYProperty().bind(v.layoutYProperty());
                    vc.endXProperty().bind(c.layoutXProperty());
                    vc.endYProperty().bind(c.layoutYProperty());
                    vc.setStroke(Color.LIGHTGRAY);

                    final Line ePrevC = new Line();
                    ePrevC.startXProperty().bind(ePrev.layoutXProperty());
                    ePrevC.startYProperty().bind(ePrev.layoutYProperty());
                    ePrevC.endXProperty().bind(c.layoutXProperty());
                    ePrevC.endYProperty().bind(c.layoutYProperty());
                    ePrevC.setStroke(Color.LIGHTGRAY);

                    edges.getChildren().addAll(vePrev, veNext, vc, ePrevC);
                }
            }

        final Group all = new Group(polygons, edges, vertices, decorations);

            getChildren().setAll(all);
    }

    /**
     * can we find a vertex node with the same coordinates?
     *
     * @param vertexNode
     * @param list
     * @return node with same coordinates or null
     */
    private static NGonShape find(NGonShape vertexNode, ObservableList<Node> list) {
        for (Node node : list) {
            if (node instanceof NGonShape) {
                final NGonShape other = (NGonShape) node;

                if (Math.abs(vertexNode.getLayoutX() - other.getLayoutX()) < 0.000001 && Math.abs(vertexNode.getLayoutY() - other.getLayoutY()) < 0.000001)
                    return other;
            }
        }
        return null;
    }


    public FDomain getFDomain() {
        return fDomain.get();
    }

    private void setFDomain(FDomain fDomain) {
        this.fDomain.set(fDomain);
    }

    public DSymbol getDSymbol() {
        return dSymbol.get();
    }

    private void setDSymbol(DSymbol dSymbol) {
        this.dSymbol.set(dSymbol);
    }

    private Point2D d2(Point3D apt) {
        if (true) {
            Point2D point2D = Tools.map3Dto2D(getFDomain().getGeometry(), apt);
            System.err.println(point2D);
            return point2D;
        } else
            return new Point2D(apt.getX(), apt.getY());
    }

    private Point2D[][] oldCoordinates;
    private boolean moved = false;

    /**
     * set the mouse handler
     */
    private void setMouseHandler(UndoManager undoManager, double factor, Shape shape, ReshapeUtilities.Type type, int k, int a) {
        final ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>(new Point2D(0, 0));

        shape.setOnMousePressed((e) -> {
            mouseDown.set(new Point2D(e.getSceneX(), e.getSceneY()));
            oldCoordinates = getFDomain().getCoordinates();
            moved = false;
        });

        shape.setOnMouseDragged((e) -> {
            final double deltaX = e.getSceneX() - mouseDown.get().getX();
            final double deltaY = e.getSceneY() - mouseDown.get().getY();

            if (deltaX != 0 || deltaY != 0) {
                // Reset shape of fundamental domain
                final Point2D constraintsAdjustmentTranslationVector = ReshapeUtilities.resetShape(getFDomain(), deltaX/factor, deltaY/factor, type, k, a, factor);
                // Move handles along transVector
                double fac = 1;
                shape.setLayoutX(shape.getLayoutX() + fac* constraintsAdjustmentTranslationVector.getX());
                shape.setLayoutY(shape.getLayoutY() + fac* constraintsAdjustmentTranslationVector.getY());

                mouseDown.set(new Point2D(e.getSceneX() - deltaX + fac* constraintsAdjustmentTranslationVector.getX(), e.getSceneY() - deltaY +fac* constraintsAdjustmentTranslationVector.getY()));
                moved = true;
            }
        });

        shape.setOnMouseReleased((e) -> {
            if (moved) {
                undoManager.doAndAdd(new ChangeCoordinatesCommand(oldCoordinates, getFDomain().getCoordinates(), (c) -> {
                    getFDomain().setCoordinates(c);
                    tilingEditorTab.getTilingPane().update();
                    update();
                }));
                oldCoordinates = null;
                moved = false;
            }
        });
    }

    public Group getDecorations() {
        return decorations;
    }

    public double getScaleFactor() {
        return scaleFactor.get();
    }

    public ReadOnlyDoubleProperty scaleFactorProperty() {
        return scaleFactor;
    }
}
