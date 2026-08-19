# Stable catalogs

One catalog per tilings database, derived with `tegula.db.CatalogNumbering`. The databases in
`../tilings/` were not modified.

Each file is a gzipped, tab-separated table with a comment header and the columns:

| column | meaning |
| --- | --- |
| `number` | catalog number: position in this catalog, counting from 1 |
| `key` | canonical key, e.g. `DS07-80VG-B42W-4N1Y` |
| `size` | size of the Delaney-Dress symbol |
| `canonical_symbol` | the canonical form, a valid D-symbol string with number `0.0` |
| `source_id` | the `id` this tiling happens to have in the source database |

Tilings are ordered by increasing size, then by increasing protocol, where the protocol of a symbol
of size n is the sequence of 5n numbers s0(1)..s0(n), s1(1)..s1(n), s2(1)..s2(n), m01(1)..m01(n),
m12(1)..m12(n) of its canonical form. Comparing protocols rather than text means neither the
ordering nor the keys depend on how a symbol happens to be printed. The order depends only on the
set of tilings, so the numbering is reproducible from the canonical forms alone, and extending a
catalog to larger symbols appends to it rather than renumbering it. The `source_id` is the only
column that is specific to the source database.

Passing `-g` adds a sixth column holding Delgado-Friedrichs' invariant, as computed by Gavrog, for
anyone who wants to join a catalog against Gavrog's own identifier for the same tiling. It is not
stored here: it adds about half again to the size of a catalog, and it is easier to regenerate on
demand than to carry for everybody.

The scheme, the canonicalization algorithm and the verification results are described in
[../doc/tiling-identity.html](../doc/tiling-identity.html).

## Rebuilding

```
java -Xmx8g -cp target/classes:'target/dependency/*' tegula.db.CatalogNumbering \
     -i tilings/tilings-1-19.tdb -o catalog/tilings-1-19.catalog.tsv.gz
```

## Contents

| catalog | tilings | sizes | duplicates | key collisions at 60 bits |
| --- | ---: | ---: | ---: | ---: |
| `tilings-1-16` | 795,590 | 1-16 | 0 | 0 |
| `tilings-1-18` | 5,214,516 | 1-18 | 0 | 0 |
| `tilings-1-19` | 5,214,516 | 1-18 | 0 | 0 |
| `euclidean-1-24` | 1,728,488 | 1-24 | 0 | 0 |
| `spherical-1-24` | 2,155,818 | 1-24 | 0 | 0 |

`tilings-1-18` and `tilings-1-19` hold the same 5,214,516 tilings under different `id` numbering,
and produce byte-identical catalogs. The `tilings-1-16` catalog is an exact prefix of the
`tilings-1-19` catalog.
