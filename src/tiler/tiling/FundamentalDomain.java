package tiler.tiling;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
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

        final int orientation = (computeWindingNumber(fDomain.getVertex3D(0, 1), fDomain.getVertex3D(1, 1), fDomain.getVertex3D(2, 1)) < 0 ? fDomain.getOrientation(1) :
                -fDomain.getOrientation(1));


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


            if (false) { // two sided mesh:
                faces = new int[]{
                        0, 0, 6, 1, 5, 2, // v0 cc e2
                        1, 0, 5, 1, 6, 2, // v1 e2 cc
                        1, 0, 6, 1, 3, 2, // v1 cc e0
                        2, 0, 3, 0, 6, 2, // v2 e0 cc
                        2, 0, 6, 1, 4, 2, // v2 cc e1
                        0, 0, 4, 1, 6, 2, // v0 e1 cc

                        0, 0, 5, 1, 6, 2, // v0 cc e2
                        1, 0, 6, 1, 5, 2, // v1 e2 cc
                        1, 0, 3, 1, 6, 2, // v1 cc e0
                        2, 0, 6, 0, 3, 2, // v2 e0 cc
                        2, 0, 4, 1, 6, 2, // v2 cc e1
                        0, 0, 6, 1, 4, 2  // v0 e1 cc

                };
                smoothing = new int[]{1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2};
            } else { // one sided mesh:
                if (fDomain.getOrientation(a) == orientation) {
                    faces = new int[]{
                            0, 0, 6, 1, 5, 2, // v0 cc e2
                            1, 0, 5, 1, 6, 2, // v1 e2 cc
                            1, 0, 6, 1, 3, 2, // v1 cc e0
                            2, 0, 3, 0, 6, 2, // v2 e0 cc
                            2, 0, 6, 1, 4, 2, // v2 cc e1
                            0, 0, 4, 1, 6, 2  // v0 e1 cc

                    };
                }
                else {
                    faces = new int[]{
                            0, 0, 5, 1, 6, 2,
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
            meshView.setMesh(mesh);
            meshView.setMaterial(new PhongMaterial(colors[a]));
            group.getChildren().addAll(meshView);
        }

        // Add lines
        if(true) {
            //Lines for barycentric subdivision of chambers:
            /*for (int a = 1; a <= fDomain.size(); a++) {
                group.getChildren().add(Cylinderline.createConnection(fDomain.getVertex3D(0, a), fDomain.getEdgeCenter3D(1, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
                group.getChildren().add(Cylinderline.createConnection(fDomain.getEdgeCenter3D(1, a), fDomain.getVertex3D(2, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));

                group.getChildren().add(Cylinderline.createConnection(fDomain.getVertex3D(2, a), fDomain.getEdgeCenter3D(0, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
                group.getChildren().add(Cylinderline.createConnection(fDomain.getEdgeCenter3D(0, a), fDomain.getVertex3D(1, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));

                group.getChildren().add(Cylinderline.createConnection(fDomain.getVertex3D(0, a), fDomain.getChamberCenter3D(a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
                group.getChildren().add(Cylinderline.createConnection(fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(0, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));

                group.getChildren().add(Cylinderline.createConnection(fDomain.getVertex3D(1, a), fDomain.getChamberCenter3D(a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
                group.getChildren().add(Cylinderline.createConnection(fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(1, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));

                group.getChildren().add(Cylinderline.createConnection(fDomain.getVertex3D(2, a), fDomain.getChamberCenter3D(a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
                group.getChildren().add(Cylinderline.createConnection(fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(2, a), Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
            }*/


            double width = 0;
            if (fDomain.getGeometry() == Geometry.Hyperbolic){
                Point3D refPoint = fDomain.getChamberCenter3D(1).multiply(0.01);
                Point3D origin = new Point3D(0, 0, 1);
                double w = 0.01;
                double h = (1+w*w)/(1-w*w);
                //Length of translation
                double t = Tools.distance(fDomain, refPoint, origin);
                // Affine translation:
                Affine translateT = new Affine(Math.cosh(t), Math.sinh(t), 0, Math.sinh(t), Math.cosh(t), 0); // Translation along x-axis
                Point2D x = translateT.transform(0,1);
                Point2D y = translateT.transform((1+h)*w, h);

                width = 100*(y.getX()/(1+y.getY())-x.getX()/(1+x.getY()));
            }
            else if (fDomain.getGeometry() == Geometry.Euclidean){
                width = 1;
            }
            else if (fDomain.getGeometry() == Geometry.Spherical){
                width = 0.5;
            }

            System.out.println("Width: " + width);

            // Vertices of Tiling:
            for (int a = 1; a <= fDomain.size(); a++) {
                final Point3D v0 = fDomain.getVertex3D(0, a);
                final Point3D e2 = fDomain.getEdgeCenter3D(2, a);
                final Point3D v1 = fDomain.getVertex3D(1, a);
                group.getChildren().add(Cylinderline.createConnection(v0, e2, Color.BLACK, width));
                group.getChildren().add(Cylinderline.createConnection(e2, v1, Color.BLACK, width));
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

    private static double computeWindingNumber(Point3D a0, Point3D a1, Point3D a2) {
        return (a1.getX() - a0.getX()) * (a1.getY() + a0.getY()) + (a2.getX() - a1.getX()) * (a2.getY() + a1.getY()) + (a0.getX() - a2.getX()) * (a0.getY() + a2.getY());
    }
}
