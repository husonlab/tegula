/*
 * CatalogNumbering.java Copyright (C) 2026 Daniel H. Huson
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

package tegula.db;

import jloda.fx.util.ArgsOptions;
import jloda.fx.util.ProgramProperties;
import jloda.util.Basic;
import jloda.util.FileUtils;
import org.sqlite.SQLiteConfig;
import tegula.core.dsymbols.DSymbol;
import tegula.core.dsymbols.DSymbolAlgorithms;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.IntStream;

/**
 * re-derives a stable catalog numbering for a database of tilings
 * <p>
 * The id column of a Tegula tilings database records the order in which the tilings happened to be enumerated and
 * so differs between databases that contain exactly the same tilings. This program computes, for each tiling, the
 * canonical form of its Delaney symbol and the canonical key derived from it, and then numbers the tilings by
 * increasing size and, within one size, by increasing canonical form. That numbering depends only on the set of
 * tilings, so anybody can recompute it, and extending the catalog to larger symbols appends to it rather than
 * renumbering it.
 * <p>
 * Daniel Huson, 8.2026
 */
public class CatalogNumbering {
    private static final int BATCH_SIZE = 1 << 16;

    /**
     * one tiling of the catalog, held as the protocol of its canonical form
     */
    public record Entry(long sourceId, int[] protocol) {
        public int size() {
            return protocol.length / 5;
        }
    }

    public static void main(String[] args) {
        try {
            ProgramProperties.setProgramName("CatalogNumbering");
            (new CatalogNumbering()).run(args);
            System.exit(0);
        } catch (Exception ex) {
            if (!"Help".equals(ex.getMessage()))
                Basic.caught(ex);
            System.exit(1);
        }
    }

    public void run(String[] args) throws Exception {
        final ArgsOptions options = new ArgsOptions(args, this,
                "Computes a stable catalog numbering for a database of tilings"
                + " (allow about 300 bytes of heap per tiling)");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input and output:");
        final String inputFile = options.getOptionMandatory("-i", "input", "Input tilings database (.tdb)", "");
        final String outputFile = options.getOption("-o", "output", "Output catalog file (.tsv, .gz and stdout ok)", "stdout");

        options.comment("Options:");
        final int keyBits = options.getOption("-k", "keyBits", "Number of hash bits in a canonical key, a multiple of 5",
                DSymbolAlgorithms.DEFAULT_KEY_BITS);
        final int maxSize = options.getOption("-m", "maxSize", "Only consider symbols up to this size (0: all)", 0);
        final boolean updateDatabase = options.getOption("-u", "updateDatabase",
                "Add canonical_key and catalog_number columns to the input database, modifying it in place", false);
        options.done();

        if (!FileUtils.fileExistsAndIsNonEmpty(inputFile))
            throw new IOException("File not found or unreadable: " + inputFile);

        final ArrayList<Entry> entries;
        try (Connection connection = new SQLiteConfig().createConnection("jdbc:sqlite:" + inputFile)) {
            entries = readAndCanonicalize(connection, maxSize);
        }
        System.err.printf("Tilings read:      %,13d%n", entries.size());

        entries.sort(Comparator.comparingInt(Entry::size).thenComparing(Entry::protocol, Arrays::compare));
        removeDuplicates(entries);

        final String[] keys = new String[entries.size()];
        IntStream.range(0, keys.length).parallel()
                .forEach(i -> keys[i] = DSymbolAlgorithms.keyForProtocol(entries.get(i).protocol(), keyBits));
        final int collisions = reportCollisions(entries, keys, keyBits);

        try (Writer w = FileUtils.getOutputWriterPossiblyZIPorGZIP(outputFile)) {
            write(w, entries, keys, keyBits, inputFile);
        }
        if (!outputFile.toLowerCase().startsWith("std"))
            System.err.println("Catalog written to: " + outputFile);

        if (updateDatabase) {
            if (collisions > 0)
                throw new IOException("Refusing to update the database: %d key collisions, use a larger -k".formatted(collisions));
            try (Connection connection = new SQLiteConfig().createConnection("jdbc:sqlite:" + inputFile)) {
                updateDatabase(connection, entries, keys);
            }
        }
    }

    /**
     * reads all symbols and computes their canonical forms
     *
     * @return entries, in database order
     */
    private ArrayList<Entry> readAndCanonicalize(Connection connection, int maxSize) throws SQLException {
        final ArrayList<Entry> result = new ArrayList<>(1 << 20);
        final ArrayList<Long> ids = new ArrayList<>(BATCH_SIZE);
        final ArrayList<String> symbols = new ArrayList<>(BATCH_SIZE);

        final String query = (maxSize > 0 ? "select id, symbol from tilings where complexity <= %d;".formatted(maxSize)
                : "select id, symbol from tilings;");

        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
                symbols.add(rs.getString(2));
                if (ids.size() == BATCH_SIZE) {
                    canonicalizeBatch(ids, symbols, maxSize, result);
                    System.err.printf("\rCanonicalized:     %,13d", result.size());
                }
            }
            canonicalizeBatch(ids, symbols, maxSize, result);
            System.err.printf("\rCanonicalized:     %,13d%n", result.size());
        }
        return result;
    }

    /**
     * canonicalizes one batch of symbols in parallel and appends the entries to the result
     */
    private void canonicalizeBatch(ArrayList<Long> ids, ArrayList<String> symbols, int maxSize, List<Entry> result) {
        if (ids.isEmpty())
            return;
        final Entry[] array = new Entry[ids.size()];
        IntStream.range(0, array.length).parallel().forEach(i -> {
            final DSymbol ds = new DSymbol(symbols.get(i));
            if (maxSize == 0 || ds.size() <= maxSize)
                array[i] = new Entry(ids.get(i), DSymbolAlgorithms.canonicalProtocol(ds));
        });
        for (Entry entry : array) {
            if (entry != null)
                result.add(entry);
        }
        ids.clear();
        symbols.clear();
    }

    /**
     * removes all tilings that occur more than once, that is, whose Delaney symbols are isomorphic,
     * keeping the one with the smallest source id. Requires the entries to be sorted
     */
    private void removeDuplicates(ArrayList<Entry> entries) {
        int keep = 0;
        int duplicates = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0 && Arrays.equals(entries.get(i).protocol(), entries.get(keep - 1).protocol())) {
                if (++duplicates <= 10)
                    System.err.printf("Duplicate: source ids %d and %d are isomorphic: %s%n",
                            entries.get(keep - 1).sourceId(), entries.get(i).sourceId(),
                            DSymbolAlgorithms.fromProtocol(entries.get(i).protocol()));
                if (entries.get(i).sourceId() < entries.get(keep - 1).sourceId())
                    entries.set(keep - 1, entries.get(i));
            } else
                entries.set(keep++, entries.get(i));
        }
        entries.subList(keep, entries.size()).clear();
        System.err.printf("Duplicates:        %,13d%s%n", duplicates, (duplicates > 0 ? " (removed)" : ""));
    }

    /**
     * reports any two distinct tilings that receive the same canonical key
     *
     * @return number of colliding keys
     */
    private int reportCollisions(List<Entry> entries, String[] keys, int keyBits) {
        final String[] sorted = keys.clone();
        Arrays.parallelSort(sorted);
        final Set<String> colliding = new HashSet<>();
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i].equals(sorted[i - 1]))
                colliding.add(sorted[i]);
        }

        if (!colliding.isEmpty()) {
            final TreeMap<String, List<String>> details = new TreeMap<>();
            for (int i = 0; i < keys.length; i++) {
                if (colliding.contains(keys[i]))
                    details.computeIfAbsent(keys[i], k -> new ArrayList<>())
                            .add(DSymbolAlgorithms.fromProtocol(entries.get(i).protocol()).toString());
            }
            details.forEach((key, forms) -> System.err.printf("Collision: %s <- %s%n", key, String.join(" ", forms)));
        }
        System.err.printf("Key collisions:    %,13d at %d bits%s%n", colliding.size(), keyBits,
                (colliding.isEmpty() ? "" : ", use a larger -k"));
        return colliding.size();
    }

    /**
     * writes the catalog
     */
    private void write(Writer w, List<Entry> entries, String[] keys, int keyBits, String inputFile) throws IOException {
        w.write("# Tegula tiling catalog\n");
        w.write("# source: %s\n".formatted(FileUtils.getFileNameWithoutPath(inputFile)));
        w.write("# tilings: %d\n".formatted(entries.size()));
        w.write("# key: size, then the leading %d bits of the SHA-256 hash of the protocol, in Crockford base 32\n".formatted(keyBits));
        w.write("# order: by increasing size, then by increasing protocol as a sequence of numbers\n");
        w.write("number\tkey\tsize\tcanonical_symbol\tsource_id\n");

        for (int i = 0; i < entries.size(); i++) {
            final Entry entry = entries.get(i);
            w.write("%d\t%s\t%d\t%s\t%d\n".formatted(i + 1, keys[i], entry.size(),
                    DSymbolAlgorithms.fromProtocol(entry.protocol()), entry.sourceId()));
        }
    }

    /**
     * adds the canonical key and catalog number to the database
     */
    private void updateDatabase(Connection connection, List<Entry> entries, String[] keys) throws SQLException {
        System.err.println("Updating database (this modifies the input file)...");
        try (Statement statement = connection.createStatement()) {
            for (String column : List.of("canonical_key TEXT", "catalog_number INTEGER")) {
                try {
                    statement.execute("alter table tilings add column " + column + ";");
                } catch (SQLException ignored) {
                    // column is already present
                }
            }
        }
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(
                "update tilings set canonical_key=?, catalog_number=? where id=?;")) {
            for (int i = 0; i < entries.size(); i++) {
                statement.setString(1, keys[i]);
                statement.setLong(2, i + 1);
                statement.setLong(3, entries.get(i).sourceId());
                statement.addBatch();
                if ((i + 1) % BATCH_SIZE == 0) {
                    statement.executeBatch();
                    System.err.printf("\rUpdated:           %,13d", i + 1);
                }
            }
            statement.executeBatch();
            System.err.printf("\rUpdated:           %,13d%n", entries.size());
        }
        connection.commit();
        connection.setAutoCommit(true);
        try (Statement statement = connection.createStatement()) {
            statement.execute("create index if not exists idx_canonical_key on tilings(canonical_key);");
            statement.execute("create index if not exists idx_catalog_number on tilings(catalog_number);");
        }
        System.err.println("Database updated");
    }
}
