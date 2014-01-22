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

import com.feedzai.fos.common.validation.Nullable;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of the @{GenericObjectPool} to auto populate based on the minIdle even if the monitor is not enabled.
 *
 * @param <T> the type of elements in the pool
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class AutoPopulateGenericObjectPool<T> extends GenericObjectPool<T> {
    private static final int DISABLED = -1;

    /**
     * Create a new pool with the given factory and configuration.
     *
     * @param factory the factory that produces the elements of the pool
     * @param config  the configuration of the pool
     * @throws Exception
     */
    public AutoPopulateGenericObjectPool(PoolableObjectFactory<T> factory, @Nullable Config config) throws Exception {
        super(factory, config);
        this.populate();
    }

    /**
     * Populate the pool up to minIdle instances. Is limited to maxIdle and maxActive.
     *
     * @throws Exception when could not borrow or return objects to the pool
     */
    private void populate() throws Exception {
        List<T> initializer = new ArrayList<T>(this.getMinIdle());

        for (int idx = 0; idx < this.getMinIdle() && (this.getMaxIdle() == DISABLED || idx < this.getMaxIdle()) && (this.getMaxActive() == DISABLED || idx < this.getMaxActive()); idx++) {
            initializer.add(this.borrowObject());
        }
        for (int idx = 0; idx < this.getMinIdle() && (this.getMaxIdle() == DISABLED || idx < this.getMaxIdle()) && (this.getMaxActive() == DISABLED || idx < this.getMaxActive()); idx++) {
            this.returnObject(initializer.get(idx));
        }
    }
}
