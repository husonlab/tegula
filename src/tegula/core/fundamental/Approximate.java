/*
 * Approximate.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.core.fundamental;

import tegula.core.fundamental.data.Base;
import tegula.core.fundamental.data.DELANEY;
import tegula.core.fundamental.utils.Maths;
import tegula.core.fundamental.utils.Wrap;


/**
 * approximation calculations
 * Created by huson on 3/27/16.
 * Based on del_aprx.h by Klaus Westphal, 1990
 */
public class Approximate {

    public static void compute_radius(DELANEY d) {
        double lr, la, hr, ha;
        int op;
        for (op = 0; op < d.getOrbs(); ++op) {
            d.getOrb(op).setCosa(Math.cos(0.5 * d.getOrb(op).getAlph()));
        }
        /*
        if (d.getCrv() != 0.0) {
        } else {
        }
        */
        if (d.getCrv() < 0.0) {
            lr = Math.PI / d.getImax();
            la = beta_defect(d, lr);
            hr = Math.PI;
            ha = beta_defect(d, hr);
            while (ha <= 0.0) {
                lr = hr;
                la = ha;
                hr += hr;
                ha = beta_defect(d, hr);
            }
            d.setRad(regula_falsi(d, hr, ha, lr, la));
        } else if (d.getCrv() > 0.0) {
            lr = 0.0;
            la = beta_defect(d, lr);
            hr = Math.PI / d.getImin();
            ha = beta_defect(d, hr);
            d.setRad(regula_falsi(d, lr, la, hr, ha));
        } else {
            lr = 0.5;
            la = beta_defect(d, lr);
            hr = 0.5;
            ha = beta_defect(d, hr);
            d.setRad(regula_falsi(d, lr, la, hr, ha));
        }
        for (op = 0; op < d.getOrbs(); ++op) {
            if (d.getOrb(op).getI() <= 2) {
                continue;
            }
            d.getOrb(op).setDist(Math.cos(0.5 * d.getOrb(op).getBeta()));
            if (d.getCrv() < 0.0) {
                d.getOrb(op).setDist(Maths.acoth(Maths.coth(d.getRad()) * d.getOrb(op).getDist()));
            } else if (d.getCrv() > 0.0) {
                d.getOrb(op).setDist(Maths.acot(Maths.cot(d.getRad()) * d.getOrb(op).getDist()));
            } else {
                d.getOrb(op).setDist(d.getRad() / d.getOrb(op).getDist());
            }
        }
    }

    static private double beta_defect(DELANEY d, double rad) {
        double sum = 0.0;
        double cosr;
        int p, oc, op;
        if (d.getCrv() < 0.0) {
            cosr = Math.cosh(rad);
        } else if (d.getCrv() > 0.0) {
            cosr = Math.cos(rad);
        } else {
            cosr = 1.0;
        }
        for (p = 0; p < d.getFcrs(); ++p) {
            oc = d.getFcr(p);
            op = d.getOcr(oc).getOp();
            if (d.getOrb(op).getI() <= 2) {
                continue;
            }
            if (d.getOrb(op).getCosa() >= cosr) {
                d.getOrb(op).setBeta(Math.PI);
            } else {
                d.getOrb(op).setBeta(2.0 * Math.asin(d.getOrb(op).getCosa() / cosr));
            }
            sum += d.getOrb(op).getBeta();
        }
        if (d.getCrv() < 0.0) {
            return (Maths.m_2pi - sum);
        } else if (d.getCrv() > 0.0) {
            return (sum - Maths.m_2pi);
        } else {
            return (0.0);
        }
    }

    static private double regula_falsi(DELANEY d, double lr, double la, double hr, double ha) {
        int count = 0;
        double olr, ola;    /*	    lower value		*/
        double ohr, oha;    /*	   higher value		*/
        double dr, da;        /*	    value range		*/
        double mr, ma;        /*	     mean value		*/
        double cr, ca;        /*	estimated value		*/
        double buffer;
        dr = hr - lr;
        da = ha - la;
        if (la == 0.0) {
            return (lr);
        }
        if (ha == 0.0) {
            return (hr);
        }
        do {
            cr = mr = 0.5 * (hr + lr);
            ca = ma = beta_defect(d, mr);
            if (da != 0.0) {
                cr = Math.max(lr - dr / da * la, 0.0);
                ca = beta_defect(d, cr);
            }
            if (ma > ca) {
                buffer = mr;
                mr = cr;
                cr = buffer;
                buffer = ma;
                ma = ca;
                ca = buffer;
            }
            olr = lr;
            ola = la;
            ohr = hr;
            oha = ha;
            if (ca < 0.0) {
                lr = cr;
                la = ca;
            } else if (ma < 0.0) {
                lr = mr;
                la = ma;
            }
            if (ma > 0.0) {
                hr = mr;
                ha = ma;
            } else if (ca > 0.0) {
                hr = cr;
                ha = ca;
            }
            dr = hr - lr;
            da = ha - la;
        }
        while (++count < 100 && !(lr == olr && la == ola && hr == ohr && ha == oha));
        return (0.5 * (hr + lr));
    }

    static public void compute_coords(DELANEY d) {
        int s;
        int b, cb;
        int p;
        int e, ce, oe;
        int ec;
        int op;
        int cp = 0, ocx, ocy;
        int k, n;
        double sum;
        boolean is_circle;
        final Wrap<Double> Cen_Re = new Wrap<>(0.0);
        final Wrap<Double> Cen_Im = new Wrap<>(0.0);
        final Wrap<Double> Cen_R = new Wrap<>(0.0);
        final Wrap<Double> Ang_B = new Wrap<>(0.0);
        final Wrap<Double> Ang_E = new Wrap<>(0.0);
        final Wrap<Double> Ang_D = new Wrap<>(0.0);
        final Wrap<Double> Ang_P = new Wrap<>(0.0);

        double Del_Re, Del_Im;

        sum = Maths.M_PI_2;
        for (p = 0; p < d.getFcrs(); ++p) {
            cp = d.getFcr(p);
            op = d.getOcr(cp).getOp();
            if (d.getCrv() < 0.0) {
                d.getOcr(cp).setDist(Math.tanh(0.5 * d.getOrb(op).getDist()));
            } else if (d.getCrv() > 0.0) {
                d.getOcr(cp).setDist(Math.tan(0.5 * d.getOrb(op).getDist()));
            } else {
                d.getOcr(cp).setDist(d.getOrb(op).getDist());
            }
            if (d.getOrb(op).getI() > 2) {
                sum += 0.5 * d.getOrb(op).getBeta();
                if (sum > Maths.m_2pi) {
                    sum -= Maths.m_2pi;
                }
                d.getOcr(cp).setBeta(sum);
                {
                    final double[] pair = Maths.Compute_Cart_2(d.getOcr(cp).getDist(), d.getOcr(cp).getBeta());
                    d.getOcr(cp).setPosx(pair[0]);
                    d.getOcr(cp).setPosy(pair[1]);
                }

                sum += 0.5 * d.getOrb(op).getBeta();
                if (sum > Maths.m_2pi) {
                    sum -= Maths.m_2pi;
                }
            } else {
                d.getOcr(cp).setBeta(sum);
            }
        }
        sum -= Maths.M_PI_2;
        for (s = 0; s < d.getFcrs(); ++s) {
            cp = d.getFcr(s);
            op = d.getOcr(cp).getOp();
            if (d.getOrb(op).getI() > 2) {
                break;
            }
        }
        if (s >= d.getFcrs()) {
            if (d.getCrv() <= 0)
                throw new RuntimeException("compute_coords: Couldn't find vertex");
            else {
                Ang_D.set((2.0 * Math.PI) / d.getFcrs());
                Ang_P.set(0.0);

                d.setMinx(0);
                d.setMaxx(0);
                d.setMiny(0);
                d.setMaxy(0);

                for (p = 0; p < d.getFcrs(); p++) {
                    cp = d.getFcr(p);
                    op = d.getOcr(cp).getOp();
                    d.getOcr(cp).setBeta(Ang_P.get());
                    d.getOcr(cp).setDist(1.0);
                    {
                        double[] coords = Maths.Compute_Cart_2(d.getOcr(cp).getDist(), d.getOcr(cp).getBeta());
                        d.getOcr(cp).setPosx(coords[0]);
                        d.getOcr(cp).setPosy(coords[1]);
                    }
                    if (d.getOcr(cp).getPosx() < d.getMinx()) {
                        d.setMinx(d.getOcr(cp).getPosx());
                    }
                    if (d.getOcr(cp).getPosy() < d.getMiny()) {
                        d.setMiny(d.getOcr(cp).getPosy());
                    }
                    if (d.getOcr(cp).getPosx() > d.getMaxx()) {
                        d.setMaxx(d.getOcr(cp).getPosx());
                    }
                    if (d.getOcr(cp).getPosy() > d.getMaxy()) {
                        d.setMaxy(d.getOcr(cp).getPosy());
                    }
                    Ang_P.set(Ang_P.get() + Ang_D.get());
                }
                for (ec = 0; ec < d.getEcrs(); ++ec) {
                    ocx = d.getEcr()[ec].getOcx();
                    ocy = d.getEcr()[ec].getOcy();
                    Base.check_ocr(d, ocx);
                    Base.check_ocr(d, ocy);
                    is_circle = SphericalGeometry.Compute_Sph_Line(d.getOcr()[ocx].getDist(), d.getOcr()[ocx].getBeta(),
                            /* 1.0,d.getOcr()[ocy].getBeta(),*/
                            d.getOcr()[ocy].getDist(), d.getOcr()[ocy].getBeta(), Math.tan(0.5 * d.getRad()),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOcr()[ocx].accessPosx(), d.getOcr()[ocx].accessPosy(),
                            d.getOcr()[ocy].accessPosx(), d.getOcr()[ocy].accessPosy());
                    Cen_R.set(1.0);
                    if (is_circle) {
                        Ang_P.set(0.5 * (Ang_E.get() + Ang_B.get()));
                        d.getEcr()[ec].setDist(Cen_R.get());
                        d.getEcr()[ec].setBeta(Ang_P.get());
                        {
                            double[] coords = Maths.Compute_Cart_2(d.getEcr()[ec].getDist(), d.getEcr()[ec].getBeta());
                            d.getEcr()[ec].setPosx(coords[0] + Cen_Re.get());
                            d.getEcr()[ec].setPosy(coords[1] + Cen_Im.get());
                        }
                    } else {
                        d.getEcr()[ec].setPosx(0.5 * (d.getOcr()[ocy].getPosx() + d.getOcr()[ocx].getPosx()));
                        d.getEcr()[ec].setPosy(0.5 * (d.getOcr()[ocy].getPosy() + d.getOcr()[ocx].getPosy()));
                        {
                            double[] coords = Maths.Compute_Polar_2(d.getEcr()[ec].getPosx(), d.getEcr()[ec].getPosy());
                            d.getEcr()[ec].setDist(coords[0]);
                            d.getEcr()[ec].setBeta(coords[1]);
                        }
                    }
                    if (d.getEcr()[ec].getPosx() < d.getMinx()) {
                        d.setMinx(d.getEcr()[ec].getPosx());
                    }
                    if (d.getEcr()[ec].getPosy() < d.getMiny()) {
                        d.setMiny(d.getEcr()[ec].getPosy());
                    }
                    if (d.getEcr()[ec].getPosx() > d.getMaxx()) {
                        d.setMaxx(d.getEcr()[ec].getPosx());
                    }
                    if (d.getEcr()[ec].getPosy() > d.getMaxy()) {
                        d.setMaxy(d.getEcr()[ec].getPosy());
                    }
                }
                return;
            }
        }

        e = s;
        ce = d.getFcr()[e];
        oe = d.getOcr()[ce].getOp();
        n = 0;
        b = e;
        cb = ce;
        ++n;
        if (e < d.getFcrs() - 1) {
            ++e;
        } else {
            e = 0;
        }
        ce = d.getFcr()[e];
        oe = d.getOcr()[ce].getOp();

        d.setMinx(0);
        d.setMaxx(0);
        d.setMiny(0);
        d.setMaxy(0);

        while (true) {
            if (d.getOrb()[oe].getI() > 2) {
                k = 0;
                p = b;
                ++k;
                if (p < d.getFcrs() - 1) {
                    ++p;
                } else {
                    p = 0;
                }
                cp = d.getFcr()[p];
                op = d.getOcr(cp).getOp();
                if (d.getCrv() < 0.0) {
                    is_circle = HyperbolicGeometry.Compute_Hyp_Line(d.getOcr()[cb].getDist(), d.getOcr()[cb].getBeta(),
                            d.getOcr()[ce].getDist(), d.getOcr()[ce].getBeta(),
                            Math.tanh(0.5 * d.getRad()),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOcr()[cb].accessPosx(), d.getOcr()[cb].accessPosy(),
                            d.getOcr()[ce].accessPosx(), d.getOcr()[ce].accessPosy());
                    if (Ang_E.get() > Ang_B.get()) {
                        Ang_E.set(Ang_E.get() - Maths.m_2pi);
                    }
                } else if (d.getCrv() > 0.0) {
                    is_circle = SphericalGeometry.Compute_Sph_Line(d.getOcr()[cb].getDist(), d.getOcr()[cb].getBeta(),
                            d.getOcr()[ce].getDist(), d.getOcr()[ce].getBeta(),
                            Math.tan(0.5 * d.getRad()),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOcr()[cb].accessPosx(), d.getOcr()[cb].accessPosy(),
                            d.getOcr()[ce].accessPosx(), d.getOcr()[ce].accessPosy());
                    if (Ang_E.get() < Ang_B.get()) {
                        Ang_E.set(Ang_E.get() + Maths.m_2pi);
                    }
                } else {
                    is_circle = ParabolicGeometry.Compute_Par_Line(d.getOcr()[cb].getDist(), d.getOcr()[cb].getBeta(),
                            d.getOcr()[ce].getDist(), d.getOcr()[ce].getBeta(),
                            d.getRad(),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOcr()[cb].accessPosx(), d.getOcr()[cb].accessPosy(),
                            d.getOcr()[ce].accessPosx(), d.getOcr()[ce].accessPosy());
                    if (Ang_E.get() < Ang_B.get()) {
                        Ang_E.set(Ang_E.get() + Maths.m_2pi);
                    }
                }
                if (is_circle) {
                    Ang_D.set((Ang_E.get() - Ang_B.get()) / (double) n);
                    while (p != e) {
                        Ang_P.set(Ang_B.get() + (double) k * Ang_D.get());
                        {
                            double[] coords = Maths.Compute_Cart_2(Cen_R.get(), Ang_P.get());
                            d.getOcr(cp).setPosx(coords[0] + Cen_Re.get());
                            d.getOcr(cp).setPosy(coords[1] + Cen_Im.get());
                        }
                        {
                            double[] coords = Maths.Compute_Polar_2(d.getOcr(cp).getPosx(), d.getOcr(cp).getPosy());
                            d.getOcr(cp).setDist(coords[0]);
                            d.getOcr(cp).setBeta(coords[1]);
                        }
                        ++k;
                        if (p < d.getFcrs() - 1) {
                            ++p;
                        } else {
                            p = 0;
                        }
                        cp = d.getFcr()[p];
                        op = d.getOcr(cp).getOp();
                    }
                } else {
                    Del_Re = (d.getOcr()[ce].getPosx() - d.getOcr()[cb].getPosx()) / (double) n;
                    Del_Im = (d.getOcr()[ce].getPosy() - d.getOcr()[cb].getPosy()) / (double) n;
                    while (p != e) {
                        d.getOcr(cp).setPosx(d.getOcr()[cb].getPosx() + (double) k * Del_Re);
                        d.getOcr(cp).setPosy(d.getOcr()[cb].getPosy() + (double) k * Del_Im);
                        {
                            double[] coords = Maths.Compute_Polar_2(d.getOcr(cp).getPosx(), d.getOcr(cp).getPosy());
                            d.getOcr(cp).setDist(coords[0]);
                            d.getOcr(cp).setBeta(coords[1]);
                        }
                        ++k;
                        if (p < d.getFcrs() - 1) {
                            ++p;
                        } else {
                            p = 0;
                        }
                        cp = d.getFcr()[p];
                        op = d.getOcr(cp).getOp();
                    }
                }
                n = 0;
                b = e;
                cb = ce;
                if (b == s) {
                    break;
                }
            }
            ++n;
            if (d.getOcr(cp).getPosx() < d.getMinx()) {
                d.setMinx(d.getOcr(cp).getPosx());
            }
            if (d.getOcr(cp).getPosy() < d.getMiny()) {
                d.setMiny(d.getOcr(cp).getPosy());
            }
            if (d.getOcr(cp).getPosx() > d.getMaxx()) {
                d.setMaxx(d.getOcr(cp).getPosx());
            }
            if (d.getOcr(cp).getPosy() > d.getMaxy()) {
                d.setMaxy(d.getOcr(cp).getPosy());
            }
            if (e < d.getFcrs() - 1) {
                ++e;
            } else {
                e = 0;
            }
            ce = d.getFcr()[e];
            oe = d.getOcr()[ce].getOp();
        }
        for (ec = 0; ec < d.getEcrs(); ++ec) {
            ocx = d.getEcr()[ec].getOcx();
            ocy = d.getEcr()[ec].getOcy();
            Base.check_ocr(d, ocx);
            Base.check_ocr(d, ocy);
            if (d.getCrv() < 0.0) {
                is_circle = HyperbolicGeometry.Compute_Hyp_Line(d.getOcr()[ocx].getDist(), d.getOcr()[ocx].getBeta(),
                        d.getOcr()[ocy].getDist(), d.getOcr()[ocy].getBeta(),
                        Math.tanh(0.5 * d.getRad()),
                        false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                        d.getOcr()[ocx].accessPosx(), d.getOcr()[ocx].accessPosy(),
                        d.getOcr()[ocy].accessPosx(), d.getOcr()[ocy].accessPosy());
            } else if (d.getCrv() > 0.0) {
                is_circle = SphericalGeometry.Compute_Sph_Line(d.getOcr()[ocx].getDist(), d.getOcr()[ocx].getBeta(),
                        d.getOcr()[ocy].getDist(), d.getOcr()[ocy].getBeta(),
                        Math.tan(0.5 * d.getRad()),
                        false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                        d.getOcr()[ocx].accessPosx(), d.getOcr()[ocx].accessPosy(),
                        d.getOcr()[ocy].accessPosx(), d.getOcr()[ocy].accessPosy());
            } else {
                is_circle = ParabolicGeometry.Compute_Par_Line(d.getOcr()[ocx].getDist(), d.getOcr()[ocx].getBeta(),
                        d.getOcr()[ocy].getDist(), d.getOcr()[ocy].getBeta(),
                        d.getRad(),
                        false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                        d.getOcr()[ocx].accessPosx(), d.getOcr()[ocx].accessPosy(),
                        d.getOcr()[ocy].accessPosx(), d.getOcr()[ocy].accessPosy());
            }
            if (is_circle) {
                Ang_P.set(0.5 * (Ang_E.get() + Ang_B.get()));
                d.getEcr()[ec].setDist(Cen_R.get());
                d.getEcr()[ec].setBeta(Ang_P.get());
                {
                    double[] coords = Maths.Compute_Cart_2(d.getEcr()[ec].getDist(), d.getEcr()[ec].getBeta());
                    d.getEcr()[ec].setPosx(coords[0] + Cen_Re.get());
                    d.getEcr()[ec].setPosy(coords[1] + Cen_Im.get());
                }
            } else {
                d.getEcr()[ec].setPosx(0.5 * (d.getOcr()[ocy].getPosx() + d.getOcr()[ocx].getPosx()));
                d.getEcr()[ec].setPosy(0.5 * (d.getOcr()[ocy].getPosy() + d.getOcr()[ocx].getPosy()));
                {
                    double[] coords = Maths.Compute_Polar_2(d.getEcr()[ec].getPosx(), d.getEcr()[ec].getPosy());
                    d.getEcr()[ec].setDist(coords[0]);
                    d.getEcr()[ec].setBeta(coords[1]);
                }
            }
            if (d.getEcr()[ec].getPosx() < d.getMinx()) {
                d.setMinx(d.getEcr()[ec].getPosx());
            }
            if (d.getEcr()[ec].getPosy() < d.getMiny()) {
                d.setMiny(d.getEcr()[ec].getPosy());
            }
            if (d.getEcr()[ec].getPosx() > d.getMaxx()) {
                d.setMaxx(d.getEcr()[ec].getPosx());
            }
            if (d.getEcr()[ec].getPosy() > d.getMaxy()) {
                d.setMaxy(d.getEcr()[ec].getPosy());
            }
        }
    }

    static public boolean relax_coords(DELANEY d) {
        boolean nod_changes = relax_nods(d);
        boolean edg_changes = relax_edgs(d);
        boolean orb_changes = relax_orbs(d);
        boolean any_changes = nod_changes || edg_changes || orb_changes;
        return (any_changes);
    }

    static private boolean relax_nods(DELANEY d) {
        boolean nod_changes;
        int nc, ty;
        int ec;
        int oc;
        double mx, my, fac;
        nod_changes = false;
        for (nc = 0; nc < d.getNcrs(); ++nc) {
            mx = 0.0;
            my = 0.0;
            fac = 0.0;
            for (ty = 0; ty < 3; ++ty) {
                ec = d.getNcr()[nc].getEc(ty);
                mx += d.getEcr()[ec].getPosx();
                my += d.getEcr()[ec].getPosy();
                fac += 1.0;
                oc = d.getNcr()[nc].getOc(ty);
                mx += d.getOcr(oc).getPosx();
                my += d.getOcr(oc).getPosy();
                fac += 1.0;
            }
            if (fac == 0.0) {
                continue;
            }
            mx /= fac;
            my /= fac;
            if (Math.abs(mx - d.getNcr()[nc].getPosx()) >= Maths.EPSILON ||
                    Math.abs(my - d.getNcr()[nc].getPosy()) >= Maths.EPSILON) {
                nod_changes = true;
                d.getNcr()[nc].setPosx(mx);
                d.getNcr()[nc].setPosy(my);
            }
        }
        return (nod_changes);
    }

    static public boolean relax_edgs(DELANEY d) {
        boolean edg_changes;
        int ec, ep;
        int nca, ncb;
        int ocx, ocy;
        double mx, my, fac;
        edg_changes = false;
        for (ec = 0; ec < d.getEcrs(); ++ec) {
            ep = d.getEcr()[ec].getEp();
            if (d.getEdg()[ep].getId() <= 0) {
                continue;
            }
            mx = 0.0;
            my = 0.0;
            fac = 0.0;
            ocx = d.getEcr()[ec].getOcx();
            mx += d.getOcr()[ocx].getPosx();
            my += d.getOcr()[ocx].getPosy();
            fac += 1.0;
            ocy = d.getEcr()[ec].getOcy();
            mx += d.getOcr()[ocy].getPosx();
            my += d.getOcr()[ocy].getPosy();
            fac += 1.0;
            nca = d.getEcr()[ec].getNca();
            mx += d.getNcr()[nca].getPosx();
            my += d.getNcr()[nca].getPosy();
            fac += 1.0;
            ncb = d.getEcr()[ec].getNcb();
            mx += d.getNcr()[ncb].getPosx();
            my += d.getNcr()[ncb].getPosy();
            fac += 1.0;
            if (fac == 0.0) {
                continue;
            }
            mx /= fac;
            my /= fac;
            if (Math.abs(mx - d.getEcr()[ec].getPosx()) >= Maths.EPSILON ||
                    Math.abs(my - d.getEcr()[ec].getPosy()) >= Maths.EPSILON) {
                edg_changes = true;
                d.getEcr()[ec].setPosx(mx);
                d.getEcr()[ec].setPosy(my);
            }
        }
        return (edg_changes);
    }

    static private boolean relax_orbs(DELANEY d) {
        boolean orb_changes = false;
        int oc, op;
        int nc;
        int ec;
        int p;
        double mx, my, fac;

        for (oc = 0; oc < d.getOcrs(); ++oc) {
            op = d.getOcr(oc).getOp();
            if (d.getOrb(op).getI() > 1) {
                continue;
            }
            mx = 0.0;
            my = 0.0;
            fac = 0.0;
            for (p = 0; p < d.getOcr(oc).getEcs(); ++p) {
                nc = d.getOcr(oc).getNc(p);
                mx += d.getNcr()[nc].getPosx();
                my += d.getNcr()[nc].getPosy();
                fac += 1.0;
                ec = d.getOcr(oc).getEc(p);
                mx += d.getEcr()[ec].getPosx();
                my += d.getEcr()[ec].getPosy();
                fac += 1.0;
            }
            if (fac == 0.0) {
                continue;
            }
            mx /= fac;
            my /= fac;
            if (Math.abs(mx - d.getOcr(oc).getPosx()) >= Maths.EPSILON ||
                    Math.abs(my - d.getOcr(oc).getPosy()) >= Maths.EPSILON) {
                orb_changes = true;
                d.getOcr(oc).setPosx(mx);
                d.getOcr(oc).setPosy(my);
            }
        }
        return (orb_changes);
    }

    static public void compute_dist(DELANEY d) {
        int oc, op;
        int ec;
        int nc;
        for (oc = 0; oc < d.getOcrs(); ++oc) {
            double[] coords = Maths.Compute_Polar_2(d.getOcr(oc).getPosx(), d.getOcr(oc).getPosy());
            d.getOcr(oc).setDist(coords[0]);
            d.getOcr(oc).setBeta(coords[1]);

        }
        for (ec = 0; ec < d.getEcrs(); ++ec) {
            double[] coords = Maths.Compute_Polar_2(d.getEcr()[ec].getPosx(), d.getEcr()[ec].getPosy());
            d.getEcr()[ec].setDist(coords[0]);
            d.getEcr()[ec].setBeta(coords[1]);
        }
        for (nc = 0; nc < d.getNcrs(); ++nc) {
            double[] coords = Maths.Compute_Polar_2(d.getNcr()[nc].getPosx(), d.getNcr()[nc].getPosy());
            d.getNcr()[nc].setDist(coords[0]);
            d.getNcr()[nc].setBeta(coords[1]);
        }
        for (oc = 0; oc < d.getOcrs(); ++oc) {
            op = d.getOcr(oc).getOp();
            if (d.getOrb(op).getI() > 1) {
                continue;
            }
            if (d.getCrv() < 0.0) {
                d.getOrb(op).setDist(2.0 * Maths.atanh(d.getOcr(oc).getDist()));
            } else if (d.getCrv() > 0.0) {
                d.getOrb(op).setDist(2.0 * Math.atan(d.getOcr(oc).getDist()));
            } else {
                d.getOrb(op).setDist(d.getOcr(oc).getDist());
            }
        }
    }
}
