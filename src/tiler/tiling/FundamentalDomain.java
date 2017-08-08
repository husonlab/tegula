package tiler.tiling;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.main.Document;
import tiler.util.ShapeHandler;
import tiler.core.fundamental.utils.WrapInt;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

/**
 * builds fundamental domain in JavaFX Created by huson on 4/5/16.
 */
public class FundamentalDomain {
	/**
	 * construct a fundamental domain
	 *
	 * @param dsymbol
	 *            Delaney symbol from old DH code
	 * @param fDomain
	 *            domain computed by KW
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
			//final Color color = new Color(1, 1, 1, 1);  //all white
			dsymbol.visitOrbit(0, 1, a, new DSymbol.OrbitVisitor() {
				public void visit(int a) {
					colors[a] = color;
				}
			});
		}

		// construct triangles as meshes:

		final int orientation = (computeWindingNumber(fDomain.getVertex3D(0, 1), fDomain.getVertex3D(1, 1),
				fDomain.getVertex3D(2, 1)) < 0 ? fDomain.getOrientation(1) : -fDomain.getOrientation(1));

		Geometry geom = fDomain.getGeometry();

		for (int a = 1; a <= fDomain.size(); a++) {
			final float[] points;
			final Point3D[] points3d;

			final int[] faces;
			final int[] fac;

			final int[] smoothing;

			if (geom == Geometry.Spherical) {

				//Spherical
				int depth = 4; // 4^5 = 1024

				fac = new int[(int) Math.pow(4, (depth + 1)) * 6];
				points3d = new Point3D[1026]; // 3, 6, 66, 258, 1026

				WrapInt p = new WrapInt(0);
				WrapInt f = new WrapInt(0);

				// be careful, as changing the order of the numbers is crucial
				points3d[p.incrementInt()] = fDomain.getVertex3D(0, a);
				points3d[p.incrementInt()] = fDomain.getVertex3D(1, a);
				points3d[p.incrementInt()] = fDomain.getVertex3D(2, a);
				points3d[p.incrementInt()] = fDomain.getEdgeCenter3D(0, a);
				points3d[p.incrementInt()] = fDomain.getEdgeCenter3D(1, a);
				points3d[p.incrementInt()] = fDomain.getEdgeCenter3D(2, a);

				class triangle {

					private boolean orientationUp;
					private int pointA, pointB, pointC;
					private int depth;
					private triangle tri1;
					private triangle tri2;
					private triangle tri3;
					private triangle tri4;
					

					triangle(boolean orientationUp, int pointA, int pointB, int pointC, int depth) {
						this.orientationUp = orientationUp;
						this.pointA = pointA;
						this.pointB = pointB;
						this.pointC = pointC;
						this.depth = depth;

						if (this.depth > 0) {
							int midAB = p.incrementInt();
							points3d[midAB] = Tools.midpoint3D(geom, points3d[pointA], points3d[pointB]);
							int midAC = p.incrementInt();
							points3d[midAC] = Tools.midpoint3D(geom, points3d[pointA], points3d[pointC]);
							int midBC = p.incrementInt();
							points3d[midBC] = Tools.midpoint3D(geom, points3d[pointB], points3d[pointC]);

							this.tri1 = new triangle(this.orientationUp, this.pointA, midAB, midAC, --this.depth);
							this.tri2 = new triangle(this.orientationUp, midAB, this.pointB, midBC, this.depth);
							this.tri3 = new triangle(this.orientationUp, midAC, midBC, this.pointC, this.depth);

							if (this.orientationUp) {
								this.tri4 = new triangle(!this.orientationUp, midAB, midBC, midAC, this.depth);
							} else {
								this.tri4 = new triangle(!this.orientationUp, midAC, midAB, midBC, this.depth);
							}
						} else {
							int facPos = 6 * f.incrementInt();
							fac[facPos] = pointA;
							fac[facPos + 1] = 0;
							fac[facPos + 2] = pointB;
							fac[facPos + 3] = 1;
							fac[facPos + 4] = pointC;
							fac[facPos + 5] = 2;
						}
					}
				}

				// glockwise orientation -> the order of the numbers is crucial
				new triangle(true, 0, 4, 5, depth);
				new triangle(true, 5, 3, 1, depth);
				new triangle(true, 4, 2, 3, depth);
				new triangle(false, 4, 3, 5, depth);

			} else if (geom == Geometry.Euclidean) {

				// euclidian
				points3d = new Point3D[7];

				int p = 0;

				for (int i = 0; i <= 2; i++) {
					points3d[p++] = fDomain.getVertex3D(i, a);
				}
				for (int i = 0; i <= 2; i++) {
					points3d[p++] = fDomain.getEdgeCenter3D(i, a);
				}
				points3d[p++] = fDomain.getChamberCenter3D(a);

				/// original mesh structure
				int[] original = new int[] { 0, 0, 6, 1, 5, 2, // v0 cc e2
						1, 0, 5, 1, 6, 2, // v1 e2 cc
						1, 0, 6, 1, 3, 2, // v1 cc e0
						2, 0, 3, 0, 6, 2, // v2 e0 cc
						2, 0, 6, 1, 4, 2, // v2 cc e1
						0, 0, 4, 1, 6, 2 // v0 e1 cc
				};

				fac = original;

			} else {

				// hyperbolic
				points3d = new Point3D[13];

				int p = 0;

				for (int i = 0; i <= 2; i++) {
					points3d[p++] = fDomain.getVertex3D(i, a);
				}
				for (int i = 0; i <= 2; i++) {
					points3d[p++] = fDomain.getEdgeCenter3D(i, a);
				}
				points3d[p++] = fDomain.getChamberCenter3D(a);

				// hyper
				points3d[p++] = Tools.midpoint3D(geom, points3d[0], points3d[5]);
				points3d[p++] = Tools.midpoint3D(geom, points3d[5], points3d[1]);
				points3d[p++] = Tools.midpoint3D(geom, points3d[0], points3d[7]);
				points3d[p++] = Tools.midpoint3D(geom, points3d[7], points3d[5]);
				points3d[p++] = Tools.midpoint3D(geom, points3d[5], points3d[8]);
				points3d[p++] = Tools.midpoint3D(geom, points3d[8], points3d[1]);

				int[] hyper = new int[] { 0, 0, 6, 1, 9, 2, //
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

				fac = hyper;

			} // end of geometric cases

			points = new float[3 * points3d.length];

			for (int i = 0; i < points3d.length; i++) {
				points[3 * i] = (float) points3d[i].getX();
				points[3 * i + 1] = (float) points3d[i].getY();
				points[3 * i + 2] = (float) points3d[i].getZ();
			}

			if (fDomain.getOrientation(a) == orientation) {
				faces = fac;
			} else {
				faces = invertOrientation(fac);
			}

			smoothing = new int[faces.length / 6];
			Arrays.fill(smoothing, 1);

			final float[] texCoords = { 0.5f, 0, 0, 0, 1, 1 };

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
		if (true) {
			// Lines for barycentric subdivision of chambers:
			/*
			 * for (int a = 1; a <= fDomain.size(); a++) {
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getVertex3D(0, a), fDomain.getEdgeCenter3D(1, a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getEdgeCenter3D(1, a), fDomain.getVertex3D(2, a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * 
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getVertex3D(2, a), fDomain.getEdgeCenter3D(0, a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getEdgeCenter3D(0, a), fDomain.getVertex3D(1, a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * 
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getVertex3D(0, a), fDomain.getChamberCenter3D(a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getChamberCenter3D(a), fDomain.getEdgeCenter3D(0, a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * 
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getVertex3D(1, a), fDomain.getChamberCenter3D(a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getChamberCenter3D(a), fDomain.getEdgeCenter3D(1, a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * 
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getVertex3D(2, a), fDomain.getChamberCenter3D(a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f));
			 * group.getChildren().add(Cylinderline.createConnection(fDomain.
			 * getChamberCenter3D(a), fDomain.getEdgeCenter3D(2, a),
			 * Color.WHITE.deriveColor(0, 1, 1, 0.4), 0.5f)); }
			 */

			double width = 0;
			if (fDomain.getGeometry() == Geometry.Hyperbolic) {
				Point3D refPoint = fDomain.getChamberCenter3D(1).multiply(0.01);
				Point3D origin = new Point3D(0, 0, 1);
				double w = 0.01;
				double h = (1 + w * w) / (1 - w * w);
				// Length of translation
				double t = Tools.distance(fDomain, refPoint, origin);
				// Affine translation:
				Affine translateT = new Affine(Math.cosh(t), Math.sinh(t), 0, Math.sinh(t), Math.cosh(t), 0); // Translation
																												// along
																												// x-axis
				Point2D x = translateT.transform(0, 1);
				Point2D y = translateT.transform((1 + h) * w, h);

				width = 100 * (y.getX() / (1 + y.getY()) - x.getX() / (1 + x.getY()));
			} else if (fDomain.getGeometry() == Geometry.Euclidean) {
				width = 1;
			} else if (fDomain.getGeometry() == Geometry.Spherical) {
				width = 0.5;
			}

			System.out.println("Width: " + width);

			// Edges of Tiling:
			Point3D v0, e2, v1;
			int m = fDomain.size();
			BitSet visited = new BitSet(m); // ??
			int a = 1;
			// Fallunterscheidung neeeds some serious refactoring
			if (geom == Geometry.Euclidean) {
				while (a <= m) {
					if (!visited.get(a)) {
						v0 = fDomain.getVertex3D(0, a);
						e2 = fDomain.getEdgeCenter3D(2, a);
						v1 = fDomain.getVertex3D(1, a);
						group.getChildren().add(Cylinderline.createConnection(v0, e2, Color.BLACK, width));
						group.getChildren().add(Cylinderline.createConnection(e2, v1, Color.BLACK, width));
						visited.set(dsymbol.getS2(a));
					}
					a++;
				}
			} else if (geom == Geometry.Hyperbolic) {
				while (a <= m) {
					if (!visited.get(a)) {
						v0 = fDomain.getVertex3D(0, a);
						e2 = fDomain.getEdgeCenter3D(2, a);
						v1 = fDomain.getVertex3D(1, a);

						Point3D[] linePoints = new Point3D[9];
						linePoints[0] = v0;
						linePoints[4] = e2;
						linePoints[8] = v1;
						for (int i = 1; i < 4; i++) {
							linePoints[i] = Tools.interpolateHyperbolicPoints(v0, e2, i / 8d);
						}
						for (int i = 5; i < 8; i++) {
							linePoints[i] = Tools.interpolateHyperbolicPoints(e2, v1, i / 8d);
						}
						for (int i = 0; i < 8; i++) {
							group.getChildren().add(Cylinderline.createConnection(linePoints[i], linePoints[i + 1],
									Color.BLACK, width));
						}
						visited.set(dsymbol.getS2(a));
					}
					a++;
				}

			} else {
				// performanceProbleme durch Adden, also Magic numbers
				while (a <= m) {
					if (!visited.get(a)) {
						v0 = fDomain.getVertex3D(0, a);
						e2 = fDomain.getEdgeCenter3D(2, a);
						v1 = fDomain.getVertex3D(1, a);

						Point3D[] linePoints = new Point3D[33];
						linePoints[0] = v0;
						linePoints[16] = e2;
						linePoints[32] = v1;
						for (int i = 1; i < 16; i++) {
							linePoints[i] = Tools.interpolateSpherePoints(v0, e2, i / 32.0);
						}
						for (int i = 17; i < 32; i++) {
							linePoints[i] = Tools.interpolateSpherePoints(e2, v1, i / 32.0);
						}
						for (int j = 0; j < 32; j++) {
							group.getChildren().add(Cylinderline.createConnection(linePoints[1 * j],
									linePoints[1 * (j + 1)], Color.BLACK, width));
						}
						visited.set(dsymbol.getS2(a));
					}
					a++;
				}
			}

		}

		// add numbers:
		if (false) {
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

			final Transform transform = Tiling.getTransform(fDomain.getGeometry(), fDomain.getVertex3D(0, 16),
					fDomain.getVertex3D(1, 16), fDomain.getVertex3D(0, 19), fDomain.getVertex3D(1, 19), true);

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

	////// Hilfsfunktionen

	private static int[] invertOrientation(int[] arr) {
		int[] invArr = Arrays.copyOf(arr, arr.length);
		for (int i = 0; i < invArr.length / 6; i++) {
			int save = invArr[i * 6 + 2];
			invArr[i * 6 + 2] = invArr[i * 6 + 4];
			invArr[i * 6 + 4] = save;
		}

		return invArr;
	}

	private static double computeWindingNumber(Point3D a0, Point3D a1, Point3D a2) {
		return (a1.getX() - a0.getX()) * (a1.getY() + a0.getY()) + (a2.getX() - a1.getX()) * (a2.getY() + a1.getY())
				+ (a0.getX() - a2.getX()) * (a0.getY() + a2.getY());
	}

}
