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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.metadata.MetadataEditor;

public class VariableReplacer {

    private enum MetadataLevel {
        ALL,
        FIRSTCHILD,
        TOPSTRUCT
    }

    private static final Logger logger = LogManager.getLogger(VariableReplacer.class);

    private static final Pattern VARIABLE_FINDER_REGEX = Pattern
            .compile(
                "(\\$?)\\((?:(prefs|processid|processtitle|projectid|stepid|stepname)|(?:(meta|process|product|template)\\.(?:(firstchild|topstruct)\\.)?([^)]+)))\\)");

    private static Map<String, String> legacyVariablesMap;
    private static Pattern legacyVariablesPattern;

    private Workpiece workpiece;
    private Process process;
    private Task task;

    /**
     * Constructor.
     *
     * @param workpiece
     *            DigitalDocument object
     * @param p
     *            Process object
     * @param s
     *            Task object
     */
    public VariableReplacer(Workpiece workpiece, Process p, Task s) {
        initializeLegacyVariablesPreprocessor();
        this.workpiece = workpiece;
        this.process = p;
        this.task = s;
    }

    private final void initializeLegacyVariablesPreprocessor() {
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

    private String determineReplacement(Matcher variableFinder) {
        if (Objects.nonNull(variableFinder.group(2))) {
            return determineReplacementForInternalValue(variableFinder);
        }
        if (Objects.nonNull(variableFinder.group(3))) {
            return determineReplacementForMetadata(variableFinder);
        }
        return variableFinder.group();
    }

    private String determineReplacementForInternalValue(Matcher variableFinder) {
        switch (variableFinder.group(2)) {

            case "prefs":
                String rulesetsDirectory;
                try {
                    rulesetsDirectory = ConfigCore.getParameter(ParameterCore.DIR_RULESETS);
                } catch (NoSuchElementException e) {
                    logger.warn("Cannot replace \"(prefs)\": Missing configuration entry: directory.rulesets");
                    return variableFinder.group(1);
                }
                if (Objects.isNull(process)) {
                    logger.warn("Cannot replace \"(prefs)\": no process given");
                    return variableFinder.group(1);
                }
                if (Objects.isNull(process.getRuleset())) {
                    logger.warn("Cannot replace \"(prefs)\": process has no ruleset assigned");
                    return variableFinder.group(1);
                }
                if (Objects.isNull(process.getRuleset().getFile())) {
                    logger.warn("Cannot replace \"(prefs)\": process's ruleset has no file");
                    return variableFinder.group(1);
                }
                return variableFinder.group(1) + rulesetsDirectory + process.getRuleset().getFile();

            case "processid":
                if (Objects.isNull(process)) {
                    logger.warn("Cannot replace \"(processid)\": no process given");
                    return variableFinder.group(1);
                }
                return variableFinder.group(1) + process.getId().toString();

            case "processtitle":
                if (Objects.isNull(process)) {
                    logger.warn("Cannot replace \"(processtitle)\": no process given");
                    return variableFinder.group(1);
                }
                return variableFinder.group(1) + process.getTitle();

            case "projectid":
                if (Objects.isNull(process)) {
                    logger.warn("Cannot replace \"(projectid)\": no process given");
                    return variableFinder.group(1);
                }
                if (Objects.isNull(process.getProject())) {
                    logger.warn("Cannot replace \"(projectid)\": process has no project assigned");
                    return variableFinder.group(1);
                }
                return variableFinder.group(1) + String.valueOf(process.getProject().getId());

            case "stepid":
                if (Objects.isNull(task)) {
                    logger.warn("Cannot replace \"(stepid)\": no task given");
                    return variableFinder.group(1);
                }
                return variableFinder.group(1) + String.valueOf(task.getId());

            case "stepname":
                if (Objects.isNull(task)) {
                    logger.warn("Cannot replace \"(stepname)\": no task given");
                    return variableFinder.group(1);
                }
                return variableFinder.group(1) + task.getTitle();

            default:
                logger.warn("Cannot replace \"{}\": no such case defined in switch", variableFinder.group());
                return variableFinder.group();
        }
    }

    private String determineReplacementForMetadata(Matcher variableFinder) {
        String dollarSignIfToKeep = variableFinder.group(1);
        MetadataLevel metadataLevel;
        if (variableFinder.group(3).equals("meta")) {
            if (dollarSignIfToKeep.isEmpty()) {
                return variableFinder.group();
            } else {
                dollarSignIfToKeep = "";
            }
            metadataLevel = Objects.isNull(variableFinder.group(4)) ? MetadataLevel.ALL
                    : MetadataLevel.valueOf(variableFinder.group(4).toUpperCase());
        } else {
            metadataLevel = MetadataLevel.TOPSTRUCT;
        }

        if (Objects.isNull(workpiece)) {
            logger.warn("Cannot replace \"{}\": no workpiece given", variableFinder.group());
            return dollarSignIfToKeep;
        }

        switch (metadataLevel) {
            case ALL:
                List<IncludedStructuralElement> allChildren = workpiece.getRootElement().getChildren();
                String allFirstchildValue = allChildren.isEmpty() ? null
                        : MetadataEditor.getMetadataValue(allChildren.get(0), variableFinder.group(5));
                if (Objects.nonNull(allFirstchildValue)) {
                    return allFirstchildValue;
                }
                // else fall through

            case TOPSTRUCT:
                String topstructValue = MetadataEditor.getMetadataValue(workpiece.getRootElement(),
                    variableFinder.group(5));
                if (Objects.isNull(topstructValue)) {
                    logger.warn("Cannot replace \"{}\": No such metadata entry in the root element",
                        variableFinder.group());
                    return dollarSignIfToKeep;
                }
                return topstructValue;

            case FIRSTCHILD:
                List<IncludedStructuralElement> firstchildChildren = workpiece.getRootElement().getChildren();
                if (firstchildChildren.isEmpty()) {
                    logger.warn("Cannot replace \"{}\": Workpiece doesn't have subordinate logical divisions",
                        variableFinder.group());
                    return dollarSignIfToKeep;
                }
                String firstchildValue = MetadataEditor.getMetadataValue(firstchildChildren.get(0),
                    variableFinder.group(5));
                if (Objects.isNull(firstchildValue)) {
                    logger.warn("Cannot replace \"{}\": No such metadata entry in the first division",
                        variableFinder.group());
                    return dollarSignIfToKeep;
                }
                return firstchildValue;

            default:
                throw new IllegalStateException("complete switch");
        }
    }
}
