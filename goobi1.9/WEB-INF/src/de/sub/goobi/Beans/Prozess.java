package de.sub.goobi.Beans;

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
import de.sub.goobi.Beans.Property.DisplayPropertyList;
import de.sub.goobi.Beans.Property.IGoobiEntity;
import de.sub.goobi.Beans.Property.IGoobiProperty;
import de.sub.goobi.Metadaten.MetadatenHelper;
import de.sub.goobi.Metadaten.MetadatenSperrung;
import de.sub.goobi.Persistence.BenutzerDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FileUtils;
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
	// private Batch batch;
	private Integer batch;

	private Boolean swappedOut = false;
	private Boolean panelAusgeklappt = false;
	private Boolean selected = false;
	private final MetadatenSperrung msp = new MetadatenSperrung();
	Helper help = new Helper();

	public static String DIRECTORY_PREFIX = "orig";
	public static String DIRECTORY_SUFFIX = "images";

	private static int numberOfBackups = 0;
	private static String FORMAT = "";

	private DisplayPropertyList displayProperties;
	private String wikifield;

	public Prozess() {
		this.swappedOut = false;
		this.titel = "";
		this.istTemplate = false;
		this.inAuswahllisteAnzeigen = false;
		this.eigenschaften = new HashSet<Prozesseigenschaft>();
		this.schritte = new HashSet<Schritt>();
		this.erstellungsdatum = new Date();

	}

	/*
	 * Getter und Setter
	 */

	@Override
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSortHelperStatus() {
		return this.sortHelperStatus;
	}

	public void setSortHelperStatus(String sortHelperStatus) {
		this.sortHelperStatus = sortHelperStatus;
	}

	public boolean isIstTemplate() {
		if (this.istTemplate == null) {
			this.istTemplate = Boolean.valueOf(false);
		}
		return this.istTemplate;
	}

	public void setIstTemplate(boolean istTemplate) {
		this.istTemplate = istTemplate;
	}

	public String getTitel() {
		return this.titel;
	}

	public void setTitel(String inTitel) {
		this.titel = inTitel.trim();
	}

	public Set<Schritt> getSchritte() {
		return this.schritte;
	}

	public void setSchritte(Set<Schritt> schritte) {
		this.schritte = schritte;
	}

	public Set<HistoryEvent> getHistory() {
		if (this.history == null) {
			this.history = new HashSet<HistoryEvent>();
		}
		return this.history;
	}

	public void setHistory(Set<HistoryEvent> history) {
		this.history = history;
	}

	public Set<Vorlage> getVorlagen() {
		return this.vorlagen;
	}

	public void setVorlagen(Set<Vorlage> vorlagen) {
		this.vorlagen = vorlagen;
	}

	public Set<Werkstueck> getWerkstuecke() {
		return this.werkstuecke;
	}

	public void setWerkstuecke(Set<Werkstueck> werkstuecke) {
		this.werkstuecke = werkstuecke;
	}

	public String getAusgabename() {
		return this.ausgabename;
	}

	public void setAusgabename(String ausgabename) {
		this.ausgabename = ausgabename;
	}

	public Set<Prozesseigenschaft> getEigenschaften() {
		return this.eigenschaften;
	}

	public void setEigenschaften(Set<Prozesseigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	/*
	 * Metadaten-Sperrungen zurückgeben
	 */

	public Benutzer getBenutzerGesperrt() {
		Benutzer rueckgabe = null;
		if (MetadatenSperrung.isLocked(this.id.intValue())) {
			String benutzerID = this.msp.getLockBenutzer(this.id.intValue());
			try {
				rueckgabe = new BenutzerDAO().get(new Integer(benutzerID));
			} catch (Exception e) {
				Helper.setFehlerMeldung(Helper.getTranslation("userNotFound"), e);
			}
		}
		return rueckgabe;
	}

	public long getMinutenGesperrt() {
		return this.msp.getLockSekunden(this.id.longValue()) / 60;
	}

	public long getSekundenGesperrt() {
		return this.msp.getLockSekunden(this.id.longValue()) % 60;
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
			@Override
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

		if (tifOrdner.equals("")) {
			tifOrdner = this.titel + "_" + DIRECTORY_SUFFIX;
		}

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
				@Override
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
				origOrdner = DIRECTORY_PREFIX + "_" + this.titel + "_" + DIRECTORY_SUFFIX;
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
		if (!new File(pfad).exists()) {
			new Helper().createMetaDirectory(pfad);
		}
		return pfad;
	}

	public String getProcessDataDirectory() throws IOException, InterruptedException, SwapException, DAOException {
		String pfad = getProcessDataDirectoryIgnoreSwapping();

		if (isSwappedOutGui()) {
			ProcessSwapInTask pst = new ProcessSwapInTask();
			pst.initialize(this);
			pst.execute();
			if (pst.getStatusProgress() == -1) {
				if (!new File(pfad, "images").exists() && !new File(pfad, "meta.xml").exists()) {
					throw new SwapException(pst.getStatusMessage());
				} else {
					setSwappedOutGui(false);
				}
				new ProzessDAO().save(this);
			}
		}
		return pfad;
	}

	public String getOcrDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getProcessDataDirectory() + "ocr" + File.separator;
	}

	public String getTxtDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getOcrDirectory() + this.titel + "_txt" + File.separator;
	}

	public String getWordDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getOcrDirectory() + this.titel + "_wc" + File.separator;
	}

	public String getPdfDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getOcrDirectory() + this.titel + "_pdf" + File.separator;
	}

	public String getAltoDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		// TODO FIXME
		return getOcrDirectory() + this.titel + "_xml" + File.separator;
	}

	public String getSourceDirectory() throws SwapException, DAOException, IOException, InterruptedException {
		return getProcessDataDirectory() + "source" + File.separator;
	}

	public String getProcessDataDirectoryIgnoreSwapping() throws IOException, InterruptedException, SwapException, DAOException {
		String pfad = this.help.getGoobiDataDirectory() + this.id.intValue() + File.separator;
		pfad = pfad.replaceAll(" ", "__");
		if (!new File(pfad).exists()) {
			new Helper().createMetaDirectory(pfad);
		}
		return pfad;
	}

	/*
	 * ##################################################### ##################################################### ## ## Helper ##
	 * ##################################################### ####################################################
	 */

	public Projekt getProjekt() {
		return this.projekt;
	}

	public void setProjekt(Projekt projekt) {
		this.projekt = projekt;
	}

	// public Batch getBatch() {
	// return this.batch;
	// }
	//
	// public void setBatch(Batch batch) {
	// this.batch = batch;
	// }

	public Integer getBatch() {
		return this.batch;
	}

	public void setBatch(Integer batch) {
		this.batch = batch;
	}

	public Regelsatz getRegelsatz() {
		return this.regelsatz;
	}

	public void setRegelsatz(Regelsatz regelsatz) {
		this.regelsatz = regelsatz;
	}

	public int getSchritteSize() {
		if (this.schritte == null) {
			return 0;
		} else {
			return this.schritte.size();
		}
	}

	public List<Schritt> getSchritteList() {
		List<Schritt> temp = new ArrayList<Schritt>();
		if (this.schritte != null) {
			temp.addAll(this.schritte);
		}
		return temp;
	}

	public int getHistorySize() {
		if (this.history == null) {
			return 0;
		} else {
			return this.history.size();
		}
	}

	public List<HistoryEvent> getHistoryList() {
		List<HistoryEvent> temp = new ArrayList<HistoryEvent>();
		if (this.history != null) {
			temp.addAll(this.history);
		}
		return temp;
	}

	public int getEigenschaftenSize() {
		if (this.eigenschaften == null) {
			return 0;
		} else {
			return this.eigenschaften.size();
		}
	}

	public List<Prozesseigenschaft> getEigenschaftenList() {
		if (this.eigenschaften == null) {
			return new ArrayList<Prozesseigenschaft>();
		} else {
			return new ArrayList<Prozesseigenschaft>(this.eigenschaften);
		}
	}

	public int getWerkstueckeSize() {
		if (this.werkstuecke == null) {
			return 0;
		} else {
			return this.werkstuecke.size();
		}
	}

	public List<Werkstueck> getWerkstueckeList() {
		if (this.werkstuecke == null) {
			return new ArrayList<Werkstueck>();
		} else {
			return new ArrayList<Werkstueck>(this.werkstuecke);
		}
	}

	public int getVorlagenSize() {
		if (this.vorlagen == null) {
			this.vorlagen = new HashSet<Vorlage>();
		}
		return this.vorlagen.size();
	}

	public List<Vorlage> getVorlagenList() {
		if (this.vorlagen == null) {
			this.vorlagen = new HashSet<Vorlage>();
		}
		return new ArrayList<Vorlage>(this.vorlagen);
	}

	public Integer getSortHelperArticles() {
		if (this.sortHelperArticles == null) {
			this.sortHelperArticles = 0;
		}
		return this.sortHelperArticles;
	}

	public void setSortHelperArticles(Integer sortHelperArticles) {
		this.sortHelperArticles = sortHelperArticles;
	}

	public Integer getSortHelperImages() {
		if (this.sortHelperImages == null) {
			this.sortHelperImages = 0;
		}
		return this.sortHelperImages;
	}

	public void setSortHelperImages(Integer sortHelperImages) {
		this.sortHelperImages = sortHelperImages;
	}

	public Integer getSortHelperMetadata() {
		if (this.sortHelperMetadata == null) {
			this.sortHelperMetadata = 0;
		}
		return this.sortHelperMetadata;
	}

	public void setSortHelperMetadata(Integer sortHelperMetadata) {
		this.sortHelperMetadata = sortHelperMetadata;
	}

	public Integer getSortHelperDocstructs() {
		if (this.sortHelperDocstructs == null) {
			this.sortHelperDocstructs = 0;
		}
		return this.sortHelperDocstructs;
	}

	public void setSortHelperDocstructs(Integer sortHelperDocstructs) {
		this.sortHelperDocstructs = sortHelperDocstructs;
	}

	public boolean isInAuswahllisteAnzeigen() {
		return this.inAuswahllisteAnzeigen;
	}

	public void setInAuswahllisteAnzeigen(boolean inAuswahllisteAnzeigen) {
		this.inAuswahllisteAnzeigen = inAuswahllisteAnzeigen;
	}

	public boolean isPanelAusgeklappt() {
		return this.panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

	public Schritt getAktuellerSchritt() {
		for (Schritt step : getSchritteList()) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN || step.getBearbeitungsstatusEnum() == StepStatus.INWORK) {
				return step;
			}
		}
		return null;
	}

	public boolean isSelected() {
		return (this.selected == null ? false : this.selected);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Date getErstellungsdatum() {
		return this.erstellungsdatum;
	}

	public void setErstellungsdatum(Date erstellungsdatum) {
		this.erstellungsdatum = erstellungsdatum;
	}

	/*
	 * Auswertung des Fortschritts
	 */

	public String getFortschritt() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (Schritt step : this.schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		double offen2 = 0;
		double inBearbeitung2 = 0;
		double abgeschlossen2 = 0;

		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}

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

		for (Schritt step : this.schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}
		return (offen * 100) / (offen + inBearbeitung + abgeschlossen);
	}

	public int getFortschritt2() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (Schritt step : this.schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}
		return (inBearbeitung * 100) / (offen + inBearbeitung + abgeschlossen);
	}

	public int getFortschritt3() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (Schritt step : this.schritte) {
			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (step.getBearbeitungsstatusEnum() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}
		double offen2 = 0;
		double inBearbeitung2 = 0;
		double abgeschlossen2 = 0;

		offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		abgeschlossen2 = 100 - offen2 - inBearbeitung2;
		return (int) abgeschlossen2;
	}

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
		checkForMetadataFile();
		/* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
		String type = MetadatenHelper.getMetaFileType(getMetadataFilePath());
		// createBackupFile(getNumberOfBackups());
		myLogger.debug("current meta.xml file type for id " + getId() + ": " + type);
		Fileformat ff = null;
		if (type.equals("metsmods")) {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.mets.xml"));
			ff = new MetsModsImportExport(this.regelsatz.getPreferences());
		} else if (type.equals("mets")) {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.mets.xml"));
			ff = new MetsMods(this.regelsatz.getPreferences());
		} else if (type.equals("xstream")) {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.xstream.xml"));
			ff = new XStream(this.regelsatz.getPreferences());
		} else {
			Helper.copyFile(new File(getMetadataFilePath()), new File(getProcessDataDirectory(), "meta.rdf.xml"));
			ff = new RDFFile(this.regelsatz.getPreferences());
		}
		ff.read(getMetadataFilePath());
		return ff;
	}

	// backup of meta.xml

	private void createBackupFile() throws IOException, InterruptedException, SwapException, DAOException {
		if (ConfigMain.getIntParameter("numberOfMetaBackups") != 0) {
			numberOfBackups = ConfigMain.getIntParameter("numberOfMetaBackups");
			FORMAT = ConfigMain.getParameter("formatOfMetaBackups");
		}
		if (numberOfBackups != 0 && FORMAT != null) {
			FilenameFilter filter = new FileUtils.FileListFilter(FORMAT);
			File metaFilePath = new File(getProcessDataDirectory());
			File[] meta = metaFilePath.listFiles(filter);
			List<File> files = Arrays.asList(meta);
			Collections.reverse(files);

			int count;
			if (meta != null) {
				if (files.size() > numberOfBackups) {
					count = numberOfBackups;
				} else {
					count = meta.length;
				}
				while (count > 0) {
					for (File data : files) {
						if (data.getName().endsWith("xml." + (count - 1))) {
							Long lastModified = data.lastModified();
							File newFile = new File(data.toString().substring(0, data.toString().lastIndexOf(".")) + "." + (count));
							data.renameTo(newFile);
							if (lastModified > 0L) {
								newFile.setLastModified(lastModified);
							}
						}
						if (data.getName().endsWith(".xml") && count == 1) {
							Long lastModified = data.lastModified();
							File newFile = new File(data.toString() + ".1");
							data.renameTo(newFile);
							if (lastModified > 0L) {
								newFile.setLastModified(lastModified);
							}
						}
					}
					count--;
				}
			}
		}
	}

	private void checkForMetadataFile() throws IOException, InterruptedException, SwapException, DAOException, WriteException, PreferencesException {
		/* prüfen ob die xml-Datei überhaupt existiert, wenn nicht, neu anlegen */
		File f = new java.io.File(getMetadataFilePath());
		if (!f.exists()) {
			myLogger.warn(Helper.getTranslation("metadataFileNotFound") + f.getAbsolutePath());
			storeDefaultMetaFile(f);
		}
	}

	private void storeDefaultMetaFile(File f) throws IOException {
		// boolean ok = false;
		/* wenn Verzeichnis angelegt wurde, jetzt die xml-Datei anlegen */
		File fstandard = new java.io.File(this.help.getGoobiDataDirectory() + "standard.xml");

		if (!fstandard.exists()) {
			URL standardURL = Helper.class.getResource("standard.xml");
			if (standardURL != null) {
				try {
					Helper.copyFile(new File(standardURL.toURI()), fstandard);
				} catch (URISyntaxException e) {
					throw new IOException("Fehler beim Anlegen der Metdaten-Datei meta.xml (IOException): " + e.getMessage());
				}
			}
		}

		if (fstandard.exists()) {
			try {
				Helper.copyFile(fstandard, f);
			} catch (IOException e) {
				throw new IOException("Fehler beim Anlegen der Metdaten-Datei meta.xml (IOException): " + e.getMessage());
			}
		} else {
			throw new IOException("Fehler beim Anlegen der Metadaten-Datei, standard.xml nicht vorhanden (" + fstandard.getAbsolutePath() + ")");
		}
	}

	public void writeMetadataFile(Fileformat gdzfile) throws IOException, InterruptedException, SwapException, DAOException, WriteException,
			PreferencesException {
		Fileformat ff;
		switch (MetadataFormat.findFileFormatsHelperByName(this.projekt.getFileFormatInternal())) {
		case METS:
			ff = new MetsMods(this.regelsatz.getPreferences());
			break;

		case RDF:
			ff = new RDFFile(this.regelsatz.getPreferences());
			break;

		default:
			ff = new XStream(this.regelsatz.getPreferences());
			break;
		}
		createBackupFile();
		ff.setDigitalDocument(gdzfile.getDigitalDocument());
		ff.write(getMetadataFilePath());
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
				ff = new MetsMods(this.regelsatz.getPreferences());
			} else if (type.equals("xstream")) {
				ff = new XStream(this.regelsatz.getPreferences());
			} else {
				ff = new RDFFile(this.regelsatz.getPreferences());
			}
			ff.read(getTemplateFilePath());
			return ff;
		} else {
			throw new IOException("File does not exist: " + getTemplateFilePath());
		}
	}

	/**
	 * prüfen, ob der Vorgang Schritte enthält, die keinem Benutzer und keiner Benutzergruppe zugewiesen ist
	 * ================================================================
	 */
	public boolean getContainsUnreachableSteps() {
		for (Schritt s : getSchritteList()) {
			if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0) {
				return true;
			}
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
		return this.swappedOut;
	}

	public void setSwappedOutHibernate(Boolean inSwappedOut) {
		this.swappedOut = inSwappedOut;
	}

	public boolean isSwappedOutGui() {
		if (this.swappedOut == null) {
			this.swappedOut = false;
		}
		return this.swappedOut;
	}

	public void setSwappedOutGui(boolean inSwappedOut) {
		this.swappedOut = inSwappedOut;
	}

	public String getWikifield() {
		return this.wikifield;
	}

	public void setWikifield(String wikifield) {
		this.wikifield = wikifield;
	}

	@Override
	public Status getStatus() {
		return Status.getProcessStatus(this);
	}

	@Override
	public List<IGoobiProperty> getProperties() {
		List<IGoobiProperty> returnlist = new ArrayList<IGoobiProperty>();
		returnlist.addAll(getEigenschaftenList());

		return returnlist;
	}

	@Override
	public void addProperty(IGoobiProperty toAdd) {
		this.eigenschaften.add((Prozesseigenschaft) toAdd);
	}

	@Override
	public void removeProperty(IGoobiProperty toRemove) {
		getEigenschaften().remove(toRemove);
		toRemove.setOwningEntity(null);
	}

	/**
	 * 
	 * @return instance of {@link DisplayPropertyList}
	 */

	public DisplayPropertyList getDisplayProperties() {
		if (this.displayProperties == null) {
			this.displayProperties = new DisplayPropertyList(this);
		}
		return this.displayProperties;
	}

	@Override
	public void refreshProperties() {
		this.displayProperties = null;
	}

	public String downloadDocket() {
		myLogger.debug("generate run note for process " + this.id);
		String rootpath = ConfigMain.getParameter("xsltFolder");
		File xsltfile = new File(rootpath, "docket.xsl");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			String fileName = this.titel + ".pdf";
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

	public Schritt getFirstOpenStep() {

		for (Schritt s : getSchritteList()) {
			if (s.getBearbeitungsstatusEnum().equals(StepStatus.OPEN) || s.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)) {
				return s;
			}
		}
		return null;
	}

	public String getMethodFromName(String methodName) {
		java.lang.reflect.Method method;
		try {
			method = this.getClass().getMethod(methodName);
			Object o = method.invoke(this);
			return (String) o;
		} catch (SecurityException e) {
			myLogger.error(e);
		} catch (NoSuchMethodException e) {
			myLogger.error(e);
		} catch (IllegalArgumentException e) {
			myLogger.error(e);
		} catch (IllegalAccessException e) {
			myLogger.error(e);
		} catch (InvocationTargetException e) {
			myLogger.error(e);
		}

		// TODO Auto-generated method stub
		return null;
	}

}
