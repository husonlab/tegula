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
import tiler.color.ColorSchemeManager;

/**
 * class representing styling choices for tiling
 * Daniel Huson, 11.18
 */
public class TilingStyle {
    private final IntegerProperty bandWidth = new SimpleIntegerProperty(15);
    private final SimpleObjectProperty<Color> bandColor = new SimpleObjectProperty<>(Color.WHITE);
    private final IntegerProperty bandCapFineness = new SimpleIntegerProperty(24);

    private final BooleanProperty showFaces = new SimpleBooleanProperty(true);
    private final BooleanProperty showBands = new SimpleBooleanProperty(true);

    private final BooleanProperty showBackEdges = new SimpleBooleanProperty(false);
    private final BooleanProperty smoothEdges = new SimpleBooleanProperty(true);

    private final ObservableList<Color> tileColors = FXCollections.observableArrayList();

    private final StringProperty tileColorsScheme = new SimpleStringProperty();

    private final BooleanProperty showFundamentalChambers = new SimpleBooleanProperty(false);
    private final BooleanProperty showAllChambers = new SimpleBooleanProperty(false);
    private final BooleanProperty showHandles = new SimpleBooleanProperty(false);
    private final BooleanProperty showSymmetryIcons = new SimpleBooleanProperty(false);
    private final BooleanProperty showOtherStuff = new SimpleBooleanProperty(false);


    public TilingStyle() {
        setTileColorsScheme(ColorSchemeManager.getInstance().getLastColorScheme());
    }

    public void setTileColorsScheme(String colorSchemeName) {
        tileColorsScheme.set(colorSchemeName);
        tileColors.setAll(ColorSchemeManager.getInstance().getColorScheme(colorSchemeName));
    }

    public String getTileColorsScheme() {
        return tileColorsScheme.get();
    }

    public ObservableList<Color> getTileColors() {
        return tileColors;
    }

    public Color getTileColor(int tileNumber) {
        return tileColors.get(tileNumber % tileColors.size());
    }

    public void setTileColor(int tileNumber, Color color) {
        { // pad to size
            final ObservableList<Color> colorScheme = ColorSchemeManager.getInstance().getColorScheme(getTileColorsScheme());
            while (tileNumber >= tileColors.size())
                tileColors.add(colorScheme.get(tileNumber % colorScheme.size()));
        }
        tileColors.set(tileNumber, color);
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

    public boolean isShowFundamentalChambers() {
        return showFundamentalChambers.get();
    }

    public BooleanProperty showFundamentalChambersProperty() {
        return showFundamentalChambers;
    }

    public void setShowFundamentalChambers(boolean showFundamentalChambers) {
        this.showFundamentalChambers.set(showFundamentalChambers);
    }

    public boolean isShowAllChambers() {
        return showAllChambers.get();
    }

    public BooleanProperty showAllChambersProperty() {
        return showAllChambers;
    }

    public void setShowAllChambers(boolean showAllChambers) {
        this.showAllChambers.set(showAllChambers);
    }

    public boolean isShowHandles() {
        return showHandles.get();
    }

    public BooleanProperty showHandlesProperty() {
        return showHandles;
    }

    public void setShowHandles(boolean showHandles) {
        this.showHandles.set(showHandles);
    }

    public boolean isShowSymmetryIcons() {
        return showSymmetryIcons.get();
    }

    public BooleanProperty showSymmetryIconsProperty() {
        return showSymmetryIcons;
    }

    public void setShowSymmetryIcons(boolean showSymmetryIcons) {
        this.showSymmetryIcons.set(showSymmetryIcons);
    }

    public boolean isShowOtherStuff() {
        return showOtherStuff.get();
    }

    public BooleanProperty showOtherStuffProperty() {
        return showOtherStuff;
    }

    public void setShowOtherStuff(boolean showOtherStuff) {
        this.showOtherStuff.set(showOtherStuff);
    }
}
