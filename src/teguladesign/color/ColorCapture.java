/*
 * ColorCapture.java Copyright (C) 2019. Daniel H. Huson
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

package teguladesign.color;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.control.FlowPaneDragAndDrop;
import jloda.fx.util.ColorSchemeManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.NotificationManager;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

/**
 * color capture dialog
 * Daniel Huson, 5.2019
 */
public class ColorCapture {
    private final ImageView imageView=new ImageView();

    private final ObservableList<Rectangle> colorSwatches=FXCollections.observableArrayList();

    private final ColorCaptureController controller;
    private final Parent root;
    private final Stage stage;

    private final ObservableList<Color> colors= FXCollections.observableArrayList();

    public ColorCapture (ColorSchemeManager colorSchemeManager) {
        final ExtendedFXMLLoader<ColorCaptureController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        final Pane pane=controller.getImagePane();

        pane.setOnDragOver((e)->mouseDragOver(e,pane));

        pane.setOnDragDropped((e)->mouseDragDropped(e,pane,controller.getNameTextField()));

        pane.setOnDragExited((e)->pane.setStyle("-fx-border-color: #C6C6C6;"));

        final Scene scene=new Scene(root);
        stage=new Stage();
        stage.setTitle("Color Scheme Capture");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setMinWidth(((Pane)root).getPrefWidth());
        stage.setMinHeight(((Pane)root).getPrefHeight());
        stage.show();

        setupMouseHandler(imageView);
        FlowPaneDragAndDrop.setup(controller.getColorsFlowPane());

        controller.getClearButton().setOnAction((e)->{
            controller.getNameTextField().setText("");
            controller.getImagePane().getChildren().setAll(controller.getDropLabel());
            imageView.setImage(null);
            colors.clear();
        });
        controller.getClearButton().disableProperty().bind(Bindings.isEmpty(colors));

        controller.getCancelButton().setOnAction((e)->{
            controller.getClearButton().getOnAction().handle(null);
            stage.hide();
        });

        controller.getApplyButton().setOnAction((e)->{
            final String name=controller.getNameTextField().getText();
            if(colorSchemeManager.getNames().contains(name)) {
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("A color scheme with name '"+name+"' already exists.");
                alert.setContentText("Ok to overwrite it?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK){
                   return;
                }
            }
            colorSchemeManager.setColorScheme(name,colors);
            NotificationManager.showInformation("Saved color scheme '"+name+"'");
        });
        controller.getApplyButton().disableProperty().bind(controller.getNameTextField().textProperty().length().isEqualTo(0)
        .or(Bindings.isEmpty(colors)));

       imageView.imageProperty().addListener((e)-> {
           Platform.runLater(()->ColorExtraction.apply(imageView, controller.getSeparationSlider().getValue(), colors,256));
           controller.getNameTextField().setText("");
       });
       imageView.viewportProperty().addListener((e)-> Platform.runLater(()->ColorExtraction.apply(imageView, controller.getSeparationSlider().getValue(), colors,256)));

        controller.getSeparationSlider().valueProperty().addListener((c, o, n) -> {
            final double value = Math.round(n.doubleValue());
            ColorExtraction.apply(imageView, value, colors, 256);
            controller.getSeparationSlider().setTooltip(new Tooltip(String.format("Percent separation of colors: %d", (int) value)));
        });

        colorSwatches.addListener((InvalidationListener)(e)->{
            controller.getColorsFlowPane().getChildren().setAll(colorSwatches);
        });

        controller.getImagePane().setOnMouseClicked((e)->{
            if(e.getClickCount()==2) {
                final Image image=getImageFromFileDialog(stage);
                if(image!=null)
                    addImage(image,controller.getImagePane());
            }
        });

            colors.addListener((ListChangeListener<Color>)(e)->{
                while(e.next()) {
                    for(Color color:e.getAddedSubList()) {
                        final Rectangle rectangle=new Rectangle(32,32);
                        rectangle.setFill(color);
                        rectangle.setStroke(Color.LIGHTGRAY);
                        Tooltip.install(rectangle,new Tooltip(color.toString()));
                        colorSwatches.add(rectangle);
                    }
                    for(Color color:e.getRemoved()) {
                        for(Node node:controller.getColorsFlowPane().getChildren()) {
                            if(node instanceof Rectangle && ((Rectangle) node).getFill().equals(color)) {
                                colorSwatches.remove(node);
                                break;
                            }
                        }
                    }
                }
                controller.getCountLabel().setText(""+colors.size());
            });

            scene.setOnKeyPressed((e)->{
                if(e.isMetaDown()  && e.getCode()== KeyCode.V) {
                    final Clipboard cb = Clipboard.getSystemClipboard();
                    if (cb.hasImage()) {
                        Image image = cb.getImage();
                        addImage(image,controller.getImagePane());
                    }
                }
            });
    }

    public Stage getStage() {
        return stage;
    }

    private void addImage(Image image, Pane imagePane){
        imageView.setImage(image);
        final Rectangle2D rectangle=new Rectangle2D(0,0,image.getWidth(),image.getHeight());
        imageView.setViewport(rectangle);
        imageView.setFitWidth(imagePane.getWidth());
        imageView.setFitHeight(imagePane.getHeight());
        imageView.setSmooth(true);

        final Group group=new Group();
        group.getChildren().add(imageView);

        imagePane.getChildren().setAll(group);
    }

    private void mouseDragDropped(final DragEvent e,final Pane contentPane,final TextField name) {
        final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            // Only get the first file from the list
            final File file = db.getFiles().get(0);
            if(file.canRead()) {
                if(name.getText().length()==0)
                    name.setText(Basic.replaceFileSuffix(file.getName(),""));
                success = true;
                Platform.runLater(() -> {
                    try {
                        final Image img = new Image(new FileInputStream(file.getAbsolutePath()));
                        addImage(img, contentPane);
                    } catch (FileNotFoundException ex) {
                        Basic.caught(ex);
                    }
                });
            }
        }
        e.setDropCompleted(success);
        e.consume();
    }

    private  void mouseDragOver(final DragEvent e,final Pane contentPane) {
        final Dragboard db = e.getDragboard();

        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".png")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpeg")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".jpg");

        if (db.hasFiles()) {
            if (isAccepted) {
                contentPane.setStyle("-fx-border-color: cornflowerblue;"
                        + "-fx-border-width: 3;"
                        + "-fx-background-color: #C6C6C6;"
                        + "-fx-border-style: solid;");
                e.acceptTransferModes(TransferMode.COPY);
            }
        } else {
            e.consume();
        }
    }

    private double mouseDownX;
    private double mouseDownY;
    
    private void setupMouseHandler(ImageView imageView) {
        imageView.setOnMousePressed((e)->{
            mouseDownX=e.getSceneX();
            mouseDownY=e.getSceneY();
        });
        imageView.setOnMouseDragged((e)->{
            final double deltaX=e.getSceneX()-mouseDownX;
            final double deltaY=e.getSceneY()-mouseDownY;

            final Rectangle2D oldRect=imageView.getViewport();
            final Rectangle2D newRect=new Rectangle2D(oldRect.getMinX()-deltaX,oldRect.getMinY()-deltaY,oldRect.getWidth(),oldRect.getHeight());
            imageView.setViewport(newRect);

            mouseDownX=e.getSceneX();
            mouseDownY=e.getSceneY();
        });
        
        imageView.setOnScroll((e)->{
            final double factorX;
            if(e.getDeltaX()>0)
                factorX=1.1;
            else if(e.getDeltaX()<0)
                factorX=1/1.1;
            else
                factorX=1;
            final double factorY;
            if(e.getDeltaY()>0)
                factorY=1.1;
            else if(e.getDeltaY()<0)
                factorY=1/1.1;
            else
                factorY=1;
            final Rectangle2D oldRect=imageView.getViewport();
            final Rectangle2D newRect=new Rectangle2D(oldRect.getMinX()+(1-factorX)*oldRect.getWidth(),oldRect.getMinY()+(1-factorY)*oldRect.getHeight(),
                    factorX*oldRect.getWidth(),factorY*oldRect.getHeight());
            imageView.setViewport(newRect);
        });

    }

    private Image getImageFromFileDialog(Stage stage) {
        final FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("Load image file");
        fileChooser.setInitialDirectory(new File(ProgramProperties.get("ColorSchemeInputDirectory", ".")));

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image file", "*.png","*.jpg","*.jpeg","*.bmp","*.gif"));

        final File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            ProgramProperties.put("ColorSchemeInputDirectory", file.getParent());
            try {
                return new Image(new FileInputStream(file.getAbsolutePath()));
            } catch (FileNotFoundException e) {
                Basic.caught(e);
            }
        }
        return null;
    }

}
