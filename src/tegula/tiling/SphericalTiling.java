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

package tegula.tiling;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.main.TilingStyle;
import tegula.tiling.parts.OctTree;
import tegula.util.JavaFXUtils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * a spherical tiling
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class SphericalTiling extends TilingBase implements TilingCreator {

    /**
     * constructor
     *
     * @param ds
     */
    public SphericalTiling(DSymbol ds, TilingStyle tilingStyle) {
        super(ds, tilingStyle);
    }

    /**
     * update the tiling
     */
    public Group update() {
        fDomain.updateGeneratorsAndContraints();
        generators = fDomain.getGenerators();
        referenceChamberIndex = fDomain.computeOptimalChamberIndex();
        tolerance = computeTolerance(fDomain, generators, referenceChamberIndex);
        fundamentalDomain.buildFundamentalDomain(ds, fDomain, tilingStyle);

        final Group tiles = produceTiles();
        setNumberOfCopies(tiles.getChildren().size());
        return tiles;
    }

    /**
     * produces the tiles
     *
     * @return tiles
     */
    private Group produceTiles() {
        handles.getChildren().clear();

        final Group all = new Group();
        final Group fundPrototype = new Group();
        fundPrototype.getChildren().setAll(fundamentalDomain.getAllRequested());

        all.getChildren().add(fundPrototype);

        if (!isDrawFundamentalDomainOnly()) {
            // Make copies of fundamental domain.
            final OctTree seen = new OctTree();
            final Point3D refPoint = fDomain.getChamberCenter3D(referenceChamberIndex).multiply(0.01); // refPoint lies on unit sphere
            seen.insert(getGeometry(), refPoint, tolerance); //root node of OctTree is point of reference.

            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());
            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                Point3D genRef = g.transform(refPoint);
                if (seen.insert(getGeometry(), genRef, tolerance)) {    // Checks whether point "genRef" is in OctTree "seen". Adds it if not.
                    final Group group2 = JavaFXUtils.copyGroup(fundPrototype);
                    group2.getTransforms().add(g);
                    all.getChildren().add(group2);
                }
            }

            while (queue.size() > 0) {
                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) {
                    Transform tg = t.createConcatenation(g);
                    Point3D bpt = tg.transform(refPoint);
                    if (seen.insert(getGeometry(), bpt, tolerance)) {
                        final Group group2 = JavaFXUtils.copyGroup(fundPrototype);
                        group2.getTransforms().add(tg);
                        all.getChildren().add(group2);
                        queue.add(tg);
                    }

                    Transform gt = g.createConcatenation(t);
                    bpt = gt.transform(refPoint);
                    if (seen.insert(getGeometry(), bpt, tolerance)) {
                        Group group2 = JavaFXUtils.copyGroup(fundPrototype);
                        group2.getTransforms().add(gt);
                        all.getChildren().add(group2);
                        queue.add(gt);
                    }
                }
            }
        }

        // only want one copy of these things:
        if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
            all.getChildren().add(fundamentalDomain.getHandles());

        if (tilingStyle.isShowHandles())
            all.getChildren().add(fundamentalDomain.getHandles());

        return all;
    }

    public DSymbol getDSymbol() {
        return ds;
    }

    public FDomain getfDomain() {
        return fDomain;
    }

    public Group getHandles() {
        return handles;
    }
}
