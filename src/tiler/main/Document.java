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

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.tiling.*;
import tiler.util.JavaFXUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;

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
    private AmbientLight ambientLight = new AmbientLight();
    private PointLight pointLight = new PointLight();

    private boolean camPoincare = true; // Variable saving camera settings

    private boolean drawFundamentalDomainOnly = false;

    public static boolean isBreak = false;

    private boolean changeDirection;

    private Point2D vec = new Point2D(0,0);

    private int limitHyperbolicGroup = 5;

    private static double validHyperbolicRange = 4.8;

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
    public double width=800, height=506; //Width and height of window


    private Group tiles = new Group(), linesInFDomain = new Group();
    public static int numberOfCopies; //Counts number of copies.

    private static int chamberIndex = 0;

    private static double tol;

    public void update() {

        final Tiling tiling = tilings.get(current);
        geometryProperty().setValue(tiling.getGeometry());


        // Calculate optimal chamber, where chamber center is as far away from boundary as possible
        chamberIndex = optimalChamber(tiling.getfDomain());
        // Compute tolerance for rounding errors (depends on shape of fundamental domain):
        tiling.computeConstraintsAndGenerators();
        tol = tiling.computeTolerance();
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

            if (!tiling.isInWindowEuclidean(tiling.refPointEuclidean, windowCorner, width, height)) { // Fund. domain is not in visible window
                recenterFDomain(tiling.calculateBackShiftEuclidean(windowCorner, width, height, tol)); // Shifts back fDomain into valid range for fund. domain
                tiles = tiling.createTilingEuclidean(this, isDrawFundamentalDomainOnly(), windowCorner, width, height, tol);
            }
            else { // If fDomain is inside visible window
                tiles = tiling.createTilingEuclidean(this, isDrawFundamentalDomainOnly(), windowCorner, width, height, tol);
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
            tiles = tiling.createTilingSpherical(tol);

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


            double diameterFDomain = calculateDiameter(tiling.getfDomain());
            if (2.8 * diameterFDomain > getLimitHyperbolicGroup()){
                setLimitHyperbolicGroup((int) Math.round(2.8 * diameterFDomain));
            }

            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());  // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.

            // Reset hyperbolic fundamental domain.
            setHyperbolicFund(new Group());
            setKeptHyperbolicCopy(new OctTree());

            //Reset Fundamental Domain if necessary:
            if (Tiling.refPointHyperbolic.getZ() >= validHyperbolicRange){// Fundamental domain is shifted back
                recenterFDomain(tiling.calculateBackShiftHyperbolic(tol)); // Shifts back fDomain into valid range (slower algorithm)
                tiles = tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist, tol);
            }
            else {
                tiles = tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist, tol);
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

        // Build up world
        getWorld().getChildren().clear();
        getWorld().getChildren().addAll(tiles, rect, range, test, test2);
        if (tiling.getGeometry() != Geometry.Spherical) {
            getWorld().getChildren().add(ambientLight);
        }
        else{
            pointLight.setTranslateZ(-7000);
            getWorld().getChildren().add(pointLight);
        }
        if (controller.getCbShowLines().isSelected()){
            removeLinesFromFDomain();
            addLinesToFDomain();
        }

        getWorld().getChildren().add(tiling.getHandles());

        getController().getStatusTextField().setText(tilings.get(current).getStatusLine());
        GroupEditing.update(this);
        controller.updateNavigateTilings();
    }

    // Reset fundamental domain (without updating tiling)
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
            if (controller.getCbShowLines().isSelected()){
                Translate t = new Translate(dx, dy);
                Transform lineTrans = linesInFDomain.getTransforms().get(0);
                lineTrans = t.createConcatenation(lineTrans);
                linesInFDomain.getTransforms().clear();
                linesInFDomain.getTransforms().add(lineTrans);
            }

            tiles.getChildren().clear();
            tiles.getChildren().addAll(tiling.createTilingEuclidean(this, true, windowCorner, width, height, tol));
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
            Point3D refPoint = tiling.getfDomain().getChamberCenter3D(chamberIndex).multiply(0.01);
            double a = refPoint.getX(); double b = refPoint.getY();
            if (refPoint.getZ() >= 7 && a*dx+b*dy >= 0) { // Left condition: boarder. Right condition: Calculates whether (dx,dy) points into unit circle (scalar product).
                // Change (dx,dy) to tangent vector.
                dx = b*(b*dx-a*dy)/(a*a+b*b);
                dy = a*(a*dy-b*dx)/(a*a+b*b);
                translate(dx,dy);
                if (controller.getCbShowLines().isSelected()){
                    // Hyperbolic translation
                    Transform translate = Tools.hyperbolicTranslation(dx,dy);

                    // Transformation of lines in fundamental domain
                    Transform lineTrans = linesInFDomain.getTransforms().get(0);
                    lineTrans = translate.createConcatenation(lineTrans);
                    linesInFDomain.getTransforms().clear();
                    linesInFDomain.getTransforms().add(lineTrans);
                }

                // Change direction in MouseHandler
                changeDirection = true;
                vec = new Point2D(a*(a*dx+b*dy)/(a*a+b*b),b*(a*dx+b*dy)/(a*a+b*b)); // Difference between actual mouse position and tangent vector
            }
            else if (refPoint.getZ() >= 9){
                reset();
                removeLinesFromFDomain();
                if (controller.getCbShowLines().isSelected()){
                    addLinesToFDomain();
                }
            }
            else {
                translate(dx, dy); // Translates fDomain by vector (dx,dy).
                if (controller.getCbShowLines().isSelected()){
                    // Hyperbolic translation
                    Transform translate = Tools.hyperbolicTranslation(dx,dy);

                    // Transformation of lines in fundamental domain
                    Transform lineTrans = linesInFDomain.getTransforms().get(0);
                    lineTrans = translate.createConcatenation(lineTrans);
                    linesInFDomain.getTransforms().clear();
                    linesInFDomain.getTransforms().add(lineTrans);
                }
                changeDirection = false;
            }

            tiles.getChildren().clear();
            tiles.getChildren().addAll(tiling.createTilingHyperbolic(true, maxDist, tol));
        }
    }

    public void translateTiling(double dx, double dy) {

        changeDirection = false;

        final Tiling tiling = tilings.get(current);

        // Translation of whole tiling in Euclidean case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Euclidean) {

            setKeptEuclideanCopy(new QuadTree()); // Saves copies which are kept under translation
            Translate translate = new Translate(dx,dy,0); // Mouse translation (MouseHandler)

            translate(dx,dy); // Translates fDomain by vector (dx,dy).
            setTransformRecycled(translate.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
            if (controller.getCbShowLines().isSelected()){
                Transform lineTrans = linesInFDomain.getTransforms().get(0);
                lineTrans = translate.createConcatenation(lineTrans);
                linesInFDomain.getTransforms().clear();
                linesInFDomain.getTransforms().add(lineTrans);
            }


            final Point3D refPoint = tiling.getfDomain().getChamberCenter3D(chamberIndex); // Point of reference in Euclidean fundamental domain

            if (!tiling.isInWindowEuclidean(refPoint, windowCorner, width, height)){ // If fundamental domain is out of visible window
                Transform t = tiling.calculateBackShiftEuclidean(windowCorner, width, height, tol);
                setTransformRecycled(t.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
                recenterFDomain(t); // Shifts back fDomain into visible window
                if (controller.getCbShowLines().isSelected()){
                    Transform lineTrans = linesInFDomain.getTransforms().get(0);
                    lineTrans = t.createConcatenation(lineTrans);
                    linesInFDomain.getTransforms().clear();
                    linesInFDomain.getTransforms().add(lineTrans);
                }
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
            Group newTiles = tiling.createTilingEuclidean(this, false, windowCorner, width, height, tol);

            if (isBreak){ // Generates new tiling if too much rounding errors
                isBreak = false;
                reset(); // Reset fundamental domain
                update(); // Update tiling
            }
            else { // No rounding errors: add new tiles
                tiles.getChildren().addAll(newTiles.getChildren());
                System.out.println("Number of copies: " + tiles.getChildren().size());
            }
        }

        // Translation of whole tiling in hyperbolic case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Hyperbolic) {

            changeDirection = false;

            dx/=300; dy/=300;
            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());

            // Calculate hyperbolic translation of group:
            Transform translate = Tools.hyperbolicTranslation(dx,dy);

            // OctTree is used for saving copies which are kept under translation
            setKeptHyperbolicCopy(new OctTree());

            // Translates fDomain by vector (dx,dy).
            translate(dx,dy);
            setTransformRecycled(translate.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
            if (controller.getCbShowLines().isSelected()){
                Transform lineTrans = linesInFDomain.getTransforms().get(0);
                lineTrans = translate.createConcatenation(lineTrans);
                linesInFDomain.getTransforms().clear();
                linesInFDomain.getTransforms().add(lineTrans);
            }

            // Recenter fDomain if too far away from center
            Point3D refPoint = tiling.getfDomain().getChamberCenter3D(chamberIndex).multiply(0.01);
            if (refPoint.getZ() >= validHyperbolicRange){
                Transform t = tiling.calculateBackShiftHyperbolic(tol);
                recenterFDomain(t); // Shifts back fDomain into valid range
                setTransformRecycled(t.createConcatenation(getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
                if (controller.getCbShowLines().isSelected()){
                    Transform lineTrans = linesInFDomain.getTransforms().get(0);
                    lineTrans = t.createConcatenation(lineTrans);
                    linesInFDomain.getTransforms().clear();
                    linesInFDomain.getTransforms().add(lineTrans);
                }
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
            Group newTiles = tiling.createTilingHyperbolic(false, maxDist, tol);
            if (isBreak){ // Generates new tiling if too much rounding errors
                isBreak = false;
                reset(); // Reset fundamental domain
                update(); // Update tiling
            }
            else { // No rounding errors: add new tiles
                tiles.getChildren().addAll(newTiles.getChildren());
            }
        }

    }


    /**
     * Deletes copies of fundamental domain in hyperbolic case when less tiles are shown.
     */
    public void decreaseTiling(){
        double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
        int bound = tiles.getChildren().size();
        for (int i = 1; i <= bound; i++){
            Node node = tiles.getChildren().get(bound-i);
            if (node.getRotationAxis().getZ() > maxDist){
                //tiles.getChildren().remove(node);
                getRecycler().getChildren().add(node);
            }
        }
    }

    /**
     * Adds copies of fundamental domain in hyperbolic case when more tiles are shown
     */
    public void increaseTiling(){
        final Tiling tiling = tilings.get(current);
        double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
        setKeptHyperbolicCopy(new OctTree());
        for (int i = 0; i < tiles.getChildren().size(); i++){
            insertKeptHyperbolicCopy(tiles.getChildren().get(i).getRotationAxis()); // Add existing tiles to tree structure
        }

        // Empty recycler (because not in translate mode).
        //getRecycler().getChildren().clear();

        if (getRecycler().getChildren().size() == 0){ // Fill recycler if necessary
            Group recycler2 = JavaFXUtils.copyFundamentalDomain(getHyperbolicFund()); // Copy original fundamental domain which was used to build "tiles"
            getRecycler().getChildren().addAll(recycler2); // Add copy to recycler
        }

        numberOfCopies = 0;
        // Add new tiles
        Group newTiles = tiling.createTilingHyperbolic(false, maxDist, tol);
        tiles.getChildren().addAll(newTiles.getChildren());
        numberOfCopies = tiles.getChildren().size();
    }

    /**
     * Adds lines to fundamental domain
     */
    public void addLinesToFDomain(){
        final Tiling tiling = tilings.get(current);

        for (int k = 1; k <= tiling.getfDomain().size(); k++) {
            linesInFDomain.getChildren().add(makeLine(tiling.getfDomain().getGeometry(), tiling.getfDomain().getVertex3D(0, k), tiling.getfDomain().getEdgeCenter3D(1, k), tiling.getfDomain().getVertex3D(2, k), Color.BLACK, 1));

            linesInFDomain.getChildren().add(makeLine(tiling.getfDomain().getGeometry(), tiling.getfDomain().getVertex3D(2, k), tiling.getfDomain().getEdgeCenter3D(0, k), tiling.getfDomain().getVertex3D(1, k), Color.BLACK, 1));

            linesInFDomain.getChildren().add(makeLine(tiling.getfDomain().getGeometry(), tiling.getfDomain().getVertex3D(0, k), tiling.getfDomain().getChamberCenter3D(k), tiling.getfDomain().getEdgeCenter3D(0, k), Color.BLACK, 0.5f));
            linesInFDomain.getChildren().add(makeLine(tiling.getfDomain().getGeometry(), tiling.getfDomain().getVertex3D(1, k), tiling.getfDomain().getChamberCenter3D(k), tiling.getfDomain().getEdgeCenter3D(1, k), Color.BLACK, 0.5f));
            linesInFDomain.getChildren().add(makeLine(tiling.getfDomain().getGeometry(), tiling.getfDomain().getVertex3D(2, k), tiling.getfDomain().getChamberCenter3D(k), tiling.getfDomain().getEdgeCenter3D(2, k), Color.BLACK, 0.5f));

        }
        for (int k = 1; k <= tiling.getfDomain().size(); k++) {
            final Point3D v0 = tiling.getfDomain().getVertex3D(0, k);
            final Point3D e2 = tiling.getfDomain().getEdgeCenter3D(2, k);
            final Point3D v1 = tiling.getfDomain().getVertex3D(1, k);
            linesInFDomain.getChildren().add(makeLine(tiling.getfDomain().getGeometry(), v0, e2, v1, Color.BLACK, 1));
        }

        linesInFDomain.getTransforms().clear();
        linesInFDomain.getTransforms().add(new Translate());
        getWorld().getChildren().add(linesInFDomain);
    }

    public void removeLinesFromFDomain(){
        linesInFDomain.getChildren().clear();
        getWorld().getChildren().remove(linesInFDomain);
    }

    private static Node makeLine(Geometry geometry, Point3D a, Point3D b, Point3D c, Color color, float width) {
        if (geometry == Geometry.Euclidean) {
                Polyline polyLine = new Polyline(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY());
                polyLine.setStroke(color);
                polyLine.setStrokeWidth(width);
                polyLine.setStrokeLineCap(StrokeLineCap.ROUND);
                return polyLine;

            }
        else {
            Group g = new Group();
            g.getChildren().add(Cylinderline.createConnection(a,b,color,width));
            g.getChildren().addAll(Cylinderline.createConnection(b,c,color,width));
            return g;
        }
    }

    private int optimalChamber(FDomain f){
        double dMax = 0, dMin = 1000, dist; int index = 1;
            for (int i = 1; i <= f.size(); i++) {
                Point3D a = f.getChamberCenter3D(i).multiply(0.01);
                for (int j = 1; j <= f.size(); j++) {
                    if (j != i) {
                        dist = Tools.distance(f,a,f.getChamberCenter3D(j).multiply(0.01));
                        if (dist > dMax){
                            dMax = dist;
                        }
                    }
                }
                if (dMax < dMin) {
                    dMin = dMax;
                    index = i;
                }
                dMax = 0;
            }
        return index;
    }

    private double calculateDiameter(FDomain f){
        // Save vertices of fundamental domain in list:
        LinkedList<Point3D> vertices = new LinkedList<>();
        for (int k = 1; k <= f.size(); k++){
            vertices.add(f.getVertex3D(0,k));
            vertices.add(f.getVertex3D(1,k));
            vertices.add(f.getVertex3D(2,k));
        }
        double d = 0;

        for (int i = 0; i <= vertices.size()-1; i++){
            for (int j = i+1; j <= vertices.size()-1; j++){
                Point3D a = vertices.get(i), b = vertices.get(j);
                // Calculate hyperbolic distance between a and b:
                double scalar = (a.getZ()*b.getZ() - a.getX()*b.getX() - a.getY()*b.getY())/10000;
                double dist = Math.log(Math.abs(scalar + Math.sqrt(Math.abs(scalar * scalar - 1)))); // Inverse function of cosh
                if (dist > d){ // Find maximal distance
                    d = dist;
                }
            }
        }
        return d;
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

    private void insertKeptHyperbolicCopy(Point3D point){
        Tiling  t = tilings.get(current);
        Tiling.keptHyperbolicCopy.insert(t.getfDomain(), point, tol);
    }

    private void insertKeptEuclideanCopy(Point3D point){ Tiling.keptEuclideanCopy.insert(point.getX(), point.getY(), tol); }

    private void setKeptEuclideanCopy(QuadTree seen){ Tiling.keptEuclideanCopy = seen; }

    public void translate(double dx, double dy) {
        tilings.get(current).getfDomain().translate(dx, dy);
    }

    public void recenterFDomain(Transform t) { tilings.get(current).getfDomain().recenterFDomain(t); }

    public static int getChamberIndex() { return chamberIndex; }

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
        if (limitHyperbolicGroup >= 5)
            this.limitHyperbolicGroup = limitHyperbolicGroup;
    }

    public static double getTol(){return tol;}

    public static double getValidHyperbolicRange() { return validHyperbolicRange; }

    public SimpleObjectProperty<Geometry> geometryProperty() {
        return geometryProperty;
    }
}
