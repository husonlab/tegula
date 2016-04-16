package tiler.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private Pane worldPane;

    @FXML
    private Pane topPane;

    @FXML
    private MenuItem firstTilingMenuItem;

    @FXML
    private MenuItem nextTilingMenuItem;

    @FXML
    private MenuItem previousTilingMenuItem;

    @FXML
    private MenuItem lastTilingMenuItem;

    @FXML
    private MenuItem maxSymmetryMenuItem;

    @FXML
    private MenuItem dualizeMenuItem;

    @FXML
    private CheckMenuItem showSymmetryGroupMenuItem;

    /**
     * initialize
     *
     * @param fxmlFileLocation
     * @param resources
     */
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        if (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().startsWith("mac"))
            menuBar.setUseSystemMenuBar(true);
    }

    public Pane getWorldPane() {
        return worldPane;
    }

    public Pane getTopPane() {
        return topPane;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    @FXML
    void fireAbout(ActionEvent event) {

    }

    @FXML
    void fireAlwaysStraighten(ActionEvent event) {

    }

    @FXML
    void fireClose(ActionEvent event) {

    }

    @FXML
    void fireCopy(ActionEvent event) {

    }

    @FXML
    void fireDualize(ActionEvent event) {

    }

    @FXML
    void fireFirstTiling(ActionEvent event) {

    }

    @FXML
    void fireLastTiling(ActionEvent event) {

    }

    @FXML
    void fireLineWidth(ActionEvent event) {

    }

    @FXML
    void fireMaxSymmetry(ActionEvent event) {

    }

    @FXML
    void fireNextTiling(ActionEvent event) {
        System.err.println("next tiling");

    }

    @FXML
    void fireOpenFile(ActionEvent event) {

    }

    @FXML
    void firePaste(ActionEvent event) {

    }

    @FXML
    void firePreviousTiling(ActionEvent event) {

    }

    @FXML
    void firePrint(ActionEvent event) {

    }

    @FXML
    void fireQuit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Quit Tiler");
        alert.setContentText("Sure you want to quit?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            alert.close();
            Platform.exit();
        } else {
            alert.close();
        }

    }

    @FXML
    void fireResetLayout(ActionEvent event) {

    }

    @FXML
    void fireSaveFile(ActionEvent event) {

    }

    @FXML
    void fireSelectAll(ActionEvent event) {

    }

    @FXML
    void fireShowChambers(ActionEvent event) {

    }

    @FXML
    void fireShowFundamentalDomain(ActionEvent event) {

    }

    @FXML
    void fireShowLessTiles(ActionEvent event) {

    }

    @FXML
    void fireShowMoreTiles(ActionEvent event) {

    }

    @FXML
    void fireShowSymmetryGroup(ActionEvent event) {

    }

    @FXML
    void fireStraigthenAll(ActionEvent event) {

    }

    @FXML
    void fireStraigthenSelected(ActionEvent event) {

    }
}
