package tiler.next;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CollectionTabController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane borderPane;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private Button firstTilingButton;

    @FXML
    private Button previousTilingButton;

    @FXML
    private TextField tilingNumberTextField;

    @FXML
    private Button nextTilingButton;

    @FXML
    private Button lastTilingButton;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private FlowPane flowPane;

    @FXML
    private Slider sizeSlider;

    @FXML
    void initialize() {
        assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert firstTilingButton != null : "fx:id=\"firstTilingButton\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert previousTilingButton != null : "fx:id=\"previousTilingButton\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert tilingNumberTextField != null : "fx:id=\"tilingNumberTextField\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert nextTilingButton != null : "fx:id=\"nextTilingButton\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert lastTilingButton != null : "fx:id=\"lastTilingButton\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert scrollPane != null : "fx:id=\"scrollPane\" was not injected: check your FXML file 'CollectionTab.fxml'.";
        assert flowPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'CollectionTab.fxml'.";

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

    public Button getFirstTilingButton() {
        return firstTilingButton;
    }

    public Button getPreviousTilingButton() {
        return previousTilingButton;
    }

    public TextField getTilingNumberTextField() {
        return tilingNumberTextField;
    }

    public Button getNextTilingButton() {
        return nextTilingButton;
    }

    public Button getLastTilingButton() {
        return lastTilingButton;
    }

    public AnchorPane getMainAnchorPane() {
        return mainAnchorPane;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public FlowPane getFlowPane() {
        return flowPane;
    }

    public Slider getSizeSlider() {
        return sizeSlider;
    }
}
