package tiling;

import core.dsymbols.DSymbol;

/**
 * a tiling
 * Created by huson on 4/11/16.
 */
public class Tiling {
    private final DSymbol ds;
    private final String groupName;

    private final int[] flag2vert;
    private final int[] flag2edge;
    private final int[] flag2tile;

    private final int[] vert2flag;
    private final int[] edge2flag;
    private final int[] tile2flag;

    private final int nr1;
    private final int nr2;

    private final int numbVert;
    private final int numbEdge;
    private final int numbTile;


    /**
     * constructor
     *
     * @param ds
     * @param groupName
     */
    public Tiling(DSymbol ds, String groupName) {
        this.ds = ds;
        this.groupName = groupName;

        this.nr1 = ds.getNr1();
        this.nr2 = ds.getNr2();

        {
            flag2vert = new int[ds.size() + 1];
            numbVert = ds.countOrbits(1, 2);
            vert2flag = new int[numbVert];

            int count = 1;
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(1, 2, a, flag2vert, count++)) // this also sets flag2vert
                vert2flag[count] = a;

        }
        {
            flag2edge = new int[ds.size() + 1];
            numbEdge = ds.countOrbits(0, 2);
            edge2flag = new int[numbEdge];

            int count = 1;
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(0, 2, a, flag2edge, count++)) // this also sets flag2vert
                edge2flag[count] = a;

        }
        {
            flag2tile = new int[ds.size() + 1];
            numbTile = ds.countOrbits(0, 1);
            tile2flag = new int[numbTile];

            int count = 1;
            for (int a = 1; a <= ds.size(); a = ds.nextOrbit(0, 1, a, flag2tile, count++)) // this also sets flag2vert
                tile2flag[count] = a;

        }
    }
}
