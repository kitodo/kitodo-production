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

package org.kitodo.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.BasisForm;
import de.sub.goobi.helper.Helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.kitodo.workflow.model.Reader;
import org.kitodo.workflow.model.beans.Diagram;

@Named("WorkflowForm")
@SessionScoped
public class WorkflowForm extends BasisForm {

    private static final long serialVersionUID = 2865600843136821176L;
    private static final Logger logger = LogManager.getLogger(WorkflowForm.class);
    private Workflow workflow = new Workflow();
    private transient ServiceManager serviceManager = new ServiceManager();
    private String svgDiagram;
    private String xmlDiagram;
    private String xmlDiagramName;
    private static final String diagramsFolder = ConfigCore.getKitodoDiagramDirectory();
    private static final String BPMN_EXTENSION = ".bpmn20.xml";
    private String workflowListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String workflowEditPath = MessageFormat.format(REDIRECT_PATH, "workflowEdit");

    /**
     * Constructor.
     */
    public WorkflowForm() {
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getWorkflowService()));
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
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    void saveSVGDiagram() {
        try (OutputStream outputStream = serviceManager.getFileService()
                .write(new File(diagramsFolder + decodeXMLDiagramName(xmlDiagramName) + ".svg").toURI());
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(svgDiagram);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    void saveXMLDiagram() {
        try (OutputStream outputStream = serviceManager.getFileService()
                .write(new File(diagramsFolder + encodeXMLDiagramName(xmlDiagramName)).toURI());
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(xmlDiagram);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Save workflow.
     */
    public void save() {
        saveFiles();
        saveWorkflow();
    }

    /**
     * Save workflow and redirect to list view.
     *
     * @return url to list view
     */
    public String saveAndRedirect() {
        save();
        return workflowListPath;
    }

    /**
     * Save updated content of the diagram.
     */
    private void saveFiles() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();
        svgDiagram = requestParameterMap.get("svg");
        if (Objects.nonNull(svgDiagram)) {
            saveSVGDiagram();
        }

        xmlDiagram = requestParameterMap.get("xml");
        if (Objects.nonNull(xmlDiagram)) {
            saveXMLDiagram();
        }
    }

    private String decodeXMLDiagramName(String xmlDiagramName) {
        if (xmlDiagramName.contains(BPMN_EXTENSION)) {
            return xmlDiagramName.replace(BPMN_EXTENSION, "");
        }
        return xmlDiagramName;

    }

    private String encodeXMLDiagramName(String xmlDiagramName) {
        if (!xmlDiagramName.contains(BPMN_EXTENSION)) {
            return xmlDiagramName + BPMN_EXTENSION;
        }
        return xmlDiagramName;
    }

    private void saveWorkflow() {
        String decodedXMLDiagramName = decodeXMLDiagramName(xmlDiagramName);
        try {
            Reader reader = new Reader(decodedXMLDiagramName);
            Diagram diagram = reader.getWorkflow();
            this.workflow.setTitle(diagram.getId());
            if (isWorkflowAlreadyInUse(this.workflow)) {
                this.workflow.setActive(false);
                Workflow newWorkflow = new Workflow(diagram.getId(), decodedXMLDiagramName);
                serviceManager.getWorkflowService().save(newWorkflow);
            }
            serviceManager.getWorkflowService().save(this.workflow);
        } catch (DataException | IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    private boolean isWorkflowAlreadyInUse(Workflow workflow) {
        return !workflow.getTemplates().isEmpty();
    }

    /**
     * Create new workflow.
     *
     * @return page
     */
    public String newWorkflow() {
        this.workflow = new Workflow();
        return workflowEditPath + "&id=" + (Objects.isNull(this.workflow.getId()) ? 0 : this.workflow.getId());
    }

    /**
     * Method being used as viewAction for workflow edit form. If the given
     * parameter 'id' is '0', the form for creating a new workflow will be
     * displayed.
     *
     * @param id
     *            of the workflow to load
     */
    public void loadWorkflow(int id) {
        try {
            if (id != 0) {
                setWorkflow(this.serviceManager.getWorkflowService().getById(id));
                this.xmlDiagramName = this.workflow.getFileName();
                readXMLDiagram(this.workflow.getFileName());
            } else {
                newWorkflow();
            }
            setSaveDisabled(false);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("workflow"), id }, logger, e);
        }
    }

    /**
     * Get workflow.
     *
     * @return value of workflow
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * Set workflow.
     *
     * @param workflow
     *            as Workflow
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
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
     * Get content of SVG diagram file.
     *
     * @return content of SVG diagram file as String
     */
    public String getSvgDiagram() {
        return svgDiagram;
    }

    /**
     * Set content of SVG diagram file.
     *
     * @param svgDiagram
     *            content of SVG diagram as String
     */
    public void setSvgDiagram(String svgDiagram) {
        this.svgDiagram = svgDiagram;
    }
}
