# Tegula User Manual

Tegula is an interactive desktop application for exploring periodic tilings of the three
two-dimensional geometries: the sphere, the euclidean plane and the hyperbolic plane.

- Version: 1.0.0
- Written by Daniel H. Huson and Rüdiger Zeller, using code by Klaus Westphal
- License: GPL v3
- Source: [https://github.com/husonlab/tegula](https://github.com/husonlab/tegula)

---

## 1. Introduction

A *periodic tiling* covers a surface with tiles, repeating according to a symmetry group. Tegula
represents such a tiling by its **Delaney–Dress symbol**, a finite combinatorial encoding of both the
tiling and its symmetry group. From that symbol the program computes a fundamental domain with real
coordinates and renders it in 3D, replicating it by the generators of the symmetry group.

Because the encoding is finite, tilings can be enumerated and stored in databases. Tegula ships as a
browser for such databases: you open a collection of many thousands of tilings, scroll through them
as a grid of thumbnails, filter them, and open any one of them in an editor.

### What is a Delaney–Dress symbol?

A tiling is cut into *chambers* (also called flags): triangles each with one corner at a tile centre,
one at an edge centre and one at a vertex of the tiling. The symbol records how chambers are glued to
one another across their three sides, together with the orders of the rotations at the tiling's
vertices and edge centres. Two tilings have the same symbol exactly when they are equivalent as
symmetric tilings, so the symbol is a complete and compact invariant.

In the program's notation a symbol is written on one line, for example:

```
<1.1:1:1,1,1:5,3>
```

This is the dodecahedron: one chamber, and rotation orders 5 and 3.

### The three geometries

The rotation orders decide the geometry, and Tegula computes it for you:

| | condition | rendered as |
|---|---|---|
| **Spherical** | positive curvature | a sphere you can rotate |
| **Euclidean** | zero curvature | a flat plane you can pan |
| **Hyperbolic** | negative curvature | a disk, in the Poincaré or Klein model |

Raising a single rotation order will often move a tiling from one geometry to another, and Tegula
follows it there.

### Key features

- Browse databases of enumerated tilings as a grid of thumbnails
- Open any tiling in an editor and change its symmetry interactively
- Reshape the fundamental domain by dragging its corners and edges
- Choose colours, band widths, lighting and the hyperbolic model
- Apply operations: dualize, maximize symmetry, orientate, straighten
- Export images and save selections

---

## 2. Installation

Installers for macOS, Linux and Windows are available from the
[GitHub releases page](https://github.com/husonlab/tegula/releases/latest).

Download and run the installer for your platform. On macOS, open the `.dmg` and drag the application
to your Applications folder. On Linux, install the `.deb` or unpack the `.tar.gz`. On Windows, run
the `.msi`.

To get started you will also want a database of tilings; see section 4.

---

## 3. The main window at a glance

Tegula opens a window with a menu bar, a toolbar and a tabbed area. Each tab holds either a
**collection** of tilings or a single tiling in the **editor**.

### 3.1 Collection tabs

A collection tab shows the tilings of an opened file or database as a grid of thumbnails, with a
label under each giving its number and its characteristics, for example:

```
Tiling 628 - n:6 t:3 e:3 v:2 g:*532
```

Here `n` is the size of the Delaney–Dress symbol, `t`, `e` and `v` are the numbers of tile, edge and
vertex classes, and `g` is the symmetry group in orbifold notation.

- Scroll to move through the collection
- Double-click a tiling to open it in an editor tab
- Use **Tiling → First Page**, **Last Page** and **Choose Page...** to move through large collections

### 3.2 The editor tab

Double-clicking a tiling opens it in an editor tab, which renders the tiling in 3D and surrounds it
with collapsible panes.

- Drag with the mouse to rotate a spherical tiling, or to pan a euclidean or hyperbolic one
- Scroll or use **View → Zoom In** / **Zoom Out** to change the scale
- **View → Show More Tiles** and **Show Less Tiles** control how much of an unbounded tiling is drawn

### 3.3 The Symmetries pane

Shows the symmetry group in orbifold notation, with a row of spinners beneath it. Each spinner is the
order of one rotation of the tiling: press `++` or `--` to change it.

Changing a rotation order changes the tiling, and often its geometry — a euclidean tiling will become
spherical or hyperbolic. Any reshaping you have done to the fundamental domain is carried over.

### 3.4 The Hyperbolic model pane

Enabled for hyperbolic tilings only. Choose between the **Poincaré** model, which is conformal and
preserves angles, and the **Klein** model, in which geodesics are straight lines. **Tiles** `--` and
`++` set how far out from the centre the tiling is drawn.

### 3.5 The Lighting pane

Choose **Point light**, which shades the surface and gives it depth, or **Ambient light**, which
lights every part of it equally and gives flat, even colour.

### 3.6 The Appearance pane

- **Color scheme** — the palette used for the tiles
- **Faces** / **Backs** — one colour chooser per tile class, and whether to draw the back sides
- The slider under the colour choosers sets tile opacity
- **Permute** — cycle the colours through the tile classes
- **Edges** / **Nodes**, with **Backs** for each — whether to draw bands along the edges and discs at
  the vertices
- **Width** and the colour chooser set the width and colour of the bands
- **Smooth** — subdivide the mesh for smoother curved surfaces
- **Background** — the colour behind the tiling

### 3.7 The Fundamental domain pane

Opens an editable view of the fundamental domain, the piece of the tiling from which the whole is
generated. It is drawn as a polygon, subdivided into its chambers, with draggable handles:

| handle | meaning |
|---|---|
| red diamond | a vertex of the tiling |
| green square | an edge centre |
| yellow square | a point restricted to lie on a mirror line |

Drag a handle to reshape the domain; the tiling redraws to match. Dragging an edge centre bends that
edge, which is how you give tiles curved or interlocking boundaries. **Reset** restores the computed
shape.

### 3.8 The Algorithms pane

Holds operations that can be applied to the tiling; see section 5.

---

## 4. Loading and saving data

### 4.1 Supported formats

| extension | contents |
|---|---|
| `.tdb` | a database of enumerated tilings, holding many thousands of symbols |
| `.tgs` | a plain-text file of Delaney–Dress symbols, one per line |
| `.tegula` | a collection together with its styling |

### 4.2 Getting a database

Databases of enumerated tilings can be downloaded from:

[https://software-ab.cs.uni-tuebingen.de/download/tegula](https://software-ab.cs.uni-tuebingen.de/download/tegula)

Unzip the download and open the file with **File → Open...**.

### 4.3 Opening and saving

- **File → Open...** opens a database, a `.tgs` file or a `.tegula` file
- **File → Open Recent** lists files you have opened before
- **File → Save Selected...** writes the tilings you have selected to a file
- **File → Print...** prints the current view, after **Page Setup...**

---

## 5. Operations on a tiling

These act on the tiling in the current editor tab, and are undoable.

| operation | effect |
|---|---|
| **Dualize** | exchange the roles of tiles and vertices |
| **Max Symmetry** | replace the tiling by the one with the largest symmetry group having the same shape |
| **Orientate** | pass to the orientation double cover, so that the symmetry group contains no reflections |
| **Straighten** | straighten all edges of the fundamental domain, undoing any reshaping |

---

## 6. Menu reference

### 6.1 File menu

| item | |
|---|---|
| New... | open an empty window |
| Open... | open a database or file of tilings |
| Open Recent | reopen a previously opened file |
| Save Selected... | write the selected tilings to a file |
| Page Setup... / Print... | print the current view |
| Close | close the current window |
| Quit | leave the program |

### 6.2 Edit menu

Undo, Redo, Copy, Paste, Select All, Select None, and **Open in Editor...**, which opens the selected
tiling in an editor tab.

### 6.3 View menu

| item | |
|---|---|
| Use Dark Theme | switch between the light and dark interface |
| Show Labels | show or hide the label under each tiling |
| Color Preview | colour the thumbnails in a collection |
| Show Chambers | draw the chamber subdivision on the tiling |
| Show More / Less Tiles | how much of an unbounded tiling is drawn |
| Reset | restore the default view |
| Zoom In / Zoom Out | change the scale |
| Set Max Copies Hyperbolic... / Euclidean... | limits on how many copies are generated |
| Enter Fullscreen | fill the screen |

### 6.4 Tiling menu

First Page, Last Page and Choose Page... move through a collection; Dualize, Max Symmetry, Orientate
and Straighten are described in section 5.

### 6.5 Window menu

Lists the open windows, so that you can bring any of them to the front.

### 6.6 Help menu

| item | |
|---|---|
| Check for Updates... | ask whether a newer version has been released |
| About... | show the version and authors |
| Open User Manual in Browser... | open this manual |

---

## 7. Notes on the mathematics

The symmetry groups are named in **orbifold notation**: digits are the orders of rotation points, a
`*` introduces mirror lines, and digits after a `*` are the orders of the corners where mirrors meet.
So `*532` is the symmetry group of the dodecahedron including its reflections, and `532` is its
rotation-only subgroup of index two — which is what **Orientate** computes.

The databases distributed with Tegula contain all periodic tilings of Dress complexity at most 18,
and all euclidean and spherical tilings of Dress complexity at most 24. They were computed with
`genDSyms`, available at [https://github.com/odf/julia-dsymbols](https://github.com/odf/julia-dsymbols).

The underlying mathematics, the algorithms and the software are described in the paper cited below.

---

## 8. Citation

If you use Tegula in your work, please cite:

- Rüdiger Zeller, Olaf Delgado-Friedrichs and Daniel H. Huson. Tegula — exploring a galaxy of
  two-dimensional periodic tilings. *Computer Aided Geometric Design* **90** (2021).
  [doi:10.1016/j.cagd.2021.102027](https://doi.org/10.1016/j.cagd.2021.102027)

---

## 9. License

Tegula is free software, released under the GNU General Public License v3. It comes with no warranty.
See `LICENSE.txt` in the source repository for the full text.
