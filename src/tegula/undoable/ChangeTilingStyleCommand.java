/*
 * ChangeTilingStyleCommand.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.undoable;

import jloda.fx.undo.UndoableRedoableCommand;
import tegula.main.TilingStyle;

public class ChangeTilingStyleCommand extends UndoableRedoableCommand {
    private final TilingStyle oldTilingStyle;

    private final Runnable undo;
    private final Runnable redo;

    public ChangeTilingStyleCommand(TilingStyle tilingStyle, Runnable change, Runnable updater) {
        super("change style");

        this.oldTilingStyle = new TilingStyle(tilingStyle);

       undo = () -> {
           tilingStyle.copy(oldTilingStyle);
           updater.run();
       };

        redo = change;

    }

    @Override
    public void undo() {
        undo.run();

    }

    @Override
    public void redo() {
        redo.run();
    }
}
