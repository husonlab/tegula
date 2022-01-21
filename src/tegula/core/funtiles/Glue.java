/*
 * Glue.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.core.funtiles;

import tegula.core.funtiles.data.Base;
import tegula.core.funtiles.data.DelaneySymbolWithGeometry;
import tegula.core.funtiles.data.Utils;
import tegula.core.funtiles.utils.Maths;

/**
 * glue flags together
 * Based on del_glue.h by Klaus Westphal, 1990
 */
public class Glue {
    private int ncnt, ecnt, ocnt;
    private final DelaneySymbolWithGeometry d;

    public static void apply(DelaneySymbolWithGeometry d) {
        new Glue(d).apply();
    }

    private Glue(DelaneySymbolWithGeometry d) {
        this.d = d;
    }

    private void apply() {
        int ep, op;
        int np, oq;
        boolean change;
        Utils.prepare_m(d);
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            d.getOrb(op).setS(0);
            if (orb_is_chain(op)) {
                d.getOrb(op).setB(d.getOrb(op).getR() - 1);
            } else {
                d.getOrb(op).setB(2 * d.getOrb(op).getR());
            }
        }
        ncnt = 0;
        ecnt = 0;
        ocnt = 0;
        do {
            change = false;
            op = find_optimal_orb();
            if (op >= 0) {
/*			if((orb_is_complete (op) && orb_is_cut (op)) ||
                (orb_is_loop (op) &&
			     (orb_is_split (op) || orb_is_rotation (op)))) {	*/
                /**/
                if (orb_is_loop(op) && (orb_is_cut(op) || orb_is_split(op) || orb_is_rotation(op))) {
                    ep = find_weakest_edg(op);
                    if (ep < 0) {
                        throw new RuntimeException("no split edge found");
                    } else if (d.getEdge(ep).getOpx() == op) {
                        oq = d.getEdge(ep).getOpy();
                    } else if (d.getEdge(ep).getOpy() == op) {
                        oq = d.getEdge(ep).getOpx();
                    } else {
                        throw new RuntimeException("ep inconsistent");
                    }
                    if (d.getEdge(ep).getNpa() != d.getEdge(ep).getNpb()) {
                        d.getEdge(ep).incrMk();    /* inhibit gluing */
                        d.getOrb(oq).incrMk();    /* cut orbit */
                    }
                }
                if (glue_orb(op))
                    change = true;
                if (change) {
                    continue;
                }    /*	ok	*/
            }
            ep = find_optimal_edg();
            if (ep >= 0) {
                if (glue_edg(ep))
                    change = true;
                if (change) {
                }    /*	ok	*/
            }
        } while (change);
        if (ncnt != d.getNumberOfNodes()) {
            throw new RuntimeException("Graph not connected");
        }
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            d.getOrb(op).setB(0);
        }
        for (ep = 0; ep < d.getNumberOfEdges(); ++ep) {
            if (!edg_is_glued(ep)) {
                op = d.getEdge(ep).getOpx();
                d.getOrb(op).setB(d.getOrb(op).getB() + 1);
                op = d.getEdge(ep).getOpy();
                d.getOrb(op).setB(d.getOrb(op).getB() + 1);
            }
        }
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            if (d.getOrb(op).getS() == 0) {
                d.getOrb(op).setS(1);
            }
        }
        for (np = 0; np < d.getNumberOfNodes(); ++np) {
            if (d.getNode(np).getId() == 1) {
                d.getNode(np).setSg(+1);
                spread_sg(np);
                break;
            }
        }
        d.setImax(1);
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            d.getOrb(op).setI(d.getOrb(op).getV() * d.getOrb(op).getF() * d.getOrb(op).getS());
            // System.err.print(String.format("Glue %d -> %d\n",op, d.getOrb(op).getI()));

            if (d.getOrb(op).getI() > d.getImax()) {
                d.setImax(d.getOrb(op).getI());
            }
            d.getOrb(op).setAlph(Maths.m_2pi / (double) d.getOrb(op).getI());
            d.getOrb(op).setCosa(Math.cos(0.5 * d.getOrb(op).getAlph()));
            d.getOrb(op).setBeta(Math.PI - d.getOrb(op).getAlph());
        }
        d.setImin(d.getImax());
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            if (d.getOrb(op).getI() <= 2) {
                continue;
            }
            if (d.getOrb(op).getI() < d.getImin()) {
                d.setImin(d.getOrb(op).getI());
            }
        }
        create_coords();
        trace_fd();
        d.setFdl(0);
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            d.setFdl(d.getFdl() + d.getOrb(op).getS());
        }
        d.setFre(0);
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            d.setFre(d.getFre() + d.getOrb(op).getS());
            if (d.getOrb(op).getB() == 0) {
                d.setFre(d.getFre() + 1);
            } else if (d.getOrb(op).getF() < 2) {
                d.setFre(d.getFre() - 1);
            }
        }
        d.setCrv(-d.getNumberOfNodes());
        for (np = 0; np < d.getNumberOfNodes(); ++np) {
            op = d.getNode(np).getOp(0);
            d.setCrv(d.getCrv() + inv(d.getOrb(op).getM()));
            op = d.getNode(np).getOp(1);
            d.setCrv(d.getCrv() + inv(d.getOrb(op).getM()));
            op = d.getNode(np).getOp(2);
            d.setCrv(d.getCrv() + inv(d.getOrb(op).getM()));
        }
        if (Math.abs(d.getCrv()) < Maths.EPSILON) {
            d.setCrv(0.0);
        }
        d.setChi(-d.getNumberOfNodes());
        for (np = 0; np < d.getNumberOfNodes(); ++np) {
            op = d.getNode(np).getOp(0);
            d.setChi(d.getChi() + inv(d.getOrb(op).getR()));
            op = d.getNode(np).getOp(1);
            d.setChi(d.getChi() + inv(d.getOrb(op).getR()));
            op = d.getNode(np).getOp(2);
            d.setChi(d.getChi() + inv(d.getOrb(op).getR()));
        }
        if (Math.abs(d.getChi()) < Maths.EPSILON) {
            d.setChi(0.0);
        }
        d.setChi(d.getChi() * 0.5);
        d.setChr(0);
        for (np = 0; np < d.getNumberOfNodes(); ++np) {
            op = d.getNode(np).getOp(0);
            d.setChi(d.getChi() + d.getOrb(op).getV());
            op = d.getNode(np).getOp(1);
            d.setChi(d.getChi() - d.getOrb(op).getV());
            op = d.getNode(np).getOp(2);
            d.setChi(d.getChi() + d.getOrb(op).getV());
        }
        d.setDef(-2.0);
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            if (d.getOrb(op).getI() > 2) {
                d.setDef(d.getDef() + (1.0 - 2.0 / (double) d.getOrb(op).getI()) * (double) d.getOrb(op).getS());
            }
        }
        if (Math.abs(d.getDef()) < Maths.EPSILON) {
            d.setDef(0.0);
        }
        d.setDef(d.getDef() * Math.PI);
    }

    private int find_optimal_orb() {
        int op;
        int opt_o, min_i, max_m, min_b;
        opt_o = -1;
        min_i = +Integer.MAX_VALUE;    /*	Rotation * Mirror	*/
        max_m = -Integer.MAX_VALUE;    /*	Size		*/
        min_b = +Integer.MAX_VALUE;    /*	Remaining	*/
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            if (orb_is_glued(op)) {
                continue;
            }
            if (ncnt > 0 && orb_is_unreacheable(op)) {
                continue;
            }
            if (orb_is_complete(op) && orb_is_cut(op)) {
                continue;
            }
            /*		if(orb_is_split (op) && orb_is_loop (op)) { continue;}	*/
            if (orb_is_split(op)) {
                continue;
            }
            if (greater_than(
                    min_i, d.getOrb(op).getI(),    /*	Rotation * Mirror	*/
                    -max_m, -d.getOrb(op).getM(),    /*	Size		*/
                    min_b, d.getOrb(op).getB()        /*	Remaining	*/
            )) {
                opt_o = op;
                min_i = d.getOrb(op).getI();    /*	Rotation * Mirror	*/
                max_m = d.getOrb(op).getM();    /*	Size		*/
                min_b = d.getOrb(op).getB();    /*	Remaining	*/
            }
        }
        return (opt_o);
    }

    private int find_weakest_edg(int oq) {
        int ep, op;
        int na, nb;
        int p;
        int opt_e, max_i, min_m, max_b;
        opt_e = -1;
        max_i = -Integer.MAX_VALUE;    /*	Rotation * Mirror	*/
        min_m = +Integer.MAX_VALUE;    /*	Size		*/
        max_b = -Integer.MAX_VALUE;    /*	Remaining	*/
        for (p = 0; p < d.getOrb(oq).getEps(); ++p) {
            ep = d.getOrb(oq).getEp()[p];
            if (edg_is_glued(ep)) {
                continue;
            }
            if (edg_is_cut(ep)) {
                continue;
            }
            if (d.getEdge(ep).getOpx() == oq) {
                op = d.getEdge(ep).getOpy();
            } else if (d.getEdge(ep).getOpy() == oq) {
                op = d.getEdge(ep).getOpx();
            } else {
                throw new RuntimeException("ep inconsistent");
            }
            if (orb_is_glued(op)) {
                continue;
            }
            if (orb_is_complete(op)) {
                continue;
            }
            if (orb_is_cut(op)) {
                return (ep);
            }
            /*		if(orb_is_split (op) && orb_is_loop (op)) { return (ep);}	*/
            /**/
            if (orb_is_split(op)) {
                return (ep);
            }
            /**/
            na = d.getEdge(ep).getNpa();
            nb = d.getEdge(ep).getNpb();
            /**/
            if (na == nb) {
                continue;
            }    /*	Mirror	*/
            /**/
            if (nod_is_glued(na) && nod_is_glued(nb)) {
                return (ep);
            }
            if (less_or_equal(
                    max_i, d.getOrb(op).getI(),    /*	Rotation * Mirror	*/
                    -min_m, -d.getOrb(op).getM(),    /*	Size		*/
                    max_b, d.getOrb(op).getB()        /*	Remaining	*/
            )) {
                opt_e = ep;
                max_i = d.getOrb(op).getI();    /*	Rotation * Mirror	*/
                min_m = d.getOrb(op).getM();    /*	Size		*/
                max_b = d.getOrb(op).getB();    /*	Remaining	*/
            }
        }
        return (opt_e);
    }

    private int find_optimal_edg() {
        int ep;
        int na, nb;
        for (ep = 0; ep < d.getNumberOfEdges(); ++ep) {
            if (edg_is_glued(ep)) {
                continue;
            }
            if (edg_is_cut(ep)) {
                continue;
            }
            na = d.getEdge(ep).getNpa();
            nb = d.getEdge(ep).getNpb();
            if (na == nb) {
                continue;
            }    /*	Mirror	*/
            if (nod_is_glued(na) == nod_is_glued(nb)) {
                continue;
            }
            return (ep);
        }
        return (-1);
    }

    private boolean glue_orb(int op) {
        boolean change;
        int ti = d.getOrb(op).getTi();
        int tj = d.getOrb(op).getTj();
        int nb = d.getOrb(op).getNp(0);
        int na;

        if (ncnt == 0) {
            na = nb;
        } else {
            do {
                na = nb;
                nb = d.getNodes()[nb].getNp(ti);
                if (nod_is_glued(na)) {
                    break;
                }
                na = nb;
                nb = d.getNodes()[nb].getNp(tj);
                if (nod_is_glued(na)) {
                    break;
                }
            } while (na != d.getOrb(op).getNp(0));
        }
        int np = na;
        do {
            // System.err.print(String.format("A%d.%d.%d  np:%d\n",np,ti,d.getNode(np).getEp(ti),d.getNode(np).getNp(ti)));
            if (glue_edg(d.getNode(np).getEp(ti))) {
            }
            np = d.getNode(np).getNp(ti);
            // System.err.print(String.format("B%d.%d.%d\n",np,tj,d.getNode(np).getEp(tj)));

            if (glue_edg(d.getNode(np).getEp(tj))) {
            }
            np = d.getNode(np).getNp(tj);
        } while (np != na);
        d.getOrb(op).setId(++ocnt);        /*	glue orbit	*/
        change = true;
        return change;
    }

    private boolean glue_edg(int ep) {
        int na, nb;
        int ox, oy;
        boolean change = false;
        if (edg_is_glued(ep)) {
            return false;
        }
        if (edg_is_cut(ep)) {
            return false;
        }
        na = d.getEdge(ep).getNpa();
        nb = d.getEdge(ep).getNpb();
        ox = d.getEdge(ep).getOpx();
        oy = d.getEdge(ep).getOpy();
        if (na == nb) {                /*	Mirror	*/
            if (glue_nod(na))
                change = true;
            return change;
        }
        switch ((nod_is_glued(na) ? 1 : 0) + (nod_is_glued(nb) ? 1 : 0)) {
            case 0:
                d.getOrb(ox).setS(d.getOrb(ox).getS() + 1);
                d.getOrb(oy).setS(d.getOrb(oy).getS() + 1);    /*	split orbs	*/
                break;
            case 2:
                d.getOrb(ox).setS(d.getOrb(ox).getS() - 1);
                d.getOrb(oy).setS(d.getOrb(oy).getS() - 1);    /*	join splits	*/
                break;
            default:
                break;
        }
        d.getOrb(ox).setB(d.getOrb(ox).getB() - 1);
        d.getOrb(oy).setB(d.getOrb(oy).getB() - 1);    /*	count remaining boundary edges	*/
        d.getEdge(ep).setId(++ecnt);        /*	glue edge	*/
        change = true;
        if (glue_nod(na))
            change = true;
        if (glue_nod(nb))
            change = true;
        return change;                /*	ok	*/
    }

    private boolean glue_nod(int np) {
        int ty;
        int op;
        int ti, ei, ni;
        int tj, ej, nj;
        boolean change = false;
        d.getNode(np).incrMk();            /*	count glued edges of node	*/
        if (nod_is_glued(np)) {
            return change;
        }
        d.getNode(np).setId(++ncnt);        /*	glue node	*/
        change = true;
        for (ty = 0; ty < 3; ++ty) {
            op = d.getNode(np).getOp(ty);
            ti = d.getOrb(op).getTi();
            ei = d.getNode(np).getEp()[ti];
            ni = d.getNode(np).getNp()[ti];
            tj = d.getOrb(op).getTj();
            ej = d.getNode(np).getEp()[tj];
            nj = d.getNode(np).getNp()[tj];
            switch ((edg_is_glued(ei) ? 1 : 0) + (edg_is_glued(ej) ? 1 : 0)) {
                case 0:
                    d.getOrb(op).setS(d.getOrb(op).getS() + 1); /*	insert new split	*/
                    break;
                case 2:
                    d.getOrb(op).setS(d.getOrb(op).getS() - 1); /*	join splits		*/
                    break;
                default:
                    break;
            }
            // // System.err.print(String.format("%d.%d: %d\n",ty,op,(edg_is_glued(ei)?1:0)+(edg_is_glued(ej)?1:0)));

        }
        return change;                /*	ok	*/
    }

    private void spread_sg(int nr) {
        int ty, np, ep, op;
        for (ty = 0; ty < 3; ++ty) {
            op = d.getNodes()[nr].getOp(ty);
            ep = d.getNodes()[nr].getEp()[ty];
            np = d.getNodes()[nr].getNp()[ty];
            if (d.getNode(np).getSg() != 0) {
                if (d.getNode(np).getSg() != d.getNodes()[nr].getSg()) {
                    d.getEdge(ep).setSg(1);
                    if (d.getOrb(op).getSg() == 0) {
                        d.getOrb(op).setSg(1);
                    }
                } else {
                    d.getEdge(ep).setSg(-1);
                    d.getOrb(op).setSg(-1);
                }
                continue;        /*	sign ok		*/
            }
            if (!edg_is_glued(ep)) {
                continue;
            }
            d.getNode(np).setSg(-d.getNodes()[nr].getSg());
            spread_sg(np);
        }
    }

    private void create_coords() {
        int ep, ec;
        int npa, nca;
        int npb, ncb;
        int epax;
        int epay;
        int epbx;
        int epby;
        int ty;

        d.setNumberNodeCoordinates(0);
        d.setNumberEdgeCoordinates(0);
        d.setNumberOrbitCoordinates(0);

        for (npa = 0; npa < d.getNumberOfNodes(); ++npa) {
            d.getNodes()[npa].setCr(Base.create_ncr(d, npa));
        }
        for (npa = 0; npa < d.getNumberOfNodes(); ++npa) {
            nca = d.getNodes()[npa].getCr();
            for (ty = 0; ty < 3; ++ty) {
                npb = d.getNodes()[npa].getNp()[ty];
                ncb = d.getNodes()[npb].getCr();
                d.getNodeCoordinates()[nca].setNc(ty, ncb);
            }
        }
        for (npa = 0; npa < d.getNumberOfNodes(); ++npa) {
            nca = d.getNodes()[npa].getCr();
            for (ty = 0; ty < 3; ++ty) {
                ec = d.getNodeCoordinates()[nca].getEc()[ty];
                if (ec >= 0) {
                    continue;
                }
                ep = d.getNodes()[npa].getEp()[ty];
                ec = Base.create_ecr(d, ep);
                npb = d.getNodes()[npa].getNp()[ty];
                ncb = d.getNodes()[npb].getCr();
                if (d.getEdge(ep).getNpa() == npa && d.getEdge(ep).getNpb() == npb) {
                    d.getEdgeCoordinates(ec).setNca(nca);
                    d.getEdgeCoordinates(ec).setNcb(ncb);
                } else if (d.getEdge(ep).getNpb() == npa && d.getEdge(ep).getNpa() == npb) {
                    d.getEdgeCoordinates(ec).setNcb(nca);
                    d.getEdgeCoordinates(ec).setNca(ncb);
                } else {
                    throw new RuntimeException("np inconsistent");
                }
                d.getNodeCoordinates()[nca].getEc()[ty] = ec;
                if (d.getEdge(ep).getNpa() == npa) {
                    d.getEdge(ep).setCa(ec);
                }
                if (d.getEdge(ep).getNpb() == npa) {
                    d.getEdge(ep).setCb(ec);
                }
                if (!edg_is_glued(ep)) {
                    continue;
                }
                d.getNodeCoordinates()[ncb].getEc()[ty] = ec;
                if (d.getEdge(ep).getNpa() == npb) {
                    d.getEdge(ep).setCa(ec);
                }
                if (d.getEdge(ep).getNpb() == npb) {
                    d.getEdge(ep).setCb(ec);
                }
            }
        }
        for (ec = 0; ec < d.getNumberOfEdgeCoordinates(); ++ec) {
            ep = d.getEdgeCoordinates(ec).getEp();
            epax = d.getEdge(ep).getEpax();
            epay = d.getEdge(ep).getEpay();
            epbx = d.getEdge(ep).getEpbx();
            epby = d.getEdge(ep).getEpby();
            d.getEdgeCoordinates(ec).setEcax(d.getEdges()[epax].getCa());
            d.getEdgeCoordinates(ec).setEcay(d.getEdges()[epay].getCa());
            d.getEdgeCoordinates(ec).setEcbx(d.getEdges()[epbx].getCb());
            d.getEdgeCoordinates(ec).setEcby(d.getEdges()[epby].getCb());
        }
    }

    private void trace_fd() {
        int ot, et;
        int np, ep, op;
        int nc, ec, oc;
        int nb, eb, ob;

        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            Base.clear_orb_crds(d, op);
        }
        d.clearBorderCoordinates();

        eb = -1;
        for (ep = 0; ep < d.getNumberOfEdges(); ++ep) {
            if (!edg_is_glued(ep)) {
                eb = ep;
                break;
            }
        }
        if (eb < 0) {
            throw new RuntimeException("No unglued edge found");
        }
        np = d.getEdge(ep).getNpa();
        op = d.getEdge(ep).getOpx();
        ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
        et = d.getEdge(ep).getTy();
        nb = np;
        eb = ep;
        ob = op;
        nc = d.getNode(np).getCr();
        if (d.getEdge(ep).getNpa() == np) {
            ec = d.getEdge(ep).getCa();
        } else if (d.getEdge(ep).getNpb() == np) {
            ec = d.getEdge(ep).getCb();
        } else {
            throw new RuntimeException("ep inconsistent");
        }
        if (ec < 0) {
            throw new RuntimeException("ec inconsistent");
        }
        oc = -1;
        do {
            if (!edg_is_glued(ep)) {
                ot = 3 - ot - et;
                op = d.getNode(np).getOp(ot);
                oc = Base.create_ocr(d, op);
                Base.create_orb_crd(d, op, oc);
                /*			od = d.getNodeCoordinates()[nc].oc[3-ot-et];	*/
                Base.create_fcr(d, oc);
                Base.create_ocr_ncr(d, oc, nc);
                Base.create_ocr_ecr(d, oc, ec);
                /*			Base.create_ocr_ocr (d, oc, od);	*/
            } else {
                np = d.getNode(np).getNp()[et];
                nc = d.getNode(np).getCr();
                if (ec < 0) {
                    throw new RuntimeException("ec inconsistent");
                }
            }
            if (d.getEdge(ep).getOpx() == op) {
                d.getEdgeCoordinates(ec).setOcx(oc);
            }
            if (d.getEdge(ep).getOpy() == op) {
                d.getEdgeCoordinates(ec).setOcy(oc);
            }
            et = 3 - ot - et;
            ep = d.getNode(np).getEp()[et];
            if (d.getEdge(ep).getNpa() == np) {
                ec = d.getEdge(ep).getCa();
            } else if (d.getEdge(ep).getNpb() == np) {
                ec = d.getEdge(ep).getCb();
            } else {
                throw new RuntimeException("ep inconsistent");
            }
            if (ec < 0) {
                throw new RuntimeException("ec inconsistent");
            }
            if (d.getEdge(ep).getOpx() == op) {
                d.getEdgeCoordinates(ec).setOcx(oc);
            }
            if (d.getEdge(ep).getOpy() == op) {
                d.getEdgeCoordinates(ec).setOcy(oc);
            }
        } while (!(np == nb && ep == eb && op == ob));

        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
            if (d.getOrb(op).getI() > 1) {
                continue;
            }
            ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
            oc = Base.create_ocr(d, op);
            Base.create_orb_crd(d, op, oc);
            for (int p = 0; p < d.getOrb(op).getNps(); ++p) {
                np = d.getOrb(op).getNp(p);
                nc = d.getNode(np).getCr();
                d.getNodeCoordinates()[nc].setOc(ot, oc);
                ep = d.getOrb(op).getEp(p);
                et = d.getEdge(ep).getTy();
                if (d.getEdge(ep).getNpa() == np) {
                    ec = d.getEdge(ep).getCa();
                } else if (d.getEdge(ep).getNpb() == np) {
                    ec = d.getEdge(ep).getCb();
                } else {
                    throw new RuntimeException("ep inconsistent");
                }
                if (ec < 0) {
                    throw new RuntimeException("ec inconsistent");
                }
                if (d.getEdge(ep).getOpx() == op) {
                    d.getEdgeCoordinates(ec).setOcx(oc);
                } else if (d.getEdge(ep).getOpy() == op) {
                    d.getEdgeCoordinates(ec).setOcy(oc);
                } else {
                    throw new RuntimeException("ep inconsistent");
                }
                /*			od = d.getNodeCoordinates()[nc].oc[3-ot-et];	*/
                Base.create_ocr_ncr(d, oc, nc);
                Base.create_ocr_ecr(d, oc, ec);
                /*			Base.create_ocr_ocr (d, oc, od);	*/
            }
        }
        for (ec = 0; ec < d.getNumberOfEdgeCoordinates(); ++ec) {
            ep = d.getEdgeCoordinates(ec).getEp();
            op = d.getEdge(ep).getOpx();
            ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
            oc = d.getEdgeCoordinates(ec).getOcx();
            nc = d.getEdgeCoordinates(ec).getNca();
            d.getNodeCoordinates()[nc].setOc(ot, oc);
            nc = d.getEdgeCoordinates(ec).getNcb();
            d.getNodeCoordinates()[nc].setOc(ot, oc);
            op = d.getEdge(ep).getOpy();
            ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
            oc = d.getEdgeCoordinates(ec).getOcy();
            nc = d.getEdgeCoordinates(ec).getNca();
            d.getNodeCoordinates()[nc].setOc(ot, oc);
            nc = d.getEdgeCoordinates(ec).getNcb();
            d.getNodeCoordinates()[nc].setOc(ot, oc);
        }
        for (nc = 0; nc < d.getNumberOfNodeCoordinates(); ++nc) {
            for (et = 0; et < 3; ++et) {
                ec = d.getNodeCoordinates()[nc].getEc()[et];
                oc = d.getEdgeCoordinates(ec).getOcx();
                op = d.getOrbitCoordinates(oc).getOp();
                ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
                d.getNodeCoordinates()[nc].setOc(ot, oc);
                oc = d.getEdgeCoordinates(ec).getOcy();
                op = d.getOrbitCoordinates(oc).getOp();
                ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
                d.getNodeCoordinates()[nc].setOc(ot, oc);
            }
        }
    }


    private boolean nod_is_glued(int np) {
        return d.getNodes()[np].getId() > 0;
    }

    private boolean edg_is_glued(int ep) {
        return d.getEdge(ep).getId() > 0;
    }

    private boolean orb_is_glued(int op) {
        return d.getOrb(op).getId() > 0;
    }

    private boolean edg_is_cut(int ep) {
        return d.getEdge(ep).getMk() > 0;
    }

    private boolean orb_is_cut(int op) {
        return d.getOrb(op).getMk() > 0;
    }

    private boolean orb_is_rotation(int op) {
        return d.getOrb(op).getV() > 1;
    }

    private boolean orb_is_chain(int op) {
        return d.getOrb(op).getF() == 2;
    }

    private boolean orb_is_loop(int op) {
        return d.getOrb(op).getF() == 1;
    }

    private boolean orb_is_split(int op) {
        return d.getOrb(op).getS() > 1;
    }

    private boolean orb_is_unreacheable(int op) {
        return d.getOrb(op).getS() == 0;
    }

    private boolean orb_is_complete(int op) {
        return d.getOrb(op).getB() < 1;
    }

    private static boolean greater_than(int a1, int b1, int a2, int b2, int a3, int b3) {
        // System.err.print(String.format("GT: %d %d %d %d %d %d\n",a1,b1,a2,b2,a3,b3));
        return !less_or_equal(a1, b1, a2, b2, a3, b3);
    }

    private static boolean less_or_equal(int a1, int b1, int a2, int b2, int a3, int b3) {
        if (a1 < b1) {
            return (true);
        } else if (a1 > b1) {
            return (false);
        }
        if (a2 < b2) {
            return (true);
        } else if (a2 > b2) {
            return (false);
        }
        if (a3 < b3) {
            return (true);
        } else return a3 <= b3;
    }

    private static double inv(int x) {
        return (1.0 / (double) x);
    }
}
