/*
 * Copyright 2016 Mark H. Wood.
 */

package com.markhwood.dspace.testing;

import com.markhwood.launcher.Tool;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
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
 * Use the output of the DSpace structure-builder to guide the creating of SAF
 * batches that will fill the given structure with Items.
 *
 * @author mhwood
 */
public class GenerateItems
        implements Tool
{
    private static boolean debug;

    private static int nItems;

    private static File outputDirectory;

    private static String bitstreamText;

    private static DocumentBuilder documentBuilder;

    /**
     * Read the output of structure-builder and create batches to fill it.
     *
     * @param name configured name of this tool.
     * @param argv command line arguments.
     * @return 0 if success; nonzero if failure.
     * @throws JAXBException if there is trouble reading the structure document.
     * @throws IOException if resources cannot be read.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be configured.
     */
    @Override
    public int run(String name, String[] argv)
            throws JAXBException, IOException, ParserConfigurationException
    {
        final String DEFAULT_ITEM_COUNT = "1";
        final String DEFAULT_OUTPUT_DIRECTORY = "batches";

        // Load some constant text to become Item content.
        final Properties LOREM_IPSUM = new Properties();
        LOREM_IPSUM.load(GenerateItems.class.getResourceAsStream(
                "/com/markhwood/lorem-ipsum.properties"));
        bitstreamText = LOREM_IPSUM.getProperty("text", "text");

        // Analyze options
        Options options = new Options();

        options.addOption("d", "debug", false, "Enable debug output");

        options.addOption("h", "help", false, "Display this help");

        options.addOption(Option.builder("n")
                .longOpt("items")
                .desc("Create this many Items in each Collection")
                .hasArg()
                .argName("N")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .desc("Create batches in this directory")
                .hasArg()
                .argName("FILE")
                .build());

        @SuppressWarnings("UnusedAssignment")
        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse(options, argv, true);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        if (cmd.hasOption('h'))
        {
            new HelpFormatter().printHelp(name,
                    "Create DSpace SAF batches from a structure map",
                    options,
                    "The default Collection size is " + DEFAULT_ITEM_COUNT + "\n"
                            + "The default output directory is ./" + DEFAULT_OUTPUT_DIRECTORY,
                    true);
            return 0;
        }

        debug = cmd.hasOption('d');
        nItems = Integer.parseInt(cmd.getOptionValue('n', DEFAULT_ITEM_COUNT));
        outputDirectory = new File(cmd.getOptionValue('o', DEFAULT_OUTPUT_DIRECTORY));

        String mapPath = cmd.getArgs()[0];

        if (debug)
        {
            System.err.format("Structure map will be read from %s%n", mapPath);
            System.err.format("Each Collection will contain %d Items%n", nItems);
            System.err.format("Batches will be built under %s%n",
                    outputDirectory.getPath());
        }

        // Read the structure
        JAXBContext context = JAXBContext.newInstance(
                ImportStructure.class,
                Community.class,
                Collection.class
        );

        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setEventHandler(new DefaultValidationEventHandler()); // TODO something better?

        File structureFile = new File(mapPath);

        @SuppressWarnings("UnusedAssignment")
        ImportStructure importStructure = null;
        try {
            importStructure
                    = (ImportStructure) unmarshaller.unmarshal(structureFile);
        } catch (UnmarshalException e) {
            System.err.println("Failed to read structure map:  " + e.getMessage());
            return 1;
        }

        // Do something with it
        if (null != importStructure.communities)
        {
            if (debug) System.err.format("Filling %d top-level communities%n",
                    importStructure.communities.length);

            outputDirectory.mkdirs(); // Ensure that output directory exists
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            for (Community community : importStructure.communities)
            {
                doCommunity(community);
            }
            return 0;
        }
        else
        {
            System.err.println("The structure document defines no communities -- nothing to do.");
            return 1;
        }
    }

    private static void doCommunity(Community community)
    {
        if (debug)
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

    private static final String BITSTREAM_FILE_NAME = "bitstream.txt";

    private static void doCollection(Collection collection)
    {
        String identifier = collection.identifier;

        if (debug)
            System.out.format("Collection %s%n", identifier);

        // Create a batch directory.
        String batchDirName = identifier.replace('/', '_');
        File batchDir = new File(outputDirectory, batchDirName);
        batchDir.mkdir();

        for (int itemN = 1; itemN <= nItems; itemN++)
        {
            File itemDir = new File(batchDir, String.format("item%03d", itemN));
            itemDir.mkdir();    // Create an item directory.
            File aFile;

            // Create a bitstream file.
            aFile = new File(itemDir, BITSTREAM_FILE_NAME);
            try (Writer writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(aFile),
                            StandardCharsets.UTF_8)))
            {
                writer.write(bitstreamText);
                writer.write('\n');
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            // Create a "dublin_core.xml" file.
            Document dcDocument = documentBuilder.newDocument();
            Node root = dcDocument.createElement("dublin_core");
            dcDocument.appendChild(root);

            Element metadata;

            metadata = dcDocument.createElement("dcvalue");
            metadata.setAttribute("element", "title");
            metadata.setTextContent(String.format("Lorem Ipsum %d", itemN));
            root.appendChild(metadata);

            metadata = dcDocument.createElement("dcvalue");
            metadata.setAttribute("element", "date");
            metadata.setAttribute("qualifier", "issued");
            metadata.setTextContent("1957");
            root.appendChild(metadata);

            metadata = dcDocument.createElement("dcvalue");
            metadata.setAttribute("element", "contributor");
            metadata.setAttribute("qualifier", "author");
            metadata.setTextContent("GenerateItems");
            root.appendChild(metadata);

            aFile = new File(itemDir, "dublin-core.xml");
            try (Writer writer = new BufferedWriter(
                    new OutputStreamWriter(
                    new FileOutputStream(aFile),
                    StandardCharsets.UTF_8)))
            {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty("encoding", StandardCharsets.UTF_8.name());
                transformer.setOutputProperty("indent", "yes");
                transformer.transform(new DOMSource(dcDocument), new StreamResult(writer));
            } catch (TransformerException | IOException e) {
                System.err.println(e.getMessage());
            }

            // Create a "contents" file.
            aFile = new File(itemDir, "contents");
            try (Writer writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(aFile),
                            StandardCharsets.UTF_8)))
            {
                writer.write(BITSTREAM_FILE_NAME);
                writer.write('\n');
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "Generate batches of content, given the output of the DSpace structure-builder";
    }
}
