/*
 * Copyright 2016 Mark H. Wood.
 */

package com.markhwood.dspace.testing;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

/**
 * Use the output of the DSpace structure-builder to guide the creating of SAF
 * batches that will fill the given structure with Items.
 *
 * @author mhwood
 */
public class GenerateItems
{
    /**
     * Read the output of structure-builder and create batches to fill it.
     *
     * @param argv
     * @throws JAXBException
     */
    public static void main(String[] argv)
            throws JAXBException
    {
        // TODO Analyze options

        // Read the structure
        JAXBContext context = JAXBContext.newInstance(
                ImportStructure.class,
                Community.class,
                Collection.class
        );

        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setEventHandler(new DefaultValidationEventHandler()); // TODO something better?

        File structureFile = new File(argv[0]);

        @SuppressWarnings("UnusedAssignment")
        ImportStructure importStructure = null;
        try {
            importStructure
                    = (ImportStructure) unmarshaller.unmarshal(structureFile);
        } catch (UnmarshalException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // TODO Do something with it
        if (null != importStructure.communities)
            for (Community community : importStructure.communities)
            {
                doCommunity(community);
            }
        else
        {
            System.err.println("The structure document defines no communities -- nothing to do.");
            System.exit(1);
        }
    }

    private static void doCommunity(Community community)
    {
        System.out.format("Community %s%n", community.identifier);

        if (null != community.subCommunities)
            for (Community subCommunity : community.subCommunities)
            {
                doCommunity(subCommunity);
            }

        if (null != community.collections)
            for (Collection collection : community.collections)
            {
                doCollection(collection);
            }
    }

    private static void doCollection(Collection collection)
    {
        System.out.format("Collection %s%n", collection.identifier);
    }
}
