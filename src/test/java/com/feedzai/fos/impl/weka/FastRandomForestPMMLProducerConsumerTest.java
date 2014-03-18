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

import com.feedzai.fos.impl.weka.utils.pmml.PMMLConsumers;
import com.feedzai.fos.impl.weka.utils.pmml.PMMLProducers;
import hr.irb.fastRandomForest.FastRandomForest;
import org.junit.Test;
import weka.core.Instances;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 */
public class FastRandomForestPMMLProducerConsumerTest extends BasePMMLProducerConsumerTest {

    @Test
    public void testIris() throws Exception {
        File dataset = new File(getClass().getResource("/datasets/iris_model_builder.arff").getPath());
        Instances iris = readArff(dataset);
        testAbstractClassifier(new FastRandomForest(), iris);
    }

    @Test
    public void testShuttleLandingControl() throws Exception {
        File dataset = new File(getClass().getResource("/datasets/shuttle-landing-control.arff").getPath());
        Instances iris = readArff(dataset);
        testAbstractClassifier(new FastRandomForest(), iris);
    }

    @Test
    public void testSavesCompressedFile() throws Exception {
        File dataset = new File(getClass().getResource("/datasets/shuttle-landing-control.arff").getPath());
        Instances instances = readArff(dataset);
        FastRandomForest classifier = new FastRandomForest();
        classifier.buildClassifier(instances);

        File file = Files.createTempFile("frf_iris_pmml", ".xml").toFile();

        PMMLProducers.produce(classifier, file, true);

        FastRandomForest fromPmml = (FastRandomForest) PMMLConsumers.consume(new File(file.getAbsolutePath()));

        for (int i = 0; i < instances.numInstances(); i++) {
            String wekaDist = Arrays.toString(classifier.distributionForInstance(instances.instance(i)));
            String pmmlDist = Arrays.toString(fromPmml.distributionForInstance(instances.instance(i)));

            assertEquals("Distributions for instance match.", wekaDist, pmmlDist);
        }

        file.delete();
    }


}
