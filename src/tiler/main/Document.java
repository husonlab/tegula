/*
 *  Copyright (C) 2018 University of Tuebingen
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

/*
 *  Copyright (C) 2018 University of Tuebingen
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import jloda.util.Basic;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.core.dsymbols.Geometry;
import tiler.tiling.StraightenEdges;
import tiler.tiling.Tiling;
import tiler.tiling.Tools;
import tiler.util.JavaFXUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

/**
 * This contains all the data associated with a single document.
 * Daniel Huson and Ruediger Zeller, 2016
 */
public class Document {
    private final StringProperty fileName = new SimpleStringProperty("Untitled");
    static public final int FIRST = 0;
    static public final int NEXT = -1;
    static public final int PREV = -2;
    static public final int LAST = -3;
    static public final int RELOAD = -4;

    public enum HyperbolicModel {Poincare, Klein, Hyperboloid}

    private final SimpleObjectProperty<HyperbolicModel> hyperbolicModel = new SimpleObjectProperty<>(HyperbolicModel.Poincare);

    private final ObservableList<Tiling> tilings = FXCollections.observableArrayList();

    private final IntegerProperty currentIndex = new SimpleIntegerProperty(-1);
    private final SimpleObjectProperty<Tiling> currentTiling = new SimpleObjectProperty<>(null);
    private final IntegerProperty numberOfTilings = new SimpleIntegerProperty(0);

    private final Group universe;
    private final Group world;
    //private final MainViewController mainViewController;
    private PerspectiveCamera perspectiveCamera;

    private final PointLight pointLight;
    private final AmbientLight ambientLight;

    private boolean drawFundamentalDomainOnly = false;

    private boolean changeDirection;

    private Point2D vec = new Point2D(0, 0);

    private int limitHyperbolicGroup = 5;

    private static final double validHyperbolicRange = 4.8;

    private final SimpleObjectProperty<Geometry> geometryProperty = new SimpleObjectProperty<>();

    private Point3D windowCorner = new Point3D(0, 0, 0); // Upper left corner of window in Euclidean case

    private final Group tiles = new Group();
    private final Group linesInFDomain = new Group();

    private final DoubleProperty width = new SimpleDoubleProperty(1000);
    private final DoubleProperty height = new SimpleDoubleProperty(1000);

    private final BooleanProperty showLines = new SimpleBooleanProperty(false);

    private final StringProperty statusLine = new SimpleStringProperty();

    private final BooleanProperty alwaysStraightenEdges = new SimpleBooleanProperty(false);

    private final TilingStyle tilingStyle = new TilingStyle();

    private final LongProperty updateNumber = new SimpleLongProperty(0); // incremented each time updated

    /**
     * constructor
     */
    public Document(Group universe, Group world, PerspectiveCamera perspectiveCamera) {
        this.universe = universe;
        this.world = world;
        this.perspectiveCamera = perspectiveCamera;

        currentIndex.addListener((c, o, n) -> {
            if (n.intValue() >= 0 && n.intValue() < tilings.size()) {
                currentTiling.set(tilings.get(n.intValue()));
                geometryProperty.setValue(currentTiling.get().getfDomain().getGeometry());
            } else {
                currentTiling.set(null);
                geometryProperty.set(Geometry.Euclidean);
            }
        });

        numberOfTilings.bind(Bindings.size(tilings));

        pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-1000);
        pointLight.setTranslateY(-1000);
        pointLight.setTranslateZ(-10000);
        pointLight.setRotate(90);

        ambientLight = new AmbientLight(Color.WHITE);
    }

    /**
     * clear the D-symbols
     */
    public void clear() {
        tilings.clear();
        tiles.getChildren().clear();
        currentIndex.set(-1);
    }

    /**
     * read a file of Delaney symbols
     *
     * @param reader
     * @throws IOException
     */
    public void read(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                final DSymbol dSymbol = new DSymbol();
                dSymbol.read(new StringReader(line));
                tilings.add(new Tiling(dSymbol));
            }
        }
        if (tilings.size() > 0)
            currentIndex.set(0);
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
        final int old = currentIndex.get();
        switch (which) {
            case NEXT:
                currentIndex.set(Math.min(size() - 1, old + 1));
                break;
            case PREV:
                currentIndex.set(Math.max(0, old - 1));
                break;
            case FIRST:
                currentIndex.set(0);
                break;
            case LAST:
                currentIndex.set(size() - 1);
                break;
            case RELOAD: {
                tilings.set(old, new Tiling(tilings.get(old).getDSymbol()));
                currentTiling.set(tilings.get(old));
                break;
            }
            default:
                currentIndex.set(Math.max(0, Math.min(size() - 1, which)));
                break;
        }
        return currentIndex.get() != old;
    }

    public Group getWorld() {
        return world;
    }

    public IntegerProperty currentIndexProperty() {
        return currentIndex;
    }

    public SimpleObjectProperty<Tiling> currentTilingProperty() {
        return currentTiling;
    }

    public Tiling getCurrentTiling() {
        return currentTiling.get();
    }

    public void changeCurrentTiling(Tiling tiling) {
        tilings.set(currentIndex.get(), tiling);
        currentTiling.set(tiling);
        geometryProperty.setValue(tiling.getfDomain().getGeometry());
    }

    public int getNumberOfTilings() {
        return numberOfTilings.get();
    }

    public IntegerProperty numberOfTilingsProperty() {
        return numberOfTilings;
    }

    /**
     * update the tiling
     */
    public void update() {
        System.err.println("Update");
        tiles.getChildren().clear();

        final Tiling tiling = getCurrentTiling();

        if (tiling == null)
            return;

        if (isAlwaysStraightenEdges())
            StraightenEdges.straightenAllEdges(getCurrentTiling());

        geometryProperty.setValue(tiling.getGeometry());

        // Compute tolerance for rounding errors (depends on shape of fundamental domain):
        tiling.computeConstraintsAndGenerators();

        // Empty recycler for copies and reset transform for recycled copies.
        tiling.getRecycler().getChildren().clear();
        tiling.setTransformRecycled(new Translate());
        changeDirection = false; // No direction has to be changed

        getWorld().getChildren().clear();

        final Group additionalStuff = new Group();

        if (tiling.getGeometry() == Geometry.Euclidean) { //Euclidean case ---------------------------------------------
            universe.getChildren().remove(pointLight);
            if (!universe.getChildren().contains(ambientLight))
                universe.getChildren().add(ambientLight);

            // Reset Euclidean fundamental domain and QuadTree
            getCurrentTiling().setEuclideanFund(new Group());
            tiling.clearKeptEuclideanCopy();

            // Calculate optimal chamber, where chamber center is as far away from boundary as possible
            tiling.setReferenceChamberIndex(computeOptimalChamberIndex(tiling.getfDomain()));

            if (!tiling.isInWindowEuclidean(tiling.getRefPointEuclidean(), windowCorner, width.get(), height.get())) { // Fund. domain is not in visible window
                recenterFDomain(tiling.calculateBackShiftEuclidean(windowCorner, width.get(), height.get())); // Shifts back fDomain into valid range for fund. domain
            }
            tiles.getChildren().setAll(tiling.createTilingEuclidean(this, isDrawFundamentalDomainOnly(), windowCorner, width.get(), height.get(), tilingStyle).getChildren());
            tiling.setNumberOfCopies(tiles.getChildren().size());

            //Add rectangles for debugging
            if (false) {
                Rectangle rect = new Rectangle(width.get(), height.get());
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.BLACK);
                Rectangle range = new Rectangle(width.get() + 250, height.get() + 250);
                range.setFill(Color.TRANSPARENT);
                range.setStroke(Color.BLACK);
                Rectangle test = new Rectangle(width.get() + 200, height.get() + 200);
                test.setFill(Color.TRANSPARENT);
                test.setStroke(Color.BLACK);
                Rectangle test2 = new Rectangle(width.get() + 150, height.get() + 150);
                test2.setFill(Color.TRANSPARENT);
                test2.setStroke(Color.BLACK);
                additionalStuff.getChildren().addAll(rect, range, test, test2);
            }

            additionalStuff.getChildren().add(tiling.getHandles());

            // no camera options to set
        } else if (tiling.getGeometry() == Geometry.Spherical) { // Spherical case --------------------------------------
            // Calculate optimal chamber, where chamber center is as far away from boundary as possible
            tiling.setReferenceChamberIndex(computeOptimalChamberIndex(tiling.getfDomain()));

            tiles.getChildren().setAll(tiling.createTilingSpherical(getTilingStyle()).getChildren());
            tiling.setNumberOfCopies(tiles.getChildren().size());

            if (!universe.getChildren().contains(pointLight))
                universe.getChildren().add(pointLight);
            universe.getChildren().remove(ambientLight);

            perspectiveCamera.setRotate(0);
            perspectiveCamera.setTranslateX(0);
            perspectiveCamera.setTranslateZ(-850);
            perspectiveCamera.setFieldOfView(15);
            perspectiveCamera.setFarClip(10000);
        } else if (tiling.getGeometry() == Geometry.Hyperbolic) {         // Hyperbolic case -----------------------------
            universe.getChildren().remove(pointLight);
            if (!universe.getChildren().contains(ambientLight))
                universe.getChildren().add(ambientLight);

            HyperbolicModelCameraSettings.setModel(this, getHyperbolicModel(), false);

            // Calculate optimal chamber, where chamber center is as far away from boundary as possible
            tiling.setReferenceChamberIndex(computeOptimalChamberIndex(tiling.getfDomain()));

            double diameterFDomain = calculateDiameter(tiling.getfDomain());
            if (2.8 * diameterFDomain > getLimitHyperbolicGroup()) {
                setLimitHyperbolicGroup((int) Math.round(2.8 * diameterFDomain));
            }

            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());  // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.

            // Reset hyperbolic fundamental domain.
            tiling.setHyperbolicFund(new Group());
            tiling.clearKeptHyperbolicCopy();

            //Reset Fundamental Domain if necessary:
            if (tiling.getRefPointHyperbolic().getZ() >= validHyperbolicRange) {// Fundamental domain is shifted back
                recenterFDomain(tiling.calculateBackShiftHyperbolic()); // Shifts back fDomain into valid range (slower algorithm)
            }

            tiles.getChildren().setAll(tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist, tilingStyle).getChildren());
            tiling.setNumberOfCopies(tiles.getChildren().size());
        }

        // Build up world
        getWorld().getChildren().setAll(tiles);
        getWorld().getChildren().addAll(additionalStuff.getChildren());

        if (isShowLines()) {
            removeLinesFromFDomain();
            addLinesToFDomain();
        }

        statusLine.set(getCurrentTiling().getStatusLine());
        updateNumber.setValue(updateNumber.get() + 1);
    }

    // Reset fundamental domain (without updating tiling)
    public void reset() {
        tilings.set(currentIndex.get(), new Tiling(getCurrentTiling().getDSymbol()));
    }

    public void translateFDomain(double dx, double dy) {
        final Tiling tiling = getCurrentTiling();

        // Translation of fundamental domain in Euclidean case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Euclidean) {

            changeDirection = false;

            // A filled recycler is a criterion for translation of whole tiling (see tiling.translateTiling)
            if (tiling.getRecycler().getChildren().size() > 0) {
                tiling.getRecycler().getChildren().clear();
            }

            tiling.clearKeptEuclideanCopy();

            translate(dx, dy); // Translates fDomain by vector (dx,dy).
            if (isShowLines()) {
                Translate t = new Translate(dx, dy);
                Transform lineTrans = linesInFDomain.getTransforms().get(0);
                lineTrans = t.createConcatenation(lineTrans);
                linesInFDomain.getTransforms().clear();
                linesInFDomain.getTransforms().add(lineTrans);
            }

            tiles.getChildren().setAll(tiling.createTilingEuclidean(this, true, windowCorner, width.get(), height.get(), tilingStyle).getChildren());
            tiling.setNumberOfCopies(tiles.getChildren().size());
        }

        // Translation of fundamental domain in hyperbolic case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Hyperbolic) {
            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
            dx /= 300;
            dy /= 300;

            // A filled recycler is a criterion for translation of whole tiling (see tiling.translateTiling)
            if (tiling.getRecycler().getChildren().size() > 0) {
                tiling.getRecycler().getChildren().clear();
            }

            tiling.setHyperbolicFund(new Group());
            tiling.clearKeptHyperbolicCopy();

            // Insert a boarder so that fundamental domain is not pulled away too far
            Point3D refPoint = tiling.getfDomain().getChamberCenter3D(tiling.getReferenceChamberIndex()).multiply(0.01);
            double a = refPoint.getX();
            double b = refPoint.getY();
            if (refPoint.getZ() >= 7 && a * dx + b * dy >= 0) { // Left condition: boarder. Right condition: Calculates whether (dx,dy) points into unit circle (scalar product).
                // Change (dx,dy) to tangent vector.
                dx = b * (b * dx - a * dy) / (a * a + b * b);
                dy = a * (a * dy - b * dx) / (a * a + b * b);
                translate(dx, dy);
                if (isShowLines()) {
                    // Hyperbolic translation
                    Transform translate = Tools.hyperbolicTranslation(dx, dy);

                    // Transformation of lines in fundamental domain
                    Transform lineTrans = linesInFDomain.getTransforms().get(0);
                    lineTrans = translate.createConcatenation(lineTrans);
                    linesInFDomain.getTransforms().clear();
                    linesInFDomain.getTransforms().add(lineTrans);
                }

                // Change direction in MouseHandler
                changeDirection = true;
                vec = new Point2D(a * (a * dx + b * dy) / (a * a + b * b), b * (a * dx + b * dy) / (a * a + b * b)); // Difference between actual mouse position and tangent vector
            } else if (refPoint.getZ() >= 9) {
                reset();
                removeLinesFromFDomain();
                if (isShowLines()) {
                    addLinesToFDomain();
                }
            } else {
                translate(dx, dy); // Translates fDomain by vector (dx,dy).
                if (isShowLines()) {
                    // Hyperbolic translation
                    Transform translate = Tools.hyperbolicTranslation(dx, dy);

                    // Transformation of lines in fundamental domain
                    Transform lineTrans = linesInFDomain.getTransforms().get(0);
                    lineTrans = translate.createConcatenation(lineTrans);
                    linesInFDomain.getTransforms().clear();
                    linesInFDomain.getTransforms().add(lineTrans);
                }
                changeDirection = false;
            }

            tiles.getChildren().setAll(tiling.createTilingHyperbolic(true, maxDist, tilingStyle).getChildren());
            tiling.setNumberOfCopies(tiles.getChildren().size());
        }
    }

    public void translateTiling(double dx, double dy) {
        changeDirection = false;

        final Tiling tiling = getCurrentTiling();

        // Translation of whole tiling in Euclidean case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Euclidean) {

            tiling.clearKeptEuclideanCopy();
            Translate translate = new Translate(dx, dy, 0); // Mouse translation (MouseHandler)

            translate(dx, dy); // Translates fDomain by vector (dx,dy).
            tiling.setTransformRecycled(translate.createConcatenation(tiling.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain

            final Point3D refPoint = tiling.getfDomain().getChamberCenter3D(tiling.getReferenceChamberIndex()); // Point of reference in Euclidean fundamental domain

            if (!tiling.isInWindowEuclidean(refPoint, windowCorner, width.get(), height.get())) { // If fundamental domain is out of visible window
                Transform t = tiling.calculateBackShiftEuclidean(windowCorner, width.get(), height.get());
                tiling.setTransformRecycled(t.createConcatenation(tiling.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
                recenterFDomain(t); // Shifts back fDomain into visible window
            }


            //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
            int i = 0;
            while (i < tiles.getChildren().size()) {
                Node node = tiles.getChildren().get(i); // Copy with index i in tile. Each copy is a node of the group "tile".
                if (node.getTransforms().size() > 0) {
                    Transform nodeTransform = node.getTransforms().get(0); // get transform of node
                    Point3D point = node.getRotationAxis().add(dx, dy, 0); // point = reference point of node (saved as rotation axis) + mouse translation

                    if (tiling.isInRangeEuclidean(point, windowCorner, width.get(), height.get())) {  // keep copy if point still is in valid range
                        node.getTransforms().remove(0); // remove old transforms
                        node.getTransforms().add(translate.createConcatenation(nodeTransform)); // new transform = (translate)*(old transform)
                        node.setRotationAxis(point); // "point" serves as new reference of copy
                        tiling.insertKeptEuclideanCopy(point); // Save copy as a kept one
                        i++;
                    } else { // when point is out of valid range
                        tiling.getRecycler().getChildren().add(node); // Remove node and add to recycler
                    }
                } else
                    i++;
            }

            if (tiling.getRecycler().getChildren().size() == 0) { // Fill recycler if necessary
                final Group fund = JavaFXUtils.copyGroup(getCurrentTiling().getEuclideanFund()); // Copy original fundamental domain which was used to build "tiles"
                tiling.getRecycler().getChildren().addAll(fund); // Add copy to recycler
            }

            //Second step: Create new tiles ----------------------------------------------------------------------------
            // Create new tiles to fill empty space of valid range. Add new tiles to the group "tiles"
            Group newTiles = tiling.createTilingEuclidean(this, false, windowCorner, width.get(), height.get(), tilingStyle);

            if (tiling.isBreak()) { // Generates new tiling if too much rounding errors
                tiling.setBreak(false);
                reset(); // Reset fundamental domain
                update(); // Update tiling
            } else { // No rounding errors: add new tiles
                tiles.getChildren().addAll(newTiles.getChildren());
                tiling.setNumberOfCopies(tiles.getChildren().size());
                //System.err.println("Number of copies: " + tiling.getNumberOfCopies());
            }
        }

        // Translation of whole tiling in hyperbolic case
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (tiling.getGeometry() == Geometry.Hyperbolic) {
            changeDirection = false;

            dx /= 300;
            dy /= 300;
            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());

            // Calculate hyperbolic translation of group:
            Transform translate = Tools.hyperbolicTranslation(dx, dy);

            System.err.println("translate: " + translate);

            // OctTree is used for saving copies which are kept under translation
            tiling.clearKeptHyperbolicCopy();

            // Translates fDomain by vector (dx,dy).
            translate(dx, dy);
            tiling.setTransformRecycled(translate.createConcatenation(tiling.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain


            // Recenter fDomain if too far away from center
            Point3D refPoint = tiling.getfDomain().getChamberCenter3D(tiling.getReferenceChamberIndex()).multiply(0.01);
            if (refPoint.getZ() >= validHyperbolicRange) {
                final Transform t = tiling.calculateBackShiftHyperbolic();
                recenterFDomain(t); // Shifts back fDomain into valid range
                tiling.setTransformRecycled(t.createConcatenation(tiling.getTransformRecycled())); // Transforms original fundamental domain (which served as construction for the tile) to reset fundamental domain
                // todo: need to shift chamber drawing, too
            }

            //First step: Translate tiles by vector (dx,dy) ------------------------------------------------------------
            int i = 0;
            while (i < tiles.getChildren().size()) {
                final Node node = tiles.getChildren().get(i);
                if (node instanceof Group) {
                    if (((Group) node).getChildren().size() == 0)
                        throw new RuntimeException("Fund copy empty");
                }

                final Transform nodeTransform = node.getTransforms().get(0);
                final Point3D point = translate.transform(node.getRotationAxis()); // point = translated reference point of node

                if (point.getZ() > maxDist) {
                    tiling.getRecycler().getChildren().add(node); // Remove node and add to recycler
                } else {
                    node.getTransforms().remove(0);
                    node.getTransforms().add(translate.createConcatenation(nodeTransform));
                    node.setRotationAxis(point);
                    tiling.insertKeptHyperbolicCopy(point); // Save kept copy
                    i++;
                }
            }

            if (tiling.getRecycler().getChildren().size() == 0) { // Fill recycler if necessary
                final Group fund = JavaFXUtils.copyGroup(getCurrentTiling().getHyperbolicFund()); // Copy original fundamental domain which was used to build "tiles"
                if (fund.getChildren().size() == 0)
                    throw new RuntimeException("Fund copy empty");
                tiling.getRecycler().getChildren().add(fund); // Add copy to recycler
            }

            //Second step: Create new tiles ----------------------------------------------------------------------------
            Group newTiles = tiling.createTilingHyperbolic(false, maxDist, tilingStyle);
            if (tiling.isBreak()) { // Generates new tiling if too much rounding errors
                tiling.setBreak(false);
                reset(); // Reset fundamental domain
                update(); // Update tiling
            } else { // No rounding errors: add new tiles
                tiles.getChildren().addAll(newTiles.getChildren());
                tiling.setNumberOfCopies(tiles.getChildren().size());
            }
        }

    }


    /**
     * Deletes copies of fundamental domain in hyperbolic case when less tiles are shown.
     */
    public void decreaseTiling() {
        setLimitHyperbolicGroup(getLimitHyperbolicGroup() - 1);

        double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
        int bound = tiles.getChildren().size();
        for (int i = 1; i <= bound; i++) {
            Node node = tiles.getChildren().get(bound - i);
            if (node.getRotationAxis().getZ() > maxDist) {
                //tiles.getChildren().remove(node);
                getCurrentTiling().getRecycler().getChildren().add(node);
            }
        }
    }

    /**
     * Adds copies of fundamental domain in hyperbolic case when more tiles are shown
     */
    public void increaseTiling() {
        setLimitHyperbolicGroup(getLimitHyperbolicGroup() + 1);

        final Tiling tiling = getCurrentTiling();

        double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());
        tiling.clearKeptHyperbolicCopy();
        for (int i = 0; i < tiles.getChildren().size(); i++) {
            tiling.insertKeptHyperbolicCopy(tiles.getChildren().get(i).getRotationAxis()); // Add existing tiles to tree structure
        }

        tiling.setReferenceChamberIndex(computeOptimalChamberIndex(tiling.getfDomain()));

        tiling.setNumberOfCopies(0);
        // Add new tiles
        Group newTiles = tiling.createTilingHyperbolic(false, maxDist, tilingStyle);
        tiles.getChildren().addAll(newTiles.getChildren());
        tiling.setNumberOfCopies(tiles.getChildren().size());
    }

    /**
     * Adds lines to fundamental domain
     */
    public void addLinesToFDomain() {
        tilingStyle.setShowFundamentalChambers(true);
    }

    public void removeLinesFromFDomain() {
        tilingStyle.setShowFundamentalChambers(false);
    }

    private int computeOptimalChamberIndex(FDomain fDomain) {
        double dMax = 0, dMin = 1000, dist;
        int index = 1;
        for (int i = 1; i <= fDomain.size(); i++) {
            Point3D a = fDomain.getChamberCenter3D(i).multiply(0.01);
            for (int j = 1; j <= fDomain.size(); j++) {
                if (j != i) {
                    dist = Tools.distance(fDomain.getGeometry(), a, fDomain.getChamberCenter3D(j).multiply(0.01));
                    if (dist > dMax) {
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

    private double calculateDiameter(FDomain f) {
        // Save vertices of fundamental domain in list:
        LinkedList<Point3D> vertices = new LinkedList<>();
        for (int k = 1; k <= f.size(); k++) {
            vertices.add(f.getVertex3D(0, k));
            vertices.add(f.getVertex3D(1, k));
            vertices.add(f.getVertex3D(2, k));
        }
        double d = 0;

        for (int i = 0; i <= vertices.size() - 1; i++) {
            for (int j = i + 1; j <= vertices.size() - 1; j++) {
                Point3D a = vertices.get(i), b = vertices.get(j);
                // Calculate hyperbolic distance between a and b:
                double scalar = (a.getZ() * b.getZ() - a.getX() * b.getX() - a.getY() * b.getY()) / 10000;
                double dist = Math.log(Math.abs(scalar + Math.sqrt(Math.abs(scalar * scalar - 1)))); // Inverse function of cosh
                if (dist > d) { // Find maximal distance
                    d = dist;
                }
            }
        }
        return d;
    }

    public boolean directionChanged() {
        return changeDirection;
    }

    public Point2D getTranslation() {
        return vec;
    }


    public void translate(double dx, double dy) {
        getCurrentTiling().getfDomain().translate(dx, dy);
    }

    public void recenterFDomain(Transform t) {
        getCurrentTiling().getfDomain().recenterFDomain(t);
    }

    /**
     * determine whether to use depth buffer
     *
     * @param useDepthBuffer
     */
    public void setUseDepthBuffer(final Pane mainPane, boolean useDepthBuffer) {
        SubScene subScene = (SubScene) mainPane.getChildren().get(0);
        if (useDepthBuffer != subScene.isDepthBuffer()) {
            mainPane.getChildren().remove(subScene);
            ((Group) subScene.getRoot()).getChildren().remove(universe);

            subScene = new SubScene(new Group(universe), subScene.getWidth(), subScene.getHeight(), useDepthBuffer, subScene.getAntiAliasing());
            subScene.heightProperty().bind(mainPane.heightProperty());
            subScene.widthProperty().bind(mainPane.widthProperty());
            if (useDepthBuffer) {
                perspectiveCamera = new PerspectiveCamera(true);
                subScene.setCamera(perspectiveCamera);
            } else
                perspectiveCamera = null;
            mainPane.getChildren().add(0, subScene);
        }
    }

    public PerspectiveCamera getPerspectiveCamera() {
        return perspectiveCamera;
    }


    public boolean atFirstTiling() {
        return size() == 0 || currentIndex.get() == 0;
    }

    public boolean atLastTiling() {
        return size() == 0 || currentIndex.get() == tilings.size() - 1;
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

    public static double getValidHyperbolicRange() {
        return validHyperbolicRange;
    }

    public ReadOnlyObjectProperty<Geometry> geometryProperty() {
        return geometryProperty;
    }

    public Geometry getGeometry() {
        return geometryProperty.get();
    }

    public HyperbolicModel getHyperbolicModel() {
        return hyperbolicModel.get();
    }

    public ReadOnlyObjectProperty<HyperbolicModel> hyperbolicModelProperty() {
        return hyperbolicModel;
    }

    public void setHyperbolicModel(HyperbolicModel hyperbolicModel) {
        this.hyperbolicModel.set(hyperbolicModel);
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double getHeight() {
        return height.get();
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public boolean isShowLines() {
        return showLines.get();
    }

    public BooleanProperty showLinesProperty() {
        return showLines;
    }

    public void setShowLines(boolean showLines) {
        this.showLines.set(showLines);
    }

    public String getStatusLine() {
        return statusLine.get();
    }

    public ReadOnlyStringProperty statusLineProperty() {
        return statusLine;
    }

    public boolean isAlwaysStraightenEdges() {
        return alwaysStraightenEdges.get();
    }

    public BooleanProperty alwaysStraightenEdgesProperty() {
        return alwaysStraightenEdges;
    }

    public void setAlwaysStraightenEdges(boolean alwaysStraightenEdges) {
        this.alwaysStraightenEdges.set(alwaysStraightenEdges);
    }

    public ReadOnlyLongProperty updateNumberProperty() {
        return updateNumber;
    }

    /**
     * searches for a tiling that has the given text as number and chooses it
     *
     * @param text
     */
    public boolean findAndMoveTo(String text) {
        for (int i = 0; i < tilings.size(); i++) {
            final String label = tilings.get(i).getDSymbol().getNr1() + "." + tilings.get(i).getDSymbol().getNr2();
            if (label.startsWith(text)) {
                if (i != currentIndex.get()) {
                    moveTo(i);
                }
                return true;
            }
        }
        return false;
    }

    public TilingStyle getTilingStyle() {
        return tilingStyle;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getName() {
        return Basic.getFileNameWithoutPath(getFileName());
    }
}
