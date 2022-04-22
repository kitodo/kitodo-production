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
import org.kitodo.data.database.beans.OCRWorkflow;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;

@Named("OCRWorkflowForm")
@SessionScoped
public class OCRWorkflowForm extends BaseForm {
    private OCRWorkflow ocrWorkflow;
    private static final Logger logger = LogManager.getLogger(OCRWorkflowForm.class);

    private final String rulesetEditPath = MessageFormat.format(REDIRECT_PATH, "ocrWorkflowEdit");

    @Named("ProjectForm")
    private final ProjectForm projectForm;

    /**
     * Default constructor with inject project form that also sets the LazyDTOModel
     * instance of this bean.
     *
     * @param projectForm
     *            managed bean
     */
    @Inject
    public OCRWorkflowForm(ProjectForm projectForm) {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getRulesetService()));
        this.projectForm = projectForm;
    }

    /**
     * Initialize new Ruleset.
     *
     * @return page
     */
    public String createNewOCRWorkflow() {
        this.ocrWorkflow = new OCRWorkflow();
        this.ocrWorkflow.setClient(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
        return rulesetEditPath;
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String save() {
        return "";
    }

    /**
     * Delete ocr workflow.
     */
    public void delete() {

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
        List<Ruleset> rulesets = ServiceManager.getRulesetService().getByTitle(this.ocrWorkflow.getTitle()).stream()
                .filter(r -> r.getClient().equals(this.ocrWorkflow.getClient())).collect(Collectors.toList());
        if (rulesets.isEmpty()) {
            return false;
        } else {
            if (Objects.nonNull(this.ocrWorkflow.getId())) {
                if (rulesets.size() == 1) {
                    return !rulesets.get(0).getId().equals(this.ocrWorkflow.getId());
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    /**
     * Method being used as viewAction for ocr workflow edit form. If given parameter
     * 'id' is '0', the form for creating a new ocr workflow will be displayed.
     *
     * @param id
     *            ID of the ruleset to load
     */
    public void load(int id) {

    }

    /*
     * Getter und Setter
     */
    public OCRWorkflow getOcrWorkflow() {
        return this.ocrWorkflow;
    }

    public void setOcrWorkflow(OCRWorkflow inPreference) {
        this.ocrWorkflow = inPreference;
    }

    /**
     * Set ruleset by ID.
     *
     * @param rulesetID
     *            ID of the ruleset to set.
     */
    public void setRulesetById(int rulesetID) {

    }

    /**
     * Get list of ocr workflow filenames.
     *
     * @return list of ocr workflow filenames
     */
    public List<Path> getOCRWorkflowFilenames() {
        try (Stream<Path> rulesetPaths = Files.walk(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_OCR_WORKFLOWS)))) {
            return rulesetPaths.filter(f -> f.toString().endsWith(".sh") || f.toString().endsWith(".mk"))
                    .map(Path::getFileName).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.RULESET.getTranslationPlural() },
                logger, e);
            return new ArrayList<>();
        }
    }
}
