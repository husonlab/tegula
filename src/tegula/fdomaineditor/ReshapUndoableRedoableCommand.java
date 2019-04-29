/*
 * ReshapUndoableRedoableCommand.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.fdomaineditor;

import javafx.geometry.Point2D;
import jloda.fx.undo.UndoableRedoableCommand;
import tegula.core.dsymbols.FDomain;
import tegula.util.Updateable;

public class ReshapUndoableRedoableCommand extends UndoableRedoableCommand {
    private final FDomain fDomain;
    private final Updateable updateable;
    private Point2D[][] oldCoordinates;
    private Point2D[][] newCoordinates;

    ReshapUndoableRedoableCommand(FDomain fDomain, Updateable updateable) {
        super("Reshape");
        this.fDomain = fDomain;
        this.updateable = updateable;
        oldCoordinates = fDomain.getCoordinates();
    }

    public void saveNewCoordinates() {
        newCoordinates = fDomain.getCoordinates();
    }

    @Override
    public void undo() {
        fDomain.setCoordinates(oldCoordinates);
        updateable.update();
    }

    @Override
    public void redo() {
        fDomain.setCoordinates(newCoordinates);
        updateable.update();
    }

    @Override
    public boolean isUndoable() {
        return oldCoordinates != null;
    }

    @Override
    public boolean isRedoable() {
        return newCoordinates != null;
    }
}
