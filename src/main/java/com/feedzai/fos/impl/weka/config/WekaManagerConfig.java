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

import com.feedzai.fos.api.config.FosConfig;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configuration required for the weka manager.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 * @author Miguel Duarte (miguel.duarte@feedzai.com)
 */
public class WekaManagerConfig extends FosConfig {
    /**
     * The file extension of the header files.
     */
    public static final String HEADER_EXTENSION = "header";
    /**
     * Name of the configuration parameter for the maximum number of scoring threads
     */
    private static final String MAX_SIMULTANEOUS_SCORING_THREADS = "MaxSimultaneousScoringThreads";

    private FosConfig configuration;

    /**
     * Creates a new object from the given configuration.
     *
     * @param configuration creates a WekaManagerConfig with the given configuration.
     */
    public WekaManagerConfig(FosConfig configuration) {
        super(configuration.getConfig());
        checkNotNull(configuration, "Configuration cannot be null");

        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("configuration", configuration)
                .add("maxSimultaneousScoringThreads", getMaxSimultaneousScoringThreads())
                .toString();
    }

    public int getMaxSimultaneousScoringThreads() {
        return configuration.getConfig().getInt(MAX_SIMULTANEOUS_SCORING_THREADS, 100);
    }
}
