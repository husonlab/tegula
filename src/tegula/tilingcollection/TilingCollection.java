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

package tegula.tilingcollection;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import jloda.fx.control.AnotherMultipleSelectionModel;
import jloda.fx.util.AService;
import jloda.fx.util.NotificationManager;
import jloda.util.Basic;
import jloda.util.ProgressListener;
import tegula.core.dsymbols.DSymbol;
import tegula.util.IFileBased;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * a collection of tilings
 * Daniel Huson, 4.2019
 */
public class TilingCollection implements IFileBased {
    private final StringProperty fileName = new SimpleStringProperty("Untitled");

    private final ObservableList<DSymbol> dSymbols = FXCollections.observableArrayList();
    private final IntegerProperty size = new SimpleIntegerProperty(0);
    private final AnotherMultipleSelectionModel<DSymbol> selectionModel;

    private final IntegerProperty blockSize = new SimpleIntegerProperty(60);

    /**
     * setup a new tiling collection
     *
     * @param fileName
     */
    public TilingCollection(String fileName) {
        setFileName(fileName);
        size.bind((Bindings.size(dSymbols)));
        this.selectionModel = new AnotherMultipleSelectionModel<>();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        dSymbols.addListener((InvalidationListener) (c) -> {
            selectionModel.setItems(dSymbols);
        });
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
                    final ProgressListener progress = aService.getProgressListener();
                    progress.setTasks("Loading", getFileName());
                    progress.setMaximum(Basic.guessUncompressedSizeOfFile(getFileName()));

                    try (BufferedReader br = new BufferedReader(Basic.getReaderPossiblyZIPorGZIP(getFileName()))) {
                        final ArrayList<DSymbol> cache = new ArrayList<>(getBlockSize());
                        String line;
                        while ((line = br.readLine()) != null) {
                            final DSymbol dSymbol = new DSymbol();
                            dSymbol.read(new StringReader(line));
                            cache.add(dSymbol);
                            if (cache.size() == getBlockSize()) {
                                final DSymbol[] array = cache.toArray(new DSymbol[0]);
                                cache.clear();
                                Platform.runLater(() -> dSymbols.addAll(array));
                            }
                            progress.setProgress(progress.getProgress() + getBlockSize()); // wild guess
                        }
                        if (cache.size() > 0)
                            Platform.runLater(() -> dSymbols.addAll(cache));
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

    public AnotherMultipleSelectionModel<DSymbol> getSelectionModel() {
        return selectionModel;
    }

    public int getSize() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }

    public DSymbol findSymbol(String text) {
        final String numbers = Basic.getFirstWord(text);
        for (DSymbol dSymbol : dSymbols) {
            if (numbers.equals(String.format("%d.%d", dSymbol.getNr1(), dSymbol.getNr2())))
                return dSymbol;
        }
        return null;
    }

    public int getBlockSize() {
        return blockSize.get();
    }

    public IntegerProperty blockSizeProperty() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize.set(blockSize);
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getTitle() {
        return Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName.get()), "");
    }

}
