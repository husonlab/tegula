package tegula.core.fundamental;

import tegula.core.fundamental.data.Base;
import tegula.core.fundamental.data.DELANEY;
import tegula.core.fundamental.data.Util;
import tegula.core.fundamental.utils.Maths;

/**
 * glue flags together
 * Created by huson on 3/28/16.
 * Based on del_glue.h by Klaus Westphal, 1990
 */
public class Glue {
    private int ncnt, ecnt, ocnt;
    private final DELANEY d;

    public Glue(DELANEY d) {
        this.d = d;
    }

    public void glue_delaney() {
        int ep, op;
        int np, oq;
        boolean change;
        Util.prepare_m(d);
        for (op = 0; op < d.getOrbs(); ++op) {
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
                    } else if (d.getEdg(ep).getOpx() == op) {
                        oq = d.getEdg(ep).getOpy();
                    } else if (d.getEdg(ep).getOpy() == op) {
                        oq = d.getEdg(ep).getOpx();
                    } else {
                        throw new RuntimeException("ep inconsistent");
                    }
                    if (d.getEdg(ep).getNpa() != d.getEdg(ep).getNpb()) {
                        d.getEdg(ep).incrMk();    /* inhibit gluing */
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
                    continue;
                }    /*	ok	*/
            }
        } while (change);
        if (ncnt != d.getNods()) {
            throw new RuntimeException("Graph not connected");
        }
        for (op = 0; op < d.getOrbs(); ++op) {
            d.getOrb(op).setB(0);
        }
        for (ep = 0; ep < d.getEdgs(); ++ep) {
            if (!edg_is_glued(ep)) {
                op = d.getEdg(ep).getOpx();
                d.getOrb(op).setB(d.getOrb(op).getB() + 1);
                op = d.getEdg(ep).getOpy();
                d.getOrb(op).setB(d.getOrb(op).getB() + 1);
            }
        }
        for (op = 0; op < d.getOrbs(); ++op) {
            if (d.getOrb(op).getS() == 0) {
                d.getOrb(op).setS(1);
            }
        }
        for (np = 0; np < d.getNods(); ++np) {
            if (d.getNod(np).getId() == 1) {
                d.getNod(np).setSg(+1);
                spread_sg(np);
                break;
            }
        }
        d.setImax(1);
        for (op = 0; op < d.getOrbs(); ++op) {
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
        for (op = 0; op < d.getOrbs(); ++op) {
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
        for (op = 0; op < d.getOrbs(); ++op) {
            d.setFdl(d.getFdl() + d.getOrb(op).getS());
        }
        d.setFre(0);
        for (op = 0; op < d.getOrbs(); ++op) {
            d.setFre(d.getFre() + d.getOrb(op).getS());
            if (d.getOrb(op).getB() == 0) {
                d.setFre(d.getFre() + 1);
            } else if (d.getOrb(op).getF() < 2) {
                d.setFre(d.getFre() - 1);
            }
        }
        d.setCrv(-d.getNods());
        for (np = 0; np < d.getNods(); ++np) {
            op = d.getNod(np).getOp(0);
            d.setCrv(d.getCrv() + inv(d.getOrb(op).getM()));
            op = d.getNod(np).getOp(1);
            d.setCrv(d.getCrv() + inv(d.getOrb(op).getM()));
            op = d.getNod(np).getOp(2);
            d.setCrv(d.getCrv() + inv(d.getOrb(op).getM()));
        }
        if (Math.abs(d.getCrv()) < Maths.EPSILON) {
            d.setCrv(0.0);
        }
        d.setChi((double) -d.getNods());
        for (np = 0; np < d.getNods(); ++np) {
            op = d.getNod(np).getOp(0);
            d.setChi(d.getChi() + inv(d.getOrb(op).getR()));
            op = d.getNod(np).getOp(1);
            d.setChi(d.getChi() + inv(d.getOrb(op).getR()));
            op = d.getNod(np).getOp(2);
            d.setChi(d.getChi() + inv(d.getOrb(op).getR()));
        }
        if (Math.abs(d.getChi()) < Maths.EPSILON) {
            d.setChi(0.0);
        }
        d.setChi(d.getChi() * 0.5);
        d.setChr(0);
        for (np = 0; np < d.getNods(); ++np) {
            op = d.getNod(np).getOp(0);
            d.setChi(d.getChi() + d.getOrb(op).getV());
            op = d.getNod(np).getOp(1);
            d.setChi(d.getChi() - d.getOrb(op).getV());
            op = d.getNod(np).getOp(2);
            d.setChi(d.getChi() + d.getOrb(op).getV());
        }
        d.setDef(-2.0);
        for (op = 0; op < d.getOrbs(); ++op) {
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
        for (op = 0; op < d.getOrbs(); ++op) {
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
            if (d.getEdg(ep).getOpx() == oq) {
                op = d.getEdg(ep).getOpy();
            } else if (d.getEdg(ep).getOpy() == oq) {
                op = d.getEdg(ep).getOpx();
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
            na = d.getEdg(ep).getNpa();
            nb = d.getEdg(ep).getNpb();
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
        for (ep = 0; ep < d.getEdgs(); ++ep) {
            if (edg_is_glued(ep)) {
                continue;
            }
            if (edg_is_cut(ep)) {
                continue;
            }
            na = d.getEdg(ep).getNpa();
            nb = d.getEdg(ep).getNpb();
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
                nb = d.getNod()[nb].getNp(ti);
                if (nod_is_glued(na)) {
                    break;
                }
                na = nb;
                nb = d.getNod()[nb].getNp(tj);
                if (nod_is_glued(na)) {
                    break;
                }
            } while (na != d.getOrb(op).getNp(0));
        }
        int np = na;
        do {
            // System.err.print(String.format("A%d.%d.%d  np:%d\n",np,ti,d.getNod(np).getEp(ti),d.getNod(np).getNp(ti)));
            if (glue_edg(d.getNod(np).getEp(ti)))
                change = true;
            np = d.getNod(np).getNp(ti);
            // System.err.print(String.format("B%d.%d.%d\n",np,tj,d.getNod(np).getEp(tj)));

            if (glue_edg(d.getNod(np).getEp(tj)))
                change = true;
            np = d.getNod(np).getNp(tj);
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
        na = d.getEdg(ep).getNpa();
        nb = d.getEdg(ep).getNpb();
        ox = d.getEdg(ep).getOpx();
        oy = d.getEdg(ep).getOpy();
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
        d.getEdg(ep).setId(++ecnt);        /*	glue edge	*/
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
        d.getNod(np).incrMk();            /*	count glued edges of node	*/
        if (nod_is_glued(np)) {
            return change;
        }
        d.getNod(np).setId(++ncnt);        /*	glue node	*/
        change = true;
        for (ty = 0; ty < 3; ++ty) {
            op = d.getNod(np).getOp(ty);
            ti = d.getOrb(op).getTi();
            ei = d.getNod(np).getEp()[ti];
            ni = d.getNod(np).getNp()[ti];
            tj = d.getOrb(op).getTj();
            ej = d.getNod(np).getEp()[tj];
            nj = d.getNod(np).getNp()[tj];
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
            op = d.getNod()[nr].getOp(ty);
            ep = d.getNod()[nr].getEp()[ty];
            np = d.getNod()[nr].getNp()[ty];
            if (d.getNod(np).getSg() != 0) {
                if (d.getNod(np).getSg() != d.getNod()[nr].getSg()) {
                    d.getEdg(ep).setSg(1);
                    if (d.getOrb(op).getSg() == 0) {
                        d.getOrb(op).setSg(1);
                    }
                } else {
                    d.getEdg(ep).setSg(-1);
                    d.getOrb(op).setSg(-1);
                }
                continue;        /*	sign ok		*/
            }
            if (!edg_is_glued(ep)) {
                continue;
            }
            d.getNod(np).setSg(-d.getNod()[nr].getSg());
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

        Base.clear_crds(d);
        for (npa = 0; npa < d.getNods(); ++npa) {
            d.getNod()[npa].setCr(Base.create_ncr(d, npa));
        }
        for (npa = 0; npa < d.getNods(); ++npa) {
            nca = d.getNod()[npa].getCr();
            for (ty = 0; ty < 3; ++ty) {
                npb = d.getNod()[npa].getNp()[ty];
                ncb = d.getNod()[npb].getCr();
                d.getNcr()[nca].setNc(ty, ncb);
            }
        }
        for (npa = 0; npa < d.getNods(); ++npa) {
            nca = d.getNod()[npa].getCr();
            for (ty = 0; ty < 3; ++ty) {
                ec = d.getNcr()[nca].getEc()[ty];
                if (ec >= 0) {
                    continue;
                }
                ep = d.getNod()[npa].getEp()[ty];
                ec = Base.create_ecr(d, ep);
                npb = d.getNod()[npa].getNp()[ty];
                ncb = d.getNod()[npb].getCr();
                if (d.getEdg(ep).getNpa() == npa && d.getEdg(ep).getNpb() == npb) {
                    d.getEcr(ec).setNca(nca);
                    d.getEcr(ec).setNcb(ncb);
                } else if (d.getEdg(ep).getNpb() == npa && d.getEdg(ep).getNpa() == npb) {
                    d.getEcr(ec).setNcb(nca);
                    d.getEcr(ec).setNca(ncb);
                } else {
                    throw new RuntimeException("np inconsistent");
                }
                d.getNcr()[nca].getEc()[ty] = ec;
                if (d.getEdg(ep).getNpa() == npa) {
                    d.getEdg(ep).setCa(ec);
                }
                if (d.getEdg(ep).getNpb() == npa) {
                    d.getEdg(ep).setCb(ec);
                }
                if (!edg_is_glued(ep)) {
                    continue;
                }
                d.getNcr()[ncb].getEc()[ty] = ec;
                if (d.getEdg(ep).getNpa() == npb) {
                    d.getEdg(ep).setCa(ec);
                }
                if (d.getEdg(ep).getNpb() == npb) {
                    d.getEdg(ep).setCb(ec);
                }
            }
        }
        for (ec = 0; ec < d.getEcrs(); ++ec) {
            ep = d.getEcr(ec).getEp();
            epax = d.getEdg(ep).getEpax();
            epay = d.getEdg(ep).getEpay();
            epbx = d.getEdg(ep).getEpbx();
            epby = d.getEdg(ep).getEpby();
            d.getEcr(ec).setEcax(d.getEdg()[epax].getCa());
            d.getEcr(ec).setEcay(d.getEdg()[epay].getCa());
            d.getEcr(ec).setEcbx(d.getEdg()[epbx].getCb());
            d.getEcr(ec).setEcby(d.getEdg()[epby].getCb());
        }
    }

    private void trace_fd() {
        int ot, et;
        int np, ep, op;
        int nc, ec, oc;
        int nb, eb, ob;

        for (op = 0; op < d.getOrbs(); ++op) {
            Base.clear_orb_crds(d, op);
        }
        Base.clear_fcrs(d);
        eb = -1;
        for (ep = 0; ep < d.getEdgs(); ++ep) {
            if (!edg_is_glued(ep)) {
                eb = ep;
                break;
            }
        }
        if (eb < 0) {
            throw new RuntimeException("No unglued edge found");
        }
        np = d.getEdg(ep).getNpa();
        op = d.getEdg(ep).getOpx();
        ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
        et = d.getEdg(ep).getTy();
        nb = np;
        eb = ep;
        ob = op;
        nc = d.getNod(np).getCr();
        if (d.getEdg(ep).getNpa() == np) {
            ec = d.getEdg(ep).getCa();
        } else if (d.getEdg(ep).getNpb() == np) {
            ec = d.getEdg(ep).getCb();
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
                op = d.getNod(np).getOp(ot);
                oc = Base.create_ocr(d, op);
                Base.create_orb_crd(d, op, oc);
                /*			od = d.getNcr()[nc].oc[3-ot-et];	*/
                Base.create_fcr(d, oc);
                Base.create_ocr_ncr(d, oc, nc);
                Base.create_ocr_ecr(d, oc, ec);
                /*			Base.create_ocr_ocr (d, oc, od);	*/
            } else {
                np = d.getNod(np).getNp()[et];
                nc = d.getNod(np).getCr();
                if (ec < 0) {
                    throw new RuntimeException("ec inconsistent");
                }
            }
            if (d.getEdg(ep).getOpx() == op) {
                d.getEcr(ec).setOcx(oc);
            }
            if (d.getEdg(ep).getOpy() == op) {
                d.getEcr(ec).setOcy(oc);
            }
            et = 3 - ot - et;
            ep = d.getNod(np).getEp()[et];
            if (d.getEdg(ep).getNpa() == np) {
                ec = d.getEdg(ep).getCa();
            } else if (d.getEdg(ep).getNpb() == np) {
                ec = d.getEdg(ep).getCb();
            } else {
                throw new RuntimeException("ep inconsistent");
            }
            if (ec < 0) {
                throw new RuntimeException("ec inconsistent");
            }
            if (d.getEdg(ep).getOpx() == op) {
                d.getEcr(ec).setOcx(oc);
            }
            if (d.getEdg(ep).getOpy() == op) {
                d.getEcr(ec).setOcy(oc);
            }
        } while (!(np == nb && ep == eb && op == ob));

        for (op = 0; op < d.getOrbs(); ++op) {
            if (d.getOrb(op).getI() > 1) {
                continue;
            }
            ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
            oc = Base.create_ocr(d, op);
            Base.create_orb_crd(d, op, oc);
            for (int p = 0; p < d.getOrb(op).getNps(); ++p) {
                np = d.getOrb(op).getNp(p);
                nc = d.getNod(np).getCr();
                d.getNcr()[nc].setOc(ot, oc);
                ep = d.getOrb(op).getEp(p);
                et = d.getEdg(ep).getTy();
                if (d.getEdg(ep).getNpa() == np) {
                    ec = d.getEdg(ep).getCa();
                } else if (d.getEdg(ep).getNpb() == np) {
                    ec = d.getEdg(ep).getCb();
                } else {
                    throw new RuntimeException("ep inconsistent");
                }
                if (ec < 0) {
                    throw new RuntimeException("ec inconsistent");
                }
                if (d.getEdg(ep).getOpx() == op) {
                    d.getEcr(ec).setOcx(oc);
                } else if (d.getEdg(ep).getOpy() == op) {
                    d.getEcr(ec).setOcy(oc);
                } else {
                    throw new RuntimeException("ep inconsistent");
                }
                /*			od = d.getNcr()[nc].oc[3-ot-et];	*/
                Base.create_ocr_ncr(d, oc, nc);
                Base.create_ocr_ecr(d, oc, ec);
                /*			Base.create_ocr_ocr (d, oc, od);	*/
            }
        }
        for (ec = 0; ec < d.getEcrs(); ++ec) {
            ep = d.getEcr(ec).getEp();
            op = d.getEdg(ep).getOpx();
            ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
            oc = d.getEcr(ec).getOcx();
            nc = d.getEcr(ec).getNca();
            d.getNcr()[nc].setOc(ot, oc);
            nc = d.getEcr(ec).getNcb();
            d.getNcr()[nc].setOc(ot, oc);
            op = d.getEdg(ep).getOpy();
            ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
            oc = d.getEcr(ec).getOcy();
            nc = d.getEcr(ec).getNca();
            d.getNcr()[nc].setOc(ot, oc);
            nc = d.getEcr(ec).getNcb();
            d.getNcr()[nc].setOc(ot, oc);
        }
        for (nc = 0; nc < d.getNcrs(); ++nc) {
            for (et = 0; et < 3; ++et) {
                ec = d.getNcr()[nc].getEc()[et];
                oc = d.getEcr(ec).getOcx();
                op = d.getOcr(oc).getOp();
                ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
                d.getNcr()[nc].setOc(ot, oc);
                oc = d.getEcr(ec).getOcy();
                op = d.getOcr(oc).getOp();
                ot = 3 - d.getOrb(op).getTi() - d.getOrb(op).getTj();
                d.getNcr()[nc].setOc(ot, oc);
            }
        }
    }


    private boolean nod_is_glued(int np) {
        return d.getNod()[np].getId() > 0;
    }

    private boolean edg_is_glued(int ep) {
        return d.getEdg(ep).getId() > 0;
    }

    private boolean orb_is_glued(int op) {
        return d.getOrb(op).getId() > 0;
    }

    private boolean edg_is_cut(int ep) {
        return d.getEdg(ep).getMk() > 0;
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
        } else if (a3 > b3) {
            return (false);
        }
        return (true);
    }

    private static double inv(int x) {
        return (1.0 / (double) x);
    }
}
