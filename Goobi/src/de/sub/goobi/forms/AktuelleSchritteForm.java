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

package de.sub.goobi.forms;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Batch.Type;
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.HistoryEvent;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.helper.BatchStepHelper;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritteWithoutHibernate;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.PropertyListObject;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.metadaten.MetadatenVerifizierung;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.persistence.SchrittDAO;
import de.sub.goobi.persistence.apache.StepManager;
import de.sub.goobi.persistence.apache.StepObject;
import de.unigoettingen.goobi.module.api.exception.GoobiException;

public class AktuelleSchritteForm extends BasisForm {
    private static final long serialVersionUID = 5841566727939692509L;
    private static final Logger logger = Logger.getLogger(AktuelleSchritteForm.class);
    private Prozess myProzess = new Prozess();
    private Schritt mySchritt = new Schritt();
    private Integer myProblemID;
    private Integer mySolutionID;
    private String problemMessage;
    private String solutionMessage;

    private String modusBearbeiten = "";
    private final WebDav myDav = new WebDav();
    private int gesamtAnzahlImages = 0;
    private int pageAnzahlImages = 0;
    private boolean nurOffeneSchritte = false;
    private boolean nurEigeneSchritte = false;
    private boolean showAutomaticTasks = false;
    private boolean hideCorrectionTasks = false;
    private HashMap<String, Boolean> anzeigeAnpassen;
    private IEvaluableFilter myFilteredDataSource;
    private String scriptPath;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String addToWikiField = "";
    private static String DONEDIRECTORYNAME = "fertig/";
    private final ProzessDAO pdao;
    private Boolean flagWait = false;
    private final ReentrantLock flagWaitLock = new ReentrantLock();
    private BatchStepHelper batchHelper;
    private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
    private Integer container;
    private List<ProcessProperty> processPropertyList;
    private ProcessProperty processProperty;

    public AktuelleSchritteForm() {
        this.anzeigeAnpassen = new HashMap<String, Boolean>();
        this.anzeigeAnpassen.put("lockings", false);
        this.anzeigeAnpassen.put("selectionBoxes", false);
        this.anzeigeAnpassen.put("processId", false);
        this.anzeigeAnpassen.put("modules", false);
        this.anzeigeAnpassen.put("batchId", false);
        this.pdao = new ProzessDAO();
        /*
         * --------------------- Vorgangsdatum generell anzeigen? -------------------
         */
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login != null && login.getMyBenutzer() != null) {
            this.anzeigeAnpassen.put("processDate", login.getMyBenutzer().isConfVorgangsdatumAnzeigen());
        } else {
            this.anzeigeAnpassen.put("processDate", false);
        }
        DONEDIRECTORYNAME = ConfigMain.getParameter("doneDirectoryName", "fertig/");
    }

    /*
     * Filter
     */

    /**
     * Anzeige der Schritte
     */
    public String FilterAlleStart() {
        try {
            this.myFilteredDataSource = new UserDefinedStepFilter(true);

            this.myFilteredDataSource.getObservable().addObserver(new Helper().createObserver());
            ((UserDefinedStepFilter) this.myFilteredDataSource).setFilterModes(this.nurOffeneSchritte, this.nurEigeneSchritte);
            this.myFilteredDataSource.setFilter(this.filter);

            Criteria crit = this.myFilteredDataSource.getCriteria();
            if (!this.showAutomaticTasks) {
                crit.add(Restrictions.eq("typAutomatisch", false));
            }
            if (hideCorrectionTasks) {
                crit.add(Restrictions.not(Restrictions.eq("prioritaet", 10)));
            }

            sortList(crit);
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("error on reading database", he.getMessage());
            return "";
        }
        return "AktuelleSchritteAlle";
    }

    private void sortList(Criteria inCrit) {
        inCrit.addOrder(Order.desc("prioritaet"));

        Order order = Order.asc("proc.titel");
        if (this.sortierung.equals("schrittAsc")) {
            order = Order.asc("titel");
        }
        if (this.sortierung.equals("schrittDesc")) {
            order = Order.desc("titel");
        }
        if (this.sortierung.equals("prozessAsc")) {
            order = Order.asc("proc.titel");
        }
        if (this.sortierung.equals("prozessDesc")) {
            order = Order.desc("proc.titel");
        }
        if (this.sortierung.equals("batchAsc")) {
            order = Order.asc("proc.batchID");
        }
        if (this.sortierung.equals("batchDesc")) {
            order = Order.desc("proc.batchID");
        }
        if (this.sortierung.equals("prozessdateAsc")) {
            order = Order.asc("proc.erstellungsdatum");
        }
        if (this.sortierung.equals("prozessdateDesc")) {
            order = Order.desc("proc.erstellungsdatum");
        }
        if (this.sortierung.equals("projektAsc")) {
            order = Order.asc("proj.titel");
        }
        if (this.sortierung.equals("projektDesc")) {
            order = Order.desc("proj.titel");
        }
        if (this.sortierung.equals("modulesAsc")) {
            order = Order.asc("typModulName");
        }
        if (this.sortierung.equals("modulesDesc")) {
            order = Order.desc("typModulName");
        }
        if (this.sortierung.equals("statusAsc")) {
            order = Order.asc("bearbeitungsstatus");
        }
        if (this.sortierung.equals("statusDesc")) {
            order = Order.desc("bearbeitungsstatus");
        }

        inCrit.addOrder(order);
    }

    /*
     * Bearbeitung des Schritts übernehmen oder abschliessen
     */

    public String SchrittDurchBenutzerUebernehmen() {
        this.flagWaitLock.lock();
        try {

            if (!this.flagWait) {
                this.flagWait = true;

                // Helper.getHibernateSession().clear();
                Helper.getHibernateSession().refresh(this.mySchritt);

                if (this.mySchritt.getBearbeitungsstatusEnum() != StepStatus.OPEN) {
                    Helper.setFehlerMeldung("stepInWorkError");
                    this.flagWait = false;
                    return "";
                }

                else {
                    this.mySchritt.setBearbeitungsstatusEnum(StepStatus.INWORK);
                    this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
                    mySchritt.setBearbeitungszeitpunkt(new Date());
                    Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                    if (ben != null) {
                        mySchritt.setBearbeitungsbenutzer(ben);
                    }
                    if (this.mySchritt.getBearbeitungsbeginn() == null) {
                        Date myDate = new Date();
                        this.mySchritt.setBearbeitungsbeginn(myDate);
                    }
                    this.mySchritt
                            .getProzess()
                            .getHistoryInitialized()
                            .add(new HistoryEvent(this.mySchritt.getBearbeitungsbeginn(), this.mySchritt.getReihenfolge().doubleValue(),
                                    this.mySchritt.getTitel(), HistoryEventType.stepInWork, this.mySchritt.getProzess()));
                    try {
                        /*
                         * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
                         */
                        this.pdao.save(this.mySchritt.getProzess());
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                        logger.error("step couldn't get saved", e);
                    } finally {
                        this.flagWait = false;
                    }
                    /*
                     * wenn es ein Image-Schritt ist, dann gleich die Images ins Home
                     */

                    if (this.mySchritt.isTypImagesLesen() || this.mySchritt.isTypImagesSchreiben()) {
                        DownloadToHome();
                    }
                }
            } else {
                Helper.setFehlerMeldung("stepInWorkError");
                return "";
            }
            this.flagWait = false;
        } finally {
            this.flagWaitLock.unlock();
        }
        return "AktuelleSchritteBearbeiten";
    }

    public String EditStep() {

        Helper.getHibernateSession().refresh(mySchritt);

        return "AktuelleSchritteBearbeiten";
    }

    @SuppressWarnings("unchecked")
    public String TakeOverBatch() {
        // find all steps with same batch id and step status
        List<Schritt> currentStepsOfBatch = new ArrayList<Schritt>();

        String steptitle = this.mySchritt.getTitel();
        Set<Batch> batches = mySchritt.getProzess().getBatchesByType(Type.LOGISTIC);
        if (batches.size() > 1) {
            Helper.setFehlerMeldung("multipleBatchesAssigned");
            return "";
        }
        if (batches.size() != 0) {
            Integer batchNumber = batches.iterator().next().getId();
            // only steps with same title
            Session session = Helper.getHibernateSession();
            Criteria crit = session.createCriteria(Schritt.class);
            crit.add(Restrictions.eq("titel", steptitle));
            // only steps with same batchid
            crit.createCriteria("prozess", "proc");
            crit.createCriteria("proc.batches", "bat");
            crit.add(Restrictions.eq("bat.id", batchNumber));
            crit.add(Restrictions.eq("batchStep", true));

            currentStepsOfBatch = crit.list();
        } else {
            return SchrittDurchBenutzerUebernehmen();
        }
        // if only one step is assigned for this batch, use the single

        // Helper.setMeldung("found " + currentStepsOfBatch.size() + " elements in batch");
        if (currentStepsOfBatch.size() == 0) {
            return "";
        }
        if (currentStepsOfBatch.size() == 1) {
            return SchrittDurchBenutzerUebernehmen();
        }

        for (Schritt s : currentStepsOfBatch) {

            if (s.getBearbeitungsstatusEnum().equals(StepStatus.OPEN)) {
                s.setBearbeitungsstatusEnum(StepStatus.INWORK);
                s.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                s.setBearbeitungszeitpunkt(new Date());
                Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    s.setBearbeitungsbenutzer(ben);
                }
                if (s.getBearbeitungsbeginn() == null) {
                    Date myDate = new Date();
                    s.setBearbeitungsbeginn(myDate);
                }
                s.getProzess()
                        .getHistoryInitialized()
                        .add(new HistoryEvent(s.getBearbeitungsbeginn(), s.getReihenfolge().doubleValue(), s.getTitel(), HistoryEventType.stepInWork,
                                s.getProzess()));

                if (s.isTypImagesLesen() || s.isTypImagesSchreiben()) {
                    try {
                        new File(s.getProzess().getImagesOrigDirectory(false));
                    } catch (Exception e1) {

                    }
                    s.setBearbeitungszeitpunkt(new Date());

                    if (ben != null) {
                        s.setBearbeitungsbenutzer(ben);
                    }
                    this.myDav.DownloadToHome(s.getProzess(), s.getId().intValue(), !s.isTypImagesSchreiben());

                }
            }

            try {
                this.pdao.save(s.getProzess());

            } catch (DAOException e) {
                Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                logger.error("step couldn't get saved", e);
            }
        }

        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch));
        return "BatchesEdit";
    }

    @SuppressWarnings("unchecked")
    public String BatchesEdit() {
        // find all steps with same batch id and step status
        List<Schritt> currentStepsOfBatch = new ArrayList<Schritt>();

        String steptitle = this.mySchritt.getTitel();
        Set<Batch> batches = mySchritt.getProzess().getBatchesByType(Type.LOGISTIC);
        if (batches.size() > 1) {
            Helper.setFehlerMeldung("multipleBatchesAssigned");
            return "";
        }
        if (batches.size() != 0) {
            Integer batchNumber = batches.iterator().next().getId();
            // only steps with same title

            Session session = Helper.getHibernateSession();
            Criteria crit = session.createCriteria(Schritt.class);
            crit.add(Restrictions.eq("titel", steptitle));
            // only steps with same batchid
            crit.createCriteria("prozess", "proc");
            crit.createCriteria("proc.batches", "bat");
            crit.add(Restrictions.eq("bat.id", batchNumber));
            crit.add(Restrictions.eq("batchStep", true));

            currentStepsOfBatch = crit.list();
        } else {
            return "AktuelleSchritteBearbeiten";
        }
        // if only one step is assigned for this batch, use the single

        // Helper.setMeldung("found " + currentStepsOfBatch.size() + " elements in batch");

        if (currentStepsOfBatch.size() == 1) {
            return "AktuelleSchritteBearbeiten";
        }
        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch));
        return "BatchesEdit";
    }

    @Deprecated
    public void saveProperties() {
    }

    public String SchrittDurchBenutzerZurueckgeben() {
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.OPEN);
        // mySchritt.setBearbeitungsbenutzer(null);
        // if we have a correction-step here then never remove startdate
        if (this.mySchritt.isCorrectionStep()) {
            this.mySchritt.setBearbeitungsbeginn(null);
        }
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }

        try {
            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            this.pdao.save(this.mySchritt.getProzess());
        } catch (DAOException e) {
        }
        // calcHomeImages();
        return "AktuelleSchritteAlle";
    }

    public String SchrittDurchBenutzerAbschliessen() {

        if (mySchritt.getValidationPlugin() != null && mySchritt.getValidationPlugin().length() > 0) {
            IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation, mySchritt.getValidationPlugin());
            if (ivp != null) {
                ivp.setStep(mySchritt);
                if (!ivp.validate()) {
                    return "";
                }
            } else {
                Helper.setFehlerMeldung("ErrorLoadingValidationPlugin");
            }
        }

        /*
         * -------------------------------- if step allows writing of images, then count all images here --------------------------------
         */
        if (this.mySchritt.isTypImagesSchreiben()) {
            try {
                // this.mySchritt.getProzess().setSortHelperImages(
                // FileUtils.getNumberOfFiles(new File(this.mySchritt.getProzess().getImagesOrigDirectory())));
                HistoryAnalyserJob.updateHistory(this.mySchritt.getProzess());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while calculation of storage and images", e);
            }
        }

        /*
         * -------------------------------- wenn das Resultat des Arbeitsschrittes zunÃ¤chst verifiziert werden soll, dann ggf. das Abschliessen
         * abbrechen --------------------------------
         */
        if (this.mySchritt.isTypBeimAbschliessenVerifizieren()) {
            /* Metadatenvalidierung */
            if (this.mySchritt.isTypMetadaten() && ConfigMain.getBooleanParameter("useMetadatenvalidierung")) {
                MetadatenVerifizierung mv = new MetadatenVerifizierung();
                mv.setAutoSave(true);
                if (!mv.validate(this.mySchritt.getProzess())) {
                    return "";
                }
            }

            /* Imagevalidierung */
            if (this.mySchritt.isTypImagesSchreiben()) {
                MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
                try {
                    if (!mih.checkIfImagesValid(this.mySchritt.getProzess().getTitel(), this.mySchritt.getProzess().getImagesOrigDirectory(false))) {
                        return "";
                    }
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Error on image validation: ", e);
                }
            }
        }

        for (ProcessProperty processProperty : processPropertyList) {
            if (AccessCondition.WRITEREQUIRED.equals(processProperty.getCurrentStepAccessCondition())
                    && StringUtils.isBlank(processProperty.getValue())
                    && !getOccupiedProperties().contains(processProperty.getName())) {
                Helper.setFehlerMeldung(Helper.getTranslation("Eigenschaft") + " " + processProperty.getName() + " "
                        + Helper.getTranslation("requiredValue"));
                return "";
            } else if (!processProperty.isValid()) {
                List<String> parameter = new ArrayList<String>();
                parameter.add(processProperty.getName());
                Helper.setFehlerMeldung(Helper.getTranslation("PropertyValidation", parameter));
                return "";
            }
        }

        /*
         * wenn das Ergebnis der Verifizierung ok ist, dann weiter, ansonsten schon vorher draussen
         */
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        StepObject so = StepManager.getStepById(this.mySchritt.getId());
        new HelperSchritteWithoutHibernate().CloseStepObjectAutomatic(so, true);
        // new HelperSchritte().SchrittAbschliessen(this.mySchritt, true);
        return FilterAlleStart();
    }

    public String SperrungAufheben() {
        MetadatenSperrung.UnlockProcess(this.mySchritt.getProzess().getId());
        return "";
    }

    /*
     * Korrekturmeldung an vorherige Schritte
     */

    @SuppressWarnings("unchecked")
    public List<Schritt> getPreviousStepsForProblemReporting() {
        List<Schritt> alleVorherigenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class)
                .add(Restrictions.lt("reihenfolge", this.mySchritt.getReihenfolge())).addOrder(Order.desc("reihenfolge")).createCriteria("prozess")
                .add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
        return alleVorherigenSchritte;
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    @SuppressWarnings("unchecked")
    public String ReportProblem() {
        Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben == null) {
            Helper.setFehlerMeldung("userNotFound");
            return "";
        }
        if(logger.isDebugEnabled()){
            logger.debug("mySchritt.ID: " + this.mySchritt.getId().intValue());
            logger.debug("Korrekturschritt.ID: " + this.myProblemID.intValue());
        }
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        Date myDate = new Date();
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        mySchritt.setBearbeitungsbenutzer(ben);
        this.mySchritt.setBearbeitungsbeginn(null);

        try {
            SchrittDAO dao = new SchrittDAO();
            Schritt temp = dao.get(this.myProblemID);
            temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
            temp.setCorrectionStep();
            temp.setBearbeitungsende(null);

            Prozesseigenschaft pe = new Prozesseigenschaft();
            pe.setTitel(Helper.getTranslation("Korrektur notwendig"));
            pe.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] " + this.problemMessage);
            pe.setType(PropertyType.messageError);
            pe.setProzess(this.mySchritt.getProzess());
            this.mySchritt.getProzess().getEigenschaften().add(pe);

            String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitel() + ": " + this.problemMessage + " ("
                    + ben.getNachVorname() + ")";
            this.mySchritt.getProzess().setWikifield(
                    WikiFieldHelper.getWikiMessage(this.mySchritt.getProzess(), this.mySchritt.getProzess().getWikifield(), "error", message));
            dao.save(temp);
            this.mySchritt
                    .getProzess()
                    .getHistoryInitialized()
                    .add(new HistoryEvent(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError, temp.getProzess()));
            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
             */
            List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class)
                    .add(Restrictions.le("reihenfolge", this.mySchritt.getReihenfolge())).add(Restrictions.gt("reihenfolge", temp.getReihenfolge()))
                    .addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
            for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
                Schritt step = iter.next();
                step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
                step.setCorrectionStep();
                step.setBearbeitungsende(null);
                dao.save(step);
            }

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            this.pdao.save(this.mySchritt.getProzess());
        } catch (DAOException e) {
        }

        this.problemMessage = "";
        this.myProblemID = 0;
        return FilterAlleStart();
    }

    /*
     *  Problem-behoben-Meldung an nachfolgende Schritte
     */

    @SuppressWarnings("unchecked")
    public List<Schritt> getNextStepsForProblemSolution() {
        List<Schritt> alleNachfolgendenSchritte = Helper.getHibernateSession().createCriteria(Schritt.class)
                .add(Restrictions.gt("reihenfolge", this.mySchritt.getReihenfolge())).add(Restrictions.eq("prioritaet", 10))
                .addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
        return alleNachfolgendenSchritte;
    }

    public int getSizeOfNextStepsForProblemSolution() {
        return getNextStepsForProblemSolution().size();
    }

    @SuppressWarnings("unchecked")
    public String SolveProblem() {
        Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben == null) {
            Helper.setFehlerMeldung("userNotFound");
            return "";
        }
        Date now = new Date();
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.DONE);
        this.mySchritt.setBearbeitungsende(now);
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        mySchritt.setBearbeitungsbenutzer(ben);

        try {
            SchrittDAO dao = new SchrittDAO();
            Schritt temp = dao.get(this.mySolutionID);
            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
             */
            List<Schritt> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Schritt.class)
                    .add(Restrictions.ge("reihenfolge", this.mySchritt.getReihenfolge())).add(Restrictions.le("reihenfolge", temp.getReihenfolge()))
                    .addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
            for (Iterator<Schritt> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
                Schritt step = iter.next();
                step.setBearbeitungsstatusEnum(StepStatus.DONE);
                step.setBearbeitungsende(now);
                step.setPrioritaet(Integer.valueOf(0));
                if (step.getId().intValue() == temp.getId().intValue()) {
                    step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                    step.setCorrectionStep();
                    step.setBearbeitungsende(null);
                    // step.setBearbeitungsbeginn(null);
                    step.setBearbeitungszeitpunkt(now);
                }
                mySchritt.setBearbeitungszeitpunkt(new Date());
                mySchritt.setBearbeitungsbenutzer(ben);
                dao.save(step);
            }

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + this.solutionMessage + " ("
                    + ben.getNachVorname() + ")";
            this.mySchritt.getProzess().setWikifield(
                    WikiFieldHelper.getWikiMessage(this.mySchritt.getProzess(), this.mySchritt.getProzess().getWikifield(), "info", message));

            Prozesseigenschaft pe = new Prozesseigenschaft();
            pe.setTitel(Helper.getTranslation("Korrektur durchgefuehrt"));
            pe.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] "
                    + Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + this.solutionMessage);
            pe.setType(PropertyType.messageImportant);
            pe.setProzess(this.mySchritt.getProzess());
            this.mySchritt.getProzess().getEigenschaften().add(pe);

            this.pdao.save(this.mySchritt.getProzess());
        } catch (DAOException e) {
        }

        this.solutionMessage = "";
        this.mySolutionID = 0;
        return FilterAlleStart();
    }

    /*
     * Upload und Download der Images
     */

    public String UploadFromHome() {
        mySchritt.setBearbeitungszeitpunkt(new Date());
        Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        Helper.setMeldung(null, "Removed directory from user home", this.mySchritt.getProzess().getTitel());
        return "";
    }

    public String DownloadToHome() {
        try {
            new File(this.mySchritt.getProzess().getImagesOrigDirectory(false));
        } catch (Exception e1) {

        }
        mySchritt.setBearbeitungszeitpunkt(new Date());
        Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.myDav.DownloadToHome(this.mySchritt.getProzess(), this.mySchritt.getId().intValue(), !this.mySchritt.isTypImagesSchreiben());

        return "";
    }

    @SuppressWarnings("unchecked")
    public String UploadFromHomeAlle() throws NumberFormatException, DAOException {
        List<String> fertigListe = this.myDav.UploadFromHomeAlle(DONEDIRECTORYNAME);
        List<String> geprueft = new ArrayList<String>();
        /*
         * -------------------------------- die hochgeladenen Prozess-IDs durchlaufen und auf abgeschlossen setzen --------------------------------
         */
        if (fertigListe != null && fertigListe.size() > 0 && this.nurOffeneSchritte) {
            this.nurOffeneSchritte = false;
            FilterAlleStart();
        }
        for (Iterator<String> iter = fertigListe.iterator(); iter.hasNext();) {
            String element = iter.next();
            String myID = element.substring(element.indexOf("[") + 1, element.indexOf("]")).trim();

            for (Iterator<Schritt> iterator = this.page.getCompleteList().iterator(); iterator.hasNext();) {
                Schritt step = iterator.next();
                /*
                 * nur wenn der Schritt bereits im Bearbeitungsmodus ist, abschliessen
                 */
                if (step.getProzess().getId().intValue() == Integer.parseInt(myID) && step.getBearbeitungsstatusEnum() == StepStatus.INWORK) {
                    this.mySchritt = step;
                    if (!SchrittDurchBenutzerAbschliessen().isEmpty()) {
                        geprueft.add(element);
                    }
                    this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                }
            }
        }

        this.myDav.removeFromHomeAlle(geprueft, DONEDIRECTORYNAME);
        Helper.setMeldung(null, "removed " + geprueft.size() + " directories from user home:", DONEDIRECTORYNAME);
        return "";
    }

    @SuppressWarnings("unchecked")
    public String DownloadToHomePage() {

        for (Iterator<Schritt> iter = this.page.getListReload().iterator(); iter.hasNext();) {
            Schritt step = iter.next();
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
                step.setBearbeitungsstatusEnum(StepStatus.INWORK);
                step.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                mySchritt.setBearbeitungszeitpunkt(new Date());
                Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    mySchritt.setBearbeitungsbenutzer(ben);
                }
                step.setBearbeitungsbeginn(new Date());
                Prozess proz = step.getProzess();
                try {
                    this.pdao.save(proz);
                } catch (DAOException e) {
                    Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitel());
                }
                this.myDav.DownloadToHome(proz, step.getId().intValue(), false);
            }
        }
        // calcHomeImages();
        Helper.setMeldung(null, "Created directies in user home", "");
        return "";
    }

    @SuppressWarnings("unchecked")
    public String DownloadToHomeHits() {

        for (Iterator<Schritt> iter = this.page.getCompleteList().iterator(); iter.hasNext();) {
            Schritt step = iter.next();
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
                step.setBearbeitungsstatusEnum(StepStatus.INWORK);
                step.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                mySchritt.setBearbeitungszeitpunkt(new Date());
                Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    mySchritt.setBearbeitungsbenutzer(ben);
                }
                step.setBearbeitungsbeginn(new Date());
                Prozess proz = step.getProzess();
                try {
                    this.pdao.save(proz);
                } catch (DAOException e) {
                    Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitel());
                }
                this.myDav.DownloadToHome(proz, step.getId().intValue(), false);
            }
        }
        // calcHomeImages();
        Helper.setMeldung(null, "Created directories in user home", "");
        return "";
    }

    public String getScriptPath() {

        return this.scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public void executeScript() {
        StepObject so = StepManager.getStepById(this.mySchritt.getId());
        new HelperSchritteWithoutHibernate().executeScriptForStepObject(so, this.scriptPath, false);

    }

    /**
     * call module for this step ================================================================
     *
     * @throws IOException
     */
    @Deprecated
    public void executeModule() {
        Helper.setMeldung("call module");
        ModuleServerForm msf = (ModuleServerForm) Helper.getManagedBeanValue("#{ModuleServerForm}");
        String url = null;
        try {
            url = msf.startShortSession(mySchritt);
            Helper.setMeldung(url);
        } catch (GoobiException e) {
            Helper.setFehlerMeldung("GoobiException: " + e.getMessage());
            return;
        } catch (XmlRpcException e) {
            Helper.setMeldung("XmlRpcException: " + e.getMessage());
            return;
        }
        Helper.setMeldung("module called");
        if (url.length() > 0) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (!facesContext.getResponseComplete()) {
                HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
                try {
                    response.sendRedirect(url);
                } catch (IOException e) {
                    Helper.setFehlerMeldung("IOException: " + e.getMessage());
                }
                facesContext.responseComplete();
            }
        }
    }

    @Deprecated
    public int getHomeBaende() {
        return 0;
    }

    public int getAllImages() {
        return this.gesamtAnzahlImages;
    }

    public int getPageImages() {
        return this.pageAnzahlImages;
    }

    @SuppressWarnings("unchecked")
    public void calcHomeImages() {
        this.gesamtAnzahlImages = 0;
        this.pageAnzahlImages = 0;
        Benutzer aktuellerBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (aktuellerBenutzer != null && aktuellerBenutzer.isMitMassendownload()) {
            for (Iterator<Schritt> iter = this.page.getCompleteList().iterator(); iter.hasNext();) {
                Schritt step = iter.next();
                try {
                    if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
                        // gesamtAnzahlImages +=
                        // myDav.getAnzahlImages(step.getProzess().getImagesOrigDirectory());
                        this.gesamtAnzahlImages += FileUtils.getNumberOfFiles(step.getProzess().getImagesOrigDirectory(false));
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }

    /*
     *  Getter und Setter
     */

    public Prozess getMyProzess() {
        return this.myProzess;
    }

    public void setMyProzess(Prozess myProzess) {
        this.myProzess = myProzess;
    }

    public Schritt getMySchritt() {
        try {
            schrittPerParameterLaden();
        } catch (NumberFormatException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        }
        return this.mySchritt;
    }

    public void setMySchritt(Schritt mySchritt) {
        this.modusBearbeiten = "";
        this.mySchritt = mySchritt;
        loadProcessProperties();
    }

    public void setStep(Schritt step) {
        this.mySchritt = step;
        loadProcessProperties();
    }

    public Schritt getStep() {
        return this.mySchritt;
    }

    public String getModusBearbeiten() {
        return this.modusBearbeiten;
    }

    public void setModusBearbeiten(String modusBearbeiten) {
        this.modusBearbeiten = modusBearbeiten;
    }

    public Integer getMyProblemID() {
        return this.myProblemID;
    }

    public void setMyProblemID(Integer myProblemID) {
        this.myProblemID = myProblemID;
    }

    public Integer getMySolutionID() {
        return this.mySolutionID;
    }

    public void setMySolutionID(Integer mySolutionID) {
        this.mySolutionID = mySolutionID;
    }

    public String getProblemMessage() {
        return this.problemMessage;
    }

    public void setProblemMessage(String problemMessage) {
        this.problemMessage = problemMessage;
    }

    public String getSolutionMessage() {
        return this.solutionMessage;
    }

    public void setSolutionMessage(String solutionMessage) {
        this.solutionMessage = solutionMessage;
    }

    /*
     * Parameter per Get Ã¼bergeben bekommen und entsprechen den passenden Schritt laden
     */

    /**
     * prüfen, ob per Parameter vielleicht zunÃ¤chst ein anderer geladen werden soll
     *
     * @throws DAOException
     *             , NumberFormatException
     */
    private void schrittPerParameterLaden() throws DAOException, NumberFormatException {
        String param = Helper.getRequestParameter("myid");
        if (param != null && !param.equals("")) {
            /*
             * wenn bisher noch keine aktuellen Schritte ermittelt wurden, dann dies jetzt nachholen, damit die Liste vollstÃ¤ndig ist
             */
            if (this.page == null && (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}") != null) {
                FilterAlleStart();
            }
            Integer inParam = Integer.valueOf(param);
            if (this.mySchritt == null || this.mySchritt.getId() == null || !this.mySchritt.getId().equals(inParam)) {
                this.mySchritt = new SchrittDAO().get(inParam);
            }
        }
    }

    /*
     * Auswahl mittels Selectboxen
     */

    @SuppressWarnings("unchecked")
    public void SelectionAll() {
        for (Iterator<Schritt> iter = this.page.getList().iterator(); iter.hasNext();) {
            Schritt s = iter.next();
            s.setSelected(true);
        }
    }

    @SuppressWarnings("unchecked")
    public void SelectionNone() {
        for (Iterator<Schritt> iter = this.page.getList().iterator(); iter.hasNext();) {
            Schritt s = iter.next();
            s.setSelected(false);
        }
    }

    /*
     * Downloads
     */

    public void DownloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.mySchritt.getProzess());
        tiff.ExportStart();
    }


    public void ExportDMS() {
        ExportDms export = new ExportDms();
        try {
            export.startExport(this.mySchritt.getProzess());
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on export", e.getMessage());
            logger.error(e);
        }
    }

    public boolean isNurOffeneSchritte() {
        return this.nurOffeneSchritte;
    }

    public void setNurOffeneSchritte(boolean nurOffeneSchritte) {
        this.nurOffeneSchritte = nurOffeneSchritte;
    }

    public boolean isNurEigeneSchritte() {
        return this.nurEigeneSchritte;
    }

    public void setNurEigeneSchritte(boolean nurEigeneSchritte) {
        this.nurEigeneSchritte = nurEigeneSchritte;
    }

    public HashMap<String, Boolean> getAnzeigeAnpassen() {
        return this.anzeigeAnpassen;
    }

    public void setAnzeigeAnpassen(HashMap<String, Boolean> anzeigeAnpassen) {
        this.anzeigeAnpassen = anzeigeAnpassen;
    }

    /**
     * @return values for wiki field
     */
    public String getWikiField() {
        return this.mySchritt.getProzess().getWikifield();

    }

    /**
     * sets new value for wiki field
     *
     * @param inString
     */
    public void setWikiField(String inString) {
        this.mySchritt.getProzess().setWikifield(inString);
    }

    public String getAddToWikiField() {
        return this.addToWikiField;
    }

    public void setAddToWikiField(String addToWikiField) {
        this.addToWikiField = addToWikiField;
    }

    public void addToWikiField() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            Benutzer user = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            this.mySchritt.getProzess().addToWikiField(user, this.addToWikiField);
            this.addToWikiField = "";
            try {
                this.pdao.save(this.mySchritt.getProzess());
            } catch (DAOException e) {
                logger.error(e);
            }
        }
    }

    // TODO property

    public ProcessProperty getProcessProperty() {
        return this.processProperty;
    }

    public void setProcessProperty(ProcessProperty processProperty) {
        this.processProperty = processProperty;
    }

    public List<ProcessProperty> getProcessProperties() {
        return this.processPropertyList;
    }

    private void loadProcessProperties() {
        this.containers = new TreeMap<Integer, PropertyListObject>();
        this.processPropertyList = PropertyParser.getPropertiesForStep(this.mySchritt);

        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getProzesseigenschaft() == null) {
                Prozesseigenschaft pe = new Prozesseigenschaft();
                pe.setProzess(this.mySchritt.getProzess());
                pt.setProzesseigenschaft(pe);
                this.mySchritt.getProzess().getEigenschaftenInitialized().add(pe);
                pt.transfer();
            }
            if (!this.containers.keySet().contains(pt.getContainer())) {
                PropertyListObject plo = new PropertyListObject(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            } else {
                PropertyListObject plo = this.containers.get(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            }
        }
    }

    public void saveProcessProperties() {
        boolean valid = true;
        for (IProperty p : this.processPropertyList) {
            if (!p.isValid()) {
                List<String> param = new ArrayList<String>();
                param.add(p.getName());
                String value = Helper.getTranslation("propertyNotValid", param);
                Helper.setFehlerMeldung(value);
                valid = false;
            }
        }

        if (valid) {
            for (ProcessProperty p : this.processPropertyList) {
                if (p.getProzesseigenschaft() == null) {
                    Prozesseigenschaft pe = new Prozesseigenschaft();
                    pe.setProzess(this.mySchritt.getProzess());
                    p.setProzesseigenschaft(pe);
                    this.mySchritt.getProzess().getEigenschaftenInitialized().add(pe);
                }
                p.transfer();
                if (!this.mySchritt.getProzess().getEigenschaftenInitialized().contains(p.getProzesseigenschaft())) {
                    this.mySchritt.getProzess().getEigenschaftenInitialized().add(p.getProzesseigenschaft());
                }
            }
            Prozess p = this.mySchritt.getProzess();
            List<Prozesseigenschaft> props = p.getEigenschaftenList();
            for (Prozesseigenschaft pe : props) {
                if (pe.getTitel() == null) {
                    p.getEigenschaftenInitialized().remove(pe);
                }
            }

            try {
                this.pdao.save(p);
                Helper.setMeldung("propertiesSaved");
            } catch (DAOException e) {
                logger.error(e);
                Helper.setFehlerMeldung("propertiesNotSaved");
            }
        }
    }

    public void saveCurrentProperty() {
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                List<String> param = new ArrayList<String>();
                param.add(processProperty.getName());
                String value = Helper.getTranslation("propertyNotValid", param);
                Helper.setFehlerMeldung(value);
                Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                Prozesseigenschaft pe = new Prozesseigenschaft();
                pe.setProzess(this.mySchritt.getProzess());
                this.processProperty.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaftenInitialized().add(pe);
            }
            this.processProperty.transfer();

            List<Prozesseigenschaft> props = this.mySchritt.getProzess().getEigenschaftenList();
            for (Prozesseigenschaft pe : props) {
                if (pe.getTitel() == null) {
                    this.mySchritt.getProzess().getEigenschaftenInitialized().remove(pe);
                }
            }
            if (!this.mySchritt.getProzess().getEigenschaftenInitialized().contains(this.processProperty.getProzesseigenschaft())) {
                this.mySchritt.getProzess().getEigenschaftenInitialized().add(this.processProperty.getProzesseigenschaft());
                this.processProperty.getProzesseigenschaft().setProzess(this.mySchritt.getProzess());
            }
            try {
                this.pdao.save(this.mySchritt.getProzess());
                Helper.setMeldung("propertySaved");
            } catch (DAOException e) {
                logger.error(e);
                Helper.setFehlerMeldung("propertyNotSaved");
            }
        }
        loadProcessProperties();
    }

    public Map<Integer, PropertyListObject> getContainers() {
        return this.containers;
    }

    public List<Integer> getContainerList() {
        return new ArrayList<Integer>(this.containers.keySet());
    }

    public int getPropertyListSize() {
        if (this.processPropertyList == null) {
            return 0;
        }
        return this.processPropertyList.size();
    }

    public List<ProcessProperty> getSortedProperties() {
        Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    public void deleteProperty() {
        this.processPropertyList.remove(this.processProperty);
        // if (this.processProperty.getProzesseigenschaft().getId() != null) {
        this.mySchritt.getProzess().getEigenschaftenInitialized().remove(this.processProperty.getProzesseigenschaft());
        // this.mySchritt.getProzess().removeProperty(this.processProperty.getProzesseigenschaft());
        // }

        List<Prozesseigenschaft> props = this.mySchritt.getProzess().getEigenschaftenList();
        for (Prozesseigenschaft pe : props) {
            if (pe.getTitel() == null) {
                this.mySchritt.getProzess().getEigenschaftenInitialized().remove(pe);
            }
        }
        try {
            this.pdao.save(this.mySchritt.getProzess());
        } catch (DAOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("propertiesNotDeleted");
        }
        // saveWithoutValidation();
        loadProcessProperties();
    }

    public void duplicateProperty() {
        ProcessProperty pt = this.processProperty.getClone(0);
        this.processPropertyList.add(pt);
        this.processProperty = pt;
        saveCurrentProperty();
        loadProcessProperties();
    }

    public BatchStepHelper getBatchHelper() {
        return this.batchHelper;
    }

    public void setBatchHelper(BatchStepHelper batchHelper) {
        this.batchHelper = batchHelper;
    }

    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
        for (ProcessProperty pp : this.processPropertyList) {
            if (pp.getContainer() == 0
                    && (StringUtils.isNotBlank(pp.getValue()) || !getOccupiedProperties().contains(pp.getName()))) {
                answer.add(pp);
            }
        }
        return answer;
    }

    private Set<String> getOccupiedProperties() {
        Set<String> occupiedProperties = new HashSet<>();
        for (ProcessProperty processProperty : processPropertyList) {
            if (StringUtils.isNotBlank(processProperty.getValue())) {
                occupiedProperties.add(processProperty.getName());
            }
        }
        return occupiedProperties;
    }

    public Integer getContainer() {
        return this.container;
    }

    public void setContainer(Integer container) {
        this.container = container;
        if (container != null && container > 0) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    public List<ProcessProperty> getContainerProperties() {
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
        // int currentContainer = this.processProperty.getContainer();

        if (this.container != null && this.container > 0) {
            for (ProcessProperty pp : this.processPropertyList) {
                if (pp.getContainer() == this.container) {
                    answer.add(pp);
                }
            }
        } else {
            answer.add(this.processProperty);
        }

        return answer;
    }

    public String duplicateContainer() {
        Integer currentContainer = this.processProperty.getContainer();
        List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
        // search for all properties in container
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getContainer() == currentContainer) {
                plist.add(pt);
            }
        }
        int newContainerNumber = 0;
        if (currentContainer > 0) {
            newContainerNumber++;
            // find new unused container number
            boolean search = true;
            while (search) {
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    newContainerNumber++;
                }
            }
        }
        // clone properties
        for (ProcessProperty pt : plist) {
            ProcessProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            if (this.processProperty.getProzesseigenschaft() == null) {
                Prozesseigenschaft pe = new Prozesseigenschaft();
                pe.setProzess(this.mySchritt.getProzess());
                this.processProperty.setProzesseigenschaft(pe);
                this.mySchritt.getProzess().getEigenschaftenInitialized().add(pe);
            }
            this.processProperty.transfer();

        }
        try {
            this.pdao.save(this.mySchritt.getProzess());
            Helper.setMeldung("propertySaved");
        } catch (DAOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("propertiesNotSaved");
        }
        loadProcessProperties();
        return "";
    }

    public boolean getShowAutomaticTasks() {
        return this.showAutomaticTasks;
    }

    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        this.showAutomaticTasks = showAutomaticTasks;
    }

    public boolean getHideCorrectionTasks() {
        return hideCorrectionTasks;
    }

    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        this.hideCorrectionTasks = hideCorrectionTasks;
    }

    public String callStepPlugin() {
        if (mySchritt.getStepPlugin() != null && mySchritt.getStepPlugin().length() > 0) {
            IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, mySchritt.getStepPlugin());
            isp.initialize(mySchritt, "");
            isp.execute();
        }
        return "";
    }
}
