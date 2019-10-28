/*
 * SymmetryClass.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.core.dsymbols;

import jloda.util.Basic;

import java.util.ArrayList;

/**
 * computes the class of the symmetry group
 * Daniel Huson, 10.2019
 */
public enum SymmetryClass {
    coxeter, stellate, hat, projective, moebius, annular, toroidal, other;


    /**
     * computes the symmetry class as defined in doi:10.1107/S205327331400549X
     *
     * @param dSymbol
     * @return group
     */
    public static SymmetryClass valueOf(DSymbol dSymbol) {
        final ArrayList<String> list = OrbifoldGroupName.getGroupNameAsList(dSymbol);
        final String orbfoldName = OrbifoldGroupName.getGroupName(dSymbol);

        int circles = 0;
        int stars = 0;
        int crosses = 0;
        int leadingCones = 0;
        int otherCones = 0;
        boolean inLeading = true;
        for (String part : list) {
            if (part.startsWith("(") && part.endsWith(")"))
                part = part.substring(1, part.length() - 1);
            if (Basic.isInteger(part)) {
                if (Basic.parseInt(part) > 1) {
                    if (inLeading)
                        leadingCones++;
                    else
                        otherCones++;
                }
            } else if (inLeading)
                inLeading = false;
            if (part.equals("*"))
                stars++;
            if (part.equals("o"))
                circles++;
            if (part.equals("x"))
                crosses++;
        }

        if (stars > 0 && circles == 0 && crosses == 0 && leadingCones == 0)
            return coxeter;
        else if (leadingCones > 0 && stars == 0 && circles == 0 && crosses == 0)
            return stellate;
        else if (leadingCones > 0 && stars > 0 && circles == 0 && crosses == 0)
            return hat;
        else if (circles == 0 && stars == 0 && crosses > 0)
            return projective;
        else if (circles == 0 && stars > 0 && crosses > 0)
            return moebius;
        else if (stars >= 2 && crosses == 0)
            return annular;
        else if (circles > 0 && stars == 0 && crosses == 0)
            return toroidal;
        else
            return other;
    }

}
