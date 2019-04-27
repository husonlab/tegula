/*
 * TilingBase.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.geometry.Tools;
import tegula.main.TilingStyle;

import java.util.ArrayList;
import java.util.Collection;

/**
 * tiling  base class
 * Daniel Huson, 4.2019
 */
abstract public class TilingBase {
    public final static ArrayList<Node> FAILED = new ArrayList<>(); // returned by createTiling when too many rounding errors occur, indicating a recompute
    final Pane pane;
    final TilingStyle tilingStyle;
    final DSymbol ds;
    FDomain fDomain;
    final FundamentalDomain fundamentalDomain;
    Generators generators;
    double tolerance = 0.0;
    int referenceChamberIndex = 1;

    private BooleanProperty drawFundamentalDomainOnly = new SimpleBooleanProperty(false);

    /**
     * constructor
     *
     * @param tilingStyle
     * @param ds
     * @param ds
     */
    public TilingBase(Pane pane, TilingStyle tilingStyle, DSymbol ds) {
        this.pane=pane;
        this.tilingStyle = tilingStyle;
        this.ds = ds;
        fDomain = new FDomain(ds);
        fundamentalDomain = new FundamentalDomain();
        generators = fDomain.getGenerators();

        fundamentalDomain.includeTilesProperty().bind(tilingStyle.showFacesProperty());
        fundamentalDomain.includeBandsProperty().bind(tilingStyle.showBandsProperty());
        fundamentalDomain.includeDecorationsProperty().bind(tilingStyle.showOtherStuffProperty());
        fundamentalDomain.includeChambersProperty().bind(tilingStyle.showAllChambersProperty());
        // do NOT bind handles property
    }

    /**
     * create the tiling as a group of meshes
     *
     * @return meshes
     * @param reset
     */
    abstract public Collection<Node> createTiling(boolean reset);

    /**
     * Computes tolerance for rounding errors. Tolerance depends on shape of fundamental domain.
     *
     * @return tolerance
     */
    public double computeTolerance(int referenceChamberIndex) {
        final Point3D refPoint;
        if (fDomain.getGeometry() == Geometry.Euclidean) {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        } else {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        }
        double tolerance = 100;
        for (Transform g : generators.getTransforms()) {
            double dist = Tools.distance(fDomain.getGeometry(), g.transform(refPoint), refPoint);
            if (dist < tolerance) {
                tolerance = dist;
            }
        }
        return 0.8 * tolerance;
    }

    /**
     * compute the tolerance
     *
     * @param geometry
     * @param refPoint
     * @param generators
     * @return tolerance
     */
    public double computeTolerance(final Geometry geometry, Point3D refPoint, Generators generators) {
        double tolerance = 100;
        for (Transform g : generators.getTransforms()) {
            double dist = Tools.distance(geometry, g.transform(refPoint), refPoint);
            if (dist < tolerance) {
                tolerance = dist;
            }
        }
        return 0.8 * tolerance;
    }

    public DSymbol getDSymbol() {
        return ds;
    }

    public Geometry getGeometry() {
        return fDomain.getGeometry();
    }

    public FDomain getfDomain() {
        return fDomain;
    }

    public void updateReferenceChamberIndex() {
        referenceChamberIndex = fDomain.computeOptimalChamberIndex();
        tolerance = computeTolerance(fDomain.getGeometry(), fDomain.getChamberCenter3D(referenceChamberIndex), generators);
    }

    public String getGroupName() {
        return fDomain.getGroupName();
    }

    /**
     * gets the status line
     *
     * @return status line
     */
    public String getStatusLine() {
        return String.format("Tiling: %d.%d  Vertices: %d  Edges: %d  Tiles: %d  Symmetry group: %s",
                ds.getNr1(), ds.getNr2(), ds.countOrbits(1, 2), ds.countOrbits(0, 2), ds.countOrbits(0, 1), getGroupName());
    }

    public Group getHandles() {
        return fundamentalDomain.getHandles();
    }

    public boolean isDrawFundamentalDomainOnly() {
        return drawFundamentalDomainOnly.get();
    }

    public BooleanProperty drawFundamentalDomainOnlyProperty() {
        return drawFundamentalDomainOnly;
    }

    public void setDrawFundamentalDomainOnly(boolean drawFundamentalDomainOnly) {
        this.drawFundamentalDomainOnly.set(drawFundamentalDomainOnly);
    }

    public int getReferenceChamberIndex() {
        return referenceChamberIndex;
    }

    /**
     * create a tiling
     *
     * @param tilingStyle
     * @param ds
     * @return tiling
     */
    public static TilingBase createTiling(Pane pane, TilingStyle tilingStyle, DSymbol ds) {
        switch (ds.computeGeometry()) {
            case Hyperbolic:
                return new HyperbolicTiling(pane,tilingStyle, ds);
            case Spherical:
                return new SphericalTiling(pane,tilingStyle, ds);
            default:
            case Euclidean:
                return new EuclideanTiling2(pane,tilingStyle, ds);
        }
    }

    public Pane getPane() {
        return pane;
    }
}
