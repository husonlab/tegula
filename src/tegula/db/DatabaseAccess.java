/*
 * DatabaseAccess.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.db;

import jloda.util.Basic;
import org.sqlite.SQLiteConfig;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * provides access to database of tilings
 * Daniel Huson, 10.2019
 */
public class DatabaseAccess implements Closeable {
    private final Connection connection;

    /**
     * constructor
     *
     * @param dbFile
     * @throws IOException
     * @throws SQLException
     */
    public DatabaseAccess(String dbFile) throws IOException, SQLException {
        if (!Basic.fileExistsAndIsNonEmpty(dbFile))
            throw new IOException("File not found or unreadable: " + dbFile);

        final SQLiteConfig config = new SQLiteConfig();

        connection = config.createConnection("jdbc:sqlite:" + dbFile);

        System.err.printf("Opened: " + dbFile + ": %,d%n", computeDBSize());
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Basic.caught(e);
            }
        }
    }

    /**
     * gets all D-symbols that match the select expression
     *
     * @param selectExpression
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public ArrayList<String> getDSymbols(String selectExpression) throws IOException, SQLException {
        selectExpression = selectExpression.trim();
        if (selectExpression.endsWith(";"))
            selectExpression = selectExpression.substring(0, selectExpression.length() - 1);
        if (selectExpression.contains(";"))
            throw new IOException("Illegal ';' in select expression");
        final String query;
        if (selectExpression.toLowerCase().startsWith("select"))
            query = selectExpression + ";";
        else
            query = "select symbol from tilings where " + selectExpression + ";";

        return executeQueryString(query, 1);

    }

    /**
     * gets all D-symbols that match the select expression
     *
     * @param selectExpression
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public int countDSymbols(String selectExpression) throws IOException, SQLException {
        selectExpression = selectExpression.trim();
        if (selectExpression.endsWith(";"))
            selectExpression = selectExpression.substring(0, selectExpression.length() - 1);
        if (selectExpression.contains(";"))
            throw new IOException("Illegal ';' in select expression");
        final String query;
        if (selectExpression.toLowerCase().startsWith("select"))
            query = selectExpression + ";";
        else if (selectExpression.length() == 0)
            query = "select count(*) from tilings;";
        else
            query = "select count(*) from tilings where " + selectExpression + ";";

        ArrayList<Integer> result = executeQueryInt(query, 1);
        return result.size() > 0 ? result.get(0) : 0;
    }

    /**
     * computes the size of the mappings database by querying the mappings table with count(*)
     *
     * @return size of the database or 0 if an error occurred
     */
    public long computeDBSize() throws SQLException {
        return executeQueryLong("SELECT count(*) FROM tilings;", 1).get(0);
    }

    /**
     * computes the size of the mappings database by querying the mappings table with count(*)
     *
     * @return size of the database or 0 if an error occurred
     */
    public long getDBDSize() throws SQLException {
        return Basic.parseLong(executeQueryString("select info_String from info where id='size';", 1).get(0));
    }

    /**
     * gets the database version
     *
     * @return version
     * @throws SQLException
     */
    public float getVersion() throws SQLException {
        return Basic.parseFloat(executeQueryString("select info_String from info where id='version';", 1).get(0));
    }


    /**
     * generic method for executing queries with results of type int/Integer
     *
     * @param query the SQL query
     * @return ArrayList containing all query results of the specified type
     * @throws SQLException if something went wrong with the database
     */
    private ArrayList<Integer> executeQueryInt(String query, int index) throws SQLException {
        final ResultSet rs = connection.createStatement().executeQuery(query);
        final ArrayList<Integer> resultlist = new ArrayList<>();
        while (rs.next()) {
            resultlist.add(rs.getInt(index));
        }
        return resultlist;
    }

    /**
     * generic method for executing queries with results of type int/Integer
     *
     * @param query the SQL query
     * @return ArrayList containing all query results of the specified type
     * @throws SQLException if something went wrong with the database
     */
    private ArrayList<Long> executeQueryLong(String query, int index) throws SQLException {
        final ResultSet rs = connection.createStatement().executeQuery(query);
        final ArrayList<Long> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rs.getLong(index));
        }
        return list;
    }


    /**
     * generic method for executing queries with results of type String
     *
     * @param query   the SQL query
     * @param indices the index of the columns of interest
     * @return ArrayList containing all query results of the specified type
     * @throws SQLException if something went wrong with the database
     */
    private ArrayList<String> executeQueryString(String query, int... indices) throws SQLException {
        //System.err.println("Query: " + query);
        final ResultSet rs = connection.createStatement().executeQuery(query);
        final ArrayList<String> result = new ArrayList<>();
        while (rs.next()) {
            for (int i : indices)
                result.add(rs.getString(i));
        }
        return result;
    }
}
