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

package org.kitodo.production.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataTypeHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;

public class VariableReplacer {

    private enum MetadataLevel {
        ALL,
        FIRSTCHILD,
        TOPSTRUCT
    }

    private static final Logger logger = LogManager.getLogger(VariableReplacer.class);

    private LegacyMetsModsDigitalDocumentHelper digitalDocument;
    private LegacyPrefsHelper prefs;
    // $(meta.abc)
    private static final String NAMESPACE_META = "\\$\\(meta\\.([\\w.-]*)\\)";

    private Process process;
    private Task task;

    /**
     * Constructor.
     *
     * @param digitalDocument
     *            DigitalDocument object
     * @param prefs
     *            Prefs object
     * @param process
     *            Process object
     * @param task
     *            Task object
     */
    public VariableReplacer(LegacyMetsModsDigitalDocumentHelper digitalDocument, LegacyPrefsHelper prefs,
            Process process, Task task) {
        this.digitalDocument = digitalDocument;
        this.prefs = prefs;
        this.process = process;
        this.task = task;
    }

    /**
     * Replace variables within a string. Like ant, run through the variables
     * and fetch them from the digital document.
     *
     * @param inString
     *            to replacement
     * @return replaced String
     */
    public String replace(String inString) {
        if (Objects.isNull(inString)) {
            return "";
        }
        inString = replaceMetadata(inString);

        // replace paths and files
        String prefs = ConfigCore.getParameter(ParameterCore.DIR_RULESETS) + this.process.getRuleset().getFile();
        inString = replaceString(inString, "(prefs)", prefs);

        inString = replaceString(inString, "(processtitle)", this.process.getTitle());
        inString = replaceString(inString, "(processid)", String.valueOf(this.process.getId().intValue()));

        inString = replaceStringForTask(inString);

        inString = replaceForWorkpieceProperty(inString);
        inString = replaceForTemplateProperty(inString);
        inString = replaceForProcessProperty(inString);

        return inString;
    }

    /**
     * Replace metadata, usage: $(meta.firstchild.METADATANAME).
     *
     * @param input
     *            String for replacement
     * @return replaced String
     */
    private String replaceMetadata(String input) {
        for (MatchResult r : findRegexMatches(NAMESPACE_META, input)) {
            if (r.group(1).toLowerCase().startsWith("firstchild.")) {
                input = input.replace(r.group(),
                    getMetadataFromDigitalDocument(MetadataLevel.FIRSTCHILD, r.group(1).substring(11)));
            } else if (r.group(1).toLowerCase().startsWith("topstruct.")) {
                input = input.replace(r.group(),
                    getMetadataFromDigitalDocument(MetadataLevel.TOPSTRUCT, r.group(1).substring(10)));
            } else {
                input = input.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.ALL, r.group(1)));
            }
        }

        return input;
    }

    private String replaceString(String input, String condition, String replacer) {
        if (input.contains(condition)) {
            input = input.replace(condition, replacer);
        }
        return input;
    }

    private String replaceStringForTask(String input) {
        if (Objects.nonNull(this.task)) {
            String taskId = String.valueOf(this.task.getId());
            String taskName = this.task.getTitle();

            input = input.replace("(stepid)", taskId);
            input = input.replace("(stepname)", taskName);
        }
        return input;
    }

    /**
     * Replace WerkstueckEigenschaft, usage: (product.PROPERTYTITLE).
     *
     * @param input
     *            String for replacement
     * @return replaced String
     */
    private String replaceForWorkpieceProperty(String input) {
        for (MatchResult r : findRegexMatches("\\(product\\.([\\w.-]*)\\)", input)) {
            String propertyTitle = r.group(1);
            for (Property workpieceProperty : this.process.getWorkpieces()) {
                if (workpieceProperty.getTitle().equalsIgnoreCase(propertyTitle)) {
                    input = input.replace(r.group(), workpieceProperty.getValue());
                    break;
                }
            }
        }
        return input;
    }

    /**
     * Replace Vorlageeigenschaft, usage: (template.PROPERTYTITLE).
     *
     * @param input
     *            String for replacement
     * @return replaced String
     */
    private String replaceForTemplateProperty(String input) {
        for (MatchResult r : findRegexMatches("\\(template\\.([\\w.-]*)\\)", input)) {
            String propertyTitle = r.group(1);
            for (Property templateProperty : this.process.getTemplates()) {
                if (templateProperty.getTitle().equalsIgnoreCase(propertyTitle)) {
                    input = input.replace(r.group(), templateProperty.getValue());
                    break;
                }
            }
        }
        return input;
    }

    /**
     * Replace Prozesseigenschaft, usage: (process.PROPERTYTITLE).
     *
     * @param input
     *            String for replacement
     * @return replaced String
     */
    private String replaceForProcessProperty(String input) {
        for (MatchResult r : findRegexMatches("\\(process\\.([\\w.-]*)\\)", input)) {
            String propertyTitle = r.group(1);
            List<Property> ppList = this.process.getProperties();
            for (Property pe : ppList) {
                if (pe.getTitle().equalsIgnoreCase(propertyTitle)) {
                    input = input.replace(r.group(), pe.getValue());
                    break;
                }
            }
        }
        return input;
    }

    /**
     * Determine metadata from first child or top struct, preferably from first
     * child.
     */
    private String getMetadataFromDigitalDocument(MetadataLevel inLevel, String metadata) {
        if (Objects.nonNull(this.digitalDocument)) {
            /* determine top struct and first child */
            LegacyDocStructHelperInterface topstruct = this.digitalDocument.getLogicalDocStruct();
            LegacyDocStructHelperInterface firstchildstruct = null;
            if (!topstruct.getAllChildren().isEmpty()) {
                firstchildstruct = topstruct.getAllChildren().get(0);
            }

            /* determine metadata type and likely report error */
            LegacyMetadataTypeHelper mdt;
            try {
                mdt = LegacyPrefsHelper.getMetadataType(this.prefs, metadata);
            } catch (IllegalArgumentException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                return "";
            }

            String resultTop = getMetadataValue(topstruct, mdt);
            String resultFirst = null;
            if (Objects.nonNull(firstchildstruct)) {
                resultFirst = getMetadataValue(firstchildstruct, mdt);
            }
            return getResultAccordingToMetadataLevel(inLevel, metadata, resultFirst, resultTop);
        } else {
            return "";
        }
    }

    private String getResultAccordingToMetadataLevel(MetadataLevel metadataLevel, String metadata, String resultFirst,
            String resultTop) {
        String resultAccordingToMetadataLevel = "";
        switch (metadataLevel) {
            case FIRSTCHILD:
                // without existing first child, this cannot be returned
                if (Objects.isNull(resultFirst)) {
                    logger.info("Can not replace firstChild-variable for METS: {}", metadata);
                } else {
                    resultAccordingToMetadataLevel = resultFirst;
                }
                break;
            case TOPSTRUCT:
                if (Objects.isNull(resultTop)) {
                    logger.warn("Can not replace topStruct-variable for METS: {}", metadata);
                } else {
                    resultAccordingToMetadataLevel = resultTop;
                }
                break;
            case ALL:
                if (Objects.nonNull(resultFirst)) {
                    resultAccordingToMetadataLevel = resultFirst;
                } else if (Objects.nonNull(resultTop)) {
                    resultAccordingToMetadataLevel = resultTop;
                } else {
                    logger.warn("Can not replace variable for METS: {}", metadata);
                }
                break;
            default:
                break;
        }
        return resultAccordingToMetadataLevel;
    }

    /**
     * Determine the metadata of the transferred doc struct. In the event of an
     * error, {code null} is returned.
     */
    private String getMetadataValue(LegacyDocStructHelperInterface inDocstruct, LegacyMetadataTypeHelper mdt) {
        List<? extends LegacyMetadataHelper> mds = inDocstruct.getAllMetadataByType(mdt);
        if (!mds.isEmpty()) {
            return mds.get(0).getValue();
        } else {
            return null;
        }
    }

    /**
     * Searches for regular expressions in a string, returns all hits found as a
     * list.
     */
    private static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
        List<MatchResult> results = new ArrayList<>();
        for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
            results.add(m.toMatchResult());
        }
        return results;
    }
}
