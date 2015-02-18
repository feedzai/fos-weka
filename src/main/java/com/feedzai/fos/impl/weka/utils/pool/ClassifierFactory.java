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

import com.feedzai.fos.api.FOSException;
import com.feedzai.fos.api.ModelDescriptor;
import com.feedzai.fos.common.validation.NotNull;
import com.feedzai.fos.impl.weka.exception.WekaClassifierException;
import com.feedzai.fos.impl.weka.utils.Cloner;
import com.feedzai.fos.impl.weka.utils.pmml.PMMLConsumers;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Classifier Factory.
 * This factory is used to backup the {@link org.apache.commons.pool.ObjectPool} object life cycle.
 *
 * @author Rafael Marmelo
 */
public class ClassifierFactory extends BasePoolableObjectFactory<Classifier> {
    private static final Logger logger = LoggerFactory.getLogger(ClassifierFactory.class);

    /**
     * The {@link ModelDescriptor} representing the classifier.
     */
    private ModelDescriptor modelDescriptor;


    /**
     * Create a new classifier factory using the given cloner.
     *
     * @param modelDescriptor The {@link ModelDescriptor} representing the classifier.
     */
    public ClassifierFactory(ModelDescriptor modelDescriptor) {
        checkNotNull(modelDescriptor, "Model descriptor cannot be null");
        checkNotNull(modelDescriptor.getModelFilePath(), "The path cannot be null");

        File modelFile = new File(modelDescriptor.getModelFilePath());

        checkArgument(modelFile.exists(), "The model file must exist");
        checkArgument(modelFile.canRead(), "The model file must be readable.");

        this.modelDescriptor = modelDescriptor;
    }

    /**
     * Make on object of the factory type.
     * <p>
     * Reads the {@link Classifier} from file.
     *
     * @return a new classifier instance
     * @throws WekaClassifierException when the object could not be read from file.
     */
    @Override
    @NotNull
    public Classifier makeObject() throws WekaClassifierException {
        logger.debug("Creating classifier");

        File file = new File(modelDescriptor.getModelFilePath());

        checkArgument(file.exists(), "Source file '"+ file.getAbsolutePath() + "' must exist");

        switch (modelDescriptor.getFormat()) {
            case BINARY:
                try (FileInputStream fileInputStream = new FileInputStream(file);
                     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                    return (Classifier) objectInputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new WekaClassifierException(e);
                }
            case PMML:
                try {
                    return PMMLConsumers.consume(file);
                } catch (FOSException e) {
                    throw new WekaClassifierException("Failed to consume PMML file " + file.getAbsolutePath() + ".", e);
                }
            default:
                throw new WekaClassifierException("Unknown model type " + modelDescriptor.getFormat());
        }
    }
}
