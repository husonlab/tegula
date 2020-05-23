/*
 * FundamentalDomain.java Copyright (C) 2020. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tiling;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.core.funtiles.utils.WrapInt;
import tegula.geometry.Tools;
import tegula.main.TilingStyle;
import tegula.tiling.parts.Band3D;
import tegula.tiling.parts.BandCap3D;
import tegula.tiling.parts.Lines;
import tegula.tiling.parts.MeshUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Fundamental domain for tiling
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class FundamentalDomain {
    /**
     * Computes a fundamental domain
     *
     * @param dsymbol Delaney symbol from old DH code
     * @param fDomain domain computed by KW
     * @return fundamental domain
     */
    public static ArrayList<Group> compute(final DSymbol dsymbol, final FDomain fDomain, TilingStyle tilingStyle) {
        final Geometry geom = fDomain.getGeometry();

        final int[] a2tile = dsymbol.computeOrbits(0, 1);
        final int[] a2edge = dsymbol.computeOrbits(0, 2);
        final BitSet firstInEdge = dsymbol.computeFirstInOrbit(0, 2);
        final int[] a2vertex = dsymbol.computeOrbits(1, 2);
        final BitSet firstInVertex = dsymbol.computeFirstInOrbit(1, 2);

        // set colors
        final Material[] tile2material = new Material[a2tile.length + 1];
        {
            for (int t = 1; t < tile2material.length; t++)
                tile2material[t] = new PhongMaterial(tilingStyle.getTileColor(t));
        }

        final double bandWidth = 0.2 * tilingStyle.getBandWidth();

        final Material[] edge2material = new Material[a2edge.length + 1];
        final double[] edgeWidth = new double[a2edge.length + 1];
        // todo: support different colors for different edges
        {
            for (int e = 1; e < edge2material.length; e++) {
                edge2material[e] = new PhongMaterial(tilingStyle.getBandColor());
                edgeWidth[e] = bandWidth;
            }
        }

        final Material[] vertex2material = new Material[a2vertex.length + 1];
        final double[] vertexDiameter = new double[a2vertex.length + 1];
        // todo: support different colors for different vertices
        {
            for (int v = 1; v < vertex2material.length; v++) {
                vertex2material[v] = new PhongMaterial(tilingStyle.getBandColor());
                vertexDiameter[v] = bandWidth;
            }
        }

        // For bands and the band caps (i.e. circles at the ends of bands)

        final int bandCapFineness = tilingStyle.getBandCapFineness(); // defines how smooth the vertices are

        final Group facesGroup = new Group();
        final Group edgesGroup = new Group();
        final Group verticesGroup = new Group();
        final Group othersGroup = new Group();

        BitSet vertex1WithGaps = computeVertex1WithGaps(fDomain);
        BitSet edgeCenter2WithGaps = computeEdgeCenter2WithGaps(fDomain);


        // construct triangles as meshes:

        final int orientation = (computeWindingNumber(fDomain.getVertex3D(0, 1), fDomain.getVertex3D(1, 1),
                fDomain.getVertex3D(2, 1)) < 0 ? fDomain.getOrientation(1) : -fDomain.getOrientation(1));

        for (int a = 1; a <= dsymbol.size(); a++) {
            final float[] trianglePointsCoordinates;
            final Point3D[] trianglePoints3D; // points that create the triangles
            final Point3D[] edgePoints3D;
            final Point3D[] cornerPoints3D;
            final int[] faces;

            switch (geom) {
                case Spherical -> {

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

                        private Triangle(boolean orientationUp, int pointA, int pointB, int pointC, int depth) {
                            int depth1 = depth;

                            if (depth1 > 0) {
                                int midAB = p.incrementInt();
                                trianglePoints3D[midAB] = Tools.sphericalMidpoint(trianglePoints3D[pointA], trianglePoints3D[pointB]); // Tools.midpoint3D(geom,
                                int midAC = p.incrementInt();
                                trianglePoints3D[midAC] = Tools.sphericalMidpoint(trianglePoints3D[pointA], trianglePoints3D[pointC]); // Tools.midpoint3D(geom,
                                int midBC = p.incrementInt();
                                trianglePoints3D[midBC] = Tools.sphericalMidpoint(trianglePoints3D[pointB], trianglePoints3D[pointC]);// Tools.midpoint3D(geom,

                                new Triangle(orientationUp, pointA, midAB, midAC, --depth1);
                                new Triangle(orientationUp, midAB, pointB, midBC, depth1);
                                new Triangle(orientationUp, midAC, midBC, pointC, depth1);

                                if (orientationUp) {
                                    new Triangle(!orientationUp, midAB, midBC, midAC, depth1);
                                } else {
                                    new Triangle(!orientationUp, midAC, midAB, midBC, depth1);
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

                        edgePoints3D = new Point3D[pointsOf2EdgeSorted.length];

                        for (int i = 0; i < pointsOf2EdgeSorted.length; i++) {
                            edgePoints3D[i] = trianglePoints3D[pointsOf2EdgeSorted[i]];
                        }
                    } else {
                        edgePoints3D = new Point3D[]{trianglePoints3D[0], trianglePoints3D[5], trianglePoints3D[5], trianglePoints3D[1]};
                    }

                    // sets points for band caps
                    cornerPoints3D = new Point3D[]{trianglePoints3D[0], trianglePoints3D[5], trianglePoints3D[1]};


                    // scales the points on sphere to reduce rendering problems
                    for (int i = 0; i < trianglePoints3D.length; i++) {
                        trianglePoints3D[i] = trianglePoints3D[i].multiply(0.995);
                    }

                    break;
                }
                case Euclidean -> {
                    /// Original mesh structure
                    trianglePoints3D = new Point3D[7];

                    trianglePoints3D[0] = fDomain.getVertex3D(0, a);
                    trianglePoints3D[1] = fDomain.getVertex3D(1, a);
                    trianglePoints3D[2] = fDomain.getVertex3D(2, a);

                    trianglePoints3D[3] = fDomain.getEdgeCenter3D(0, a);
                    trianglePoints3D[4] = fDomain.getEdgeCenter3D(1, a);
                    trianglePoints3D[5] = fDomain.getEdgeCenter3D(2, a);

                    trianglePoints3D[6] = fDomain.getChamberCenter3D(a);


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

                    edgePoints3D = new Point3D[3];
                    edgePoints3D[0] = trianglePoints3D[0];
                    edgePoints3D[1] = trianglePoints3D[5];
                    edgePoints3D[2] = trianglePoints3D[1];
                    cornerPoints3D = edgePoints3D;
                    break;
                }
                case Hyperbolic -> {
                    trianglePoints3D = new Point3D[13];

                    trianglePoints3D[0] = fDomain.getVertex3D(0, a);
                    trianglePoints3D[1] = fDomain.getVertex3D(1, a);
                    trianglePoints3D[2] = fDomain.getVertex3D(2, a);

                    trianglePoints3D[3] = fDomain.getEdgeCenter3D(0, a);
                    trianglePoints3D[4] = fDomain.getEdgeCenter3D(1, a);
                    trianglePoints3D[5] = fDomain.getEdgeCenter3D(2, a);

                    trianglePoints3D[6] = fDomain.getChamberCenter3D(a);

                    trianglePoints3D[7] = Tools.midpoint3D(geom, trianglePoints3D[0], trianglePoints3D[5]);
                    trianglePoints3D[8] = Tools.midpoint3D(geom, trianglePoints3D[5], trianglePoints3D[1]);
                    trianglePoints3D[9] = Tools.midpoint3D(geom, trianglePoints3D[0], trianglePoints3D[7]);
                    trianglePoints3D[10] = Tools.midpoint3D(geom, trianglePoints3D[7], trianglePoints3D[5]);
                    trianglePoints3D[11] = Tools.midpoint3D(geom, trianglePoints3D[5], trianglePoints3D[8]);
                    trianglePoints3D[12] = Tools.midpoint3D(geom, trianglePoints3D[8], trianglePoints3D[1]);

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

                    int[] pointsOf2EdgeSorted = {0, 9, 7, 10, 5, 11, 8, 12, 1};
                    edgePoints3D = new Point3D[9];
                    for (int i = 0; i < 9; i++) {
                        edgePoints3D[i] = trianglePoints3D[pointsOf2EdgeSorted[i]];
                    }

                    cornerPoints3D = new Point3D[]{trianglePoints3D[0], trianglePoints3D[5], trianglePoints3D[1]};

                    // scales points to reduce rendering problems
                    for (int i = 0; i < trianglePoints3D.length; i++) {
                        trianglePoints3D[i] = trianglePoints3D[i].multiply(1.0125);
                    }
                }
                default -> throw new RuntimeException("Invalid case");
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

            // compute faces
            {
                final TriangleMesh mesh = new TriangleMesh();
                mesh.getPoints().addAll(trianglePointsCoordinates);
                mesh.getTexCoords().addAll(texCoords);
                mesh.getFaces().addAll(faces);
                mesh.getFaceSmoothingGroups().addAll(smoothing);
                if (tilingStyle.isShowFaces()) {
                    final MeshView meshView = new MeshView(mesh);
                    // meshView.setMesh(mesh);
                    // material.setSpecularColor(Color.YELLOW);
                    meshView.setId("t=" + a2tile[a]);
                    meshView.setMaterial(tile2material[a2tile[a]]);
                    facesGroup.getChildren().add(meshView);
                }
                if (tilingStyle.isShowBackFaces()) {
                    MeshView backMeshView = new MeshView(MeshUtils.reverseOrientation(mesh));
                    // meshView.setMesh(mesh);
                    // material.setSpecularColor(Color.YELLOW);
                    backMeshView.setMaterial(tile2material[a2tile[a]]);
                    backMeshView.setId("t=" + a2tile[a]);
                    facesGroup.getChildren().add(backMeshView);
                }
            }

            // defines the height of band and caps above the surface
            final double linesAbove = (geom == Geometry.Euclidean ? 1 : 0);

            // compute edges:
            if (dsymbol.getS2(a) >= a || fDomain.isBoundaryEdge(2, a)) {
                TriangleMesh mesh = null;

                for (int i = 0; i < edgePoints3D.length - 1; i++) {
                    final TriangleMesh tmpMesh = Band3D.connect(geom, edgePoints3D[i], edgePoints3D[i + 1], edgeWidth[a2edge[a]], linesAbove, StrokeLineCap.BUTT, StrokeLineCap.BUTT);
                    if (mesh == null)
                        mesh = tmpMesh;
                    else
                        mesh = MeshUtils.combineTriangleMesh(mesh, tmpMesh);
                }
                if (mesh == null)
                    continue;

                for (int i = 0; i < cornerPoints3D.length; i++) {
                    Point3D direction;
                    Point3D center = cornerPoints3D[i];
                    if (i == cornerPoints3D.length - 1) {
                        direction = cornerPoints3D[i].subtract(cornerPoints3D[i - 1]);
                    } else {
                        direction = cornerPoints3D[i].subtract(cornerPoints3D[i + 1]);
                    }

                    // gets circle coordinates
                    Point3D[] coordinates = BandCap3D.circle(center, direction, vertexDiameter[a2vertex[a]], bandCapFineness, geom);

                    final TriangleMesh circleMesh = BandCap3D.CircleMesh(center, coordinates, geom, linesAbove, false);

                    circleMesh.getTexCoords().addAll(texCoords);

                    // if(firstInVertex.get(a) || fDomain.isBoundaryEdge(2,a))
                    if (true) {
                        if (i == 0) {
                            if (tilingStyle.isShowVertices()) {
                                final MeshView meshView = new MeshView(circleMesh);
                                meshView.setMaterial(vertex2material[a2vertex[a]]);
                                meshView.setId("v=" + a2vertex[a]);
                                verticesGroup.getChildren().add(meshView);
                            }
                            if (tilingStyle.isShowBackVertices()) {
                                final MeshView meshView = new MeshView(MeshUtils.reverseOrientation(circleMesh));
                                meshView.setMaterial(vertex2material[a2vertex[a]]);
                                meshView.setId("v=" + a2vertex[a]);
                                verticesGroup.getChildren().add(meshView);
                            }
                        } else { // is edge corner
                            if (tilingStyle.isShowVertices()) {
                                if (i == 1 && edgeCenter2WithGaps.get(a) || i == 2 && vertex1WithGaps.get(a))
                                    mesh = MeshUtils.combineTriangleMesh(mesh, circleMesh);
                            }
                        }
                    }

                }

                mesh.getTexCoords().addAll(texCoords);
                if (tilingStyle.isShowEdges()) {
                    final MeshView edgeMeshView = new MeshView(mesh);
                    edgeMeshView.setMaterial(edge2material[a2edge[a]]);
                    edgeMeshView.setId("e=" + a2edge[a]);
                    edgesGroup.getChildren().add(edgeMeshView);
                }
                if (tilingStyle.isShowBackEdges()) {
                    final MeshView backEdgeMeshView = new MeshView(MeshUtils.reverseOrientation(mesh));
                    backEdgeMeshView.setMaterial(edge2material[a2edge[a]]);
                    backEdgeMeshView.setId("e=" + a2edge[a]);
                    edgesGroup.getChildren().add(backEdgeMeshView);
                }
            }
        }

        if (false) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(6.0);
            dropShadow.setOffsetY(4.0);
            dropShadow.setColor(Color.BLACK);

            {
                Text text = new Text("To A &");
                text.getTransforms().add(new Translate(58, -12, 0));
                text.setFont(Font.font("Helvetica", 10));
                text.setFill(Color.DARKRED);
                text.setEffect(dropShadow);
                othersGroup.getChildren().add(text);
            }

            {
                Text text = new Text("B");
                text.getTransforms().add(new Translate(60, 0, 0));
                text.setFont(Font.font("Helvetica", 10));
                text.setFill(Color.DARKRED);
                text.setEffect(dropShadow);
                othersGroup.getChildren().add(text);
            }
            {
                Text text = new Text("from C");
                text.getTransforms().add(new Translate(58, 12, 0));
                text.setFont(Font.font("Helvetica", 10));
                text.setFill(Color.DARKRED);
                text.setEffect(dropShadow);
                othersGroup.getChildren().add(text);
            }
        }


        if (false) { // to test what happens if we add other stuff into the group...
            Sphere sphere = new Sphere(10);
            sphere.setTranslateZ(100);
            sphere.setMaterial(new PhongMaterial(Color.PURPLE));
            othersGroup.getChildren().add(sphere);
            sphere.setOnMouseClicked((e) -> System.err.println("Clicked " + e));
            sphere.setOnMouseEntered((e) -> System.err.println("Entered " + e));
        }

        if (tilingStyle.isShowAllChambers())
            othersGroup.getChildren().addAll(computeChambers(fDomain));

        final ArrayList<Group> all = new ArrayList<>();
        if (facesGroup.getChildren().size() > 0)
            all.add(facesGroup);
        if (edgesGroup.getChildren().size() > 0)
            all.add(edgesGroup);
        if (verticesGroup.getChildren().size() > 0)
            all.add(verticesGroup);
        if (othersGroup.getChildren().size() > 0)
            all.add(othersGroup);
        if (tilingStyle.getDecorations().getChildren().size() > 0) {
            if (geom == Geometry.Euclidean)
                all.add(tilingStyle.getDecorations());
            else
                all.add(MapImageToGeometry.apply(geom, tilingStyle.getDecorations()));
        }
        return all;
    }

    private static double computeWindingNumber(Point3D a0, Point3D a1, Point3D a2) {
        return (a1.getX() - a0.getX()) * (a1.getY() + a0.getY()) + (a2.getX() - a1.getX()) * (a2.getY() + a1.getY())
                + (a0.getX() - a2.getX()) * (a0.getY() + a2.getY());
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
    private static ArrayList<Node> computeChambers(FDomain fDomain) {
        final ArrayList<Node> all = new ArrayList<>();

        for (int a = 1; a <= fDomain.size(); a++) {
            all.add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(0, a), fDomain.getEdgeCenter3D(1, a), fDomain.getVertex3D(2, a), Color.DARKGRAY, 1));
            all.add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(2, a), fDomain.getEdgeCenter3D(0, a), fDomain.getVertex3D(1, a), Color.LIGHTGRAY, 1));

            //all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(0, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(0, a), Color.LIGHTGRAY, 0.5f));
            //all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(1, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(1, a), Color.LIGHTGRAY, 0.5f));
            //all.getChildren().add(Lines.createLine(fDomain.getGeometry(), fDomain.getVertex3D(2, a), fDomain.getChamberCenter3D(a), fDomain.getEdgeCenter3D(2, a), Color.LIGHTGRAY, 0.5f));

        }
       /*
        for (int k = 1; k <= fDomain.size(); k++) {
            final Point3D v0 = fDomain.getVertex3D(0, k);
            final Point3D e2 = fDomain.getEdgeCenter3D(2, k);
            final Point3D v1 = fDomain.getVertex3D(1, k);
            all.add(Lines.createLine(fDomain.getGeometry(), v0, e2, v1, Color.DARKGRAY, 1));
        }
        */
        for (int a = 1; a <= fDomain.getDSymbol().size(); a++) {
            final Text text = new Text("" + a);
            text.setFont(Font.font("System", 12));
            text.setFill(Color.BLACK);
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(6.0);
            dropShadow.setOffsetY(4.0);
            text.setEffect(dropShadow);
            final Point3D apt = fDomain.getChamberCenter3D(a);
            text.getTransforms().add(new Translate(apt.getX(), apt.getY(), apt.getZ()));
            all.add(text);
        }

        return all;
    }

    private static BitSet computeEdgeCenter2WithGaps(FDomain fDomain) {
        final DSymbol dSymbol = fDomain.getDSymbol();

        final BitSet result = new BitSet();
        for (int a = 1; a <= dSymbol.size(); a++) {
            final double angle = Math.abs(fDomain.getEdgeCenter(2, a).angle(fDomain.getVertex(0, a), fDomain.getVertex(1, a)));
            if (Math.abs(180 - angle) > 1)
                result.set(a);
        }
        return result;
    }

    private static BitSet computeVertex1WithGaps(FDomain fDomain) {
        final DSymbol dSymbol = fDomain.getDSymbol();

        final double[] angles = new double[dSymbol.size() + 1];

        for (int a = 1; a <= dSymbol.size(); a++) {
            angles[a] = Math.abs(fDomain.getVertex(1, a).angle(fDomain.getEdgeCenter(0, a), fDomain.getEdgeCenter(2, a)));
        }

        final BitSet result = new BitSet();
        for (int a = 1; a <= dSymbol.size(); a++) {
            if (Math.abs(180 - (angles[a] + angles[dSymbol.getS0(a)])) > 1)
                result.set(a);
        }
        return result;
    }
}
