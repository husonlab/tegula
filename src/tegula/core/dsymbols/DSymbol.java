/*
 * DSymbol.java Copyright (C) 2020. Daniel H. Huson
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
import jloda.util.BitSetUtils;
import jloda.util.Single;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Delaney symbol
 * Daniel Huson 3/2016
 */
public class DSymbol {
    private long nr1;
    private int nr2;

    private int[][] set;
    private int[][] matrix;

    public DSymbol() {
        this(0);
    }

    public DSymbol(int size) {
        set = new int[size + 1][3];
        matrix = new int[size + 1][3];
    }

    public DSymbol(DSymbol src) {
        this(0);
        copy(src);
    }

    public DSymbol(String string) {
        this(0);
        try {
            read(new StringReader(string));
        } catch (IOException e) {
            Basic.caught(e);
        }
    }

    /**
     * is D-symbol or D-set isomorphic to one in the given collection
     *
     * @param dSymbol
     * @param collection
     * @param setOnly    ignore Mij
     * @return true, if isomorphic to member of collection
     */
    public static boolean contained(DSymbol dSymbol, Collection<DSymbol> collection, boolean setOnly) {
        for (DSymbol other : collection) {
            if (Isomorphic.isomorphic(dSymbol, other, setOnly))
                return true;
        }
        return false;
    }

    public void clear() {
        nr1 = 0;
        nr2 = 0;
        set = new int[1][3];
        matrix = new int[1][3];
    }

    /**
     * copy a Delaney symbol
     *
     * @param src
     */
    public void copy(DSymbol src) {
        nr1 = src.nr1;
        nr2 = src.nr2;

        set = new int[src.set.length][3];
        for (int i = 0; i < src.set.length; i++) {
            System.arraycopy(src.set[i], 0, set[i], 0, src.set[i].length);
        }

        matrix = new int[src.matrix.length][3];
        for (int i = 0; i < src.matrix.length; i++) {
            System.arraycopy(src.matrix[i], 0, matrix[i], 0, src.matrix[i].length);
        }
    }

    /**
     * append a Delaney symbol
     *
     * @param src
     */
    public void append(DSymbol src) {
        final int offset = set.length;

        final int[][] tmpSet = new int[offset + src.set.length - 1][3];
        for (int i = 0; i < offset; i++) {
            System.arraycopy(set[i], 0, tmpSet[i], 0, set[i].length);
        }
        set = tmpSet;

        for (int a = 1; a < src.set.length; a++) { // don't copy 0-entry, it is empty
            for (int i = 0; i <= 2; i++) {
                set[offset + a - 1][i] = src.set[a][i] + offset - 1;
            }
        }

        final int[][] tmpMatrix = new int[offset + src.matrix.length - 1][3];
        for (int i = 0; i < offset; i++) {
            System.arraycopy(matrix[i], 0, tmpMatrix[i], 0, matrix[i].length);
        }
        matrix = tmpMatrix;

        for (int a = 1; a < src.matrix.length; a++) { // don't copy 0-entry, it is empty
            System.arraycopy(src.matrix[a], 0, matrix[offset + a - 1], 0, 3);
        }

    }


    /**
     * resize, maintaining previous data
     *
     * @param newSize
     */
    public void resize(int newSize) {
        final int[][] tmpSet = new int[newSize + 1][3];
        final int[][] tmpMatrix = new int[newSize + 1][3];
        final int top = Math.min(size(), newSize);
        for (int i = 0; i <= top; i++) {
            System.arraycopy(set[i], 0, tmpSet[i], 0, 3);
            System.arraycopy(matrix[i], 0, tmpMatrix[i], 0, 3);
        }
        set = tmpSet;
        matrix = tmpMatrix;
    }

    public int size() {
        return Math.max(set.length - 1, 0);
    }

    public long getNr1() {
        return nr1;
    }

    public void setNr1(long nr1) {
        this.nr1 = nr1;
    }

    public int getNr2() {
        return nr2;
    }

    public void setNr2(int nr2) {
        this.nr2 = nr2;
    }

    public int dim() {
        return 2;
    }

    public int getS0(int a) {
        return getSi(0, a);
    }

    public int getS1(int a) {
        return getSi(1, a);
    }

    public int getS2(int a) {
        return getSi(2, a);
    }

    public int getSi(int i, int a) {
        return set[a][i];
    }

    public BitSet getSi(int i, BitSet set) {
        final BitSet result = new BitSet();
        for (int a : BitSetUtils.members(set)) {
            result.set(getSi(i, a));
        }
        return result;
    }

    public void setS0(int a, int value) {
        setSi(0, a, value);
    }

    public void setS1(int a, int value) {
        setSi(1, a, value);
    }

    public void setS2(int a, int value) {
        setSi(2, a, value);
    }

    public void setSi(int i, int a, int b) {
        set[a][i] = b;
        set[b][i] = a;
    }

    public void unsetSi(int i, int a) {
        set[a][i] = 0;
    }

    public void unsetSi(int i, int a, int b) {
        set[a][i] = 0;
        set[b][i] = 0;
    }

    public int getM01(int a) {
        return getMij(0, 1, a);
    }

    public int getM02(int a) {
        return getMij(0, 2, a);
    }

    public int getM12(int a) {
        return getMij(1, 2, a);
    }

    public int getMij(final int i, final int j, final int a) {
        return matrix[a][i + j - 1]; // 0+1-1=0, 0+2-1=1, 1+2-1=2
    }

    public void setM01(final int a, final int value) {
        setMij(0, 1, a, value);
    }

    public void setM02(final int a, final int value) {
        setMij(0, 2, a, value);
    }

    public void setM12(final int a, final int value) {
        setMij(1, 2, a, value);
    }

    /**
     * marks the i,j-orbit containing a and returns the next flag not contained in that orbit
     *
     * @param i
     * @param j
     * @param a
     * @param visited if a==1, the  set is cleared
     * @return smallest node in next orbit
     */
    public int nextOrbit(final int i, final int j, final int a, final BitSet visited) {
        if (a == 1)
            visited.clear();
        markOrbit(i, j, a, visited);
        return visited.nextClearBit(a + 1);
    }

    /**
     * marks the i,j-orbit containing a and returns the next flag not contained in that orbit
     *
     * @param i
     * @param j
     * @param a
     * @param array we set array[b]=mark for all b contained in the same i,j-orbit as a
     * @param mark
     * @return smallest node in next orbit
     */
    public int nextOrbit(final int i, final int j, int a, final int[] array, int mark) {
        markOrbit(i, j, a, array, mark);
        while (a < array.length && array[a] != 0)
            a++;
        return a;
    }

    /**
     * iterates over smallest node in each i,j orbit
     *
     * @param i
     * @param j
     * @return all orbits
     */
    public Iterable<Integer> orbits(int i, int j) {
        return () -> new Iterator<>() {
            private final BitSet seen = new BitSet();
            private int a = 1;

            @Override
            public boolean hasNext() {
                return a <= size();
            }

            @Override
            public Integer next() {
                int result = a;
                a = nextOrbit(i, j, a, seen);
                return result;
            }
        };
    }

    public Iterable<int[]> orbitLabels() {
        return () -> new Iterator<>() {
            private int k = 0;
            final private BitSet fl = new BitSet();
            private int a = 1;

            @Override
            public boolean hasNext() {
                return a <= size();
            }

            @Override
            public int[] next() {
                if (hasNext()) {
                    final int[] result = {i(k), j(k), a};
                    a = nextOrbit(i(k), j(k), a, fl);
                    if (a > size() && k < 2) {
                        k++;
                        a = 1;
                    }

                    return result;
                } else
                    return null;
            }
        };
    }

    public final Stream<int[]> orbitLabelsStream() {
        return StreamSupport.stream(orbitLabels().spliterator(), false);
    }


    /**
     * visit all flags contained in the i,j-orbit containing flag a
     *
     * @param i
     * @param j
     * @param a
     * @param visitor accept this before moving to next orbit
     */
    public void visitOrbit(final int i, final int j, final int a, final Consumer<Integer> visitor) {
        int b = a;
        do {
            visitor.accept(b);
            b = getSi(i, b);
            visitor.accept(b);
            b = getSi(j, b);
        }
        while (b != a);
    }

    public ArrayList<Integer> orbitMembers(int i, int j, int a) {
        final ArrayList<Integer> set = new ArrayList<>();
        visitOrbit(i, j, a, set::add);
        return set;
    }

    /**
     * marks all flags contain in the i,j-orbit of a
     *
     * @param i
     * @param j
     * @param a
     * @param visited
     */
    public void markOrbit(final int i, final int j, final int a, final BitSet visited) {
        int b = a;
        int length = 0;
        do {
            visited.set(b);
            b = getSi(i, b);
            visited.set(b);
            b = getSi(j, b);
            if (++length > size())
                throw new RuntimeException("Orbit too long");
        }
        while (b != a);
    }

    /**
     * marks all flags contain in the i,j-orbit of a
     *
     * @param i
     * @param j
     * @param a
     * @param visited
     * @param mark
     */
    public void markOrbit(final int i, final int j, final int a, final int[] visited, final int mark) {
        if (mark == 0)
            throw new RuntimeException("Illegal: mark==0");
        int b = a;
        do {
            visited[b] = mark;
            b = getSi(i, b);
            visited[b] = mark;
            b = getSi(j, b);
        }
        while (b != a);
    }

    /**
     * marks all flags contain in the i,j-orbit of a. Unmark all others
     *
     * @param i
     * @param j
     * @param a
     * @param visited
     * @param mark
     */
    public void markOrbitX(final int i, final int j, final int a, final int[] visited, final int mark) {
        Arrays.fill(visited, 0);
        markOrbit(i, j, a, visited, mark);
    }

    /**
     * computes the orbit length (number si*sj operations required to get from a to a)
     *
     * @param i
     * @param j
     * @param a
     * @return length
     */
    public int computeOrbitLength(final int i, final int j, final int a) {
        int length = 0;
        int b = a;
        do {
            b = getSi(i, b);
            b = getSi(j, b);
            length++;
        }
        while (b != a);
        return length;
    }

    /*
     ** computes the cardinality of an i,j-orbit
     */
    public int computeOrbitCardinality(int i, int j, int a) {
        return (hasFixPoints(i, j, a) ? 1 : 2) * computeOrbitLength(i, j, a);
    }

    public int countOrbits(final int i, final int j) {
        BitSet mark = new BitSet();
        int count = 0;
        for (int a = 1; a <= size(); a = nextOrbit(i, j, a, mark))
            count++;
        return count;
    }

    public int[] computeOrbits(int i, int j) {
        final int[] orbits = new int[size() + 1];
        int a = 1;
        int count = 0;
        while (a <= size())
            a = nextOrbit(i, j, a, orbits, ++count);
        return orbits;
    }

    /**
     * set Mij on all flags contained in the i,j-orbit containing a
     *
     * @param i
     * @param j
     * @param a
     * @param value
     */
    public void setMij(int i, int j, int a, int value) {
        int b = a;
        do {
            matrix[b][i + j - 1] = value;
            b = getSi(i, b);
            matrix[b][i + j - 1] = value;
            b = getSi(j, b);
        }
        while (b != a);
    }

    public void setMatrixIJ(int i, int j, int a, int value) {
        matrix[a][i + j - 1] = value;

        //getVij(i,j,a);

    }

    public int getVij(int i, int j, int a) {
        int r = computeOrbitLength(i, j, a);
        int m = getMij(i, j, a);
        if ((m % r) != 0)
            throw new RuntimeException("m not multiple of r");
        else
            return m / r;
    }

    public void setVij(int i, int j, int a, int v) {
        int r = computeOrbitLength(i, j, a);
        setMij(i, j, a, r * v);
    }

    /**
     * gets the type of the i,j-orbit that contains a
     *
     * @param i
     * @param j
     * @param a
     * @return 2 for chain and 1 for cycle
     */
    public int getOrbitType(int i, int j, int a) {
        return hasFixPoints(i, j, a) ? 2 : 1;
    }

    public boolean hasFixPoints(int i, int j, int a) {
        int c = a;
        do {
            if (getSi(i, a) == a)
                return true;
            else
                a = getSi(i, a);
            if (getSi(j, a) == a)
                return true;
            else
                a = getSi(j, a);
        }
        while (a != c);
        return false;
    }

    /**
     * is i,j-orbit containing flag a a cycle?
     *
     * @param i
     * @param j
     * @param a
     * @return true, iff cycle
     */
    public boolean isCycle(int i, int j, int a) {
        int b = a;
        do {
            if (getSi(i, b) == b || getSi(j, b) == b)
                return false;
            b = getSi(i, b);
            if (getSi(i, b) == b || getSi(j, b) == b)
                return false;
            b = getSi(j, b);
        }
        while (a != b);
        return true;
    }

    /**
     * computes the orientation
     *
     * @return 2 if orientable and fixpoint-free, 1 if orientable with fixpoints, 0 if not orientable
     */
    public int computeOrientation() {
        int[] ori = new int[size() + 1];
        return computeOrientation(ori);
    }

    /**
     * computes the orientation
     *
     * @param ori contains orientation values for all flags
     * @return 2 if orientable and fixpoint-free, 1 if orientable with fixpoints, 0 if not orientable
     */
    public int computeOrientation(int[] ori) {
        return orientate(new Single<>(2), 1, 1, ori);
    }

    private int orientate(Single<Integer> result, int value, int a, int[] ori) {
        int b;

        ori[a] = value;
        for (int i = 0; result.get() != 0 && i <= dim(); i++) {
            b = getSi(i, a);
            if (ori[b] != 0) {
                if (ori[b] != -ori[a]) {
                    if (a == b)
                        result.set(1);
                    else
                        result.set(0);
                }
            } else
                orientate(result, -ori[a], b, ori);
        }
        return result.get();
    }

    /**
     * is fixed point free?
     *
     * @return true, if fixed point free
     */
    public boolean isFixedPointFree() {
        for (int a = 1; a <= size(); a++) {
            for (int i = 0; i < 3; i++) {
                if (getSi(i, a) == a)
                    return false;
            }
        }
        return true;
    }

    /**
     * compute the Euler characteristic of the Delaney set
     *
     * @return Euler characteristic
     */
    public int computeEulerCharacteristic() {
        int res = size() + countOrbits(0, 1) + countOrbits(0, 2) + countOrbits(1, 2);

        for (int i = 0; i <= 2; i++) {
            BitSet mark = new BitSet();
            for (int a = 1; a <= size(); a++) {
                if (!mark.get(a)) {
                    mark.set(a);
                    mark.set(getSi(i, a));
                    res--;
                }
            }
        }
        return res;
    }


    /**
     * computes the other end of a i,j-chain
     *
     * @param i
     * @param j
     * @param a
     * @return other end of chain
     */
    public int[] computeOtherChainEnd(int i, int j, int a) {
        if (getSi(i, a) != a)
            throw new RuntimeException("computeOtherChainEnd(): getSi(" + a + "," + i + ") = " + getSi(i, a) + " != " + a);
        int i0 = j;
        int j0;
        int a0 = a;
        while (true) {
            if (a0 == getSi(i0, a0)) {
                j0 = (i0 == i ? j : i);
                return new int[]{i0, j0, a0};
            } else {
                a0 = getSi(i0, a0);
                i0 = (i0 == i ? j : i);
            }
        }
    }

    /**
     * read a 2D Delaney symbol
     *
     * @param r0
     * @throws IOException
     */
    public boolean read(final Reader r0) throws IOException {
        boolean debug = false;
        final String fullDelimiters = "[\\s:.,>]";
        final String reducedDelimiters = "[\\s.,>]";

        final StringBuilder buf = new StringBuilder();
        while (r0.ready()) {
            int ch = r0.read();
            if (ch == -1 || ch == '\n')
                break;
            buf.append((char) ch);
        }
        final String line = buf.toString();

        if (line.length() > 0 && line.startsWith("<")) {
            final Scanner scanner = new Scanner(line.substring(1)).useDelimiter(fullDelimiters);

            nr1 = scanner.nextLong();
            nr2 = scanner.nextInt();
            int size = scanner.nextInt();
            resize(size);
            // this is a bit messy because string may optionally contain dimension
            Integer first;
            {
                scanner.useDelimiter(reducedDelimiters);
                final String word = scanner.next();
                final int dim;
                if (word.startsWith(":")) { // no dimension given
                    dim = 2;
                    first = Integer.parseInt(word.substring(1));
                } else { // format should be: number:number
                    int pos = word.indexOf(":");
                    dim = Integer.parseInt(word.substring(0, pos));
                    first = Integer.parseInt(word.substring(pos + 1));
                }
                scanner.useDelimiter(fullDelimiters);
                if (dim != 2)
                    throw new IOException("Invalid input: 3D Delaney symbol");
            }

            for (int i = 0; i < 3; i++) {
                final BitSet seen = new BitSet();
                for (int a = 1; a <= size; a = seen.nextClearBit(a + 1)) {
                    int b;
                    if (first != null) {
                        b = first;
                        first = null;
                    } else
                        b = scanner.nextInt();
                    seen.set(b);
                    setSi(i, a, b);
                    if (debug)
                        System.err.println("setS" + i + "(" + a + ")=" + b);
                }
            }
            for (int i = 0; i < 2; i++) {
                final BitSet seen = new BitSet();
                for (int a : orbits(i, i + 1)) {
                    int m = scanner.nextInt();
                    setMij(i, i + 1, a, m);
                    if (debug) {
                        System.err.println("setM" + i + "" + (i + 1) + "(" + a + ")=" + m);
                        System.err.println("orbit(" + i + "," + (i + 1) + "," + a + "): " + Basic.toString(orbitMembers(i, i + 1, a), " "));
                    }
                }
            }
            for (int a = 1; a <= size; a++) {
                setM02(a, 2);
            }
            if (debug) {
                for (int a : orbits(0, 2)) {
                    System.err.println("orbit(0,2," + a + "): " + Basic.toString(orbitMembers(0, 2, a), " "));
                }
            }

            if (scanner.hasNext())
                throw new IOException("End of symbol: not found");
            return true;
        } else
            return false;
    }

    public String toString() {
        final StringWriter w = new StringWriter();
        try {
            write(w);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return w.toString();
    }

    public void write(Writer w) throws IOException {
        w.write(String.format("<%d.%d:%d:", nr1, nr2, size()));
        for (int i = 0; i < 3; i++) {
            boolean first = true;
            for (int a = 1; a <= size(); a++) {
                if (a <= getSi(i, a)) {
                    if (first)
                        first = false;
                    else
                        w.write(" ");
                    w.write(String.format("%d", getSi(i, a)));
                }
            }
            w.write(i < 2 ? "," : ":");
        }
        for (int i = 0; i < 2; i++) {
            final BitSet seen = new BitSet();
            boolean first = true;
            for (int a = 1; a <= size(); a = nextOrbit(i, i + 1, a, seen)) {
                if (first)
                    first = false;
                else
                    w.write(" ");
                w.write(String.format("%d", getMij(i, i + 1, a)));
            }
            if (i == 0)
                w.write(",");
        }
        w.write(">");
    }

    public Rational computeCurvature() {
        Rational curve = new Rational(-size(), 2);

        for (int a = 1; a <= size(); a++)
            curve = curve.add(1, getM01(a)).add(1, getM12(a));
        return curve;
    }

    public Geometry computeGeometry() {
        switch (computeCurvature().sign()) {
            case -1:
                return Geometry.Hyperbolic;
            case 1:
                return Geometry.Spherical;
            case 0:
                return Geometry.Euclidean;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static int i(int k) {
        return k == 0 ? 1 : 0;
    }

    public static int j(int k) {
        return k == 2 ? 1 : 2;
    }

    public int getFixpointInOrbit(int i, int j, int a) {
        final Single<Integer> fixedPoint = new Single<>(-1);
        visitOrbit(i, j, a, (b) -> {
            if (fixedPoint.get() == -1 && getSi(i, b) == b || getSi(j, b) == b)
                fixedPoint.set(b);
        });
        return fixedPoint.get();
    }

    public BitSet computeFirstInOrbit(int i, int j) {
        final BitSet visited = new BitSet();
        final BitSet result = new BitSet();
        for (int a = 1; a <= size(); a = nextOrbit(i, j, a, visited))
            result.set(a);
        return result;
    }

    public int getFlagForOrbit(int i, int j, int whichOrbit) {
        int count = 0;
        final BitSet visited = new BitSet();
        for (int a = 1; a <= size(); a = nextOrbit(i, j, a, visited)) {
            if (++count == whichOrbit)
                return a;
        }
        return -1;
    }

    /**
     * determines whether flags 'a' and 'b' are in the same i,j-orbit
     *
     * @param i
     * @param j
     * @param a
     * @param b
     * @return
     */
    public boolean inSameOrbit(int i, int j, int a, int b) {
        if (a == b)
            return true;
        else {
            int c = a;
            do {
                c = getSi(i, c);
                if (c == b)
                    return true;
                c = getSi(j, c);
                if (c == b)
                    return true;
            }
            while (c != a);
            return false;
        }
    }

    public boolean hasTrivialStabilizer(int i, int j, int a) {
        return !hasFixPoints(i, j, a) && getVij(i, j, a) == 1;
    }
}
