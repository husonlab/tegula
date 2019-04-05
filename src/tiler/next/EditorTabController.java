package tiler.next;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class EditorTabController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane borderPane;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private ChoiceBox<?> modelChoiceBox;

    @FXML
    private Button showMoreTilesButton;

    @FXML
    private Button showLessTilesButton;

    @FXML
    private ToggleButton showRotationsToggleButton;

    @FXML
    private ToggleButton toolsToggleButton;

    @FXML
    private Button testButton;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private Pane mainPane;

    @FXML
    private ToolBar rotationsToolBar;

    @FXML
    private TextField groupTextField;

    @FXML
    private Spinner<?> v1Spinner;

    @FXML
    private Spinner<?> v2Spinner;

    @FXML
    private Spinner<?> v3Spinner;

    @FXML
    private Spinner<?> v4Spinner;

    @FXML
    private Spinner<?> v5Spinner;

    @FXML
    private Spinner<?> v6Spinner;

    @FXML
    private Spinner<?> v7Spinner;

    @FXML
    private Spinner<?> v8Spinner;

    @FXML
    private Spinner<?> v9Spinner;

    @FXML
    private Spinner<?> v10Spinner;

    @FXML
    private ToolBar toolsToolBar;

    @FXML
    private Button dualizeButton;

    @FXML
    private Button orientateButton;

    @FXML
    private Button maximizeButton;

    @FXML
    private CheckBox showBandsCheckBox;

    @FXML
    private Spinner<?> bandWidthSpinner;

    @FXML
    private ColorPicker bandsColorPicker;

    @FXML
    private CheckBox showFacesCheckBox;

    @FXML
    private CheckBox smoothEdgesCheckBox;

    @FXML
    private CheckBox backEdgesCheckBox;

    @FXML
    private ColorPicker tile1ColorPicker;

    @FXML
    private ColorPicker backgroundColorPicker;

    @FXML
    void initialize() {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert modelChoiceBox != null : "fx:id=\"modelChoiceBox\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert showMoreTilesButton != null : "fx:id=\"showMoreTilesButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert showLessTilesButton != null : "fx:id=\"showLessTilesButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert showRotationsToggleButton != null : "fx:id=\"showRotationsToggleButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert toolsToggleButton != null : "fx:id=\"toolsToggleButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert testButton != null : "fx:id=\"testButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert rotationsToolBar != null : "fx:id=\"rotationsToolBar\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert groupTextField != null : "fx:id=\"groupTextField\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v1Spinner != null : "fx:id=\"v1Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v2Spinner != null : "fx:id=\"v2Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v3Spinner != null : "fx:id=\"v3Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v4Spinner != null : "fx:id=\"v4Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v5Spinner != null : "fx:id=\"v5Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v6Spinner != null : "fx:id=\"v6Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v7Spinner != null : "fx:id=\"v7Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v8Spinner != null : "fx:id=\"v8Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v9Spinner != null : "fx:id=\"v9Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert v10Spinner != null : "fx:id=\"v10Spinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert toolsToolBar != null : "fx:id=\"toolsToolBar\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert dualizeButton != null : "fx:id=\"dualizeButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert orientateButton != null : "fx:id=\"orientateButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert maximizeButton != null : "fx:id=\"maximizeButton\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert showBandsCheckBox != null : "fx:id=\"showBandsCheckBox\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert bandWidthSpinner != null : "fx:id=\"bandWidthSpinner\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert bandsColorPicker != null : "fx:id=\"bandsColorPicker\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert showFacesCheckBox != null : "fx:id=\"showFacesCheckBox\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert smoothEdgesCheckBox != null : "fx:id=\"smoothEdgesCheckBox\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert backEdgesCheckBox != null : "fx:id=\"backEdgesCheckBox\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert tile1ColorPicker != null : "fx:id=\"tile1ColorPicker\" was not injected: check your FXML file 'EditorTab.fxml'.";
        assert backgroundColorPicker != null : "fx:id=\"backgroundColorPicker\" was not injected: check your FXML file 'EditorTab.fxml'.";

    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public ChoiceBox<?> getModelChoiceBox() {
        return modelChoiceBox;
    }

    public Button getShowMoreTilesButton() {
        return showMoreTilesButton;
    }

    public Button getShowLessTilesButton() {
        return showLessTilesButton;
    }

    public ToggleButton getShowRotationsToggleButton() {
        return showRotationsToggleButton;
    }

    public ToggleButton getToolsToggleButton() {
        return toolsToggleButton;
    }

    public Button getTestButton() {
        return testButton;
    }

    public AnchorPane getMainAnchorPane() {
        return mainAnchorPane;
    }

    public Pane getMainPane() {
        return mainPane;
    }

    public ToolBar getRotationsToolBar() {
        return rotationsToolBar;
    }

    public TextField getGroupTextField() {
        return groupTextField;
    }

    public Spinner<?> getV1Spinner() {
        return v1Spinner;
    }

    public Spinner<?> getV2Spinner() {
        return v2Spinner;
    }

    public Spinner<?> getV3Spinner() {
        return v3Spinner;
    }

    public Spinner<?> getV4Spinner() {
        return v4Spinner;
    }

    public Spinner<?> getV5Spinner() {
        return v5Spinner;
    }

    public Spinner<?> getV6Spinner() {
        return v6Spinner;
    }

    public Spinner<?> getV7Spinner() {
        return v7Spinner;
    }

    public Spinner<?> getV8Spinner() {
        return v8Spinner;
    }

    public Spinner<?> getV9Spinner() {
        return v9Spinner;
    }

    public Spinner<?> getV10Spinner() {
        return v10Spinner;
    }

    public ToolBar getToolsToolBar() {
        return toolsToolBar;
    }

    public Button getDualizeButton() {
        return dualizeButton;
    }

    public Button getOrientateButton() {
        return orientateButton;
    }

    public Button getMaximizeButton() {
        return maximizeButton;
    }

    public CheckBox getShowBandsCheckBox() {
        return showBandsCheckBox;
    }

    public Spinner<?> getBandWidthSpinner() {
        return bandWidthSpinner;
    }

    public ColorPicker getBandsColorPicker() {
        return bandsColorPicker;
    }

    public CheckBox getShowFacesCheckBox() {
        return showFacesCheckBox;
    }

    public CheckBox getSmoothEdgesCheckBox() {
        return smoothEdgesCheckBox;
    }

    public CheckBox getBackEdgesCheckBox() {
        return backEdgesCheckBox;
    }

    public ColorPicker getTile1ColorPicker() {
        return tile1ColorPicker;
    }

    public ColorPicker getBackgroundColorPicker() {
        return backgroundColorPicker;
    }
}
