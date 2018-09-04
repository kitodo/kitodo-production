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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.Property;
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
public class ExportXmlLog {
    private static final Logger logger = LoggerFactory.getLogger(ExportXmlLog.class);
    private static final String LABEL = "label";
    private static final String NAMESPACE = "http://www.kitodo.org/logfile";
    private static final String PROPERTIES = "properties";
    private static final String PROPERTY = "property";
    private static final String PROPERTY_IDENTIFIER = "propertyIdentifier";
    private static final String VALUE = "value";

    /**
     * This method exports the production metadata as xml to a given stream.
     *
     * @param docketData
     *            the docket data to export
     * @param os
     *            the OutputStream to write the contents to
     * @throws IOException
     *             Throws IOException, when document creation fails.
     */
    void startExport(DocketData docketData, OutputStream os) throws IOException {
        try {
            Document doc = createDocument(docketData, true);

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
     * @param docketDataList
     *            a list of Docket data
     * @param outputStream
     *            The output stream, to write the docket to.
     */

    void startMultipleExport(Iterable<DocketData> docketDataList, OutputStream outputStream) {
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
        for (DocketData docketData : docketDataList) {
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
    private Document createDocument(DocketData docketData, boolean addNamespace) {

        Element processElm = new Element("process");
        final Document doc = new Document(processElm);

        processElm.setAttribute("processID", String.valueOf(docketData.getProcessId()));

        Namespace xmlns = Namespace.getNamespace(NAMESPACE);
        processElm.setNamespace(xmlns);
        // namespace declaration
        if (addNamespace) {

            Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            processElm.addNamespaceDeclaration(xsi);
            Attribute attSchema = new Attribute("schemaLocation", NAMESPACE + " XML-logfile.xsd",
                    xsi);
            processElm.setAttribute(attSchema);
        }
        // process information

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

        // template information
        ArrayList<Element> templateElements = new ArrayList<>();
        Element template = new Element("original", xmlns);

        ArrayList<Element> templateProperties = new ArrayList<>();
        if (docketData.getTemplateProperties() != null) {
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
        if (!templateProperties.isEmpty()) {
            Element properties = new Element(PROPERTIES, xmlns);
            properties.addContent(templateProperties);
            template.addContent(properties);
        }
        templateElements.add(template);

        Element templates = new Element("originals", xmlns);
        templates.addContent(templateElements);
        processElements.add(templates);

        // digital document information
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

        processElm.setContent(processElements);
        return doc;
    }

    private List<Element> prepareProperties(List<Property> properties, Namespace xmlns) {
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

    private String replacer(String in) {
        in = in.replace("°", "?");
        in = in.replace("^", "?");
        in = in.replace("|", "?");
        in = in.replace(">", "?");
        in = in.replace("<", "?");
        return in;
    }

}
