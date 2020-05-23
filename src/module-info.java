module tegula {
    requires transitive jloda;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.swing;
    requires transitive javafx.fxml;
    requires transitive java.sql;
    requires sqlite.jdbc;

    requires fx.platform.utils;
    requires com.install4j.runtime;

    exports tegula.color;
    exports tegula.db;
    exports tegula.core.dsymbols;
    exports tegula.core.funtiles;
    exports tegula.core.funtiles.data;
    exports tegula.core.funtiles.utils;
    exports tegula.core.reshape;
    exports tegula.fdomaineditor;
    exports tegula.geometry;
    exports tegula.main;
    exports tegula.tiling;
    exports tegula.tiling.parts;
    exports tegula.filecollection;
    exports tegula.dbcollection;
    exports tegula.tilingeditor;
    exports tegula.tilingpane;
    exports tegula.undoable;
    exports tegula.util;

    opens tegula.resources.icons;
    opens tegula.resources.images;
    opens tegula.color;
    opens tegula.main;
    opens tegula.dbcollection;
    opens tegula.filecollection;
    opens tegula.tilingeditor;
}