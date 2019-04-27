/*
 * SphericalTiling.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tiling_old;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.DSymbol;
import tegula.main.TilingStyle;
import tegula.tiling_old.util.OctTree;
import tegula.util.JavaFXUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * computes a spherical tiling
 * <p>
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class SphericalTiling extends TilingBase {
    /**
     * constructor
     *
     * @param tilingStyle
     * @param ds
     */
    public SphericalTiling(Pane pane, TilingStyle tilingStyle, DSymbol ds) {
        super(pane,tilingStyle, ds);
    }

    /**
     * create the set of tiles to be shown in spherical case
     *
     * @return tiles
     * @param reset
     */
    public ArrayList<Node> createTiling(boolean reset) {
        final ArrayList<Node> all = new ArrayList<>();

        updateReferenceChamberIndex();

        fundamentalDomain.update(getDSymbol(), fDomain, tilingStyle);
        final Group fund = fundamentalDomain.getAll();

        all.add(fund);

        // Make copies of fundamental domain.
        final OctTree seen = new OctTree();
        final Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex); // refPoint lies on unit sphere
        seen.insert(getGeometry(), refPoint, tolerance); //root node of OctTree is point of reference.

        final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());
        for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
            Point3D genRef = g.transform(refPoint);

            if (seen.insert(getGeometry(), genRef, tolerance)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
                final Group group2 = JavaFXUtils.copyGroup(fund);
                group2.getTransforms().add(g);
                all.add(group2);
            }
        }

        while (queue.size() > 0) {
            final Transform t = queue.poll(); // remove t from queue
            for (Transform g : generators.getTransforms()) {
                Transform tg = t.createConcatenation(g);
                Point3D bpt = tg.transform(refPoint);
                if (seen.insert(getGeometry(), bpt, tolerance)) {
                    final Group group2 = JavaFXUtils.copyGroup(fund);
                    group2.getTransforms().add(tg);
                    all.add(group2);
                    queue.add(tg);
                }

                Transform gt = g.createConcatenation(t);
                bpt = gt.transform(refPoint);
                if (seen.insert(getGeometry(), bpt, tolerance)) {
                    Group group2 = JavaFXUtils.copyGroup(fund);
                    group2.getTransforms().add(gt);
                    all.add(group2);
                    queue.add(gt);
                }
            }
        }

        // only want one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.add(getHandles());

        if (tilingStyle.isShowHandles())
            all.add(getHandles());

        if (false) {
            Rectangle small=new Rectangle(40,40);
            small.setFill(Color.TRANSPARENT);
            small.setStroke(Color.ORANGE);

            all.add(small);
        }

        return all;
    }

}


