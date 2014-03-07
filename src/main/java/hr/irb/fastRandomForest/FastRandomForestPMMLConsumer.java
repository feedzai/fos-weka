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
package hr.irb.fastRandomForest;

import com.feedzai.fos.impl.weka.exception.PMMLConversionException;
import org.dmg.pmml.*;
import weka.classifiers.Classifier;
import weka.classifiers.RandomForestUtils;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import static com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.*;

/**
 * A consumer that converts PMML to a {@link hr.irb.fastRandomForest.FastRandomForest} instance.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public class FastRandomForestPMMLConsumer {

    /**
     * Builds a new {@link hr.irb.fastRandomForest.FastRandomForest} from the given PMML String.
     * <p/>
     * The given {@code pmmlString} should be a valid PMML.
     *
     * @param pmmlString A String representing the PMML that is to be converted to a {@link hr.irb.fastRandomForest.FastRandomForest}.
     * @return A new {@link hr.irb.fastRandomForest.FastRandomForest}.
     * @throws Exception If it fails to convert the given PMML to a FastRandomForest.
     */
    public static FastRandomForest consume(String pmmlString) throws PMMLConversionException {
        PMML pmml = null;
        try {
            pmml = IOUtil.unmarshal(new ByteArrayInputStream(pmmlString.getBytes()));
        } catch (Exception e) {
            throw new PMMLConversionException("Failed to unmarshal PMML from string. Make sure it is a valid PMML.", e);
        }
        return consume(pmml);
    }

    /**
     * Builds a new {@link hr.irb.fastRandomForest.FastRandomForest} from the given file.
     *
     * @param file The file with the PMML representation of the classifier.
     * @return A new {@link hr.irb.fastRandomForest.FastRandomForest}.
     * @throws Exception If it fails to convert the given file to a FastRandomForest.
     */
    public static FastRandomForest consume(File file) throws PMMLConversionException {
        PMML pmml = null;
        try {
            pmml = IOUtil.unmarshal(file);
        } catch (Exception e) {
            throw new PMMLConversionException("Failed to unmarshal PMML file '" + file + "'. Make sure the file is a valid PMML.", e);
        }

        return consume(pmml);
    }

    /**
     * Builds a new {@link hr.irb.fastRandomForest.FastRandomForest} from the given {@link org.dmg.pmml.PMML}.
     *
     * @param pmml The {@link org.dmg.pmml.PMML} which is to be converted to a {@link hr.irb.fastRandomForest.FastRandomForest}.
     * @return A new {@link hr.irb.fastRandomForest.FastRandomForest}.
     * @throws Exception If it fails to convert the given PMML to a FastRandomForest.
     */
    public static FastRandomForest consume(PMML pmml) throws PMMLConversionException {
        MiningModel miningModel = getMiningModel(pmml);
        List<Segment> segments = miningModel.getSegmentation().getSegments();

        int m_numTrees = segments.size();

        Instances instances = buildInstances(pmml.getDataDictionary());
        instances.setClassIndex(getClassIndex(instances, miningModel.getMiningSchema()));

        FastRandomForest randomForest = new FastRandomForest();

        FastRandomTree randomTree = new FastRandomTree();
        randomTree.m_MotherForest = randomForest;

        randomForest.m_bagger = new FastRfBagging();
        randomForest.m_bagger.setNumIterations(m_numTrees);
        randomForest.m_bagger.setClassifier(randomTree);
        randomForest.m_Info = instances;

        try {
            RandomForestUtils.setupBaggingClassifiers(randomForest.m_bagger);
        } catch (Exception e) {
            throw new PMMLConversionException("Failed to initialize bagging classifiers.", e);
        }

        Classifier[] baggingClassifiers = RandomForestUtils.getBaggingClassifiers(randomForest.m_bagger);

        for (int i = 0; i < baggingClassifiers.length; i++) {
            baggingClassifiers[i] = buildRandomTree(randomForest, (TreeModel) segments.get(i).getModel());
        }

        return randomForest;
    }

    /**
     * Builds a new {@link weka.classifiers.trees.RandomTree Weka RandomTree} from the given {@link org.dmg.pmml.TreeModel PMML TreeModel}.
     *
     * @param motherForest The {@link FastRandomForest} which is the mother of the tree to build..
     * @param treeModel    The {@link org.dmg.pmml.TreeModel PMML TreeModel} which is to be converted to a {@link weka.classifiers.trees.RandomTree Weka RandomTree}.
     * @return The same {@code root} instance.
     */
    private static FastRandomTree buildRandomTree(FastRandomForest motherForest, TreeModel treeModel) {
        return buildRandomTreeNode(motherForest, treeModel.getNode());
    }

    /**
     * Builds a {@link weka.classifiers.trees.RandomTree.Tree Weka RandomTree} node
     * represented by the given {@link org.dmg.pmml.Node PMML node}.
     *
     * @param motherForest The {@link FastRandomForest} which is the mother of the tree node to build..
     * @param pmmlNode     The {@link org.dmg.pmml.PMML PMML node} to be converted to a {@link weka.classifiers.trees.RandomTree.Tree Weka RandomTree} node.
     * @return A new {@link weka.classifiers.trees.RandomTree.Tree Weka RandomTree} node.
     */
    private static FastRandomTree buildRandomTreeNode(FastRandomForest motherForest, Node pmmlNode) {
        FastRandomTree treeNode = new FastRandomTree();
        treeNode.m_MotherForest = motherForest;
        //Set the class distribution.
        treeNode.m_ClassProbs = getClassDistribution(pmmlNode);

        Instances instances = motherForest.m_Info;

        boolean isLeaf = pmmlNode.getNodes().size() == 0;

        if (!isLeaf) {
            List<Node> children = pmmlNode.getNodes();

            String attributeName = ((SimplePredicate) children.get(0).getPredicate()).getField().getValue();
            Attribute attribute = instances.attribute(attributeName);

            treeNode.m_Attribute = attribute.index();

            if (attribute.isNumeric()) {
                /*
                  If the node is numeric, get its two child nodes and covert them into a new RandomTree.Tree node.
                  For example, consider the following PPML Node:

                     <Node id="2">
                       <SimplePredicate field="petal_length" operator="lessThan" value="5.05"/>
                       <Node id="3" score="Iris-setosa">
                         <SimplePredicate field="petal_length" operator="lessThan" value="2.95"/>
                       </Node>
                       <Node id="4" score="Iris-versicolor">
                         <SimplePredicate field="petal_length" operator="greaterOrEqual" value="2.95"/>
                       </Node>
                     </Node>

                  We'll grab the two child nodes and their value will be the split point of the current RandomTree.Tree node.
                */
                assert children.size() == 2 : "Numeric attributes must have exactly 2 children";

                Node left = children.get(0);
                Node right = children.get(1);

                Predicate leftPredicate = left.getPredicate();
                Predicate rightPredicate = right.getPredicate();

                assert leftPredicate instanceof SimplePredicate && leftPredicate.getClass().equals(rightPredicate.getClass()) : "Numeric attribute's nodes must have the same simple predicate.";

                double splitPoint = Double.valueOf(((SimplePredicate) leftPredicate).getValue());

                treeNode.m_SplitPoint = splitPoint;
                treeNode.m_Successors = new FastRandomTree[]{buildRandomTreeNode(motherForest, left), buildRandomTreeNode(motherForest, right)};
                treeNode.m_Prop = new double[]{getNodeTrainingProportion(left), getNodeTrainingProportion(right)};
            } else if (attribute.isNominal()) {

                treeNode.m_Successors = new FastRandomTree[children.size()];
                treeNode.m_Prop = new double[treeNode.m_Successors.length];

                for (int i = 0; i < children.size(); i++) {
                    Node child = children.get(i);

                    SimplePredicate predicate = (SimplePredicate) child.getPredicate();
                    int valueIndex = attribute.indexOfValue(predicate.getValue());

                    treeNode.m_Successors[valueIndex] = buildRandomTreeNode(motherForest, child);
                    treeNode.m_Prop[valueIndex] = getNodeTrainingProportion(child);
                }
            } else {
                throw new RuntimeException("Attribute type not supported: " + attribute);
            }
        }

        return treeNode;
    }
    /**
     * Retrieves the class distribution for the given node.
     * <p/>
     * We represent this in PMML as the confidence of a list of {@link org.dmg.pmml.ScoreDistribution} elements.
     *
     * @param node The {@link org.dmg.pmml.Node PMML node} for which to retrieve the class distribution.
     * @return A array with the class distribution for the given node.
     */
    private static double[] getClassDistribution(Node node) {
        List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

        double[] classDistribution = null;

        if (!scoreDistributions.isEmpty()) {
            classDistribution = new double[scoreDistributions.size()];

            for (int i = 0; i < scoreDistributions.size(); i++) {
                classDistribution[i] = scoreDistributions.get(i).getConfidence();
            }
        }

        return classDistribution;
    }

    /**
     * Retrieves the training proportion of the given node.
     * <p/>
     * We represent this value in PMML as an {@link org.dmg.pmml.Extension} element of the node.
     *
     * @param node The {@link org.dmg.pmml.Node PMML node} for which to retrieve the training proportion.
     * @return The training proportion of the given node.
     */
    private static double getNodeTrainingProportion(Node node) {
        for (Extension extension : node.getExtensions()) {
            if (TRAINING_PROPORTION_ELEMENT.equals(extension.getName())) {
                return Double.valueOf(extension.getValue());
            }
        }

        return 0;
    }
}
