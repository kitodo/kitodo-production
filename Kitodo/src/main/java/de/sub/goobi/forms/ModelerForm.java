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
import java.util.ArrayList;
import java.util.List;

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
    private List<URI> xmlDiagramNamesURI;
    private List<String> xmlDiagramNames = new ArrayList<>();
    private String diagramsFolder = ConfigCore.getKitodoDiagramDirectory();
    private static final Logger logger = LogManager.getLogger(ModelerForm.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    ModelerForm() {
        xmlDiagramNamesURI = serviceManager.getFileService().getSubUris(new File(diagramsFolder).toURI());
        //TODO: this needs to be removed after base file is stored inside the app
        for (URI uri : xmlDiagramNamesURI) {
            String fileName = serviceManager.getFileService().getFileNameWithExtension(uri);
            if (!fileName.equals("base.bpmn20.xml")) {
                xmlDiagramNames.add(fileName);
            }
        }
    }

    public String getXmlDiagram() {
        return xmlDiagram;
    }

    public void setXmlDiagram(String xmlDiagram) {
        this.xmlDiagram = xmlDiagram;
    }

    public String getXmlDiagramName() {
        return xmlDiagramName;
    }

    public void setXmlDiagramName(String xmlDiagramName) {
        this.xmlDiagramName = xmlDiagramName;
    }

    public List<URI> getXmlDiagramNamesURI() {
        return xmlDiagramNamesURI;
    }

    public void setXmlDiagramNamesURI(List<URI> xmlDiagramNamesURI) {
        this.xmlDiagramNamesURI = xmlDiagramNamesURI;
    }

    public List<String> getXmlDiagramNames() {
        return xmlDiagramNames;
    }

    public void setXmlDiagramNames(List<String> xmlDiagramNames) {
        this.xmlDiagramNames = xmlDiagramNames;
    }

    public void createXMLDiagram() {
        //TODO: this one needs to be stored inside the WAR file
        readXMLDiagram("base.bpmn20.xml");
        try {
            serviceManager.getFileService().createResource(new File(diagramsFolder).toURI(), xmlDiagramName);
            xmlDiagramNames.add(xmlDiagramName);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void readXMLDiagram() {
        readXMLDiagram(xmlDiagramName);
    }

    private void readXMLDiagram(String xmlDiagramName) {
        try (InputStream inputStream = serviceManager.getFileService().read(new File(diagramsFolder + xmlDiagramName).toURI());
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

    public void save() {
        xmlDiagram = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("xml");
        saveXMLDiagram();
    }

    public void saveXMLDiagram() {
        try (OutputStream outputStream = serviceManager.getFileService().write(new File(diagramsFolder + xmlDiagramName).toURI());
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(xmlDiagram);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
