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
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.ProcessService;

@Named("DocketForm")
@SessionScoped
public class DocketForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Docket myDocket = new Docket();
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(DocketForm.class);

    @Inject
    @Named("ProjekteForm")
    private ProjekteForm projectForm;

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this
     * bean.
     */
    public DocketForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getDocketService()));
    }

    /**
     * Creates a new Docket.
     *
     * @return the navigation String
     */
    public String newDocket() {
        this.myDocket = new Docket();
        return redirectToEdit();
    }

    /**
     * Save docket.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            if (hasValidRulesetFilePath(myDocket, ConfigCore.getParameter("xsltFolder"))) {
                this.serviceManager.getDocketService().save(myDocket);
                return redirectToList();
            } else {
                Helper.setFehlerMeldung("DocketNotFound");
                return null;
            }
        } catch (DataException e) {
            Helper.setErrorMessage("fehlerNichtSpeicherbar", logger, e);
            return null;
        }
    }

    private boolean hasValidRulesetFilePath(Docket d, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + d.getFile());
        return rulesetFile.exists();
    }

    /**
     * Delete docket.
     *
     * @return page or empty String
     */
    public String deleteDocket() {
        try {
            if (hasAssignedProcesses(myDocket)) {
                Helper.setFehlerMeldung("DocketInUse");
                return null;
            } else {
                this.serviceManager.getDocketService().remove(this.myDocket);
            }
        } catch (DataException e) {
            Helper.setErrorMessage("fehlerNichtLoeschbar", logger, e);
            return null;
        }
        return "/pages/DocketList";
    }

    private boolean hasAssignedProcesses(Docket d) throws DataException {
        ProcessService processService = serviceManager.getProcessService();
        Integer number = processService.findByDocket(d).size();
        return number > 0;
    }

    /**
     * Method being used as viewAction for docket edit form.
     *
     * @param id
     *            ID of the docket to load
     */
    public void loadDocket(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setMyDocket(this.serviceManager.getDocketService().getById(id));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("docket"), id }, logger, e);
        }
    }

    /*
     * Getter und Setter
     */

    public Docket getMyDocket() {
        return this.myDocket;
    }

    public void setMyDocket(Docket docket) {
        Helper.getHibernateSession().clear();
        this.myDocket = docket;
    }

    // replace calls to this function with "/pages/DocketEdit" once we have
    // completely switched to the new frontend pages
    private String redirectToEdit() {
        try {
            String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referrer.substring(referrer.lastIndexOf('/') + 1);
            if (!callerViewId.isEmpty() && callerViewId.contains("projects.jsf")) {
                return "/pages/editDocket?" + REDIRECT_PARAMETER;
            } else {
                return "/pages/DocketEdit?" + REDIRECT_PARAMETER;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when "DocketForm" is
            // used from it's integration test
            // class "DocketFormIT", where no "FacesContext" is available!
            return "/pages/DocketEdit?" + REDIRECT_PARAMETER;
        }
    }


    // TODO:
    // replace calls to this function with "/pages/projects" once we have completely
    // switched to the new frontend pages
    private String redirectToList() {
        String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("referer");
        String callerViewId = referrer.substring(referrer.lastIndexOf('/') + 1);
        if (!callerViewId.isEmpty() && callerViewId.contains("editDocket.jsf")) {
            return "/pages/projects.jsf?id=" + projectForm.getActiveTabIndex() + "&" + REDIRECT_PARAMETER;
        } else {
            return "/pages/DocketList?" + REDIRECT_PARAMETER;
        }
    }
}
