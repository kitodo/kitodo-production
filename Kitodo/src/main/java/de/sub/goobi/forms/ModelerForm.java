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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.services.ServiceManager;

@Named("ModelerForm")
@SessionScoped
public class ModelerForm implements Serializable {
    private static final long serialVersionUID = -3635859478787639614L;
    private String xmlDiagram;
    private String xmlDiagramName;
    private String newXMLDiagramName;
    private List<URI> xmlDiagramNamesURI;
    private Map<String, String> xmlDiagramNames = new TreeMap<>();
    private String diagramsFolder = ConfigCore.getKitodoDiagramDirectory();
    private static final Logger logger = LogManager.getLogger(ModelerForm.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    ModelerForm() {
        xmlDiagramNamesURI = serviceManager.getFileService().getSubUris(new File(diagramsFolder).toURI());
        // TODO: this needs to be removed after base file is stored inside the app
        for (URI uri : xmlDiagramNamesURI) {
            String fileName = serviceManager.getFileService().getFileNameWithExtension(uri);
            if (!fileName.equals("base.bpmn20.xml")) {
                xmlDiagramNames.put(decodeXMLDiagramName(fileName), fileName);
            }
        }
    }

    /**
     * Get content of XML diagram file.
     * 
     * @return content of XML diagram file as String
     */
    public String getXmlDiagram() {
        return xmlDiagram;
    }

    /**
     * Set content of XML diagram file.
     * 
     * @param xmlDiagram
     *            content of XML diagram as String
     */
    public void setXmlDiagram(String xmlDiagram) {
        this.xmlDiagram = xmlDiagram;
    }

    /**
     * Get name of XML diagram file.
     * 
     * @return name of XML diagram file as String
     */
    public String getXmlDiagramName() {
        return xmlDiagramName;
    }

    /**
     * Set name of XML diagram file.
     * 
     * @param xmlDiagramName
     *            name of XML diagram file as String
     */
    public void setXmlDiagramName(String xmlDiagramName) {
        this.xmlDiagramName = xmlDiagramName;
    }

    /**
     * Get new name of XML diagram file as String. It is used for file creation.
     * 
     * @return new name of XML diagram file as String
     */
    public String getNewXMLDiagramName() {
        return newXMLDiagramName;
    }

    /**
     * Set new name of XML diagram file as String. It is used for file creation.
     * 
     * @param newXMLDiagramName
     *            new name of XML diagram file as String
     */
    public void setNewXMLDiagramName(String newXMLDiagramName) {
        this.newXMLDiagramName = newXMLDiagramName;
    }

    /**
     * Get List of URIs for collection of the names of XML diagrams.
     * 
     * @return List of URIs
     */
    public List<URI> getXmlDiagramNamesURI() {
        return xmlDiagramNamesURI;
    }

    /**
     * Set List of URIs for collection of the names of XML diagrams.
     * 
     * @param xmlDiagramNamesURI
     *            as List of URIs
     */
    public void setXmlDiagramNamesURI(List<URI> xmlDiagramNamesURI) {
        this.xmlDiagramNamesURI = xmlDiagramNamesURI;
    }

    /**
     * Get Map of XML diagrams' names. Key store the name without extension and
     * value store name with extenstion ".bpmn20.xml".
     * 
     * @return Map of Strings
     */
    public Map<String, String> getXmlDiagramNames() {
        return xmlDiagramNames;
    }

    /**
     * Set Map of XML diagrams' names. Key store the name without extension and
     * value store name with extenstion ".bpmn20.xml".
     * 
     * @param xmlDiagramNames
     *            as Map of Strings
     */
    public void setXmlDiagramNames(Map<String, String> xmlDiagramNames) {
        this.xmlDiagramNames = xmlDiagramNames;
    }

    /**
     * Create XML diagram. Method first checks if file already exists. If not it
     * loads the base diagram content, next create new file with given name, saves
     * content to this file, and at the end it opens it in the editor.
     */
    public void createXMLDiagram() {
        for (String value : xmlDiagramNames.values()) {
            if (value.equals(encodeXMLDiagramName(newXMLDiagramName))) {
                logger.error("Diagram with name \"" + newXMLDiagramName + "\" already exists!");
                return;
            }
        }
        // TODO: this one needs to be stored inside the WAR file
        readXMLDiagram("base.bpmn20.xml");
        xmlDiagramName = newXMLDiagramName;
        newXMLDiagramName = "";
        try {
            serviceManager.getFileService().createResource(new File(diagramsFolder).toURI(),
                    encodeXMLDiagramName(xmlDiagramName));
            xmlDiagramNames.put(decodeXMLDiagramName(xmlDiagramName), encodeXMLDiagramName(xmlDiagramName));
        } catch (IOException e) {
            logger.error(e);
        }
        saveXMLDiagram();
        readXMLDiagram();
    }

    /**
     * Read XML for file chosen out of the select list.
     */
    public void readXMLDiagram() {
        readXMLDiagram(xmlDiagramName);
    }

    private void readXMLDiagram(String xmlDiagramName) {
        try (InputStream inputStream = serviceManager.getFileService()
                .read(new File(diagramsFolder + encodeXMLDiagramName(xmlDiagramName)).toURI());
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            xmlDiagram = sb.toString();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * Save updated content of the diagram.
     */
    public void save() {
        xmlDiagram = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("xml");
        saveXMLDiagram();
    }

    private String decodeXMLDiagramName(String xmlDiagramName) {
        if (xmlDiagramName.contains(".bpmn20.xml")) {
            return xmlDiagramName.replace(".bpmn20.xml", "");
        }
        return xmlDiagramName;

    }

    private String encodeXMLDiagramName(String xmlDiagramName) {
        if (!xmlDiagramName.contains(".bpmn20.xml")) {
            return xmlDiagramName + ".bpmn20.xml";
        }
        return xmlDiagramName;
    }

    void saveXMLDiagram() {
        try (OutputStream outputStream = serviceManager.getFileService()
                .write(new File(diagramsFolder + encodeXMLDiagramName(xmlDiagramName)).toURI());
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(xmlDiagram);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
