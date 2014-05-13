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

import com.feedzai.fos.api.CategoricalAttribute;
import com.feedzai.fos.impl.weka.utils.Cloner;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;

/**
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class CreateTestModel {
    public static void main(String[] args) throws Exception {
        // Declare two numeric attributes
        Attribute Attribute1 = new Attribute("firstNumeric");
        Attribute Attribute2 = new Attribute("secondNumeric");

        // Declare a nominal attribute along with its values
        FastVector fvNominalVal = new FastVector(4);
        fvNominalVal.addElement("blue");
        fvNominalVal.addElement("gray");
        fvNominalVal.addElement("black");
        Attribute Attribute3 = new Attribute("aNominal", fvNominalVal);

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("positive");
        fvClassVal.addElement("negative");
        Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

        // Declare the feature vector
        FastVector fvWekaAttributes = new FastVector(4);
        fvWekaAttributes.addElement(Attribute1);
        fvWekaAttributes.addElement(Attribute2);
        fvWekaAttributes.addElement(Attribute3);
        fvWekaAttributes.addElement(ClassAttribute);

        // Create an empty training set
        Instances isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
        // Set class index
        isTrainingSet.setClassIndex(3);

        // Create the instance
        Instance iExample1 = new Instance(4);
        iExample1.setValue((Attribute)fvWekaAttributes.elementAt(0), 1.0);
        iExample1.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);
        iExample1.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
        iExample1.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");

        Instance iExample2 = new Instance(4);
        iExample2.setValue((Attribute)fvWekaAttributes.elementAt(0), 2.0);
        iExample2.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);
        iExample2.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
        iExample2.setValue((Attribute)fvWekaAttributes.elementAt(3), "negative");

        Instance iExample3 = new Instance(4);
        iExample3.setValue((Attribute)fvWekaAttributes.elementAt(0), 0.9);
        iExample3.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);
        iExample3.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
        iExample3.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");

        // add the instance
        isTrainingSet.add(iExample1);
        isTrainingSet.add(iExample2);
        isTrainingSet.add(iExample3);

        // Create a naïve bayes classifier
        Classifier cModel = new NaiveBayes();
        cModel.buildClassifier(isTrainingSet);

        Cloner<Classifier> cloner = new Cloner<>(cModel);
        cloner.write(new File("src/test/resources/models/test.model"));
    }
}
