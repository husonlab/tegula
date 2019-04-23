/*
 *  Copyright (C) 2018. Daniel H. Huson
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

package tegula.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import jloda.util.Basic;

import java.io.File;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * setups up a file browser
 * Daniel Huson, 4.2019
 */
public class FileBrowser {
    /**
     * sets the given tree view to display all accepted files below the root directory
     *
     * @param rootDirectory
     * @param extensionFilter
     * @param treeView
     */
    public static void setup(File rootDirectory, final FileChooser.ExtensionFilter extensionFilter, final TreeView<FileNode> treeView) {
        TreeItem<FileNode> root = createNode(new FileNode(rootDirectory), extensionFilter);
        treeView.setRoot(root);

    }

    // This method creates a TreeItem to represent the given File. It does this
    // by overriding the TreeItem.getChildren() and TreeItem.isLeaf() methods
    // anonymously, but this could be better abstracted by creating a
    // 'FileTreeItem' subclass of TreeItem. However, this is left as an exercise
    // for the reader.
    private static TreeItem<FileNode> createNode(final FileNode fileNode, final FileChooser.ExtensionFilter extensionFilter) {
        final TreeItem<FileNode> treeItem = new TreeItem<FileNode>(fileNode) {
            // We cache whether the File is a leaf or not. A File is a leaf if
            // it is not a directory and does not have any files contained within
            // it. We cache this as isLeaf() is called often, and doing the
            // actual check on File is expensive.
            private boolean isLeaf;

            // We do the children and leaf testing only once, and then set these
            // booleans to false so that we do not check again during this
            // run. A more complete implementation may need to handle more
            // dynamic file system situations (such as where a folder has files
            // added after the TreeView is shown). Again, this is left as an
            // exercise for the reader.
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<FileNode>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;

                    // First getChildren() call, so we actually go off and
                    // determine the children of the File contained in this TreeItem.
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    FileNode f = getValue();
                    isLeaf = f.getFile().isFile();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<FileNode>> buildChildren(TreeItem<FileNode> TreeItem) {
                FileNode f = TreeItem.getValue();
                if (f != null && f.getFile().isDirectory()) {
                    final Collection<FileNode> accepted = filter(f.getFile().listFiles(), extensionFilter);
                    if (accepted.size() > 0) {
                        ObservableList<TreeItem<FileNode>> children = FXCollections.observableArrayList();

                        for (FileNode childFile : accepted) {
                            children.add(createNode(childFile, extensionFilter));
                        }

                        return children;
                    }
                }

                return FXCollections.emptyObservableList();
            }
        };
        treeItem.setGraphic(fileNode.getGraphic());
        return treeItem;
    }

    private static Collection<FileNode> filter(File[] listFiles, FileChooser.ExtensionFilter extensionFilter) {
        final SortedSet<FileNode> accepted = new TreeSet<>();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory() || extensionFilter == null || extensionFilter.getExtensions().size() == 0)
                    accepted.add(new FileNode(file));
                else {
                    for (String extension : extensionFilter.getExtensions()) {
                        if (file.getName().toLowerCase().endsWith(extension.toLowerCase().replaceAll("\\*", ""))) {
                            accepted.add(new FileNode(file));
                            break;
                        }
                    }

                }
            }
        }
        return accepted;
    }

    public static class FileNode implements Comparable<FileNode> {
        private final File file;
        private Node graphic;

        public FileNode(File file) {
            this.file = file;
        }

        public String toString() {
            return Basic.replaceFileSuffix(file.getName(), "");
        }

        public File getFile() {
            return file;
        }

        @Override
        public int compareTo(FileNode that) {
            return file.getPath().compareTo(that.getFile().getPath());
        }

        public Node getGraphic() {
            return graphic;
        }

        public void setGraphic(Node node) {
            graphic = node;
        }
    }
}
