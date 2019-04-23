package tegula.tiling;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.core.fundamental.utils.WrapInt;
import tegula.main.TilingStyle;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Fundamental domain for tiling represented by meshes
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class FundamentalDomainMeshes {
    private final Group all = new Group();

    private final Group tiles = new Group();
    private final Group bands = new Group();
    private final Group chambers = new Group();
    private final Group decorations = new Group();
    private final Group handles = new Group();

    private final BooleanProperty includeTiles = new SimpleBooleanProperty(true);
    private final BooleanProperty includeBands = new SimpleBooleanProperty(true);
    private final BooleanProperty includeChambers = new SimpleBooleanProperty(true);
    private final BooleanProperty includeDecorations = new SimpleBooleanProperty(false);
    private final BooleanProperty includeHandles = new SimpleBooleanProperty(false);

    private final Shape3D reference = new Sphere(5);
    private final Group references = new Group();

    /**
     * constructor
     */
    public FundamentalDomainMeshes() {
        InvalidationListener listener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                all.getChildren().clear();
                if (isIncludeTiles())
                    all.getChildren().add(tiles);
                if (isIncludeBands())
                    all.getChildren().add(bands);
                if (isIncludeChambers())
                    all.getChildren().add(chambers);
                if (isIncludeDecorations())
                    all.getChildren().add(decorations);
                if (isIncludeHandles())
                    all.getChildren().add(handles);
            }
        };
        includeTilesProperty().addListener(listener);
        includeBandsProperty().addListener(listener);
        includeChambersProperty().addListener(listener);
        includeDecorationsProperty().addListener(listener);
        includeHandlesProperty().addListener(listener);
    }

    /**
     * clear
     */
    private void clear() {
        tiles.getChildren().clear();
        bands.getChildren().clear();
        chambers.getChildren().clear();
        decorations.getChildren().clear();
        handles.getChildren().clear();
    }

    /**
     * construct a fundamental domain as meshes
     *
     * @param dsymbol Delaney symbol from old DH code
     * @param fDomain domain computed by KW
     * @return fundamental domain
     */
    public void update(final DSymbol dsymbol, final FDomain fDomain, TilingStyle tilingStyle) {
        clear();

        chambers.getChildren().addAll(computeChambers(fDomain));
        chambers.getTransforms().setAll(new Translate());

        // set reference sphere:
        {
            final int referenceChamberIndex = fDomain.computeOptimalChamberIndex();
            final Point3D point = fDomain.getChamberCenter3D(referenceChamberIndex);
            reference.setTranslateX(point.getX());
            reference.setTranslateY(point.getY());
            reference.setTranslateZ(point.getZ());
            reference.setMaterial(new PhongMaterial(Color.RED.deriveColor(1, 1, 1, 0.5)));
            reference.setUserData("ref0");

            final Sphere reference1 = new Sphere(5);
            reference1.setTranslateX(point.getX() + 10);
            reference1.setTranslateY(point.getY());
            reference1.setTranslateZ(point.getZ());
            reference1.setUserData("ref1");
            reference1.setMaterial(new PhongMaterial(Color.LIGHTSEAGREEN));

            final Sphere reference2 = new Sphere(5);
            reference2.setTranslateX(point.getX());
            reference2.setTranslateY(point.getY() + 10);
            reference2.setTranslateZ(point.getZ());
            reference2.setUserData("ref2");
            reference2.setMaterial(new PhongMaterial(Color.GREENYELLOW));

            references.getChildren().setAll(reference, reference1, reference2);

        }

        // set colors
        final Color[] colors = new Color[fDomain.size() + 1];
        {
            int tileNumber = 0;
            final BitSet set = new BitSet();
            for (int a = 1; a <= dsymbol.size(); a = dsymbol.nextOrbit(0, 1, a, set)) {
                final Color color = tilingStyle.getTileColor(tileNumber++);
                dsymbol.visitOrbit(0, 1, a, b -> colors[b] = color);
            }
        }

        final Geometry geom = fDomain.getGeometry();

        // For bands and the band caps (i.e. circles at the ends of bands)
        final double bandWidth = (fDomain.getGeometry() == Geometry.Hyperbolic ? 0.01 : 1) * tilingStyle.getBandWidth(); // size of edges
        final Color bandColor = tilingStyle.getBandColor();

        final double bandCapDiameter = bandWidth;
        final int bandCapFineness = tilingStyle.getBandCapFineness(); // defines how smooth the edges are
        final Color bandCapColor = bandColor;

        double linesAbove; // defines the height of the line above the faces


        // Booleans
        boolean drawFaces = tilingStyle.isShowFaces();
        boolean drawBands = tilingStyle.isShowBands();
        boolean drawBandCaps = tilingStyle.isShowBands();

        // construct triangles as meshes:

        final int orientation = (computeWindingNumber(fDomain.getVertex3D(0, 1), fDomain.getVertex3D(1, 1),
                fDomain.getVertex3D(2, 1)) < 0 ? fDomain.getOrientation(1) : -fDomain.getOrientation(1));

        final BitSet flagAHasBand = new BitSet(fDomain.size());
        final BitSet flagAHasBandCaps = new BitSet(fDomain.size());

        for (int a = 1; a <= fDomain.size(); a++) {
            final float[] trianglePointsCoordinates;
            final Point3D[] trianglePoints3D; // points that create the triangles
            final Point3D[] bandPoints3D;
            final Point3D[] bandCapPoints3D;

            final int[] faces;

            switch (geom) {
                case Spherical: {

                    final int depth = tilingStyle.isSmoothEdges() ? 4 : 0; // 4^5 = 1024

                    faces = new int[(int) Math.pow(4, (depth + 1)) * 6];
                    trianglePoints3D = new Point3D[depth == 0 ? 6 : 1026]; // 3, 6, 66, 258, 1026 // size of points array dependent on depth

                    final WrapInt p = new WrapInt(0);
                    final WrapInt f = new WrapInt(0);

                    trianglePoints3D[p.incrementInt()] = fDomain.getVertex3D(0, a);
                    trianglePoints3D[p.incrementInt()] = fDomain.getVertex3D(1, a);
                    trianglePoints3D[p.incrementInt()] = fDomain.getVertex3D(2, a);
                    trianglePoints3D[p.incrementInt()] = fDomain.getEdgeCenter3D(0, a);
                    trianglePoints3D[p.incrementInt()] = fDomain.getEdgeCenter3D(1, a);
                    trianglePoints3D[p.incrementInt()] = fDomain.getEdgeCenter3D(2, a);

                    // Iterative Triangle mesh generator
                    class Triangle {
                        private boolean orientationUp;
                        private int pointA, pointB, pointC;
                        private int depth;

                        private Triangle(boolean orientationUp, int pointA, int pointB, int pointC, int depth) {
                            this.orientationUp = orientationUp;
                            this.pointA = pointA;
                            this.pointB = pointB;
                            this.pointC = pointC;
                            this.depth = depth;

                            if (this.depth > 0) {
                                int midAB = p.incrementInt();
                                trianglePoints3D[midAB] = Tools.sphericalMidpoint(trianglePoints3D[pointA], trianglePoints3D[pointB]); // Tools.midpoint3D(geom,
                                int midAC = p.incrementInt();
                                trianglePoints3D[midAC] = Tools.sphericalMidpoint(trianglePoints3D[pointA], trianglePoints3D[pointC]); // Tools.midpoint3D(geom,
                                int midBC = p.incrementInt();
                                trianglePoints3D[midBC] = Tools.sphericalMidpoint(trianglePoints3D[pointB], trianglePoints3D[pointC]);// Tools.midpoint3D(geom,

                                new Triangle(this.orientationUp, this.pointA, midAB, midAC, --this.depth);
                                new Triangle(this.orientationUp, midAB, this.pointB, midBC, this.depth);
                                new Triangle(this.orientationUp, midAC, midBC, this.pointC, this.depth);

                                if (this.orientationUp) {
                                    new Triangle(!this.orientationUp, midAB, midBC, midAC, this.depth);
                                } else {
                                    new Triangle(!this.orientationUp, midAC, midAB, midBC, this.depth);
                                }
                            } else {
                                int facPos = 6 * f.incrementInt();
                                faces[facPos] = pointA;
                                faces[facPos + 1] = 0;
                                faces[facPos + 2] = pointB;
                                faces[facPos + 3] = 1;
                                faces[facPos + 4] = pointC;
                                faces[facPos + 5] = 2;
                            }
                        }
                    }

                    // clockwise orientation
                    new Triangle(true, 0, 4, 5, depth);
                    new Triangle(true, 5, 3, 1, depth);
                    new Triangle(true, 4, 2, 3, depth);
                    new Triangle(false, 4, 3, 5, depth);

                    if (tilingStyle.isSmoothEdges()) {
                        int[] pointsOf2EdgeSorted = {0, 16, 13, 22, 10, 46, 43, 52, 7, 142, 139,
                                148, 136, 172, 169, 178, 5,
                                271, 268, 277, 265, 301, 298, 307, 262, 397, 394, 403, 391, 427, 424, 433, 1};

                        bandPoints3D = new Point3D[pointsOf2EdgeSorted.length];

                        for (int i = 0; i < pointsOf2EdgeSorted.length; i++) {
                            bandPoints3D[i] = trianglePoints3D[pointsOf2EdgeSorted[i]];
                        }
                    } else {
                        bandPoints3D = new Point3D[]{trianglePoints3D[0], trianglePoints3D[5], trianglePoints3D[5], trianglePoints3D[1]};
                    }

                    // sets points for band caps
                    bandCapPoints3D = new Point3D[]{trianglePoints3D[0], trianglePoints3D[5], trianglePoints3D[1]};

                    // scales the points on sphere to reduce rendering problems
                    for (int i = 0; i < trianglePoints3D.length; i++) {
                        trianglePoints3D[i] = trianglePoints3D[i].multiply(0.995);
                    }
                    break;
                }
                case Euclidean: {
                    /// Original mesh structure
                    trianglePoints3D = new Point3D[7];
                    int p = 0;
                    for (int i = 0; i <= 2; i++) {
                        trianglePoints3D[p++] = fDomain.getVertex3D(i, a);
                    }
                    for (int i = 0; i <= 2; i++) {
                        trianglePoints3D[p++] = fDomain.getEdgeCenter3D(i, a);
                    }
                    trianglePoints3D[p++] = fDomain.getChamberCenter3D(a);
                    faces = new int[]{0, 0, 6, 1, 5, 2, // v0 cc e2
                            1, 0, 5, 1, 6, 2, // v1 e2 cc
                            1, 0, 6, 1, 3, 2, // v1 cc e0
                            2, 0, 3, 0, 6, 2, // v2 e0 cc
                            2, 0, 6, 1, 4, 2, // v2 cc e1
                            0, 0, 4, 1, 6, 2 // v0 e1 cc
                    };

                    // Reduced mesh structure: Mesh consists only of 2 triangles
                    // points3d = new Point3D[4]; //4
                    //
                    // int p = 0;
                    // for (int i = 0; i <= 2; i++) {
                    // points3d[p++] = fDomain.getVertex3D(i,a);
                    // }
                    // points3d[p++] = fDomain.getEdgeCenter3D(2,a);
                    //
                    // int[] original = new int[]{
                    // 0, 0, 2, 1, 3, 2, //v0 v2 e2
                    // 2, 0, 1, 1, 3, 2, //v2 v1 e2
                    // };

                    // sets points for bands:
                    bandPoints3D = new Point3D[3];
                    bandPoints3D[0] = trianglePoints3D[0];
                    bandPoints3D[1] = trianglePoints3D[5];
                    bandPoints3D[2] = trianglePoints3D[1];
                    bandCapPoints3D = bandPoints3D;
                    break;
                }
                default:
                case Hyperbolic: {
                    trianglePoints3D = new Point3D[13];

                    int p = 0;

                    for (int i = 0; i <= 2; i++) {
                        trianglePoints3D[p++] = fDomain.getVertex3D(i, a);
                    }
                    for (int i = 0; i <= 2; i++) {
                        trianglePoints3D[p++] = fDomain.getEdgeCenter3D(i, a);
                    }
                    trianglePoints3D[p++] = fDomain.getChamberCenter3D(a);

                    // hyper
                    trianglePoints3D[p++] = Tools.midpoint3D(geom, trianglePoints3D[0], trianglePoints3D[5]);
                    trianglePoints3D[p++] = Tools.midpoint3D(geom, trianglePoints3D[5], trianglePoints3D[1]);
                    trianglePoints3D[p++] = Tools.midpoint3D(geom, trianglePoints3D[0], trianglePoints3D[7]);
                    trianglePoints3D[p++] = Tools.midpoint3D(geom, trianglePoints3D[7], trianglePoints3D[5]);
                    trianglePoints3D[p++] = Tools.midpoint3D(geom, trianglePoints3D[5], trianglePoints3D[8]);
                    trianglePoints3D[p++] = Tools.midpoint3D(geom, trianglePoints3D[8], trianglePoints3D[1]);

                    faces = new int[]{0, 0, 6, 1, 9, 2, //
                            9, 0, 6, 1, 7, 2, //
                            7, 0, 6, 1, 10, 2, //
                            10, 0, 6, 1, 5, 2, //
                            5, 0, 6, 1, 11, 2, //
                            11, 0, 6, 1, 8, 2, //
                            8, 0, 6, 1, 12, 2, //
                            12, 0, 6, 1, 1, 2, //
                            0, 0, 4, 1, 6, 2, //
                            4, 0, 2, 1, 6, 2, //
                            2, 0, 3, 1, 6, 2, //
                            6, 0, 3, 1, 1, 2 //
                    };

                    // sets points for bands:
                    int[] pointsOf2EdgeSorted = {0, 9, 7, 10, 5, 11, 8, 12, 1};
                    bandPoints3D = new Point3D[9];
                    for (int i = 0; i < 9; i++) {
                        bandPoints3D[i] = trianglePoints3D[pointsOf2EdgeSorted[i]];
                    }

                    // sets points for band caps
                    bandCapPoints3D = new Point3D[3];
                    bandCapPoints3D[0] = trianglePoints3D[0];
                    bandCapPoints3D[1] = trianglePoints3D[5];
                    bandCapPoints3D[2] = trianglePoints3D[1];

                    // scales points to reduce rendering problems
                    for (int i = 0; i < trianglePoints3D.length; i++) {
                        trianglePoints3D[i] = trianglePoints3D[i].multiply(1.0125);
                    }
                }
            } // end of geometric cases

            trianglePointsCoordinates = new float[3 * trianglePoints3D.length];

            for (int i = 0; i < trianglePoints3D.length; i++) {
                trianglePointsCoordinates[3 * i] = (float) trianglePoints3D[i].getX();
                trianglePointsCoordinates[3 * i + 1] = (float) trianglePoints3D[i].getY();
                trianglePointsCoordinates[3 * i + 2] = (float) trianglePoints3D[i].getZ();
            }

            if (fDomain.getOrientation(a) != orientation)
                invertOrientationOfFaces(faces);


            final int[] smoothing = new int[faces.length / 6];
            Arrays.fill(smoothing, 1);

            final float[] texCoords = {0.5f, 0, 0, 0, 1, 1};

            // Draw Faces
            if (drawFaces) {
                TriangleMesh mesh = new TriangleMesh();
                mesh.getPoints().addAll(trianglePointsCoordinates);
                mesh.getTexCoords().addAll(texCoords);
                mesh.getFaces().addAll(faces);
                mesh.getFaceSmoothingGroups().addAll(smoothing);
                MeshView meshView = new MeshView(mesh);
                // meshView.setMesh(mesh);
                PhongMaterial material = new PhongMaterial(colors[a]);
                // material.setSpecularColor(Color.YELLOW);
                meshView.setMaterial(material);
                tiles.getChildren().addAll(meshView);
            }

            // defines the height of band and caps above the surface
            if (geom == Geometry.Euclidean) {
                linesAbove = 1;
            } else if (geom == Geometry.Hyperbolic) {
                linesAbove = 0.0;
            } else {
                linesAbove = 0.0;
            }

            if (drawBands && !flagAHasBand.get(a)) {
                if (geom != Geometry.Euclidean)
                    flagAHasBand.set(dsymbol.getS2(a));

                TriangleMesh bandMesh = null;
                TriangleMesh backBandMesh = null;

                for (int i = 0; i < bandPoints3D.length - 1; i++) {
                    final TriangleMesh tmpMesh = Band3D.connect(bandPoints3D[i], bandPoints3D[i + 1], geom, bandWidth, linesAbove, false);
                    if (bandMesh == null)
                        bandMesh = tmpMesh;
                    else
                        bandMesh = combineTriangleMesh(bandMesh, tmpMesh);

                    if (tilingStyle.isShowBackEdges()) {
                        final TriangleMesh tmpMesh2 = Band3D.connect(bandPoints3D[i], bandPoints3D[i + 1], geom, bandWidth, linesAbove, true);
                        if (backBandMesh == null)
                            backBandMesh = tmpMesh2;
                        else
                            backBandMesh = combineTriangleMesh(backBandMesh, tmpMesh2); // adds back side Storage to linemesh
                    }
                }

                if (drawBandCaps) {
                    if (!flagAHasBandCaps.get(a)) {
                        flagAHasBandCaps.set(a);

                        for (int i = 0; i < bandCapPoints3D.length; i++) {
                            Point3D direction;
                            Point3D center = bandCapPoints3D[i];
                            if (i == bandCapPoints3D.length - 1) {
                                direction = bandCapPoints3D[i].subtract(bandCapPoints3D[i - 1]);
                            } else {
                                direction = bandCapPoints3D[i].subtract(bandCapPoints3D[i + 1]);
                            }

                            // gets circle coordinates
                            Point3D[] coordinates = BandCap3D.circle(center, direction, bandCapDiameter, bandCapFineness, geom);

                            // creates Triangle Mesh for circle coordinates
                            TriangleMesh tmpMesh = BandCap3D.CircleMesh(center, coordinates, geom, linesAbove, false);
                            if (bandMesh == null)
                                bandMesh = tmpMesh;
                            else
                                bandMesh = combineTriangleMesh(bandMesh, tmpMesh);

                            if (tilingStyle.isShowBackEdges()) {
                                final TriangleMesh tmpMesh2 = BandCap3D.CircleMesh(center, coordinates, geom, linesAbove, true);
                                if (backBandMesh == null)
                                    backBandMesh = tmpMesh2;
                                else
                                    backBandMesh = combineTriangleMesh(backBandMesh, tmpMesh2); // adds back side Storage to linemesh
                            }
                        }
                    }
                }

                // combines band mesh and band cap mesh
                // only one mesh for both reduces computation and errors

                if (bandMesh != null) {
                    bandMesh.getTexCoords().addAll(texCoords);
                    final MeshView meshView = new MeshView(bandMesh);
                    meshView.setMaterial(new PhongMaterial(bandColor));
                    bands.getChildren().add(meshView);
                }
                if (backBandMesh != null) {
                    backBandMesh.getTexCoords().addAll(texCoords);
                    final MeshView meshView = new MeshView(backBandMesh);
                    meshView.setMaterial(new PhongMaterial(bandColor));
                    bands.getChildren().add(meshView);
                }
            }
        }

        if (false) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(6.0);
            dropShadow.setOffsetY(4.0);
            dropShadow.setColor(Color.BLACK);

            Text text = new Text("Hello");
            text.setFont(Font.font(30));
            text.setFill(Color.GOLDENROD);
            text.setEffect(dropShadow);
            decorations.getChildren().add(text);
        }


        if (false) { // to test what happens if we add other stuff into the group...
            Sphere sphere = new Sphere(10);
            sphere.setTranslateZ(100);
            sphere.setMaterial(new PhongMaterial(Color.PURPLE));
            decorations.getChildren().add(sphere);
            sphere.setOnMouseClicked((e) -> System.err.println("Clicked " + e));
            sphere.setOnMouseEntered((e) -> System.err.println("Entered " + e));
        }
    }

    private static double computeWindingNumber(Point3D a0, Point3D a1, Point3D a2) {
        return (a1.getX() - a0.getX()) * (a1.getY() + a0.getY()) + (a2.getX() - a1.getX()) * (a2.getY() + a1.getY())
                + (a0.getX() - a2.getX()) * (a0.getY() + a2.getY());
    }

    /**
     * combines two Triangle Meshes by Cornelius 21.11.18
     *
     * @param mesh1
     * @param mesh2
     * @return combined mesh of mesh1 and mesh2
     */
    private static TriangleMesh combineTriangleMesh(TriangleMesh mesh1, TriangleMesh mesh2) {

        TriangleMesh newMesh = new TriangleMesh(); // mesh that is returned later
        int mesh1pointsize = mesh1.getPoints().size(); // number of points of mesh1
        int mesh1facesize = mesh1.getFaces().size(); // number of faces of mesh1
        int facesize = mesh1facesize + mesh2.getFaces().size();// number of faces for new mesh

        // recalculates which points belong to which face
        int[] faces = new int[facesize];
        // no changes for faces of mesh1
        for (int i = 0; i < mesh1facesize; i++) {
            faces[i] = mesh1.getFaces().get(i);
        }
        // changes for mesh2
        for (int i = mesh1facesize; i < facesize; i = i + 6) {

            faces[i] = mesh2.getFaces().get(i - mesh1facesize) + (mesh1pointsize / 3);
            faces[i + 1] = 0;
            faces[i + 2] = mesh2.getFaces().get(i + 2 - mesh1facesize) + (mesh1pointsize / 3);
            faces[i + 3] = 1;
            faces[i + 4] = mesh2.getFaces().get(i + 4 - mesh1facesize) + (mesh1pointsize / 3);
            faces[i + 5] = 2;

        }

        // points can be added easily
        newMesh.getPoints().addAll(mesh1.getPoints());
        newMesh.getPoints().addAll(mesh2.getPoints());

        // handles smoothing groups of mesh
        newMesh.getFaceSmoothingGroups().addAll(mesh1.getFaceSmoothingGroups());
        newMesh.getFaceSmoothingGroups().addAll(mesh2.getFaceSmoothingGroups());

        newMesh.getFaces().addAll(faces);

        return newMesh;
    }

    /**
     * flip the orientation of faces
     *
     * @param faces
     */
    public static void invertOrientationOfFaces(int[] faces) {
        for (int i = 0; i < faces.length; i += 6) {
            int tmp = faces[i + 2];
            faces[i + 2] = faces[i + 4];
            faces[i + 4] = tmp;
            tmp = faces[i + 3];
            faces[i + 3] = faces[i + 5];
            faces[i + 5] = tmp;
        }
    }

    /**
     * computes the representation of chambers
     *
     * @param fDomain
     */
    public Group computeChambers(FDomain fDomain) {
        final Group all = new Group();

        for (int a = 1; a <= fDomain.size(); a++) {
            all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(0, a), fDomain.getEdgeCenter3D(1, a), fDomain.getVertex3D(2, a), Color.LIGHTGRAY, 1));

            all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(2, a), fDomain.getEdgeCenter3D(0, a), fDomain.getVertex3D(1, a), Color.LIGHTGRAY, 1));

            all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(0, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(0, a), Color.LIGHTGRAY, 0.5f));
            all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(1, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(1, a), Color.LIGHTGRAY, 0.5f));
            all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(2, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(2, a), Color.LIGHTGRAY, 0.5f));

        }
        for (int k = 1; k <= fDomain.size(); k++) {
            final Point3D v0 = fDomain.getVertex3D(0, k);
            final Point3D e2 = fDomain.getEdgeCenter3D(2, k);
            final Point3D v1 = fDomain.getVertex3D(1, k);
            all.getChildren().add(Lines.createLine(fDomain.getGeometry(), v0, e2, v1, Color.DARKGRAY, 1));
        }
        return all;
    }

    public Group getAll() {
        return all;
    }

    public Group getTiles() {
        return tiles;
    }

    public Group getBands() {
        return bands;
    }

    public Group getChambers() {
        return chambers;
    }

    public Group getDecorations() {
        return decorations;
    }

    public Group getHandles() {
        return decorations;
    }

    public boolean isIncludeTiles() {
        return includeTiles.get();
    }

    public BooleanProperty includeTilesProperty() {
        return includeTiles;
    }

    public void setIncludeTiles(boolean includeTiles) {
        this.includeTiles.set(includeTiles);
    }

    public boolean isIncludeBands() {
        return includeBands.get();
    }

    public BooleanProperty includeBandsProperty() {
        return includeBands;
    }

    public void setIncludeBands(boolean includeBands) {
        this.includeBands.set(includeBands);
    }

    public boolean isIncludeChambers() {
        return includeChambers.get();
    }

    public BooleanProperty includeChambersProperty() {
        return includeChambers;
    }

    public void setIncludeChambers(boolean includeChambers) {
        this.includeChambers.set(includeChambers);
    }

    public boolean isIncludeDecorations() {
        return includeDecorations.get();
    }

    public BooleanProperty includeDecorationsProperty() {
        return includeDecorations;
    }

    public void setIncludeDecorations(boolean includeDecorations) {
        this.includeDecorations.set(includeDecorations);
    }

    public boolean isIncludeHandles() {
        return includeHandles.get();
    }

    public BooleanProperty includeHandlesProperty() {
        return includeHandles;
    }

    public void setIncludeHandles(boolean includeHandles) {
        this.includeHandles.set(includeHandles);
    }

    public Shape3D getReference() {
        return reference;
    }

    public Group getReferences() {
        return references;
    }
}
