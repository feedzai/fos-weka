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
package com.feedzai.fos.impl.weka.utils.pmml;

import com.feedzai.fos.impl.weka.exception.PMMLConversionException;
import org.dmg.pmml.PMML;
import weka.classifiers.Classifier;

import java.io.File;

/**
 * A common interface to the PMML producers of different {@link Classifier} implementations.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public interface PMMLProducer<T extends Classifier> {

    /**
     * Converts the given classifier instance to PMML and saves the result in the given {@link File}.
     *
     * @param classifier The {@link Classifier} instance to convert to PMML.
     * @param targetFile The file where to save the resulting PMML.
     * @throws PMMLConversionException If if fails to convert the classifier.
     */
    void produce(T classifier, File targetFile) throws PMMLConversionException;

    /**
     * Converts the given {@link Classifier} instance to PMML.
     *
     * @param classifier The {@link Classifier} instance to convert to PMML.
     * @return A {@link org.dmg.pmml.PMML} instance representing the PMML structure.
     */
    PMML produce(T classifier);
}
