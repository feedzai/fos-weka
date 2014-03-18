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
import org.dmg.pmml.IOUtil;
import org.dmg.pmml.PMML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;

import java.io.*;
import java.util.zip.GZIPInputStream;

import static com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.ALGORITHM_EXTENSION_ELEMENT;
import static com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.Algorithm;

/**
 * A PMML consumer for Weka classifiers.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public class PMMLConsumers {

    /**
     * The logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(PMMLConsumers.class);


    /**
     * Consumes the PMML in the given file and converts it to a Weka {@link Classifier}.
     *
     * @param file The file with the PMML representation of the classifier.
     * @return A Weka {@link Classifier}.
     * @throws PMMLConversionException If if fails to consume the PMML file.
     */
    public static Classifier consume(File file) throws PMMLConversionException {
        PMML pmml = null;
        try {
            if (isGzipped(file)) {
                logger.debug("Consuming GZipped PMML file '{}'.", file.getAbsolutePath());

                try (FileInputStream fis = new FileInputStream(file);
                     GZIPInputStream gzipInputStream = new GZIPInputStream(fis))
                {
                    pmml = IOUtil.unmarshal(gzipInputStream);
                }
            } else {
                logger.debug("Consuming PMML file '{}'.", file.getAbsolutePath());

                pmml = IOUtil.unmarshal(file);
            }
        } catch (Exception e) {
            throw new PMMLConversionException("Failed to unmarshal PMML file '" + file + "'. Make sure the file is a valid PMML.", e);
        }

        Algorithm algorithm = getAlgorithm(pmml);

        logger.debug("Consumed PMML file using algorithm {}.", algorithm);

        return algorithm.getPMMLConsumer().consume(pmml);
    }

    /**
     * Checks if the given file is in GZIP format.
     * <p/>
     * It checks this by checking the head for the {@link java.util.zip.GZIPInputStream#GZIP_MAGIC} number.
     *
     * @param file The file to check.
     * @return {@code true} if the file is in GZIP format, or {@code false}
     * @throws IOException
     */
    private static boolean isGzipped(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        int head = (fileInputStream.read() & 0xff) | ((fileInputStream.read() << 8 ) & 0xff00 );

        return GZIPInputStream.GZIP_MAGIC == head;
    }

    /**
     * Retrieves the {@link com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.Algorithm} represented
     * as a PMML extension named {@link com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons#ALGORITHM_EXTENSION_ELEMENT}.
     *
     * @param pmml The {@link PMML}.
     * @return The {@link com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.Algorithm} in the PMML.
     * @throws PMMLConversionException If it fails to retrieve the algorithm from the given PPML.
     */
    private static Algorithm getAlgorithm(PMML pmml) throws PMMLConversionException {
        for (Extension extension : pmml.getExtensions()) {
            if (ALGORITHM_EXTENSION_ELEMENT.equals(extension.getName())) {
                return Algorithm.valueOf(extension.getValue());
            }
        }

        throw new PMMLConversionException("Couldn't find '" + ALGORITHM_EXTENSION_ELEMENT + "' extension element in PMML.");
    }
}
