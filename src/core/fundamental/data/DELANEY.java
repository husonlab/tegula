package core.fundamental.data;

/**
 * Delaney symbol for computing a fundamental region
 * Created by huson on 3/27/16.
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class DELANEY {
    private int fill;
    private char[] fil;        //   file name
    private int naml;
    private char[] nam;        // symbol name
    private int nods;
    private NOD[] nod;        //	 node table
    private int edgs;
    private EDG[] edg;        //	 edge table
    private int orbs;
    private ORB[] orb;        //	orbit table
    private int ncrs;
    private NCR[] ncr;        // list of  node coordinates
    private int ecrs;
    private ECR[] ecr;        // list of  edge coordinates
    private int ocrs;
    private OCR[] ocr;        // list of orbit coordinates
    private int fcrs;
    private int[] fcr;        // list of border coords
    private int imin;
    private int imax;            // Angles
    private int fdl;            // # of edges in fundamental domain
    private int fre;            // # degrees of freedom
    private int chr;            // Euler characteristic
    private double crv;            // curvature
    private double chi;            // chi
    private double def;            // defect
    private double rad;            // radius of inscribed circle
    private double cosr;            // cos (rad)
    private double minx;
    private double miny;
    private double maxx;
    private double maxy;    // range of coords

    /**
     * constructor
     */
    public DELANEY() {

    }

    public int getFill() {
        return fill;
    }

    public char[] getFil(char[] fil) {
        return fil;
    }

    public void setFil(char[] fil, int length) {
        fill = length;
        this.fil = new char[length];
        System.arraycopy(fil, 0, this.fil, 0, length);
        this.fil = fil;
    }

    public int getNaml() {
        return naml;
    }

    public char[] getNam() {
        return nam;
    }

    public void setNam(char[] nam, int length) {
        naml = length;
        this.nam = new char[length];
        System.arraycopy(fil, 0, this.nam, 0, length);
    }

    public int getNods() {
        return nods;
    }

    public NOD[] getNod() {
        return nod;
    }

    public NOD getNod(int i) {
        return nod[i];
    }

    public void setNod(NOD[] nod, int length) {
        nods = length;
        this.nod = new NOD[length];
        System.arraycopy(nod, 0, this.nod, 0, length);
    }

    public int getEdgs() {
        return edgs;
    }

    public EDG[] getEdg() {
        return edg;
    }

    public EDG getEdg(int i) {
        return edg[i];
    }

    public void setEdg(EDG[] edg, int length) {
        edgs = length;
        this.edg = new EDG[length];
        System.arraycopy(edg, 0, this.edg, 0, length);
    }

    public int getOrbs() {
        return orbs;
    }

    public ORB[] getOrb() {
        return orb;
    }

    public ORB getOrb(int i) {
        return orb[i];
    }

    public void setOrb(ORB[] orb, int length) {
        orbs = length;
        this.orb = new ORB[length];
        System.arraycopy(orb, 0, this.orb, 0, length);
    }

    public int getNcrs() {
        return ncrs;
    }

    public NCR[] getNcr() {
        return ncr;
    }

    public NCR getNcr(int i) {
        return ncr[i];
    }

    public void setNcr(NCR[] ncr, int length) {
        ncrs = length;
        this.ncr = new NCR[length];
        System.arraycopy(ncr, 0, this.ncr, 0, length);
    }

    public int getEcrs() {
        return ecrs;
    }

    public ECR[] getEcr() {
        return ecr;
    }

    public ECR getEcr(int i) {
        return ecr[i];
    }

    public void setEcr(ECR[] ecr, int length) {
        ecrs = length;
        this.ecr = new ECR[length];
        System.arraycopy(ecr, 0, this.ecr, 0, length);
    }


    public int getOcrs() {
        return ocrs;
    }

    public OCR[] getOcr() {
        return ocr;
    }

    public OCR getOcr(int i) {
        return ocr[i];
    }

    public void setOcr(OCR[] ocr, int length) {
        ocrs = length;
        this.ocr = new OCR[length];
        System.arraycopy(ocr, 0, this.ocr, 0, length);
    }

    public int getFcrs() {
        return fcrs;
    }

    public int[] getFcr() {
        return fcr;
    }

    public int getFcr(int i) {
        return fcr[i];
    }

    public void setFcr(int[] fcr, int length) {
        fcrs = length;
        this.fcr = new int[length];
        System.arraycopy(fcr, 0, this.fcr, 0, length);
    }

    public void setFcr(int i, int value) {
        fcr[i] = value;
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

    // implementation of methods in base:
    public void init() {
        DELANEY d = this;

        d.fill = 0;
        d.fil = null;
        d.naml = 0;
        d.nam = null;
        d.nods = 0;
        d.nod = null;
        d.edgs = 0;
        d.edg = null;
        d.orbs = 0;
        d.orb = null;
        d.ncrs = 0;
        d.ncr = null;
        d.ecrs = 0;
        d.ecr = null;
        d.ocrs = 0;
        d.ocr = null;
        d.fcrs = 0;
        d.fcr = null;
        d.fdl = 0;
        d.fre = -1;
        d.chr = -1;
        d.crv = 9.9;
        d.chi = 9.9;
        d.def = 9.9;
        d.rad = 9.9;
        d.minx = 9.9;
        d.miny = 9.9;
        d.maxx = 9.9;
        d.maxy = 9.9;
    }

}
