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

package org.goobi.production.export;

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.services.ServiceManager;

/**
 * This class provides xml logfile generation. After the generation the file
 * will be written to user home directory
 *
 * @author Robert Sehr
 * @author Steffen Hankiewicz
 *
 */
public class ExportXmlLog {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(ExportXmlLog.class);

    /**
     * This method exports the production metadata as xml to a given directory.
     *
     * @param p
     *            the process to export
     * @param destination
     *            the destination to write the file
     */

    public void startExport(Process p, String destination) throws IOException {
        try (FileOutputStream ostream = new FileOutputStream(destination)) {
            startExport(p, ostream);
        }
    }

    /**
     * Start export.
     *
     * @param p
     *            Process object
     * @param dest
     *            File
     */
    public void startExport(Process p, File dest) throws IOException {
        try (FileOutputStream ostream = new FileOutputStream(dest)) {
            startExport(p, ostream);
        }
    }

    /**
     * This method exports the production metadata as xml to a given stream.
     *
     * @param process
     *            the process to export
     * @param os
     *            the OutputStream to write the contents to
     */
    public void startExport(Process process, OutputStream os) throws IOException {
        try {
            Document doc = createDocument(process, true);

            XMLOutputter outp = new XMLOutputter();
            outp.setFormat(Format.getPrettyFormat());

            outp.output(doc, os);
            os.close();

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * This method exports the production metadata as xml to a given stream.
     *
     * @param processList
     *            list of Process' objects
     * @param outputStream
     *            object
     * @param xslt
     *            String
     */
    public void startExport(Iterable<Process> processList, OutputStream outputStream, String xslt) {
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
        for (Process p : processList) {
            Document doc = createDocument(p, false);
            Element processRoot = doc.getRootElement();
            processRoot.detach();
            root.addContent(processRoot);
        }

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        try {

            outp.output(answer, outputStream);
        } catch (IOException e) {
            logger.error(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    outputStream = null;
                }
            }
        }

    }

    /**
     * This method creates a new xml document with process metadata.
     *
     * @param process
     *            the process to export
     * @return a new xml document
     */
    public Document createDocument(Process process, boolean addNamespace) {

        Element processElm = new Element("process");
        Document doc = new Document(processElm);

        processElm.setAttribute("processID", String.valueOf(process.getId()));

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

        ArrayList<Element> processElements = new ArrayList<>();
        Element processTitle = new Element("title", xmlns);
        processTitle.setText(process.getTitle());
        processElements.add(processTitle);

        Element project = new Element("project", xmlns);
        project.setText(process.getProject().getTitle());
        processElements.add(project);

        Element date = new Element("time", xmlns);
        date.setAttribute("type", "creation date");
        date.setText(String.valueOf(process.getCreationDate()));
        processElements.add(date);

        Element ruleset = new Element("ruleset", xmlns);
        ruleset.setText(process.getRuleset().getFile());
        processElements.add(ruleset);

        Element comment = new Element("comment", xmlns);
        comment.setText(process.getWikiField());
        processElements.add(comment);

        StringBuilder batches = new StringBuilder();
        for (Batch batch : process.getBatches()) {
            if (batch.getType() != null) {
                batches.append(serviceManager.getBatchService().getTypeTranslated(batch));
                batches.append(": ");
            }
            if (batches.length() != 0) {
                batches.append(", ");
            }
            batches.append(serviceManager.getBatchService().getLabel(batch));
        }
        if (batches.length() != 0) {
            Element batch = new Element("batch", xmlns);
            batch.setText(batches.toString());
            processElements.add(batch);
        }

        List<Element> processProperties = prepareProperties(process.getProperties(), xmlns);

        if (processProperties.size() != 0) {
            Element properties = new Element("properties", xmlns);
            properties.addContent(processProperties);
            processElements.add(properties);
        }

        // step information
        Element steps = new Element("steps", xmlns);
        ArrayList<Element> stepElements = new ArrayList<>();
        for (Task s : process.getTasks()) {
            Element stepElement = new Element("step", xmlns);
            stepElement.setAttribute("stepID", String.valueOf(s.getId()));

            Element steptitle = new Element("title", xmlns);
            steptitle.setText(s.getTitle());
            stepElement.addContent(steptitle);

            Element state = new Element("processingstatus", xmlns);
            state.setText(serviceManager.getTaskService().getProcessingStatusAsString(s));
            stepElement.addContent(state);

            Element begin = new Element("time", xmlns);
            begin.setAttribute("type", "start time");
            begin.setText(String.valueOf(s.getProcessingBegin()));
            stepElement.addContent(begin);

            Element end = new Element("time", xmlns);
            end.setAttribute("type", "end time");
            end.setText(String.valueOf(serviceManager.getTaskService().getProcessingEndAsFormattedString(s)));
            stepElement.addContent(end);

            if (isNonOpenStateAndHasRegularUser(s)) {
                Element user = new Element("user", xmlns);
                user.setText(serviceManager.getUserService().getFullName(s.getProcessingUser()));
                stepElement.addContent(user);
            }
            Element editType = new Element("edittype", xmlns);
            editType.setText(s.getEditTypeEnum().getTitle());
            stepElement.addContent(editType);

            stepElements.add(stepElement);
        }
        steps.addContent(stepElements);
        processElements.add(steps);

        // template information
        Element templates = new Element("originals", xmlns);
        ArrayList<Element> templateElements = new ArrayList<>();

        Element template = new Element("original", xmlns);
        template.setAttribute("originalID", String.valueOf(process.getId()));

        ArrayList<Element> templateProperties = new ArrayList<>();
        for (Property prop : process.getTemplates()) {
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
        if (templateProperties.size() != 0) {
            Element properties = new Element("properties", xmlns);
            properties.addContent(templateProperties);
            template.addContent(properties);
        }
        templateElements.add(template);
        templates.addContent(templateElements);
        processElements.add(templates);

        // digital document information
        Element digdoc = new Element("digitalDocuments", xmlns);
        ArrayList<Element> docElements = new ArrayList<>();

        Element dd = new Element("digitalDocument", xmlns);
        dd.setAttribute("digitalDocumentID", String.valueOf(process.getId()));

        List<Element> docProperties = prepareProperties(process.getWorkpieces(), xmlns);

        if (docProperties.size() != 0) {
            Element properties = new Element("properties", xmlns);
            properties.addContent(docProperties);
            dd.addContent(properties);
        }
        docElements.add(dd);
        digdoc.addContent(docElements);
        processElements.add(digdoc);

        // METS information
        Element metsElement = new Element("metsInformation", xmlns);
        ArrayList<Element> metadataElements = new ArrayList<>();

        try {
            URI metadataFilePath = serviceManager.getFileService().getMetadataFilePath(process);
            Document metsDoc = new SAXBuilder()
                    .build(serviceManager.getFileService().getFile(metadataFilePath).toString());
            Document anchorDoc = null;
            URI anchorFileName = URI.create(serviceManager.getFileService().getMetadataFilePath(process).toString()
                    .replace("meta.xml", "meta_anchor.xml"));
            if (serviceManager.getFileService().fileExist(anchorFileName)
                    && serviceManager.getFileService().canRead(anchorFileName)) {
                anchorDoc = new SAXBuilder()
                        .build(serviceManager.getFileService().getFile(metadataFilePath).toString());
            }
            HashMap<String, Namespace> namespaces = new HashMap<>();

            HashMap<String, String> names = getNamespacesFromConfig();
            for (Map.Entry<String, String> entry : names.entrySet()) {
                String key = entry.getKey();
                namespaces.put(key, Namespace.getNamespace(key, entry.getValue()));
            }

            HashMap<String, String> fields = getMetsFieldsFromConfig(false);
            metadataElements = prepareMetadataElements(metadataElements, fields, metsDoc, namespaces, xmlns);

            if (anchorDoc != null) {
                fields = getMetsFieldsFromConfig(true);
                metadataElements = prepareMetadataElements(metadataElements, fields, anchorDoc, namespaces, xmlns);
            }

            metsElement.addContent(metadataElements);
            processElements.add(metsElement);

        } catch (IOException | JDOMException | JaxenException e) {
            logger.error(e);
        }
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

    private ArrayList<Element> prepareMetadataElements(ArrayList<Element> metadataElements, Map<String, String> fields,
            Document document, HashMap<String, Namespace> namespaces, Namespace xmlns) throws JaxenException {
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            List<Element> metsValues = getMetsValues(entry.getValue(), document, namespaces);
            for (Element element : metsValues) {
                Element ele = new Element("property", xmlns);
                ele.setAttribute("name", key);
                ele.addContent(element.getTextTrim());
                metadataElements.add(ele);
            }
        }
        return metadataElements;
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
    @SuppressWarnings("unchecked")
    public List<Element> getMetsValues(String expr, Object element, HashMap<String, Namespace> namespaces)
            throws JaxenException {
        JDOMXPath xpath = new JDOMXPath(expr.trim().replace("\n", ""));
        // Add all namespaces
        for (Map.Entry<String, Namespace> entry : namespaces.entrySet()) {
            xpath.addNamespace(entry.getKey(), entry.getValue().getURI());
        }
        return xpath.selectNodes(element);
    }

    /**
     * This method transforms the xml log using a xslt file and opens a new
     * window with the output file.
     *
     * @param out
     *            ServletOutputStream
     * @param doc
     *            the xml document to transform
     * @param filename
     *            the filename of the xslt
     */

    public void xmlTransformation(OutputStream out, Document doc, String filename)
            throws XSLTransformException, IOException {
        Document docTrans;
        if (filename != null && filename.equals("")) {
            XSLTransformer transformer;
            transformer = new XSLTransformer(filename);
            docTrans = transformer.transform(doc);
        } else {
            docTrans = doc;
        }
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        XMLOutputter xmlOut = new XMLOutputter(format);

        xmlOut.output(docTrans, out);

    }

    public void startTransformation(OutputStream out, Process p, String filename)
            throws XSLTransformException, IOException {
        startTransformation(p, out, filename);
    }

    public void startTransformation(Process p, OutputStream out, String filename)
            throws XSLTransformException, IOException {
        Document doc = createDocument(p, true);
        xmlTransformation(out, doc, filename);
    }

    private String replacer(String in) {
        in = in.replace("°", "?");
        in = in.replace("^", "?");
        in = in.replace("|", "?");
        in = in.replace(">", "?");
        in = in.replace("<", "?");
        return in;
    }

    private HashMap<String, String> getMetsFieldsFromConfig(boolean useAnchor) {
        String xmlpath = "mets.property";
        if (useAnchor) {
            xmlpath = "anchor.property";
        }

        HashMap<String, String> fields = new HashMap<>();
        try {
            File file = new File(ConfigCore.getKitodoConfigDirectory() + "kitodo_exportXml.xml");
            if (file.exists() && file.canRead()) {
                XMLConfiguration config = new XMLConfiguration(file);
                config.setListDelimiter('&');
                config.setReloadingStrategy(new FileChangedReloadingStrategy());

                int count = config.getMaxIndex(xmlpath);
                for (int i = 0; i <= count; i++) {
                    String name = config.getString(xmlpath + "(" + i + ")[@name]");
                    String value = config.getString(xmlpath + "(" + i + ")[@value]");
                    fields.put(name, value);
                }
            }
        } catch (Exception e) {
            fields = new HashMap<>();
        }
        return fields;
    }

    private HashMap<String, String> getNamespacesFromConfig() {
        HashMap<String, String> nss = new HashMap<>();
        try {
            File file = new File(ConfigCore.getKitodoConfigDirectory() + "kitodo_exportXml.xml");
            if (file.exists() && file.canRead()) {
                XMLConfiguration config = new XMLConfiguration(file);
                config.setListDelimiter('&');
                config.setReloadingStrategy(new FileChangedReloadingStrategy());

                int count = config.getMaxIndex("namespace");
                for (int i = 0; i <= count; i++) {
                    String name = config.getString("namespace(" + i + ")[@name]");
                    String value = config.getString("namespace(" + i + ")[@value]");
                    nss.put(name, value);
                }
            }
        } catch (Exception e) {
            nss = new HashMap<>();
        }
        return nss;

    }

    /**
     * Check step for non-open step state and step has a reqular user assigned.
     *
     * @param s
     *            step to check
     * @return boolean
     */
    private boolean isNonOpenStateAndHasRegularUser(Task s) {
        return (!TaskStatus.OPEN.equals(s.getProcessingStatusEnum())) && (s.getProcessingUser() != null)
                && (s.getProcessingUser().getId() != 0)
                && (serviceManager.getUserService().getFullName(s.getProcessingUser()) != null);
    }

}
