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
package com.feedzai.fos.impl.weka.config;

import com.feedzai.fos.api.Attribute;
import com.feedzai.fos.api.FOSException;
import com.feedzai.fos.api.ModelConfig;
import com.feedzai.fos.common.validation.NotNull;
import com.feedzai.fos.impl.weka.utils.pool.GenericObjectPoolConfig;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents header that defines the schema of the machine learning model.
 *
 * @author Marco Jorge (marco.jorge@gmail.com)
 */
public class WekaModelConfig {
    /**
     * The property name of the classIndex.
     * <p/> The classIndex is an integer that indicates the position of the classified instance fields (0 based).
     */
    public static final String CLASS_INDEX = "classIndex";

    /**
     * The property name of the model file.
     * <p/> The model file is the serialized Classifier that will be used for classification.
     */
    public static final String MODEL_FILE = "model";


    /**
     * The property name of a boolean that indicates if the classifier is thread safe..
     * <p/> If the model is thread safe then @{WekaThreadSafeScorerPassthrough} will be used, else @{WekaThreadSafeScorerPool} will be used.
     */
    public static final String IS_CLASSIFIER_THREAD_SAFE = "isClassifierThreadSafe";

    /**
     * The property name that defines the weka classifier class (FQCN).
     */
    public static final String CLASSIFIER_IMPL = "classifierimpl";

    /**
     * The property name of the ID of the model.
     */
    public static final String ID = "id";

    /**
     * The property name of the model configuration.
     */
    public static final String CLASSIFIER_CONFIG = "config";

    private ModelConfig modelConfig;
    private WekaManagerConfig wekaManagerConfig;
    private int classIndex;
    private File model;
    private UUID id;
    private transient File header;
    private transient boolean dirty = true;
    private boolean classifierThreadSafe;
    private Configuration configuration;

    /**
     * Creates a new model from the given <code>ModelConfig</code> and <code>WekaManagerConfig</code>.
     * <p/>
     * From the <code>ModelConfig.properties</code> the parameters <code>MODEL_FILE</code>, <code>ID</code> and <code>CLASS_INDEX</code> are looked up.
     * If the <code>CLASS_INDEX</code> doesn't exist int he <code>ModelConfig</code>, the default value is used from <code>WekaManagerConfig</code>.
     *
     * @param modelConfig       the configuration with <code>MODEL_FILE</code>, <code>ID</code> and <code>CLASS_INDEX</code>
     * @param wekaManagerConfig the configuration with the default <code>CLASS_INDEX</code>
     */
    public WekaModelConfig(ModelConfig modelConfig, WekaManagerConfig wekaManagerConfig) throws FOSException {
        checkNotNull(modelConfig, "Model configuration cannot be null");
        checkNotNull(wekaManagerConfig, "Manager configuration cannot be null");

        this.modelConfig = modelConfig;
        this.wekaManagerConfig = wekaManagerConfig;

        parseModelConfig();
    }

    private void parseModelConfig() throws FOSException {
        configuration = new MapConfiguration((Map) modelConfig.getProperties());
        classIndex = this.modelConfig.getIntProperty(CLASS_INDEX, -1);
        if (classIndex < 0) {
            classIndex = this.modelConfig.getAttributes().size() - 1;
        }

        String modelFile = configuration.getString(MODEL_FILE);
        if (modelFile != null) {
            this.model = new File(modelFile);
        }

        classifierThreadSafe = configuration.getBoolean(IS_CLASSIFIER_THREAD_SAFE, false /* defaults to Pool implementation*/);

        String uuid = configuration.getString(ID);
        if (uuid != null) {
            this.id = UUID.fromString(uuid);
        }
    }


    /**
     * Returns the pool configuration of the scorer.
     *
     * @return a map from configuration key to configuration value
     */
    @NotNull
    public Map<Object, Object> getPoolConfiguration() {
        return ConfigurationConverter.getMap(configuration.subset(GenericObjectPoolConfig.class.getName()));
    }

    /**
     * Returns true, if and only if the underlying classifier implementation is thread safe.
     *
     * @return true if the implementation is thread safe
     */
    public boolean isClassifierThreadSafe() {
        return classifierThreadSafe;
    }

    /**
     * Gets a boolean indicating if this configuration has changed since the last save.
     *
     * @return true if non persisted changes have been mande
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets or clears the indicator if this configuration has been changed since the last save.
     *
     * @param dirty true if changes have been persisted to disk, false if changes have been made
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Gets the header file of the model.
     *
     * @return the header file
     */
    @NotNull
    public File getHeader() {
        return header;
    }

    /**
     * Sets the header file of the model.
     *
     * @param header the header file
     */
    public void setHeader(File header) {
        this.dirty = true;
        this.header = header;
    }

    /**
     * Gets the ID of the model.
     *
     * @return the ID of the model
     */
    @NotNull
    public UUID getId() {
        return id;
    }

    /**
     * Sets the ID of the model.
     *
     * @param id the ID
     */
    public void setId(UUID id) {
        this.dirty = true;
        this.id = id;

        this.modelConfig.setProperty(ID, id.toString());
    }

    /**
     * Gets the index of the class attribute.
     *
     * @return index value od the class attribute.
     */
    public int getClassIndex() {
        return this.classIndex;
    }
    /**
     * Gets the model file of the serialized classifier.
     *
     * @return the model file
     */
    @NotNull
    public File getModel() {
        return model;
    }

    /**
     * Sets the model file of the serialized classifier.
     *
     * @param model the model file
     */
    public void setModel(File model) {
        this.dirty = true;
        this.model = model;

        this.modelConfig.setProperty(MODEL_FILE, model.getAbsolutePath());
    }

    /**
     * Gets the instance fields of this configuration.
     *
     * @return the list of instance fields of this classifier
     */
    @NotNull
    public List<Attribute> getAttributess() {
        return this.modelConfig.getAttributes();
    }


    /**
     * Updates the underlying <code>ModelConfig</code> using <code>ModelConfig.update</code>.
     *
     * @param modelConfig the model config with the new settings
     */
    public void update(ModelConfig modelConfig) throws FOSException {
        checkNotNull(modelConfig);

        this.dirty = true;

        // The class index should be reset everytime a model config is updated.
        // TODO: Refactor the classIndex to be in ModelConfig.
        this.modelConfig.getProperties().remove(CLASS_INDEX);
        this.modelConfig.update(modelConfig);

        parseModelConfig();
    }


    /**
     * Gets the current and updated model config.
     *
     * @return the underlying model config
     */
    @NotNull
    public ModelConfig getModelConfig() {
        return modelConfig;
    }
}
