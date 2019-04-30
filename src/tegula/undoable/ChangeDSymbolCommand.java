/*
 * ChangeDSymbolCommand.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.geometry.Point2D;
import jloda.fx.undo.UndoableRedoableCommand;
import tegula.core.dsymbols.DSymbol;

import java.util.function.Consumer;

/**
 * change dsymbol undoable command
 * Daniel Huson, 4.2019
 */
public class ChangeDSymbolCommand extends UndoableRedoableCommand {
    private final Runnable undo;
    private final Runnable redo;

    public ChangeDSymbolCommand(DSymbol oldDs, DSymbol newDs, Consumer<DSymbol> replaceDSymbol, Point2D[][] oldCoordinates, Consumer<Point2D[][]> coordinatesSetter) {
        super("Change D-symbol");
        final DSymbol newDsCopy = new DSymbol(newDs);

        undo = () -> {
            replaceDSymbol.accept(oldDs);
            coordinatesSetter.accept(oldCoordinates);
        };

        redo = () -> {
            replaceDSymbol.accept(newDsCopy);
        };

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
