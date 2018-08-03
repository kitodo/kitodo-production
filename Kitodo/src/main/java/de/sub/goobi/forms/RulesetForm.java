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
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.helper.SelectItemList;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;

@Named("RulesetForm")
@SessionScoped
public class RulesetForm extends BasisForm {
    private static final long serialVersionUID = -445707928042517243L;
    private Ruleset ruleset;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(RulesetForm.class);

    private String rulesetListPath = MessageFormat.format(REDIRECT_PATH, "projects");
    private String rulesetEditPath = MessageFormat.format(REDIRECT_PATH, "rulesetEdit");

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
    public RulesetForm(ProjekteForm projectForm) {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getRulesetService()));
        this.projectForm = projectForm;
    }

    /**
     * Initialize new Ruleset.
     *
     * @return page
     */
    public String createNewRuleset() {
        this.ruleset = new Ruleset();
        return rulesetEditPath;
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String saveRuleset() {
        try {
            if (hasValidRulesetFilePath(this.ruleset, ConfigCore.getParameter(Parameters.DIR_RULESETS))) {
                serviceManager.getRulesetService().save(this.ruleset);
                return rulesetListPath;
            } else {
                Helper.setErrorMessage("rulesetNotFound");
                return null;
            }
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("ruleset") }, logger, e);
            return null;
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

    /**
     * Remove.
     *
     * @return redirect link or empty String
     */
    public String removeRuleset() {
        try {
            if (hasAssignedProcesses(this.ruleset)) {
                Helper.setErrorMessage("rulesetInUse");
                return null;
            } else {
                serviceManager.getRulesetService().remove(this.ruleset);
            }
        } catch (DataException e) {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("ruleset") }, logger, e);
            return null;
        }
        return rulesetListPath;
    }

    private boolean hasAssignedProcesses(Ruleset ruleset) throws DataException {
        Integer number = serviceManager.getProcessService().findByRuleset(ruleset).size();
        return number > 0;
    }

    /**
     * Method being used as viewAction for ruleset edit form. If given parameter
     * 'id' is '0', the form for creating a new ruleset will be displayed.
     *
     * @param id
     *            ID of the ruleset to load
     */
    public void loadRuleset(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setRuleset(this.serviceManager.getRulesetService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("regelsatz"), id }, logger,
                e);
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
     * Get all available clients.
     *
     * @return list of Client objects
     */
    public List<SelectItem> getClients() {
        return SelectItemList.getClients();
    }

    /**
     * Get list of ruleset filenames.
     *
     * @return list of ruleset filenames
     */
    public List getRulesetFilenames() {
        try (Stream<Path> rulesetPaths = Files.walk(Paths.get(ConfigCore.getParameter(Parameters.DIR_RULESETS)))) {
            return rulesetPaths
                    .filter(f -> f.toString().endsWith(".xml"))
                    .map(Path::getFileName)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("rulesets")}, logger, e);
            return new ArrayList();
        }
    }
}
