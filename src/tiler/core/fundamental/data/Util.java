package tiler.core.fundamental.data;

/**
 * utilities for Delaney symbols
 * Created by huson on 3/28/16.
 * Based on del_util.h by Klaus Westphal, 1990
 */
public class Util {

    public static int define_edg(DELANEY d, int et, int na, int nb) {
        int ep;
        if (na > nb) {
            ep = na;
            na = nb;
            nb = ep;
        }
        for (ep = 0; ep < d.getEdgs(); ++ep) {
            if ((d.getEdg(ep).getNpa() == na || d.getEdg(ep).getNpb() == nb) && d.getEdg(ep).getTy() == et)
                break;
        }
        if (ep >= d.getEdgs()) {
            ep = Base.create_edg(d);
        } else if (d.getEdg(ep).getNpa() != na) {
            throw new RuntimeException("E-N-pointer bad");
        } else if (d.getEdg(ep).getNpb() != nb) {
            throw new RuntimeException("E-N-pointer bad");
        }
        d.getEdg(ep).setMk(d.getEdg(ep).getMk() + 1);

        d.getEdg(ep).setTy(et);
        d.getEdg(ep).setNpa(na);
        d.getEdg(ep).setNpb(nb);
        /*
        if(na != nb)
        {
        } else
        {
        }
        */
        return (ep);
    }


    /***********************
     * Orbit Definition
     ***********************/

    public static void finish_graph(DELANEY d) {
        link_nodes(d);
        trace_orbs(d, 1, 2);
        trace_orbs(d, 2, 0);
        trace_orbs(d, 0, 1);
        sort_orb_nods(d);
        def_orb_edgs(d);
        def_orb_orbs(d);
    }

    public static void link_nodes(DELANEY d) {
        int ep, np;
        int et, na, nb;
        for (ep = 0; ep < d.getEdgs(); ++ep) {
            if (d.getEdg(ep).getMk() <= 0)
                throw new RuntimeException("Undefined edge");

            et = d.getEdg(ep).getTy();
            na = d.getEdg(ep).getNpa();
            nb = d.getEdg(ep).getNpb();
            if (na >= d.getNods()) {
                Base.init_nod(d, na);
            } else if (na < 0) {
                throw new RuntimeException("Bad E-N-pointer");
            } else if (d.getNod(na).getNp(et) >= 0 && d.getNod(na).getNp(et) != nb) {
                throw new RuntimeException("Bad N-N-pointer");
            } else if (d.getNod(na).getEp(et) >= 0 && d.getNod(na).getEp(et) != ep) {
                throw new RuntimeException("N-E-pointer bad");
            }
            d.getNod(na).incrMk();
            d.getNod(na).setNp(et, nb);
            d.getNod(na).setEp(et, ep);
            if (na == nb) {
                continue;
            }
            if (nb >= d.getNods()) {
                Base.init_nod(d, nb);
            } else if (nb < 0) {
                throw new RuntimeException("E-N-pointer illegal");
            } else if (d.getNod(nb).getNp(et) >= 0 && d.getNod(nb).getNp(et) != na) {
                throw new RuntimeException("N-N-pointer bad");
            } else if (d.getNod(nb).getEp(et) >= 0 && d.getNod(nb).getEp(et) != ep) {
                throw new RuntimeException("N-E-pointer bad");
            }
            d.getNod(nb).incrMk();
            d.getNod(nb).setNp(et, na);
            d.getNod(nb).setEp(et, ep);
        }
        for (np = 0; np < d.getNods(); ++np) {
            if (d.getNod(np).getMk() < 3) {
                throw new RuntimeException("too few edges lead to vertices");
            } else if (d.getNod(np).getMk() > 3) {
                throw new RuntimeException("too many edges lead to vertices");
            }
        }
    }

    public static void trace_orbs(DELANEY d, int ti, int tj) {
        int nk;
        int np, op;
        int ot;

        ot = 3 - ti - tj;
        for (nk = 0; nk < d.getNods(); ++nk)
            d.getNod(nk).setMk(0);

        for (np = 0; np < d.getNods(); ++np) {
            if (d.getNod(np).getMk() <= 0) {
                op = d.getOrbs();
                Base.init_orb(d, op);
                nk = np;
                do {
                    d.getOrb(op).setR(d.getOrb(op).getR() + 1);
                    d.getNod(nk).setOp(ot, op);
                    def_orb(d, nk, op, ti, tj);
                /* d.getNod(nk).p[ot] = */
                    Base.create_orb_nod(d, op, nk);
                    d.getNod(nk).setMk(d.getNod(nk).getMk() + 1);
                    nk = d.getNod(nk).getNp(ti);
                    d.getNod(nk).setOp(ot, op);
                    def_orb(d, nk, op, tj, ti);
				/* d.getNod(nk).p[ot] = */
                    Base.create_orb_nod(d, op, nk);
                    d.getNod(nk).setMk(d.getNod(nk).getMk() + 1);
                    nk = d.getNod(nk).getNp(tj);
                }
                while (nk != np);
                d.getOrb(op).setM(d.getOrb(op).getR());
            }
        }
    }

    public static void def_orb(DELANEY d, int np, int op, int ti, int tj) {
        int na, nb;
        int ep;
        ep = d.getNod(np).getEp(ti);
        na = d.getEdg(ep).getNpa();
        nb = d.getEdg(ep).getNpb();
        d.getEdg(ep).setMk(d.getEdg(ep).getMk() + 1);
        if (d.getEdg(ep).getOpx() == -1) {
            d.getEdg(ep).setOpx(op);
            d.getEdg(ep).setEpax(d.getNod(na).getEp(tj));
            d.getEdg(ep).setEpbx(d.getNod(nb).getEp(tj));
        } else if (d.getEdg(ep).getOpx() != op) {
            d.getEdg(ep).setOpy(op);
            d.getEdg(ep).setEpay(d.getNod(na).getEp(tj));
            d.getEdg(ep).setEpby(d.getNod(nb).getEp(tj));
        }
        if (d.getNod(np).getNp(ti) == np) {
            d.getOrb(op).setF(2);
            d.getOrb(op).setI(d.getOrb(op).getF() * d.getOrb(op).getV());
            // System.err.print(String.format("def_orb %d -> %d\n",op, d.getOrb(op).getI()));

        }
        d.getOrb(op).setTi(ti);
        d.getOrb(op).setTj(tj);
    }

    public static void sort_orb_nods(DELANEY d) {
        int op, ti, tj;
        int nk, p;
        for (op = 0; op < d.getOrbs(); ++op) {
            ti = d.getOrb(op).getTi();
            tj = d.getOrb(op).getTj();
            if (d.getOrb(op).getF() == 2) {
                while (d.getOrb(op).getNp(0) != d.getOrb(op).getNp(d.getOrb(op).getNps() - 1)) {
                    nk = d.getOrb(op).getNp(0);
                    for (p = 1; p < d.getOrb(op).getNps(); ++p) {
                        d.getOrb(op).setNp(p - 1, d.getOrb(op).getNp(p));
                    }
                    d.getOrb(op).setNp(p - 1, nk);
                    nk = ti;
                    ti = tj;
                    tj = nk;
                }
                d.getOrb(op).setTi(ti);
                d.getOrb(op).setTj(tj);
            }
        }
    }

    public static void def_orb_edgs(DELANEY d) {
        int ep, op, ti, tj;
        int nk, p;
        for (op = 0; op < d.getOrbs(); ++op) {
            ti = d.getOrb(op).getTi();
            tj = d.getOrb(op).getTj();
            for (p = 0; p < d.getOrb(op).getNps(); ++p) {
                nk = d.getOrb(op).getNp(p);
                ep = d.getNod(nk).getEp(ti);
                Base.create_orb_edg(d, op, ep);
                ++p;
                if (p >= d.getOrb(op).getNps()) {
                    break;
                }
                nk = d.getOrb(op).getNp(p);
                ep = d.getNod(nk).getEp(tj);
                Base.create_orb_edg(d, op, ep);
            }
        }
    }

    public static void def_orb_orbs(DELANEY d) {
        int op;
        int ek, p;
        for (op = 0; op < d.getOrbs(); ++op) {
            for (p = 0; p < d.getOrb(op).getEps(); ++p) {
                ek = d.getOrb(op).getEp(p);
                if (d.getEdg(ek).getOpx() == op) {
                    Base.create_orb_orb(d, op, d.getEdg(ek).getOpy());
                } else {
                    Base.create_orb_orb(d, op, d.getEdg(ek).getOpx());
                }
                ++p;
                if (p >= d.getOrb(op).getEps()) {
                    break;
                }
                ek = d.getOrb(op).getEp(p);
                if (d.getEdg(ek).getOpx() == op) {
                    Base.create_orb_orb(d, op, d.getEdg(ek).getOpy());
                } else {
                    Base.create_orb_orb(d, op, d.getEdg(ek).getOpx());
                }
            }
        }
    }


    /***********************
     * Node Definition
     ***********************/

    public static void prepare_m(DELANEY d) {
        int p;
        for (p = 0; p < d.getNods(); ++p) {
            d.getNod(p).setId(0);
            d.getNod(p).setMk(0);
        }
        for (p = 0; p < d.getEdgs(); ++p) {
            d.getEdg(p).setId(0);
            d.getEdg(p).setMk(0);
        }
        for (p = 0; p < d.getOrbs(); ++p) {
            d.getOrb(p).setId(0);
            d.getOrb(p).setMk(0);
        }
    }

    public static int define_m(DELANEY d, int np, int ti, int tj, int m) {
        int op = d.getNod(np).getOp(3 - ti - tj);
        if (m % d.getOrb(op).getR() != 0) {
            throw new RuntimeException("m is not multiple of r");
        }
        if (d.getOrb(op).getMk() > 0 && d.getOrb(op).getM() != m) {
            throw new RuntimeException("m already defined");
        }
        d.getOrb(op).setMk(d.getOrb(op).getMk() + 1);
        d.getOrb(op).setTi(ti);
        d.getOrb(op).setTj(tj);
        d.getOrb(op).setM(m);
        d.getOrb(op).setV(m / d.getOrb(op).getR());
        d.getOrb(op).setI(d.getOrb(op).getF() * d.getOrb(op).getV());
        // System.err.print(String.format("define_m %d -> %d\n",op, d.getOrb(op).getI()));

        return (op);
    }
}
