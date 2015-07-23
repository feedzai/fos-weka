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

import com.feedzai.fos.api.FOSException;
import com.feedzai.fos.api.Scorer;
import com.feedzai.fos.common.validation.NotNull;
import com.feedzai.fos.impl.weka.config.WekaManagerConfig;
import com.feedzai.fos.impl.weka.config.WekaModelConfig;
import com.feedzai.fos.impl.weka.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of the classification-api that supports multiple simultaneous classifiers (thread safe!).
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaScorer implements Scorer {
    private static final Logger logger = LoggerFactory.getLogger(WekaScorer.class);

    private Map<UUID, WekaThreadSafeScorer> wekaThreadSafeScorers = new HashMap<>();
    private ExecutorService executorService;
    private ReentrantReadWriteLock reloadModelsLock = new ReentrantReadWriteLock(true /* fair */);
    private WekaManagerConfig wekaManagerConfig;

    private WekaThreadSafeScorer getScorer(UUID modelId) throws FOSException {
        WekaThreadSafeScorer wekaThreadSafeScorer = wekaThreadSafeScorers.get(modelId);
        if (wekaThreadSafeScorer == null) {
            logger.error("No model with ID '{}'", modelId);
            throw new FOSException("No model with ID " + modelId);
        }
        return wekaThreadSafeScorer;
    }

    private <T> T getFuture(Future<T> future, UUID modelId) throws FOSException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Could not score on model '{}'", modelId, e);
            throw new FOSException(e);
        } catch (ExecutionException e) {
            logger.error("Could not score on model '{}'", modelId, e);
            throw new FOSException(e);
        }
    }

    /**
     * Creates a new scorer for the models identified int he configuration.
     *
     * <p>If loading of a model was not possible, this logs a message but continues to load other models and does not throw any exception.
     *
     * @param modelConfigs      the list of models to instantiate
     * @param wekaManagerConfig the global configuration
     */
    public WekaScorer(Map<UUID, WekaModelConfig> modelConfigs, WekaManagerConfig wekaManagerConfig) {
        checkNotNull(modelConfigs, "Model configuration map cannot be null");
        checkNotNull(wekaManagerConfig, "Manager config cannot be null");

        this.wekaManagerConfig = wekaManagerConfig;

        for (Map.Entry<UUID, WekaModelConfig> wekaModelConfigEntry : modelConfigs.entrySet()) {
            try {
                if (wekaModelConfigEntry.getValue().isClassifierThreadSafe()) {
                    wekaThreadSafeScorers.put(wekaModelConfigEntry.getValue().getId(), new WekaThreadSafeScorerPassthrough(wekaModelConfigEntry.getValue(), wekaManagerConfig));
                } else {
                    wekaThreadSafeScorers.put(wekaModelConfigEntry.getValue().getId(), new WekaThreadSafeScorerPool(wekaModelConfigEntry.getValue(), wekaManagerConfig));
                }
            } catch (Exception e) {
                logger.error("Could not load from '{}' (continuing to load others)", wekaModelConfigEntry.getKey(), e);
            }
        }
        this.executorService = Executors.newFixedThreadPool(wekaManagerConfig.getThreadPoolSize());
    }


    @Override
    public void close() {
        try {
            reloadModelsLock.writeLock().lock();

            executorService.shutdown();

            for (WekaThreadSafeScorer wekaThreadSafeScorer : this.wekaThreadSafeScorers.values()) {
                if (wekaThreadSafeScorer != null) {
                    wekaThreadSafeScorer.close();
                }
            }
        } finally {
            reloadModelsLock.writeLock().unlock();
        }
    }

    /**
     * Score the <code>scorable</code> for each model ID identified by <code>modelIds</code>.
     *
     * <p> If multiple models are given as parameters, they will be scored in parallel.
     *
     * @param modelIds the list of models to score
     * @param scorable the item to score
     * @return a List of scores with the same order and the received modelIds
     * @throws FOSException when classification was not possible
     */
    @Override
    @NotNull
    public List<double[]> score(List<UUID> modelIds, Object[] scorable) throws FOSException {
        checkNotNull(modelIds, "Models to score cannot be null");
        checkNotNull(scorable, "Instance cannot be null");

        List<double[]> scores = new ArrayList<>(modelIds.size());

        try {
            reloadModelsLock.readLock().lock();

            if (modelIds.size() == 1) {
                // if only one model, then don't parallelize scoring
                WekaThreadSafeScorer wekaThreadSafeScorer = getScorer(modelIds.get(0));
                scores.add(wekaThreadSafeScorer.score(scorable));
            } else {
                Map<UUID, Future<double[]>> futureScores = new HashMap<>(modelIds.size());

                // scatter
                for (UUID modelId : modelIds) {
                    WekaThreadSafeScorer wekaThreadSafeScorer = getScorer(modelId);
                    futureScores.put(modelId, executorService.submit(new AsyncScoringTask(wekaThreadSafeScorer, scorable)));
                }
                // gather
                for (UUID modelId : modelIds) {
                    scores.add(getFuture(futureScores.get(modelId), modelId));
                }
            }
        } finally {
            reloadModelsLock.readLock().unlock();
        }

        return scores;
    }

    /**
     * Score each <code>scorable</code> with the given <code>modelId</code>.
     *
     * <p> If multiple <code>scorables</code> are given as parameters, they will be scored in parallel.
     *
     * @param modelId   the id of the model
     * @param scorables an array of instances to score
     * @return a list of scores with the same order as the scorable array
     * @throws FOSException when classification was not possible
     */
    @Override
    @NotNull
    public List<double[]> score(UUID modelId, List<Object[]> scorables) throws FOSException {
        checkNotNull(scorables, "List of scorables cannot be null");

        List<double[]> scores = new ArrayList<>(scorables.size());

        try {
            reloadModelsLock.readLock().lock();

            if (scorables.size() == 1) {
                // if only one model, then don't parallelize scoring
                WekaThreadSafeScorer wekaThreadSafeScorer = getScorer(modelId);
                scores.add(wekaThreadSafeScorer.score(scorables.get(0)));
            } else {
                Map<Object[], Future<double[]>> futureScores = new HashMap<>(scorables.size());

                // scatter
                for (Object[] scorable : scorables) {
                    WekaThreadSafeScorer wekaThreadSafeScorer = wekaThreadSafeScorers.get(modelId);
                    futureScores.put(scorable, executorService.submit(new AsyncScoringTask(wekaThreadSafeScorer, scorable)));
                }

                // gather
                for (Object[] scorable : scorables) {
                    scores.add(getFuture(futureScores.get(scorable), modelId));
                }
            }
        } finally {
            reloadModelsLock.readLock().unlock();
        }

        return scores;
    }


    /**
     * Score a single <code>scorable</code> with the given <code>modelId</code>.
     *
     * @param modelId   the id of the model
     * @param scorable  the instance to score
     * @return a list of scores with the same order as the scorable array
     * @throws FOSException when classification was not possible
     */
    @Override
    @NotNull
    public double[] score(UUID modelId, Object[] scorable) throws FOSException {
        checkNotNull(scorable, "The scorable cannot be null");

        try {
            reloadModelsLock.readLock().lock();

            WekaThreadSafeScorer wekaThreadSafeScorer = getScorer(modelId);
            return wekaThreadSafeScorer.score(scorable);

        } finally {
            reloadModelsLock.readLock().unlock();
        }
    }

    /**
     * Adds the given model to the managed models.
     *
     * <p> If the provided model id already exists, then the older model is removed and the new one is instantiated.
     *
     * @param wekaModelConfig the configuration of the new model
     * @throws FOSException when the new model could not be instantiated
     */
    public void addOrUpdate(WekaModelConfig wekaModelConfig) throws FOSException {
        checkNotNull(wekaModelConfig, "Model config cannot be null");

        WekaThreadSafeScorer newWekaThreadSafeScorer = new WekaThreadSafeScorerPool(wekaModelConfig, wekaManagerConfig);
        WekaThreadSafeScorer oldWekaThreadSafeScorer = quickSwitch(wekaModelConfig.getId(), newWekaThreadSafeScorer);

        WekaUtils.closeSilently(oldWekaThreadSafeScorer);
    }

    /**
     * Removes the given model from the managed models.
     *
     * <p> If the model does not exist no exception will be thrown.
     *
     * @param modelId the id of the model to remove.
     */
    public void removeModel(UUID modelId) {
        WekaThreadSafeScorer newWekaThreadSafeScorer = null;
        WekaThreadSafeScorer oldWekaThreadSafeScorer = quickSwitch(modelId, newWekaThreadSafeScorer);

        WekaUtils.closeSilently(oldWekaThreadSafeScorer);
    }

    /**
     * Retrieves the {@link Classifier} for the given UUID.
     *
     * @param modelId The UUID of the classifier to retrieve.
     * @return The {@link Classifier} for the given UUID.
     * @throws FOSException If it fails to retrieve the classifier.
     */
    public Classifier getClassifier(UUID modelId) throws FOSException {
        return wekaThreadSafeScorers.get(modelId).getClassifier();
    }

    /**
     * Switches the {@link com.feedzai.fos.impl.weka.WekaScorer} used for the model with the given UUID.
     *
     * @param modelId                 The UUID of the model whose scorer to switch.
     * @param newWekaThreadSafeScorer The score to switch to.
     * @return The previous scorer associated to the given UUID.
     */
    private WekaThreadSafeScorer quickSwitch(UUID modelId, WekaThreadSafeScorer newWekaThreadSafeScorer) {
        try { // quick switch - do not do anything inside for performance reasons!!
            reloadModelsLock.writeLock().lock();
            return wekaThreadSafeScorers.put(modelId, newWekaThreadSafeScorer);
        } finally {
            reloadModelsLock.writeLock().unlock();
        }
    }
}


