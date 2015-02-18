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
import hr.irb.fastRandomForest.FastRandomForest;
import junit.framework.Assert;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.MultipleClassifiersCombiner;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;

/**
 * @author Marco Jorge (marco.jorge@feezai.com)
 */
public class WekaClassifierFactoryTest {
    @Test
    public void testOneModel() throws FOSException {
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setProperty(WekaModelConfig.CLASSIFIER_IMPL, J48.class.getName());

        Classifier classifier = WekaClassifierFactory.create(modelConfig);
        Assert.assertEquals(J48.class, classifier.getClass());
    }

    @Test
    public void testMultipleModels() throws FOSException {
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setProperty(WekaModelConfig.CLASSIFIER_IMPL, Vote.class.getName());
        modelConfig.setProperty(WekaModelConfig.CLASSIFIER_CONFIG, "-R MAX -B \""+J48.class.getName()+"\" -B \"" + NaiveBayes.class.getName() + "\"");

        MultipleClassifiersCombiner classifier = (MultipleClassifiersCombiner)WekaClassifierFactory.create(modelConfig);
        Assert.assertEquals(2,classifier.getClassifiers().length);
        Assert.assertEquals(J48.class,classifier.getClassifiers()[0].getClass());
        Assert.assertEquals(NaiveBayes.class,classifier.getClassifiers()[1].getClass());
    }

    @Test
    public void testFastRandomForestModel() throws FOSException {
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setProperty(WekaModelConfig.CLASSIFIER_IMPL, "hr.irb.fastRandomForest.FastRandomForest");

        Classifier classifier = WekaClassifierFactory.create(modelConfig);

        Assert.assertEquals("A FastRandomForest model was created", FastRandomForest.class, classifier.getClass());
    }
}
