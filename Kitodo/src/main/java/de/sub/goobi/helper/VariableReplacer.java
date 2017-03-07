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

import de.sub.goobi.config.ConfigMain;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.beans.WorkpieceProperty;

import org.kitodo.services.ProcessService;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;

public class VariableReplacer {

	private enum MetadataLevel {
		ALL, FIRSTCHILD, TOPSTRUCT;
	}

	private static final Logger logger = Logger.getLogger(VariableReplacer.class);

	DigitalDocument dd;
	Prefs prefs;
	// $(meta.abc)
	private final String namespaceMeta = "\\$\\(meta\\.([\\w.-]*)\\)";

	private Process process;
	private Task task;

	private ProcessService processService = new ProcessService();

	@SuppressWarnings("unused")
	private VariableReplacer() {
	}

	public VariableReplacer(DigitalDocument inDigitalDocument, Prefs inPrefs, Process p, Task s) {
		this.dd = inDigitalDocument;
		this.prefs = inPrefs;
		this.process = p;
		this.task = s;
	}

	/**
	 * Variablen innerhalb eines Strings ersetzen. Dabei vergleichbar zu Ant die Variablen durchlaufen und aus
	 * dem Digital Document holen
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
				inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.FIRSTCHILD, r.group(1).substring(11)));
			} else if (r.group(1).toLowerCase().startsWith("topstruct.")) {
				inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.TOPSTRUCT, r.group(1).substring(10)));
			} else {
				inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.ALL, r.group(1)));
			}
		}

		// replace paths and files
		try {
			String processpath = processService.getProcessDataDirectory(this.process).replace("\\", "/");
			String tifpath = processService.getImagesTifDirectory(false, this.process).replace("\\", "/");
			String imagepath = processService.getImagesDirectory(this.process).replace("\\", "/");
			String origpath = processService.getImagesOrigDirectory(false, this.process).replace("\\", "/");
			String metaFile = processService.getMetadataFilePath(this.process).replace("\\", "/");
			String ocrBasisPath = processService.getOcrDirectory(this.process).replace("\\", "/");
			String ocrPlaintextPath = processService.getTxtDirectory(this.process).replace("\\", "/");
			// TODO name ändern?
			String sourcePath = processService.getSourceDirectory(this.process).replace("\\", "/");
			String importPath = processService.getImportDirectory(this.process).replace("\\", "/");
			String myprefs = ConfigMain.getParameter("RegelsaetzeVerzeichnis")
					+ this.process.getRuleset().getFile();

			/* da die Tiffwriter-Scripte einen Pfad ohne endenen Slash haben wollen, wird diese rausgenommen */
			if (tifpath.endsWith(File.separator)) {
				tifpath = tifpath.substring(0, tifpath.length() - File.separator.length()).replace("\\", "/");
			}
			if (imagepath.endsWith(File.separator)) {
				imagepath = imagepath.substring(0, imagepath.length() - File.separator.length()).replace("\\", "/");
			}
			if (origpath.endsWith(File.separator)) {
				origpath = origpath.substring(0, origpath.length() - File.separator.length()).replace("\\", "/");
			}
			if (processpath.endsWith(File.separator)) {
				processpath = processpath.substring(0, processpath.length() - File.separator.length()).replace("\\", "/");
			}
			if (importPath.endsWith(File.separator)) {
				importPath = importPath.substring(0, importPath.length() - File.separator.length()).replace("\\", "/");
			}
			if (sourcePath.endsWith(File.separator)) {
				sourcePath = sourcePath.substring(0, sourcePath.length() - File.separator.length()).replace("\\", "/");
			}
			if (ocrBasisPath.endsWith(File.separator)) {
				ocrBasisPath = ocrBasisPath.substring(0, ocrBasisPath.length() - File.separator.length()).replace("\\", "/");
			}
			if (ocrPlaintextPath.endsWith(File.separator)) {
				ocrPlaintextPath = ocrPlaintextPath.substring(0, ocrPlaintextPath.length() - File.separator.length()).replace("\\", "/");
			}
			if (inString.contains("(tifurl)")) {
				if (SystemUtils.IS_OS_WINDOWS) {
					inString = inString.replace("(tifurl)", "file:/" + tifpath);
				} else {
					inString = inString.replace("(tifurl)", "file://" + tifpath);
				}
			}
			if (inString.contains("(origurl)")) {
				if (SystemUtils.IS_OS_WINDOWS) {
					inString = inString.replace("(origurl)", "file:/" + origpath);
				} else {
					inString = inString.replace("(origurl)", "file://" + origpath);
				}
			}
			if (inString.contains("(imageurl)")) {
				if (SystemUtils.IS_OS_WINDOWS) {
					inString = inString.replace("(imageurl)", "file:/" + imagepath);
				} else {
					inString = inString.replace("(imageurl)", "file://" + imagepath);
				}
			}

			if (inString.contains("(tifpath)")) {
				inString = inString.replace("(tifpath)", tifpath);
			}
			if (inString.contains("(origpath)")) {
				inString = inString.replace("(origpath)", origpath);
			}
			if (inString.contains("(imagepath)")) {
				inString = inString.replace("(imagepath)", imagepath);
			}
			if (inString.contains("(processpath)")) {
				inString = inString.replace("(processpath)", processpath);
			}
			if (inString.contains("(importpath)")){
				inString = inString.replace("(importpath)", importPath);
			}
			if (inString.contains("(sourcepath)")){
				inString = inString.replace("(sourcepath)", sourcePath);
			}
			
			if (inString.contains("(ocrbasispath)")){
				inString = inString.replace("(ocrbasispath)", ocrBasisPath);
			}
			if (inString.contains("(ocrplaintextpath)")){
				inString = inString.replace("(ocrplaintextpath)", ocrPlaintextPath);
			}
			if (inString.contains("(processtitle)")) {
				inString = inString.replace("(processtitle)", this.process.getTitle());
			}
			if (inString.contains("(processid)")) {
				inString = inString.replace("(processid)", String.valueOf(this.process.getId().intValue()));
			}
			if (inString.contains("(metaFile)")) {
				inString = inString.replace("(metaFile)", metaFile);
			}
			if (inString.contains("(prefs)")) {
				inString = inString.replace("(prefs)", myprefs);
			}

			if (this.task != null) {
				String stepId = String.valueOf(this.task.getId());
				String stepname = this.task.getTitle();

				inString = inString.replace("(stepid)", stepId);
				inString = inString.replace("(stepname)", stepname);
			}

			// replace WerkstueckEigenschaft, usage: (product.PROPERTYTITLE)

			for (MatchResult r : findRegexMatches("\\(product\\.([\\w.-]*)\\)", inString)) {
				String propertyTitle = r.group(1);
				for (Workpiece ws : this.process.getWorkpieces()) {
					for (WorkpieceProperty we : ws.getProperties()) {
						if (we.getTitle().equalsIgnoreCase(propertyTitle)) {
							inString = inString.replace(r.group(), we.getValue());
							break;
						}
					}
				}
			}

			// replace Vorlageeigenschaft, usage: (template.PROPERTYTITLE)

			for (MatchResult r : findRegexMatches("\\(template\\.([\\w.-]*)\\)", inString)) {
				String propertyTitle = r.group(1);
				for (Template v : this.process.getTemplates()) {
					for (TemplateProperty ve : v.getProperties()) {
						if (ve.getTitle().equalsIgnoreCase(propertyTitle)) {
							inString = inString.replace(r.group(), ve.getValue());
							break;
						}
					}
				}
			}

			// replace Prozesseigenschaft, usage: (process.PROPERTYTITLE)

			for (MatchResult r : findRegexMatches("\\(process\\.([\\w.-]*)\\)", inString)) {
				String propertyTitle = r.group(1);
				List<ProcessProperty> ppList = PropertyParser.getPropertiesForProcess(this.process);
				for (ProcessProperty pe : ppList) {
					if (pe.getName().equalsIgnoreCase(propertyTitle)) {
						inString = inString.replace(r.group(), pe.getValue());
						break;
					}
				}

			}

		} catch (SwapException e) {
			logger.error(e);
		} catch (DAOException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (InterruptedException e) {
			logger.error(e);
		}

		return inString;
	}

	/**
	 * Metadatum von FirstChild oder TopStruct ermitteln (vorzugsweise vom FirstChild) und zurückgeben
	 * ================================================================
	 */
	private String getMetadataFromDigitalDocument(MetadataLevel inLevel, String metadata) {
		if (this.dd != null) {
			/* TopStruct und FirstChild ermitteln */
			DocStruct topstruct = this.dd.getLogicalDocStruct();
			DocStruct firstchildstruct = null;
			if (topstruct.getAllChildren() != null && topstruct.getAllChildren().size() > 0) {
				firstchildstruct = topstruct.getAllChildren().get(0);
			}

			/* MetadataType ermitteln und ggf. Fehler melden */
			MetadataType mdt;
			try {
				mdt = UghHelper.getMetadataType(this.prefs, metadata);
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
				/* ohne vorhandenes FirstChild, kann dieses nicht zurückgegeben werden */
				if (resultFirst == null) {
					if(logger.isInfoEnabled()){
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
					if (logger.isEnabledFor(Level.WARN)) {
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
					if (logger.isEnabledFor(Level.WARN)) {
						logger.warn("Can not replace variable for METS: " + metadata);
					}
				}
				break;

			}
			return result;
		} else {
			return "";
		}
	}

	/**
	 * Metadatum von übergebenen Docstruct ermitteln, im Fehlerfall wird null zurückgegeben
	 * ================================================================
	 */
	private String getMetadataValue(DocStruct inDocstruct, MetadataType mdt) {
		List<? extends Metadata> mds = inDocstruct.getAllMetadataByType(mdt);
		if (mds.size() > 0) {
			return ((Metadata) mds.get(0)).getValue();
		} else {
			return null;
		}
	}

	/**
	 * Suche nach regulären Ausdrücken in einem String, liefert alle gefundenen Treffer als Liste zurück
	 * ================================================================
	 */
	public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
		List<MatchResult> results = new ArrayList<MatchResult>();
		for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
			results.add(m.toMatchResult());
		}
		return results;
	}
}
