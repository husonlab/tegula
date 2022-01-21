/*
 * Approximate.java Copyright (C) 2022 Daniel H. Huson
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
import tegula.core.funtiles.utils.Maths;
import tegula.core.funtiles.utils.Wrap;


/**
 * approximation calculations
 * Based on del_aprx.h by Klaus Westphal, 1990
 */
public class Approximate {

    public static void compute_radius(DelaneySymbolWithGeometry d) {
        double lr, la, hr, ha;
        int op;
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
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
        for (op = 0; op < d.getNumberOfOrbits(); ++op) {
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

    static private double beta_defect(DelaneySymbolWithGeometry d, double rad) {
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
        for (p = 0; p < d.getNumberOfBorderCoordinates(); ++p) {
            oc = d.getBorderCoordinates(p);
            op = d.getOrbitCoordinates(oc).getOp();
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

    static private double regula_falsi(DelaneySymbolWithGeometry d, double lr, double la, double hr, double ha) {
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
        while (++count < 1000 && !(lr == olr && la == ola && hr == ohr && ha == oha));
        return (0.5 * (hr + lr));
    }

    static public void compute_coords(DelaneySymbolWithGeometry d) {
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
        for (p = 0; p < d.getNumberOfBorderCoordinates(); ++p) {
            cp = d.getBorderCoordinates(p);
            op = d.getOrbitCoordinates(cp).getOp();
            if (d.getCrv() < 0.0) {
                d.getOrbitCoordinates(cp).setDist(Math.tanh(0.5 * d.getOrb(op).getDist()));
            } else if (d.getCrv() > 0.0) {
                d.getOrbitCoordinates(cp).setDist(Math.tan(0.5 * d.getOrb(op).getDist()));
            } else {
                d.getOrbitCoordinates(cp).setDist(d.getOrb(op).getDist());
            }
            if (d.getOrb(op).getI() > 2) {
                sum += 0.5 * d.getOrb(op).getBeta();
                if (sum > Maths.m_2pi) {
                    sum -= Maths.m_2pi;
                }
                d.getOrbitCoordinates(cp).setBeta(sum);
                {
                    final double[] pair = Maths.Compute_Cart_2(d.getOrbitCoordinates(cp).getDist(), d.getOrbitCoordinates(cp).getBeta());
                    d.getOrbitCoordinates(cp).setPosx(pair[0]);
                    d.getOrbitCoordinates(cp).setPosy(pair[1]);
                }

                sum += 0.5 * d.getOrb(op).getBeta();
                if (sum > Maths.m_2pi) {
                    sum -= Maths.m_2pi;
                }
            } else {
                d.getOrbitCoordinates(cp).setBeta(sum);
            }
        }
        for (s = 0; s < d.getNumberOfBorderCoordinates(); ++s) {
            cp = d.getBorderCoordinates(s);
            op = d.getOrbitCoordinates(cp).getOp();
            if (d.getOrb(op).getI() > 2) {
                break;
            }
        }
        if (s >= d.getNumberOfBorderCoordinates()) {
            if (d.getCrv() <= 0)
                throw new RuntimeException("compute_coords: Couldn't find vertex");
            else {
                Ang_D.set((2.0 * Math.PI) / d.getNumberOfBorderCoordinates());
                Ang_P.set(0.0);

                d.setMinx(0);
                d.setMaxx(0);
                d.setMiny(0);
                d.setMaxy(0);

                for (p = 0; p < d.getNumberOfBorderCoordinates(); p++) {
                    cp = d.getBorderCoordinates(p);
                    op = d.getOrbitCoordinates(cp).getOp();
                    d.getOrbitCoordinates(cp).setBeta(Ang_P.get());
                    d.getOrbitCoordinates(cp).setDist(1.0);
                    {
                        double[] coords = Maths.Compute_Cart_2(d.getOrbitCoordinates(cp).getDist(), d.getOrbitCoordinates(cp).getBeta());
                        d.getOrbitCoordinates(cp).setPosx(coords[0]);
                        d.getOrbitCoordinates(cp).setPosy(coords[1]);
                    }
                    if (d.getOrbitCoordinates(cp).getPosx() < d.getMinx()) {
                        d.setMinx(d.getOrbitCoordinates(cp).getPosx());
                    }
                    if (d.getOrbitCoordinates(cp).getPosy() < d.getMiny()) {
                        d.setMiny(d.getOrbitCoordinates(cp).getPosy());
                    }
                    if (d.getOrbitCoordinates(cp).getPosx() > d.getMaxx()) {
                        d.setMaxx(d.getOrbitCoordinates(cp).getPosx());
                    }
                    if (d.getOrbitCoordinates(cp).getPosy() > d.getMaxy()) {
                        d.setMaxy(d.getOrbitCoordinates(cp).getPosy());
                    }
                    Ang_P.set(Ang_P.get() + Ang_D.get());
                }
                for (ec = 0; ec < d.getNumberOfEdgeCoordinates(); ++ec) {
                    ocx = d.getEdgeCoordinates()[ec].getOcx();
                    ocy = d.getEdgeCoordinates()[ec].getOcy();
                    Base.check_ocr(d, ocx);
                    Base.check_ocr(d, ocy);
                    is_circle = SphericalGeometry.Compute_Sph_Line(d.getOrbitCoordinates()[ocx].getDist(), d.getOrbitCoordinates()[ocx].getBeta(),
                            /* 1.0,d.getOrbitCoordinates()[ocy].getBeta(),*/
                            d.getOrbitCoordinates()[ocy].getDist(), d.getOrbitCoordinates()[ocy].getBeta(), Math.tan(0.5 * d.getRad()),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOrbitCoordinates()[ocx].accessPosx(), d.getOrbitCoordinates()[ocx].accessPosy(),
                            d.getOrbitCoordinates()[ocy].accessPosx(), d.getOrbitCoordinates()[ocy].accessPosy());
                    Cen_R.set(1.0);
                    if (is_circle) {
                        Ang_P.set(0.5 * (Ang_E.get() + Ang_B.get()));
                        d.getEdgeCoordinates()[ec].setDist(Cen_R.get());
                        d.getEdgeCoordinates()[ec].setBeta(Ang_P.get());
                        {
                            double[] coords = Maths.Compute_Cart_2(d.getEdgeCoordinates()[ec].getDist(), d.getEdgeCoordinates()[ec].getBeta());
                            d.getEdgeCoordinates()[ec].setPosx(coords[0] + Cen_Re.get());
                            d.getEdgeCoordinates()[ec].setPosy(coords[1] + Cen_Im.get());
                        }
                    } else {
                        d.getEdgeCoordinates()[ec].setPosx(0.5 * (d.getOrbitCoordinates()[ocy].getPosx() + d.getOrbitCoordinates()[ocx].getPosx()));
                        d.getEdgeCoordinates()[ec].setPosy(0.5 * (d.getOrbitCoordinates()[ocy].getPosy() + d.getOrbitCoordinates()[ocx].getPosy()));
                        {
                            double[] coords = Maths.Compute_Polar_2(d.getEdgeCoordinates()[ec].getPosx(), d.getEdgeCoordinates()[ec].getPosy());
                            d.getEdgeCoordinates()[ec].setDist(coords[0]);
                            d.getEdgeCoordinates()[ec].setBeta(coords[1]);
                        }
                    }
                    if (d.getEdgeCoordinates()[ec].getPosx() < d.getMinx()) {
                        d.setMinx(d.getEdgeCoordinates()[ec].getPosx());
                    }
                    if (d.getEdgeCoordinates()[ec].getPosy() < d.getMiny()) {
                        d.setMiny(d.getEdgeCoordinates()[ec].getPosy());
                    }
                    if (d.getEdgeCoordinates()[ec].getPosx() > d.getMaxx()) {
                        d.setMaxx(d.getEdgeCoordinates()[ec].getPosx());
                    }
                    if (d.getEdgeCoordinates()[ec].getPosy() > d.getMaxy()) {
                        d.setMaxy(d.getEdgeCoordinates()[ec].getPosy());
                    }
                }
                return;
            }
        }

        e = s;
        ce = d.getBorderCoordinates()[e];
        oe = d.getOrbitCoordinates()[ce].getOp();
        n = 0;
        b = e;
        cb = ce;
        ++n;
        if (e < d.getNumberOfBorderCoordinates() - 1) {
            ++e;
        } else {
            e = 0;
        }
        ce = d.getBorderCoordinates()[e];
        oe = d.getOrbitCoordinates()[ce].getOp();

        d.setMinx(0);
        d.setMaxx(0);
        d.setMiny(0);
        d.setMaxy(0);

        while (true) {
            if (d.getOrbits()[oe].getI() > 2) {
                k = 0;
                p = b;
                ++k;
                if (p < d.getNumberOfBorderCoordinates() - 1) {
                    ++p;
                } else {
                    p = 0;
                }
                cp = d.getBorderCoordinates()[p];
                op = d.getOrbitCoordinates(cp).getOp();
                if (d.getCrv() < 0.0) {
                    is_circle = HyperbolicGeometry.Compute_Hyp_Line(d.getOrbitCoordinates()[cb].getDist(), d.getOrbitCoordinates()[cb].getBeta(),
                            d.getOrbitCoordinates()[ce].getDist(), d.getOrbitCoordinates()[ce].getBeta(),
                            Math.tanh(0.5 * d.getRad()),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOrbitCoordinates()[cb].accessPosx(), d.getOrbitCoordinates()[cb].accessPosy(),
                            d.getOrbitCoordinates()[ce].accessPosx(), d.getOrbitCoordinates()[ce].accessPosy());
                    if (Ang_E.get() > Ang_B.get()) {
                        Ang_E.set(Ang_E.get() - Maths.m_2pi);
                    }
                } else if (d.getCrv() > 0.0) {
                    is_circle = SphericalGeometry.Compute_Sph_Line(d.getOrbitCoordinates()[cb].getDist(), d.getOrbitCoordinates()[cb].getBeta(),
                            d.getOrbitCoordinates()[ce].getDist(), d.getOrbitCoordinates()[ce].getBeta(),
                            Math.tan(0.5 * d.getRad()),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOrbitCoordinates()[cb].accessPosx(), d.getOrbitCoordinates()[cb].accessPosy(),
                            d.getOrbitCoordinates()[ce].accessPosx(), d.getOrbitCoordinates()[ce].accessPosy());
                    if (Ang_E.get() < Ang_B.get()) {
                        Ang_E.set(Ang_E.get() + Maths.m_2pi);
                    }
                } else {
                    is_circle = ParabolicGeometry.Compute_Par_Line(d.getOrbitCoordinates()[cb].getDist(), d.getOrbitCoordinates()[cb].getBeta(),
                            d.getOrbitCoordinates()[ce].getDist(), d.getOrbitCoordinates()[ce].getBeta(),
                            d.getRad(),
                            false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                            d.getOrbitCoordinates()[cb].accessPosx(), d.getOrbitCoordinates()[cb].accessPosy(),
                            d.getOrbitCoordinates()[ce].accessPosx(), d.getOrbitCoordinates()[ce].accessPosy());
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
                            d.getOrbitCoordinates(cp).setPosx(coords[0] + Cen_Re.get());
                            d.getOrbitCoordinates(cp).setPosy(coords[1] + Cen_Im.get());
                        }
                        {
                            double[] coords = Maths.Compute_Polar_2(d.getOrbitCoordinates(cp).getPosx(), d.getOrbitCoordinates(cp).getPosy());
                            d.getOrbitCoordinates(cp).setDist(coords[0]);
                            d.getOrbitCoordinates(cp).setBeta(coords[1]);
                        }
                        ++k;
                        if (p < d.getNumberOfBorderCoordinates() - 1) {
                            ++p;
                        } else {
                            p = 0;
                        }
                        cp = d.getBorderCoordinates()[p];
                        op = d.getOrbitCoordinates(cp).getOp();
                    }
                } else {
                    Del_Re = (d.getOrbitCoordinates()[ce].getPosx() - d.getOrbitCoordinates()[cb].getPosx()) / (double) n;
                    Del_Im = (d.getOrbitCoordinates()[ce].getPosy() - d.getOrbitCoordinates()[cb].getPosy()) / (double) n;
                    while (p != e) {
                        d.getOrbitCoordinates(cp).setPosx(d.getOrbitCoordinates()[cb].getPosx() + (double) k * Del_Re);
                        d.getOrbitCoordinates(cp).setPosy(d.getOrbitCoordinates()[cb].getPosy() + (double) k * Del_Im);
                        {
                            double[] coords = Maths.Compute_Polar_2(d.getOrbitCoordinates(cp).getPosx(), d.getOrbitCoordinates(cp).getPosy());
                            d.getOrbitCoordinates(cp).setDist(coords[0]);
                            d.getOrbitCoordinates(cp).setBeta(coords[1]);
                        }
                        ++k;
                        if (p < d.getNumberOfBorderCoordinates() - 1) {
                            ++p;
                        } else {
                            p = 0;
                        }
                        cp = d.getBorderCoordinates()[p];
                        op = d.getOrbitCoordinates(cp).getOp();
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
            if (d.getOrbitCoordinates(cp).getPosx() < d.getMinx()) {
                d.setMinx(d.getOrbitCoordinates(cp).getPosx());
            }
            if (d.getOrbitCoordinates(cp).getPosy() < d.getMiny()) {
                d.setMiny(d.getOrbitCoordinates(cp).getPosy());
            }
            if (d.getOrbitCoordinates(cp).getPosx() > d.getMaxx()) {
                d.setMaxx(d.getOrbitCoordinates(cp).getPosx());
            }
            if (d.getOrbitCoordinates(cp).getPosy() > d.getMaxy()) {
                d.setMaxy(d.getOrbitCoordinates(cp).getPosy());
            }
            if (e < d.getNumberOfBorderCoordinates() - 1) {
                ++e;
            } else {
                e = 0;
            }
            ce = d.getBorderCoordinates()[e];
            oe = d.getOrbitCoordinates()[ce].getOp();
        }
        for (ec = 0; ec < d.getNumberOfEdgeCoordinates(); ++ec) {
            ocx = d.getEdgeCoordinates()[ec].getOcx();
            ocy = d.getEdgeCoordinates()[ec].getOcy();
            Base.check_ocr(d, ocx);
            Base.check_ocr(d, ocy);
            if (d.getCrv() < 0.0) {
                is_circle = HyperbolicGeometry.Compute_Hyp_Line(d.getOrbitCoordinates()[ocx].getDist(), d.getOrbitCoordinates()[ocx].getBeta(),
                        d.getOrbitCoordinates()[ocy].getDist(), d.getOrbitCoordinates()[ocy].getBeta(),
                        Math.tanh(0.5 * d.getRad()),
                        false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                        d.getOrbitCoordinates()[ocx].accessPosx(), d.getOrbitCoordinates()[ocx].accessPosy(),
                        d.getOrbitCoordinates()[ocy].accessPosx(), d.getOrbitCoordinates()[ocy].accessPosy());
            } else if (d.getCrv() > 0.0) {
                is_circle = SphericalGeometry.Compute_Sph_Line(d.getOrbitCoordinates()[ocx].getDist(), d.getOrbitCoordinates()[ocx].getBeta(),
                        d.getOrbitCoordinates()[ocy].getDist(), d.getOrbitCoordinates()[ocy].getBeta(),
                        Math.tan(0.5 * d.getRad()),
                        false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                        d.getOrbitCoordinates()[ocx].accessPosx(), d.getOrbitCoordinates()[ocx].accessPosy(),
                        d.getOrbitCoordinates()[ocy].accessPosx(), d.getOrbitCoordinates()[ocy].accessPosy());
            } else {
                is_circle = ParabolicGeometry.Compute_Par_Line(d.getOrbitCoordinates()[ocx].getDist(), d.getOrbitCoordinates()[ocx].getBeta(),
                        d.getOrbitCoordinates()[ocy].getDist(), d.getOrbitCoordinates()[ocy].getBeta(),
                        d.getRad(),
                        false, Cen_Re, Cen_Im, Cen_R, Ang_B, Ang_E,
                        d.getOrbitCoordinates()[ocx].accessPosx(), d.getOrbitCoordinates()[ocx].accessPosy(),
                        d.getOrbitCoordinates()[ocy].accessPosx(), d.getOrbitCoordinates()[ocy].accessPosy());
            }
            if (is_circle) {
                Ang_P.set(0.5 * (Ang_E.get() + Ang_B.get()));
                d.getEdgeCoordinates()[ec].setDist(Cen_R.get());
                d.getEdgeCoordinates()[ec].setBeta(Ang_P.get());
                {
                    double[] coords = Maths.Compute_Cart_2(d.getEdgeCoordinates()[ec].getDist(), d.getEdgeCoordinates()[ec].getBeta());
                    d.getEdgeCoordinates()[ec].setPosx(coords[0] + Cen_Re.get());
                    d.getEdgeCoordinates()[ec].setPosy(coords[1] + Cen_Im.get());
                }
            } else {
                d.getEdgeCoordinates()[ec].setPosx(0.5 * (d.getOrbitCoordinates()[ocy].getPosx() + d.getOrbitCoordinates()[ocx].getPosx()));
                d.getEdgeCoordinates()[ec].setPosy(0.5 * (d.getOrbitCoordinates()[ocy].getPosy() + d.getOrbitCoordinates()[ocx].getPosy()));
                {
                    double[] coords = Maths.Compute_Polar_2(d.getEdgeCoordinates()[ec].getPosx(), d.getEdgeCoordinates()[ec].getPosy());
                    d.getEdgeCoordinates()[ec].setDist(coords[0]);
                    d.getEdgeCoordinates()[ec].setBeta(coords[1]);
                }
            }
            if (d.getEdgeCoordinates()[ec].getPosx() < d.getMinx()) {
                d.setMinx(d.getEdgeCoordinates()[ec].getPosx());
            }
            if (d.getEdgeCoordinates()[ec].getPosy() < d.getMiny()) {
                d.setMiny(d.getEdgeCoordinates()[ec].getPosy());
            }
            if (d.getEdgeCoordinates()[ec].getPosx() > d.getMaxx()) {
                d.setMaxx(d.getEdgeCoordinates()[ec].getPosx());
            }
            if (d.getEdgeCoordinates()[ec].getPosy() > d.getMaxy()) {
                d.setMaxy(d.getEdgeCoordinates()[ec].getPosy());
            }
        }
    }

    static public boolean relax_coords(DelaneySymbolWithGeometry d) {
        boolean nod_changes = relax_nods(d);
        boolean edg_changes = relax_edgs(d);
        boolean orb_changes = relax_orbs(d);
        return (nod_changes || edg_changes || orb_changes);
    }

    static private boolean relax_nods(DelaneySymbolWithGeometry d) {
        boolean nod_changes;
        int nc, ty;
        int ec;
        int oc;
        double mx, my, fac;
        nod_changes = false;
        for (nc = 0; nc < d.getNumberOfNodeCoordinates(); ++nc) {
            mx = 0.0;
            my = 0.0;
            fac = 0.0;
            for (ty = 0; ty < 3; ++ty) {
                ec = d.getNodeCoordinates()[nc].getEc(ty);
                mx += d.getEdgeCoordinates()[ec].getPosx();
                my += d.getEdgeCoordinates()[ec].getPosy();
                fac += 1.0;
                oc = d.getNodeCoordinates()[nc].getOc(ty);
                mx += d.getOrbitCoordinates(oc).getPosx();
                my += d.getOrbitCoordinates(oc).getPosy();
                fac += 1.0;
            }
            if (fac == 0.0) {
                continue;
            }
            mx /= fac;
            my /= fac;
            if (Math.abs(mx - d.getNodeCoordinates()[nc].getPosx()) >= Maths.EPSILON ||
                    Math.abs(my - d.getNodeCoordinates()[nc].getPosy()) >= Maths.EPSILON) {
                nod_changes = true;
                d.getNodeCoordinates()[nc].setPosx(mx);
                d.getNodeCoordinates()[nc].setPosy(my);
            }
        }
        return (nod_changes);
    }

    static public boolean relax_edgs(DelaneySymbolWithGeometry d) {
        boolean edg_changes;
        int ec, ep;
        int nca, ncb;
        int ocx, ocy;
        double mx, my, fac;
        edg_changes = false;
        for (ec = 0; ec < d.getNumberOfEdgeCoordinates(); ++ec) {
            ep = d.getEdgeCoordinates()[ec].getEp();
            if (d.getEdges()[ep].getId() <= 0) {
                continue;
            }
            mx = 0.0;
            my = 0.0;
            fac = 0.0;
            ocx = d.getEdgeCoordinates()[ec].getOcx();
            mx += d.getOrbitCoordinates()[ocx].getPosx();
            my += d.getOrbitCoordinates()[ocx].getPosy();
            fac += 1.0;
            ocy = d.getEdgeCoordinates()[ec].getOcy();
            mx += d.getOrbitCoordinates()[ocy].getPosx();
            my += d.getOrbitCoordinates()[ocy].getPosy();
            fac += 1.0;
            nca = d.getEdgeCoordinates()[ec].getNca();
            mx += d.getNodeCoordinates()[nca].getPosx();
            my += d.getNodeCoordinates()[nca].getPosy();
            fac += 1.0;
            ncb = d.getEdgeCoordinates()[ec].getNcb();
            mx += d.getNodeCoordinates()[ncb].getPosx();
            my += d.getNodeCoordinates()[ncb].getPosy();
            fac += 1.0;
            if (fac == 0.0) {
                continue;
            }
            mx /= fac;
            my /= fac;
            if (Math.abs(mx - d.getEdgeCoordinates()[ec].getPosx()) >= Maths.EPSILON ||
                    Math.abs(my - d.getEdgeCoordinates()[ec].getPosy()) >= Maths.EPSILON) {
                edg_changes = true;
                d.getEdgeCoordinates()[ec].setPosx(mx);
                d.getEdgeCoordinates()[ec].setPosy(my);
            }
        }
        return (edg_changes);
    }

    static private boolean relax_orbs(DelaneySymbolWithGeometry d) {
        boolean orb_changes = false;
        int oc, op;
        int nc;
        int ec;
        int p;
        double mx, my, fac;

        for (oc = 0; oc < d.getNumberOfOrbitCoordinates(); ++oc) {
            op = d.getOrbitCoordinates(oc).getOp();
            if (d.getOrb(op).getI() > 1) {
                continue;
            }
            mx = 0.0;
            my = 0.0;
            fac = 0.0;
            for (p = 0; p < d.getOrbitCoordinates(oc).getEcs(); ++p) {
                nc = d.getOrbitCoordinates(oc).getNc(p);
                mx += d.getNodeCoordinates()[nc].getPosx();
                my += d.getNodeCoordinates()[nc].getPosy();
                fac += 1.0;
                ec = d.getOrbitCoordinates(oc).getEc(p);
                mx += d.getEdgeCoordinates()[ec].getPosx();
                my += d.getEdgeCoordinates()[ec].getPosy();
                fac += 1.0;
            }
            if (fac == 0.0) {
                continue;
            }
            mx /= fac;
            my /= fac;
            if (Math.abs(mx - d.getOrbitCoordinates(oc).getPosx()) >= Maths.EPSILON ||
                    Math.abs(my - d.getOrbitCoordinates(oc).getPosy()) >= Maths.EPSILON) {
                orb_changes = true;
                d.getOrbitCoordinates(oc).setPosx(mx);
                d.getOrbitCoordinates(oc).setPosy(my);
            }
        }
        return (orb_changes);
    }

    static public void compute_dist(DelaneySymbolWithGeometry d) {
        int oc, op;
        int ec;
        int nc;
        for (oc = 0; oc < d.getNumberOfOrbitCoordinates(); ++oc) {
            double[] coords = Maths.Compute_Polar_2(d.getOrbitCoordinates(oc).getPosx(), d.getOrbitCoordinates(oc).getPosy());
            d.getOrbitCoordinates(oc).setDist(coords[0]);
            d.getOrbitCoordinates(oc).setBeta(coords[1]);

        }
        for (ec = 0; ec < d.getNumberOfEdgeCoordinates(); ++ec) {
            double[] coords = Maths.Compute_Polar_2(d.getEdgeCoordinates()[ec].getPosx(), d.getEdgeCoordinates()[ec].getPosy());
            d.getEdgeCoordinates()[ec].setDist(coords[0]);
            d.getEdgeCoordinates()[ec].setBeta(coords[1]);
        }
        for (nc = 0; nc < d.getNumberOfNodeCoordinates(); ++nc) {
            double[] coords = Maths.Compute_Polar_2(d.getNodeCoordinates()[nc].getPosx(), d.getNodeCoordinates()[nc].getPosy());
            d.getNodeCoordinates()[nc].setDist(coords[0]);
            d.getNodeCoordinates()[nc].setBeta(coords[1]);
        }
        for (oc = 0; oc < d.getNumberOfOrbitCoordinates(); ++oc) {
            op = d.getOrbitCoordinates(oc).getOp();
            if (d.getOrb(op).getI() > 1) {
                continue;
            }
            if (d.getCrv() < 0.0) {
                d.getOrb(op).setDist(2.0 * Maths.atanh(d.getOrbitCoordinates(oc).getDist()));
            } else if (d.getCrv() > 0.0) {
                d.getOrb(op).setDist(2.0 * Math.atan(d.getOrbitCoordinates(oc).getDist()));
            } else {
                d.getOrb(op).setDist(d.getOrbitCoordinates(oc).getDist());
            }
        }
    }
}
