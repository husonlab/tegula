/*
 * ICollectionTab.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.dbcollection;

import javafx.beans.property.BooleanProperty;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.fx.util.Printable;
import tegula.core.dsymbols.DSymbol;
import tegula.main.MainWindow;
import tegula.main.TilingStyle;
import tegula.util.IFileBased;

import java.io.Closeable;
import java.util.function.Function;

public interface ICollectionTab extends Closeable, Printable, IFileBased {
    int FirstPage = Integer.MIN_VALUE;
    int LastPage = Integer.MAX_VALUE;

    boolean isShowLabels();

    void setShowLabels(boolean show);

    BooleanProperty showLabelsProperty();

    boolean isColorPreview();

    void setColorPreview(boolean show);

    BooleanProperty colorPreviewProperty();

    Function<DSymbol, String> getLabelGetter();

    void updatePageSize();

    TilingStyle getTilingStyle();

    MainWindow getMainWindow();

    AMultipleSelectionModel<DSymbol> getSelectionModel();

    void gotoPage(int page);

    int getNumberOfPages();

    void changePreviewSize(boolean larger);
}
