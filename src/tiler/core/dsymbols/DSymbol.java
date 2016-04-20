package tiler.core.dsymbols;

import tiler.core.fundamental.utils.Wrap;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.BitSet;
import java.util.Scanner;

/**
 * Delaney symbol
 * Created by huson on 3/28/16.
 */
public class DSymbol {
    private int nr1;
    private int nr2;

    private int[][] set;
    private int[][] matrix;

    /**
     * constructor
     */
    public DSymbol() {
        this(0);
    }

    /**
     * constructor
     *
     * @param size
     */
    public DSymbol(int size) {
        set = new int[size][3];
        matrix = new int[size][3];
    }

    public void resize(int newSize) {
        final int[][] tmpSet = new int[newSize + 1][3];
        final int top = Math.min(size(), newSize);
        System.arraycopy(set, 0, tmpSet, 0, top);
        set = tmpSet;
        final int[][] tmpMatrix = new int[newSize + 1][3];
        System.arraycopy(matrix, 0, tmpMatrix, 0, top);
        matrix = tmpMatrix;
    }

    public int size() {
        return Math.max(set.length - 1, 0);
    }

    public int getNr1() {
        return nr1;
    }

    public void setNr1(int nr1) {
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
     * visit all flags contained in the i,j-orbit containing flag a
     *
     * @param i
     * @param j
     * @param a
     * @param visitor
     */
    public void visitOrbit(final int i, final int j, final int a, final OrbitVisitor visitor) {
        int b = a;
        do {
            visitor.visit(b);
            b = getSi(i, b);
            visitor.visit(b);
            b = getSi(j, b);
        }
        while (b != a);
    }

    /**
     * visit all flags contained in the i,j-orbit containing flag a
     *
     * @param i
     * @param j
     * @param a
     * @param visited
     * @param visitor
     */
    public void visitOrbit(final int i, final int j, final int a, final BitSet visited, final OrbitVisitor visitor) {
        int b = a;
        do {
            visitor.visit(b);
            visited.set(b);
            b = getSi(i, b);
            visitor.visit(b);
            visited.set(b);
            b = getSi(j, b);
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
     */
    public void markOrbit(final int i, final int j, final int a, final BitSet visited) {
        int b = a;
        do {
            visited.set(b);
            b = getSi(i, b);
            visited.set(b);
            b = getSi(j, b);
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

    public int countOrbits(final int i, final int j) {
        BitSet mark = new BitSet();
        int count = 0;
        for (int a = 1; a <= size(); a = nextOrbit(i, j, a, mark))
            count++;
        return count;
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

    public int getVij(int i, int j, int a) {
        int r = computeOrbitLength(i, j, a);
        int m = getMij(i, j, a);
        if ((m % r) != 0)
            throw new RuntimeException("m not multiple of r");
        else
            return m / r;
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
        return orientate(new Wrap<Integer>(2), 1, 1, ori);
    }

    private int orientate(Wrap<Integer> result, int value, int a, int[] ori) {
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
     * @return Eulcer characteristic
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
        final String fullDelimiters = "[\\s:\\.,>]";
        final String reducedDelimiters = "[\\s\\.,>]";

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

            nr1 = scanner.nextInt();
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
                    //System.err.println("setS"+i+"("+a+")="+b);
                }
            }
            for (int i = 0; i < 2; i++) {
                final BitSet seen = new BitSet();
                for (int a = 1; a <= size; a = nextOrbit(i, i + 1, a, seen)) {
                    int m = scanner.nextInt();
                    setMij(i, i + 1, a, m);
                    //System.err.println("setM"+i+""+(i+1)+"("+a+")="+m);
                }
            }
            for (int a = 1; a <= size; a++) {
                setM02(a, 2);
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

    public interface OrbitVisitor {
        void visit(int a);
    }
}