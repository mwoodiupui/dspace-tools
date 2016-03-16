/*
 * Copyright 2016 Mark H. Wood.
 */

package com.markhwood.dspace.testing;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Create input for DSpace content loading tools.
 *
 * @author mhwood
 */
public class GenerateContent
{
    private static int nCommunities = 0;

    private static int nCollections = 0;

    private static Document document;

    private static final String[] COMMUNITY_FIELDS = {"name", "description", "intro", "copyright", "sidebar"};

    private static final String[] COLLECTION_FIELDS = {"name", "description", "intro", "copyright", "sidebar", "license", "provenance"};

    public static void main(String[] argv)
            throws ParserConfigurationException,
                   TransformerConfigurationException,
                   TransformerException
    {
        int[] width = { 10, 10, 10 }; // TODO parameterize

        boolean format = true; // TODO parameterize

        final int depth = 0;

        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = (Element) document.createElement("import_structure");
        document.appendChild((Node) root);

        Element community;
        for (int nCommunity = 0; nCommunity < width[0]; nCommunity++)
        {
            // Create a Community
            community = (Element) document.createElement("community");
            Element element;
            String fieldContent = "Community " + nCommunity;
            for (String fieldName : COMMUNITY_FIELDS)
            {
                element = document.createElement(fieldName);
                element.setTextContent(fieldContent);
                community.appendChild(element);
            }
            root.appendChild(community);
            // TODO Recursively fill this Community with subcommunities and collections
            if (depth < width.length-2)
                addCommunities(depth+1, width, community);
            else
                addCollections(depth+1, width, community);
        }

        // Serialize the Document
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", format ? "yes" : "no");
        transformer.transform(new DOMSource(document), new StreamResult(System.out));
    }

    private static void addCommunities(int depth, int width[], Element parent)
    {
        Element community;
        for (int nCommunity = 0; nCommunity < width[0]; nCommunity++)
        {
            // Create a Community
            community = (Element) document.createElement("community");
            Element element;
            String fieldContent = "Community " + ++nCommunities;
            for (String fieldName : COMMUNITY_FIELDS)
            {
                element = document.createElement(fieldName);
                element.setTextContent(fieldContent);
                community.appendChild(element);
            }
            parent.appendChild(community);
            // TODO Recursively fill this Community with subcommunities and collections
            if (depth < width.length-2)
                addCommunities(depth+1, width, community);
            else
                addCollections(depth+1, width, community);
        }
    }

    private static void addCollections(int depth, int width[], Element parent)
    {
        Element collection;
        for (int nCollection = 0; nCollection < width[0]; nCollection++)
        {
            // Create a Collection
            collection = (Element) document.createElement("collection");
            Element element;
            String fieldContent = "Collection " + ++nCollections;
            for (String fieldName : COLLECTION_FIELDS)
            {
                element = document.createElement(fieldName);
                element.setTextContent(fieldContent);
                collection.appendChild(element);
            }
            parent.appendChild(collection);
        }
    }
}
