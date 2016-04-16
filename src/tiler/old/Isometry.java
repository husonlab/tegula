package tiler.old;

/**
 * isometry
 * Created by huson on 3/29/16.
 */
public class Isometry {
    private final double coor[][] = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

    public Isometry() {
    }

    public Isometry(Geom geom, APoint b, APoint a, boolean s) {
        double[] v;

        switch (geom) {
            case NT_EUCLIDEAN: {
                v = APoint.difference(b.locvec(), a.locvec());
                Isometry T = new Isometry();
                Isometry R = new Isometry();

                T.coor[0][2] = a.locvec(0);
                T.coor[1][2] = a.locvec(1);

                double cosR, sinR, l;
                l = Math.sqrt(APoint.dot(v, v));
                sinR = v[0] / l;
                cosR = v[1] / l;

                R.coor[0][1] = sinR;
                R.coor[1][1] = cosR;
                if (s) {
                    R.coor[0][0] = -cosR;
                    R.coor[1][0] = sinR;
                } else {
                    R.coor[0][0] = cosR;
                    R.coor[1][0] = -sinR;
                }
                setCoordinates(Isometry.multiply(T, R));
                break;
            }
            case NT_SPHERICAL:
            case NT_HYPERBOLIC: {
                double p = APoint.dot(a, b);
                double q = geom.sign() * (1 - p * p);

                if (q > 0.0)              //  if ( a.check() && b.check() )
                {
                    q = 1.0 / Math.sqrt(q);

                    v = APoint.vectorProduct(geom, b.locvec(), a.locvec());
                    double[] bv = APoint.difference(b.locvec(), APoint.multiplyBy(a.locvec(), geom.sign()));

                    if (s) {
                        v = APoint.multiplyBy(v, -q);
                    } else {
                        v = APoint.multiplyBy(v, q);
                    }
                    bv = APoint.multiplyBy(bv, q);

                    for (int i = 0; i < 3; i++) {
                        coor[i][0] = v[i];
                        coor[i][1] = bv[i];
                        coor[i][2] = a.locvec(i);
                    }
                } else for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        coor[i][j] = 0.0;
                break;
            }
        }
    }

    /**
     * copy the coordinates from a
     *
     * @param a
     */
    public void setCoordinates(Isometry a) {
        for (int i = 0; i < 3; i++)
            System.arraycopy(a.coor[i], 0, coor[i], 0, 3);
    }

    /**
     * get the transposed isometry
     *
     * @return transposed
     */
    public Isometry transpose() {
        Isometry a = new Isometry();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                a.coor[j][i] = coor[i][j];
        return a;
    }

    /**
     * multiply
     *
     * @param a
     * @param b
     * @return a*b
     */
    public static Isometry multiply(Isometry a, Isometry b) {
        final Isometry c = new Isometry();

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                c.coor[i][j] = a.coor[i][0] * b.coor[0][j]
                        + a.coor[i][1] * b.coor[1][j]
                        + a.coor[i][2] * b.coor[2][j];
        return c;
    }

    /**
     * multiplication
     *
     * @param a
     * @param p
     * @return a*p
     */
    public static APoint multiply(Isometry a, APoint p) {
        final APoint q = new APoint();

        for (int i = 0; i < 3; i++)
            for (int k = 0; k < 3; k++)
                q.locvec()[i] += a.coor[i][k] * p.locvec(k);
        return q;
    }
}
