package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.TriangleMesh;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;

import java.util.Arrays;

public class Line3D extends Group {

	static int faces[];
	static int facesedges[];

	public static TriangleMesh connect(Point3D point0, Point3D point1, Geometry geom, double size, double above) {

		Point3D[] points3d = new Point3D[6];
		faces = new int[6];

		// Points visualized
		// |2|----------|4|
		// |0|----------|1|
		// |3|----------|5|

		if (geom == Geometry.Euclidean) {

			Point3D zAxis = new Point3D(0, 0, 1);
			Point3D diff = point1.subtract(point0).normalize();
			Point3D normal = diff.crossProduct(zAxis).normalize();

			points3d[0] = point0;

			points3d[1] = point1;

			points3d[2] = point0.add(normal.multiply(size));

			points3d[3] = point0.add(normal.multiply(-size));

			points3d[4] = point1.add(normal.multiply(size));

			points3d[5] = point1.add(normal.multiply(-size));

			// raises points above surface
			for (int i = 0; i < points3d.length; i++) {
				points3d[i] = points3d[i].add(0, 0, above);
			}

			int[] fac = { 2, 0, 1, 1, 4, 2, 2, 0, 0, 1, 1, 2, 0, 0, 5, 1, 1, 2, 0, 0, 3, 1, 5, 2 };

			faces = fac;

		} else if (geom == Geometry.Spherical) {

			Point3D diff = point1.subtract(point0).normalize();
			Point3D normalToSphere0 = Tools.getNormalVector(point0, geom);
			Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
			Point3D normal = diff.crossProduct(normalToSphere0.normalize());

			// uses two different normal vectors for two different points
			points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));

			points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));

			points3d[2] = point0.add(normal.multiply(size)).add(normalToSphere0.normalize().multiply(above));

			points3d[3] = point0.subtract(normal.multiply(size)).add(normalToSphere0.normalize().multiply(above));

			points3d[4] = point1.add(normal.multiply(size)).add(normalToSphere1.normalize().multiply(above));

			points3d[5] = point1.subtract(normal.multiply(size)).add(normalToSphere1.normalize().multiply(above));

			int[] fac = new int[] { 2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2 };

			faces = fac;

		} else {

			Point3D diff = point0.subtract(point1);
			Point3D normalToSphere0 = Tools.getNormalVector(point0, geom); // normal vector for point 0 of points3d
																			// array
			Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
			Point3D refPoint1 = point0.add(diff); // direction of line used in function below to orientate points
													// correct
			Point3D refPoint2 = point1.add(diff);
			Point3D[] pointsForStart = Tools.equidistantHyperbolicPoints(point0, refPoint1, size);
			Point3D[] pointsForEnd = Tools.equidistantHyperbolicPoints(point1, refPoint2, size);

			points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));

			points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));

			// needs a new normal vector for each point
			Point3D normalToSphere2 = Tools.getHyperbolicNormal(pointsForStart[0]);

			points3d[2] = pointsForStart[0].add(normalToSphere2.normalize().multiply(above));

			Point3D normalToSphere3 = Tools.getHyperbolicNormal(pointsForStart[1]);

			points3d[3] = pointsForStart[1].add(normalToSphere3.normalize().multiply(above));

			Point3D normalToSphere4 = Tools.getHyperbolicNormal(pointsForEnd[0]);

			points3d[4] = pointsForEnd[0].add(normalToSphere4.normalize().multiply(above));

			Point3D normalToSphere5 = Tools.getHyperbolicNormal(pointsForEnd[1]);

			points3d[5] = pointsForEnd[1].add(normalToSphere5.normalize().multiply(above));

			// original structure does not use different normal vectors
			// Point3D diff = point1.subtract(point0).normalize();
			// Point3D normalToSphere0 = Tools.getNormalVector(point0, geom);
			// Point3D normalToSphere1 = Tools.getNormalVector(point1, geom);
			// Point3D normal = diff.crossProduct(normalToSphere0.normalize());
			//
			// points3d[0] = point0.add(normalToSphere0.normalize().multiply(above));
			//
			// points3d[1] = point1.add(normalToSphere1.normalize().multiply(above));
			//
			// points3d[2] =
			// point0.add(normal.multiply(size)).add(normalToSphere0.normalize().multiply(above));
			//
			// points3d[3] =
			// point0.subtract(normal.multiply(size)).add(normalToSphere0.normalize().multiply(above));
			//
			// points3d[4] =
			// point1.add(normal.multiply(size)).add(normalToSphere1.normalize().multiply(above));
			//
			// points3d[5] =
			// point1.subtract(normal.multiply(size)).add(normalToSphere1.normalize().multiply(above));

			int[] fac = new int[] { 2, 0, 4, 1, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 1, 1, 5, 2, 0, 0, 5, 1, 3, 2 };

			faces = fac;

		}

		// creates mesh
		float[] points = new float[3 * points3d.length];

		for (int i = 0; i < points3d.length; i++) {
			points[3 * i] = (float) points3d[i].getX();
			points[3 * i + 1] = (float) points3d[i].getY();
			points[3 * i + 2] = (float) points3d[i].getZ();
		}

		final float[] texCoords = { 0.5f, 0, 0, 0, 1, 1 };
		int[] smoothing = new int[faces.length / 6];
		Arrays.fill(smoothing, 1);

		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().addAll(points);
		mesh.getTexCoords().addAll(texCoords);
		mesh.getFaces().addAll(faces);
		mesh.getFaceSmoothingGroups().addAll(smoothing);

		return mesh;

	}

	// Attempt to draw lines with edges needs a lot of refactoring, not sure if
	// useful
	public static TriangleMesh[] connectEdges(Point3D point0, Point3D point1, FDomain fDomain, double size,
			double edgesize, double above) {

		Point3D[] points3d = new Point3D[0];

		if (fDomain.getGeometry() == Geometry.Euclidean) {

			points3d = new Point3D[8];

			Point3D zAxis = new Point3D(0, 0, 1);
			Point3D diff = point1.subtract(point0).normalize();
			Point3D normal = diff.crossProduct(zAxis).normalize();

			points3d[0] = point0.add(normal.multiply(size));

			points3d[1] = point0.subtract(normal.multiply(size));

			points3d[2] = point1.add(normal.multiply(size));

			points3d[3] = point1.subtract(normal.multiply(size));

			points3d[4] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point0, points3d[0]);
			points3d[5] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point0, points3d[1]);
			points3d[6] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point1, points3d[2]);
			points3d[7] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point1, points3d[3]);

			faces = new int[6];

			int[] fac = new int[] { 4, 0, 7, 1, 6, 2, 4, 0, 5, 1, 7, 2, };
			int[] facedges = new int[] { 0, 0, 6, 1, 2, 2, 0, 0, 4, 1, 6, 2, 5, 0, 3, 1, 7, 2, 5, 0, 1, 1, 3, 2 };

			faces = fac;
			facesedges = facedges;

		} else if (fDomain.getGeometry() == Geometry.Spherical) {

			points3d = new Point3D[16];

			Point3D diff = point1.subtract(point0).normalize();
			Point3D normalToSphere = new Point3D(2 * point0.getX(), 2 * point0.getY(), 2 * point0.getZ());
			Point3D normal = diff.crossProduct(normalToSphere.normalize());

			points3d[0] = point0.add(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			points3d[1] = point0.subtract(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			points3d[2] = point1.add(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			points3d[3] = point1.subtract(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			point0.add(normalToSphere.normalize().multiply(above));
			point1.add(normalToSphere.normalize().multiply(above));

			points3d[4] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point0, points3d[0]);
			points3d[5] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point0, points3d[1]);
			points3d[6] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point1, points3d[2]);
			points3d[7] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point1, points3d[3]);

			//
			points3d[8] = points3d[0].add(normalToSphere.normalize().multiply(above));
			points3d[9] = points3d[1].add(normalToSphere.normalize().multiply(above));
			points3d[10] = points3d[2].add(normalToSphere.normalize().multiply(above));
			points3d[11] = points3d[3].add(normalToSphere.normalize().multiply(above));
			points3d[12] = points3d[4].add(normalToSphere.normalize().multiply(above));
			points3d[13] = points3d[5].add(normalToSphere.normalize().multiply(above));
			points3d[14] = points3d[6].add(normalToSphere.normalize().multiply(above));
			points3d[15] = points3d[7].add(normalToSphere.normalize().multiply(above));

			faces = new int[6];

			int[] fac = new int[] { 0, 0, 2, 1, 3, 2, 0, 0, 3, 1, 1, 2, };

			int[] facedges = new int[] { 8, 0, 10, 1, 14, 2, 8, 0, 14, 1, 12, 2, 13, 0, 15, 1, 11, 2, 13, 0, 11, 1, 9,
					2 };

			faces = fac;
			facesedges = facedges;

		} else {

			points3d = new Point3D[16];

			Point3D diff = point1.subtract(point0).normalize();
			Point3D normalToSphere = new Point3D(2 * point0.getX(), 2 * point0.getY(), -2 * point0.getZ());
			Point3D normal = diff.crossProduct(normalToSphere.normalize());

			points3d[0] = point0.add(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			points3d[1] = point0.subtract(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			points3d[2] = point1.add(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			points3d[3] = point1.subtract(normal.multiply(size)).add(normalToSphere.normalize().multiply(above));

			points3d[4] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point0, points3d[0]);
			points3d[5] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point0, points3d[1]);
			points3d[6] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point1, points3d[2]);
			points3d[7] = tiler.tiling.Tools.midpoint3D(Geometry.Euclidean, point1, points3d[3]);

			//
			points3d[8] = points3d[0].add(normalToSphere.normalize().multiply(above));
			points3d[9] = points3d[1].add(normalToSphere.normalize().multiply(above));
			points3d[10] = points3d[2].add(normalToSphere.normalize().multiply(above));
			points3d[11] = points3d[3].add(normalToSphere.normalize().multiply(above));
			points3d[12] = points3d[4].add(normalToSphere.normalize().multiply(above));
			points3d[13] = points3d[5].add(normalToSphere.normalize().multiply(above));
			points3d[14] = points3d[6].add(normalToSphere.normalize().multiply(above));
			points3d[15] = points3d[7].add(normalToSphere.normalize().multiply(above));

			faces = new int[6];

			int[] fac = new int[] { 0, 0, 2, 1, 3, 2, 0, 0, 3, 1, 1, 2, };

			int[] facedges = new int[] { 8, 0, 10, 1, 14, 2, 8, 0, 14, 1, 12, 2, 13, 0, 15, 1, 11, 2, 13, 0, 11, 1, 9,
					2 };

			faces = fac;
			facesedges = facedges;

		}
		float[] points = new float[3 * points3d.length];

		for (int i = 0; i < points3d.length; i++) {
			points[3 * i] = (float) points3d[i].getX();
			points[3 * i + 1] = (float) points3d[i].getY();
			points[3 * i + 2] = (float) points3d[i].getZ();
		}

		final float[] texCoords = { 0.5f, 0, 0, 0, 1, 1 };
		int[] smoothing = new int[faces.length / 6];
		Arrays.fill(smoothing, 1);
		int[] smoothing2 = new int[facesedges.length / 6];
		Arrays.fill(smoothing2, 1);

		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().addAll(points);
		mesh.getTexCoords().addAll(texCoords);
		mesh.getFaces().addAll(faces);
		mesh.getFaceSmoothingGroups().addAll(smoothing);

		TriangleMesh meshEdge = new TriangleMesh();
		meshEdge.getPoints().addAll(points);
		meshEdge.getTexCoords().addAll(texCoords);
		meshEdge.getFaces().addAll(facesedges);
		meshEdge.getFaceSmoothingGroups().addAll(smoothing2);

		TriangleMesh[] meshes = new TriangleMesh[2];
		meshes[0] = mesh;
		meshes[1] = meshEdge;
		return meshes;
	}

}
