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

import com.feedzai.fos.api.*;
import com.feedzai.fos.common.validation.NotNull;
import com.feedzai.fos.common.validation.Nullable;
import com.feedzai.fos.impl.weka.exception.Data2ConfigurationMismatch;
import com.feedzai.fos.impl.weka.exception.WekaClassifierException;
import com.feedzai.fos.impl.weka.utils.setter.InstanceSetter;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities to map from weka specific objects to classification-api objects.
 * <p/>
 * These utilities are centralized where for easier maintenance.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaUtils {

    /**
     * Converts the given instance fields into a fast vector with <code>Attributes</code>.
     *
     * @param classIndex     classifier index
     * @param instanceFields the list of instance fields that will generate the <code>Attributes</code>
     * @return a vector with one <code>Attribute</code> for each instanceFields (in the same order).
     */
    @NotNull
    public static FastVector instanceFields2Attributes(int classIndex, List<? extends Attribute> instanceFields) throws FOSException {
        checkNotNull(instanceFields, "Instance fields cannot be null");

        FastVector result = new FastVector(instanceFields.size());
        classIndex = classIndex == -1 ? instanceFields.size() - 1 : classIndex;

        int idx = 0;
        for (Attribute instanceField : instanceFields) {
            weka.core.Attribute attribute;

            Class<?> type = instanceField.getClass();
            if (type == CategoricalAttribute.class) {
                CategoricalAttribute categorical = (CategoricalAttribute) instanceField;
                if (idx == classIndex) {
                    categorical.setClass();
                }
                List<String> instances = categorical.getCategoricalInstances();


                FastVector categoricalInstances = new FastVector(instances.size());
                for (String categoricalInstance : instances) {
                    categoricalInstances.addElement(categoricalInstance);
                }
                // Default values should only be set for features
                attribute = new weka.core.Attribute(instanceField.getName(), categoricalInstances, idx);

            } else if (type == NumericAttribute.class) {
                attribute = new weka.core.Attribute(instanceField.getName(), idx);
            } else {
                throw new FOSException("Unknown instance class");
            }
            result.addElement(attribute);
            idx++;
        }

        return result;
    }

    /**
     * Converts from the received Object[] to a weka <code>Instance</code>.
     *
     * @param data       the Object[] to convert (the scorable)
     * @param setters    the setters that will copy the data to the instance
     * @param attributes the list of attributes to copy over
     * @return an <code>Instance</code> with all data elements copied over from the Object[]
     * @throws WekaClassifierException when the setters and the data do not have the same size (more data than setters, or more setters than data)
     */

    public static Instance objectArray2Instance(Object[] data, InstanceSetter[] setters, FastVector attributes) throws FOSException {
        checkNotNull(data, "Instance cannot be null");
        checkNotNull(attributes, "Attributes cannot be null");
        checkNotNull(setters, "Setters cannot be null");

        Instance instance = new Instance(attributes.size());

        if (data.length == setters.length) {
            for (int idx = 0; idx < data.length; idx++) {
                if (data[idx] != null) {
                    setters[idx].set(instance, (weka.core.Attribute) attributes.elementAt(idx), data[idx].toString());
                } else {
                    instance.setMissing(idx);
                }
            }
        } else {
            throw new Data2ConfigurationMismatch(String.format("Data is not the same size as configured attributes (expected data size '%s' but was '%s')", setters.length, data.length));
        }

        return instance;
    }

    /**
     * Converts from <code>InstanceFields</code> to <code>InstanceSetters</code> (required for scorable manipulation).
     *
     * @param instanceFields The instance fields for which to create the converters
     * @param type           The type of instance to handle.
     * @return an array of <code>InstanceSetter</code> with one <code>InstanceSetter</code> for each <code>InstanceField</code>.
     */
    @NotNull
    public static InstanceSetter[] instanceFields2ValueSetters(final List<? extends Attribute> instanceFields, final InstanceType type) throws FOSException {
        InstanceSetter[] instanceSetters = new InstanceSetter[instanceFields.size()];

        for (int idx = 0; idx < instanceFields.size(); idx++) {
            final Attribute att = instanceFields.get(idx);
            instanceSetters[idx] = new InstanceSetter() {
                @Override
                public void set(Instance instance, weka.core.Attribute attribute, Object value) throws FOSException {
                    instance.setValue(attribute, att.parse(value, type));
                }
            };
        }
        return instanceSetters;
    }

    public static double[] score(Classifier classifier, Object[] scorable, InstanceSetter[] instanceSetters, Instances instances, FastVector attributes) throws FOSException {
        try {
            Instance instance = WekaUtils.objectArray2Instance(scorable, instanceSetters, attributes);
            instance.setDataset(instances);

            return classifier.distributionForInstance(instance);
        } catch (Exception e) {
            throw new FOSException(e.getMessage(), e);
        }
    }

    /**
     * Closes the given scorer (check for null first).
     *
     * @param wekaThreadSafeScorer the scorer to close.
     */
    public static void closeSilently(@Nullable WekaThreadSafeScorer wekaThreadSafeScorer) {
        if (wekaThreadSafeScorer != null) {
            wekaThreadSafeScorer.close();
        }
    }


}
