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
package weka.classifiers;

/**
 * Utility classes to deal with the Weka API.
 *
 * @author Ricardo Ferreira (ricardo.ferreira@feedzai.com)
 * @since 1.0.4
 */
public final class RandomForestUtils {

    /**
     * Retrieves the array of classifiers in a bagging.
     *
     * @param bagging The bag for which to retrieve the classifiers.
     * @return The array of classifiers in a bagging.
     */
    public static Classifier[] getBaggingClassifiers(IteratedSingleClassifierEnhancer bagging) {
        return bagging.m_Classifiers;
    }

    /**
     * Sets the classifiers in a bagging.
     *
     * @param bagging The bag for which to set the classifiers.
     * @throws Exception If it fails to set the classifiers.
     */
    public static void setupBaggingClassifiers(IteratedSingleClassifierEnhancer bagging) throws Exception {
        bagging.m_Classifiers = Classifier.makeCopies(bagging.m_Classifier, bagging.m_NumIterations);
    }
}
