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
package com.feedzai.fos.impl.weka.utils.pool;

import org.apache.commons.pool.PoolableObjectFactory;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
@RunWith(PowerMockRunner.class)
public class AutoPopulateGenericObjectPoolTest {
    private PoolableObjectFactory<Object> objectFactory;

    @Before
    public void setup() throws Exception {
        objectFactory = EasyMock.createNiceMock(PoolableObjectFactory.class);
        EasyMock.expect(objectFactory.makeObject()).andReturn(null).times(10);
        PowerMock.replay(objectFactory);
    }

    @Test
    public void testAutoPopulateMinEqualsMax() throws Exception {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMinIdle(10);
        genericObjectPoolConfig.setMaxActive(10);
        genericObjectPoolConfig.setMaxIdle(-1);

        new AutoPopulateGenericObjectPool<Object>(objectFactory, genericObjectPoolConfig);

        PowerMock.verify(objectFactory);
    }

    @Test
    public void testAutoPopulateMinOverMax() throws Exception {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMinIdle(100);
        genericObjectPoolConfig.setMaxActive(10);
        genericObjectPoolConfig.setMaxIdle(-1);

        new AutoPopulateGenericObjectPool<Object>(objectFactory, genericObjectPoolConfig);

        PowerMock.verify(objectFactory);
    }

    @Test
    public void testAutoPopulateMinUnderMax() throws Exception {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMinIdle(10);
        genericObjectPoolConfig.setMaxActive(100);
        genericObjectPoolConfig.setMaxIdle(-1);

        new AutoPopulateGenericObjectPool<Object>(objectFactory, genericObjectPoolConfig);

        PowerMock.verify(objectFactory);
    }
}
