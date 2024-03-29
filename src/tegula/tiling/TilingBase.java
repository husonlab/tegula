/*
 * TilingBase.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tiling;

import javafx.beans.property.*;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.core.dsymbols.OrbifoldGroupName;
import tegula.geometry.Tools;
import tegula.main.TilingStyle;

import java.util.Stack;

/**
 * tiling base class
 * Daniel Huson and Ruediger Zeller, 2016
 */
abstract public class TilingBase {
    public static final Group FAILED = new Group();
    protected final Stack<Node> recycler = new Stack<>();
    protected final Group fundPrototype = new Group();
    protected final DSymbol ds;
    protected String groupName;
    protected final ObjectProperty<FDomain> fDomain = new SimpleObjectProperty<>();

    protected final TilingStyle tilingStyle;

    protected Generators generators;
    protected Constraints constraints;

    protected final Group handles = new Group();

    protected double tolerance = 0.0;
    protected Point3D referencePoint;

    private final BooleanProperty drawFundamentalDomainOnly = new SimpleBooleanProperty(false);
    private int numberOfCopies = 0;

    /**
     * constructor
     *
	 */
    public TilingBase(DSymbol ds, TilingStyle tilingStyle) {
        this.ds = ds;
        this.groupName = OrbifoldGroupName.getGroupName(ds);
        this.fDomain.set(new FDomain(ds, tilingStyle.isBendAnEdge()));
        this.constraints = getfDomain().getConstraints();
        this.generators = getfDomain().getGenerators();

        this.tilingStyle = tilingStyle;
    }


    abstract public Group update();

    /**
     * reset everything
     */
    public void reset() {
        this.groupName = OrbifoldGroupName.getGroupName(ds);
        this.fDomain.set(new FDomain(ds, tilingStyle.isBendAnEdge()));
        this.constraints = getfDomain().getConstraints();
        this.generators = getfDomain().getGenerators();
    }


    public double getTolerance() {
        return tolerance;
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
    public static double computeTolerance(Geometry geometry, Point3D refPoint, Generators generators) {

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

    public FDomain getfDomain() {
        return fDomain.get();
    }

    public ReadOnlyObjectProperty<FDomain> fDomainObjectProperty() {
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
        return getfDomain().getGeometry();
    }

    public Stack<Node> recycler() {
        return recycler;
    }

    public Group getFundPrototype() {
        return fundPrototype;
    }
}
