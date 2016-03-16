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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
                   TransformerException,
                   ParseException
    {
        int[] width = new int[0];

        boolean format;

        // Analyze the command line
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Hellllp meeeee!");
        options.addOption("f", "format", false, "try to make output readable (otherwise make it compact)");
        Option option = Option.builder("w")
                .longOpt("width")
                .desc("number of containers to create at each depth")
                .hasArgs()
                .build();
        options.addOption(option);
        CommandLine cmd = parser.parse(options, argv);

        // Help confused users
        if (cmd.hasOption('h'))
        {
            new HelpFormatter().printHelp(
                    "java " + GenerateContent.class.getCanonicalName(),
                    options);
            System.exit(0);
        }

        // Collect options and arguments
        format = cmd.hasOption('f');
        if (cmd.hasOption('w'))
        {
            String[] widths = cmd.getOptionValues('w');
            width = new int[widths.length];
            for (int atDepth = 0; atDepth < widths.length; atDepth++)
                width[atDepth] = Integer.parseInt(widths[atDepth]);
        }
        else
        {
            System.err.println("The --width \"option\" is required");
            System.exit(1);
        }

        // Begin work
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
