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

package org.goobi.production.cli.helper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.importer.ImportObject;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.ProzessDAO;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

public class CopyProcess extends ProzesskopieForm {

    private static final Logger logger = Logger.getLogger(ProzesskopieForm.class);
    private Fileformat myRdf;
    private String opacSuchfeld = "12";
    private String opacSuchbegriff;
    private String opacKatalog;
    private Prozess prozessVorlage = new Prozess();
    private Prozess prozessKopie = new Prozess();
    /* komplexe Anlage von Vorgängen anhand der xml-Konfiguration */
    private boolean useOpac;
    private boolean useTemplates;
    public String metadataFile;

    private HashMap<String, Boolean> standardFields;
    private List<AdditionalField> additionalFields;
    private List<String> digitalCollections;
    private String tifHeader_imagedescription = "";
    private String tifHeader_documentname = "";

    private String naviFirstPage;
    private Integer auswahl;
    private String docType;
    // TODO: check use of atstsl. Why is it never modified?
    private static final String atstsl = "";
    private List<String> possibleDigitalCollection;

    public final static String DIRECTORY_SUFFIX = "_".concat(ConfigMain.getParameter("DIRECTORY_SUFFIX", "tif"));

    /* =============================================================== */

    public String prepare(ImportObject io) {
        if (this.prozessVorlage.getContainsUnreachableSteps()) {
            return "";
        }

        clearValues();
        Prefs myPrefs = this.prozessVorlage.getRegelsatz().getPreferences();
        try {
            this.myRdf = new MetsMods(myPrefs);
            this.myRdf.read(this.metadataFile);
        } catch (PreferencesException e) {
            logger.error(e);
        } catch (ReadException e) {
            logger.error(e);
        }
        ;
        this.prozessKopie = new Prozess();
        this.prozessKopie.setTitel("");
        this.prozessKopie.setIstTemplate(false);
        this.prozessKopie.setInAuswahllisteAnzeigen(false);
        this.prozessKopie.setProjekt(this.prozessVorlage.getProjekt());
        this.prozessKopie.setRegelsatz(this.prozessVorlage.getRegelsatz());
        this.prozessKopie.setDocket(this.prozessVorlage.getDocket());
        this.digitalCollections = new ArrayList<String>();

        /*
         * -------------------------------- Kopie der Prozessvorlage anlegen --------------------------------
         */
        BeanHelper.SchritteKopieren(this.prozessVorlage, this.prozessKopie);
        BeanHelper.ScanvorlagenKopieren(this.prozessVorlage, this.prozessKopie);
        BeanHelper.WerkstueckeKopieren(this.prozessVorlage, this.prozessKopie);
        BeanHelper.EigenschaftenKopieren(this.prozessVorlage, this.prozessKopie);


        return this.naviFirstPage;
    }

    @Override
    public String prepare() {
        if (this.prozessVorlage.getContainsUnreachableSteps()) {
            for (Schritt s : this.prozessVorlage.getSchritteList()) {
                if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0) {
                    Helper.setFehlerMeldung("Kein Benutzer festgelegt für: ", s.getTitel());
                }
            }
            return "";
        }

        clearValues();
        Prefs myPrefs = this.prozessVorlage.getRegelsatz().getPreferences();
        try {
            this.myRdf = new MetsMods(myPrefs);
            this.myRdf.read(this.metadataFile);
        } catch (PreferencesException e) {
            logger.error(e);
        } catch (ReadException e) {
            logger.error(e);
        }
        ;
        this.prozessKopie = new Prozess();
        this.prozessKopie.setTitel("");
        this.prozessKopie.setIstTemplate(false);
        this.prozessKopie.setInAuswahllisteAnzeigen(false);
        this.prozessKopie.setProjekt(this.prozessVorlage.getProjekt());
        this.prozessKopie.setRegelsatz(this.prozessVorlage.getRegelsatz());
        this.digitalCollections = new ArrayList<String>();

        /*
         * -------------------------------- Kopie der Prozessvorlage anlegen --------------------------------
         */
        BeanHelper.SchritteKopieren(this.prozessVorlage, this.prozessKopie);
        BeanHelper.ScanvorlagenKopieren(this.prozessVorlage, this.prozessKopie);
        BeanHelper.WerkstueckeKopieren(this.prozessVorlage, this.prozessKopie);
        BeanHelper.EigenschaftenKopieren(this.prozessVorlage, this.prozessKopie);

        initializePossibleDigitalCollections();

        return this.naviFirstPage;
    }

    /* =============================================================== */

    private void readProjectConfigs() {
        /*--------------------------------
         * projektabhängig die richtigen Felder in der Gui anzeigen
         * --------------------------------*/
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        this.docType = cp.getParamString("createNewProcess.defaultdoctype", ConfigOpac.getAllDoctypes().get(0)
                .getTitle());
        this.useOpac = cp.getParamBoolean("createNewProcess.opac[@use]");
        this.useTemplates = cp.getParamBoolean("createNewProcess.templates[@use]");
        this.naviFirstPage = "ProzessverwaltungKopie1";
        if (this.opacKatalog.equals("")) {
            this.opacKatalog = cp.getParamString("createNewProcess.opac.catalogue");
        }

        /*
         * -------------------------------- die auszublendenden Standard-Felder ermitteln --------------------------------
         */
        for (String t : cp.getParamList("createNewProcess.itemlist.hide")) {
            this.standardFields.put(t, false);
        }

        /*
         * -------------------------------- die einzublendenen (zusätzlichen) Eigenschaften ermitteln --------------------------------
         */
        int count = cp.getParamList("createNewProcess.itemlist.item").size();
        for (int i = 0; i < count; i++) {
            AdditionalField fa = new AdditionalField(this);
            fa.setFrom(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@from]"));
            fa.setTitel(cp.getParamString("createNewProcess.itemlist.item(" + i + ")"));
            fa.setRequired(cp.getParamBoolean("createNewProcess.itemlist.item(" + i + ")[@required]"));
            fa.setIsdoctype(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@isdoctype]"));
            fa.setIsnotdoctype(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@isnotdoctype]"));

            // attributes added 30.3.09
            String test = (cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@initStart]"));
            fa.setInitStart(test);

            fa.setInitEnd(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@initEnd]"));

            /*
             * -------------------------------- Bindung an ein Metadatum eines Docstructs --------------------------------
             */
            if (cp.getParamBoolean("createNewProcess.itemlist.item(" + i + ")[@ughbinding]")) {
                fa.setUghbinding(true);
                fa.setDocstruct(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@docstruct]"));
                fa.setMetadata(cp.getParamString("createNewProcess.itemlist.item(" + i + ")[@metadata]"));
            }

            /*
             * -------------------------------- prüfen, ob das aktuelle Item eine Auswahlliste werden soll --------------------------------
             */
            int selectItemCount = cp.getParamList("createNewProcess.itemlist.item(" + i + ").select").size();
            /* Children durchlaufen und SelectItems erzeugen */
            if (selectItemCount > 0) {
                fa.setSelectList(new ArrayList<SelectItem>());
            }
            for (int j = 0; j < selectItemCount; j++) {
                String svalue = cp.getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")[@label]");
                String sid = cp.getParamString("createNewProcess.itemlist.item(" + i + ").select(" + j + ")");
                fa.getSelectList().add(new SelectItem(sid, svalue, null));
            }
            this.additionalFields.add(fa);
        }
    }

    public String OpacAuswerten(ImportObject io) {
        clearValues();
        readProjectConfigs();
        try {
            Prefs myPrefs = this.prozessVorlage.getRegelsatz().getPreferences();
            /* den Opac abfragen und ein RDF draus bauen lassen */
            this.myRdf = new MetsMods(myPrefs);
            this.myRdf.read(this.metadataFile);

            this.docType = this.myRdf.getDigitalDocument().getLogicalDocStruct().getType().getName();

            fillFieldsFromMetadataFile(this.myRdf);

            fillFieldsFromConfig();

        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
            e.printStackTrace();
        }
        return "";
    }

    /**
     * OpacAnfrage
     */
    @Override
    public String OpacAuswerten() {
        clearValues();
        readProjectConfigs();
        try {
            Prefs myPrefs = this.prozessVorlage.getRegelsatz().getPreferences();
            /* den Opac abfragen und ein RDF draus bauen lassen */
            this.myRdf = new MetsMods(myPrefs);
            this.myRdf.read(this.metadataFile);

            this.docType = this.myRdf.getDigitalDocument().getLogicalDocStruct().getType().getName();

            fillFieldsFromMetadataFile(this.myRdf);

            fillFieldsFromConfig();

        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
            e.printStackTrace();
        }
        return "";
    }

    /* =============================================================== */

    /**
     * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei füllen
     *
     * @throws PreferencesException
     */
    private void fillFieldsFromMetadataFile(Fileformat myRdf) throws PreferencesException {
        if (myRdf != null) {

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype()) {
                    /* welches Docstruct */

                    DocStruct myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
                    if (field.getDocstruct().equals("firstchild")) {
                        try {
                            myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                        }
                    }
                    if (field.getDocstruct().equals("boundbook")) {
                        myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        if (field.getMetadata().equals("ListOfCreators")) {
                            /* bei Autoren die Namen zusammenstellen */
                            String myautoren = "";
                            if (myTempStruct.getAllPersons() != null) {
                                for (Person p : myTempStruct.getAllPersons()) {
                                    myautoren += p.getLastname();
                                    if (StringUtils.isNotBlank(p.getFirstname())) {
                                        myautoren += ", " + p.getFirstname();
                                    }
                                    myautoren += "; ";
                                }
                                if (myautoren.endsWith("; ")) {
                                    myautoren = myautoren.substring(0, myautoren.length() - 2);
                                }
                            }
                            field.setWert(myautoren);
                        } else {
                            /* bei normalen Feldern die Inhalte auswerten */
                            MetadataType mdt = UghHelper.getMetadataType(this.prozessKopie.getRegelsatz()
                                    .getPreferences(), field.getMetadata());
                            Metadata md = UghHelper.getMetadata(myTempStruct, mdt);
                            if (md != null) {
                                field.setWert(md.getValue());
                            }
                        }
                    } catch (UghHelperException e) {
                        Helper.setFehlerMeldung(e.getMessage(), "");
                    }
                } // end if ughbinding
            }// end for
        } // end if myrdf==null
    }

    private void fillFieldsFromConfig() {
        for (AdditionalField field : this.additionalFields) {
            if (!field.isUghbinding() && field.getShowDependingOnDoctype()) {
                if (field.getSelectList() != null && field.getSelectList().size() > 0) {
                    field.setWert((String) field.getSelectList().get(0).getValue());
                }

            }
        }
        CalcTiffheader();

    }

    /**
     * alle Konfigurationseigenschaften und Felder zurücksetzen ================================================================
     */
    private void clearValues() {
        if (this.opacKatalog == null) {
            this.opacKatalog = "";
        }
        this.standardFields = new HashMap<String, Boolean>();
        this.standardFields.put("collections", true);
        this.standardFields.put("doctype", true);
        this.standardFields.put("regelsatz", true);
        this.additionalFields = new ArrayList<AdditionalField>();
        this.tifHeader_documentname = "";
        this.tifHeader_imagedescription = "";
    }

    /**
     * Auswahl des Prozesses auswerten
     *
     * @throws DAOException
     * @throws NamingException
     * @throws SQLException
     *             ============================================================ == ==
     */
    @Override
    public String TemplateAuswahlAuswerten() throws DAOException {
        /* den ausgewählten Prozess laden */
        Prozess tempProzess = new ProzessDAO().get(this.auswahl);
        if (tempProzess.getWerkstueckeSize() > 0) {
            /* erstes Werkstück durchlaufen */
            Werkstueck werk = tempProzess.getWerkstueckeList().get(0);
            for (Werkstueckeigenschaft eig : werk.getEigenschaften()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitel().equals(eig.getTitel())) {
                        field.setWert(eig.getWert());
                    }
                }
            }
        }

        if (tempProzess.getVorlagenSize() > 0) {
            /* erste Vorlage durchlaufen */
            Vorlage vor = tempProzess.getVorlagenList().get(0);
            for (Vorlageeigenschaft eig : vor.getEigenschaften()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitel().equals(eig.getTitel())) {
                        field.setWert(eig.getWert());
                    }
                }
            }
        }

        try {
            this.myRdf = tempProzess.readMetadataAsTemplateFile();
        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen der Template-Metadaten ", e);
        }

        /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
        try {
            DocStruct colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
            removeCollections(colStruct);
            colStruct = colStruct.getAllChildren().get(0);
            removeCollections(colStruct);
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Fehler beim Anlegen des Vorgangs", e);
            logger.error("Fehler beim Anlegen des Vorgangs", e);
        } catch (RuntimeException e) {
            /*
             * das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden
             */
        }

        return "";
    }

    /**
     * Validierung der Eingaben
     *
     * @return sind Fehler bei den Eingaben vorhanden? ================================================================
     */
    private boolean isContentValid() {
        /*
         * -------------------------------- Vorbedingungen prüfen --------------------------------
         */
        boolean valide = true;

        /*
         * -------------------------------- grundsätzlich den Vorgangstitel prüfen --------------------------------
         */
        /* kein Titel */
        if (this.prozessKopie.getTitel() == null || this.prozessKopie.getTitel().equals("")) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
        }

        String validateRegEx = ConfigMain.getParameter("validateProzessTitelRegex", "[\\w-]*");
        if (!this.prozessKopie.getTitel().matches(validateRegEx)) {
            valide = false;
            Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
        }

        /* prüfen, ob der Prozesstitel schon verwendet wurde */
        if (this.prozessKopie.getTitel() != null) {
            long anzahl = 0;
            try {
                anzahl = new ProzessDAO().count("from Prozess where titel='" + this.prozessKopie.getTitel() + "'");
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Fehler beim Einlesen der Vorgaenge", e.getMessage());
                valide = false;
            }
            if (anzahl > 0) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UngueltigeDaten: ") + Helper.getTranslation("ProcessCreationErrorTitleAllreadyInUse"));
            }
        }

        /*
         * -------------------------------- Prüfung der standard-Eingaben, die angegeben werden müssen --------------------------------
         */
        /* keine Collektion ausgewählt */
        if (this.standardFields.get("collections") && getDigitalCollections().size() == 0) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorNoCollection"));
        }

        /*
         * -------------------------------- Prüfung der additional-Eingaben, die angegeben werden müssen --------------------------------
         */
        for (AdditionalField field : this.additionalFields) {
            if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype() && (StringUtils.isBlank(field.getWert()))) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + field.getTitel() + " " + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));
            }
        }
        return valide;
    }

    /* =============================================================== */

    @Override
    public String GoToSeite1() {
        return this.naviFirstPage;
    }

    /* =============================================================== */

    @Override
    public String GoToSeite2() {
        if (!isContentValid()) {
            return this.naviFirstPage;
        } else {
            return "ProzessverwaltungKopie2";
        }
    }

    public boolean testTitle() {
        boolean valide = true;

        if (ConfigMain.getBooleanParameter("MassImportUniqueTitle", true)) {
            /*
             * -------------------------------- grundsätzlich den Vorgangstitel prüfen --------------------------------
             */
            /* kein Titel */
            if (this.prozessKopie.getTitel() == null || this.prozessKopie.getTitel().equals("")) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
            }

            String validateRegEx = ConfigMain.getParameter("validateProzessTitelRegex", "[\\w-]*");
            if (!this.prozessKopie.getTitel().matches(validateRegEx)) {
                valide = false;
                Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
            }

            /* prüfen, ob der Prozesstitel schon verwendet wurde */
            if (this.prozessKopie.getTitel() != null) {
                long anzahl = 0;
                try {
                    anzahl = new ProzessDAO().count("from Prozess where titel='" + this.prozessKopie.getTitel() + "'");
                } catch (DAOException e) {
                    Helper.setFehlerMeldung("Fehler beim Einlesen der Vorgaenge", e.getMessage());
                    valide = false;
                }
                if (anzahl > 0) {
                    valide = false;
                    Helper.setFehlerMeldung(Helper.getTranslation("UngueltigeDaten:") + Helper.getTranslation("ProcessCreationErrorTitleAllreadyInUse"));
                }
            }
        }
        return valide;
    }

    /**
     * Anlegen des Prozesses und Speichern der Metadaten ================================================================
     *
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     */

    public Prozess NeuenProzessAnlegen2() throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException,
            WriteException {
        Helper.getHibernateSession().evict(this.prozessKopie);

        this.prozessKopie.setId(null);

        addProperties(null);


        for (Schritt step : this.prozessKopie.getSchritteList()) {
            /*
             * -------------------------------- always save date and user for each step --------------------------------
             */
            step.setBearbeitungszeitpunkt(this.prozessKopie.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);
            LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
            if (loginForm != null) {
                step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());
            }

            /*
             * -------------------------------- only if its done, set edit start and end date --------------------------------
             */
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(this.prozessKopie.getErstellungsdatum());
                // this concerns steps, which are set as done right on creation
                // bearbeitungsbeginn is set to creation timestamp of process
                // because the creation of it is basically begin of work
                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }

        }

        try {
            ProzessDAO dao = new ProzessDAO();
            dao.save(this.prozessKopie);
            dao.refresh(this.prozessKopie);
        } catch (DAOException e) {
            e.printStackTrace();
            logger.error("error on save: ", e);
            return this.prozessKopie;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage stattfand, dann jetzt eine anlegen
         */
        if (this.myRdf == null) {
            createNewFileformat();
        }

        this.prozessKopie.writeMetadataFile(this.myRdf);

        // Adding process to history
        if (!HistoryAnalyserJob.updateHistoryForProcess(this.prozessKopie)) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                new ProzessDAO().save(this.prozessKopie);
            } catch (DAOException e) {
                e.printStackTrace();
                logger.error("error on save: ", e);
                return this.prozessKopie;
            }
        }

        this.prozessKopie.readMetadataFile();

        /* damit die Sortierung stimmt nochmal einlesen */
        Helper.getHibernateSession().refresh(this.prozessKopie);
        return this.prozessKopie;

    }

    public Prozess createProcess(ImportObject io) throws ReadException, IOException, InterruptedException, PreferencesException, SwapException,
            DAOException, WriteException {
        Helper.getHibernateSession().evict(this.prozessKopie);

        this.prozessKopie.setId(null);
        addProperties(io);


        for (Schritt step : this.prozessKopie.getSchritteList()) {
            /*
             * -------------------------------- always save date and user for each step --------------------------------
             */
            step.setBearbeitungszeitpunkt(this.prozessKopie.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);
            LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
            if (loginForm != null) {
                step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());
            }

            /*
             * -------------------------------- only if its done, set edit start and end date --------------------------------
             */
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(this.prozessKopie.getErstellungsdatum());
                // this concerns steps, which are set as done right on creation
                // bearbeitungsbeginn is set to creation timestamp of process
                // because the creation of it is basically begin of work
                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }

        }

        if (!io.getBatches().isEmpty()) {
            this.prozessKopie.getBatchesInitialized().addAll(io.getBatches());
        }
        try {
            ProzessDAO dao = new ProzessDAO();
            dao.save(this.prozessKopie);
            dao.refresh(this.prozessKopie);
        } catch (DAOException e) {
            e.printStackTrace();
            logger.error("error on save: ", e);
            return this.prozessKopie;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage stattfand, dann jetzt eine anlegen
         */
        if (this.myRdf == null) {
            createNewFileformat();
        }

        File f = new File(this.prozessKopie.getProcessDataDirectoryIgnoreSwapping());
        if (!f.exists() && !f.mkdir()) {
            Helper.setFehlerMeldung("Could not create process directory");
            logger.error("Could not create process directory");
            return this.prozessKopie;
        }

        this.prozessKopie.writeMetadataFile(this.myRdf);

        // }

        // Adding process to history
        if (!HistoryAnalyserJob.updateHistoryForProcess(this.prozessKopie)) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                new ProzessDAO().save(this.prozessKopie);
            } catch (DAOException e) {
                e.printStackTrace();
                logger.error("error on save: ", e);
                return this.prozessKopie;
            }
        }

        this.prozessKopie.readMetadataFile();

        /* damit die Sortierung stimmt nochmal einlesen */
        Helper.getHibernateSession().refresh(this.prozessKopie);
        return this.prozessKopie;

    }

    /**
     * alle Kollektionen eines übergebenen DocStructs entfernen ================================================================
     */
    private void removeCollections(DocStruct colStruct) {
        try {
            MetadataType mdt = UghHelper.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(),
                    "singleDigCollection");
            ArrayList<Metadata> myCollections = new ArrayList<Metadata>(colStruct.getAllMetadataByType(mdt));
            if (myCollections.size() > 0) {
                for (Metadata md : myCollections) {
                    colStruct.removeMetadata(md);
                }
            }
        } catch (UghHelperException e) {
            Helper.setFehlerMeldung(e.getMessage(), "");
            e.printStackTrace();
        } catch (DocStructHasNoTypeException e) {
            Helper.setFehlerMeldung(e.getMessage(), "");
            e.printStackTrace();
        }
    }

    /* =============================================================== */

    @Override
    public void createNewFileformat() {

        Prefs myPrefs = this.prozessKopie.getRegelsatz().getPreferences();

        Fileformat ff;
        try {
            ff = new MetsMods(myPrefs);
            ff.read(this.metadataFile);
        } catch (PreferencesException e) {
            logger.error(e);
        } catch (ReadException e) {
            logger.error(e);
        }

    }

    private void addProperties(ImportObject io) {
        /*
         * -------------------------------- Vorlageneigenschaften initialisieren --------------------------------
         */

        Vorlage vor;
        if (this.prozessKopie.getVorlagenSize() > 0) {
            vor = this.prozessKopie.getVorlagenList().get(0);
        } else {
            vor = new Vorlage();
            vor.setProzess(this.prozessKopie);
            Set<Vorlage> vorlagen = new HashSet<Vorlage>();
            vorlagen.add(vor);
            this.prozessKopie.setVorlagen(vorlagen);
        }

        /*
         * -------------------------------- Werkstückeigenschaften initialisieren --------------------------------
         */
        Werkstueck werk;
        if (this.prozessKopie.getWerkstueckeSize() > 0) {
            werk = this.prozessKopie.getWerkstueckeList().get(0);
        } else {
            werk = new Werkstueck();
            werk.setProzess(this.prozessKopie);
            Set<Werkstueck> werkstuecke = new HashSet<Werkstueck>();
            werkstuecke.add(werk);
            this.prozessKopie.setWerkstuecke(werkstuecke);
        }

        /*
         * -------------------------------- jetzt alle zusätzlichen Felder durchlaufen und die Werte hinzufügen --------------------------------
         */
        if (io == null) {
            for (AdditionalField field : this.additionalFields) {
                if (field.getShowDependingOnDoctype()) {
                    if (field.getFrom().equals("werk")) {
                        BeanHelper.EigenschaftHinzufuegen(werk, field.getTitel(), field.getWert());
                    }
                    if (field.getFrom().equals("vorlage")) {
                        BeanHelper.EigenschaftHinzufuegen(vor, field.getTitel(), field.getWert());
                    }
                    if (field.getFrom().equals("prozess")) {
                        BeanHelper.EigenschaftHinzufuegen(this.prozessKopie, field.getTitel(), field.getWert());
                    }
                }
            }
            /* Doctype */
            BeanHelper.EigenschaftHinzufuegen(werk, "DocType", this.docType);
            /* Tiffheader */
            BeanHelper.EigenschaftHinzufuegen(werk, "TifHeaderImagedescription", this.tifHeader_imagedescription);
            BeanHelper.EigenschaftHinzufuegen(werk, "TifHeaderDocumentname", this.tifHeader_documentname);
        } else {
            BeanHelper.EigenschaftHinzufuegen(werk, "DocType", this.docType);
            /* Tiffheader */
            BeanHelper.EigenschaftHinzufuegen(werk, "TifHeaderImagedescription", this.tifHeader_imagedescription);
            BeanHelper.EigenschaftHinzufuegen(werk, "TifHeaderDocumentname", this.tifHeader_documentname);

            for (Prozesseigenschaft pe : io.getProcessProperties()) {
                addProperty(this.prozessKopie, pe);
            }
            for (Werkstueckeigenschaft we : io.getWorkProperties()) {
                addProperty(werk, we);
            }

            for (Vorlageeigenschaft ve : io.getTemplateProperties()) {
                addProperty(vor, ve);
            }
            BeanHelper.EigenschaftHinzufuegen(prozessKopie, "Template", prozessVorlage.getTitel());
            BeanHelper.EigenschaftHinzufuegen(prozessKopie, "TemplateID", String.valueOf(prozessVorlage.getId()));
        }
    }

    @Override
    public String getDocType() {
        return this.docType;
    }

    @Override
    public void setDocType(String docType) {
        this.docType = docType;
    }

    @Override
    public Prozess getProzessVorlage() {
        return this.prozessVorlage;
    }

    @Override
    public void setProzessVorlage(Prozess prozessVorlage) {
        this.prozessVorlage = prozessVorlage;
    }

    @Override
    public Integer getAuswahl() {
        return this.auswahl;
    }

    @Override
    public void setAuswahl(Integer auswahl) {
        this.auswahl = auswahl;
    }

    @Override
    public List<AdditionalField> getAdditionalFields() {
        return this.additionalFields;
    }

    /*
     * this is needed for GUI, render multiple select only if this is false if this is true use the only choice
     *
     * @author Wulf
     */
    @Override
    public boolean isSingleChoiceCollection() {
        return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);

    }

    /*
     * this is needed for GUI, render multiple select only if this is false if isSingleChoiceCollection is true use this choice
     *
     * @author Wulf
     */
    @Override
    public String getDigitalCollectionIfSingleChoice() {
        List<String> pdc = getPossibleDigitalCollections();
        if (pdc.size() == 1) {
            return pdc.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getPossibleDigitalCollections() {
        return this.possibleDigitalCollection;
    }


    @SuppressWarnings("unchecked")
    private void initializePossibleDigitalCollections() {
        this.possibleDigitalCollection = new ArrayList<String>();
        ArrayList<String> defaultCollections = new ArrayList<String>();
        String filename = new Helper().getGoobiConfigDirectory() + "goobi_digitalCollections.xml";
        if (!(new File(filename).exists())) {
            Helper.setFehlerMeldung("File not found: ", filename);
            return;
        }
        this.digitalCollections = new ArrayList<String>();
        try {
            /* Datei einlesen und Root ermitteln */
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new File(filename));
            Element root = doc.getRootElement();
            /* alle Projekte durchlaufen */
            List<Element> projekte = root.getChildren();
            for (Iterator<Element> iter = projekte.iterator(); iter.hasNext();) {
                Element projekt = iter.next();

                // collect default collections
                if (projekt.getName().equals("default")) {
                    List<Element> myCols = projekt.getChildren("DigitalCollection");
                    for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
                        Element col = it2.next();

                        if (col.getAttribute("default") != null && col.getAttributeValue("default").equalsIgnoreCase("true")) {
                            digitalCollections.add(col.getText());
                        }

                        defaultCollections.add(col.getText());
                    }
                } else {
                    // run through the projects
                    List<Element> projektnamen = projekt.getChildren("name");
                    for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
                        Element projektname = iterator.next();
                        // all all collections to list
                        if (projektname.getText().equalsIgnoreCase(this.prozessKopie.getProjekt().getTitel())) {
                            List<Element> myCols = projekt.getChildren("DigitalCollection");
                            for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
                                Element col = it2.next();

                                if (col.getAttribute("default") != null && col.getAttributeValue("default").equalsIgnoreCase("true")) {
                                    digitalCollections.add(col.getText());
                                }

                                this.possibleDigitalCollection.add(col.getText());
                            }
                        }
                    }
                }
            }
        } catch (JDOMException e1) {
            logger.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        } catch (IOException e1) {
            logger.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        }

        if (this.possibleDigitalCollection.size() == 0) {
            this.possibleDigitalCollection = defaultCollections;
        }

        // if only one collection is possible take it directly

        if (isSingleChoiceCollection()) {
            this.digitalCollections.add(getDigitalCollectionIfSingleChoice());
        }
    }


    @Override
    public List<String> getAllOpacCatalogues() {
        return ConfigOpac.getAllCatalogueTitles();
    }

    @Override
    public List<ConfigOpacDoctype> getAllDoctypes() {
        return ConfigOpac.getAllDoctypes();
    }

    /*
     * changed, so that on first request list gets set if there is only one choice
     */
    @Override
    public List<String> getDigitalCollections() {
        return this.digitalCollections;
    }

    @Override
    public void setDigitalCollections(List<String> digitalCollections) {
        this.digitalCollections = digitalCollections;
    }

    @Override
    public HashMap<String, Boolean> getStandardFields() {
        return this.standardFields;
    }

    @Override
    public boolean isUseOpac() {
        return this.useOpac;
    }

    @Override
    public boolean isUseTemplates() {
        return this.useTemplates;
    }

    @Override
    public String getTifHeader_documentname() {
        return this.tifHeader_documentname;
    }

    @Override
    public void setTifHeader_documentname(String tifHeader_documentname) {
        this.tifHeader_documentname = tifHeader_documentname;
    }

    @Override
    public String getTifHeader_imagedescription() {
        return this.tifHeader_imagedescription;
    }

    @Override
    public void setTifHeader_imagedescription(String tifHeader_imagedescription) {
        this.tifHeader_imagedescription = tifHeader_imagedescription;
    }

    @Override
    public Prozess getProzessKopie() {
        return this.prozessKopie;
    }

    @Override
    public void setProzessKopie(Prozess prozessKopie) {
        this.prozessKopie = prozessKopie;
    }

    @Override
    public String getOpacSuchfeld() {
        return this.opacSuchfeld;
    }

    @Override
    public void setOpacSuchfeld(String opacSuchfeld) {
        this.opacSuchfeld = opacSuchfeld;
    }

    @Override
    public String getOpacKatalog() {
        return this.opacKatalog;
    }

    @Override
    public void setOpacKatalog(String opacKatalog) {
        this.opacKatalog = opacKatalog;
    }

    @Override
    public String getOpacSuchbegriff() {
        return this.opacSuchbegriff;
    }

    @Override
    public void setOpacSuchbegriff(String opacSuchbegriff) {
        this.opacSuchbegriff = opacSuchbegriff;
    }

    /*
     * ##################################################### ##################################################### ## ## Helper ##
     * ##################################################### ####################################################
     */

    /**
     * Prozesstitel und andere Details generieren ================================================================
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void CalcProzesstitel() {
        String newTitle = "";
        String titeldefinition = "";
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        int count = cp.getParamList("createNewProcess.itemlist.processtitle").size();
        for (int i = 0; i < count; i++) {
            String titel = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")");
            String isdoctype = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isdoctype]");
            String isnotdoctype = cp.getParamString("createNewProcess.itemlist.processtitle(" + i + ")[@isnotdoctype]");

            if (titel == null) {
                titel = "";
            }
            if (isdoctype == null) {
                isdoctype = "";
            }
            if (isnotdoctype == null) {
                isnotdoctype = "";
            }

            /* wenn nix angegeben wurde, dann anzeigen */
            if (isdoctype.equals("") && isnotdoctype.equals("")) {
                titeldefinition = titel;
                break;
            }

            /* wenn beides angegeben wurde */
            if (!isdoctype.equals("") && !isnotdoctype.equals("") && StringUtils.containsIgnoreCase(isdoctype, this.docType) && !StringUtils.containsIgnoreCase(isnotdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }

            /* wenn nur pflicht angegeben wurde */
            if (isnotdoctype.equals("") && StringUtils.containsIgnoreCase(isdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }
            /* wenn nur "darf nicht" angegeben wurde */
            if (isdoctype.equals("") && !StringUtils.containsIgnoreCase(isnotdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }
        }

        StringTokenizer tokenizer = new StringTokenizer(titeldefinition, "+");
        /* jetzt den Bandtitel parsen */
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            // System.out.println(myString);
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'")) {
                newTitle += myString.substring(1, myString.length() - 1);
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (Iterator it2 = this.additionalFields.iterator(); it2.hasNext();) {
                    AdditionalField myField = (AdditionalField) it2.next();

                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL")) && myField.getShowDependingOnDoctype()
                            && (myField.getWert() == null || myField.getWert().equals(""))) {
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype() && myField.getWert() != null) {
                        newTitle += calcProcessTitleCheck(myField.getTitel(), myField.getWert());
                    }
                }
            }
        }

        if (newTitle.endsWith("_")) {
            newTitle = newTitle.substring(0, newTitle.length() - 1);
        }
        this.prozessKopie.setTitel(newTitle);
        CalcTiffheader();
    }

    /* =============================================================== */

    private String calcProcessTitleCheck(String inFeldName, String inFeldWert) {
        String rueckgabe = inFeldWert;

        /*
         * -------------------------------- Bandnummer --------------------------------
         */
        if (inFeldName.equals("Bandnummer")) {
            try {
                int bandint = Integer.parseInt(inFeldWert);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
                rueckgabe = df.format(bandint);
            } catch (NumberFormatException e) {
                Helper.setFehlerMeldung("Ungültige Daten: ", "Bandnummer ist keine gültige Zahl");
            }
            if (rueckgabe != null && rueckgabe.length() < 4) {
                rueckgabe = "0000".substring(rueckgabe.length()) + rueckgabe;
            }
        }

        return rueckgabe;
    }

    /* =============================================================== */

    @Override
    public void CalcTiffheader() {
        String tif_definition = "";
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        tif_definition = cp.getParamString("tifheader." + this.docType.toLowerCase(), "blabla");

        /*
         * -------------------------------- evtuelle Ersetzungen --------------------------------
         */
        tif_definition = tif_definition.replaceAll("\\[\\[", "<");
        tif_definition = tif_definition.replaceAll("\\]\\]", ">");

        /*
         * -------------------------------- Documentname ist im allgemeinen = Prozesstitel --------------------------------
         */
        this.tifHeader_documentname = this.prozessKopie.getTitel();
        this.tifHeader_imagedescription = "";
        /*
         * -------------------------------- Imagedescription --------------------------------
         */
        StringTokenizer tokenizer = new StringTokenizer(tif_definition, "+");
        /* jetzt den Tiffheader parsen */
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2) {
                this.tifHeader_imagedescription += myString.substring(1, myString.length() - 1);
            } else if (myString.equals("$Doctype")) {

                this.tifHeader_imagedescription += this.docType;
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (Iterator<AdditionalField> it2 = this.additionalFields.iterator(); it2.hasNext();) {
                    AdditionalField myField = it2.next();

                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL")) && myField.getShowDependingOnDoctype()
                            && (myField.getWert() == null || myField.getWert().equals(""))) {
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype() && myField.getWert() != null) {
                        this.tifHeader_imagedescription += calcProcessTitleCheck(myField.getTitel(), myField.getWert());
                    }
                }
            }
            // }
        }
    }

    private void addProperty(Vorlage inVorlage, Vorlageeigenschaft property) {
        if (property.getContainer() == 0) {
            for (Vorlageeigenschaft ve : inVorlage.getEigenschaftenList()) {
                if (ve.getTitel().equals(property.getTitel()) && ve.getContainer() > 0) {
                    ve.setWert(property.getWert());
                    return;
                }
            }
        }
        Vorlageeigenschaft eig = new Vorlageeigenschaft();
        eig.setTitel(property.getTitel());
        eig.setWert(property.getWert());
        eig.setAuswahl(property.getAuswahl());
        eig.setContainer(property.getContainer());
        eig.setType(property.getType());
        eig.setVorlage(inVorlage);
        Set<Vorlageeigenschaft> eigenschaften = inVorlage.getEigenschaften();
        if (eigenschaften != null) {
            eigenschaften.add(eig);
        }
    }

    private void addProperty(Prozess inProcess, Prozesseigenschaft property) {
        if (property.getContainer() == 0) {
            for (Prozesseigenschaft pe : inProcess.getEigenschaftenList()) {
                if (pe.getTitel().equals(property.getTitel()) && pe.getContainer() > 0) {
                    pe.setWert(property.getWert());
                    return;
                }
            }
        }
        Prozesseigenschaft eig = new Prozesseigenschaft();
        eig.setTitel(property.getTitel());
        eig.setWert(property.getWert());
        eig.setAuswahl(property.getAuswahl());
        eig.setContainer(property.getContainer());
        eig.setType(property.getType());
        eig.setProzess(inProcess);
        Set<Prozesseigenschaft> eigenschaften = inProcess.getEigenschaftenInitialized();
        if (eigenschaften != null) {
            eigenschaften.add(eig);
        }
    }

    private void addProperty(Werkstueck inWerk, Werkstueckeigenschaft property) {
        if (property.getContainer() == 0) {
            for (Werkstueckeigenschaft we : inWerk.getEigenschaftenList()) {
                if (we.getTitel().equals(property.getTitel()) && we.getContainer() > 0) {
                    we.setWert(property.getWert());
                    return;
                }
            }
        }
        Werkstueckeigenschaft eig = new Werkstueckeigenschaft();
        eig.setTitel(property.getTitel());
        eig.setWert(property.getWert());
        eig.setAuswahl(property.getAuswahl());
        eig.setContainer(property.getContainer());
        eig.setType(property.getType());
        eig.setWerkstueck(inWerk);
        Set<Werkstueckeigenschaft> eigenschaften = inWerk.getEigenschaften();
        if (eigenschaften != null) {
            eigenschaften.add(eig);
        }
    }
}
