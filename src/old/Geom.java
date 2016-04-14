package old;

/**
 * three types of 2D geometry
 * Created by huson on 3/30/16.
 */
public enum Geom {
    NT_EUCLIDEAN,
    NT_SPHERICAL, NT_HYPERBOLIC;

    public int sign() {
        switch (this) {
            case NT_SPHERICAL:
                return 1;
            case NT_HYPERBOLIC:
                return -1;
            default:
            case NT_EUCLIDEAN:
                return 0;
        }
    }
}
