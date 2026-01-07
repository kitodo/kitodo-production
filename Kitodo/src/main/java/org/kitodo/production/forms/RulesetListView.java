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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("RulesetListView")
@ViewScoped
public class RulesetListView extends BaseListView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "projects") + "&tabIndex=4";

    private static final Logger logger = LogManager.getLogger(RulesetEditView.class);

    

    /**
     * Initialize Rulset form.
     */
    @PostConstruct
    public void init() {
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getRulesetService()));
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();

        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("ruleset"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("ruleset");
    }

    /**
     * Initialize new Ruleset.
     *
     * @return page
     */
    public String createNewRuleset() {
        return RulesetEditView.VIEW_PATH;
    }

    /**
     * Delete ruleset.
     */
    public void delete(Ruleset ruleset) {
        try {
            if (hasAssignedProcessesOrTemplates(ruleset.getId())) {
                Helper.setErrorMessage("rulesetInUse");
            } else {
                ServiceManager.getRulesetService().remove(ruleset);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.RULESET.getTranslationSingular() }, logger,
                    e);
        }
    }

    private boolean hasAssignedProcessesOrTemplates(int rulesetId) throws DAOException {
        return !ServiceManager.getProcessService().findByRuleset(rulesetId).isEmpty()
                || !ServiceManager.getTemplateService().findByRuleset(rulesetId).isEmpty();
    }

    /**
     * The set of allowed sort fields (columns) to sanitize the URL query parameter "sortField".
     * 
     * @return the set of allowed sort fields (columns)
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of("title", "file", "orderMetadataByRuleset");
    }

}
