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
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;

@Named("RulesetForm")
@SessionScoped
public class RulesetForm extends BaseForm {
    private Ruleset ruleset;
    private static final Logger logger = LogManager.getLogger(RulesetForm.class);

    private final String rulesetEditPath = MessageFormat.format(REDIRECT_PATH, "rulesetEdit");

    @Named("ProjectForm")
    private final ProjectForm projectForm;

    /**
     * Default constructor with inject project form that also sets the
     * LazyBeanModel instance of this bean.
     *
     * @param projectForm
     *            managed bean
     */
    @Inject
    public RulesetForm(ProjectForm projectForm) {
        super();
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getRulesetService()));
        this.projectForm = projectForm;
    }

    /**
     * Initialize new Ruleset.
     *
     * @return page
     */
    public String createNewRuleset() {
        this.ruleset = new Ruleset();
        this.ruleset.setClient(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
        return rulesetEditPath;
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            if (hasValidRulesetFilePath(this.ruleset, ConfigCore.getParameter(ParameterCore.DIR_RULESETS))) {
                if (existsRulesetWithSameName()) {
                    Helper.setErrorMessage("rulesetTitleDuplicated");
                    return this.stayOnCurrentPage;
                }
                ServiceManager.getRulesetService().save(this.ruleset);
                return projectsPage;
            } else {
                Helper.setErrorMessage("rulesetNotFound", new Object[] {this.ruleset.getFile()});
                return this.stayOnCurrentPage;
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.RULESET.getTranslationSingular() }, logger,
                e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Delete ruleset.
     */
    public void delete() {
        try {
            if (hasAssignedProcessesOrTemplates(this.ruleset.getId())) {
                Helper.setErrorMessage("rulesetInUse");
            } else {
                ServiceManager.getRulesetService().remove(this.ruleset);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.RULESET.getTranslationSingular() }, logger,
                    e);
        }
    }

    /**
     * Checks that ruleset file exists.
     *
     * @param ruleset
     *            ruleset
     * @param pathToRulesets
     *            path to ruleset
     * @return true if ruleset file exists
     */
    private boolean hasValidRulesetFilePath(Ruleset ruleset, String pathToRulesets) {
        File rulesetFile = new File(pathToRulesets + ruleset.getFile());
        return rulesetFile.exists();
    }

    private boolean existsRulesetWithSameName() {
        List<Ruleset> rulesets = ServiceManager.getRulesetService()
                .getByTitleAndClient(this.ruleset.getTitle(), ServiceManager.getUserService()
                        .getSessionClientOfAuthenticatedUser());
        if (rulesets.isEmpty()) {
            return false;
        } else {
            if (Objects.nonNull(this.ruleset.getId())) {
                if (rulesets.size() == 1) {
                    return !rulesets.get(0).getId().equals(this.ruleset.getId());
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    private boolean hasAssignedProcessesOrTemplates(int rulesetId) throws DAOException {
        return !ServiceManager.getProcessService().findByRuleset(rulesetId).isEmpty()
                || !ServiceManager.getTemplateService().findByRuleset(rulesetId).isEmpty();
    }

    /**
     * Method being used as viewAction for ruleset edit form. If given parameter
     * 'id' is '0', the form for creating a new ruleset will be displayed.
     *
     * @param id
     *            ID of the ruleset to load
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setRuleset(ServiceManager.getRulesetService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.RULESET.getTranslationSingular(), id },
                logger, e);
        }
    }

    /*
     * Getter und Setter
     */

    public Ruleset getRuleset() {
        return this.ruleset;
    }

    public void setRuleset(Ruleset inPreference) {
        this.ruleset = inPreference;
    }

    /**
     * Set ruleset by ID.
     *
     * @param rulesetID
     *            ID of the ruleset to set.
     */
    public void setRulesetById(int rulesetID) {
        try {
            setRuleset(ServiceManager.getRulesetService().getById(rulesetID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                new Object[] {ObjectType.RULESET.getTranslationSingular(), rulesetID }, logger, e);
        }
    }

    /**
     * Get list of ruleset filenames.
     *
     * @return list of ruleset filenames
     */
    public List<Path> getRulesetFilenames() {
        try (Stream<Path> rulesetPaths = Files.walk(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_RULESETS)))) {
            return rulesetPaths.filter(f -> f.toString().endsWith(".xml")).map(Path::getFileName).sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.RULESET.getTranslationPlural() },
                logger, e);
            return new ArrayList<>();
        }
    }
}
