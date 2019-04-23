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

package tegula.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * some Java FX utilities
 * Created by huson on 4/19/16.
 */
public class JavaFXUtils {
    private final static Set<Class> warned = new HashSet<>();

    /**
     * copy a group of meshes, shapes and texts
     *
     * @param group
     * @return copy of group
     */
    public static Group copyGroup(Group group) {
        final Group result = new Group();

        for (Node node : group.getChildren()) {
            if (node instanceof MeshView) {
                final MeshView src = (MeshView) node;
                final MeshView target = new MeshView(((MeshView) node).getMesh());
                target.setMaterial(src.getMaterial());
                target.setUserData(src.getUserData());
                target.getTransforms().addAll(copy(src.getTransforms()));
                target.setTranslateX(src.getTranslateX());
                target.setTranslateY(src.getTranslateY());
                target.setTranslateZ(src.getTranslateZ());

                result.getChildren().add(target);
            } else if (node instanceof Polyline) {
                final Polyline src = (Polyline) node;
                final Polyline target = new Polyline();
                target.getPoints().addAll(src.getPoints());
                target.setFill(src.getFill());
                target.setStroke(src.getStroke());
                target.setStrokeLineCap(src.getStrokeLineCap());
                target.setUserData(src.getUserData());
                target.getTransforms().addAll(copy(src.getTransforms()));
                target.setTranslateX(src.getTranslateX());
                target.setTranslateY(src.getTranslateY());
                target.setTranslateZ(src.getTranslateZ());

                result.getChildren().add(target);
            } else if (node instanceof Sphere) {
                Sphere src = (Sphere) node;
                Sphere target = new Sphere(src.getRadius());
                target.setMaterial(src.getMaterial());
                target.getTransforms().addAll(src.getTransforms());
                target.setUserData(src.getUserData());
                target.getTransforms().addAll(copy(src.getTransforms()));
                target.setTranslateX(src.getTranslateX());
                target.setTranslateY(src.getTranslateY());
                target.setTranslateZ(src.getTranslateZ());

                result.getChildren().add(target);


                target.setRotationAxis(src.getRotationAxis());

                if (src.getUserData() instanceof String && ((src.getUserData()).equals("ref0")))
                    target.setMaterial(new PhongMaterial(Color.ORANGE));

            } else if (node instanceof Cylinder) {
                final Cylinder src = (Cylinder) node;
                final Cylinder target = new Cylinder(src.getRadius(), src.getHeight(), src.getDivisions());
                target.setCullFace(src.getCullFace());
                target.setDrawMode(src.getDrawMode());
                target.setMaterial(src.getMaterial());
                target.setUserData(src.getUserData());
                target.getTransforms().addAll(copy(src.getTransforms()));
                target.setTranslateX(src.getTranslateX());
                target.setTranslateY(src.getTranslateY());
                target.setTranslateZ(src.getTranslateZ());

                result.getChildren().add(target);
            } else if (node instanceof Text) {
                Text src = (Text) node;
                Text target = new Text(src.getText());
                target.setFont(src.getFont());
                target.setFill(src.getFill());
                target.getTransforms().addAll(src.getTransforms());
                target.setUserData(src.getUserData());
                target.getTransforms().addAll(copy(src.getTransforms()));
                result.getChildren().add(target);
            } else if (node instanceof Group) {
                Group src = (Group) node;
                Group target = copyGroup(src);
                target.getTransforms().addAll(src.getTransforms());
                target.setUserData(src.getUserData());
                target.getTransforms().addAll(copy(src.getTransforms()));
                target.setTranslateX(src.getTranslateX());
                target.setTranslateY(src.getTranslateY());
                target.setTranslateZ(src.getTranslateZ());

                result.getChildren().add(target);
            } else if (!warned.contains(node.getClass())) {
                System.err.println("Warning: copyGroup(): not implemented for class: " + node.getClass());
                warned.add(node.getClass());
            }
        }
        result.setRotationAxis(group.getRotationAxis());
        result.setRotate(group.getRotate());

        // enabling these breaks translation in Euclidean case:\
        result.setTranslateX(group.getTranslateX());
        result.setTranslateY(group.getTranslateY());
        result.setTranslateZ(group.getTranslateZ());
        result.getTransforms().setAll(copy(group.getTransforms()));

        return result;
    }

    /**
     * copy a list of transforms
     *
     * @param transforms
     * @return copies
     */
    private static Collection<Transform> copy(Collection<Transform> transforms) {
        Collection<Transform> result = new ArrayList<>(transforms.size());
        for (Transform transform : transforms) {
            result.add(transform.clone());
        }
        return result;
    }

}
