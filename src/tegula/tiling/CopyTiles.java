/*
 * CopyTiles.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.tiling;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;

import java.util.HashSet;
import java.util.Set;

/**
 * Copy the tiles in a fundamental domain
 * Daniel Huson 4/2016
 */
public class CopyTiles {
    private final static Set<Class> warned = new HashSet<>();

    /**
     * copy a group of meshes, shapes and texts
     *
     * @param group
     * @return copy of group
     */
    public static Group apply(Group group) {
        final Group result = new Group();

        for (Node node : group.getChildren()) {
            if (node instanceof MeshView) {
                MeshView src = (MeshView) node;
                MeshView target = new MeshView(((MeshView) node).getMesh());
                target.setMaterial(src.getMaterial());
                target.setId(src.getId());
                target.setUserData(src.getUserData());
                target.setDrawMode(src.getDrawMode());
                result.getChildren().add(target);

                if (node.getOnMouseClicked() != null)
                    target.setOnMouseClicked(node.getOnMouseClicked());

            } else if (node instanceof ImageView) {
                ImageView src = (ImageView) node;
                ImageView target = new ImageView(((ImageView) node).getImage());
                target.setFitWidth(src.getFitWidth());
                target.setFitHeight(src.getFitHeight());
                target.setId(src.getId());
                target.setUserData(src.getUserData());
                result.getChildren().add(target);
                target.setTranslateX(src.getTranslateX());
                target.setTranslateY(src.getTranslateY());
                target.setTranslateZ(src.getTranslateZ());
                target.setRotationAxis(src.getRotationAxis());
                target.setRotate(src.getRotate());

                if (node.getOnMouseClicked() != null)
                    target.setOnMouseClicked(node.getOnMouseClicked());

            } else if (node instanceof Polyline) {
                Polyline src = (Polyline) node;
                Polyline target = new Polyline();
                target.getPoints().addAll(src.getPoints());
                target.setFill(src.getFill());
                target.setStroke(src.getStroke());
                target.setStrokeLineCap(src.getStrokeLineCap());
                target.setId(src.getId());
                target.setUserData(src.getUserData());
                result.getChildren().add(target);

                if (node.getOnMouseClicked() != null)
                    target.setOnMouseClicked(node.getOnMouseClicked());

            } else if (node instanceof Sphere) {
                Sphere src = (Sphere) node;
                Sphere target = new Sphere(src.getRadius());
                target.setMaterial(src.getMaterial());
                target.getTransforms().addAll(src.getTransforms());
                target.setId(src.getId());
                target.setUserData(src.getUserData());
                result.getChildren().add(target);

                if (node.getOnMouseClicked() != null)
                    target.setOnMouseClicked(node.getOnMouseClicked());

            } else if (node instanceof Cylinder) {
                Cylinder src = (Cylinder) node;
                Cylinder target = new Cylinder(src.getRadius(), src.getHeight(), src.getDivisions());
                target.setCullFace(src.getCullFace());
                target.setDrawMode(src.getDrawMode());
                target.setMaterial(src.getMaterial());
                target.getTransforms().addAll(src.getTransforms());
                target.setId(src.getId());
                target.setUserData(src.getUserData());
                result.getChildren().add(target);

                if (node.getOnMouseClicked() != null)
                    target.setOnMouseClicked(node.getOnMouseClicked());

            } else if (node instanceof Text) {
                Text src = (Text) node;
                Text target = new Text(src.getText());
                target.setFont(src.getFont());
                target.setFill(src.getFill());
                target.getTransforms().addAll(src.getTransforms());
                target.setId(src.getId());
                target.setUserData(src.getUserData());
                result.getChildren().add(target);
            } else if (node instanceof Group) {
                Group src = (Group) node;
                Group target = apply(src);
                target.getTransforms().addAll(src.getTransforms());
                target.setId(src.getId());
                target.setUserData(src.getUserData());
                result.getChildren().add(target);

                if (node.getOnMouseClicked() != null)
                    target.setOnMouseClicked(node.getOnMouseClicked());
            } else if (!warned.contains(node.getClass())) {
                System.err.println("Warning: copyGroup(): not implemented for class: " + node.getClass());
                warned.add(node.getClass());
            }
        }
        result.setId(group.getId());
        result.setUserData(group.getUserData());
        result.setRotationAxis(group.getRotationAxis());
        result.setRotate(group.getRotate());
        result.setTranslateX(group.getTranslateX());
        result.setTranslateY(group.getTranslateY());
        result.setTranslateZ(group.getTranslateZ());

        return result;
    }

}
