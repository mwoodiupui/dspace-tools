/*
 * Copyright 2016 Mark H. Wood.
 */

package com.markhwood.dspace.testing;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represent a {@code <collection>} element from the structure document.
 *
 * @author mhwood
 */
@XmlType(propOrder={})
public class Collection
{
    @XmlAttribute
    String identifier;

    @XmlElement
    String name;

    @XmlElement
    String description;

    @XmlElement
    String intro;

    @XmlElement
    String copyright;

    @XmlElement
    String sidebar;

    @XmlElement
    String license;

    @XmlElement
    String provenance;
}
