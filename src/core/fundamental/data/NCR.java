package core.fundamental.data;

/**
 * node coordinates
 * Created by huson on 3/27/16.
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class NCR {
    private int np;                // node number
    private int[] nc = new int[]{-1, -1, -1};            // related node  coords [type]
    private int[] ec = new int[]{-1, -1, -1};            // related edge  coords [type]
    private int[] oc = new int[]{-1, -1, -1};            // related orbit coords [type]
    private double dist;            // center distance
    private double beta = Math.PI;            // center angle
    private double posx;            // x-Coordinate
    private double posy;            // y-Coordinate

    public int getNp() {
        return np;
    }

    public void setNp(int np) {
        this.np = np;
    }

    public int getNc(int i) {
        return nc[i];
    }

    public void setNc(int i, int value) {
        this.nc[i] = value;
    }

    public int[] getEc() {
        return ec;
    }

    public int getEc(int i) {
        return ec[i];
    }

    public void setEc(int i, int value) {
        this.ec[i] = value;
    }

    public int getOc(int i) {
        return oc[i];
    }

    public void setOc(int i, int value) {
        this.oc[i] = value;
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

    public void setPosy(double posy) {
        this.posy = posy;
    }
}
