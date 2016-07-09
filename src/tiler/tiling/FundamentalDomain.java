package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;

import java.util.BitSet;
import java.util.Random;

/**
 * builds fundamental domain in JavaFX
 * Created by huson on 4/5/16.
 */
public class FundamentalDomain {
    /**
     * construct a fundamental domain
     *
     * @param dsymbol Delaney symbol from old DH code
     * @param fDomain domain computed by KW
     * @return fundamental domain
     */
    public static Group buildFundamentalDomain(final DSymbol dsymbol, final FDomain fDomain) {
        final Group group = new Group();

        final Color[] colors = new Color[fDomain.size() + 1];

        final BitSet set = new BitSet();
        final Random random = new Random(666);
        // set colors
        for (int a = 1; a <= dsymbol.size(); a = dsymbol.nextOrbit(0, 1, a, set)) {
            final Color color = new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
            dsymbol.visitOrbit(0, 1, a, new DSymbol.OrbitVisitor() {
                public void visit(int a) {
                    colors[a] = color;
                }
            });
        }

        // construct triangles as meshes:

        for (int a = 1; a <= fDomain.size(); a++) {
            final float[] points = new float[21];
            int p = 0;

            // chamber vertices:
            for (int i = 0; i <= 2; i++) { // points 0, 1, 2
                final Point3D apt = fDomain.getVertex3D(i, a);
                points[p++] = (float) apt.getX();
                points[p++] = (float) apt.getY();
                points[p++] = (float) apt.getZ();
            }
            // chamber edge centers:
            for (int i = 0; i <= 2; i++) { // points 3, 4, 5
                final Point3D apt = fDomain.getEdgeCenter3D(i, a);
                points[p++] = (float) apt.getX();
                points[p++] = (float) apt.getY();
                points[p++] = (float) apt.getZ();
            }
            // chamber center:
            { // point 6
                final Point3D apt = fDomain.getChamberCenter3D(a);
                points[p++] = (float) apt.getX();
                points[p++] = (float) apt.getY();
                points[p] = (float) apt.getZ();
            }
            final float[] texCoords = {0.5f, 0, 0, 1, 1, 1};

            final int[] faces;
            final int[] smoothing;

            if (true) { // two sided mesh:
                faces = new int[]{
                        0, 0, 6, 1, 5, 2, // v0 cc e2
                        1, 0, 5, 1, 6, 2, // v1 e2 cc
                        1, 0, 6, 1, 3, 2, // v1 cc e0
                        2, 0, 3, 0, 6, 2, // v2 e0 cc
                        2, 0, 6, 1, 4, 2, // v2 cc e1
                        0, 0, 4, 1, 6, 2,  // v0 e1 cc

                       0, 0, 5, 1, 6, 2, // v0 cc e2
                        1, 0, 6, 1, 5, 2, // v1 e2 cc
                        1, 0, 3, 1, 6, 2, // v1 cc e0
                        2, 0, 6, 0, 3, 2, // v2 e0 cc
                        2, 0, 4, 1, 6, 2, // v2 cc e1
                        0, 0, 6, 1, 4, 2  // v0 e1 cc

                };
                smoothing = new int[]{1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2};
            } else { // one sided mesh:
                if (fDomain.getOrientation(a) == 1) { //Todo: Condition wrong?
                    faces = new int[]{
                            0, 0, 6, 1, 5, 2, // v0 cc e2
                            1, 0, 5, 1, 6, 2, // v1 e2 cc
                            1, 0, 6, 1, 3, 2, // v1 cc e0
                            2, 0, 3, 0, 6, 2, // v2 e0 cc
                            2, 0, 6, 1, 4, 2, // v2 cc e1
                            0, 0, 4, 1, 6, 2  // v0 e1 cc

                    };
                } else {
                    faces = new int[]{0, 0, 5, 1, 6, 2,
                            1, 0, 6, 1, 5, 2,
                            1, 0, 3, 1, 6, 2,
                            2, 0, 6, 0, 3, 2,
                            2, 0, 4, 1, 6, 2,
                            0, 0, 6, 1, 4, 2
                    };
                }
                smoothing = new int[]{1, 1, 1, 1, 1, 1};
            }

            TriangleMesh mesh = new TriangleMesh();
            mesh.getPoints().addAll(points);
            mesh.getTexCoords().addAll(texCoords);
            mesh.getFaces().addAll(faces);
            mesh.getFaceSmoothingGroups().addAll(smoothing);
            MeshView meshView = new MeshView(mesh);
            //meshView.setDrawMode(DrawMode.LINE);
            meshView.setMaterial(new PhongMaterial(colors[a]));

            group.getChildren().add(meshView);
        }

        // add lines
        if (fDomain.getGeometry() == FDomain.Geometry.Euclidean) {
            for (int a = 1; a <= fDomain.size(); a++) {
                group.getChildren().add(makeLine(fDomain.getGeometry(), fDomain.getVertex3D(0, a), fDomain.getEdgeCenter3D(1, a), fDomain.getVertex3D(2, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));

                group.getChildren().add(makeLine(fDomain.getGeometry(), fDomain.getVertex3D(2, a), fDomain.getEdgeCenter3D(0, a), fDomain.getVertex3D(1, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));

                group.getChildren().add(makeLine(fDomain.getGeometry(), fDomain.getVertex3D(0, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(0, a), Color.WHITE.deriveColor(0, 1, 1, 0.2), 0.5f));
                group.getChildren().add(makeLine(fDomain.getGeometry(), fDomain.getVertex3D(1, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(1, a), Color.WHITE.deriveColor(0, 1, 1, 0.2), 0.5f));
                group.getChildren().add(makeLine(fDomain.getGeometry(), fDomain.getVertex3D(2, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(2, a), Color.WHITE.deriveColor(0, 1, 1, 0.2), 0.5f));

            }
            for (int a = 1; a <= fDomain.size(); a++) {
                final Point3D v0 = fDomain.getVertex3D(0, a);
                final Point3D e2 = fDomain.getEdgeCenter3D(2, a);
                final Point3D v1 = fDomain.getVertex3D(1, a);
                group.getChildren().add(makeLine(fDomain.getGeometry(), v0, e2, v1, Color.BLACK, 1));
            }
        }
        // add numbers:
        if (false)
        {
            for (int a = 1; a <= fDomain.size(); a++) {
                final Point3D apt = fDomain.getChamberCenter3D(a);
                Text label = new Text("" + a);
                label.setFont(Font.font(8));
                label.getTransforms().add(new Translate(apt.getX() - 4, apt.getY() + 4, apt.getZ()));

                label.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.4));
                group.getChildren().add(label);
            }
        }

        // add some points to debug transforms:

        if (false) {
                for (int i = 0; i < 3; i++) {
                    final Point3D a = fDomain.getVertex3D(i, 16);
                    final Sphere sphere = new Sphere(2);
                    switch (i) {
                        case 0:
                            sphere.setMaterial(new PhongMaterial(Color.GREEN));
                            break;
                        case 1:
                            sphere.setMaterial(new PhongMaterial(Color.YELLOW));
                            break;
                        case 2:
                            sphere.setMaterial(new PhongMaterial(Color.RED));
                            break;
                    }
                    sphere.getTransforms().add(new Translate(a.getX(), a.getY(), a.getZ()));
                    group.getChildren().add(sphere);
                }

            final Transform transform = Tiling.getTransform(fDomain.getGeometry(), fDomain.getVertex3D(0, 16), fDomain.getVertex3D(1, 16),
                    fDomain.getVertex3D(0, 19), fDomain.getVertex3D(1, 19), true);

                for (int i = 0; i < 3; i++) {
                    final Point3D a = fDomain.getVertex3D(i, 16);
                    final Sphere sphere = new Sphere(2);
                    sphere.getTransforms().addAll(transform, new Translate(a.getX(), a.getY(), a.getZ()));

                    switch (i) {
                        case 0:
                            sphere.setMaterial(new PhongMaterial(Color.LIGHTGREEN));
                            break;
                        case 1:
                            sphere.setMaterial(new PhongMaterial(Color.LIGHTYELLOW));
                            break;
                        case 2:
                            sphere.setMaterial(new PhongMaterial(Color.PINK));
                            break;

                    }
                    group.getChildren().add(sphere);
                }
        }
        return group;
    }

    /**
     * construct a line
     *
     * @param a
     * @param b
     * @param c
     * @param color
     * @param width
     * @return a line
     */
    private static Node makeLine(FDomain.Geometry geometry, Point3D a, Point3D b, Point3D c, Color color, float width) {
        if (geometry == FDomain.Geometry.Euclidean) {
            Polyline polyLine = new Polyline(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY());
            polyLine.setStroke(color);
            polyLine.setStrokeWidth(width);
            polyLine.setStrokeLineCap(StrokeLineCap.ROUND);
            return polyLine;
        } else //         // todo: implement for three-dimensions
            return null;
    }
}
