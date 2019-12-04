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

import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import tegula.dbcollection.DBCollection;
import tegula.dbcollection.DBCollectionTab;
import tegula.filecollection.FileCollection;
import tegula.filecollection.FileCollectionTab;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * opens a file or database
 * Daniel Huson, 2018
 */
public class FileOpener implements Consumer<String> {
    public void accept(String fileName) {
        final File file;
        if (fileName.startsWith("select:")) {
            file = new File(fileName);
        } else {
            try {
                file = (new File(fileName)).getCanonicalFile();
                System.err.println("File: " + file);
            } catch (IOException e) {
                Basic.caught(e);
                return;
            }
        }
        final MainWindow window = (MainWindow) MainWindowManager.getInstance().getLastFocusedMainWindow();
        if (window == null)
            return;
        try {
            if (file.getPath().endsWith(".tdb")) {
                try {
                    final DBCollection dbCollection = new DBCollection(file.getPath());
                    final DBCollectionTab dbCollectionTab = new DBCollectionTab(window, dbCollection);
                    window.getMainTabPane().getTabs().add(dbCollectionTab);
                    RecentFilesManager.getInstance().insertRecentFile(file.getPath());
                } catch (IOException | SQLException ex) {
                    NotificationManager.showError(("Open failed: " + ex.getMessage()));
                }
            } else if (file.isFile() || file.getPath().startsWith("select:")) {
                final FileCollection FileCollection = new FileCollection(file.getPath());
                final FileCollectionTab tab = new FileCollectionTab(window, FileCollection);
                window.getMainTabPane().getTabs().add(tab);
                RecentFilesManager.getInstance().insertRecentFile(file.getPath());
            }
        } catch (Exception ex) {
            NotificationManager.showError("Open '" + file + "' failed: " + ex.getMessage());
        }
    }
}
