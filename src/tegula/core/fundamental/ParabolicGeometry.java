package tegula.core.fundamental;

import tegula.core.fundamental.utils.Wrap;

/**
 * parabolic geometry
 * Created by huson on 3/28/16.
 * Based on del_pgeo.h by Klaus Westphal, 1990
 */
class ParabolicGeometry {

    static boolean Compute_Par_Line(double Beg_R, double Beg_A, double End_R, double End_A, double Aux_R, boolean infinite,
                                    Wrap<Double> Cen_Re, Wrap<Double> Cen_Im, Wrap<Double> Cen_R, Wrap<Double> Ang_B, Wrap<Double> Ang_E,
                                    Wrap<Double> Beg_Re, Wrap<Double> Beg_Im, Wrap<Double> End_Re, Wrap<Double> End_Im) {
        double Del_Re, Del_Im;
        Beg_Re.set(Beg_R * Math.cos(Beg_A));
        Beg_Im.set(Beg_R * Math.sin(Beg_A));
        End_Re.set(End_R * Math.cos(End_A));
        End_Im.set(End_R * Math.sin(End_A));
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
        Cen_Re.set(0.0);
        Cen_Im.set(0.0);
        Cen_R.set(0.0);
        Ang_B.set(0.0);
        Ang_E.set(0.0);
        return (Aux_R != Aux_R);
    }
}
