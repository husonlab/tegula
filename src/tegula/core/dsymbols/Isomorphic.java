/*
 * Isomorphic.java Copyright (C) 2022 Daniel H. Huson
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

package tegula.core.dsymbols;

public class Isomorphic {
    public static boolean isomorphic(DSymbol ds1, DSymbol ds2) {
        return isomorphic(ds1, ds2, false);
    }

    /**
     * determines whether Delaney symbols ds1 and ds2 are isomorphic
     *
     * @param ds1
     * @param ds2
     * @param compareSetsOnly
     * @return
     */
    public static boolean isomorphic(DSymbol ds1, DSymbol ds2, boolean compareSetsOnly) {
        if (ds1.size() != ds2.size())
            return false;
        for (int a = 1; a <= ds1.size(); a++)
            if (isomorphism(1, a, ds1, ds2, compareSetsOnly))
                return true;
        return false;
    }

    /*
     ** determines whether mapping a onto b extends to an isomorphism
     ** mapping d,m onto ds
     */
    public static boolean isomorphism(int a, int b, DSymbol ds1, DSymbol ds2, boolean compareSetsOnly) {
        if (ds1.size() != ds2.size())
            return false;

        return isomorphicRec(a, b, new int[ds2.size() + 1], ds1, ds2, compareSetsOnly);
    }

    private static boolean isomorphicRec(int b1, int b2, int[] fl, DSymbol ds1, DSymbol ds2, boolean compareSetsOnly) {
        if (fl[b2] != 0)
            return b1 == fl[b2] && (compareSetsOnly || (ds1.getM01(b1) == ds2.getM01(b2) && ds1.getM12(b1) == ds2.getM12(b2)));

        fl[b2] = b1;
        for (int i = 0; i <= 2; i++)
            if (!isomorphicRec(ds1.getSi(i, b1), ds2.getSi(i, b2), fl, ds1, ds2, compareSetsOnly))
                return false;
        return true;
    }
}
