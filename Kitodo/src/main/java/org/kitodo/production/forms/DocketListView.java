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
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("DocketListView")
@ViewScoped
public class DocketListView extends BaseListView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "projects") + "&tab=docketTab";

    private static final Logger logger = LogManager.getLogger(DocketEditView.class);

    /**
     * Initialize Docket form.
     */
    @PostConstruct
    public void init() {
        setLazyBeanModel(new LazyBeanModel(ServiceManager.getDocketService()));
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();

        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("docket"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("docket");
    }

    /**
     * Creates a new Docket.
     *
     * @return the navigation String
     */
    public String newDocket() {
        return DocketEditView.VIEW_PATH;
    }

    /**
     * Delete docket.
     */
    public void delete(Docket docket) {
        try {
            if (hasAssignedProcessesOrTemplates(docket.getId())) {
                Helper.setErrorMessage("docketInUse");
            } else {
                ServiceManager.getDocketService().remove(docket);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.DOCKET.getTranslationSingular() }, logger,
                e);
        }
    }

    private boolean hasAssignedProcessesOrTemplates(int docketId) throws DAOException {
        return !ServiceManager.getProcessService().findByDocket(docketId).isEmpty()
                || !ServiceManager.getTemplateService().findByDocket(docketId).isEmpty();
    }

    /**
     * The set of allowed sort fields (columns) to sanitize the URL query parameter "sortField".
     * 
     * @return the set of allowed sort fields (columns)
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of("title", "file");
    }

}
