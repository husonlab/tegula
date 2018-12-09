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
package jloda.util;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * gets version info
 * Daniel Huson, 2014
 */
public class Version {
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
