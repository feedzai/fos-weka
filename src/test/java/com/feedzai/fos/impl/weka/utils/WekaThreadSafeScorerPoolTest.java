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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedzai.fos.api.ModelConfig;
import com.feedzai.fos.api.config.FosConfig;
import com.feedzai.fos.impl.weka.WekaManagerFactory;
import com.feedzai.fos.impl.weka.config.WekaManagerConfig;
import com.feedzai.fos.impl.weka.config.WekaModelConfig;
import com.feedzai.fos.impl.weka.utils.pool.GenericObjectPoolConfig;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;

import static junit.framework.Assert.assertEquals;

/**
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaThreadSafeScorerPoolTest {
    @Test
    public void testScoring() throws Exception {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".minIdle", 10);
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".maxActive", 10);
        configuration.setProperty(FosConfig.HEADER_LOCATION, "target/test-classes/models/threadsafe");
        configuration.setProperty(FosConfig.FACTORY_NAME, WekaManagerFactory.class.getName());

        FileInputStream fis = new FileInputStream("target/test-classes/models/threadsafe/test.header");
        String modelConfigJson = IOUtils.toString(fis);
        ObjectMapper mapper = new ObjectMapper();

        ModelConfig modelConfig = mapper.readValue(modelConfigJson, ModelConfig.class);


        WekaManagerConfig wekaManagerConfig = new WekaManagerConfig(new FosConfig(configuration));
        WekaThreadSafeScorer wekaThreadSafeScorer = new WekaThreadSafeScorerPool(new WekaModelConfig(modelConfig,wekaManagerConfig), wekaManagerConfig);

        double[] score = wekaThreadSafeScorer.score(new Object[]{1.5, 0, "gray", "positive"});
        assertEquals(2, score.length);
        assertEquals(1.0, score[0] + score[1], 0.001);
    }
}
