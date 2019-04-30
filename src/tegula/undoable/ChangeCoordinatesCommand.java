/*
 * ChangeCoordinatesCommand.java Copyright (C) 2019. Daniel H. Huson
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
import tegula.core.dsymbols.FDomain;

import java.util.function.Consumer;

/**
 * change coordinates undoable command
 * Daniel Huson, 4.2019
 */
public class ChangeCoordinatesCommand extends UndoableRedoableCommand {
    private final Runnable undo;
    private final Runnable redo;

    public ChangeCoordinatesCommand(Point2D[][] oldCoordinates, Point2D[][] newCoordinates, Consumer<Point2D[][]> coordinatesApplicator) {
        super("Change coordinates");

        System.err.println("Change coordinates OLD:");
        FDomain.reportCoordinates(oldCoordinates);
        System.err.println("Change coordinates NEW:");
        FDomain.reportCoordinates(newCoordinates);

        undo = () -> coordinatesApplicator.accept(oldCoordinates);
        redo = () -> coordinatesApplicator.accept(newCoordinates);
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
