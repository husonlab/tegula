/*
 *  Copyright (C) 2018 University of Tuebingen
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

package tiler.main_new;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.DSymbolAlgorithms;
import tiler.core.dsymbols.Geometry;
import tiler.main.Document;
import tiler.main.HyperbolicModelCameraSettings;
import tiler.main.Main;
import tiler.tiling.Tiling;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public static void setup(MainViewController controller, Document document, Stage mainStage) {
        //document.showLinesProperty().bind(mainViewController.getCbShowLines().selectedProperty());
        controller.getStatusTextField().textProperty().bind(document.statusLineProperty());
        document.statusLineProperty().addListener((c, o, n) -> {
            GroupEditing.update(controller, document);
            controller.getTilingNumberTextField().setText(document.getCurrentTiling().getDSymbol().getNr1() + "."
                            + document.getCurrentTiling().getDSymbol().getNr2());
            controller.getGroupTextField().setText(document.getCurrentTiling().getGroupName());
                }
        );

        controller.getNewMenuItem().setOnAction((e) -> System.err.print("Not implemented"));

        controller.getOpenMenuItem().setOnAction((e) -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open file of tilings");
            fileChooser.setInitialDirectory(
                    new File(Main.getProgramPreferences().getProperty("InputDirectory", ".")));
            fileChooser.setInitialFileName(Main.getProgramPreferences().getProperty("InputFile", ""));

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

        controller.getCloseMenuItem().setOnAction((e) -> System.err.print("Not implemented"));

        controller.getQuitMenuItem().setOnAction((e) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Quit Tiler_new");
            alert.setContentText("Sure you want to quit?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                alert.close();
                Platform.exit();
            } else {
                alert.close();
            }
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


        controller.getModelChoiceBox().getSelectionModel().selectedIndexProperty().addListener((c, o, n) -> {
            switch (n.intValue()) {
                default:
                case 0: // Poincare
                    HyperbolicModelCameraSettings.setModel(document, Document.HyperbolicModel.Poincare, true);
                    break;
                case 1: // Klein
                    HyperbolicModelCameraSettings.setModel(document, Document.HyperbolicModel.Klein, true);
                    break;
                case 2: // Hyperboloid
                    HyperbolicModelCameraSettings.setModel(document, Document.HyperbolicModel.Hyperboloid, true);
                    break;
            }
        });
        controller.getModelChoiceBox().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        controller.getIncreaseHyperbolicTilesButton().setOnAction((e) -> {
            document.increaseTiling();
        });
        controller.getShowMoreTilesMenuItem().setOnAction((e) -> controller.getIncreaseHyperbolicTilesButton().fire());

        controller.getIncreaseHyperbolicTilesButton().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        controller.getShowMoreTilesMenuItem().disableProperty().bind(controller.getIncreaseHyperbolicTilesButton().disableProperty());

        controller.getDecreaseHyperbolicTilesButton().setOnAction((e) -> document.decreaseTiling());
        controller.getShowLessTilesMenuItem().setOnAction((e) -> controller.getDecreaseHyperbolicTilesButton().fire());

        controller.getDecreaseHyperbolicTilesButton().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        controller.getShowLessTilesMenuItem().disableProperty().bind(controller.getDecreaseHyperbolicTilesButton().disableProperty());

        controller.getShowRotationsToggleButton().setSelected(false);
        controller.getRotationsToolBar().visibleProperty().bind(controller.getShowRotationsToggleButton().selectedProperty());

        controller.getToolsToggleButton().setSelected(false);
        controller.getToolsToolBar().visibleProperty().bind(controller.getToolsToggleButton().selectedProperty());


        controller.getResetMenuItem().setOnAction((e) -> {
            System.err.println("Not implemented");
        });

        controller.getDualizeMenuItem().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.dualize(document.getCurrentTiling().getDSymbol());
            System.err.println(ds.toString());
            document.changeCurrentTiling(new Tiling(ds));
            document.update();
        });
        controller.getDualizeButton().setOnAction((e) -> controller.getDualizeMenuItem().fire());

        controller.getMaxSymmetryMenuItem().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.maxSymmetry(document.getCurrentTiling().getDSymbol());
            System.err.println(ds.toString());
            document.changeCurrentTiling(new Tiling(ds));
            document.update();
        });
        controller.getMaximizeButton().setOnAction((e) -> controller.getMaxSymmetryMenuItem().fire());

        controller.getOrientateMenuItem().setOnAction((e) -> {
            final DSymbol ds = DSymbolAlgorithms.orientate(document.getCurrentTiling().getDSymbol());
            System.err.println(ds.toString());
            final Tiling tiling = new Tiling(ds);
            System.err.println("Group: " + tiling.getGroupName());
            System.err.println("Geometry: " + tiling.getGeometry());
            document.changeCurrentTiling(tiling);
            document.update();
        });
        controller.getOrientateButton().setOnAction((e) -> controller.getOrientateMenuItem().fire());

    }

}

