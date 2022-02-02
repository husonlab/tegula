/*
 * Rational.java Copyright (C) 2022 Daniel H. Huson
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

/**
 * rational  numbers
 * Daniel Huson, 1.2019
 */
public class Rational implements Comparable<Rational> {
    private final long n;
    private final long d;
    public final static Rational ZERO = new Rational(0);
    public final static Rational ONE = new Rational(1);


    /**
     * constructor
     *
     * @param n number
     */
    public Rational(long n) {
        this.n = n;
        this.d = 1;
    }

    /**
     * constructor
     *
	 */
    public Rational(long n, long d) {
        if (d <= 0)
            throw new IllegalArgumentException("divisor must be positive: " + d);
        if (n == 0) {
            d = 1;
        } else {
            final long g = gcd(n, d);
            if (g > 1) {
                n /= g;
                d /= g;
            }
        }
        this.n = n;
        this.d = d;
    }

    private long gcd(long a, long b) {
        if (a < 0)
            a = -a;
        if (b < 0)
            b = -b;
        if (b > a) {
            long c = a;
            a = b;
            b = c;
        }

        long r = (a % b);
        while (r != 0) {
            a = b;
            b = r;
            r = (a % b);
        }
        return b;
    }

    public boolean equals(Rational other) {
        return n * other.d == d * other.n;
    }

    public int compareTo(Rational other) {
        return Long.compare(n * other.d, d * other.n);
    }

    public Rational add(Rational other) {
        return new Rational(n * other.d + d * other.n, d * other.d);
    }

    public Rational add(int n, int d) {
        return new Rational(this.n * d + this.d * n, this.d * d);
    }

    public Rational subtract(Rational other) {
        return new Rational(n * other.d - d * other.n, d * other.d);
    }

    public Rational subtract(int n, int d) {
        return new Rational(this.n * d - this.d * n, this.d * d);
    }

    public Rational multiply(Rational other) {
        final Rational a = new Rational(n, other.d);
        final Rational b = new Rational(other.n, d);
        return new Rational(a.n * b.n, a.d * b.d);
    }

    public Rational divide(Rational other) {
        if (other.n <= 0)
            throw new IllegalArgumentException("divide by 0");

        final Rational a = new Rational(n, other.n);
        final Rational b = new Rational(other.d, d);
        return new Rational(a.n * b.n, a.d * b.d);
    }

    public Rational inverse() {
        if (n <= 0)
            throw new IllegalArgumentException("inverse of 0");
        return new Rational(d, n);
    }

    public double doubleValue() {
        return (double) n / (double) d;
    }

    public double floatValue() {
        return (float) n / (float) d;
    }


    public long num() {
        return n;
    }

    public long denom() {
        return d;
    }

    public int sign() {
        return compareTo(ZERO);
    }

    public String toString() {
        if (n == 0)
            return "0";
        else
            return String.format("%d/%d", n, d);
    }

}
