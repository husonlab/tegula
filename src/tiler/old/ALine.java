package tiler.old;

/**
 * a line
 * Created by huson on 3/29/16.
 */
public class ALine {
    private final double[] normvec = new double[3];

    /**
     * construct an empty line
     */
    public ALine() {
    }

    /**
     * construct a line for two points
     *
     * @param a
     * @param b
     */
    public ALine(Geom geom, APoint a, APoint b) {
        double d;

        switch (geom) {
            case NT_EUCLIDEAN:
                d = APoint.distance(geom, a, b);
                if (d > 0) {
                    normvec[0] = (a.locvec(1) - b.locvec(1)) / d;
                    normvec[1] = (-a.locvec(0) + b.locvec(0)) / d;
                    normvec[2] = (a.locvec(0) * b.locvec(1) - a.locvec(1) * b.locvec(0)) / d;
                }
                break;
            case NT_SPHERICAL:
            case NT_HYPERBOLIC:
                d = APoint.dot(a, b);
                d = (1 - d * d) * geom.sign();
                if (d > 0) {
                    APoint v = APoint.vectorProduct(geom, a, b);
                    normvec[0] = v.locvec(0) / Math.sqrt(d);
                    normvec[1] = v.locvec(1) / Math.sqrt(d);
                    normvec[2] = v.locvec(2) / Math.sqrt(d);
                }
                break;
        }
    }

    public double[] normvec() {
        return normvec;
    }

    public double normvec(int i) {
        return normvec[i];
    }

    public static boolean equal(ALine aLine, ALine bLine) {
        return (aLine.normvec == bLine.normvec) || (APoint.equal(aLine.normvec, APoint.multiplyBy(bLine.normvec, -1)));
    }

    public static boolean parallel(Geom geom, ALine aLine, ALine bLine) {
        boolean result;

        switch (geom) {
            default:
            case NT_EUCLIDEAN: {
                double d = (bLine.normvec(0) * aLine.normvec(1) - bLine.normvec(1) * aLine.normvec(0));
                result = (d * d <= Geometry.maxDiffForEquals);
                break;
            }
            case NT_SPHERICAL: {
                result = false;
                break;
            }
            case NT_HYPERBOLIC: {
                double d = APoint.dot(aLine.normvec, bLine.normvec);
                result = (d * d >= 1);
                break;
            }
        }
        return result;
    }

    /**
     * get point in intersection of two lines
     *
     * @param aLine
     * @param bLine
     * @return intersection point
     */
    public static APoint intersection(Geom geom, ALine aLine, ALine bLine) {
        double d;

        switch (geom) {
            default:
            case NT_EUCLIDEAN: {
                d = bLine.normvec(0) * aLine.normvec(1) - bLine.normvec(1) * aLine.normvec(0);
                if (d != 0) d = 1 / d;
                break;
            }
            case NT_SPHERICAL:
            case NT_HYPERBOLIC: {
                d = APoint.dot(aLine.normvec, bLine.normvec);
                d = 1 - d * d;
                if (d > 0) {
                    d = 1 / Math.sqrt(d);
                } else {
                    d = 0;
                }
                break;
            }
        }
        double[] vec = APoint.vectorProduct(geom, aLine.normvec, bLine.normvec);
        APoint p = new APoint(vec[0], vec[1], vec[2]);
        p = p.multiplyBy(d);
        if (p.locvec(2) < 0) {
            p.multiplyBy(-1);
        }
        return p;
    }
}
