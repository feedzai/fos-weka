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
 * General exception for the Weka implementation.
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaClassifierException extends FOSException {
    /**
     * Builds a new exception from the given throwable.
     *
     * @param e the underlying throwable
     */
    public WekaClassifierException(Throwable e) {
        super(e);
    }

    /**
     * Create a new exception with the given message.
     *
     * @param msg the message that explains the exception
     */
    protected WekaClassifierException(String msg) {
        super(msg);
    }
}
