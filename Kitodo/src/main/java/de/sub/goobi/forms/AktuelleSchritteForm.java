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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.helper.BatchStepHelper;
import de.sub.goobi.helper.FileUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritteWithoutHibernate;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.PropertyListObject;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.metadaten.MetadatenVerifizierung;
import de.unigoettingen.goobi.module.api.exception.GoobiException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

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
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryType;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.apache.StepManager;
import org.kitodo.data.database.persistence.apache.StepObject;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.services.ServiceManager;

public class AktuelleSchritteForm extends BasisForm {
    private static final long serialVersionUID = 5841566727939692509L;
    private static final Logger myLogger = Logger.getLogger(AktuelleSchritteForm.class);
    private Process myProcess = new Process();
    private Task mySchritt = new Task();
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
    private Boolean flagWait = false;
    private final ReentrantLock flagWaitLock = new ReentrantLock();
    private BatchStepHelper batchHelper;
    private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
    private Integer container;
    private List<ProcessProperty> processPropertyList;
    private ProcessProperty processProperty;
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Constructor.
     */
    public AktuelleSchritteForm() {
        this.anzeigeAnpassen = new HashMap<String, Boolean>();
        this.anzeigeAnpassen.put("lockings", false);
        this.anzeigeAnpassen.put("selectionBoxes", false);
        this.anzeigeAnpassen.put("processId", false);
        this.anzeigeAnpassen.put("modules", false);
        this.anzeigeAnpassen.put("batchId", false);
        /*
         * Vorgangsdatum generell anzeigen?
         */
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login != null && login.getMyBenutzer() != null) {
            this.anzeigeAnpassen.put("processDate", login.getMyBenutzer().isConfigProductionDateShow());
        } else {
            this.anzeigeAnpassen.put("processDate", false);
        }
        DONEDIRECTORYNAME = ConfigCore.getParameter("doneDirectoryName", "fertig/");
    }

    /*
     * Filter
     */

    /**
     * Anzeige der Schritte.
     */
    public String FilterAlleStart() {
        try {
            this.myFilteredDataSource = new UserDefinedStepFilter(true);

            this.myFilteredDataSource.getObservable().addObserver(new Helper().createObserver());
            ((UserDefinedStepFilter) this.myFilteredDataSource).setFilterModes(this.nurOffeneSchritte,
                    this.nurEigeneSchritte);
            this.myFilteredDataSource.setFilter(this.filter);

            Criteria crit = this.myFilteredDataSource.getCriteria();
            if (!this.showAutomaticTasks) {
                crit.add(Restrictions.eq("typeAutomatic", false));
            }
            if (hideCorrectionTasks) {
                crit.add(Restrictions.not(Restrictions.eq("priority", 10)));
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
        inCrit.addOrder(Order.desc("priority"));

        Order order = Order.asc("proc.title");
        if (this.sortierung.equals("schrittAsc")) {
            order = Order.asc("title");
        }
        if (this.sortierung.equals("schrittDesc")) {
            order = Order.desc("title");
        }
        if (this.sortierung.equals("prozessAsc")) {
            order = Order.asc("proc.title");
        }
        if (this.sortierung.equals("prozessDesc")) {
            order = Order.desc("proc.title");
        }
        if (this.sortierung.equals("batchAsc")) {
            order = Order.asc("proc.batchID");
        }
        if (this.sortierung.equals("batchDesc")) {
            order = Order.desc("proc.batchID");
        }
        if (this.sortierung.equals("prozessdateAsc")) {
            order = Order.asc("proc.creationDate");
        }
        if (this.sortierung.equals("prozessdateDesc")) {
            order = Order.desc("proc.creationDate");
        }
        if (this.sortierung.equals("projektAsc")) {
            order = Order.asc("proj.title");
        }
        if (this.sortierung.equals("projektDesc")) {
            order = Order.desc("proj.title");
        }
        if (this.sortierung.equals("modulesAsc")) {
            order = Order.asc("typeModuleName");
        }
        if (this.sortierung.equals("modulesDesc")) {
            order = Order.desc("typeModuleName");
        }
        if (this.sortierung.equals("statusAsc")) {
            order = Order.asc("processingStatus");
        }
        if (this.sortierung.equals("statusDesc")) {
            order = Order.desc("processingStatus");
        }

        inCrit.addOrder(order);
    }

    /**
     * Bearbeitung des Schritts übernehmen oder abschliessen.
     */
    public String SchrittDurchBenutzerUebernehmen() {
        this.flagWaitLock.lock();
        try {
            if (!this.flagWait) {
                this.flagWait = true;

                // Helper.getHibernateSession().clear();
                Helper.getHibernateSession().refresh(this.mySchritt);

                if (this.mySchritt.getProcessingStatusEnum() != TaskStatus.OPEN) {
                    Helper.setFehlerMeldung("stepInWorkError");
                    this.flagWait = false;
                    return "";
                } else {
                    this.mySchritt.setProcessingStatusEnum(TaskStatus.INWORK);
                    this.mySchritt.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
                    mySchritt.setProcessingTime(new Date());
                    User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                    if (ben != null) {
                        mySchritt.setProcessingUser(ben);
                    }
                    if (this.mySchritt.getProcessingBegin() == null) {
                        Date myDate = new Date();
                        this.mySchritt.setProcessingBegin(myDate);
                    }
                    this.serviceManager.getProcessService().getHistoryInitialized(this.mySchritt.getProcess())
                            .add(new History(this.mySchritt.getProcessingBegin(),
                                    this.mySchritt.getOrdering().doubleValue(), this.mySchritt.getTitle(),
                                    HistoryType.taskInWork, this.mySchritt.getProcess()));
                    try {
                        /*
                         * den Prozess aktualisieren, so dass der
                         * Sortierungshelper gespeichert wird
                         */
                        this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                        myLogger.error("step couldn't get saved", e);
                    } catch (IOException e) {
                        myLogger.error("process couldn't get inserted", e);
                    } catch (ResponseException e) {
                        myLogger.error("Elastic Search incorrect server response", e);
                    } finally {
                        this.flagWait = false;
                    }
                    /*
                     * wenn es ein Image-Schritt ist, dann gleich die Images ins
                     * Home
                     */

                    if (this.mySchritt.isTypeImagesRead() || this.mySchritt.isTypeImagesWrite()) {
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

    /**
     * Edit task.
     *
     * @return page
     */
    public String EditStep() {

        Helper.getHibernateSession().refresh(mySchritt);

        return "AktuelleSchritteBearbeiten";
    }

    /**
     * Take over batch.
     *
     * @return page
     */
    @SuppressWarnings("unchecked")
    public String TakeOverBatch() {
        // find all steps with same batch id and step status
        List<Task> currentStepsOfBatch = new ArrayList<Task>();

        String steptitle = this.mySchritt.getTitle();
        List<Batch> batches = serviceManager.getProcessService().getBatchesByType(mySchritt.getProcess(),
                Type.LOGISTIC);
        if (batches.size() > 1) {
            Helper.setFehlerMeldung("multipleBatchesAssigned");
            return "";
        }
        if (batches.size() != 0) {
            Integer batchNumber = batches.iterator().next().getId();
            // only steps with same title
            Session session = Helper.getHibernateSession();
            Criteria crit = session.createCriteria(Task.class);
            crit.add(Restrictions.eq("title", steptitle));
            // only steps with same batchid
            crit.createCriteria("process", "proc");
            crit.createCriteria("proc.batches", "bat");
            crit.add(Restrictions.eq("bat.id", batchNumber));
            crit.add(Restrictions.eq("batchStep", true));

            currentStepsOfBatch = crit.list();
        } else {
            return SchrittDurchBenutzerUebernehmen();
        }
        // if only one step is assigned for this batch, use the single

        // Helper.setMeldung("found " + currentStepsOfBatch.size() + " elements
        // in batch");
        if (currentStepsOfBatch.size() == 0) {
            return "";
        }
        if (currentStepsOfBatch.size() == 1) {
            return SchrittDurchBenutzerUebernehmen();
        }

        for (Task s : currentStepsOfBatch) {
            if (s.getProcessingStatusEnum().equals(TaskStatus.OPEN)) {
                s.setProcessingStatusEnum(TaskStatus.INWORK);
                s.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
                s.setProcessingTime(new Date());
                User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    s.setProcessingUser(ben);
                }
                if (s.getProcessingBegin() == null) {
                    Date myDate = new Date();
                    s.setProcessingBegin(myDate);
                }
                serviceManager.getProcessService().getHistoryInitialized(s.getProcess())
                        .add(new History(s.getProcessingBegin(), s.getOrdering().doubleValue(), s.getTitle(),
                                HistoryType.taskInWork, s.getProcess()));

                if (s.isTypeImagesRead() || s.isTypeImagesWrite()) {
                    try {
                        new File(serviceManager.getProcessService().getImagesOrigDirectory(false, s.getProcess()));
                    } catch (Exception e1) {

                    }
                    s.setProcessingTime(new Date());

                    if (ben != null) {
                        s.setProcessingUser(ben);
                    }
                    this.myDav.downloadToHome(s.getProcess(), s.getId(), !s.isTypeImagesWrite());

                }
            }

            try {
                this.serviceManager.getProcessService().save(s.getProcess());
            } catch (DAOException e) {
                Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                myLogger.error("task couldn't get saved", e);
            } catch (IOException | ResponseException e) {
                myLogger.error("task couldn't get inserted", e);
            }
        }

        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch));
        return "BatchesEdit";
    }

    /**
     * Edit batch.
     *
     * @return page
     */
    @SuppressWarnings("unchecked")
    public String BatchesEdit() {
        // find all steps with same batch id and step status
        List<Task> currentStepsOfBatch = new ArrayList<Task>();

        String steptitle = this.mySchritt.getTitle();
        List<Batch> batches = serviceManager.getProcessService().getBatchesByType(mySchritt.getProcess(),
                Type.LOGISTIC);
        if (batches.size() > 1) {
            Helper.setFehlerMeldung("multipleBatchesAssigned");
            return "";
        }
        if (batches.size() != 0) {
            Integer batchNumber = batches.iterator().next().getId();
            // only steps with same title

            Session session = Helper.getHibernateSession();
            Criteria crit = session.createCriteria(Task.class);
            crit.add(Restrictions.eq("title", steptitle));
            // only steps with same batchid
            crit.createCriteria("process", "proc");
            crit.createCriteria("proc.batches", "bat");
            crit.add(Restrictions.eq("bat.id", batchNumber));
            crit.add(Restrictions.eq("batchStep", true));

            currentStepsOfBatch = crit.list();
        } else {
            return "AktuelleSchritteBearbeiten";
        }
        // if only one step is assigned for this batch, use the single

        // Helper.setMeldung("found " + currentStepsOfBatch.size() + " elements
        // in batch");

        if (currentStepsOfBatch.size() == 1) {
            return "AktuelleSchritteBearbeiten";
        }
        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch));
        return "BatchesEdit";
    }

    @Deprecated
    public void saveProperties() {
    }

    /**
     * Not sure.
     *
     * @return page
     */
    public String SchrittDurchBenutzerZurueckgeben() {
        this.myDav.uploadFromHome(this.mySchritt.getProcess());
        this.mySchritt.setProcessingStatusEnum(TaskStatus.OPEN);
        // mySchritt.setBearbeitungsbenutzer(null);
        // if we have a correction-step here then never remove startdate
        if (serviceManager.getTaskService().isCorrectionStep(this.mySchritt)) {
            this.mySchritt.setProcessingBegin(null);
        }
        this.mySchritt.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        mySchritt.setProcessingTime(new Date());
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setProcessingUser(ben);
        }

        try {
            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper
             * gespeichert wird
             */
            this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
        } catch (DAOException | IOException | ResponseException e) {
            myLogger.error("task couldn't get saved/inserted", e);
        }
        // calcHomeImages();
        return "AktuelleSchritteAlle";
    }

    /**
     * Not sure.
     *
     * @return page
     */
    public String SchrittDurchBenutzerAbschliessen() {

        if (mySchritt.getValidationPlugin() != null && mySchritt.getValidationPlugin().length() > 0) {
            IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation,
                    mySchritt.getValidationPlugin());
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
         * if step allows writing of images, then count all images here
         */
        if (this.mySchritt.isTypeImagesWrite()) {
            try {
                // this.mySchritt.getProzess().setSortHelperImages(
                // FileUtils.getNumberOfFiles(new
                // File(this.mySchritt.getProzess().getImagesOrigDirectory())));
                HistoryAnalyserJob.updateHistory(this.mySchritt.getProcess());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while calculation of storage and images", e);
            }
        }

        /*
         * wenn das Resultat des Arbeitsschrittes zunÃ¤chst verifiziert werden
         * soll, dann ggf. das Abschliessen abbrechen
         */
        if (this.mySchritt.isTypeCloseVerify()) {
            /* Metadatenvalidierung */
            if (this.mySchritt.isTypeMetadata() && ConfigCore.getBooleanParameter("useMetadatenvalidierung")) {
                MetadatenVerifizierung mv = new MetadatenVerifizierung();
                mv.setAutoSave(true);
                if (!mv.validate(this.mySchritt.getProcess())) {
                    return "";
                }
            }

            /* Imagevalidierung */
            if (this.mySchritt.isTypeImagesWrite()) {
                MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
                try {
                    if (!mih.checkIfImagesValid(this.mySchritt.getProcess().getTitle(), serviceManager
                            .getProcessService().getImagesOrigDirectory(false, this.mySchritt.getProcess()))) {
                        return "";
                    }
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Error on image validation: ", e);
                }
            }
        }

        for (ProcessProperty prop : processPropertyList) {
            if (prop.getCurrentStepAccessCondition().equals(AccessCondition.WRITEREQUIRED)
                    && (prop.getValue() == null || prop.getValue().equals(""))) {
                Helper.setFehlerMeldung(Helper.getTranslation("Eigenschaft") + " " + prop.getName() + " "
                        + Helper.getTranslation("requiredValue"));
                return "";
            } else if (!prop.isValid()) {
                List<String> parameter = new ArrayList<String>();
                parameter.add(prop.getName());
                Helper.setFehlerMeldung(Helper.getTranslation("PropertyValidation", parameter));
                return "";
            }
        }

        /*
         * wenn das Ergebnis der Verifizierung ok ist, dann weiter, ansonsten
         * schon vorher draussen
         */
        this.myDav.uploadFromHome(this.mySchritt.getProcess());
        this.mySchritt.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        // it returns null! - not possible to close task
        StepObject so = StepManager.getStepById(this.mySchritt.getId());
        new HelperSchritteWithoutHibernate().CloseStepObjectAutomatic(so, true);
        // new HelperSchritte().SchrittAbschliessen(this.mySchritt, true);
        return FilterAlleStart();
    }

    public String SperrungAufheben() {
        MetadatenSperrung.unlockProcess(this.mySchritt.getProcess().getId());
        return "";
    }

    /**
     * Korrekturmeldung an vorherige Schritte.
     */
    @SuppressWarnings("unchecked")
    public List<Task> getPreviousStepsForProblemReporting() {
        List<Task> alleVorherigenSchritte = Helper.getHibernateSession().createCriteria(Task.class)
                .add(Restrictions.lt("ordering", this.mySchritt.getOrdering())).addOrder(Order.desc("ordering"))
                .createCriteria("process").add(Restrictions.idEq(this.mySchritt.getProcess().getId())).list();
        return alleVorherigenSchritte;
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    /**
     * Report the problem.
     *
     * @return problem as String
     */
    @SuppressWarnings("unchecked")
    public String ReportProblem() {
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben == null) {
            Helper.setFehlerMeldung("userNotFound");
            return "";
        }
        if (myLogger.isDebugEnabled()) {
            myLogger.debug("mySchritt.ID: " + this.mySchritt.getId());
            myLogger.debug("Korrekturschritt.ID: " + this.myProblemID);
        }
        this.myDav.uploadFromHome(this.mySchritt.getProcess());
        Date myDate = new Date();
        this.mySchritt.setProcessingStatusEnum(TaskStatus.LOCKED);
        this.mySchritt.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        mySchritt.setProcessingTime(new Date());
        mySchritt.setProcessingUser(ben);
        this.mySchritt.setProcessingBegin(null);

        try {
            Task temp = serviceManager.getTaskService().find(this.myProblemID);
            temp.setProcessingStatusEnum(TaskStatus.OPEN);
            temp = serviceManager.getTaskService().setCorrectionStep(temp);
            temp.setProcessingEnd(null);

            org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
            pe.setTitle(Helper.getTranslation("Korrektur notwendig"));
            pe.setValue("[" + this.formatter.format(new Date()) + ", "
                    + serviceManager.getUserService().getFullName(ben) + "] " + this.problemMessage);
            pe.setType(PropertyType.messageError);
            pe.setProcess(this.mySchritt.getProcess());
            this.mySchritt.getProcess().getProperties().add(pe);

            String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitle() + ": " + this.problemMessage
                    + " (" + serviceManager.getUserService().getFullName(ben) + ")";
            this.mySchritt.getProcess().setWikiField(WikiFieldHelper.getWikiMessage(this.mySchritt.getProcess(),
                    this.mySchritt.getProcess().getWikiField(), "error", message));
            serviceManager.getTaskService().save(temp);
            serviceManager.getProcessService().getHistoryInitialized(this.mySchritt.getProcess())
                    .add(new History(myDate, temp.getOrdering().doubleValue(), temp.getTitle(), HistoryType.taskError,
                            temp.getProcess()));
            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt
             * wieder schliessen
             */
            List<Task> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Task.class)
                    .add(Restrictions.le("ordering", this.mySchritt.getOrdering()))
                    .add(Restrictions.gt("ordering", temp.getOrdering())).addOrder(Order.asc("ordering"))
                    .createCriteria("process").add(Restrictions.idEq(this.mySchritt.getProcess().getId())).list();
            for (Iterator<Task> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
                Task step = iter.next();
                step.setProcessingStatusEnum(TaskStatus.LOCKED);
                step = serviceManager.getTaskService().setCorrectionStep(step);
                step.setProcessingEnd(null);
                serviceManager.getTaskService().save(step);
            }

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper
             * gespeichert wird
             */
            this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
        } catch (DAOException | IOException | ResponseException e) {
            myLogger.error("task couldn't get saved/inserted", e);
        }

        this.problemMessage = "";
        this.myProblemID = 0;
        return FilterAlleStart();
    }

    /**
     * Problem-behoben-Meldung an nachfolgende Schritte.
     */
    @SuppressWarnings("unchecked")
    public List<Task> getNextStepsForProblemSolution() {
        List<Task> alleNachfolgendenSchritte = Helper.getHibernateSession().createCriteria(Task.class)
                .add(Restrictions.gt("ordering", this.mySchritt.getOrdering())).add(Restrictions.eq("priority", 10))
                .addOrder(Order.asc("ordering")).createCriteria("process")
                .add(Restrictions.idEq(this.mySchritt.getProcess().getId())).list();
        return alleNachfolgendenSchritte;
    }

    public int getSizeOfNextStepsForProblemSolution() {
        return getNextStepsForProblemSolution().size();
    }

    /**
     * Solve problem.
     *
     * @return String
     */
    @SuppressWarnings("unchecked")
    public String SolveProblem() {
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben == null) {
            Helper.setFehlerMeldung("userNotFound");
            return "";
        }
        Date now = new Date();
        this.myDav.uploadFromHome(this.mySchritt.getProcess());
        this.mySchritt.setProcessingStatusEnum(TaskStatus.DONE);
        this.mySchritt.setProcessingEnd(now);
        this.mySchritt.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        mySchritt.setProcessingTime(new Date());
        mySchritt.setProcessingUser(ben);

        try {
            Task temp = serviceManager.getTaskService().find(this.mySolutionID);
            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt
             * wieder schliessen
             */
            List<Task> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Task.class)
                    .add(Restrictions.ge("ordering", this.mySchritt.getOrdering()))
                    .add(Restrictions.le("ordering", temp.getOrdering())).addOrder(Order.asc("ordering"))
                    .createCriteria("process").add(Restrictions.idEq(this.mySchritt.getProcess().getId())).list();
            for (Iterator<Task> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
                Task step = iter.next();
                step.setProcessingStatusEnum(TaskStatus.DONE);
                step.setProcessingEnd(now);
                step.setPriority(0);
                if (step.getId().intValue() == temp.getId().intValue()) {
                    step.setProcessingStatusEnum(TaskStatus.OPEN);
                    step = serviceManager.getTaskService().setCorrectionStep(step);
                    step.setProcessingEnd(null);
                    // step.setBearbeitungsbeginn(null);
                    step.setProcessingTime(now);
                }
                mySchritt.setProcessingTime(new Date());
                mySchritt.setProcessingUser(ben);
                serviceManager.getTaskService().save(step);
            }

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper
             * gespeichert wird
             */
            String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitle() + ": "
                    + this.solutionMessage + " (" + serviceManager.getUserService().getFullName(ben) + ")";
            this.mySchritt.getProcess().setWikiField(WikiFieldHelper.getWikiMessage(this.mySchritt.getProcess(),
                    this.mySchritt.getProcess().getWikiField(), "info", message));

            org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
            pe.setTitle(Helper.getTranslation("Korrektur durchgefuehrt"));
            pe.setValue(
                    "[" + this.formatter.format(new Date()) + ", " + serviceManager.getUserService().getFullName(ben)
                            + "] " + Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitle() + ": "
                            + this.solutionMessage);
            pe.setType(PropertyType.messageImportant);
            pe.setProcess(this.mySchritt.getProcess());
            this.mySchritt.getProcess().getProperties().add(pe);

            this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
        } catch (DAOException | IOException | ResponseException e) {
            myLogger.error("task couldn't get saved/inserted", e);
        }

        this.solutionMessage = "";
        this.mySolutionID = 0;
        return FilterAlleStart();
    }

    /**
     * Upload und Download der Images.
     */
    public String UploadFromHome() {
        mySchritt.setProcessingTime(new Date());
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setProcessingUser(ben);
        }
        this.myDav.uploadFromHome(this.mySchritt.getProcess());
        Helper.setMeldung(null, "Removed directory from user home", this.mySchritt.getProcess().getTitle());
        return "";
    }

    /**
     * Download to home.
     *
     * @return String
     */
    public String DownloadToHome() {
        try {
            new File(serviceManager.getProcessService().getImagesOrigDirectory(false, this.mySchritt.getProcess()));
        } catch (Exception e1) {

        }
        mySchritt.setProcessingTime(new Date());
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setProcessingUser(ben);
        }
        this.myDav.downloadToHome(this.mySchritt.getProcess(), this.mySchritt.getId(),
                !this.mySchritt.isTypeImagesWrite());

        return "";
    }

    /**
     * Upload from home.
     *
     * @return String
     */
    @SuppressWarnings("unchecked")
    public String UploadFromHomeAlle() throws NumberFormatException, DAOException {
        List<String> fertigListe = this.myDav.uploadAllFromHome(DONEDIRECTORYNAME);
        List<String> geprueft = new ArrayList<String>();
        /*
         * die hochgeladenen Prozess-IDs durchlaufen und auf abgeschlossen
         * setzen
         */
        if (fertigListe != null && fertigListe.size() > 0 && this.nurOffeneSchritte) {
            this.nurOffeneSchritte = false;
            FilterAlleStart();
        }
        for (Iterator<String> iter = fertigListe.iterator(); iter.hasNext();) {
            String element = iter.next();
            String myID = element.substring(element.indexOf("[") + 1, element.indexOf("]")).trim();

            for (Iterator<Task> iterator = this.page.getCompleteList().iterator(); iterator.hasNext();) {
                Task step = iterator.next();
                /*
                 * nur wenn der Schritt bereits im Bearbeitungsmodus ist,
                 * abschliessen
                 */
                if (step.getProcess().getId() == Integer.parseInt(myID)
                        && step.getProcessingStatusEnum() == TaskStatus.INWORK) {
                    this.mySchritt = step;
                    if (!SchrittDurchBenutzerAbschliessen().isEmpty()) {
                        geprueft.add(element);
                    }
                    this.mySchritt.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
                }
            }
        }

        this.myDav.removeAllFromHome(geprueft, DONEDIRECTORYNAME);
        Helper.setMeldung(null, "removed " + geprueft.size() + " directories from user home:", DONEDIRECTORYNAME);
        return "";
    }

    /**
     * Download to home page.
     *
     * @return String
     */
    @SuppressWarnings("unchecked")
    public String DownloadToHomePage() {
        for (Iterator<Task> iter = this.page.getListReload().iterator(); iter.hasNext();) {
            Task step = iter.next();
            if (step.getProcessingStatusEnum() == TaskStatus.OPEN) {
                step.setProcessingStatusEnum(TaskStatus.INWORK);
                step.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
                mySchritt.setProcessingTime(new Date());
                User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    mySchritt.setProcessingUser(ben);
                }
                step.setProcessingBegin(new Date());
                Process proz = step.getProcess();
                try {
                    this.serviceManager.getProcessService().save(proz);
                } catch (DAOException e) {
                    Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitle());
                } catch (IOException e) {
                    Helper.setMeldung("errorElasticSearch" + proz.getTitle());
                } catch (ResponseException e) {
                    Helper.setMeldung("ElasticSearch server incorrect response" + proz.getTitle());
                }
                this.myDav.downloadToHome(proz, step.getId(), false);
            }
        }
        // calcHomeImages();
        Helper.setMeldung(null, "Created directies in user home", "");
        return "";
    }

    /**
     * Download to home.
     *
     * @return String
     */
    @SuppressWarnings("unchecked")
    public String DownloadToHomeHits() {

        for (Iterator<Task> iter = this.page.getCompleteList().iterator(); iter.hasNext();) {
            Task step = iter.next();
            if (step.getProcessingStatusEnum() == TaskStatus.OPEN) {
                step.setProcessingStatusEnum(TaskStatus.INWORK);
                step.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
                mySchritt.setProcessingTime(new Date());
                User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    mySchritt.setProcessingUser(ben);
                }
                step.setProcessingBegin(new Date());
                Process proz = step.getProcess();
                try {
                    this.serviceManager.getProcessService().save(proz);
                } catch (DAOException e) {
                    Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitle());
                } catch (IOException e) {
                    Helper.setMeldung("errorElasticSearch" + proz.getTitle());
                } catch (ResponseException e) {
                    Helper.setMeldung("ElasticSearch server incorrect response" + proz.getTitle());
                }
                this.myDav.downloadToHome(proz, step.getId(), false);
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

    /**
     * Execute script.
     */
    public void executeScript() {
        StepObject so = StepManager.getStepById(this.mySchritt.getId());
        new HelperSchritteWithoutHibernate().executeScriptForStepObject(so, this.scriptPath, false);

    }

    /**
     * call module for this step.
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

    /**
     * Calc home images.
     */
    @SuppressWarnings("unchecked")
    public void calcHomeImages() {
        this.gesamtAnzahlImages = 0;
        this.pageAnzahlImages = 0;
        User aktuellerBenutzer = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (aktuellerBenutzer != null && aktuellerBenutzer.isWithMassDownload()) {
            for (Iterator<Task> iter = this.page.getCompleteList().iterator(); iter.hasNext();) {
                Task step = iter.next();
                try {
                    if (step.getProcessingStatusEnum() == TaskStatus.OPEN) {
                        // gesamtAnzahlImages +=
                        // myDav.getAnzahlImages(step.getProzess().getImagesOrigDirectory());
                        this.gesamtAnzahlImages += FileUtils.getNumberOfFiles(
                                serviceManager.getProcessService().getImagesOrigDirectory(false, step.getProcess()));
                    }
                } catch (Exception e) {
                    myLogger.error(e);
                }
            }
        }
    }

    public Process getMyProzess() {
        return this.myProcess;
    }

    public void setMyProzess(Process myProzess) {
        this.myProcess = myProzess;
    }

    /**
     * Get my task.
     *
     * @return task
     */
    public Task getMySchritt() {
        try {
            schrittPerParameterLaden();
        } catch (DAOException | NumberFormatException e) {
            myLogger.error(e);
        }
        return this.mySchritt;
    }

    /**
     * Set my task.
     *
     * @param mySchritt
     *            task
     */
    public void setMySchritt(Task mySchritt) {
        this.modusBearbeiten = "";
        this.mySchritt = mySchritt;
        loadProcessProperties();
    }

    public void setStep(Task step) {
        this.mySchritt = step;
        loadProcessProperties();
    }

    public Task getStep() {
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
     * Parameter per Get Ã¼bergeben bekommen und entsprechen den passenden
     * Schritt laden
     */

    /**
     * prüfen, ob per Parameter vielleicht zunÃ¤chst ein anderer geladen werden
     * soll.
     *
     * @throws DAOException
     *             , NumberFormatException
     */
    private void schrittPerParameterLaden() throws DAOException, NumberFormatException {
        String param = Helper.getRequestParameter("myid");
        if (param != null && !param.equals("")) {
            /*
             * wenn bisher noch keine aktuellen Schritte ermittelt wurden, dann
             * dies jetzt nachholen, damit die Liste vollstÃ¤ndig ist
             */
            if (this.page == null && (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}") != null) {
                FilterAlleStart();
            }
            Integer inParam = Integer.valueOf(param);
            if (this.mySchritt == null || this.mySchritt.getId() == null || !this.mySchritt.getId().equals(inParam)) {
                this.mySchritt = serviceManager.getTaskService().find(inParam);
            }
        }
    }

    /**
     * Auswahl mittels Selectboxen.
     */
    @SuppressWarnings("unchecked")
    public void SelectionAll() {
        for (Iterator<Task> iter = this.page.getList().iterator(); iter.hasNext();) {
            Task s = iter.next();
            s.setSelected(true);
        }
    }

    /**
     * Selection none.
     */
    @SuppressWarnings("unchecked")
    public void SelectionNone() {
        for (Iterator<Task> iter = this.page.getList().iterator(); iter.hasNext();) {
            Task s = iter.next();
            s.setSelected(false);
        }
    }

    /**
     * Downloads.
     */
    public void DownloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.mySchritt.getProcess());
        tiff.exportStart();
    }

    /**
     * Export DMS.
     */
    public void ExportDMS() {
        ExportDms export = new ExportDms();
        try {
            export.startExport(this.mySchritt.getProcess());
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on export", e.getMessage());
            myLogger.error(e);
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
     * Get Wiki field.
     *
     * @return values for wiki field
     */
    public String getWikiField() {
        return this.mySchritt.getProcess().getWikiField();

    }

    /**
     * Sets new value for wiki field.
     *
     * @param inString
     *            input String
     */
    public void setWikiField(String inString) {
        this.mySchritt.getProcess().setWikiField(inString);
    }

    public String getAddToWikiField() {
        return this.addToWikiField;
    }

    public void setAddToWikiField(String addToWikiField) {
        this.addToWikiField = addToWikiField;
    }

    /**
     * Add to wiki field.
     */
    public void addToWikiField() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            this.mySchritt.setProcess(serviceManager.getProcessService().addToWikiField(this.addToWikiField,
                    this.mySchritt.getProcess()));
            this.addToWikiField = "";
            try {
                this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
            } catch (DAOException | IOException | ResponseException e) {
                myLogger.error(e);
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
                org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
                pe.setProcess(this.mySchritt.getProcess());
                pt.setProzesseigenschaft(pe);
                serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess()).add(pe);
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

    /**
     * Save process properties.
     */
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
                    org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
                    pe.setProcess(this.mySchritt.getProcess());
                    p.setProzesseigenschaft(pe);
                    serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess()).add(pe);
                }
                p.transfer();
                if (!serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess())
                        .contains(p.getProzesseigenschaft())) {
                    serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess())
                            .add(p.getProzesseigenschaft());
                }
            }
            Process p = this.mySchritt.getProcess();
            List<org.kitodo.data.database.beans.ProcessProperty> props = p.getProperties();
            for (org.kitodo.data.database.beans.ProcessProperty pe : props) {
                if (pe.getTitle() == null) {
                    serviceManager.getProcessService().getPropertiesInitialized(p).remove(pe);
                }
            }

            try {
                this.serviceManager.getProcessService().save(p);
                Helper.setMeldung("propertiesSaved");
            } catch (DAOException e) {
                myLogger.error(e);
                Helper.setFehlerMeldung("propertiesNotSaved");
            } catch (IOException e) {
                myLogger.error(e);
            } catch (ResponseException e) {
                myLogger.error(e);
                Helper.setMeldung("ElasticSearch server incorrect response");
            }
        }
    }

    /**
     * Save current property.
     */
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
                org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
                pe.setProcess(this.mySchritt.getProcess());
                this.processProperty.setProzesseigenschaft(pe);
                serviceManager.getProcessService().getPropertiesInitialized(this.myProcess).add(pe);
            }
            this.processProperty.transfer();

            List<org.kitodo.data.database.beans.ProcessProperty> props = this.mySchritt.getProcess().getProperties();
            for (org.kitodo.data.database.beans.ProcessProperty pe : props) {
                if (pe.getTitle() == null) {
                    // TODO: check carefully how this list is modified
                    serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess()).remove(pe);
                }
            }
            if (!serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess())
                    .contains(this.processProperty.getProzesseigenschaft())) {
                serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess())
                        .add(this.processProperty.getProzesseigenschaft());
                this.processProperty.getProzesseigenschaft().setProcess(this.mySchritt.getProcess());
            }
            try {
                this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
                Helper.setMeldung("propertySaved");
            } catch (DAOException e) {
                myLogger.error(e);
                Helper.setFehlerMeldung("propertyNotSaved");
            } catch (IOException | ResponseException e) {
                myLogger.error(e);
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

    /**
     * Get size of properties list.
     *
     * @return size
     */
    public int getPropertyListSize() {
        if (this.processPropertyList == null) {
            return 0;
        }
        return this.processPropertyList.size();
    }

    /**
     * Get sorted properties.
     *
     * @return list of sorted properties
     */
    public List<ProcessProperty> getSortedProperties() {
        Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    /**
     * Delete property.
     */
    public void deleteProperty() {
        this.processPropertyList.remove(this.processProperty);
        // if (this.processProperty.getProzesseigenschaft().getId() != null) {
        serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess())
                .remove(this.processProperty.getProzesseigenschaft());
        // this.mySchritt.getProzess().removeProperty(this.processProperty.getProzesseigenschaft());
        // }

        List<org.kitodo.data.database.beans.ProcessProperty> props = this.mySchritt.getProcess().getProperties();
        for (org.kitodo.data.database.beans.ProcessProperty pe : props) {
            if (pe.getTitle() == null) {
                serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess()).remove(pe);
            }
        }
        try {
            this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
        } catch (DAOException e) {
            myLogger.error(e);
            Helper.setFehlerMeldung("propertiesNotDeleted");
        } catch (IOException | ResponseException e) {
            myLogger.error(e);
        }
        // saveWithoutValidation();
        loadProcessProperties();
    }

    /**
     * Duplicate property.
     */
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

    /**
     * Get containerless properties.
     *
     * @return list of properties
     */
    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
        for (ProcessProperty pp : this.processPropertyList) {
            if (pp.getContainer() == 0) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public Integer getContainer() {
        return this.container;
    }

    /**
     * Set container.
     *
     * @param container
     *            Integer
     */
    public void setContainer(Integer container) {
        this.container = container;
        if (container != null && container > 0) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    /**
     * Get container properties.
     *
     * @return list of properties
     */
    public List<ProcessProperty> getContainerProperties() {
        List<ProcessProperty> answer = new ArrayList<>();
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

    /**
     * Duplicate container.
     *
     * @return String
     */
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
                org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
                pe.setProcess(this.mySchritt.getProcess());
                this.processProperty.setProzesseigenschaft(pe);
                serviceManager.getProcessService().getPropertiesInitialized(this.mySchritt.getProcess()).add(pe);
            }
            this.processProperty.transfer();

        }
        try {
            this.serviceManager.getProcessService().save(this.mySchritt.getProcess());
            Helper.setMeldung("propertySaved");
        } catch (DAOException e) {
            myLogger.error(e);
            Helper.setFehlerMeldung("propertiesNotSaved");
        } catch (IOException | ResponseException e) {
            myLogger.error(e);
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

    /**
     * Call task plugin.
     *
     * @return String
     */
    public String callStepPlugin() {
        if (mySchritt.getStepPlugin() != null && mySchritt.getStepPlugin().length() > 0) {
            IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, mySchritt.getStepPlugin());
            isp.initialize(mySchritt, "");
            isp.execute();
        }
        return "";
    }
}
