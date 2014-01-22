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
package com.feedzai.fos.impl.weka.utils;

import com.feedzai.fos.api.Attribute;
import com.feedzai.fos.api.CategoricalAttribute;
import com.feedzai.fos.api.FOSException;
import com.feedzai.fos.api.NumericAttribute;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.core.FastVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class WekaMapperTests {
    private final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private List<Attribute> attributes;

    @Before
    public void setup() {
        attributes = new ArrayList<Attribute>();
        attributes.add(new NumericAttribute("a0"));
        attributes.add(new NumericAttribute("a1"));
        attributes.add(new NumericAttribute("a2"));
        attributes.add(new NumericAttribute("a3"));
        attributes.add(new CategoricalAttribute("a4", Arrays.asList("TRUE", "FALSE")));
        attributes.add(new CategoricalAttribute("a5", Arrays.asList("6", "7")));
        attributes.add(new NumericAttribute("a6"));
        attributes.add(new NumericAttribute("a7"));
    }

    @Test
    public void instanceField2AttributesTest() throws FOSException {
        FastVector attributes = WekaUtils.instanceFields2Attributes(5, this.attributes);
        Assert.assertEquals(this.attributes.size(), attributes.size());

        Assert.assertNotNull(attributes.elementAt(0));
        Assert.assertNotNull(attributes.elementAt(1));
        Assert.assertNotNull(attributes.elementAt(2));
        Assert.assertNotNull(attributes.elementAt(3));
        Assert.assertNotNull(attributes.elementAt(4));
        Assert.assertNotNull(attributes.elementAt(5));
        Assert.assertNotNull(attributes.elementAt(6));
        Assert.assertNotNull(attributes.elementAt(7));

        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(0)).isNumeric());
        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(1)).isNumeric());
        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(2)).isNumeric());
        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(3)).isNumeric());
        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(4)).isNominal());
        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(5)).isNominal());
        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(6)).isString());
        Assert.assertTrue(((weka.core.Attribute) attributes.elementAt(7)).isDate());
    }
}
