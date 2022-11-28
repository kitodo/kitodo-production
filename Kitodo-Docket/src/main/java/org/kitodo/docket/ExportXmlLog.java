/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.docket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.Property;
import org.kitodo.config.KitodoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides xml logfile generation. After the generation the file
 * will be written to user home directory
 *
 * @author Robert Sehr
 * @author Steffen Hankiewicz
 *
 */
public class ExportXmlLog implements Consumer<OutputStream> {
    private static final Logger logger = LoggerFactory.getLogger(ExportXmlLog.class);
    private static final String LABEL = "label";
    private static final String NAMESPACE = "http://www.kitodo.org/logfile";
    private static final String PROPERTIES = "properties";
    private static final String PROPERTY = "property";
    private static final String PROPERTY_IDENTIFIER = "propertyIdentifier";
    private static final String VALUE = "value";

    List<DocketData> docketData;

    /**
     * Makes the class polymorphic.
     *
     * @param docketData
     *            docket data
     */
    ExportXmlLog(Iterable<DocketData> docketData) {
        this.docketData = docketData instanceof List ? (List<DocketData>) docketData
                : StreamSupport.stream(docketData.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Makes the class polymorphic.
     *
     * @param docketData
     *            docket data
     */
    ExportXmlLog(DocketData docketData) {
        this.docketData = Arrays.asList(docketData);
    }

    @Override
    public void accept(OutputStream outputStream) {
        try {
            if (docketData.size() == 1) {
                startExport(outputStream);
            } else {
                startMultipleExport(outputStream);
            }
        } catch (IOException ioFailed) {
            throw new UncheckedIOException(ioFailed);
        }
    }

    /**
     * This method exports the production metadata as XML to a given stream.
     *
     * @param os
     *            the OutputStream to write the contents to
     * @throws IOException
     *             Throws IOException, when document creation fails.
     */
    void startExport(OutputStream os) throws IOException {
        try {
            Document doc = createDocument(docketData.get(0), true);

            XMLOutputter outp = new XMLOutputter();
            outp.setFormat(Format.getPrettyFormat());

            outp.output(doc, os);
            os.close();

        } catch (RuntimeException e) {
            logger.error("Document creation failed.");
            throw new IOException(e);
        }
    }

    /**
     * This method exports the production metadata for al list of processes as a
     * single file to a given stream.
     *
     * @param outputStream
     *            The output stream, to write the docket to.
     */

    void startMultipleExport(OutputStream outputStream) {
        Document answer = new Document();
        Element root = new Element("processes");
        answer.setRootElement(root);
        Namespace xmlns = Namespace.getNamespace(NAMESPACE);

        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.addNamespaceDeclaration(xsi);
        root.setNamespace(xmlns);
        Attribute attSchema = new Attribute("schemaLocation", NAMESPACE + " XML-logfile.xsd",
                xsi);
        root.setAttribute(attSchema);
        for (DocketData docketData : this.docketData) {
            Document doc = createDocument(docketData, false);
            Element processRoot = doc.getRootElement();
            processRoot.detach();
            root.addContent(processRoot);
        }

        XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());

        try {
            outp.output(answer, outputStream);
        } catch (IOException e) {
            logger.error("Generating XML Output failed.", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Closing the output stream failed.", e);
                }
            }
        }

    }

    /**
     * This method creates a new xml document with process metadata.
     *
     * @param docketData
     *            the docketData to export
     * @return a new xml document
     */
    private static Document createDocument(DocketData docketData, boolean addNamespace) {

        Element processElm = new Element("process");
        final Document doc = new Document(processElm);

        processElm.setAttribute("processID", String.valueOf(docketData.getProcessId()));

        Namespace xmlns = Namespace.getNamespace(NAMESPACE);
        processElm.setNamespace(xmlns);

        // namespace declaration
        processNamespaceDeclaration(addNamespace, processElm);

        // process information
        ArrayList<Element> processElements = processProcessInformation(docketData, xmlns);

        // template information
        processTemplateInformation(docketData, xmlns, processElements);

        // digital document information
        processDigitalDocumentInformation(docketData, xmlns, processElements);

        // METS information
        Element metsElement = new Element("metsInformation", xmlns);
        List<Element> metadataElements = createMetadataElements(xmlns, docketData);
        metsElement.addContent(metadataElements);
        processElements.add(metsElement);

        processElm.setContent(processElements);
        return doc;
    }

    private static void processDigitalDocumentInformation(DocketData docketData, Namespace xmlns, ArrayList<Element> processElements) {
        ArrayList<Element> docElements = new ArrayList<>();
        Element dd = new Element("digitalDocument", xmlns);

        List<Element> docProperties = prepareProperties(docketData.getWorkpieceProperties(), xmlns);

        if (!docProperties.isEmpty()) {
            Element properties = new Element(PROPERTIES, xmlns);
            properties.addContent(docProperties);
            dd.addContent(properties);
        }
        docElements.add(dd);

        Element digdoc = new Element("digitalDocuments", xmlns);
        digdoc.addContent(docElements);
        processElements.add(digdoc);
    }

    private static void processTemplateInformation(DocketData docketData, Namespace xmlns, ArrayList<Element> processElements) {
        ArrayList<Element> templateElements = new ArrayList<>();
        Element template = new Element("original", xmlns);

        ArrayList<Element> templateProperties = new ArrayList<>();
        if (docketData.getTemplateProperties() != null) {
            processTemplateProperties(docketData, xmlns, templateProperties);
        }
        if (!templateProperties.isEmpty()) {
            Element properties = new Element(PROPERTIES, xmlns);
            properties.addContent(templateProperties);
            template.addContent(properties);
        }
        templateElements.add(template);

        Element templates = new Element("originals", xmlns);
        templates.addContent(templateElements);
        processElements.add(templates);
    }

    private static void processTemplateProperties(DocketData docketData, Namespace xmlns, ArrayList<Element> templateProperties) {
        for (Property prop : docketData.getTemplateProperties()) {
            Element property = new Element(PROPERTY, xmlns);
            property.setAttribute(PROPERTY_IDENTIFIER, prop.getTitle());
            if (prop.getValue() != null) {
                property.setAttribute(VALUE, replacer(prop.getValue()));
            } else {
                property.setAttribute(VALUE, "");
            }

            Element label = new Element(LABEL, xmlns);

            label.setText(prop.getTitle());
            property.addContent(label);

            templateProperties.add(property);
            if (prop.getTitle().equals("Signatur")) {
                Element secondProperty = new Element(PROPERTY, xmlns);
                secondProperty.setAttribute(PROPERTY_IDENTIFIER, prop.getTitle() + "Encoded");
                if (prop.getValue() != null) {
                    secondProperty.setAttribute(VALUE, "vorl:" + replacer(prop.getValue()));
                    Element secondLabel = new Element(LABEL, xmlns);
                    secondLabel.setText(prop.getTitle());
                    secondProperty.addContent(secondLabel);
                    templateProperties.add(secondProperty);
                }
            }
        }
    }

    private static ArrayList<Element> processProcessInformation(DocketData docketData, Namespace xmlns) {
        ArrayList<Element> processElements = new ArrayList<>();
        Element processTitle = new Element("title", xmlns);
        processTitle.setText(docketData.getProcessName());
        processElements.add(processTitle);

        Element project = new Element("project", xmlns);
        project.setText(docketData.getProjectName());
        processElements.add(project);

        Element date = new Element("time", xmlns);
        date.setAttribute("type", "creation date");
        date.setText(String.valueOf(docketData.getCreationDate()));
        processElements.add(date);

        Element ruleset = new Element("ruleset", xmlns);
        ruleset.setText(docketData.getRulesetName());
        processElements.add(ruleset);

        Element comment = new Element("comment", xmlns);
        comment.setText(docketData.getComment());
        processElements.add(comment);

        List<Element> processProperties = prepareProperties(docketData.getProcessProperties(), xmlns);

        if (!processProperties.isEmpty()) {
            Element properties = new Element(PROPERTIES, xmlns);
            properties.addContent(processProperties);
            processElements.add(properties);
        }
        return processElements;
    }

    private static void processNamespaceDeclaration(boolean addNamespace, Element processElm) {
        if (addNamespace) {

            Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            processElm.addNamespaceDeclaration(xsi);
            Attribute attSchema = new Attribute("schemaLocation", NAMESPACE + " XML-logfile.xsd",
                    xsi);
            processElm.setAttribute(attSchema);
        }
    }

    private static List<Element> prepareProperties(List<Property> properties, Namespace xmlns) {
        ArrayList<Element> preparedProperties = new ArrayList<>();
        for (Property property : properties) {
            Element propertyElement = new Element(PROPERTY, xmlns);
            propertyElement.setAttribute(PROPERTY_IDENTIFIER, property.getTitle());
            if (property.getValue() != null) {
                propertyElement.setAttribute(VALUE, replacer(property.getValue()));
            } else {
                propertyElement.setAttribute(VALUE, "");
            }

            Element label = new Element(LABEL, xmlns);

            label.setText(property.getTitle());
            propertyElement.addContent(label);
            preparedProperties.add(propertyElement);
        }
        return preparedProperties;
    }

    private static List<Element> createMetadataElements(Namespace xmlns, DocketData docketData) {
        List<Element> metadataElements = new ArrayList<>();
        try {
            HashMap<String, String> names = getNamespacesFromConfig();
            Namespace[] namespaces = new Namespace[names.size()];
            int index = 0;
            for (var entries = names.entrySet().iterator(); entries.hasNext(); index++) {
                Entry<String, String> entry = entries.next();
                namespaces[index] = Namespace.getNamespace(entry.getKey(), entry.getValue());
            }

            prepareMetadataElements(metadataElements, false, docketData, namespaces, xmlns);
            if (Objects.nonNull(docketData.getParent())) {
                prepareMetadataElements(metadataElements, true, docketData.getParent(), namespaces, xmlns);
            }

        } catch (IOException | JDOMException | IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
        return metadataElements;
    }

    private static HashMap<String, String> getNamespacesFromConfig() {
        return getXmlPathFromConfig("namespace");
    }

    private static HashMap<String, String> getXmlPathFromConfig(String xmlPath) {
        HashMap<String, String> fields = new HashMap<>();
        try {
            File file = new File(KitodoConfig.getKitodoConfigDirectory() + "kitodo_exportXml.xml");
            if (file.exists() && file.canRead()) {
                XMLConfiguration config = new XMLConfiguration(file);
                config.setListDelimiter('&');
                config.setReloadingStrategy(new FileChangedReloadingStrategy());

                int count = config.getMaxIndex(xmlPath);
                for (int i = 0; i <= count; i++) {
                    String name = config.getString(xmlPath + "(" + i + ")[@name]");
                    String value = config.getString(xmlPath + "(" + i + ")[@value]");
                    fields.put(name, value);
                }
            }
        } catch (ConfigurationException | RuntimeException e) {
            logger.debug(e.getMessage(), e);
            fields = new HashMap<>();
        }
        return fields;
    }

    private static void prepareMetadataElements(List<Element> metadataElements, boolean useAnchor, DocketData docketData,
            Namespace[] namespaces, Namespace xmlns)
            throws IOException, JDOMException {
        HashMap<String, String> fields = getMetsFieldsFromConfig(useAnchor);
        try (InputStream in = docketData.metadataFile().toURL().openStream()) {
            Document metsDoc = new SAXBuilder().build(in);
            prepareMetadataElements(metadataElements, fields, metsDoc, namespaces, xmlns);
        }
    }

    private static void prepareMetadataElements(List<Element> metadataElements, Map<String, String> fields, Document document,
            Namespace[] namespaces, Namespace xmlns) {
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            List<Element> metsValues = getMetsValues(entry.getValue(), document, namespaces);
            for (Element element : metsValues) {
                Element ele = new Element(PROPERTY, xmlns);
                ele.setAttribute("name", key);
                ele.addContent(element.getTextTrim());
                metadataElements.add(ele);
            }
        }
    }

    private static HashMap<String, String> getMetsFieldsFromConfig(boolean useAnchor) {
        String xmlpath = "mets." + PROPERTY;
        if (useAnchor) {
            xmlpath = "anchor." + PROPERTY;
        }

        HashMap<String, String> fields = getXmlPathFromConfig(xmlpath);
        return fields;
    }

    /**
     * Get METS values.
     *
     * @param expr
     *            String
     * @param element
     *            Object
     * @param namespaces
     *            HashMap
     * @return list of elements
     */
    private static List<Element> getMetsValues(String expr, Document document, Namespace[] namespaces) {
        XPathExpression<Element> xpath = XPathFactory.instance().compile(expr.trim().replace("\n", ""),
            Filters.element(), Collections.emptyMap(), namespaces);
        return xpath.evaluate(document);
    }

    private static String replacer(String in) {
        in = in.replace("°", "?");
        in = in.replace("^", "?");
        in = in.replace("|", "?");
        in = in.replace(">", "?");
        in = in.replace("<", "?");
        return in;
    }

}
