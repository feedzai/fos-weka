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
package weka.classifiers.trees;

import com.feedzai.fos.impl.weka.exception.PMMLConversionException;
import org.dmg.pmml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.RandomForestUtils;
import weka.core.*;
import weka.core.Attribute;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.feedzai.fos.impl.weka.utils.pmml.PMMLConversionCommons.*;

/**
 * A producer that converts a {@link weka.classifiers.trees.RandomForest} instance to PMML.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public class RandomForestPMMLProducer {

    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RandomForestPMMLProducer.class);

    /**
     * The algorithm used.
     */
    private static final String ALGORITHM_NAME = "weka:"+RandomForest.class.getName();

    /**
     * The model name.
     */
    private static final String MODEL_NAME = ALGORITHM_NAME+"_Model";


    /**
     * Converts the given {@link weka.classifiers.trees.RandomForest} instance to PMML and
     * saves the result in the given {@link File}.
     *
     * @param randomForestClassifier The {@link weka.classifiers.trees.RandomForest} instance to convert to PMML.
     * @param targetFile             The file where to save the resulting PMML.
     * @throws PMMLConversionException If if fails to convert the classifier.
     */
    public static void produce(RandomForest randomForestClassifier, File targetFile) throws PMMLConversionException {
        PMML pmml = produce(randomForestClassifier);
        try {
            IOUtil.marshal(pmml, targetFile);
        } catch (Exception e) {
            throw new PMMLConversionException("Failed to marshal the PMML to the given file.", e);
        }
    }

    /**
     * Converts the given {@link weka.classifiers.trees.RandomForest} instance to PMML and
     * saves the result in the given {@link File}.
     *
     * @param randomForestClassifier The {@link weka.classifiers.trees.RandomForest} instance to convert to PMML.
     * @return A {@link org.dmg.pmml.PMML} instance representing the PMML structure.
     */
    public static PMML produce(RandomForest randomForestClassifier) {
        // Get the Instances from the first tree in the forest.
        Classifier[] baggingClassifiers = RandomForestUtils.getBaggingClassifiers(randomForestClassifier.m_bagger);
        Instances data = ((RandomTree) baggingClassifiers[0]).m_Info;

        Header header = buildPMMLHeader("Weka RandomForest as PMML.");

        PMML pmml = new PMML(header, new DataDictionary(), "4.1");

        // Builds the PMML DataDictionary and MiningSchema elements.
        DataDictionary dataDictionary = new DataDictionary();
        MiningSchema miningSchema = new MiningSchema();

        for (int i = 0; i < data.numAttributes(); i++) {
            Attribute attribute = data.attribute(i);

            DataType fieldType;
            if (attribute.isNumeric()) {
                fieldType = DataType.DOUBLE;
            } else {
                fieldType = DataType.STRING;
            }

            DataField dataField = new DataField(new FieldName(attribute.name()), attribute.isNominal() ? OpType.CATEGORICAL : OpType.CONTINUOUS, fieldType);
            if (attribute.isNominal()) {
                Enumeration enumeration = attribute.enumerateValues();
                while (enumeration.hasMoreElements()) {
                    dataField.withValues(new Value(String.valueOf(enumeration.nextElement())));
                }
            }

            dataDictionary.withDataFields(dataField);

            MiningField miningField = new MiningField(new FieldName(attribute.name()));

            if (data.classIndex() == i) {
                miningField.withUsageType(FieldUsageType.PREDICTED);
            } else {
                miningField.withUsageType(FieldUsageType.ACTIVE);
            }
            miningSchema.withMiningFields(miningField);
        }

        pmml.withDataDictionary(dataDictionary);

        MiningModel miningModel = new MiningModel(miningSchema, MiningFunctionType.CLASSIFICATION);
        miningModel.withModelName(MODEL_NAME);

        pmml.withModels(miningModel);

        Segmentation segmentation = new Segmentation(MultipleModelMethodType.MAJORITY_VOTE);
        miningModel.withSegmentation(segmentation);

        int segmentId = 1;
        for (Classifier classifier : RandomForestUtils.getBaggingClassifiers(randomForestClassifier.m_bagger)) {
            Segment segment = buildSegment(miningSchema, segmentId++, (RandomTree) classifier);
            segmentation.withSegments(segment);
        }

        return pmml;
    }


    /**
     * Builds a {@link org.dmg.pmml.Segment PMML Segment} that contains the {@link org.dmg.pmml.TreeModel PMML TreeModel}
     * representing the given {@link weka.classifiers.trees.RandomTree Weka RandomTree}.
     *
     * @param miningSchema The {@link org.dmg.pmml.MiningSchema PMML MiningSchema} that lists fields as used in the model.
     * @param segmentId    The Id to given to the {@link org.dmg.pmml.Segment PMML Segment element}.
     * @param randomTree   The {@link weka.classifiers.trees.RandomTree Weka RandomTree} to be converted to a {@link org.dmg.pmml.TreeModel PMML TreeModel}.
     * @return The created {@link org.dmg.pmml.Segment PMML Segment}.
     */
    private static Segment buildSegment(MiningSchema miningSchema, int segmentId, RandomTree randomTree) {
        int rootNodeId = 1;

        Node rootNode = new Node().withId(String.valueOf(rootNodeId)).withPredicate(new True());
        TreeModel treeModel = new TreeModel(miningSchema, rootNode, MiningFunctionType.CLASSIFICATION).withAlgorithmName(ALGORITHM_NAME).withModelName(MODEL_NAME).withSplitCharacteristic(TreeModel.SplitCharacteristic.BINARY_SPLIT);

        buildTreeNode(randomTree, randomTree.m_Tree, rootNodeId, rootNode);

        Segment segment = new Segment();
        segment.withId(String.valueOf(segmentId));
        segment.withModel(treeModel);

        return segment;
    }

    /**
     * Builds a new {@link org.dmg.pmml.Node PMML Node} from the given {@link RandomTree.Tree Weka Tree Node}.
     *
     * @param tree           The {@link weka.classifiers.trees.RandomTree Weka RandomTree} being converted to a {@link org.dmg.pmml.PMML TreeModel}.
     * @param node           The Id to give to the generted {@link org.dmg.pmml.Node PMML Node}.
     * @param nodeId         The Id to give to the generted {@link org.dmg.pmml.Node PMML Node}.
     * @param parentPMMLNode The parent {@link org.dmg.pmml.Node PMML Node}.
     * @return The incremented Id given to recursively created {@link org.dmg.pmml.Node PMML Nodes}.
     */
    private static int buildTreeNode(RandomTree tree, RandomTree.Tree node, int nodeId, Node parentPMMLNode) {
        addScoreDistribution(parentPMMLNode, node.m_ClassDistribution, tree.m_Info);

        if (node.m_Attribute == -1) {
            // Leaf: Add the node's score.
            parentPMMLNode.withScore(leafScoreFromDistribution(node.m_ClassDistribution, tree.m_Info));
            return nodeId;
        }

        Attribute attribute = tree.m_Info.attribute(node.m_Attribute);

        if (attribute.isNominal()) {
            return buildNominalNode(tree, attribute, node, nodeId, parentPMMLNode);
        } else if (attribute.isNumeric()) {
            return buildNumericNode(tree, attribute, node, nodeId, parentPMMLNode);
        } else {
            throw new RuntimeException("Unsupported attribute type for: " + attribute);
        }
    }

    /**
     * Builds the {@link org.dmg.pmml.Node PMML Node} for a nominal attribute.
     * <p/>
     * In PMML these nodes are represented with multiple children, one for each of the attribute's values.
     * <p/>
     * For example, consider a nominal attribute, named "nominalAttribute", with values "cold", "hot" and "warm". In PMML this translates to:
     * <pre>
     *     {@code
     *       <Node id="2" score="1">
     *         <SimplePredicate field="nominalAttribute" operator="equal" value="cold"/>
     *       </Node>
     *       <Node id="3" score="0">
     *         <SimplePredicate field="nominalAttribute" operator="equal" value="hot"/>
     *       </Node>
     *       <Node id="4" score="1.5">
     *         <SimplePredicate field="nominalAttribute" operator="equal" value="warm"/>
     *       </Node>
     *     }
     * </pre>
     *
     * @param tree           The {@link weka.classifiers.trees.RandomTree Weka RandomTree} being converted to a {@link org.dmg.pmml.PMML TreeModel}.
     * @param attribute      The {@link weka.core.Attribute} to which the node to build refers to.
     * @param node           The {@link weka.classifiers.trees.RandomTree.Tree Weka RandomTree node} we are converting to PMML.
     * @param nodeId         The Id to give to the generted {@link org.dmg.pmml.Node PMML Node}.
     * @param parentPMMLNode The parent {@link org.dmg.pmml.Node PMML Node}.
     * @return The incremented Id given to recursively created {@link org.dmg.pmml.Node PMML Nodes}.
     */
    private static int buildNominalNode(RandomTree tree, Attribute attribute, RandomTree.Tree node, int nodeId, Node parentPMMLNode) {
        List<Object> values = new ArrayList<>();
        Enumeration enumeration = attribute.enumerateValues();
        while (enumeration.hasMoreElements()) {
            values.add(enumeration.nextElement());
        }

        assert values.size() == node.m_Successors.length : "Number of successors expected to be the same as the number of attribute values";

        List<Node> children = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);

            SimplePredicate predicate = new SimplePredicate(new FieldName(attribute.name()), SimplePredicate.Operator.EQUAL).withValue(String.valueOf(value));
            Node child = new Node().withId(String.valueOf(++nodeId)).withPredicate(predicate);

            nodeId = buildTreeNode(tree, node.m_Successors[i], nodeId, child);

            // Training proportion extension.
            child.withExtensions(new Extension().withName(TRAINING_PROPORTION_ELEMENT).withValue(String.valueOf(node.m_Prop[i])));

            children.add(child);
        }

        parentPMMLNode.withNodes(children);

        return nodeId;
    }

    /**
     * Builds the {@link org.dmg.pmml.Node PMML Node} for a numeric attribute.
     * <p/>
     * In PMML these nodes are represented having two children, each with a predicate that checks the node's split point.
     * <p/>
     * For example, consider a numeric attribute, named "numericAttribute", with a split point of 2.5 and two leaf nodes. In PMML this translates to:
     * <pre>
     *     {@code
     *       <Node id="2" score="1">
     *         <SimplePredicate field="numericAttribute" operator="lessThan" value="2.5"/>
     *       </Node>
     *       <Node id="3" score="0">
     *         <SimplePredicate field="numericAttribute" operator="greaterOrEqual" value="2.5"/>
     *       </Node>
     *     }
     * </pre>
     *
     * @param tree           The {@link weka.classifiers.trees.RandomTree Weka RandomTree} being converted to a {@link org.dmg.pmml.PMML TreeModel}.
     * @param attribute      The {@link weka.core.Attribute} to which the node to build refers to.
     * @param node           The {@link weka.classifiers.trees.RandomTree.Tree Weka RandomTree node} we are converting to PMML.
     * @param nodeId         The Id to give to the generted {@link org.dmg.pmml.Node PMML Node}.
     * @param parentPMMLNode The parent {@link org.dmg.pmml.Node PMML Node}.
     * @return The incremented Id given to recursively created {@link org.dmg.pmml.Node PMML Nodes}.
     */
    private static int buildNumericNode(RandomTree tree, Attribute attribute, RandomTree.Tree node, int nodeId, Node parentPMMLNode) {
        SimplePredicate predicateLo = new SimplePredicate(new FieldName(attribute.name()), SimplePredicate.Operator.LESS_THAN).withValue(String.valueOf(node.m_SplitPoint));
        SimplePredicate predicateHi = new SimplePredicate(new FieldName(attribute.name()), SimplePredicate.Operator.GREATER_OR_EQUAL).withValue(String.valueOf(node.m_SplitPoint));

        Node nodeLo = new Node().withId(String.valueOf(++nodeId));
        nodeLo.withPredicate(predicateLo);

        nodeId = buildTreeNode(tree, node.m_Successors[0], nodeId, nodeLo);

        Node nodeHi = new Node().withId(String.valueOf(++nodeId));
        nodeHi.withPredicate(predicateHi);

        nodeId = buildTreeNode(tree, node.m_Successors[1], nodeId, nodeHi);

        // Training proportion extension.
        nodeLo.withExtensions(new Extension().withName(TRAINING_PROPORTION_ELEMENT).withValue(String.valueOf(node.m_Prop[0])));
        nodeHi.withExtensions(new Extension().withName(TRAINING_PROPORTION_ELEMENT).withValue(String.valueOf(node.m_Prop[1])));

        parentPMMLNode.withNodes(nodeLo, nodeHi);

        return nodeId;
    }
}
