package tiler.core.fundamental.data;

/**
 * node
 * Created by huson on 3/27/16.
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class NOD {
    private int cr = -1; // node  coords
    private int id; // node number with respect to gluing
    private int mk; // # of glued edges
    private int sg; // sign
    private final int[] np = new int[]{-1, -1, -1}; // related node
    private final int[] ep = new int[]{-1, -1, -1}; // related edge
    private final int[] op = new int[]{-1, -1, -1}; // related orbit

    public int getCr() {
        return cr;
    }

    public void setCr(int cr) {
        this.cr = cr;
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

    public void incrMk() {
        this.mk++;
    }

    public int getSg() {
        return sg;
    }

    public void setSg(int sg) {
        this.sg = sg;
    }

    public int[] getNp() {
        return np;
    }

    public int getNp(int i) {
        return np[i];
    }

    public int[] getEp() {
        return ep;
    }

    public int getEp(int i) {
        return ep[i];
    }

    public int getOp(int i) {
        return op[i];
    }

    public void setNp(int i, int value) {
        np[i] = value;
    }

    public void setEp(int i, int value) {
        ep[i] = value;
    }

    public void setOp(int i, int value) {
        op[i] = value;
    }

}
