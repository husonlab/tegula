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
package jloda.fx;

import javafx.application.Platform;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import jloda.util.ProgramProperties;

import java.util.Optional;

/**
 * print a  node
 * Daniel Huson, 1.2018
 */
public class Print {
    private static PageLayout pageLayoutSelected;

    /**
     * print the given node
     *
     * @param owner
     * @param node
     */
    public static void print(Stage owner, Node node) {
        final PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            if (job.showPrintDialog(owner)) {
                //System.err.println(job.getJobSettings());

                final PageLayout pageLayout = (pageLayoutSelected != null ? pageLayoutSelected : job.getJobSettings().getPageLayout());

                final Scale scale;
                if (node.getBoundsInParent().getWidth() > pageLayout.getPrintableWidth() || node.getBoundsInParent().getHeight() > pageLayout.getPrintableHeight()) {
                    if (true) {
                        System.err.println(String.format("Scene size (%.0f x %.0f) exceeds printable area (%.0f x %.0f), scaled to fit", node.getBoundsInParent().getWidth(),
                                node.getBoundsInParent().getHeight(), pageLayout.getPrintableWidth(), pageLayout.getPrintableHeight()));
                        double factor = Math.min(pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth(), pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight());
                        scale = new Scale(factor, factor);
                        node.getTransforms().add(scale);
                    } else {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                        alert.initOwner(owner);
                        alert.setTitle("Scale Before Printing - " + ProgramProperties.getProgramName());
                        alert.setHeaderText(String.format("Scene size (%.0f x %.0f) exceeds printable area (%.0f x %.0f)", node.getBoundsInParent().getWidth(),
                                node.getBoundsInParent().getHeight(), pageLayout.getPrintableWidth(), pageLayout.getPrintableHeight()));
                        alert.setContentText("Scale to fit printable area?");
                        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
                        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent()) {
                            if (result.get() == buttonTypeYes) {
                                final double factor = Math.min(pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth(), pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight());
                                scale = new Scale(factor, factor);
                                node.getTransforms().add(scale);
                            } else if (result.get() == buttonTypeCancel)
                                return;
                            else
                                scale = null;
                        } else
                            scale = null;
                    }
                } else
                    scale = null;

                job.jobStatusProperty().addListener((c, o, n) -> {
                    //System.err.println("Status: " + o + " -> " + n);
                    if (scale != null && n != PrinterJob.JobStatus.NOT_STARTED && n != PrinterJob.JobStatus.PRINTING) {
                        Platform.runLater(() -> node.getTransforms().remove(scale));
                    }
                });
                if (job.printPage(pageLayout, node))
                    job.endJob();
            }
        } else
            new Alert("Failed to create Printer Job");
    }

    /**
     * show the page layout dialog
     *
     * @param owner
     */
    public static void showPageLayout(Stage owner) {
        final PrinterJob job = PrinterJob.createPrinterJob();
        if (job.showPageSetupDialog(owner)) {
            pageLayoutSelected = (job.getJobSettings().getPageLayout());
        }
    }
}
