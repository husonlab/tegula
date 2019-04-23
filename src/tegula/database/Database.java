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

package tegula.database;

import jloda.util.Basic;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.OrbifoldGroupName;

import java.io.StringReader;
import java.sql.*;

/**
 * Manages a database of Delaney symbols and tilings
 * Daniel Huson, 1.2019
 */
public class Database {

    /**
     * Connect to a sample database
     *
     * @param url the database file name
     */
    public static void createNewDatabase(String url) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                final DatabaseMetaData meta = conn.getMetaData();
                System.err.println("The driver name is " + meta.getDriverName());
                System.err.println("A new database has been created.");
            }
        }
    }

    /**
     * Create a new table in the test database
     */
    public static void createNewTable(String url) throws SQLException {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS tilings (\n"
                + "	DelaneySymbol TEXT NOT NULL PRIMARY KEY,\n"
                + " Name STRING,\n"
                + " Geometry STRING NOT NULL,\n"
                + " Curvature  STRING NOT NULL,\n"
                + "	SymmetryGroup TEXT NOT NULL,\n"
                + "	NumberOfTiles integers NOT NULL,\n"
                + "	NumberOfVertices integers NOT NULL,\n"
                + " Classification TEXT NOT NULL,\n"
                + "	Icon TEXT\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        }
    }

    /**
     * insert a tiling into the database
     *
     * @param ds
     */
    public static void insertTiling(String url, DSymbol ds, String name) throws SQLException {
        final String sql = String.format("INSERT into TILINGS\nVALUES\n(\n'%s', '%s', '%s', '%s', '%s', %d, %d, '%s', '%s')\n",
                ds.toString(), name, ds.computeGeometry(), ds.computeCurvature(), OrbifoldGroupName.getGroupName(ds), ds.countOrbits(0, 1), ds.countOrbits(1, 2), "test", null);

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        }
    }

    /**
     * list all tilings found in the database
     *
     * @param url
     * @throws SQLException
     */
    public static void dumpTilings(String url) throws SQLException {
        final String sql = "SELECT * FROM tilings;\n";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString("DelaneySymbol") + "\t" +
                        rs.getString("Name") + "\t" +
                        rs.getString("Geometry") + "\t" +
                        rs.getString("Curvature") + "\t" +
                        rs.getString("SymmetryGroup") + "\t" +
                        rs.getInt("NumberOfTiles") + "\t" +
                        rs.getInt("NumberOfVertices") + "\t" +
                        rs.getString("Classification")
                );
            }
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final String url = "jdbc:sqlite:tilings.db";
        try {
            createNewDatabase(url);
            createNewTable(url);
            for (int r = 5; r <= 7; r++) {
                DSymbol ds = new DSymbol();
                ds.read(new StringReader("<23.1:20:2 4 6 8 10 12 14 16 18 20,2 10 5 9 8 20 13 15 17 19,11 12 13 14 15 16 17 18 19 20:3 3 " + r + " 5,4 4 4>"));
                insertTiling(url, ds, "test" + r);
            }

            dumpTilings(url);
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }
}
