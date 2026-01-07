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

import static java.util.Map.entry;

import java.util.Map;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("ProjectsTabView")
@ViewScoped
public class ProjectsTabView extends BaseForm {

    @Inject
    private ProjectListView projectListView;

    @Inject
    private TemplateListView templateListView;

    @Inject
    private WorkflowListView workflowListView;

    @Inject
    private DocketListView docketListView;

    @Inject
    private RulesetListView rulesetListView;

    @Inject
    private ImportConfigurationListView importConfigurationListView;

    @Inject
    private MappingFileListView mappingFileListView;

    @Inject
    private LtpValidationConfigurationListView ltpValidationConfigurationListView;

    /**
     * Apply view parameter "firstRow" to currently active list view depending on tab index.
     * 
     * @param firstRow the row index of the first row to be displayed in the active list view
     */
    @Override
    public void setFirstRowFromTemplate(String firstRow) {
        BaseForm activeListView = getActiveListView();

        if (Objects.nonNull(activeListView)) {
            activeListView.setFirstRowFromTemplate(firstRow);
        }
    }

    /**
     * Sets the sort by query parameters for the currently active list view.
     * 
     * @param field the sort by field
     * @param order the sort by order
     */
    public void setSortByFromTemplate(String field, String order) {
        BaseListView activeListView = getActiveListView();

        if (Objects.nonNull(activeListView)) {
            activeListView.setSortByFromTemplate(field, order);
        }
    }

    /**
     * Return the currently active list view.
     * 
     * @return the currently active list view
     */
    private BaseListView getActiveListView() {
        return Map.ofEntries(
            entry(0, projectListView),
            entry(1, templateListView),
            entry(2, workflowListView),
            entry(3, docketListView),
            entry(4, rulesetListView),
            entry(5, importConfigurationListView),
            entry(6, mappingFileListView),
            entry(7, ltpValidationConfigurationListView)
        ).get(getActiveTabIndex());
    }

}
