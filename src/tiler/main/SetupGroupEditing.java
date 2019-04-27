/*
 * SetupGroupEditing.java Copyright (C) 2019. Daniel H. Huson
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

package tiler.main;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Spinner;
import tiler.core.dsymbols.DSymbol;
import tiler.tiling.Tiling;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * manages editing of symmetry group
 * Created by huson on 4/22/16.
 */
public class SetupGroupEditing {

    /**
     * update the group editing buttons
     *
     * @param document
     */
    public static void apply(MainWindowController mainViewController, Document document) {

        final Tiling tiling = document.getCurrentTiling();
        final DSymbol ds = tiling.getDSymbol();

        // setup the rotation controls:
        final BooleanProperty ignoreUpdatesDuringSetup = new SimpleBooleanProperty(true);
        try {
            int count = 0;
            for (int i = 0; i <= 1; i++) {
                final BitSet seen = new BitSet();
                for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, i + 1, a, seen)) {
                    final int fi = i;
                    final int fa = a;

                    final Spinner<Integer> spinner = mainViewController.getVSpinner(count);


                    if (spinner.getUserData() instanceof ChangeListener)
                        spinner.valueProperty().removeListener((ChangeListener) spinner.getUserData());

                    spinner.getValueFactory().setValue(ds.getVij(i, i + 1, a));

                    final ChangeListener<Integer> listener = ((c, o, n) -> {
                        if (!ignoreUpdatesDuringSetup.get()) {
                            if (n < o && isOkDecreaseVij(ds, fa, fi, fi + 1, ds.getVij(fi, fi + 1, fa))) {
                                ds.setVij(fi, fi + 1, fa, n);
                                ensureNNForSpherical(ds, n);
                                final Tiling newTiling = new Tiling(ds, document.getTilingStyle());
                                document.changeCurrentTiling(newTiling);
                                document.update();
                            } else if (n > o) {
                                ds.setVij(fi, fi + 1, fa, n);
                                ensureNNForSpherical(ds, n);
                                final Tiling newTiling = new Tiling(ds, document.getTilingStyle());
                                document.changeCurrentTiling(newTiling);
                                document.update();
                            }
                        }
                    });
                    spinner.valueProperty().addListener(listener);
                    spinner.setUserData(listener);

                    spinner.setDisable(false);
                    spinner.setVisible(true);
                    spinner.setEditable(true);

                    count++;
                    if (count == 10)
                        break; // only support 10 choices
                }
            }
            // disable the rest:
            while (count < 10) {
                final Spinner<Integer> spinner = mainViewController.getVSpinner(count);
                if (spinner.getUserData() instanceof ChangeListener)
                    spinner.valueProperty().removeListener((ChangeListener) spinner.getUserData());

                spinner.getValueFactory().setValue(1);

                spinner.setDisable(true);
                spinner.setVisible(false);

                count++;
            }
        } finally {
            ignoreUpdatesDuringSetup.set(false);
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
            if (ds.getMij(i, j, a) / ds.getVij(i, j, a) * (currentValue - 1) < 3)
                return false;
        }
        return true;
    }


    /**
     * is this a spherical NN group?
     *
     * @param ds
     * @return true, if spherical NN group
     */
    private static boolean isSphericalNN(DSymbol ds) {
        if (ds.computeEulerCharacteristic() > 0) {
            final ArrayList<Integer> values = new ArrayList<>();
            for (int i = 0; i <= 1; i++) {
                BitSet seen = new BitSet();
                for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, i + 1, a, seen)) {
                    if (ds.getVij(i, i + 1, a) > 1) {
                        values.add(ds.getVij(i, i + 1, a));
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
    private static void ensureNNForSpherical(DSymbol ds, int newValue) {
        if (ds.computeEulerCharacteristic() > 0) {
            ArrayList<int[]> values = new ArrayList<>();
            for (int i = 0; i <= 1; i++) {
                BitSet seen = new BitSet();
                for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, i + 1, a, seen)) {
                    if (ds.getVij(i, i + 1, a) > 1) {
                        values.add(new int[]{i, i + 1, a, ds.getVij(i, i + 1, a)});
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
                }
            }
        }
    }
}
