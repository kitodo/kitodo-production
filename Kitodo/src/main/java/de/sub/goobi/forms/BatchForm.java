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
import de.sub.goobi.helper.BatchProcessHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.tasks.ExportNewspaperBatchTask;
import de.sub.goobi.helper.tasks.ExportSerialBatchTask;
import de.sub.goobi.helper.tasks.TaskManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.constants.Parameters;
import org.goobi.production.export.ExportDocket;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.production.exceptions.UnreachableCodeException;
import org.kitodo.services.ServiceManager;

@ManagedBean
@ViewScoped
public class BatchForm extends BasisForm {

    private static final long serialVersionUID = 8234897225425856549L;

    private static final Logger logger = LogManager.getLogger(BatchForm.class);

    private List<Process> currentProcesses;
    private List<Process> selectedProcesses;
    private List<Batch> currentBatches;
    private List<Integer> selectedBatches;
    private String batchfilter;
    private String processfilter;
    private IEvaluableFilter myFilteredDataSource;
    private String modusBearbeiten = "";
    private String batchTitle;
    private final ServiceManager serviceManager = new ServiceManager();

    public List<Process> getCurrentProcesses() {
        return this.currentProcesses;
    }

    public void setCurrentProcesses(List<Process> currentProcesses) {
        this.currentProcesses = currentProcesses;
    }

    /**
     * Load Batch data.
     */
    public void loadBatchData() throws DAOException {
        if (selectedProcesses == null || selectedProcesses.size() == 0) {
            this.currentBatches = serviceManager.getBatchService().findAll();
            this.selectedBatches = new ArrayList<Integer>();
        } else {
            selectedBatches = new ArrayList<Integer>();
            List<Batch> batchesToSelect = new ArrayList<>();
            for (Process process : selectedProcesses) {
                batchesToSelect.addAll(serviceManager.getProcessService().getBatchesInitialized(process));
            }
            for (Batch batch : batchesToSelect) {
                selectedBatches.add(Integer.valueOf(serviceManager.getBatchService().getIdString(batch)));
            }
        }
    }

    /**
     * Load Process data.
     */
    public void loadProcessData() {
        List<Process> processes = new ArrayList<>();
        try {
            for (int b : selectedBatches) {
                processes.addAll(serviceManager.getBatchService().find(b).getProcesses());
            }
            currentProcesses = new ArrayList<Process>(processes);
        } catch (Exception e) { // NumberFormatException, DAOException
            logger.error(e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen");
            return;
        }
    }

    /**
     * Filter processes.
     */
    @SuppressWarnings("unchecked")
    public void filterProcesses() {

        if (this.processfilter == null) {
            this.processfilter = "";
        }
        this.myFilteredDataSource = new UserDefinedFilter(this.processfilter);
        Criteria crit = this.myFilteredDataSource.getCriteria();
        crit.addOrder(Order.desc("creationDate"));
        crit.add(Restrictions.eq("template", Boolean.FALSE));
        int batchMaxSize = ConfigCore.getIntParameter(Parameters.BATCH_DISPLAY_LIMIT, -1);
        if (batchMaxSize > 0) {
            crit.setMaxResults(batchMaxSize);
        }
        try {
            this.currentProcesses = crit.list();
        } catch (HibernateException e) {
            this.currentProcesses = new ArrayList<Process>();
        }
    }

    /**
     * Filter batches.
     */
    public void filterBatches() throws DAOException {
        currentBatches = new ArrayList<Batch>();
        for (Batch batch : serviceManager.getBatchService().findAll()) {
            if (serviceManager.getBatchService().contains(batch, batchfilter)) {
                currentBatches.add(batch);
            }
        }
    }

    /**
     * Get current processes as select items.
     *
     * @return list of select items
     */
    public List<SelectItem> getCurrentProcessesAsSelectItems() {
        List<SelectItem> answer = new ArrayList<SelectItem>();
        for (Process p : this.currentProcesses) {
            answer.add(new SelectItem(p, p.getTitle()));
        }
        return answer;
    }

    public String getBatchfilter() {
        return this.batchfilter;
    }

    public void setBatchfilter(String batchfilter) {
        this.batchfilter = batchfilter;
    }

    public String getBatchName() {
        return batchTitle;
    }

    public void setBatchName(String batchName) {
        batchTitle = batchName;
    }

    public String getProcessfilter() {
        return this.processfilter;
    }

    public void setProcessfilter(String processfilter) {
        this.processfilter = processfilter;
    }

    public List<Batch> getCurrentBatches() {
        return this.currentBatches;
    }

    public void setCurrentBatches(List<Batch> currentBatches) {
        this.currentBatches = currentBatches;
    }

    public List<Process> getSelectedProcesses() {
        return this.selectedProcesses;
    }

    public void setSelectedProcesses(List<Process> selectedProcesses) {
        this.selectedProcesses = selectedProcesses;
    }

    public List<Integer> getSelectedBatches() {
        return this.selectedBatches;
    }

    public void setSelectedBatches(List<Integer> selectedBatches) {
        this.selectedBatches = selectedBatches;
    }

    /**
     * Filter all start.
     *
     * @return page - all batches
     */
    public String filterAlleStart() throws DAOException {
        filterBatches();
        filterProcesses();
        return "/newpages/BatchesAll";
    }

    /**
     * Download docket.
     *
     * @return String
     */
    public String downloadDocket() {
        logger.debug("generate docket for process list");
        String rootpath = ConfigCore.getParameter("xsltFolder");
        File xsltfile = new File(rootpath, "docket_multipage.xsl");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<Process> docket = Collections.emptyList();
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
        } else if (this.selectedBatches.size() == 1) {
            try {
                docket = serviceManager.getBatchService().find(Integer.valueOf(selectedBatches.get(0))).getProcesses();
            } catch (DAOException e) {
                logger.error(e);
                Helper.setFehlerMeldung("fehlerBeimEinlesen");
                return null;
            }
        } else {
            Helper.setFehlerMeldung("tooManyBatchesSelected");
        }
        if (docket.size() > 0) {
            if (!facesContext.getResponseComplete()) {
                HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
                String fileName = "batch_docket" + ".pdf";
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType(fileName);
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

                try {
                    ServletOutputStream out = response.getOutputStream();
                    ExportDocket ern = new ExportDocket();
                    ern.startExport(docket, out, xsltfile.getAbsolutePath());
                    out.flush();
                } catch (IOException e) {
                    logger.error("IOException while exporting run note", e);
                }

                facesContext.responseComplete();
            }
        }
        return null;
    }

    /**
     * The method deleteBatch() is called if the user clicks the action link to
     * delete batches. It runs the deletion of the batches.
     */
    public void deleteBatch() {
        int selectedBatchesSize = this.selectedBatches.size();
        if (selectedBatchesSize == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return;
        }
        List<Integer> ids = new ArrayList<Integer>(selectedBatchesSize);
        for (Integer entry : this.selectedBatches) {
            ids.add(entry);
        }
        try {
            serviceManager.getBatchService().removeAll(ids);
            filterAlleStart();
        } catch (DAOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
        }
    }

    /**
     * Add processes to Batch.
     */
    public void addProcessesToBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return;
        }
        if (this.selectedProcesses.size() == 0) {
            Helper.setFehlerMeldung("noProcessSelected");
            return;
        }
        try {
            for (Integer entry : this.selectedBatches) {
                Batch batch = serviceManager.getBatchService().find(entry);
                serviceManager.getBatchService().addAll(batch, this.selectedProcesses);
                serviceManager.getBatchService().save(batch);
                if (ConfigCore.getBooleanParameter("batches.logChangesToWikiField", false)) {
                    for (Process p : this.selectedProcesses) {
                        serviceManager.getProcessService().addToWikiField(Helper.getTranslation("addToBatch",
                                Arrays.asList(new String[] {serviceManager.getBatchService().getLabel(batch) })), p);
                    }
                    this.serviceManager.getProcessService().saveList(this.selectedProcesses);
                }
            }
        } catch (DAOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
        } catch (IOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("errorElasticSearch", e.getMessage());
        } catch (CustomResponseException e) {
            logger.error(e);
            Helper.setFehlerMeldung("ElasticSearch incorrect server response", e.getMessage());
        }
    }

    /**
     * Remove processes from Batch.
     */
    public void removeProcessesFromBatch() throws DAOException, IOException, CustomResponseException {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return;
        }
        if (this.selectedProcesses.size() == 0) {
            Helper.setFehlerMeldung("noProcessSelected");
            return;
        }

        for (Integer entry : this.selectedBatches) {
            Batch batch = serviceManager.getBatchService().find(entry);
            serviceManager.getBatchService().removeAll(batch, this.selectedProcesses);
            serviceManager.getBatchService().save(batch);
            if (ConfigCore.getBooleanParameter("batches.logChangesToWikiField", false)) {
                for (Process p : this.selectedProcesses) {
                    serviceManager.getProcessService()
                            .addToWikiField(
                                    Helper.getTranslation("removeFromBatch",
                                            Arrays.asList(
                                                    new String[] {serviceManager.getBatchService().getLabel(batch) })),
                                    p);
                }
                this.serviceManager.getProcessService().saveList(this.selectedProcesses);
            }
        }
        filterAlleStart();
    }

    /**
     * Rename Batch.
     */
    public void renameBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return;
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung("tooManyBatchesSelected");
            return;
        } else {
            try {
                Integer selected = selectedBatches.get(0);
                for (Batch batch : currentBatches) {
                    if (selected.equals(batch.getId())) {
                        batch.setTitle(batchTitle == null || batchTitle.trim().length() == 0 ? null : batchTitle);
                        serviceManager.getBatchService().save(batch);
                        return;
                    }
                }
            } catch (DAOException e) {
                Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
                logger.error(e);
            } catch (IOException e) {
                Helper.setFehlerMeldung("errorElasticSearch", e.getMessage());
                logger.error(e);
            } catch (CustomResponseException e) {
                logger.error(e);
                Helper.setFehlerMeldung("ElasticSearch incorrect server response", e.getMessage());
            }
        }
    }

    /**
     * Create new Batch.
     */
    public void createNewBatch() throws DAOException, IOException, CustomResponseException {
        if (selectedProcesses.size() > 0) {
            Batch batch = null;
            if (batchTitle != null && batchTitle.trim().length() > 0) {
                batch = new Batch(batchTitle.trim(), Type.LOGISTIC, selectedProcesses);
            } else {
                batch = new Batch(Type.LOGISTIC, selectedProcesses);
            }

            serviceManager.getBatchService().save(batch);
            if (ConfigCore.getBooleanParameter("batches.logChangesToWikiField", false)) {
                for (Process p : selectedProcesses) {
                    serviceManager.getProcessService()
                            .addToWikiField(
                                    Helper.getTranslation("addToBatch",
                                            Arrays.asList(
                                                    new String[] {serviceManager.getBatchService().getLabel(batch) })),
                                    p);
                }
                this.serviceManager.getProcessService().saveList(selectedProcesses);
            }
        }
        filterAlleStart();
    }

    /*
     * properties
     */
    private BatchProcessHelper batchHelper;

    /**
     * Edit properties.
     *
     * @return page
     */
    public String editProperties() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return null;
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung("tooManyBatchesSelected");
            return null;
        } else {
            if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("")
                    && !this.selectedBatches.get(0).equals("null")) {
                Batch batch;
                try {
                    batch = serviceManager.getBatchService().find(Integer.valueOf(selectedBatches.get(0)));
                    this.batchHelper = new BatchProcessHelper(batch);
                    return "/newpages/BatchProperties";
                } catch (DAOException e) {
                    logger.error(e);
                    Helper.setFehlerMeldung("fehlerBeimEinlesen");
                    return null;
                }
            } else {
                Helper.setFehlerMeldung("noBatchSelected");
                return null;
            }
        }
    }

    public BatchProcessHelper getBatchHelper() {
        return this.batchHelper;
    }

    public void setBatchHelper(BatchProcessHelper batchHelper) {
        this.batchHelper = batchHelper;
    }

    public String getModusBearbeiten() {
        return this.modusBearbeiten;
    }

    public void setModusBearbeiten(String modusBearbeiten) {
        this.modusBearbeiten = modusBearbeiten;
    }

    /**
     * Creates a batch export task to export the selected batch. The type of
     * export task depends on the batch type. If asynchronous tasks have been
     * created, the user will be redirected to the task manager page where it
     * can observe the task progressing.
     *
     * @return the next page to show as named in a &lt;from-outcome&gt; element
     *         in faces_config.xml
     */
    public String exportBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung("noBatchSelected");
            return null;
        }
        for (Integer batchID : selectedBatches) {
            try {
                Batch batch = serviceManager.getBatchService().find(batchID);
                switch (batch.getType()) {
                    case LOGISTIC:
                        for (Process prozess : batch.getProcesses()) {
                            Hibernate.initialize(prozess.getProject());
                            Hibernate.initialize(prozess.getProject().getProjectFileGroups());
                            Hibernate.initialize(prozess.getRuleset());
                            ExportDms dms = new ExportDms(
                                    ConfigCore.getBooleanParameter(Parameters.EXPORT_WITH_IMAGES, true));
                            dms.startExport(prozess);
                        }
                        return ConfigCore.getBooleanParameter("asynchronousAutomaticExport") ? "taskmanager" : null;
                    case NEWSPAPER:
                        TaskManager.addTask(new ExportNewspaperBatchTask(batch));
                        return "/newpages/taskmanager";
                    case SERIAL:
                        TaskManager.addTask(new ExportSerialBatchTask(batch));
                        return "/newpages/taskmanager";
                    default:
                        throw new UnreachableCodeException("Complete switch statement");
                }
            } catch (Exception e) {
                logger.error(e);
                Helper.setFehlerMeldung("fehlerBeimEinlesen");
                return null;
            }
        }
        Helper.setFehlerMeldung("noBatchSelected");
        return null;
    }

    /**
     * Sets the type of all currently selected batches to LOGISTIC.
     */
    public void setLogistic() {
        setType(Type.LOGISTIC);
    }

    /**
     * Sets the type of all currently selected batches to NEWSPAPER.
     */
    public void setNewspaper() {
        setType(Type.NEWSPAPER);
    }

    /**
     * Sets the type of all currently selected batches to SERIAL.
     */
    public void setSerial() {
        setType(Type.SERIAL);
    }

    /**
     * Sets the type of all currently selected batches to the named one,
     * overriding a previously set type, if any.
     *
     * @param type
     *            type to set
     */
    private void setType(Type type) {
        try {
            for (Batch batch : currentBatches) {
                if (selectedBatches.contains(batch.getId().toString())) {
                    batch.setType(type);
                    serviceManager.getBatchService().save(batch);
                }
            }
        } catch (DAOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen");
        } catch (IOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("errorElasticSearch");
        } catch (CustomResponseException e) {
            logger.error(e);
            Helper.setFehlerMeldung("ElasticSearch incorrect server response", e.getMessage());
        }
    }
}
