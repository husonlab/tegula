package core.fundamental;

import core.fundamental.utils.Maths;
import core.fundamental.utils.Wrap;

/**
 * spherical geometry
 * Created by huson on 3/28/16.
 * Based on del_sgeo.h by Klaus Westphal, 1990
 */
public class SphericalGeometry {

    public static Boolean Compute_Sph_Line(double Beg_R, double Beg_A, double End_R, double End_A, double Aux_R, Boolean infinite,
                                           Wrap<Double> Cen_Re, Wrap<Double> Cen_Im, Wrap<Double> Cen_R, Wrap<Double> Ang_B, Wrap<Double> Ang_E,
                                           Wrap<Double> Beg_Re, Wrap<Double> Beg_Im, Wrap<Double> End_Re, Wrap<Double> End_Im) {
        Boolean is_circle;
        double Del_Re, Del_Im;
        double Beg_F, End_F, Cen_F;

        Beg_Re.set(Beg_R * Math.cos(Beg_A));
        Beg_Im.set(Beg_R * Math.sin(Beg_A));
        End_Re.set(End_R * Math.cos(End_A));
        End_Im.set(End_R * Math.sin(End_A));
        Cen_F = Beg_Re.get() * End_Im.get() - End_Re.get() * Beg_Im.get();

        if (Math.abs(Cen_F) >= Maths.EPSILON) {
            is_circle = true;
            Beg_F = 0.5 * (Maths.sqr(Beg_Re.get()) + Maths.sqr(Beg_Im.get()) - 1.0);
            End_F = 0.5 * (Maths.sqr(End_Re.get()) + Maths.sqr(End_Im.get()) - 1.0);
            Cen_Re.set((Beg_F * End_Im.get() - End_F * Beg_Im.get()) / Cen_F);
            Cen_Im.set((End_F * Beg_Re.get() - Beg_F * End_Re.get()) / Cen_F);
        } else if (Math.abs(Aux_R) < Maths.EPSILON) {
            is_circle = false;
        } else if (Math.abs(Math.abs(Beg_R) - 1.0) < Maths.EPSILON &&
                Math.abs(Math.abs(End_R) - 1.0) < Maths.EPSILON) {
            is_circle = true;
            Cen_R.set(0.5 * (Aux_R + 1.0 / Aux_R));
            Cen_Re.set((Cen_R.get() - Aux_R) * 0.5 * (Beg_Im.get() - End_Im.get()));
            Cen_Im.set((Cen_R.get() - Aux_R) * 0.5 * (End_Re.get() - Beg_Re.get()));
        } else
            is_circle = false;
        if (is_circle) {
            Cen_R.set(Math.sqrt(Maths.sqr(Cen_Re.get()) + Maths.sqr(Cen_Im.get()) + 1.0));
            is_circle = (Cen_R.get() < 1.0 / Maths.EPSILON);
            if (infinite) {
                Ang_B.set(0.0);
                Ang_E.set(Maths.m_2pi);
            } else {
                Ang_B.set(Maths.atanxy(Beg_Re.get() - Cen_Re.get(), Beg_Im.get() - Cen_Im.get()));
                Ang_E.set(Maths.atanxy(End_Re.get() - Cen_Re.get(), End_Im.get() - Cen_Im.get()));
                if (Ang_E.get() - Ang_B.get() > Math.PI) {
                    Ang_E.set(Ang_E.get() - Maths.m_2pi);
                }
                if (Ang_B.get() - Ang_E.get() > Math.PI) {
                    Ang_B.set(Ang_B.get() - Maths.m_2pi);
                }
            }
        } else {
            Cen_Re.set(0.0);
            Cen_Im.set(0.0);
            Cen_R.set(0.0);
            Ang_B.set(0.0);
            Ang_E.set(0.0);
            if (infinite) {
                Del_Re = End_Re.get() - Beg_Re.get();
                Del_Im = End_Im.get() - Beg_Im.get();
                Del_Re *= 100.0;
                Del_Im *= 100.0;
                Beg_Re.set(Beg_Re.get() - Del_Re);
                Beg_Im.set(Beg_Im.get() - Del_Im);
                End_Re.set(End_Re.get() + Del_Re);
                End_Im.set(End_Im.get() + Del_Im);
            }
        }
        return (is_circle);
    }

}
