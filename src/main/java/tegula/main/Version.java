/*
 * Version.java Copyright (C) 2023 Daniel H. Huson
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

package tegula.main;

/**
 * Maintains the version string
 * Daniel Huson, 12.2018
 */
public class Version {
    static public final String NAME = "Tegula";
    public static final String VERSION = resolveVersion();
    public static final String SHORT_DESCRIPTION = NAME + " (version " + VERSION + ") - License GPL v3";

    static public final String HOME_URL = "https://github.com/husonlab/tegula";

    static public final String WEBSITE_URL = "https://husonlab.github.io/tegula";

    public static String resolveVersion() {
        var pkg = Version.class.getPackage();
        var v = (pkg != null) ? pkg.getImplementationVersion() : null;
        return (v != null && !v.isBlank()) ? v : "dev";
    }
}
