/*
 * ControlBindings.java Copyright (C) 2020. Daniel H. Huson
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
import javafx.geometry.Point2D;
import javafx.scene.control.SpinnerValueFactory;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableChangeProperty;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.ColorSchemeManager;
import tegula.core.dsymbols.*;
import tegula.main.CameraSettings;
import tegula.main.TilingStyle;
import tegula.tilingpane.TilingPane;
import tegula.undoable.ChangeDSymbolCommand;

import java.util.ArrayList;

/**
 * setup control bindings
 * Daniel Huson, 4.2019
 */
public class ControlBindings {
    public static void setup(TilingEditorTab tilingEditorTab) {
        final TilingPane tilingPane = tilingEditorTab.getTilingPane();
        final TilingStyle tilingStyle = tilingEditorTab.getTilingStyle();
        final TilingEditorTabController controller = tilingEditorTab.getController();
        final UndoManager undoManager = tilingEditorTab.getUndoManager();

        controller.getModelChoiceBox().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo()) {
                final CameraSettings.HyperbolicModel oldModel = tilingPane.getHyperbolicModel();
                final CameraSettings.HyperbolicModel newModel = CameraSettings.HyperbolicModel.valueOf(controller.getModelChoiceBox().getSelectionModel().getSelectedItem());
                undoManager.doAndAdd(new UndoableChangeProperty<>("hyperbolic model", tilingPane.hyperbolicModelProperty(), oldModel, newModel,
                        (v) -> controller.getModelChoiceBox().getSelectionModel().select(v.toString())));
            }
        });
        controller.getModelChoiceBox().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getShowMoreTilesButton().setOnAction((e) -> undoManager.doAndAdd(new UndoableRedoableCommand("show more tiles") {
            @Override
            public void undo() {
                tilingPane.decreaseTiling();
            }

            @Override
            public void redo() {
                tilingPane.increaseTiling();
            }
        }));
        controller.getShowMoreTilesButton().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getShowLessTilesButton().setOnAction((e) -> {
            if (tilingStyle.getHyperbolicLimit() > TilingStyle.minLimitHyperbolicGroup) {
                undoManager.doAndAdd(new UndoableRedoableCommand("show less tiles") {
                    @Override
                    public void undo() {
                        tilingPane.increaseTiling();
                    }

                    @Override
                    public void redo() {
                        tilingPane.decreaseTiling();
                    }
                });
            }
        });
        controller.getShowLessTilesButton().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getGroupTextField().textProperty().bind(tilingEditorTab.groupNameProperty());

        controller.getStraightenEdgesButton().setOnAction((e) -> {

            final Point2D[][] oldCoordinates = tilingPane.getTiling().getfDomain().getCoordinates();
            final ArrayList<Integer> edges = new ArrayList<>(tilingEditorTab.getEdgeSelection().getSelectedItems());

            undoManager.doAndAdd(new UndoableRedoableCommand("straighten") {
                public void undo() {
                    tilingPane.changeCoordinates(oldCoordinates);
                    tilingPane.update();
                }

                public void redo() {
                    if (edges.size() == 0)
                        StraightenEdges.straightenAllEdges(tilingPane.getTiling().getfDomain());
                    else
                        StraightenEdges.straightenEdges(tilingPane.getTiling().getfDomain(), edges);

                    tilingPane.update();
                }
            });
        });

        controller.getDualizeButton().setOnAction((e) -> {
            final Point2D[][] coordinates = tilingPane.getTiling().getfDomain().getCoordinates();
            undoManager.doAndAdd(new ChangeDSymbolCommand("dualize", tilingPane.getTiling().getDSymbol(), DSymbolAlgorithms.dualize(tilingPane.getTiling().getDSymbol()),
                    tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));
        });

        controller.getMaximizeButton().setOnAction((e) -> {
            final Point2D[][] coordinates = tilingPane.getTiling().getfDomain().getCoordinates();
            undoManager.doAndAdd(new ChangeDSymbolCommand("maximize", tilingPane.getTiling().getDSymbol(), DSymbolAlgorithms.maxSymmetry(tilingPane.getTiling().getDSymbol()),
                    tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));

        });
        controller.getMaximizeButton().disableProperty().bind(tilingEditorTab.maximalTilingProperty());

        controller.getOrientateButton().setOnAction((e) -> {
            final Point2D[][] coordinates = tilingPane.getTiling().getfDomain().getCoordinates();
            undoManager.doAndAdd(new ChangeDSymbolCommand("orientate", tilingPane.getTiling().getDSymbol(), DSymbolAlgorithms.orientate(tilingPane.getTiling().getDSymbol()),
                    tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));

        });
        controller.getOrientateButton().disableProperty().bind(tilingEditorTab.orientableTilingProperty());

        controller.getRemoveRotationsButton().setOnAction((e) -> {
            final int k;
            final int which;
            if (tilingEditorTab.getVertexSelection().getSelectedItems().size() > 0) {
                k = 0;
                which = tilingEditorTab.getVertexSelection().getSelectedItems().get(0);
            } else if (tilingEditorTab.getEdgeSelection().getSelectedItems().size() > 0) {
                k = 1;
                which = tilingEditorTab.getEdgeSelection().getSelectedItems().get(0);
            } else if (tilingEditorTab.getTileSelection().getSelectedItems().size() > 0) {
                k = 2;
                which = tilingEditorTab.getTileSelection().getSelectedItems().get(0);
            } else {
                k = 0;
                which = 0;
            }
            if (which > 0) {
                final DSymbol result = BreakSymmetries.removeRotations(tilingPane.getTiling().getDSymbol(), k, which);
                if (result != null) {
                    final Point2D[][] coordinates = tilingPane.getTiling().getfDomain().getCoordinates();
                    undoManager.doAndAdd(new ChangeDSymbolCommand("remove rotations", tilingPane.getTiling().getDSymbol(), result,
                            tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));
                }
            }
        });
        controller.getRemoveRotationsButton().disableProperty().bind((tilingEditorTab.orientableTilingProperty().not()).or((tilingEditorTab.singleVertexSelectedProperty().not())
                .and(tilingEditorTab.singleEdgeSelectedProperty().not()).and(tilingEditorTab.singleTileSelectedProperty().not())));

        controller.getContractEdgeButton().setOnAction((e) -> {
            final Point2D[][] coordinates = tilingPane.getTiling().getfDomain().getCoordinates();
            if (tilingEditorTab.canContractEdgeProperty().get()) {
                final int edge = tilingEditorTab.getEdgeSelection().getSelectedItems().get(0);
                undoManager.doAndAdd(new ChangeDSymbolCommand("contract", tilingPane.getTiling().getDSymbol(), ContractEdge.apply(edge, tilingPane.getTiling().getDSymbol()),
                        tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));
            }

        });
        controller.getContractEdgeButton().disableProperty().bind(tilingEditorTab.canContractEdgeProperty().not());


        controller.getTruncateVertexButton().setOnAction((e) -> {
            final Point2D[][] coordinates = tilingPane.getTiling().getfDomain().getCoordinates();
            if (tilingEditorTab.singleVertexSelectedProperty().get()) {
                final int vertex = tilingEditorTab.getVertexSelection().getSelectedItems().get(0);
                undoManager.doAndAdd(new ChangeDSymbolCommand("truncate", tilingPane.getTiling().getDSymbol(), TruncateVertex.apply(vertex, tilingPane.getTiling().getDSymbol()),
                        tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));
            }

        });
        controller.getTruncateVertexButton().disableProperty().bind(tilingEditorTab.singleVertexSelectedProperty().not());


        controller.getGlueTilesButton().setOnAction((e) -> {
            final Point2D[][] coordinates = tilingPane.getTiling().getfDomain().getCoordinates();
            if (tilingEditorTab.canGlueTilesAroundEdgeProperty().get()) {
                final int edge = tilingEditorTab.getEdgeSelection().getSelectedItems().get(0);
                undoManager.doAndAdd(new ChangeDSymbolCommand("glue", tilingPane.getTiling().getDSymbol(), GlueTilesAroundEdge.apply(edge, tilingPane.getTiling().getDSymbol()),
                        tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));
            } else if (tilingEditorTab.canGlueTilesAroundVertexProperty().get()) {
                final int vertex = tilingEditorTab.getVertexSelection().getSelectedItems().get(0);
                undoManager.doAndAdd(new ChangeDSymbolCommand("glue", tilingPane.getTiling().getDSymbol(), GlueTilesAroundVertex.apply(vertex, tilingPane.getTiling().getDSymbol()),
                        tilingPane::computTiling, coordinates, tilingPane::changeCoordinates));
            }
        });
        controller.getGlueTilesButton().disableProperty().bind(tilingEditorTab.canGlueTilesAroundEdgeProperty().not().and(tilingEditorTab.canGlueTilesAroundVertexProperty().not()));

        // implement split here
        controller.getSplitTileButton().disableProperty().bind(tilingEditorTab.getCanSplitTileProperty().not());

        controller.getBandWidthSpinner().setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, tilingStyle.getBandWidth()));
        controller.getBandWidthSpinner().valueProperty().addListener((c, o, n) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("width",
                        tilingStyle.bandWidthProperty(), tilingStyle.getBandWidth(), n,
                        (v) -> {
                            tilingPane.update();
                            controller.getBandWidthSpinner().getValueFactory().setValue(v.intValue());
                        }));
            tilingStyle.setBandWidth(n);
            tilingPane.update();
        });

        controller.getShowFacesToggleButton().setSelected(tilingStyle.isShowFaces());
        controller.getShowFacesToggleButton().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("show faces",
                        tilingStyle.showFacesProperty(), !controller.getShowFacesToggleButton().isSelected(),
                        controller.getShowFacesToggleButton().isSelected(),
                        (v) -> {
                            tilingPane.update();
                            controller.getShowFacesToggleButton().setSelected(v);
                        }));
        });

        controller.getBackFacesToggleButton().setSelected(tilingStyle.isShowBackFaces());
        controller.getBackFacesToggleButton().setOnAction((e) -> {
            tilingStyle.setShowBackFaces(controller.getBackFacesToggleButton().isSelected());
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("show back faces",
                        tilingStyle.showBackFacesProperty(), !controller.getBackFacesToggleButton().isSelected(),
                        controller.getBackFacesToggleButton().isSelected(),
                        (v) -> {
                            tilingPane.update();
                            controller.getBackFacesToggleButton().setSelected(v);
                        }));
        });
        tilingStyle.showBackFacesProperty().addListener((c, o, n) -> controller.getBackFacesToggleButton().setSelected(n));
        controller.getBackFacesToggleButton().disableProperty().bind(tilingPane.geometryProperty().isEqualTo(Geometry.Euclidean));

        controller.getTilesOpacitySlider().valueProperty().addListener((c, o, n) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("tile opacity",
                        tilingStyle.tileOpacityProperty(), o, n,
                        (v) -> tilingPane.updateTileColors())
                );
        });
        controller.getTilesOpacitySlider().setValue(tilingStyle.getTileOpacity());
        undoManager.clear(); // don't want to keep this event

        controller.getShowEdgesToggleButton().setSelected(tilingStyle.isShowEdges());
        controller.getShowEdgesToggleButton().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("show edges",
                        tilingStyle.showEdgesProperty(), !controller.getShowEdgesToggleButton().isSelected(),
                        controller.getShowEdgesToggleButton().isSelected(),
                        (v) -> {
                            tilingPane.update();
                            controller.getShowEdgesToggleButton().setSelected(v);
                        }));
        });

        controller.getShowBackEdgesToggleButton().setSelected(tilingStyle.isShowBackEdges());
        controller.getShowBackEdgesToggleButton().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("show back edges",
                        tilingStyle.showBackEdgesProperty(), !controller.getShowBackEdgesToggleButton().isSelected(),
                        controller.getShowBackEdgesToggleButton().isSelected(),
                        (v) -> {
                            tilingPane.update();
                            controller.getShowBackEdgesToggleButton().setSelected(v);
                        }));
        });
        controller.getShowBackEdgesToggleButton().disableProperty().bind(tilingPane.geometryProperty().isEqualTo(Geometry.Euclidean));

        controller.getShowNodesToggleButton().setSelected(tilingStyle.isShowVertices());
        controller.getShowNodesToggleButton().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("show nodes",
                        tilingStyle.showVerticesProperty(), !controller.getShowNodesToggleButton().isSelected(),
                        controller.getShowNodesToggleButton().isSelected(),
                        (v) -> {
                            tilingPane.update();
                            controller.getShowNodesToggleButton().setSelected(v);
                        }));
        });


        controller.getPointLightRadioButton().selectedProperty().addListener((c, o, n) -> {
            undoManager.doAndAdd(new UndoableChangeProperty<>("point lighting",
                    tilingStyle.sphericalUsePointLightProperty(), !n, n,
                    (v) -> {
                        tilingPane.update();
                    }));

        });

        controller.getShowBackNodesToggleButton().setSelected(tilingStyle.isShowBackVertices());
        controller.getShowBackNodesToggleButton().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("show back nodes",
                        tilingStyle.showBackVerticesProperty(), !controller.getShowBackNodesToggleButton().isSelected(),
                        controller.getShowBackNodesToggleButton().isSelected(),
                        (v) -> {
                            tilingPane.update();
                            controller.getShowBackNodesToggleButton().setSelected(v);
                        }));
        });
        controller.getShowBackNodesToggleButton().disableProperty().bind(tilingPane.geometryProperty().isEqualTo(Geometry.Euclidean));


        controller.getSmoothEdgesCheckBox().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("smooth",
                        tilingStyle.smoothEdgesProperty(), !controller.getSmoothEdgesCheckBox().isSelected(),
                        controller.getSmoothEdgesCheckBox().isSelected(),
                        (v) -> {
                            tilingPane.update();
                            controller.getSmoothEdgesCheckBox().setSelected(v);
                        }
                ));
        });
        controller.getSmoothEdgesCheckBox().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Spherical));

        controller.getBandsColorPicker().setValue(tilingStyle.getBandColorFullOpacity());
        controller.getBandsColorPicker().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("band color",
                        tilingStyle.bandColorProperty(), tilingStyle.getBandColorFullOpacity(), controller.getBandsColorPicker().getValue(),
                        (v) -> tilingPane.updateBandColors()));
        });
        tilingStyle.bandColorProperty().addListener((c, o, n) -> controller.getBandsColorPicker().setValue(n));

        controller.getBandsColorPicker().setOnShowing((e) -> {
            controller.getBandsColorPicker().getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(tilingStyle.getTileColorsScheme()));
            controller.getBandsColorPicker().setValue(tilingStyle.getBandColorFullOpacity());
        });

        controller.getBandsOpacitySlider().setValue(tilingStyle.getBandOpacity());
        controller.getBandsOpacitySlider().valueProperty().addListener((c, o, n) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("band opacity",
                        tilingStyle.bandOpacityProperty(), o,
                        n,
                        (v) -> tilingPane.updateBandColors()));
        });

        controller.getBackgroundColorPicker().setOnAction((e) -> {
            if (!undoManager.isPerformingUndoOrRedo())
                undoManager.doAndAdd(new UndoableChangeProperty<>("background",
                        tilingStyle.backgroundColorProperty(), tilingStyle.getBackgroundColor(), controller.getBackgroundColorPicker().getValue(),
                        null));
        });
        tilingStyle.backgroundColorProperty().addListener((c, o, n) -> controller.getBackgroundColorPicker().setValue(n));
        //controller.getBackgroundColorPicker().disableProperty().bind(tilingPane.geometryProperty().isEqualTo(Geometry.Euclidean));

        controller.getBackgroundColorPicker().setOnShowing((e) -> {
            controller.getBackgroundColorPicker().getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(tilingStyle.getTileColorsScheme()));
            controller.getBackgroundColorPicker().setValue(tilingStyle.getBackgroundColor());
        });


        controller.getInfoTextField().textProperty().bind(tilingEditorTab.infoLineProperty());

        {
            controller.getColorSchemeChoiceBox().getSelectionModel().select(tilingStyle.getTileColorsScheme());
            for (String colorSchemeName : ColorSchemeManager.getInstance().getNames()) {
                controller.getColorSchemeChoiceBox().getItems().add(colorSchemeName);
            }

            controller.getColorSchemeChoiceBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
                if (!undoManager.isPerformingUndoOrRedo() && n != null) {
                    undoManager.doAndAdd(new UndoableChangeProperty<>("colors",
                            tilingStyle.tileColorsSchemeProperty(), tilingStyle.getTileColorsScheme(), n,
                            (v) -> {
                                tilingPane.updateTileColors();
                                controller.getColorSchemeChoiceBox().setValue(v);
                                TileColorControls.setup(tilingEditorTab);
                            }));
                }
            });

            ColorSchemeManager.getInstance().getName2ColorSchemes().addListener((InvalidationListener) (c) -> {
                controller.getColorSchemeChoiceBox().getItems().clear();
                for (String colorSchemeName : ColorSchemeManager.getInstance().getNames()) {
                    controller.getColorSchemeChoiceBox().getItems().add(colorSchemeName);
                }
            });
        }
        tilingStyle.tileColorsSchemeProperty().addListener((c, o, n) -> controller.getColorSchemeChoiceBox().getSelectionModel().select(n));

        controller.getStopAnimationButton().setVisible(false);
        if (tilingPane.getMouseHandler() != null) {
            controller.getStopAnimationButton().setOnAction((e) -> tilingPane.getMouseHandler().getAnimator().stop());
            controller.getStopAnimationButton().visibleProperty().bind(tilingPane.getMouseHandler().getAnimator().playingProperty());
        }

        controller.getHyperbolicModelTitledPane().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        tilingPane.geometryProperty().addListener((c, o, n) -> {
            if (n != Geometry.Hyperbolic)
                controller.getHyperbolicModelTitledPane().setExpanded(false);
        });

        controller.getSphericalLightingTitledPane().disableProperty().bind(tilingPane.geometryProperty().isNotEqualTo(Geometry.Spherical));
        tilingPane.geometryProperty().addListener((c, o, n) -> {
            if (n != Geometry.Spherical)
                controller.getSphericalLightingTitledPane().setExpanded(false);
        });
    }
}
