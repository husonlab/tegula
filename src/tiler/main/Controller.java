package tiler.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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

public class Controller implements Initializable {
    @FXML
    private BorderPane mainPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private StackPane stackPane;

    @FXML
    private AnchorPane topPane;

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
    private Button poincareButton;

    @FXML
    private Button kleinButton;

    @FXML
    private Button increaseButton;

    @FXML
    private Button decreaseButton;

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
        if (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().startsWith("mac"))
            menuBar.setUseSystemMenuBar(true);

        statusTextField.setEditable(false);

        topPane.getChildren().remove(groupVBox);
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

    public StackPane getStackPane() {
        return stackPane;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public AnchorPane getTopPane() {
        return topPane;
    }

    public TextField getStatusTextField() {
        return statusTextField;
    }

    public Button getPoincareButton() {
        return poincareButton;
    }

    public Button getKleinButton() {
        return kleinButton;
    }

    public Button getIncreaseButton() {
        return increaseButton;
    }

    public Button getDecreaseButton() {
        return decreaseButton;
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
                new File(Main.getProperties().getProperty("InputDirectory", ".")));
        fileChooser.setInitialFileName(Main.getProperties().getProperty("InputFile", ""));

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
        getDocument().setLimitHyperbolicGroup(getDocument().getLimitHyperbolicGroup() - 1);
        document.update();

    }

    @FXML
    void fireShowMoreTiles(ActionEvent event) {
        getDocument().setLimitHyperbolicGroup(getDocument().getLimitHyperbolicGroup() + 1);
        document.update();
    }

    @FXML
    void fireEditSymmetryGroup(ActionEvent event) {
        if (topPane.getChildren().contains(groupVBox)) {
            topPane.getChildren().remove(groupVBox);
        } else {
            topPane.getChildren().add(groupVBox);
        }
        editSymmetryGroupMenuItem.setSelected(topPane.getChildren().contains(groupVBox));
        editSymmetryGroupButton.setSelected(topPane.getChildren().contains(groupVBox));

    }

    @FXML
    void fireStraigthenAll(ActionEvent event) {
        document.straightenAll();
        document.update();
    }

    @FXML
    void fireStraigthenSelected(ActionEvent event) {
    }


    @FXML
    void fireKlein(ActionEvent event) {
        double maxDist = Math.cosh(0.5 * getDocument().getLimitHyperbolicGroup());  // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.
        document.getCamera().setTranslateZ(0);
        if (getDocument().getLimitHyperbolicGroup() < 12) {
            document.getCamera().setFarClip(65 * maxDist);
        }
        else {
            document.getCamera().setFarClip(100 * maxDist);
        }
        document.setCamPoincare(false);

    }

    @FXML
    void firePoincare(ActionEvent event) {
        double maxDist = Math.cosh(0.5 * getDocument().getLimitHyperbolicGroup());  // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.
        if (getDocument().getLimitHyperbolicGroup() < 12) {
            document.getCamera().setFarClip(65 * (maxDist + 1));
        }
        else {
            document.getCamera().setFarClip(100 * (maxDist + 1));
        }
        document.getCamera().setTranslateZ(-100);
        document.setCamPoincare(true);

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
                return labelV1;
            case 1:
                return labelV2;
            case 2:
                return labelV3;
            case 3:
                return labelV4;
            case 4:
                return labelV5;
            case 5:
                return labelV6;
            case 6:
                return labelV7;
            default:
            case 7:
                return labelV8;
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
                return decreaseV1;
            case 1:
                return decreaseV2;
            case 2:
                return decreaseV3;
            case 3:
                return decreaseV4;
            case 4:
                return decreaseV5;
            case 5:
                return decreaseV6;
            case 6:
                return decreaseV7;
            default:
            case 7:
                return decreaseV8;
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
                return increaseV1;
            case 1:
                return increaseV2;
            case 2:
                return increaseV3;
            case 3:
                return increaseV4;
            case 4:
                return increaseV5;
            case 5:
                return increaseV6;
            case 6:
                return increaseV7;
            default:
            case 7:
                return increaseV8;
        }
    }
}
