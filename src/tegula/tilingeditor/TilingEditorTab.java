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

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableChangeProperty;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Basic;
import tegula.color.ColorSchemeManager;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.Geometry;
import tegula.fdomaineditor.FDomainEditor;
import tegula.main.TilingStyle;
import tegula.single.SingleTilingPane;
import tegula.tiling.TilingBase;
import tegula.util.HasHyperbolicModel;
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

        controller.getModelChoiceBox().getSelectionModel().selectedIndexProperty().addListener((c, o, n) -> {
            final HasHyperbolicModel.HyperbolicModel oldModel = tilingPane.getHyperbolicModel();
            switch (n.intValue()) {
                default:
                case 0: // Poincare
                    tilingPane.setHyperbolicModel(HasHyperbolicModel.HyperbolicModel.Poincare);
                    break;
                case 1: // Klein
                    tilingPane.setHyperbolicModel(HasHyperbolicModel.HyperbolicModel.Klein);
                    break;
                case 2: // Hyperboloid
                    tilingPane.setHyperbolicModel(HasHyperbolicModel.HyperbolicModel.Hyperboloid);
                    break;
            }
            if (oldModel != null)
                undoManager.add(new UndoableChangeProperty<>("set hyperbolic model", tilingPane.hyperbolicModelProperty(), oldModel, tilingPane.getHyperbolicModel()));
        });
        controller.getModelChoiceBox().disableProperty().bind(getTilingPane().geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getShowMoreTilesButton().setOnAction((e) -> {
            tilingPane.increaseTiling();
        });

        controller.getShowMoreTilesButton().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getShowLessTilesButton().setOnAction((e) -> tilingPane.decreaseTiling());

        controller.getShowLessTilesButton().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getGroupTextField().textProperty().bind(tilingPane.groupNameProperty());

        controller.getStraightenEdgesButton().setOnAction((e) -> {
            StraightenEdges.straightenAllEdges(getTiling().getfDomain());
            tilingPane.update();
        });

        controller.getDualizeButton().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.dualize(getTiling().getDSymbol());
            getTilingPane().replaceTiling(ds);
        });

        controller.getMaximizeButton().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.maxSymmetry(getTiling().getDSymbol());
            getTilingPane().replaceTiling(ds);
        });
        controller.getMaximizeButton().disableProperty().bind(tilingPane.maximalTilingProperty());

        controller.getOrientateButton().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.orientate(getTiling().getDSymbol());
            getTilingPane().replaceTiling(ds);
        });
        controller.getOrientateButton().disableProperty().bind(tilingPane.orientableTilingProperty());

        controller.getBandWidthSpinner().setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, tilingStyle.getBandWidth()));

        controller.getBandWidthSpinner().valueProperty().addListener((c, o, n) -> {
            tilingStyle.setBandWidth(n);
            tilingPane.update();
        });

        controller.getShowFacesCheckBox().setOnAction((e) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("show tiles",
                    tilingStyle.showFacesProperty(), !controller.getShowFacesCheckBox().isSelected(),
                    controller.getShowFacesCheckBox().isSelected(),
                    (v) -> {
                        tilingPane.update();
                        controller.getShowFacesCheckBox().setSelected(v);
                    }));
        });

        controller.getBackFacesCheckBox().setSelected(tilingStyle.isShowBackFaces());
        controller.getBackFacesCheckBox().setOnAction((e) -> {
            tilingStyle.setShowBackFaces(controller.getBackFacesCheckBox().isSelected());
            undoManager.doAndAdd(new UndoableChangeProperty<>("show back faces",
                    tilingStyle.showBackFacesProperty(), !controller.getBackFacesCheckBox().isSelected(),
                    controller.getBackFacesCheckBox().isSelected(),
                    (v) -> {
                        tilingPane.updateTileColors();
                        controller.getBackBandsCheckBox().setSelected(v);
                    }));
        });
        tilingStyle.showBackFacesProperty().addListener((c, o, n) -> controller.getBackFacesCheckBox().setSelected(n));
        controller.getBackFacesCheckBox().disableProperty().bind(tilingPane.geometryProperty().isEqualTo(Geometry.Euclidean));

        final DoubleProperty tileOpacity = new SimpleDoubleProperty(1);
        controller.getTilesOpacitySlider().valueProperty().addListener((c, o, n) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("tile opacity",
                    tileOpacity, tileOpacity.get(), n,
                    (v) -> {
                        final int numTiles = dSymbol.countOrbits(0, 1);
                        for (int t = 1; t <= numTiles; t++) {
                            final Color color = tilingStyle.getTileColor(t);
                            tilingStyle.setTileColor(t, new Color(color.getRed(), color.getGreen(), color.getBlue(), tileOpacity.getValue()));
                        }
                        tilingPane.updateTileColors();
                        controller.getTilesOpacitySlider().setValue(n.doubleValue());
                    })
            );
        });
        tileOpacity.set(0.8);
        undoManager.clear(); // don't want to keep this event


        controller.getShowBandsCheckBox().setOnAction((e) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("show bands",
                    tilingStyle.showBandsProperty(), !controller.getShowBandsCheckBox().isSelected(),
                    controller.getShowBandsCheckBox().isSelected(),
                    (v) -> {
                        tilingPane.update();
                        controller.getShowBandsCheckBox().setSelected(v);
                    }));
        });

        controller.getBackBandsCheckBox().setSelected(tilingStyle.isShowBackBands());
        controller.getBackBandsCheckBox().setOnAction((e) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("show back bands",
                    tilingStyle.showBackBandsProperty(), !controller.getBackBandsCheckBox().isSelected(),
                    controller.getBackBandsCheckBox().isSelected(),
                    (v) -> {
                        controller.getBackBandsCheckBox().setSelected(v);
                        tilingPane.update();
                    }));
        });
        controller.getBackBandsCheckBox().disableProperty().bind(tilingPane.geometryProperty().isEqualTo(Geometry.Euclidean));


        controller.getSmoothEdgesCheckBox().setOnAction((e) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("smooth edges",
                    tilingStyle.smoothEdgesProperty(), !controller.getSmoothEdgesCheckBox().isSelected(),
                    controller.getSmoothEdgesCheckBox().isSelected(),
                    (v) -> {
                        tilingPane.update();
                        controller.getSmoothEdgesCheckBox().setSelected(v);
                    }
            ));
        });
        controller.getSmoothEdgesCheckBox().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Spherical));


        controller.getBandsColorPicker().setValue(tilingStyle.getBackgroundColor());
        controller.getBandsColorPicker().setOnAction((e) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("band color",
                    tilingStyle.bandColorProperty(), tilingStyle.getBandColor(), controller.getBandsColorPicker().getValue(),
                    (v) -> tilingPane.updateBandColors()));
        });
        tilingStyle.bandColorProperty().addListener((c, o, n) -> {
            controller.getBandsColorPicker().setValue(n);
            controller.getBandsOpacitySlider().setValue(n.getOpacity());
        });

        controller.getBandsColorPicker().setOnShowing((e) -> {
            controller.getBandsColorPicker().getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(tilingStyle.getTileColorsScheme()));
            controller.getBandsColorPicker().setValue(tilingStyle.getBandColor());
        });

        controller.getBandsOpacitySlider().setValue(tilingStyle.getBandColor().getOpacity());
        controller.getBandsOpacitySlider().valueProperty().addListener((c, o, n) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("band opacity",
                    tilingStyle.bandColorProperty(), tilingStyle.getBandColor(),
                    new Color(tilingStyle.getBandColor().getRed(), tilingStyle.getBandColor().getGreen(), tilingStyle.getBandColor().getBlue(), n.doubleValue()),
                    (v) -> {
                        tilingPane.updateBandColors();
                        controller.getBandsOpacitySlider().setValue(v.getOpacity());
                    }));
        });

        controller.getBackgroundColorPicker().setOnAction((e) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("background color",
                    tilingStyle.backgroundColorProperty(), tilingStyle.getBackgroundColor(), controller.getBandsColorPicker().getValue(),
                    null));
        });
        tilingStyle.backgroundColorProperty().addListener((c, o, n) -> controller.getBackgroundColorPicker().setValue(n));
        controller.getBackgroundColorPicker().disableProperty().bind(tilingPane.geometryProperty().isEqualTo(Geometry.Euclidean));

        controller.getBackgroundColorPicker().setOnShowing((e) -> {
            controller.getBackgroundColorPicker().setValue(tilingStyle.getBackgroundColor());
        });

        controller.getInfoTextField().textProperty().bind(tilingPane.infoLineProperty());

        controller.getColorSchemeChoiceBox().getSelectionModel().select(tilingStyle.getTileColorsScheme());
        for (String colorSchemeName : ColorSchemeManager.getInstance().getNames()) {
            controller.getColorSchemeChoiceBox().getItems().add(colorSchemeName);
        }
        controller.getColorSchemeChoiceBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("color scheme",
                    tilingStyle.tileColorsSchemeProperty(), tilingStyle.getTileColorsScheme(), n,
                    (v) -> {
                        controller.getColorSchemeChoiceBox().setValue(v);
                        tilingPane.updateTileColors();
                    }));
        });


        controller.getStopAnimationButton().setVisible(false);
        if (getTilingPane().getMouseHandler() != null) {
            controller.getStopAnimationButton().setOnAction((e) -> getTilingPane().getMouseHandler().getAnimator().stop());
            controller.getStopAnimationButton().visibleProperty().bind(getTilingPane().getMouseHandler().getAnimator().playingProperty());
        }

        controller.getHyperbolicModelAccordion().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        tilingPane.geometryProperty().addListener((c, o, n) -> {
            if (n != Geometry.Hyperbolic)
                controller.getHyperbolicModelAccordion().setExpandedPane(null);
        });

        controller.getUndoReshapeButton().setOnAction((e) -> fDomainEditor.getUndoManager().undo());
        controller.getUndoReshapeButton().disableProperty().bind(fDomainEditor.getUndoManager().canUndoProperty().not());
        controller.getRedoReshapeButton().setOnAction((e) -> fDomainEditor.getUndoManager().redo());
        controller.getRedoReshapeButton().disableProperty().bind(fDomainEditor.getUndoManager().canRedoProperty().not());

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
