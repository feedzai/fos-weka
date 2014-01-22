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
package com.feedzai.fos.impl.weka.utils.setter;

import com.feedzai.fos.api.FOSException;
import weka.core.Attribute;
import weka.core.Instance;

/**
 * Command pattern for converting the scorable from Object[] to actual data types like {String, Integer, Date...}.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public interface InstanceSetter {
    /**
     * Sets the given <code>attribute</code> to the <code>value</code>.
     *
     * @param instance  the instance where to set the value
     * @param attribute the attribute to set
     * @param value     the value to set
     */
    void set(Instance instance,Attribute attribute, Object value) throws FOSException;
}
