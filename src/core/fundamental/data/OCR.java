package core.fundamental.data;

import core.fundamental.utils.Wrap;

/**
 * orbit coordinates
 * Created by huson on 3/27/16.
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class OCR {
    private int op;                // orbit number
    private int ncs;
    int[] nc;        // list of orbit coords
    private int ecs;
    int[] ec;        // list of orbit coords
    private int ocs;
    int[] oc;        // list of orbit coords
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
        return ncs;
    }

    public int[] getNc() {
        return nc;
    }

    public int getNc(int i) {
        return nc[i];
    }

    public void setNc(int[] nc, int length) {
        this.ncs = length;
        this.nc = new int[length];
        System.arraycopy(nc, 0, this.nc, 0, length);
    }

    public void setNc(int i, int value) {
        nc[i] = value;
    }

    public int getEcs() {
        return ecs;
    }

    public int[] getEc() {
        return ec;
    }

    public int getEc(int i) {
        return ec[i];
    }

    public void setEc(int[] ec, int length) {
        this.ecs = length;
        this.ec = new int[length];
        System.arraycopy(ec, 0, this.ec, 0, length);
    }

    public void setEc(int i, int value) {
        ec[i] = value;
    }

    public int getOcs() {
        return ocs;
    }

    public int[] getOc() {
        return oc;
    }

    public int getOc(int i) {
        return oc[i];
    }

    public void setOc(int[] oc, int length) {
        this.ocs = length;
        this.oc = new int[length];
        System.arraycopy(oc, 0, this.oc, 0, length);
    }

    public void setOc(int i, int value) {
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
        return new Wrap<Double>(0.0) {
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
        return new Wrap<Double>(0.0) {
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
