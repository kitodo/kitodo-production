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

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
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

    /**
     * This method exports the production metadata as xml to a given stream.
     *
     * @param docketData
     *            the docketdata to export
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
     *            a list of Docketdata
     * @param outputStream
     *            The outputstream, to write the docket to.
     */

    void startMultipleExport(Iterable<DocketData> docketDataList, OutputStream outputStream) {
        Document answer = new Document();
        Element root = new Element("processes");
        answer.setRootElement(root);
        Namespace xmlns = Namespace.getNamespace("http://www.kitodo.org/logfile");

        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.addNamespaceDeclaration(xsi);
        root.setNamespace(xmlns);
        Attribute attSchema = new Attribute("schemaLocation", "http://www.kitodo.org/logfile" + " XML-logfile.xsd",
                xsi);
        root.setAttribute(attSchema);
        for (DocketData docketData : docketDataList) {
            Document doc = createDocument(docketData, false);
            Element processRoot = doc.getRootElement();
            processRoot.detach();
            root.addContent(processRoot);
        }

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        try {
            outp.output(answer, outputStream);
        } catch (IOException e) {
            logger.error("Generating XML Output failed.", e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Closing the outputstream failed.", e.getMessage());
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

        Namespace xmlns = Namespace.getNamespace("http://www.kitodo.org/logfile");
        processElm.setNamespace(xmlns);
        // namespace declaration
        if (addNamespace) {

            Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            processElm.addNamespaceDeclaration(xsi);
            Attribute attSchema = new Attribute("schemaLocation", "http://www.kitodo.org/logfile" + " XML-logfile.xsd",
                    xsi);
            processElm.setAttribute(attSchema);
        }
        // process information

        ArrayList<Element> processElements = new ArrayList<Element>();
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

        if (processProperties.size() != 0) {
            Element properties = new Element("properties", xmlns);
            properties.addContent(processProperties);
            processElements.add(properties);
        }

        // template information
        ArrayList<Element> templateElements = new ArrayList<>();
        Element template = new Element("original", xmlns);

        ArrayList<Element> templateProperties = new ArrayList<>();
        if (docketData.getTemplateProperties() != null) {
            for (Property prop : docketData.getTemplateProperties()) {
                Element property = new Element("property", xmlns);
                property.setAttribute("propertyIdentifier", prop.getTitle());
                if (prop.getValue() != null) {
                    property.setAttribute("value", replacer(prop.getValue()));
                } else {
                    property.setAttribute("value", "");
                }

                Element label = new Element("label", xmlns);

                label.setText(prop.getTitle());
                property.addContent(label);

                templateProperties.add(property);
                if (prop.getTitle().equals("Signatur")) {
                    Element secondProperty = new Element("property", xmlns);
                    secondProperty.setAttribute("propertyIdentifier", prop.getTitle() + "Encoded");
                    if (prop.getValue() != null) {
                        secondProperty.setAttribute("value", "vorl:" + replacer(prop.getValue()));
                        Element secondLabel = new Element("label", xmlns);
                        secondLabel.setText(prop.getTitle());
                        secondProperty.addContent(secondLabel);
                        templateProperties.add(secondProperty);
                    }
                }
            }
        }
        if (templateProperties.size() != 0) {
            Element properties = new Element("properties", xmlns);
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

        if (docProperties.size() != 0) {
            Element properties = new Element("properties", xmlns);
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
            Element propertyElement = new Element("property", xmlns);
            propertyElement.setAttribute("propertyIdentifier", property.getTitle());
            if (property.getValue() != null) {
                propertyElement.setAttribute("value", replacer(property.getValue()));
            } else {
                propertyElement.setAttribute("value", "");
            }

            Element label = new Element("label", xmlns);

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
