/*
 * FileCollection.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.filecollection;

import javafx.beans.property.*;
import jloda.util.FileUtils;
import jloda.util.ProgramProperties;
import tegula.core.dsymbols.DSymbol;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.util.ArrayList;

/**
 * opens a collection of tilings from a file
 * Daniel Huson, 10.2019
 */
public class FileCollection implements Closeable, IFileBased {
    private final ArrayList<String> lines = new ArrayList<>();
    private final StringProperty fileName = new SimpleStringProperty();

    private final IntegerProperty pageSize = new SimpleIntegerProperty(1);
    private final IntegerProperty totalCount = new SimpleIntegerProperty(0);

    /**
     * constructor
     *
	 */
    public FileCollection(String inputFile) {
        fileName.set(inputFile);
    }

    public int getNumberOfPages() {
        return (int) Math.ceil((float) getTotalCount() / getPageSize());
    }

    public int getNumberOfDSymbolsOnPage(int pageNumber) {
        return pageNumber < getNumberOfPages() ? getPageSize() : getTotalCount() - pageNumber * getPageSize();
    }

    public void setLines(ArrayList<String> lines) {
        this.lines.addAll(lines);
        totalCount.set(lines.size());
    }

    /**
     * get all D-symbols for the given page number
     *
     * @param pageNumber 1-based
     * @return D-symbols
	 */
    public ArrayList<DSymbol> getPageOfDSymbols(int pageNumber) {
        final ArrayList<DSymbol> result = new ArrayList<>();
        if (pageNumber >= 0 && pageNumber < getNumberOfPages()) {
            for (int i = (pageNumber) * getPageSize(); i < Math.min(getTotalCount(), (pageNumber + 1) * getPageSize()); i++) {
                result.add(new DSymbol(lines.get(i)));
            }
        }
        //count.set(result.size());
        return result;
    }

    public int getPageSize() {
        return pageSize.get();
    }

    public IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize.set(pageSize);
    }

    @Override
    public void close() {
    }

    public int getTotalCount() {
        return totalCount.get();
    }

    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount;
    }

    @Override
    public String getFileName() {
        return fileName.get();
    }

    @Override
    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        //this.fileName.set(fileName);
    }

    @Override
    public String getTitle() {
		return String.format("%s - %s", FileUtils.getFileNameWithoutPath(getFileName()), ProgramProperties.getProgramVersion());
    }

    public ArrayList<String> getLines() {
        return lines;
    }
}
