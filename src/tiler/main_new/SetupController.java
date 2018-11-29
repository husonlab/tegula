/*
 *  Copyright (C) 2016 Daniel H. Huson
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
import tiler.core.dsymbols.Geometry;
import tiler.main.Document;
import tiler.main.HyperbolicModelCameraSettings;
import tiler.main.Main;

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
     * @param mainViewController
     * @param document
     * @param mainStage
     */
    public static void setup(MainViewController mainViewController, Document document, Stage mainStage) {

        mainViewController.getNewMenuItem().setOnAction((e) -> System.err.print("Not implemented"));

        mainViewController.getOpenMenuItem().setOnAction((e) -> {
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

        mainViewController.getOpenFileButton().setOnAction((e) -> mainViewController.getOpenMenuItem().fire());

        mainViewController.getCloseMenuItem().setOnAction((e) -> System.err.print("Not implemented"));

        mainViewController.getQuitMenuItem().setOnAction((e) -> {
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

        mainViewController.getFirstTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.FIRST))
                document.update();
        });
        mainViewController.getFirstTilingMenuItem().disableProperty().bind(document.currentIndexProperty().isEqualTo(0));
        mainViewController.getFirstTilingButton().setOnAction((e) -> mainViewController.getFirstTilingMenuItem().fire());
        mainViewController.getFirstTilingButton().disableProperty().bind(mainViewController.getFirstTilingMenuItem().disableProperty());

        mainViewController.getPreviousTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.PREV))
                document.update();
        });
        mainViewController.getPreviousTilingMenuItem().disableProperty().bind((document.currentIndexProperty().isEqualTo(0)));
        mainViewController.getPreviousTilingButton().setOnAction((e) -> mainViewController.getPreviousTilingMenuItem().fire());
        mainViewController.getPreviousTilingButton().disableProperty().bind(mainViewController.getPreviousTilingMenuItem().disableProperty());


        mainViewController.getNextTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.NEXT))
                document.update();
        });
        mainViewController.getNextTilingMenuItem().disableProperty().bind((document.currentIndexProperty().isEqualTo(document.numberOfTilingsProperty().subtract(1))));
        mainViewController.getNextTilingButton().setOnAction((e) -> mainViewController.getNextTilingMenuItem().fire());
        mainViewController.getNextTilingButton().disableProperty().bind(mainViewController.getNextTilingMenuItem().disableProperty());

        mainViewController.getLastTilingMenuItem().setOnAction((e) -> {
            if (document.moveTo(Document.LAST))
                document.update();
        });
        mainViewController.getLastTilingMenuItem().disableProperty().bind((document.currentIndexProperty().isEqualTo(document.numberOfTilingsProperty().subtract(1))));
        mainViewController.getLastTilingButton().setOnAction((e) -> mainViewController.getLastTilingMenuItem().fire());
        mainViewController.getLastTilingButton().disableProperty().bind(mainViewController.getLastTilingMenuItem().disableProperty());

        mainViewController.getModelChoiceBox().getSelectionModel().selectedIndexProperty().addListener((c, o, n) -> {
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
        mainViewController.getModelChoiceBox().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));

        mainViewController.getIncreaseHyperbolicTilesButton().setOnAction((e) -> {
            document.increaseTiling();
        });
        mainViewController.getShowMoreTilesMenuItem().setOnAction((e) -> mainViewController.getIncreaseHyperbolicTilesButton().fire());

        mainViewController.getIncreaseHyperbolicTilesButton().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        mainViewController.getShowMoreTilesMenuItem().disableProperty().bind(mainViewController.getIncreaseHyperbolicTilesButton().disableProperty());

        mainViewController.getDecreaseHyperbolicTilesButton().setOnAction((e) -> document.decreaseTiling());
        mainViewController.getShowLessTilesMenuItem().setOnAction((e) -> mainViewController.getDecreaseHyperbolicTilesButton().fire());

        mainViewController.getDecreaseHyperbolicTilesButton().disableProperty().bind(document.geometryProperty().isNotEqualTo(Geometry.Hyperbolic));
        mainViewController.getShowLessTilesMenuItem().disableProperty().bind(mainViewController.getDecreaseHyperbolicTilesButton().disableProperty());

        mainViewController.getShowRotationsToggleButton().setSelected(false);
        mainViewController.getRotationsToolBar().visibleProperty().bind(mainViewController.getShowRotationsToggleButton().selectedProperty());
    }

}

