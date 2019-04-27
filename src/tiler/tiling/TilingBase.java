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

package tiler.tiling;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Transform;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.core.dsymbols.OrbifoldGroupName;
import tiler.geometry.Tools;
import tiler.main.TilingStyle;

/**
 * tiling base class
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class TilingBase {
    public static final Group FAILED = new Group();
    final DSymbol ds;
    String groupName;
    FDomain fDomain;

    final TilingStyle tilingStyle;

    final FundamentalDomain fundamentalDomain = new FundamentalDomain();

    Generators generators;
    Constraints constraints;

    Group handles = new Group();

    double tolerance = 0.0;
    int referenceChamberIndex = 0;

    private final BooleanProperty drawFundamentalDomainOnly = new SimpleBooleanProperty(false);
    private int numberOfCopies = 0;

    /**
     * constructor
     *
     * @param ds
     */
    public TilingBase(DSymbol ds, TilingStyle tilingStyle) {
        this.ds = ds;
        this.groupName = OrbifoldGroupName.getGroupName(ds);
        this.fDomain = new FDomain(ds);
        this.constraints = fDomain.getConstraints();
        this.generators = fDomain.getGenerators();

        this.tilingStyle = tilingStyle;

        fundamentalDomain.includeTilesProperty().bind(tilingStyle.showFacesProperty());
        fundamentalDomain.includeBandsProperty().bind(tilingStyle.showBandsProperty());
        fundamentalDomain.includeDecorationsProperty().bind(tilingStyle.showOtherStuffProperty());
        fundamentalDomain.includeChambersProperty().bind(tilingStyle.showAllChambersProperty());
    }


    public Group update() {
        return new Group();
    }

    /**
     * reset everything
     */
    public void reset() {
        this.groupName = OrbifoldGroupName.getGroupName(ds);
        this.fDomain = new FDomain(ds);
        this.constraints = fDomain.getConstraints();
        this.generators = fDomain.getGenerators();
    }

    /**
     * set the reference chamber index and the corresponding tolerance
     *
     * @param referenceChamberIndex
     */
    public void setReferenceChamberIndex(int referenceChamberIndex) {
        this.referenceChamberIndex = referenceChamberIndex;
        tolerance = computeTolerance(fDomain, generators, referenceChamberIndex);
    }

    public double getTolerance() {
        return tolerance;
    }

    public int getReferenceChamberIndex() {
        return referenceChamberIndex;
    }


    public String getGroupName() {
        return groupName;
    }


//----------------------------------------------------------------------------------------------------------------------

    /**
     * Computes tolerance for rounding errors. Tolerance depends on shape of fundamental domain.
     *
     * @return tolerance
     */
    public static double computeTolerance(FDomain fDomain, Generators generators, int referenceChamberIndex) {
        final Point3D refPoint;
        if (fDomain.getGeometry() == Geometry.Euclidean) {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex);
        } else {
            refPoint = fDomain.getChamberCenter3D(referenceChamberIndex).multiply(0.01);
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

    public DSymbol getDSymbol() {
        return ds;
    }

    public FDomain getfDomain() {
        return fDomain;
    }

    public Group getHandles() {
        return handles;
    }


    public Generators getGenerators() {
        return generators;
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
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

    public void setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public Geometry getGeometry() {
        return fDomain.getGeometry();
    }
}
