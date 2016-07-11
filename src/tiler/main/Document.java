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
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

    private boolean camPoincare = true; // Variable saving camera settings

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


    public void update() {
        final Tiling tiling = tilings.get(current);
        Group tiles = new Group();

        if (tiling.getGeometry() == FDomain.Geometry.Euclidean){
            Point3D refPoint = new Point3D(0,0,0); double width=800, height=600;
            tiles = tiling.createTiling(refPoint,width,height);

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
            tiles = tiling.createTiling();

            camera.setTranslateZ(-500);
            camera.setFieldOfView(35);
            camera.setFarClip(600);

            controller.getPoincareButton().setVisible(false);
            controller.getKleinButton().setVisible(false);
            controller.getIncreaseButton().setVisible(false);
            controller.getDecreaseButton().setVisible(false);

        }
        else if (tiling.getGeometry() == FDomain.Geometry.Hyperbolic){
            double maxDist = Math.cosh(0.5*Main.counter);  // maxDist is height of hyperboloid defined by z^2 = x^2+y^2+1.
            System.out.println("Height of hperboloid " + 100*maxDist);
            tiles = tiling.createTiling(maxDist);

            camera.setFieldOfView(90);
            if (camPoincare){
                camera.setFarClip(100*(maxDist+1));
                camera.setTranslateZ(-100);
            }
            else{
                camera.setTranslateZ(0);
                camera.setFarClip(100*maxDist);
            }


            controller.getPoincareButton().setVisible(true);
            controller.getKleinButton().setVisible(true);
            controller.getIncreaseButton().setVisible(true);
            controller.getDecreaseButton().setVisible(true);
        }

        setUseDepthBuffer(!tiling.getGeometry().equals(FDomain.Geometry.Euclidean));

        getWorld().getChildren().clear();
        getWorld().getChildren().addAll(tiles);
        getController().getStatusTextField().setText(tilings.get(current).getStatusLine());
        GroupEditing.update(this);

        controller.updateNavigateTilings();
    }

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
}
