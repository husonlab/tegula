/*
 * DSymbolCode.java Copyright (C) 2026 Daniel H. Huson
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * a self-contained name for a Delaney symbol
 * <p>
 * A code such as <code>DS07-3R55-E1RJ-G</code> holds the whole tiling. Unlike a hash it can be turned
 * back into the symbol it names, so it needs no catalog, no lookup and no registry, it cannot collide
 * with the code of a different tiling, and it names tilings that have never been enumerated. Isomorphic
 * symbols get the same code, because the symbol is canonicalized before it is written.
 * <p>
 * <b>Specification.</b> To compute the code of a Delaney symbol:
 * <ol>
 * <li>Replace the symbol by its canonical form, see {@link DSymbolAlgorithms#canonicalForm}. Let n be its size.</li>
 * <li>Write the size: six bits holding n, or, if n is 63 or more, six bits of 1 followed by gamma(n).</li>
 * <li>For i = 0, 1, 2 in turn, keep the list of flags 1..n that are not yet paired under s_i, in increasing
 * order. While that list is not empty, remove its first element a and let b = s_i(a). If b equals a, write
 * rank 0. Otherwise write rank 1 + p, where p is the position of b in what remains of the list, and remove
 * b from the list as well.</li>
 * <li>For (i,j) = (0,1) and then (1,2), visit the (i,j)-orbits in increasing order of their smallest flag
 * and write m_ij for each. m_02 is always 2 and is not written.</li>
 * <li>Pad with zero bits to a multiple of five and write the bits in Crockford base 32,
 * that is, the alphabet 0123456789ABCDEFGHJKMNPQRSTVWXYZ, which omits I, L, O and U.</li>
 * </ol>
 * where the three variable-length codes are
 * <pre>
 *   gamma(v), v &gt;= 1   floor(log2 v) zero bits, then v itself in floor(log2 v) + 1 bits
 *   rank r             r = 1: "0";  r = 0: "10";  r &gt;= 2: "11" then gamma(r - 1)
 *   m value            3: "00";  4: "01";  5: "100";  6: "101";  otherwise "11" then gamma(m)
 * </pre>
 * The rank and m codes are fitted to what Delaney symbols actually look like once canonicalized: because
 * the canonical labeling numbers the flags of a tile consecutively, s_i(a) is nearly always the very next
 * unpaired flag, and 83% of ranks are 0 or 1; and 62% of m values are 3 or 4.
 * <p>
 * <b>Display form.</b> "DS", the size in at least two digits, then the base-32 characters in groups of
 * four, all separated by hyphens: <code>DS18-9ANA-NANA-M040-0ANA-K14</code>. The size is already in the
 * bits, so the prefix is for people rather than for the decoder. Reading a code ignores case, treats I
 * and L as 1 and O as 0, and drops hyphens, so it survives being written down by hand. A payload may
 * itself begin with DS and digits, so where the prefix ends is decided by trying each reading and
 * keeping the one whose declared size is the size that actually decodes.
 * <p>
 * A typical code runs to 9 characters for a symbol of size 1, 28 for size 18 and 38 for size 24. Every
 * character is in the QR alphanumeric set, so a code packs into a QR code at 5.5 bits per character.
 * <p>
 * Daniel Huson, 8.2026
 */
public class DSymbolCode {
    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    /**
     * the code of a Delaney symbol, in display form
     *
     * @return code, for example DS07-3R55-E1RJ-G
     */
    public static String encode(DSymbol ds) {
        final String payload = encodeBare(ds);
        final StringBuilder buf = new StringBuilder(String.format("DS%02d", ds.size()));
        for (int i = 0; i < payload.length(); i += 4)
            buf.append("-").append(payload, i, Math.min(i + 4, payload.length()));
        return buf.toString();
    }

    /**
     * the code of a Delaney symbol, without the prefix and the grouping
     *
     * @return code
     */
    public static String encodeBare(DSymbol ds) {
        final DSymbol canonical = DSymbolAlgorithms.canonicalForm(ds);
        final int size = canonical.size();
        final BitWriter out = new BitWriter();

        if (size < 63)
            out.bits(size, 6);
        else {
            out.bits(63, 6);
            out.gamma(size);
        }

        for (int i = 0; i <= 2; i++) {
            final List<Integer> free = new ArrayList<>();
            for (int a = 1; a <= size; a++)
                free.add(a);
            while (!free.isEmpty()) {
                final int a = free.remove(0);
                final int b = canonical.getSi(i, a);
                if (b == a)
                    out.rank(0);
                else {
                    out.rank(1 + free.indexOf(b));
                    free.remove(Integer.valueOf(b));
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            final BitSet seen = new BitSet();
            for (int a = 1; a <= size; a = canonical.nextOrbit(i, i + 1, a, seen))
                out.mValue(canonical.getMij(i, i + 1, a));
        }
        return out.done();
    }

    /**
     * the Delaney symbol named by a code. The symbol number is set to 0.0
     * <p>
     * The code is checked by encoding the result again and comparing, so a code that has been mistyped or
     * that does not name a canonical symbol is rejected rather than silently yielding the wrong tiling
     *
     * @return symbol
     * @throws IllegalArgumentException if the code is not a well-formed name
     */
    public static DSymbol decode(String code) {
        final String cleaned = code.trim().toUpperCase().replace(" ", "");
        IllegalArgumentException failure = null;

        // In display form a code begins with DS and the size. Where the prefix ends is not obvious: the
        // payload may itself begin with DS and digits, and the hyphen after the prefix may have been
        // dropped. So try each reading and keep the one whose declared size is the size that decodes
        if (cleaned.startsWith("DS")) {
            int end = 2;
            while (end < cleaned.length() && Character.isDigit(cleaned.charAt(end)))
                end++;
            for (int split = 3; split <= end; split++) {
                String rest = cleaned.substring(split);
                if (rest.startsWith("-"))
                    rest = rest.substring(1);
                try {
                    final DSymbol ds = decodePayload(payloadOf(rest), code);
                    if (ds.size() == Integer.parseInt(cleaned.substring(2, split)))
                        return ds;
                } catch (IllegalArgumentException ex) {
                    if (failure == null)
                        failure = ex;
                }
            }
        }

        try {
            return decodePayload(payloadOf(cleaned), code);
        } catch (IllegalArgumentException ex) {
            throw failure != null ? failure : ex;
        }
    }

    private static DSymbol decodePayload(String payload, String code) {
        final BitReader in = new BitReader(payload);
        final DSymbol result;
        try {
            int size = in.bits(6);
            if (size == 63)
                size = in.gamma();
            result = new DSymbol(size);

            for (int i = 0; i <= 2; i++) {
                final List<Integer> free = new ArrayList<>();
                for (int a = 1; a <= size; a++)
                    free.add(a);
                while (!free.isEmpty()) {
                    final int a = free.remove(0);
                    final int rank = in.rank();
                    if (rank == 0)
                        result.setSi(i, a, a);
                    else {
                        if (rank - 1 >= free.size())
                            throw new IllegalArgumentException("rank out of range: " + rank);
                        result.setSi(i, a, free.remove(rank - 1));
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                final BitSet seen = new BitSet();
                for (int a = 1; a <= size; a = result.nextOrbit(i, i + 1, a, seen)) {
                    final int m = in.mValue();
                    if (m < 1)
                        throw new IllegalArgumentException("illegal m value: " + m);
                    result.setMij(i, i + 1, a, m);
                }
            }
            for (int a = 1; a <= size; a++)
                result.setMatrixIJ(0, 2, a, 2);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Not a Delaney symbol code: " + code + ": " + ex.getMessage());
        }

        if (!encodeBare(result).equals(payload))
            throw new IllegalArgumentException("Not a Delaney symbol code: " + code + ": does not name a canonical symbol");
        return result;
    }


    /**
     * is this a well-formed code?
     *
     * @return true, if {@link #decode} would succeed
     */
    public static boolean isValid(String code) {
        try {
            decode(code);
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            return false;
        }
    }

    /**
     * drops the hyphens and applies the Crockford readings of I, L and O
     *
     * @return payload
     */
    private static String payloadOf(String text) {
        return text.replace("-", "").replace('I', '1').replace('L', '1').replace('O', '0');
    }

    /**
     * writes a bit stream as Crockford base 32
     */
    private static class BitWriter {
        private final StringBuilder out = new StringBuilder();
        private int accumulator = 0;
        private int bits = 0;

        void bit(int b) {
            accumulator = (accumulator << 1) | (b & 1);
            if (++bits == 5) {
                out.append(ALPHABET.charAt(accumulator));
                accumulator = 0;
                bits = 0;
            }
        }

        void bits(int value, int width) {
            for (int i = width - 1; i >= 0; i--)
                bit((value >> i) & 1);
        }

        void gamma(int value) {
            final int leading = 31 - Integer.numberOfLeadingZeros(value);
            for (int i = 0; i < leading; i++)
                bit(0);
            for (int i = leading; i >= 0; i--)
                bit((value >> i) & 1);
        }

        void rank(int rank) {
            if (rank == 1)
                bit(0);
            else if (rank == 0) {
                bit(1);
                bit(0);
            } else {
                bit(1);
                bit(1);
                gamma(rank - 1);
            }
        }

        void mValue(int m) {
            switch (m) {
                case 3 -> bits(0, 2);
                case 4 -> bits(1, 2);
                case 5 -> bits(4, 3);
                case 6 -> bits(5, 3);
                default -> {
                    bits(3, 2);
                    gamma(m);
                }
            }
        }

        String done() {
            while (bits != 0)
                bit(0);
            return out.toString();
        }
    }

    /**
     * reads a bit stream from Crockford base 32
     */
    private static class BitReader {
        private final String text;
        private int position = 0;

        BitReader(String text) {
            this.text = text;
        }

        int bit() {
            if (position / 5 >= text.length())
                throw new IllegalArgumentException("code is too short");
            final int value = ALPHABET.indexOf(text.charAt(position / 5));
            if (value == -1)
                throw new IllegalArgumentException("illegal character: " + text.charAt(position / 5));
            final int b = (value >> (4 - position % 5)) & 1;
            position++;
            return b;
        }

        int bits(int width) {
            int value = 0;
            for (int i = 0; i < width; i++)
                value = (value << 1) | bit();
            return value;
        }

        int gamma() {
            int leading = 0;
            while (bit() == 0) {
                if (++leading > 31)
                    throw new IllegalArgumentException("run-away number");
            }
            int value = 1;
            for (int i = 0; i < leading; i++)
                value = (value << 1) | bit();
            return value;
        }

        int rank() {
            if (bit() == 0)
                return 1;
            if (bit() == 0)
                return 0;
            return gamma() + 1;
        }

        int mValue() {
            if (bit() == 0)
                return bit() == 0 ? 3 : 4;
            if (bit() == 0)
                return bit() == 0 ? 5 : 6;
            return gamma();
        }
    }
}
