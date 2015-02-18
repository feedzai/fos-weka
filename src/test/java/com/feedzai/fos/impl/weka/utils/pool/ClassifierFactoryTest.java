/*
 * $#
 * FOS Weka
 *  
 * Copyright (C) 2015 Feedzai SA
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

import com.feedzai.fos.api.ModelDescriptor;
import com.feedzai.fos.impl.weka.exception.WekaClassifierException;
import org.junit.Test;
import weka.classifiers.Classifier;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link ClassifierFactory}.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.6.0
 */
public class ClassifierFactoryTest {

    /**
     * Tests a Classifier Factory that handles classifiers stored in binary format.
     */
    @Test
    public void testMakeObjectBinary() throws WekaClassifierException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");

        ClassifierFactory classifierFactory = new ClassifierFactory(descriptor);

        Classifier classifier = classifierFactory.makeObject();

        assertNotNull("Read classifier cannot be null.", classifier);
    }

    /**
     * Tests a Classifier Factory that handles classifiers stored in PMML format.
     */
    @Test
    public void testMakeObjectPMML() throws WekaClassifierException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.PMML, "target/test-classes/models/iris.pmml");

        ClassifierFactory classifierFactory = new ClassifierFactory(descriptor);

        Classifier classifier = classifierFactory.makeObject();

        assertNotNull("Read classifier cannot be null.", classifier);
    }
}
