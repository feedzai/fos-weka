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

import com.feedzai.fos.impl.weka.utils.pmml.PMMLConsumer;
import com.feedzai.fos.impl.weka.utils.pmml.PMMLProducer;
import org.dmg.pmml.IOUtil;
import org.dmg.pmml.PMML;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomForestPMMLProducer;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by rfer on 3/4/14.
 */
public abstract class BasePMMLProducerConsumerTest {

    protected void testAbstractClassifier(Classifier classifier, Instances instances) throws Exception {
        classifier.setOptions(new String[]{"-I", "1", "-K", "1", "-S", "1", "-depth", "1"});
        classifier.buildClassifier(instances);
        testConversion(classifier, instances);

        classifier.setOptions(new String[]{"-I", "2", "-K", "2", "-S", "2", "-depth", "2"});
        classifier.buildClassifier(instances);
        testConversion(classifier, instances);

        classifier.setOptions(new String[]{"-I", ""+instances.numInstances(), "-K", ""+instances.numInstances(), "-S", "4", "-depth", "0"});
        classifier.buildClassifier(instances);
        testConversion(classifier, instances);
    }

    protected void testConversion(Classifier classifier, Instances instances) throws Exception {
        File file = Files.createTempFile("frf_iris_pmml", ".xml").toFile();

        System.err.println("writing to file: " + file.getAbsolutePath());

        PMMLProducer.produce(classifier, file, false);

        Classifier fromPmml = PMMLConsumer.consume(file);

        for (int i = 0; i < instances.numInstances(); i++) {
            String wekaDist = Arrays.toString(classifier.distributionForInstance(instances.instance(i)));
            String pmmlDist = Arrays.toString(fromPmml.distributionForInstance(instances.instance(i)));

            assertEquals("Distributions for instance match.", wekaDist, pmmlDist);
        }

        file.delete();
    }


    protected Instances readArff(File arffSource) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(arffSource));

        Instances data = new Instances(reader);
        reader.close();

        data.setClassIndex(data.numAttributes() - 1);

        return data;
    }
}
