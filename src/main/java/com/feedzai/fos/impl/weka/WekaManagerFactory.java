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

import com.feedzai.fos.api.Manager;
import com.feedzai.fos.api.ManagerFactory;
import com.feedzai.fos.api.config.FosConfig;
import com.feedzai.fos.impl.weka.config.WekaManagerConfig;

/**
 * FActory for creating weka managers.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaManagerFactory implements ManagerFactory {

    @Override
    public Manager createManager(FosConfig configuration) {
        WekaManagerConfig wekaManagerConfig = new WekaManagerConfig(configuration);
        return new WekaManager(wekaManagerConfig);
    }
}

