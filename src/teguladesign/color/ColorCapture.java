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
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import jloda.fx.util.ColorSchemeManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ColorCapture {
    private ImageView imageView;

    private final ColorCaptureController controller;
    private final Parent root;

    public ColorCapture (ColorSchemeManager colorSchemeManager) {
        final ExtendedFXMLLoader<ColorCaptureController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        final Pane pane=controller.getDropTarget();

        pane.setOnDragOver((e)->mouseDragOver(e,pane));

        pane.setOnDragDropped((e)->mouseDragDropped(e,pane));

        pane.setOnDragExited((e)->pane.setStyle("-fx-border-color: #C6C6C6;"));
    }

    void addImage(Image i, Pane pane){

        imageView = new ImageView();
        imageView.setImage(i);
        pane.getChildren().setAll(imageView);
    }

    private void mouseDragDropped(final DragEvent e,final Pane contentPane) {
        final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            // Only get the first file from the list
            final File file = db.getFiles().get(0);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println(file.getAbsolutePath());
                    try {
                        final Image img = new Image(new FileInputStream(file.getAbsolutePath()));

                        addImage(img, contentPane);
                    } catch (FileNotFoundException ex) {
                        Basic.caught(ex);
                    }
                }
            });
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
                contentPane.setStyle("-fx-border-color: red;"
                        + "-fx-border-width: 5;"
                        + "-fx-background-color: #C6C6C6;"
                        + "-fx-border-style: solid;");
                e.acceptTransferModes(TransferMode.COPY);
            }
        } else {
            e.consume();
        }
    }

}
