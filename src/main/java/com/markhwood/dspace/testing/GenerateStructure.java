package com.markhwood.dspace.testing;

/*
 * #%L
 * Personal DSpace tools
 * %%
 * Copyright (C) 2016 Mark H. Wood
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.markhwood.launcher.Tool;
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
public class GenerateStructure
        implements Tool
{
    private static int nCommunities = 0;

    private static int nCollections = 0;

    private static Document document;

    private static final String[] COMMUNITY_FIELDS = {"name", "description", "intro", "copyright", "sidebar"};

    private static final String[] COLLECTION_FIELDS = {"name", "description", "intro", "copyright", "sidebar", "license", "provenance"};

    @Override
    public int run(String name, String[] argv)
            throws ParserConfigurationException,
                   TransformerConfigurationException,
                   TransformerException,
                   ParseException
    {
        @SuppressWarnings("UnusedAssignment")
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
            new HelpFormatter().printHelp("java " + GenerateStructure.class.getCanonicalName(),
                    options);
            return 0;
        }

        // Collect options and arguments
        format = cmd.hasOption('f');
        if (cmd.hasOption('w'))
        {
            String[] widths = cmd.getOptionValues('w');
            if (widths.length < 2)
            {
                System.err.println("The --width option must have at least two values");
                return 1;
            }
            width = new int[widths.length];
            for (int atDepth = 0; atDepth < widths.length; atDepth++)
                width[atDepth] = Integer.parseInt(widths[atDepth]);
        }
        else
        {
            System.err.println("The --width \"option\" is required");
            return 1;
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
                element.setTextContent(fieldContent + " " + fieldName);
                community.appendChild(element);
            }
            root.appendChild(community);
            // Recursively fill this Community with subcommunities and collections
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
        return 0;
    }

    private static void addCommunities(int depth, int width[], Element parent)
    {
        Element community;
        for (int nCommunity = 0; nCommunity < width[depth]; nCommunity++)
        {
            // Create a Community
            community = (Element) document.createElement("community");
            Element element;
            String fieldContent = "Community " + ++nCommunities;
            for (String fieldName : COMMUNITY_FIELDS)
            {
                element = document.createElement(fieldName);
                element.setTextContent(fieldContent + " " + fieldName);
                community.appendChild(element);
            }
            parent.appendChild(community);
            // Recursively fill this Community with subcommunities and collections
            if (depth < width.length-2)
                addCommunities(depth+1, width, community);
            else
                addCollections(depth+1, width, community);
        }
    }

    private static void addCollections(int depth, int width[], Element parent)
    {
        Element collection;
        for (int nCollection = 0; nCollection < width[depth]; nCollection++)
        {
            // Create a Collection
            collection = (Element) document.createElement("collection");
            Element element;
            String fieldContent = "Collection " + ++nCollections;
            for (String fieldName : COLLECTION_FIELDS)
            {
                element = document.createElement(fieldName);
                element.setTextContent(fieldContent + " " + fieldName);
                collection.appendChild(element);
            }
            parent.appendChild(collection);
        }
    }

    @Override
    public String getDescription()
    {
        return "Generate Community/Collection structure as input to the DSpace structure-builder";
    }
}
