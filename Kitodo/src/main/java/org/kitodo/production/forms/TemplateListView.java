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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TemplateService;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("TemplateListView")
@ViewScoped
public class TemplateListView extends BaseListView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "projects") + "&tab=templateTab";

    private static final Logger logger = LogManager.getLogger(TemplateEditView.class);

    private boolean showInactiveTemplates = false;
    private List<String> templateFilters;
    private List<String> selectedTemplateFilters;

    private static final String DEACTIVATED_TEMPLATES_FILTER = "deactivatedTemplates";
    private Map<Integer,Boolean> templateUsageMap;

    /**
     * Initialize list of template filters (currently only 'deactivated templates').
     */
    @PostConstruct
    public void init() {
        setLazyBeanModel(new LazyBeanModel(ServiceManager.getTemplateService()));
        templateFilters = new LinkedList<>();
        templateFilters.add(DEACTIVATED_TEMPLATES_FILTER);
        selectedTemplateFilters = new LinkedList<>();
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();

        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("template"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("template");
    }

    /**
     * Check if inactive templates should be shown.
     *
     * @return true or false
     */
    public boolean isShowInactiveTemplates() {
        return this.showInactiveTemplates;
    }

    /**
     * Set if inactive templates should be shown.
     *
     * @param showInactiveTemplates
     *            true or false
     */
    public void setShowInactiveTemplates(boolean showInactiveTemplates) {
        this.showInactiveTemplates = showInactiveTemplates;
        ServiceManager.getTemplateService().setShowInactiveTemplates(showInactiveTemplates);
    }

    /**
     * Create new template.
     *
     * @return page
     */
    public String newTemplate() {
        return TemplateEditView.VIEW_PATH;
    }

    /**
     * Remove template if there is no assigned processes.
     */
    public void delete(Template template) {
        if (!template.getProcesses().isEmpty()) {
            Helper.setErrorMessage("processAssignedError");
        } else {
            try {
                TemplateService.deleteTemplate(template);
            } catch (DAOException | IOException e) {
                Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.TEMPLATE.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Returns a read handle for the SVG image of this production template's
     * workflow. If the file cannot be read (due to an error), returns an empty
     * input stream.
     *
     * @return read file handle for the SVG
     */
    public InputStream getDiagramImage(String title) {
        if (Objects.nonNull(title)) {
            return ServiceManager.getTemplateService().getTasksDiagram(title);
        }
        return ServiceManager.getTemplateService().getTasksDiagram("");
    }

    /**
     * Check and return whether the template with the provided ID 'templateId' is used by any processes.
     *
     * @param templateId
     *          ID of template to check
     * @return whether template is used by any processes or not
     */
    public boolean isTemplateUsed(int templateId) {
        if (Objects.isNull(templateUsageMap)) {
            templateUsageMap = ServiceManager.getTemplateService().getTemplateUsageMap();
        }
        Boolean isUsed = templateUsageMap.get(templateId);
        return Boolean.TRUE.equals(isUsed);
    }

    /**
     * Get templateFilters.
     *
     * @return value of templateFilters
     */
    public List<String> getTemplateFilters() {
        return templateFilters;
    }

    /**
     * Set templateFilters.
     *
     * @param templateFilters as list of Strings
     */
    public void setTemplateFilters(List<String> templateFilters) {
        this.templateFilters = templateFilters;
    }

    /**
     * Get selectedTemplateFilters.
     *
     * @return value of selectedTemplateFilters
     */
    public List<String> getSelectedTemplateFilters() {
        return selectedTemplateFilters;
    }

    /**
     * Set selectedTemplateFilters.
     *
     * @param selectedTemplateFilters as list of Strings
     */
    public void setSelectedTemplateFilters(List<String> selectedTemplateFilters) {
        this.selectedTemplateFilters = selectedTemplateFilters;
    }

    /**
     * Event listener for template filter changed event.
     */
    public void templateFiltersChanged() {
        setShowInactiveTemplates(selectedTemplateFilters.contains(DEACTIVATED_TEMPLATES_FILTER));
    }

    /**
     * The set of allowed sort fields (columns) to sanitize the URL query parameter "sortField".
     * 
     * @return the set of allowed sort fields (columns)
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of("title", "ruleset.title", "active");
    }
}
