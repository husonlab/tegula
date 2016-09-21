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

import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import tiler.core.dsymbols.DSymbol;
import tiler.core.dsymbols.FDomain;
import tiler.tiling.Tiling;

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

    private int limitHyperbolicGroup = 5;

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
    }

    public Point3D windowCorner = new Point3D(0,0,0); // Upper left corner of window in Euclidean case

    public void update() {
        final Tiling tiling = tilings.get(current);
        Group tiles = new Group();

        if (tiling.getGeometry() == FDomain.Geometry.Euclidean){
            double width=700, height=500;
            //System.out.println(tiling.refPointEuclidean);

            if (!isDrawFundamentalDomainOnly() && (windowCorner.getX()-200 >= tiling.refPointEuclidean.getX() || // Worst case: fDomain is completely out of range and must be shifted back.
                    tiling.refPointEuclidean.getX() >= windowCorner.getX()+width+250 ||
                    windowCorner.getY()-200 >= tiling.refPointEuclidean.getY() ||
                    tiling.refPointEuclidean.getY() >= windowCorner.getY()+height+250)) {
                recenterFDomain(tiling.calculateBackShiftEuclidean(windowCorner, width, height)); // Shifts back fDomain into valid range (slower algorithm)
                tiling.setResetEuclidean(true); // Variable to calculate a transform leading back into the visible window
                tiles = tiling.createTilingEuclidean(isDrawFundamentalDomainOnly(), windowCorner, width, height);
                recenterFDomain(tiling.transformFDEuclidean); // Shifts back fDomain into visible window (faster algorithm)
            }
            else { // If fDomain is out of visible window
                if (!isDrawFundamentalDomainOnly() && (windowCorner.getX() - 1 >= tiling.refPointEuclidean.getX() ||
                        tiling.refPointEuclidean.getX() >= windowCorner.getX() + width || windowCorner.getY() - 1 >= tiling.refPointEuclidean.getY() ||
                        tiling.refPointEuclidean.getY() >= windowCorner.getY() + height)) {
                    tiling.setResetEuclidean(true);
                    tiles = tiling.createTilingEuclidean(isDrawFundamentalDomainOnly(), windowCorner, width, height);
                    recenterFDomain(tiling.transformFDEuclidean); // Shifts back fDomain into visible window (fast algorithm)
                }
                else { // If fDomain is inside visible window
                    tiles = tiling.createTilingEuclidean(isDrawFundamentalDomainOnly(), windowCorner, width, height);
                }
            }
            //Add rectangle for debugging
            Rectangle rect = new Rectangle(width, height);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.BLACK);
            tiles.getChildren().addAll(rect);

            camera.setTranslateZ(-500);
            camera.setFarClip(10000);

            controller.getPoincareButton().setVisible(false);
            controller.getKleinButton().setVisible(false);
            controller.getIncreaseButton().setVisible(false);
            controller.getDecreaseButton().setVisible(false);
        }
        else if (tiling.getGeometry() == FDomain.Geometry.Spherical){
            tiles = tiling.createTilingSpherical(isDrawFundamentalDomainOnly());

            camera.setTranslateZ(-500);
            camera.setFieldOfView(35);
            camera.setFarClip(600);

            controller.getPoincareButton().setVisible(false);
            controller.getKleinButton().setVisible(false);
            controller.getIncreaseButton().setVisible(false);
            controller.getDecreaseButton().setVisible(false);

        }
        else if (tiling.getGeometry() == FDomain.Geometry.Hyperbolic){
            double maxDist = Math.cosh(0.5 * getLimitHyperbolicGroup());  // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.
            System.out.println("Height of hyperboloid " + 100*maxDist);

            //Reset Fundamental Domain if necessary:
            if (!isDrawFundamentalDomainOnly() && (Tiling.refPointHyperbolic.getZ() >= 3 || Tiling.refPointHyperbolic.getZ() >= 0.6 * maxDist)) {
                tiling.setResetHyperbolic(true);
                tiles = tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist);
                recenterFDomain(tiling.transformFDHyperbolic);
            }
            else {
                tiles = tiling.createTilingHyperbolic(isDrawFundamentalDomainOnly(), maxDist);
            }

            //Camera:
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

            controller.getPoincareButton().setVisible(true);
            controller.getKleinButton().setVisible(true);
            controller.getIncreaseButton().setVisible(true);
            controller.getDecreaseButton().setVisible(true);
        }

        setUseDepthBuffer(!tiling.getGeometry().equals(FDomain.Geometry.Euclidean));


        getWorld().getChildren().clear();
        getWorld().getChildren().addAll(tiles);
        if (tiling.getGeometry() == FDomain.Geometry.Hyperbolic){ getWorld().getChildren().add(light); }
        getController().getStatusTextField().setText(tilings.get(current).getStatusLine());
        GroupEditing.update(this);

        controller.updateNavigateTilings();
    }


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
}
