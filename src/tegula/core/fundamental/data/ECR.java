/*
 * ECR.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.core.fundamental.data;

/**
 * edge coordinates
 * Created by huson on 3/27/16.
 * Based on del_data.h by Klaus Westphal, 1990
 */
public class ECR {
    private int ep;                // edge number
    private int nca = -1, ncb = -1;            // related  node coords
    private int ecax = -1, ecay = -1, ecbx = -1, ecby = -1;    // related  edge coords
    private int ocx = -1, ocy = -1;            // related orbit coords
    private double dist;            // center distance
    private double beta = Math.PI;            // center angle
    private double posx;            // x-Coordinate
    private double posy;            // y-Coordinate

    public int getEp() {
        return ep;
    }

    public void setEp(int ep) {
        this.ep = ep;
    }

    public int getNca() {
        return nca;
    }

    public void setNca(int nca) {
        this.nca = nca;
    }

    public int getNcb() {
        return ncb;
    }

    public void setNcb(int ncb) {
        this.ncb = ncb;
    }

    public int getEcax() {
        return ecax;
    }

    public void setEcax(int ecax) {
        this.ecax = ecax;
    }

    public int getEcay() {
        return ecay;
    }

    public void setEcay(int ecay) {
        this.ecay = ecay;
    }

    public int getEcbx() {
        return ecbx;
    }

    public void setEcbx(int ecbx) {
        this.ecbx = ecbx;
    }

    public int getEcby() {
        return ecby;
    }

    public void setEcby(int ecby) {
        this.ecby = ecby;
    }

    public int getOcx() {
        return ocx;
    }

    public void setOcx(int ocx) {
        this.ocx = ocx;
    }

    public int getOcy() {
        return ocy;
    }

    public void setOcy(int ocy) {
        this.ocy = ocy;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getPosx() {
        return posx;
    }

    public void setPosx(double posx) {
        this.posx = posx;
    }

    public double getPosy() {
        return posy;
    }

    public void setPosy(double posy) {
        this.posy = posy;
    }
}
