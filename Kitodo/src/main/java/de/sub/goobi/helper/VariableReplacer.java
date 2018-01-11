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

package de.sub.goobi.helper;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.exceptions.UghHelperException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class VariableReplacer {

    private enum MetadataLevel {
        ALL, FIRSTCHILD, TOPSTRUCT
    }

    private static final Logger logger = LogManager.getLogger(VariableReplacer.class);

    DigitalDocumentInterface dd;
    PrefsInterface prefsInterface;
    // $(meta.abc)
    private final String namespaceMeta = "\\$\\(meta\\.([\\w.-]*)\\)";

    private Process process;
    private Task task;
    private final ServiceManager serviceManager = new ServiceManager();
    private final FileService fileService = serviceManager.getFileService();

    @SuppressWarnings("unused")
    private VariableReplacer() {
    }

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
    public VariableReplacer(DigitalDocumentInterface inDigitalDocument, PrefsInterface inPrefs, Process p, Task s) {
        this.dd = inDigitalDocument;
        this.prefsInterface = inPrefs;
        this.process = p;
        this.task = s;
    }

    /**
     * Variablen innerhalb eines Strings ersetzen. Dabei vergleichbar zu Ant die
     * Variablen durchlaufen und aus dem Digital Document holen
     */
    public String replace(String inString) {
        if (inString == null) {
            return "";
        }

        /*
         * replace metadata, usage: $(meta.firstchild.METADATANAME)
         */
        for (MatchResult r : findRegexMatches(this.namespaceMeta, inString)) {
            if (r.group(1).toLowerCase().startsWith("firstchild.")) {
                inString = inString.replace(r.group(),
                        getMetadataFromDigitalDocument(MetadataLevel.FIRSTCHILD, r.group(1).substring(11)));
            } else if (r.group(1).toLowerCase().startsWith("topstruct.")) {
                inString = inString.replace(r.group(),
                        getMetadataFromDigitalDocument(MetadataLevel.TOPSTRUCT, r.group(1).substring(10)));
            } else {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.ALL, r.group(1)));
            }
        }

        // replace paths and files
        try {
            String processPath = fileService
                    .getFileName(serviceManager.getProcessService().getProcessDataDirectory(this.process))
                    .replace("\\", "/");
            String tifPath = fileService
                    .getFileName(serviceManager.getProcessService().getImagesTifDirectory(false, this.process))
                    .replace("\\", "/");
            String imagePath = fileService.getFileName(fileService.getImagesDirectory(this.process)).replace("\\", "/");
            String origPath = fileService
                    .getFileName(serviceManager.getProcessService().getImagesOrigDirectory(false, this.process))
                    .replace("\\", "/");
            String metaFile = fileService.getFileName(fileService.getMetadataFilePath(this.process)).replace("\\", "/");
            String ocrBasisPath = fileService.getFileName(fileService.getOcrDirectory(this.process)).replace("\\", "/");
            String ocrPlaintextPath = fileService.getFileName(fileService.getTxtDirectory(this.process)).replace("\\",
                    "/");
            // TODO name ändern?
            String sourcePath = fileService.getFileName(fileService.getSourceDirectory(this.process))
                    .replace("\\", "/");
            String importPath = fileService.getFileName(fileService.getImportDirectory(this.process)).replace("\\",
                    "/");
            String prefs = ConfigCore.getParameter("RegelsaetzeVerzeichnis") + this.process.getRuleset().getFile();

            /*
             * da die Tiffwriter-Scripte einen Pfad ohne endenen Slash haben wollen, wird
             * diese rausgenommen
             */
            tifPath = replaceSeparator(tifPath);
            imagePath = replaceSeparator(imagePath);
            origPath = replaceSeparator(origPath);
            processPath = replaceSeparator(processPath);
            importPath = replaceSeparator(importPath);
            sourcePath = replaceSeparator(sourcePath);
            ocrBasisPath = replaceSeparator(ocrBasisPath);
            ocrPlaintextPath = replaceSeparator(ocrPlaintextPath);

            inString = replaceStringAccordingToOS(inString, "(tifurl)", tifPath);
            inString = replaceStringAccordingToOS(inString, "(origurl)", origPath);
            inString = replaceStringAccordingToOS(inString, "(imageurl)", imagePath);

            inString = replaceString(inString, "(tifpath)", tifPath);
            inString = replaceString(inString, "(origpath)", origPath);
            inString = replaceString(inString, "(imagepath)", imagePath);
            inString = replaceString(inString, "(processpath)", processPath);
            inString = replaceString(inString, "(importpath)", importPath);
            inString = replaceString(inString, "(sourcepath)", sourcePath);
            inString = replaceString(inString, "(ocrbasispath)", ocrBasisPath);
            inString = replaceString(inString, "(ocrplaintextpath)", ocrPlaintextPath);
            inString = replaceString(inString, "(processtitle)", this.process.getTitle());
            inString = replaceString(inString, "(processid)", String.valueOf(this.process.getId().intValue()));
            inString = replaceString(inString, "(metaFile)", metaFile);
            inString = replaceString(inString, "(prefs)", prefs);

            inString = replaceStringForTask(inString);

            // replace WerkstueckEigenschaft, usage: (product.PROPERTYTITLE)
            for (MatchResult r : findRegexMatches("\\(product\\.([\\w.-]*)\\)", inString)) {
                String propertyTitle = r.group(1);
                for (Workpiece ws : this.process.getWorkpieces()) {
                    for (Property workpieceProperty : ws.getProperties()) {
                        if (workpieceProperty.getTitle().equalsIgnoreCase(propertyTitle)) {
                            inString = inString.replace(r.group(), workpieceProperty.getValue());
                            break;
                        }
                    }
                }
            }

            // replace Vorlageeigenschaft, usage: (template.PROPERTYTITLE)
            for (MatchResult r : findRegexMatches("\\(template\\.([\\w.-]*)\\)", inString)) {
                String propertyTitle = r.group(1);
                for (Template v : this.process.getTemplates()) {
                    for (Property templateProperty : v.getProperties()) {
                        if (templateProperty.getTitle().equalsIgnoreCase(propertyTitle)) {
                            inString = inString.replace(r.group(), templateProperty.getValue());
                            break;
                        }
                    }
                }
            }

            // replace Prozesseigenschaft, usage: (process.PROPERTYTITLE)
            for (MatchResult r : findRegexMatches("\\(process\\.([\\w.-]*)\\)", inString)) {
                String propertyTitle = r.group(1);
                List<Property> ppList = this.process.getProperties();
                for (Property pe : ppList) {
                    if (pe.getTitle().equalsIgnoreCase(propertyTitle)) {
                        inString = inString.replace(r.group(), pe.getValue());
                        break;
                    }
                }

            }

        } catch (IOException e) {
            logger.error(e);
        }

        return inString;
    }

    private String replaceSeparator(String input) {
        if (input.endsWith(File.separator)) {
            input = input.substring(0, input.length() - File.separator.length()).replace("\\", "/");
        }
        return input;
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

    private String replaceStringForTask(String input) {
        if (this.task != null) {
            String taskId = String.valueOf(this.task.getId());
            String taskName = this.task.getTitle();

            input = input.replace("(stepid)", taskId);
            input = input.replace("(stepname)", taskName);
        }
        return input;
    }

    /**
     * Metadatum von FirstChild oder TopStruct ermitteln (vorzugsweise vom
     * FirstChild) und zurückgeben.
     */
    private String getMetadataFromDigitalDocument(MetadataLevel inLevel, String metadata) {
        if (this.dd != null) {
            /* TopStruct und FirstChild ermitteln */
            DocStructInterface topstruct = this.dd.getLogicalDocStruct();
            DocStructInterface firstchildstruct = null;
            if (topstruct.getAllChildren() != null && topstruct.getAllChildren().size() > 0) {
                firstchildstruct = topstruct.getAllChildren().get(0);
            }

            /* MetadataType ermitteln und ggf. Fehler melden */
            MetadataTypeInterface mdt;
            try {
                mdt = UghHelper.getMetadataType(this.prefsInterface, metadata);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung(e);
                return "";
            }

            String result = "";
            String resultTop = getMetadataValue(topstruct, mdt);
            String resultFirst = null;
            if (firstchildstruct != null) {
                resultFirst = getMetadataValue(firstchildstruct, mdt);
            }

            switch (inLevel) {
                case FIRSTCHILD:
                    /*
                     * ohne vorhandenes FirstChild, kann dieses nicht zurückgegeben werden
                     */
                    if (resultFirst == null) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Can not replace firstChild-variable for METS: " + metadata);
                        }
                        result = "";
                    } else {
                        result = resultFirst;
                    }
                    break;

                case TOPSTRUCT:
                    if (resultTop == null) {
                        result = "";
                        if (logger.isWarnEnabled()) {
                            logger.warn("Can not replace topStruct-variable for METS: " + metadata);
                        }
                    } else {
                        result = resultTop;
                    }
                    break;

                case ALL:
                    if (resultFirst != null) {
                        result = resultFirst;
                    } else if (resultTop != null) {
                        result = resultTop;
                    } else {
                        result = "";
                        if (logger.isWarnEnabled()) {
                            logger.warn("Can not replace variable for METS: " + metadata);
                        }
                    }
                    break;

                default:
                    break;
            }
            return result;
        } else {
            return "";
        }
    }

    /**
     * Metadatum von übergebenen Docstruct ermitteln, im Fehlerfall wird null
     * zurückgegeben.
     */
    private String getMetadataValue(DocStructInterface inDocstruct, MetadataTypeInterface mdt) {
        List<? extends MetadataInterface> mds = inDocstruct.getAllMetadataByType(mdt);
        if (mds.size() > 0) {
            return mds.get(0).getValue();
        } else {
            return null;
        }
    }

    /**
     * Suche nach regulären Ausdrücken in einem String, liefert alle gefundenen
     * Treffer als Liste zurück.
     */
    public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
        List<MatchResult> results = new ArrayList<>();
        for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
            results.add(m.toMatchResult());
        }
        return results;
    }
}
