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

import org.apache.logging.log4j.Level;
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

public class LegacyPrefsHelper implements PrefsInterface {
    private static final Logger logger = LogManager.getLogger(LegacyPrefsHelper.class);
    private final ServiceManager serviceLoader = new ServiceManager();
    private final RulesetManagementService rulesetManagementService = serviceLoader.getRulesetManagementService();

    RulesetManagementInterface ruleset;

    @Override
    public List<DocStructTypeInterface> getAllDocStructTypes() {
        logger.log(Level.TRACE, "getAllDocStructTypes()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
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
                List<MetadataViewWithValuesInterface<Void>> svm = divisionView
                        .getSortedVisibleMetadata(Collections.emptyMap(), Arrays.asList(identifier));
                MetadataViewInterface v = svm.parallelStream().map(x -> x.getMetadata()).filter(Optional::isPresent)
                        .map(Optional::get).filter(mv -> mv.getId().equals(identifier)).findFirst()
                        .orElseThrow(IllegalStateException::new);
                return new LegacyMetadataTypeHelper(v);
        }
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

}
