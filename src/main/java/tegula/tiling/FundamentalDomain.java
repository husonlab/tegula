/*
 * FundamentalDomain.java Copyright (C) 2023 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tiling;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import jloda.util.BitSetUtils;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.FDomain;
import tegula.core.dsymbols.Geometry;
import tegula.core.funtiles.utils.WrapInt;
import tegula.geometry.Tools;
import tegula.tiling.parts.*;
import tegula.window.TilingStyle;

import java.util.ArrayList;
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
        final int[] a2vertex = dsymbol.computeOrbits(1, 2);

        BitSet invisibleEdgeFlags=null;
        if(tilingStyle.isHideFirstTile()) {
            var orbit01= BitSetUtils.asBitSet(dsymbol.orbitMembers(0,1,1));
            invisibleEdgeFlags=new BitSet();
            for(var a:BitSetUtils.members(orbit01)) {
                if(orbit01.get(dsymbol.getS2(a))) {
                    invisibleEdgeFlags.or(BitSetUtils.asBitSet(dsymbol.orbitMembers(0, 2, a)));
                }
            }
        }

        // set colors
        final Material[] tile2material = new Material[a2tile.length + 1];
        {
            for (int t = 1; t < tile2material.length; t++) {
                tile2material[t] = new PhongMaterial(tilingStyle.getTileColor(t));
            }
        }

        final double bandWidth = (geom == Geometry.Euclidean ? 0.2 : 0.2) * tilingStyle.getBandWidth();

        final Material[] edge2material = new Material[a2edge.length + 1];
        final double[] edgeWidth = new double[a2edge.length + 1];
        // todo: support different colors for different edges
        {
            for (int e = 1; e < edge2material.length; e++) {
                final PhongMaterial material = new PhongMaterial(tilingStyle.getBandColor());
                TilingStyle.applyBandSpecular(material, geom);
                edge2material[e] = material;
                edgeWidth[e] = bandWidth;
            }
        }

        final Material[] vertex2material = new Material[a2vertex.length + 1];
        final double[] vertexDiameter = new double[a2vertex.length + 1];
        // todo: support different colors for different vertices
        {
            for (int v = 1; v < vertex2material.length; v++) {
                final PhongMaterial material = new PhongMaterial(tilingStyle.getBandColor());
                TilingStyle.applyBandSpecular(material, geom);
                vertex2material[v] = material;
                vertexDiameter[v] = bandWidth;
            }
        }

        // For bands and the band caps (i.e. circles at the ends of bands)

        final int bandCapFineness = tilingStyle.getBandCapFineness(); // defines how smooth the vertices are

        final Group facesGroup = new Group();
        final Group edgesGroup = new Group();
        final Group verticesGroup = new Group();
        final Group othersGroup = new Group();

        final int orientation = (computeWindingNumber(fDomain.getVertex3D(0, 1), fDomain.getVertex3D(1, 1),
                fDomain.getVertex3D(2, 1)) < 0 ? fDomain.getOrientation(1) : -fDomain.getOrientation(1));

        final float[][] a2ChamberCoordinates = new float[dsymbol.size() + 1][];
        final Point3D[][] a2edgePoints = new Point3D[dsymbol.size() + 1][];
        final Point3D[] a2VertexPoints3D = new Point3D[dsymbol.size() + 1];
        final int[][] a2ChamberFaces = new int[dsymbol.size() + 1][];

		// tiles in relief: inset each tile from its true boundary (gap) and optionally run a lip along the boundary
		final double tileGap = tilingStyle.getTileGap();
		final double tileLipDepth = tilingStyle.getTileLipDepth();
		final boolean tileSubstrate = tilingStyle.isShowTileSubstrate();
		// the substrate sits just below the bottom of the lip, so that lips visually rest on it, but never
		// closer to the tiles than MIN_SUBSTRATE_DEPTH, as otherwise it z-fights with them
		final double substrateDepth = Math.max(tileLipDepth, TileRelief.MIN_SUBSTRATE_DEPTH);
		// with a substrate the lip becomes the side wall of the tile and must reach all the way down to the
		// grout floor, so that the groove is closed; without one it is a free-standing skirt
		final double wallDepth = (tileSubstrate ? substrateDepth : tileLipDepth);
		final boolean buildWall = tileSubstrate || tileLipDepth >= TileRelief.MIN_LIP_DEPTH;
		final int[] boundaryIndices = TileRelief.boundaryIndices(geom, tilingStyle.isSmoothEdges());
		final Point3D[][] a2LipBoundary = new Point3D[dsymbol.size() + 1][];
		final Point3D[][] a2OuterBoundary = new Point3D[dsymbol.size() + 1][];
		final Point3D[] a2TileCenter = new Point3D[dsymbol.size() + 1];
		final Point3D[][] a2ChamberPoints = new Point3D[dsymbol.size() + 1][];
		final double[][] a2CornerSines = new double[dsymbol.size() + 1][];
		// a small scaling applied to all chamber points to reduce rendering problems, after any insetting
		final double postScale = switch (geom) {
			case Spherical -> 0.995;
			case Hyperbolic -> 1.0125;
			default -> 1;
		};

        // compute all coordinates for each chamber:
        for (int a = 1; a <= dsymbol.size(); a++) {
            final Point3D[] chamberPoints; // points that create the triangles
            final Point3D[] edgePoints3D;
            final int[] chamberFaces;

            // end of geometric cases
            switch (geom) {
                // Iterative Triangle mesh generator
                // clockwise orientation
                // sets points for band caps
                // scales the points on sphere to reduce rendering problems
                case Spherical: {
                    final int depth = tilingStyle.isSmoothEdges() ? 4 : 0; // 4^5 = 1024
                    chamberFaces = new int[(int) Math.pow(4, (depth + 1)) * 6];
                    chamberPoints = new Point3D[depth == 0 ? 6 : 1026]; // 3, 6, 66, 258, 1026 // size of points array dependent on depth
                    final WrapInt p = new WrapInt(0);
                    final WrapInt f = new WrapInt(0);
                    chamberPoints[p.incrementInt()] = fDomain.getVertex3D(0, a);
                    chamberPoints[p.incrementInt()] = fDomain.getVertex3D(1, a);
                    chamberPoints[p.incrementInt()] = fDomain.getVertex3D(2, a);
                    chamberPoints[p.incrementInt()] = fDomain.getEdgeCenter3D(0, a);
                    chamberPoints[p.incrementInt()] = fDomain.getEdgeCenter3D(1, a);
                    chamberPoints[p.incrementInt()] = fDomain.getEdgeCenter3D(2, a);
                    class Triangle {

                        private Triangle(boolean orientationUp, int pointA, int pointB, int pointC, int depth) {
                            int depth1 = depth;

                            if (depth1 > 0) {
                                int midAB = p.incrementInt();
                                chamberPoints[midAB] = Tools.sphericalMidpoint(chamberPoints[pointA], chamberPoints[pointB]); // Tools.midpoint3D(geom,
                                int midAC = p.incrementInt();
                                chamberPoints[midAC] = Tools.sphericalMidpoint(chamberPoints[pointA], chamberPoints[pointC]); // Tools.midpoint3D(geom,
                                int midBC = p.incrementInt();
                                chamberPoints[midBC] = Tools.sphericalMidpoint(chamberPoints[pointB], chamberPoints[pointC]);// Tools.midpoint3D(geom,

                                new Triangle(orientationUp, pointA, midAB, midAC, --depth1);
                                new Triangle(orientationUp, midAB, pointB, midBC, depth1);
                                new Triangle(orientationUp, midAC, midBC, pointC, depth1);

                                if (orientationUp) {
                                    new Triangle(false, midAB, midBC, midAC, depth1);
                                } else {
                                    new Triangle(true, midAC, midAB, midBC, depth1);
                                }
                            } else {
                                int facPos = 6 * f.incrementInt();
                                chamberFaces[facPos] = pointA;
                                chamberFaces[facPos + 1] = 0;
                                chamberFaces[facPos + 2] = pointB;
                                chamberFaces[facPos + 3] = 1;
                                chamberFaces[facPos + 4] = pointC;
                                chamberFaces[facPos + 5] = 2;
                            }
                        }
                    }
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
                            edgePoints3D[i] = chamberPoints[pointsOf2EdgeSorted[i]];
                        }
                    } else {
                        edgePoints3D = new Point3D[]{chamberPoints[0], chamberPoints[5], chamberPoints[5], chamberPoints[1]};
                    }
                    a2VertexPoints3D[a] = chamberPoints[0];
                    break;
                }
                case Euclidean: {
                    chamberPoints = new Point3D[7];
                    chamberPoints[0] = fDomain.getVertex3D(0, a);
                    chamberPoints[1] = fDomain.getVertex3D(1, a);
                    chamberPoints[2] = fDomain.getVertex3D(2, a);
                    chamberPoints[3] = fDomain.getEdgeCenter3D(0, a);
                    chamberPoints[4] = fDomain.getEdgeCenter3D(1, a);
                    chamberPoints[5] = fDomain.getEdgeCenter3D(2, a);
                    chamberPoints[6] = fDomain.getChamberCenter3D(a);
                    chamberFaces = new int[]{0, 0, 6, 1, 5, 2, // v0 cc e2
                            1, 0, 5, 1, 6, 2, // v1 e2 cc
                            1, 0, 6, 1, 3, 2, // v1 cc e0
                            2, 0, 3, 0, 6, 2, // v2 e0 cc
                            2, 0, 6, 1, 4, 2, // v2 cc e1
                            0, 0, 4, 1, 6, 2 // v0 e1 cc
                    };
                    edgePoints3D = new Point3D[3];
                    edgePoints3D[0] = chamberPoints[0];
                    edgePoints3D[1] = chamberPoints[5];
                    edgePoints3D[2] = chamberPoints[1];
                    a2VertexPoints3D[a] = chamberPoints[0];
                    break;
                }
// scales points to reduce rendering problems
                case Hyperbolic: {
                    chamberPoints = new Point3D[13];
                    chamberPoints[0] = fDomain.getVertex3D(0, a);
                    chamberPoints[1] = fDomain.getVertex3D(1, a);
                    chamberPoints[2] = fDomain.getVertex3D(2, a);
                    chamberPoints[3] = fDomain.getEdgeCenter3D(0, a);
                    chamberPoints[4] = fDomain.getEdgeCenter3D(1, a);
                    chamberPoints[5] = fDomain.getEdgeCenter3D(2, a);
                    chamberPoints[6] = fDomain.getChamberCenter3D(a);
                    chamberPoints[7] = Tools.midpoint3D(geom, chamberPoints[0], chamberPoints[5]);
                    chamberPoints[8] = Tools.midpoint3D(geom, chamberPoints[5], chamberPoints[1]);
                    chamberPoints[9] = Tools.midpoint3D(geom, chamberPoints[0], chamberPoints[7]);
                    chamberPoints[10] = Tools.midpoint3D(geom, chamberPoints[7], chamberPoints[5]);
                    chamberPoints[11] = Tools.midpoint3D(geom, chamberPoints[5], chamberPoints[8]);
                    chamberPoints[12] = Tools.midpoint3D(geom, chamberPoints[8], chamberPoints[1]);
                    chamberFaces = new int[]{0, 0, 6, 1, 9, 2, //
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
                    a2VertexPoints3D[a] = chamberPoints[0];

                    int[] pointsOf2EdgeSorted = {0, 9, 7, 10, 5, 11, 8, 12, 1};
                    edgePoints3D = new Point3D[9];
                    for (int i = 0; i < 9; i++) {
                        edgePoints3D[i] = chamberPoints[pointsOf2EdgeSorted[i]];
                    }
                    break;
                }
                default:
                    throw new RuntimeException("Invalid case");
            }

			a2ChamberPoints[a] = chamberPoints;
			// the angles at the two ends of the boundary are needed before any chamber is inset, because each
			// end is shared with a neighbouring chamber and both must agree on where to move it
			a2CornerSines[a] = TileRelief.cornerSines(chamberPoints, chamberPoints[2], boundaryIndices);
			a2edgePoints[a] = edgePoints3D;
			a2ChamberFaces[a] = chamberFaces;
		}

		// inset the chambers and record the resulting tile boundaries:
		for (int a = 1; a <= dsymbol.size(); a++) {
			final Point3D[] chamberPoints = a2ChamberPoints[a];

			// The first point of the boundary is a vertex of the tiling, shared with chamber s1(a) of this same
			// tile, and the last is an edge center, shared with chamber s0(a). The spoke from the tile center to
			// such a point does not in general bisect the angle there, so the two chambers see the boundary
			// leave at different angles and would offset the shared point by different amounts, leaving the tile
			// torn open along the spoke. Both therefore use the smaller of the two sines, i.e. the larger of the
			// two offsets, which insets the boundary by at least the requested amount on either side.
			final double[] cornerSines = {
					Math.min(a2CornerSines[a][0], a2CornerSines[dsymbol.getS1(a)][0]),
					Math.min(a2CornerSines[a][1], a2CornerSines[dsymbol.getS0(a)][1])};

			// un-inset copy, used to build the gap-free substrate
			final Point3D[] preInsetPoints = TileRelief.insetTowardsCenter(geom, chamberPoints, chamberPoints[2],
					boundaryIndices, tileGap, tileSubstrate, cornerSines);

			if (postScale != 1) {
				for (int i = 0; i < chamberPoints.length; i++) {
					chamberPoints[i] = chamberPoints[i].multiply(postScale);
				}
			}

			if (preInsetPoints != null) // the true tile boundary, i.e. the outer rim of the grout floor
				a2OuterBoundary[a] = TileRelief.extractBoundary(preInsetPoints, boundaryIndices);

			if (buildWall) { // record the (inset) tile boundary running through this chamber
				a2LipBoundary[a] = TileRelief.extractBoundary(chamberPoints, boundaryIndices);
				a2TileCenter[a] = chamberPoints[2];
			}

            final float[] chamberCoordinates = new float[3 * chamberPoints.length];

            for (int i = 0; i < chamberPoints.length; i++) {
                chamberCoordinates[3 * i] = (float) chamberPoints[i].getX();
                chamberCoordinates[3 * i + 1] = (float) chamberPoints[i].getY();
                chamberCoordinates[3 * i + 2] = (float) chamberPoints[i].getZ();
            }

            a2ChamberCoordinates[a] = chamberCoordinates;
        }

        if (tilingStyle.isShowFaces() || tilingStyle.isShowBackFaces()) { // construct triangles. All triangles belonging to flags of the same 0,1-orbit are put into a single mesh
            for (int a0 : dsymbol.orbits(0, 1)) {
                if(tilingStyle.isHideFirstTile() && a0==1)
                    continue;

                final PhongMaterial material = (PhongMaterial) tile2material[a2tile[a0]];
                final ArrayList<TriangleMesh> meshes = new ArrayList<>();

                for (int a : dsymbol.orbitMembers(0, 1, a0)) {
                    final float[] chamberCoordinates = a2ChamberCoordinates[a]; // points that support triangles
                    final int[] chamberFaces = a2ChamberFaces[a];

                    if (fDomain.getOrientation(a) != orientation) {
                        invertOrientationOfFaces(chamberFaces);
                    }

                    {
                        final TriangleMesh mesh = new TriangleMesh();
                        mesh.getPoints().addAll(chamberCoordinates);
                        mesh.getFaces().addAll(chamberFaces);
                        meshes.add(mesh);
                    }
                }
                final TriangleMesh mesh = MeshUtils.combineTriangleMeshes(meshes.toArray(new TriangleMesh[0]));
                mesh.getTexCoords().setAll(0.5f, 0, 0, 0, 1, 1);
                // no face smoothing groups: a curved surface gets explicit per-vertex normals below, and a
                // flat one needs no smoothing. What stood here filled them with the face *index*, which is a
                // bit mask, so face 3 shared bit 0 with face 1 and face 0 shared nothing with anything

                if (tilingStyle.isShowFaces()) {
					final MeshView meshView = new MeshView(MeshUtils.withSurfaceNormals(mesh, geom, false));
					TilingStyle.applyTileSpecular(material, geom);
                    meshView.setId("t=" + a2tile[a0]);
                    meshView.setMaterial(material);
                    meshes.add(mesh);
                    facesGroup.getChildren().add(meshView);
                }
                if (tilingStyle.isShowBackFaces()) {
					final MeshView meshView = new MeshView(MeshUtils.withSurfaceNormals(MeshUtils.reverseOrientation(mesh), geom, true));
					TilingStyle.applyTileSpecular(material, geom);
                    meshView.setId("t=" + a2tile[a0]);
                    meshView.setMaterial(material);
                    meshes.add(mesh);
                    facesGroup.getChildren().add(meshView);
                }
				if (buildWall && (tilingStyle.isShowFaces() || tilingStyle.isShowBackFaces())) {
					// the lip / side wall of the tile, running along the boundary of all of its chambers
					final ArrayList<Point3D[]> insetBoundaries = new ArrayList<>();
					final ArrayList<Point3D[]> outerBoundaries = new ArrayList<>();
					Point3D tileCenter = null;
					for (int a : dsymbol.orbitMembers(0, 1, a0)) {
						if (a2LipBoundary[a] != null) {
							insetBoundaries.add(a2LipBoundary[a]);
							outerBoundaries.add(a2OuterBoundary[a]);
							tileCenter = a2TileCenter[a];
						}
					}
					if (tileCenter != null) {
						final MeshView lipView = new MeshView(TileRelief.createLip(geom, insetBoundaries, tileCenter, wallDepth));
						TilingStyle.applyTileSpecular(material, geom);
						lipView.setId("t=" + a2tile[a0]);
						lipView.setMaterial(material);
						// two-sided, like the grout floor: the winding of these walls comes out reversed for
						// mirrored chambers, so back-face culling would drop the lip along some tile edges
						lipView.setCullFace(CullFace.NONE);
						facesGroup.getChildren().add(lipView);

						if (tileSubstrate && !outerBoundaries.contains(null)) {
							// the grout floor, filling the gap only, in the band color
							final PhongMaterial floorMaterial = new PhongMaterial(tilingStyle.getBandColor());
							TilingStyle.applyTileSpecular(floorMaterial, geom);
							final MeshView floorView = new MeshView(TileRelief.createFloorRing(geom, insetBoundaries, outerBoundaries, substrateDepth));
							floorView.setMaterial(floorMaterial);
							floorView.setCullFace(CullFace.NONE);
							facesGroup.getChildren().add(floorView);
						}
					}
				}
            }
        }
        if (tilingStyle.isShowEdges() || tilingStyle.isShowBackEdges()) {
            final double linesAbove = (geom == Geometry.Euclidean ? -1 : 0);

            // interlaced bands: each band rises where its strand passes over a vertex and sinks where it passes
            // under, and the depth buffer then hides the one below. In the euclidean case the viewer looks down
            // the z axis, so "above the surface" is towards smaller z, hence the sign
            final boolean weave = tilingStyle.isWeaveEdges();
            final double aboveSign = (geom == Geometry.Euclidean ? -1 : 1);
            final int[] dartLabels = (weave ? Weave.computeDartLabels(dsymbol) : null);
            final double weaveDepth = tilingStyle.getWeaveDepth();
            final PhongMaterial borderMaterial = new PhongMaterial(tilingStyle.getWeaveBorderColor());
            TilingStyle.applyBandSpecular(borderMaterial, geom);
            final double borderWidth = tilingStyle.getWeaveBorderWidth();

            for (int a0 : dsymbol.orbits(0, 2)) {
                if (invisibleEdgeFlags!=null && invisibleEdgeFlags.get(a0))
                    continue;

                final ArrayList<TriangleMesh> meshes = new ArrayList<>();
                final ArrayList<TriangleMesh> borderMeshes = new ArrayList<>();

                for (int a : dsymbol.orbitMembers(0, 2, a0)) {
                    final Point3D[] edgePoints = a2edgePoints[a];

                    if (false && geom == Geometry.Euclidean) {
                        if (fDomain.getOrientation(a) != orientation) {
                            reverseOrderOfPoints(edgePoints);
                        }
                        meshes.add(HalfBand3D.createEuclidean(edgePoints, edgeWidth[a2edge[a]], StrokeLineCap.ROUND, null)); // todo: this is broken!

                    } else if (dsymbol.getS2(a) > a || fDomain.isBoundaryEdge(2, a)) {
                        final boolean reversed = (fDomain.getOrientation(a) != orientation);
                        if (reversed) {
                            reverseOrderOfPoints(edgePoints);
                        }
                        final StrokeLineCap cap = (tilingStyle.getBandCapFineness() > 0 ? StrokeLineCap.ROUND : StrokeLineCap.BUTT);
                        final double width = edgeWidth[a2edge[a]];
                        if (weave) {
                            // these points run from the tiling vertex to the center of the edge, unless reversed
                            final Point3D[] wovenPoints = Weave.refine(geom, edgePoints);
                            final double[] height = Weave.heightProfile(dsymbol, dartLabels, a, wovenPoints.length, reversed, weaveDepth);
                            final double[] nudge = new double[height.length];
                            final double[] outlineNudge = new double[height.length];
                            for (int i = 0; i < height.length; i++) {
                                nudge[i] = linesAbove + aboveSign * height[i];
                                // The outline runs beside its own band, not beneath it, so this bias is not what
                                // makes it visible. It only settles the ties where straps meet flat at a vertex
                                // that cannot be woven: there one strap's outline meets another's band at the
                                // very same height, and without it the two speckle each other.
                                outlineNudge[i] = linesAbove + aboveSign * (height[i] - 0.05 * width);
                            }
                            meshes.add(Band3D.connect(geom, wovenPoints, width, nudge, cap));
                            borderMeshes.add(Band3D.outline(geom, wovenPoints, width, width * borderWidth, outlineNudge));
                        } else {
                            meshes.add(Band3D.connect(geom, edgePoints, width, linesAbove, cap));
                        }
                    }
                }
                if (!borderMeshes.isEmpty() && tilingStyle.isShowEdges()) {
                    // a thin strip along either side of each strap
                    final TriangleMesh mesh = MeshUtils.combineTriangleMeshes(borderMeshes.toArray(new TriangleMesh[0]));
                    mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);
                    final MeshView meshView = new MeshView(MeshUtils.withSurfaceNormals(mesh, geom, false));
                    meshView.setId("e=" + a2edge[a0]);
                    meshView.setUserData(Weave.OUTLINE); // so that the band colors are not applied to it
                    meshView.setMaterial(borderMaterial);
                    edgesGroup.getChildren().add(meshView);
                }
                if (tilingStyle.isShowEdges()) {
                    final ArrayList<TriangleMesh> list = new ArrayList<>(meshes);
                    final TriangleMesh mesh = MeshUtils.combineTriangleMeshes(list.toArray(new TriangleMesh[0]));
                    mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);

                    final MeshView meshView = new MeshView(MeshUtils.withSurfaceNormals(mesh, geom, false));
                    // material.setSpecularColor(Color.YELLOW);
                    meshView.setId("e=" + a2edge[a0]);
                    meshView.setMaterial(edge2material[a2edge[a0]]);
                    meshes.add(mesh);
                    edgesGroup.getChildren().add(meshView);

                    // test createFan:
                    //meshes.add(MeshUtils.createFan(cornerPoints[0],cornerPoints[0].add(50,0,0),cornerPoints[0].add(0,50,0),cornerPoints[0].add(50,50,0)));
                }
                if (tilingStyle.isShowBackEdges()) {
                    final ArrayList<TriangleMesh> list = new ArrayList<>(meshes);
                    final TriangleMesh mesh = MeshUtils.combineTriangleMeshes(list.toArray(new TriangleMesh[0]));
                    mesh.getTexCoords().addAll(0.5f, 0, 0, 0, 1, 1);

                    final MeshView meshView = new MeshView(MeshUtils.withSurfaceNormals(MeshUtils.reverseOrientation(mesh), geom, true));
                    // material.setSpecularColor(Color.YELLOW);
                    meshView.setId("e=" + a2edge[a0]);
                    meshView.setMaterial(edge2material[a2edge[a0]]);
                    meshes.add(mesh);
                    edgesGroup.getChildren().add(meshView);
                }
            }
        }
        if (tilingStyle.isShowVertices() || tilingStyle.isShowBackVertices()) {
            final double linesAbove = (geom == Geometry.Euclidean ? -1 : 0);

            for (int a : dsymbol.orbits(1, 2)) {
                final Point3D center = a2VertexPoints3D[a];
                // todo: need better tangent computation
                final Point3D tangent = geom == Geometry.Euclidean ? new Point3D(1, 0, 0) : center.crossProduct(new Point3D(1, 0, 0));

                final Point3D[] coordinates = BandCap3D.circle(center, tangent, bandWidth, bandCapFineness, geom);
                final TriangleMesh mesh = BandCap3D.CircleMesh(center, coordinates, geom, linesAbove, false);
                MeshUtils.setDefaultTexCoordinates(mesh);

                if (tilingStyle.isShowVertices()) {
                    final MeshView meshView = new MeshView(MeshUtils.withSurfaceNormals(mesh, geom, false));
                    // material.setSpecularColor(Color.YELLOW);
                    meshView.setId("v=" + a2vertex[a]);
                    meshView.setMaterial(vertex2material[a2vertex[a]]);
                    verticesGroup.getChildren().add(meshView);
                }
                if (tilingStyle.isShowBackVertices()) {
                    final MeshView meshView = new MeshView(MeshUtils.withSurfaceNormals(MeshUtils.reverseOrientation(mesh), geom, true));
                    // material.setSpecularColor(Color.YELLOW);
                    meshView.setId("v=" + a2vertex[a]);
                    meshView.setMaterial(vertex2material[a2vertex[a]]);
                    verticesGroup.getChildren().add(meshView);
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
     * reverse the order of points
     *
	 */
    private static void reverseOrderOfPoints(Point3D[] list) {
        for (int i = 0; i < list.length / 2; i++) {
            Point3D tmp = list[i];
            list[i] = list[list.length - i - 1];
            list[list.length - i - 1] = tmp;
        }
    }


    /**
     * computes the representation of chambers
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
