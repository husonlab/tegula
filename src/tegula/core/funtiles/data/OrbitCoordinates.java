/*
 * OCR.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.core.funtiles.data;

import tegula.core.funtiles.utils.Wrap;

/**
 * orbit coordinates
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class OrbitCoordinates {
    private int op;                // orbit number
    int[] nc = {};        // list of orbit coords
    int[] ec = {};        // list of orbit coords
    int[] oc = {};        // list of orbit coords
    private double dist;            // center distance
    private double beta = Math.PI;            // center angle
    private double posx;            // x-Coordinate
    private double posy;            // y-Coordinate

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public int getNcs() {
        return nc.length;
    }

    public int getNc(int i) {
        return nc[i];
    }

    public void setNc(int i, int value) {
        if (i >= nc.length) {
            final int[] old = nc;
            nc = new int[i + 1];
            System.arraycopy(old, 0, nc, 0, Math.min(old.length, nc.length));
        }
        nc[i] = value;
    }

    public int getEcs() {
        return ec.length;
    }

    public int[] getEc() {
        return ec;
    }

    public int getEc(int i) {
        return ec[i];
    }

    public void setEc(int i, int value) {
        if (i >= ec.length) {
            final int[] old = ec;
            ec = new int[i + 1];
            System.arraycopy(old, 0, ec, 0, Math.min(old.length, ec.length));
        }
        ec[i] = value;
    }

    public int getOcs() {
        return oc.length;
    }

    public int[] getOc() {
        return oc;
    }

    public int getOc(int i) {
        return oc[i];
    }

    public void setOc(int i, int value) {
        if (i >= oc.length) {
            final int[] old = oc;
            oc = new int[i + 1];
            System.arraycopy(old, 0, oc, 0, Math.min(old.length, oc.length));
        }
        oc[i] = value;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getPosx() {
        return posx;
    }

    public void setPosx(double posx) {
        this.posx = posx;
    }

    public double getPosy() {
        return posy;
    }

    public Wrap<Double> accessPosx() {
        return new Wrap<>(0.0) {
            @Override
            public Double get() {
                return posx;
            }

            @Override
            public void set(Double value) {
                posx = value;
            }
        };
    }

    public void setPosy(double posy) {
        this.posy = posy;
    }

    public Wrap<Double> accessPosy() {
        return new Wrap<>(0.0) {
            @Override
            public Double get() {
                return posy;
            }

            @Override
            public void set(Double value) {
                posy = value;
            }
        };
    }
}
