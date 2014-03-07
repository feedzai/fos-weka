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

import com.feedzai.fos.api.FOSException;
import weka.classifiers.Classifier;

/**
 * Base interface for scorers.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public interface WekaThreadSafeScorer {

    /**
     * Scores the give instance.
     *
     * @param scorable The instance to score.
     * @return The result of the score.
     * @throws FOSException If it fails to score the instance.
     */
    double[] score(Object[] scorable) throws FOSException;

    /**
     * Retrieves the {@link weka.classifiers.Classifier} associated with this scorer.
     *
     * @return The {@link weka.classifiers.Classifier} associated with this scorer.
     * @throws FOSException If it fails to retrieve the classifier.
     */
    Classifier getClassifier() throws FOSException;

    /**
     * Closes all resources allocated to the scorer (thread pools, etc).
     */
    void close();
}
