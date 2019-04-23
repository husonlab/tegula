/*
 *  Copyright (C) 2018. Daniel H. Huson
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

package tegula.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import jloda.fx.util.AService;
import jloda.fx.util.ProgramPropertiesFX;
import jloda.fx.window.MainWindowManager;
import tegula.main.Document;
import tegula.main.Version;
import tegula.util.TilingFileFilter;

import java.io.File;
import java.io.FileReader;
import java.util.function.Consumer;

public class OpenFileAction implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
        File previousDir = new File(ProgramPropertiesFX.get("OpenFileDir", ""));

        final FileChooser fileChooser = new FileChooser();
        if (previousDir.isDirectory())
            fileChooser.setInitialDirectory(previousDir);
        fileChooser.setTitle("Open File - " + Version.NAME);
        fileChooser.getExtensionFilters().addAll(TilingFileFilter.getInstance());
        final File selectedFile = fileChooser.showOpenDialog(MainWindowManager.getInstance().getLastFocusedMainWindow().getStage());

        if (selectedFile != null) {
            ProgramPropertiesFX.put("OpenFileDir", selectedFile.getParent());
            fileOpener().accept(selectedFile.getPath());
        }
    }

    /**
     * load a file
     *
     * @param document
     * @param fileName
     */
    public static void loadFile(final Document document, final Pane statusBar, final String fileName) {
        // document.clear();

        final AService<Boolean> aService = new AService<>(statusBar);
        aService.setCallable(() -> {
                    try (FileReader reader = new FileReader(fileName)) {
                        //  document.read(reader);
                    }
                    return true;
                }
        );
        aService.setOnSucceeded((e) -> {
            /*
            document.setFileName(fileName);
            NotificationManager.showInformation(String.format("Document '%s' loaded: %,d tilings", document.getName(), document.size()));
            RecentFilesManager.getInstance().insertRecentFile(fileName);
            */
        });
        aService.start();
    }

    /**
     * file opener
     *
     * @return filer opener for use with recent files menu
     */
    public static Consumer<String> fileOpener() {
        return fileName -> {
            /*
            if (Basic.fileExistsAndIsNonEmpty(fileName)) {
                final MainWindow mainWindow = (MainWindow) MainWindowManager.getInstance().createAndShowWindow(true);
                loadFile(mainWindow.getDocument(), mainWindow.getStatusPane(), fileName);
            } else
                NotificationManager.showError("File not found or empty: " + fileName);

             */
        };
    }

}
