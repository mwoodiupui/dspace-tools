/*
 * Copyright 2016 Mark H. Wood.
 */

package com.markhwood.dspace.testing;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author mhwood
 */
@XmlRootElement(name="import_structure")
public class ImportStructure
{
    @XmlElement(name="community")
    Community[] communities;
}
