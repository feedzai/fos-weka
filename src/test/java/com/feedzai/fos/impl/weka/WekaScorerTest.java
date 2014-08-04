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
import com.feedzai.fos.api.config.FosConfig;
import com.feedzai.fos.impl.weka.config.WekaManagerConfig;
import com.feedzai.fos.impl.weka.utils.WekaThreadSafeScorer;
import com.feedzai.fos.impl.weka.utils.WekaThreadSafeScorerPassthrough;
import com.feedzai.fos.impl.weka.utils.WekaThreadSafeScorerPool;
import com.feedzai.fos.impl.weka.utils.pool.GenericObjectPoolConfig;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaScorerTest {
    private static final UUID testUUID = UUID.fromString("54c5f947-fb25-46cd-b3af-499376171053");

    public void poolCreationTest() throws FOSException {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".minIdle", 10);
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".maxActive", 10);
        configuration.setProperty(FosConfig.HEADER_LOCATION, "target/test-classes/models/threadsafe");
        configuration.setProperty(FosConfig.FACTORY_NAME, WekaManagerFactory.class.getName());

        WekaManagerConfig wekaManagerConfig = new WekaManagerConfig(new FosConfig(configuration));
        WekaManager wekaManager = new WekaManager(wekaManagerConfig);
        WekaScorer wekaScorer = wekaManager.getScorer();

        double[] score = wekaScorer.score(Lists.newArrayList(testUUID), new Object[]{1.5, 0, "gray", "positive"}).get(0);
        assertEquals(2, score.length);
        assertEquals(1.0, score[0] + score[1], 0.001);
        Assert.assertTrue(((Map<Integer, WekaThreadSafeScorer>) Whitebox.getInternalState(wekaScorer, "wekaThreadSafeScorers")).get(testUUID) instanceof WekaThreadSafeScorerPool);

        wekaManager.close();
    }

    @Test
    public void passthroughCreationTest() throws FOSException {
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".minIdle", 10);
        configuration.setProperty(GenericObjectPoolConfig.class.getName() + ".maxActive", 10);
        configuration.setProperty(FosConfig.HEADER_LOCATION, "target/test-classes/models/threadunsafe");
        configuration.setProperty(FosConfig.FACTORY_NAME, WekaManagerFactory.class.getName());

        WekaManagerConfig wekaManagerConfig = new WekaManagerConfig(new FosConfig(configuration));
        WekaManager wekaManager = new WekaManager(wekaManagerConfig);
        WekaScorer wekaScorer = wekaManager.getScorer();

        double[] score = wekaScorer.score(Lists.newArrayList(testUUID), new Object[]{1.5, 0, "gray", "positive"}).get(0);
        assertEquals(2, score.length);
        assertEquals(1.0, score[0] + score[1], 0.001);
        Assert.assertTrue(((Map<Integer, WekaThreadSafeScorer>) Whitebox.getInternalState(wekaScorer, "wekaThreadSafeScorers")).get(testUUID) instanceof WekaThreadSafeScorerPassthrough);

        wekaManager.close();
    }
}
