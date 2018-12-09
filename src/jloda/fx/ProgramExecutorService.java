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
package jloda.fx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * program executor service
 * all concurrent services should use this executor service
 * Daniel Huson, 12/11/16.
 */
public class ProgramExecutorService {
    private static ExecutorService instance;
    private static int maxNumberOfThreadsForParallelAlgorithm = Integer.MAX_VALUE; // max number of threads to use by a parallel algorithm

    /**
     * get the program wide executor service
     *
     * @return executor service
     */
    public static ExecutorService getInstance() {
        if (instance == null)
            instance = Executors.newCachedThreadPool();
        return instance;
    }

    public static void setMaxNumberOfThreadsForParallelAlgorithm(int maxNumberOfThreadsForParallelAlgorithm) {
        if (maxNumberOfThreadsForParallelAlgorithm > 0)
            ProgramExecutorService.maxNumberOfThreadsForParallelAlgorithm = maxNumberOfThreadsForParallelAlgorithm;
        else
            ProgramExecutorService.maxNumberOfThreadsForParallelAlgorithm = Integer.MAX_VALUE;
    }

    public static int getMaxNumberOfThreadsForParallelAlgorithm() {
        return maxNumberOfThreadsForParallelAlgorithm;
    }
}
