/*
 * ExportControls.java Copyright (C) 2026 Daniel H. Huson
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

package tegula.tilingeditor;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import jloda.fx.qr.QRViewUtils;
import jloda.fx.util.ClipboardUtils;
import jloda.fx.util.DraggableUtils;
import tegula.core.dsymbols.DSymbolCode;

/**
 * the Export menu button: copy the tiling's name or its image, and show the name or the QR code over the tiling
 * Daniel Huson, 8.2026
 */
public class ExportControls {
    /**
     * set up the Export menu button and the overlay that carries the name and the QR code
     */
    public static void setup(TilingEditorTab tilingEditorTab) {
        final TilingEditorTabController controller = tilingEditorTab.getController();

        // The name and the QR code float over the main pane, rather than over the whole tab, so that a
        // snapshot of the main pane catches them and leaves the toolbar and the titled panes out.
        final AnchorPane overlay = new AnchorPane();
        overlay.setPickOnBounds(false); // the mouse reaches the tiling everywhere except on the name and the code

        final Label nameLabel = new Label();
        nameLabel.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12; -fx-text-fill: black;"
                           + " -fx-background-color: rgba(255,255,255,0.75); -fx-background-radius: 4; -fx-padding: 2 6 2 6;");
        nameLabel.visibleProperty().bind(controller.getShowNameMenuItem().selectedProperty());
        AnchorPane.setTopAnchor(nameLabel, 20.0);
        AnchorPane.setLeftAnchor(nameLabel, 20.0);
        overlay.getChildren().add(nameLabel); // must be non-empty, QRViewUtils inserts the code after this
        DraggableUtils.makeDraggableInAnchorPane(nameLabel);

        controller.getMainPane().getChildren().add(overlay);

        final InvalidationListener nameListener = e -> nameLabel.setText(name(tilingEditorTab));
        tilingEditorTab.getTilingPane().lastDSymbolUpdateProperty().addListener(nameListener);
        controller.getShowNameMenuItem().selectedProperty().addListener(nameListener);

        QRViewUtils.setup(overlay, tilingEditorTab.getTilingPane().lastDSymbolUpdateProperty(),
                () -> name(tilingEditorTab), new SimpleObjectProperty<>(),
                controller.getShowQRCodeMenuItem().selectedProperty());

        controller.getCopySymbolMenuItem().setOnAction(e -> ClipboardUtils.putString(name(tilingEditorTab)));
        controller.getCopyImageMenuItem().setOnAction(e -> ClipboardUtils.putImage(controller.getMainPane()));
    }

    /**
     * the tiling's self-contained name, for example DS18-9ANA-NANA-M040-0ANA-K14
     */
    private static String name(TilingEditorTab tilingEditorTab) {
        return tilingEditorTab.getTiling() == null ? null : DSymbolCode.encode(tilingEditorTab.getTiling().getDSymbol());
    }
}
