/*
 * SingleTilingWindow.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.xtra;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tegula.core.dsymbols.DSymbol;
import tegula.main.TilingStyle;
import tegula.tilingpane.TilingPane;


/**
 * setup and show a single tiling window
 * Only for testing purposes, not part of the tegula program
 * Daniel Huson, 4.2019
 */
public class SingleTilingWindow {
    /**
     * constructor
     *
     * @param dSymbol
     */
    public SingleTilingWindow(DSymbol dSymbol) {
        final Stage stage = new Stage();
        stage.setTitle("New SingleWindow");

        final TilingStyle tilingStyle = new TilingStyle();
        tilingStyle.setBandColor(Color.WHITE);

        final TilingPane tilingPane = new TilingPane(dSymbol, tilingStyle);

        final AnchorPane root = new AnchorPane();
        AnchorPane.setBottomAnchor(tilingPane, 0.0);
        AnchorPane.setTopAnchor(tilingPane, 0.0);
        AnchorPane.setLeftAnchor(tilingPane, 0.0);
        AnchorPane.setRightAnchor(tilingPane, 0.0);
        root.getChildren().add(tilingPane);

        root.widthProperty().addListener((c, o, n) -> {
            tilingPane.setPrefWidth(n.doubleValue());
            if (o != null && o.doubleValue() > 0)
                tilingPane.setEuclideanWidth(tilingPane.getEuclideanWidth() / o.doubleValue() * n.doubleValue());
        });
        root.heightProperty().addListener((c, o, n) -> {
            tilingPane.setPrefHeight(n.doubleValue());
            if (o != null && o.doubleValue() > 0)
                tilingPane.setEuclideanHeight(tilingPane.getEuclideanHeight() / o.doubleValue() * n.doubleValue());
        });


        stage.setScene(new Scene(root, 800, 800));
        stage.sizeToScene();
        stage.show();


    }
}
