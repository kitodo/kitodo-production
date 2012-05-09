/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.beans;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.goobi.io.BackupFileRotation;
import org.goobi.production.api.property.xmlbasedprovider.Status;
import org.goobi.production.export.ExportDocket;

import ugh.dl.Fileformat;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import ugh.fileformats.mets.XStream;
import de.sub.goobi.beans.property.DisplayPropertyList;
import de.sub.goobi.beans.property.IGoobiEntity;
import de.sub.goobi.beans.property.IGoobiProperty;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;

public class Prozess implements Serializable, IGoobiEntity {
	private static final Logger myLogger = Logger.getLogger(Prozess.class);
	private static final long serialVersionUID = -6503348094655786275L;
	private Integer id;
	private String titel;
	private String ausgabename;
	private Boolean istTemplate;
	private Boolean inAuswahllisteAnzeigen;
	private Projekt projekt;
	private Date erstellungsdatum;
	private Set<Schritt> schritte;
	private Set<HistoryEvent> history;
	private Set<Werkstueck> werkstuecke;
	private Set<Vorlage> vorlagen;
	private Set<Prozesseigenschaft> eigenschaften;
	private String sortHelperStatus;
	private Integer sortHelperImages;
	private Integer sortHelperArticles;
	private Integer sortHelperMetadata;
	private Integer sortHelperDocstructs;
	private Regelsatz regelsatz;

	private Boolean swappedOut = false;
	private Boolean panelAusgeklappt = false;
	private Boolean selected = false;
	private MetadatenSperrung msp = new MetadatenSperrung();
	Helper help = new Helper();

	public static String DIRECTORY_PREFIX = "orig";
	public static String DIRECTORY_SUFFIX = "tif";

	private DisplayPropertyList displayProperties;
	private String wikifield;

	private static final String TEMPORARY_FILENAME_PREFIX = "temporary_";

	public Prozess() {
		swappedOut = false;
		titel = "";
		istTemplate = false;
		inAuswahllisteAnzeigen = false;
		eigenschaften = new HashSet<Prozesseigenschaft>();
		schritte = new HashSet<Schritt>();
		erstellungsdatum = new Date();

	}

	/*
	 * Getter und Setter
	 */

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSortHelperStatus() {
		return sortHelperStatus;
	}

	public void setSortHelperStatus(String sortHelperStatus) {
		this.sortHelperStatus = sortHelperStatus;
	}

	public boolean isIstTemplate() {
		if (istTemplate == null)
			istTemplate = Boolean.valueOf(false);
		return istTemplate;
	}

	public void setIstTemplate(boolean istTemplate) {
		this.istTemplate = istTemplate;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String inTitel) {
		titel = inTitel.trim();
	}

	public Set<Schritt> getSchritte() {
		return schritte;
	}

	public void setSchritte(Set<Schritt> schritte) {
		this.schritte = schritte;
	}

	public Set<HistoryEvent> getHistory() {
		if (history == null) {
			history = new HashSet<HistoryEvent>();
		}
		return history;
	}

	public void setHistory(Set<HistoryEvent> history) {
		this.history = history;
	}

	public Set<Vorlage> getVorlagen() {
		return vorlagen;
	}

	public void setVorlagen(Set<Vorlage> vorlagen) {
		this.vorlagen = vorlagen;
	}

	public Set<Werkstueck> getWerkstuecke() {
		return werkstuecke;
	}

	public void setWerkstuecke(Set<Werkstueck> werkstuecke) {
		this.werkstuecke = werkstuecke;
	}

	public String getAusgabename() {
		return ausgabename;
	}

	public void setAusgabename(String ausgabename) {
		this.ausgabename = ausgabename;
	}

	public Set<Prozesseigenschaft> getEigenschaften() {
		return eigenschaften;
	}

	public void setEigenschaften(Set<Prozesseigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	/*
	 * Metadaten-Sperrungen zurückgeben
	 */

	public Benutzer getBenutzerGesperrt() {
		Benutzer rueckgabe = null;
		if (MetadatenSperrung.isLocked(id.intValue())) {
			String benutzerID = msp.getLockBenutzer(id.intValue());
			try {
				rueckgabe = new BenutzerDAO().get(new Integer(benutzerID));
			} catch (Exception e) {
				// TODO Meldung in messages implementieren
				Helper.setFehlerMeldung(Helper.getTranslation("userNotFound"), e);
			}
		}
		return rueckgabe;
	}

	public long getMinutenGesperrt() {
		return msp.getLockSekunden(id.longValue()) / 60;
	}

	public long getSekundenGesperrt() {
		return msp.getLockSekunden(id.longValue()) % 60;
	}

	/*
	 * Metadaten- und ImagePfad
	 */

	public String getImagesTifDirectory() throws IOException, InterruptedException, SwapException, DAOException {
		File dir = new File(getImagesDirectory());
		DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");
		DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
		/* nur die _tif-Ordner anzeigen, die nicht mir orig_ anfangen */
		FilenameFilter filterVerz = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith("_" + DIRECTORY_SUFFIX) && !name.startsWith(DIRECTORY_PREFIX + "_"));
			}
		};

		String tifOrdner = "";
		String[] verzeichnisse = dir.list(filterVerz);

		if (verzeichnisse != null) {
			for (int i = 0; i < verzeichnisse.length; i++) {
				tifOrdner = verzeichnisse[i];
			}
		}

		if (tifOrdner.equals(""))
			tifOrdner = titel + "_" + DIRECTORY_SUFFIX;

		String rueckgabe = getImagesDirectory() + tifOrdner;

		if (!rueckgabe.endsWith(File.separator)) {
			rueckgabe += File.separator;
		}
		if (!ConfigMain.getBooleanParameter("useOrigFolder", true) && ConfigMain.getBooleanParameter("createOrigFolderIfNotExists", false)) {
			if (!new File(rueckgabe).exists()) {
				new Helper().createMetaDirectory(rueckgabe);
			}
		}
		return rueckgabe;
	}

	/*
	 * @return true if the Tif-Image-Directory exists, false if not
	 */
	public Boolean getTifDirectoryExists() {
		File testMe;
		try {
			testMe = new File(getImagesTifDirectory());
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		} catch (SwapException e) {
			return false;
		} catch (DAOException e) {
			return false;
		}
		if (testMe.list() == null) {
			return false;
		}
		if (testMe.exists() && testMe.list().length > 0) {
			return true;
		} else {
			return false;
		}
	}

	public String getImagesOrigDirectory() throws IOException, InterruptedException, SwapException, DAOException {
		if (ConfigMain.getBooleanParameter("useOrigFolder", true)) {
			File dir = new File(getImagesDirectory());
			DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");
			DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
			/* nur die _tif-Ordner anzeigen, die mit orig_ anfangen */
			FilenameFilter filterVerz = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.endsWith("_" + DIRECTORY_SUFFIX) && name.startsWith(DIRECTORY_PREFIX + "_"));
				}
			};

			String origOrdner = "";
			String[] verzeichnisse = dir.list(filterVerz);
			for (int i = 0; i < verzeichnisse.length; i++) {
				origOrdner = verzeichnisse[i];
			}
			if (origOrdner.equals("")) {
				origOrdner = DIRECTORY_PREFIX + "_" + titel + "_" + DIRECTORY_SUFFIX;
			}
			String rueckgabe = getImagesDirectory() + origOrdner + File.separator;
			if (!new File(rueckgabe).exists() && ConfigMain.getBooleanParameter("createOrigFolderIfNotExists", false)) {
				new Helper().createMetaDirectory(rueckgabe);
			}
			return rueckgabe;
		} else {
			return getImagesTifDirectory();
		}
	}

	public String getImagesDirectory() throws IOException, InterruptedException, SwapException, DAOException {
		String pfad = getProcessDataDirectory() + "images" + File.separator;
		if (!new File(pfad).exists())
			new Helper().createMetaDirectory(pfad);
		return pfad;
	}

	public String getProcessDataDirectory() throws IOException, InterruptedException, SwapException, DAOException {
		String pfad = getProcessDataDirectoryIgnoreSwapping();

		if (isSwappedOutGui()) {
			ProcessSwapInTask pst = new ProcessSwapInTask();
			pst.initialize(this);
			pst.execute();
			if (pst.getStatusProgress() == -1) {
				if (!new File(pfad, "images").exists() && !new File(pfad, "meta.xml").exists())
					throw new SwapException(pst.getStatusMessage());
				else
					setSwappedOutGui(false);
				new ProzessDAO().save(this);
			}
		}
		return pfad;
	}

	public String getOcrDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getProcessDataDirectory() + "ocr" + File.separator;
	}

	public String getTxtDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getOcrDirectory() + titel + "_txt" + File.separator;
	}

	public String getWordDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getOcrDirectory() + titel + "_wc" + File.separator;
	}

	public String getPdfDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getOcrDirectory() + titel + "_pdf" + File.separator;
	}

	public String getProcessDataDirectoryIgnoreSwapping() throws IOException, InterruptedException, SwapException, DAOException {
		String pfad = help.getGoobiDataDirectory() + id.intValue() + File.separator;
		pfad = pfad.replaceAll(" ", "__");
		if (!new File(pfad).exists())
			new Helper().createMetaDirectory(pfad);
		return pfad;
	}

	/*
	 * ##################################################### ##################################################### ## ## Helper ##
	 * ##################################################### ####################################################
	 */

	public Projekt getProjekt() {
		return projekt;
	}

	public void setProjekt(Projekt projekt) {
		this.projekt = projekt;
	}

	public Regelsatz getRegelsatz() {
		return regelsatz;
	}

	public void setRegelsatz(Regelsatz regelsatz) {
		this.regelsatz = regelsatz;
	}

	public int getSchritteSize() {
		if (schritte == null)
			return 0;
		else
			return schritte.size();
	}

	public List<Schritt> getSchritteList() {
		List<Schritt> temp = new ArrayList<Schritt>();
		if (schritte != null)
			temp.addAll(schritte);
		return temp;
	}

	public int getHistorySize() {
		if (history == null)
			return 0;
		else
			return history.size();
	}

	public List<HistoryEvent> getHistoryList() {
		List<HistoryEvent> temp = new ArrayList<HistoryEvent>();
		if (history != null)
			temp.addAll(history);
		return temp;
	}

	public int getEigenschaftenSize() {
		if (eigenschaften == null)
			return 0;
		else
			return eigenschaften.size();
	}

	public List<Prozesseigenschaft> getEigenschaftenList() {
		if (eigenschaften == null) {
			return new ArrayList<Prozesseigenschaft>();
		} else {
			return new ArrayList<Prozesseigenschaft>(eigenschaften);
		}
	}

	public int getWerkstueckeSize() {
		if (werkstuecke == null)
			return 0;
		else
			return werkstuecke.size();
	}

	public List<Werkstueck> getWerkstueckeList() {
		if (werkstuecke == null)
			return new ArrayList<Werkstueck>();
		else
			return new ArrayList<Werkstueck>(werkstuecke);
	}

	public int getVorlagenSize() {
		if (vorlagen == null) {
			vorlagen = new HashSet<Vorlage>();
		}
		return vorlagen.size();
	}

	public List<Vorlage> getVorlagenList() {
		if (vorlagen == null) {
			vorlagen = new HashSet<Vorlage>();
		}
		return new ArrayList<Vorlage>(vorlagen);
	}

	public Integer getSortHelperArticles() {
		if (sortHelperArticles == null) {
			sortHelperArticles = 0;
		}
		return sortHelperArticles;
	}

	public void setSortHelperArticles(Integer sortHelperArticles) {
		this.sortHelperArticles = sortHelperArticles;
	}

	public Integer getSortHelperImages() {
		if (sortHelperImages == null) {
			sortHelperImages = 0;
		}
		return sortHelperImages;
	}

	public void setSortHelperImages(Integer sortHelperImages) {
		this.sortHelperImages = sortHelperImages;
	}

	public Integer getSortHelperMetadata() {
		if (sortHelperMetadata == null) {
			sortHelperMetadata = 0;
		}
		return sortHelperMetadata;
	}

	public void setSortHelperMetadata(Integer sortHelperMetadata) {
		this.sortHelperMetadata = sortHelperMetadata;
	}

	public Integer getSortHelperDocstructs() {
		if (sortHelperDocstructs == null) {
			sortHelperDocstructs = 0;
		}
		return sortHelperDocstructs;
	}

	public void setSortHelperDocstructs(Integer sortHelperDocstructs) {
		this.sortHelperDocstructs = sortHelperDocstructs;
	}

	public boolean isInAuswahllisteAnzeigen() {
		return inAuswahllisteAnzeigen;
	}

	public void setInAuswahllisteAnzeigen(boolean inAuswahllisteAnzeigen) {
		this.inAuswahllisteAnzeigen = inAuswahllisteAnzeigen;
	}

	public boolean isPanelAusgeklappt() {
		return panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

	public Schritt getAktuellerSchritt() {
		for (Schritt step : getSchritteList()) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN || step.getBearbeitungsstatusEnum() == StepStatus.INWORK)
				return step;
		}
		return null;
	}

	public boolean isSelected() {
		return (selected == null ? false : selected);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Date getErstellungsdatum() {
		return erstellungsdatum;
	}

	public void setErstellungsdatum(Date erstellungsdatum) {
		this.erstellungsdatum = erstellungsdatum;
	}

	/*
	 * Auswertung des Fortschritts
	 */

	// TODO: Remove three out of four methods

	public String getFortschritt() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (Schritt step : schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE)
				abgeschlossen++;
			else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED)
				offen++;
			else
				inBearbeitung++;
		}
		double offen2 = 0;
		double inBearbeitung2 = 0;
		double abgeschlossen2 = 0;

		if ((offen + inBearbeitung + abgeschlossen) == 0)
			offen = 1;

		offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		abgeschlossen2 = 100 - offen2 - inBearbeitung2;
		// (abgeschlossen * 100) / (offen + inBearbeitung + abgeschlossen);
		java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
		return df.format(abgeschlossen2) + df.format(inBearbeitung2) + df.format(offen2);
	}

	public int getFortschritt1() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (Schritt step : schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE)
				abgeschlossen++;
			else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED)
				offen++;
			else
				inBearbeitung++;
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0)
			offen = 1;
		return (offen * 100) / (offen + inBearbeitung + abgeschlossen);
	}

	public int getFortschritt2() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (Schritt step : schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE)
				abgeschlossen++;
			else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED)
				offen++;
			else
				inBearbeitung++;
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0)
			offen = 1;
		return (inBearbeitung * 100) / (offen + inBearbeitung + abgeschlossen);
	}

	public int getFortschritt3() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (Schritt step : schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE)
				abgeschlossen++;
			else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED)
				offen++;
			else
				inBearbeitung++;
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0)
			offen = 1;
		double offen2 = 0;
		double inBearbeitung2 = 0;
		double abgeschlossen2 = 0;

		offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		abgeschlossen2 = 100 - offen2 - inBearbeitung2;
		return (int) abgeschlossen2;
	}

	/*
	 * ##################################################### ##################################################### ## ## RDF-Datei ##
	 * ##################################################### ####################################################
	 */

	public String getMetadataFilePath() throws IOException, InterruptedException, SwapException, DAOException {
		return getProcessDataDirectory() + "meta.xml";
	}

	public String getTemplateFilePath() throws IOException, InterruptedException, SwapException, DAOException {
		return getProcessDataDirectory() + "template.xml";
	}

	public String getFulltextFilePath() throws IOException, InterruptedException, SwapException, DAOException {
		return getProcessDataDirectory() + "fulltext.xml";
	}

	public Fileformat readMetadataFile() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException,
			WriteException {

		if (! checkForMetadataFile()) {
		    throw new IOException("Metadata file: " + getMetadataFilePath() + " not found!");
		}

		/* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
		String type = MetadatenHelper.getMetaFileType(getMetadataFilePath());
		myLogger.debug("current meta.xml file type for id " + getId() + ": " + type);
		Fileformat ff = null;
		if (type.equals("metsmods")) {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.mets.xml"));
			ff = new MetsModsImportExport(regelsatz.getPreferences());
		} else if (type.equals("mets")) {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.mets.xml"));
			ff = new MetsMods(regelsatz.getPreferences());
		} else if (type.equals("xstream")) {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.xstream.xml"));
			ff = new XStream(regelsatz.getPreferences());
		} else {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.rdf.xml"));
			ff = new RDFFile(regelsatz.getPreferences());
		}
		ff.read(getMetadataFilePath());
		return ff;
	}

	// backup of meta.xml
	private void createBackupFile() throws IOException, InterruptedException, SwapException, DAOException {
		int numberOfBackups = 0;
		String format = "";

		if (ConfigMain.getIntParameter("numberOfMetaBackups") != 0) {
			numberOfBackups = ConfigMain.getIntParameter("numberOfMetaBackups");
			format = ConfigMain.getParameter("formatOfMetaBackups");
		}

		if (format != null) {
			myLogger.info("Option 'formatOfMetaBackups' is deprecated and will be ignored.");
		}

		if (numberOfBackups != 0) {
			BackupFileRotation bfr = new BackupFileRotation();
			bfr.setNumberOfBackups(numberOfBackups);
			bfr.setFormat("meta.*\\.xml");
			bfr.setProcessDataDirectory(getProcessDataDirectory());
			bfr.performBackup();
		} else {
			myLogger.warn("No backup configured for meta data files.");
		}
	}

	private boolean checkForMetadataFile() throws IOException, InterruptedException, SwapException, DAOException, WriteException, PreferencesException {
		boolean result = true;
		File f = new File(getMetadataFilePath());

		if (!f.exists()) {
			String errorMessage = Helper.getTranslation("metadataFileNotFound") + " " + f.getAbsolutePath();
			myLogger.warn(errorMessage);
			Helper.setFehlerMeldung(errorMessage);
			result = false;
		}

		return result;
	}

	private String getTemporaryMetadataFileName(String fileName) {
		File temporaryFile = new File(fileName);
		String directoryPath = temporaryFile.getParentFile().getPath();
		String temporaryFileName = TEMPORARY_FILENAME_PREFIX + temporaryFile.getName();

		return directoryPath + File.separator + temporaryFileName;
	}

	private void removePrefixFromRelatedMetsAnchorFileFor(String temporaryMetadataFilename) {
		File temporaryFile = new File(temporaryMetadataFilename);
		File temporaryAnchorFile;

		String directoryPath = temporaryFile.getParentFile().getPath();
		String temporaryAnchorFileName = temporaryFile.getName().replace("meta.xml", "meta_anchor.xml");

		temporaryAnchorFile = new File(directoryPath + File.separator + temporaryAnchorFileName);

		if (temporaryAnchorFile.exists()) {
			String anchorFileName = temporaryAnchorFileName.replace(TEMPORARY_FILENAME_PREFIX, "");

			temporaryAnchorFileName = directoryPath + File.separator + temporaryAnchorFileName;
			anchorFileName = directoryPath + File.separator + anchorFileName;

			FilesystemHelper.renameFile(temporaryAnchorFileName, anchorFileName);
		}
	}

	public void writeMetadataFile(Fileformat gdzfile) throws IOException, InterruptedException, SwapException, DAOException, WriteException,
			PreferencesException {
		Fileformat ff;
		String metadataFileName;
		String temporaryMetadataFileName;
		boolean writeResult;

		switch (MetadataFormat.findFileFormatsHelperByName(projekt.getFileFormatInternal())) {
		case METS:
			ff = new MetsMods(regelsatz.getPreferences());
			break;

		case RDF:
			ff = new RDFFile(regelsatz.getPreferences());
			break;

		default:
			ff = new XStream(regelsatz.getPreferences());
			break;
		}

		metadataFileName = getMetadataFilePath();
		temporaryMetadataFileName = getTemporaryMetadataFileName(metadataFileName);

		ff.setDigitalDocument(gdzfile.getDigitalDocument());
		writeResult = ff.write(temporaryMetadataFileName);
		if (writeResult) {
			createBackupFile();
			FilesystemHelper.renameFile(temporaryMetadataFileName, metadataFileName);
			removePrefixFromRelatedMetsAnchorFileFor(temporaryMetadataFileName);
		}
	}

	public void writeMetadataAsTemplateFile(Fileformat inFile) throws IOException, InterruptedException, SwapException, DAOException, WriteException,
			PreferencesException {
		inFile.write(getTemplateFilePath());
	}

	public Fileformat readMetadataAsTemplateFile() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException,
			DAOException {
		if (new File(getTemplateFilePath()).exists()) {
			Fileformat ff = null;
			String type = MetadatenHelper.getMetaFileType(getTemplateFilePath());
			myLogger.debug("current template.xml file type: " + type);
			if (type.equals("mets")) {
				ff = new MetsMods(regelsatz.getPreferences());
			} else if (type.equals("xstream")) {
				ff = new XStream(regelsatz.getPreferences());
			} else {
				ff = new RDFFile(regelsatz.getPreferences());
			}
			ff.read(getTemplateFilePath());
			return ff;
		} else
			throw new IOException("File does not exist: " + getTemplateFilePath());
	}

	/**
	 * prüfen, ob der Vorgang Schritte enthält, die keinem Benutzer und keiner Benutzergruppe zugewiesen ist
	 * ================================================================
	 */
	public boolean isContainsUnreachableSteps() {
		for (Schritt s : getSchritteList()) {
			if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0)
				return true;
		}
		return false;
	}

	/**
	 * check if there is one task in edit mode, where the user has the rights to write to image folder
	 * ================================================================
	 */
	public boolean isImageFolderInUse() {
		for (Schritt s : getSchritteList()) {
			if (s.getBearbeitungsstatusEnum() == StepStatus.INWORK && s.isTypImagesSchreiben()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get user of task in edit mode with rights to write to image folder ================================================================
	 */
	public Benutzer getImageFolderInUseUser() {
		for (Schritt s : getSchritteList()) {
			if (s.getBearbeitungsstatusEnum() == StepStatus.INWORK && s.isTypImagesSchreiben()) {
				return s.getBearbeitungsbenutzer();
			}
		}
		return null;
	}

	/**
	 * here differet Getters and Setters for the same value, because Hibernate does not like bit-Fields with null Values (thats why Boolean) and
	 * MyFaces seams not to like Boolean (thats why boolean for the GUI) ================================================================
	 */
	public Boolean isSwappedOutHibernate() {
		return swappedOut;
	}

	public void setSwappedOutHibernate(Boolean inSwappedOut) {
		swappedOut = inSwappedOut;
	}

	public boolean isSwappedOutGui() {
		if (swappedOut == null)
			swappedOut = false;
		return swappedOut;
	}

	public void setSwappedOutGui(boolean inSwappedOut) {
		swappedOut = inSwappedOut;
	}

	public String getWikifield() {
		return wikifield;
	}

	public void setWikifield(String wikifield) {
		this.wikifield = wikifield;
	}

	public Status getStatus() {
		return Status.getProcessStatus(this);
	}

	public List<IGoobiProperty> getProperties() {
		List<IGoobiProperty> returnlist = new ArrayList<IGoobiProperty>();
		returnlist.addAll(getEigenschaftenList());

		return returnlist;
	}

	public void addProperty(IGoobiProperty toAdd) {
		eigenschaften.add((Prozesseigenschaft) toAdd);
	}

	public void removeProperty(IGoobiProperty toRemove) {
		getEigenschaften().remove(toRemove);
		toRemove.setOwningEntity(null);
	}

	/**
	 * 
	 * @return instance of {@link DisplayPropertyList}
	 */

	public DisplayPropertyList getDisplayProperties() {
		if (displayProperties == null) {
			displayProperties = new DisplayPropertyList(this);
		}
		return displayProperties;
	}

	public void refreshProperties() {
		displayProperties = null;
	}

	public String downloadDocket() {
		myLogger.debug("generate run note for process " + id);
		String rootpath = ConfigMain.getParameter("xsltFolder");
		File xsltfile = new File(rootpath, "docket.xsl");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			String fileName = titel + ".pdf";
			ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
			String contentType = servletContext.getMimeType(fileName);
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

			// write run note to servlet output stream
			try {
				ServletOutputStream out = response.getOutputStream();
				ExportDocket ern = new ExportDocket();
				ern.startExport(this, out, xsltfile.getAbsolutePath());
				out.flush();
			} catch (IOException e) {
				myLogger.error("IOException while exporting run note", e);
			}

			facesContext.responseComplete();
		}
		return "";
	}
}
