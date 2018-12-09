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

package tiler.color;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ColorSchemeManager {
    private final Map<String, ObservableList<Color>> name2ColorSchemes = new TreeMap<>();
    private final StringProperty lastColorScheme = new SimpleStringProperty("Fews8");

    public static final String[] BuiltInColorTables = {
            "Fews8;8;0x5da6dc;0xfba53a;0x60be68;0xf27db0;0xb39230;0xb376b2;0xdfd040;0xf15954;",
            "Caspian8;8;0xf64d1b;0x8633bc;0x41a744;0x747474;0x2746bc;0xff9301;0xc03150;0x2198bc;",
            "Sea9;9;0xffffdb;0xedfbb4;0xc9ecb6;0x88cfbc;0x56b7c4;0x3c90bf;0x345aa7;0x2f2b93;0x121858;",
            "Pale12;12;0xdbdada;0xf27e75;0xba7bbd;0xceedc5;0xfbf074;0xf8cbe5;0xf9b666;0xfdffb6;0x86b0d2;0x95d6c8;0xb3e46c;0xbfb8da;",
            "Rainbow13;13;0xed1582;0xf73e43;0xee8236;0xe5ae3d;0xe5da45;0xa1e443;0x22da27;0x21d18e;0x21c8c7;0x1ba2fc;0x2346fb;0x811fd9;0x9f1cc5;",
            "Retro29;29;0xf4d564;0x97141d;0xe9af6b;0x82ae92;0x356c7c;0x5c8c83;0x3a2b27;0xe28b90;0x242666;0xc2a690;0xb80614;0x35644f;0xe3a380;0xb9a253;" +
                    "0x72a283;0x73605b;0x94a0ad;0xf7a09d;0xe5c09e;0x4a4037;0xcec07c;0x6c80bb;0x7fa0a4;0xb9805b;0xd5c03f;0xdd802e;0x8b807f;0xc42030;0xc2603d;",
            "Pairs12;12;0x267ab2;0xa8cfe3;0x399f34;0xb4df8e;0xe11f27;0xfa9b9b;0xfe7f23;0xfcbf75;0x6a4199;0xcab3d6;0xb05a2f;0xffff9f;"
    };

    private static ColorSchemeManager instance;

    public static ColorSchemeManager getInstance() {
        if (instance == null)
            instance = new ColorSchemeManager();
        return instance;
    }

    private ColorSchemeManager() {

        parseTables(ProgramProperties.get("ColorSchemes", BuiltInColorTables));
    }

    /**
     * parse the definition of tables
     *
     * @param tables
     */
    public void parseTables(String... tables) {
        int alpha = Math.max(0, Math.min(255, ProgramProperties.get("ColorAlpha", 255)));

        for (String table : tables) {
            final String[] tokens = Basic.split(table, ';');
            if (tokens.length > 0) {
                int i = 0;
                while (i < tokens.length) {
                    String name = tokens[i++];
                    int numberOfColors = Integer.valueOf(tokens[i++]);
                    final ObservableList<Color> colors = FXCollections.observableArrayList();
                    for (int k = 0; k < numberOfColors; k++) {
                        Color color = Color.web(tokens[i++]);
                        if (alpha < 255)
                            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                        colors.add(color);
                    }
                    if (colors.size() > 0 && !name2ColorSchemes.containsKey(name)) {
                        name2ColorSchemes.put(name, colors);
                    }
                }
            }
        }
    }

    public String writeTables() {
        final StringBuilder buf = new StringBuilder();
        for (String name : name2ColorSchemes.keySet()) {
            buf.append(String.format("%s;%d;", name.replaceAll(";", "_"), name2ColorSchemes.get(name).size()));
            for (Color color : name2ColorSchemes.get(name)) {
                buf.append(String.format("0X%02x%02x%02x;", (int) (255 * color.getRed()),
                        (int) (255 * color.getGreen()),
                        (int) (255 * color.getBlue())));

            }
        }
        return buf.toString();
    }

    public ObservableList<Color> getColorScheme(String name) {
        lastColorScheme.set(name);
        return name2ColorSchemes.get(name);
    }

    public void setColorScheme(String name, ObservableList<Color> colors) {
        name2ColorSchemes.put(name, colors);
        ProgramProperties.get("ColorSchemes", writeTables());
    }

    public String getLastColorScheme() {
        return lastColorScheme.get();
    }

    public ReadOnlyStringProperty lastColorSchemeProperty() {
        return lastColorScheme;
    }

    public Collection<String> getNames() {
        return name2ColorSchemes.keySet();
    }
}
