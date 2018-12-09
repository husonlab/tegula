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
 * ResourceManager.java
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


import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * get icons and  cursors from resources
 * Daniel Huson and others, 2003, 2018
 */
public class ResourceManager {
    public static final String iconPackagePath = "resources.icons";
    public static final String imagePackagePath = "resources.images";
    public static final String filePackagePath = "resources.files";
    public static final String cssPackagePath = "resources.css";

    private static final HashMap<String, Image> iconMap = new HashMap<>();
    private static final HashMap<String, Image> imageMap = new HashMap<>();
    private static final HashMap<String, File> fileMap = new HashMap<>();

    private static boolean warnMissing;

    /**
     * gets the named icon
     */
    public static Image getIcon(String name) {
        if (!iconMap.containsKey(name)) {
            Image iconImage = getImageResource(iconPackagePath, name);
            if (iconImage != null) {
                iconMap.put(name, iconImage);
            } else {
                if (Basic.getDebugMode() && warnMissing)
                    System.err.println("ICON NOT FOUND: " + name + ", path: " + iconPackagePath);
                Image image = getImageResource(iconPackagePath, "sun/toolbarButtonGraphics/general/Help16.gif");
                if (image != null)
                    return image;
                else
                    return null;
            }
        }
        return iconMap.get(name);
    }

    /**
     * gets the named image
     */
    public static Image getImage(String name) {
        if (!imageMap.containsKey(name)) {
            Image image = getImageResource(imagePackagePath, name);
            if (image != null) {
                imageMap.put(name, image);
            } else {
                if (Basic.getDebugMode() && warnMissing)
                    System.err.println("IMAGE NOT FOUND: " + name + ", path: " + imagePackagePath);
                image = getImageResource(imagePackagePath, "sun/toolbarButtonGraphics/general/Help16.gif");
                if (image != null)
                    return image;
                else
                    return null;
            }
        }
        return imageMap.get(name);
    }

    /**
     * Gets the named file
     */
    public static File getFile(String name) {
        if (!fileMap.containsKey(name)) {
            File file = getFileResource(filePackagePath, name);
            fileMap.put(name, file);
        }
        return fileMap.get(name);
    }

    /**
     * Returns the file with name specified by the parameter, or <code>null</code> if there is none.
     */
    public static URL getCssURL(String name) {
        return getFileURL(cssPackagePath, name);
    }

    /**
     * Returns the path with name specified by the parameter, or just the name, else
     */
    public static String getFileName(String name) {
        if (!fileMap.containsKey(name)) {
            File data = getFileResource(filePackagePath, name);
            fileMap.put(name, data);
        }
        File file = fileMap.get(name);
        if (file != null)
            return file.getPath().replaceAll("%20", " ");
        else
            return "File " + name + ": Path not found";
    }

    /**
     * Gets stream from package resources.files, else attempts to open
     * stream from named file in file system
     *
     * @param fileName the name of the file
     */
    public static InputStream getFileAsStream(String fileName) {
        if (fileName == null)
            return null;
        fileName = fileName.trim();
        if (fileName.length() == 0)
            return null;
        return getFileAsStream(filePackagePath, fileName);
    }

    /**
     * Returns file resource as stream, unless the string contains a slash, in which case returns Stream from the file system
     *
     * @param filePackage the package containing file
     * @param fileName    the name of the file
     */
    public static InputStream getFileAsStream(String filePackage, String fileName) {
        if (fileName.contains("/") || fileName.contains("\\")) {
            final File file = new File(fileName);
            try {
                return Basic.getInputStreamPossiblyZIPorGZIP(file.getPath());
            } catch (IOException e) {
                if (!fileName.endsWith(".info")) // don't complain about missing info files
                    System.err.println(e.getMessage());
                return null;
            }
        } else
            return getFileResourceAsStream(filePackage, fileName);

    }


    /**
     * Returns an Image (icon) with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the icon file
     */
    public static Image getImageResource(String packageName, String fileName) {
        String resname = "/" + packageName.replace('.', '/') + "/" + fileName;
        resname = resname.replaceAll(" ", "\\ ");
        try (InputStream is = ResourceManager.class.getResourceAsStream(resname)) {
            byte[] buffer = new byte[0];
            byte[] tmpbuf = new byte[1024];
            while (true) {
                int len = is.read(tmpbuf);
                if (len <= 0)
                    break;
                byte[] newbuf = new byte[buffer.length + len];
                System.arraycopy(buffer, 0, newbuf, 0, buffer.length);
                System.arraycopy(tmpbuf, 0, newbuf, buffer.length, len);
                buffer = newbuf;
            }
            return new Image(new ByteArrayInputStream(buffer));
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Returns File with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the file
     */
    public static File getFileResource(String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            final URL url = ResourceManager.class.getResource(resourceName);
            return new File(url.getFile());
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Returns URL with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the file
     */
    public static URL getFileURL(String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            return ResourceManager.class.getResource(resourceName);
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Returns file resource as stream
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the file
     */
    public static InputStream getFileResourceAsStream(String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replace(" ", "\\ ");
            return ResourceManager.class.getResourceAsStream(resourceName);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * does resource file exist?
     *
     * @param fileName
     * @return true if file exists
     */
    public static boolean resourceFileExists(String fileName) {
        try (InputStream ins = ResourceManager.class.getResourceAsStream("/resources/files/" + fileName)) {
            return (ins != null);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * gets an image from the named package
     *
     * @param packageName
     * @param fileName
     * @return image
     * @throws IOException
     */
    public static Image getImage(String packageName, String fileName) throws IOException {
        return getImageResource(packageName, fileName);
    }

    /**
     * does the named file exist as a resource or file?
     *
     * @param name
     * @return true if named file exists as a resource or file
     */
    public static boolean fileExists(String name) {
        if (name == null || name.length() == 0)
            return false;

        try (InputStream ins = getFileAsStream(name)) {
            return ins != null;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * does the named file exist as a resource?
     *
     * @param packageName
     * @param name
     * @return true if named file exists as a resource
     */
    public static boolean fileResourceExists(String packageName, String name) {
        if (name == null || name.length() == 0)
            return false;

        boolean existsAsStream = false;
        try (InputStream ins = getFileResourceAsStream(packageName, name)) {
            existsAsStream = (ins != null);
        } catch (Exception ex) {
        }
        return existsAsStream || (new File(name)).canRead();
    }

    public static void setWarnMissing(boolean warnMissing) {
        ResourceManager.warnMissing = warnMissing;
    }

    public static boolean isWarnMissing() {
        return warnMissing;
    }

    /**
     * gets the compile time version of the given class
     *
     * @param clazz
     * @param name
     * @return compile time version
     */
    public static String getVersion(final Class clazz, final String name) {
        String version;
        try {
            final ClassLoader classLoader = clazz.getClassLoader();
            String threadContexteClass = clazz.getName().replace('.', '/');
            URL url = classLoader.getResource(threadContexteClass + ".class");
            if (url == null) {
                version = name + " $ (no manifest) $";
            } else {
                final String path = url.getPath();
                final String jarExt = ".jar";
                int index = path.indexOf(jarExt);
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                if (index != -1) {
                    final String jarPath = path.substring(0, index + jarExt.length());
                    final File file = new File(jarPath);
                    final String jarVersion = file.getName();
                    final JarFile jarFile = new JarFile(new File(new URI(jarPath)));
                    final JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
                    version = name + " $ " + jarVersion.substring(0, jarVersion.length() - jarExt.length()) + " $ " + sdf.format(new Date(entry.getTime()));
                    jarFile.close();
                } else {
                    final File file = new File(path);
                    version = name + " $ " + sdf.format(new Date(file.lastModified()));
                }
            }
        } catch (Exception e) {
            //Basic.caught(e);
            version = name + " $ " + e.toString();
        }
        return version;
    }
}


