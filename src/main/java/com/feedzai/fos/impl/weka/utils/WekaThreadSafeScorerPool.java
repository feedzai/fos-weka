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

import com.feedzai.fos.api.FOSException;
import com.feedzai.fos.api.InstanceType;
import com.feedzai.fos.impl.weka.config.WekaManagerConfig;
import com.feedzai.fos.impl.weka.config.WekaModelConfig;
import com.feedzai.fos.impl.weka.utils.pool.AutoPopulateGenericObjectPool;
import com.feedzai.fos.impl.weka.utils.pool.ClassifierFactory;
import com.feedzai.fos.impl.weka.utils.pool.GenericObjectPoolConfig;
import com.feedzai.fos.impl.weka.utils.setter.InstanceSetter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instances;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a new scorer based on the weka library.
 * Because weka classifiers are not guaranteed to be thread sate, this class wraps around the actual weka library using a pool of thread-unsafe calssifiers.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaThreadSafeScorerPool implements WekaThreadSafeScorer {
    private final static Logger logger = LoggerFactory.getLogger(WekaThreadSafeScorerPool.class);

    private WekaManagerConfig wekaManagerConfig;
    private ObjectPool<Classifier> pool;
    private GenericObjectPool.Config poolConfig = new GenericObjectPoolConfig();
    private WekaModelConfig wekaModelConfig;
    private FastVector attributes;
    private Instances instances;
    private InstanceSetter[] instanceSetters;

    /**
     * Creates a new thread safe scorer from the given configuration parameters.
     *
     * @param wekaModelConfig   the configuration of the model
     * @param wekaManagerConfig the global configurations
     * @throws FOSException when the underlying classifier could not be instantiated
     */
    public WekaThreadSafeScorerPool(WekaModelConfig wekaModelConfig,WekaManagerConfig wekaManagerConfig) throws FOSException {
        checkNotNull(wekaModelConfig, "Model config cannot be null");
        checkNotNull(wekaManagerConfig, "Manager config cannot be null");
        checkNotNull(wekaModelConfig.getAttributess(), "Model instances fields cannot be null");
        checkArgument(wekaModelConfig.getAttributess().size() > 0, "Model must have at least one field");

        this.poolConfig = new GenericObjectPoolConfig();
        this.wekaManagerConfig = wekaManagerConfig;
        this.wekaModelConfig = wekaModelConfig;


        this.attributes = WekaUtils.instanceFields2Attributes(wekaModelConfig.getClassIndex(), wekaModelConfig.getAttributess());
        this.instanceSetters = WekaUtils.instanceFields2ValueSetters(wekaModelConfig.getAttributess(), InstanceType.SCORING);

        this.instances = new Instances(Integer.toString(this.wekaModelConfig.hashCode()), attributes, 0 /*this set is for scoring only*/);
        this.instances.setClassIndex(wekaModelConfig.getClassIndex());
        try {
            BeanUtils.populate(poolConfig, this.wekaModelConfig.getPoolConfiguration());
            this.pool = new AutoPopulateGenericObjectPool<>(new ClassifierFactory(wekaModelConfig.getModel()), poolConfig);
        } catch (Exception e) {
            throw new FOSException(e);
        }
    }

    /**
     * The the given <code>Object[]</code> with this scorer (thread safe!).
     * <p/>
     * The score in bound by the configuration <code>minScore</code> and <code>maxScore</code>.
     *
     *
     * @param scorable the scorable data to score
     * @return the score value already bound the configuration range
     * @throws FOSException when classification was not possible
     */
    @Override
    public double[] score(Object[] scorable) throws FOSException {
        /* the pool can change while this is processing (reload) so assign a local variable */
        final ObjectPool<Classifier> localPool = pool;

        Classifier classifier = null;
        try {
            classifier = localPool.borrowObject();

            return WekaUtils.score(classifier, scorable, instanceSetters, instances, attributes);
        } catch (Exception e) {
            throw new FOSException(e);
        } finally {
            returnObject(localPool, classifier);
        }
    }

    private void returnObject(ObjectPool<Classifier> pool, Classifier object) {
        try {
            pool.returnObject(object);
        } catch (Exception e) {
            logger.error("Could not return object to pool", e);
        }
    }

    /**
     * Close the resources allocated with this scorer.
     */
    @Override
    public void close() {
        try {
            pool.close();
        } catch (Exception e) {
            logger.error("Could not close pool", e);
        }
    }

}
