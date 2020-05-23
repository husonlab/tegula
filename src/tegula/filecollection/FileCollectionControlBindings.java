/*
 * FileCollectionPresenter.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.filecollection;

import javafx.scene.Cursor;

/**
 * presenter
 * Daniel Huson, 10.2019
 */
public class FileCollectionControlBindings {

    /**
     * constructor
     *
     * @param fileCollectionTab
     */
    public static void setup(FileCollectionTab fileCollectionTab) {
        FileCollection fileCollection = fileCollectionTab.getFileCollection();
        FileCollectionTabController controller = fileCollectionTab.getController();

        fileCollectionTab.getFileCollection().totalCountProperty().addListener((c, o, n) -> {
            if (n != null && n.intValue() == -1)
                fileCollectionTab.getRoot().setCursor(Cursor.WAIT);
            else
                fileCollectionTab.getRoot().setCursor(Cursor.DEFAULT);
        });

        fileCollectionTab.tabPaneProperty().addListener((c, o, n) -> {
            if (n != null) {
                controller.getPagination().setPrefWidth(fileCollectionTab.getTabPane().getWidth());
                controller.getPagination().setPrefHeight(fileCollectionTab.getTabPane().getHeight());

                controller.getPagination().prefWidthProperty().bind(n.widthProperty());
                controller.getPagination().prefHeightProperty().bind(n.heightProperty());
            }
        });

        fileCollection.totalCountProperty().addListener((c, o, n) -> controller.getCountLabel().setText(n == null ? "?" : String.format("Found: %,d", n.intValue())));
        controller.getSizeSlider().setOnMouseReleased((e) -> fileCollectionTab.updatePageSize());

        fileCollectionTab.getMainWindow().getStage().widthProperty().addListener((c, o, n) -> fileCollectionTab.updatePageSize());
        fileCollectionTab.getMainWindow().getStage().heightProperty().addListener((c, o, n) -> fileCollectionTab.updatePageSize());
    }
}
