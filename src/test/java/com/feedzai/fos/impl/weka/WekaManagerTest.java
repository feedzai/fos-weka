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

import com.feedzai.fos.api.*;
import com.feedzai.fos.api.config.FosConfig;
import com.feedzai.fos.impl.weka.config.WekaManagerConfig;
import com.feedzai.fos.impl.weka.config.WekaModelConfig;
import com.feedzai.fos.impl.weka.utils.Cloner;
import com.feedzai.fos.impl.weka.utils.pool.GenericObjectPoolConfig;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.*;

/**
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaManagerTest {
    private WekaManager wekaManager = null;
    private ModelConfig modelConfig = null;
    private final UUID testUID = UUID.fromString("54c5f947-fb25-46cd-b3af-499376171053");

    private WekaManagerConfig setupWekaConfig(String headerLocation) {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".minIdle", 10);
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".maxActive", 10);
        configuration.setProperty(FosConfig.HEADER_LOCATION, headerLocation);
        configuration.setProperty(FosConfig.FACTORY_NAME, WekaManagerFactory.class.getName());
        return new WekaManagerConfig(new FosConfig(configuration));
    }

    private ModelConfig setupModelConfig() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new NumericAttribute("firstNumeric"));
        attributes.add(new NumericAttribute("secondNumeric"));

        ModelConfig mc = new ModelConfig(new ArrayList<Attribute>() {{
            add(new NumericAttribute("field"));
        }}, Collections.EMPTY_MAP);

        return mc;
    }

    private String createRandomDir() {
        String randomDirName = "tmp/" + UUID.randomUUID().toString();
        File f = new File(randomDirName);

        f.mkdirs();

        return randomDirName;
    }


    @Before
    public void setup() {
        cleanup();

        WekaManagerConfig wekaManagerConfig = setupWekaConfig("target/test-classes/models/threadsafe");
        wekaManager = new WekaManager(wekaManagerConfig);
        modelConfig = setupModelConfig();
    }

    @After
    public void cleanup() {
        Collection<File> files = FileUtils.listFiles(new File("target/test-classes/models/threadsafe"), new String[]{WekaManagerConfig.HEADER_EXTENSION}, false);
        for (File file : files) {
            if (!file.getName().startsWith("test")) {
                file.delete();
            }
        }
        if (wekaManager != null) {
            try {
                wekaManager.close();
            } catch (FOSException e) {
            }
        }
    }

    @Test
    public void listModelsTest() throws FOSException {
        assertNotNull(wekaManager.listModels());
        Assert.assertEquals(1, wekaManager.listModels().size());
    }

    @Test
    public void addModelByByteArrayTest() throws FOSException, IOException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");
        wekaManager.addModel(modelConfig, new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        Assert.assertEquals(2, wekaManager.listModels().size());
    }

    @Test
    public void addModelByFilenameTest() throws FOSException {
        wekaManager.addModel(modelConfig, new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model"));

        Assert.assertEquals(2, wekaManager.listModels().size());
    }

    @Test
    public void removeModelTest() throws FOSException, IOException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");
        UUID id = wekaManager.addModel(modelConfig, new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        Assert.assertEquals(2, wekaManager.listModels().size());

        wekaManager.removeModel(id);
        Assert.assertEquals(1, wekaManager.listModels().size());
    }

    @Test
    public void reconfigureModelByModelTest() throws FOSException, IOException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");
        UUID id = wekaManager.addModel(wekaManager.listModels().get(testUID), new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        wekaManager.reconfigureModel(id, modelConfig);
        Assert.assertEquals(2, wekaManager.listModels().size());
        Assert.assertEquals(modelConfig.getAttributes(), wekaManager.listModels().get(id).getAttributes());
    }

    @Test
    public void reconfigureModelByFileNameTest() throws FOSException, IOException, ClassNotFoundException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");

        UUID id = wekaManager.addModel(new Cloner<>(wekaManager.listModels().get(testUID)).get(), new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        wekaManager.reconfigureModel(id, modelConfig, descriptor);

        Assert.assertEquals(2, wekaManager.listModels().size());
        Assert.assertEquals(modelConfig.getAttributes(), wekaManager.listModels().get(id).getAttributes());
        Assert.assertEquals(new File(descriptor.getModelFilePath()).getAbsolutePath(), (wekaManager.listModels().get(id).getProperties().get(WekaModelConfig.MODEL_FILE)));
    }

    @Test
    public void reconfigureModelByByteArrayTest() throws FOSException, IOException, ClassNotFoundException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");

        UUID id = wekaManager.addModel(new Cloner<>(wekaManager.listModels().get(testUID)).get(), new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        String previousFiles = wekaManager.listModels().get(id).getProperties().get(WekaModelConfig.MODEL_FILE);

        wekaManager.reconfigureModel(id, modelConfig, new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        Assert.assertEquals(2, wekaManager.listModels().size());
        Assert.assertEquals(modelConfig.getAttributes(), wekaManager.listModels().get(id).getAttributes());
        Assert.assertTrue(new File(wekaManager.listModels().get(id).getProperties().get(WekaModelConfig.MODEL_FILE)).exists());
        Assert.assertFalse(new File(previousFiles).getAbsolutePath().equals(wekaManager.listModels().get(id).getProperties().get(WekaModelConfig.MODEL_FILE)));
    }

    @Test
    public void closeTest() throws FOSException, IOException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");
        UUID id = wekaManager.addModel(modelConfig, new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        assertNotNull(wekaManager.listModels().get(id).getProperties().get(WekaModelConfig.MODEL_FILE));
        Assert.assertTrue(new File(wekaManager.listModels().get(id).getProperties().get(WekaModelConfig.MODEL_FILE)).exists());

        wekaManager.close();
    }

    @Test
    public void scoreOnNewModel() throws FOSException, IOException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");

        UUID id = wekaManager.addModel(wekaManager.listModels().get(testUID), new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        double[] score = wekaManager.getScorer().score(Lists.newArrayList(id), new Object[]{1.5, 0, "gray", "positive"}).get(0);
        assertEquals(2, score.length);
        assertEquals(1.0, score[0] + score[1], 0.001);
    }

    @Test
    public void scoreOnUpdated() throws FOSException, IOException, ClassNotFoundException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");
        UUID id = wekaManager.addModel(new Cloner<>(wekaManager.listModels().get(testUID)).get(), new ModelBinary(new Cloner<Classifier>(descriptor).getSerialized()));

        double[] score = wekaManager.getScorer().score(Lists.newArrayList(id), new Object[]{1.5, 0, "gray", "positive"}).get(0);
        assertEquals(2, score.length);
        assertEquals(1.0, score[0] + score[1], 0.001);
        wekaManager.reconfigureModel(id, new Cloner<>(wekaManager.listModels().get(id)).get());

        score = wekaManager.getScorer().score(Lists.newArrayList(id), new Object[]{1.5, 0, "gray", "positive"}).get(0);
        assertEquals(2, score.length);
        assertEquals(1.0, score[0] + score[1], 0.001);
    }

    @Test
    public void testIris() throws FOSException, IOException, ClassNotFoundException {
        ModelConfig config = ModelConfig.fromFile("target/test-classes/models/iris/iris.header");
        UUID model = wekaManager.trainAndAddFile(config, "target/test-classes/models/iris/iris.data");
        ModelConfig modelConfig = wekaManager.listModels().get(model);
        assertNotNull("A valid model config must exist for the existing UUID", modelConfig);
        File modelfile = new File(modelConfig.getProperty(WekaModelConfig.MODEL_FILE));
        assertTrue(modelfile.exists());
        byte[] serialized_model = new byte[(int) modelfile.length()];

        FileInputStream fis = new FileInputStream(modelfile);
        fis.read(serialized_model);

        Cloner<Classifier> cloner = new Cloner<Classifier>(serialized_model);
        Classifier c = cloner.get();
        assertNotNull(c.getCapabilities());

        Object[] scoring_instance = new Object[]{4.7, 3.2, 1.3, 0.2, "Iris-setosa"};
        List<double[]> scores = wekaManager.getScorer().score(Arrays.asList(model), scoring_instance);

        KryoScorer kryoScorer = new KryoScorer("127.0.0.1", FosConfig.DEFAULT_SCORING_PORT);

        List<double[]> kryoScores = kryoScorer.score(Arrays.asList(model), scoring_instance);
        assertNotNull(scores);
        assertNotNull(kryoScores);

        assertEquals("Must return 1 score", 1, scores.size());
        assertEquals("Kryo Must return 1 score", 1, kryoScores.size());
        assertEquals("Scores must be equal", kryoScores.get(0).length, scores.get(0).length);
        assertEquals("Scores must be equal", kryoScores.get(0)[0], scores.get(0)[0], 1e-6);

    }
}
