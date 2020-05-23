/*
 * Util.java Copyright (C) 2020. Daniel H. Huson
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

/**
 * utilities for Delaney symbols
 * Based on del_util.h by Klaus Westphal, 1990
 */
public class Utils {

    public static void define_edg(DelaneySymbolWithGeometry d, int et, int na, int nb) {
        int ep;
        if (na > nb) {
            ep = na;
            na = nb;
            nb = ep;
        }
        for (ep = 0; ep < d.getNumberOfEdges(); ++ep) {
            if ((d.getEdge(ep).getNpa() == na || d.getEdge(ep).getNpb() == nb) && d.getEdge(ep).getTy() == et)
                break;
        }
        if (ep >= d.getNumberOfEdges()) {
            ep = d.addNewEdge();
        } else if (d.getEdge(ep).getNpa() != na) {
            throw new RuntimeException("E-N-pointer bad");
        } else if (d.getEdge(ep).getNpb() != nb) {
            throw new RuntimeException("E-N-pointer bad");
        }
        d.getEdge(ep).setMk(d.getEdge(ep).getMk() + 1);

        d.getEdge(ep).setTy(et);
        d.getEdge(ep).setNpa(na);
        d.getEdge(ep).setNpb(nb);
        /*
        if(na != nb)
        {
        } else
        {
        }
        */
    }

    /***********************
     * Orbit Definition
     ***********************/

    public static void finish_graph(DelaneySymbolWithGeometry d) {
        link_nodes(d);
        trace_orbs(d, 1, 2);
        trace_orbs(d, 2, 0);
        trace_orbs(d, 0, 1);
        sort_orb_nods(d);
        def_orb_edgs(d);
        def_orb_orbs(d);
    }

    public static void link_nodes(DelaneySymbolWithGeometry d) {
        int ep, np;
        int et, na, nb;
        for (ep = 0; ep < d.getNumberOfEdges(); ++ep) {
            if (d.getEdge(ep).getMk() <= 0)
                throw new RuntimeException("Undefined edge");

            et = d.getEdge(ep).getTy();
            na = d.getEdge(ep).getNpa();
            nb = d.getEdge(ep).getNpb();
            if (na >= d.getNumberOfNodes()) {
                d.setNumberOfNodes(d.getNumberOfNodes() + 1);
            } else if (na < 0) {
                throw new RuntimeException("Bad E-N-pointer");
            } else if (d.getNode(na).getNp(et) >= 0 && d.getNode(na).getNp(et) != nb) {
                throw new RuntimeException("Bad N-N-pointer");
            } else if (d.getNode(na).getEp(et) >= 0 && d.getNode(na).getEp(et) != ep) {
                throw new RuntimeException("N-E-pointer bad");
            }
            d.getNode(na).incrMk();
            d.getNode(na).setNp(et, nb);
            d.getNode(na).setEp(et, ep);
            if (na == nb) {
                continue;
            }

            d.ensureNode(nb);

            if (nb < 0) {
                throw new RuntimeException("E-N-pointer illegal");
            } else if (d.getNode(nb).getNp(et) >= 0 && d.getNode(nb).getNp(et) != na) {
                throw new RuntimeException("N-N-pointer bad");
            } else if (d.getNode(nb).getEp(et) >= 0 && d.getNode(nb).getEp(et) != ep) {
                throw new RuntimeException("N-E-pointer bad");
            }
            d.getNode(nb).incrMk();
            d.getNode(nb).setNp(et, na);
            d.getNode(nb).setEp(et, ep);
        }
        for (np = 0; np < d.getNumberOfNodes(); ++np) {
            if (d.getNode(np).getMk() < 3) {
                throw new RuntimeException("too few edges lead to vertices");
            } else if (d.getNode(np).getMk() > 3) {
                throw new RuntimeException("too many edges lead to vertices");
            }
        }
    }

    public static void trace_orbs(DelaneySymbolWithGeometry d, int ti, int tj) {
        int nk;
        int np, op;
        int ot;

        ot = 3 - ti - tj;
        for (nk = 0; nk < d.getNumberOfNodes(); ++nk)
            d.getNode(nk).setMk(0);

        for (np = 0; np < d.getNumberOfNodes(); ++np) {
            if (d.getNode(np).getMk() <= 0) {
                op = d.addNewOrbit();
                nk = np;
                do {
                    d.getOrb(op).setR(d.getOrb(op).getR() + 1);
                    d.getNode(nk).setOp(ot, op);
                    def_orb(d, nk, op, ti, tj);
                    /* d.getNode(nk).p[ot] = */
                    Base.create_orb_nod(d, op, nk);
                    d.getNode(nk).setMk(d.getNode(nk).getMk() + 1);
                    nk = d.getNode(nk).getNp(ti);
                    d.getNode(nk).setOp(ot, op);
                    def_orb(d, nk, op, tj, ti);
                    /* d.getNode(nk).p[ot] = */
                    Base.create_orb_nod(d, op, nk);
                    d.getNode(nk).setMk(d.getNode(nk).getMk() + 1);
                    nk = d.getNode(nk).getNp(tj);
                }
                while (nk != np);
                d.getOrb(op).setM(d.getOrb(op).getR());
            }
        }
    }

    public static void def_orb(DelaneySymbolWithGeometry d, int np, int op, int ti, int tj) {
        int na, nb;
        int ep;
        ep = d.getNode(np).getEp(ti);
        na = d.getEdge(ep).getNpa();
        nb = d.getEdge(ep).getNpb();
        d.getEdge(ep).setMk(d.getEdge(ep).getMk() + 1);
        if (d.getEdge(ep).getOpx() == -1) {
            d.getEdge(ep).setOpx(op);
            d.getEdge(ep).setEpax(d.getNode(na).getEp(tj));
            d.getEdge(ep).setEpbx(d.getNode(nb).getEp(tj));
        } else if (d.getEdge(ep).getOpx() != op) {
            d.getEdge(ep).setOpy(op);
            d.getEdge(ep).setEpay(d.getNode(na).getEp(tj));
            d.getEdge(ep).setEpby(d.getNode(nb).getEp(tj));
        }
        if (d.getNode(np).getNp(ti) == np) {
            d.getOrb(op).setF(2);
            d.getOrb(op).setI(d.getOrb(op).getF() * d.getOrb(op).getV());
            // System.err.print(String.format("def_orb %d -> %d\n",op, d.getOrb(op).getI()));

        }
        d.getOrb(op).setTi(ti);
        d.getOrb(op).setTj(tj);
    }

    public static void sort_orb_nods(DelaneySymbolWithGeometry d) {
        int op, ti, tj;
        int nk, p;
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
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

    public static void def_orb_edgs(DelaneySymbolWithGeometry d) {
        int ep, op, ti, tj;
        int nk, p;
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            ti = d.getOrb(op).getTi();
            tj = d.getOrb(op).getTj();
            for (p = 0; p < d.getOrb(op).getNps(); ++p) {
                nk = d.getOrb(op).getNp(p);
                ep = d.getNode(nk).getEp(ti);
                Base.create_orb_edg(d, op, ep);
                ++p;
                if (p >= d.getOrb(op).getNps()) {
                    break;
                }
                nk = d.getOrb(op).getNp(p);
                ep = d.getNode(nk).getEp(tj);
                Base.create_orb_edg(d, op, ep);
            }
        }
    }

    public static void def_orb_orbs(DelaneySymbolWithGeometry d) {
        int op;
        int ek, p;
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            for (p = 0; p < d.getOrb(op).getEps(); ++p) {
                ek = d.getOrb(op).getEp(p);
                if (d.getEdge(ek).getOpx() == op) {
                    Base.create_orb_orb(d, op, d.getEdge(ek).getOpy());
                } else {
                    Base.create_orb_orb(d, op, d.getEdge(ek).getOpx());
                }
                ++p;
                if (p >= d.getOrb(op).getEps()) {
                    break;
                }
                ek = d.getOrb(op).getEp(p);
                if (d.getEdge(ek).getOpx() == op) {
                    Base.create_orb_orb(d, op, d.getEdge(ek).getOpy());
                } else {
                    Base.create_orb_orb(d, op, d.getEdge(ek).getOpx());
                }
            }
        }
    }


    /***********************
     * Node Definition
     ***********************/

    public static void prepare_m(DelaneySymbolWithGeometry d) {
        int p;
        for (p = 0; p < d.getNumberOfNodes(); ++p) {
            d.getNode(p).setId(0);
            d.getNode(p).setMk(0);
        }
        for (p = 0; p < d.getNumberOfEdges(); ++p) {
            d.getEdge(p).setId(0);
            d.getEdge(p).setMk(0);
        }
        for (p = 0; p < d.getNumberOfOrbits(); ++p) {
            d.getOrb(p).setId(0);
            d.getOrb(p).setMk(0);
        }
    }

    public static void define_m(DelaneySymbolWithGeometry d, int np, int ti, int tj, int m) {
        int op = d.getNode(np).getOp(3 - ti - tj);
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
    }
}
