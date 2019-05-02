/*
 * SetupController.java Copyright (C) 2019. Daniel H. Huson
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

package tiler.main;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.control.CopyableLabel;
import jloda.fx.util.ColorSchemeManager;
import jloda.fx.util.Print;
import jloda.swing.util.ProgramProperties;
import tegula.single.SingleTilingWindow;
import tiler.color.ColorSchemeDialog;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.DSymbolAlgorithms;
import tiler.core.dsymbols.Geometry;
import tiler.tiling.StraightenEdges;
import tiler.tiling.Tiling;
import tiler.util.HasHyperbolicModel;

import java.io.*;
import java.util.Optional;

/**
 * sets up the controller, linking GUI FXML to Java code
 * Daniel Huson, 11.2018
 */
public class SetupController {
    /**
     * setup the menu items
     *
     * @param controller
     * @param document
     * @param mainStage
     */
    public static void setup(MainWindowController controller, Document document, Stage mainStage) {
        document.updateNumberProperty().addListener((c, o, n) -> {
            SetupGroupEditing.apply(controller, document);
            SetupTileColors.apply(controller, document);
            controller.getTilingNumberTextField().setText(document.getCurrentTiling().getDSymbol().getNr1() + "."
                            + document.getCurrentTiling().getDSymbol().getNr2());
            controller.getGroupTextField().setText(document.getCurrentTiling().getGroupName());
            controller.getOrientateMenuItem().setDisable(document.getCurrentTiling().getDSymbol().computeOrientation() == 2);
            controller.getMaxSymmetryMenuItem().setDisable(DSymbolAlgorithms.isMaximalSymmetry(document.getCurrentTiling().getDSymbol()));
            controller.getBandWidthSpinner().getValueFactory().setValue(document.getTilingStyle().getBandWidth());
            controller.getShowBandsCheckBox().setSelected(document.getTilingStyle().isShowBands());
            controller.getShowFacesCheckBox().setSelected(document.getTilingStyle().isShowFaces());
            controller.getSmoothEdgesCheckBox().setSelected(document.getTilingStyle().isSmoothEdges());
            {
                int tileNumber = 0;
                for (ColorPicker colorPicker : controller.tileColorPickers) {
                    colorPicker.setValue(document.getTilingStyle().getTileColor(tileNumber++));
                }
            }
                }
        );

        //document.showLinesProperty().bind(mainViewController.getCbShowLines().selectedProperty());

        final CopyableLabel statusLabel = new CopyableLabel();
        controller.getStatusFlowPane().getChildren().add(statusLabel);
        statusLabel.textProperty().bind(document.statusLineProperty());

        controller.getNewMenuItem().setOnAction((e) -> System.err.print("Not implemented"));

        controller.getOpenMenuItem().setOnAction((e) -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open file of tilings");
            fileChooser.setInitialDirectory(
                    new File(ProgramProperties.get("InputDirectory", ".")));
            fileChooser.setInitialFileName(ProgramProperties.get("InputFile", ""));

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tilings", "*.tgs"));

            final File file = fileChooser.showOpenDialog(mainStage);

            if (file != null) {
                try (FileReader reader = new FileReader(file)) {
                    document.clear();
                    document.read(reader);
                    document.update();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        controller.getOpenFileButton().setOnAction((e) -> controller.getOpenMenuItem().fire());

        controller.getQuitMenuItem().setOnAction((e) -> {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Quit PeriodicTiler");
            alert.setContentText("Sure you want to quit?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                alert.close();
                Platform.exit();
            } else {
                alert.close();
            }
        });

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(mainStage));
        controller.getPrintMenuItem().setOnAction((e) -> Print.print(mainStage, new ImageView(controller.getMainPane().snapshot(null, null))));
        controller.getPrintButton().setOnAction((e) -> controller.getPrintMenuItem().fire());

        controller.getCopyMenuItem().setOnAction((e) -> {
            final ClipboardContent content = new ClipboardContent();
            content.putImage(controller.getMainPane().snapshot(null, null));
            Clipboard.getSystemClipboard().setContent(content);
        });

        controller.getFirstTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.FIRST))
                document.update();
        });
        controller.getFirstTilingMenuItem().disableProperty().bind(document.currentIndexProperty().isEqualTo(0));
        controller.getFirstTilingButton().setOnAction((e) -> controller.getFirstTilingMenuItem().fire());
        controller.getFirstTilingButton().disableProperty().bind(controller.getFirstTilingMenuItem().disableProperty());

        controller.getPreviousTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.PREV))
                document.update();
        });
        controller.getPreviousTilingMenuItem().disableProperty().bind((document.currentIndexProperty().isEqualTo(0)));
        controller.getPreviousTilingButton().setOnAction((e) -> controller.getPreviousTilingMenuItem().fire());
        controller.getPreviousTilingButton().disableProperty().bind(controller.getPreviousTilingMenuItem().disableProperty());


        controller.getNextTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.NEXT))
                document.update();
        });
        controller.getNextTilingMenuItem().disableProperty().bind((document.currentIndexProperty().isEqualTo(document.numberOfTilingsProperty().subtract(1))));
        controller.getNextTilingButton().setOnAction((e) -> controller.getNextTilingMenuItem().fire());
        controller.getNextTilingButton().disableProperty().bind(controller.getNextTilingMenuItem().disableProperty());

        controller.getLastTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.LAST))
                document.update();
        });
        controller.getLastTilingMenuItem().disableProperty().bind((document.currentIndexProperty().isEqualTo(document.numberOfTilingsProperty().subtract(1))));
        controller.getLastTilingButton().setOnAction((e) -> controller.getLastTilingMenuItem().fire());
        controller.getLastTilingButton().disableProperty().bind(controller.getLastTilingMenuItem().disableProperty());

        controller.getTilingNumberTextField().setOnAction((e) ->
        {
            if (document.findAndMoveTo(controller.getTilingNumberTextField().getText())) {
                document.update();
            } else {
                controller.getTilingNumberTextField().setText(document.getCurrentTiling().getDSymbol().getNr1() + "."
                        + document.getCurrentTiling().getDSymbol().getNr2());
            }
        });

        controller.getTestButton().setOnAction((e) -> {
            final DSymbol dSymbol = document.getCurrentTiling().getfDomain().getDSymbol();
            if (dSymbol != null) {
                tegula.core.dsymbols.DSymbol tegulaDSymbol = new tegula.core.dsymbols.DSymbol();
                try {
                    final StringWriter w = new StringWriter();
                    dSymbol.write(w);
                    tegulaDSymbol.read(new StringReader(w.toString()));
                    new SingleTilingWindow(tegulaDSymbol);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        controller.getModelChoiceBox().getSelectionModel().selectedIndexProperty().addListener((c, o, n) -> {
            switch (n.intValue()) {
                default:
                case 0: // Poincare
                    HyperbolicModelCameraSettings.setModel(document, HasHyperbolicModel.HyperbolicModel.Poincare, true);
                    break;
                case 1: // Klein
                    HyperbolicModelCameraSettings.setModel(document, HasHyperbolicModel.HyperbolicModel.Klein, true);
                    break;
                case 2: // Hyperboloid
                    HyperbolicModelCameraSettings.setModel(document, HasHyperbolicModel.HyperbolicModel.Hyperboloid, true);
                    break;
            }
        });
        controller.getModelChoiceBox().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getShowMoreTilesButton().setOnAction((e) -> {
            document.increaseTiling();
        });
        controller.getShowMoreTilesMenuItem().setOnAction((e) -> controller.getShowMoreTilesButton().fire());

        controller.getShowMoreTilesButton().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        controller.getShowMoreTilesMenuItem().disableProperty().bind(controller.getShowMoreTilesButton().disableProperty());

        controller.getShowLessTilesButton().setOnAction((e) -> document.decreaseTiling());
        controller.getShowLessTilesMenuItem().setOnAction((e) -> controller.getShowLessTilesButton().fire());

        controller.getShowLessTilesButton().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        controller.getShowLessTilesMenuItem().disableProperty().bind(controller.getShowLessTilesButton().disableProperty());

        controller.getShowRotationsToggleButton().setSelected(false);
        controller.getRotationsToolBar().visibleProperty().bind(controller.getShowRotationsToggleButton().selectedProperty());

        controller.getToolsToggleButton().setSelected(false);
        controller.getToolsToolBar().visibleProperty().bind(controller.getToolsToggleButton().selectedProperty());


        controller.getResetMenuItem().setOnAction((e) -> {
            System.err.println("Not implemented");
        });

        controller.getDualizeMenuItem().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.dualize(document.getCurrentTiling().getDSymbol());
            document.changeCurrentTiling(new Tiling(ds, document.getTilingStyle()));
            document.update();
        });
        controller.getDualizeButton().setOnAction((e) -> controller.getDualizeMenuItem().fire());

        controller.getMaxSymmetryMenuItem().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.maxSymmetry(document.getCurrentTiling().getDSymbol());
            document.changeCurrentTiling(new Tiling(ds, document.getTilingStyle()));
            document.update();
        });
        controller.getMaximizeButton().setOnAction((e) -> controller.getMaxSymmetryMenuItem().fire());
        controller.getMaximizeButton().disableProperty().bind(controller.getMaxSymmetryMenuItem().disableProperty());

        controller.getOrientateMenuItem().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.orientate(document.getCurrentTiling().getDSymbol());
            final Tiling tiling = new Tiling(ds, document.getTilingStyle());
            document.changeCurrentTiling(tiling);
            document.update();
        });
        controller.getOrientateButton().setOnAction((e) -> controller.getOrientateMenuItem().fire());
        controller.getOrientateButton().disableProperty().bind(controller.getOrientateMenuItem().disableProperty());

        controller.getStraightenAllMenuItem().setOnAction((e) -> {
            StraightenEdges.straightenAllEdges(document.getCurrentTiling());
            document.update();
        });

        controller.getStraightenAlwaysCheckMenuItem().setOnAction((e) -> {
            document.setAlwaysStraightenEdges(!document.isAlwaysStraightenEdges());
            if (document.isAlwaysStraightenEdges())
                controller.getStraightenAllMenuItem().fire();
        });
        document.alwaysStraightenEdgesProperty().addListener((c, o, n) -> controller.getStraightenAlwaysCheckMenuItem().setSelected(n));


        controller.getBandWidthSpinner().valueProperty().addListener((c, o, n) -> {
            document.getTilingStyle().setBandWidth(n);
            document.update();
        });

        controller.getShowFacesCheckBox().setOnAction((e) -> {
            document.getTilingStyle().setShowFaces(controller.getShowFacesCheckBox().isSelected());
            document.update();
        });

        controller.getShowBandsCheckBox().setOnAction((e) -> {
            document.getTilingStyle().setShowBands(controller.getShowBandsCheckBox().isSelected());
            document.update();
        });

        controller.getSmoothEdgesCheckBox().setOnAction((e) -> {
            document.getTilingStyle().setSmoothEdges(controller.getSmoothEdgesCheckBox().isSelected());
            document.update();
        });
        controller.getSmoothEdgesCheckBox().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Spherical));

        controller.getBandsColorPicker().setOnAction((e) -> {
            document.getTilingStyle().setBandColor(controller.getBandsColorPicker().getValue());
            document.update();
        });
        controller.getBandsColorPicker().setOnShowing((e) -> {
            controller.getBandsColorPicker().getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(document.getTilingStyle().getTileColorsScheme()));
            controller.getBandsColorPicker().setValue(document.getTilingStyle().getBandColor());
        });

        controller.getBackgroundColorPicker().setOnAction((e) -> {
            controller.getMainPane().setBackground(new Background(new BackgroundFill(controller.getBackgroundColorPicker().getValue(), null, null)));
            document.update();
        });
        controller.getBackgroundColorPicker().setOnShowing((e) -> {
            controller.getBackgroundColorPicker().getCustomColors().setAll(ColorSchemeManager.getInstance().getColorScheme(document.getTilingStyle().getTileColorsScheme()));
            controller.getBackgroundColorPicker().setValue(document.getTilingStyle().getBandColor());
        });

        controller.getBackEdgesCheckBox().setSelected(document.getTilingStyle().isShowBackEdges());
        controller.getBackEdgesCheckBox().setOnAction((e) -> {
            document.getTilingStyle().setShowBackEdges(controller.getBackEdgesCheckBox().isSelected());
            document.update();
        });
        controller.getBackEdgesCheckBox().disableProperty().bind(document.geometryProperty().isEqualTo(Geometry.Euclidean));

        controller.getAddColorSchemeMenuItem().setOnAction((e) -> {
            try {
                new ColorSchemeDialog(document, controller);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        for (String name : ColorSchemeManager.getInstance().getNames()) {
            addToColorsMenu(controller, document, name);
        }

        controller.getShowFDomainMenuItem().setOnAction((e) -> {
            document.getTilingStyle().setShowFundamentalChambers(controller.getShowFDomainMenuItem().isSelected());
            document.update();
        });

        controller.getShowChambersMenuItem().setOnAction((e) -> {
            document.getTilingStyle().setShowAllChambers(controller.getShowChambersMenuItem().isSelected());
            document.update();
        });
    }

    public static void addToColorsMenu(MainWindowController controller, Document document, String name) {
        for (int pos = 0; pos < controller.getColorsMenu().getItems().size(); pos++) {
            if (controller.getColorsMenu().getItems().get(pos) instanceof SeparatorMenuItem) {
                final RadioMenuItem menuItem = new RadioMenuItem(name);
                menuItem.setUserData(name);
                menuItem.setOnAction((e) -> {
                    document.getTilingStyle().setTileColorsScheme(name);
                    document.update();
                });
                controller.getColorsMenuToggleGroup().getToggles().add(menuItem);
                controller.getColorsMenu().getItems().add(pos, menuItem);
                break;
            }
        }
        for (Toggle toggle : controller.getColorsMenuToggleGroup().getToggles()) {
            if (toggle.getUserData().equals(document.getTilingStyle().getTileColorsScheme()))
                toggle.setSelected(true);
        }
    }
}

