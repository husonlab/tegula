/*
 *  Copyright (C) 2016 Daniel H. Huson
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

import javafx.scene.control.Button;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.OrbifoldGroupName;
import tiler.tiling.Tiling;

import java.util.BitSet;

/**
 * manages editing of symmetry group
 * Created by huson on 4/22/16.
 */
public class GroupEditing {
    /**
     * update the group editing buttons
     *
     * @param document
     */
    public static void update(Document document) {

        final Tiling tiling = document.getCurrent();
        final DSymbol ds = tiling.getDSymbol();
        final Controller controller = document.getController();

        int count = 0;
        for (int i = 0; i <= 1; i++) {
            BitSet seen = new BitSet();
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, i + 1, a, seen)) {
                if (ds.getVij(i, i + 1, a) > 0) {
                    controller.getLabelV(count).setText("" + ds.getVij(i, i + 1, a));
                    controller.getLabelV(count).setDisable(false);
                    final int fi = i;
                    final int fa = a;
                    final Button decreaseVButton = controller.getDecreaseV(count);
                    decreaseVButton.setOnAction((e) -> {
                        if (OrbifoldGroupName.isSphericalNN(ds)) {
                            int n = ds.getVij(fi, fi + 1, fa);
                            setNN(ds, n, n - 1);
                        } else
                            ds.setVij(fi, fi + 1, fa, ds.getVij(fi, fi + 1, fa) - 1);
                        document.setCurrent(new Tiling(ds));
                        document.update();
                        decreaseVButton.setDisable(ds.getVij(fi, fi + 1, fa) <= (OrbifoldGroupName.isSphericalNN(ds) ? 3 : 1));
                    });
                    decreaseVButton.setDisable(ds.getVij(fi, fi + 1, fa) <= (OrbifoldGroupName.isSphericalNN(ds) ? 3 : 1));
                    controller.getIncreaseV(count).setOnAction((e) -> {
                        if (OrbifoldGroupName.isSphericalNN(ds)) {
                            int n = ds.getVij(fi, fi + 1, fa);
                            setNN(ds, n, n + 1);
                        } else
                            ds.setVij(fi, fi + 1, fa, ds.getVij(fi, fi + 1, fa) + 1);
                        document.setCurrent(new Tiling(ds));
                        document.update();
                        decreaseVButton.setDisable(ds.getVij(fi, fi + 1, fa) <= (OrbifoldGroupName.isSphericalNN(ds) ? 3 : 1));
                    });
                    controller.getIncreaseV(count).setDisable(false);
                    count++;
                }
            }
        }
        while (count < 8) {
            controller.getLabelV(count).setText("1");
            controller.getLabelV(count).setDisable(true);
            controller.getDecreaseV(count).setDisable(true);
            controller.getIncreaseV(count).setDisable(true);
            count++;
        }
    }

    /**
     * set all branching numbers that have value oldN to value newN
     *
     * @param ds
     * @param oldN
     * @param newN
     */
    private static void setNN(DSymbol ds, int oldN, int newN) {
        for (int i = 0; i <= 1; i++) {
            BitSet seen = new BitSet();
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(i, i + 1, a, seen)) {
                if (ds.getVij(i, i + 1, a) == oldN) {
                    ds.setVij(i, i + 1, a, newN);
                }
            }
        }
    }
}
