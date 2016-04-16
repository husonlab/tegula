package tiler.old;

/**
 * A point
 * Created by huson on 3/29/16.
 */
public class APoint {
    private final double[] locvec = new double[3];

    public APoint() {
    }

    public APoint(Geom geom, double x, double y) {
        final double d = x * x + y * y;

        switch (geom) {
            default:
            case NT_EUCLIDEAN:
                locvec[0] = x;
                locvec[1] = y;
                locvec[2] = 1.0;
                break;
            case NT_SPHERICAL:
                locvec[0] = 2 * x / (1 + d);
                locvec[1] = 2 * y / (1 + d);
                locvec[2] = (d - 1) / (1 + d);
                break;
            case NT_HYPERBOLIC:
                if (d < 1) {
                    locvec[0] = 2 * x / (1 - d);
                    locvec[1] = 2 * y / (1 - d);
                    locvec[2] = (1 + d) / (1 - d);
                } else {
                    locvec[0] = 0.0;
                    locvec[1] = 0.0;
                    locvec[2] = 0.0;
                }
                break;
        }
    }

    public APoint(double x, double y, double z) {
        locvec[0] = x;
        locvec[1] = y;
        locvec[2] = z;
    }

    public APoint(double[] locvec) {
        this.locvec[0] = locvec[0];
        this.locvec[1] = locvec[1];
        this.locvec[2] = locvec[2];
    }

    public double[] coordinates(Geom geom) {
        double q = 1.0 - geom.sign() * locvec[2];
        if (q != 0.0) {
            return new double[]{locvec[0] / q, locvec[1] / q};
        } else
            return new double[2];
    }

    public double[] locvec() {
        return locvec;
    }


    public double locvec(int i) {
        return locvec[i];
    }

    public void locvec(double[] a) {
        locvec[0] = a[0];
        locvec[1] = a[1];
        locvec[2] = a[2];
    }

    /**
     * reflect this point over line
     *
     * @param line
     * @return reflected point
     */
    public APoint reflect(Geom geom, ALine line) {
        double d = dot(line.normvec(), locvec);
        APoint p = new APoint(add(multiplyBy(line.normvec(), (-2 * d)), locvec));

        if (geom == Geom.NT_EUCLIDEAN)
            p.locvec[2] = locvec(2);
        return p;
    }

    /**
     * drop the point onto the given line
     *
     * @param aLine
     * @return
     */
    public APoint drop(Geom geom, ALine aLine) {
        double d = dot(aLine.normvec(), locvec);
        double[] vec = difference(locvec, multiplyBy(aLine.normvec(), d));
        APoint p = new APoint(vec[0], vec[1], vec[2]);
        switch (geom) {
            case NT_EUCLIDEAN:
                p.locvec[2] = locvec[2];
                break;
            case NT_SPHERICAL:
            case NT_HYPERBOLIC:
                d = 1 - geom.sign() * d * d;
                if (d <= 0)
                    p = new APoint();
                else
                    p.locvec(APoint.multiplyBy(p.locvec, 1 / Math.sqrt(d)));
                break;
        }
        return p;
    }


    public APoint multiplyBy(double factor) {
        double[] values = multiplyBy(locvec, factor);
        return new APoint(values[0], values[1], values[2]);
    }

    public static double[] difference(double[] a, double b[]) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    public static double[] multiplyBy(double[] vec, double factor) {
        return new double[]{vec[0] *= factor, vec[1] *= factor, vec[2] *= factor};
    }

    public static double dot(APoint a, APoint b) {
        return dot(a.locvec, b.locvec);
    }

    public static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    public static double[] add(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    public static APoint vectorProduct(Geom geom, APoint v, APoint w) {
        double[] vec = vectorProduct(geom, v.locvec, w.locvec);
        return new APoint(vec[0], vec[1], vec[2]);
    }

    public static double[] vectorProduct(Geom geom, double[] v, double[] w) {
        int factor = (geom == Geom.NT_EUCLIDEAN ? Geom.NT_SPHERICAL.sign() : geom.sign());
        return new double[]{v[1] * w[2] - v[2] * w[1], v[2] * w[0] - v[0] * w[2], (v[0] * w[1] - v[1] * w[0]) * factor};
    }

    public static boolean equal(APoint a, APoint b) {
        return equal(a.locvec, b.locvec);
    }

    public static boolean equal(double[] a, double[] b) {
        return (Math.abs(a[0] - b[0]) < Geometry.maxDiffForEquals)
                && (Math.abs(a[1] - b[1]) < Geometry.maxDiffForEquals)
                && (Math.abs(a[2] - b[2]) < Geometry.maxDiffForEquals);
    }


    /**
     * distance
     *
     * @param p
     * @param q
     * @return distance
     */
    public static double distance(Geom geom, APoint p, APoint q) {

        double d = 0;

        switch (geom) {
            case NT_EUCLIDEAN: {
                d = Math.sqrt((p.locvec[0] - q.locvec[0]) * (p.locvec[0] - q.locvec[0])
                        + (p.locvec[1] - q.locvec[1]) * (p.locvec[1] - q.locvec[1]));
                break;
            }
            case NT_SPHERICAL: {
                double temp = dot(p, q);
                if ((-1 <= temp) && (temp <= 1)) d = Math.acos(temp);
                break;
            }
            case NT_HYPERBOLIC: {
                double temp = dot(p, q);
                if (temp <= -1) d = Math.log(Math.sqrt(temp * temp - 1) - temp);       // =arcosh(-temp)
                break;
            }
        }
        return d;
    }

    /**
     * compute middle point
     *
     * @param p
     * @param q
     * @return middle
     */
    public static APoint middle(Geom geom, APoint p, APoint q) {
        double d;

        switch (geom) {
            default:
            case NT_EUCLIDEAN:
                d = 0.5;
                break;
            case NT_SPHERICAL:
            case NT_HYPERBOLIC:
                d = 2 * (1 + geom.sign() * dot(p, q));
                if (d <= 0) d = 0;
                else d = 1 / Math.sqrt(d);
                break;
        }
        return new APoint(d * (p.locvec[0] + q.locvec[0]), d * (p.locvec[1] + q.locvec[1]), d * (p.locvec[2] + q.locvec[2]));
    }
}
