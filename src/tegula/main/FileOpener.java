/*
 * FileOpener.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.main;

import jloda.fx.util.NotificationManager;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.util.Basic;
import tegula.tilingcollection.TilingCollection;
import tegula.tilingcollection.TilingCollectionTab;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class FileOpener implements Consumer<String> {
    public void accept(String fileName) {
        final File file;
        try {
            file = (new File(fileName)).getCanonicalFile();
            System.err.println("File: " + file);
        } catch (IOException e) {
            Basic.caught(e);
            return;
        }
        final MainWindow window = (MainWindow) MainWindowManager.getInstance().getLastFocusedMainWindow();
        if (window == null)
            return;
        try {
            if (file.isFile()) {
                TilingCollection tilingCollection = window.getDocument().getFile2tilingCollection().get(file);
                if (tilingCollection == null) {
                    tilingCollection = new TilingCollection(file.getPath());
                    window.getDocument().getFile2tilingCollection().put(file, tilingCollection);
                    tilingCollection.load(window.getStatusPane(), null);
                }
                if (window.getFile2CollectionTab().get(file) == null) {
                    final TilingCollectionTab tab = new TilingCollectionTab(window, tilingCollection);
                    tab.setOnClosed((f) -> {
                        window.getDocument().getFile2tilingCollection().remove(file);
                        window.getFile2CollectionTab().remove(file);
                        tab.close();
                    });
                    window.getFile2CollectionTab().put(file, tab);
                    window.getMainTabPane().getTabs().add(tab);
                }
                window.getStage().toFront();
                window.getMainTabPane().getSelectionModel().select(window.getFile2CollectionTab().get(file));
                RecentFilesManager.getInstance().insertRecentFile(file.getPath());

            }
        } catch (Exception ex) {
            NotificationManager.showError("Open file failed: " + ex.getMessage());
        }
    }
}
