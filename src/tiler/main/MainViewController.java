package tiler.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    @FXML
    private Pane mainPane;

    @FXML
    private MenuBar menuBar;

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
    private Button firstTilingButton;

    @FXML
    private Button nextTilingButton;

    @FXML
    private Button previousTilingButton;

    @FXML
    private Button lastTilingButton;

    @FXML
    private CheckMenuItem editSymmetryGroupMenuItem;

    @FXML
    private ToggleButton editSymmetryGroupButton;


    @FXML
    private TextField statusTextField;

    @FXML
    private VBox groupVBox;

    @FXML
    private Button decreasev0;

    @FXML
    private Button increaseV0;

    @FXML
    private Label labelV0;

    @FXML
    private Button decreaseV1;

    @FXML
    private Button increaseV1;

    @FXML
    private Label labelV1;

    @FXML
    private Button decreaseV2;

    @FXML
    private Button increaseV2;

    @FXML
    private Label labelV2;

    @FXML
    private Button decreaseV3;

    @FXML
    private Button increaseV3;

    @FXML
    private Label labelV3;

    @FXML
    private Button decreaseV4;

    @FXML
    private Button increaseV4;

    @FXML
    private Label labelV4;

    @FXML
    private Button decreaseV5;

    @FXML
    private Button increaseV5;

    @FXML
    private Label labelV5;

    @FXML
    private Button decreaseV6;

    @FXML
    private Button increaseV6;

    @FXML
    private Label labelV6;

    @FXML
    private Button decreaseV7;

    @FXML
    private Button increaseV7;

    @FXML
    private Label labelV7;

    @FXML
    private Button decreaseV8;

    @FXML
    private Button increaseV8;

    @FXML
    private Label labelV8;

    @FXML
    private Button decreaseV9;

    @FXML
    private Button increaseV9;

    @FXML
    private Label labelV9;

    @FXML
    private RadioButton poincareRadioButton;

    @FXML
    private RadioButton kleinRadioButton;

    @FXML
    private RadioButton hyperboloidRadioButton;


    @FXML
    private Button increaseButton;

    @FXML
    private Button decreaseButton;

    @FXML
    private Button resetButton;

    @FXML
    private CheckBox cbPullFDomain;

    @FXML
    private CheckBox cbShowLines;

    @FXML
    private VBox hyperbolicControlsVBox;

    @FXML
    private BorderPane borderPane;



    private Document document;
    private Stage stage;

    /**
     * initialize
     *
     * @param fxmlFileLocation
     * @param resources
     */
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        if (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().startsWith("mac")) {
            menuBar.setUseSystemMenuBar(true);
        }

        borderPane.setRight(null);
        editSymmetryGroupMenuItem.setSelected(false);
        editSymmetryGroupButton.setSelected(false);

        statusTextField.setEditable(false);

        final ToggleGroup toggleGroup = new ToggleGroup();
        poincareRadioButton.setToggleGroup(toggleGroup);
        kleinRadioButton.setToggleGroup(toggleGroup);
        hyperboloidRadioButton.setToggleGroup(toggleGroup);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Window getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Pane getTopPane() {
        return topPane;
    }

    public TextField getStatusTextField() {
        return statusTextField;
    }

    public RadioButton getPoincareButton() {
        return poincareRadioButton;
    }

    public RadioButton getKleinButton() {
        return kleinRadioButton;
    }

    public RadioButton getHyperboloidButton() {
        return hyperboloidRadioButton;
    }

    public Button getIncreaseButton() {
        return increaseButton;
    }

    public Button getDecreaseButton() {
        return decreaseButton;
    }


    @FXML
    void setCbPullFDomain(ActionEvent event){
        if (cbPullFDomain.isSelected()){setCBPullFDomain(true);}
        else{setCBPullFDomain(false);}
    }

    private void setCBPullFDomain(boolean b){ MouseHandler.cbPullFDomain = b; }

    public CheckBox getCBPullFDomain(){ return cbPullFDomain; }

    @FXML
    void setCbShowLines(ActionEvent event){
        if (cbShowLines.isSelected()){
            document.addLinesToFDomain();
        }
        else {
            document.removeLinesFromFDomain();
        }
    }

    public CheckBox getCbShowLines() {
        return cbShowLines;
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
        if (getDocument().moveTo(Document.FIRST))
            getDocument().update();
    }

    @FXML
    void fireNextTiling(ActionEvent event) {
        if (getDocument().moveTo(Document.NEXT))
            getDocument().update();
    }


    @FXML
    void firePreviousTiling(ActionEvent event) {
        if (getDocument().moveTo(Document.PREV))
            getDocument().update();
    }

    @FXML
    void fireLastTiling(ActionEvent event) {
        if (getDocument().moveTo(Document.LAST))
            getDocument().update();
    }

    public void updateNavigateTilings() {
        firstTilingMenuItem.setDisable(getDocument().atFirstTiling());
        previousTilingMenuItem.setDisable(getDocument().atFirstTiling());
        lastTilingMenuItem.setDisable(getDocument().atLastTiling());
        nextTilingMenuItem.setDisable(getDocument().atLastTiling());

        firstTilingButton.setDisable(getDocument().atFirstTiling());
        previousTilingButton.setDisable(getDocument().atFirstTiling());
        nextTilingButton.setDisable(getDocument().atLastTiling());
        lastTilingButton.setDisable(getDocument().atLastTiling());

    }

    @FXML
    void fireLineWidth(ActionEvent event) {

    }

    @FXML
    void fireMaxSymmetry(ActionEvent event) {
    }

    @FXML
    void fireOpenFile(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file of tilings");
        fileChooser.setInitialDirectory(
                new File(Main.getProgramPreferences().getProperty("InputDirectory", ".")));
        fileChooser.setInitialFileName(Main.getProgramPreferences().getProperty("InputFile", ""));

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tilings", "*.tgs"));

        final File file = fileChooser.showOpenDialog(getStage());

        if (file != null) {
            try (FileReader reader = new FileReader(file)) {
                getDocument().clear();
                getDocument().read(reader);
                getDocument().update();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void firePaste(ActionEvent event) {
    }

    @FXML
    void firePrint(ActionEvent event) {
    }

    @FXML
    void fireQuit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Quit Tiler_new");
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
        document.decreaseTiling();
    }

    @FXML
    void fireShowMoreTiles(ActionEvent event) {
        document.increaseTiling();
    }

    @FXML
    void fireEditSymmetryGroup(ActionEvent event) {
        if (editSymmetryGroupButton.isSelected()) {
            borderPane.setRight(groupVBox);
        } else
            borderPane.setRight(null);
        editSymmetryGroupMenuItem.setSelected(borderPane.getRight() == groupVBox);
        editSymmetryGroupButton.setSelected(borderPane.getRight() == groupVBox);
    }

    @FXML
    void fireStraigthenAll(ActionEvent event) {
        document.getCurrentTiling().straightenAllEdges();
        document.update();
    }

    @FXML
    void fireStraigthenSelected(ActionEvent event) {
    }


    @FXML
    void fireReset(ActionEvent event){
        document.reset();
        document.update();
    }

    public Pane getMainPane() {
        return mainPane;
    }


    /**
     * gets a V label
     *
     * @param i
     * @return
     */
    public Label getLabelV(int i) {
        switch (i) {
            case 0:
                return labelV0;
            case 1:
                return labelV1;
            case 2:
                return labelV2;
            case 3:
                return labelV3;
            case 4:
                return labelV4;
            case 5:
                return labelV5;
            case 6:
                return labelV6;
            case 7:
                return labelV7;
            case 8:
                return labelV8;
            default:
            case 9:
                return labelV9;
        }
    }

    /**
     * gets a V decreaser
     *
     * @param i
     * @return
     */
    public Button getDecreaseV(int i) {
        switch (i) {
            case 0:
                return decreasev0;
            case 1:
                return decreaseV1;
            case 2:
                return decreaseV2;
            case 3:
                return decreaseV3;
            case 4:
                return decreaseV4;
            case 5:
                return decreaseV5;
            case 6:
                return decreaseV6;
            case 7:
                return decreaseV7;
            case 8:
                return decreaseV8;
            default:
            case 9:
                return decreaseV9;
        }
    }

    /**
     * gets a V increaser
     *
     * @param i
     * @return
     */
    public Button getIncreaseV(int i) {
        switch (i) {
            case 0:
                return increaseV0;
            case 1:
                return increaseV1;
            case 2:
                return increaseV2;
            case 3:
                return increaseV3;
            case 4:
                return increaseV4;
            case 5:
                return increaseV5;
            case 6:
                return increaseV6;
            case 7:
                return increaseV7;
            case 8:
                return increaseV8;
            default:
            case 9:
                return increaseV9;
        }
    }

    public void showHyperbolicControls(boolean show) {
        if (show) {
            if (borderPane.getLeft() != hyperbolicControlsVBox) {
                borderPane.setLeft(hyperbolicControlsVBox);
            }
        } else {
            borderPane.setLeft(null);
        }
    }
}
