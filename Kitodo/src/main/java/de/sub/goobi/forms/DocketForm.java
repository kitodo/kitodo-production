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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.helper.SelectItemList;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.ProcessService;

@Named("DocketForm")
@SessionScoped
public class DocketForm extends BaseForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Docket myDocket = new Docket();
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(DocketForm.class);

    private String docketListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String docketEditPath = MessageFormat.format(REDIRECT_PATH, "docketEdit");

    @Named("ProjekteForm")
    private ProjekteForm projectForm;

    /**
     * Default constructor with inject project form that also sets the LazyDTOModel
     * instance of this bean.
     * 
     * @param projectForm
     *            managed bean
     */
    @Inject
    public DocketForm(ProjekteForm projectForm) {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getDocketService()));
        this.projectForm = projectForm;
    }

    /**
     * Creates a new Docket.
     *
     * @return the navigation String
     */
    public String newDocket() {
        this.myDocket = new Docket();
        return docketEditPath;
    }

    /**
     * Save docket.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            if (hasValidRulesetFilePath(myDocket, ConfigCore.getParameter(Parameters.DIR_XSLT)) ) {
                if (existsDocketWithSameName()) {
                    Helper.setErrorMessage("docketTitleDuplicated");
                    return null;
                }
                serviceManager.getDocketService().save(myDocket);
                return docketListPath;
            } else {
                Helper.setErrorMessage("docketNotFound");
                return null;
            }
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("docket") }, logger, e);
            return null;
        }
    }

    private boolean hasValidRulesetFilePath(Docket d, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + d.getFile());
        return rulesetFile.exists();
    }

    /**
     * Delete docket.
     */
    public void deleteDocket() {
        try {
            if (hasAssignedProcesses(myDocket)) {
                Helper.setErrorMessage("docketInUse");
            } else {
                this.serviceManager.getDocketService().remove(this.myDocket);
            }
        } catch (DataException e) {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("docket") }, logger, e);
        }
    }

    private boolean existsDocketWithSameName() {
        List<Docket> dockets = serviceManager.getDocketService().getByTitle(this.myDocket.getTitle());
        if (dockets.isEmpty()) {
            return false;
        } else {
            if (Objects.nonNull(this.myDocket.getId())) {
                if (dockets.size() == 1) {
                    return !dockets.get(0).getId().equals(this.myDocket.getId());
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
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
            setSaveDisabled(true);
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

    /**
     * Set docket by ID.
     *
     * @param docketID
     *          ID of docket to set.
     */
    public void setDocketById(int docketID) {
        try {
            setMyDocket(serviceManager.getDocketService().getById(docketID));
        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to find docket with ID " + docketID, logger, e);
        }
    }

    public void setMyDocket(Docket docket) {
        this.myDocket = docket;
    }

    /**
     * Get all available clients.
     *
     * @return list of Client objects
     */
    public List<SelectItem> getClients() {
        return SelectItemList.getClients();
    }

    /**
     * Get list of docket filenames.
     *
     * @return list of docket filenames
     */
    public List getDocketFiles() {
        try (Stream<Path> docketPaths = Files.walk(Paths.get(ConfigCore.getParameter(Parameters.DIR_XSLT)))) {
            return docketPaths
                    .filter(s -> s.toString().endsWith(".xsl"))
                    .map(Path::getFileName)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("dockets")}, logger, e);
            return new ArrayList<>();
        }
    }
}
