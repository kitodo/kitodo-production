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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.helper.Helper;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.model.Reader;
import org.kitodo.workflow.model.beans.Diagram;

@Named("WorkflowForm")
@SessionScoped
public class WorkflowForm extends BaseForm {

    private static final long serialVersionUID = 2865600843136821176L;
    private static final Logger logger = LogManager.getLogger(WorkflowForm.class);
    private Workflow workflow = new Workflow();
    private transient FileService fileService = ServiceManager.getFileService();
    private String svgDiagram;
    private String xmlDiagram;
    private String newFileName;
    private static final String BPMN_EXTENSION = ".bpmn20.xml";
    private static final String SVG_EXTENSION = ".svg";
    private static final String SVG_DIAGRAM_URI = "svgDiagramURI";
    private static final String XML_DIAGRAM_URI = "xmlDiagramURI";
    private String workflowListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String workflowEditPath = MessageFormat.format(REDIRECT_PATH, "workflowEdit");
    private Integer roleId;

    /**
     * Constructor.
     */
    public WorkflowForm() {
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getWorkflowService()));
    }

    /**
     * Read XML for file chosen out of the select list.
     */
    public void readXMLDiagram() {
        URI xmlDiagramURI = new File(
                ConfigCore.getKitodoDiagramDirectory() + encodeXMLDiagramName(this.workflow.getFileName())).toURI();

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
        try {
            if (saveFiles()) {
                saveWorkflow();
                return workflowListPath;
            } else {
                return this.stayOnCurrentPage;
            }
        } catch (IOException e) {
            Helper.setErrorMessage("errorDiagramFile", new Object[] {this.workflow.getTitle() }, logger, e);
            return this.stayOnCurrentPage;
        } catch (WorkflowException e) {
            Helper.setErrorMessage("errorDiagramTask", new Object[] {this.workflow.getTitle() }, logger, e);
            return this.stayOnCurrentPage;
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
                ServiceManager.getWorkflowService().remove(this.workflow);

                String diagramDirectory = ConfigCore.getKitodoDiagramDirectory();
                URI svgDiagramURI = new File(
                        diagramDirectory + decodeXMLDiagramName(this.workflow.getFileName()) + SVG_EXTENSION).toURI();
                URI xmlDiagramURI = new File(diagramDirectory + encodeXMLDiagramName(this.workflow.getFileName()))
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
    private boolean saveFiles() throws IOException, WorkflowException {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap();

        Map<String, URI> diagramsUris = getDiagramUris();

        if (isWorkflowAlreadyInUse(this.workflow)) {
            this.newFileName = decodeXMLDiagramName(this.workflow.getFileName()) + "_" + Helper.generateRandomString(3);
            diagramsUris = getDiagramUris(this.newFileName);
        }

        URI svgDiagramURI = diagramsUris.get(SVG_DIAGRAM_URI);
        URI xmlDiagramURI = diagramsUris.get(XML_DIAGRAM_URI);

        xmlDiagram = requestParameterMap.get("editForm:workflowTabView:xmlDiagram");
        if (Objects.nonNull(xmlDiagram)) {
            svgDiagram = StringUtils.substringAfter(xmlDiagram, "kitodo-diagram-separator");
            xmlDiagram = StringUtils.substringBefore(xmlDiagram, "kitodo-diagram-separator");

            Reader reader = new Reader(new ByteArrayInputStream(xmlDiagram.getBytes(StandardCharsets.UTF_8)));
            reader.validateWorkflowTasks();

            saveFile(svgDiagramURI, svgDiagram);
            saveFile(xmlDiagramURI, xmlDiagram);
        }

        return fileService.fileExist(xmlDiagramURI) && fileService.fileExist(svgDiagramURI);
    }

    private Map<String, URI> getDiagramUris() {
        return getDiagramUris(this.workflow.getFileName());
    }

    private Map<String, URI> getDiagramUris(String fileName) {
        String diagramDirectory = ConfigCore.getKitodoDiagramDirectory();
        URI svgDiagramURI = new File(diagramDirectory + decodeXMLDiagramName(fileName) + SVG_EXTENSION)
                .toURI();
        URI xmlDiagramURI = new File(diagramDirectory + encodeXMLDiagramName(fileName)).toURI();

        Map<String, URI> diagramUris = new HashMap<>();
        diagramUris.put(SVG_DIAGRAM_URI, svgDiagramURI);
        diagramUris.put(XML_DIAGRAM_URI, xmlDiagramURI);
        return diagramUris;
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
                Workflow newWorkflow = new Workflow(diagram.getId(), this.newFileName);
                newWorkflow.setActive(this.workflow.isActive());
                newWorkflow.setReady(this.workflow.isReady());
                newWorkflow.setClient(this.workflow.getClient());
                this.workflow.setActive(false);
                ServiceManager.getWorkflowService().save(newWorkflow);
            }
            ServiceManager.getWorkflowService().save(this.workflow);
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
        this.workflow.setClient(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
        return workflowEditPath + "&id=" + (Objects.isNull(this.workflow.getId()) ? 0 : this.workflow.getId());
    }

    /**
     * Duplicate the selected workflow.
     *
     * @param itemId
     *            ID of the workflow to duplicate
     * @return page address; either redirect to the edit workflow page or return
     *         'null' if the workflow could not be retrieved, which will prompt JSF
     *         to remain on the same page and reuse the bean.
     */
    public String duplicate(Integer itemId) {
        try {
            Workflow baseWorkflow = ServiceManager.getWorkflowService().getById(itemId);

            Map<String, URI> diagramsUris = getDiagramUris(baseWorkflow.getFileName());

            URI svgDiagramURI = diagramsUris.get(SVG_DIAGRAM_URI);
            URI xmlDiagramURI = diagramsUris.get(XML_DIAGRAM_URI);

            this.workflow = ServiceManager.getWorkflowService().duplicateWorkflow(baseWorkflow);
            Map<String, URI> diagramsCopyUris = getDiagramUris();

            URI svgDiagramCopyURI = diagramsCopyUris.get(SVG_DIAGRAM_URI);
            URI xmlDiagramCopyURI = diagramsCopyUris.get(XML_DIAGRAM_URI);

            try (InputStream svgInputStream = ServiceManager.getFileService().read(svgDiagramURI);
                    InputStream xmlInputStream = ServiceManager.getFileService().read(xmlDiagramURI)) {
                saveFile(svgDiagramCopyURI, IOUtils.toString(svgInputStream, StandardCharsets.UTF_8));
                this.xmlDiagram = IOUtils.toString(xmlInputStream, StandardCharsets.UTF_8);
                saveFile(xmlDiagramCopyURI, this.xmlDiagram);
            } catch (IOException e) {
                Helper.setErrorMessage("unableToDuplicateWorkflow", logger, e);
                return this.stayOnCurrentPage;
            }
            return workflowEditPath;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DUPLICATE, new Object[] {ObjectType.WORKFLOW.getTranslationSingular() },
                logger, e);
            return this.stayOnCurrentPage;
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
            setWorkflow(ServiceManager.getWorkflowService().getById(id));
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
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setWorkflow(ServiceManager.getWorkflowService().getById(id));
                readXMLDiagram();
            }
            setSaveDisabled(false);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.WORKFLOW.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Get role id.
     *
     * @return value of roleId
     */
    public Integer getRoleId() {
        return roleId;
    }

    /**
     * Set role idd.
     * 
     * @param roleId
     *            as Integer.
     */
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    /**
     * Get hidden list of roles.
     *
     * @return hidden list of roles
     */
    public List<SelectItem> getRoles() {
        List<SelectItem> selectItems = new ArrayList<>();

        List<Role> roles = ServiceManager.getRoleService()
                .getAllRolesByClientId(ServiceManager.getUserService().getSessionClientId());
        for (Role role : roles) {
            selectItems.add(new SelectItem(role.getId(), role.getTitle(), null));
        }
        return selectItems;
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
