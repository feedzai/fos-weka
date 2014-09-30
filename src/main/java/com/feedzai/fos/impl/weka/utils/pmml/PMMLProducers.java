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
import org.dmg.pmml.Extension;
import org.dmg.pmml.PMML;
import org.jpmml.model.JAXBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;

import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.ALGORITHM_EXTENSION_ELEMENT;
import static com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.Algorithm;

/**
 * A PMML producer for Weka classifiers.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public class PMMLProducers {

    /**
     * The logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(PMMLProducers.class);


    /**
     * Converts the given {@link weka.classifiers.Classifier} to PMML and saves it to the given file.
     *
     * @param classifier The {@link weka.classifiers.Classifier} to convert to PMML.
     * @param file       The file to save the PMML.
     * @param compress   {@code true} to save the classifier GZipped, or {@code false} otherwise.
     * @throws com.feedzai.fos.impl.weka.exception.PMMLConversionException If if fails to convert to PMML.
     */
    public static void produce(Classifier classifier, File file, boolean compress) throws PMMLConversionException {
        Algorithm algorithm = Algorithm.fromClassifier(classifier);

        PMML pmml = algorithm.getPMMLProducer().produce(classifier);
        addAlgorithm(pmml, algorithm);

        try {
            if (compress) {
                String finalPath = file.getAbsolutePath();

                logger.debug("Saving compressed file to '{}'.", finalPath);

                try (FileOutputStream fileOutputStream = new FileOutputStream(finalPath);
                     GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);)
                {
                    JAXBUtil.marshalPMML(pmml, new StreamResult(gzipOutputStream));
                }
            } else {
                logger.debug("Saving file to '{}'.", file.getAbsolutePath());

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    JAXBUtil.marshalPMML(pmml, new StreamResult(file));
                }
            }

            logger.debug("File successfully saved.");
        } catch (Exception e) {
            logger.error("Failed to marshal the PMML to file.", e);
            throw new PMMLConversionException("Failed to marshal the PMML to file.", e);
        }
    }

    /**
     * Adds a {@link org.dmg.pmml.Extension} element to the {@link org.dmg.pmml.PMML} with the
     * algorithm used to build the classifier.
     * <p/>
     * The name of the extension is {@link com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons#ALGORITHM_EXTENSION_ELEMENT}.
     *
     * @param pmml      The {@link PMML} to which add the algorithm.
     * @param algorithm The algorithm used to build the classifier.
     */
    private static void addAlgorithm(PMML pmml, Algorithm algorithm) {
        Extension extension = new Extension();
        extension.withName(ALGORITHM_EXTENSION_ELEMENT);
        extension.withValue(algorithm.name());

        pmml.withExtensions(extension);
    }
}
