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

package org.kitodo.production.forms;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.enums.WorkflowStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("WorkflowListView")
@ViewScoped
public class WorkflowListView extends BaseListView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "projects") + "&tab=workflowTab";

    private static final Logger logger = LogManager.getLogger(WorkflowListView.class);
  
    private final transient FileService fileService = ServiceManager.getFileService();
    
    /**
     * Initialize Workflow list view.
     */
    @PostConstruct
    public void init() {
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getWorkflowService()));

        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("workflow"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("workflow");

        if (selectedColumns.stream().anyMatch(column -> "workflow.title".equals(column.getTitle()))) {
            // if title column is visible, sort by title by default
            sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
        }
    }

    /**
     * Archive active workflow.
     */
    public void archive(Workflow workflow) {
        workflow.setStatus(WorkflowStatus.ARCHIVED);
        try {
            ServiceManager.getWorkflowService().saveWorkflow(workflow);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Remove workflow if no template is assigned to it.
     */
    public void delete(Workflow workflow) {
        if (!workflow.getTemplates().isEmpty()) {
            Helper.setErrorMessage("templateAssignedError");
        } else {
            try {
                ServiceManager.getWorkflowService().remove(workflow);

                String diagramDirectory = ConfigCore.getKitodoDiagramDirectory();
                URI svgDiagramURI = new File(diagramDirectory 
                        + WorkflowEditView.decodeXMLDiagramName(workflow.getTitle()) + WorkflowEditView.SVG_EXTENSION).toURI();
                URI xmlDiagramURI = new File(diagramDirectory + WorkflowEditView.encodeXMLDiagramName(workflow.getTitle()))
                        .toURI();

                fileService.delete(svgDiagramURI);
                fileService.delete(xmlDiagramURI);
            } catch (DAOException | IOException e) {
                Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.WORKFLOW.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Create new workflow.
     *
     * @return page
     */
    public String newWorkflow() {
        return WorkflowEditView.VIEW_PATH;
    }

    /**
     * The set of allowed sort fields (columns) to sanitize the URL query parameter "sortField".
     * 
     * @return the set of allowed sort fields (columns)
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        Map<String, String> fieldsToColumns = Map.ofEntries(
            Map.entry("title", "workflow.title"),
            Map.entry("status", "workflow.status")
        );

        return fieldsToColumns.entrySet().stream()
            .filter(entry ->  selectedColumns.stream().anyMatch(column -> entry.getValue().equals(column.getTitle())))
            .map(entry -> entry.getKey())
            .collect(Collectors.toSet());
    }

}
