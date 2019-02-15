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

package org.kitodo.production.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.configuration.ConfigurationException;
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
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.production.services.ServiceManager;

/**
 * This class provides xml logfile generation. After the generation the file
 * will be written to user home directory
 *
 * @author Robert Sehr
 * @author Steffen Hankiewicz
 *
 */
public class ExportXmlLog {

    private static final Logger logger = LogManager.getLogger(ExportXmlLog.class);
    private static final String LABEL = "label";
    private static final String NAMESPACE = "http://www.kitodo.org/logfile";
    private static final String PROPERTIES = "properties";
    private static final String PROPERTY = "property";
    private static final String PROPERTY_IDENTIFIER = "propertyIdentifier";
    private static final String VALUE = "value";

    /**
     * This method exports the production metadata as xml to a given directory.
     *
     * @param process
     *            the process to export
     * @param destination
     *            the destination to write the file
     */

    public void startExport(Process process, String destination) throws IOException {
        try (FileOutputStream ostream = new FileOutputStream(destination)) {
            startExport(process, ostream);
        }
    }

    /**
     * Start export.
     *
     * @param process
     *            Process object
     * @param dest
     *            File
     */
    public void startExport(Process process, File dest) throws IOException {
        try (FileOutputStream ostream = new FileOutputStream(dest)) {
            startExport(process, ostream);
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
    private void startExport(Process process, OutputStream os) throws IOException {
        Document doc = createDocument(process, true);

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        outp.output(doc, os);
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
    public void startExport(Iterable<Process> processList, OutputStream outputStream, String xslt) throws IOException {
        Document answer = new Document();
        Element root = new Element("processes");
        answer.setRootElement(root);
        Namespace xmlns = Namespace.getNamespace(NAMESPACE);
        root.setNamespace(xmlns);

        addNamespaceDeclaration(root);
        for (Process p : processList) {
            Document doc = createDocument(p, false);
            Element processRoot = doc.getRootElement();
            processRoot.detach();
            root.addContent(processRoot);
        }

        XMLOutputter outp = new XMLOutputter();
        outp.output(answer, outputStream);
    }

    /**
     * This method creates a new xml document with process metadata.
     *
     * @param process
     *            the process to export
     * @return a new xml document
     */
    private Document createDocument(Process process, boolean addNamespace) {
        Element processElement = new Element("process");

        processElement.setAttribute("processID", String.valueOf(process.getId()));

        Namespace xmlns = Namespace.getNamespace(NAMESPACE);
        processElement.setNamespace(xmlns);

        // namespace declaration
        if (addNamespace) {
            addNamespaceDeclaration(processElement);
        }

        // process information
        List<Element> processElements = getProcessInformation(xmlns, process);

        processElement.setContent(processElements);

        return new Document(processElement);
    }

    private void addNamespaceDeclaration(Element element) {
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        element.addNamespaceDeclaration(xsi);
        Attribute attSchema = new Attribute("schemaLocation", NAMESPACE + " XML-logfile.xsd", xsi);
        element.setAttribute(attSchema);
    }

    private List<Element> getProcessInformation(Namespace xmlns, Process process) {
        List<Element> processElements = new ArrayList<>();

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
            if (Objects.nonNull(batch.getType())) {
                batches.append(ServiceManager.getBatchService().getTypeTranslated(batch));
                batches.append(": ");
            }
            if (batches.length() != 0) {
                batches.append(", ");
            }
            batches.append(ServiceManager.getBatchService().getLabel(batch));
        }
        if (batches.length() != 0) {
            Element batch = new Element("batch", xmlns);
            batch.setText(batches.toString());
            processElements.add(batch);
        }

        List<Element> processProperties = prepareProperties(process.getProperties(), xmlns);

        if (!processProperties.isEmpty()) {
            Element properties = new Element(PROPERTIES, xmlns);
            properties.addContent(processProperties);
            processElements.add(properties);
        }

        // task information
        Element tasks = getTasksElement(process.getTasks(), xmlns);
        processElements.add(tasks);

        // template information
        Element templates = new Element("originals", xmlns);
        List<Element> templateElements = new ArrayList<>();

        Element template = getTemplateElement(process, xmlns);
        templateElements.add(template);
        templates.addContent(templateElements);
        processElements.add(templates);

        // digital document information
        List<Element> docElements = new ArrayList<>();

        Element dd = new Element("digitalDocument", xmlns);
        dd.setAttribute("digitalDocumentID", String.valueOf(process.getId()));

        List<Element> docProperties = prepareProperties(process.getWorkpieces(), xmlns);

        if (!docProperties.isEmpty()) {
            Element properties = new Element(PROPERTIES, xmlns);
            properties.addContent(docProperties);
            dd.addContent(properties);
        }
        docElements.add(dd);

        Element digdoc = new Element("digitalDocuments", xmlns);
        digdoc.addContent(docElements);
        processElements.add(digdoc);

        // METS information
        Element metsElement = new Element("metsInformation", xmlns);
        List<Element> metadataElements = getMetadataElements(xmlns, process);
        metsElement.addContent(metadataElements);
        processElements.add(metsElement);

        return processElements;
    }

    private Element getTasksElement(List<Task> tasks, Namespace xmlns) {
        // step information
        Element steps = new Element("steps", xmlns);
        List<Element> stepElements = new ArrayList<>();
        for (Task task : tasks) {
            Element stepElement = new Element("step", xmlns);
            stepElement.setAttribute("stepID", String.valueOf(task.getId()));

            Element stepTitle = new Element("title", xmlns);
            stepTitle.setText(task.getTitle());
            stepElement.addContent(stepTitle);

            Element state = new Element("processingstatus", xmlns);
            state.setText(ServiceManager.getTaskService().getProcessingStatusAsString(task));
            stepElement.addContent(state);

            Element begin = new Element("time", xmlns);
            begin.setAttribute("type", "start time");
            begin.setText(String.valueOf(task.getProcessingBegin()));
            stepElement.addContent(begin);

            Element end = new Element("time", xmlns);
            end.setAttribute("type", "end time");
            end.setText(String.valueOf(ServiceManager.getTaskService().getProcessingEndAsFormattedString(task)));
            stepElement.addContent(end);

            if (isNonOpenStateAndHasRegularUser(task)) {
                Element user = new Element("user", xmlns);
                user.setText(ServiceManager.getUserService().getFullName(task.getProcessingUser()));
                stepElement.addContent(user);
            }
            Element editType = new Element("edittype", xmlns);
            editType.setText(task.getEditTypeEnum().getTitle());
            stepElement.addContent(editType);

            stepElements.add(stepElement);
        }
        steps.addContent(stepElements);
        return steps;
    }

    private Element getTemplateElement(Process process, Namespace xmlns) {
        Element template = new Element("original", xmlns);
        template.setAttribute("originalID", String.valueOf(process.getId()));

        List<Element> templateProperties = new ArrayList<>();
        for (Property prop : process.getTemplates()) {
            Element property = new Element(PROPERTY, xmlns);
            property.setAttribute(PROPERTY_IDENTIFIER, prop.getTitle());
            if (Objects.nonNull(prop.getValue())) {
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
                if (Objects.nonNull(prop.getValue())) {
                    secondProperty.setAttribute(VALUE, "vorl:" + replacer(prop.getValue()));
                    Element secondLabel = new Element(LABEL, xmlns);
                    secondLabel.setText(prop.getTitle());
                    secondProperty.addContent(secondLabel);
                    templateProperties.add(secondProperty);
                }
            }
        }
        if (!templateProperties.isEmpty()) {
            Element properties = new Element(PROPERTIES, xmlns);
            properties.addContent(templateProperties);
            template.addContent(properties);
        }
        return template;
    }

    private List<Element> prepareProperties(List<Property> properties, Namespace xmlns) {
        List<Element> preparedProperties = new ArrayList<>();
        for (Property property : properties) {
            Element propertyElement = new Element(PROPERTY, xmlns);
            propertyElement.setAttribute(PROPERTY_IDENTIFIER, property.getTitle());
            if (Objects.nonNull(property.getValue())) {
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

    private void prepareMetadataElements(List<Element> metadataElements, Map<String, String> fields, Document document,
            HashMap<String, Namespace> namespaces, Namespace xmlns) throws JaxenException {
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

    private List<Element> getMetadataElements(Namespace xmlns, Process process) {
        List<Element> metadataElements = new ArrayList<>();
        try {
            URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
            Document metsDoc = new SAXBuilder()
                    .build(ServiceManager.getFileService().getFile(metadataFilePath).toString());
            Document anchorDoc = null;
            URI anchorFileName = URI.create(ServiceManager.getFileService().getMetadataFilePath(process).toString()
                    .replace("meta.xml", "meta_anchor.xml"));
            if (ServiceManager.getFileService().fileExist(anchorFileName)
                    && ServiceManager.getFileService().canRead(anchorFileName)) {
                anchorDoc = new SAXBuilder()
                        .build(ServiceManager.getFileService().getFile(metadataFilePath).toString());
            }
            HashMap<String, Namespace> namespaces = new HashMap<>();

            HashMap<String, String> names = getNamespacesFromConfig();
            for (Map.Entry<String, String> entry : names.entrySet()) {
                String key = entry.getKey();
                namespaces.put(key, Namespace.getNamespace(key, entry.getValue()));
            }

            HashMap<String, String> fields = getMetsFieldsFromConfig(false);
            prepareMetadataElements(metadataElements, fields, metsDoc, namespaces, xmlns);

            if (Objects.nonNull(anchorDoc)) {
                fields = getMetsFieldsFromConfig(true);
                prepareMetadataElements(metadataElements, fields, anchorDoc, namespaces, xmlns);
            }
        } catch (IOException | JDOMException | JaxenException e) {
            logger.error(e.getMessage(), e);
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
    private List<Element> getMetsValues(String expr, Object element, Map<String, Namespace> namespaces)
            throws JaxenException {
        JDOMXPath xpath = new JDOMXPath(expr.trim().replace("\n", ""));
        // Add all namespaces
        for (Map.Entry<String, Namespace> entry : namespaces.entrySet()) {
            xpath.addNamespace(entry.getKey(), entry.getValue().getURI());
        }
        return xpath.selectNodes(element);
    }

    /**
     * This method transforms the xml log using a xslt file and opens a new window
     * with the output file.
     *
     * @param out
     *            ServletOutputStream
     * @param doc
     *            the xml document to transform
     * @param filename
     *            the filename of the xslt
     */

    private void xmlTransformation(OutputStream out, Document doc, String filename)
            throws XSLTransformException, IOException {
        Document docTrans;
        if (Objects.nonNull(filename) && filename.isEmpty()) {
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

    private void startTransformation(Process p, OutputStream out, String filename)
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
        String xmlpath = "mets." + PROPERTY;
        if (useAnchor) {
            xmlpath = "anchor." + PROPERTY;
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
        } catch (ConfigurationException | RuntimeException e) {
            logger.debug(e.getMessage(), e);
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
        } catch (ConfigurationException | RuntimeException e) {
            logger.debug(e.getMessage(), e);
            nss = new HashMap<>();
        }
        return nss;

    }

    /**
     * Check task for non-open step state and step has a regular user assigned.
     *
     * @param task
     *            task to check
     * @return boolean
     */
    private boolean isNonOpenStateAndHasRegularUser(Task task) {
        return (!TaskStatus.OPEN.equals(task.getProcessingStatusEnum())) && (Objects.nonNull(task.getProcessingUser()))
                && (task.getProcessingUser().getId() != 0)
                && (Objects.nonNull(ServiceManager.getUserService().getFullName(task.getProcessingUser())));
    }

}
