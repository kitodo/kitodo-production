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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.model.Reader;
import org.kitodo.workflow.model.beans.Diagram;

@Named("WorkflowForm")
@SessionScoped
public class WorkflowForm extends BaseForm {

    private static final long serialVersionUID = 2865600843136821176L;
    private static final Logger logger = LogManager.getLogger(WorkflowForm.class);
    private Workflow workflow = new Workflow();
    private FileService fileService = serviceManager.getFileService();
    private String svgDiagram;
    private String xmlDiagram;
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
        URI xmlDiagramURI = new File(diagramsFolder + encodeXMLDiagramName(this.workflow.getFileName())).toURI();

        try (InputStream inputStream = fileService.read(xmlDiagramURI);
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

    /**
     * Save workflow and redirect to list view.
     *
     * @return url to list view
     */
    public String saveAndRedirect() {
        // FIXME: in this solution workflow is saved but redirect doesn't work
        boolean filesSaved = saveFiles();
        if (filesSaved) {
            saveWorkflow();
            return workflowListPath;
        } else {
            return null;
        }
    }

    /**
     * Remove workflow if no template is assigned to it.
     */
    public void delete() {
        if (!this.workflow.getTemplates().isEmpty()) {
            Helper.setErrorMessage("templateAssignedError");
        } else {
            try {
                serviceManager.getWorkflowService().remove(this.workflow);

                URI svgDiagramURI = new File(
                        diagramsFolder + decodeXMLDiagramName(this.workflow.getFileName()) + ".svg").toURI();
                URI xmlDiagramURI = new File(diagramsFolder + encodeXMLDiagramName(this.workflow.getFileName()))
                        .toURI();

                fileService.delete(svgDiagramURI);
                fileService.delete(xmlDiagramURI);
            } catch (DataException | IOException e) {
                Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.WORKFLOW.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Save content of the diagram files.
     *
     * @return true if save, false if not
     */
    private boolean saveFiles() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();

        if (isWorkflowAlreadyInUse(this.workflow)) {
            this.workflow.setFileName(decodeXMLDiagramName(this.workflow.getFileName()) + "_" + randomString(3));
        }
        URI svgDiagramURI = new File(diagramsFolder + decodeXMLDiagramName(this.workflow.getFileName()) + ".svg")
                .toURI();
        URI xmlDiagramURI = new File(diagramsFolder + encodeXMLDiagramName(this.workflow.getFileName())).toURI();

        xmlDiagram = requestParameterMap.get("diagram");
        if (Objects.nonNull(xmlDiagram)) {
            svgDiagram = StringUtils.substringAfter(xmlDiagram, "kitodo-diagram-separator");
            xmlDiagram = StringUtils.substringBefore(xmlDiagram, "kitodo-diagram-separator");

            saveFile(svgDiagramURI, svgDiagram);
            saveFile(xmlDiagramURI, xmlDiagram);
        }

        return fileService.fileExist(xmlDiagramURI) && fileService.fileExist(svgDiagramURI);
    }

    void saveFile(URI fileURI, String fileContent) {
        try (OutputStream outputStream = fileService.write(fileURI);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(fileContent);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
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
        String decodedXMLDiagramName = decodeXMLDiagramName(this.workflow.getFileName());
        try {
            Reader reader = new Reader(decodedXMLDiagramName);
            Diagram diagram = reader.getWorkflow();
            this.workflow.setTitle(diagram.getId());
            if (isWorkflowAlreadyInUse(this.workflow)) {
                this.workflow.setActive(false);
                Workflow newWorkflow = new Workflow(diagram.getId(), decodedXMLDiagramName);
                newWorkflow.setActive(this.workflow.isActive());
                newWorkflow.setReady(this.workflow.isReady());
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

    private static String randomString(int length) {
        final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
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
     * Duplicate the selected workflow.
     *
     * @param itemId
     *            ID of the workflow to duplicate
     * @return page address; either redirect to the edit workflow page or return
     *         'null' if the workflow could not be retrieved, which will prompt
     *         JSF to remain on the same page and reuse the bean.
     */
    public String duplicateWorkflow(Integer itemId) {
        try {
            Workflow baseWorkflow = serviceManager.getWorkflowService().getById(itemId);
            this.workflow = serviceManager.getWorkflowService().duplicateWorkflow(baseWorkflow);
            return workflowEditPath;
        } catch (DAOException e) {
            Helper.setErrorMessage("unableToDuplicateWorkflow", logger, e);
            return null;
        }
    }

    /**
     * Set workflow by id.
     *
     * @param id
     *            of workflow to set
     */
    public void setWorkflowById(int id) {
        try {
            setWorkflow(serviceManager.getWorkflowService().getById(id));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.WORKFLOW.getTranslationSingular(), id },
                logger, e);
        }
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
                readXMLDiagram();
            } else {
                newWorkflow();
            }
            setSaveDisabled(false);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.WORKFLOW.getTranslationSingular(), id },
                logger, e);
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
     * Get content of SVG diagram file.
     *
     * @return content of SVG diagram file as String
     */
    String getSvgDiagram() {
        return svgDiagram;
    }

    /**
     * Set content of SVG diagram file.
     *
     * @param svgDiagram
     *            content of SVG diagram as String
     */
    void setSvgDiagram(String svgDiagram) {
        this.svgDiagram = svgDiagram;
    }
}
