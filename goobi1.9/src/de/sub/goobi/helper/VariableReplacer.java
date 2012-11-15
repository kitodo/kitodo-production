package de.sub.goobi.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class VariableReplacer {

	private enum MetadataLevel {
		ALL, FIRSTCHILD, TOPSTRUCT;
	}

	private static final Logger logger = Logger.getLogger(VariableReplacer.class);

	DigitalDocument dd;
	Prefs prefs;
	UghHelper uhelp;
	// Helper help;
	// $(meta.abc)
	private final String namespaceMeta = "\\$\\(meta\\.([\\w.-]*)\\)";
	// $(abc)
	// private final String namespaceOther = "\\$\\([\\w.-]*\\)";

	private Prozess process;
	private Schritt step;

	@SuppressWarnings("unused")
	private VariableReplacer() {
	}

	public VariableReplacer(DigitalDocument inDigitalDocument, Prefs inPrefs, Prozess p, Schritt s) {
		this.dd = inDigitalDocument;
		this.prefs = inPrefs;
		this.uhelp = new UghHelper();
		// help = new Helper();
		this.process = p;
		this.step = s;
	}

	/**
	 * Variablen innerhalb eines Strings ersetzen. Dabei vergleichbar zu Ant die Variablen durchlaufen und aus dem Digital Document holen
	 * ================================================================
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
			String processpath = this.process.getProcessDataDirectory().replace("\\", "/");
			String tifpath = this.process.getImagesTifDirectory(false).replace("\\", "/");
			String imagepath = this.process.getImagesDirectory().replace("\\", "/");
			String origpath = this.process.getImagesOrigDirectory(false).replace("\\", "/");
			String metaFile = this.process.getMetadataFilePath().replace("\\", "/");
			String ocrBasisPath = this.process.getOcrDirectory().replace("\\", "/");
			String ocrPlaintextPath = this.process.getTxtDirectory().replace("\\", "/");
			// TODO name ändern?
			String sourcePath = this.process.getSourceDirectory().replace("\\", "/");
			String importPath = this.process.getImportDirectory().replace("\\", "/");
			String myprefs = ConfigMain.getParameter("RegelsaetzeVerzeichnis") + this.process.getRegelsatz().getDatei();

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
				inString = inString.replace("(processtitle)", this.process.getTitel());
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

			if (this.step != null) {
				String stepId = String.valueOf(this.step.getId());
				String stepname = this.step.getTitel();

				inString = inString.replace("(stepid)", stepId);
				inString = inString.replace("(stepname)", stepname);
			}

			// replace WerkstueckEigenschaft, usage: (product.PROPERTYTITLE)

			for (MatchResult r : findRegexMatches("\\(product\\.([\\w.-]*)\\)", inString)) {
				String propertyTitle = r.group(1);
				for (Werkstueck ws : this.process.getWerkstueckeList()) {
					for (Werkstueckeigenschaft we : ws.getEigenschaftenList()) {
						if (we.getTitel().equalsIgnoreCase(propertyTitle)) {
							inString = inString.replace(r.group(), we.getWert());
							break;
						}
					}
				}
			}

			// replace Vorlageeigenschaft, usage: (template.PROPERTYTITLE)

			for (MatchResult r : findRegexMatches("\\(template\\.([\\w.-]*)\\)", inString)) {
				String propertyTitle = r.group(1);
				for (Vorlage v : this.process.getVorlagenList()) {
					for (Vorlageeigenschaft ve : v.getEigenschaftenList()) {
						if (ve.getTitel().equalsIgnoreCase(propertyTitle)) {
							inString = inString.replace(r.group(), ve.getWert());
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
				mdt = this.uhelp.getMetadataType(this.prefs, metadata);
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
					logger.info("Can not replace firstChild-variable for METS: " + metadata);
					result = "";
					// help.setFehlerMeldung("Can not replace firstChild-variable for METS: " + metadata);
				} else {
					result = resultFirst;
				}
				break;

			case TOPSTRUCT:
				if (resultTop == null) {
					result = "";
					logger.warn("Can not replace topStruct-variable for METS: " + metadata);
					// help.setFehlerMeldung("Can not replace topStruct-variable for METS: " + metadata);
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
					logger.warn("Can not replace variable for METS: " + metadata);
					// help.setFehlerMeldung("Can not replace variable for METS: " + metadata);
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