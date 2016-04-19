package tiler.tiling;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.OrbifoldGroupName;

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

    private final int numbVert;
    private final int numbEdge;
    private final int numbTile;


    /**
     * constructor
     *
     * @param ds
     */
    public Tiling(DSymbol ds) {
        this.ds = ds;
        this.groupName = OrbifoldGroupName.getGroupName(ds);

        flag2vert = new int[ds.size() + 1];
        numbVert = ds.countOrbits(1, 2);
        vert2flag = new int[numbVert + 1];

        for (int a = 1, count = 1; a <= ds.size(); a = ds.nextOrbit(1, 2, a, flag2vert, count++)) // this also sets flag2vert
            vert2flag[count] = a;
        flag2edge = new int[ds.size() + 1];
        numbEdge = ds.countOrbits(0, 2);
        edge2flag = new int[numbEdge + 1];

        for (int a = 1, count = 1; a <= ds.size(); a = ds.nextOrbit(0, 2, a, flag2edge, count++)) // this also sets flag2vert
            edge2flag[count] = a;
        flag2tile = new int[ds.size() + 1];
        numbTile = ds.countOrbits(0, 1);
        tile2flag = new int[numbTile + 1];

        for (int a = 1, count = 1; a <= ds.size(); a = ds.nextOrbit(0, 1, a, flag2tile, count++)) // this also sets flag2vert
            tile2flag[count] = a;
    }

    /**
     * create the set of tiles to be shown
     *
     * @param radiusToCover radius to cover
     * @return tiles
     */
    public Group createTiling(double radiusToCover) {
        final FDomain fDomain = new FDomain(ds);

        final Group group = new Group();

        // add a huge sphere for debugging:
        if (fDomain.getGeometry() == FDomain.Geometry.Spherical) {
            Sphere sphere = new Sphere(100);
            sphere.setDrawMode(DrawMode.LINE);
            group.getChildren().add(sphere);
        } else if (fDomain.getGeometry() == FDomain.Geometry.Hyperbolic) {
            Circle circle = new Circle(100);
            circle.getTransforms().add(new Translate(0, 0, 100));
            circle.setStroke(Color.DARKGREY);
            circle.setFill(Color.TRANSPARENT);
            group.getChildren().add(circle);
        }

        final Group fund = FundamentalDomain.buildFundamentalDomain(ds, fDomain);
        group.getChildren().addAll(fund);

        if (false) {
            Transform transform = EuclideanGeometry.createTransform(fDomain.getVertex3D(0, 15), fDomain.getVertex3D(1, 15), fDomain.getVertex3D(0, 5), fDomain.getVertex3D(1, 5), true);
            Group group2 = FundamentalDomain.buildFundamentalDomain(ds, fDomain);
            group2.getTransforms().add(transform);
            group.getChildren().add(group2);
        }

        if (false) {
            Transform transform = EuclideanGeometry.createTransform(fDomain.getVertex3D(0, 7), fDomain.getVertex3D(2, 7), fDomain.getVertex3D(0, 8), fDomain.getVertex3D(2, 8), true);
            Group group2 = FundamentalDomain.buildFundamentalDomain(ds, fDomain);
            group2.getTransforms().add(transform);
            group.getChildren().add(group2);
        }

        if (false) {
            Transform transform = EuclideanGeometry.createTransform(fDomain.getVertex3D(0, 15), fDomain.getVertex3D(1, 15), fDomain.getVertex3D(0, 5),
                    fDomain.getVertex3D(1, 5), true);
            Group group2 = FundamentalDomain.buildFundamentalDomain(ds, fDomain);
            group2.getTransforms().add(transform);
            group.getChildren().add(group2);
        }

        if (false) {
            Transform transform = EuclideanGeometry.createTransform(fDomain.getVertex3D(0, 7), fDomain.getVertex3D(2, 7), fDomain.getVertex3D(0, 8), fDomain.getVertex3D(2, 8), true);
            Group group2 = FundamentalDomain.buildFundamentalDomain(ds, fDomain);
            group2.getTransforms().add(transform);
            group.getChildren().add(group2);
        }

        return group;
    }
}
