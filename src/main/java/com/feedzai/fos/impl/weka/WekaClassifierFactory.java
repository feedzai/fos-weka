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
package com.feedzai.fos.impl.weka;

import com.feedzai.fos.api.FOSException;
import com.feedzai.fos.api.ModelConfig;
import com.feedzai.fos.impl.weka.config.WekaModelConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This will be responsible for creating weka classifiers instances
 * and supply the relevant configuration parameters for each one of them
 */
public class WekaClassifierFactory {
    private static final Logger logger = LoggerFactory.getLogger(WekaClassifierFactory.class);

    /**
     * @param modelConfig model config should contain the wanted classifier
     * @return instantiated classifier
     * @throws FOSException if it has failed to create a classifier
     */
    public static Classifier create(ModelConfig modelConfig) throws FOSException {
        checkNotNull(modelConfig, "Model config cannot be null");

        String classifierNames = modelConfig.getProperty(WekaModelConfig.CLASSIFIER_IMPL);

        try {
            Classifier classifier = (Classifier) Class.forName(modelConfig.getProperty(WekaModelConfig.CLASSIFIER_IMPL)).newInstance();

            classifier.setOptions(weka.core.Utils.splitOptions(StringUtils.defaultString(modelConfig.getProperty(WekaModelConfig.CLASSIFIER_CONFIG))));

            return classifier;
        } catch (Exception e) {
            throw new FOSException(e);
        }
    }
}
