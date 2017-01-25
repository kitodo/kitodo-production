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

import de.sub.goobi.config.ConfigMain;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
import org.kitodo.production.exceptions.UnreachableCodeException;
import org.kitodo.services.BatchService;
import org.kitodo.services.ProcessService;

public class BatchForm extends BasisForm {

	private static final long serialVersionUID = 8234897225425856549L;

	private static final Logger logger = Logger.getLogger(BatchForm.class);

	private List<Process> currentProcesses;
	private List<Process> selectedProcesses;
	private List<Batch> currentBatches;
	private List<String> selectedBatches;
	private String batchfilter;
	private String processfilter;
	private IEvaluableFilter myFilteredDataSource;

	private BatchService batchService = new BatchService();
	private ProcessService processService = new ProcessService();
	private String modusBearbeiten = "";

	private String batchTitle;

	public List<Process> getCurrentProcesses() {
		return this.currentProcesses;
	}

	public void setCurrentProcesses(List<Process> currentProcesses) {
		this.currentProcesses = currentProcesses;
	}

	public void loadBatchData() throws DAOException {
		if (selectedProcesses == null || selectedProcesses.size() == 0) {
			this.currentBatches = batchService.findAll();
			this.selectedBatches = new ArrayList<String>();
		} else {
			selectedBatches = new ArrayList<String>();
			List<Batch> batchesToSelect = new ArrayList<>();
			for (Process process : selectedProcesses) {
				batchesToSelect.addAll(processService.getBatchesInitialized(process));
			}
			for (Batch batch : batchesToSelect) {
				selectedBatches.add(batchService.getIdString(batch));
			}
		}
	}

	public void loadProcessData() {
		List<Process> processes = new ArrayList<>();
		try {
			for (String b : selectedBatches) {
				processes.addAll(batchService.find(Integer.parseInt(b)).getProcesses());
			}
			currentProcesses = new ArrayList<Process>(processes);
		} catch (Exception e) { // NumberFormatException, DAOException
			logger.error(e);
			Helper.setFehlerMeldung("fehlerBeimEinlesen");
			return;
		}
	}

	@SuppressWarnings("unchecked")
	public void filterProcesses() {

		if (this.processfilter == null) {
			this.processfilter = "";
		}
		this.myFilteredDataSource = new UserDefinedFilter(this.processfilter);
		Criteria crit = this.myFilteredDataSource.getCriteria();
		crit.addOrder(Order.desc("erstellungsdatum"));
		crit.add(Restrictions.eq("istTemplate", Boolean.FALSE));
		int batchMaxSize = ConfigMain.getIntParameter(Parameters.BATCH_DISPLAY_LIMIT, -1);
		if (batchMaxSize > 0) {
			crit.setMaxResults(batchMaxSize);
		}
		try {
			this.currentProcesses = crit.list();
		} catch (HibernateException e) {
			this.currentProcesses = new ArrayList<Process>();
		}
	}

	public void filterBatches() throws DAOException {
		currentBatches = new ArrayList<Batch>();
		for (Batch batch : batchService.findAll()) {
			if (batchService.contains(batch, batchfilter)) {
				currentBatches.add(batch);
			}
		}
	}

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

	public List<String> getSelectedBatches() {
		return this.selectedBatches;
	}

	public void setSelectedBatches(List<String> selectedBatches) {
		this.selectedBatches = selectedBatches;
	}

	public String FilterAlleStart() throws DAOException {
		filterBatches();
		filterProcesses();
		return "BatchesAll";
	}

	public String downloadDocket() {
		logger.debug("generate docket for process list");
		String rootpath = ConfigMain.getParameter("xsltFolder");
		File xsltfile = new File(rootpath, "docket_multipage.xsl");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		List<Process> docket = Collections.emptyList();
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
		} else if (this.selectedBatches.size() == 1) {
			try {
				docket = batchService.find(Integer.valueOf(selectedBatches.get(0))).getProcesses();
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("fehlerBeimEinlesen");
				return "";
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
		return "";
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
		for (String entry : this.selectedBatches) {
			ids.add(Integer.parseInt(entry));
		}
		try {
			batchService.removeAll(ids);
			FilterAlleStart();
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
		}
	}

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
			for (String entry : this.selectedBatches) {
				Batch batch = batchService.find(Integer.parseInt(entry));
				batchService.addAll(batch, this.selectedProcesses);
				batchService.save(batch);
				if (ConfigMain.getBooleanParameter("batches.logChangesToWikiField", false)) {
					for (Process p : this.selectedProcesses) {
						processService.addToWikiField(Helper.getTranslation("addToBatch", Arrays.asList(new String[] { batchService.getLabel(batch) })), p);
					}
					this.processService.saveList(this.selectedProcesses);
				}
			}
			return;
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
		}
	}

	public void removeProcessesFromBatch() throws DAOException{
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return;
		}
		if (this.selectedProcesses.size() == 0) {
			Helper.setFehlerMeldung("noProcessSelected");
			return;
		}
		try {
			for (String entry : this.selectedBatches) {
				Batch batch = batchService.find(Integer.parseInt(entry));
				batchService.removeAll(batch, this.selectedProcesses);
				batchService.save(batch);
				if (ConfigMain.getBooleanParameter("batches.logChangesToWikiField", false)) {
					for (Process p : this.selectedProcesses) {
						processService.addToWikiField(
								Helper.getTranslation("removeFromBatch",
										Arrays.asList(new String[] { batchService.getLabel(batch) })), p);
					}
					this.processService.saveList(this.selectedProcesses);
				}
			}
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
			return;
		}
		FilterAlleStart();
	}

	public void renameBatch() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return;
		} else if (this.selectedBatches.size() > 1) {
			Helper.setFehlerMeldung("tooManyBatchesSelected");
			return;
		} else {
			try {
				Integer selected = Integer.valueOf(selectedBatches.get(0));
				for (Batch batch : currentBatches) {
					if (selected.equals(batch.getId())) {
						batch.setTitle(batchTitle == null || batchTitle.trim().length() == 0 ? null : batchTitle);
						batchService.save(batch);
						return;
					}
				}
			} catch (DAOException e) {
				Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
				logger.error(e);
				return;
			}
		}
	}

	public void createNewBatch() throws DAOException {
		if (selectedProcesses.size() > 0) {
			Batch batch = null;
			if(batchTitle != null && batchTitle.trim().length() > 0){
				batch = new Batch(batchTitle.trim(), Type.LOGISTIC, selectedProcesses);
			}else{
				batch = new Batch(Type.LOGISTIC, selectedProcesses);
			}
			try {
				batchService.save(batch);
				if (ConfigMain.getBooleanParameter("batches.logChangesToWikiField", false)) {
					for (Process p : selectedProcesses) {
						processService.addToWikiField(Helper.getTranslation("addToBatch", Arrays.asList(new String[] { batchService.getLabel(batch) })), p);
					}
					this.processService.saveList(selectedProcesses);
				}
			} catch (DAOException e) {
				Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
				logger.error(e);
				return;
			}
		}
		FilterAlleStart();
	}

	/*
	 * properties
	 */
	private BatchProcessHelper batchHelper;

	public String editProperties() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return "";
		} else if (this.selectedBatches.size() > 1) {
			Helper.setFehlerMeldung("tooManyBatchesSelected");
			return "";
		} else {
			if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("") && !this.selectedBatches.get(0).equals("null")) {
				Batch batch;
				try {
					batch = batchService.find(Integer.valueOf(selectedBatches.get(0)));
					this.batchHelper = new BatchProcessHelper(batch);
					return "BatchProperties";
				} catch (DAOException e) {
					logger.error(e);
					Helper.setFehlerMeldung("fehlerBeimEinlesen");
					return "";
				}
			} else {
				Helper.setFehlerMeldung("noBatchSelected");
				return "";
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
			return "";
		}
		for (String batchID : selectedBatches) {
			try {
				Batch batch = batchService.find(Integer.valueOf(batchID));
				switch (batch.getType()) {
				case LOGISTIC:
					for (Process prozess : batch.getProcesses()) {
						Hibernate.initialize(prozess.getProject());
						Hibernate.initialize(prozess.getProject().getProjectFileGroups());
						Hibernate.initialize(prozess.getRuleset());
						ExportDms dms = new ExportDms(ConfigMain.getBooleanParameter(Parameters.EXPORT_WITH_IMAGES,
								true));
						dms.startExport(prozess);
					}
					return ConfigMain.getBooleanParameter("asynchronousAutomaticExport") ? "taskmanager" : "";
				case NEWSPAPER:
					TaskManager.addTask(new ExportNewspaperBatchTask(batch));
					return "taskmanager";
				case SERIAL:
					TaskManager.addTask(new ExportSerialBatchTask(batch));
					return "taskmanager";
				default:
					throw new UnreachableCodeException("Complete switch statement");
				}
			} catch (Exception e) {
				logger.error(e);
				Helper.setFehlerMeldung("fehlerBeimEinlesen");
				return "";
			}
		}
		Helper.setFehlerMeldung("noBatchSelected");
		return "";
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
					batchService.save(batch);
				}
			}
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("fehlerBeimEinlesen");
		}
	}
}
