/*
 *  Copyright (C) 2019. Daniel H. Huson
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import tegula.tilingcollection.TilingCollection;

import java.io.File;

/**
 * the document associated with a main window
 * Daniel Huson, 4.0219
 */
public class Document {
    private final ObservableMap<File, TilingCollection> file2tilingCollection = FXCollections.observableHashMap();

    public ObservableMap<File, TilingCollection> getFile2tilingCollection() {
        return file2tilingCollection;
    }
}
