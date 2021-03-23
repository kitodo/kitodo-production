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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
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
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;

public class VariableReplacer {

    private enum MetadataLevel {
        ALL,
        FIRSTCHILD,
        TOPSTRUCT
    }

    private static final Logger logger = LogManager.getLogger(VariableReplacer.class);

    private static final Pattern VARIABLE_FINDER_REGEX = Pattern
            .compile("(\\$?)\\((prefs|processid|processtitle|projectid|stepid|stepname)\\)");

    private LegacyMetsModsDigitalDocumentHelper dd;
    private LegacyPrefsHelper prefs;
    // $(meta.abc)
    private static final String NAMESPACE_META = "\\$\\(meta\\.([\\w.-]*)\\)";

    private Process process;
    private Task task;
    private final FileService fileService = ServiceManager.getFileService();
    private final ProcessService processService = ServiceManager.getProcessService();

    /**
     * Constructor.
     *
     * @param inDigitalDocument
     *            DigitalDocument object
     * @param inPrefs
     *            Prefs object
     * @param p
     *            Process object
     * @param s
     *            Task object
     */
    public VariableReplacer(LegacyMetsModsDigitalDocumentHelper inDigitalDocument, LegacyPrefsHelper inPrefs, Process p, Task s) {
        initializeLegacyVariablesPreprocessor();
        this.dd = inDigitalDocument;
        this.prefs = inPrefs;
        this.process = p;
        this.task = s;
    }

    Map<String, String> legacyVariablesMap;
    Pattern legacyVariablesPattern;

    private void initializeLegacyVariablesPreprocessor() {
        StringBuilder regexBuilder = null;
        boolean useLegacyVariablesPreprocessor = false;
        for (Iterator<String> iterator = ConfigCore.getConfig().getKeys(); iterator.hasNext();) {
            String key = iterator.next();
            if (key.startsWith("variable.")) {
                String variableName = key.substring(9);
                if (useLegacyVariablesPreprocessor) {
                    regexBuilder.append('|');
                } else {
                    regexBuilder = new StringBuilder("\\((");
                    legacyVariablesMap = new HashMap<>();
                    useLegacyVariablesPreprocessor = true;
                }
                regexBuilder.append(Pattern.quote(variableName));
                legacyVariablesMap.put(variableName, ConfigCore.getParameter(key));
            }
        }
        if (useLegacyVariablesPreprocessor) {
            regexBuilder.append(")\\)");
            legacyVariablesPattern = Pattern.compile(regexBuilder.toString());
        }
    }


    /**
     * Variablen innerhalb eines Strings ersetzen. Dabei vergleichbar zu Ant die
     * Variablen durchlaufen und aus dem Digital Document holen
     *
     * @param inString
     *            to replacement
     * @return replaced String
     */
    public String replace(String inString) {
        if (Objects.isNull(inString)) {
            return "";
        }

        inString = invokeLegacyVariableReplacer(inString);

        inString = replaceMetadata(inString);
        inString = replaceForWorkpieceProperty(inString);
        inString = replaceForTemplateProperty(inString);
        inString = replaceForProcessProperty(inString);

        // replace paths and files
        Matcher variableFinder = VARIABLE_FINDER_REGEX.matcher(inString);
        boolean stringChanged = false;
        StringBuffer replacedStringBuffer = null;
        while (variableFinder.find()) {
            if (!stringChanged) {
                replacedStringBuffer = new StringBuffer();
                stringChanged = true;
            }
            variableFinder.appendReplacement(replacedStringBuffer, determineReplacement(variableFinder));
        }
        if (stringChanged) {
            variableFinder.appendTail(replacedStringBuffer);
            return replacedStringBuffer.toString();
        } else {
            return inString;
        }
    }

    private String determineReplacement(Matcher variableFinder) {
        switch (variableFinder.group(2)) {
            case "prefs":
                return variableFinder.group(1) + ConfigCore.getParameter(ParameterCore.DIR_RULESETS)
                        + process.getRuleset().getFile();
            case "processid":
                return variableFinder.group(1) + process.getId().toString();
            case "processtitle":
                return variableFinder.group(1) + process.getTitle();
            case "projectid":
                return variableFinder.group(1) + process.getProject().getId().toString();
            case "stepid":
                return variableFinder.group(1) + String.valueOf(task.getId());
            case "stepname":
                return variableFinder.group(1) + task.getTitle();
            default:
                return variableFinder.group();
        }
    }

        private String invokeLegacyVariableReplacer(String stringToReplace) {
            if (Objects.isNull(legacyVariablesPattern)) {
                return stringToReplace;
            }
            Matcher legacyVariablesMatcher = legacyVariablesPattern.matcher(stringToReplace);
            StringBuffer replacedLegaycVariablesBuffer = new StringBuffer();
            while (legacyVariablesMatcher.find()) {
                legacyVariablesMatcher.appendReplacement(replacedLegaycVariablesBuffer,
                    legacyVariablesMap.get(legacyVariablesMatcher.group(1)));
        }
            legacyVariablesMatcher.appendTail(replacedLegaycVariablesBuffer);
            return replacedLegaycVariablesBuffer.toString();
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

    private String replaceSlash(URI directory) {
        return fileService.getFileName(directory).replace("\\", "/");
    }

    private String replaceSeparator(String input) {
        if (input.endsWith(File.separator)) {
            input = input.substring(0, input.length() - File.separator.length()).replace("\\", "/");
        }
        return input;
    }

    private String replaceSlashAndSeparator(URI directory) {
        return replaceSeparator(replaceSlash(directory));
    }

    private String replaceStringAccordingToOS(String input, String condition, String replacer) {
        if (input.contains(condition)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                input = input.replace(condition, "file:/" + replacer);
            } else {
                input = input.replace(condition, "file://" + replacer);
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
     * Metadatum von FirstChild oder TopStruct ermitteln (vorzugsweise vom
     * FirstChild) und zurückgeben.
     */
    private String getMetadataFromDigitalDocument(MetadataLevel inLevel, String metadata) {
        if (Objects.nonNull(this.dd)) {
            /* TopStruct und FirstChild ermitteln */
            LegacyDocStructHelperInterface topstruct = this.dd.getLogicalDocStruct();
            LegacyDocStructHelperInterface firstchildstruct = null;
            if (!topstruct.getAllChildren().isEmpty()) {
                firstchildstruct = topstruct.getAllChildren().get(0);
            }

            /* MetadataType ermitteln und ggf. Fehler melden */
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
                // without existing FirstChild, this cannot be returned
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
     * Metadatum von übergebenen Docstruct ermitteln, im Fehlerfall wird null
     * zurückgegeben.
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
     * Suche nach regulären Ausdrücken in einem String, liefert alle gefundenen
     * Treffer als Liste zurück.
     */
    private static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
        List<MatchResult> results = new ArrayList<>();
        for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
            results.add(m.toMatchResult());
        }
        return results;
    }
}
