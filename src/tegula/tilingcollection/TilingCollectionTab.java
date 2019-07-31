/*
 * TilingCollectionTab.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tilingcollection;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import jloda.fx.control.AnotherMultipleSelectionModel;
import jloda.fx.control.FlowView;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.Searcher;
import jloda.fx.util.*;
import jloda.util.ProgramProperties;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.OrbifoldGroupName;
import tegula.main.MainWindow;
import tegula.main.TilingStyle;
import tegula.tilingpane.TilingPane;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Function;


/**
 * tab that shows a collection of tilings
 * Daniel Huson, 4.2019
 */
public class TilingCollectionTab extends Tab implements Closeable, Printable, IFileBased {
    private final TilingCollectionTabController controller;
    private final Parent root;
    private final IntegerProperty focusIndex = new SimpleIntegerProperty(-1);

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.WHITE);

    private final TilingCollection tilingCollection;

    private final TilingStyle tilingStyle;

    private int spawnedCount = 0; // how many tilings have been opened from this collection

    private final FlowView<DSymbol> flowView;

    private ListChangeListener<DSymbol> selectionListener;
    private ListChangeListener<DSymbol> dsymbolsListener;

    private final BooleanProperty showLabels = new SimpleBooleanProperty(true);

    private final IntegerProperty tilingsComputed = new SimpleIntegerProperty(0);

    private final FindToolBar findToolBar;

    private final Function<DSymbol, String> labelGetter;

    public static Font font = new Font("Arial", 12);

    private final MainWindow mainWindow;

    /**
     * constructor
     */
    public TilingCollectionTab(MainWindow mainWindow, TilingCollection tilingCollection) {
        this.mainWindow = mainWindow;
        this.tilingCollection = tilingCollection;
        final AnotherMultipleSelectionModel<DSymbol> selectionModel = tilingCollection.getSelectionModel();

        final ExtendedFXMLLoader<TilingCollectionTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        setContent(root);

        controller.getMainAnchorPane().getChildren().clear();

        flowView = new FlowView<>(createNodeProducer(selectionModel));
        flowView.setHgap(20);
        flowView.setVgap(20);
        flowView.setSelectionModel(tilingCollection.getSelectionModel());
        flowView.setScrollToSelection(true);
        flowView.setPrecomputeSnapshots(false);

        flowView.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> getTabPane().requestFocus());

        this.tilingStyle = new TilingStyle();
        tilingStyle.setShowEdges(true);
        tilingStyle.setBandWidth(4);
        tilingStyle.setShowFaces(false);
        tilingStyle.setShowVertices(false);
        tilingStyle.setBandColor(Color.BLACK);
        tilingStyle.setBackgroundColor(Color.GHOSTWHITE);

        final ChangeListener<Number> listener = (d, o, n) -> {
            // todo: need to do a better job of computing the new block size
            final int newBlockSize = (int) Math.max(1, Math.floor((flowView.getWidth() - 2 * flowView.getHgap()) / (flowView.getHgap() + controller.getSizeSlider().getValue())));
            if (newBlockSize != flowView.getBlockSize()) {
                flowView.setBlockSize(newBlockSize);
            }
        };

        flowView.widthProperty().addListener(listener);
        controller.getSizeSlider().valueProperty().addListener(listener);
        controller.getSizeSlider().setValue(ProgramProperties.get("PreviewSize", 128.0));
        controller.getSizeSlider().valueProperty().addListener((c, o, n) -> ProgramProperties.put("PreviewSize", n.doubleValue()));

        AnchorPane.setLeftAnchor(flowView, 0.0);
        AnchorPane.setRightAnchor(flowView, 0.0);
        AnchorPane.setBottomAnchor(flowView, 0.0);
        AnchorPane.setTopAnchor(flowView, 0.0);
        controller.getMainAnchorPane().getChildren().add(flowView);

        backgroundColorProperty().addListener((c, o, n) -> flowView.setBackground(new Background(new BackgroundFill(n, null, null))));

        selectionListener = (c) -> {
            while (c.next()) {
                for (DSymbol dSymbol : c.getRemoved()) {
                    final Node node = flowView.getNode(dSymbol);
                    if (node != null)
                        node.setEffect(null);
                }
                for (DSymbol dSymbol : c.getAddedSubList()) {
                    final Node node = flowView.getNode(dSymbol);
                    if (node != null) {
                        node.setEffect(SelectionEffect.getInstance());
                    }
                }
            }
            if (selectionModel.getSelectedItems().size() == 1) {
                Platform.runLater(() -> setFocusIndex(selectionModel.getSelectedIndex()));
            }
        };

        selectionModel.getSelectedItems().addListener(selectionListener);

        setText(tilingCollection.getTitle());
        setTooltip(new Tooltip(tilingCollection.getFileName()));

        dsymbolsListener = (e) -> {
            while (e.next()) {
                if (e.getAddedSize() > 0) {
                    flowView.getItems().addAll(e.getAddedSubList());
                }
            }
        };

        tilingCollection.getDSymbols().addListener(dsymbolsListener);

        focusIndex.addListener((c, o, n) -> {
            if (n != null && n.intValue() >= 0 && n.intValue() < flowView.size()) {
                selectionModel.clearAndSelect(n.intValue());
            }
        });

        labelGetter = (ds) -> String.format("%d. n:%d t:%d e:%d v:%d g:%s%s", ds.getNr1(), ds.size(),
                ds.countOrbits(0, 1), ds.countOrbits(0, 2), ds.countOrbits(1, 2), OrbifoldGroupName.getGroupName(ds),
                (DSymbolAlgorithms.isMaximalSymmetry(ds) ? " max" : ""));

        findToolBar = new FindToolBar(new Searcher<>(selectionModel, labelGetter, null));
        controller.getTopVBox().getChildren().add(findToolBar);
    }

    private boolean closing = false;

    public void close() {
        if (!closing) {
            closing = true;
            tilingCollection.cancelLoading();
            flowView.close();
            tilingCollection.getSelectionModel().getSelectedItems().removeListener(selectionListener);
            tilingCollection.getDSymbols().removeListener(dsymbolsListener);
            tilingsComputed.addListener((c, o, n) -> System.err.println("(Total tilings computed: " + tilingsComputed.get() + ")"));
            if (getFileName() != null && (new File(getFileName()).exists()))
                RecentFilesManager.getInstance().insertRecentFile(getFileName());

            if (getOnClosed() != null)
                getOnClosed().handle(null);
        }
    }

    /**
     * creates the node producer
     *
     * @param selectionModel
     * @return node producer
     */
    private Function<DSymbol, Node> createNodeProducer(AnotherMultipleSelectionModel<DSymbol> selectionModel) {

        return dSymbol -> {
            final Label label = new Label(labelGetter.apply(dSymbol));
            label.setFont(font);

            final Label shortLabel = new Label(dSymbol.getNr1() + ".");
            shortLabel.setFont(font);

            showLabelsProperty().addListener((c, o, n) -> {
                if (n) {
                    label.setTextFill(Color.BLACK);
                    shortLabel.setTextFill(Color.BLACK);
                } else {
                    label.setTextFill(Color.WHITE);
                    shortLabel.setTextFill(Color.WHITE);
                }
            });

            final Rectangle rectangle = new Rectangle();
            rectangle.setStroke(Color.LIGHTGRAY);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setWidth(controller.getSizeSlider().getValue());
            rectangle.setHeight(controller.getSizeSlider().getValue());
            rectangle.widthProperty().bind(controller.getSizeSlider().valueProperty());
            rectangle.heightProperty().bind(controller.getSizeSlider().valueProperty());
            final VBox vBox = new VBox(rectangle, shortLabel);

            ProgramExecutorService.getInstance().submit(() -> {
                final Image preview = tilingCollection.getPreviewImage(dSymbol);
                if (preview != null) {
                    final ImageView imageView = new ImageView(preview);
                    imageView.setPreserveRatio(true);
                    imageView.fitWidthProperty().addListener((c, o, n) -> {
                        if (n.doubleValue() < 128)
                            vBox.getChildren().set(1, shortLabel);
                        else
                            vBox.getChildren().set(1, label);
                    });


                    Platform.runLater(() -> {
                        imageView.setFitWidth(controller.getSizeSlider().getValue());
                        imageView.fitWidthProperty().bind(controller.getSizeSlider().valueProperty());
                        tilingsComputed.set(tilingsComputed.get() + 1);
                        vBox.getChildren().set(0, imageView);
                    });
                } else {
                    final TilingPane tilingPane = new TilingPane(dSymbol, tilingStyle, true, false);
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
                            if (n.doubleValue() < 128)
                                vBox.getChildren().set(1, shortLabel);
                            else
                                vBox.getChildren().set(1, label);
                        });

                        tilingsComputed.set(tilingsComputed.get() + 1);
                        vBox.getChildren().set(0, imageView);
                    });
                }
            });

            vBox.setOnMouseClicked((e) -> {
                if (e.getClickCount() == 1) {
                    if (!e.isShiftDown() && !e.isMetaDown())
                        selectionModel.clearSelection();
                    if (e.isShiftDown()) {
                        if (selectionModel.isSelected(dSymbol)) {
                            selectionModel.clearSelection(dSymbol);
                        } else if (selectionModel.isEmpty()) {
                            selectionModel.select(dSymbol);
                        } else {
                            final int start = selectionModel.getItems().indexOf(dSymbol);
                            int end = start;
                            while (end >= 0 && !selectionModel.isSelected(end))
                                end--;
                            if (end >= 0) {
                                for (int index = start; index > end; index--)
                                    selectionModel.select(index);
                            } else {
                                end = start;
                                while (end < selectionModel.getItems().size() && !selectionModel.isSelected(end))
                                    end++;
                                for (int index = start; index < end; index++)
                                    selectionModel.select(index);
                            }
                        }
                    } else
                        tilingCollection.getSelectionModel().select(dSymbol);
                } else if (e.getClickCount() == 2) {
                    mainWindow.getController().getOpenInEditorMenuItem().getOnAction().handle(null);
                }
            });
            return vBox;
        };
    }

    public TilingCollection getTilingCollection() {
        return tilingCollection;
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return tilingCollection.sizeProperty();
    }

    public int getFocusIndex() {
        return focusIndex.get();
    }

    public IntegerProperty focusIndexProperty() {
        return focusIndex;
    }

    public void setFocusIndex(int focusIndex) {
        this.focusIndex.set(focusIndex);
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }


    public class Line extends ArrayList<DSymbol> {
    }

    public MultipleSelectionModel<DSymbol> getSelectionModel() {
        return tilingCollection.getSelectionModel();
    }

    public Node getPrintable() {
        return flowView;
    }

    public TilingCollectionTabController getController() {
        return controller;
    }

    public FindToolBar getFindToolBar() {
        return findToolBar;
    }

    public int incrementSpawnedCount() {
        return ++spawnedCount;
    }

    public boolean isShowLabels() {
        return showLabels.get();
    }

    public BooleanProperty showLabelsProperty() {
        return showLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels.set(showLabels);
    }

    public BooleanProperty precomputeSnapshotsProperty() {
        return flowView.precomputeSnapshotsProperty();
    }

    public FlowView<DSymbol> getFlowView() {
        return flowView;
    }

    @Override
    public String getFileName() {
        return tilingCollection.getFileName();
    }

    @Override
    public StringProperty fileNameProperty() {
        return tilingCollection.fileNameProperty();
    }

    @Override
    public void setFileName(String fileName) {
        tilingCollection.setFileName(fileName);

    }

    @Override
    public String getTitle() {
        return tilingCollection.getTitle();
    }
}
