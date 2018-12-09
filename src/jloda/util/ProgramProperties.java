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

/**
 * ProgramProperties.java
 * Copyright (C) 2018 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;

/**
 * track program properties
 * <p>
 * Daniel Huson,  2004, 2018
 */
public class ProgramProperties {
    static public final java.util.Properties props = new java.util.Properties();

    static private final ObservableList<Image> programIcons = FXCollections.observableArrayList();

    static private Font defaultFont = Font.font("Arial", 12);

    static private String defaultFileName = null;
    static private String programName = "";
    static private String programVersion = "";
    private static final boolean macOS = (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().startsWith("mac"));
    private static boolean useGUI;

    /**
     * load properties from default file
     */
    public static void load() {
        try (FileInputStream fis = new FileInputStream(getDefaultFileName())) {
            props.load(fis);
            //System.err.println("Loaded properties from: " + getDefaultFileName());
        } catch (Exception ex) {
            //Basic.caught(ex);
        }
    }

    /**
     * load properties from specified file
     */
    public static void load(String fileName) {
        setPropertiesFileName(fileName);
        load();
    }

    /**
     * save properties to default file
     */
    public static void store() {
        try (OutputStream fos = new FileOutputStream(getDefaultFileName())) {
            props.store(fos, programName);
            //System.err.println("Stored properties to: " + getDefaultFileName());
        } catch (Exception ex) {
            //Basic.caught(ex);
        }
    }

    /**
     * save properties to specified file
     */
    public static void store(String fileName) {
        setPropertiesFileName(fileName);
        store();
    }

    /**
     * default font for graphics
     *
     * @return default font
     */
    public static Font getDefaultFont() {
        return defaultFont;
    }

    public static void setDefaultFont(Font defaultFont) {
        ProgramProperties.defaultFont = defaultFont;
    }

    /**
     * gets a int property
     *
     * @return set property or default
     */
    public static int get(Object name, int def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else
            return Integer.parseInt(value);
    }

    /**
     * gets a int[] property
     *
     * @return set property or default
     */
    public static int[] get(Object name, int[] def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            try {
                final String[] tokens = value.split(value.contains(";") ? ";" : "\\s+");
                final int[] result = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++)
                    result[i] = Integer.parseInt(tokens[i]);
                return result;
            } catch (Exception ex) {
                return def;
            }
        }
    }

    /**
     * gets a color property
     *
     * @return set property or default
     */
    public static Color get(Object name, Color def) {
        String value = (String) props.get(name);
        if (value == null || value.equalsIgnoreCase("null"))
            return def;
        else
            return Color.valueOf(value);
    }


    /**
     * gets a double property
     *
     * @return set property or default
     */
    public static double get(Object name, double def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else
            return Double.parseDouble(value);
    }

    /**
     * gets a boolean property
     *
     * @return set property or default
     */
    public static boolean get(Object name, boolean def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else
            return Boolean.valueOf(value);
    }


    /**
     * gets a string property
     *
     * @return set property or default
     */
    public static String get(String name, String def) {
        return props.getProperty(name, def);
    }

    /**
     * gets a list of string pairs
     *
     * @return list of string pairs
     */
    public static Collection<Pair<String, String>> get(String name, Collection<Pair<String, String>> def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            final Collection<Pair<String, String>> list = new LinkedList<>();
            String[] tokens = value.split("%%%");
            for (int i = 0; i < tokens.length - 1; i += 2)
                list.add(new Pair<>(tokens[i].trim(), tokens[i + 1].trim()));
            return list;
        }
    }

    /**
     * gets a list of strings
     *
     * @return list of string pairs
     */
    public static String[] get(String name, String[] def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            return value.split("%%%");
        }
    }

    /**
     * get the default properties file name
     *
     * @return file name
     */
    public static String getDefaultFileName() {
        return defaultFileName;
    }

    /**
     * set the default properties file name
     */
    public static void setPropertiesFileName(String defaultFileName) {
        ProgramProperties.defaultFileName = defaultFileName;
    }

    public static File getFile(String key) {
        String fileName = props.getProperty(key);
        if (fileName != null)
            return new File(fileName);
        return null;
    }

    /**
     * remove a property
     */
    public static void remove(String key) {
        props.remove(key);
    }

    /**
     * put a property
     */
    public static void put(String key, int value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     */
    public static void put(String key, int[] value) {
        StringBuilder buf = new StringBuilder();
        for (int aValue : value) buf.append(aValue).append(";");
        props.setProperty(key, "" + buf.toString());
    }

    /**
     * put a property
     */
    public static void put(String key, double value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     */
    public static void put(String key, boolean value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     */
    public static void put(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * put a file property
     */
    public static void put(String key, File value) {
        props.setProperty(key, value.getAbsolutePath());
    }


    /**
     * put a property
     */
    public static void put(String key, Color value) {
        if (value == null)
            props.setProperty(key, "null");
        else
            props.setProperty(key, "" + value.toString());
    }


    /**
     * put a property
     */
    public static void put(String key, Collection<Pair<String, String>> value) {
        StringBuilder buf = new StringBuilder();
        for (Pair<String, String> pair : value) {
            buf.append(pair.getFirst()).append("%%%");
            buf.append(pair.getSecond()).append("%%%");
        }
        props.setProperty(key, buf.toString());
    }

    /**
     * put a property
     */
    public static void put(String key, String[] value) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (String s : value) {
            if (first)
                first = false;
            else
                buf.append("%%%");
            buf.append(s);
        }
        props.setProperty(key, buf.toString());
    }


    /**
     * get a property
     *
     * @return property for key
     */
    public static String get(String key) {
        return props.getProperty(key);
    }

    /**
     * sets the name of the program generating these properties
     */
    public static void setProgramName(String programName) {
        ProgramProperties.programName = programName;
    }

    /**
     * gets the program name
     *
     * @return name
     */
    public static String getProgramName() {
        return programName;
    }

    /**
     * sets the program version string, if not already set...
     */
    public static void setProgramVersion(String version) {
        if (programVersion == null || programVersion.length() == 0)
            ProgramProperties.programVersion = version;
    }

    public static void resetProgramVersion(String version) {
        ProgramProperties.programVersion = version;
    }

    /**
     * gets the program versions string
     *
     * @return version
     */
    public static String getProgramVersion() {
        return programVersion;
    }

    /**
     * are we running on a mac?
     *
     * @return true, if os is mac
     */
    public static boolean isMacOS() {
        return macOS;
    }


    /**
     * returns the given text, if the key has been set, otherwise returns ""
     *
     * @return text or ""
     */
    public static String getIfEnabled(String key, String text) {
        if (get(key, false))
            return text;
        else
            return "";
    }

    public static ObservableList<Image> getProgramIcons() {
        return programIcons;
    }

    public static void setUseGUI(boolean useGUI) {
        ProgramProperties.useGUI = useGUI;
    }

    public static boolean isUseGUI() {
        return useGUI;
    }
}
