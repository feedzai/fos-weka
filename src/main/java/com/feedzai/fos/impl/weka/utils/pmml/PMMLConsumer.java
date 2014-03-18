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
 * A common interface to the PMML consumers of different {@link Classifier} implementations.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public interface PMMLConsumer<T extends Classifier> {

    /**
     * Builds a new classifier from the given PMML String.
     * <p/>
     * The given {@code pmmlString} should be a valid PMML.
     *
     * @param pmmlString A String representing the PMML that is to be converted to a {@link hr.irb.fastRandomForest.FastRandomForest}.
     * @return A new {@link Classifier} instance.
     * @throws Exception If it fails to convert the given PMML to a {@link Classifier}.
     */
    T consume(String pmmlString) throws PMMLConversionException;

    /**
     * Builds a new classifier from the given file.
     *
     * @param file The file with the PMML representation of the classifier.
     * @return A new {@link Classifier} instance.
     * @throws Exception If it fails to convert the given file to a {@link Classifier}.
     */
    T consume(File file) throws PMMLConversionException;

    /**
     * Builds a new classifier from the given {@link org.dmg.pmml.PMML}.
     *
     * @param pmml The {@link org.dmg.pmml.PMML} which is to be converted to a {@link hr.irb.fastRandomForest.FastRandomForest}.
     * @return A new {@link Classifier} instance.
     * @throws Exception If it fails to convert the given PMML to a {@link Classifier}.
     */
    T consume(PMML pmml) throws PMMLConversionException;
}
