/*
 * SphericalTiling.java Copyright (C) 2020. Daniel H. Huson
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
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.main.TilingStyle;
import tegula.tiling.parts.OctTree;

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
        generators = fDomain.getGenerators();
        referencePoint = fDomain.computeReferencePoint();
        tolerance = computeTolerance(getGeometry(), referencePoint, generators);

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
        fundPrototype.getChildren().setAll(FundamentalDomain.compute(ds, fDomain, tilingStyle));

        {
            final Group group = CopyTiles.apply(fundPrototype);
            group.getTransforms().setAll(new Translate());
            all.getChildren().add(group);
        }
        if (!isDrawFundamentalDomainOnly()) {
            // Make copies of fundamental domain.
            final OctTree seen = new OctTree();
            seen.insert(getGeometry(), referencePoint, tolerance); //root node of OctTree is point of reference.

            final Queue<Transform> queue = new LinkedList<>(generators.getTransforms());
            for (Transform g : generators.getTransforms()) {  // Makes copies of fundamental domain by using generators
                final Point3D ref = g.transform(referencePoint);
                if (seen.insert(getGeometry(), ref, tolerance)) {
                    final Group group = CopyTiles.apply(fundPrototype);
                    group.getTransforms().setAll(g);
                    all.getChildren().add(group);
                }
            }

            while (queue.size() > 0) {
                final Transform t = queue.poll(); // remove t from queue

                for (Transform g : generators.getTransforms()) {
                    {
                        final Transform tg = t.createConcatenation(g);
                        final Point3D ref = tg.transform(referencePoint);
                        if (seen.insert(getGeometry(), ref, tolerance)) {
                            final Group group = CopyTiles.apply(fundPrototype);
                            group.getTransforms().setAll(tg);
                            all.getChildren().add(group);
                            queue.add(tg);
                        }
                    }

                    {
                        final Transform gt = g.createConcatenation(t);
                        final Point3D ref = gt.transform(referencePoint);
                        if (seen.insert(getGeometry(), ref, tolerance)) {
                            final Group group = CopyTiles.apply(fundPrototype);
                            group.getTransforms().setAll(gt);
                            all.getChildren().add(group);
                            queue.add(gt);
                        }
                    }
                }
            }
        }
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
