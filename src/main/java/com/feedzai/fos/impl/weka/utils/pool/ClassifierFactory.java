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

import com.feedzai.fos.common.validation.NotNull;
import com.feedzai.fos.impl.weka.exception.WekaClassifierException;
import com.feedzai.fos.impl.weka.utils.Cloner;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Classifier Factory.
 * This factory is used to backup the {@link org.apache.commons.pool.ObjectPool} object life cycle.
 *
 * @author Rafael Marmelo
 */
public class ClassifierFactory extends BasePoolableObjectFactory<Classifier> {
    private static final Logger logger = LoggerFactory.getLogger(ClassifierFactory.class);

    private Cloner<Classifier> cloner;

    /**
     * Create a new classifier factory using the given cloner.
     *
     * @param cloner the cloner that creates instances of the factory
     */
    public ClassifierFactory(Cloner<Classifier> cloner) {
        checkNotNull(cloner, "Cloner cannot be null");

        this.cloner = cloner;
    }



    /**
     * Make on object of the factory type.
     * <p/>
     * Uses the cloner to create a new instance.
     *
     * @return a new object of the specified type
     * @throws WekaClassifierException when the object could not be clonned
     */
    @Override
    @NotNull
    public Classifier makeObject() throws WekaClassifierException {
        logger.debug("Creating classifier");

        try {
            return cloner.get();
        } catch (IOException e) {
            throw new WekaClassifierException(e);
        } catch (ClassNotFoundException e) {
            throw new WekaClassifierException(e);
        }
    }
}
