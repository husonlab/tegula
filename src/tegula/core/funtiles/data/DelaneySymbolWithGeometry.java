/*
 * DelaneySymbolWithGeometry.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.core.funtiles.data;

/**
 * Delaney symbol for computing a fundamental region
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class DelaneySymbolWithGeometry {
    private Node[] nodes = {};        //	 node table
    private Edge[] edges = {};        //	 edge table
    private Orbit[] orbits = {};        //	orbit table

    private NodeCoordinates[] nodeCoordinates = {};        // list of  node coordinates
    private EdgeCoordinates[] edgeCoordinates = {};        // list of  edge coordinates
    private OrbitCoordinates[] orbitCoordinates = {};        // list of orbit coordinates
    private int[] borderCoordinates = {};        // list of border coords

    private int imin;
    private int imax;            // Angles
    private int fdl = 0;            // # of edges in fundamental domain
    private int fre = 1;            // # degrees of freedom
    private int chr = -1;            // Euler characteristic
    private double crv = 9.9;            // curvature
    private double chi = 9.9;            // chi
    private double def = 9.9;            // defect
    private double rad = 9.9;            // radius of inscribed circle
    private double cosr;            // cos (rad)

    private double minx = 9.9;
    private double miny = 9.9;
    private double maxx = 9.9;
    private double maxy = 9.9;    // range of coords

    /**
     * constructor
     */
    public DelaneySymbolWithGeometry() {
    }

    public int getNumberOfNodes() {
        return nodes.length;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Node getNode(int i) {
        return nodes[i];
    }

    public void setNumberOfNodes(int n) {
        final Node[] old = nodes;
        nodes = new Node[n];
        System.arraycopy(old, 0, nodes, 0, Math.min(old.length, nodes.length));
        for (int i = old.length; i < nodes.length; i++) {
            nodes[i] = new Node();
        }
    }

    public void ensureNode(int n) {
        if (nodes.length <= n) {
            setNumberOfNodes(n + 1);
        }
    }

    public int getNumberOfEdges() {
        return edges.length;
    }

    public Edge[] getEdges() {
        return edges;
    }

    public Edge getEdge(int i) {
        return edges[i];
    }

    public void setNumberOfEdges(int n) {
        final Edge[] old = edges;
        edges = new Edge[n];
        System.arraycopy(old, 0, edges, 0, Math.min(old.length, edges.length));
        for (int i = old.length; i < edges.length; i++) {
            edges[i] = new Edge();
        }
    }

    public int addNewEdge() {
        final int current = getNumberOfEdges();
        setNumberOfEdges(current + 1);
        return current;
    }

    public int getNumberOfOrbits() {
        return orbits.length;
    }

    public Orbit[] getOrbits() {
        return orbits;
    }

    public Orbit getOrb(int i) {
        return orbits[i];
    }

    public void setNumberOfOrbits(int n) {
        final Orbit[] old = orbits;
        orbits = new Orbit[n];
        System.arraycopy(old, 0, orbits, 0, Math.min(old.length, orbits.length));
        for (int i = old.length; i < orbits.length; i++) {
            orbits[i] = new Orbit();
        }
    }

    public int addNewOrbit() {
        final int current = getNumberOfOrbits();
        setNumberOfOrbits(current + 1);
        return current;
    }

    public int getNumberOfNodeCoordinates() {
        return nodeCoordinates.length;
    }

    public NodeCoordinates[] getNodeCoordinates() {
        return nodeCoordinates;
    }

    public NodeCoordinates getNodeCoordinates(int i) {
        return nodeCoordinates[i];
    }

    public void setNumberNodeCoordinates(int n) {
        final NodeCoordinates[] old = nodeCoordinates;
        nodeCoordinates = new NodeCoordinates[n];
        System.arraycopy(old, 0, nodeCoordinates, 0, Math.min(old.length, nodeCoordinates.length));
        for (int i = old.length; i < nodeCoordinates.length; i++) {
            nodeCoordinates[i] = new NodeCoordinates();
        }
    }

    public int getNumberOfEdgeCoordinates() {
        return edgeCoordinates.length;
    }

    public EdgeCoordinates[] getEdgeCoordinates() {
        return edgeCoordinates;
    }

    public EdgeCoordinates getEdgeCoordinates(int i) {
        return edgeCoordinates[i];
    }

    public void setNumberEdgeCoordinates(int n) {
        final EdgeCoordinates[] old = edgeCoordinates;
        edgeCoordinates = new EdgeCoordinates[n];
        System.arraycopy(old, 0, edgeCoordinates, 0, Math.min(old.length, edgeCoordinates.length));
        for (int i = old.length; i < edgeCoordinates.length; i++) {
            edgeCoordinates[i] = new EdgeCoordinates();
        }
    }

    public int getNumberOfOrbitCoordinates() {
        return orbitCoordinates.length;
    }

    public OrbitCoordinates[] getOrbitCoordinates() {
        return orbitCoordinates;
    }

    public OrbitCoordinates getOrbitCoordinates(int i) {
        return orbitCoordinates[i];
    }

    public void setNumberOrbitCoordinates(int n) {
        final OrbitCoordinates[] old = orbitCoordinates;
        orbitCoordinates = new OrbitCoordinates[n];
        System.arraycopy(old, 0, orbitCoordinates, 0, Math.min(old.length, orbitCoordinates.length));
        for (int i = old.length; i < orbitCoordinates.length; i++) {
            orbitCoordinates[i] = new OrbitCoordinates();
        }
    }

    public int getNumberOfBorderCoordinates() {
        return borderCoordinates.length;
    }

    public int[] getBorderCoordinates() {
        return borderCoordinates;
    }

    public int getBorderCoordinates(int i) {
        return borderCoordinates[i];
    }

    public void setBorderCoordinates(int i, int value) {
        if (i >= borderCoordinates.length) {
            final int[] old = borderCoordinates;
            borderCoordinates = new int[i + 1];
            System.arraycopy(old, 0, this.borderCoordinates, 0, Math.min(old.length, borderCoordinates.length));

        }
        borderCoordinates[i] = value;
    }

    public void clearBorderCoordinates() {
        borderCoordinates = new int[0];
    }

    public int getImin() {
        return imin;
    }

    public void setImin(int imin) {
        this.imin = imin;
    }

    public int getImax() {
        return imax;
    }

    public void setImax(int imax) {
        this.imax = imax;
    }

    public int getFdl() {
        return fdl;
    }

    public void setFdl(int fdl) {
        this.fdl = fdl;
    }

    public int getFre() {
        return fre;
    }

    public void setFre(int fre) {
        this.fre = fre;
    }

    public int getChr() {
        return chr;
    }

    public void setChr(int chr) {
        this.chr = chr;
    }

    public double getCrv() {
        return crv;
    }

    public void setCrv(double crv) {
        this.crv = crv;
    }

    public double getChi() {
        return chi;
    }

    public void setChi(double chi) {
        this.chi = chi;
    }

    public double getDef() {
        return def;
    }

    public void setDef(double def) {
        this.def = def;
    }

    public double getRad() {
        return rad;
    }

    public void setRad(double rad) {
        this.rad = rad;
    }

    public double getCosr() {
        return cosr;
    }

    public void setCosr(double cosr) {
        this.cosr = cosr;
    }

    public double getMinx() {
        return minx;
    }

    public void setMinx(double minx) {
        this.minx = minx;
    }

    public double getMiny() {
        return miny;
    }

    public void setMiny(double miny) {
        this.miny = miny;
    }

    public double getMaxx() {
        return maxx;
    }

    public void setMaxx(double maxx) {
        this.maxx = maxx;
    }

    public double getMaxy() {
        return maxy;
    }

    public void setMaxy(double maxy) {
        this.maxy = maxy;
    }

    /**
     * scale all coordinates
     *
	 */
    public void scale(double factor) {
        DelaneySymbolWithGeometry d = this;

        for (NodeCoordinates ncr : d.nodeCoordinates) {
            ncr.setPosx(ncr.getPosx() * factor);
            ncr.setPosy(ncr.getPosy() * factor);
        }

        for (EdgeCoordinates ecr : d.edgeCoordinates) {
            ecr.setPosx(ecr.getPosx() * factor);
            ecr.setPosy(ecr.getPosy() * factor);
        }

        for (OrbitCoordinates ocr : d.orbitCoordinates) {
            ocr.setPosx(ocr.getPosx() * factor);
            ocr.setPosy(ocr.getPosy() * factor);
        }

        rad *= factor;
        minx *= factor;
        maxx *= factor;
        miny *= factor;
        maxy *= factor;
    }
}
