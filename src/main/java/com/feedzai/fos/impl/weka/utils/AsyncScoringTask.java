/*
 * $#
 * FOS Weka
 *  
 * Copyright (C) 2013 Feedzai SA
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #$
 */
package com.feedzai.fos.impl.weka.utils;

import com.feedzai.fos.common.validation.NotNull;

import java.util.concurrent.Callable;

/**
 * Task for asynchronous scoring.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class AsyncScoringTask implements Callable<double[]> {
    private WekaThreadSafeScorer wekaThreadSafeScorer;
    private Object[] scorable;

    /**
     * Creates a new task with the given scorer and scorable.
     *
     * @param wekaThreadSafeScorer the scorer that will score the scorable
     * @param scorable             the scorable to score
     */
    public AsyncScoringTask(WekaThreadSafeScorer wekaThreadSafeScorer,Object[] scorable) {
        this.wekaThreadSafeScorer = wekaThreadSafeScorer;
        this.scorable = scorable;
    }

    /**
     * Uses the scorer to score the scorable and return the numeric score (so many scor*!).
     *
     * @return the value of the score
     * @throws Exception when the underlying scorer threw exception
     */
    @Override
    @NotNull
    public double[] call() throws Exception {
        return wekaThreadSafeScorer.score(scorable);
    }
}
