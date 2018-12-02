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

package tiler.main;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * class representing styling choices for tiling
 * Daniel Huson, 11.18
 */
public class TilingStyle {
    public enum ColorScheme {Random}

    private final IntegerProperty bandWidth = new SimpleIntegerProperty(15);
    private final SimpleObjectProperty<Color> bandColor = new SimpleObjectProperty<>(Color.WHITE);
    private final IntegerProperty bandCapFineness = new SimpleIntegerProperty(24);

    private final BooleanProperty showFaces = new SimpleBooleanProperty(true);
    private final BooleanProperty showBands = new SimpleBooleanProperty(true);
    private final BooleanProperty showBandCaps = new SimpleBooleanProperty(true);

    private final BooleanProperty showBackEdges = new SimpleBooleanProperty(false);
    private final BooleanProperty smoothEdges = new SimpleBooleanProperty(true);

    private final ObservableList<SimpleObjectProperty<Color>> tileColors = FXCollections.observableArrayList();
    private int randomTileColorSeed = 666;

    TilingStyle() {
        setupTileColors(ColorScheme.Random);
    }

    /**
     * setup the tile colors scheme
     *
     * @param colorScheme
     */
    public void setupTileColors(ColorScheme colorScheme) {
        switch (colorScheme) {
            default:
            case Random: {
                tileColors.clear();
                final Random random = new Random(randomTileColorSeed);
                for (int i = 0; i < 255; i++) {
                    final Color color = new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
                    tileColors.add(new SimpleObjectProperty<>(color));
                }
            }
        }
    }

    public void changeRandomColors() {
        randomTileColorSeed = (new Random()).nextInt();
    }

    public SimpleObjectProperty<Color> tileColor(int tileNumber) {
        return tileColors.get(tileNumber % tileColors.size());
    }

    public Color getTileColor(int tileNumber) {
        return tileColor(tileNumber).get();
    }

    public void setTileColor(int tileNumber, Color color) {
        tileColor(tileNumber).set(color);
    }

    public int getBandWidth() {
        return bandWidth.get();
    }

    public IntegerProperty bandWidthProperty() {
        return bandWidth;
    }

    public void setBandWidth(int bandWidth) {
        this.bandWidth.set(bandWidth);
    }

    public Color getBandColor() {
        return bandColor.get();
    }

    public SimpleObjectProperty<Color> bandColorProperty() {
        return bandColor;
    }

    public void setBandColor(Color bandColor) {
        this.bandColor.set(bandColor);
    }

    public int getBandCapFineness() {
        return bandCapFineness.get();
    }

    public IntegerProperty bandCapFinenessProperty() {
        return bandCapFineness;
    }

    public void setBandCapFineness(int bandCapFineness) {
        this.bandCapFineness.set(bandCapFineness);
    }

    public boolean isShowBands() {
        return showBands.get();
    }

    public BooleanProperty showBandsProperty() {
        return showBands;
    }

    public void setShowBands(boolean showBands) {
        this.showBands.set(showBands);
    }

    public boolean isShowBandCaps() {
        return showBandCaps.get();
    }

    public BooleanProperty showBandCapsProperty() {
        return showBandCaps;
    }

    public void setShowBandCaps(boolean showBandCaps) {
        this.showBandCaps.set(showBandCaps);
    }

    public boolean isShowFaces() {
        return showFaces.get();
    }

    public BooleanProperty showFacesProperty() {
        return showFaces;
    }

    public void setShowFaces(boolean showFaces) {
        this.showFaces.set(showFaces);
    }

    public boolean isSmoothEdges() {
        return smoothEdges.get();
    }

    public BooleanProperty smoothEdgesProperty() {
        return smoothEdges;
    }

    public void setSmoothEdges(boolean smoothEdges) {
        this.smoothEdges.set(smoothEdges);
    }

    public boolean isShowBackEdges() {
        return showBackEdges.get();
    }

    public BooleanProperty showBackEdgesProperty() {
        return showBackEdges;
    }

    public void setShowBackEdges(boolean showBackEdges) {
        this.showBackEdges.set(showBackEdges);
    }
}
