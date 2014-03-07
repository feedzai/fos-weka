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
package com.feedzai.fos.impl.weka.exception;

import com.feedzai.fos.api.FOSException;

/**
 * Exception thrown when consuming/producing PMML from/to a {@link weka.classifiers.Classifier}.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public class PMMLConversionException extends FOSException {

    /**
     * @see com.feedzai.fos.api.FOSException#FOSException(String)
     */
    public PMMLConversionException(String message) {
        super(message);
    }

    /**
     * @see com.feedzai.fos.api.FOSException#FOSException(String, java.lang.Throwable)
     */
    public PMMLConversionException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * @see com.feedzai.fos.api.FOSException#FOSException(java.lang.Throwable)
     */
    public PMMLConversionException(Throwable e) {
        super(e);
    }
}
