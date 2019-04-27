/*
 * ColorSchemeManager.java Copyright (C) 2019. Daniel H. Huson
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

package tegula.color;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import jloda.fx.util.ProgramPropertiesFX;
import jloda.util.Basic;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ColorSchemeManager {
    private final Map<String, ObservableList<Color>> name2ColorSchemes = new TreeMap<>();
    private final StringProperty lastColorScheme = new SimpleStringProperty("Alhambra");

    public static final String[] BuiltInColorTables = {
            "Alhambra;6;0X4d66cc;0Xb3e6e6;0Xcc9933;0X669966;0X666666;0X994d00;" +
                    "Caspian8;8;0Xf64d1b;0X8633bc;0X41a744;0X747474;0X2746bc;0Xff9301;0Xc03150;0X2198bc;" +
                    "Fews8;8;0X5da6dc;0Xfba53a;0X60be68;0Xf27db0;0Xb39230;0Xb376b2;0Xdfd040;0Xf15954;" +
                    "Pairs12;12;0X267ab2;0Xa8cfe3;0X399f34;0Xb4df8e;0Xe11f27;0Xfa9b9b;0Xfe7f23;0Xfcbf75;0X6a4199;0Xcab3d6;0Xb05a2f;0Xffff9f;" +
                    "Pale12;12;0Xdbdada;0Xf27e75;0Xba7bbd;0Xceedc5;0Xfbf074;0Xf8cbe5;0Xf9b666;0Xfdffb6;0X86b0d2;0X95d6c8;0Xb3e46c;0Xbfb8da;" +
                    "Rainbow13;13;0Xed1582;0Xf73e43;0Xee8236;0Xe5ae3d;0Xe5da45;0Xa1e443;0X22da27;0X21d18e;0X21c8c7;0X1ba2fc;0X2346fb;0X811fd9;0X9f1cc5;" +
                    "Retro29;29;0Xf4d564;0X97141d;0Xe9af6b;0X82ae92;0X356c7c;0X5c8c83;0X3a2b27;0Xe28b90;0X242666;0Xc2a690;0Xb80614;0X35644f;0Xe3a380;0Xb9a253;0X72a283;0X73605b;0X94a0ad;0Xf7a09d;0Xe5c09e;0X4a4037;0Xcec07c;0X6c80bb;0X7fa0a4;0Xb9805b;0Xd5c03f;0Xdd802e;0X8b807f;0Xc42030;0Xc2603d;" +
                    "Sea9;9;0Xffffdb;0Xedfbb4;0Xc9ecb6;0X88cfbc;0X56b7c4;0X3c90bf;0X345aa7;0X2f2b93;0X121858;"
    };

    private static ColorSchemeManager instance;

    public static ColorSchemeManager getInstance() {
        if (instance == null)
            instance = new ColorSchemeManager();
        return instance;
    }

    private ColorSchemeManager() {
        parseTables(ProgramPropertiesFX.get("ColorSchemes", BuiltInColorTables));
    }

    /**
     * parse the definition of tables
     *
     * @param tables
     */
    public void parseTables(String... tables) {
        int alpha = Math.max(0, Math.min(255, ProgramPropertiesFX.get("ColorAlpha", 255)));

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
        ProgramPropertiesFX.put("ColorSchemes", writeTables());
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
