/*
 * GroupEditingControls.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.tilingeditor;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import jloda.fx.undo.UndoManager;
import tegula.core.dsymbols.DSymbol;
import tegula.tilingpane.TilingPane;
import tegula.undoable.ChangeDSymbolCommand;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * manages editing of symmetry group
 * Created by huson on 4/22/16.
 */
public class GroupEditingControls {
    /**
     * update the group editing buttons
     *
     * @param tilingEditorTab
     */
    public static void setup(TilingEditorTab tilingEditorTab) {
        final TilingPane tilingPane = tilingEditorTab.getTilingPane();
        final DSymbol ds = tilingPane.getTiling().getDSymbol();
        final UndoManager undoManager = tilingEditorTab.getUndoManager();

        final VBox vbox = tilingEditorTab.getController().getSymmetiesVBox();
        if (vbox.getChildren().size() > 1)
            vbox.getChildren().remove(1, vbox.getChildren().size());

        // setup the rotation controls:
        int count = 0;
        for (int k = 0; k <= 2; k++) {
            final int i = DSymbol.i(k);
            final int j = DSymbol.j(k);

            for (final int a : ds.orbits(i, j)) {
                if (k == 1 && ds.getVij(i, j, a) != 2)
                    continue; // is an edge without a rotation

                final IntegerChooser vChooser = new IntegerChooser(k == 1);
                vbox.getChildren().add(vChooser);

                vChooser.setValue(ds.getVij(i, j, a));

                final ChangeListener<Number> listener = ((c, o, n) -> {
                    final DSymbol dsOld = new DSymbol(ds);
                    final Point2D[][] oldCoordinates = tilingPane.getTiling().getfDomain().getCoordinates();

                    if (n.intValue() < o.intValue()) {
                        if (isOkDecreaseVij(ds, a, i, j, ds.getVij(i, j, a))) {
                            ds.setVij(i, j, a, n.intValue());
                            final boolean changed = ensureNNForSpherical(ds, n.intValue());
                            tilingPane.computTiling(ds);
                            tilingEditorTab.getTabPane().requestFocus();
                            if (changed) // had to adjust a second value, need to update all values to capture this
                                Platform.runLater(() -> setup(tilingEditorTab));

                        } else // set the value back
                            Platform.runLater(() -> vChooser.setValue(o.intValue()));
                    } else if (n.intValue() > o.intValue()) {
                        ds.setVij(i, j, a, n.intValue());
                        final boolean changed = ensureNNForSpherical(ds, n.intValue());
                        tilingPane.computTiling(ds);
                        tilingEditorTab.getTabPane().requestFocus();
                        if (changed) // had to adjust a second value, need to update all values to capture this
                            Platform.runLater(() -> setup(tilingEditorTab));
                    }
                    if (!undoManager.isPerformingUndoOrRedo())
                        undoManager.add(new ChangeDSymbolCommand("change rotation", dsOld, ds, tilingPane::computTiling, oldCoordinates, tilingPane::changeCoordinates));
                });
                vChooser.valueProperty().addListener(listener);
                vChooser.setUserData(listener);
                switch (k) {
                    case 0:
                        Tooltip.install(vChooser, new Tooltip("Vertex"));
                        break;
                    case 1:
                        Tooltip.install(vChooser, new Tooltip("Edge"));
                        break;
                    case 2:
                        Tooltip.install(vChooser, new Tooltip("Tile"));
                        break;
                }
                vChooser.setVisible(true);
            }
        }
    }

    /**
     * determines whether it is ok to decrease Vij
     *
     * @param ds
     * @param a
     * @param i
     * @param j
     * @param currentValue
     * @return true, if ok
     */
    private static boolean isOkDecreaseVij(DSymbol ds, int a, int i, int j, int currentValue) {
        if (isSphericalNN(ds) && currentValue <= 3)
            return false;

        if (ds.getMij(i, j, a) < 3)
            return false;

        if (i == 0 && j == 1) { // don't want a resulting tile with only one edge
            if (ds.getMij(i, j, a) / ds.getVij(i, j, a) * (currentValue - 1) < 2)
                return false;
        }
        if (i == 1 && j == 2) { // don't want a resulting node of degree 2
            return ds.getMij(i, j, a) / ds.getVij(i, j, a) * (currentValue - 1) >= 3;
        } else
            return true;
    }


    /**
     * is this a spherical NN group?
     *
     * @param ds
     * @return true, if spherical NN group
     */
    private static boolean isSphericalNN(DSymbol ds) {
        if (ds.computeCurvature().doubleValue() > 0) {
            final ArrayList<Integer> values = new ArrayList<>();
            for (int k = 0; k <= 2; k++) {
                final int i = DSymbol.i(k);
                final int j = DSymbol.j(k);

                BitSet seen = new BitSet();
                for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, i + 1, a, seen)) {
                    if (ds.getVij(i, j, a) > 1) {
                        values.add(ds.getVij(i, j, a));
                    }
                }
            }
            return values.size() == 2;
        }
        return false;
    }

    /**
     * ensures that the DS symbol is a valid spherical symbol (with NN rather than NM)
     *
     * @param ds       a Delaney symbol for which the new value has already been set
     * @param newValue the new value that should be copied to the other
     */
    private static boolean ensureNNForSpherical(DSymbol ds, int newValue) {
        if (ds.computeCurvature().doubleValue() > 0) {
            ArrayList<int[]> values = new ArrayList<>();
            for (int k = 0; k <= 2; k++) {
                final int i = DSymbol.i(k);
                final int j = DSymbol.j(k);
                BitSet seen = new BitSet();
                for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, j, a, seen)) {
                    if (ds.getVij(i, j, a) > 1) {
                        values.add(new int[]{i, j, a, ds.getVij(i, j, a)});
                    }
                }
            }
            if (values.size() == 1) {
                System.err.println("Internal error");
            } else if (values.size() == 2) {
                int[] a = values.get(0);
                int[] b = values.get(1);
                if (a[3] != b[3]) {
                    ds.setVij(a[0], a[1], a[2], newValue);
                    ds.setVij(b[0], b[1], b[2], newValue);
                    return true;
                }
            }
        }
        return false;
    }
}
