/*
 * Base.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
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

package tegula.core.funtiles.data;

/**
 * basic methods for setting up stuff
 * Created by huson on 3/28/16.
 */
public class Base {

    public static int create_ncr(DelaneySymbolWithGeometry d, int np) {
        final int nc = d.getNumberOfNodeCoordinates();
        check_nod(d, np);
        d.setNumberNodeCoordinates(nc + 1);
        d.getNodeCoordinates(nc).setNp(np);
        return nc;
    }

    public static int create_ecr(DelaneySymbolWithGeometry d, int ep) {
        final int ec = d.getNumberOfEdgeCoordinates();
        check_edg(d, ep);
        d.setNumberEdgeCoordinates(ec + 1);
        d.getEdgeCoordinates(ec).setEp(ep);
        return ec;
    }

    public static int create_ocr(DelaneySymbolWithGeometry d, int op) {
        final int oc = d.getNumberOfOrbitCoordinates();
        check_orb(d, op);
        d.setNumberOrbitCoordinates(oc + 1);
        d.getOrbitCoordinates(oc).setOp(op);
        return (oc);
    }

    public static void create_orb_crd(DelaneySymbolWithGeometry d, int op, int oc) {
        final int uu = d.getOrb(op).getNumberOfCoordinates();
        Base.check_orb(d, op);
        Base.check_ocr(d, oc);
        d.getOrb(op).setCr(uu, oc);
    }

    public static void create_orb_nod(DelaneySymbolWithGeometry d, int op, int np) {
        final int uu = d.getOrb(op).getNps();
        check_orb(d, op);
        check_nod(d, np);
        d.getOrb(op).setNp(uu, np);
    }

    public static void create_orb_edg(DelaneySymbolWithGeometry d, int op, int ep) {
        final int uu = d.getOrb(op).getEps();
        check_orb(d, op);
        check_edg(d, ep);
        d.getOrb(op).setEp(uu, ep);
    }

    public static void create_orb_orb(DelaneySymbolWithGeometry d, int op, int oq) {
        final int uu = d.getOrb(op).getOps();
        check_orb(d, op);
        check_orb(d, oq);
        d.getOrb(op).setOp(uu, oq);
    }

    public static void create_ocr_ncr(DelaneySymbolWithGeometry d, int oc, int nc) {
        int uu = d.getOrbitCoordinates(oc).getNcs();
        check_ocr(d, oc);
        check_ncr(d, nc);
        d.getOrbitCoordinates(oc).setNc(uu, nc);
    }

    public static void create_ocr_ecr(DelaneySymbolWithGeometry d, int oc, int ec) {
        final int uu = d.getOrbitCoordinates(oc).getEcs();
        check_ocr(d, oc);
        check_ecr(d, ec);
        d.getOrbitCoordinates(oc).setEc(uu, ec);
    }

    public static void create_fcr(DelaneySymbolWithGeometry d, int oc) {
        final int of = d.getNumberOfBorderCoordinates();
        check_ocr(d, oc);
        d.setBorderCoordinates(of, oc);
    }

    public static void clear_orb_crds(DelaneySymbolWithGeometry d, int op) {
        d.getOrb(op).clearCr();
    }

    /***********************
     * Check
     ***********************/

    public static void check_nod(DelaneySymbolWithGeometry d, int np) {
        check_nod_adr(np);
        check_nod_def(d, np);
    }

    public static void check_edg(DelaneySymbolWithGeometry d, int ep) {
        check_edg_adr(ep);
        check_edg_def(d, ep);
    }

    public static void check_orb(DelaneySymbolWithGeometry d, int op) {
        check_orb_adr(op);
        check_orb_def(d, op);
    }

    public static void check_ncr(DelaneySymbolWithGeometry d, int nc) {
        check_ncr_adr(nc);
        check_ncr_def(d, nc);
    }

    public static void check_ecr(DelaneySymbolWithGeometry d, int ec) {
        check_ecr_adr(ec);
        check_ecr_def(d, ec);
    }

    public static void check_ocr(DelaneySymbolWithGeometry d, int oc) {
        check_ocr_adr(oc);
        check_ocr_def(d, oc);
    }

    public static void check_nod_adr(int np) {
        if (np < 0) {
            throw new RuntimeException("Illegal flag number");
        }
    }

    public static void check_edg_adr(int ep) {
        if (ep < 0) {
            throw new RuntimeException("Illegal edge number");
        }
    }

    public static void check_orb_adr(int op) {
        if (op < 0) {
            throw new RuntimeException("Illegal orbit number");
        }
    }

    public static void check_ncr_adr(int nc) {
        if (nc < 0) {
            throw new RuntimeException("Illegal flag-coordinates-number");
        }
    }

    public static void check_ecr_adr(int ec) {
        if (ec < 0) {
            throw new RuntimeException("Illegal edge-coordinates-number");
        }
    }

    public static void check_ocr_adr(int oc) {
        if (oc < 0) {
            throw new RuntimeException("Illegal orbit-coordinates-number");
        }
    }

    public static void check_nod_def(DelaneySymbolWithGeometry d, int np) {
        if (np >= d.getNumberOfNodes()) {
            throw new RuntimeException("Undefined flag number");
        }
    }

    public static void check_edg_def(DelaneySymbolWithGeometry d, int ep) {
        if (ep >= d.getNumberOfEdges()) {
            throw new RuntimeException("Undefined edge number");
        }
    }

    public static void check_orb_def(DelaneySymbolWithGeometry d, int op) {
        if (op >= d.getNumberOfOrbits()) {
            throw new RuntimeException("Undefined orbit number");
        }
    }

    public static void check_ncr_def(DelaneySymbolWithGeometry d, int nc) {
        if (nc >= d.getNumberOfNodeCoordinates()) {
            throw new RuntimeException("Undefined flag-coord-number");
        }
    }

    public static void check_ecr_def(DelaneySymbolWithGeometry d, int ec) {
        if (ec >= d.getNumberOfEdgeCoordinates()) {
            throw new RuntimeException("Undefined edge-coord-number");
        }
    }

    public static void check_ocr_def(DelaneySymbolWithGeometry d, int oc) {
        if (oc >= d.getNumberOfOrbitCoordinates()) {
            throw new RuntimeException("undefined orbit-coord-number");
        }
    }
}
