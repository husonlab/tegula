/*
 * SymmetryClass.java Copyright (C) 2020. Daniel H. Huson
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

import jloda.util.NumberUtils;

import java.util.ArrayList;

/**
 * computes the class of the symmetry group
 * Daniel Huson, 10.2019
 */
public enum SymmetryClass {
    Coxeter, Stellate, Hat, Projective, Moebius, Annular, Toroidal;

    /**
     * computes the symmetry class as defined in doi:10.1107/S205327331400549X
     *
     * @param dSymbol
     * @return group
     */
    public static SymmetryClass valueOf(DSymbol dSymbol) {
        final ArrayList<String> list = OrbifoldGroupName.getGroupNameAsList(dSymbol);

        int circles = 0;
        int stars = 0;
        int crosses = 0;
        int cones = 0;
        boolean inLeading = true;
        for (String part : list) {
			if (part.startsWith("(") && part.endsWith(")"))
				part = part.substring(1, part.length() - 1);
			if (NumberUtils.isInteger(part)) {
				if (NumberUtils.parseInt(part) > 1) {
					if (inLeading)
						cones++;
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

        if (circles == 0 && cones == 0 && stars == 1 && crosses == 0)
            return Coxeter;
        else if (circles == 0 && cones >= 2 && stars == 0 && crosses == 0)
            return Stellate;
        else if (circles == 0 && cones >= 1 && stars == 1 && crosses == 0)
            return Hat;
        else if (circles == 0 && cones >= 0 && stars == 0 && crosses >= 1)
            return Projective;
        else if (circles == 0 && cones >= 0 && stars >= 1 && crosses >= 1)
            return Moebius;
        else if (cones >= 1 && stars == 0 && crosses == 0)
            return Toroidal;
        else
            return Annular;

    }
}
