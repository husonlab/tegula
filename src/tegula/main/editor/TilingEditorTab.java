/*
 *  Copyright (C) 2018. Daniel H. Huson
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

/*
 *  Copyright (C) 2018. Daniel H. Huson
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

package tegula.main.editor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Basic;
import tegula.color.ColorSchemeManager;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;
import tegula.core.dsymbols.Geometry;
import tegula.main.TilingStyle;
import tegula.main.fdomain.FDomainEditor;
import tegula.util.IFileBased;
import tegula.util.IHasHyperbolicModel;

import java.io.File;

/**
 * a tab that contains a single editable tiling
 * Daniel Huson, 4.2019
 */
public class TilingEditorTab extends Tab implements IFileBased {
    private final StringProperty fileName = new SimpleStringProperty("Untitled");

    private final TilingEditorTabController controller;
    private final Node root;

    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final ExtendedTilingPane tilingPane;
    private final ExtendedTiling extendedTiling;
    private final TilingStyle tilingStyle = new TilingStyle();
    private final FDomainEditor fDomainEditor;

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

        tilingPane = new ExtendedTilingPane(dSymbol, tilingStyle, Color.TRANSPARENT);
        extendedTiling = tilingPane.getExtendedTiling();
        tilingPane.prefWidthProperty().bind(controller.getMainPane().widthProperty());

        fDomainEditor = new FDomainEditor(this);

        tilingPane.prefHeightProperty().bind(controller.getMainPane().prefHeightProperty());

        final TilingStyle tilingStyle = extendedTiling.getTilingStyle();

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
            switch (n.intValue()) {
                default:
                case 0: // Poincare
                    extendedTiling.setHyperbolicModel(IHasHyperbolicModel.HyperbolicModel.Poincare);
                    break;
                case 1: // Klein
                    extendedTiling.setHyperbolicModel(IHasHyperbolicModel.HyperbolicModel.Klein);
                    break;
                case 2: // Hyperboloid
                    extendedTiling.setHyperbolicModel(IHasHyperbolicModel.HyperbolicModel.Hyperboloid);
                    break;
            }
        });
        controller.getModelChoiceBox().disableProperty().bind(extendedTiling.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getShowMoreTilesButton().setOnAction((e) -> {
            extendedTiling.increaseTiling();
        });

        controller.getShowMoreTilesButton().disableProperty().bind(extendedTiling.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getShowLessTilesButton().setOnAction((e) -> extendedTiling.decreaseTiling());

        controller.getShowLessTilesButton().disableProperty().bind(extendedTiling.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getGroupTextField().textProperty().bind(getTilingPane().getExtendedTiling().groupProperty());

        controller.getStraightenEdgesButton().setOnAction((e) -> {
            StraightenEdges.straightenAllEdges(extendedTiling.getTilingMeshes().getfDomain());
            extendedTiling.update();
        });

        controller.getDualizeButton().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.dualize(getTilingPane().getExtendedTiling().getTilingMeshes().getDSymbol());
            getTilingPane().replaceTiling(ds);
        });

        controller.getMaximizeButton().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.maxSymmetry(getTilingPane().getExtendedTiling().getTilingMeshes().getDSymbol());
            getTilingPane().replaceTiling(ds);
        });
        controller.getMaximizeButton().disableProperty().bind(extendedTiling.maximalTilingProperty());

        controller.getOrientateButton().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.orientate(getTilingPane().getExtendedTiling().getTilingMeshes().getDSymbol());
            getTilingPane().replaceTiling(ds);
        });
        controller.getOrientateButton().disableProperty().bind(extendedTiling.orientableTilingProperty());

        controller.getBandWidthSpinner().setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, tilingStyle.getBandWidth()));

        controller.getBandWidthSpinner().valueProperty().addListener((c, o, n) -> {
            tilingStyle.setBandWidth((Integer) n);
            extendedTiling.update();
        });

        controller.getShowFacesCheckBox().setOnAction((e) -> {
            tilingStyle.setShowFaces(controller.getShowFacesCheckBox().isSelected());
            extendedTiling.update();
        });

        controller.getShowBandsCheckBox().setOnAction((e) -> {
            tilingStyle.setShowBands(controller.getShowBandsCheckBox().isSelected());
            extendedTiling.update();
        });

        controller.getSmoothEdgesCheckBox().setOnAction((e) -> {
            tilingStyle.setSmoothEdges(controller.getSmoothEdgesCheckBox().isSelected());
            extendedTiling.update();
        });
        controller.getSmoothEdgesCheckBox().disableProperty().bind(extendedTiling.geometryProperty().isNotEqualTo(Geometry.Spherical));

        controller.getBandsColorPicker().setOnAction((e) -> {
            tilingStyle.setBandColor(controller.getBandsColorPicker().getValue());
            extendedTiling.update();
        });
        controller.getBandsColorPicker().setOnShowing((e) -> {
            controller.getBandsColorPicker().getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(tilingStyle.getTileColorsScheme()));
            controller.getBandsColorPicker().setValue(tilingStyle.getBandColor());
        });

        controller.getBandsOpacitySlider().valueProperty().addListener((c, o, n) -> {
            final Color color = tilingStyle.getBandColor();
            tilingStyle.setBandColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), n.doubleValue()));
        });
        controller.getBandsOpacitySlider().valueChangingProperty().addListener((c, o, n) -> {
            if (!n)
                extendedTiling.update();
        });
        controller.getBandsOpacitySlider().setValue(tilingStyle.getBandColor().getOpacity());
        // todo: recolor without updating


        controller.getBackgroundColorPicker().setOnAction((e) -> {
            controller.getMainPane().setBackground(new Background(new BackgroundFill(controller.getBackgroundColorPicker().getValue(), null, null)));
        });
        controller.getBackgroundColorPicker().setOnShowing((e) -> {
            controller.getBackgroundColorPicker().getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(tilingStyle.getTileColorsScheme()));
            controller.getBackgroundColorPicker().setValue(tilingStyle.getBandColor());
        });

        controller.getBackEdgesCheckBox().setSelected(tilingStyle.isShowBackEdges());
        controller.getBackEdgesCheckBox().setOnAction((e) -> {
            tilingStyle.setShowBackEdges(controller.getBackEdgesCheckBox().isSelected());
            extendedTiling.update();
        });
        controller.getBackEdgesCheckBox().disableProperty().bind(extendedTiling.geometryProperty().isEqualTo(Geometry.Euclidean));

        controller.getInfoTextField().textProperty().bind(extendedTiling.infoLineProperty());

        for (String colorSchemeName : ColorSchemeManager.getInstance().getNames()) {
            controller.getColorSchemeChoiceBox().getItems().add(colorSchemeName);
        }
        controller.getColorSchemeChoiceBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            tilingStyle.setTileColorsScheme(n);
            extendedTiling.update();
        });
        controller.getColorSchemeChoiceBox().getSelectionModel().select(tilingStyle.getTileColorsScheme());

        // reset the top node so it is drawn on top of the tiling:

        controller.getBorderPane().setTop(controller.getBorderPane().getTop());
        controller.getBorderPane().setBottom(controller.getBorderPane().getBottom());

        MouseHandler.setup(this);
        GroupEditingControls.setup(this);
        TileColorControls.setup(this);

        extendedTiling.lastUpdateProperty().addListener((c, o, n) -> {
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

    public ExtendedTilingPane getTilingPane() {
        return tilingPane;
    }

    public TilingEditorTabController getController() {
        return controller;
    }
}
