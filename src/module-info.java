module tegula {
    requires transitive jloda;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.swing;
    requires transitive javafx.fxml;
    requires transitive java.sql;

    requires fx.platform.utils;
    requires com.install4j.runtime;

    exports tegula.color;
    exports tegula.core.dsymbols;
    exports tegula.core.fundamental;
    exports tegula.core.fundamental.data;
    exports tegula.core.fundamental.utils;
    exports tegula.core.reshape;
    exports tegula.fdomaineditor;
    exports tegula.geometry;
    exports tegula.main;
    exports tegula.tiling;
    exports tegula.tiling.parts;
    exports tegula.tilingcollection;
    exports tegula.tilingeditor;
    exports tegula.tilingpane;
    exports tegula.undoable;
    exports tegula.util;

    opens tegula.resources.icons;
    opens tegula.color;
    opens tegula.main;
    opens tegula.tilingcollection;
    opens tegula.tilingeditor;
}