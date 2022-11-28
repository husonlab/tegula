# Tegula

Tegula is a program for displaying and exploring two-dimensional periodic tilings, based on the concept of Delaney-Dress
symbols. Tegula was written by Daniel Huson and Rüdiger Zeller, using code by Klaus Westphal. Tegula is written in
Java (12) and uses JavaFX.

Installers for Tegula can be found here: https://software-ab.cs.uni-tuebingen.de/download/tegula

We provide three databases of tilings, containing all periodic tilings with Dress complexity ≤ 18 and all euclidean and
spherical tilings with Dress complexity ≤ 24. These were computed using genDSyms, available
here: https://github.com/odf/julia-dsymbols.

We have computed 2.4 billion tilings in total, but that file is too big to put online.

<img src="https://github.com/husonlab/tegula/blob/master/images/greens-2.png" width="250"><img src="https://github.com/husonlab/tegula/blob/master/images/greens-3.png" width="250"><img src="https://github.com/husonlab/tegula/blob/master/images/greens-1.png" width="250">
Image credits: Daniel Huson

The underlying mathematics, algorithms and sofware are described in this paper:

Rüdiger Zeller, Olaf Delgado-Friedrichs, Daniel H. Huson, Tegula – exploring a galaxy of two-dimensional periodic tilings, Computer Aided Geometric Design,
90 (2021) https://doi.org/10.1016/j.cagd.2021.102027.
