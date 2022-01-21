/*
 * Maths.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.core.funtiles.utils;

/**
 * math
 * Created by huson on 3/27/16.
 * Based on del_math.h by Klaus Westphal, 1990
 */
public class Maths {
    public static final double m_2pi = 6.28318530717958647692428676655900576839433979875020; // 2* PI
    public static final double M_PI_2 = 1.57079632679489661923132169163975144;   // pi/2
    public static final double EPSILON = 1.0e-7;


    public static double sqr(double a) {
        return (a * a);
    }

    public static double atanxy(double x, double y) {
        return ((sqr(x) + sqr(y) < EPSILON) ? 0.0 : Math.atan2(y, x));
    }

    public static double acoth(double x) {
        return (0.5 * Math.log((x + 1.0) / (x - 1.0)));
    }

    public static double coth(double x) {
        return (1.0 / Math.tanh(x));
    }

    public static double cot(double angle) {
        return (Math.tan(M_PI_2 - angle));
    }

    public static double acot(double m) {
        return (M_PI_2 - Math.atan(m));
    }

    public static double atanh(double x) {
        return (0.5 * Math.log((1.0 + x) / (1.0 - x)));
    }

    public static double Length_2(double Pos_Re, double Pos_Im) {
        return (Math.hypot(Pos_Re, Pos_Im));
    }

    public static double Length_3(double Pos_X, double Pos_Y, double Pos_Z) {
        return (Math.sqrt(sqr(Pos_X) + sqr(Pos_Y) + sqr(Pos_Z)));
    }


    public static double[] Compute_Cart_2(double dd, double Phi) {
        return new double[]{dd * Math.cos(Phi), dd * Math.sin(Phi)};
    }

    public static double[] Compute_Cart_3(double dd, double Phi, double Rho) {
        final double[] x_y_z = new double[3];
        x_y_z[2] = dd * Math.sin(Rho);
        dd *= Math.cos(Rho);
        x_y_z[0] = dd * Math.cos(Phi);
        x_y_z[1] = dd * Math.sin(Phi);
        return x_y_z;
    }

    public static double[] Compute_Polar_2(double Pos_Re, double Pos_Im) {
        return new double[]{Length_2(Pos_Re, Pos_Im), atanxy(Pos_Re, Pos_Im)};
    }

    public static double[] Compute_Polar_3(double Pos_X, double Pos_Y, double Pos_Z) {
        final double[] double_Phi_Rho = new double[3];
        double_Phi_Rho[0] = Length_2(Pos_X, Pos_Y);
        double_Phi_Rho[1] = atanxy(Pos_X, Pos_Y);
        double_Phi_Rho[2] = atanxy(double_Phi_Rho[0], Pos_Z);
        double_Phi_Rho[0] = Length_3(Pos_X, Pos_Y, Pos_Z);
        return double_Phi_Rho;
    }

    public static double fmod(double a, double b) {
        return Math.IEEEremainder(a, b);
    }
}
