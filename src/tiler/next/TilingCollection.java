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

package tiler.next;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import jloda.fx.util.AService;
import jloda.fx.util.NotificationManager;
import jloda.util.Basic;
import tiler.core.dsymbols.DSymbol;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * a collection of tilings
 * Daniel Huson, 4.2019
 */
public class TilingCollection extends FileBased {
    private final ObservableList<DSymbol> dSymbols = FXCollections.observableArrayList();
    private final IntegerProperty size = new SimpleIntegerProperty(0);

    /**
     * setup a new tiling collection
     *
     * @param fileName
     */
    public TilingCollection(String fileName) {
        setFileName(fileName);
        size.bind((Bindings.size(dSymbols)));
    }

    /**
     * load tilings from file
     *
     * @param statusPane
     * @param runAfterwards if non null, will be run in FX thread after loading
     */
    public void load(Pane statusPane, Runnable runAfterwards) {
        final AService<Boolean> aService = new AService<>(statusPane);

        aService.setCallable(() -> {
                    try (BufferedReader br = new BufferedReader(Basic.getReaderPossiblyZIPorGZIP(getFileName()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            final DSymbol dSymbol = new DSymbol();
                            dSymbol.read(new StringReader(line));
                            Platform.runLater(() -> dSymbols.add(dSymbol));
                        }
                    }
                    return true;
                }
        );
        aService.setOnCancelled((e) -> NotificationManager.showInformation(String.format("CANCELED, loaded %,d tilings", getSize())));
        aService.setOnSucceeded((e) -> {
            NotificationManager.showInformation(String.format("Loaded %,d tilings", getSize()));
            if (runAfterwards != null)
                runAfterwards.run();
        });
        aService.start();
    }


    private static FileChooser.ExtensionFilter instance;

    /**
     * gets an extension filter
     *
     * @return extension filter
     */
    public static FileChooser.ExtensionFilter getExtensionFilter() {
        if (instance == null)
            instance = new FileChooser.ExtensionFilter("Tilings File", "*.tgs", "*.tgs.gz");
        return instance;
    }

    public ObservableList<DSymbol> getDSymbols() {
        return dSymbols;
    }

    public int getSize() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }
}
