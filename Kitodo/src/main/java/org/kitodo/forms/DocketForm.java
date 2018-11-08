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
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;
import org.kitodo.helper.SelectItemList;
import org.kitodo.model.LazyDTOModel;

@Named("DocketForm")
@SessionScoped
public class DocketForm extends BaseForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Docket myDocket = new Docket();
    private static final Logger logger = LogManager.getLogger(DocketForm.class);

    private String docketListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String docketEditPath = MessageFormat.format(REDIRECT_PATH, "docketEdit");

    @Named("ProjectForm")
    private ProjectForm projectForm;

    /**
     * Default constructor with inject project form that also sets the LazyDTOModel
     * instance of this bean.
     * 
     * @param projectForm
     *            managed bean
     */
    @Inject
    public DocketForm(ProjectForm projectForm) {
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
        this.myDocket.setClient(serviceManager.getUserService().getSessionClientOfAuthenticatedUser());
        return docketEditPath;
    }

    /**
     * Save docket.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            if (hasValidRulesetFilePath(myDocket, ConfigCore.getParameter(ParameterCore.DIR_XSLT))) {
                if (existsDocketWithSameName()) {
                    Helper.setErrorMessage("docketTitleDuplicated");
                    return this.stayOnCurrentPage;
                }
                serviceManager.getDocketService().save(myDocket);
                return docketListPath;
            } else {
                Helper.setErrorMessage("docketNotFound");
                return this.stayOnCurrentPage;
            }
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.DOCKET.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    private boolean hasValidRulesetFilePath(Docket d, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + d.getFile());
        return rulesetFile.exists();
    }

    /**
     * Delete docket.
     */
    public void delete() {
        try {
            if (hasAssignedProcessesOrTemplates(this.myDocket.getId())) {
                Helper.setErrorMessage("docketInUse");
            } else {
                this.serviceManager.getDocketService().remove(this.myDocket);
            }
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.DOCKET.getTranslationSingular() }, logger,
                e);
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

    private boolean hasAssignedProcessesOrTemplates(int docketId) throws DataException {
        return !serviceManager.getProcessService().findByDocket(docketId).isEmpty()
                || !serviceManager.getTemplateService().findByDocket(docketId).isEmpty();
    }

    /**
     * Method being used as viewAction for docket edit form.
     *
     * @param id
     *            ID of the docket to load
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setMyDocket(this.serviceManager.getDocketService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.DOCKET.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Getter docket.
     *
     * @return Docket object
     */
    public Docket getMyDocket() {
        return this.myDocket;
    }

    /**
     * Set docket by ID.
     *
     * @param docketID
     *            ID of docket to set.
     */
    public void setDocketById(int docketID) {
        try {
            setMyDocket(serviceManager.getDocketService().getById(docketID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                new Object[] {ObjectType.DOCKET.getTranslationSingular(), docketID }, logger, e);
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
        try (Stream<Path> docketPaths = Files.walk(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_XSLT)))) {
            return docketPaths.filter(s -> s.toString().endsWith(".xsl")).map(Path::getFileName).sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.DOCKET.getTranslationPlural() }, logger,
                e);
            return new ArrayList<>();
        }
    }
}
