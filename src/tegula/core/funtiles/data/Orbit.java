/*
 * ORB.java Copyright (C) 2020. Daniel H. Huson
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

import tegula.core.funtiles.utils.Maths;

/**
 * orbit
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class Orbit {
    int[] cr = {};        // orbit coords
    private int id;                // orb number with respect to gluing
    private int mk;                // splitted flag
    private int sg;                // sign
    private int ti = -1, tj = -1;            // orbit type
    private int[] np = {};        // list of related nodes
    private int[] ep = {};        // list of related edges
    private int[] op = {};        // list of related orbitLabels
    private int m, r, v = 1;            // delaney numbers (m / r = v)
    private int f = 1;                // fixpoint factor (1:loop, 2:chain)
    private int s;                // # of splits in fundamental domain
    private int b;                // # of unglued edges
    private int i = 1;                // = v * f * s = 2 * PI / alph
    private double alph = Maths.m_2pi;            // corner angle
    private double cosa;            // cos (0.5 * alph)
    private double beta = -Maths.M_PI_2;            // center angle
    private double dist = 1.0;            // center distance

    public int getNumberOfCoordinates() {
        return cr.length;
    }

    public int[] getCr() {
        return cr;
    }

    public void setCr(int n, int value) {
        if (n >= cr.length) {
            final int[] old = cr;
            cr = new int[n + 1];
            System.arraycopy(old, 0, cr, 0, Math.min(old.length, cr.length));
        }
        cr[n] = value;
    }

    public void clearCr() {
        cr = new int[0];
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMk() {
        return mk;
    }

    public void setMk(int mk) {
        this.mk = mk;
    }

    public int getSg() {
        return sg;
    }

    public void setSg(int sg) {
        this.sg = sg;
    }

    public int getTi() {
        return ti;
    }

    public void setTi(int ti) {
        this.ti = ti;
    }

    public int getTj() {
        return tj;
    }

    public void setTj(int tj) {
        this.tj = tj;
    }

    public int getNps() {
        return np.length;
    }

    public int[] getNp() {
        return np;
    }

    public int getNp(int i) {
        return np[i];
    }

    public void setNp(int i, int value) {
        if (i >= np.length) {
            final int[] old = np;
            np = new int[i + 1];
            System.arraycopy(old, 0, np, 0, Math.min(old.length, np.length));
        }
        np[i] = value;
    }

    public int getEps() {
        return ep.length;
    }

    public int[] getEp() {
        return ep;
    }

    public int getEp(int i) {
        return ep[i];
    }

    public void setEp(int i, int value) {
        if (i >= ep.length) {
            final int[] old = ep;
            ep = new int[i + 1];
            System.arraycopy(old, 0, ep, 0, Math.min(old.length, ep.length));
        }
        ep[i] = value;
    }

    public int getOps() {
        return op.length;
    }

    public int[] getOp() {
        return op;
    }
    public void setOp(int i, int value) {
        if (i >= op.length) {
            final int[] old = op;
            op = new int[i + 1];
            System.arraycopy(old, 0, op, 0, Math.min(old.length, op.length));
        }
        op[i] = value;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public void incrMk() {
        this.mk++;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public double getAlph() {
        return alph;
    }

    public void setAlph(double alph) {
        this.alph = alph;
    }

    public double getCosa() {
        return cosa;
    }

    public void setCosa(double cosa) {
        this.cosa = cosa;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }
}
