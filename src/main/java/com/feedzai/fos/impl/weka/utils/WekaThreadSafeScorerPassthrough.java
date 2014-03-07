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
import com.feedzai.fos.impl.weka.utils.setter.InstanceSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instances;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a new scorer based on the weka library.
 * This should be used with only with weka classifiers that are thread safe!
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaThreadSafeScorerPassthrough implements WekaThreadSafeScorer {
    private final static Logger logger = LoggerFactory.getLogger(WekaThreadSafeScorerPassthrough.class);

    private WekaManagerConfig wekaManagerConfig;
    private Classifier classifier;
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
    public WekaThreadSafeScorerPassthrough(WekaModelConfig wekaModelConfig,WekaManagerConfig wekaManagerConfig) throws FOSException {
        checkNotNull(wekaModelConfig, "Model config cannot be null");
        checkNotNull(wekaManagerConfig, "Manager config cannot be null");
        checkNotNull(wekaModelConfig.getAttributess(), "Model instances fields cannot be null");
        checkArgument(wekaModelConfig.getAttributess().size() > 0, "Model must have at least one field");

        this.wekaManagerConfig = wekaManagerConfig;
        this.wekaModelConfig = wekaModelConfig;

        this.attributes = WekaUtils.instanceFields2Attributes(wekaModelConfig.getClassIndex(), this.wekaModelConfig.getAttributess());
        this.instanceSetters = WekaUtils.instanceFields2ValueSetters(wekaModelConfig.getAttributess(), InstanceType.SCORING);

        this.instances = new Instances(Integer.toString(this.wekaModelConfig.hashCode()), attributes, 0 /*this set is for scoring only*/);
        this.instances.setClassIndex(this.wekaModelConfig.getClassIndex());

        try {
            this.classifier = new Cloner<Classifier>(wekaModelConfig.getModelDescriptor()).get();
        } catch (Exception e) {
            throw new FOSException(e);
        }
    }

    /**
     * The the given <code>Object[]</code> with this scorer (thread safe!).
     * <p/>
     * The score in bound by the configuration <code>minScore</code> and <code>maxScore</code>.
     *
     * @param scorable the scorable data to score
     * @return the score value already bound the configuration range
     * @throws FOSException when classification was not possible
     */
    @Override
    public double[] score(Object[] scorable) throws FOSException {
        return WekaUtils.score(classifier, scorable, instanceSetters, instances, attributes);
    }

    @Override
    public Classifier getClassifier() throws FOSException {
        return classifier;
    }

    /**
     * Close the resources allocated with this scorer.
     */
    @Override
    public void close() {
        /* nothing to do */
    }
}
