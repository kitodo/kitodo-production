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

package org.kitodo.helper.metadata;

import de.sub.goobi.metadaten.Metadaten;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.data.database.beans.User;
import org.kitodo.helper.Helper;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.dataeditor.RulesetManagementService;

/**
 * Connects a legacy prefs to a ruleset. This is a soldering class to keep
 * legacy code operational which is about to be removed. Do not use this class.
 */
public class LegacyPrefsHelper implements PrefsInterface {
    private static final Logger logger = LogManager.getLogger(LegacyPrefsHelper.class);

    private final ServiceManager serviceLoader = new ServiceManager();
    private final RulesetManagementService rulesetManagementService = serviceLoader.getRulesetManagementService();

    /**
     * The ruleset accessed via this soldering class.
     */
    private RulesetManagementInterface ruleset;

    @Override
    public List<DocStructTypeInterface> getAllDocStructTypes() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public DocStructTypeInterface getDocStrctTypeByName(String identifier) {
        switch (identifier) {
            case "page":
                return LegacyInnerPhysicalDocStructTypePageHelper.INSTANCE;
            default:
                User user = new Metadaten().getCurrentUser();
                String metadataLanguage = user != null ? user.getMetadataLanguage()
                        : Helper.getRequestParameter("Accept-Language");
                List<LanguageRange> priorityList = LanguageRange
                        .parse(metadataLanguage != null ? metadataLanguage : "en");
                StructuralElementViewInterface divisionView = ruleset.getStructuralElementView(identifier, "edit",
                    priorityList);
                return new LegacyLogicalDocStructTypeHelper(divisionView);
        }
    }

    @Override
    public MetadataTypeInterface getMetadataTypeByName(String identifier) {
        switch (identifier) {
            case "logicalPageNumber":
                return LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDERLABEL;
            case "physPageNumber":
                return LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDER;
            default:
                User user = new Metadaten().getCurrentUser();
                String metadataLanguage = user != null ? user.getMetadataLanguage()
                        : Helper.getRequestParameter("Accept-Language");
                List<LanguageRange> priorityList = LanguageRange
                        .parse(metadataLanguage != null ? metadataLanguage : "en");
                StructuralElementViewInterface divisionView = ruleset.getStructuralElementView("", "edit",
                    priorityList);
                List<MetadataViewWithValuesInterface<Void>> entryViews = divisionView
                        .getSortedVisibleMetadata(Collections.emptyMap(), Arrays.asList(identifier));
                MetadataViewInterface resultKeyView = entryViews.parallelStream()
                        .map(entryView -> entryView.getMetadata()).filter(Optional::isPresent).map(Optional::get)
                        .filter(keyView -> keyView.getId().equals(identifier)).findFirst()
                        .orElseThrow(IllegalStateException::new);
                return new LegacyMetadataTypeHelper(resultKeyView);
        }
    }

    /**
     * Returns the ruleset of the legacy prefs helper.
     * 
     * @return the ruleset
     */
    RulesetManagementInterface getRuleset() {
        return ruleset;
    }

    @Override
    public void loadPrefs(String fileName) throws PreferencesException {
        File rulesetFile = new File(fileName);
        RulesetManagementInterface ruleset = rulesetManagementService.getRulesetManagement();
        try {
            ruleset.load(rulesetFile);
        } catch (IOException e) {
            throw new PreferencesException("Error reading " + fileName + ": " + e.getMessage(), e);
        }
        this.ruleset = ruleset;
    }

    /**
     * This method generates a comprehensible log message in case something was
     * overlooked and one of the unimplemented methods should ever be called in
     * operation. The name was chosen deliberately short in order to keep the
     * calling code clear. This method must be implemented in every class
     * because it uses the logger tailored to the class.
     * 
     * @param exception
     *            created {@code UnsupportedOperationException}
     * @return the exception
     */
    private static RuntimeException andLog(UnsupportedOperationException exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StringBuilder buffer = new StringBuilder(255);
        buffer.append(stackTrace[1].getClassName());
        buffer.append('.');
        buffer.append(stackTrace[1].getMethodName());
        buffer.append("()");
        if (stackTrace[1].getLineNumber() > -1) {
            buffer.append(" line ");
            buffer.append(stackTrace[1].getLineNumber());
        }
        buffer.append(" unexpectedly called unimplemented ");
        buffer.append(stackTrace[0].getMethodName());
        if (exception.getMessage() != null) {
            buffer.append(": ");
            buffer.append(exception.getMessage());
        }
        logger.error(buffer.toString());
        return exception;
    }
}
