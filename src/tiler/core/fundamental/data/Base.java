package tiler.core.fundamental.data;

import tiler.core.fundamental.utils.Utilities;

/**
 * basic methods for setting up stuff
 * Created by huson on 3/28/16.
 */
public class Base {
    public static Boolean init_delaney(DELANEY d) {
        d.init();
        return true;
    }

    public static int init_nod(DELANEY d, int np) {
        check_nod_adr(d, np);
        while (d.getNods() <= np) {
            create_nod(d);
        }
        return (np);
    }

    public static int init_edg(DELANEY d, int ep) {
        check_edg_adr(d, ep);
        while (d.getEdgs() <= ep) {
            create_edg(d);
        }
        return (ep);
    }

    public static int init_orb(DELANEY d, int op) {
        check_orb_adr(d, op);
        while (d.getOrbs() <= op) {
            create_orb(d);
        }
        return (op);
    }

    /**
     * create a node
     *
     * @return the node number
     */
    public static int create_nod(DELANEY d) {
        final int np = d.getNods();
        final NOD[] array = Utilities.alloc("create_nod", d.getNod(), null, np + 1, null);
        d.setNod(array, array.length);
        return np;
    }

    /**
     * create an edge
     *
     * @return the edge number
     */
    public static int create_edg(DELANEY d) {
        final int ep = d.getEdgs();
        final EDG[] array = Utilities.alloc("create_edg", d.getEdg(), null, ep + 1, null);
        d.setEdg(array, array.length);
        return ep;
    }

    /**
     * create an orbit
     *
     * @return the orbit number
     */
    public static int create_orb(DELANEY d) {
        final int op = d.getOrbs();
        final ORB[] array = Utilities.alloc("create_orb", d.getOrb(), null, op + 1, null);
        d.setOrb(array, array.length);
        return op;
    }

    public static int create_ncr(DELANEY d, int np) {
        final int nc = d.getNcrs();
        check_nod(d, np);
        final NCR[] array = Utilities.alloc("create_ncr", d.getNcr(), null, nc + 1, null);
        d.setNcr(array, array.length);
        d.getNcr(nc).setNp(np);
        return nc;
    }

    public static int create_ecr(DELANEY d, int ep) {
        final int ec = d.getEcrs();
        check_edg(d, ep);
        final ECR[] array = Utilities.alloc("create_ecr", d.getEcr(), null, ec + 1, null);
        d.setEcr(array, array.length);
        d.getEcr(ec).setEp(ep);
        return ec;
    }

    public static int create_ocr(DELANEY d, int op) {
        final int oc = d.getOcrs();
        check_orb(d, op);
        final OCR[] array = Utilities.alloc("create_ocr", d.getOcr(), null, oc + 1, null);
        d.setOcr(array, array.length);
        d.getOcr(oc).setOp(op);
        return (oc);
    }


    public static int create_orb_crd(DELANEY d, int op, int oc) {
        final int uu = d.getOrb(op).getCrs();
        Base.check_orb(d, op);
        Base.check_ocr(d, oc);
        final int[] array = Utilities.alloc("create_orb_crd", d.getOrb(op).getCr(), null, uu + 1, null);
        d.getOrb(op).setCr(array, array.length);
        d.getOrb(op).setCr(uu, oc);
        return uu;
    }

    public static int create_orb_nod(DELANEY d, int op, int np) {
        final int uu = d.getOrb(op).getNps();
        check_orb(d, op);
        check_nod(d, np);
        final int[] array = Utilities.alloc("create_orb_nod", d.getOrb(op).getNp(), null, uu + 1, null);
        d.getOrb(op).setNp(array, array.length);
        d.getOrb(op).setNp(uu, np);
        return (uu);
    }

    public static int create_orb_edg(DELANEY d, int op, int ep) {
        final int uu = d.getOrb(op).getEps();
        check_orb(d, op);
        check_edg(d, ep);
        final int[] array = Utilities.alloc("create_orb_edg", d.getOrb(op).getEp(), null, uu + 1, null);
        d.getOrb(op).setEp(array, array.length);
        d.getOrb(op).setEp(uu, ep);
        return (uu);
    }

    public static int create_orb_orb(DELANEY d, int op, int oq) {
        final int uu = d.getOrb(op).getOps();
        check_orb(d, op);
        check_orb(d, oq);
        final int[] array = Utilities.alloc("create_orb_orb", d.getOrb(op).getOp(), null, uu + 1, null);
        d.getOrb(op).setOp(array, array.length);
        d.getOrb(op).setOp(uu, oq);
        return (uu);
    }


    public static int create_ocr_ncr(DELANEY d, int oc, int nc) {
        int uu = d.getOcr(oc).getNcs();
        check_ocr(d, oc);
        check_ncr(d, nc);
        final int[] array = Utilities.alloc("create_ocr_ncr", d.getOcr(oc).getNc(), null, uu + 1, null);
        d.getOcr(oc).setNc(array, array.length);
        d.getOcr(oc).setNc(uu, nc);
        return (uu);
    }

    public static int create_ocr_ecr(DELANEY d, int oc, int ec) {
        final int uu = d.getOcr(oc).getEcs();
        check_ocr(d, oc);
        check_ecr(d, ec);
        final int[] array = Utilities.alloc("create_ocr_ecr", d.getOcr(oc).getEc(), null, uu + 1, null);
        d.getOcr(oc).setEc(array, array.length);
        d.getOcr(oc).setEc(uu, ec);
        return (uu);
    }

    public static int create_ocr_ocr(DELANEY d, int oc, int od) {
        final int uu = d.getOcr(oc).getOcs();
        check_ocr(d, oc);
        check_ocr(d, od);
        final int[] array = Utilities.alloc("create_ocr_ocr", d.getOcr(oc).getOc(), null, uu + 1, null);
        d.getOcr(oc).setOc(array, array.length);
        d.getOcr(oc).setOc(uu, oc);
        return (uu);
    }

    public static int create_fcr(DELANEY d, int oc) {
        final int of = d.getFcrs();
        check_ocr(d, oc);
        final int[] array = Utilities.alloc("create_fcr", d.getFcr(), null, of + 1, null);
        d.setFcr(array, array.length);
        d.setFcr(of, oc);
        return (of);
    }

    /***********************
     * Check
     ***********************/

    public static Boolean check_nod(DELANEY d, int np) {
        return (check_nod_adr(d, np) && check_nod_def(d, np));
    }

    public static Boolean check_edg(DELANEY d, int ep) {
        return (check_edg_adr(d, ep) && check_edg_def(d, ep));
    }

    public static Boolean check_orb(DELANEY d, int op) {
        return (check_orb_adr(d, op) && check_orb_def(d, op));
    }

    public static Boolean check_ncr(DELANEY d, int nc) {
        return (check_ncr_adr(d, nc) && check_ncr_def(d, nc));
    }

    public static Boolean check_ecr(DELANEY d, int ec) {
        return (check_ecr_adr(d, ec) && check_ecr_def(d, ec));
    }

    public static Boolean check_ocr(DELANEY d, int oc) {
        return (check_ocr_adr(d, oc) && check_ocr_def(d, oc));
    }


    public static Boolean check_nod_adr(DELANEY d, int np) {
        if (np < 0) {
            throw new RuntimeException("Illegal flag number");
        }
        return (true);
    }

    public static Boolean check_edg_adr(DELANEY d, int ep) {
        if (ep < 0) {
            throw new RuntimeException("Illegal edge number");
        }
        return (true);
    }

    public static Boolean check_orb_adr(DELANEY d, int op) {
        if (op < 0) {
            throw new RuntimeException("Illegal orbit number");
        }
        return (true);
    }

    public static Boolean check_ncr_adr(DELANEY d, int nc) {
        if (nc < 0) {
            throw new RuntimeException("Illegal flag-coordinates-number");
        }
        return (true);
    }

    public static Boolean check_ecr_adr(DELANEY d, int ec) {
        if (ec < 0) {
            throw new RuntimeException("Illegal edge-coordinates-number");
        }
        return (true);
    }

    public static Boolean check_ocr_adr(DELANEY d, int oc) {
        if (oc < 0) {
            throw new RuntimeException("Illegal orbit-coordinates-number");
        }
        return (true);
    }


    public static Boolean check_nod_def(DELANEY d, int np) {
        if (np >= d.getNods()) {
            throw new RuntimeException("Undefined flag number");
        }
        return (true);
    }

    public static Boolean check_edg_def(DELANEY d, int ep) {
        if (ep >= d.getEdgs()) {
            throw new RuntimeException("Undefined edge number");
        }
        return (true);
    }

    public static Boolean check_orb_def(DELANEY d, int op) {
        if (op >= d.getOrbs()) {
            throw new RuntimeException("Undefined orbit number");
        }
        return (true);
    }

    public static Boolean check_ncr_def(DELANEY d, int nc) {
        if (nc >= d.getNcrs()) {
            throw new RuntimeException("Undefined flag-coord-number");
        }
        return (true);
    }

    public static Boolean check_ecr_def(DELANEY d, int ec) {
        if (ec >= d.getEcrs()) {
            throw new RuntimeException("Undefined edge-coord-number");
        }
        return (true);
    }

    public static Boolean check_ocr_def(DELANEY d, int oc) {
        if (oc >= d.getOcrs()) {
            throw new RuntimeException("undefined orbit-coord-number");
        }
        return (true);
    }


    public static Boolean check_orb_nod(DELANEY d, int op, int p) {
        if (p < 0) {
            throw new RuntimeException("Illegal flag-position");
        }
        if (p >= d.getOrb(op).getNps()) {
            throw new RuntimeException("Undefined flag-position");
        }
        return (true);
    }

    public static Boolean check_orb_edg(DELANEY d, int op, int p) {
        if (p < 0) {
            throw new RuntimeException("Illegal edge-position");
        }
        if (p >= d.getOrb(op).getEps()) {
            throw new RuntimeException("Undefined edge-position");
        }
        return (true);
    }

    public static Boolean check_orb_orb(DELANEY d, int op, int p) {
        if (p < 0) {
            throw new RuntimeException("Illegal orbit-position");
        }
        if (p >= d.getOrb(op).getOps()) {
            throw new RuntimeException("Undefined orbit-position");
        }
        return (true);
    }

    public static Boolean check_orb_crd(DELANEY d, int op, int p) {
        if (p < 0) {
            throw new RuntimeException("Illegal coord-position");
        }
        if (p >= d.getOrb(op).getCrs()) {
            throw new RuntimeException("Undefined coord-position");
        }
        return (true);
    }


    public static Boolean check_fcr(DELANEY d, int p) {
        if (p < 0) {
            throw new RuntimeException("Illegal FD-position");
        }
        if (p >= d.getFcrs()) {
            throw new RuntimeException("Undefined FD-position");
        }
        return (true);
    }


    /***********************
     * Clear
     ***********************/

    public static Boolean clear_delaney(DELANEY d) {
        clear_file(d);
        clear_name(d);
        clear_nods(d);
        clear_edgs(d);
        clear_orbs(d);
        clear_crds(d);
        clear_fcrs(d);
        return (true);
    }

    public static Boolean clear_file(DELANEY d) {
        d.setFil(new char[0], 0);
        return true;
    }


    public static Boolean clear_name(DELANEY d) {
        d.setNam(new char[0], 0);
        return true;
    }


    public static Boolean clear_nods(DELANEY d) {
        d.setNod(new NOD[0], 0);
        return true;
    }

    public static Boolean clear_edgs(DELANEY d) {
        d.setEdg(new EDG[0], 0);
        return true;
    }

    public static Boolean clear_orbs(DELANEY d) {
        for (int op = 0; op < d.getOrbs(); ++op) {
            clear_orb_nods(d, op);
            clear_orb_edgs(d, op);
            clear_orb_orbs(d, op);
            clear_orb_crds(d, op);
        }
        d.setOrb(new ORB[0], 0);
        return true;
    }

    public static Boolean clear_crds(DELANEY d) {
        clear_ncrs(d);
        clear_ecrs(d);
        clear_ocrs(d);
        return (true);
    }

    public static Boolean clear_ncrs(DELANEY d) {
        d.setNcr(new NCR[0], 0);
        return true;
    }

    public static Boolean clear_ecrs(DELANEY d) {
        d.setEcr(new ECR[0], 0);
        return true;
    }

    public static Boolean clear_ocrs(DELANEY d) {
        d.setOcr(new OCR[0], 0);
        return true;
    }


    public static Boolean clear_orb_nods(DELANEY d, int op) {
        d.getOrb(op).setNp(new int[0], 0);
        return true;
    }

    public static Boolean clear_orb_edgs(DELANEY d, int op) {
        d.getOrb(op).setEp(new int[0], 0);
        return true;
    }

    public static Boolean clear_orb_orbs(DELANEY d, int op) {
        d.getOrb(op).setOp(new int[0], 0);
        return true;
    }

    public static Boolean clear_orb_crds(DELANEY d, int op) {
        d.getOrb(op).setCr(new int[0], 0);
        return true;
    }


    public static Boolean clear_fcrs(DELANEY d) {
        d.setFcr(new int[0], 0);
        return true;
    }
}
