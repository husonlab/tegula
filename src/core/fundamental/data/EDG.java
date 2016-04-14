package core.fundamental.data;

/**
 * Edge
 * Created by huson on 3/27/16.
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class EDG {
    private int ca = -1;
    private int cb = -1;    // edge  coords
    private int id;    // edge number with respect to gluing
    private int mk;                // inhibit gluing flag
    private int sg;                // sign
    private int ty = -1;                // edge type
    private int npa = -1, npb = -1;            // related nodes
    private int epax = -1, epay = -1, epbx = -1, epby = -1;    // related edges
    private int opx = -1, opy = -1;            // related orbits

    public int getCa() {
        return ca;
    }

    public void setCa(int ca) {
        this.ca = ca;
    }

    public int getCb() {
        return cb;
    }

    public void setCb(int cb) {
        this.cb = cb;
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

    public int getTy() {
        return ty;
    }

    public void setTy(int ty) {
        this.ty = ty;
    }

    public int getNpa() {
        return npa;
    }

    public void setNpa(int npa) {
        this.npa = npa;
    }

    public int getNpb() {
        return npb;
    }

    public void setNpb(int npb) {
        this.npb = npb;
    }

    public int getEpax() {
        return epax;
    }

    public void setEpax(int epax) {
        this.epax = epax;
    }

    public int getEpay() {
        return epay;
    }

    public void setEpay(int epay) {
        this.epay = epay;
    }

    public int getEpbx() {
        return epbx;
    }

    public void setEpbx(int epbx) {
        this.epbx = epbx;
    }

    public int getEpby() {
        return epby;
    }

    public void setEpby(int epby) {
        this.epby = epby;
    }

    public int getOpx() {
        return opx;
    }

    public void setOpx(int opx) {
        this.opx = opx;
    }

    public int getOpy() {
        return opy;
    }

    public void setOpy(int opy) {
        this.opy = opy;
    }
}
