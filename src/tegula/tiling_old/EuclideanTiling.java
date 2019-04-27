/*
 * EuclideanTiling.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tiling_old;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import tegula.core.dsymbols.DSymbol;
import tegula.main.TilingStyle;
import tegula.tiling_old.util.QuadTree;
import tegula.util.JavaFXUtils;

import java.util.*;

import static tegula.Options.showReferencePoints;

/**
 * computes a tiling represented by meshes from a fundamental domain represented by meshes
 * <p>
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class EuclideanTiling extends TilingBase {
    final QuadTree refPointsQuadTree = new QuadTree(); // Saves reference points of tiles

    final ArrayList<Node> all = new ArrayList<>();

    private final Stack<Group> unusedCopies = new Stack<>();

    private Transform transformRecycled = new Translate();
    private Group euclideanFund = new Group();

    private int numberOfCopies;

    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(1);

    private boolean replaced=false;

    /**
     * constructor
     *
     * @param tilingStyle
     * @param ds
     */
    public EuclideanTiling(Pane pane, TilingStyle tilingStyle, DSymbol ds) {

        super(pane, tilingStyle, ds);
    }

    /**
     * create tiling
     *
     * @param reset
     */
    public ArrayList<Node> createTiling(boolean reset) {
            final Point3D refPoint;

            if (reset) {
                replaced=false;
                refPointsQuadTree.clear();
                all.clear();
                unusedCopies.clear();

                setNumberOfCopies(0);
                fundamentalDomain.update(ds, fDomain, tilingStyle);

                final Group fund = fundamentalDomain.getAll();
                refPoint = fundamentalDomain.getReferencePoint();
                fund.setRotationAxis(refPoint);

                tolerance = computeTolerance(fDomain.getGeometry(), refPoint, generators);

                all.add(fund);
                fund.getTransforms().add(new Translate()); // Add transform (= identity)

                setEuclideanFund(fund); // Saves the original fundamental domain
                fund.setUserData(fundamentalDomain.getReferenceSpheres().getChildren().get(0));

                refPointsQuadTree.insert(refPoint.getX(), refPoint.getY(), tolerance);

                if (showReferencePoints)
                    getEuclideanFund().getChildren().addAll(fundamentalDomain.getReferenceSpheres().getChildren());
            } else {
               if(!JavaFXUtils.screenCoordinatesIntersectBounds(pane, (Node) getEuclideanFund().getUserData())) {
                   final Group visibleFund=findVisibleFund(pane,all,getEuclideanFund());
                   if(visibleFund!=null && visibleFund!=getEuclideanFund()) {
                       final Transform transform=visibleFund.getTransforms().get(0);
                       getEuclideanFund().getTransforms().setAll(transform);
                       getEuclideanFund().setRotationAxis(visibleFund.getRotationAxis());
                       all.remove(visibleFund);
                       unusedCopies.add(visibleFund);
                       ((Sphere)getEuclideanFund().getUserData()).setMaterial(new PhongMaterial(Color.BLACK));
                       System.err.println("Replaced fundamental domain");
                       replaced=true;
                   }
                   else
                       System.err.println("Nothing found");
               }
                refPoint = getEuclideanFund().getRotationAxis();


                if(false) {
                   all.remove(getEuclideanFund());
                   for (Node node : all)
                       if (node instanceof Group)
                           unusedCopies.add((Group) node);
                   all.clear();

                   refPointsQuadTree.clear();
                   {
                       all.add(getEuclideanFund());
                       final Point3D ref = getEuclideanFund().getRotationAxis();
                       refPointsQuadTree.insert(ref.getX(), ref.getY(), tolerance);
                   }
               }

               else {
                   // update quadtree:
                   refPointsQuadTree.clear();
                   for (Node node : all) {
                       if (node instanceof Group && node.getUserData() instanceof Node) {
                           final Point3D ref = node.getRotationAxis();
                           refPointsQuadTree.insert(ref.getX(), ref.getY(), tolerance);
                       }
                   }
               }

                System.err.println("Starting with: " + all.size() + " " + refPointsQuadTree.size());
            }
            final int oldNumberOfCopies=all.size();

            double minX=Double.MAX_VALUE;
            double minY=Double.MAX_VALUE;
            double maxX=Double.MIN_VALUE;
            double maxY=Double.MIN_VALUE;

            System.err.println(String.format("Ref point: %.1f %.1f",refPoint.getX(),refPoint.getY()));

            if (!isDrawFundamentalDomainOnly()) {
                final QuadTree seen = new QuadTree(); // points generated in this computation

                final Queue<Transform> queue = new LinkedList<>(); // Add generators

                for (Transform generator : generators.getTransforms()) {
                    queue.add(generator);

                    final Point3D copyRefPoint = generator.transform(refPoint);

                    minX=Math.min(minX,copyRefPoint.getX());
                    maxX=Math.max(maxX,copyRefPoint.getX());
                    minY=Math.min(minY,copyRefPoint.getY());
                    maxY=Math.max(maxY,copyRefPoint.getY());

                    System.err.println(String.format("Neighbor: %.1f %.1f",copyRefPoint.getX(),copyRefPoint.getY()));


                    if (isInRangeEuclidean(copyRefPoint, getWidthToCover(), getHeightToCover()) && seen.insert(copyRefPoint.getX(), copyRefPoint.getY(), tolerance)) {
                        //System.err.println("old quad: "+Basic.toString(refPointsQuadTree.values()," "));

                        if (refPointsQuadTree.insert(copyRefPoint.getX(), copyRefPoint.getY(), tolerance)) {
                            all.add(provideCopy(generator, copyRefPoint, getEuclideanFund()));

                           // System.err.println("ref: "+copyRefPoint);
                           // System.err.println("new quad: "+Basic.toString(refPointsQuadTree.values()," "));
                           // System.err.println();
                        }
                    }
                }

                while (queue.size() > 0) {
                     final Transform transform = queue.poll(); // remove transform from queue

                    for (Transform generator : generators.getTransforms()) { // Creates new transforms for copies
                        {
                            final Transform tg = transform.createConcatenation(generator);
                            final Point3D copyRefPoint = tg.transform(refPoint); // Reference point corresponding to transform tg

                            minX=Math.min(minX,copyRefPoint.getX());
                            maxX=Math.max(maxX,copyRefPoint.getX());
                            minY=Math.min(minY,copyRefPoint.getY());
                            maxY=Math.max(maxY,copyRefPoint.getY());


                            if (isInRangeEuclidean(copyRefPoint, getWidthToCover(), getHeightToCover()) && seen.insert(copyRefPoint.getX(), copyRefPoint.getY(), tolerance)) {
                                //if(!replaced)
                                    queue.add(tg);
                                if (refPointsQuadTree.insert(copyRefPoint.getX(), copyRefPoint.getY(), tolerance))
                                    all.add(provideCopy(tg, copyRefPoint, getEuclideanFund()));
                            }
                        }

                        {
                            final Transform gt = generator.createConcatenation(transform);
                            final Point3D copyRefPoint = gt.transform(refPoint);

                            minX=Math.min(minX,copyRefPoint.getX());
                            maxX=Math.max(maxX,copyRefPoint.getX());
                            minY=Math.min(minY,copyRefPoint.getY());
                            maxY=Math.max(maxY,copyRefPoint.getY());


                            if (isInRangeEuclidean(copyRefPoint, getWidthToCover(), getHeightToCover()) && seen.insert(copyRefPoint.getX(), copyRefPoint.getY(), tolerance)) {
                               // if(!replaced)
                                    queue.add(gt);
                                if (refPointsQuadTree.insert(copyRefPoint.getX(), copyRefPoint.getY(), tolerance))
                                    all.add(provideCopy(gt, copyRefPoint, getEuclideanFund()));
                            }
                        }
                        if (all.size() > 10000) {
                            break;
                        }

                    }
                    if(false && !reset && all.size()>1000 && all.size()>10*oldNumberOfCopies) // unstable?
                        return FAILED;
                    if (all.size() > 10000) {
                         System.err.println("Too many copies: " + all.size());
                        break;
                    }
                }
            }

        System.err.println(String.format("Pane: %.1f x %.1f",pane.getWidth(),pane.getHeight()));

        System.err.println(String.format("Range: %.1f - %.1f  %.1f - %.1f",-0.6 * getWidthToCover() ,0.6 * getWidthToCover(),-0.6 * getHeightToCover() ,0.6 * getHeightToCover()));
            System.err.println(String.format("minX: %.1f",minX));
        System.err.println(String.format("maxX: %.1f",maxX));
        System.err.println(String.format("minY: %.1f",minY));
        System.err.println(String.format("maxY: %.1f",maxY));

            System.err.println("to cover: " + getWidthToCover() + " " + getHeightToCover());

            System.err.println("ending with: " + all.size() + " " + refPointsQuadTree.size());


            if (reset) {
                // only want one copy of these things:
                if (tilingStyle.isShowFundamentalChambers() && !tilingStyle.isShowAllChambers())
                    all.add(fundamentalDomain.getChambers());
            }

                if (true) {
                    final Point2D windowCorner = new Point2D(-0.5 * getWidthToCover() - 10, -0.5 * getHeightToCover() - 10);
                    Rectangle rectangle = new Rectangle(windowCorner.getX(), windowCorner.getY(), getWidthToCover(), getHeightToCover());
                    rectangle.setFill(Color.TRANSPARENT);
                    rectangle.setStroke(Color.DARKRED);
                    rectangle.setTranslateZ(10);
                    all.add(rectangle);
                }

                removeCopiesOutsideVisibleRectangle(all);

            setNumberOfCopies(all.size());

            return all;
    }

    private Group findVisibleFund(Pane pane, ArrayList<Node> all,Group fund) {
            final Node ref0 = JavaFXUtils.find("ref0", fund);
            final Node ref1 = JavaFXUtils.find("ref1", fund);
            final Node ref2 = JavaFXUtils.find("ref2", fund);

            final Point2D point0 = getCoordinatesInPane(pane, ref0);
            final Point2D point1 = getCoordinatesInPane(pane, ref1);
            final Point2D point2 = getCoordinatesInPane(pane, ref2);

            if(point0!=null && point1!=null && point2!=null) {

                for (Node node : all) {
                    if (node instanceof Group && node.getUserData() instanceof Node) {
                        final Node nRef0 = JavaFXUtils.find("ref0", node);
                        if (JavaFXUtils.screenCoordinatesIntersectBounds(pane, nRef0)) {
                            final Node nRef1 = JavaFXUtils.find("ref1", node);
                            final Node nRef2 = JavaFXUtils.find("ref2", node);
                            final Point2D nPoint0 = getCoordinatesInPane(pane, nRef0);
                            final Point2D nPoint1 = getCoordinatesInPane(pane, nRef1);
                            final Point2D nPoint2 = getCoordinatesInPane(pane, nRef2);


                            if(nPoint0!=null && nPoint1!=null && nPoint2!=null) {

                                final Point2D delta0 = point0.subtract(nPoint0);
                                final Point2D delta1 = point1.subtract(nPoint1).subtract(delta0);
                                final Point2D delta2 = point2.subtract(nPoint2).subtract(delta0);

                                if (delta1.magnitude() < 0.01 && delta2.magnitude() < 0.01) {
                                    System.err.println("Found insider");
                                    return (Group) node;
                                }
                            }
                        }
                    }
                }
            }
        return null;
    }

    /**
     * translate a euclidean tiling
     *
     * @param dx
     * @param dy
     * @param tiles
     */
    public void translateTiling(double dx, double dy, Group tiles) {
        final Translate translate = new Translate(dx, dy, 0); // Mouse translation (MouseHandler)

        for (Node node : all) {
            if (node instanceof Group && node.getTransforms().size() > 0) {
                final Transform nodeTransform = node.getTransforms().get(0); // get transform of node
                node.getTransforms().setAll(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)

                Point3D referencePoint = node.getRotationAxis(); // use rotation axis to store reference point

                referencePoint=referencePoint.add(dx*getZoomFactor()/100,dy*getZoomFactor()/100,0);
                //referencePoint = translate.transform(referencePoint);
                node.setRotationAxis(referencePoint);

                if(false) {
                    Sphere sphere = new Sphere(5);
                    sphere.setTranslateX(referencePoint.getX());
                    sphere.setTranslateY(referencePoint.getY());
                    sphere.setMaterial(new PhongMaterial(Color.BLACK));
                    ((Group) node).getChildren().add(sphere);
                }

            }
        }

        System.err.println("Fund visible: " + JavaFXUtils.screenCoordinatesIntersectBounds(pane, (Node) getEuclideanFund().getUserData()));

        if (true) {
            ArrayList<Node> result=createTiling(false);
            if(result==FAILED)
                result=createTiling(true);
                tiles.getChildren().setAll(result);
        } else {
            final Group fund = getEuclideanFund();

            final Node ref0 = (Node) fund.getUserData();
            if (ref0 instanceof Sphere) {
                if (!JavaFXUtils.screenCoordinatesIntersectBounds(pane, ref0)) {
                    final Node ref1 = JavaFXUtils.find("ref1", fund);
                    final Node ref2 = JavaFXUtils.find("ref2", fund);

                    if (ref1 instanceof Sphere && ref2 instanceof Sphere) {
                        final Point2D point0 = getCoordinatesInPane(pane, ref0);
                        final Point2D point1 = getCoordinatesInPane(pane, ref1);
                        final Point2D point2 = getCoordinatesInPane(pane, ref2);

                        for (Node copy : all) {
                            if (copy != fund) {
                                final Node nRef0 = JavaFXUtils.find("ref0", ((Group) copy).getChildren());
                                if (nRef0 instanceof Sphere) {
                                    if (JavaFXUtils.screenCoordinatesIntersectBounds(pane, nRef0)) { // this ref is visible
                                        final Node nRef1 = JavaFXUtils.find("ref1", ((Group) copy).getChildren());
                                        final Node nRef2 = JavaFXUtils.find("ref2", ((Group) copy).getChildren());
                                        if (nRef1 instanceof Sphere && nRef2 instanceof Sphere) {
                                            final Point2D nPoint0 = getCoordinatesInPane(pane, nRef0);
                                            final Point2D nPoint1 = getCoordinatesInPane(pane, nRef1);
                                            final Point2D nPoint2 = getCoordinatesInPane(pane, nRef2);

                                            final Point2D delta0 = point0.subtract(nPoint0);
                                            final Point2D delta1 = point1.subtract(nPoint1).subtract(delta0);
                                            final Point2D delta2 = point2.subtract(nPoint2).subtract(delta0);


                                            if (delta1.magnitude() < 0.01 && delta2.magnitude() < 0.01) {
                                                System.err.println("Found insider");
                                                ((Sphere) nRef0).setMaterial(new PhongMaterial(Color.BLACK));
                                                ((Sphere) ref0).setMaterial(new PhongMaterial(Color.GRAY));
                                                // make this the new fund
                                                setEuclideanFund((Group) copy);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    }
                }
            }
        }

    }

    /**
     * provides a copy of the fundamental domain
     *
     * @param transform
     * @param refPoint
     * @param fund
     * @return copy
     */
    private Group provideCopy(Transform transform, Point3D refPoint, Group fund) {
        final Group copy;
        if (unusedCopies.size() > 0)
            copy = unusedCopies.pop();
        else
            copy = JavaFXUtils.copyGroup(fund);

        copy.getTransforms().setAll(fund.getTransforms().get(0).createConcatenation(transform));

        copy.setRotationAxis(refPoint);

        if(false) {
            Sphere sphere = new Sphere(8);
            sphere.setTranslateX(refPoint.getX());
            sphere.setTranslateY(refPoint.getY());
            sphere.setMaterial(new PhongMaterial(Color.BLACK.deriveColor(1,1,1,0.5)));
            copy.getChildren().add(sphere);
        }

        copy.setUserData(JavaFXUtils.find("ref0", copy));

        return copy;
    }


    public void removeCopiesOutsideVisibleRectangle(ArrayList<Node> all) {
        final int originalSize = all.size();
        ArrayList<Node> toKeep = new ArrayList<>(all.size());

        // todo: this need tuning to proper size:
        final Bounds paneBounds= JavaFXUtils.grow(pane.localToScreen(pane.getBoundsInParent()),200*getZoomFactor(),200*getZoomFactor());


        for (Node node : all) {
            if (node instanceof Group) {
                final Group group = (Group) node;
                if (group == euclideanFund || !(group.getUserData() instanceof Node)) {
                    toKeep.add(group);
                } else {
                    final Node ref0 = (Node) group.getUserData();

                    if (ref0 == null)
                        toKeep.add(group);
                    else {
                        final Bounds refBounds = ref0.localToScreen(ref0.getBoundsInParent());
                        if (refBounds == null)
                            toKeep.add(node);
                        else {
                            if (JavaFXUtils.screenCoordinatesIntersectBounds(paneBounds, ref0))
                                toKeep.add(group);
                            else {
                                unusedCopies.add(group);
                            }
                        }
                    }
                }
            } else
                toKeep.add(node);
        }
        if (toKeep.size() == 0)
            all.clear();
        else if (toKeep.size() < all.size()) {
            all.clear();
            all.addAll(toKeep);
        }

        if (all.size() < originalSize) {
            System.err.println("Removed: " + (originalSize + " -> " + all.size()));
            System.err.println("unused copies: " + unusedCopies.size());

        }
    }


    private double getWidthToCover() {
        return 800/getZoomFactor(); //Math.max(400, pane.getWidth() / getZoomFactor())/100;
    }

    private double getHeightToCover() {
        return 800/getZoomFactor(); //Math.max(400, pane.getHeight() / getZoomFactor())/100;
    }


    /**
     * Euclidean case: Checks whether "point" is in valid range
     *
     * @param point
     * @param width
     * @param height
     * @return
     */
    public boolean isInRangeEuclidean(Point3D point, double width, double height) {

        return -0.6 * width <= point.getX() && point.getX() <= 0.6 * width && -0.6 * height <= point.getY() && point.getY() <= 0.6 * height;
    }

    public void setEuclideanFund(Group g) {
        euclideanFund = g;
    }

    public Group getEuclideanFund() {
        return euclideanFund;
    }


    public Transform getTransformRecycled() {
        return transformRecycled;
    }

    public void setTransformRecycled(Transform transformRecycled) {
        this.transformRecycled = transformRecycled;
    }

    public int getReferenceChamberIndex() {
        return referenceChamberIndex;
    }

    public void setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public double getZoomFactor() {
        return zoomFactor.get();
    }

    public DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }


    /**
     * get the main coordinates of a node
     *
     * @param pane
     * @param shape
     * @return pane coordinates
     */
    private static Point2D getCoordinatesInPane(Pane pane, Node shape) {
        if(pane!=null && shape!=null) {
            final Bounds paneBounds = pane.localToScreen(pane.getBoundsInParent());
            return getCoordinatesInPane(paneBounds, shape);
        }
            else
    return null;
    }

    /**
     * get the main coordinates of a node
     *
     * @param paneBounds
     * @param shape
     * @return pane coordinates
     */
    private static Point2D getCoordinatesInPane(Bounds paneBounds, Node shape) {
        if(paneBounds!=null && shape!=null) {
            final Bounds refBounds = shape.localToScreen(shape.getBoundsInParent());
            if (refBounds != null)
                return new Point2D(0.5 * (refBounds.getMinX() + refBounds.getMaxX()) - paneBounds.getMinX(),
                        0.5 * (refBounds.getMinY() + refBounds.getMaxY()) - paneBounds.getMinY());
        }
            return null;
    }

}


