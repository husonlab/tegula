/*
 * Copyright (C) 2013-2015 F(X)yz, 
 * Sean Phillips, Jason Pollastrini and Jose Pereda
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tiler.tiling;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.TriangleMesh;

import java.util.List;

/**
 * Sets up a line trough a list of 3d-points
 * @author Ruediger
 */
public class PolyLine3D extends TriangleMesh {
    public PolyLine3D(List<Point3D> points) {
        for(Point3D point: points) {
            getPoints().addAll((float) (point.getX()), (float) (point.getY()), (float) (point.getZ())); //Add points from list
        }
        //Dummy texture coordinates
        getTexCoords().addAll(0,0);
        //Add each line segments
        for(int i=0; i<points.size()-1; i++) {
            getFaces().addAll(i,0,i+1,0,i,0);  // Endpoints of line segment serve as points for triangle
        }
    }
}