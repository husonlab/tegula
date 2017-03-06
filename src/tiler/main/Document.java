/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package tiler.main;

import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.transform.Affine2D;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.tiling.OctTree;
import tiler.tiling.QuadTree;
import tiler.tiling.Tiling;
import tiler.util.JavaFXUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * document
 * Created by huson on 4/22/16.
 */
public class Document {

    public static final int FIRST = 0;
    public static final int NEXT = -1;
    public static final int PREV = -2;
    public static final int LAST = -3;

    private final ArrayList<Tiling> tilings = new ArrayList<>();
    private int current = -1;

    private final Group world;
    private final Controller controller;
    private PerspectiveCamera camera;
    private AmbientLight light = new AmbientLight();

    private boolean camPoincare = true; // Variable saving camera settings

    private boolean drawFundamentalDomainOnly = false;

    public static boolean isBreak = false;

    private boolean changeDirection;

    private Point2D vec = new Point2D(0,0);

    private int limitHyperbolicGroup = 5;

    private final SimpleObjectProperty<Geometry> geometryProperty = new SimpleObjectProperty<>();

    /**
     * constructor
     */
    public Document(Stage stage, Group world, Controller controller, PerspectiveCamera camera) {
        this.world = world;
        this.controller = controller;
        this.camera = camera;
        controller.setDocument(this);
        controller.setStage(stage);
    }

    /**
     * clear the D-symbols
     */
    public void clear() {
        tilings.clear();
        current = -1;
    }

    /**
     * read a file of Delaney symbols
     *
     * @param reader
     * @throws IOException
     */
    public void read(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            DSymbol dSymbol = new DSymbol();
            dSymbol.read(new StringReader(line));
            tilings.add(new Tiling(dSymbol));
        }
        if (tilings.size() > 0)
            current = 0;
    }

    public int size() {
        return tilings.size();
    }

    /**
     * get one of the D-symbols and update the value of current
     *
     * @param which, between 0 and size()-1, or one of FIRST, NEXT, PREV and LAST
     * @return get the indicated symbol
     */
    public boolean moveTo(int which) {
        int old = current;
        switch (which) {
            case NEXT:
                current = Math.min(size() - 1, current + 1);
                break;
            case PREV:
                current = Math.max(0, current - 1);
                break;
            case FIRST:
                current = 0;
                break;
            case LAST:
                current = size() - 1;
                break;
            default:
                current = Math.max(0, Math.min(size() - 1, which));
                break;
        }
        return current != old;
    }

    public Group getWorld() {
        return world;
    }

    public Controller getController() {
        return controller;
    }

    public Tiling getCurrent() {
        return tilings.get(current);
    }

    public void setCurrent(Tiling tiling) {
        tilings.set(current, tiling);
        geometryProperty.setValue(tiling.getfDomain().getGeometry());
    }

    public Point3D windowCorner = new Point3D(0,0,0); // Upper left corner of window in Euclidean case
    private double width=600, height=600; //Width and height of window

    private Group tiles = new Group();
    public static int numberOfCopies; //Counts number of copies.

    public void update() {
        final Tiling tiling = tilings.get(current);
        geometryProperty().setValue(tiling.getGeometry());

        tiles.getChildren().clear();

        // Empty recycler for copies and reset transform for recycled copies.
        getRecycler().getChildren().clear();
        setTransformRecycled(new Translate());
        changeDirection = false; // No direction has to be changed

        Rectangle rect = new Rectangle(), range = new Rectangle(), test = new Rectangle(), test2 = new Rectangle(); //Rectangles for Debugging

        //Euclidean case -----------------------------------------------------------------------------------------------
        if (tiling.getGeometry() == Geometry.Euclidean) {
            // Reset Euclidean fundamental domain and QuadTree
            setEuclideanFund(new Group());
            setKeptEuclideanCopy(new QuadTree());

            if (!isDrawFundamentalDomainOnly() && !tiling.isInRangeEuclidean(tiling.refPointEuclidean, windowCorner, width, height)) { // Worst case: refPoint is not in valid range
                recenterFDomain(tiling.calculateBackShiftEuclidean(windowCorner, width, height)); // Shifts back fDomain into valid range (slower algorithm)
                tiling.setResetEuclidean(true); // Variable to calculate a transform leading back into the visible window
                tiles = tiling.createTilingEuclidean(isDrawFundamentalDomainOnly(), windowCorner, width, height, 0, 0);
                recenterFDomain(tiling.transformFDEuclidean); // Shifts back fDomain into visible window (faster algorithm)
                setTransformRecycled(tiling.transformFDEuclidean); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
            }
            else { // If fDomain is out of visible window
                if (!isDrawFundamentalDomainOnly() && !tiling.isInWindowEuclidean(tiling.refPointEuclidean, windowCorner, width, height)) {
                    tiling.setResetEuclidean(true);
                    tiles = tiling.createTilingEuclidean(isDrawFundamentalDomainOnly(), windowCorner, width, height, 0, 0);
                    recenterFDomain(tiling.transformFDEuclidean); // Shifts back fDomain into visible window (fast algorithm)
                    setTransformRecycled(tiling.transformFDEuclidean); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
                }
                else { // If fDomain is inside visible window
                    tiles = tiling.createTilingEuclidean(isDrawFundamentalDomainOnly(), windowCorner, width, height, 0, 0);
                }
            }

            numberOfCopies = tiles.getChildren().size();

            //Add rectangles for debugging
            rect = new Rectangle(width, height);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.BLACK);
            range = new Rectangle(width+250,height+250);
            range.setFill(Color.TRANSPARENT);
            range.setStroke(Color.BLACK);
            test = new Rectangle(width+200, height+200);
            test.setFill(Color.TRANSPARENT);
            test.setStroke(Color.BLACK);
            test2 = new Rectangle(width+150, height+150);
            test2.setFill(Color.TRANSPARENT);
            test2.setStroke(Color.BLACK);

            //Camera options
            camera.setTranslateZ(-500);
            camera.setFarClip(10000);

            controller.getPoincareButton().setVisible(false);
            controller.getKleinButton().setVisible(false);
            controller.getIncreaseButton().setVisible(false);
            controller.getDecreaseButton().setVisible(false);
            controller.getCBPullFDomain().setVisible(true);
        }

        // Spherical case ----------------------------------------------------------------------------------------------
        else if (tiling.getGeometry() == Geometry.Spherical) {
            tiles = tiling.createTilingSpherical(isDrawFundamentalDomainOnly());

            camera.setTranslateZ(-500);
            camera.setFieldOfView(35);
            camera.setFarClip(600);

            controller.getPoincareButton().setVisible(false);
            controller.getKleinButton().setVisible(false);
            controller.getIncreaseButton().setVisible(false);
            controller.getDecreaseButton().setVisible(false);
            controller.getCBPullFDomain().setVisible(false);

        }

        // Hyperbolic case ---------------------------------------------------------------------------------------------
        else if (tiling.getGeometry() == Geometry.Hyperbolic) {
            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());  // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.
            //System.out.println("Height of hyperboloid " + 100*maxDist);

            // Reset hyperbolic fundamental domain.
            setHyperbolicFund(new Group());
            setKeptHyperbolicCopy(new OctTree());

            //Reset Fundamental Domain if necessary:
            if (!isDrawFundamentalDomainOnly() && Tiling.refPointHyperbolic.getZ() >= maxDist){// Worst case: fDomain is out of range and must be translated back
                recenterFDomain(tiling.calculateBackShiftHyperbolic(maxDist)); // Shifts back fDomain into valid range (slower algorithm)
                tiling.setResetHyperbolic(true); // Variable to calculate a transform leading back into the visible window
                tiles = tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist, 0, 0);
                recenterFDomain(tiling.transformFDHyperbolic); // Shifts back fDomain into visible window (faster algorithm)
            }
            else {
                if (!isDrawFundamentalDomainOnly() && (Tiling.refPointHyperbolic.getZ() >= 2.5 || Tiling.refPointHyperbolic.getZ() >= 0.6 * maxDist)) {
                    tiling.setResetHyperbolic(true);
                    tiles = tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist, 0, 0);
                    recenterFDomain(tiling.transformFDHyperbolic);
                }
                else {
                    tiles = tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist, 0, 0);
                }
            }

            numberOfCopies = tiles.getChildren().size();

            //Camera settings:
            camera.setFieldOfView(90);
            if (camPoincare){
                if (getLimitHyperbolicGroup() < 12) {
                    camera.setFarClip(65 * (maxDist + 1));
                }
                else{
                    camera.setFarClip(100 * (maxDist + 1));

                }
                camera.setTranslateZ(-100);
            }
            else{
                if (getLimitHyperbolicGroup() < 12) {
                    camera.setFarClip(65 * maxDist);
                }
                else {
                    camera.setFarClip(100 * maxDist);
                }
                camera.setTranslateZ(0);

            }
            camera.setFarClip(100000);

            controller.getPoincareButton().setVisible(true);
            controller.getKleinButton().setVisible(true);
            controller.getIncreaseButton().setVisible(true);
            controller.getDecreaseButton().setVisible(true);
            controller.getCBPullFDomain().setVisible(true);
        }

        setUseDepthBuffer(!tiling.getGeometry().equals(Geometry.Euclidean));


        getWorld().getChildren().clear();
        getWorld().getChildren().addAll(tiles, rect, range, test, test2);
        if (tiling.getGeometry() == Geometry.Hyperbolic) {
            getWorld().getChildren().add(light);
        }
        getController().getStatusTextField().setText(tilings.get(current).getStatusLine());
        GroupEditing.update(this);
        controller.updateNavigateTilings();
    }

    public void reset() {
        tilings.set(current, new Tiling(tilings.get(current).getDSymbol()));
    }

    public void translateFDomain(double dx, double dy){
        final Tiling tiling = tilings.get(current);

        // Translation of fundamental domain in Euclidean case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Euclidean) {

            changeDirection = false;

            // A filled recycler is a criterion for translation of whole tiling (see tiling.translateTiling)
            if (getRecycler().getChildren().size() > 0){
                getRecycler().getChildren().clear();
            }

            setKeptEuclideanCopy(new QuadTree()); // Reset Tree (see tiling.makeCopyEuclidean())

            translate(dx, dy); // Translates fDomain by vector (dx,dy).

            tiles.getChildren().clear();
            tiles.getChildren().addAll(tiling.createTilingEuclidean(true,windowCorner,width,height,dx,dy));
        }

        // Translation of fundamental domain in hyperbolic case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Hyperbolic){
            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
            dx /= 300; dy /= 300;

            // A filled recycler is a criterion for translation of whole tiling (see tiling.translateTiling)
            if (getRecycler().getChildren().size() > 0){
                getRecycler().getChildren().clear();
            }

            setKeptHyperbolicCopy(new OctTree()); // Reset Tree (see tiling.makeCopyEuclidean())

            // Insert a boarder so that fundamental domain is not pulled away too far
            Point3D refPoint = tiling.getfDomain().getChamberCenter3D(1).multiply(0.01);
            double a = refPoint.getX(); double b = refPoint.getY();
            if (refPoint.getZ() >= 8 && a*dx+b*dy >= 0) { // Left condition: boarder. Right condition: Calculates whether (dx,dy) points into unit circle (scalar product).
                // Change (dx,dy) to tangent vector.
                dx = b*(b*dx-a*dy)/(a*a+b*b);
                dy = a*(a*dy-b*dx)/(a*a+b*b);
                translate(dx,dy);
                // Change direction in MouseHandler
                changeDirection = true;
                vec = new Point2D(a*(a*dx+b*dy)/(a*a+b*b),b*(a*dx+b*dy)/(a*a+b*b)); // Difference between actual mouse position and tangent vector
            }
            else {
                translate(dx, dy); // Translates fDomain by vector (dx,dy).
                changeDirection = false;
            }

            tiles.getChildren().clear();
            tiles.getChildren().addAll(tiling.createTilingHyperbolic(true,maxDist,dx,dy));
        }
    }

    public void translateTiling(double dx, double dy) {

        changeDirection = false;

        final Tiling tiling = tilings.get(current);

        // Translation in Euclidean case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Euclidean) {

            setKeptEuclideanCopy(new QuadTree()); // Saves copies which are kept under translation
            Translate translate = new Translate(dx,dy,0); // Mouse translation (MouseHandler)

            translate(dx,dy); // Translates fDomain by vector (dx,dy).
            setTransformRecycled(translate.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain

            final Point3D refPoint = tiling.getfDomain().getChamberCenter3D(1); // Point of reference in Euclidean fundamental domain

            if (!tiling.isInRangeEuclidean(refPoint, windowCorner, width, height)){ // If fundamental domain is out of valid range
                Transform t = tiling.calculateBackShiftEuclidean(windowCorner, width, height);
                setTransformRecycled(t.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
                recenterFDomain(t); // Shifts back fDomain into visible window
            }


            //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
            int i = 0;
            while (i < tiles.getChildren().size()){
                Node node = tiles.getChildren().get(i); // Copy with index i in tile. Each copy is a node of the group "tile".
                Transform nodeTransform = node.getTransforms().get(0); // get transform of node
                Point3D point = node.getRotationAxis().add(dx, dy, 0); // point = reference point of node (saved as rotation axis) + mouse translation

                if (tiling.isInRangeEuclidean(point, windowCorner, width, height)){  // keep copy if point still is in valid range
                    node.getTransforms().remove(0); // remove old transforms
                    node.getTransforms().add(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)
                    node.setRotationAxis(point); // "point" serves as new reference of copy
                    insertKeptEuclideanCopy(point); // Save copy as a kept one
                    i++;
                }
                else { // when point is out of valid range
                    getRecycler().getChildren().add(node); // Remove node and add to recycler
                }
            }

            if (getRecycler().getChildren().size() == 0){ // Fill recycler if necessary
                Group recycler2 = JavaFXUtils.copyFundamentalDomain(getEuclideanFund()); // Copy original fundamental domain which was used to build "tiles"
                getRecycler().getChildren().addAll(recycler2); // Add copy to recycler
            }

            //Second step: Create new tiles ----------------------------------------------------------------------------
            // Create new tiles to fill empty space of valid range. Add new tiles to the group "tiles"
            Group newTiles = tiling.createTilingEuclidean(false, windowCorner, width, height, dx, dy);

            if (isBreak){ // Generates new tiling if too much rounding errors
                isBreak = false;
                update();
            }
            else { // No rounding errors: add new tiles
                tiles.getChildren().addAll(newTiles.getChildren());
                System.out.println("Number of copies: " + tiles.getChildren().size());
            }

        }

        // Translation in hyperbolic case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Hyperbolic) {

            changeDirection = false;

            dx/=300; dy/=300;
            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Calculate hyperbolic translation of group:
            Rotate rotateForward, rotateBackward; //Rotations to x-axis and back
            Affine translateX;
            final Point3D X_Axis = new Point3D(1,0,0);
            double d = Math.sqrt(dx*dx+dy*dy);  // Length of translation
            final Point3D vec = new Point3D(dx,dy,0);

            double rotAngle = vec.angle(X_Axis); //Rotation angle between direction of translation and x-axis
            Point3D rotAxis = new Point3D(0,0,1);  // Rotation axis

            if (dy <= 0){ rotAxis = new Point3D(0,0,-1); }

            rotateForward = new Rotate(rotAngle, rotAxis);
            rotateBackward = new Rotate(-rotAngle, rotAxis);

            translateX = new Affine(Math.cosh(d), 0 , Math.sinh(d), 0, 0, 1, 0, 0, Math.sinh(d), 0, Math.cosh(d), 0); // Translation along x-axis

            Transform translate = rotateForward.createConcatenation(translateX).createConcatenation(rotateBackward); // Hyperbolic translation
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // OctTree is used for saving copies which are kept under translation
            setKeptHyperbolicCopy(new OctTree());

            // Translates fDomain by vector (dx,dy).
            translate(dx,dy);
            setTransformRecycled(translate.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain

            // Recenter fDomain if too far away from center
            Point3D refPoint = tiling.getfDomain().getChamberCenter3D(1).multiply(0.01);
            if (refPoint.getZ() >= 2.5 || refPoint.getZ() >= 0.6*maxDist){
                double intoValidRange;
                if (0.6*maxDist < 2.5){
                    intoValidRange = 0.6*maxDist;
                }
                else {
                    intoValidRange = 2.5;
                }
                Transform t = tiling.calculateBackShiftHyperbolic(intoValidRange);
                recenterFDomain(t); // Shifts back fDomain into valid range
                setTransformRecycled(t.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
            }

            //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
            int i = 0;
            while (i < tiles.getChildren().size()){
                Node node = tiles.getChildren().get(i);
                Transform nodeTransform = node.getTransforms().get(0);
                Point3D point = translate.transform(node.getRotationAxis()); // point = translated reference point of node

                if (point.getZ() > maxDist){
                    getRecycler().getChildren().add(node); // Remove node and add to recycler
                }
                else {
                    node.getTransforms().remove(0);
                    node.getTransforms().add(translate.createConcatenation(nodeTransform));
                    node.setRotationAxis(point);
                    insertKeptHyperbolicCopy(point); // Save kept copy
                    i++;
                }
            }

            if (getRecycler().getChildren().size() == 0){ // Fill recycler if necessary
                Group recycler2 = JavaFXUtils.copyFundamentalDomain(getHyperbolicFund()); // Copy original fundamental domain which was used to build "tiles"
                getRecycler().getChildren().addAll(recycler2); // Add copy to recycler
            }

            //Second step: Create new tiles ----------------------------------------------------------------------------
            Group newTiles = tiling.createTilingHyperbolic(false, maxDist, dx, dy);
            if (isBreak){ // Generates new tiling if too much rounding errors
                isBreak = false;
                update();
            }
            else { // No rounding errors: add new tiles
                tiles.getChildren().addAll(newTiles.getChildren());
            }
        }

    }

    public boolean directionChanged(){ return changeDirection; }

    public Point2D getTranslation(){ return vec;  }

    private void setHyperbolicFund(Group g){ Tiling.HyperbolicFund = g; }

    private Group getHyperbolicFund(){ return Tiling.HyperbolicFund; }

    private void setEuclideanFund(Group g){ Tiling.EuclideanFund = g; }

    private Group getEuclideanFund(){ return Tiling.EuclideanFund; }

    private Group getRecycler(){ return Tiling.recycler; }

    private void setTransformRecycled(Transform t){ Tiling.transformRecycled = t; }

    private Transform getTransformRecycled(){ return Tiling.transformRecycled; }

    private void setKeptHyperbolicCopy(OctTree o){ Tiling.keptHyperbolicCopy = o; }

    private void insertKeptHyperbolicCopy(Point3D point){ Tiling.keptHyperbolicCopy.insert(tilings.get(current).getfDomain(), point); }

    private void insertKeptEuclideanCopy(Point3D point){ Tiling.keptEuclideanCopy.insert(point.getX(), point.getY()); }

    private void setKeptEuclideanCopy(QuadTree seen){ Tiling.keptEuclideanCopy = seen; }

    public void translate(double dx, double dy) {
        tilings.get(current).getfDomain().translate(dx, dy);
    }

    public void recenterFDomain(Transform t) { tilings.get(current).getfDomain().recenterFDomain(t); }

    public void straightenAll() {
        tilings.get(current).straightenAllEdges();
    }

    /**
     * determine whether to use depth buffer
     *
     * @param useDepthBuffer
     */
    public void setUseDepthBuffer(boolean useDepthBuffer) {
        final StackPane stackPane = controller.getStackPane();
        SubScene subScene = (SubScene) stackPane.getChildren().get(0);
        if (useDepthBuffer != subScene.isDepthBuffer()) {
            stackPane.getChildren().remove(subScene);
            final Group group = (Group) subScene.getRoot();
            group.getChildren().removeAll();

            subScene = new SubScene(new Group(getWorld()), subScene.getWidth(), subScene.getHeight(), useDepthBuffer, subScene.getAntiAliasing());
            subScene.heightProperty().bind(stackPane.heightProperty());
            subScene.widthProperty().bind(stackPane.widthProperty());
            if (useDepthBuffer) {
                PerspectiveCamera newCamera = new PerspectiveCamera(camera.isFixedEyeAtCameraZero());
                newCamera.setNearClip(camera.getNearClip());
                newCamera.setFarClip(camera.getFarClip());
                newCamera.setFieldOfView(camera.getFieldOfView());
                newCamera.setTranslateZ(camera.getTranslateZ());
                camera = newCamera;
                subScene.setCamera(camera);
            }
            stackPane.getChildren().add(0, subScene);
        }
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    /**
     * are we using a depth buffer?
     *
     * @return true, if so
     */
    public boolean isUseDepthBuffer() {
        final StackPane stackPane = controller.getStackPane();
        SubScene subScene = (SubScene) stackPane.getChildren().get(0);
        return subScene.isDepthBuffer();
    }

    public boolean atFirstTiling() {
        return size() == 0 || current == 0;
    }

    public boolean atLastTiling() {
        return size() == 0 || current == tilings.size() - 1;
    }

    public boolean isCamPoincare() {
        return camPoincare;
    }

    public void setCamPoincare(boolean camPoincare) {
        this.camPoincare = camPoincare;
    }

    public boolean isDrawFundamentalDomainOnly() {
        return drawFundamentalDomainOnly;
    }

    public void setDrawFundamentalDomainOnly(boolean drawFundamentalDomainOnly) {
        this.drawFundamentalDomainOnly = drawFundamentalDomainOnly;
    }

    public int getLimitHyperbolicGroup() {
        return limitHyperbolicGroup;
    }

    public void setLimitHyperbolicGroup(int limitHyperbolicGroup) {
        if (limitHyperbolicGroup > 3)
            this.limitHyperbolicGroup = limitHyperbolicGroup;
    }

    public SimpleObjectProperty<Geometry> geometryProperty() {
        return geometryProperty;
    }

}
