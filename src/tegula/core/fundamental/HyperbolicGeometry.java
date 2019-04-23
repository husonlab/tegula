package tegula.core.fundamental;

import tegula.core.fundamental.utils.Maths;
import tegula.core.fundamental.utils.Wrap;

/**
 * hyperbolic geometry
 * Created by huson on 3/28/16.
 * Based on del_hgeo.h by Klaus Westphal, 1990
 */
class HyperbolicGeometry {
    static boolean Compute_Hyp_Line(double Beg_R, double Beg_A, double End_R, double End_A, double Aux_R, boolean infinite,
                                    Wrap<Double> Cen_Re, Wrap<Double> Cen_Im, Wrap<Double> Cen_R, Wrap<Double> Ang_B, Wrap<Double> Ang_E,
                                    Wrap<Double> Beg_Re, Wrap<Double> Beg_Im, Wrap<Double> End_Re, Wrap<Double> End_Im) {
        boolean is_circle;
        double Beg_D_Re, Beg_D_Im, Beg_M_Re, Beg_M_Im;
        double End_D_Re, End_D_Im, End_M_Re, End_M_Im;
        Beg_D_Re = Math.cos(Beg_A);
        Beg_D_Im = Math.sin(Beg_A);
        End_D_Re = Math.cos(End_A);
        End_D_Im = Math.sin(End_A);
        Beg_Re.set(Beg_D_Re * Beg_R);
        Beg_Im.set(Beg_D_Im * Beg_R);
        End_Re.set(End_D_Re * End_R);
        End_Im.set(End_D_Im * End_R);
        is_circle = (Beg_R > 0.0) && (End_R > 0.0) &&
                (Math.abs(Maths.fmod(Beg_A - End_A, Math.PI)) >= Maths.EPSILON);
        if (is_circle) {
            Beg_M_Re = 0.5 * (Beg_D_Re / Beg_R + Beg_Re.get());
            Beg_M_Im = 0.5 * (Beg_D_Im / Beg_R + Beg_Im.get());
            End_M_Re = 0.5 * (End_D_Re / End_R + End_Re.get());

            End_M_Im = 0.5 * (End_D_Im / End_R + End_Im.get());
            {
                Wrap<Double> CutI = new Wrap<>(0.0);
                Wrap<Double> CutO = new Wrap<>(0.0);

                EuclideanGeometry.cut_line_line(Cen_Re, Cen_Im, CutI, CutO,
                        Beg_M_Re, Beg_M_Im, Beg_M_Re + Beg_D_Im, Beg_M_Im - Beg_D_Re,
                        End_M_Re, End_M_Im, End_M_Re + End_D_Im, End_M_Im - End_D_Re);
            }

            Cen_R.set(Maths.Length_2(End_Re.get() - Cen_Re.get(), End_Im.get() - Cen_Im.get()));
            is_circle = (Cen_R.get() < 1.0 / Maths.EPSILON);
        }
        if (is_circle) {
            if (infinite) {
                Wrap<Double> Cut_Re = new Wrap<>(0.0);
                Wrap<Double> Cut_Im = new Wrap<>(0.0);

                EuclideanGeometry.cut_circle_circle(Cut_Re, Cut_Im,
                        Beg_Re, Beg_Im,
                        End_Re, End_Im,
                        0.0, 0.0, 1.0, Cen_Re.get(), Cen_Im.get(), Cen_R.get());
            }
            Ang_B.set(Maths.atanxy(Beg_Re.get() - Cen_Re.get(), Beg_Im.get() - Cen_Im.get()));
            Ang_E.set(Maths.atanxy(End_Re.get() - Cen_Re.get(), End_Im.get() - Cen_Im.get()));
            if (Ang_E.get() - Ang_B.get() > Math.PI) {
                Ang_E.set(Ang_E.get() - Maths.m_2pi);
            }
            if (Ang_B.get() - Ang_E.get() > Math.PI) {
                Ang_B.set(Ang_B.get() - Maths.m_2pi);
            }
        } else {
            Cen_Re.set(0.0);
            Cen_Im.set(0.0);
            Cen_R.set(0.0);
            Ang_B.set(0.0);
            Ang_E.set(0.0);
            if (infinite) {
                if (Beg_R > 0.0) {
                    Beg_Re.set(Beg_D_Re);
                    Beg_Im.set(Beg_D_Im);
                    End_Re.set(-Beg_D_Re);
                    End_Im.set(-Beg_D_Im);
                }
                if (End_R > 0.0) {
                    Beg_Re.set(-End_D_Re);
                    Beg_Im.set(-End_D_Im);
                    End_Re.set(End_D_Re);
                    End_Im.set(End_D_Im);
                }
            }
        }
        // Aux_R = Aux_R;
        return (is_circle);
    }

}
