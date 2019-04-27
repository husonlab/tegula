/*
 * EuclideanGeometry.java Copyright (C) 2019. Daniel H. Huson
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

import tegula.core.fundamental.utils.Maths;
import tegula.core.fundamental.utils.Wrap;

/**
 * Euclidean geometry
 * Created by huson on 3/28/16.
 * Based on del_egeo.h by Klaus Westphal, 1990
 */
public class EuclideanGeometry {

    static double cut_circle_circle(Wrap<Double> CutMX, Wrap<Double> CutMY,
                                    Wrap<Double> CutLX, Wrap<Double> CutLY,
                                    Wrap<Double> CutRX, Wrap<Double> CutRY,
                                    double CenAX, double CenAY, double RadA,
                                    double CenBX, double CenBY, double RadB) {
        double DisX, DisY;
        double Dis2, RadA2, RadB2;
        double Fact;
        double Dis;
        DisX = CenBX - CenAX;
        DisY = CenBY - CenAY;
        Dis2 = Maths.sqr(DisX) + Maths.sqr(DisY);
        RadA2 = Maths.sqr(RadA);
        RadB2 = Maths.sqr(RadB);
        Fact = 0.5 * (RadB2 - RadA2) / Dis2;
        CutMX.set((0.5 + Fact) * CenAX + (0.5 - Fact) * CenBX);
        CutMY.set((0.5 + Fact) * CenAY + (0.5 - Fact) * CenBY);
        CenAX = CutMX.get() - CenAX;
        CenAY = CutMY.get() - CenAY;
        CenBX = CutMX.get() - CenBX;
        CenBY = CutMY.get() - CenBY;
        Dis = Maths.sqr(CenAX) + Maths.sqr(CenAY);
        RadA2 -= Dis;
        Dis = Math.sqrt(Dis);
        RadA2 = Math.sqrt(RadA2);
        Dis = RadA2 / Dis;
        CutLX.set(CutMX.get() - CenAY * Dis);
        CutLY.set(CutMY.get() + CenAX * Dis);
        CutRX.set(CutMX.get() + CenAY * Dis);
        CutRY.set(CutMY.get() - CenAX * Dis);
        Dis *= (Maths.sqr(CenAX) + Maths.sqr(CenAY));
        return (Dis);
    }

    static double cut_circle_line(Wrap<Double> CutMX, Wrap<Double> CutMY, Wrap<Double> CutM,
                                  Wrap<Double> CutLX, Wrap<Double> CutLY, Wrap<Double> CutL,
                                  Wrap<Double> CutRX, Wrap<Double> CutRY, Wrap<Double> CutR,
                                  double CenX, double CenY, double Rad,
                                  double BegX, double BegY, double EndX, double EndY) {
        double Beg2, End2;
        double MidX, MidY, Mid2;
        double DelX, DelY, Del2;
        double Del;
        double Dif2, Sum2, Dis2;
        double Rad2;
        MidX = 0.5 * (EndX + BegX);
        MidY = 0.5 * (EndY + BegY);
        DelX = 0.5 * (EndX - BegX);
        DelY = 0.5 * (EndY - BegY);
        BegX -= CenX;
        BegY -= CenY;
        EndX -= CenX;
        EndY -= CenY;
        MidX -= CenX;
        MidY -= CenY;
        Del2 = Maths.sqr(DelX) + Maths.sqr(DelY);
        Del = Math.sqrt(Del2);
        Beg2 = Maths.sqr(BegX) + Maths.sqr(BegY);
        End2 = Maths.sqr(EndX) + Maths.sqr(EndY);
        Mid2 = Maths.sqr(MidX) + Maths.sqr(MidY);
        Dif2 = 0.5 * (End2 - Beg2);
        Sum2 = 0.5 * (End2 + Beg2);
        Dis2 = Sum2 - 0.25 * Maths.sqr(Dif2) / Del2 - Del2;
        Rad2 = Maths.sqr(Rad);
        if (CutM != null) {
            CutM.set(Math.sqrt(Mid2 - Dis2) / Del);
            CutMX.set(MidX + CenX - CutM.get() * DelX);
            CutMY.set(MidY + CenY - CutM.get() * DelY);
            CutL.set(+Math.sqrt(Rad2 - Dis2) / Del);
            CutLX.set(CutMX.get() + CutL.get() * DelX);
            CutLY.set(CutMY.get() + CutL.get() * DelY);
            CutR.set(-Math.sqrt(Rad2 - Dis2) / Del);
            CutRX.set(CutMX.get() + CutR.get() * DelX);
            CutRY.set(CutMY.get() + CutR.get() * DelY);
        }
        return (Math.sqrt(Dis2));
    }

    static double cut_line_line(Wrap<Double> CutX, Wrap<Double> CutY, Wrap<Double> Cut1, Wrap<Double> Cut2,
                                double Beg1X, double Beg1Y, double End1X, double End1Y,
                                double Beg2X, double Beg2Y, double End2X, double End2Y) {
        double Del1X, Del1Y, Del2X, Del2Y;
        double Mid1X, Mid1Y, Mid2X, Mid2Y;
        double Dis, Fac0, Fac1, Fac2;
        Del1X = 0.5 * (End1X - Beg1X);
        Del1Y = 0.5 * (End1Y - Beg1Y);
        Del2X = 0.5 * (End2X - Beg2X);
        Del2Y = 0.5 * (End2Y - Beg2Y);
        Mid1X = 0.5 * (End1X + Beg1X);
        Mid1Y = 0.5 * (End1Y + Beg1Y);
        Mid2X = 0.5 * (End2X + Beg2X);
        Mid2Y = 0.5 * (End2Y + Beg2Y);
        Dis = 0.0;
        Fac0 = Del1X * Del2Y - Del2X * Del1Y;
        if (Fac0 == 0.0) {
            return (Dis);
        }
        Fac1 = (Del1Y * Mid1X - Del1X * Mid1Y) / Fac0;
        Fac2 = (Del2Y * Mid2X - Del2X * Mid2Y) / Fac0;
        CutX.set(Del1X * Fac2 - Del2X * Fac1);
        CutY.set(Del1Y * Fac2 - Del2Y * Fac1);
        if (Cut1 != null) {
            Cut1.set((CutX.get() * Del1X + CutY.get() * Del1Y) / (Del1X * Del1X + Del1Y * Del1Y));
        }
        if (Cut2 != null) {
            Cut2.set((CutX.get() * Del2X + CutY.get() * Del2Y) / (Del2X * Del2X + Del2Y * Del2Y));
        }
        return (Dis);
    }
}
