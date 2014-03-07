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

import com.feedzai.fos.api.ModelDescriptor;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import weka.classifiers.Classifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class ClonerTest {
    @Test
    public void fromObjectTest() throws IOException, ClassNotFoundException {
        ArrayList<String> serializable = new ArrayList<String>();
        serializable.add("1");

        Cloner<ArrayList<String>> cloner = new Cloner<ArrayList<String>>(serializable);
        ArrayList<String> serializable1 = cloner.get();
        ArrayList<String> serializable2 = cloner.get();

        Assert.assertNotSame(serializable, serializable1);
        Assert.assertNotSame(serializable1, serializable2);
    }

    @Test
    public void fromFileTest() throws IOException, ClassNotFoundException {
        ModelDescriptor descriptor = new ModelDescriptor(ModelDescriptor.Format.BINARY, "target/test-classes/models/test.model");

        Cloner<Classifier> cloner = new Cloner<Classifier>(descriptor);
        Classifier serializable1 = cloner.get();
        Classifier serializable2 = cloner.get();

        Assert.assertNotSame(serializable1, serializable2);
    }

    @Test
    public void fromByteArrayTest() throws IOException, ClassNotFoundException {
        Cloner<Classifier> cloner = new Cloner<Classifier>(FileUtils.readFileToByteArray(new File("target/test-classes/models/test.model")));
        Classifier serializable1 = cloner.get();
        Classifier serializable2 = cloner.get();

        Assert.assertNotSame(serializable1, serializable2);
    }
}
