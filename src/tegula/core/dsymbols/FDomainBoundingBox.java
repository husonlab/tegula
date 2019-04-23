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

package tegula.core.dsymbols;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public class FDomainBoundingBox {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double minX3D;
    private double maxX3D;
    private double minY3D;
    private double maxY3D;
    private double minZ3D;
    private double maxZ3D;

    private final FDomain fDomain;

    public FDomainBoundingBox(FDomain fDomain) {
        this.fDomain = fDomain;
        update();
    }

    public void update() {
        minX = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        minY = Double.MAX_VALUE;
        maxY = Double.MIN_VALUE;

        for (Point2D[] array : fDomain.getCoordinates()) {
            if (array != null) {
                for (Point2D apt : array) {
                    if (apt != null) {
                        minX = Math.min(minX, apt.getX());
                        maxX = Math.max(maxX, apt.getX());
                        minY = Math.min(minY, apt.getY());
                        maxY = Math.max(maxY, apt.getY());
                    }
                }
            }
        }

        minX3D = Double.MAX_VALUE;
        maxX3D = Double.MIN_VALUE;
        minY3D = Double.MAX_VALUE;
        maxY3D = Double.MIN_VALUE;
        minZ3D = Double.MAX_VALUE;
        maxZ3D = Double.MIN_VALUE;

        for (Point3D[] array : fDomain.getCoordinates3D()) {
            if (array != null) {
                for (Point3D apt : array) {
                    if (apt != null) {
                        minX3D = Math.min(minX3D, apt.getX());
                        maxX3D = Math.max(maxX3D, apt.getX());
                        minY3D = Math.min(minY3D, apt.getY());
                        maxY3D = Math.max(maxY3D, apt.getY());
                        minZ3D = Math.min(minZ3D, apt.getZ());
                        maxZ3D = Math.max(maxZ3D, apt.getZ());
                    }
                }
            }
        }
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public double getMinX3D() {
        return minX3D;
    }

    public void setMinX3D(double minX3D) {
        this.minX3D = minX3D;
    }

    public double getMaxX3D() {
        return maxX3D;
    }

    public void setMaxX3D(double maxX3D) {
        this.maxX3D = maxX3D;
    }

    public double getMinY3D() {
        return minY3D;
    }

    public void setMinY3D(double minY3D) {
        this.minY3D = minY3D;
    }

    public double getMaxY3D() {
        return maxY3D;
    }

    public void setMaxY3D(double maxY3D) {
        this.maxY3D = maxY3D;
    }

    public double getMinZ3D() {
        return minZ3D;
    }

    public void setMinZ3D(double minZ3D) {
        this.minZ3D = minZ3D;
    }

    public double getMaxZ3D() {
        return maxZ3D;
    }

    public void setMaxZ3D(double maxZ3D) {
        this.maxZ3D = maxZ3D;
    }

    public String toString() {
        return String.format("2D: %.1f %.1f %.1f %.1f\n3D: %.1f %.1f %.1f %.1f %.1f %.1f", minX, maxX, minY, maxY,
                minX3D, maxX3D, minY3D, maxY3D, minZ3D, maxZ3D);
    }
}
