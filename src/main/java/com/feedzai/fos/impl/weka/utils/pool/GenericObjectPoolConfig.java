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

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Configuration of a @{see GenericoObjectPool}.
 * Required for BeanUtils.populate():
 */
public class GenericObjectPoolConfig extends GenericObjectPool.Config {
    public void setMinIdle(int minIdle) {
        super.minIdle = minIdle;
    }

    public void setLifo(boolean lifo) {
        super.lifo = lifo;
    }

    public void setMaxActive(int maxActive) {
        super.maxActive = maxActive;
    }

    public void setMaxIdle(int maxIdle) {
        super.maxIdle = maxIdle;
    }

    public void setMaxWait(long maxWait) {
        super.maxWait = maxWait;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        super.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        super.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        super.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        super.testOnBorrow = testOnBorrow;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        super.testOnReturn = testOnReturn;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        super.testWhileIdle = testWhileIdle;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        super.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public void setWhenExhaustedAction(byte whenExhaustedAction) {
        super.whenExhaustedAction = whenExhaustedAction;
    }
}
