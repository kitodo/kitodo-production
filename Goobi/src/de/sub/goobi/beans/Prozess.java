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

package de.sub.goobi.beans;

import org.goobi.io.SafeFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.goobi.io.BackupFileRotation;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportDocket;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdom.JDOMException;

import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import ugh.fileformats.mets.XStream;
import de.sub.goobi.beans.Batch.Type;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.DigitalCollections;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.ProzessDAO;

@XmlAccessorType(XmlAccessType.NONE)
// This annotation is to instruct the Jersey API not to generate arbitrary XML
// elements. Further XML elements can be added as needed by annotating with
// @XmlElement, but their respective names should be wisely chosen according to
// the Coding Guidelines (e.g. *english* names).
public class Prozess implements Serializable {
    private static final Logger logger = Logger.getLogger(Prozess.class);
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
    private Set<Batch> batches = new HashSet<Batch>(0);
    private String sortHelperStatus;
    private Integer sortHelperImages;
    private Integer sortHelperArticles;
    private Integer sortHelperMetadata;
    private Integer sortHelperDocstructs;
    private Regelsatz regelsatz;
    private Boolean swappedOut = false;
    private Boolean panelAusgeklappt = false;
    private Boolean selected = false;
    private Docket docket;

    private final MetadatenSperrung msp = new MetadatenSperrung();
    Helper help = new Helper();

    private static final String DIRECTORY_PREFIX = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
    public static final String DIRECTORY_SUFFIX = ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif");

    private String wikifield = "";
    private static final String TEMPORARY_FILENAME_PREFIX = "temporary_";

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
            this.istTemplate = Boolean.FALSE;
        }
        return this.istTemplate;
    }

    public void setIstTemplate(boolean istTemplate) {
        this.istTemplate = istTemplate;
    }

    @XmlAttribute(name="key")
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

    /**
     * The function getHistory() returns the history events for a process or
     * some Hibernate proxy object which may be uninitialized if its contents
     * have not been accessed yet. However, this function is also called by
     * Hibernate itself when its updating the database and in this case it is
     * absolutely fine to return a proxy object uninitialized.
     *
     * If you want to get the history and be sure it has been loaded, use
     * {@link #getHistoryInitialized()} instead.
     *
     * @return the history field of the process which may be not yet loaded
     */
    public Set<HistoryEvent> getHistory() {
        return this.history;
    }

    /**
     * The function getHistoryInitialized() returns the history events for a
     * process and takes care that the object is initialized from Hibernate
     * already and will not be bothered if the Hibernate session ends.
     *
     * @return the history field of the process which is loaded
     */
    public Set<HistoryEvent> getHistoryInitialized() {
        try {
            @SuppressWarnings("unused")
            Session s = Helper.getHibernateSession();
            Hibernate.initialize(this.history);
        } catch (HibernateException e) {
        }
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

    /**
     * The function getBatches() returns the batches for a process or some
     * Hibernate proxy object which may be uninitialized if its contents have
     * not been accessed yet. However, this function is also called by Hibernate
     * itself when its updating the database and in this case it is absolutely
     * fine to return a proxy object uninitialized.
     *
     * If you want to get the history and be sure it has been loaded, use
     * {@link #getBatchesInitialized()} instead.
     *
     * @return the batches field of the process which may be not yet loaded
     */
    public Set<Batch> getBatches() {
        return this.batches;
    }

    /**
     * Returns the batches of the desired type for a process.
     *
     * @param type
     *            type of batches to return
     *
     * @return all batches of the desired type
     */
    public Set<Batch> getBatchesByType(Type type) {
        Set<Batch> batches = getBatchesInitialized();
        if (type != null) {
            HashSet<Batch> result = new HashSet<Batch>(batches);
            Iterator<Batch> indicator = result.iterator();
            while (indicator.hasNext()) {
                if (!type.equals(indicator.next().getType())) {
                    indicator.remove();
                }
            }
            return result;
        }
        return batches;
    }

    /**
     * The function getBatchesInitialized() returns the batches for a process
     * and takes care that the object is initialized from Hibernate already and
     * will not be bothered if the Hibernate session ends.
     *
     * @return the batches field of the process which is loaded
     */
    public Set<Batch> getBatchesInitialized() {
        if (id != null) {
            Hibernate.initialize(batches);
        }
        return this.batches;
    }

    /**
     * The function setBatches() is intended to be called by Hibernate to inject
     * the batches into the process object. To associate a batch with a process,
     * use {@link Batch#add(Prozess)}.
     *
     * @param batches
     *            set to inject
     */
    public void setBatches(Set<Batch> batches) {
        this.batches = batches;
    }

    public String getAusgabename() {
        return this.ausgabename;
    }

    public void setAusgabename(String ausgabename) {
        this.ausgabename = ausgabename;
    }

    /**
     * The function getEigenschaften() returns the descriptive fields
     * (“properties”) for a process or some Hibernate proxy object which may be
     * uninitialized if its contents have not been accessed yet. However, this
     * function is also called by Hibernate itself when its updating the
     * database and in this case it is absolutely fine to return a proxy object
     * uninitialized.
     *
     * If you want to get the history and be sure it has been loaded, use
     * {@link #getEigenschaftenInitialized()} instead.
     *
     * @return the properties field of the process which may be not yet loaded
     */
    public Set<Prozesseigenschaft> getEigenschaften() {
        return this.eigenschaften;
    }

    /**
     * The function getEigenschaftenInitialized() returns the descriptive fields
     * (“properties”) for a process and takes care that the object is
     * initialized from Hibernate already and will not be bothered if the
     * Hibernate session ends.
     *
     * @return the properties field of the process which is loaded
     */
    public Set<Prozesseigenschaft> getEigenschaftenInitialized() {
        try {
            Hibernate.initialize(this.eigenschaften);
        } catch (HibernateException e) {
        }
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
                rueckgabe = new BenutzerDAO().get(Integer.valueOf(benutzerID));
            } catch (Exception e) {
                Helper.setFehlerMeldung(Helper.getTranslation("userNotFound"), e);
            }
        }
        return rueckgabe;
    }

    public long getMinutenGesperrt() {
        return this.msp.getLockSekunden(this.id) / 60;
    }

    public long getSekundenGesperrt() {
        return this.msp.getLockSekunden(this.id) % 60;
    }

    /*
     * Metadaten- und ImagePfad
     */

    public String getImagesTifDirectory(boolean useFallBack) throws IOException, InterruptedException, SwapException, DAOException {
        SafeFile dir = new SafeFile(getImagesDirectory());

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

        if (tifOrdner.equals("") && useFallBack) {
            String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
            if (!suffix.equals("")) {
                String[] folderList = dir.list();
                for (String folder : folderList) {
                    if (folder.endsWith(suffix)) {
                        tifOrdner = folder;
                        break;
                    }
                }
            }
        }

         if (!tifOrdner.equals("") && useFallBack) {
                String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    SafeFile tif = new SafeFile(tifOrdner);
                    String[] files = tif.list();
                    if (files == null || files.length == 0) {
                        String[] folderList = dir.list();
                        for (String folder : folderList) {
                            if (folder.endsWith(suffix) && !folder.startsWith(DIRECTORY_PREFIX)) {
                                tifOrdner = folder;
                                break;
                            }
                        }
                    }
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
            FilesystemHelper.createDirectory(rueckgabe);
        }
        return rueckgabe;
    }

    /*
     * @return true if the Tif-Image-Directory exists, false if not
     */
    public Boolean getTifDirectoryExists() {
        SafeFile testMe;
        try {
            testMe = new SafeFile(getImagesTifDirectory(true));
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

    public String getImagesOrigDirectory(boolean useFallBack) throws IOException, InterruptedException, SwapException, DAOException {
        if (ConfigMain.getBooleanParameter("useOrigFolder", true)) {
            SafeFile dir = new SafeFile(getImagesDirectory());

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

            if (origOrdner.equals("") && useFallBack) {
                String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    String[] folderList = dir.list();
                    for (String folder : folderList) {
                        if (folder.endsWith(suffix)) {
                            origOrdner = folder;
                            break;
                        }
                    }
                }
            }

            if (!origOrdner.equals("") && useFallBack) {
                String suffix = ConfigMain.getParameter("MetsEditorDefaultSuffix", "");
                if (!suffix.equals("")) {
                    SafeFile tif = new SafeFile(origOrdner);
                    String[] files = tif.list();
                    if (files == null || files.length == 0) {
                        String[] folderList = dir.list();
                        for (String folder : folderList) {
                            if (folder.endsWith(suffix)) {
                                origOrdner = folder;
                                break;
                            }
                        }
                    }
                }
            }

            if (origOrdner.equals("")) {
                origOrdner = DIRECTORY_PREFIX + "_" + this.titel + "_" + DIRECTORY_SUFFIX;
            }
            String rueckgabe = getImagesDirectory() + origOrdner + File.separator;
            if (ConfigMain.getBooleanParameter("createOrigFolderIfNotExists", false) && this.getSortHelperStatus() != "100000000") {
                FilesystemHelper.createDirectory(rueckgabe);
            }
            return rueckgabe;
        } else {
            return getImagesTifDirectory(useFallBack);
        }
    }

    public String getImagesDirectory() throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = getProcessDataDirectory() + "images" + File.separator;
        FilesystemHelper.createDirectory(pfad);
        return pfad;
    }

    public String getSourceDirectory() throws IOException, InterruptedException, SwapException, DAOException {
        SafeFile dir = new SafeFile(getImagesDirectory());
        FilenameFilter filterVerz = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("_" + "source"));
            }
        };
        SafeFile sourceFolder = null;
        String[] verzeichnisse = dir.list(filterVerz);
        if (verzeichnisse == null || verzeichnisse.length == 0) {
            sourceFolder = new SafeFile(dir, titel + "_source");
            if (ConfigMain.getBooleanParameter("createSourceFolder", false)) {
                sourceFolder.mkdir();
            }
        } else {
            sourceFolder = new SafeFile(dir, verzeichnisse[0]);
        }

        return sourceFolder.getAbsolutePath();
    }

    public String getProcessDataDirectory() throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = getProcessDataDirectoryIgnoreSwapping();

        if (isSwappedOutGui()) {
            ProcessSwapInTask pst = new ProcessSwapInTask();
            pst.initialize(this);
            pst.setProgress(1);
            pst.setShowMessages(true);
            pst.run();
            if (pst.getException() != null) {
                if (!new SafeFile(pfad, "images").exists() && !new SafeFile(pfad, "meta.xml").exists()) {
                    throw new SwapException(pst.getException().getMessage());
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
        return getOcrDirectory() + this.titel + "_alto" + File.separator;
    }

    public String getImportDirectory() throws SwapException, DAOException, IOException, InterruptedException {
        return getProcessDataDirectory() + "import" + File.separator;
    }

    public String getProcessDataDirectoryIgnoreSwapping() throws IOException, InterruptedException, SwapException, DAOException {
        String pfad = this.help.getGoobiDataDirectory() + this.id.intValue() + File.separator;
        pfad = pfad.replaceAll(" ", "__");
        FilesystemHelper.createDirectory(pfad);
        return pfad;
    }

    /*
     * Helper
     */

    public Projekt getProjekt() {
        return this.projekt;
    }

    public void setProjekt(Projekt projekt) {
        this.projekt = projekt;
    }

    /**
     * The function getBatchID returns the batches the process is associated
     * with as readable text as read-only property "batchID".
     *
     * @return the batches the process is in
     */
    public String getBatchID() {
        if (batches == null || batches.size() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Batch batch : batches) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(batch.getLabel());
        }
        return result.toString();
    }

    public Regelsatz getRegelsatz() {
        return this.regelsatz;
    }

    public void setRegelsatz(Regelsatz regelsatz) {
        this.regelsatz = regelsatz;
    }

    public int getSchritteSize() {
        try {
            Hibernate.initialize(this.schritte);
        } catch (HibernateException e) {
        }
        if (this.schritte == null) {
            return 0;
        } else {
            return this.schritte.size();
        }
    }

    public List<Schritt> getSchritteList() {
        try {
            Hibernate.initialize(this.schritte);
        } catch (HibernateException e) {
        }
        List<Schritt> temp = new ArrayList<Schritt>();
        if (this.schritte != null) {
            temp.addAll(this.schritte);
        }
        return temp;
    }

    public int getHistorySize() {
        try {
            Hibernate.initialize(this.history);
        } catch (HibernateException e) {
        }
        if (this.history == null) {
            return 0;
        } else {
            return this.history.size();
        }
    }

    public List<HistoryEvent> getHistoryList() {
        try {
            Hibernate.initialize(this.history);
        } catch (HibernateException e) {
        }
        List<HistoryEvent> temp = new ArrayList<HistoryEvent>();
        if (this.history != null) {
            temp.addAll(this.history);
        }
        return temp;
    }

    public int getEigenschaftenSize() {
        try {
            Hibernate.initialize(this.eigenschaften);
        } catch (HibernateException e) {
        }
        if (this.eigenschaften == null) {
            return 0;
        } else {
            return this.eigenschaften.size();
        }
    }

    public List<Prozesseigenschaft> getEigenschaftenList() {
        try {
            Hibernate.initialize(this.eigenschaften);
        } catch (HibernateException e) {
        }
        if (this.eigenschaften == null) {
            return new ArrayList<Prozesseigenschaft>();
        } else {
            return new ArrayList<Prozesseigenschaft>(this.eigenschaften);
        }
    }

    public int getWerkstueckeSize() {
        try {
            Hibernate.initialize(this.werkstuecke);
        } catch (HibernateException e) {
        }
        if (this.werkstuecke == null) {
            return 0;
        } else {
            return this.werkstuecke.size();
        }
    }

    public List<Werkstueck> getWerkstueckeList() {
        try {
            Hibernate.initialize(this.werkstuecke);
        } catch (HibernateException e) {
        }
        if (this.werkstuecke == null) {
            return new ArrayList<Werkstueck>();
        } else {
            return new ArrayList<Werkstueck>(this.werkstuecke);
        }
    }

    public int getVorlagenSize() {
        try {
            Hibernate.initialize(this.vorlagen);
        } catch (HibernateException e) {
        }
        if (this.vorlagen == null) {
            this.vorlagen = new HashSet<Vorlage>();
        }
        return this.vorlagen.size();
    }

    public List<Vorlage> getVorlagenList() {
        try {
            Hibernate.initialize(this.vorlagen);
        } catch (HibernateException e) {
        }
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

    public String getErstellungsdatumAsString() {
        return Helper.getDateAsFormattedString(this.erstellungsdatum);
    }

    /*
     * Auswertung des Fortschritts
     */

    public String getFortschritt() {
        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        Hibernate.initialize(this.schritte);
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
        Hibernate.initialize(this.schritte);
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
        Hibernate.initialize(this.schritte);
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

    public Fileformat readMetadataFile() throws ReadException, IOException, InterruptedException, PreferencesException,
            SwapException, DAOException {
        if (!checkForMetadataFile()) {
            throw new IOException(Helper.getTranslation("metadataFileNotFound") + " " + getMetadataFilePath());
        }
        Hibernate.initialize(getRegelsatz());
        /* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
        String type = MetadatenHelper.getMetaFileType(getMetadataFilePath());
        if (logger.isDebugEnabled()) {
            logger.debug("current meta.xml file type for id " + getId() + ": " + type);
        }
        Fileformat ff = null;
        if (type.equals("metsmods")) {
            ff = new MetsModsImportExport(this.regelsatz.getPreferences());
        } else if (type.equals("mets")) {
            ff = new MetsMods(this.regelsatz.getPreferences());
        } else if (type.equals("xstream")) {
            ff = new XStream(this.regelsatz.getPreferences());
        } else {
            ff = new RDFFile(this.regelsatz.getPreferences());
        }
        try {
            ff.read(getMetadataFilePath());
        } catch (ReadException e) {
            if (e.getMessage().startsWith("Parse error at line -1")) {
                Helper.setFehlerMeldung("metadataCorrupt");
            } else {
                throw e;
            }
        }
        return ff;
    }

    // backup of meta.xml

    private void createBackupFile() throws IOException, InterruptedException, SwapException, DAOException {
        int numberOfBackups = 0;

        if (ConfigMain.getIntParameter("numberOfMetaBackups") != 0) {
            numberOfBackups = ConfigMain.getIntParameter("numberOfMetaBackups");
        }

        if (numberOfBackups != 0) {
            BackupFileRotation bfr = new BackupFileRotation();
            bfr.setNumberOfBackups(numberOfBackups);
            bfr.setFormat("meta.*\\.xml");
            bfr.setProcessDataDirectory(getProcessDataDirectory());
            bfr.performBackup();
        } else {
            logger.warn("No backup configured for meta data files.");
        }
    }

    private boolean checkForMetadataFile() throws IOException, InterruptedException, SwapException, DAOException,
            PreferencesException {
        boolean result = true;
        SafeFile f = new SafeFile(getMetadataFilePath());
        if (!f.exists()) {
            result = false;
        }

        return result;
    }

    private String getTemporaryMetadataFileName(String fileName) {

        SafeFile temporaryFile = new SafeFile(fileName);
        String directoryPath = temporaryFile.getParentFile().getPath();
        String temporaryFileName = TEMPORARY_FILENAME_PREFIX + temporaryFile.getName();

        return directoryPath + File.separator + temporaryFileName;
    }

    private void removePrefixFromRelatedMetsAnchorFilesFor(String temporaryMetadataFilename) throws IOException {
        SafeFile temporaryFile = new SafeFile(temporaryMetadataFilename);
        SafeFile directoryPath = new SafeFile(temporaryFile.getParentFile().getPath());
        for (SafeFile temporaryAnchorFile : directoryPath.listFiles()) {
            String temporaryAnchorFileName = temporaryAnchorFile.toString();
            if (temporaryAnchorFile.isFile()
                    && FilenameUtils.getBaseName(temporaryAnchorFileName).startsWith(TEMPORARY_FILENAME_PREFIX)) {
                String anchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                        temporaryAnchorFileName.replace(TEMPORARY_FILENAME_PREFIX, ""));
                temporaryAnchorFileName = FilenameUtils.concat(FilenameUtils.getFullPath(temporaryAnchorFileName),
                        temporaryAnchorFileName);
                FilesystemHelper.renameFile(temporaryAnchorFileName, anchorFileName);
            }
        }
    }

    public void writeMetadataFile(Fileformat gdzfile) throws IOException, InterruptedException, SwapException, DAOException, WriteException,
            PreferencesException {
        boolean backupCondition;
        boolean writeResult;
        SafeFile temporaryMetadataFile;

        Fileformat ff;
        String metadataFileName;
        String temporaryMetadataFileName;

        Hibernate.initialize(getRegelsatz());
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
        // createBackupFile();
        metadataFileName = getMetadataFilePath();
        temporaryMetadataFileName = getTemporaryMetadataFileName(metadataFileName);

        ff.setDigitalDocument(gdzfile.getDigitalDocument());
        // ff.write(getMetadataFilePath());
        writeResult = ff.write(temporaryMetadataFileName);
        temporaryMetadataFile = new SafeFile(temporaryMetadataFileName);
        backupCondition = writeResult && temporaryMetadataFile.exists() && (temporaryMetadataFile.length() > 0);
        if (backupCondition) {
            createBackupFile();
            FilesystemHelper.renameFile(temporaryMetadataFileName, metadataFileName);
            removePrefixFromRelatedMetsAnchorFilesFor(temporaryMetadataFileName);
        }
    }


    public void writeMetadataAsTemplateFile(Fileformat inFile) throws IOException, InterruptedException, SwapException, DAOException, WriteException,
            PreferencesException {
        inFile.write(getTemplateFilePath());
    }

    public Fileformat readMetadataAsTemplateFile() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException,
            DAOException {
        Hibernate.initialize(getRegelsatz());
        if (new SafeFile(getTemplateFilePath()).exists()) {
            Fileformat ff = null;
            String type = MetadatenHelper.getMetaFileType(getTemplateFilePath());
            if(logger.isDebugEnabled()){
                logger.debug("current template.xml file type: " + type);
            }
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
        if (getSchritteList().size() == 0) {
            return true;
        }
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

    public String downloadDocket() {

        if(logger.isDebugEnabled()){
            logger.debug("generate docket for process " + this.id);
        }
        String rootpath = ConfigMain.getParameter("xsltFolder");
        SafeFile xsltfile = new SafeFile(rootpath, "docket.xsl");
        if (docket != null) {
            xsltfile = new SafeFile(rootpath, docket.getFile());
            if (!xsltfile.exists()) {
                Helper.setFehlerMeldung("docketMissing");
                return "";
            }
        }
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
                facesContext.responseComplete();
            } catch (Exception e) {
                Helper.setFehlerMeldung("Exception while exporting run note.", e.getMessage());
                response.reset();
            }

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

        } catch (NoSuchMethodException e) {

        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }

        try {
            String folder = this.getImagesTifDirectory(false);
            folder = folder.substring(0, folder.lastIndexOf("_"));
            folder = folder + "_" + methodName;
            if (new SafeFile(folder).exists()) {
                return folder;
            }

        } catch (SwapException e) {

        } catch (DAOException e) {

        } catch (IOException e) {

        } catch (InterruptedException e) {

        }

        return null;
    }

    public Docket getDocket() {
        return docket;
    }

    public void setDocket(Docket docket) {
        this.docket = docket;
    }

    @XmlElement(name = "collection")
    public List<String> getPossibleDigitalCollections() throws JDOMException, IOException {
        return DigitalCollections.possibleDigitalCollectionsForProcess(this);
    }

    /**
     * The addMessageToWikiField() method is a helper method which composes the
     * new wiki field using a StringBuilder. The message is encoded using HTML
     * entities to prevent certain characters from playing merry havoc when the
     * message box shall be rendered in a browser later.
     *
     * @param message
     *            the message to append
     */
    public void addToWikiField(String message) {
        StringBuilder composer = new StringBuilder();
        if (wikifield != null && wikifield.length() > 0) {
            composer.append(wikifield);
            composer.append("\r\n");
        }
        composer.append("<p>");
        composer.append(StringEscapeUtils.escapeHtml(message));
        composer.append("</p>");
        wikifield = composer.toString();
        return;
    }

    /**
     * The method addToWikiField() adds a message with a given level to the wiki
     * field of the process. Four level strings will be recognized and result in
     * different colors:
     *
     * <dl>
     * <dt><code>debug</code></dt>
     * <dd>gray</dd>
     * <dt><code>error</code></dt>
     * <dd>red</dd>
     * <dt><code>user</code></dt>
     * <dd>green</dd>
     * <dt><code>warn</code></dt>
     * <dd>orange</dd>
     * <dt><i>any other value</i></dt>
     * <dd>blue</dd>
     * <dt>
     *
     * @param level
     *            message colour, one of: "debug", "error", "info", "user" or
     *            "warn"; any other value defaults to "info"
     * @param message
     *            message text
     */
    public void addToWikiField(String level, String message) {
        wikifield = WikiFieldHelper.getWikiMessage(this, wikifield, level, message);
    }

    /**
     * The method addToWikiField() adds a message signed by the given user to
     * the wiki field of the process.
     *
     * @param user
     *            user to sign the message with
     * @param message
     *            message to print
     */
    public void addToWikiField(Benutzer user, String message) {
        String text = message + " (" + user.getNachVorname() + ")";
        addToWikiField("user", text);
    }

    /**
     * The method createProcessDirs() starts creation of directories configured by parameter processDirs within goobi_config.properties
     * @throws InterruptedException
     * @throws IOException
     * @throws DAOException
     * @throws SwapException
     */
    public void createProcessDirs() throws SwapException, DAOException, IOException, InterruptedException {

        String[] processDirs = ConfigMain.getStringArrayParameter("processDirs");

        for(String processDir : processDirs) {

            FilesystemHelper.createDirectory(FilenameUtils.concat(this.getProcessDataDirectory(), processDir.replace("(processtitle)", this.getTitel())));
        }

    }

    /**
     * The function getDigitalDocument() returns the digital act of this
     * process.
     *
     * @return the digital act of this process
     * @throws PreferencesException
     *             if the no node corresponding to the file format is available
     *             in the rule set configured
     * @throws ReadException
     *             if the meta data file cannot be read
     * @throws SwapException
     *             if an error occurs while the process is swapped back in
     * @throws DAOException
     *             if an error occurs while saving the fact that the process has
     *             been swapped back in to the database
     * @throws IOException
     *             if creating the process directory or reading the meta data
     *             file fails
     * @throws InterruptedException
     *             if the current thread is interrupted by another thread while
     *             it is waiting for the shell script to create the directory to
     *             finish
     */
    public DigitalDocument getDigitalDocument() throws PreferencesException, ReadException, SwapException,
            DAOException, IOException, InterruptedException {
        return readMetadataFile().getDigitalDocument();
    }

    /**
     * Filter for correction / solution messages.
     *
     * @param lpe List of process properties
     * @return List of filtered correction / solution messages
     */
    protected List<Prozesseigenschaft> filterForCorrectionSolutionMessages(List<Prozesseigenschaft> lpe) {
        ArrayList<Prozesseigenschaft> filteredList = new ArrayList<Prozesseigenschaft>();
        List<String> listOfTranslations = new ArrayList<String>();
        String propertyTitle = "";

        listOfTranslations.add("Korrektur notwendig");
        listOfTranslations.add("Korrektur durchgefuehrt");
        listOfTranslations.add(Helper.getTranslation("Korrektur notwendig"));
        listOfTranslations.add(Helper.getTranslation("Korrektur durchgefuehrt"));

        if ((lpe == null) || (lpe.size() == 0)) {
            return filteredList;
        }

        // filtering for correction and solution messages
        for (Prozesseigenschaft pe : lpe) {
            propertyTitle = pe.getTitel();
            if (listOfTranslations.contains(propertyTitle)) {
                filteredList.add(pe);
            }
        }
        return filteredList;
    }

    /**
     * Filter and sort after creation date list of process properties for correction and solution messages.
     *
     * @return list of Prozesseigenschaft objects
     */
    public List<Prozesseigenschaft> getSortedCorrectionSolutionMessages() {
        List<Prozesseigenschaft> filteredList;
        List<Prozesseigenschaft> lpe = this.getEigenschaftenList();

        if (lpe.isEmpty()) {
            return new ArrayList<Prozesseigenschaft>();
        }

        filteredList = filterForCorrectionSolutionMessages(lpe);

        // sorting after creation date
        Collections.sort(filteredList, new Comparator<Prozesseigenschaft>() {
            @Override
            public int compare(Prozesseigenschaft o1, Prozesseigenschaft o2) {
                Date o1Date = o1.getCreationDate();
                Date o2Date = o2.getCreationDate();
                if (o1Date == null) {
                    o1Date = new Date();
                }
                if (o2Date == null) {
                    o2Date = new Date();
                }
                return o1Date.compareTo(o2Date);
            }
        });

        return new ArrayList<Prozesseigenschaft>(filteredList);
    }

}
