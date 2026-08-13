/*
 * TilingStyle.java Copyright (C) 2026 Daniel H. Huson
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

package tegula.window;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import jloda.fx.util.ColorSchemeManager;

/**
 * class representing styling choices for tiling
 * Daniel Huson, 11.18
 */
public class TilingStyle {
    /**
     * Subtle specular highlight applied to all tile-face materials. IMPORTANT: the specular color must be
     * non-null. With a null specular color, JavaFX's point-light shader renders spherical tile faces black at
     * grazing angles (the "dark ring" bug that made point-light mode look broken). Keep this applied wherever a
     * tile-face PhongMaterial is created.
     */
    public static final Color TILE_SPECULAR_COLOR = Color.gray(0.2);
    public static final double TILE_SPECULAR_POWER = 128;

    /**
     * Applies the standard, subtle tile specular highlight to the given material. Always use this for tile-face
     * materials so the specular color is never left null (see {@link #TILE_SPECULAR_COLOR}).
     */
    public static void applyTileSpecular(PhongMaterial material) {
        material.setSpecularColor(TILE_SPECULAR_COLOR);
        material.setSpecularPower(TILE_SPECULAR_POWER);
    }

    private final Group decorations = new Group();

    private final IntegerProperty bandWidth = new SimpleIntegerProperty(4);
    private final IntegerProperty bandCapFineness = new SimpleIntegerProperty(24);

    private final SimpleObjectProperty<Color> bandColor = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty bandOpacity = new SimpleDoubleProperty(1);

    private final BooleanProperty showFaces = new SimpleBooleanProperty(true);
    private final BooleanProperty showBackFaces = new SimpleBooleanProperty(false);

    private final BooleanProperty showEdges = new SimpleBooleanProperty(true);
    private final BooleanProperty showBackEdges = new SimpleBooleanProperty(false);

    private final BooleanProperty showVertices = new SimpleBooleanProperty(false);
    private final BooleanProperty showBackVertices = new SimpleBooleanProperty(false);

    private final BooleanProperty sphericalUsePointLight = new SimpleBooleanProperty(true);

    private final BooleanProperty smoothEdges = new SimpleBooleanProperty(true);

    private final ObservableList<Color> tileColors = FXCollections.observableArrayList();

    private final DoubleProperty tileOpacity = new SimpleDoubleProperty(0.8);

    private final StringProperty tileColorsScheme = new SimpleStringProperty();

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.TRANSPARENT);

    private final BooleanProperty bendAnEdge = new SimpleBooleanProperty(false);

    private final BooleanProperty showAllChambers = new SimpleBooleanProperty(false);
    private final BooleanProperty showHandles = new SimpleBooleanProperty(false);
    private final BooleanProperty showSymmetryIcons = new SimpleBooleanProperty(false);
    private final BooleanProperty showDecorations = new SimpleBooleanProperty(false);

    private final ObjectProperty<CameraSettings.HyperbolicModel> hyperbolicModel = new SimpleObjectProperty<>(CameraSettings.HyperbolicModel.Poincare);
    public static final int minLimitHyperbolicGroup = 5;
    private final IntegerProperty hyperbolicLimit = new SimpleIntegerProperty(minLimitHyperbolicGroup);

    public static final BooleanProperty hideFirstTile=new SimpleBooleanProperty(false);

    private final StringProperty styleEdits=new SimpleStringProperty();

    public TilingStyle() {
        tileColorsScheme.addListener((c, o, n) -> tileColors.setAll(ColorSchemeManager.getInstance().getColorScheme(n)));
        setTileColorsScheme(ColorSchemeManager.getInstance().getLastColorScheme());
    }

    public TilingStyle(TilingStyle src) {
        this();
        copy(src);

        showEdges.addListener((c, o, n) -> System.err.println("Show edges: " + n));

        showVertices.addListener((c, o, n) -> System.err.println("Show vertices: " + n));
    }

    public void copy(TilingStyle src) {
        setBandWidth(src.getBandWidth());
        setBandColor(src.getBandColorFullOpacity());
        setBandOpacity(src.getBandOpacity());
        setBandCapFineness(src.getBandCapFineness());
        setShowFaces(src.isShowFaces());
        setShowBackFaces(src.isShowBackFaces());
        setShowEdges(src.isShowEdges());
        setShowBackEdges(src.isShowBackEdges());
        setShowVertices(src.isShowVertices());
        setShowBackVertices(src.isShowBackVertices());

        setSmoothEdges(src.isSmoothEdges());

        tileColors.setAll(src.getTileColors());
        setTileOpacity(src.getTileOpacity());

        setTileColorsScheme(src.getTileColorsScheme());

        setBackgroundColor(src.getBackgroundColor());

        setShowAllChambers(src.isShowAllChambers());
        setShowHandles(src.isShowHandles());
        setShowSymmetryIcons(src.isShowSymmetryIcons());
        setShowDecorations(src.getShowDecorations());

        setHideFirstTile(src.isHideFirstTile());

        setSphericalUsePointLight(src.isSphericalUsePointLight());
    }

    public void setTileColorsScheme(String colorSchemeName) {
        tileColorsScheme.set(colorSchemeName);
    }

    public StringProperty tileColorsSchemeProperty() {
        return tileColorsScheme;
    }

    public String getTileColorsScheme() {
        return tileColorsScheme.get();
    }


    public ObservableList<Color> getTileColors() {
        return tileColors;
    }

    public Color getTileColorFullOpacity(int tileNumber) {
        return tileColors.get((tileNumber - 1) % tileColors.size());
    }

    public Color getTileColor(int tileNumber) {
        if (getTileOpacity() == 1)
            return tileColors.get((tileNumber - 1) % tileColors.size());
        else
            return tileColors.get((tileNumber - 1) % tileColors.size()).deriveColor(1, 1, 1, getTileOpacity());
    }

    public void setTileColor(int tileNumber, Color color) {
        { // pad to size
            final ObservableList<Color> colorScheme = ColorSchemeManager.getInstance().getColorScheme(getTileColorsScheme());
            while (tileNumber >= tileColors.size())
                tileColors.add(colorScheme.get((tileNumber - 1) % colorScheme.size()));
        }
        tileColors.set(tileNumber - 1, color);
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

    public Color getBandColorFullOpacity() {
        return bandColor.get();
    }

    public Color getBandColor() {
        if (getBandOpacity() == 1)
            return bandColor.get();
        else
            return bandColor.get().deriveColor(1, 1, 1, getBandOpacity());
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

    public boolean isShowEdges() {
        return showEdges.get();
    }

    public BooleanProperty showEdgesProperty() {
        return showEdges;
    }

    public void setShowEdges(boolean showEdges) {
        this.showEdges.set(showEdges);
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

    public boolean isShowBackFaces() {
        return showBackFaces.get();
    }

    public BooleanProperty showBackFacesProperty() {
        return showBackFaces;
    }

    public void setShowBackFaces(boolean showBackFaces) {
        this.showBackFaces.set(showBackFaces);
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

    public boolean isShowVertices() {
        return showVertices.get();
    }

    public BooleanProperty showVerticesProperty() {
        return showVertices;
    }

    public void setShowVertices(boolean showVertices) {
        this.showVertices.set(showVertices);
    }

    public boolean isShowBackVertices() {
        return showBackVertices.get();
    }

    public BooleanProperty showBackVerticesProperty() {
        return showBackVertices;
    }

    public void setShowBackVertices(boolean showBackVertices) {
        this.showBackVertices.set(showBackVertices);
    }

    public boolean isSphericalUsePointLight() {
        return sphericalUsePointLight.get();
    }

    public BooleanProperty sphericalUsePointLightProperty() {
        return sphericalUsePointLight;
    }

    public void setSphericalUsePointLight(boolean sphericalUsePointLight) {
        this.sphericalUsePointLight.set(sphericalUsePointLight);
    }

    public boolean isBendAnEdge() {
        return bendAnEdge.get();
    }

    public BooleanProperty bendAnEdgeProperty() {
        return bendAnEdge;
    }

    public void setBendAnEdge(boolean bendAnEdge) {
        this.bendAnEdge.set(bendAnEdge);
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

    public boolean getShowDecorations() {
        return showDecorations.get();
    }

    public BooleanProperty showDecorationsProperty() {
        return showDecorations;
    }

    public void setShowDecorations(boolean showDecorations) {
        this.showDecorations.set(showDecorations);
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public double getTileOpacity() {
        return tileOpacity.get();
    }

    public DoubleProperty tileOpacityProperty() {
        return tileOpacity;
    }

    public void setTileOpacity(double tileOpacity) {
        this.tileOpacity.set(tileOpacity);
    }

    public double getBandOpacity() {
        return bandOpacity.get();
    }

    public DoubleProperty bandOpacityProperty() {
        return bandOpacity;
    }

    public void setBandOpacity(double bandOpacity) {
        this.bandOpacity.set(bandOpacity);
    }

    public CameraSettings.HyperbolicModel getHyperbolicModel() {
        return hyperbolicModel.get();
    }

    public ObjectProperty<CameraSettings.HyperbolicModel> hyperbolicModelProperty() {
        return hyperbolicModel;
    }

    public void setHyperbolicModel(CameraSettings.HyperbolicModel hyperbolicModel) {
        this.hyperbolicModel.set(hyperbolicModel);
    }

    public int getHyperbolicLimit() {
        return hyperbolicLimit.get();
    }

    public IntegerProperty hyperbolicLimitProperty() {
        return hyperbolicLimit;
    }

    public void setHyperbolicLimit(int hyperbolicLimit) {
        this.hyperbolicLimit.set(hyperbolicLimit);
    }

    public Group getDecorations() {
        return decorations;
    }

    public boolean isHideFirstTile() {
        return hideFirstTile.get();
    }

    public BooleanProperty hideFirstTileProperty() {
        return hideFirstTile;
    }

    public void setHideFirstTile(boolean hideFirstTile) {
        this.hideFirstTile.set(hideFirstTile);
    }

    public String getStyleEdits() {
        return styleEdits.get();
    }

    public StringProperty styleEditsProperty() {
        return styleEdits;
    }

    public void setStyleEdits(String styleEdits) {
        this.styleEdits.set(styleEdits);
    }
}
